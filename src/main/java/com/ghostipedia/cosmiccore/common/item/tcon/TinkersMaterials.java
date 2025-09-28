package com.ghostipedia.cosmiccore.common.item.tcon;

import com.ghostipedia.cosmiccore.common.data.materials.tinkers.TinkersMaterial;

import com.ghostipedia.cosmiccore.common.item.tcon.modifiers.CosmicCoreModifiers;
import net.minecraft.world.item.Tiers;

import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.tools.TinkerModifiers;

public class TinkersMaterials {

    public static void init() {
    /*
        new TinkersMaterial.Builder("one")
                .headMaterialStats(100, 5.0f, Tiers.WOOD, 1.0f)
                .craftable(true).tier(1)
                .trait( () -> new ModifierEntry(TinkerModifiers.decay.get(), 1), MaterialRegistry.MELEE_HARVEST)
                .build();

        new TinkersMaterial.Builder("two")
                .headMaterialStats(120, 5.5f, Tiers.WOOD, 1.2f)
                .craftable(true).tier(1)
                .trait(TinkerModifiers.decay.get(), 1, MaterialRegistry.MELEE_HARVEST)
                .build();

        new TinkersMaterial.Builder("three")
                .headMaterialStats(140, 6.0f, Tiers.STONE, 1.4f)
                .craftable(true).tier(2)
                .trait(TinkerModifiers.decay.get(), 1, MaterialRegistry.MELEE_HARVEST)
                .build();

        new TinkersMaterial.Builder("four")
                .headMaterialStats(160, 6.5f, Tiers.STONE, 1.6f)
                .craftable(true).tier(2)
                .trait(TinkerModifiers.decay.get(), 1, MaterialRegistry.MELEE_HARVEST)
                .build();

        new TinkersMaterial.Builder("five")
                .headMaterialStats(180, 7.0f, Tiers.IRON, 1.8f)
                .craftable(true).tier(2)
                .trait(TinkerModifiers.decay.get(), 1, MaterialRegistry.MELEE_HARVEST)
                .build();

        new TinkersMaterial.Builder("six")
                .headMaterialStats(200, 7.5f, Tiers.IRON, 2.0f)
                .craftable(true).tier(2)
                .trait(TinkerModifiers.decay.get(), 1, MaterialRegistry.MELEE_HARVEST)
                .build();

        new TinkersMaterial.Builder("seven")
                .headMaterialStats(220, 8.0f, Tiers.IRON, 2.2f)
                .craftable(true).tier(2)
                .trait(TinkerModifiers.decay.get(), 1, MaterialRegistry.MELEE_HARVEST)
                .build();

        new TinkersMaterial.Builder("eight")
                .headMaterialStats(240, 8.5f, Tiers.DIAMOND, 2.4f)
                .craftable(true).tier(3)
                .trait(TinkerModifiers.decay.get(), 1, MaterialRegistry.MELEE_HARVEST)
                .build();

        new TinkersMaterial.Builder("nine")
                .headMaterialStats(260, 9.0f, Tiers.DIAMOND, 2.6f)
                .craftable(true).tier(3)
                .trait(TinkerModifiers.decay.get(), 1, MaterialRegistry.MELEE_HARVEST)
                .build();

        new TinkersMaterial.Builder("ten")
                .headMaterialStats(280, 9.5f, Tiers.DIAMOND, 2.8f)
                .craftable(true).tier(3)
                .trait(TinkerModifiers.decay.get(), 1, MaterialRegistry.MELEE_HARVEST)
                .build();

        new TinkersMaterial.Builder("eleven")
                .headMaterialStats(300, 10.0f, Tiers.NETHERITE, 3.0f)
                .craftable(true).tier(4)
                .trait(TinkerModifiers.decay.get(), 1, MaterialRegistry.MELEE_HARVEST)
                .build();

        new TinkersMaterial.Builder("twelve")
                .headMaterialStats(320, 10.5f, Tiers.NETHERITE, 3.2f)
                .craftable(true).tier(4)
                .trait(TinkerModifiers.decay.get(), 1, MaterialRegistry.MELEE_HARVEST)
                .build();

        new TinkersMaterial.Builder("thirteen")
                .headMaterialStats(340, 11.0f, Tiers.NETHERITE, 3.4f)
                .craftable(true).tier(4)
                .trait(TinkerModifiers.decay.get(), 1, MaterialRegistry.MELEE_HARVEST)
                .build();

        new TinkersMaterial.Builder("fourteen")
                .headMaterialStats(360, 11.5f, Tiers.NETHERITE, 3.6f)
                .craftable(true).tier(4)
                .trait(TinkerModifiers.decay.get(), 1, MaterialRegistry.MELEE_HARVEST)
                .build();

        new TinkersMaterial.Builder("fifteen")
                .headMaterialStats(380, 12.0f, Tiers.NETHERITE, 3.8f)
                .craftable(true).tier(4)
                .trait(TinkerModifiers.decay.get(), 1, MaterialRegistry.MELEE_HARVEST)
                .build();

        new TinkersMaterial.Builder("sixteen")
                .headMaterialStats(400, 12.5f, Tiers.NETHERITE, 4.0f)
                .craftable(true).tier(4)
                .trait(TinkerModifiers.decay.get(), 1, MaterialRegistry.MELEE_HARVEST)
                .build();

        new TinkersMaterial.Builder("seventeen")
                .headMaterialStats(420, 13.0f, Tiers.NETHERITE, 4.2f)
                .craftable(true).tier(4)
                .trait(TinkerModifiers.decay.get(), 1, MaterialRegistry.MELEE_HARVEST)
                .build();

        new TinkersMaterial.Builder("eighteen")
                .headMaterialStats(440, 13.5f, Tiers.NETHERITE, 4.4f)
                .craftable(true).tier(4)
                .trait(TinkerModifiers.decay.get(), 1, MaterialRegistry.MELEE_HARVEST)
                .build();

        new TinkersMaterial.Builder("nineteen")
                .headMaterialStats(460, 14.0f, Tiers.NETHERITE, 4.6f)
                .craftable(true).tier(4)
                .trait(TinkerModifiers.decay.get(), 1, MaterialRegistry.MELEE_HARVEST)
                .build();

        new TinkersMaterial.Builder("twenty")
                .headMaterialStats(480, 14.5f, Tiers.NETHERITE, 4.8f)
                .craftable(true).tier(4)
                .trait(TinkerModifiers.decay.get(), 1, MaterialRegistry.MELEE_HARVEST)
                .build();

        new TinkersMaterial.Builder("twentyone")
                .headMaterialStats(500, 15.0f, Tiers.NETHERITE, 5.0f)
                .craftable(true).tier(5)
                .trait(TinkerModifiers.decay.get(), 1, MaterialRegistry.MELEE_HARVEST)
                .build();

        new TinkersMaterial.Builder("twentytwo")
                .headMaterialStats(520, 15.5f, Tiers.NETHERITE, 5.2f)
                .craftable(true).tier(5)
                .trait(TinkerModifiers.decay.get(), 1, MaterialRegistry.MELEE_HARVEST)
                .build();

        new TinkersMaterial.Builder("twentythree")
                .headMaterialStats(540, 16.0f, Tiers.NETHERITE, 5.4f)
                .craftable(true).tier(5)
                .trait(TinkerModifiers.decay.get(), 1, MaterialRegistry.MELEE_HARVEST)
                .build();

        new TinkersMaterial.Builder("twentyfour")
                .headMaterialStats(560, 16.5f, Tiers.NETHERITE, 5.6f)
                .craftable(true).tier(5)
                .trait(TinkerModifiers.decay.get(), 1, MaterialRegistry.MELEE_HARVEST)
                .build();

        new TinkersMaterial.Builder("twentyfive")
                .headMaterialStats(580, 17.0f, Tiers.NETHERITE, 5.8f)
                .craftable(true).tier(5)
                .trait(TinkerModifiers.decay.get(), 1, MaterialRegistry.MELEE_HARVEST)
                .build();

        new TinkersMaterial.Builder("twentysix")
                .headMaterialStats(600, 17.5f, Tiers.NETHERITE, 6.0f)
                .craftable(true).tier(5)
                .trait(TinkerModifiers.decay.get(), 1, MaterialRegistry.MELEE_HARVEST)
                .build();

        new TinkersMaterial.Builder("twentyseven")
                .headMaterialStats(620, 18.0f, Tiers.NETHERITE, 6.2f)
                .craftable(true).tier(5)
                .trait(TinkerModifiers.decay.get(), 1, MaterialRegistry.MELEE_HARVEST)
                .build();

        new TinkersMaterial.Builder("twentyeight")
                .headMaterialStats(640, 18.5f, Tiers.NETHERITE, 6.4f)
                .craftable(true).tier(5)
                .trait(TinkerModifiers.decay.get(), 1, MaterialRegistry.MELEE_HARVEST)
                .build();

        new TinkersMaterial.Builder("twentynine")
                .headMaterialStats(660, 19.0f, Tiers.NETHERITE, 6.6f)
                .craftable(true).tier(5)
                .trait(TinkerModifiers.decay.get(), 1, MaterialRegistry.MELEE_HARVEST)
                .build();

        new TinkersMaterial.Builder("thirty")
                .headMaterialStats(680, 19.5f, Tiers.NETHERITE, 6.8f)
                .craftable(true).tier(5)
                .trait(TinkerModifiers.decay.get(), 1, MaterialRegistry.MELEE_HARVEST)
                .build();
         */
    }
}
