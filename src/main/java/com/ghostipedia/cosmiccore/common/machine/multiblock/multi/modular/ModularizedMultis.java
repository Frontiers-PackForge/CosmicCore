package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.modular;

import com.ghostipedia.cosmiccore.common.data.CosmicModularMachines;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.StellarIris;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.StellarStarBallast;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.modular.VomahineShredder.ShredderMultiblock;

// Class for Bulk-Init
public class ModularizedMultis {

    public static void init() {
        // Vomahine Modular Hardware
        ShredderMultiblock.init();

        // MegaStructures
        StellarIris.init();
        StellarStarBallast.init();

        StarLadder.init();
        CosmicModularMachines.init();
    }
}
