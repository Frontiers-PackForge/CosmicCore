package com.ghostipedia.cosmiccore.common.item.tcon.modifiers;

import com.ghostipedia.cosmiccore.common.item.tcon.TinkersMaterials;
import net.minecraft.data.PackOutput;

import slimeknights.tconstruct.library.data.tinkering.AbstractModifierProvider;
import slimeknights.tconstruct.library.modifiers.util.ModifierLevelDisplay;

public class CosmicModifierProvider extends AbstractModifierProvider {

    public CosmicModifierProvider(PackOutput packOutput) {
        super(packOutput);
    }

    @Override
    protected void addModifiers() {
        buildModifier(CosmicModifierIds.wrenchModeBehavior)
                .levelDisplay(ModifierLevelDisplay.NO_LEVELS);
    }


    @Override
    public String getName() {
        return "cosmic modifiers";
    }
}
