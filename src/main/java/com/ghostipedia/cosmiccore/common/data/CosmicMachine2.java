package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.api.machine.part.CosmicPartAbility;
import com.ghostipedia.cosmiccore.common.machine.multiblock.electric.modular.ModularMainTest;
import com.ghostipedia.cosmiccore.common.machine.multiblock.electric.modular.ModuleTest;
import com.ghostipedia.cosmiccore.common.machine.multiblock.part.ModuleConnectorPartMachine;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.abilities;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;

public class CosmicMachine2 {

    static {
        REGISTRATE.creativeModeTab(() -> CosmicCreativeModeTabs.COSMIC_TEST);
    }

    public static final MachineDefinition MODULE_CONNECTOR = REGISTRATE
            .machine("module_connector", ModuleConnectorPartMachine::new)
            .langValue("Module Connector")
            .rotationState(RotationState.Y_AXIS)
            .abilities(CosmicPartAbility.MODULE_CONNECTOR)
            .overlayTieredHullRenderer("module_connector")
            .register();

    public static final MultiblockMachineDefinition MODULAR_MACHINE_BASE = REGISTRATE
            .multiblock("modular_machine_base", ModularMainTest::new)
            .langValue("Modular Base")
            .rotationState(RotationState.NON_Y_AXIS)
            .appearanceBlock(COMPUTER_CASING)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("#INI#")
                    .aisle("ICCCI")
                    .aisle("ICNCI")
                    .aisle("ICCCI")
                    .aisle("#IXI#")
                    .where('X', controller(blocks(definition.get())))
                    .where('N', abilities(CosmicPartAbility.MODULE_CONNECTOR))
                    .where('I', blocks(COMPUTER_CASING.get()).or(abilities(PartAbility.INPUT_ENERGY, PartAbility.MAINTENANCE)))
                    .where('C', blocks(COMPUTER_CASING.get()))
                    .where("#", any())
                    .build()
            )
            .sidedWorkableCasingRenderer("block/casings/hpca/computer_casing", GTCEu.id("block/multiblock/hpca"))
            .register();

    public static final MultiblockMachineDefinition MODULE_TEST = REGISTRATE
            .multiblock("machine_module", ModuleTest::new)
            .langValue("Test Module")
            .rotationState(RotationState.ALL)
            .appearanceBlock(ADVANCED_COMPUTER_CASING)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("N", "C", "C", "X", "C", "C", "N")
                    .where('X', controller(blocks(definition.get())))
                    .where('N', abilities(CosmicPartAbility.MODULE_CONNECTOR))
                    .where('C', blocks(ADVANCED_COMPUTER_CASING.get()))
                    .build())
            .sidedWorkableCasingRenderer("block/casings/hpca/advanced_computer_casing", GTCEu.id("block/multiblock/generator/large_gas_turbine"))
            .register();


    public static void init() {}
}
