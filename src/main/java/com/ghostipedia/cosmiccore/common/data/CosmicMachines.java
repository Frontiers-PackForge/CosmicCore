package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.machine.multiblock.DimensionalEnergyCapacitor;
import com.ghostipedia.cosmiccore.api.machine.multiblock.DimensionalEnergyInterface;
import com.ghostipedia.cosmiccore.api.machine.part.CosmicPartAbility;
import com.ghostipedia.cosmiccore.api.machine.part.DroneMaintenanceInterfacePartMachine;
import com.ghostipedia.cosmiccore.api.machine.part.SteamFluidHatchPartMachine;
import com.ghostipedia.cosmiccore.api.machine.part.WirelessEnergyHatchPartMachine;
import com.ghostipedia.cosmiccore.api.registries.CosmicRegistration;
import com.ghostipedia.cosmiccore.client.renderer.machine.CosmicDynamicRenderHelpers;
import com.ghostipedia.cosmiccore.common.block.debug.CreativeThermiaContainerMachine;
import com.ghostipedia.cosmiccore.common.machine.WirelessChargerMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.electric.hpca.HPCAMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.WirelessDataBankMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.part.*;
import com.ghostipedia.cosmiccore.common.machine.part.WirelessDataSensor;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.client.util.TooltipHelper;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.common.data.models.GTModels;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.ActiveTransformerMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FusionReactorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.PowerSubstationMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.MaintenanceHatchPartMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import it.unimi.dsi.fastutil.Pair;

import java.util.*;
import java.util.function.BiFunction;

import static com.ghostipedia.cosmiccore.api.machine.part.CosmicPartAbility.*;
import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.*;
import static com.ghostipedia.cosmiccore.common.data.CosmicMachinesUtils.*;
import static com.ghostipedia.cosmiccore.common.data.datagen.CosmicMachineModels.*;
import static com.ghostipedia.cosmiccore.common.machine.multiblock.electric.hpca.HPCAMachine.*;
import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.api.pattern.util.RelativeDirection.*;
import static com.gregtechceu.gtceu.common.data.GCYMBlocks.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.GTMachines.CREATIVE_TOOLTIPS;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.DUMMY_RECIPES;
import static com.gregtechceu.gtceu.common.data.machines.GTMachineUtils.*;
import static com.gregtechceu.gtceu.common.data.machines.GTMultiMachines.FUSION_REACTOR;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.*;

public class CosmicMachines {

    static {
        CosmicRegistration.REGISTRATE.creativeModeTab(() -> CosmicCreativeModeTabs.COSMIC_CORE);
    }

    public final static MachineDefinition[] SOUL_IMPORT_HATCH = registerSoulHatch(
            "soul_input_hatch", "Soul Input Hatch",
            IO.IN, HIGH_TIERS, IMPORT_SOUL);
    public static final MachineDefinition[] SOUL_EXPORT_HATCH = registerSoulHatch(
            "soul_output_hatch", "Soul Output Hatch",
            IO.OUT, HIGH_TIERS, CosmicPartAbility.EXPORT_SOUL);

    public static final MachineDefinition[] THERMIA_VENT = registerThermiaTieredHatch(
            "thermia_export_hatch", "Thermia Vent", "thermia_output_hatch",
            IO.OUT, HIGH_TIERS, CosmicPartAbility.EXPORT_THERMIA);
    public static final MachineDefinition[] THERMIA_SOCKET = registerThermiaTieredHatch(
            "thermia_import_hatch", "Thermia Socket", "thermia_input_hatch",
            IO.IN, HIGH_TIERS, CosmicPartAbility.IMPORT_THERMIA);

