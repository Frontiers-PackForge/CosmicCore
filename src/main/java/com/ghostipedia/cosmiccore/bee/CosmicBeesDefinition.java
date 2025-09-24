package com.ghostipedia.cosmiccore.bee;

import com.ghostipedia.cosmiccore.bee.feature.CosmicBeesItems;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials;
import com.ghostipedia.cosmiccore.common.item.CosmicBeesHoneyComb;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;

import net.minecraft.network.chat.TextColor;

import forestry.api.apiculture.ForestryBeeSpecies;
import forestry.api.plugin.IApicultureRegistration;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.toolHeadBuzzSaw;

public class CosmicBeesDefinition {

    public static void defineBees(IApicultureRegistration apicultureRegistration) {
        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.COBBLED, CosmicBeesTaxa.GENUS_STONY, CosmicBeesTaxa.SPECIES_WEIGHTY,
                        true, TextColor.fromRgb(0x9c5faff))
                .setBody(TextColor.fromRgb(0X105aff))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.ROCKY), 1)
                .addMutations(mutations -> {
                    mutations.add(ForestryBeeSpecies.ABYSSAL, ForestryBeeSpecies.COMMON, 10);
                })
                .setAuthority("Ghostipedia");
        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.LOFTY_OXYGEN, CosmicBeesTaxa.GENUS_LOFTY,
                        CosmicBeesTaxa.SPECIES_OXYGEN,
                        true, TextColor.fromRgb(0x8080FF))
                .setBody(TextColor.fromRgb(0x4242FF))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.LOFTY_OXYGEN), 1)
                .addMutations(mutations -> {
                    mutations.add(ForestryBeeSpecies.ABYSSAL, ForestryBeeSpecies.COMMON, 10);
                })
                .setAuthority("Ghostipedia");

        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.LOFTY_HYDROGEN, CosmicBeesTaxa.GENUS_LOFTY,
                        CosmicBeesTaxa.SPECIES_HYDROGEN,
                        true, TextColor.fromRgb(0x80FFE1))
                .setBody(TextColor.fromRgb(0x4242FF))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.LOFTY_HYDROGEN), 1)
                .addMutations(mutations -> {
                    mutations.add(ForestryBeeSpecies.ABYSSAL, ForestryBeeSpecies.COMMON, 10);
                })
                .setAuthority("Ghostipedia");
        apicultureRegistration
                .registerSpecies(CosmicBeesSpecies.LOFTY_NITROGEN, CosmicBeesTaxa.GENUS_LOFTY,
                        CosmicBeesTaxa.SPECIES_NITROGEN,
                        true, TextColor.fromRgb(0xFF80F9))
                .setBody(TextColor.fromRgb(0x4242FF))
                .addProduct(CosmicBeesItems.BEE_COMBS.stack(CosmicBeesHoneyComb.LOFTY_NITROGEN), 1)
                .addMutations(mutations -> {
                    mutations.add(ForestryBeeSpecies.ABYSSAL, ForestryBeeSpecies.COMMON, 10);
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
                .addProduct(ChemicalHelper.get(toolHeadBuzzSaw, CosmicMaterials.PaleSteel), 0.35f)
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
    }
}
