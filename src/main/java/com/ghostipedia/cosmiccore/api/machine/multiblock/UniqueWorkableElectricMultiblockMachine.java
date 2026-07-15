package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.ghostipedia.cosmiccore.api.data.savedData.UniqueMultiblockSavedData;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;

import net.minecraft.server.level.ServerLevel;

import java.util.UUID;

public class UniqueWorkableElectricMultiblockMachine extends WorkableElectricMultiblockMachine {

    public UniqueWorkableElectricMultiblockMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    // Used to make sure you cannot have more than one of this multiblock per player / team
    @SaveField
    public boolean isDuplicate = false;

    @Override
    public void formStructure(@org.jetbrains.annotations.NotNull String substructureName) {
        super.formStructure(substructureName);

        if (getLevel() instanceof ServerLevel serverLevel) {
            var owner = getTeamUUID();
            var multiblockId = getDefinition().getId().toString();
            var uniqueMultiblockMapping = UniqueMultiblockSavedData.getOrCreate(serverLevel);

            if (uniqueMultiblockMapping.hasData(owner, multiblockId, getDimension())) {
                this.isDuplicate = !uniqueMultiblockMapping.isUnique(owner, multiblockId, getDimension(),
                        getBlockPos());
                if (isDuplicate) recipeLogic.setStatus(RecipeLogic.Status.SUSPEND);
            } else uniqueMultiblockMapping.addMultiblock(owner, getDefinition().getId().toString(), getDimension(),
                    getBlockPos());

        }
    }

    protected UUID getTeamUUID() {
        var team = ((FTBOwner) getOwner()).getPlayerTeam(getOwnerUUID());
        return team != null ? team.getTeamId() : getOwnerUUID();
    }

    @Override
    public void invalidateStructure(String name) {
        super.invalidateStructure(name);
        if (getLevel() instanceof ServerLevel serverLevel) {
            var owner = getTeamUUID();
            var uniqueMultiblockMapping = UniqueMultiblockSavedData.getOrCreate(serverLevel);
            uniqueMultiblockMapping.removeMultiblock(owner, getDefinition().getId().toString(), getDimension(),
                    getBlockPos());
        }
    }

    // TODO(8.0.0 MUI2): custom display text shelved; base default getWidgetsForDisplay UI used for now.
    // addDisplayText(List<Component>) was removed from WorkableElectricMultiblockMachine in 8.0.0.
    // Original "duplicate multiblock" warning text (orig in git):
    // if (this.isDuplicate) {
    // textList.add(Component.translatable("cosmic.multiblock.capacitor.duplicate.multiblock.1")
    // .setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_RED)));
    // textList.add(Component.translatable("cosmic.multiblock.capacitor.duplicate.multiblock.2")
    // .setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_RED)));
    // } else super.addDisplayText(textList);

    private String getDimension() {
        if (getLevel() instanceof ServerLevel serverLevel) {
            return serverLevel.dimension().location().toString();
        }
        return null;
    }
}
