package com.ghostipedia.cosmiccore.mixin.gtceu;

import com.ghostipedia.cosmiccore.api.capability.recipe.SoulRecipeCapability;
import com.ghostipedia.cosmiccore.api.capability.souls.SoulType;
import com.ghostipedia.cosmiccore.api.recipe.ingredient.SoulIngredient;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.ImbumentPylonMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.bloomwyrm.BloomwyrmRecipeKeys;
import com.ghostipedia.cosmiccore.common.recipe.condition.DeedCondition;

import com.gregtechceu.gtceu.integration.kjs.recipe.GTRecipeSchema;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.gregtechceu.gtceu.integration.kjs.recipe.GTRecipeSchema$GTKubeRecipe", remap = false)
public abstract class GTKubeRecipeMixin {

    public GTRecipeSchema.GTKubeRecipe altarTier(int tier) {
        if (tier < 4 || tier > 6) {
            throw new KubeRuntimeException("Imbument Pylon altar level must be between 4 and 6");
        }
        return cosmiccore$putIntData((GTRecipeSchema.GTKubeRecipe) (Object) this,
                ImbumentPylonMachine.ALTAR_TIER_KEY, tier);
    }

    public GTRecipeSchema.GTKubeRecipe animaInput(int amount) {
        return cosmiccore$soulInput(SoulType.Anima, amount);
    }

    public GTRecipeSchema.GTKubeRecipe spiritusInput(int amount) {
        return cosmiccore$soulInput(SoulType.Spiritus, amount);
    }

    @Unique
    private GTRecipeSchema.GTKubeRecipe cosmiccore$soulInput(SoulType type, int amount) {
        if (amount < 0) {
            throw new KubeRuntimeException(type + " input must be non-negative");
        }
        GTRecipeSchema.GTKubeRecipe self = (GTRecipeSchema.GTKubeRecipe) (Object) this;
        if (amount == 0) return self;
        boolean perTick = self.perTick;
        self.perTick = false;
        try {
            return self.input(SoulRecipeCapability.CAP, SoulIngredient.of(type, amount));
        } finally {
            self.perTick = perTick;
        }
    }

    public GTRecipeSchema.GTKubeRecipe deed(String deedId) {
        return deed(deedId, false);
    }

    public GTRecipeSchema.GTKubeRecipe deed(String deedId, boolean reverse) {
        GTRecipeSchema.GTKubeRecipe self = (GTRecipeSchema.GTKubeRecipe) (Object) this;
        return self.addCondition(new DeedCondition(reverse, ResourceLocation.parse(deedId)));
    }

    public GTRecipeSchema.GTKubeRecipe biopowerInput(int amount) {
        if (amount < 0) {
            throw new KubeRuntimeException("Bloomwyrm Biopower input must be non-negative");
        }
        GTRecipeSchema.GTKubeRecipe self = (GTRecipeSchema.GTKubeRecipe) (Object) this;
        return cosmiccore$putIntData(self, BloomwyrmRecipeKeys.BIOPOWER_INPUT, amount);
    }

    public GTRecipeSchema.GTKubeRecipe biopowerOutput(int amount) {
        if (amount < 0) {
            throw new KubeRuntimeException("Bloomwyrm Biopower output must be non-negative");
        }
        GTRecipeSchema.GTKubeRecipe self = (GTRecipeSchema.GTKubeRecipe) (Object) this;
        return cosmiccore$putIntData(self, BloomwyrmRecipeKeys.BIOPOWER_OUTPUT, amount);
    }

    public GTRecipeSchema.GTKubeRecipe bloomwyrmChargeInput(long amount) {
        if (amount < 0) {
            throw new KubeRuntimeException("Bloomwyrm Charge input must be non-negative");
        }
        GTRecipeSchema.GTKubeRecipe self = (GTRecipeSchema.GTKubeRecipe) (Object) this;
        return cosmiccore$putLongData(self, BloomwyrmRecipeKeys.CHARGE_INPUT, amount);
    }

    public GTRecipeSchema.GTKubeRecipe murkbloomChargeInput(long amount) {
        return bloomwyrmChargeInput(amount);
    }

    public GTRecipeSchema.GTKubeRecipe bloomwyrmChargeOutput(long amount) {
        if (amount < 0) {
            throw new KubeRuntimeException("Bloomwyrm Charge output must be non-negative");
        }
        GTRecipeSchema.GTKubeRecipe self = (GTRecipeSchema.GTKubeRecipe) (Object) this;
        return cosmiccore$putLongData(self, BloomwyrmRecipeKeys.CHARGE_OUTPUT, amount);
    }

    public GTRecipeSchema.GTKubeRecipe murkbloomChargeOutput(long amount) {
        return bloomwyrmChargeOutput(amount);
    }

    public GTRecipeSchema.GTKubeRecipe maxCampusParallel(int amount) {
        if (amount < 1) {
            throw new KubeRuntimeException("Bloomwyrm campus parallel limit must be positive");
        }
        GTRecipeSchema.GTKubeRecipe self = (GTRecipeSchema.GTKubeRecipe) (Object) this;
        return cosmiccore$putIntData(self, BloomwyrmRecipeKeys.MAX_PARALLEL, amount);
    }

    @Unique
    private static GTRecipeSchema.GTKubeRecipe cosmiccore$putIntData(
                                                                     GTRecipeSchema.GTKubeRecipe recipe, String key,
                                                                     int value) {
        CompoundTag data = recipe.getValue(GTRecipeSchema.DATA);
        if (data == null) {
            data = new CompoundTag();
        }
        data.putInt(key, value);
        recipe.setValue(GTRecipeSchema.DATA, data);
        return recipe;
    }

    @Unique
    private static GTRecipeSchema.GTKubeRecipe cosmiccore$putLongData(
                                                                      GTRecipeSchema.GTKubeRecipe recipe, String key,
                                                                      long value) {
        CompoundTag data = recipe.getValue(GTRecipeSchema.DATA);
        if (data == null) {
            data = new CompoundTag();
        }
        data.putLong(key, value);
        recipe.setValue(GTRecipeSchema.DATA, data);
        return recipe;
    }

    @Inject(method = "getOrCreateId", at = @At("RETURN"), cancellable = true)
    private void cosmiccore$canonicalizeId(CallbackInfoReturnable<ResourceLocation> cir) {
        KubeRecipe self = (KubeRecipe) (Object) this;
        ResourceLocation rid = self.id;
        if (rid == null || self.type == null || self.type.id == null) {
            return;
        }
        String typePath = self.type.id.getPath();
        ResourceLocation canonical = rid.withPath(p -> {
            int slash = p.indexOf('/');
            return typePath + "/" + (slash >= 0 ? p.substring(slash + 1) : p);
        });
        self.id = canonical;
        cir.setReturnValue(canonical);
    }
}
