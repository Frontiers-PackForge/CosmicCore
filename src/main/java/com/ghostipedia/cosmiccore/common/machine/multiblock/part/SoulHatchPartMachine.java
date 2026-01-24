package com.ghostipedia.cosmiccore.common.machine.multiblock.part;

import com.ghostipedia.cosmiccore.api.machine.trait.NotifiableSoulContainer;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
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
        this.soulContainer = new NotifiableSoulContainer(this, io, getMaxConsumption(tier), getMaxCapacity(tier));
    }

    @Override
    public void addedToController(IMultiController controller) {
        super.addedToController(controller);
        boolean hasDuplicate = controller.getParts().stream()
                .filter(part -> part != this)
                .anyMatch(part -> part instanceof SoulHatchPartMachine soulHatch && soulHatch.io == this.io);
        if (hasDuplicate) controller.onStructureInvalid();
    }

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 128, 63);

        group.addWidget(new ImageWidget(4, 4, 120, 55, GuiTextures.DISPLAY));
        group.addWidget(new LabelWidget(8, 8,
                Component.translatable("gui.cosmiccore.soul_hatch.label." + (this.io == IO.IN ? "import" : "export"))));

        // TODO: Get and display proper player/team Name
        group.addWidget(
                new LabelWidget(8, 18,
                        () -> I18n.get("gui.cosmiccore.soul_hatch.owner",
                                PlayerHelper.getUsernameFromUUID(this.soulContainer.getMachine().getOwnerUUID())))
                        .setClientSideWidget());

        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    public static int getMaxConsumption(int tier) {
        return switch (tier) {
            case GTValues.IV  -> 10_000;
            case GTValues.LuV -> 50_000;
            case GTValues.ZPM -> 5_000_000;
            case GTValues.UV  -> 10_000_000;
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
            case GTValues.IV  -> 1_000_000;
            case GTValues.LuV -> 10_000_000;
            case GTValues.ZPM -> 50_000_000;
            case GTValues.UV  -> 100_000_000;
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
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public int tintColor(int index) {
        return (index == 2) ? GTValues.VC[getTier()] : super.tintColor(index);
    }
}
