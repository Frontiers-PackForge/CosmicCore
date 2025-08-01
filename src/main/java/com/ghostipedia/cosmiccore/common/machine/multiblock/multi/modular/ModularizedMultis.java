package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.modular;

import com.ghostipedia.cosmiccore.common.data.CosmicModularMachines;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.*;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.modular.VomahineShredder.ShredderMultiblock;

// Class for Bulk-Init
public class ModularizedMultis {

    public static void init() {
        // Vomahine Modular Hardware
        ShredderMultiblock.init();

        // MegaStructures
        PrismaticOreFoundry.init();
        StellarIris.init();
        StellarStarBallast.init();
        HemophagicTransfuser.init();
        PlasmiteDistillery.init();
        HeavyAssembler.init();
        WelderMulti.init();
        StarLadder.init();
        VoidMiner.init();
        AtmoPump.init();
        DroneStation.init();
        CosmicModularMachines.init();
        ManaDigitizer.init();
    }
}
