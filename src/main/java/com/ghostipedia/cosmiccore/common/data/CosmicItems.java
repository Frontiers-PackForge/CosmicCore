package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.api.item.armor.*;
import com.ghostipedia.cosmiccore.api.registries.CosmicRegistration;
import com.ghostipedia.cosmiccore.common.data.tag.item.CosmicItemTags;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.common.item.armor.GTArmorMaterials;
import com.gregtechceu.gtceu.common.item.armor.QuarkTechSuite;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.integration.jade.GTJadePlugin;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import earth.terrarium.adastra.common.tags.ModItemTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Rarity;
import wayoftime.bloodmagic.common.item.BloodOrb;
import wayoftime.bloodmagic.common.item.ItemBloodOrb;
import wayoftime.bloodmagic.common.registration.impl.BloodOrbDeferredRegister;
import wayoftime.bloodmagic.common.registration.impl.BloodOrbRegistryObject;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static wayoftime.bloodmagic.common.item.BloodMagicItems.BLOOD_ORBS;


@SuppressWarnings("Convert2MethodRef")
public class CosmicItems {
    public static final BloodOrbRegistryObject<BloodOrb> ORB_ASCENDANT;
    public static final BloodOrbRegistryObject<BloodOrb> ORB_VOIDSENT;
    public static final BloodOrbRegistryObject<BloodOrb> ORB_SOVEREIGN;

    static {
        CosmicRegistration.REGISTRATE.creativeModeTab(() -> CosmicCreativeModeTabs.COSMIC_CORE);
        ORB_ASCENDANT = BLOOD_ORBS.register("ascendantbloodorb", () -> {
            return new BloodOrb(new ResourceLocation("bloodmagic", "ascendantbloodorb"), 5, 25000000, 1000);
        });
        ORB_VOIDSENT = BLOOD_ORBS.register("voidsentbloodorb", () -> {
            return new BloodOrb(new ResourceLocation("bloodmagic", "voidsentbloodorb"), 5, 50000000, 1000);
        });
        ORB_SOVEREIGN = BLOOD_ORBS.register("sovereignbloodorb", () -> {
            return new BloodOrb(new ResourceLocation("bloodmagic", "sovereignbloodorb"), 5, 100000000, 10000);
        });
    }

    public static final CosmicBloodOrbDeferredRegister COSMIC_BLOOD_ORBS = new CosmicBloodOrbDeferredRegister("cosmiccore");

    //Literally Random shit
    public static final ItemEntry<ComponentItem> DONK = REGISTRATE.item("donk", ComponentItem::create)
            .lang("Donk")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> DILUMIXAL_NAQ_DOPED_BOULE = REGISTRATE.item("dilumixal_naquadah_doped_silicon_boule", ComponentItem::create)
            .lang("DiLumixal Naquadah Doped Silicon Boule")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> DILUMIXAL_NAQ_DOPED_WAFER = REGISTRATE.item("dilumixal_naquadah_doped_silicon_wafer", ComponentItem::create)
            .lang("DiLumixal Naquadah Doped Silicon Wafer")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> CRYSTAL_CHIPLET_BASE = REGISTRATE.item("crystal_chiplet_base", ComponentItem::create)
            .lang("Crystal Chiplet Base")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ENGRAVED_CRYSTAL_CHIPLET = REGISTRATE.item("engraved_crystal_chiplet", ComponentItem::create)
            .lang("Engraved Crystal Chiplet")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> UNSEALED_CRYSTAL_CPU = REGISTRATE.item("unsealed_crystal_cpu", ComponentItem::create)
            .lang("Unsealed Crystal CPU")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> CRYSTALLINE_TRANSISTOR = REGISTRATE.item("crystalline_transistor", ComponentItem::create)
            .lang("Crystalline Transistor")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> CRYSTALLINE_RESISTOR = REGISTRATE.item("crystalline_resistor", ComponentItem::create)
            .lang("Crystalline Resistor")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> CRYSTALLINE_CAPACITOR = REGISTRATE.item("crystalline_capacitor", ComponentItem::create)
            .lang("Crystalline Capacitor")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> CRYSTALLINE_DIODE = REGISTRATE.item("crystalline_diode", ComponentItem::create)
            .lang("Crystalline Diode")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> CRYSTALLINE_INDUCTOR = REGISTRATE.item("crystalline_inductor", ComponentItem::create)
            .lang("Crystalline Inductor")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ItemBloodOrb> ITEM_ORB_ASCENDANT = REGISTRATE.item("asc_blood_orb", (p) -> new ItemBloodOrb(ORB_ASCENDANT))
            .lang("Ascendant Blood Orb")
            .properties(p -> p.stacksTo(1))
            .defaultModel()
            .register();
    public static final ItemEntry<ItemBloodOrb> ITEM_ORB_VOIDSENT = REGISTRATE.item("void_blood_orb", (p) -> new ItemBloodOrb(ORB_VOIDSENT))
            .lang("Ascendant Blood Orb")
            .properties(p -> p.stacksTo(1))
            .defaultModel()
            .register();
    public static final ItemEntry<ItemBloodOrb> ITEM_ORB_SOVEREIGNT = REGISTRATE.item("sov_blood_orb", (p) -> new ItemBloodOrb(ORB_SOVEREIGN))
            .lang("Ascendant Blood Orb")
            .properties(p -> p.stacksTo(1))
            .defaultModel()
            .register();

