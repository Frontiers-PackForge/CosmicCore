package com.ghostipedia.cosmiccore.bee;

import com.ghostipedia.cosmiccore.bee.feature.CosmicBeesItems;
import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import com.ghostipedia.cosmiccore.common.item.CosmicBeesHoneyComb;

import net.minecraft.network.chat.TextColor;

import forestry.api.apiculture.ForestryBeeSpecies;
import forestry.api.plugin.IApicultureRegistration;

public class CosmicBeesDefinition {

    public static void defineBees(IApicultureRegistration apicultureRegistration) {
        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.LOFTY_OXYGEN, CosmicBeesTaxa.GENUS_LOFTY,
                        CosmicBeesTaxa.SPECIES_OXYGEN,
                        true, TextColor.fromRgb(0x8080FF))
                .setBody(TextColor.fromRgb(0x4242FF))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.LOFTY_OXYGEN), 1)
                .addMutations(mutations -> {
                    mutations.add(ForestryBeeSpecies.ABYSSAL, ForestryBeeSpecies.IMPERIAL, 10);
                })
                .setAuthority("Ghostipedia");

        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.LOFTY_HYDROGEN, CosmicBeesTaxa.GENUS_LOFTY,
                        CosmicBeesTaxa.SPECIES_HYDROGEN,
                        true, TextColor.fromRgb(0x80FFE1))
                .setBody(TextColor.fromRgb(0x4242FF))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.LOFTY_HYDROGEN), 1)
                .addMutations(mutations -> {
                    mutations.add(ForestryBeeSpecies.ABYSSAL, ForestryBeeSpecies.AGRARIAN, 10);
                })
                .setAuthority("Ghostipedia");
        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.LOFTY_NITROGEN, CosmicBeesTaxa.GENUS_LOFTY,
                        CosmicBeesTaxa.SPECIES_NITROGEN,
                        true, TextColor.fromRgb(0xFF80F9))
                .setBody(TextColor.fromRgb(0x4242FF))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.LOFTY_NITROGEN), 1)
                .addMutations(mutations -> {
                    mutations.add(ForestryBeeSpecies.ABYSSAL, ForestryBeeSpecies.CULTIVATED, 10);
                })
                .setAuthority("Ghostipedia");
        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.LOFTY_ARGON, CosmicBeesTaxa.GENUS_LOFTY,
                        CosmicBeesTaxa.SPECIES_ARGON,
                        true, TextColor.fromRgb(0x97FF80))
                .setBody(TextColor.fromRgb(0x4242FF))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.LOFTY_ARGON), 1)
                .addMutations(mutations -> {
                    mutations.add(ForestryBeeSpecies.ABYSSAL, ForestryBeeSpecies.COMMON, 10);
                })
                .setAuthority("Ghostipedia");
        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.ROSE_POLYMER, CosmicBeesTaxa.GENUS_PLASTID,
                        CosmicBeesTaxa.SPECIES_ROSE_POLYMER,
                        true, TextColor.fromRgb(0xFF4E6F))
                .setBody(TextColor.fromRgb(0x5D5D5D))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.ROSE_POLYMER), 1)
                .addMutations(mutations -> {
                    mutations.add(ForestryBeeSpecies.SCULK, ForestryBeeSpecies.COMMON, 10);
                })
                .setAuthority("Ghostipedia");
        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.CITRUS_POLYMER, CosmicBeesTaxa.GENUS_PLASTID,
                        CosmicBeesTaxa.SPECIES_CITRUS_POLYMER,
                        true, TextColor.fromRgb(0xFF9900))
                .setBody(TextColor.fromRgb(0x5D5D5D))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.CITRUS_POLYMER), 1)
                .addMutations(mutations -> {
                    mutations.add(ForestryBeeSpecies.ICY, CosmicBeesSpecies.ROSE_POLYMER, 35);
                })
                .setAuthority("Ghostipedia");
        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.WAXY_POLYMER, CosmicBeesTaxa.GENUS_PLASTID,
                        CosmicBeesTaxa.SPECIES_WAXY_POLYMER,
                        true, TextColor.fromRgb(0xA100FF))
                .setBody(TextColor.fromRgb(0x5D5D5D))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.WAXY_POLYMER), 1)
                .addMutations(mutations -> {
                    mutations.add(ForestryBeeSpecies.ICY, CosmicBeesSpecies.ROSE_POLYMER, 35);
                })
                .setAuthority("Ghostipedia");
        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.BIOHAZARD, CosmicBeesTaxa.GENUS_ESOTERIC,
                        CosmicBeesTaxa.SPECIES_WAXY_POLYMER,
                        true, TextColor.fromRgb(0x00FF33))
                .setBody(TextColor.fromRgb(0x082C00))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.BIOHAZARD), 1)
                .addMutations(mutations -> {
                    mutations.add(ForestryBeeSpecies.EMBITTERED, ForestryBeeSpecies.MARSHY, 35);
                })
                .setAuthority("Ghostipedia");
        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.PALE, CosmicBeesTaxa.GENUS_ESOTERIC,
                        CosmicBeesTaxa.SPECIES_PALE,
                        true, TextColor.fromRgb(0xC8E7F1))
                .setBody(TextColor.fromRgb(0x3F3F3F))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.PALE), 1)
                .addProduct(CosmicItems.PALE_SAW.asStack(), 0.35f)
                .addMutations(mutations -> {
                    mutations.add(ForestryBeeSpecies.PHANTASMAL, ForestryBeeSpecies.EDENIC, 35);
                })
                .setGlint(true)
                .setAuthority("Ghostipedia");
        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.SOUL, CosmicBeesTaxa.GENUS_ESOTERIC,
                        CosmicBeesTaxa.SPECIES_SOUL,
                        true, TextColor.fromRgb(0x3FEBF1))
                .setBody(TextColor.fromRgb(0x3A3A3A))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.SOUL), 1)
                .addMutations(mutations -> {
                    mutations.add(ForestryBeeSpecies.PHANTASMAL, ForestryBeeSpecies.DEMONIC, 35);
                })
                .setAuthority("Ghostipedia");
        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.RUNIC, CosmicBeesTaxa.GENUS_ESOTERIC,
                        CosmicBeesTaxa.SPECIES_SOUL,
                        true, TextColor.fromRgb(0xA68941))
                .setBody(TextColor.fromRgb(0xA2A2A2))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.SOUL), 1)
                .addMutations(mutations -> {
                    mutations.add(ForestryBeeSpecies.PHANTASMAL, ForestryBeeSpecies.DEMONIC, 35);
                })
                .setAuthority("Ghostipedia");
        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.AMBROSIC, CosmicBeesTaxa.GENUS_ESOTERIC,
                        CosmicBeesTaxa.SPECIES_AMBROSIC,
                        true, TextColor.fromRgb(0xD7C238))
                .setBody(TextColor.fromRgb(0x314234))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.AMBROSIC), 1)
                .addMutations(mutations -> {
                    mutations.add(ForestryBeeSpecies.UNWEARY, ForestryBeeSpecies.TIPSY, 35);
                })
                .setAuthority("Ghostipedia");

        //TODO - MUTATIONS

        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.ABRASIVE, CosmicBeesTaxa.GENUS_INDUSTRIAL,
                        CosmicBeesTaxa.SPECIES_ABRASIVE,
                        true, TextColor.fromRgb(0x834500))
                .setBody(TextColor.fromRgb(0x312E2B))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.ABRASIVE), 0.75f)
                .setAuthority("Ghostipedia");

        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.ENERGIZED, CosmicBeesTaxa.GENUS_INDUSTRIAL,
                        CosmicBeesTaxa.SPECIES_ENERGIZED,
                        true, TextColor.fromRgb(0xD7C238))
                .setBody(TextColor.fromRgb(0x312E2B))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.ENERGIZED), 0.75f)
                .setAuthority("Ghostipedia");

        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.SLICK, CosmicBeesTaxa.GENUS_INDUSTRIAL,
                        CosmicBeesTaxa.SPECIES_SLICK,
                        true, TextColor.fromRgb(0x251531))
                .setBody(TextColor.fromRgb(0x312E2B))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.SLICK), 0.75f)
                .setAuthority("Ghostipedia");

        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.PYROLYTIC, CosmicBeesTaxa.GENUS_INDUSTRIAL,
                        CosmicBeesTaxa.SPECIES_PYROLYTIC,
                        true, TextColor.fromRgb(0x5B4B3F))
                .setBody(TextColor.fromRgb(0x312E2B))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.PYROLYTIC), 0.75f)
                .setAuthority("Ghostipedia");

        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.LUNAR, CosmicBeesTaxa.GENUS_SOLAR,
                        CosmicBeesTaxa.SPECIES_LUNAR,
                        true, TextColor.fromRgb(0x10735F))
                .setBody(TextColor.fromRgb(0x223149))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.LUNAR), 0.75f)
                .setAuthority("Ghostipedia");
        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.SOLAR, CosmicBeesTaxa.GENUS_SOLAR,
                        CosmicBeesTaxa.SPECIES_SOLAR,
                        true, TextColor.fromRgb(0xF3DC4C))
                .setBody(TextColor.fromRgb(0x223149))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.SOLAR), 0.75f)
                .setAuthority("Ghostipedia");

        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.COSMOS, CosmicBeesTaxa.GENUS_SOLAR,
                        CosmicBeesTaxa.SPECIES_COSMOS,
                        true, TextColor.fromRgb(0xA276CB))
                .setBody(TextColor.fromRgb(0x223149))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.COSMOS), 0.75f)
                .setAuthority("Ghostipedia");

        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.HADAL, CosmicBeesTaxa.GENUS_ESOTERIC,
                        CosmicBeesTaxa.SPECIES_HADAL,
                        true, TextColor.fromRgb(0xE0099B))
                .setBody(TextColor.fromRgb(0x720303))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.HADAL), 0.75f)
                .setAuthority("Ghostipedia");

        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.SHAMAN, CosmicBeesTaxa.GENUS_ESOTERIC,
                        CosmicBeesTaxa.SPECIES_SHAMAN,
                        true, TextColor.fromRgb(0xE0099B))
                .setBody(TextColor.fromRgb(0x720303))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.SHAMAN), 0.75f)
                .setAuthority("Ghostipedia");

        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.ASHEN, CosmicBeesTaxa.GENUS_INDUSTRIAL,
                        CosmicBeesTaxa.SPECIES_ASHEN,
                        true, TextColor.fromRgb(0x6D6872))
                .setBody(TextColor.fromRgb(0x223149))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.HADAL), 0.75f)
                .setAuthority("Ghostipedia");

        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.FRACKING, CosmicBeesTaxa.GENUS_INDUSTRIAL,
                        CosmicBeesTaxa.SPECIES_FRACKING,
                        true, TextColor.fromRgb(0xDAD3B8))
                .setBody(TextColor.fromRgb(0x223149))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.FRACKING), 0.75f)
                .setAuthority("Ghostipedia");

        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.FATE, CosmicBeesTaxa.GENUS_ESOTERIC,
                        CosmicBeesTaxa.SPECIES_FATE,
                        true, TextColor.fromRgb(0x06B64D))
                .setBody(TextColor.fromRgb(0x193D05))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.HADAL), 0.75f)
                .setAuthority("Ghostipedia");

        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.GRAND_GARDEN, CosmicBeesTaxa.GENUS_LOFTY,
                        CosmicBeesTaxa.SPECIES_HADAL,
                        true, TextColor.fromRgb(0x42801D))
                .setBody(TextColor.fromRgb(0x1D5703))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.GRAND_GARDEN), 0.9f)
                .setAuthority("Ghostipedia");

        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.ARCHITECT, CosmicBeesTaxa.GENUS_FORGE_KIN,
                        CosmicBeesTaxa.SPECIES_ARCHITECT,
                        true, TextColor.fromRgb(0xD0FFE9))
                .setBody(TextColor.fromRgb(0x003588))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.ARCHITECT), 0.9f)
                .setAuthority("Ghostipedia");

        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.INQUISITIVE, CosmicBeesTaxa.GENUS_FORGE_KIN,
                        CosmicBeesTaxa.SPECIES_INQUISITIVE,
                        true, TextColor.fromRgb(0xDCA260))
                .setBody(TextColor.fromRgb(0x666C77))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.INQUISITIVE), 0.9f)
                .setAuthority("Ghostipedia");

        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.HELLSMITH, CosmicBeesTaxa.GENUS_FORGE_KIN,
                        CosmicBeesTaxa.SPECIES_HELLSMITH,
                        true, TextColor.fromRgb(0xEA5555))
                .setBody(TextColor.fromRgb(0x640529))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.HELLSMITH), 0.9f)
                .setAuthority("Ghostipedia");

        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.RADOXIA, CosmicBeesTaxa.GENUS_FORGE_KIN,
                        CosmicBeesTaxa.SPECIES_RADOXIA,
                        true, TextColor.fromRgb(0x4D0E88))
                .setBody(TextColor.fromRgb(0x55647E))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.RADOXIA), 0.9f)
                .setAuthority("Ghostipedia");

        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.ABSENT, CosmicBeesTaxa.GENUS_ESOTERIC,
                        CosmicBeesTaxa.SPECIES_ABSENT,
                        true, TextColor.fromRgb(0xE1E1E1))
                .setBody(TextColor.fromRgb(0x9A9A9A))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.ABSENT), 0.9f)
                .setAuthority("Ghostipedia");

        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.ILLUSIVE, CosmicBeesTaxa.GENUS_ESOTERIC,
                        CosmicBeesTaxa.SPECIES_ILLUSIVE,
                        true, TextColor.fromRgb(0xEFFAAC))
                .setBody(TextColor.fromRgb(0x7C92B6))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.ILLUSIVE), 0.9f)
                .setAuthority("Ghostipedia");

        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.CONSTRUCTIVE, CosmicBeesTaxa.GENUS_INDUSTRIAL,
                        CosmicBeesTaxa.SPECIES_CONSTRUCTIVE,
                        true, TextColor.fromRgb(0xCBA676))
                .setBody(TextColor.fromRgb(0x464922))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.CONSTRUCTIVE), 0.9f)
                .setAuthority("Ghostipedia");

        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.PRISMATIC, CosmicBeesTaxa.GENUS_ESOTERIC,
                        CosmicBeesTaxa.SPECIES_PRISMATIC,
                        true, TextColor.fromRgb(0x76CB87))
                .setBody(TextColor.fromRgb(0x436CAD))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.PRISMATIC), 0.9f)
                .setAuthority("Ghostipedia");

        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.HYDRAULIC, CosmicBeesTaxa.GENUS_INDUSTRIAL,
                        CosmicBeesTaxa.SPECIES_HYDRAULIC,
                        true, TextColor.fromRgb(0x715F81))
                .setBody(TextColor.fromRgb(0x0F521F))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.HYDRAULIC), 0.9f)
                .setAuthority("Ghostipedia");

        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.COBBLED, CosmicBeesTaxa.GENUS_INDUSTRIAL,
                        CosmicBeesTaxa.SPECIES_COBBLED,
                        true, TextColor.fromRgb(0x6A6272))
                .setBody(TextColor.fromRgb(0x323741))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.COBBLED), 0.9f)
                .setAuthority("Ghostipedia");

        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.EXHAUSTIVE, CosmicBeesTaxa.GENUS_ESOTERIC,
                        CosmicBeesTaxa.SPECIES_EXHAUSTIVE,
                        true, TextColor.fromRgb(0x4F625B))
                .setBody(TextColor.fromRgb(0x3A4350))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.EXHAUSTIVE), 0.9f)
                .setAuthority("Ghostipedia");

        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.VIRTUE, CosmicBeesTaxa.GENUS_ESOTERIC,
                        CosmicBeesTaxa.SPECIES_VIRTUE,
                        true, TextColor.fromRgb(0x6BD1D5))
                .setBody(TextColor.fromRgb(0x223149))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.VIRTUE), 0.9f)
                .setAuthority("Ghostipedia");
    }
}
