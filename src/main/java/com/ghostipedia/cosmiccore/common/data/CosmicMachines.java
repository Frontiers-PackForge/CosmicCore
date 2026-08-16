package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.capability.recipe.CosmicRecipeCapabilities;
import com.ghostipedia.cosmiccore.api.machine.multiblock.DimensionalEnergyCapacitor;
import com.ghostipedia.cosmiccore.api.machine.multiblock.DimensionalEnergyInterface;
import com.ghostipedia.cosmiccore.api.machine.part.CosmicPartAbility;
import com.ghostipedia.cosmiccore.api.machine.part.DroneMaintenanceInterfacePartMachine;
import com.ghostipedia.cosmiccore.api.machine.part.SteamFluidHatchPartMachine;
import com.ghostipedia.cosmiccore.api.machine.part.WirelessEnergyHatchPartMachine;
import com.ghostipedia.cosmiccore.api.registries.CosmicRegistration;
import com.ghostipedia.cosmiccore.common.ae2gt.CosmicStockingBusPartMachine;
import com.ghostipedia.cosmiccore.common.ae2gt.CosmicStockingHatchPartMachine;
import com.ghostipedia.cosmiccore.common.block.debug.CreativeThermiaContainerMachine;
import com.ghostipedia.cosmiccore.common.machine.WirelessChargerMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.VacuumDistillationTower;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.WirelessDataBankMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.MEComputationComponentTier;
import com.ghostipedia.cosmiccore.common.machine.multiblock.part.*;
import com.ghostipedia.cosmiccore.common.machine.multiblock.tier.TieredMultiblockPatterns;
import com.ghostipedia.cosmiccore.common.machine.part.WirelessDataSensor;
import com.ghostipedia.cosmiccore.common.power.steam.SteamBoilerTooltips;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;
import com.ghostipedia.nebulaeae2.compute.ComputeTuning;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.*;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifierList;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.client.util.TooltipHelper;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.machines.GCYMMachines;
import com.gregtechceu.gtceu.common.data.machines.GTAEMachines;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.common.data.models.GTModels;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.ActiveTransformerMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FusionReactorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.PowerSubstationMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.primitive.PrimitiveWorkableMachine;
import com.gregtechceu.gtceu.common.mui.GTGuiTheme;
import com.gregtechceu.gtceu.common.mui.GTSingleblockMachinePanels;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.memoization.GTMemoizer;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import it.unimi.dsi.fastutil.Pair;

import java.util.*;
import java.util.function.BiFunction;

import static com.ghostipedia.cosmiccore.api.machine.part.CosmicPartAbility.*;
import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.*;
import static com.ghostipedia.cosmiccore.common.data.CosmicMachinesUtils.*;
import static com.ghostipedia.cosmiccore.common.data.datagen.CosmicMachineModels.createSingleTextureMachineModel;
import static com.ghostipedia.cosmiccore.common.data.recipe.CosmicRecipeModifiers.COSMIC_MODULES;
import static com.ghostipedia.cosmiccore.common.data.recipe.CosmicRecipeModifiers.EBF_TIER_STREAK;
import static com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes.BIO_LAB;
import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.*;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.*;
import static com.gregtechceu.gtceu.common.data.GCYMBlocks.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.GTMachines.CREATIVE_TOOLTIPS;
import static com.gregtechceu.gtceu.common.data.GTMachines.ITEM_IMPORT_BUS;
import static com.gregtechceu.gtceu.common.data.GTRecipeModifiers.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.CENTRIFUGE_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.DUMMY_RECIPES;
import static com.gregtechceu.gtceu.common.data.machines.GTMachineUtils.*;
import static com.gregtechceu.gtceu.common.data.machines.GTMultiMachines.FUSION_REACTOR;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.*;

public class CosmicMachines {

    static {
        CosmicRegistration.REGISTRATE.creativeModeTab(() -> CosmicCreativeModeTabs.COSMIC_CORE);
    }

    private static final int[] EMBER_TIERS = tiersBetween(ULV, ELECTRIC_TIERS[ELECTRIC_TIERS.length - 1]);

    public final static MachineDefinition[] SOUL_IMPORT_HATCH = registerSoulHatch(
            "soul_input_hatch", "Soul Input Hatch",
            IO.IN, HIGH_TIERS, IMPORT_SOUL);
    public static final MachineDefinition[] SOUL_EXPORT_HATCH = registerSoulHatch(
            "soul_output_hatch", "Soul Output Hatch",
            IO.OUT, HIGH_TIERS, EXPORT_SOUL);

    public static final MachineDefinition[] EMBER_IMPORT_HATCH = registerEmberHatch(
            "ember_input_hatch", "Ember Input Hatch",
            IO.IN, EMBER_TIERS, IMPORT_EMBER);
    public static final MachineDefinition[] EMBER_EXPORT_HATCH = registerEmberHatch(
            "ember_output_hatch", "Ember Output Hatch",
            IO.OUT, EMBER_TIERS, EXPORT_EMBER);

    public static final MachineDefinition[] THERMIA_VENT = registerThermiaTieredHatch(
            "thermia_export_hatch", "Thermia Vent", "thermia_output_hatch",
            IO.OUT, HIGH_TIERS, EXPORT_THERMIA);
    public static final MachineDefinition[] THERMIA_SOCKET = registerThermiaTieredHatch(
            "thermia_import_hatch", "Thermia Socket", "thermia_input_hatch",
            IO.IN, HIGH_TIERS, IMPORT_THERMIA);

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