    //    public static ItemEntry<SpaceArmorComponentItem> SPACE_NANO_CHESTPLATE = REGISTRATE.item("space_nanomuscle_chestplate", (p) -> new SpaceArmorComponentItem(GTArmorMaterials.ARMOR, ArmorItem.Type.CHESTPLATE, 5000, p)
//                    .setArmorLogic(new NanoMuscleSpaceSuite(ArmorItem.Type.CHESTPLATE, 512,
//                            6_400_000L * (long) Math.max(1, Math.pow(4, ConfigHolder.INSTANCE.tools.voltageTierNanoSuit - 3)),
//                            ConfigHolder.INSTANCE.tools.voltageTierNanoSuit)))
//            .tag(CosmicItemTags.NANOMUSCLE_SPACE_SUITE, ModItemTags.SPACE_SUITS, ModItemTags.FREEZE_RESISTANT_ARMOR, ModItemTags.HEAT_RESISTANT_ARMOR)
//            .lang("NanoMuscle™ Space Suite Chestplate")
//            .properties(p -> p.rarity(Rarity.RARE))
//            .register();
    public static final ItemEntry<ComponentItem> THERMAL_CHAIN_AGENT = REGISTRATE.item("thermal_chain_agent", ComponentItem::create)
            .lang("Thermal Chain Agent")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> VOMAPLAST = REGISTRATE.item("vomaplast", ComponentItem::create)
            .lang("Vomaplast")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ECTOPHASM = REGISTRATE.item("ectophasm", ComponentItem::create)
            .lang("Ectophasm")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> DEMONIC_DESIRE = REGISTRATE.item("demonic_desire", ComponentItem::create)
            .lang("Deomic Desire")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> WEAKENED_SOUL = REGISTRATE.item("weakened_soul", ComponentItem::create)
            .lang("Weakened Soul")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();


