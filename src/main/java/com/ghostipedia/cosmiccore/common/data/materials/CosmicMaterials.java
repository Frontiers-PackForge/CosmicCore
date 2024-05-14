package com.ghostipedia.cosmiccore.common.data.materials;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;
import com.gregtechceu.gtceu.api.fluids.FluidState;
import com.gregtechceu.gtceu.api.registry.registrate.forge.GTFluidBuilder;
import com.gregtechceu.gtceu.common.data.GTElements;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.misc.alloyblast.CustomAlloyBlastRecipeProducer;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public class CosmicMaterials {
    public static Material Prisma;
    public static Material Virtue;
    public static Material PrismaticTungstensteel;
    public static Material ResonantVirtueMeld;
    public static Material NaquadicSuperalloy;
    public static Material Trinavine;
    public static Material LivingIgniclad;
    public static Material PsionicGalvorn;
    public static Material ProgrammableMatter;
    public static Material ShimmeringNeutronium;
    public static Material CausalFabric;

    public static void register() {
        Prisma = new Material.Builder(CosmicCore.id("prisma"))
                .liquid(new FluidBuilder().state(FluidState.LIQUID).customStill())
                .element(CosmicElements.Pi)
                .buildAndRegister();
        Virtue = new Material.Builder(CosmicCore.id("virtue_meld"))
                .liquid(new FluidBuilder().temperature(666))
                .element(CosmicElements.ViR)
                .buildAndRegister();
        PrismaticTungstensteel = new Material.Builder(CosmicCore.id("prismatic_tungstensteel"))
                .ingot()
                .liquid(new FluidBuilder().temperature(933))
                .color(0x6f42cf).secondaryColor(0xc71414).iconSet(MaterialIconSet.BRIGHT)
                .flags(GENERATE_ROUND,GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING, GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE)
                .components(Prisma, 1, TungstenSteel, 1)
                .rotorStats(10.0f, 2.0f, 128)
                .cableProperties(GTValues.V[4], 1, 1)
                .fluidPipeProperties(1166, 100, true)
                .blastTemp(3600, BlastProperty.GasTier.HIGH, GTValues.VA[GTValues.EV], 1200)
                .buildAndRegister();
        ResonantVirtueMeld = new Material.Builder(CosmicCore.id("resonant_virtue_meld"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1240))
                .color(0xff8400).secondaryColor(0xcffee00).iconSet(MaterialIconSet.SHINY)
                .flags(GENERATE_BOLT_SCREW,GENERATE_ROUND,GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING, GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE)
                .components(Virtue, 4, Naquadah, 1,TungstenSteel, 15, Chromium, 3, Molybdenum, 6, Vanadium, 3,Prisma,8)
                .cableProperties(GTValues.V[5], 4, 6)
                .blastTemp(4200, BlastProperty.GasTier.HIGHER, GTValues.VA[GTValues.IV], 1800)
                .buildAndRegister().setFormula("ERROR4Nq((FeW)5CrMo2V)3Pi8");
        NaquadicSuperalloy = new Material.Builder(CosmicCore.id("naquadric_superalloy"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1300))
                .color(0x001f04).secondaryColor(0x000000).iconSet(MaterialIconSet.RADIOACTIVE)
                .flags(GENERATE_BOLT_SCREW,GENERATE_ROUND,GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING, GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE)
                .components(BlackSteel, 4, NaquadahEnriched, 3, RhodiumPlatedPalladium, 6)
                .cableProperties(GTValues.V[6], 4, 6)
                .blastTemp(5400, BlastProperty.GasTier.HIGHER, GTValues.VA[GTValues.LuV],2400)
                .buildAndRegister();
        //TODO: Pack Progression Materials in core when needed. For now things below this are sorted out well enough - I don't really feel like having to write 10000000 elements and placeholder materials just to do component based auto-gen rn
        Trinavine = new Material.Builder(CosmicCore.id("trinavine"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1500))
                .color(0xdb5e5e).secondaryColor(0x5e0f3d).iconSet(MaterialIconSet.BRIGHT)
                .flags(GENERATE_BOLT_SCREW,GENERATE_ROUND,GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING, GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE)
                .cableProperties(GTValues.V[7], 4, 6)
                .blastTemp(7000, BlastProperty.GasTier.HIGHER, GTValues.VA[GTValues.ZPM],5600)
                .buildAndRegister();

        LivingIgniclad = new Material.Builder(CosmicCore.id("living_igniclad"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1760))
                .color(0x940002).secondaryColor(0xe35e17)
                .flags(GENERATE_BOLT_SCREW,GENERATE_ROUND,GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING, GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE)
                .cableProperties(GTValues.V[8], 4, 6)
                .blastTemp(9000, BlastProperty.GasTier.HIGHER, GTValues.VA[GTValues.UV],5600)
                .buildAndRegister();
        PsionicGalvorn = new Material.Builder(CosmicCore.id("psionic_galvorn"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1760))
                .color(0x004094).secondaryColor(0x0962ab)
                .flags(GENERATE_BOLT_SCREW,GENERATE_ROUND,GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING, GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE)
                .cableProperties(GTValues.V[9], 4, 6)
                .blastTemp(10500, BlastProperty.GasTier.HIGHER, GTValues.VA[GTValues.UV],5600)
                .buildAndRegister();
        ProgrammableMatter = new Material.Builder(CosmicCore.id("programmable_matter"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1760))
                .color(0x003e4f).secondaryColor(0x0a2830)
                .flags(GENERATE_BOLT_SCREW,GENERATE_ROUND,GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING, GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE)
                .cableProperties(GTValues.V[10], 4, 6)
                .blastTemp(13000, BlastProperty.GasTier.HIGHEST, GTValues.VA[GTValues.UHV],5600)
                .buildAndRegister();
        ShimmeringNeutronium = new Material.Builder(CosmicCore.id("shimmering_neutronium"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1760))
                .color(0xc7e9f2).secondaryColor(0xffffff)
                .flags(GENERATE_BOLT_SCREW,GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING, GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE)
                .cableProperties(GTValues.V[11], 4, 6)
                .blastTemp(15000, BlastProperty.GasTier.HIGHEST, GTValues.VA[GTValues.UEV],5600)
                .buildAndRegister();
        CausalFabric = new Material.Builder(CosmicCore.id("causal_fabric"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1760))
                .color(0x270054).secondaryColor(0x481c7a)
                .flags(GENERATE_BOLT_SCREW,GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING, GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE)
                .cableProperties(GTValues.V[11], 4, 6)
                .blastTemp(19000, BlastProperty.GasTier.HIGHEST, GTValues.VA[GTValues.UIV],5600)
                .buildAndRegister();

    }
}
