package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.machine.part.CosmicPartAbility;
import com.ghostipedia.cosmiccore.api.registries.CosmicRegistration;
import com.ghostipedia.cosmiccore.common.machine.multiblock.electric.MagneticFieldMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.part.SoulHatchPartMachine;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import net.minecraft.network.chat.Component;
import com.gregtechceu.gtceu.utils.GTHashMaps;
import com.gregtechceu.gtceu.utils.ItemStackHashStrategy;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.ghostipedia.cosmiccore.api.pattern.CosmicPredicates.magnetCoils;
import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;

public class CosmicMachines {
    static {
        CosmicRegistration.REGISTRATE.creativeModeTab(() -> CosmicCreativeModeTabs.COSMIC_CORE);
    }

    public static final int[] HIGH_TIERS = GTValues.tiersBetween(GTValues.IV, GTCEuAPI.isHighTier() ? GTValues.OpV : GTValues.UHV);

    public static GTRecipe copyOutputs(GTRecipe recipe, ContentModifier modifier) {
        return new GTRecipe(recipe.recipeType, recipe.getId(), recipe.inputs, recipe.copyContents(recipe.outputs, modifier), recipe.tickInputs, recipe.copyContents(recipe.tickOutputs, modifier), new ArrayList<>(recipe.conditions), new ArrayList<>(recipe.ingredientActions), recipe.data, recipe.duration, recipe.isFuel);
    }

    public final static MachineDefinition[] SOUL_IMPORT_HATCH = registerSoulTieredHatch(
            "soul_input_hatch", "Soul Input Hatch", "soul_hatch.import",
            IO.IN, HIGH_TIERS, CosmicPartAbility.IMPORT_SOUL);

    public static final MachineDefinition[] SOUL_EXPORT_HATCH = registerSoulTieredHatch(
            "soul_output_hatch", "Soul Output Hatch", "soul_hatch.export",
            IO.OUT, HIGH_TIERS, CosmicPartAbility.EXPORT_SOUL);

//Enable If needed Inside of Dev
//    public static final MultiblockMachineDefinition SOUL_TESTER = REGISTRATE.multiblock("soul_tester", PrimitiveWorkableMachine::new)
//            .rotationState(RotationState.NON_Y_AXIS)
//            .recipeType(CosmicCoreRecipeTypes.SOUL_TESTER_RECIPES)
//            .appearanceBlock(GTBlocks.CASING_PRIMITIVE_BRICKS)
//            .pattern(definition -> FactoryBlockPattern.start()
//                    .aisle("S", "C", "I")
//                    .where("C", controller(blocks(definition.getBlock())))
//                    .where("S", abilities(CosmicPartAbility.IMPORT_SOUL).or(abilities(CosmicPartAbility.EXPORT_SOUL)))
//                    .where("I", abilities(PartAbility.EXPORT_ITEMS).or(abilities(PartAbility.IMPORT_ITEMS)))
//                    .build())
//            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_inert_ptfe"), GTCEu.id("block/multiblock/coke_oven"))
//            .register();

