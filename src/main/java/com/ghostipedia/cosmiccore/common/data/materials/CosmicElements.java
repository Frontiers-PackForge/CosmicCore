package com.ghostipedia.cosmiccore.common.data.materials;

import com.gregtechceu.gtceu.api.data.chemical.Element;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

public class CosmicElements {

    public static final Element Pi = createAndRegister(1, 0, -1, null, "Prisma", "Pi", false);
    public static final Element ViR = createAndRegister(35, 450, -1, null, "Virtue", "ERROR", false);

    public static Element createAndRegister(long protons, long neutrons, long halfLifeSeconds, String decayTo,
                                            String name, String symbol, boolean isIsotope) {
        Element element = new Element(protons, neutrons, halfLifeSeconds, decayTo, name, symbol, isIsotope);
        GTRegistries.ELEMENTS.register(name, element);
        return element;
    }

    public static void init() {}
}
