package com.ghostipedia.cosmiccore.forge;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

final class AlloyBlastLifecyclePolicy {

    static final int MAX_CASING_MOLTEN_TEMPERATURE = 3600;
    static final int MAX_RESTORED_ALLOYS = 128;

    private static final Set<String> CASING_ALLOYS = Set.of(
            "hsla_steel",
            "incoloy_ma_956",
            "watertight_steel",
            "zeron_100",
            "hastelloy_x",
            "titanium_tungsten_carbide",
            "stellite_100",
            "hastelloy_c_276",
            "maraging_steel_300",
            "cobalt_brass");

    private AlloyBlastLifecyclePolicy() {}

    static @Nullable Integer casingMoltenTemperatureOverride(String materialName, int inferredTemperature) {
        if (!CASING_ALLOYS.contains(materialName)) return null;
        return Math.min(inferredTemperature, MAX_CASING_MOLTEN_TEMPERATURE);
    }

    static RecipeShape recipeShape(List<Component> components) {
        int itemInputs = 0;
        int fluidInputs = 0;
        int outputUnits = 0;
        for (Component component : components) {
            if (component.amount() <= 0 || component.form() == InputForm.INVALID) {
                return RecipeShape.invalid();
            }
            if (component.form() == InputForm.DUST) {
                itemInputs++;
            } else if (++fluidInputs > 2) {
                return RecipeShape.invalid();
            }
            outputUnits += component.amount();
        }
        return new RecipeShape(components.size() >= 2, itemInputs, fluidInputs, outputUnits, components.size());
    }

    enum InputForm {
        DUST,
        FLUID,
        INVALID
    }

    record Component(InputForm form, int amount) {}

    record RecipeShape(boolean valid, int itemInputs, int fluidInputs, int outputUnits, int circuitMeta) {

        private static RecipeShape invalid() {
            return new RecipeShape(false, 0, 0, 0, 0);
        }
    }
}
