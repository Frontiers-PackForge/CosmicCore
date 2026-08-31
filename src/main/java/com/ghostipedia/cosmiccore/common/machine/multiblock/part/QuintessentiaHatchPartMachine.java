package com.ghostipedia.cosmiccore.common.machine.multiblock.part;

import com.ghostipedia.cosmiccore.api.capability.souls.SoulType;
import com.ghostipedia.cosmiccore.api.machine.trait.NotifiableSoulContainer;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.layout.Flow;

import java.util.Locale;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class QuintessentiaHatchPartMachine extends TieredIOPartMachine implements IMuiMachine {

    @SaveField
    private final NotifiableSoulContainer networkContainer;

    public QuintessentiaHatchPartMachine(BlockEntityCreationInfo info, int tier, IO io) {
        super(info, tier, io);
        networkContainer = new NotifiableSoulContainer(
                this,
                io,
                type -> getMaxTransfer(tier, type),
                type -> getMaxCapacity(tier, type));
    }

    @Override
    public void addedToController(MultiblockControllerMachine controller, String substructureName) {
        super.addedToController(controller, substructureName);
        var level = controller.self().getLevel();
        if (level != null && level.getServer() != null) {
            level.getServer().tell(new TickTask(0, this::invalidateIfDuplicate));
        }
    }

    private void invalidateIfDuplicate() {
        for (var controller : getControllers()) {
            for (var part : controller.getParts()) {
                if (part != this && part instanceof QuintessentiaHatchPartMachine) {
                    controller.invalidateStructure();
                }
            }
        }
    }

    @Override
    public ModularPanel<?> buildUI(PosGuiData guiData, PanelSyncManager syncManager, UISettings settings) {
        IntSyncValue anima = new IntSyncValue(() -> networkContainer.getAmount(SoulType.Anima));
        IntSyncValue spiritus = new IntSyncValue(() -> networkContainer.getAmount(SoulType.Spiritus));
        syncManager.syncValue("quintessentia_anima", anima);
        syncManager.syncValue("quintessentia_spiritus", spiritus);

        int limit = getTierLimit(getTier());
        String scaleKey = io == IO.IN ?
                "cosmiccore.quintessentia_hatch.scale.input" :
                "cosmiccore.quintessentia_hatch.scale.output";

        var panel = ModularPanel.defaultPanel(getDefinition().getId().getPath(), 276, 76);
        panel.child(GTMuiWidgets.createTitleBar(getDefinition(), 276));
        panel.child(Flow.col()
                .width(260)
                .height(50)
                .left(8)
                .top(18)
                .childPadding(1)
                .child(Text.lang("cosmiccore.quintessentia_hatch.network")
                        .asWidget()
                        .color(ChatFormatting.DARK_GRAY.getColor()))
                .child(Text.lang(scaleKey)
                        .asWidget()
                        .color(ChatFormatting.DARK_GRAY.getColor()))
                .child(Text.dynamic(() -> Component.translatable(
                        "cosmiccore.quintessentia_hatch.channel",
                        Component.translatable("gui.cosmiccore.soul.anima.name"),
                        coloredAmount(anima.getIntValue(), ChatFormatting.RED),
                        coloredAmount(limit, ChatFormatting.RED)).withStyle(ChatFormatting.DARK_GRAY)).asWidget())
                .child(Text.dynamic(() -> Component.translatable(
                        "cosmiccore.quintessentia_hatch.channel",
                        Component.translatable("gui.cosmiccore.soul.spiritus.name"),
                        coloredAmount(spiritus.getIntValue(), ChatFormatting.LIGHT_PURPLE),
                        coloredAmount(limit, ChatFormatting.LIGHT_PURPLE)).withStyle(ChatFormatting.DARK_GRAY))
                        .asWidget()));
        return panel;
    }

    public static int getMaxTransfer(int tier, SoulType type) {
        if (type != SoulType.Anima && type != SoulType.Spiritus) return 0;
        return getTierLimit(tier);
    }

    public static int getMaxCapacity(int tier, SoulType type) {
        if (type != SoulType.Anima && type != SoulType.Spiritus) return 0;
        return getTierLimit(tier);
    }

    private static int getTierLimit(int tier) {
        return switch (tier) {
            case GTValues.MV -> 10_000;
            case GTValues.HV -> 50_000;
            case GTValues.EV -> 250_000;
            case GTValues.IV -> 1_000_000;
            case GTValues.LuV -> 5_000_000;
            case GTValues.ZPM -> 25_000_000;
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

    private static String formatAmount(int amount) {
        return String.format(Locale.ROOT, "%,d", amount);
    }

    private static Component coloredAmount(int amount, ChatFormatting color) {
        return Component.literal(formatAmount(amount)).withStyle(color);
    }

    @Override
    public int tintColor(int index) {
        return index == 2 ? GTValues.VC[getTier()] : super.tintColor(index);
    }
}
