package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.DropSaved;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HelixFusionMachine extends WorkableElectricMultiblockMachine implements IDisplayUIMachine {

    @Persisted
    @DescSynced
    @DropSaved
    @Getter
    private long EUSpent = 0L;

    @Persisted
    @DescSynced
    @DropSaved
    @Getter
    private int reactorTier = 3;  // To 10, 7 Upgrades

    @Persisted
    @DescSynced
    @DropSaved
    private boolean canUpgrade = false;

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(HelixFusionMachine.class,
            WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Override
    @NotNull
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public HelixFusionMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    long baseUpgrade = 1_000_000_000L;
    long cost = 1_000_000_000L;

    public void attemptUpgrade() {
        long upgradeStep = clampLong(this.reactorTier * 1_000_000_000L, 1_000_000_000L, 100_000_000_000_000L);
        cost = baseUpgrade + upgradeStep;
        if (EUSpent >= cost) {
            if (reactorTier == 10) {
                return;
            }
            EUSpent -= cost;
            reactorTier++;
        }
    }

    @Override // IDEK if this does anything
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(330, 208, this, entityPlayer).widget(new FancyMachineUIWidget(this, 300, 208));
    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        return new HelixFusionRecipeLogic(this);
    }

    public void increaseEUConsumed(long EUSpent) {
        this.EUSpent += EUSpent;
    }

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 220 + 8, 160 + 8);
        group.addWidget(new DraggableScrollableWidgetGroup(4, 4, 220, 160).setBackground(getScreenTexture())
                .addWidget(new LabelWidget(4, 5, self().getBlockState().getBlock().getDescriptionId()))
                .addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText)
                        .textSupplier(this.getLevel().isClientSide ? null : this::addDisplayText)
                        .setMaxWidthLimit(200)
                        .clickHandler(this::handleDisplayClick)));
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(new ButtonWidget(
                9,
                142,
                210,
                20,
                new GuiTextureGroup(
                        GuiTextures.BUTTON,
                        new TextTexture("Upgrade Reactor Tier")),
                clickData -> attemptUpgrade()));
        return group;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        int numParallels;
        int batchParallels;
        boolean exact;
        if (recipeLogic.isActive() && recipeLogic.getLastRecipe() != null) {
            numParallels = recipeLogic.getLastRecipe().parallels;
            batchParallels = recipeLogic.getLastRecipe().batchParallels;
            exact = true;
        } else {
            exact = false;
            numParallels = getParallelHatch()
                    .map(c -> c.getCurrentParallel() * 64 * reactorTier)
                    .orElse(0);
            batchParallels = 0;
        }
        var builder = MultiblockDisplayText.builder(textList, isFormed())
                .setWorkingStatus(recipeLogic.isWorkingEnabled(), recipeLogic.isActive())
                .addEnergyUsageLine(energyContainer)
                .addEnergyTierLine(tier)
                .addMachineModeLine(getRecipeType(), getRecipeTypes().length > 1);
        builder.addCustom((components) -> {
            if (numParallels > 1) {
                Component parallels = Component.literal(FormattingUtil.formatNumbers(numParallels))
                        .withStyle(ChatFormatting.GOLD);
                String key = "cosmic.multiblock.parallel_fixed_64";
                if (exact) key += ".exact";
                textList.add(Component.translatable(key, parallels)
                        .withStyle(ChatFormatting.GRAY));
            }
        });
        builder.addCustom((components) -> {
            // TODO: TRACK AND DISPLAY ORVEX APPROPRIATELY
            textList.add(Component.translatable("cosmic.multiblock.orvex_tier", Component
                    .literal(FormattingUtil.formatNumberReadable(this.reactorTier)).withStyle(ChatFormatting.GOLD)));
            textList.add(Component.translatable("cosmic.multiblock.orvex_count", Component
                    .literal(FormattingUtil.formatNumberReadable(this.EUSpent)).withStyle(ChatFormatting.AQUA)));
            textList.add(Component.translatable("cosmic.multiblock.orvex_upgrade_requires",
                    Component.literal(FormattingUtil.formatNumberReadable(this.cost)).withStyle(ChatFormatting.AQUA)));
            textList.add(Component.translatable("cosmic.multiblock.orvex_upgrade_check"));
        })
                .addBatchModeLine(isBatchEnabled(), batchParallels)
                .addWorkingStatusLine()
                .addProgressLine(recipeLogic.getProgress(), recipeLogic.getMaxProgress(),
                        recipeLogic.getProgressPercent())
                .addOutputLines(recipeLogic.getLastRecipe());
        getDefinition().getAdditionalDisplay().accept(this, textList);
    }

    public static class HelixFusionRecipeLogic extends RecipeLogic {

        public HelixFusionRecipeLogic(IRecipeLogicMachine machine) {
            super(machine);
        }

        @Override
        public void onRecipeFinish() {
            if (lastRecipe != null) {
                var inputs = lastRecipe.getInputEUt();
                long totalEUt = inputs.getTotalEU() * lastRecipe.duration;
                ((HelixFusionMachine) machine).increaseEUConsumed(totalEUt);
            }
            super.onRecipeFinish();
        }
    }

    public static long clampLong(long v, long min, long max) {
        return Math.max(min, Math.min(v, max));
    }
}
