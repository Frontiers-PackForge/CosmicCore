package com.ghostipedia.cosmiccore.common.data.materials;

import com.ghostipedia.cosmiccore.api.registries.CosmicRegistration;

import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;

import net.minecraft.core.Holder;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet.*;
import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class CosmicMaterialSet {

    public static final Holder<MaterialIconSet> INFINITY = REGISTRATE.materialIconSet("infinity", SHINY);
    public static final Holder<MaterialIconSet> CHRONON = REGISTRATE.materialIconSet("chronon", SHINY);
    public static final Holder<MaterialIconSet> NEUTRONITE = REGISTRATE.materialIconSet("neutronite", SHINY);
    public static final Holder<MaterialIconSet> NEUTRONIUM_CCORE = REGISTRATE.materialIconSet("neutronium", SHINY);
    public static final Holder<MaterialIconSet> COSMIC_NEUTRONIUM_CCORE = REGISTRATE
            .materialIconSet("cosmic_neutronium", SHINY);
    public static final Holder<MaterialIconSet> TRINAVINE = REGISTRATE.materialIconSet("trinavine", SHINY);
    public static final Holder<MaterialIconSet> STARMETAL = REGISTRATE.materialIconSet("starmetal", SHINY);
    public static final Holder<MaterialIconSet> NEVRAMITE = REGISTRATE.materialIconSet("nevramite", DULL);
    public static final Holder<MaterialIconSet> VOIDSPARK = REGISTRATE.materialIconSet("voidspark", DULL);
    public static final Holder<MaterialIconSet> SOL = REGISTRATE.materialIconSet("sol", DULL);
    public static final Holder<MaterialIconSet> NOSTIUM = REGISTRATE.materialIconSet("nostium", SHINY);
    public static final Holder<MaterialIconSet> KUVITE = REGISTRATE.materialIconSet("kuvite", SHINY);
    public static final Holder<MaterialIconSet> GALVANIZED_ETHERSTEEL = CosmicRegistration.REGISTRATE
            .materialIconSet("galvanized_ethersteel", SHINY);
    public static final Holder<MaterialIconSet> CRYSTAL = REGISTRATE.materialIconSet("crystal", DULL);
    public static final Holder<MaterialIconSet> MAGIC = REGISTRATE.materialIconSet("magic", DULL);

    public static final Holder<MaterialIconSet> MANA = REGISTRATE.materialIconSet("mana", SHINY);
    public static final Holder<MaterialIconSet> FLUID_A = REGISTRATE.materialIconSet("fluid_a", SHINY);
    public static final Holder<MaterialIconSet> FLUID_B = REGISTRATE.materialIconSet("fluid_b", SHINY);
    public static final Holder<MaterialIconSet> FLUID_C = REGISTRATE.materialIconSet("fluid_c", SHINY);
    public static final Holder<MaterialIconSet> UTHERIUM = REGISTRATE.materialIconSet("utherium", DIAMOND);
    public static final Holder<MaterialIconSet> RUNE_STEEL = REGISTRATE.materialIconSet("rune_steel", BRIGHT);
    public static final Holder<MaterialIconSet> PRISMA = REGISTRATE.materialIconSet("prisma", DIAMOND);
    public static final Holder<MaterialIconSet> STARRY = REGISTRATE.materialIconSet("starry", DULL);

    public static void init() {}
}