    public static final MachineDefinition[] WIRELESS_ENERGY_INPUT_HATCH = registerWirelessEnergyTieredHatch(
            "wireless_energy_hatch", "Wireless Energy Hatch", "wireless_energy_1a",
            IO.IN, HIGH_TIERS, 1, PartAbility.INPUT_ENERGY);
    public static final MachineDefinition[] WIRELESS_ENERGY_OUTPUT_DYNAMO = registerWirelessEnergyTieredHatch(
            "wireless_energy_dynamo", "Wireless Energy Dynamo", "wireless_energy_1a",
            IO.OUT, HIGH_TIERS, 1, PartAbility.OUTPUT_ENERGY);
    public static final MachineDefinition[] WIRELESS_ENERGY_INPUT_HATCH_4A = registerWirelessEnergyTieredHatch(
            "4a_wireless_energy_hatch", "4A Wireless Energy Hatch", "wireless_energy_4a",
            IO.IN, HIGH_TIERS, 4, PartAbility.INPUT_ENERGY);
    public static final MachineDefinition[] WIRELESS_ENERGY_OUTPUT_DYNAMO_4A = registerWirelessEnergyTieredHatch(
            "4a_wireless_energy_dynamo", "4A Wireless Energy Dynamo", "wireless_energy_4a",
            IO.OUT, HIGH_TIERS, 4, PartAbility.OUTPUT_ENERGY);
    public static final MachineDefinition[] WIRELESS_ENERGY_INPUT_HATCH_16A = registerWirelessEnergyTieredHatch(
            "16a_wireless_energy_hatch", "16A Wireless Energy Hatch", "wireless_energy_16a",
            IO.IN, HIGH_TIERS, 16, PartAbility.INPUT_ENERGY);
    public static final MachineDefinition[] WIRELESS_ENERGY_OUTPUT_DYNAMO_16A = registerWirelessEnergyTieredHatch(
            "16a_wireless_energy_dynamo", "16A Wireless Energy Dynamo", "wireless_energy_16a",
            IO.OUT, HIGH_TIERS, 16, PartAbility.OUTPUT_ENERGY);

    public static final MachineDefinition[] NAQUAHINE_MINI_REACTOR = CosmicMachinesUtils.registerSimpleGenerator(
            "naquahine_mini_reactor",
            CosmicRecipeTypes.MINI_NAQUAHINE_REACTOR, genericGeneratorTankSizeFunction, 0.0f, GTValues.IV, GTValues.LuV,
            GTValues.ZPM, GTValues.UV, GTValues.UHV);
    public static final Pair<MachineDefinition, MachineDefinition> STEAM_BENDER = CosmicMachinesUtils
            .registerSteamMachines(
                    "steam_bender", SimpleSteamMachine::new, (pressure, builder) -> builder
                            .rotationState(RotationState.NON_Y_AXIS)
                            .recipeType(GTRecipeTypes.BENDER_RECIPES)
                            .recipeModifier(SimpleSteamMachine::recipeModifier)
                            .addOutputLimit(ItemRecipeCapability.CAP, 1)
                            .workableSteamHullModel(pressure, GTCEu.id("block/machines/bender"))
                            .register());
    public static final Pair<MachineDefinition, MachineDefinition> STEAM_WIREMILL = CosmicMachinesUtils
            .registerSteamMachines(
                    "steam_wiremill", SimpleSteamMachine::new, (pressure, builder) -> builder
                            .rotationState(RotationState.NON_Y_AXIS)
                            .recipeType(GTRecipeTypes.WIREMILL_RECIPES)
                            .recipeModifier(SimpleSteamMachine::recipeModifier)
                            .addOutputLimit(ItemRecipeCapability.CAP, 1)
                            .modelProperty(SimpleSteamMachine.VENT_DIRECTION_PROPERTY, RelativeDirection.BACK)
                            .workableSteamHullModel(pressure, GTCEu.id("block/machines/wiremill"))
                            .register());

    public static final MachineDefinition[] COSMIC_PARALLEL_HATCH = registerTieredMachines("cosmic_parallel_hatch",
            CosmicParallelHatchPartMachine::new,
            (tier, builder) -> builder
                    .langValue(switch (tier) {
                        case 7 -> "Ultimate";
                        case 8 -> "Super";
                        case 9 -> "Extreme";
                        case 10 -> "WarpTech";
                        case 15 -> "Paradox";
                        default -> "Simple"; // Should never be hit.
                    } + " Parallel Control Hatch")
                    .rotationState(RotationState.ALL)
                    .abilities(CosmicPartAbility.COSMIC_PARALLEL_HATCH)
                    .workableTieredHullModel(GTCEu.id("block/machines/parallel_hatch_mk" + (tier - 4)))
                    .tooltips(Component.translatable("gtceu.machine.parallel_hatch_mk" + tier + ".tooltip"))
                    .register(),
            ZPM, UV, UHV, UEV, UIV);

    public static final MachineDefinition[] WIRELESS_CHARGER = registerTieredMachines("wireless_charger",
            WirelessChargerMachine::new,
            (tier, builder) -> builder
                    .langValue("%s Wireless Charger".formatted(VN[tier]))
                    .tooltipBuilder((stack, list) -> {
                        list.add(Component.translatable("cosmiccore.wireless_charger.range.single",
                                FormattingUtil.formatNumbers(2048L * (tier - GTValues.HV))));
                        list.add(Component.translatable("cosmiccore.wireless_charger.range.mixed",
                                FormattingUtil.formatNumbers(1024L * (tier - GTValues.HV))));
                    })
                    .workableTieredHullModel(CosmicCore.id("block/overlay/machine/wireless_charger"))
                    .register(),
            GTValues.tiersBetween(HV, UIV));

