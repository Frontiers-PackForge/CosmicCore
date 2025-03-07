package com.ghostipedia.cosmiccore.common.machine.multiblock.part;

import com.ghostipedia.cosmiccore.api.machine.trait.NotifiableSoulContainer;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import wayoftime.bloodmagic.util.helper.PlayerHelper;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SoulHatchPartMachine extends TieredIOPartMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(SoulHatchPartMachine.class,
            TieredIOPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    @DescSynced
    private final NotifiableSoulContainer soulContainer;

    public SoulHatchPartMachine(IMachineBlockEntity holder, int tier, IO io) {
        super(holder, tier, io);
        this.soulContainer = new NotifiableSoulContainer(this, io, getMaxCapacity(tier), getMaxConsumption(tier));
    }

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 128, 63);

        group.addWidget(new ImageWidget(4, 4, 120, 55, GuiTextures.DISPLAY));
        group.addWidget(new LabelWidget(8, 8,
                Component.translatable("gui.cosmiccore.soul_hatch.label." + (this.io == IO.IN ? "import" : "export"))));

        if (soulContainer.getOwner() == null) {
            group.addWidget(
                    new LabelWidget(8, 18, I18n.get("gui.cosmiccore.soul_hatch.no_network")).setClientSideWidget());
        } else {
            group.addWidget(
                    new LabelWidget(8, 18,
                            () -> I18n.get("gui.cosmiccore.soul_hatch.owner",
                                    PlayerHelper.getUsernameFromUUID(this.soulContainer.getOwner())))
                            .setClientSideWidget());
            group.addWidget(
                    new LabelWidget(8, 28,
                            () -> I18n.get("gui.cosmiccore.soul_hatch.lp",
                                    FormattingUtil.formatNumbers(soulContainer.getCurrentEssence())))
                            .setClientSideWidget());
        }

        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    public static int getMaxCapacity(int tier) {
        return switch (tier) {
            case GTValues.ZPM -> 10000000;
            case GTValues.UV -> 20000000;
            case GTValues.UHV -> 50000000;
            case GTValues.UEV -> 100000000;
            case GTValues.UIV -> 250000000;
            case GTValues.UXV -> 500000000;
            case GTValues.OpV -> 1000000000;
            case GTValues.MAX -> Integer.MAX_VALUE;
            default -> 0;
        };
    }

    public static int getMaxConsumption(int tier) {
        return switch (tier) {
            case GTValues.ZPM -> 5000000;
            case GTValues.UV -> 10000000;
            case GTValues.UHV -> 25000000;
            case GTValues.UEV -> 50000000;
            case GTValues.UIV -> 125000000;
            case GTValues.UXV -> 250000000;
            case GTValues.OpV -> 500000000;
            case GTValues.MAX -> Integer.MAX_VALUE;
            default -> 0;
        };
    }

    public void attachSoulNetwork(Player player) {
        this.soulContainer.setOwner(player.getUUID());
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public int tintColor(int index) {
        return (index == 2) ? GTValues.VC[getTier()] : super.tintColor(index);
    }
}
