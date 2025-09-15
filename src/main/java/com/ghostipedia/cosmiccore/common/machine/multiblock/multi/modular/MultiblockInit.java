package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.modular;

import com.ghostipedia.cosmiccore.common.data.CosmicModularMachines;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.*;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.modular.VomahineShredder.ShredderMultiblock;

// Class for Bulk-Init
public class MultiblockInit {

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
        PCBFoundry.init();
        WelderMulti.init();
        CryogenicsChamber.init();
        StarLadder.init();
        VoidMiner.init();
        VoraxReactor.init();
        SpiritCrucible.init();
        AtmoPump.init();
        DroneStation.init();
        CosmicModularMachines.init();
        ManaDigitizer.init();
        ArcaneDistillery.init();
        BioVat.init();
        CelestialBore.init();
        ChromaticDistillery.init();
        ChromaticFlotationPlant.init();
        DrygmyGrove.init();
        HellFireFoundry.init();
        IndustChemVat.init();
        IPBF.init();
        Laminator.init();
        ComponentAssemblyLine.init();
        LargeSpoolingMachine.init();
        LunarHammer.init();
        MantleBore.init();
        NPR.init();
        OrbitalForge.init();
        Polymerizer.init();
        SteamAssembler.init();
        SteamCaster.init();
        SteamMixer.init();
        SufferingChamber.init();
        TitanFusion.init();
    }
}
