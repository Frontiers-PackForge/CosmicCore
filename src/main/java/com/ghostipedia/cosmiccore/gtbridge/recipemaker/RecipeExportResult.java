package com.ghostipedia.cosmiccore.gtbridge.recipemaker;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.Nullable;

public record RecipeExportResult(String script, @Nullable Component message, boolean copy) {

    public static RecipeExportResult copied(String script) {
        return new RecipeExportResult(script, null, true);
    }

    public static RecipeExportResult copied(String script, Component message) {
        return new RecipeExportResult(script, message, true);
    }

    public static RecipeExportResult failed(Component message) {
        return new RecipeExportResult("", message, false);
    }

    public static RecipeExportResult resolved(String script, KubeJsRecipeId.Resolution resolution) {
        return switch (resolution.outcome()) {
            case AUTO_VARIANT -> copied(script,
                    Component.translatable("cosmiccore.recipe_maker.id.amended", resolution.loadedId().toString(),
                            resolution.variant()).withStyle(ChatFormatting.GREEN));
            case EXPLICIT_OCCUPIED -> copied(script,
                    Component
                            .translatable("cosmiccore.recipe_maker.id.explicit_occupied",
                                    resolution.loadedId().toString())
                            .withStyle(ChatFormatting.YELLOW));
            case FAILED -> failed(Component.translatable("cosmiccore.recipe_maker.id.amend_failed",
                    resolution.loadedId().toString()).withStyle(ChatFormatting.RED));
            default -> copied(script);
        };
    }
}
