package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.bloomwyrm;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.capability.recipe.CosmicRecipeCapabilities;
import com.ghostipedia.cosmiccore.api.capability.souls.SoulType;
import com.ghostipedia.cosmiccore.api.machine.trait.NotifiableSoulContainer;
import com.ghostipedia.cosmiccore.api.recipe.ingredient.SoulStack;
import com.ghostipedia.cosmiccore.common.machine.multiblock.part.SpawnerHatchPartMachine;
import com.ghostipedia.cosmiccore.common.vitae.CultivationProfile;
import com.ghostipedia.cosmiccore.common.vitae.CultivationProfileManager;
import com.ghostipedia.cosmiccore.common.vitae.EnderIOSpawnerResolver;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.StringSyncValue;
import brachy.modularui.widget.Widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class BiomeldVivariumMachine extends BloomwyrmUnitMachine {

    private static final ResourceLocation VITAE_FLUID = ResourceLocation.fromNamespaceAndPath(
            "neovitae", "essentia_vitae_source");
    private static final ResourceLocation EXPERIENCE_FLUID = ResourceLocation.fromNamespaceAndPath(
            "enderio", "xp_juice");
    private static final int EXPERIENCE_FLUID_PER_POINT = 20;
    private static final String VITAE_BYPRODUCT = "vivarium_vitae";
    private static final String SPIRITUS_BYPRODUCT = "vivarium_spiritus";

    @SaveField
    private int mode;
    @SaveField
    private long cultivationSequence;

    public BiomeldVivariumMachine(BlockEntityCreationInfo info) {
        super(info, new BiomeldVivariumRecipeLogic());
        getRecipeLogic().setKeepSubscribing(true);
    }

    @Override
    public boolean supportsParallelControl() {
        return false;
    }

    @Override
    public boolean usesHeartCycleAllocation() {
        return false;
    }

    public Mode getMode() {
        return Mode.values()[Math.floorMod(mode, Mode.values().length)];
    }

    public int getModeOrdinal() {
        return getMode().ordinal();
    }

    public void setModeOrdinal(int mode) {
        if (hasAllocation() || getRecipeLogic().isActive()) return;
        this.mode = Math.floorMod(mode, Mode.values().length);
        getRecipeLogic().markLastRecipeDirty();
    }

    public Optional<GTRecipe> createCultivationRecipe() {
        Optional<SpawnerHatchPartMachine> spawnerHatch = getSpawnerHatch();
        if (spawnerHatch.isEmpty()) return Optional.empty();

        Optional<ResourceLocation> entity = EnderIOSpawnerResolver.resolve(spawnerHatch.get().getSpawner());
        if (entity.isEmpty()) return Optional.empty();

        Optional<CultivationProfile> profileResult = CultivationProfileManager.INSTANCE.get(entity.get());
        if (profileResult.isEmpty()) return Optional.empty();
        CultivationProfile profile = profileResult.get();
        if (spawnerHatch.get().getTier() < profile.tier().voltageTier()) return Optional.empty();

        Optional<FluidStack> nutrient = findAvailableNutrient(profile);
        if (nutrient.isEmpty()) return Optional.empty();

        var builder = GTRecipeBuilder.of(
                CosmicCore.id("biomeld_vivarium/" + getMode().serializedName() + "/" + entity.get().getPath()),
                CosmicRecipeTypes.BIOMELD_VIVARIUM)
                .inputFluids(nutrient.get())
                .EUt(profile.tier().eut())
                .duration(profile.tier().duration())
                .addData(BloomwyrmRecipeKeys.CHARGE_INPUT, profile.tier().bloomwyrmCharge())
                .addData(BloomwyrmRecipeKeys.MAX_PARALLEL, 1);

        RandomSource random = RandomSource.create(cultivationSeed(entity.get()));
        if (getMode() == Mode.EXPERIENCE) {
            Optional<FluidStack> experience = createExperienceOutput(profile, random);
            if (experience.isEmpty()) return Optional.empty();
            builder.outputFluids(experience.get());
        } else {
            for (ItemStack output : createItemOutputs(profile, random)) {
                builder.outputItems(output);
            }
            builder.addData(VITAE_BYPRODUCT, profile.vitae().units(profile.tier()));
            builder.addData(SPIRITUS_BYPRODUCT, profile.spiritus().units(profile.tier()));
        }
        return Optional.of(builder.build());
    }

    public void advanceCultivationSequence() {
        cultivationSequence++;
        getRecipeLogic().markLastRecipeDirty();
    }

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        List<IWidget> widgets = new ArrayList<>(super.getWidgetsForDisplay(syncManager));
        IntSyncValue modeValue = new IntSyncValue(this::getModeOrdinal);
        StringSyncValue entityValue = new StringSyncValue(() -> getAttunedEntity().map(ResourceLocation::toString)
                .orElse(""));
        StringSyncValue tierValue = new StringSyncValue(() -> getProfile().map(profile -> profile.tier()
                .getSerializedName().toUpperCase(Locale.ROOT)).orElse(""));
        syncManager.syncValue("biomeld_vivarium_mode", modeValue);
        syncManager.syncValue("biomeld_vivarium_entity", entityValue);
        syncManager.syncValue("biomeld_vivarium_tier", tierValue);
        widgets.add(Text.dynamic(() -> Component.translatable(
                Mode.values()[Math.floorMod(modeValue.getIntValue(), Mode.values().length)].translationKey())
                .withStyle(ChatFormatting.AQUA)).asWidget());
        widgets.add(Text.dynamic(() -> entityValue.getStringValue().isEmpty() ?
                Component.translatable("cosmiccore.biomeld_vivarium.unattuned").withStyle(ChatFormatting.RED) :
                Component.translatable(
                        "cosmiccore.biomeld_vivarium.attuned",
                        entityValue.getStringValue(),
                        tierValue.getStringValue()).withStyle(ChatFormatting.GREEN))
                .asWidget());
        return widgets;
    }

    @Override
    public Widget<?> getMainTextPanel(PanelSyncManager syncManager) {
        return BiomeldVivariumDisplayUI.create(this, syncManager);
    }

    private Optional<SpawnerHatchPartMachine> getSpawnerHatch() {
        return getParts().stream()
                .filter(SpawnerHatchPartMachine.class::isInstance)
                .map(SpawnerHatchPartMachine.class::cast)
                .findFirst();
    }

    private Optional<ResourceLocation> getAttunedEntity() {
        return getSpawnerHatch().flatMap(hatch -> EnderIOSpawnerResolver.resolve(hatch.getSpawner()));
    }

    private Optional<CultivationProfile> getProfile() {
        return getAttunedEntity().flatMap(CultivationProfileManager.INSTANCE::get);
    }

    private Optional<FluidStack> findAvailableNutrient(CultivationProfile profile) {
        for (var material : profile.tier().acceptedNutrients()) {
            FluidStack candidate = material.getFluid(profile.tier().nutrientAmount());
            int available = 0;
            for (var handler : getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP)) {
                if (!(handler instanceof NotifiableFluidTank fluidTank)) continue;
                for (var content : fluidTank.getContents()) {
                    if (content instanceof FluidStack stack && candidate.isFluidEqual(stack)) {
                        available += stack.getAmount();
                    }
                }
            }
            if (available >= candidate.getAmount()) return Optional.of(candidate);
        }
        return Optional.empty();
    }

    public void outputCultivationByproducts(GTRecipe recipe) {
        int vitae = Math.max(0, recipe.data.getInt(VITAE_BYPRODUCT));
        int spiritus = Math.max(0, recipe.data.getInt(SPIRITUS_BYPRODUCT));
        for (var handler : getCapabilitiesFlat(IO.OUT, CosmicRecipeCapabilities.SOUL)) {
            if (handler instanceof NotifiableSoulContainer container) {
                container.insertUpTo(new SoulStack(SoulType.Anima, vitae), false);
                container.insertUpTo(new SoulStack(SoulType.Spiritus, spiritus), false);
                return;
            }
        }
        if (vitae <= 0 || !BuiltInRegistries.FLUID.containsKey(VITAE_FLUID)) return;
        List<?> remaining = List
                .of(SizedFluidIngredient.of(new FluidStack(BuiltInRegistries.FLUID.get(VITAE_FLUID), vitae)));
        for (var handler : getCapabilitiesFlat(IO.OUT, FluidRecipeCapability.CAP)) {
            remaining = handler.handleRecipe(IO.OUT, recipe, remaining, false);
            if (remaining.isEmpty()) break;
        }
    }

    private Optional<FluidStack> createExperienceOutput(CultivationProfile profile, RandomSource random) {
        if (!BuiltInRegistries.FLUID.containsKey(EXPERIENCE_FLUID)) return Optional.empty();
        int points = nextInclusive(random, profile.experienceMin(), profile.experienceMax());
        return Optional.of(new FluidStack(
                BuiltInRegistries.FLUID.get(EXPERIENCE_FLUID),
                Math.max(1, points * EXPERIENCE_FLUID_PER_POINT)));
    }

    private List<ItemStack> createItemOutputs(CultivationProfile profile, RandomSource random) {
        List<ItemStack> outputs = new ArrayList<>();
        for (var output : profile.itemOutputs()) {
            if (random.nextDouble() >= output.chance()) continue;
            int count = nextInclusive(random, output.minCount(), output.maxCount());
            if (count <= 0) continue;
            outputs.add(new ItemStack(BuiltInRegistries.ITEM.get(output.item()), count));
        }
        return outputs;
    }

    private long cultivationSeed(ResourceLocation entity) {
        long seed = getBlockPos().asLong();
        seed = seed * 31L + cultivationSequence;
        seed = seed * 31L + entity.hashCode();
        return seed;
    }

    private static int nextInclusive(RandomSource random, int minimum, int maximum) {
        if (maximum <= minimum) return minimum;
        return minimum + random.nextInt(maximum - minimum + 1);
    }

    public enum Mode {

        MATERIAL("material"),
        EXPERIENCE("experience");

        private final String serializedName;

        Mode(String serializedName) {
            this.serializedName = serializedName;
        }

        public String serializedName() {
            return serializedName;
        }

        public String translationKey() {
            return "cosmiccore.biomeld_vivarium.mode." + serializedName;
        }
    }
}
