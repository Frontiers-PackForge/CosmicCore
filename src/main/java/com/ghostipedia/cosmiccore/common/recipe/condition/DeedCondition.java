package com.ghostipedia.cosmiccore.common.recipe.condition;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.mirror.deed.Deed;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedLedger;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedRegistry;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringUtil;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class DeedCondition extends RecipeCondition<DeedCondition> {

    public ResourceLocation deed;

    public static final MapCodec<DeedCondition> CODEC = RecordCodecBuilder
            .mapCodec(instance -> RecipeCondition.isReverse(instance)
                    .and(ResourceLocation.CODEC.fieldOf("deed").forGetter(val -> val.deed))
                    .apply(instance, DeedCondition::new));

    public static RecipeConditionType<DeedCondition> TYPE;

    public DeedCondition(boolean isReverse, ResourceLocation deed) {
        this.isReverse = isReverse;
        this.deed = deed;
    }

    public DeedCondition(ResourceLocation deed) {
        this(false, deed);
    }

    public DeedCondition() {
        this.deed = CosmicCore.id("first_flame");
    }

    public static void register() {
        TYPE = GTRegistries.register(GTRegistries.RECIPE_CONDITIONS, CosmicCore.id("deed"),
                new RecipeConditionType<>(DeedCondition::new, DeedCondition.CODEC));
    }

    @Override
    public RecipeConditionType<DeedCondition> getType() {
        return TYPE;
    }

    @Override
    public Component getTooltips() {
        Deed known = DeedRegistry.get(deed);
        String nameKey = known != null ? known.nameKey() :
                "deed." + deed.getNamespace() + "." + deed.getPath().replace('/', '.');
        Component name = Component.translatableWithFallback(nameKey, prettify(deed));
        if (isReverse) {
            return Component.translatableWithFallback("cosmiccore.recipe.condition.deed.not.tooltip",
                    "Sol must not have been told: %s", name);
        }
        return Component.translatableWithFallback("cosmiccore.recipe.condition.deed.tooltip",
                "Sol must have been told: %s", name);
    }

    private static String prettify(ResourceLocation id) {
        return StringUtil.isNullOrEmpty(id.getPath()) ? id.toString() : id.getPath().replace('_', ' ');
    }

    @Override
    protected boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        MachineOwner owner = recipeLogic.getMachine().getOwner();
        if (owner == null) return false;
        UUID key = owner.getUUID();
        if (key == null) return false;
        if (!(recipeLogic.getMachine().self().getLevel() instanceof ServerLevel serverLevel)) return false;
        return DeedLedger.get(serverLevel.getServer()).isWoven(key.toString(), deed);
    }

    @Override
    public DeedCondition createTemplate() {
        return new DeedCondition();
    }
}
