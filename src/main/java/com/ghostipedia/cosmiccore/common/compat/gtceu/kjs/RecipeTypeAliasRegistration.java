package com.ghostipedia.cosmiccore.common.compat.gtceu.kjs;

import com.ghostipedia.cosmiccore.mixin.gtfix.kjs.RegisterEventConstructorInvoker;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.integration.kjs.helpers.KubeGTRegistryEventHandler;

import net.minecraft.core.registries.Registries;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.registries.RegisterEvent;

import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.registry.RegistryEventHandler;

public final class RecipeTypeAliasRegistration {

    private RecipeTypeAliasRegistration() {}

    public static void afterRegistration(RegisterEvent event) {
        if (!event.getRegistryKey().equals(Registries.RECIPE_TYPE) || ModLoader.hasErrors()) {
            return;
        }

        var aliasEvent = RegisterEventConstructorInvoker.cosmiccore$create(
                GTRegistries.Keys.RECIPE_TYPE, event.getRegistry());
        var context = ModLoadingContext.get();
        var previousContainer = context.getActiveContainer();
        try {
            context.setActiveContainer(ModList.get().getModContainerById(KubeJS.MOD_ID).orElseThrow());
            RegistryEventHandler.registerAll(aliasEvent);
            context.setActiveContainer(ModList.get().getModContainerById(GTCEu.MOD_ID).orElseThrow());
            KubeGTRegistryEventHandler.registerAll(aliasEvent);
        } finally {
            context.setActiveContainer(previousContainer);
        }
    }
}