    public static final ItemEntry<ComponentItem> WAXED_LEATHER = REGISTRATE.item("waxed_leather", ComponentItem::create)
            .lang("Waxed Leather")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OVERLOADED_PEARLS = REGISTRATE.item("overloaded_pearls", ComponentItem::create)
            .lang("Overloaded Pearls")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ADVANCED_RAM_WAFER = REGISTRATE.item("aram_wafer", ComponentItem::create)
            .lang("ARAM Wafer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ADVANCED_RAM_CHIP = REGISTRATE.item("aram_chip", ComponentItem::create)
            .lang("ARAM Chip")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> BLACKSTONE_PUSTULE = REGISTRATE.item("blackstone_pustule", ComponentItem::create)
            .lang("Blackstone Pustule")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> WRAPPED_S = REGISTRATE.item("blackstone_pustule", ComponentItem::create)
            .lang("Blackstone Pustule")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();

    //New Circuits
    //Echo (ZPM-UEV)
    public static final ItemEntry<ComponentItem> SONAR_PROCESSOR = REGISTRATE.item("echo_processor", ComponentItem::create)
            .lang("Echo Processor")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> SONAR_PROCESSOR_ASSEMBLY = REGISTRATE.item("echo_processor_assembly", ComponentItem::create)
            .lang("Echo Processor Assembly")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> SONAR_PROCESSOR_SUPERCOMPUTER = REGISTRATE.item("echo_processor_supercomputer", ComponentItem::create)
            .lang("Echo Processor Supercomputer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> SONAR_PROCESSOR_MAINFRAME = REGISTRATE.item("echo_processor_mainframe", ComponentItem::create)
            .lang("Echo Processor Mainframe")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    //Optical (UV-UIV)
    public static final ItemEntry<ComponentItem> OPTICAL_PROCESSOR = REGISTRATE.item("optical_processor", ComponentItem::create)
            .lang("Optical Processor")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OPTICAL_PROCESSOR_ASSEMBLY = REGISTRATE.item("optical_processor_assembly", ComponentItem::create)
            .lang("Optical Processor Assembly")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OPTICAL_PROCESSOR_SUPERCOMPUTER = REGISTRATE.item("optical_processor_supercomputer", ComponentItem::create)
            .lang("Optical Processor Supercomputer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OPTICAL_PROCESSOR_MAINFRAME = REGISTRATE.item("optical_processor_mainframe", ComponentItem::create)
            .lang("Optical Processor Mainframe")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    //Cosmic (UHV-UXV)
    public static final ItemEntry<ComponentItem> COSMIC_PROCESSOR = REGISTRATE.item("cosmic_processor", ComponentItem::create)
            .lang("Cosmic Processor")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> COSMIC_PROCESSOR_ASSEMBLY = REGISTRATE.item("cosmic_processor_assembly", ComponentItem::create)
            .lang("Cosmic Processor Assembly")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> COSMIC_PROCESSOR_SUPERCOMPUTER = REGISTRATE.item("cosmic_processor_supercomputer", ComponentItem::create)
            .lang("Cosmic Processor Supercomputer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> COSMIC_PROCESSOR_MAINFRAME = REGISTRATE.item("cosmic_processor_mainframe", ComponentItem::create)
            .lang("Cosmic Processor Mainframe")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    //Psionic Circuit (UEV-OPV)
    public static final ItemEntry<ComponentItem> PSIONIC_PROCESSOR = REGISTRATE.item("psionic_processor", ComponentItem::create)
            .lang("Psionic Processor")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PSIONIC_PROCESSOR_ASSEMBLY = REGISTRATE.item("psionic_processor_assembly", ComponentItem::create)
            .lang("Psionic Processor Assembly")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PSIONIC_PROCESSOR_SUPERCOMPUTER = REGISTRATE.item("psionic_processor_supercomputer", ComponentItem::create)
            .lang("Psionic Processor Supercomputer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PSIONIC_PROCESSOR_MAINFRAME = REGISTRATE.item("psionic_processor_mainframe", ComponentItem::create)
            .lang("Psionic Processor Mainframe")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    //Macroverse (UIV-MAX)
    public static final ItemEntry<ComponentItem> MACROVERSE_PROCESSOR = REGISTRATE.item("macroverse_processor", ComponentItem::create)
            .lang("Macroverse Processor")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> MACROVERSE_PROCESSOR_ASSEMBLY = REGISTRATE.item("macroverse_processor_assembly", ComponentItem::create)
            .lang("Macroverse Processor Assembly")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> MACROVERSE_PROCESSOR_SUPERCOMPUTER = REGISTRATE.item("macroverse_processor_supercomputer", ComponentItem::create)
            .lang("Macroverse Processor Supercomputer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> MACROVERSE_PROCESSOR_MAINFRAME = REGISTRATE.item("macroverse_processor_mainframe", ComponentItem::create)
            .lang("Macroverse Processor Mainframe")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();

    // Space Suite
    public static ItemEntry<SpaceArmorComponentItem> SPACE_NANO_CHESTPLATE = REGISTRATE.item("space_nanomuscle_chestplate", (p) -> new SpaceArmorComponentItem(GTArmorMaterials.ARMOR, ArmorItem.Type.CHESTPLATE, 5000, p)
                    .setArmorLogic(new NanoMuscleSpaceSuite(ArmorItem.Type.CHESTPLATE, 512,
                            6_400_000L * (long) Math.max(1, Math.pow(4, ConfigHolder.INSTANCE.tools.voltageTierNanoSuit - 3)),
                            ConfigHolder.INSTANCE.tools.voltageTierNanoSuit)))
            .tag(CosmicItemTags.NANOMUSCLE_SPACE_SUITE, ModItemTags.SPACE_SUITS, ModItemTags.FREEZE_RESISTANT_ARMOR, ModItemTags.HEAT_RESISTANT_ARMOR)
            .lang("NanoMuscle™ Space Suite Chestplate")
            .properties(p -> p.rarity(Rarity.RARE))
            .register();
    public static ItemEntry<SpaceArmorComponentItem> ADVANCED_SPACE_NANO_CHESTPLATE = REGISTRATE.item("space_advanced_nanomuscle_chestplate", (p) -> new SpaceArmorComponentItem(GTArmorMaterials.ARMOR, ArmorItem.Type.CHESTPLATE, 10000, p)
                    .setArmorLogic(new AdvancedNanoMuscleSpaceSuite(512,
                            12_800_000L * (long) Math.max(1, Math.pow(4, ConfigHolder.INSTANCE.tools.voltageTierAdvNanoSuit - 3)),
                            ConfigHolder.INSTANCE.tools.voltageTierAdvNanoSuit)))
            .tag(CosmicItemTags.NANOMUSCLE_SPACE_SUITE, ModItemTags.SPACE_SUITS, ModItemTags.FREEZE_RESISTANT_ARMOR, ModItemTags.HEAT_RESISTANT_ARMOR)
            .lang("Advanced NanoMuscle™ Space Suite Chestplate")
            .properties(p -> p.rarity(Rarity.EPIC))
            .register();
    public static ItemEntry<SpaceArmorComponentItem> SPACE_QUARK_CHESTPLATE = REGISTRATE.item("space_quarktech_chestplate", (p) -> new SpaceArmorComponentItem(GTArmorMaterials.ARMOR, ArmorItem.Type.CHESTPLATE, 20000, p)
                    .setArmorLogic(new QuarkTechSpaceSuite(ArmorItem.Type.CHESTPLATE, 8192,
                            100_000_000L * (long) Math.max(1, Math.pow(4, ConfigHolder.INSTANCE.tools.voltageTierQuarkTech - 5)),
                            ConfigHolder.INSTANCE.tools.voltageTierNanoSuit)))
            .tag(CosmicItemTags.QUARKTECH_SPACE_SUITE, ModItemTags.SPACE_SUITS, ModItemTags.FREEZE_RESISTANT_ARMOR, ModItemTags.HEAT_RESISTANT_ARMOR)
            .lang("QuarkTech™ Space Suite Chestplate")
            .properties(p -> p.rarity(Rarity.RARE))
            .register();
    public static ItemEntry<SpaceArmorComponentItem> ADVANCED_SPACE_QUARK_CHESTPLATE = REGISTRATE.item("space_advanced_quarktech_chestplate", (p) -> new SpaceArmorComponentItem(GTArmorMaterials.ARMOR, ArmorItem.Type.CHESTPLATE, 50000, p)
                    .setArmorLogic(new AdvancedQuarkTechSpaceSuite(8192,
                            1_000_000_000L * (long) Math.max(1, Math.pow(4, ConfigHolder.INSTANCE.tools.voltageTierAdvQuarkTech - 6)),
                            ConfigHolder.INSTANCE.tools.voltageTierAdvNanoSuit)))
            .tag(CosmicItemTags.QUARKTECH_SPACE_SUITE, ModItemTags.SPACE_SUITS, ModItemTags.FREEZE_RESISTANT_ARMOR, ModItemTags.HEAT_RESISTANT_ARMOR)
            .lang("Advanced QuarkTech™ Space Suite Chestplate")
            .properties(p -> p.rarity(Rarity.EPIC))
            .register();
    //Oiled up white girl trying to understand what the FUCK an armor tag is, i'm doing to fucking shove a whole pineapple up the ass of whatever mojang employee thought these were **OKAY TO CODE**
    public static ItemEntry<SpaceArmorComponentItem> VOMHINEE_WARPTECH_CHESTPLATE = REGISTRATE.item("vomahine_warptech_chestplate", (p) -> new SpaceArmorComponentItem(GTArmorMaterials.ARMOR, ArmorItem.Type.CHESTPLATE, 100000, p)
                    .setArmorLogic(new QuarkTechSpaceSuite(ArmorItem.Type.CHESTPLATE, 8192,
                            100_000_000L * (long) Math.max(1, Math.pow(4, ConfigHolder.INSTANCE.tools.voltageTierQuarkTech - 5)),
                            ConfigHolder.INSTANCE.tools.voltageTierAdvQuarkTech)))
            .tag(CosmicItemTags.QUARKTECH_SPACE_SUITE, ModItemTags.SPACE_SUITS, ModItemTags.FREEZE_RESISTANT_ARMOR, ModItemTags.HEAT_RESISTANT_ARMOR)
            .lang("Vomahine™ WarpTech Chestplate")
            .properties(p -> p.rarity(Rarity.EPIC))
            .register();
    public static ItemEntry<SpaceArmorComponentItem> ADVANCED_VOMHINEE_WARPTECH_CHESTPLATE = REGISTRATE.item("vomahine_warptech_gravplate", (p) -> new SpaceArmorComponentItem(GTArmorMaterials.ARMOR, ArmorItem.Type.CHESTPLATE, 100000, p)
                    .setArmorLogic(new AdvancedQuarkTechSpaceSuite(8192,
                            10_000_000_000L * (long) Math.max(1, Math.pow(4, ConfigHolder.INSTANCE.tools.voltageTierAdvQuarkTech - 6)),
                            ConfigHolder.INSTANCE.tools.voltageTierAdvQuarkTech)))
            .tag(CosmicItemTags.QUARKTECH_SPACE_SUITE, ModItemTags.SPACE_SUITS, ModItemTags.FREEZE_RESISTANT_ARMOR, ModItemTags.HEAT_RESISTANT_ARMOR)
            .lang("Vomahine™ WarpTech Gravplate")
            .properties(p -> p.rarity(Rarity.EPIC))
            .register();
    public static ItemEntry<ArmorComponentItem> VOMHINEE_WARPTECH_LEGGINGS = REGISTRATE.item("vomahine_warptech_leggings",
                    (p) -> new ArmorComponentItem(GTArmorMaterials.ARMOR, ArmorItem.Type.LEGGINGS, p)
                            .setArmorLogic(new QuarkTechSuite(ArmorItem.Type.LEGGINGS,
                                    8192,
                                    100_000_000L * (long) Math.max(1,
                                            Math.pow(4, ConfigHolder.INSTANCE.tools.voltageTierQuarkTech - 5)),
                                    ConfigHolder.INSTANCE.tools.voltageTierQuarkTech)))
            .lang("Vomahine™ WarpTech Leggings")
            .properties(p -> p.rarity(Rarity.EPIC))
            .tag(CustomTags.PPE_ARMOR)
            .register();
    public static ItemEntry<ArmorComponentItem> VOMHINEE_WARPTECH_HELMET = REGISTRATE.item("vomahine_warptech_helmet",
                    (p) -> new ArmorComponentItem(GTArmorMaterials.ARMOR, ArmorItem.Type.HELMET, p)
                            .setArmorLogic(new QuarkTechSuite(ArmorItem.Type.HELMET,
                                    8192,
                                    100_000_000L * (long) Math.max(1,
                                            Math.pow(4, ConfigHolder.INSTANCE.tools.voltageTierQuarkTech - 5)),
                                    ConfigHolder.INSTANCE.tools.voltageTierQuarkTech)))
            .lang("Vomahine™ WarpTech Leggings")
            .properties(p -> p.rarity(Rarity.EPIC))
            .tag(CustomTags.PPE_ARMOR)
            .register();
    public static ItemEntry<ArmorComponentItem> VOMHINEE_WARPTECH_BOOTS = REGISTRATE.item("vomahine_warptech_boots",
                    (p) -> new ArmorComponentItem(GTArmorMaterials.ARMOR, ArmorItem.Type.BOOTS, p)
                            .setArmorLogic(new QuarkTechSuite(ArmorItem.Type.BOOTS,
                                    8192,
                                    100_000_000L * (long) Math.max(1,
                                            Math.pow(4, ConfigHolder.INSTANCE.tools.voltageTierQuarkTech - 5)),
                                    ConfigHolder.INSTANCE.tools.voltageTierQuarkTech)))
            .lang("Vomahine™ WarpTech Leggings")
            .properties(p -> p.rarity(Rarity.EPIC))
            .tag(CustomTags.PPE_ARMOR)
            .register();

    public static <T extends ComponentItem> NonNullConsumer<T> attach(IItemComponent... components) {
        return item -> item.attachComponents(components);
    }

    public static void init() {

    }
}