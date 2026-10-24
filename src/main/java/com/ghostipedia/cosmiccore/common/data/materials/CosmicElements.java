package com.ghostipedia.cosmiccore.common.data.materials;

import com.ghostipedia.cosmiccore.api.registries.CosmicRegistration;

import com.gregtechceu.gtceu.api.data.chemical.Element;

import net.minecraft.core.Holder;

import java.util.Locale;

public class CosmicElements {

    public static final Holder<Element> ViR = createAndRegister(35, 450, -1, null, "Virtue", "", false);
    public static final Holder<Element> Pi = createAndRegister(1, 0, -1, null, "Prisma", "", false);
    public static final Holder<Element> Vir = createAndRegister(1, 0, -1, null, "Vitrius", "", false);
    public static final Holder<Element> EtherSteel = createAndRegister("ether_steel", 27, 177, -1, null, "Ether Steel",
            "Ma₂FeMnNi(Si(Fe₂S₂)₅(Cr₂ZiAbAl)Hg₃)", false);

    public static Holder<Element> createAndRegister(long protons, long neutrons, long halfLifeSeconds, String decayTo,
                                                    String name, String symbol, boolean isIsotope) {
        return createAndRegister(name.toLowerCase(Locale.ROOT), protons, neutrons, halfLifeSeconds, decayTo,
                name, symbol, isIsotope);
    }

    public static Holder<Element> createAndRegister(String id, long protons, long neutrons, long halfLifeSeconds,
                                                    String decayTo, String name, String symbol, boolean isIsotope) {
        return CosmicRegistration.REGISTRATE.element(id, protons, neutrons, halfLifeSeconds, decayTo, name, symbol,
                isIsotope);
    }

    public static void init() {}
}