    // Enable If needed Inside of Dev
    // public static final MultiblockMachineDefinition SOUL_TESTER = REGISTRATE.multiblock("soul_tester",
    // PrimitiveWorkableMachine::new)
    // .rotationState(RotationState.NON_Y_AXIS)
    // .recipeType(CosmicCoreRecipeTypes.SOUL_TESTER_RECIPES)
    // .appearanceBlock(GTBlocks.CASING_PRIMITIVE_BRICKS)
    // .pattern(definition -> FactoryBlockPattern.start()
    // .aisle("S", "C", "I")
    // .where("C", controller(blocks(definition.getBlock())))
    // .where("S", abilities(CosmicPartAbility.IMPORT_SOUL).or(abilities(CosmicPartAbility.EXPORT_SOUL)))
    // .where("I", abilities(PartAbility.EXPORT_ITEMS).or(abilities(PartAbility.IMPORT_ITEMS)))
    // .build())
    // .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_inert_ptfe"),
    // GTCEu.id("block/multiblock/coke_oven"))
    // .register();

    public static final MultiblockMachineDefinition LARGE_COMBUSTION_ENGINE = registerCosmicLargeCombustionEngine(
            "large_combustion_engine_cc", EV,
            CASING_TITANIUM_STABLE, CASING_TITANIUM_GEARBOX, CASING_ENGINE_INTAKE,
            GTCEu.id("block/casings/solid/machine_casing_stable_titanium"),
            GTCEu.id("block/multiblock/generator/large_combustion_engine"));
    public static final MultiblockMachineDefinition EXTREME_COMBUSTION_ENGINE = registerCosmicLargeCombustionEngine(
            "extreme_combustion_engine_cc", IV,
            CASING_TUNGSTENSTEEL_ROBUST, CASING_TUNGSTENSTEEL_GEARBOX, CASING_EXTREME_ENGINE_INTAKE,
            GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel"),
            GTCEu.id("block/multiblock/generator/extreme_combustion_engine"));
    public static final MultiblockMachineDefinition LUDICROUS_COMBUSTION_ENGINE = registerCosmicLargeCombustionEngine(
            "ludicrous_combustion_engine_cc", LuV,
            GILDED_PTHANTERUM_CASING, GEARBOX_PTHANTERUM, CASING_INTAKE_LUDICRIOUS,
            CosmicCore.id("block/casings/solid/gilded_pthanterum_casing"),
            GTCEu.id("block/multiblock/generator/extreme_combustion_engine"));
    public static final MultiblockMachineDefinition ULTIMATE_COMBUSTION_ENGINE = registerCosmicLargeCombustionEngine(
            "ultimate_combustion_engine_cc", ZPM,
            REINFORCED_NAQUADRIA_CASING, GEARBOX_NAQUADRIA, CASING_INTAKE_ULTIMATE,
            CosmicCore.id("block/casings/solid/reinforced_naquadria_casing"),
            GTCEu.id("block/multiblock/generator/extreme_combustion_engine"));

    public static MachineDefinition HPCA_INDICATOR = REGISTRATE
            .machine("hpca_indicator", HPCAIndicatorPartMachine::new)
            .langValue("HPCA Indicator")
            .appearanceBlock(COMPUTER_CASING)
            .model(createOverlayTieredHullMachineModel(CosmicCore.id("block/machine/part/hpca_indicator"))
                    .andThen(b -> b.addDynamicRenderer(CosmicDynamicRenderHelpers::getHPCAIndicatorRender)))
            .tier(ZPM)
            .register();

