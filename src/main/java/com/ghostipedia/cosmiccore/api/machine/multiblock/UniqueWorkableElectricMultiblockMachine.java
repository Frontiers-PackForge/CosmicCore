package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.ghostipedia.cosmiccore.api.data.savedData.UniqueMultiblockSavedData;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;

import java.util.List;
import java.util.UUID;

public class UniqueWorkableElectricMultiblockMachine extends WorkableElectricMultiblockMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            UniqueWorkableElectricMultiblockMachine.class, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    public UniqueWorkableElectricMultiblockMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    // Used to make sure you cannot have more than one of this multiblock per player / team
    @Persisted
    public boolean isDuplicate = false;

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();

        if (getLevel() instanceof ServerLevel serverLevel) {
            var owner = getTeamUUID();
            var multiblockId = getDefinition().getId().toString();
            var uniqueMultiblockMapping = UniqueMultiblockSavedData.getOrCreate(serverLevel);

            if (uniqueMultiblockMapping.hasData(owner, multiblockId, getDimension())) {
                this.isDuplicate = !uniqueMultiblockMapping.isUnique(owner, multiblockId, getDimension(), getPos());
                if (isDuplicate) recipeLogic.setStatus(RecipeLogic.Status.SUSPEND);
            } else uniqueMultiblockMapping.addMultiblock(owner, getDefinition().getId().toString(), getDimension(),
                    getPos());

        }
    }

    protected UUID getTeamUUID() {
        var team = ((FTBOwner) getOwner()).getPlayerTeam(getOwnerUUID());
        return team != null ? team.getTeamId() : getOwnerUUID();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        if (getLevel() instanceof ServerLevel serverLevel) {
            var owner = getTeamUUID();
            var uniqueMultiblockMapping = UniqueMultiblockSavedData.getOrCreate(serverLevel);
            uniqueMultiblockMapping.removeMultiblock(owner, getDefinition().getId().toString(), getDimension(),
                    getPos());
        }
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        if (this.isDuplicate) {
            textList.add(Component.translatable("cosmic.multiblock.capacitor.duplicate.multiblock.1")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_RED)));
            textList.add(Component.translatable("cosmic.multiblock.capacitor.duplicate.multiblock.2")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_RED)));
        } else super.addDisplayText(textList);
    }

    private String getDimension() {
        if (getLevel() instanceof ServerLevel serverLevel) {
            return serverLevel.dimension().location().toString();
        }
        return null;
    }
}
