package com.ghostipedia.cosmiccore.common.machine.multiblock.part;

import com.ghostipedia.cosmiccore.common.wireless.WirelessComputationStore;
import com.ghostipedia.cosmiccore.mixin.accessor.RecipeLogicAccessor;
import com.ghostipedia.cosmiccore.utils.OwnershipUtils;

import com.gregtechceu.gtceu.api.capability.IOpticalComputationHatch;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationProvider;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationReceiver;
import com.gregtechceu.gtceu.api.capability.recipe.CWURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;

import com.lowdragmc.lowdraglib.gui.widget.*;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Wireless computation hatch that routes computation requests to WirelessComputationStore.
 * Acts as a drop-in replacement for wired computation hatches in consumer multiblocks.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class WirelessComputationHatchMachine extends MultiblockPartMachine {

    protected final WirelessComputationContainer computationContainer;

    public WirelessComputationHatchMachine(IMachineBlockEntity holder) {
        super(holder);
        this.computationContainer = new WirelessComputationContainer(this);
    }

    protected UUID getTeamUUID() {
        var owner = getOwner();
        if (owner == null) {
            return getOwnerUUID();
        }
        if (owner instanceof FTBOwner ftbOwner) {
            var team = ftbOwner.getPlayerTeam(getOwnerUUID());
            return team != null ? team.getTeamId() : getOwnerUUID();
        }
        return getOwnerUUID();
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return true;
    }

    @Override
    public boolean canShared() {
        return false;
    }

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 182 + 8, 117 + 8);
        group.addWidget(new DraggableScrollableWidgetGroup(4, 4, 182, 117).setBackground(GuiTextures.DISPLAY)
                .addWidget(new LabelWidget(4, 5, self().getBlockState().getBlock().getDescriptionId()))
                .addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText)
                        .textSupplier(this.getLevel().isClientSide ? null : this::addDisplayText)
                        .setMaxWidthLimit(200)));
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    public void addDisplayText(List<Component> textList) {
        var store = WirelessComputationStore.getStore(getTeamUUID());
        int maxCWU = store.getMaxCWUt();
        int providerCount = store.getProviderCount();

        MultiblockDisplayText.builder(textList, isFormed())
                .addCustom(list -> {
                    list.add(Component.translatable("cosmiccore.machine.wireless_computation_hatch.providers", providerCount));
                    list.add(Component.translatable("cosmiccore.machine.wireless_computation_hatch.max_cwu", maxCWU));
                })
                .addCustom(list -> OwnershipUtils.addOwnerLine(list, getOwner(), true));
    }

    /**
     * Custom computation container that routes requests to WirelessComputationStore.
     */
    protected class WirelessComputationContainer extends NotifiableRecipeHandlerTrait<Integer>
            implements IOpticalComputationHatch, IOpticalComputationReceiver {

        @Getter
        protected final IO handlerIO = IO.IN;

        protected long lastTimeStamp;
        private int currentOutputCwu = 0, lastOutputCwu = 0;

        public WirelessComputationContainer(WirelessComputationHatchMachine machine) {
            super(machine);
            this.lastTimeStamp = Long.MIN_VALUE;
        }

        @Override
        public boolean isTransmitter() {
            return false; // Acts as receiver - receives computation from wireless network
        }

        @Override
        public int requestCWUt(int cwut, boolean simulate, @NotNull Collection<IOpticalComputationProvider> seen) {
            var latestTimeStamp = getMachine().getOffsetTimer();
            if (lastTimeStamp < latestTimeStamp) {
                lastOutputCwu = currentOutputCwu;
                currentOutputCwu = 0;
                lastTimeStamp = latestTimeStamp;
            }

            seen.add(this);

            // Route to wireless computation store
            var store = WirelessComputationStore.getStore(getTeamUUID());
            return store.requestCWUt(cwut, simulate, seen);
        }

        @Override
        public int getMaxCWUt(@NotNull Collection<IOpticalComputationProvider> seen) {
            seen.add(this);
            var store = WirelessComputationStore.getStore(getTeamUUID());
            return store.getMaxCWUt(seen);
        }

        @Override
        public boolean canBridge(@NotNull Collection<IOpticalComputationProvider> seen) {
            seen.add(this);
            var store = WirelessComputationStore.getStore(getTeamUUID());
            return store.canBridge(seen);
        }

        @Override
        public List<Integer> handleRecipeInner(IO io, GTRecipe recipe, List<Integer> left, boolean simulate) {
            var teamUUID = getTeamUUID();
            var store = WirelessComputationStore.getStore(teamUUID);

            int sum = left.stream().mapToInt(Integer::intValue).sum();
            if (!simulate) {
                com.ghostipedia.cosmiccore.CosmicCore.LOGGER.info("handleRecipeInner: io={}, sum={}, teamUUID={}, providers={}, maxCWU={}",
                    io, sum, teamUUID, store.getProviderCount(), store.getMaxCWUt());
            }
            if (io == IO.IN) {
                int availableCWUt = store.requestCWUt(Integer.MAX_VALUE, true);
                if (availableCWUt >= sum) {
                    if (recipe.data.getBoolean("duration_is_total_cwu")) {
                        int drawn = store.requestCWUt(availableCWUt, simulate);
                        if (!simulate) {
                            if (machine instanceof IRecipeLogicMachine rlm) {
                                RecipeLogic logic = rlm.getRecipeLogic();
                                RecipeLogicAccessor accessor = (RecipeLogicAccessor) logic;
                                accessor.setProgress(accessor.getProgress() - 1 + drawn);
                            } else if (machine instanceof IMultiPart multiPart) {
                                for (IMultiController controller : multiPart.getControllers()) {
                                    if (controller instanceof IRecipeLogicMachine rlm) {
                                        RecipeLogic logic = rlm.getRecipeLogic();
                                        RecipeLogicAccessor accessor = (RecipeLogicAccessor) logic;
                                        accessor.setProgress(accessor.getProgress() - 1 + drawn);
                                    }
                                }
                            }
                        }
                        sum -= drawn;
                    } else {
                        sum -= store.requestCWUt(sum, simulate);
                    }
                }
            } else if (io == IO.OUT) {
                int canInput = this.getMaxCWUt() - this.lastOutputCwu;
                if (!simulate) {
                    this.currentOutputCwu = Math.min(canInput, sum);
                }
                sum = sum - canInput;
            }
            return sum <= 0 ? null : Collections.singletonList(sum);
        }

        @Override
        public @NotNull List<Object> getContents() {
            return List.of(lastOutputCwu);
        }

        @Override
        public double getTotalContentAmount() {
            return lastOutputCwu;
        }

        @Override
        public RecipeCapability<Integer> getCapability() {
            return CWURecipeCapability.CAP;
        }

        @Nullable
        @Override
        public IOpticalComputationProvider getComputationProvider() {
            return WirelessComputationStore.getStore(getTeamUUID());
        }
    }
}