    public static final MachineDefinition[] BLOOMWYRM_POWER_ROOT = registerTieredMachines(
            "bloomwyrm_power_root",
            (holder, tier) -> new EnergyHatchPartMachine(holder, tier, IO.IN, 4),
            (tier, builder) -> builder
                    .langValue(VNF[tier] + " Bloomwyrm Power Root")
                    .rotationState(RotationState.ALL)
                    .abilities(CosmicPartAbility.BLOOMWYRM_POWER_INPUT)
                    .modelProperty(IS_FORMED, false)
                    .tooltips(
                            Component.translatable("gtceu.universal.tooltip.voltage_in",
                                    FormattingUtil.formatNumbers(V[tier]), VNF[tier]),
                            Component.translatable("gtceu.universal.tooltip.amperage_in", 4),
                            Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                    FormattingUtil.formatNumbers(
                                            EnergyHatchPartMachine.getHatchEnergyCapacity(tier, 4))),
                            Component.translatable("gtceu.machine.energy_hatch.input_hi_amp.tooltip"),
                            Component.translatable("cosmiccore.machine.bloomwyrm_power_root.tooltip"))
                    .overlayTieredHullModel(GTCEu.id("block/machine/part/energy_input_hatch_4a"))
                    .register(),
            LV, MV, HV);

    public static final MachineDefinition[] NAQUAHINE_MINI_REACTOR = CosmicMachinesUtils.registerSimpleGenerator(
            "naquahine_mini_reactor",
            CosmicRecipeTypes.MINI_NAQUAHINE_REACTOR, genericGeneratorTankSizeFunction, 0.0f, GTValues.IV, GTValues.LuV,
            GTValues.ZPM, GTValues.UV, GTValues.UHV);
    public static final Pair<MachineDefinition, MachineDefinition> STEAM_BENDER = CosmicMachinesUtils
            .registerSteamMachines(
                    "steam_bender", SimpleSteamMachine::new, (pressure, builder) -> builder
                            .rotationState(RotationState.NON_Y_AXIS)
                            .recipeType(GTRecipeTypes.BENDER_RECIPES)
                            .ui(GTSingleblockMachinePanels.GENERAL_MACHINE)
                            .themeId(i -> i > 0 ? GTGuiTheme.STEEL.getId() : GTGuiTheme.BRONZE.getId())
                            .recipeModifier(SimpleSteamMachine::recipeModifier)
                            .addOutputLimit(ItemRecipeCapability.CAP, 1)
                            .modelProperty(GTMachineModelProperties.VENT_DIRECTION, RelativeDirection.BACK)
                            .workableSteamHullModel(pressure, GTCEu.id("block/machines/bender"))
                            .register());
    public static final Pair<MachineDefinition, MachineDefinition> STEAM_WIREMILL = CosmicMachinesUtils
            .registerSteamMachines(
                    "steam_wiremill", SimpleSteamMachine::new, (pressure, builder) -> builder
                            .rotationState(RotationState.NON_Y_AXIS)
                            .recipeType(GTRecipeTypes.WIREMILL_RECIPES)
                            .ui(GTSingleblockMachinePanels.GENERAL_MACHINE)
                            .themeId(i -> i > 0 ? GTGuiTheme.STEEL.getId() : GTGuiTheme.BRONZE.getId())
                            .recipeModifier(SimpleSteamMachine::recipeModifier)
                            .addOutputLimit(ItemRecipeCapability.CAP, 1)
                            .modelProperty(GTMachineModelProperties.VENT_DIRECTION, RelativeDirection.BACK)
                            .workableSteamHullModel(pressure, GTCEu.id("block/machines/wiremill"))
                            .register());

    public static final MachineDefinition[] COSMIC_PARALLEL_HATCH = registerTieredMachines("cosmic_parallel_hatch",
            CosmicParallelHatchPartMachine::new,
            (tier, builder) -> builder
                    .langValue(switch (tier) {
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

    public static final MachineDefinition[] COSMIC_MODULE_HATCH = registerTieredMachines("module_hatch",
            ModuleHatchPartMachine::new,
            (tier, builder) -> builder
                    .langValue(switch (tier) {
                        case 8 -> "Basic";
                        case 9 -> "Advanced";
                        case 10 -> "Elite";
                        default -> "Simple";
                    } + " Module Hatch")
                    .rotationState(RotationState.ALL)
                    .modelProperty(GTMachineModelProperties.IS_FORMED, false)
                    .abilities(CosmicPartAbility.MODULE_HATCH)
                    // TODO for ghosti: give tooltip and model
                    .overlayTieredHullModel("module_hatch")
                    .tooltips(Component.translatable("gtceu.machine.parallel_hatch_mk_1.tooltip"))
                    .register(),
            UV, UHV, UEV);
    public static final MachineDefinition[] CALX_REACTOR = registerTieredMachines("calx_reactor",
            (holder, tier) -> new SimpleTieredMachine(holder, tier, defaultTankSizeFunction),
            (tier, builder) -> builder
                    .recipeType(CosmicRecipeTypes.CALX_REACTOR)
                    .ui(GTSingleblockMachinePanels.GENERAL_MACHINE)
                    .tooltipBuilder((stack, list) -> {
                        list.add(Component.translatable("cosmiccore.calx_reactor.desc"));
                    })
                    .tooltips(workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64,
                            CosmicRecipeTypes.CALX_REACTOR,
                            defaultTankSizeFunction.applyAsInt(tier), true))
                    .workableTieredHullModel(CosmicCore.id("block/overlay/machine/calx_reactor"))
                    .register(),
            ELECTRIC_TIERS);

    public static final MachineDefinition[] MANA_LEACHING_TUB = registerTieredMachines("mana_leaching_tub",
            (holder, tier) -> new SimpleTieredMachine(holder, tier, defaultTankSizeFunction),
            (tier, builder) -> builder.recipeType(CosmicRecipeTypes.MANA_LEACHING_TUB)
                    .ui(GTSingleblockMachinePanels.GENERAL_MACHINE)
                    .tooltipBuilder((stack, list) -> {
                        list.add(Component.translatable("cosmiccore.mana_leaching_tub.desc"));
                    })
                    .tooltips(workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64,
                            CosmicRecipeTypes.MANA_LEACHING_TUB,
                            defaultTankSizeFunction.applyAsInt(tier), true))
                    .workableTieredHullModel(CosmicCore.id("block/overlay/machine/mana_leaching_tub"))
                    .register(),
            ELECTRIC_TIERS);

    public static final MachineDefinition[] ROASTER = registerTieredMachines("roaster",
            (holder, tier) -> new SimpleTieredMachine(holder, tier, defaultTankSizeFunction),
            (tier, builder) -> builder.recipeType(CosmicRecipeTypes.ROASTER)
                    .ui(GTSingleblockMachinePanels.GENERAL_MACHINE)
                    .tooltipBuilder((stack, list) -> {
                        list.add(Component.translatable("cosmiccore.roaster.desc"));
                    })
                    .tooltips(workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64, CosmicRecipeTypes.ROASTER,
                            defaultTankSizeFunction.applyAsInt(tier), true))
                    .workableTieredHullModel(CosmicCore.id("block/overlay/machine/roaster"))
                    .register(),
            ELECTRIC_TIERS);

    public static final MachineDefinition[] THERMOMAG = registerTieredMachines("thermomagnitizer",
            (holder, tier) -> new SimpleTieredMachine(holder, tier, defaultTankSizeFunction),
            (tier, builder) -> builder.recipeType(CosmicRecipeTypes.THERMOMAG)
                    .ui(GTSingleblockMachinePanels.GENERAL_MACHINE)
                    .tooltipBuilder((stack, list) -> {
                        list.add(Component.translatable("cosmiccore.thermomagnitizer.desc"));
                    })
                    .tooltips(workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64, CosmicRecipeTypes.THERMOMAG,
                            defaultTankSizeFunction.applyAsInt(tier), true))
                    .workableTieredHullModel(CosmicCore.id("block/overlay/machine/thermomagnitizer"))
                    .register(),
            ELECTRIC_TIERS);

    public static final MachineDefinition[] SPOOLING_MACHINE = registerTieredMachines("spooling_machine",
            (holder, tier) -> new SimpleTieredMachine(holder, tier, defaultTankSizeFunction),
            (tier, builder) -> builder.recipeType(CosmicRecipeTypes.SPOOLING_MACHINE)
                    .ui(GTSingleblockMachinePanels.GENERAL_MACHINE)
                    .tooltips(workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64,
                            CosmicRecipeTypes.SPOOLING_MACHINE,
                            defaultTankSizeFunction.applyAsInt(tier), true))
                    .workableTieredHullModel(GTCEu.id("block/machines/wiremill"))
                    .register(),
            ELECTRIC_TIERS);

    public static final MachineDefinition[] LAMINATOR = registerTieredMachines("laminator",
            (holder, tier) -> new SimpleTieredMachine(holder, tier, defaultTankSizeFunction),
            (tier, builder) -> builder
                    .langValue("%s Laminator %s".formatted(VLVH[tier], VLVT[tier]))
                    .recipeType(CosmicRecipeTypes.LAMINATOR)
                    .ui(GTSingleblockMachinePanels.GENERAL_MACHINE)
                    .tooltips(workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64,
                            CosmicRecipeTypes.LAMINATOR, defaultTankSizeFunction.applyAsInt(tier), true))
                    .workableTieredHullModel(GTCEu.id("block/machines/forming_press"))
                    .register(),
            tiersBetween(LV, UIV));

    public static final MachineDefinition[] CHEMICAL_DEHYDRATOR = registerTieredMachines("chemical_dehydrator",
            (holder, tier) -> new SimpleTieredMachine(holder, tier, defaultTankSizeFunction),
            (tier, builder) -> builder
                    .langValue("%s Chemical Dehydrator %s".formatted(VLVH[tier], VLVT[tier]))
                    .recipeType(CosmicRecipeTypes.CHEMICAL_DEHYDRATOR)
                    .ui(GTSingleblockMachinePanels.GENERAL_MACHINE)
                    .tooltipBuilder((stack, list) -> list.add(
                            Component.translatable("cosmiccore.machine.chemical_dehydrator.tooltip")))
                    .tooltips(workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64,
                            CosmicRecipeTypes.CHEMICAL_DEHYDRATOR,
                            defaultTankSizeFunction.applyAsInt(tier), true))
                    .workableTieredHullModel(GTCEu.id("block/machines/thermal_centrifuge"))
                    .register(),
            tiersBetween(LV, UIV));

    public static final MachineDefinition[] CRYSTALLIZER = registerTieredMachines("crystallizer",
            (holder, tier) -> new SimpleTieredMachine(holder, tier, defaultTankSizeFunction),
            (tier, builder) -> builder
                    .langValue("%s Crystallizer %s".formatted(VLVH[tier], VLVT[tier]))
                    .recipeType(CosmicRecipeTypes.CRYSTALLIZER)
                    .recipeModifier(OC_NON_PERFECT)
                    .ui(GTSingleblockMachinePanels.GENERAL_MACHINE)
                    .tooltips(workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64,
                            CosmicRecipeTypes.CRYSTALLIZER,
                            defaultTankSizeFunction.applyAsInt(tier), true))
                    .workableTieredHullModel(GTCEu.id("block/machines/autoclave"))
                    .register(),
            tiersBetween(MV, UIV));

    public static final MachineDefinition[] DESALTER = registerTieredMachines("desalter",
            (holder, tier) -> new SimpleTieredMachine(holder, tier, defaultTankSizeFunction),
            (tier, builder) -> builder
                    .langValue("%s Desalter %s".formatted(VLVH[tier], VLVT[tier]))
                    .recipeType(CosmicRecipeTypes.SIMPLE_DESALTER)
                    .recipeModifier(OC_NON_PERFECT)
                    .ui(GTSingleblockMachinePanels.GENERAL_MACHINE)
                    .tooltips(workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64,
                            CosmicRecipeTypes.SIMPLE_DESALTER,
                            defaultTankSizeFunction.applyAsInt(tier), true))
                    .workableTieredHullModel(GTCEu.id("block/machines/chemical_bath"))
                    .register(),
            tiersBetween(LV, MV));

    public static final MachineDefinition[] VAC_BUBBLER = registerTieredMachines("vacuum_bubbler",
            (holder, tier) -> new SimpleTieredMachine(holder, tier, defaultTankSizeFunction),
            (tier, builder) -> builder
                    .recipeType(CosmicRecipeTypes.VAC_BUBBLER)
                    .ui(GTSingleblockMachinePanels.GENERAL_MACHINE)
                    .tooltipBuilder((stack, list) -> {
                        list.add(Component.translatable("cosmiccore.vacuum_bubbler.desc"));
                    })
                    .tooltips(
                            workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64, CosmicRecipeTypes.VAC_BUBBLER,
                                    defaultTankSizeFunction.applyAsInt(tier), true))
                    .workableTieredHullModel(CosmicCore.id("block/overlay/machine/vacuum_bubbler"))
                    .register(),
            ELECTRIC_TIERS);

    public static final MachineDefinition[] WIRELESS_CHARGER = registerTieredMachines("wireless_charger",
            WirelessChargerMachine::new,
            (tier, builder) -> builder
                    .langValue("%s Wireless Charger".formatted(VN[tier]))
                    .tooltipBuilder((stack, list) -> {
                        list.add(Component.translatable("cosmiccore.wireless_charger.range.single",
                                FormattingUtil.formatNumbers(2048L * (tier - GTValues.MV))));
                        list.add(Component.translatable("cosmiccore.wireless_charger.range.mixed",
                                FormattingUtil.formatNumbers(1024L * (tier - GTValues.MV))));
                    })
                    .workableTieredHullModel(CosmicCore.id("block/overlay/machine/wireless_charger"))
                    .register(),
            GTValues.tiersBetween(HV, UIV));

    // TODO(forestry): INDUSTRIAL_APIARY (IndustrialApiaryMachine + BEES recipes) shelved with Forestry (bead
    // cosmiccore-42.13)

    public static final MachineDefinition[] BIO_LAB_SINGLE = registerTieredMachines("biolab",
            (holder, tier) -> new SimpleTieredMachine(holder, tier, defaultTankSizeFunction), (tier, builder) -> builder
                    .langValue("%s Bio Lab %s".formatted(VLVH[tier], VLVT[tier]))
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(BIO_LAB)
                    .recipeModifier(OC_NON_PERFECT)
                    .ui(GTSingleblockMachinePanels.GENERAL_MACHINE)
                    .workableTieredHullModel(GTCEu.id("block/machines/brewery"))
                    .tooltips(workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64,
                            BIO_LAB, defaultTankSizeFunction.applyAsInt(tier), true))
                    .register(),
            EV, IV, LuV, ZPM, UV);

    public final static MachineDefinition COSMIC_STOCKING_ME_PART_BUS = REGISTRATE
            .machine("cosmic_me_assemblyline_bus", CosmicStockingBusPartMachine::new)
            .langValue("ME Assembly Line Bus")
            .tier(UV)
            .rotationState(RotationState.ALL)
            .abilities(ME_ASSEMBLY_PARTS)
            .modelProperty(GTMachineModelProperties.IS_FORMED, false)
            .overlayTieredHullModel("item_me_assemblyline")
            .tooltips(
                    Component.translatable("cosmiccore.machine.me.stocking_item.tooltip.0"),
                    Component.translatable("cosmiccore.machine.me.stocking_item.tooltip.1"),
                    Component.translatable("cosmiccore.machine.me.stocking_item.tooltip.2"),
                    Component.translatable("cosmiccore.machine.me.stocking_item.tooltip.3"),
                    Component.translatable("cosmiccore.machine.me.stocking_item.tooltip.4"),
                    Component.translatable("cosmiccore.machine.me.stocking_item.tooltip.5"),
                    Component.translatable("cosmiccore.machine.me.stocking_item.tooltip.6"),
                    Component.translatable("gtceu.part_sharing.enabled"))
            .register();

    public static final MachineDefinition[] ME_COMPUTATION_CORES = registerMEComputationComponents(
            MEComputationComponentPartMachine.Role.COMPUTATION_CORE);
    public static final MachineDefinition ME_COMPUTATION_CORE = ME_COMPUTATION_CORES[LV];

    public static final MachineDefinition[] ME_POWER_RELAYS = registerMEComputationComponents(
            MEComputationComponentPartMachine.Role.POWER_RELAY);
    public static final MachineDefinition ME_POWER_RELAY = ME_POWER_RELAYS[LV];

    public static final MachineDefinition ME_COMPUTATION_UPLINK = REGISTRATE
            .machine("me_computation_uplink", MEComputationUplinkPartMachine::new)
            .langValue("ME Computation Uplink")
            .tier(HV)
            .rotationState(RotationState.ALL)
            .abilities(CosmicPartAbility.ME_COMPUTATION_UPLINK)
            .modelProperty(IS_FORMED, false)
            .overlayTieredHullModel(GTCEu.id("block/machine/part/computation_data_hatch"))
            .tooltips(
                    Component.translatable("cosmiccore.machine.me_computation_uplink.tooltip.0"),
                    Component.translatable("cosmiccore.machine.me_computation_uplink.tooltip.1"),
                    Component.translatable("cosmiccore.machine.me_computation_uplink.tooltip.2"),
                    Component.translatable("cosmiccore.machine.me_computation_uplink.tooltip.3"),
                    Component.translatable(
                            "cosmiccore.machine.me_computation_uplink.tooltip.4",
                            ComputeTuning.PHYSICAL_LINK_GROUP_RESERVATION,
                            ComputeTuning.PHYSICAL_LINKS_PER_GROUP),
                    Component.translatable("gtceu.part_sharing.disabled"))
            .register();

    public final static MachineDefinition COSMIC_STOCKING_ME_PART_HATCH = REGISTRATE
            .machine("cosmic_me_assemblyline_hatch", CosmicStockingHatchPartMachine::new)
            .langValue("ME Assembly Line Hatch")
            .tier(UV)
            .rotationState(RotationState.ALL)
            .abilities(ME_ASSEMBLY_PARTS_FLUID)
            .modelProperty(GTMachineModelProperties.IS_FORMED, false)
            .overlayTieredHullModel("fluid_me_assemblyline")
            .tooltips(
                    Component.translatable("cosmiccore.machine.me.stocking_item.tooltip.0"),
                    Component.translatable("cosmiccore.machine.me.stocking_item.tooltip.1"),
                    Component.translatable("cosmiccore.machine.me.stocking_item.tooltip.2"),
                    Component.translatable("cosmiccore.machine.me.stocking_item.tooltip.3"),
                    Component.translatable("cosmiccore.machine.me.stocking_item.tooltip.4"),
                    Component.translatable("cosmiccore.machine.me.stocking_item.tooltip.5"),
                    Component.translatable("cosmiccore.machine.me.stocking_item.tooltip.6"),
                    Component.translatable("gtceu.part_sharing.enabled"))
            .register();

    static {
        REGISTRATE.creativeModeTab(GTCreativeModeTabs.MACHINE);
    }

    public static final MachineDefinition[] ENERGY_INPUT_HATCH_4A = registerEnergyHatches(
            "energy_input_hatch_4a", IO.IN, 4, GTValues.tiersBetween(LV, HV));
    public static final MachineDefinition[] ENERGY_OUTPUT_HATCH_4A = registerEnergyHatches(
            "energy_output_hatch_4a", IO.OUT, 4, GTValues.tiersBetween(ULV, HV));
    public static final MachineDefinition[] ENERGY_INPUT_HATCH_16A = registerEnergyHatches(
            "energy_input_hatch_16a", IO.IN, 16, GTValues.tiersBetween(LV, HV));
    public static final MachineDefinition[] ENERGY_OUTPUT_HATCH_16A = registerEnergyHatches(
            "energy_output_hatch_16a", IO.OUT, 16, GTValues.tiersBetween(ULV, HV));
    public static final MachineDefinition[] ENERGY_INPUT_HATCH_64A = registerEnergyHatches(
            "energy_input_hatch_64a", IO.IN, 64, GTValues.tiersBetween(LV, HV));
    public static final MachineDefinition[] ENERGY_OUTPUT_HATCH_64A = registerEnergyHatches(
            "energy_output_hatch_64a", IO.OUT, 64, GTValues.tiersBetween(LV, HV));

    static {
        REGISTRATE.creativeModeTab(CosmicCreativeModeTabs.COSMIC_CORE);
    }

    // Enable If needed Inside of Dev
    public static final MultiblockMachineDefinition SOUL_TESTER = REGISTRATE
            .multiblock("soul_tester", PrimitiveWorkableMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.SOUL_TESTER_RECIPES)
            .appearanceBlock(GTBlocks.CASING_PRIMITIVE_BRICKS)
            .pattern(definition -> MultiblockPatternBuilder
                    .start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.LEFT)
                    .slice("S", "S", "C", "I", "I")
                    .where('C', controller(blocks(definition.getBlock())))
                    .where('S', abilities(CosmicPartAbility.IMPORT_SOUL).or(abilities(CosmicPartAbility.EXPORT_SOUL)))
                    .where('I', abilities(PartAbility.EXPORT_ITEMS).or(abilities(PartAbility.IMPORT_ITEMS)))
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_inert_ptfe"),
                    GTCEu.id("block/multiblock/coke_oven"))
            .register();

    /*
     * public static final MultiblockMachineDefinition EMBER_TESTER = REGISTRATE.multiblock("ember_tester",
     * PrimitiveWorkableMachine::new)
     * .rotationState(RotationState.NON_Y_AXIS)
     * .recipeType(CosmicRecipeTypes.EMBER_TESTER_RECIPES)
     * .appearanceBlock(GTBlocks.CASING_PRIMITIVE_BRICKS)
     * .pattern(definition -> MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.UP,
     * RelativeDirection.LEFT)
     * .slice("S", "C", "I")
     * .where('C', controller(blocks(definition.getBlock())))
     * .where('S', abilities(IMPORT_EMBER).or(abilities(EXPORT_EMBER)))
     * .where('I', abilities(PartAbility.EXPORT_ITEMS).or(abilities(PartAbility.IMPORT_ITEMS)))
     * .build())
     * .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_inert_ptfe"),
     * GTCEu.id("block/multiblock/coke_oven"))
     * .register();
     */

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

    private static MachineDefinition[] registerSoulHatch(String name, String displayName, IO io,
                                                         int[] tiers, PartAbility... abilities) {
        return registerTieredMachines(name,
                (holder, tier) -> new SoulHatchPartMachine(holder, tier, io),
                (tier, builder) -> builder
                        .langValue(GTValues.VNF[tier] + ' ' + displayName)
                        .abilities(abilities)
                        .rotationState(RotationState.ALL)
                        .modelProperty(GTMachineModelProperties.IS_FORMED, false)
                        .overlayTieredHullModel("soul_hatch")
                        .tooltipBuilder((item, tooltip) -> {
                            if (io == IO.IN)
                                tooltip.add(Component.translatable("tooltip.cosmiccore.soul_hatch.input",
                                        SoulHatchPartMachine.getMaxConsumption(tier)));
                        }).register(),
                tiers);
    }

    private static MachineDefinition[] registerEmberHatch(String name, String displayName, IO io,
                                                          int[] tiers, PartAbility... abilities) {
        return registerTieredMachines(name,
                (holder, tier) -> new EmberHatchPartMachine(holder, tier, io),
                (tier, builder) -> {
                    builder.langValue((tier == GTValues.ULV ? "Steam" : GTValues.VNF[tier]) + ' ' + displayName)
                            .abilities(abilities)
                            .rotationState(RotationState.ALL)
                            .modelProperty(GTMachineModelProperties.IS_FORMED, false);
                    if (tier == GTValues.ULV) {
                        builder.overlaySteamHullModel("ember_hatch");
                    } else {
                        builder.overlayTieredHullModel("ember_hatch");
                    }
                    return builder.tooltipBuilder((item, tooltip) -> {
                        if (io == IO.IN) {
                            tooltip.add(Component.translatable("tooltip.cosmiccore.ember_hatch.capacity",
                                    EmberHatchPartMachine.getMaxCapacity(tier)));
                            tooltip.add(Component.translatable("tooltip.cosmiccore.ember_hatch.consumption",
                                    EmberHatchPartMachine.getMaxConsumption(tier)));
                        } else {
                            tooltip.add(Component.translatable("tooltip.cosmiccore.ember_hatch.capacity",
                                    EmberHatchPartMachine.getMaxCapacity(tier)));
                        }
                    }).register();
                },
                tiers);
    }

    private static MachineDefinition[] registerEnergyHatches(String name, IO io, int amperage, int... tiers) {
        String direction = io == IO.IN ? "input" : "output";
        String tooltipDirection = io == IO.IN ? "in" : "out";
        String hatchName = io == IO.IN ? "Energy Hatch" : "Dynamo Hatch";
        PartAbility ability = io == IO.IN ? PartAbility.INPUT_ENERGY : PartAbility.OUTPUT_ENERGY;
        long capacityMultiplier = io == IO.IN ? 16L : 64L;
        return registerTieredMachines(name,
                (holder, tier) -> new EnergyHatchPartMachine(holder, tier, io, amperage),
                (tier, builder) -> builder
                        .langValue(VNF[tier] + " " + amperage + "A " + hatchName)
                        .rotationState(RotationState.ALL)
                        .abilities(ability)
                        .modelProperty(IS_FORMED, false)
                        .tooltips(
                                Component.translatable("gtceu.universal.tooltip.voltage_" + tooltipDirection,
                                        FormattingUtil.formatNumbers(V[tier]), VNF[tier]),
                                Component.translatable("gtceu.universal.tooltip.amperage_" + tooltipDirection,
                                        amperage),
                                Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                        FormattingUtil.formatNumbers(V[tier] * capacityMultiplier * amperage)),
                                Component.translatable("gtceu.machine.energy_hatch." + direction + "_hi_amp.tooltip"))
                        .overlayTieredHullModel(GTCEu.id(
                                "block/machine/part/energy_" + direction + "_hatch_" + amperage + "a"))
                        .register(),
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
                        .modelProperty(GTMachineModelProperties.IS_FORMED, false)
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
                        .modelProperty(GTMachineModelProperties.IS_FORMED, false)
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
            .modelProperty(GTMachineModelProperties.IS_FORMED, false)
            .abilities(PartAbility.IMPORT_ITEMS)
            .modelProperty(RecipeLogic.STATUS_PROPERTY, RecipeLogic.Status.IDLE)
            .model(createWorkableTieredHullMachineModel(GTCEu.id("block/machines/object_holder"))
                    .andThen((ctx, prov, model) -> {
                        model.addReplaceableTextures("bottom", "top", "side");
                    }))
            .register();

    // TODO(forestry): BEE_HOLDER (BeeHolderPartMachine) shelved with Forestry (bead cosmiccore-42.13)

    public static final MachineDefinition SENSOR_HATCH = REGISTRATE.machine("sensor_hatch", WirelessDataSensor::new)
            .langValue("Sensor Hatch")
            .rotationState(RotationState.ALL)
            .abilities(PSS_SENSORS)
            .modelProperty(GTMachineModelProperties.IS_FORMED, false)
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
            .tier(GTValues.MAX)
            .register();

    private static MachineDefinition[] registerTieredMachines(String name,
                                                              BiFunction<BlockEntityCreationInfo, Integer, MetaMachine> factory,
                                                              BiFunction<Integer, MachineBuilder<MachineDefinition, MetaMachine, ?>, MachineDefinition> builder,
                                                              int... tiers) {
        MachineDefinition[] definitions = new MachineDefinition[GTValues.TIER_COUNT];
        for (int tier : tiers) {
            var register = REGISTRATE.machine(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name,
                    holder -> factory.apply(holder, tier)).tier(tier);
            definitions[tier] = builder.apply(tier, register);
        }
        return definitions;
    }

    private static MachineDefinition[] registerMEComputationComponents(
                                                                       MEComputationComponentPartMachine.Role role) {
        MachineDefinition[] definitions = new MachineDefinition[GTValues.TIER_COUNT];
        for (MEComputationComponentTier componentTier : MEComputationComponentTier.values()) {
            int tier = componentTier.gtTier();
            String baseName = role == MEComputationComponentPartMachine.Role.COMPUTATION_CORE ?
                    "me_computation_core" : "me_power_relay";
            String id = tier == LV ? baseName : GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + baseName;
            String displayName = GTValues.VN[tier] + (role == MEComputationComponentPartMachine.Role.COMPUTATION_CORE ?
                    " ME Computation Core" : " ME Power Relay");
            var builder = REGISTRATE
                    .machine(id, info -> new MEComputationComponentPartMachine(info, role, componentTier))
                    .langValue(displayName)
                    .tier(tier)
                    .rotationState(RotationState.ALL)
                    .abilities(role == MEComputationComponentPartMachine.Role.COMPUTATION_CORE ?
                            CosmicPartAbility.ME_COMPUTATION_CORE : CosmicPartAbility.ME_POWER_RELAY)
                    .appearanceBlock(ULTRA_POWERED_CASING)
                    .model(createSingleTextureMachineModel(
                            CosmicCore.id("block/casings/solid/compute_module_base")));
            definitions[tier] = role == MEComputationComponentPartMachine.Role.COMPUTATION_CORE ?
                    builder.tooltips(
                            Component.translatable(
                                    "cosmiccore.machine.me_computation_core.tooltip.0",
                                    componentTier.coreEuPerTick(),
                                    componentTier.coreCwutPerTick()),
                            Component.translatable(
                                    "cosmiccore.machine.me_computation_core.tooltip.1",
                                    componentTier.coreStandbyEuPerTick()),
                            Component.translatable("gtceu.part_sharing.disabled"))
                            .register() :
                    builder.tooltips(
                            Component.translatable(
                                    "cosmiccore.machine.me_power_relay.tooltip.0",
                                    componentTier.relayEuPerTick()),
                            Component.translatable("gtceu.part_sharing.disabled"))
                            .register();
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
            .pattern(definition -> MultiblockPatternBuilder.start(UP, BACK, RIGHT)
                    .slice("XXSXX", "XXXXX", "XXXXX", "XXXXX", "XXXXX")
                    .slice("XXXXX", "XCCCX", "XCCCX", "XCCCX", "XXXXX")
                    .sliceRepeatable(1, DimensionalEnergyCapacitor.MAX_BATTERY_LAYER, "GGGGG", "GBBBG", "GBBBG",
                            "GBBBG", "GGGGG")
                    .slice("GGGGG", "GGGGG", "GGGGG", "GGGGG", "GGGGG")
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
            // TODO(8.0.0): MultiblockShapeInfo + MultiblockMachineBuilder.shapeInfos() were removed in the
            // GTCEu pattern rewrite. Reimplement the JEI multiblock preview against the new api.multiblock
            // preview system once it's available. Shelved for launch (preview only, not core function).
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
            .pattern((definition) -> MultiblockPatternBuilder
                    .start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.LEFT)
                    .slice("XXX", "XXX", "XXX")
                    .slice("XXX", "XCX", "XXX")
                    .slice("XMX", "XSX", "XXX")
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
            .abilities(PartAbility.IMPORT_FLUIDS, STEAM_IMPORT_FLUIDS)
            .colorOverlaySteamHullModel(
                    ResourceLocation.fromNamespaceAndPath(GTCEu.MOD_ID, "block/overlay/machine/overlay_pipe"),
                    ResourceLocation.fromNamespaceAndPath(GTCEu.MOD_ID, "block/overlay/machine/overlay_fluid_hatch"),
                    ResourceLocation.fromNamespaceAndPath(GTCEu.MOD_ID, "block/overlay/machine/overlay_fluid_hatch"))
            .tooltips(Component.translatable("gtceu.machine.steam_fluid_hatch_notice"))
            .modelProperty(GTMachineModelProperties.IS_FORMED, false)
            .langValue("Fluid Input Hatch (Steam)")
            .register();
    public static final MachineDefinition STEAM_EXPORT_HATCH = REGISTRATE
            .machine("steam_fluid_output_hatch", holder -> new SteamFluidHatchPartMachine(holder, IO.OUT, 4000, 1))
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.EXPORT_FLUIDS)
            .colorOverlaySteamHullModel(
                    ResourceLocation.fromNamespaceAndPath(GTCEu.MOD_ID, "block/overlay/machine/overlay_pipe"),
                    ResourceLocation.fromNamespaceAndPath(GTCEu.MOD_ID, "block/overlay/machine/overlay_fluid_hatch"),
                    ResourceLocation.fromNamespaceAndPath(GTCEu.MOD_ID, "block/overlay/machine/overlay_fluid_hatch"))
            .modelProperty(GTMachineModelProperties.IS_FORMED, false)
            .langValue("Fluid Output Hatch (Steam)")
            .register();

    public static final MultiblockMachineDefinition WIRELESS_DATA_TRANSMITTER = REGISTRATE
            .multiblock("wireless_data_transmitter", WirelessDataBankMachine::new)
            .langValue("Wireless Data Transmitter")
            .rotationState(RotationState.NON_Y_AXIS)
            .appearanceBlock(HIGH_POWER_CASING)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .pattern(definition -> MultiblockPatternBuilder
                    .start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.LEFT)
                    // spotless: off
                    .slice("   AAAAAAA   ", "     BBB     ", "     BBB     ", "             ", "             ",
                            "             ", "             ", "             ", "             ", "             ",
                            "             ", "             ", "             ", "             ", "             ",
                            "             ", "             ", "             ", "             ", "             ",
                            "             ", "             ", "             ", "             ", "             ",
                            "             ", "             ", "             ", "             ")
                    .slice(" AAAAAAAAAAA ", " A         A ", "             ", "             ", "             ",
                            "             ", "             ", "             ", "             ", "             ",
                            "             ", "             ", "             ", "             ", "             ",
                            "             ", "             ", "             ", "             ", "             ",
                            "             ", "             ", "             ", "             ", "             ",
                            "             ", "             ", "             ", "             ")
                    .slice(" ACAACCCAACA ", "  C  CCC  C  ", "  C  CCC  C  ", "  C  CCC  C  ", "  CC  C  CC  ",
                            "      C      ", "             ", "             ", "             ", "             ",
                            "             ", "             ", "             ", "             ", "             ",
                            "             ", "             ", "             ", "             ", "             ",
                            "             ", "             ", "             ", "             ", "             ",
                            "             ", "             ", "             ", "             ")
                    .slice("AAAAAD DAAAAA", "     D D     ", "     D D     ", "     D D     ", "  CCCD DCCC  ",
                            "     D D     ", "             ", "             ", "             ", "             ",
                            "             ", "             ", "             ", "      D      ", "             ",
                            "             ", "             ", "             ", "             ", "      D      ",
                            "             ", "             ", "             ", "             ", "             ",
                            "             ", "             ", "             ", "             ")
                    .slice("AAAAAADAAAAAA", "      D      ", "      D      ", "      D      ", "   CCCDCCC   ",
                            "      D      ", "      D      ", "      D      ", "      D      ", "      D      ",
                            "      D      ", "      D      ", "      D      ", "     DDD     ", "      D      ",
                            "      D      ", "      D      ", "      D      ", "      D      ", "     DDD     ",
                            "      D      ", "      D      ", "             ", "             ", "             ",
                            "             ", "             ", "             ", "             ")
                    .slice("AACDAAAAADCAA", "B CD     DC B", "B CD     DC B", "  CD     DC  ", "   DC   CD   ",
                            "   D     D   ", "             ", "             ", "             ", "             ",
                            "             ", "             ", "             ", "    DD DD    ", "             ",
                            "             ", "             ", "             ", "             ", "    DD DD    ",
                            "             ", "             ", "             ", "      E      ", "             ",
                            "      E      ", "             ", "      E      ", "             ")
                    .slice("AAC DAAAD CAA", "B C D E D C B", "B C D E D C B", "  C D E D C  ", "  C D E D C  ",
                            "  C D E D C  ", "    D E D    ", "    D E D    ", "    D E D    ", "    D E D    ",
                            "    D E D    ", "    D E D    ", "    D E D    ", "   DD E DD   ", "    D E D    ",
                            "    D E D    ", "    D E D    ", "    D E D    ", "    D E D    ", "   DD E DD   ",
                            "    D E D    ", "    D E D    ", "      E      ", "     EEE     ", "      E      ",
                            "     EEE     ", "      E      ", "     EEE     ", "      E      ")
                    .slice("AACDAAAAADCAA", "B CD     DC B", "B CD     DC B", "  CD     DC  ", "   DC   CD   ",
                            "   D     D   ", "             ", "             ", "             ", "             ",
                            "             ", "             ", "             ", "    DD DD    ", "             ",
                            "             ", "             ", "             ", "             ", "    DD DD    ",
                            "             ", "             ", "             ", "      E      ", "             ",
                            "      E      ", "             ", "      E      ", "             ")
                    .slice("AAAAAADAAAAAA", "      D      ", "      D      ", "      D      ", "   CCCDCCC   ",
                            "      D      ", "      D      ", "      D      ", "      D      ", "      D      ",
                            "      D      ", "      D      ", "      D      ", "     DDD     ", "      D      ",
                            "      D      ", "      D      ", "      D      ", "      D      ", "     DDD     ",
                            "      D      ", "      D      ", "             ", "             ", "             ",
                            "             ", "             ", "             ", "             ")
                    .slice("AAAAAD DAAAAA", "     D D     ", "     D D     ", "     D D     ", "  CCCD DCCC  ",
                            "     D D     ", "             ", "             ", "             ", "             ",
                            "             ", "             ", "             ", "      D      ", "             ",
                            "             ", "             ", "             ", "             ", "      D      ",
                            "             ", "             ", "             ", "             ", "             ",
                            "             ", "             ", "             ", "             ")
                    .slice(" ACAACCCAACA ", "  C  CCC  C  ", "  C  CCC  C  ", "  C  CCC  C  ", "  CC  C  CC  ",
                            "      C      ", "             ", "             ", "             ", "             ",
                            "             ", "             ", "             ", "             ", "             ",
                            "             ", "             ", "             ", "             ", "             ",
                            "             ", "             ", "             ", "             ", "             ",
                            "             ", "             ", "             ", "             ")
                    .slice(" AAAAAAAAAAA ", " A         A ", "             ", "             ", "             ",
                            "             ", "             ", "             ", "             ", "             ",
                            "             ", "             ", "             ", "             ", "             ",
                            "             ", "             ", "             ", "             ", "             ",
                            "             ", "             ", "             ", "             ", "             ",
                            "             ", "             ", "             ", "             ")
                    .slice("   AAAAAAA   ", "     BBB     ", "     BQB     ", "             ", "             ",
                            "             ", "             ", "             ", "             ", "             ",
                            "             ", "             ", "             ", "             ", "             ",
                            "             ", "             ", "             ", "             ", "             ",
                            "             ", "             ", "             ", "             ", "             ",
                            "             ", "             ", "             ", "             ")
                    // spotless: on
                    .where('Q', controller(blocks(definition.getBlock())))
                    .where('A', blocks(TRITANIUM_LINED_HEAVY_NEUTRONIUM_CASING.get()))
                    .where('B', blocks(HIGH_POWER_CASING.get())
                            .or(abilities(PartAbility.OPTICAL_DATA_RECEPTION))
                            .or(abilities(PartAbility.INPUT_ENERGY))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where('C', blocks(HIGH_TOLERANCE_RHENIUM_CASING.get()))
                    .where('D', frames(GTMaterials.Neutronium))
                    .where('E', blocks(REFLECTIVE_STARMETAL_CASING.get()))
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
                    Component.translatable("cosmiccore.machine.capacitor_array.tooltip.1"))
            .pattern(definition -> MultiblockPatternBuilder.start(RIGHT, BACK, UP)
                    .slice("ACA", "AAA", "AAA")
                    .sliceRepeatable(1, 11, "ABA", "BDB", "ABA")
                    .slice("AAA", "AAA", "AAA")
                    .where('C', controller(blocks(definition.getBlock())))
                    .where('A', blocks(CASING_PALLADIUM_SUBSTATION.get())
                            .or(abilities(PartAbility.INPUT_ENERGY, PartAbility.OUTPUT_ENERGY,
                                    PartAbility.SUBSTATION_INPUT_ENERGY,
                                    PartAbility.SUBSTATION_OUTPUT_ENERGY)
                                    .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1))))
                    .where('D', Predicates.powerSubstationBatteries())
                    .where('B', blocks(CASING_LAMINATED_GLASS.get()))
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_palladium_substation"),
                    GTCEu.id("block/multiblock/power_substation"))
            .register();

    public static final MachineDefinition WIRELESS_DATA_HATCH = REGISTRATE
            .machine("wireless_data_hatch", WirelessDataHatchPartMachine::new)
            .langValue("Wireless Data Hatch")
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.DATA_ACCESS)
            .modelProperty(GTMachineModelProperties.IS_FORMED, false)
            .tier(UEV)
            .overlayTieredHullModel("wireless_data_hatch")
            .register();

    public static final MachineDefinition DRONE_MAINTENANCE_INTERFACE = REGISTRATE
            .machine("drone_maintenance_interface",
                    (blockEntity) -> new DroneMaintenanceInterfacePartMachine(blockEntity))
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.MAINTENANCE)
            .tooltips(Component.translatable("gtceu.part_sharing.disabled"))
            .tier(EV)
            .modelProperty(GTMachineModelProperties.IS_FORMED, false)
            .overlayTieredHullModel("drone_hatch_maint")
            .langValue("Drone Maintenance Interface")
            .register();

    public static final MachineDefinition STERILIZATION_HATCH = REGISTRATE
            .machine("sterilization_hatch", (holder) -> new SterilizationHatchPartMachine(holder, ZPM, IO.IN, 16000))
            .langValue("Sterilzation Hatch")
            .rotationState(RotationState.ALL)
            .tier(ZPM)
            .modelProperty(GTMachineModelProperties.IS_FORMED, false)
            .overlayTieredHullModel("cleaning_cover")
            .abilities(STERILIZE_HATCH)
            .register();

    public static final MachineDefinition HUGE_STERILIZATION_HATCH = REGISTRATE
            .machine("huge_sterilization_hatch",
                    (holder) -> new SterilizationHatchPartMachine(holder, ZPM, IO.IN, 16000))
            .langValue("Sterilzation Hatch")
            .rotationState(RotationState.ALL)
            .tier(ZPM)
            .modelProperty(GTMachineModelProperties.IS_FORMED, false)
            .overlayTieredHullModel("cleaning_cover")
            .abilities(STERILIZE_HATCH)
            .register();

    public static final MachineDefinition[] FLUID_IMPORT_HATCH_4X_HV = CosmicMachinesUtils.registerFluidHatches(
            "input_hatch_4x", "Quadruple Input Hatch", "fluid_hatch.import",
            IO.IN, FluidHatchPartMachine.INITIAL_TANK_CAPACITY_4X, 4,
            new int[] { GTValues.HV },
            PartAbility.IMPORT_FLUIDS, PartAbility.IMPORT_FLUIDS_4X);

    public static final MachineDefinition[] FLUID_EXPORT_HATCH_4X_HV = CosmicMachinesUtils.registerFluidHatches(
            "output_hatch_4x", "Quadruple Output Hatch", "fluid_hatch.export",
            IO.OUT, FluidHatchPartMachine.INITIAL_TANK_CAPACITY_4X, 4,
            new int[] { GTValues.HV },                                   // only HV
            PartAbility.EXPORT_FLUIDS, PartAbility.EXPORT_FLUIDS_4X);

    public static final MultiblockMachineDefinition LARGE_STEAM_TURBINE = registerLargeTurbineCosmic(
            "steam_large_turbine",
            HV,
            GTRecipeTypes.STEAM_TURBINE_FUELS,
            CASING_STEEL_TURBINE, CASING_STEEL_GEARBOX,
            GTCEu.id("block/casings/mechanic/machine_casing_turbine_steel"),
            GTCEu.id("block/multiblock/generator/large_steam_turbine"),
            false);

    // Dreamer's Basin is now registered in DreamersBasin.java

    // Alveary casing texture (single texture for all faces)
    private static final ResourceLocation ALVEARY_CASING_TEXTURE = CosmicCore.id("block/casings/solid/alveary_casing");

    /**
     * Creates an alveary part model with overlay on all 4 horizontal faces.
     * Uses custom template that renders overlay on north/south/east/west.
     */
    public static MachineBuilder.ModelInitializer createAlvearyPartModel(
                                                                         ResourceLocation casingTexture,
                                                                         ResourceLocation overlayTexture) {
        return (ctx, prov, builder) -> {
            ResourceLocation alvearyTemplate = CosmicCore.id("block/machine/template/part/alveary_part_machine");

            var baseModel = prov.models().withExistingParent(ctx.getName() + "_base", alvearyTemplate);
            baseModel.texture("bottom", casingTexture);
            baseModel.texture("top", casingTexture);
            baseModel.texture("side", casingTexture);

            builder.forAllStatesModels(state -> prov.models().nested().parent(baseModel)
                    .texture("overlay", overlayTexture)
                    .texture("overlay_emissive", GTCEu.id("block/void")));
        };
    }

    // TODO(forestry): Alveary modifier casings (14 ALVEARY_*_PART + registerAlvearyModifier /
    // AlvearyModifierPartMachine) shelved with Forestry (bead cosmiccore-42.13)

    public static void init() {
        SteamBoilerTooltips.init();
        GTRecipeTypes.MIXER_RECIPES.setMaxSize(IO.IN, CosmicRecipeCapabilities.EMBER, 1);
        TieredMultiblockPatterns.register(GTMultiMachines.ELECTRIC_BLAST_FURNACE,
                () -> MultiblockPatternBuilder.start(FRONT, UP, LEFT)
                        .slice("XXXXX", "FRRRF", "F   F", "F   F", "F   F", "FRRRF", "XXXXX")
                        .slice("XRRRX", "RRRRR", " CCC ", " CCC ", " CCC ", "RRRRR", "XXXXX")
                        .slice("XRRRX", "RRRRR", " C C ", " C C ", " C C ", "RRRRR", "XXMXX")
                        .slice("XRRRX", "RRRRR", " CCC ", " CCC ", " CCC ", "RRRRR", "XXXXX")
                        .slice("XXSXX", "FRRRF", "F   F", "F   F", "F   F", "FRRRF", "XXXXX")
                        .where('S', controller(blocks(GTMultiMachines.ELECTRIC_BLAST_FURNACE.getBlock())))
                        .where('X', blocks(CASING_INVAR_HEATPROOF.get()).setMinGlobalLimited(32)
                                .or(autoAbilities(GTMultiMachines.ELECTRIC_BLAST_FURNACE.getRecipeTypes()))
                                .or(autoAbilities(true, false, false)))
                        .where('M', abilities(PartAbility.MUFFLER))
                        .where('F', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.StainlessSteel)))
                        .where('R', blocks(SOMARUST_CASING.get()))
                        .where('C', blocks(COIL_CUPRONICKEL.get()))
                        .where(' ', any())
                        .build());
        GTMultiMachines.ELECTRIC_BLAST_FURNACE.setRecipeModifier(new RecipeModifierList(
                GTMultiMachines.ELECTRIC_BLAST_FURNACE.getRecipeModifier(), EBF_TIER_STREAK));
        GTMultiMachines.ELECTRIC_BLAST_FURNACE.setAlwaysTryModifyRecipe(true);
        GTMultiMachines.LARGE_COMBUSTION_ENGINE.setRecipeTypes(new GTRecipeType[] { DUMMY_RECIPES });
        GTMultiMachines.LARGE_COMBUSTION_ENGINE.setRenderXEIPreview(false);
        GTMultiMachines.LARGE_COMBUSTION_ENGINE.setRenderWorldPreview(false);
        GTMultiMachines.LARGE_PLASMA_TURBINE.setRecipeTypes(new GTRecipeType[] { DUMMY_RECIPES });
        GTMultiMachines.LARGE_PLASMA_TURBINE.setRenderXEIPreview(false);
        GTMultiMachines.LARGE_PLASMA_TURBINE.setRenderWorldPreview(false);
        GTMultiMachines.EXTREME_COMBUSTION_ENGINE.setRecipeTypes(new GTRecipeType[] { DUMMY_RECIPES });
        GTMultiMachines.EXTREME_COMBUSTION_ENGINE.setRenderXEIPreview(false);
        GTMultiMachines.EXTREME_COMBUSTION_ENGINE.setRenderWorldPreview(false);
        GTMultiMachines.ASSEMBLY_LINE.setRecipeModifier(new RecipeModifierList(
                DEFAULT_ENVIRONMENT_REQUIREMENT, COSMIC_MODULES, OC_NON_PERFECT));
        GTMultiMachines.ASSEMBLY_LINE.setAlwaysTryModifyRecipe(true);
        // TODO(cosmiccore-42.14): GCYMMachines.MEGA_BLAST_FURNACE removed in GTCEu 8.0
        GTMultiMachines.POWER_SUBSTATION.setRenderXEIPreview(false);
        GTMultiMachines.POWER_SUBSTATION.setRenderWorldPreview(false);

        GTMultiMachines.LARGE_STEAM_TURBINE.setRecipeTypes(new GTRecipeType[] { DUMMY_RECIPES });
        GTMultiMachines.LARGE_STEAM_TURBINE.setRenderXEIPreview(false);
        GTMultiMachines.LARGE_STEAM_TURBINE.setRenderWorldPreview(false);

        for (MachineDefinition miner : GTMachines.MINER) {
            if (miner == null) continue;
            miner.setRecipeTypes(new GTRecipeType[] { DUMMY_RECIPES });
        }
        GTMachines.STEAM_MINER.first().setRecipeTypes(new GTRecipeType[] { DUMMY_RECIPES });
        GTMachines.STEAM_MINER.second().setRecipeTypes(new GTRecipeType[] { DUMMY_RECIPES });

        for (MultiblockMachineDefinition largeMiner : GTMultiMachines.LARGE_MINER) {
            if (largeMiner == null) continue;
            largeMiner.setRecipeTypes(new GTRecipeType[] { DUMMY_RECIPES });
            largeMiner.setRenderXEIPreview(false);
            largeMiner.setRenderWorldPreview(false);
        }

        for (MultiblockMachineDefinition definition : FUSION_REACTOR) {
            if (definition == null) continue;
            definition.setPattern("main", GTMemoizer.memoize(() -> {
                var casing = blocks(FusionReactorMachine.getCasingState(definition.getTier()));
                return MultiblockPatternBuilder
                        .start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.LEFT)
                        .slice("###############", "######OGO######", "###############")
                        .slice("######ICI######", "####GGAAAGG####", "######ICI######")
                        .slice("####CC###CC####", "###EAAOGOAAE###", "####CC###CC####")
                        .slice("###C#######C###", "##EKEG###GEKE##", "###C#######C###")
                        .slice("##C#########C##", "#GAE#######EAG#", "##C#########C##")
                        .slice("##C#########C##", "#GAG#######GAG#", "##C#########C##")
                        .slice("#I###########I#", "OAO#########OAO", "#I###########I#")
                        .slice("#C###########C#", "GAG#########GAG", "#C###########C#")
                        .slice("#I###########I#", "OAO#########OAO", "#I###########I#")
                        .slice("##C#########C##", "#GAG#######GAG#", "##C#########C##")
                        .slice("##C#########C##", "#GAE#######EAG#", "##C#########C##")
                        .slice("###C#######C###", "##EKEG###GEKE##", "###C#######C###")
                        .slice("####CC###CC####", "###EAAOGOAAE###", "####CC###CC####")
                        .slice("######ICI######", "####GGAAAGG####", "######ICI######")
                        .slice("###############", "######OSO######", "###############")
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
            }));
        }
        GCYMMachines.LARGE_CENTRIFUGE.setPattern("main",
                GTMemoizer.memoize(() -> MultiblockPatternBuilder
                        .start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.LEFT)
                        .slice("#XXX#", "XXXXX", "#XXX#")
                        .slice("XXXXX", "XAPAX", "XXXXX")
                        .slice("XXXXX", "XPAPX", "XXXXX")
                        .slice("XXXXX", "XAPAX", "XXXXX")
                        .slice("#XXX#", "XXSXX", "#XXX#")
                        .where('S', controller(blocks(GCYMMachines.LARGE_CENTRIFUGE.getBlock())))
                        .where('X', blocks(CASING_VIBRATION_SAFE.get()).setMinGlobalLimited(40)
                                .or(Predicates.autoAbilities(CENTRIFUGE_RECIPES))
                                .or(Predicates.autoAbilities(true, false, true))
                                .or(abilities(STERILIZE_HATCH).setMaxGlobalLimited(1, 1)))
                        .where('P', Predicates.blocks(CASING_STEEL_PIPE.get()))
                        .where('A', Predicates.air())
                        .where('#', Predicates.any())
                        .build()));

        GTMultiMachines.DISTILLATION_TOWER
                .setAppearance(LIGHTWEIGHT_STAINLESS_STEEL_CASING::getDefaultState);
        GTMultiMachines.DISTILLATION_TOWER.setPattern("main", GTMemoizer.memoize(() -> {
            PatternPredicate exportPredicate = abilities(PartAbility.EXPORT_FLUIDS_1X);
            if (GTCEu.Mods.isAE2Loaded()) {
                exportPredicate = exportPredicate.or(blocks(GTAEMachines.FLUID_EXPORT_HATCH_ME.get()));
            }
            exportPredicate.setMaxLayerLimited(1);
            PatternPredicate maintenance = autoAbilities(true, false, false).setMaxGlobalLimited(1);
            return MultiblockPatternBuilder.start(UP, FRONT, LEFT)
                    .slice(" AAA ", "AAAAA", "AAAAA", "AAAAA", " ABA ")
                    .sliceRepeatable(1, 12, " CCC ", "C###C", "C###C", "C###C", " CCC ")
                    .slice(" DDD ", "DDDDD", "DDEDD", "DDDDD", " DDD ")
                    .where('A', blocks(LIGHTWEIGHT_STAINLESS_STEEL_CASING.get())
                            .or(abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(1))
                            .or(abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(1))
                            .or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1)
                                    .setMaxGlobalLimited(2))
                            .or(abilities(PartAbility.IMPORT_FLUIDS).setExactLimit(1))
                            .or(maintenance))
                    .where('B', controller(blocks(GTMultiMachines.DISTILLATION_TOWER.getBlock())))
                    .where('C', blocks(LIGHTWEIGHT_STAINLESS_STEEL_CASING.get()).or(exportPredicate))
                    .where('D', blocks(LIGHTWEIGHT_STAINLESS_STEEL_CASING.get()).or(exportPredicate))
                    .where('E', abilities(PartAbility.MUFFLER).setExactLimit(1))
                    .where('#', air())
                    .where(' ', any())
                    .build();
        }));
        VacuumDistillationTower.init();

        GTMultiMachines.ASSEMBLY_LINE.setPattern("main",
                GTMemoizer.memoize(() -> MultiblockPatternBuilder.start(BACK, UP, RIGHT)
                        .slice("FIF", "RTR", "SAG", "#Y#")
                        .sliceRepeatable(3, 15, "FIF", "RTR", "DAG", "#Y#")
                        .slice("FOF", "RTR", "DAG", "#Y#")
                        .where('S', Predicates.controller(blocks(GTMultiMachines.ASSEMBLY_LINE.getBlock())))
                        .where('F', blocks(CASING_STEEL_SOLID.get())
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS_1X, ME_ASSEMBLY_PARTS_FLUID)
                                        .setMaxGlobalLimited(4)))
                        .where('O',
                                Predicates.abilities(PartAbility.EXPORT_ITEMS)
                                        .addTooltips(Component.translatable("gtceu.multiblock.pattern.location_end")))
                        .where('Y',
                                blocks(CASING_STEEL_SOLID.get()).or(Predicates.abilities(PartAbility.INPUT_ENERGY)
                                        .setMinGlobalLimited(1).setMaxGlobalLimited(2)))
                        .where('I', blocks(ITEM_IMPORT_BUS[0].getBlock()).or(Predicates.abilities(ME_ASSEMBLY_PARTS)))
                        .where('G', blocks(CASING_GRATE.get()))
                        .where('A', blocks(CASING_ASSEMBLY_CONTROL.get()))
                        .where('R', blocks(CASING_LAMINATED_GLASS.get()))
                        .where('T', blocks(CASING_ASSEMBLY_LINE.get()))
                        .where('D',
                                Predicates.abilities(PartAbility.DATA_ACCESS, PartAbility.OPTICAL_DATA_RECEPTION)
                                        .setExactLimit(1)
                                        .or(Predicates.abilities(MODULE_HATCH).setMaxGlobalLimited(1, 1))
                                        .or(blocks(CASING_GRATE.get())))
                        .where('#', Predicates.any())
                        .build()));
    }
}
