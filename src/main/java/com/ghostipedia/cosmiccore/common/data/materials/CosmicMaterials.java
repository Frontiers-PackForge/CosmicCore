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
    public static Material DilutedPrisma;
    public static Material Virtue;
    public static Material PrismaticTungstensteel;
    public static Material ResonantVirtueMeld;
    public static Material NaquadicSuperalloy;
    public static Material Trinavine;
    public static Material TriniumNaqide;
    public static Material LivingIgniclad;
    public static Material PsionicGalvorn;
    public static Material ProgrammableMatter;
    public static Material ShimmeringNeutronium;
    public static Material CausalFabric;
    public static Material Potential;
    public static Material FalseInfinity;
    public static Material SuitableInfinity;
    public static Material LogicalInfinity;
    public static Material Taranium;
    public static Material Spacetime;
    public static Material StygianPlague;
    public static Material EnragedStygianPlague;
    public static Material Cosmocite;
    public static Material TearsOfTheUniverse;
    public static Material Ichor;
    public static Material Ichorium;
    public static Material Chronon;
    public static Material Temmerite;

    public static void register() {
        Prisma = new Material.Builder(CosmicCore.id("prisma"))
                .liquid(new FluidBuilder().state(FluidState.LIQUID).customStill())
                .element(CosmicElements.Pi)
                .buildAndRegister();
        DilutedPrisma = new Material.Builder(CosmicCore.id("diluted_prisma"))
                .liquid()
                .color(0x4995b3).secondaryColor(0x4995b3)
                .components(Prisma, 1, Water, 4)
                .flags(DISABLE_DECOMPOSITION)
                .buildAndRegister();
        Virtue = new Material.Builder(CosmicCore.id("virtue_meld"))
                .liquid(new FluidBuilder().temperature(666))
                .element(CosmicElements.ViR)
                .buildAndRegister();
        TriniumNaqide = new Material.Builder(CosmicCore.id("trinium_naqide"))
                .ingot()
                .liquid(new FluidBuilder().temperature(3400))
                .color(0x6f42cf).secondaryColor(0xc71414).iconSet(MaterialIconSet.BRIGHT)
                .flags(GENERATE_GEAR, GENERATE_RING, GENERATE_FRAME, GENERATE_FINE_WIRE)
                .components(Trinium, 2, NaquadahEnriched, 4)
                .blastTemp(3600, BlastProperty.GasTier.HIGH, GTValues.VA[GTValues.LuV], 1200)
                .buildAndRegister();
        PrismaticTungstensteel = new Material.Builder(CosmicCore.id("prismatic_tungstensteel"))
                .ingot()
                .liquid(new FluidBuilder().temperature(933))
                .color(0x6f42cf).secondaryColor(0xc71414).iconSet(MaterialIconSet.BRIGHT)
                .flags(GENERATE_ROUND,GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING, GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE)
                .components(Prisma, 1, TungstenSteel, 1)
                .cableProperties(GTValues.V[4], 1, 1)
                .fluidPipeProperties(1166, 100, true)
                .blastTemp(3600, BlastProperty.GasTier.HIGH, GTValues.VA[GTValues.EV], 1200)
                .buildAndRegister();
        ResonantVirtueMeld = new Material.Builder(CosmicCore.id("resonant_virtue_meld"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1240))
                .color(0x1acde1).secondaryColor(0x00935d).iconSet(MaterialIconSet.SHINY)
                .flags(GENERATE_ROTOR,GENERATE_BOLT_SCREW,GENERATE_ROUND,GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING, GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE)
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
                .blastTemp(5400, BlastProperty.GasTier.HIGHER, GTValues.VA[GTValues.LuV],900)
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

        //Misc Materials

        //TODO - Colors , Textures, Fluid Textures, they're all gonna look the same in game for now.
        //TODO - Infinity Line Scripts On KubeJS side.
        Potential = new Material.Builder(CosmicCore.id("potential"))
                .dust()
                .liquid(new FluidBuilder().temperature(1500))
                .color(0x828282).secondaryColor(0x5e0f3d).iconSet(MaterialIconSet.BRIGHT)
                .flags(DISABLE_DECOMPOSITION)
                .buildAndRegister();
        FalseInfinity = new Material.Builder(CosmicCore.id("false_infinity"))
                .liquid(new FluidBuilder().temperature(3500))
                .color(0x508582).secondaryColor(0x5e0f3d).iconSet(MaterialIconSet.BRIGHT)
                .flags(DISABLE_DECOMPOSITION)
                .buildAndRegister();
        SuitableInfinity = new Material.Builder(CosmicCore.id("suitable_infinity"))
                .liquid(new FluidBuilder().temperature(7900))
                .color(0x508582).secondaryColor(0x5e0f3d).iconSet(MaterialIconSet.BRIGHT)
                .flags(DISABLE_DECOMPOSITION)
                .buildAndRegister();
        //TODO - Actual Textures for the entire material set... Let's do something that isn't the typical rainbow infinity, maybe make it something special.
        LogicalInfinity = new Material.Builder(CosmicCore.id("logical_infinity"))
                .ingot()
                .liquid(new FluidBuilder().temperature(10750))
                .iconSet(CosmicMaterialSet.INFINITY)
                .flags(GENERATE_ROTOR, GENERATE_FRAME,GENERATE_BOLT_SCREW,GENERATE_ROUND,GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING, GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE)
                .cableProperties(GTValues.V[7], 16, 0, true)
                .buildAndRegister();
        //TODO - Taranium Bedrock Soot + Adamantium Processing
        Taranium = new Material.Builder(CosmicCore.id("taranium"))
                .ingot()
                .liquid(new FluidBuilder().temperature(18000))
                .color(0x383838).secondaryColor(0x5e0f3d).iconSet(MaterialIconSet.BRIGHT)
                .flags(GENERATE_BOLT_SCREW,GENERATE_ROUND,GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING, GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE)
                .cableProperties(GTValues.V[12], 64, 0, true)
                .buildAndRegister();
        //Todo Temporal Tear
        Chronon = new Material.Builder(CosmicCore.id("chronon"))
                .ingot()
                .liquid(new FluidBuilder().temperature(100000000))
                .iconSet(CosmicMaterialSet.CHRONON)
                .flags(GENERATE_BOLT_SCREW,GENERATE_ROUND,GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING, GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE)
                .cableProperties(GTValues.V[12], 64, 0, true)
                .buildAndRegister();
        //TODO - Tengam but worse, way worse, please like actually make this the most soul crushing material to make at the current tier, but super easy after lol
        Temmerite = new Material.Builder(CosmicCore.id("temmerite"))
                .ingot()
                .liquid(new FluidBuilder().temperature(19500))
                .color(0x42f5a1).secondaryColor(0x42f5e0).iconSet(MaterialIconSet.BRIGHT)
                .flags(GENERATE_BOLT_SCREW,GENERATE_ROUND,GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING, GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE)
                .cableProperties(GTValues.V[12], 64, 0, true)
                .buildAndRegister();
        //If For some reason using hebrew as denotations for absolute constants upsets people, go back to using Greek, I doubt it'll be an issue but denote this just incase.
        //TODO - Thaumic Tinker Inspired chaos Ft Virtue Meld, Make your dead gods cry rivers of mutated blood - Resistant to Acausal decay and contains the Universe constance of "Dalet" (ד)
        Ichor = new Material.Builder(CosmicCore.id("ichor"))
                .liquid(new FluidBuilder().temperature(19500))
                .color(0xfca103).secondaryColor(0xfcbe03).iconSet(MaterialIconSet.BRIGHT)
                .buildAndRegister();
        //TODO - The Finalized Version of Virtue Meld, only present on Grand Virtues. Resistant to Atemporal decay and contains the Universe constance of "Alef" (א)
        Ichorium = new Material.Builder(CosmicCore.id("ichorium"))
                .ingot()
                .liquid(new FluidBuilder().temperature(19500))
                .color(0xfca103).secondaryColor(0xfcbe03).iconSet(MaterialIconSet.BRIGHT)
                .flags(GENERATE_BOLT_SCREW,GENERATE_ROUND,GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING, GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE)
                .cableProperties(GTValues.V[12], 64, 0, true)
                .buildAndRegister();
        //TODO - A ????? of nothing from the empty space between each instance of cylindrical multiverses. Dissolves the Constant of "Resh" (ר)
        Cosmocite = new Material.Builder(CosmicCore.id("cosmocite"))
                .liquid(new FluidBuilder().temperature(12123500))
                .color(0xdb5e5e).secondaryColor(0x5e0f3d).iconSet(MaterialIconSet.SHINY)
                .buildAndRegister();
        //TODO - A ????? of ???? comprised of the collective agony of Universe 0S37 (I keep forgetting the descriptor for the main universe the game is in) . Dissolves the Constant of "Waw/Vav" (ו)
        TearsOfTheUniverse = new Material.Builder(CosmicCore.id("universe_tears"))
                .liquid(new FluidBuilder().temperature(1))
                .color(0xdb5e5e).secondaryColor(0x5e0f3d).iconSet(MaterialIconSet.SHINY)
                .buildAndRegister();
        //TODO - A dangerous bioweapon unleashed on many orbital bodies in space, entirely designed to sterilize life from anything it touches.
        // Oh it's also like, self-aware but not fully sentient. Only works on impulse kinda - I'm not lore dumping more here lmfao
        StygianPlague = new Material.Builder(CosmicCore.id("stygian_plague"))
                .liquid(new FluidBuilder().state(FluidState.LIQUID).customStill().temperature(240))
                .color(0x4f035e).iconSet(MaterialIconSet.SHINY)
                .buildAndRegister();
        //TODO - Now it's mad. Now it's airborne vs waterborne, and it knows how to mentally abuse you.
        EnragedStygianPlague = new Material.Builder(CosmicCore.id("enraged_stygian_plague"))
                .liquid(new FluidBuilder().state(FluidState.LIQUID).customStill().temperature(350))
                .color(0x5e031d).iconSet(MaterialIconSet.SHINY)
                .buildAndRegister();
        //TODO - OMG! ! ! ! GTNH ! ! ! !?!?!?! SPACE!?!?!TIMEE?E!?!?!?!?! WOEAEAOWE SO FUNNY, Fuck off - Make this part of the fuel for the Temporal Tear.
        Spacetime = new Material.Builder(CosmicCore.id("spacetime"))
                .liquid(new FluidBuilder().temperature(10))
                .color(0xdb5e5e).secondaryColor(0x5e0f3d).iconSet(MaterialIconSet.SHINY)
                .buildAndRegister();

    }
}