    //Terrifying Recipe Modifiers half of this is moonruns to me :lets:
    public final static MultiblockMachineDefinition DRYGMY_GROVE = REGISTRATE.multiblock("drygmy_grove", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.GROVE_RECIPES)
            .recipeModifiers(true,
                    (machine, recipe) -> {
                        if (machine instanceof IRecipeCapabilityHolder holder) {
                            // Find all the items in the combined Item Input inventories and create oversized ItemStacks
                            Object2IntMap<ItemStack> ingredientStacks = Objects.requireNonNullElseGet(holder.getCapabilitiesProxy().get(IO.IN, ItemRecipeCapability.CAP), Collections::<IRecipeHandler<?>>emptyList)
                                    .stream()
                                    .map(container -> container.getContents().stream().filter(ItemStack.class::isInstance).map(ItemStack.class::cast).toList())
                                    .flatMap(container -> GTHashMaps.fromItemStackCollection(container).object2IntEntrySet().stream())
                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Integer::sum, () -> new Object2IntOpenCustomHashMap<>(ItemStackHashStrategy.comparingAllButCount())));
                            ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation("ars_nouveau:drygmy_charm")));
                            //Never let the multiplier be 0 (THIS IS NOT ACTUALLY PARALLEL, It's just being used to to some goober grade math)
                            if (ingredientStacks.getInt(stack) >= 1) {
                                var maxParallel = ingredientStacks.getInt(stack) / 2;
                                recipe = copyOutputs(recipe, ContentModifier.multiplier(maxParallel));
                            }
                        }
                        return recipe;
                    },
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK))
            .appearanceBlock(GTBlocks.CASING_STAINLESS_CLEAN)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("##QQQ##", "##QQQ##", "#######", "#######", "#######", "##QQQ##", "##QQQ##")
                    .aisle("#QQQQQ#", "#QMMMQ#", "#FLBBF#", "#F#B#F#", "#F###F#", "#QGGGQ#", "#QQQQQ#")
                    .aisle("QQQQQQQ", "QMMMMMQ", "#B#####", "#B#####", "#######", "QGP#PGQ", "QQQQQQQ")
                    .aisle("QQQQQQQ", "QMMMMMQ", "#B###B#", "#######", "#######", "QG###GQ", "QQQQQQQ")
                    .aisle("QQQQQQQ", "QMMMMMQ", "####LB#", "#####B#", "#######", "QGP#PGQ", "QQQQQQQ")
                    .aisle("#QQQQQ#", "#QMMMQ#", "#F#BBF#", "#F##BF#", "#F###F#", "#QGGGQ#", "#QQQQQ#")
                    .aisle("##QQQ##", "##QCQ##", "#######", "#######", "#######", "##QQQ##", "##QQQ##")
                    .where('#', any())
                    .where("C", controller(blocks(definition.getBlock())))
                    .where('F', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.StainlessSteel)))
                    .where('M', blocks(Blocks.MOSS_BLOCK))
                    .where('B', blocks(Blocks.AZALEA_LEAVES)
                            .or(blocks(Blocks.FLOWERING_AZALEA_LEAVES)))
                    .where('L', blocks(Blocks.FLOWERING_AZALEA)
                            .or(blocks(Blocks.AZALEA)))
                    .where('P', blocks(GTBlocks.CASING_STEEL_PIPE.get()))
                    .where('G', blocks(Blocks.SEA_LANTERN)) //WHAT THE HELL IS A LAMP BRO - HALP
                    .where('Q', blocks(GTBlocks.CASING_STAINLESS_CLEAN.get())
                            .or(abilities(PartAbility.IMPORT_FLUIDS))
                            .or(abilities(PartAbility.EXPORT_FLUIDS))
                            .or(abilities(PartAbility.IMPORT_ITEMS))
                            .or(abilities(PartAbility.EXPORT_ITEMS))
                            .or(abilities(PartAbility.INPUT_ENERGY))
                            .or(abilities(PartAbility.MAINTENANCE))
                            .or(abilities(CosmicPartAbility.IMPORT_SOUL))
                    ).build())
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_clean_stainless_steel"), GTCEu.id("block/multiblock/data_bank"))
            .register();
    public final static MultiblockMachineDefinition NAQUAHINE_PRESSURE_REACTOR = REGISTRATE.multiblock("naquahine_pressure_reactor", MagneticFieldMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.NAQUAHINE_REACTOR)
            .recipeModifier(GTRecipeModifiers.PARALLEL_HATCH)
            .appearanceBlock(CosmicBlocks.NAQUADAH_PRESSURE_RESISTANT_CASING)
            .generator(true)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("##QQQ##", "##QQQ##", "###Q###", "#######", "#######","#######","#######","#######","#######","#######","###Q###","##QQQ##","##QQQ##")
                    .aisle("#QQQQQ#", "#QQSQQ#", "#FQQQF#", "#FQFQF#", "#F###F#","#F###F#","#F###F#","#F###F#","#F###F#","#FQFQF#","#FQQQF#","#QQSQQ#","#QQQQQ#")
                    .aisle("QQQQQQQ", "QQSSSQQ", "#QSSSQ#", "#QHGHQ#", "#QHGHQ#","#QHGHQ#","#QHGHQ#","#QHGHQ#","#QHGHQ#","#QHGHQ#","#QSSSQ#","QQSSSQQ","QQQQQQQ")
                    .aisle("QQQQQQQ", "QSSSSSQ", "QQSSSQQ", "#FGSGF#", "##GSG##","##GSG##","##GSG##","##GSG##","##GSG##","#FGSGF#","QQSSSQQ","QSSSSSQ","QQQQQQQ")
                    .aisle("QQQQQQQ", "QQSSSQQ", "#QSSSQ#", "#QHGHQ#", "#QHGHQ#","#QHGHQ#","#QHGHQ#","#QHGHQ#","#QHGHQ#","#QHGHQ#","#QSSSQ#","QQSSSQQ","QQQQQQQ")
                    .aisle("#QQQQQ#", "#QQSQQ#", "#FQQQF#", "#FQFQF#", "#F###F#","#F###F#","#F###F#","#F###F#","#F###F#","#FQFQF#","#FQQQF#","#QQSQQ#","#QQQQQ#")
                    .aisle("##QQQ##", "##QCQ##", "###Q###", "#######", "#######","#######","#######","#######","#######","#######","###Q###","##QQQ##","##QQQ##")
                    .where('#', any())
                    .where("C", controller(blocks(definition.getBlock())))
                    .where('F', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.NaquadahAlloy)))
                    .where('S', magnetCoils())
                    .where('H', blocks(CosmicBlocks.RESONANTLY_TUNED_VIRTUE_MELD_CASING.get()))
                    .where('G', blocks(GTBlocks.FUSION_GLASS.get()))
                    .where('Q', blocks(CosmicBlocks.NAQUADAH_PRESSURE_RESISTANT_CASING.get())
                            .or(abilities(PartAbility.IMPORT_FLUIDS))
                            .or(abilities(PartAbility.EXPORT_FLUIDS))
                            .or(abilities(PartAbility.IMPORT_ITEMS))
                            .or(abilities(PartAbility.EXPORT_ITEMS))
                            .or(abilities(PartAbility.INPUT_ENERGY))
                            .or(abilities(PartAbility.MAINTENANCE))
                            .or(abilities(PartAbility.OUTPUT_LASER))
                            .or(abilities(PartAbility.PARALLEL_HATCH))
                            .or(abilities(PartAbility.INPUT_ENERGY))
                    ).build())
            .workableCasingRenderer(CosmicCore.id("block/casings/solid/naquadah_pressure_resistant_casing"), GTCEu.id("block/multiblock/hpca"))

            .register();

    private static MachineDefinition[] registerSoulTieredHatch(String name, String displayName, String model, IO io, int[] tiers, PartAbility... abilities) {
        return registerTieredMachines(name,
                (holder, tier) -> new SoulHatchPartMachine(holder, tier, io),
                (tier, builder) -> builder
                        .langValue(GTValues.VNF[tier] + ' ' + displayName)
                        .abilities(abilities)
                        .rotationState(RotationState.ALL)
                        .overlayTieredHullRenderer(model)
                        .compassNode("soul_hatch")
                        .tooltipBuilder((item, tooltip) -> {
                            if (io == IO.IN)
                                tooltip.add(Component.translatable("tooltip.cosmiccore.soul_hatch.input", SoulHatchPartMachine.getMaxConsumption(tier)));
                            else
                                tooltip.add(Component.translatable("tooltip.cosmiccore.soul_hatch.output", SoulHatchPartMachine.getMaxCapacity(tier)));
                        }).register(), tiers);
    }

    private static MachineDefinition[] registerTieredMachines(String name, BiFunction<IMachineBlockEntity, Integer, MetaMachine> factory, BiFunction<Integer, MachineBuilder<MachineDefinition>, MachineDefinition> builder, int... tiers) {
        MachineDefinition[] definitions = new MachineDefinition[GTValues.TIER_COUNT];
        for (int tier : tiers) {
            var register = REGISTRATE.machine(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name, holder -> factory.apply(holder, tier)).tier(tier);
            definitions[tier] = builder.apply(tier, register);
        }
        return definitions;
    }

    public static void init() {
    }
}