    public static final MachineDefinition HIGH_PERFORMANCE_COMPUTATION_ARRAY = REGISTRATE
            .multiblock("high_performance_computation_array", HPCAMachine::new)
            .langValue("High Performance Computation Array (HPCA)")
            .rotationState(RotationState.NON_Y_AXIS)
            .appearanceBlock(COMPUTER_CASING)
            .recipeType(DUMMY_RECIPES)
            // Builds from the front top left to the back bottom right. Each aisle iss a vertical slice. We draw each
            // aisle left to right, top to bottom
            .pattern(definition -> FactoryBlockPattern.start(RelativeDirection.LEFT, DOWN, RelativeDirection.BACK)
                    .aisle("AA", "CC", "CC", "CC", "SA")
                    .aisle("BA", "XV", "XV", "XV", "VA")
                    .setRepeatable(MIN_COMPONENTS_SLICES, MAX_COMPONENTS_SLICES)
                    .aisle("AA", "BC", "BC", "BC", "AA")
                    .where('S', controller(blocks(definition.getBlock())))
                    .where('A', blocks(ADVANCED_COMPUTER_CASING.get()))
                    .where('B', blocks(HPCA_INDICATOR.get()))
                    .where('V', blocks(COMPUTER_HEAT_VENT.get()))
                    .where('X', abilities(PartAbility.HPCA_COMPONENT)
                            .or(blocks(Blocks.AIR)))
                    .where('C', blocks(COMPUTER_CASING.get()).setMinGlobalLimited(5)
                            .or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2, 1))
                            .or(abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(1))
                            .or(abilities(PartAbility.COMPUTATION_DATA_TRANSMISSION).setExactLimit(1))
                            .or(autoAbilities(true, false, false)))
                    .build())
            .sidedWorkableCasingModel(GTCEu.id("block/casings/hpca/computer_casing"), GTCEu.id("block/multiblock/hpca"))
            .register();

    private static MachineDefinition[] registerSoulHatch(String name, String displayName, IO io,
                                                         int[] tiers, PartAbility... abilities) {
        return registerTieredMachines(name,
                (holder, tier) -> new SoulHatchPartMachine(holder, tier, io),
                (tier, builder) -> builder
                        .langValue(GTValues.VNF[tier] + ' ' + displayName)
                        .abilities(abilities)
                        .rotationState(RotationState.ALL)
                        .overlayTieredHullModel("soul_hatch")
                        .tooltipBuilder((item, tooltip) -> {
                            if (io == IO.IN)
                                tooltip.add(Component.translatable("tooltip.cosmiccore.soul_hatch.input",
                                        SoulHatchPartMachine.getMaxConsumption(tier)));
                            else
                                tooltip.add(Component.translatable("tooltip.cosmiccore.soul_hatch.output",
                                        SoulHatchPartMachine.getMaxCapacity(tier)));
                        }).register(),
                tiers);
    }

    private static MachineDefinition[] registerWirelessEnergyTieredHatch(String name, String displayName, String model,
                                                                         IO io, int[] tiers, int amperage,
                                                                         PartAbility... abilities) {
        return registerTieredMachines(name,
                (holder, tier) -> new WirelessEnergyHatchPartMachine(holder, tier, io, amperage),
                (tier, builder) -> builder
                        .langValue(VNF[tier] + ' ' + displayName)
                        .abilities(abilities)
                        .rotationState(RotationState.ALL)
                        .tooltips(WirelessEnergyHatchPartMachine.getTooltipComponents(tier, io, amperage))
                        .overlayTieredHullModel(model)
                        .register(),
                tiers);
    }

    private static MachineDefinition[] registerThermiaTieredHatch(String name, String displayName, String model, IO io,
                                                                  int[] tiers, PartAbility... abilities) {
        return registerTieredMachines(name,
                (holder, tier) -> new ThermiaHatchPartMachine(holder, tier, io),
                (tier, builder) -> builder
                        .abilities(abilities)
                        .rotationState(RotationState.ALL)
                        .overlayTieredHullModel(model)
                        .tooltipBuilder((item, tooltip) -> {
                            if (io == IO.IN)
                                tooltip.add(Component.translatable("tooltip.cosmiccore.thermia_hatch_limit",
                                        ThermiaHatchPartMachine.getThermiaLimits(tier)));
                            else
                                tooltip.add(Component.translatable("tooltip.cosmiccore.thermia_hatch_limit",
                                        ThermiaHatchPartMachine.getThermiaLimits(tier)));
                        }).register(),
                tiers);
    }

    public static final MachineDefinition CROP_HOLDER = REGISTRATE.machine("crop_holder", CropHolderPartMachines::new)
            .langValue("Crop Holder")
            .tier(HV)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.IMPORT_ITEMS)
            .modelProperty(RecipeLogic.STATUS_PROPERTY, RecipeLogic.Status.IDLE)
            .model(createWorkableTieredHullMachineModel(GTCEu.id("block/machines/object_holder"))
                    .andThen((ctx, prov, model) -> {
                        model.addReplaceableTextures("bottom", "top", "side");
                    }))
            .register();

    public static final MachineDefinition SENSOR_HATCH = REGISTRATE.machine("sensor_hatch", WirelessDataSensor::new)
            .langValue("Sensor Hatch")
            .rotationState(RotationState.ALL)
            .abilities(CosmicPartAbility.PSS_SENSORS)
            .modelProperty(RecipeLogic.STATUS_PROPERTY, RecipeLogic.Status.IDLE)
            .model(createWorkableTieredHullMachineModel(GTCEu.id("block/machines/object_holder"))
                    .andThen((ctx, prov, model) -> {
                        model.addReplaceableTextures("bottom", "top", "side");
                    }))
            .register();

    public static final MachineDefinition CREATIVE_HEAT = REGISTRATE
            .machine("creative_thermal", CreativeThermiaContainerMachine::new)
            .rotationState(RotationState.NONE)
            .model(createSingleOverlayTieredHullMachineModel(GTModels.BLANK_TEXTURE,
                    GTCEu.id("block/overlay/machine/overlay_energy_emitter")))
            .tooltipBuilder(CREATIVE_TOOLTIPS)
            .register();

    private static MachineDefinition[] registerTieredMachines(String name,
                                                              BiFunction<IMachineBlockEntity, Integer, MetaMachine> factory,
                                                              BiFunction<Integer, MachineBuilder<MachineDefinition>, MachineDefinition> builder,
                                                              int... tiers) {
        MachineDefinition[] definitions = new MachineDefinition[GTValues.TIER_COUNT];
        for (int tier : tiers) {
            var register = REGISTRATE.machine(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name,
                    holder -> factory.apply(holder, tier)).tier(tier);
            definitions[tier] = builder.apply(tier, register);
        }
        return definitions;
    }

    public static final MultiblockMachineDefinition DIMENSIONAL_ENERGY_CAPACITOR = REGISTRATE
            .multiblock("dimensional_energy_capacitor", DimensionalEnergyCapacitor::new)
            .langValue("Power Substation")
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .appearanceBlock(CASING_PALLADIUM_SUBSTATION)
            .tooltips(Component.translatable("gtceu.machine.power_substation.tooltip.0"),
                    Component.translatable("gtceu.machine.power_substation.tooltip.1"),
                    Component.translatable("gtceu.machine.power_substation.tooltip.2",
                            PowerSubstationMachine.MAX_BATTERY_LAYERS),
                    Component.translatable("gtceu.machine.power_substation.tooltip.3"),
                    Component.translatable("gtceu.machine.power_substation.tooltip.4",
                            PowerSubstationMachine.PASSIVE_DRAIN_MAX_PER_STORAGE / 1000),
                    Component.translatable("gtceu.machine.dec.tooltip.0"),
                    Component.translatable("gtceu.machine.dec.tooltip.1"),
                    Component.translatable("gtceu.machine.dec.tooltip.2"),
                    Component.translatable("gtceu.machine.dec.tooltip.3"))
            .pattern(definition -> FactoryBlockPattern.start(RIGHT, BACK, UP)
                    .aisle("XXSXX", "XXXXX", "XXXXX", "XXXXX", "XXXXX")
                    .aisle("XXXXX", "XCCCX", "XCCCX", "XCCCX", "XXXXX")
                    .aisle("GGGGG", "GBBBG", "GBBBG", "GBBBG", "GGGGG")
                    .setRepeatable(1, DimensionalEnergyCapacitor.MAX_BATTERY_LAYER)
                    .aisle("GGGGG", "GGGGG", "GGGGG", "GGGGG", "GGGGG")
                    .where('S', controller(blocks(definition.getBlock())))
                    .where('C', blocks(CASING_PALLADIUM_SUBSTATION.get()))
                    .where('X',
                            blocks(CASING_PALLADIUM_SUBSTATION.get())
                                    .setMinGlobalLimited(DimensionalEnergyCapacitor.MIN_CASINGS)
                                    .or(abilities(PSS_SENSORS))
                                    .or(autoAbilities(true, false, false))
                                    .or(abilities(PartAbility.INPUT_ENERGY, PartAbility.SUBSTATION_INPUT_ENERGY,
                                            PartAbility.INPUT_LASER))
                                    .or(abilities(PartAbility.OUTPUT_ENERGY, PartAbility.SUBSTATION_OUTPUT_ENERGY,
                                            PartAbility.OUTPUT_LASER)))
                    .where('G', blocks(CASING_LAMINATED_GLASS.get()))
                    .where('B', Predicates.powerSubstationBatteries())
                    .build())
            .shapeInfos(definition -> {
                List<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
                MultiblockShapeInfo.ShapeInfoBuilder builder = MultiblockShapeInfo.builder()
                        .aisle("ICSCO", "NCMCT", "GGGGG", "GGGGG", "GGGGG")
                        .aisle("CCCCC", "CCCCC", "GBBBG", "GBBBG", "GGGGG")
                        .aisle("CCCCC", "CCCCC", "GBBBG", "GBBBG", "GGGGG")
                        .aisle("CCCCC", "CCCCC", "GBBBG", "GBBBG", "GGGGG")
                        .aisle("CCCCC", "CCCCC", "GGGGG", "GGGGG", "GGGGG")
                        .where('S', definition, Direction.NORTH)
                        .where('C', CASING_PALLADIUM_SUBSTATION)
                        .where('G', CASING_LAMINATED_GLASS)
                        .where('I', GTMachines.ENERGY_INPUT_HATCH[HV], Direction.NORTH)
                        .where('N', GTMachines.SUBSTATION_ENERGY_INPUT_HATCH[EV], Direction.NORTH)
                        .where('O', GTMachines.ENERGY_OUTPUT_HATCH[HV], Direction.NORTH)
                        .where('T', GTMachines.SUBSTATION_ENERGY_OUTPUT_HATCH[EV], Direction.NORTH)
                        .where('M',
                                ConfigHolder.INSTANCE.machines.enableMaintenance ?
                                        GTMachines.MAINTENANCE_HATCH.getBlock().defaultBlockState().setValue(
                                                GTMachines.MAINTENANCE_HATCH.get().getRotationState().property,
                                                Direction.NORTH) :
                                        CASING_PALLADIUM_SUBSTATION.get().defaultBlockState());

                GTCEuAPI.PSS_BATTERIES.entrySet().stream()
                        // filter out empty batteries in example structures, though they are still
                        // allowed in the predicate (so you can see them on right-click)
                        .filter(entry -> entry.getKey().getCapacity() > 0)
                        .sorted(Comparator.comparingInt(entry -> entry.getKey().getTier()))
                        .forEach(entry -> shapeInfo.add(builder.where('B', entry.getValue().get()).build()));

                return shapeInfo;
            })
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_palladium_substation"),
                    GTCEu.id("block/multiblock/power_substation"))
            .register();

    public static final MultiblockMachineDefinition DIMENSIONAL_ENERGY_INTERFACE = REGISTRATE
            .multiblock("dimensional_energy_interface", DimensionalEnergyInterface::new)
            .langValue("Power Substation Dimensional Interface")
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .appearanceBlock(TRITANIUM_LINED_HEAVY_NEUTRONIUM_CASING)
            .tooltips(Component.translatable("gtceu.machine.active_transformer.tooltip.0"),
                    Component.translatable("gtceu.machine.active_transformer.tooltip.1"))
            .tooltipBuilder(
                    (stack,
                     components) -> components.add(Component.translatable("gtceu.machine.active_transformer.tooltip.2")
                             .append(Component.translatable("gtceu.machine.active_transformer.tooltip.3")
                                     .withStyle(TooltipHelper.RAINBOW_HSL_SLOW))))
            .tooltips(Component.translatable("gtceu.machine.dec.tooltip.4"))
            .pattern((definition) -> FactoryBlockPattern.start()
                    .aisle("XXX", "XXX", "XXX")
                    .aisle("XXX", "XCX", "XXX")
                    .aisle("XMX", "XSX", "XXX")
                    .where('S', controller(blocks(definition.getBlock())))
                    .where('X', blocks(TRITANIUM_LINED_HEAVY_NEUTRONIUM_CASING.get()).setMinGlobalLimited(12)
                            .or(ActiveTransformerMachine.getHatchPredicates()))
                    .where('C', blocks(GTBlocks.SUPERCONDUCTING_COIL.get()))
                    .where('M',
                            blocks(TRITANIUM_LINED_HEAVY_NEUTRONIUM_CASING.get()).or(autoAbilities(true, false, false)))
                    .build())
            .workableCasingModel(CosmicCore.id("block/casings/solid/tritanium_lined_heavy_bolted_neutronium_casing"),
                    GTCEu.id("block/multiblock/data_bank"))
            .register();

    public static final MachineDefinition STEAM_IMPORT_HATCH = REGISTRATE
            .machine("steam_fluid_input_hatch", holder -> new SteamFluidHatchPartMachine(holder, IO.IN, 4000, 1))
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.IMPORT_FLUIDS)
            .colorOverlaySteamHullModel(new ResourceLocation(GTCEu.MOD_ID, "block/overlay/machine/overlay_pipe"),
                    new ResourceLocation(GTCEu.MOD_ID, "block/overlay/machine/overlay_fluid_hatch"),
                    new ResourceLocation(GTCEu.MOD_ID, "block/overlay/machine/overlay_fluid_hatch"))
            .tooltips(Component.translatable("gtceu.machine.steam_fluid_hatch_notice"))
            .langValue("Fluid Input Hatch (Steam)")
            .register();
    public static final MachineDefinition STEAM_EXPORT_HATCH = REGISTRATE
            .machine("steam_fluid_output_hatch", holder -> new SteamFluidHatchPartMachine(holder, IO.OUT, 4000, 1))
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.EXPORT_FLUIDS)
            .colorOverlaySteamHullModel(new ResourceLocation(GTCEu.MOD_ID, "block/overlay/machine/overlay_pipe"),
                    new ResourceLocation(GTCEu.MOD_ID, "block/overlay/machine/overlay_fluid_hatch"),
                    new ResourceLocation(GTCEu.MOD_ID, "block/overlay/machine/overlay_fluid_hatch"))
            .langValue("Fluid Output Hatch (Steam)")
            .register();

    public static final MultiblockMachineDefinition WIRELESS_DATA_TRANSMITTER = REGISTRATE
            .multiblock("wireless_data_transmitter", WirelessDataBankMachine::new)
            .langValue("Wireless Data Transmitter")
            .rotationState(RotationState.NON_Y_AXIS)
            .appearanceBlock(HIGH_POWER_CASING)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("M", "A", "A")
                    .aisle("S", "C", "I")
                    .where("C", controller(blocks(definition.getBlock())))
                    .where("S", abilities(PartAbility.OPTICAL_DATA_RECEPTION))
                    .where("I", abilities(PartAbility.INPUT_ENERGY))
                    .where("M", abilities(PartAbility.MAINTENANCE))
                    .where("A", blocks(HIGH_POWER_CASING.get()))
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/hpca/high_power_casing"),
                    CosmicCore.id("block/multiblock/wireless_data_transmitter"))
            .register();
    public static final MultiblockMachineDefinition LOCAL_POWER_CAPACITOR = REGISTRATE
            .multiblock("capacitor_array", PowerSubstationMachine::new)
            .langValue("Capacitor Array")
            .rotationState(RotationState.NON_Y_AXIS)
            .appearanceBlock(CASING_PALLADIUM_SUBSTATION)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .tooltips(Component.translatable("cosmiccore.machine.capacitor_array.tooltip.0"),
                    Component.translatable("cosmiccore.machine.capacitor_array.tooltip.1"),
                    Component.translatable("cosmiccore.machine.capacitor_array.tooltip.2"))
            .pattern(definition -> FactoryBlockPattern.start(RIGHT, BACK, UP)
                    .aisle("ACA", "AAA", "AAA")
                    .aisle("ABA", "BDB", "ABA")
                    .setRepeatable(1, 11)
                    .aisle("AAA", "AAA", "AAA")
                    .where("C", controller(blocks(definition.getBlock())))
                    .where("A", blocks(CASING_PALLADIUM_SUBSTATION.get())
                            .or(abilities(PartAbility.INPUT_ENERGY, PartAbility.OUTPUT_ENERGY,
                                    PartAbility.SUBSTATION_INPUT_ENERGY,
                                    PartAbility.SUBSTATION_OUTPUT_ENERGY)
                                    .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1))))
                    .where("D", Predicates.powerSubstationBatteries())
                    .where("B", blocks(CASING_LAMINATED_GLASS.get()))
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_palladium_substation"),
                    GTCEu.id("block/multiblock/power_substation"))
            .register();

    public static final MachineDefinition WIRELESS_DATA_HATCH = REGISTRATE
            .machine("wireless_data_hatch", WirelessDataHatchPartMachine::new)
            .langValue("Wireless Data Hatch")
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.DATA_ACCESS)
            .tier(UEV)
            .overlayTieredHullModel("wireless_data_hatch")
            .register();

    public static final MachineDefinition DRONE_MAINTENANCE_INTERFACE = REGISTRATE
            .machine("drone_maintenance_interface",
                    (blockEntity) -> new DroneMaintenanceInterfacePartMachine(blockEntity))
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.MAINTENANCE)
            .tooltips(Component.translatable("gtceu.part_sharing.disabled"))
            // TODO: Remove this property since it can't be taped, and also add proper models
            .modelProperty(MaintenanceHatchPartMachine.MAINTENANCE_TAPED_PROPERTY, false)
            .model(createMaintenanceModel(GTCEu.id("block/machine/part/maintenance_hatch")))
            .tier(HV)
            .langValue("Drone Maintenance Interface")

    public static final MachineDefinition STERILIZATION_HATCH = REGISTRATE
            .machine("sterilization_hatch", (holder) -> new SterilizationHatchPartMachine(holder, ZPM, IO.IN))
            .langValue("Sterilzation Hatch")
            .overlayTieredHullModel("wireless_data_hatch")
            .abilities(STERILIZE_HATCH)

            .register();

    public static void init() {
        GTMultiMachines.LARGE_COMBUSTION_ENGINE.setRecipeTypes(new GTRecipeType[] { DUMMY_RECIPES });
        GTMultiMachines.LARGE_COMBUSTION_ENGINE.setRenderXEIPreview(false);
        GTMultiMachines.LARGE_COMBUSTION_ENGINE.setRenderWorldPreview(false);
        GTMultiMachines.LARGE_PLASMA_TURBINE.setRecipeTypes(new GTRecipeType[] { DUMMY_RECIPES });
        GTMultiMachines.LARGE_PLASMA_TURBINE.setRenderXEIPreview(false);
        GTMultiMachines.LARGE_PLASMA_TURBINE.setRenderWorldPreview(false);
        GTMultiMachines.EXTREME_COMBUSTION_ENGINE.setRecipeTypes(new GTRecipeType[] { DUMMY_RECIPES });
        GTMultiMachines.EXTREME_COMBUSTION_ENGINE.setRenderXEIPreview(false);
        GTMultiMachines.EXTREME_COMBUSTION_ENGINE.setRenderWorldPreview(false);
        // GCYMMachines.MEGA_BLAST_FURNACE.setRecipeTypes(new GTRecipeType[] { DUMMY_RECIPES });
        // GCYMMachines.MEGA_BLAST_FURNACE.setRenderXEIPreview(false);
        // GCYMMachines.MEGA_BLAST_FURNACE.setRenderWorldPreview(false);
        GTMultiMachines.POWER_SUBSTATION.setRenderXEIPreview(false);
        GTMultiMachines.POWER_SUBSTATION.setRenderWorldPreview(false);
        for (MultiblockMachineDefinition definition : FUSION_REACTOR) {
            if (definition == null) continue;
            definition.setPatternFactory(() -> {
                var casing = blocks(FusionReactorMachine.getCasingState(definition.getTier()));
                return FactoryBlockPattern.start()
                        .aisle("###############", "######OGO######", "###############")
                        .aisle("######ICI######", "####GGAAAGG####", "######ICI######")
                        .aisle("####CC###CC####", "###EAAOGOAAE###", "####CC###CC####")
                        .aisle("###C#######C###", "##EKEG###GEKE##", "###C#######C###")
                        .aisle("##C#########C##", "#GAE#######EAG#", "##C#########C##")
                        .aisle("##C#########C##", "#GAG#######GAG#", "##C#########C##")
                        .aisle("#I###########I#", "OAO#########OAO", "#I###########I#")
                        .aisle("#C###########C#", "GAG#########GAG", "#C###########C#")
                        .aisle("#I###########I#", "OAO#########OAO", "#I###########I#")
                        .aisle("##C#########C##", "#GAG#######GAG#", "##C#########C##")
                        .aisle("##C#########C##", "#GAE#######EAG#", "##C#########C##")
                        .aisle("###C#######C###", "##EKEG###GEKE##", "###C#######C###")
                        .aisle("####CC###CC####", "###EAAOGOAAE###", "####CC###CC####")
                        .aisle("######ICI######", "####GGAAAGG####", "######ICI######")
                        .aisle("###############", "######OSO######", "###############")
                        .where('S', controller(blocks(definition.get())))
                        .where('G', blocks(FUSION_GLASS.get()).or(casing))
                        .where('E', casing.or(
                                blocks(PartAbility.INPUT_ENERGY.getBlockRange(definition.getTier(), UV)
                                        .toArray(Block[]::new))
                                        .setMinGlobalLimited(1).setPreviewCount(16)))
                        .where('C', casing)
                        .where('K', blocks(FusionReactorMachine.getCoilState(definition.getTier())))
                        .where('O', casing.or(abilities(PartAbility.EXPORT_FLUIDS))
                                .or(abilities(PartAbility.EXPORT_ITEMS)))
                        .where('A', air())
                        .where('I', casing.or(abilities(PartAbility.IMPORT_FLUIDS).setMinGlobalLimited(2))
                                .or(abilities(PartAbility.IMPORT_ITEMS)))
                        .where('#', any())
                        .build();
            });
        }
    }
}
