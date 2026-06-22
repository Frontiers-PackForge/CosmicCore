package com.ghostipedia.cosmiccore.common.machine.multiblock.part;

import com.ghostipedia.cosmiccore.common.wireless.WirelessDataStore;
import com.ghostipedia.cosmiccore.utils.OwnershipUtils;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IDataAccessHatch;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;
import com.gregtechceu.gtceu.common.recipe.condition.ResearchCondition;

import com.lowdragmc.lowdraglib.gui.widget.*;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class WirelessDataHatchPartMachine extends MultiblockPartMachine implements IDataAccessHatch {

    public WirelessDataHatchPartMachine(BlockEntityCreationInfo holder) {
        super(holder);
    }

    @Override
    public GTRecipe modifyRecipe(GTRecipe recipe) {
        return IDataAccessHatch.super.modifyRecipe(recipe);
    }

    @Override
    public boolean isCreative() {
        return false;
    }

    @Override
    public boolean isRecipeAvailable(@NotNull GTRecipe recipe, @NotNull Collection<IDataAccessHatch> seen) {
        var team = getOwner() instanceof FTBOwner ftbOwner ? ftbOwner.getPlayerTeam(getOwnerUUID()) : null;
        var owner = team != null ? team.getTeamId() : getOwnerUUID();

        seen.add(this);
        var dataStore = WirelessDataStore.getWirelessDataStore(owner);
        return recipe.conditions.stream().noneMatch(ResearchCondition.class::isInstance) ||
                dataStore.isRecipeAvailable(recipe, seen);
    }

    private UUID getTeamUUID() {
        var team = getOwner() instanceof FTBOwner ftbOwner ? ftbOwner.getPlayerTeam(getOwnerUUID()) : null;
        return team != null ? team.getTeamId() : getOwnerUUID();
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
        MultiblockDisplayText.builder(textList, isFormed())
                .addCustom(list -> OwnershipUtils.addOwnerLine(list, getOwner(), true));
    }
}
