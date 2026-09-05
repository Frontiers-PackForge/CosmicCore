package com.ghostipedia.cosmiccore.common.machine.transmission;

import com.ghostipedia.cosmiccore.common.transmission.energy.LoadedPowerTowerTerminal;
import com.ghostipedia.cosmiccore.common.transmission.energy.PowerTowerEnergyRouter;
import com.ghostipedia.cosmiccore.common.transmission.graph.PowerTowerGraph;
import com.ghostipedia.cosmiccore.common.transmission.graph.PowerTowerNode;
import com.ghostipedia.cosmiccore.common.transmission.graph.PowerTowerRole;
import com.ghostipedia.cosmiccore.common.transmission.graph.PowerTowerSavedData;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.multiblock.error.PatternStringError;
import com.gregtechceu.gtceu.api.multiblock.pattern.BlockPattern;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.utils.ISubscription;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PowerTowerMachine extends MultiblockControllerMachine {

    private final ConditionalSubscriptionHandler energyTransferSubscription;
    private final Set<BlockPos> formedStructurePositions = new HashSet<>();
    private final List<ISubscription> hatchEnergySubscriptions = new ArrayList<>();
    private @Nullable UUID graphNodeId;
    private @Nullable LoadedPowerTowerTerminal loadedTerminal;
    private boolean unloadingChunk;
    private boolean brokenStructureValidationScheduled;

    public PowerTowerMachine(BlockEntityCreationInfo info) {
        super(info);
        energyTransferSubscription = new ConditionalSubscriptionHandler(this, this::transferEnergyTick,
                this::hasTransferWork);
    }

    @Override
    public void onLoad() {
        unloadingChunk = false;
        super.onLoad();
    }

    @Override
    public void onUnload() {
        unloadingChunk = true;
        // Chunk unload removes the live hatch endpoint but preserves the graph.
        unregisterLoadedTerminal();
        clearHatchEnergySubscriptions();
        energyTransferSubscription.unsubscribe();
        super.onUnload();
    }

    @Override
    public void formStructure(@NotNull String substructureName) {
        super.formStructure(substructureName);
        if (!isFormed()) return;
        if (!(getLevel() instanceof ServerLevel serverLevel)) return;
        captureFormedStructurePositions();
        EnergyHatches hatches = collectAndSubscribeEnergyHatches();
        Integer terminalVoltageTier = resolveExactTerminalVoltageTier(hatches);
        if ((!hatches.inputHatches().isEmpty() || !hatches.outputHatches().isEmpty()) &&
                terminalVoltageTier == null) {
            getPatternState(substructureName).setError(new PatternStringError(
                    Component.translatable("cosmiccore.multiblock.power_tower.mixed_voltage")));
            invalidateStructure(substructureName);
            return;
        }
        PowerTowerRole role = terminalVoltageTier == null ? PowerTowerRole.DUMMY : PowerTowerRole.TERMINAL;
        int voltageTier = terminalVoltageTier == null ? -1 : terminalVoltageTier;
        PowerTowerSavedData data = PowerTowerSavedData.getOrCreate(serverLevel);
        PowerTowerGraph graph = data.graph();
        PowerTowerNode node = graph.nodeAtController(getBlockPos());
        try {
            if (node == null) {
                graphNodeId = graph.addNode(getBlockPos(), wireAttachmentCenter(), role, resolveGraphOwner(),
                        voltageTier, wireAttachmentPoints());
                data.markGraphDirty();
            } else {
                graphNodeId = node.id();
                if (graph.updateNode(node.id(), wireAttachmentCenter(), role, resolveGraphOwner(), voltageTier,
                        wireAttachmentPoints())) {
                    data.markGraphDirty();
                }
            }
        } catch (IllegalArgumentException exception) {
            getPatternState(substructureName).setError(new PatternStringError(
                    Component.translatable("cosmiccore.multiblock.power_tower.graph_conflict")));
            invalidateStructure(substructureName);
            return;
        }
        if (role == PowerTowerRole.TERMINAL) {
            loadedTerminal = new LoadedPowerTowerTerminal(graphNodeId, voltageTier, hatches.inputHatches(),
                    hatches.outputHatches());
            data.loadedTerminals().register(graph, loadedTerminal,
                    energyTransferSubscription::updateSubscription);
        }
        data.loadedTerminals().wakeComponentTerminals(graph, graphNodeId);
        brokenStructureValidationScheduled = false;
        energyTransferSubscription.updateSubscription();
    }

    @Override
    public void invalidateStructure(String substructureName) {
        unregisterLoadedTerminal();
        clearHatchEnergySubscriptions();
        energyTransferSubscription.unsubscribe();
        super.invalidateStructure(substructureName);
        if (!unloadingChunk && DEFAULT_STRUCTURE.equals(substructureName)) scheduleBrokenStructureValidation();
    }

    @Override
    public void onMachineDestroyed() {
        unregisterLoadedTerminal();
        if (getLevel() instanceof ServerLevel serverLevel) {
            PowerTowerSavedData data = PowerTowerSavedData.getOrCreate(serverLevel);
            PowerTowerNode node = data.graph().nodeAtController(getBlockPos());
            if (node != null && data.graph().removeNode(node.id())) data.markGraphDirty();
        }
        super.onMachineDestroyed();
    }

    public @Nullable UUID getGraphNodeId() {
        PowerTowerNode node = getLevel() instanceof ServerLevel serverLevel ?
                PowerTowerSavedData.getOrCreate(serverLevel).graph().nodeAtController(getBlockPos()) : null;
        return node == null ? graphNodeId : node.id();
    }

    public boolean containsFormedStructurePosition(BlockPos pos) {
        return formedStructurePositions.contains(pos);
    }

    protected Vec3 wireAttachmentCenter() {
        var points = wireAttachmentPoints();
        return points.stream().reduce(Vec3.ZERO, Vec3::add).scale(1.0 / points.size());
    }

    private List<Vec3> wireAttachmentPoints() {
        if (!(getSubstructurePattern(DEFAULT_STRUCTURE) instanceof BlockPattern pattern))
            throw new IllegalStateException("Power Tower requires an authored block pattern");
        return com.ghostipedia.cosmiccore.common.transmission.geometry.PowerTowerAttachments.resolve(pattern,
                getBlockPos(), getFrontFacing(), getUpwardsFacing(), isFlipped());
    }

    private EnergyHatches collectAndSubscribeEnergyHatches() {
        List<IEnergyContainer> inputHatches = new ArrayList<>();
        List<IEnergyContainer> outputHatches = new ArrayList<>();
        getParts().stream().sorted(Comparator.comparing(MultiblockPartMachine::getBlockPos)).forEach(part -> {
            for (var handler : part.getRecipeHandlers()) {
                List<IEnergyContainer> containers = handler.getCapability(EURecipeCapability.CAP).stream()
                        .filter(IEnergyContainer.class::isInstance)
                        .map(IEnergyContainer.class::cast)
                        .toList();
                if (handler.getHandlerIO().support(IO.IN)) inputHatches.addAll(containers);
                if (handler.getHandlerIO().support(IO.OUT)) outputHatches.addAll(containers);
                if (!containers.isEmpty()) {
                    hatchEnergySubscriptions.add(handler.subscribe(this::onTerminalHatchEnergyChanged,
                            EURecipeCapability.CAP));
                }
            }
        });
        return new EnergyHatches(List.copyOf(inputHatches), List.copyOf(outputHatches));
    }

    private @Nullable Integer resolveExactTerminalVoltageTier(EnergyHatches hatches) {
        Set<Integer> tiers = new HashSet<>();
        for (IEnergyContainer input : hatches.inputHatches()) addExactVoltageTier(tiers, input.getInputVoltage());
        for (IEnergyContainer output : hatches.outputHatches()) {
            addExactVoltageTier(tiers, output.getOutputVoltage());
        }
        return tiers.size() == 1 && !tiers.contains(-1) ? tiers.iterator().next() : null;
    }

    private static void addExactVoltageTier(Set<Integer> tiers, long voltage) {
        int tier = GTUtil.getTierByVoltage(voltage);
        if (tier < 0 || tier >= GTValues.V.length || GTValues.V[tier] != voltage) {
            tiers.add(-1);
        } else {
            tiers.add(tier);
        }
    }

    private boolean hasTransferWork() {
        if (!isFormed() || loadedTerminal == null || graphNodeId == null ||
                !(getLevel() instanceof ServerLevel serverLevel))
            return false;
        PowerTowerSavedData data = PowerTowerSavedData.getOrCreate(serverLevel);
        return PowerTowerEnergyRouter.hasTransferWork(data.graph(), data.loadedTerminals(), graphNodeId);
    }

    private void transferEnergyTick() {
        if (graphNodeId != null && getLevel() instanceof ServerLevel serverLevel) {
            PowerTowerSavedData data = PowerTowerSavedData.getOrCreate(serverLevel);
            PowerTowerEnergyRouter.transferFrom(data.graph(), data.loadedTerminals(), graphNodeId);
        }
        energyTransferSubscription.updateSubscription();
    }

    private void onTerminalHatchEnergyChanged() {
        if (graphNodeId != null && getLevel() instanceof ServerLevel serverLevel) {
            PowerTowerSavedData data = PowerTowerSavedData.getOrCreate(serverLevel);
            data.loadedTerminals().wakeComponentTerminals(data.graph(), graphNodeId);
        }
        energyTransferSubscription.updateSubscription();
    }

    private void unregisterLoadedTerminal() {
        if (loadedTerminal != null && graphNodeId != null && getLevel() instanceof ServerLevel serverLevel) {
            PowerTowerSavedData data = PowerTowerSavedData.getOrCreate(serverLevel);
            data.loadedTerminals().unregister(data.graph(), graphNodeId, loadedTerminal);
        }
        loadedTerminal = null;
        graphNodeId = null;
    }

    private void clearHatchEnergySubscriptions() {
        hatchEnergySubscriptions.forEach(ISubscription::unsubscribe);
        hatchEnergySubscriptions.clear();
    }

    private void captureFormedStructurePositions() {
        formedStructurePositions.clear();
        for (long packedPos : getDefaultPatternState().getCache().keySet()) {
            formedStructurePositions.add(BlockPos.of(packedPos));
        }
        formedStructurePositions.add(getBlockPos().immutable());
    }

    private void scheduleBrokenStructureValidation() {
        if (brokenStructureValidationScheduled) return;
        brokenStructureValidationScheduled = true;
        scheduleForNextServerTick(() -> {
            brokenStructureValidationScheduled = false;
            if (isRemoved() || isFormed() || !(getLevel() instanceof ServerLevel serverLevel)) return;
            // GTM also invalidates during unload, so only a fully loaded broken structure may disable its node.
            if (formedStructurePositions.stream().anyMatch(pos -> !serverLevel.isLoaded(pos))) return;
            PowerTowerSavedData data = PowerTowerSavedData.getOrCreate(serverLevel);
            PowerTowerNode node = data.graph().nodeAtController(getBlockPos());
            if (node != null && data.graph().setStructureOperational(node.id(), false)) {
                data.markGraphDirty();
                data.loadedTerminals().wakeComponentTerminals(data.graph(), node.id());
            }
        });
    }

    private @Nullable UUID resolveGraphOwner() {
        UUID owner = getOwnerUUID();
        if (owner == null) return null;
        if (getOwner() instanceof FTBOwner ftbOwner) {
            var team = ftbOwner.getPlayerTeam(owner);
            if (team != null) return team.getTeamId();
        }
        return owner;
    }

    private record EnergyHatches(List<IEnergyContainer> inputHatches, List<IEnergyContainer> outputHatches) {}
}
