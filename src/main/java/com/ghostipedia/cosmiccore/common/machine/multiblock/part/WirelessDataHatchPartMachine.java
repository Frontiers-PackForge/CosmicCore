package com.ghostipedia.cosmiccore.common.machine.multiblock.part;

import com.ghostipedia.cosmiccore.common.wireless.WirelessDataStore;
import com.ghostipedia.cosmiccore.utils.OwnershipUtils;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IDataAccessHatch;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternState;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;
import com.gregtechceu.gtceu.common.recipe.condition.ResearchCondition;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class WirelessDataHatchPartMachine extends MultiblockPartMachine implements IDataAccessHatch, IMuiMachine {

    public WirelessDataHatchPartMachine(BlockEntityCreationInfo info) {
        super(info);
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
        var machineOwner = getOwner();
        if (machineOwner == null) return false;
        var team = ((FTBOwner) machineOwner).getPlayerTeam(getOwnerUUID());
        var owner = team != null ? team.getTeamId() : getOwnerUUID();

        seen.add(this);
        var dataStore = WirelessDataStore.getWirelessDataStore(owner);
        return recipe.conditions.stream().noneMatch(ResearchCondition.class::isInstance) ||
                dataStore.isRecipeAvailable(recipe, seen);
    }

    private UUID getTeamUUID() {
        var owner = getOwner();
        if (owner == null) return getOwnerUUID();
        var team = ((FTBOwner) owner).getPlayerTeam(getOwnerUUID());
        return team != null ? team.getTeamId() : getOwnerUUID();
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        var panel = ModularPanel.defaultPanel(getDefinition().getId().getPath(), 190, 130);
        panel.child(GTMuiWidgets.createTitleBar(this.getDefinition(), 190));

        panel.child(Flow.column()
                .coverChildren()
                .padding(8)
                .top(14)
                .horizontalCenter() // brachy 3.3.0: alignX(float) removed; horizontalCenter() == leftRel(0.5f)
                .childPadding(4)
                .child(new TextWidget<>(Text.dynamic(() -> {
                    var textList = new java.util.ArrayList<Component>();
                    addDisplayText(textList);
                    if (textList.isEmpty()) return Component.empty();
                    var result = Component.literal("");
                    for (int i = 0; i < textList.size(); i++) {
                        if (i > 0) result.append(Component.literal("\n"));
                        result.append(textList.get(i));
                    }
                    return result;
                }))));

        return panel;
    }

    public void addDisplayText(List<Component> textList) {
        PatternState state = new PatternState();
        state.setFormed(isFormed());
        MultiblockDisplayText.builder(textList, state)
                .addCustom(list -> OwnershipUtils.addOwnerLine(list, getOwner(), true));
    }
}
