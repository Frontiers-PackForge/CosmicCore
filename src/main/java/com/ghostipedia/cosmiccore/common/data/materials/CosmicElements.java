package com.ghostipedia.cosmiccore.common.data.materials;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.api.data.chemical.Element;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import java.util.Locale;

public class CosmicElements {

    public static final Element ViR = createAndRegister(35, 450, -1, null, "Virtue", "", false);
    public static final Element Pi = createAndRegister(1, 0, -1, null, "Prisma", "", false);
    public static final Element Vir = createAndRegister(1, 0, -1, null, "Vitrius", "", false);
    public static final Element EtherSteel = createAndRegister("ether_steel", 27, 177, -1, null, "Ether Steel",
            "Ma₂FeMnNi(Si(Fe₂S₂)₅(Cr₂ZiAbAl)Hg₃)", false);

    public static Element createAndRegister(long protons, long neutrons, long halfLifeSeconds, String decayTo,
                                            String name, String symbol, boolean isIsotope) {
        Element element = new Element(protons, neutrons, halfLifeSeconds, decayTo, name, symbol, isIsotope);
        GTRegistries.register(GTRegistries.ELEMENTS, CosmicCore.id(name.toLowerCase(Locale.ROOT)), element);
        return element;
    }

    public static Element createAndRegister(String id, long protons, long neutrons, long halfLifeSeconds,
                                            String decayTo, String name, String symbol, boolean isIsotope) {
        Element element = new Element(protons, neutrons, halfLifeSeconds, decayTo, name, symbol, isIsotope);
        GTRegistries.register(GTRegistries.ELEMENTS, CosmicCore.id(id), element);
        return element;
    }

    public static void init() {}
}
