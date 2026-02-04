package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.bee;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import forestry.api.core.HumidityType;
import forestry.api.core.TemperatureType;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MechanicalAlvearyMachine extends WorkableElectricMultiblockMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MechanicalAlvearyMachine.class, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    @DescSynced
    @Getter
    private int tier = GTValues.LV;

    @Persisted
    @DescSynced
    @Getter
    private int maxQueens = 1;

    @Persisted
    @DescSynced
    @Getter
    private int activeQueenCount = 0;

    @Nullable
    @Getter
    private AlvearyModifierComposite modifierComposite;

    @Nullable
    @Getter
    private AlvearyClimateState climateState;

    @Getter
    private final List<AlvearyQueenThread> queenThreads = new ArrayList<>();

    @Nullable
    private TickableSubscription threadTickSubscription;

    @Persisted
    private int[] savedColors = new int[0];
    @Persisted
    private int[] savedWorkCounters = new int[0];
    @Persisted
    private int[] savedBreedCounters = new int[0];

    public MechanicalAlvearyMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    @NotNull
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        return new RecipeLogic(this) {

            @Override
            public void serverTick() {}
        };
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();

        var tierObj = getMultiblockState().getMatchContext().get("AlvearyTier");
        if (tierObj instanceof Integer t) {
            this.tier = t;
            this.maxQueens = t - GTValues.LV + 1;
        }

        modifierComposite = AlvearyModifierComposite.fromParts(getParts());

        var level = getLevel();
        if (level != null) {
            climateState = AlvearyClimateState.create(level, getPos(), modifierComposite);
        }

        createQueenThreads();
        updateThreadSubscription();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        saveThreadProgress();
        activeQueenCount = 0;
        modifierComposite = null;
        climateState = null;
        queenThreads.clear();

        if (threadTickSubscription != null) {
            threadTickSubscription.unsubscribe();
            threadTickSubscription = null;
        }
    }

    private void saveThreadProgress() {
        int size = queenThreads.size();
        savedColors = new int[size];
        savedWorkCounters = new int[size];
        savedBreedCounters = new int[size];
        for (int i = 0; i < size; i++) {
            AlvearyQueenThread thread = queenThreads.get(i);
            savedColors[i] = thread.getColor();
            savedWorkCounters[i] = thread.getWorkCounter();
            savedBreedCounters[i] = thread.getBreedCounter();
        }
    }

    private void restoreThreadProgress() {
        for (AlvearyQueenThread thread : queenThreads) {
            int color = thread.getColor();
            for (int i = 0; i < savedColors.length; i++) {
                if (savedColors[i] == color) {
                    thread.setWorkCounter(savedWorkCounters[i]);
                    thread.setBreedCounter(savedBreedCounters[i]);
                    break;
                }
            }
        }
    }

    /**
     * Partition I/O handlers by paint color and create one AlvearyQueenThread per matched pair,
     * up to maxQueens. Both input AND output are color-isolated (diverges from MultithreadedMachine).
     */
    private void createQueenThreads() {
        queenThreads.clear();

        Int2ObjectMap<List<RecipeHandlerList>> inputsByColor = new Int2ObjectLinkedOpenHashMap<>();
        Int2ObjectMap<List<RecipeHandlerList>> outputsByColor = new Int2ObjectLinkedOpenHashMap<>();

        for (IMultiPart part : getParts()) {
            if (!(part instanceof ItemBusPartMachine)) continue;

            for (RecipeHandlerList handlerList : part.getRecipeHandlers()) {
                IO io = handlerList.getHandlerIO();
                if (io == IO.NONE) continue;

                boolean hasItem = handlerList.hasCapability(ItemRecipeCapability.CAP);
                if (!hasItem) continue;

                int color = handlerList.getColor();

                if (io == IO.IN || io == IO.BOTH) {
                    inputsByColor.computeIfAbsent(color, k -> new ArrayList<>()).add(handlerList);
                }
                if (io == IO.OUT || io == IO.BOTH) {
                    outputsByColor.computeIfAbsent(color, k -> new ArrayList<>()).add(handlerList);
                }
            }
        }

        int threadIndex = 0;
        for (Int2ObjectMap.Entry<List<RecipeHandlerList>> entry : inputsByColor.int2ObjectEntrySet()) {
            if (threadIndex >= maxQueens) break;

            int color = entry.getIntKey();
            List<RecipeHandlerList> inputs = entry.getValue();
            List<RecipeHandlerList> outputs = outputsByColor.getOrDefault(color, List.of());

            if (outputs.isEmpty()) continue;

            queenThreads.add(new AlvearyQueenThread(this, threadIndex, color, inputs, outputs));
            threadIndex++;
        }

        restoreThreadProgress();
    }

    private void updateThreadSubscription() {
        if (isFormed() && !queenThreads.isEmpty()) {
            threadTickSubscription = subscribeServerTick(threadTickSubscription, this::tickQueenThreads);
        } else if (threadTickSubscription != null) {
            threadTickSubscription.unsubscribe();
            threadTickSubscription = null;
        }
    }

    private void tickQueenThreads() {
        if (!isFormed() || !isWorkingEnabled()) return;

        if (hasMaintenanceProblems()) return;

        var energyContainer = getEnergyContainer();
        if (energyContainer == null) return;

        int active = 0;
        for (AlvearyQueenThread thread : List.copyOf(queenThreads)) {
            long euPerTick = GTValues.VA[tier];
            if (energyContainer.getEnergyStored() < euPerTick) break;

            thread.tick();
            if (thread.isHasActiveQueen()) {
                energyContainer.removeEnergy(euPerTick);
                active++;
            }
        }
        this.activeQueenCount = active;
    }

    private boolean hasMaintenanceProblems() {
        for (IMultiPart part : getParts()) {
            if (part instanceof IMaintenanceMachine maintenance) {
                return maintenance.hasMaintenanceProblems();
            }
        }
        return false;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        MultiblockDisplayText.builder(textList, isFormed())
                .addCustom(l -> {
                    l.add(Component.literal("Tier: ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(GTValues.VNF[tier])
                                    .withStyle(ChatFormatting.WHITE)));
                    l.add(Component.literal("Max Queens: ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(String.valueOf(maxQueens))
                                    .withStyle(ChatFormatting.GOLD)));
                    l.add(Component.literal("Threads: ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(queenThreads.size() + "/" + maxQueens)
                                    .withStyle(queenThreads.isEmpty() ? ChatFormatting.RED : ChatFormatting.GREEN)));

                    if (modifierComposite != null && modifierComposite.getTotalModifierCount() > 0) {
                        l.add(Component.literal("Modifier Casings: ")
                                .withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(
                                        String.valueOf(modifierComposite.getTotalModifierCount()))
                                        .withStyle(ChatFormatting.AQUA)));
                    }

                    if (climateState != null) {
                        var baseTemp = climateState.getBaseTemperature();
                        var effectiveTemp = climateState.getEffectiveTemperature();
                        var baseHumid = climateState.getBaseHumidity();
                        var effectiveHumid = climateState.getEffectiveHumidity();

                        var tempText = Component.literal("Temp: ")
                                .withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(effectiveTemp.name())
                                        .withStyle(temperatureColor(effectiveTemp)));
                        if (baseTemp != effectiveTemp) {
                            tempText.append(Component.literal(" (" + baseTemp.name() + ")")
                                    .withStyle(ChatFormatting.DARK_GRAY));
                        }
                        l.add(tempText);

                        var humidText = Component.literal("Humidity: ")
                                .withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(effectiveHumid.name())
                                        .withStyle(humidityColor(effectiveHumid)));
                        if (baseHumid != effectiveHumid) {
                            humidText.append(Component.literal(" (" + baseHumid.name() + ")")
                                    .withStyle(ChatFormatting.DARK_GRAY));
                        }
                        l.add(humidText);
                    }

                    if (activeQueenCount > 0) {
                        l.add(Component.literal("Active Queens: ")
                                .withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(String.valueOf(activeQueenCount))
                                        .withStyle(ChatFormatting.GOLD)));
                    }

                    if (!queenThreads.isEmpty()) {
                        l.add(Component.literal("--- Threads ---")
                                .withStyle(ChatFormatting.DARK_GRAY));
                        for (AlvearyQueenThread thread : queenThreads) {
                            String status = thread.getStatusText();
                            String species = thread.getActiveSpeciesName();
                            var line = Component.literal("  [" + thread.getColorName() + "] ")
                                    .withStyle(ChatFormatting.YELLOW);
                            if (species != null) {
                                line.append(Component.literal(species + " ")
                                        .withStyle(ChatFormatting.WHITE));
                            }
                            ChatFormatting statusColor = switch (status) {
                                case "Working" -> ChatFormatting.GREEN;
                                case "Breeding" -> ChatFormatting.LIGHT_PURPLE;
                                case "Waiting for Drone" -> ChatFormatting.GOLD;
                                default -> ChatFormatting.GRAY;
                            };
                            line.append(Component.literal(status).withStyle(statusColor));

                            int progress = thread.getProgressPercent();
                            if (progress > 0) {
                                line.append(Component.literal(" " + progress + "%")
                                        .withStyle(ChatFormatting.AQUA));
                            }
                            l.add(line);

                            if (thread.isHasActiveQueen()) {
                                int lifecycle = thread.getLifecyclePercent();
                                int hp = thread.getQueenHealth();
                                int maxHp = thread.getQueenMaxHealth();
                                l.add(Component.literal("    Life: " + hp + "/" + maxHp +
                                        " (" + lifecycle + "% aged)")
                                        .withStyle(ChatFormatting.DARK_GRAY));
                            }
                        }
                    }
                });
    }

    private static ChatFormatting temperatureColor(TemperatureType temp) {
        return switch (temp) {
            case ICY -> ChatFormatting.AQUA;
            case COLD -> ChatFormatting.BLUE;
            case NORMAL -> ChatFormatting.GREEN;
            case WARM -> ChatFormatting.YELLOW;
            case HOT -> ChatFormatting.RED;
            case HELLISH -> ChatFormatting.DARK_RED;
        };
    }

    private static ChatFormatting humidityColor(HumidityType humid) {
        return switch (humid) {
            case ARID -> ChatFormatting.YELLOW;
            case NORMAL -> ChatFormatting.GREEN;
            case DAMP -> ChatFormatting.DARK_AQUA;
        };
    }
}
