package com.ghostipedia.cosmiccore.common.machine.multiblock.part;

import com.ghostipedia.cosmiccore.api.machine.trait.NotifiableSoulContainer;
import com.ghostipedia.cosmiccore.utils.OwnershipUtils;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;

import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SoulHatchPartMachine extends TieredIOPartMachine {


    @Persisted
    @DescSynced
    private final NotifiableSoulContainer soulContainer;

    public SoulHatchPartMachine(BlockEntityCreationInfo holder, int tier, IO io) {
        super(holder, tier, io);
        this.soulContainer = new NotifiableSoulContainer(this, io, getMaxConsumption(tier), getMaxCapacity(tier));
    }

    @Override
    public void addedToController(MultiblockControllerMachine controller) {
        super.addedToController(controller);
        var level = controller.self().getLevel();
        if (level != null && level.getServer() != null) {
            level.getServer().tell(new TickTask(0, this::invalidateIfDuplicate));
        }
    }

    private void invalidateIfDuplicate() {
        for (var controller : getControllers()) {
            for (var part : controller.getParts()) {
                if (part == this) continue;
                if (part instanceof SoulHatchPartMachine soulHatch && soulHatch.io == this.io) {
                    controller.onStructureInvalid();
                }
            }
        }
    }

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 176, 117);

        var scrollable = new DraggableScrollableWidgetGroup(4, 4, 168, 109).setBackground(GuiTextures.DISPLAY);
        scrollable.addWidget(new LabelWidget(4, 5,
                "gui.cosmiccore.soul_hatch.label." + (this.io == IO.IN ? "import" : "export")));
        scrollable.addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText)
                .textSupplier(getLevel() != null && !getLevel().isClientSide ? this::addDisplayText : null)
                .setMaxWidthLimit(160));

        group.addWidget(scrollable);
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    private void addDisplayText(List<Component> textList) {
        textList.add(Component.translatable("gui.cosmiccore.soul_hatch.owner",
                OwnershipUtils.getName(getOwner())));

        var stacks = this.soulContainer.getStacks();
        textList.add(Component.empty());
        if (stacks.isEmpty()) {
            textList.add(Component.translatable("gui.cosmiccore.soul.empty_network").withStyle(ChatFormatting.GRAY));
        } else {
            textList.add(Component.translatable("gui.cosmiccore.soul.network_contents").withStyle(ChatFormatting.GOLD));
            for (var stack : stacks) {
                textList.add(Component.literal("  ").append(stack.type().toComponent(stack.amount())));
            }
        }
    }

    public static int getMaxConsumption(int tier) {
        return switch (tier) {
            case GTValues.IV -> 10_000;
            case GTValues.LuV -> 50_000;
            case GTValues.ZPM -> 5_000_000;
            case GTValues.UV -> 10_000_000;
            case GTValues.UHV -> 25_000_000;
            case GTValues.UEV -> 50_000_000;
            case GTValues.UIV -> 125_000_000;
            case GTValues.UXV -> 250_000_000;
            case GTValues.OpV -> 500_000_000;
            case GTValues.MAX -> Integer.MAX_VALUE;
            default -> 0;
        };
    }

    public static int getMaxCapacity(int tier) {
        return switch (tier) {
            case GTValues.IV -> 1_000_000;
            case GTValues.LuV -> 10_000_000;
            case GTValues.ZPM -> 50_000_000;
            case GTValues.UV -> 100_000_000;
            case GTValues.UHV -> 250_000_000;
            case GTValues.UEV -> 500_000_000;
            case GTValues.UIV -> 1_000_000_000;
            case GTValues.UXV -> 1_500_000_000;
            case GTValues.OpV -> 2_000_000_000;
            case GTValues.MAX -> Integer.MAX_VALUE;
            default -> 0;
        };
    }


    @Override
    public int tintColor(int index) {
        return (index == 2) ? GTValues.VC[getTier()] : super.tintColor(index);
    }
}
