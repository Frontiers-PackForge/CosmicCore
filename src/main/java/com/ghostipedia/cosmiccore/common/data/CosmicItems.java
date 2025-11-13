package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.item.LinkedTerminalBehavior;
import com.ghostipedia.cosmiccore.api.item.armor.*;
import com.ghostipedia.cosmiccore.api.registries.CosmicRegistration;
import com.ghostipedia.cosmiccore.client.renderer.item.HaloItemRenderer;
import com.ghostipedia.cosmiccore.client.renderer.item.RadianceItemRenderer;
import com.ghostipedia.cosmiccore.common.data.tag.item.CosmicItemTags;
import com.ghostipedia.cosmiccore.common.item.AsteroidItem;
import com.ghostipedia.cosmiccore.common.item.AsteroidTargetingChipItem;
import com.ghostipedia.cosmiccore.common.item.CosmicScytheItem;
import com.ghostipedia.cosmiccore.common.item.armor.ChestSanguineWarptechSuite;
import com.ghostipedia.cosmiccore.common.item.armor.HelmetSanguineWarptechSuite;
import com.ghostipedia.cosmiccore.common.item.armor.SanguineWarptechSuite;
import com.ghostipedia.cosmiccore.common.item.behavior.EffectApplicationBehavior;
import com.ghostipedia.cosmiccore.common.item.behavior.InfiniteSprayCanBehavior;
import com.ghostipedia.cosmiccore.common.item.behavior.StructureWriteBehavior;
import com.ghostipedia.cosmiccore.common.item.behavior.WirelessPDABehavior;
import com.ghostipedia.cosmiccore.utils.StringUtil;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.api.item.component.ICustomDescriptionId;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.item.component.ThermalFluidStats;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.item.ItemFluidContainer;
import com.gregtechceu.gtceu.common.item.TooltipBehavior;
import com.gregtechceu.gtceu.common.item.armor.GTArmorMaterials;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import com.sammy.malum.common.item.spirit.SpiritShardItem;
import com.sammy.malum.core.systems.spirit.MalumSpiritType;
import com.sammy.malum.core.systems.spirit.SpiritVisualMotif;
import com.sammy.malum.registry.common.SpiritTypeRegistry;
import com.teamresourceful.resourcefullib.common.registry.RegistryEntry;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import earth.terrarium.adastra.common.items.rendered.RenderedBlockItem;
import earth.terrarium.adastra.common.tags.ModItemTags;
import team.lodestar.lodestone.systems.easing.Easing;
import wayoftime.bloodmagic.common.item.BloodOrb;
import wayoftime.bloodmagic.common.item.ItemBloodOrb;
import wayoftime.bloodmagic.common.registration.impl.BloodOrbRegistryObject;

import java.awt.*;
import java.util.function.Function;

import static com.ghostipedia.cosmiccore.CosmicUtils.attachRenderer;
import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.gregtechceu.gtceu.common.data.GTItems.attach;
import static com.sammy.malum.registry.common.SpiritTypeRegistry.SPIRITS;
import static com.sammy.malum.registry.common.item.ItemTiers.ItemTierEnum.SOUL_STAINED_STEEL;
import static earth.terrarium.adastra.common.registry.ModItems.GLOBES;
import static wayoftime.bloodmagic.common.item.BloodMagicItems.BLOOD_ORBS;

public class CosmicItems {

    public static final BloodOrbRegistryObject<BloodOrb> ORB_ASCENDANT;
    public static final BloodOrbRegistryObject<BloodOrb> ORB_VOIDSENT;
    public static final BloodOrbRegistryObject<BloodOrb> ORB_SOVEREIGN;

    public static final RegistryEntry<Item> SUN_GLOBE = GLOBES.register("sun_globe",
            () -> new RenderedBlockItem(CosmicBlocks.SUN_GLOBE.get(),
                    new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    static {
        CosmicRegistration.REGISTRATE.creativeModeTab(() -> CosmicCreativeModeTabs.COSMIC_CORE);
        ORB_ASCENDANT = BLOOD_ORBS.register("ascendantbloodorb", () -> {
            return new BloodOrb(new ResourceLocation("bloodmagic", "ascendantbloodorb"), 6, 25000000, 1000);
        });
        ORB_VOIDSENT = BLOOD_ORBS.register("voidsentbloodorb", () -> {
            return new BloodOrb(new ResourceLocation("bloodmagic", "voidsentbloodorb"), 7, 50000000, 1000);
        });
        ORB_SOVEREIGN = BLOOD_ORBS.register("sovereignbloodorb", () -> {
            return new BloodOrb(new ResourceLocation("bloodmagic", "sovereignbloodorb"), 8, 100000000, 10000);
        });
    }

    public static final CosmicBloodOrbDeferredRegister COSMIC_BLOOD_ORBS = new CosmicBloodOrbDeferredRegister(
            "cosmiccore");
    // Modules

    public static final ItemEntry<SpiritShardItem> ETHERIC_SPIRIT_ITEM = REGISTRATE
            .item("etheric_spirit", (properties -> new SpiritShardItem(properties, CosmicItems.ETHERIC_SPIRIT)))
            .lang("Etheric Spirit")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    public static MalumSpiritType ETHERIC_SPIRIT = SpiritTypeRegistry.register(MalumSpiritType.create("etheric",
            new SpiritVisualMotif(new Color(120, 75, 255), new Color(55, 55, 55), 0.9f, Easing.BOUNCE_IN_OUT),
            ETHERIC_SPIRIT_ITEM)
            .setItemColor(SpiritVisualMotif::getPrimaryColor)
            .build());

    public static final ItemEntry<SpiritShardItem> WRATHFUL_SPIRIT_ITEM = REGISTRATE
            .item("wrathful_spirit", (properties -> new SpiritShardItem(properties, CosmicItems.WRATHFUL_SPIRIT)))
            .lang("Wrathful Spirit")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    public static MalumSpiritType WRATHFUL_SPIRIT = SpiritTypeRegistry.register(MalumSpiritType.create("wrathful",
            new SpiritVisualMotif(2, new Color(120, 200, 80), new Color(200, 55, 0), 0.9f, Easing.SINE_IN_OUT),
            WRATHFUL_SPIRIT_ITEM)
            .setItemColor(SpiritVisualMotif::getPrimaryColor)
            .build());

    public static final ItemEntry<SpiritShardItem> PRIDEFUL_SPIRIT_ITEM = REGISTRATE
            .item("prideful_spirit", (properties -> new SpiritShardItem(properties, CosmicItems.PRIDEFUL_SPIRIT)))
            .lang("Prideful Spirit")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    public static MalumSpiritType PRIDEFUL_SPIRIT = SpiritTypeRegistry.register(MalumSpiritType.create("prideful",
            new SpiritVisualMotif(4, new Color(120, 0, 100), new Color(200, 55, 0), 0.9f, Easing.SINE_IN_OUT),
            PRIDEFUL_SPIRIT_ITEM)
            .setItemColor(SpiritVisualMotif::getPrimaryColor)
            .build());

    public static final ItemEntry<SpiritShardItem> MALICE_SPIRIT_ITEM = REGISTRATE
            .item("malice_spirit", (properties -> new SpiritShardItem(properties, CosmicItems.MALICE_SPIRIT)))
            .lang("Malice Spirit")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    public static MalumSpiritType MALICE_SPIRIT = SpiritTypeRegistry.register(MalumSpiritType.create("malice",
            new SpiritVisualMotif(4, new Color(210, 210, 210), new Color(200, 55, 0), 0.9f, Easing.SINE_IN_OUT),
            MALICE_SPIRIT_ITEM)
            .setItemColor(SpiritVisualMotif::getPrimaryColor)
            .build());
    //
    // public static final ItemEntry<ItemBloodOrb> ITEM_ORB_SOVEREIGN = REGISTRATE
    // .item("sov_blood_orb", (p) -> new ItemBloodOrb(ORB_SOVEREIGN))
    // .lang("Sovereign Blood Orb")
    // .properties(p -> p.stacksTo(1))
    // .defaultModel()
    // .register();

    public static final ItemEntry<ComponentItem> PROD_MOD_1 = REGISTRATE.item("prod_mod_1", ComponentItem::create)
            .lang("Productivity Module Mk.1")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PROD_MOD_2 = REGISTRATE.item("prod_mod_2", ComponentItem::create)
            .lang("Productivity Module Mk.2")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PROD_MOD_3 = REGISTRATE.item("prod_mod_3", ComponentItem::create)
            .lang("Productivity Module Mk.3")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PROD_MOD_4 = REGISTRATE.item("prod_mod_4", ComponentItem::create)
            .lang("Productivity Module Mk.4")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> PARA_MOD_1 = REGISTRATE.item("para_mod_1", ComponentItem::create)
            .lang("Parallelization Module Mk.1")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PARA_MOD_2 = REGISTRATE.item("para_mod_2", ComponentItem::create)
            .lang("Parallelization Module Mk.2")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PARA_MOD_3 = REGISTRATE.item("para_mod_3", ComponentItem::create)
            .lang("Parallelization Module Mk.3")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PARA_MOD_4 = REGISTRATE.item("para_mod_4", ComponentItem::create)
            .lang("Parallelization Module Mk.4")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> RESONANT_MODULE = REGISTRATE
            .item("resonant_mod", ComponentItem::create)
            .lang("Resonant Module")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> PROTOCYTE_MOD = REGISTRATE.item("protocyte_mod", ComponentItem::create)
            .lang("Protocyte Module")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> FUSION_MODULE_MK1 = REGISTRATE
            .item("resonant_mod", ComponentItem::create)
            .lang("Fusion Module Mk.1")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> PALE_SAW = REGISTRATE
            .item("pale_saw", ComponentItem::create)
            .lang("Pale Saw")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PALE_SCRAP = REGISTRATE
            .item("pale_scrap", ComponentItem::create)
            .lang("Pale Scrap")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ABRASIVE_ROSIN_MILLSTONES = REGISTRATE
            .item("abrasive_rosin_millstones", ComponentItem::create)
            .lang("Abrasive Rosin Millstones")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> BITUMEN_WAX = REGISTRATE
            .item("bitumen_wax", ComponentItem::create)
            .lang("Bitumen Wax")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ENERGIZED_SILK = REGISTRATE
            .item("energized_silk", ComponentItem::create)
            .lang("Energized Silk")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> HARMONICALLY_TUNED_CIRCUIT_BOARD = REGISTRATE
            .item("harmonically_tuned_circuit_board",
                    ComponentItem::create)
            .lang("Harmonically Tuned Circuit Board")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> HARMONICALLY_TUNED_PRINTED_CIRCUIT_BOARD = REGISTRATE
            .item("harmonically_tuned_printed_circuit_board",
                    ComponentItem::create)
            .lang("Harmonically Tuned Printed Circuit Board")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> OPTICALLY_REFINED_CIRCUIT_BOARD = REGISTRATE
            .item("optically_refined_circuit_board",
                    ComponentItem::create)
            .lang("Optically Refined Circuit Board")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OPTICALLY_REFINED_PRINTED_CIRCUIT_BOARD = REGISTRATE
            .item("optically_refined_printed_circuit_board",
                    ComponentItem::create)
            .lang("Optically Refined Printed Circuit Board")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> PERSONA_CORE_ASSISTED_CIRCUIT_BOARD = REGISTRATE
            .item("persona_core_assisted_circuit_board",
                    ComponentItem::create)
            .lang("Persona Core Assisted Circuit Board")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PERSONA_CORE_ASSISTED_PRINTED_CIRCUIT_BOARD = REGISTRATE
            .item("persona_core_assisted_printed_circuit_board",
                    ComponentItem::create)
            .lang("Persona Core Assisted Printed Circuit Board")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> RECORD_KEPT_CIRCUIT_BOARD = REGISTRATE
            .item("record_kept_circuit_board",
                    ComponentItem::create)
            .lang("Record Kept Circuit Board")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RECORD_KEPT_PRINTED_CIRCUIT_BOARD = REGISTRATE
            .item("record_kept_printed_circuit_board",
                    ComponentItem::create)
            .lang("Record Kept Printed Circuit Board")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    // Drone Frames
    public static final ItemEntry<ComponentItem> DRONE_FRAME_1 = REGISTRATE.item("drone_frame_1", ComponentItem::create)
            .lang("Drone Frame Mk.1")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> DRONE_FRAME_2 = REGISTRATE.item("drone_frame_2", ComponentItem::create)
            .lang("Drone Frame Mk.2")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> DRONE_FRAME_3 = REGISTRATE.item("drone_frame_3", ComponentItem::create)
            .lang("Drone Frame Mk.3")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> DRONE_FRAME_4 = REGISTRATE.item("drone_frame_4", ComponentItem::create)
            .lang("Drone Frame Mk.4")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> DRONE_FRAME_5 = REGISTRATE.item("drone_frame_5", ComponentItem::create)
            .lang("Drone Frame Mk.5")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();

    // Harmonic Chip Stuff
    public static final ItemEntry<ComponentItem> FLAWED_RESONANT_WAFER = REGISTRATE
            .item("flawed_resonant_wafer", ComponentItem::create)
            .lang("Flawed Harmonic Wafer")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> REFINED_RESONANT_WAFER = REGISTRATE
            .item("refined_resonant_wafer", ComponentItem::create)
            .lang("Refined Harmonic Wafer")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> WAFER_PRAGMISO = REGISTRATE
            .item("wafer_pragmiso", ComponentItem::create)
            .lang("Pragmiso Wafer [Physics]")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> WAFER_ALCHEMICA = REGISTRATE
            .item("alchemia_wafer", ComponentItem::create)
            .lang("Alchemica Wafer [Chemistry]")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> WAFER_THAUMICA = REGISTRATE
            .item("thaumica_wafer", ComponentItem::create)
            .lang("Thaumica Wafer [Arcana]")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> WAFER_ETERNA = REGISTRATE.item("eterna_wafer", ComponentItem::create)
            .lang("Eterna Wafer [Aionology]")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> WAFER_LOGOS = REGISTRATE
            .item("fused_wafer_of_logos", ComponentItem::create)
            .lang("Fused Harmonic Wafer of Logos")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> WAFER_ESOTERIC = REGISTRATE
            .item("fused_wafer_of_esoterica", ComponentItem::create)
            .lang("Fused Harmonic Wafer of Esoterica")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> HARMONIC_OSCILLATING_CHIP = REGISTRATE
            .item("harmonic_chiplet_oscillating", ComponentItem::create)
            .lang("Harmonic Central Processing Unit")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> NULL_WAFER_HARMONIC = REGISTRATE
            .item("null_refined_resonant_wafer", ComponentItem::create)
            .lang("Nullified Harmonic Wafer")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> NULLIFIED_HARMONICS_WAFER = REGISTRATE
            .item("nullified_harmonics_wafer", ComponentItem::create)
            .lang("Nullified Harmonic Wafer")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> REFINED_HARMONICS_WAFER = REGISTRATE
            .item("refined_harmonics_wafer", ComponentItem::create)
            .lang("Refined Harmonic Wafer")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    // Tesserae
    public static final ItemEntry<ComponentItem> TESSARON = REGISTRATE.item("tessaron", ComponentItem::create)
            .lang("Vexil - [Tessaron]")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ESSON = REGISTRATE.item("esson", ComponentItem::create)
            .lang("Luminon - [Esson]")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> VEXIUN = REGISTRATE.item("vexiun", ComponentItem::create)
            .lang("Vexil - [Vexiun]")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PHANTNON = REGISTRATE.item("phantnon", ComponentItem::create)
            .lang("Luminon - [Phantnon]")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> AMBRION = REGISTRATE.item("ambrion", ComponentItem::create)
            .lang("Vexil - [Ambrion]")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> SPECTIL = REGISTRATE.item("spectil", ComponentItem::create)
            .lang("Luminon - [Spectil]")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> ETHERA = REGISTRATE.item("ethera", ComponentItem::create)
            .lang("Vexil - [Ethera]")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> NYXON = REGISTRATE.item("nyxon", ComponentItem::create)
            .lang("Luminon - [Nyxon]")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> PYRITH = REGISTRATE.item("pyrith", ComponentItem::create)
            .lang("Vexil - [Pyrith]")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> SERAPHON = REGISTRATE.item("seraphon", ComponentItem::create)
            .lang("Luminon - [Seraphon]")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> TENAEBRUM = REGISTRATE.item("tenaebrum", ComponentItem::create)
            .lang("Vexil - [Tenaebrum]")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> DYNAMIA = REGISTRATE.item("dynamia", ComponentItem::create)
            .lang("Luminon - [Dynamia]")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> CRYSTALA = REGISTRATE.item("crystala", ComponentItem::create)
            .lang("Vexil - [Crystala]")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> MYSTRIX = REGISTRATE.item("mystrix", ComponentItem::create)
            .lang("Luminon - [Mystrix]")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> CHRONIA = REGISTRATE.item("chronia", ComponentItem::create)
            .lang("Vexil - [Chronia]")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ECHON = REGISTRATE.item("echon", ComponentItem::create)
            .lang("Luminon - [Echon]")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();

    // Literally Random shit
    public static final ItemEntry<ComponentItem> DONK = REGISTRATE.item("donk", ComponentItem::create)
            .lang("Donk")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> DILUMIXAL_NAQ_DOPED_BOULE = REGISTRATE
            .item("dilumixal_naquadah_doped_silicon_boule", ComponentItem::create)
            .lang("DiLumixal Naquadah-doped Silicon Boule")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> DILUMIXAL_NAQ_DOPED_WAFER = REGISTRATE
            .item("dilumixal_naquadah_doped_silicon_wafer", ComponentItem::create)
            .lang("DiLumixal Naquadah-doped Silicon Wafer")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> CRYSTAL_CHIPLET_MASK = REGISTRATE
            .item("crystal_chiplet_mask", ComponentItem::create)
            .lang("Crystal Chiplet Mask")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> MASKED_CRYSTAL_CHIPLET_PACKAGE = REGISTRATE
            .item("masked_crystal_chiplet_package", ComponentItem::create)
            .lang("Masked Crystal Chiplet Package")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> CRYSTAL_CHIPLET_BASE = REGISTRATE
            .item("crystal_chiplet_base", ComponentItem::create)
            .lang("Crystal Chiplet Base")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ENGRAVED_CRYSTAL_CHIPLET = REGISTRATE
            .item("engraved_crystal_chiplet", ComponentItem::create)
            .lang("Engraved Crystal Chiplet")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> UNSEALED_CRYSTAL_CPU = REGISTRATE
            .item("unsealed_crystal_cpu", ComponentItem::create)
            .lang("Unsealed Crystal CPU")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> CRYSTALLINE_TRANSISTOR = REGISTRATE
            .item("crystalline_transistor", ComponentItem::create)
            .lang("Crystalline Transistor")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> CRYSTALLINE_RESISTOR = REGISTRATE
            .item("crystalline_resistor", ComponentItem::create)
            .lang("Crystalline Resistor")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> CRYSTALLINE_CAPACITOR = REGISTRATE
            .item("crystalline_capacitor", ComponentItem::create)
            .lang("Crystalline Capacitor")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> CRYSTALLINE_DIODE = REGISTRATE
            .item("crystalline_diode", ComponentItem::create)
            .lang("Crystalline Diode")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> CRYSTALLINE_INDUCTOR = REGISTRATE
            .item("crystalline_inductor", ComponentItem::create)
            .lang("Crystalline Inductor")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ItemBloodOrb> ITEM_ORB_ASCENDANT = REGISTRATE
            .item("asc_blood_orb", (p) -> new ItemBloodOrb(ORB_ASCENDANT))
            .lang("Ascendant Blood Orb")
            .properties(p -> p.stacksTo(1))
            .defaultModel()
            .register();
    public static final ItemEntry<ItemBloodOrb> ITEM_ORB_VOIDSENT = REGISTRATE
            .item("void_blood_orb", (p) -> new ItemBloodOrb(ORB_VOIDSENT))
            .lang("Voidsent Blood Orb")
            .properties(p -> p.stacksTo(1))
            .defaultModel()
            .register();
    public static final ItemEntry<ItemBloodOrb> ITEM_ORB_SOVEREIGN = REGISTRATE
            .item("sov_blood_orb", (p) -> new ItemBloodOrb(ORB_SOVEREIGN))
            .lang("Sovereign Blood Orb")
            .properties(p -> p.stacksTo(1))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> THERMAL_CHAIN_AGENT = REGISTRATE
            .item("thermal_chain_agent", ComponentItem::create)
            .lang("Thermal Chain Agent")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RADIO_MODULE_HV = REGISTRATE
            .item("hv_radio_module", ComponentItem::create)
            .lang("HV Radio Module")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RADIO_MODULE_EV = REGISTRATE
            .item("ev_radio_module", ComponentItem::create)
            .lang("EV Radio Module")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RADIO_MODULE_IV = REGISTRATE
            .item("iv_radio_module", ComponentItem::create)
            .lang("IV Radio Module")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RADIO_MODULE_LUV = REGISTRATE
            .item("luv_radio_module", ComponentItem::create)
            .lang("LuV Radio Module")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RADIO_MODULE_ZPM = REGISTRATE
            .item("zpm_radio_module", ComponentItem::create)
            .lang("ZPM Radio Module")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RADIO_MODULE_UV = REGISTRATE
            .item("uv_radio_module", ComponentItem::create)
            .lang("UV Radio Module")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RADIO_MODULE_UHV = REGISTRATE
            .item("uhv_radio_module", ComponentItem::create)
            .lang("UHV Radio Module")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RADIO_MODULE_UEV = REGISTRATE
            .item("uev_radio_module", ComponentItem::create)
            .lang("UEV Radio Module")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RADIO_MODULE_UIV = REGISTRATE
            .item("uiv_radio_module", ComponentItem::create)
            .lang("UIV Radio Module")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RADIO_MODULE_UXV = REGISTRATE
            .item("uxv_radio_module", ComponentItem::create)
            .lang("UXV Radio Module")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RADIO_MODULE_OPV = REGISTRATE
            .item("opv_radio_module", ComponentItem::create)
            .lang("OPV Radio Module")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();

    // The Fuckin Spinny Boi
    public static final ItemEntry<ComponentItem> GYROSCOPE_UV = REGISTRATE
            .item("uv_gyroscope", ComponentItem::create)
            .lang("UV Gyroscope")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> GYROSCOPE_UHV = REGISTRATE
            .item("uhv_gyroscope", ComponentItem::create)
            .lang("UHV Gyroscope")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> GYROSCOPE_UEV = REGISTRATE
            .item("uev_gyroscope", ComponentItem::create)
            .lang("UEV Gyroscope")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> GYROSCOPE_UIV = REGISTRATE
            .item("uiv_gyroscope", ComponentItem::create)
            .lang("UIV Gyroscope")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> GYROSCOPE_UXV = REGISTRATE
            .item("uxv_gyroscope", ComponentItem::create)
            .lang("UXV Gyroscope")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> GYROSCOPE_OPV = REGISTRATE
            .item("opv_gyroscope", ComponentItem::create)
            .lang("OPv Gyroscope")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> GELATIN_SCAFFOLD = REGISTRATE
            .item("gelatin_scaffold", ComponentItem::create)
            .lang("Gelatin Scaffold")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> BIFIDOBACTERIUM_BREVE_CULTURE = REGISTRATE
            .item("bifidobacterium_breve_culture", ComponentItem::create)
            .lang("Bifidobacterium Breve Culture")
            .properties(p -> p.stacksTo(4))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> BIFIDOBACTERIUM_BREVE = REGISTRATE
            .item("bifidobacterium_breve", ComponentItem::create)
            .lang("Bifidobacterium Breve")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    // Strep
    public static final ItemEntry<ComponentItem> STREPTOCOCCUS_PYOGENES_CULTURE = REGISTRATE
            .item("streptococcus_pyogenes_culture", ComponentItem::create)
            .lang("Streptococcus Pyogenes Culture")
            .properties(p -> p.stacksTo(4))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> STREPTOCOCCUS_PYOGENES = REGISTRATE
            .item("streptococcus_pyogenes", ComponentItem::create)
            .lang("Streptococcus Pyogenes")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    // E COLI
    public static final ItemEntry<ComponentItem> ESCHERICHIA_COLI_CULTURE = REGISTRATE
            .item("escherichia_coli_culture", ComponentItem::create)
            .lang("Escherichia Coli Culture")
            .properties(p -> p.stacksTo(4))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ESCHERICHIA_COLI = REGISTRATE
            .item("escherichia_coli", ComponentItem::create)
            .lang("Escherichia Coli")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> CONTAMINATED_PETRI_DISH = REGISTRATE
            .item("contaminated_petri_dish", ComponentItem::create)
            .lang("Contaminated Petri Dish")
            .properties(p -> p.stacksTo(8))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PREPARED_PETRI_DISH = REGISTRATE
            .item("prepared_petri_dish", ComponentItem::create)
            .lang("Prepared Petri Dish")
            .properties(p -> p.stacksTo(8))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ULTRASONIC_HOMOGENIZER = REGISTRATE
            .item("ultrasonic_homogenizer", ComponentItem::create)
            .lang("Ultrasonic Homogenizer")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> COMPUTATION_SUPPORT_UNIT = REGISTRATE
            .item("computation_support_unit", ComponentItem::create)
            .lang("Computation Support Unit")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> WIRED_PETRI_DISH = REGISTRATE
            .item("wired_petri_dish", ComponentItem::create)
            .lang("Wired Petri Dish")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> SCULK_FIBROBLAST = REGISTRATE
            .item("sculk_fibroblast", ComponentItem::create)
            .lang("Sculk Fibroblast")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> SCULK_MYOFIBROBLAST = REGISTRATE
            .item("sculk_myofibroblast", ComponentItem::create)
            .lang("Sculk Myofibroblast")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    // UNSURE IF THESE WILL BE USED
    public static final ItemEntry<ComponentItem> RESPIRATORY_SCULK_HEMOCYTOBLAST = REGISTRATE
            .item("resipiratory_sculk_hemocytoblast", ComponentItem::create)
            .lang("Respiratory Sculk Hemocytoblast")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> SATURATED_SCULK_HEMOCYTOBLAST = REGISTRATE
            .item("saturated_sculk_hemocytoblast", ComponentItem::create)
            .lang("Saturated Sculk Hemocytoblast")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> INERT_FUNGAL_SPORES = REGISTRATE
            .item("inert_fungal_spores", ComponentItem::create)
            .lang("Inert Fungal Spores")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> HEME_RING = REGISTRATE
            .item("heme_ring", ComponentItem::create)
            .lang("Heme Ring")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    // Thrusters (Space Industry stuff)

    public static final ItemEntry<ComponentItem> THRUSTER_UV = REGISTRATE
            .item("uv_thruster", ComponentItem::create)
            .lang(" Thruster")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> THRUSTER_UHV = REGISTRATE
            .item("uhv_thruster", ComponentItem::create)
            .lang("UHV Thruster")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> THRUSTER_UEV = REGISTRATE
            .item("uev_thruster", ComponentItem::create)
            .lang("UEV Thruster")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> THRUSTER_UIV = REGISTRATE
            .item("uiv_thruster", ComponentItem::create)
            .lang("UIV Thruster")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> THRUSTER_UXV = REGISTRATE
            .item("uxv_thruster", ComponentItem::create)
            .lang("UXV Thruster")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> THRUSTER_OPV = REGISTRATE
            .item("opv_thruster", ComponentItem::create)
            .lang("OPv Thruster")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    // Power Cells
    public static final ItemEntry<ComponentItem> POWER_CELL_UV = REGISTRATE
            .item("uv_powercell", ComponentItem::create)
            .lang("UV Power Cell")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> POWER_CELL_UHV = REGISTRATE
            .item("uhv_powercell", ComponentItem::create)
            .lang("UHV Power Cell")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> POWER_CELL_UEV = REGISTRATE
            .item("uev_powercell", ComponentItem::create)
            .lang("UEV Power Cell")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> POWER_CELL_UIV = REGISTRATE
            .item("uiv_powercell", ComponentItem::create)
            .lang("UIV Power Cell")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> POWER_CELL_UXV = REGISTRATE
            .item("uxv_powercell", ComponentItem::create)
            .lang("UXV Power Cell")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> POWER_CELL_OPV = REGISTRATE
            .item("opv_powercell", ComponentItem::create)
            .lang("OPv Power Cell")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> FERMIUM_RAD_CHARGES = REGISTRATE
            .item("fermium_rad_charges", ComponentItem::create)
            .lang("Fermium Radiation Charge")
            .properties(p -> p.stacksTo(8))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> NEURO_PROCESSING_ASSEMBLY = REGISTRATE
            .item("neuro_processing_assembly", ComponentItem::create)
            .lang("Neuroprocessing Assembly Board")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> SOMATIC_PROCESSING_ASSEMBLY = REGISTRATE
            .item("somatic_processing_assembly", ComponentItem::create)
            .lang("Somatoprocessing Assembly Board")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OPTIC_PROCESSING_ASSEMBLY = REGISTRATE
            .item("optical_processing_assembly", ComponentItem::create)
            .lang("Optical Processor Assembly")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> SELF_AWARE_PROCESSING_ASSEMBLY = REGISTRATE
            .item("self_aware_processing_assembly", ComponentItem::create)
            .lang("Self Aware Processor Assembly")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RECORD_KEEPING_PROCESSOR_ASSEMBLY = REGISTRATE
            .item("record_keeping_processor_assembly", ComponentItem::create)
            .lang("Record Keeping Processor Assembly")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PROGRAMMABLE_MOTE = REGISTRATE
            .item("programmable_mote", ComponentItem::create)
            .lang("§5Programmable Mote")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PERPETUITY_SHARD = REGISTRATE
            .item("shard_of_perpetuity", ComponentItem::create)
            .lang("Shard of Perpetuity")
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.lore.shard_small.0"));
                tooltips.add(Component.translatable("cosmiccore.lore.shard_small.1"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PERPETUITY_SHARD_LARGE = REGISTRATE
            .item("large_shard_of_perpetuity", ComponentItem::create)
            .lang("Large Shard of Perpetuity")
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.lore.shard_large.0"));
                tooltips.add(Component.translatable("cosmiccore.lore.shard_large.1"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PERPETUITY_SHARD_MASSIVE = REGISTRATE
            .item("cluster_of_perpetuity", ComponentItem::create)
            .lang("Cluster of Perpetuity")
            .properties(p -> p.stacksTo(60))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.lore.shard_huge.0"));
                tooltips.add(Component.translatable("cosmiccore.lore.shard_huge.1"));
                tooltips.add(Component.translatable("cosmiccore.lore.shard_huge.2"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> WIRELESS_PDA = REGISTRATE
            .item("wireless_pda", ComponentItem::create)
            .lang("Wireless Data PDA")
            .properties(p -> p.stacksTo(1))
            .tag()
            .onRegister(attach(new WirelessPDABehavior()))
            .defaultModel()
            .register();

    public static final ItemEntry<CosmicScytheItem> NANO_SCYTHE = REGISTRATE
            .item("nano_scythe",
                    props -> new CosmicScytheItem(
                            SOUL_STAINED_STEEL,
                            10.5f, 0.5f, props))
            .properties(p -> p.stacksTo(1))
            .lang("Nano Scythe")
            .defaultModel()
            .register();

    public static final ItemEntry<CosmicScytheItem> QUANTUM_SCYTHE = REGISTRATE
            .item("quantum_scythe",
                    props -> new CosmicScytheItem(
                            SOUL_STAINED_STEEL,
                            25.5f, 0.5f, props))
            .properties(p -> p.stacksTo(1))
            .lang("Quark Scythe")
            .defaultModel()
            .register();

    public static final ItemEntry<CosmicScytheItem> SANGUINE_SCYTHE = REGISTRATE
            .item("sanguine_scythe",
                    props -> new CosmicScytheItem(
                            SOUL_STAINED_STEEL,
                            100f, 0.5f, props))
            .properties(p -> p.stacksTo(1))
            .lang("Sanguine Scythe")
            .defaultModel()
            .register();

    public static ItemEntry<ComponentItem> THE_ONE_RING = REGISTRATE
            .item("the_one_ring", p -> (ComponentItem) new ComponentItem(p) {

                @Override
                public boolean canBeHurtBy(DamageSource damageSource) {
                    return damageSource.is(DamageTypes.LAVA);
                }

                @Override
                public int getEntityLifespan(ItemStack itemStack, Level level) {
                    return Short.MIN_VALUE;
                }

                @Override
                public boolean onDroppedByPlayer(ItemStack item, Player player) {
                    return false;
                }

                @Override
                public boolean isFoil(ItemStack stack) {
                    return true;
                }
            })
            .lang("The One Ring")
            .properties(p -> p.stacksTo(1).fireResistant())
            .onRegister(attach(new EffectApplicationBehavior()
                    .addEffect(() -> new MobEffectInstance(MobEffects.INVISIBILITY, 10), 1.0F)
                    .addEffect(() -> new MobEffectInstance(MobEffects.UNLUCK, 10, 5), 1.0F)
                    .addEffect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 10, 1), 1.0F),
                    new TooltipBehavior(list -> {
                        list.add(Component.translatable("item.cosmiccore.the_one_ring.tooltip.0"));
                        list.add(Component.translatable("item.cosmiccore.the_one_ring.tooltip.1"));
                    })))
            .register();
    // public static final ItemEntry<ComponentItem> PARADOX_ECHOS = REGISTRATE.item("paradox_harmonics",
    // ComponentItem::create)
    // .lang("Paradox Harmonics")
    // .properties(p -> p.stacksTo(64))
    // .defaultModel()
    // .register();
    // public static final ItemEntry<ComponentItem> ECTOPHASM = REGISTRATE.item("ectophasm", ComponentItem::create)
    // .lang("Ectophasm")
    // .properties(p -> p.stacksTo(64))
    // .defaultModel()
    // .register();
    // public static final ItemEntry<ComponentItem> DEMONIC_DESIRE = REGISTRATE.item("demonic_desire",
    // ComponentItem::create)
    // .lang("Deomic Desire")
    // .properties(p -> p.stacksTo(64))
    // .defaultModel()
    // .register();
    // public static final ItemEntry<ComponentItem> WEAKENED_SOUL = REGISTRATE.item("weakened_soul",
    // ComponentItem::create)
    // .lang("Weakened Soul")
    // .properties(p -> p.stacksTo(64))
    // .defaultModel()
    // .register();
    //

    public static ItemEntry<ComponentItem> SPACE_RADIO = REGISTRATE
            .item("space_radio", ComponentItem::create)
            .lang("Space Radio")
            .properties(p -> p.stacksTo(1).fireResistant())
            .onRegister(attach(new TooltipBehavior(list -> {
                list.add(Component.translatable("item.cosmiccore.space_radio.tooltip"));
            })))
            .register();

    public static final ItemEntry<ComponentItem> WAXED_LEATHER = REGISTRATE.item("waxed_leather", ComponentItem::create)
            .lang("Waxed Leather")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OVERLOADED_PEARLS = REGISTRATE
            .item("overloaded_pearls", ComponentItem::create)
            .lang("Overloaded Pearls")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ADVANCED_RAM_WAFER = REGISTRATE
            .item("aram_wafer", ComponentItem::create)
            .lang("ARAM Wafer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ADVANCED_RAM_CHIP = REGISTRATE.item("aram_chip", ComponentItem::create)
            .lang("ARAM Chip")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> RUNEWOVEN_PCB = REGISTRATE
            .item("runewoven_plastic_circuit_board", ComponentItem::create)
            .lang("Runewoven Plastic Circuit Board")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> MANA_PCB = REGISTRATE
            .item("plastic_circuit_board", ComponentItem::create)
            .lang("Mana-doped Plastic Circuit Board")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RUNIC_HEX_CPU = REGISTRATE.item("runic_hex_cpu", ComponentItem::create)
            .lang("Hex Etched CPU Chip")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RUNIC_HEX_CPU_WAFER = REGISTRATE
            .item("runic_hex_cpu_wafer", ComponentItem::create)
            .lang("Hex Etched CPU Wafer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> BLACKSTONE_PUSTULE = REGISTRATE
            .item("blackstone_pustule", ComponentItem::create)
            .lang("Blackstone Pustule")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();

    // public static final ItemEntry<ComponentItem> WRAPPED_S = REGISTRATE
    // .item("blackstone_pustule", ComponentItem::create)
    // .lang("Blackstone Pustule")
    // .properties(p -> p.stacksTo(64))
    // .defaultModel()
    // .register();

    // New Circuits

    // Hex circuits
    public static final ItemEntry<ComponentItem> HEX_PROCESSOR = REGISTRATE
            .item("hex_processor", ComponentItem::create)
            .lang("Hex Processor")
            .tag(CustomTags.MV_CIRCUITS)
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> HEX_PROCESSOR_ASSEMBLY = REGISTRATE
            .item("hex_processor_assembly", ComponentItem::create)
            .lang("Hex Processor Assembly")
            .tag(CustomTags.HV_CIRCUITS)
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> HEX_PROCESSOR_SUPERCOMPUTER = REGISTRATE
            .item("hex_processor_supercomputer", ComponentItem::create)
            .lang("Hex Processor Supercomputer")
            .tag(CustomTags.EV_CIRCUITS)
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> HEX_PROCESSOR_MAINFRAME = REGISTRATE
            .item("hex_processor_mainframe", ComponentItem::create)
            .lang("Hex Processor Mainframe")
            .tag(CustomTags.IV_CIRCUITS)
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    // Harmonic (ZPM-UEV)
    public static final ItemEntry<ComponentItem> SONAR_PROCESSOR = REGISTRATE
            .item("harmonic_processor", ComponentItem::create)
            .lang("Harmonic Processor")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> SONAR_PROCESSOR_ASSEMBLY = REGISTRATE
            .item("harmonic_processor_assembly", ComponentItem::create)
            .lang("Harmonic Processor Assembly")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> SONAR_PROCESSOR_SUPERCOMPUTER = REGISTRATE
            .item("harmonic_processor_supercomputer", ComponentItem::create)
            .lang("Harmonic Processor Supercomputer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> SONAR_PROCESSOR_MAINFRAME = REGISTRATE
            .item("harmonic_processor_mainframe", ComponentItem::create)
            .lang("Harmonic Processor Mainframe")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    // Optical (UV-UIV)
    public static final ItemEntry<ComponentItem> OPTICAL_PROCESSOR = REGISTRATE
            .item("optical_processor", ComponentItem::create)
            .lang("Optical Processor")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OPTICAL_PROCESSOR_ASSEMBLY = REGISTRATE
            .item("optical_processor_assembly", ComponentItem::create)
            .lang("Optical Processor Assembly")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OPTICAL_PROCESSOR_SUPERCOMPUTER = REGISTRATE
            .item("optical_processor_supercomputer", ComponentItem::create)
            .lang("Optical Processor Supercomputer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OPTICAL_PROCESSOR_MAINFRAME = REGISTRATE
            .item("optical_processor_mainframe", ComponentItem::create)
            .lang("Optical Processor Mainframe")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    // Suelescent (UHV-UXV)
    public static final ItemEntry<ComponentItem> COSMIC_PROCESSOR = REGISTRATE
            .item("suelescent_processor", ComponentItem::create)
            .lang("Suelescent Processor")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> COSMIC_PROCESSOR_ASSEMBLY = REGISTRATE
            .item("suelescent_processor_assembly", ComponentItem::create)
            .lang("Suelescent Processor Assembly")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> COSMIC_PROCESSOR_SUPERCOMPUTER = REGISTRATE
            .item("suelescent_processor_supercomputer", ComponentItem::create)
            .lang("Suelescent Processor Supercomputer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> COSMIC_PROCESSOR_MAINFRAME = REGISTRATE
            .item("suelescent_processor_mainframe", ComponentItem::create)
            .lang("Suelescent Processor Mainframe")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    // Akashic Circuit (UEV-OPV)
    public static final ItemEntry<ComponentItem> PSIONIC_PROCESSOR = REGISTRATE
            .item("akashic_processor", ComponentItem::create)
            .lang("Akashic Processor")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PSIONIC_PROCESSOR_ASSEMBLY = REGISTRATE
            .item("akashic_processor_assembly", ComponentItem::create)
            .lang("Akashic Processor Assembly")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PSIONIC_PROCESSOR_SUPERCOMPUTER = REGISTRATE
            .item("akashic_processor_supercomputer", ComponentItem::create)
            .lang("Akashic Processor Supercomputer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PSIONIC_PROCESSOR_MAINFRAME = REGISTRATE
            .item("akashic_processor_mainframe", ComponentItem::create)
            .lang("Akashic Processor Mainframe")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    // Eschaton (UIV-MAX)
    public static final ItemEntry<ComponentItem> ESCHATON_PROCESSOR = REGISTRATE
            .item("eschaton_processor", ComponentItem::create)
            .lang("Eschaton Processor")
            .properties(p -> p.stacksTo(64))
            .onRegister(attachRenderer(() -> HaloItemRenderer.create(6, 0xFFFFFFFF,
                    new ResourceLocation(CosmicCore.MOD_ID, "block/iris/rnd/tentacle_halo"), true,
                    false)))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ESCHATON_PROCESSOR_ASSEMBLY = REGISTRATE
            .item("eschaton_processor_assembly", ComponentItem::create)
            .lang("Eschaton Processor Assembly")
            .properties(p -> p.stacksTo(64))
            .onRegister(attachRenderer(() -> HaloItemRenderer.create(6, 0xFFFFFFFF,
                    new ResourceLocation(CosmicCore.MOD_ID, "block/iris/rnd/tentacle_halo"), true,
                    false)))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ESCHATON_PROCESSOR_SUPERCOMPUTER = REGISTRATE
            .item("eschaton_processor_supercomputer", ComponentItem::create)
            .lang("Eschaton Processor Supercomputer")
            .properties(p -> p.stacksTo(64))
            .onRegister(attachRenderer(() -> HaloItemRenderer.create(6, 0xFFFFFFFF,
                    new ResourceLocation(CosmicCore.MOD_ID, "block/iris/rnd/tentacle_halo"), true,
                    false)))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ESCHATON_PROCESSOR_MAINFRAME = REGISTRATE
            .item("eschaton_processor_mainframe", ComponentItem::create)
            .lang("Eschaton Processor Mainframe")
            .properties(p -> p.stacksTo(64))
            .onRegister(attachRenderer(() -> HaloItemRenderer.create(6, 0xFFFFFFFF,
                    new ResourceLocation(CosmicCore.MOD_ID, "block/iris/rnd/tentacle_halo"), true,
                    false)))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.literal(StringUtil
                        .rainbowDancing(LocalizationUtils.format("cosmiccore.circuit.lore.tier.max.0"))));
                lines.add(Component.translatable("cosmiccore.circuit.lore.tier.max.1"));
                lines.add(Component.translatable("cosmiccore.circuit.lore.tier.max.2"));
                lines.add(Component.translatable("cosmiccore.circuit.lore.tier.max.3"));

            })))
            .defaultModel()
            .register();

    // Demon/Soul Related Items

    public static final ItemEntry<ComponentItem> WICKED_ESSENCE = REGISTRATE
            .item("wicked_essence", ComponentItem::create)
            .lang("Wicked Essence")
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.literal(StringUtil
                        .goldFlicker(LocalizationUtils.format("cosmiccore.lore.broken_virtue.0"))));
            })))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> ABERRANT_ESSENCE = REGISTRATE
            .item("aberrant_essence", ComponentItem::create)
            .lang("§6Aberrant Essence")
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.literal(StringUtil
                        .midnightOscillation(LocalizationUtils.format("cosmiccore.lore.broken_virtue.1"))));
            })))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> FIRECLAY_BALL = REGISTRATE.item("fireclay_ball", ComponentItem::create)
            .lang("Fireclay Ball")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> HARDENED_RESIN = REGISTRATE
            .item("hardened_resin", ComponentItem::create)
            .lang("Hardened Resin")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static ItemEntry<ComponentItem> DEBUG_STRUCTURE_WRITER = REGISTRATE
            .item("debug_structure_writer", ComponentItem::create)
            .lang("Debug Structure Writer")
            .properties(p -> p.stacksTo(1))
            .onRegister(attach(StructureWriteBehavior.INSTANCE))
            .register();

    // Space Suite
    public static ItemEntry<SpaceArmorComponentItem> SPACE_NANO_CHESTPLATE = REGISTRATE
            .item("space_nanomuscle_chestplate",
                    (p) -> new SpaceArmorComponentItem(GTArmorMaterials.ARMOR, ArmorItem.Type.CHESTPLATE, 5000, p)
                            .setArmorLogic(new NanoMuscleSpaceSuite(ArmorItem.Type.CHESTPLATE, 512,
                                    6_400_000L * (long) Math.max(1,
                                            Math.pow(4, ConfigHolder.INSTANCE.tools.voltageTierNanoSuit - 3)),
                                    ConfigHolder.INSTANCE.tools.voltageTierNanoSuit)))
            .tag(CosmicItemTags.NANOMUSCLE_SPACE_SUITE, ModItemTags.SPACE_SUITS, ModItemTags.FREEZE_RESISTANT_ARMOR,
                    ModItemTags.HEAT_RESISTANT_ARMOR)
            .lang("NanoMuscle™ Space Suite Chestplate")
            .properties(p -> p.rarity(Rarity.RARE))
            .register();
    public static ItemEntry<SpaceArmorComponentItem> ADVANCED_SPACE_NANO_CHESTPLATE = REGISTRATE
            .item("space_advanced_nanomuscle_chestplate",
                    (p) -> new SpaceArmorComponentItem(GTArmorMaterials.ARMOR, ArmorItem.Type.CHESTPLATE, 10000, p)
                            .setArmorLogic(new AdvancedNanoMuscleSpaceSuite(512,
                                    12_800_000L * (long) Math.max(1,
                                            Math.pow(4, ConfigHolder.INSTANCE.tools.voltageTierAdvNanoSuit - 3)),
                                    ConfigHolder.INSTANCE.tools.voltageTierAdvNanoSuit)))
            .tag(CosmicItemTags.NANOMUSCLE_SPACE_SUITE, ModItemTags.SPACE_SUITS, ModItemTags.FREEZE_RESISTANT_ARMOR,
                    ModItemTags.HEAT_RESISTANT_ARMOR)
            .lang("Advanced NanoMuscle™ Space Suite Chestplate")
            .properties(p -> p.rarity(Rarity.EPIC))
            .register();
    public static ItemEntry<SpaceArmorComponentItem> SPACE_QUARK_CHESTPLATE = REGISTRATE
            .item("space_quarktech_chestplate",
                    (p) -> new SpaceArmorComponentItem(GTArmorMaterials.ARMOR, ArmorItem.Type.CHESTPLATE, 20000, p)
                            .setArmorLogic(new QuarkTechSpaceSuite(ArmorItem.Type.CHESTPLATE, 8192,
                                    100_000_000L * (long) Math.max(1,
                                            Math.pow(4, ConfigHolder.INSTANCE.tools.voltageTierQuarkTech - 5)),
                                    ConfigHolder.INSTANCE.tools.voltageTierQuarkTech)))
            .tag(CosmicItemTags.QUARKTECH_SPACE_SUITE, ModItemTags.SPACE_SUITS, ModItemTags.FREEZE_RESISTANT_ARMOR,
                    ModItemTags.HEAT_RESISTANT_ARMOR)
            .lang("QuarkTech™ Space Suite Chestplate")
            .properties(p -> p.rarity(Rarity.RARE))
            .register();
    public static ItemEntry<SpaceArmorComponentItem> ADVANCED_SPACE_QUARK_CHESTPLATE = REGISTRATE
            .item("space_advanced_quarktech_chestplate",
                    (p) -> new SpaceArmorComponentItem(GTArmorMaterials.ARMOR, ArmorItem.Type.CHESTPLATE, 50000, p)
                            .setArmorLogic(new AdvancedQuarkTechSpaceSuite(8192,
                                    1_000_000_000L * (long) Math.max(1,
                                            Math.pow(4, ConfigHolder.INSTANCE.tools.voltageTierAdvQuarkTech - 6)),
                                    ConfigHolder.INSTANCE.tools.voltageTierAdvQuarkTech)))
            .tag(CosmicItemTags.QUARKTECH_SPACE_SUITE, ModItemTags.SPACE_SUITS, ModItemTags.FREEZE_RESISTANT_ARMOR,
                    ModItemTags.HEAT_RESISTANT_ARMOR)
            .lang("Advanced QuarkTech™ Space Suite Chestplate")
            .properties(p -> p.rarity(Rarity.EPIC))
            .register();
    // Oiled up white girl trying to understand what the FUCK an armor tag is, i'm doing to fucking shove a whole
    // pineapple up the ass of whatever mojang employee thought these were **OKAY TO CODE**

    public static ItemEntry<ArmorComponentItem> SANGUINE_WARPTECH_HELMET = REGISTRATE.item("sanguine_warptech_helmet",
            (p) -> new ArmorComponentItem(GTArmorMaterials.ARMOR, ArmorItem.Type.HELMET, p)
                    .setArmorLogic(new HelmetSanguineWarptechSuite(ArmorItem.Type.HELMET,
                            8192,
                            100_000_000L * (long) Math.max(1,
                                    Math.pow(4, ConfigHolder.INSTANCE.tools.voltageTierQuarkTech - 5)),
                            ConfigHolder.INSTANCE.tools.voltageTierQuarkTech)))
            .lang("Sanguine WarpTech Helmet")
            .properties(p -> p.rarity(Rarity.EPIC))
            .tag(CustomTags.PPE_ARMOR)
            .register();

    public static ItemEntry<SpaceArmorComponentItem> SANGUINE_WARPTECH_CHESTPLATE = REGISTRATE
            .item("sanguine_warptech_chestplate",
                    (p) -> new SpaceArmorComponentItem(GTArmorMaterials.ARMOR, ArmorItem.Type.CHESTPLATE, 100000, p)
                            .setArmorLogic(new ChestSanguineWarptechSuite(8192,
                                    10_000_000_000L * (long) Math.max(1,
                                            Math.pow(4, ConfigHolder.INSTANCE.tools.voltageTierAdvQuarkTech - 6)),
                                    ConfigHolder.INSTANCE.tools.voltageTierAdvQuarkTech)))
            .tag(CosmicItemTags.QUARKTECH_SPACE_SUITE, ModItemTags.SPACE_SUITS, ModItemTags.FREEZE_RESISTANT_ARMOR,
                    ModItemTags.HEAT_RESISTANT_ARMOR)
            .lang("Sanguine WarpTech Gravplate")
            .properties(p -> p.rarity(Rarity.EPIC))
            .register();
    public static ItemEntry<ArmorComponentItem> SANGUINE_WARPTECH_LEGGINGS = REGISTRATE
            .item("sanguine_warptech_leggings",
                    (p) -> new ArmorComponentItem(GTArmorMaterials.ARMOR, ArmorItem.Type.LEGGINGS, p)
                            .setArmorLogic(new SanguineWarptechSuite(ArmorItem.Type.LEGGINGS,
                                    8192,
                                    100_000_000L * (long) Math.max(1,
                                            Math.pow(4, ConfigHolder.INSTANCE.tools.voltageTierQuarkTech - 5)),
                                    ConfigHolder.INSTANCE.tools.voltageTierQuarkTech)))
            .lang("Sanguine WarpTech Leggings")
            .properties(p -> p.rarity(Rarity.EPIC))
            .tag(CustomTags.PPE_ARMOR)
            .register();

    public static ItemEntry<ArmorComponentItem> SANGUINE_WARPTECH_BOOTS = REGISTRATE.item("sanguine_warptech_boots",
            (p) -> new ArmorComponentItem(GTArmorMaterials.ARMOR, ArmorItem.Type.BOOTS, p)
                    .setArmorLogic(new SanguineWarptechSuite(ArmorItem.Type.BOOTS,
                            8192,
                            100_000_000L * (long) Math.max(1,
                                    Math.pow(4, ConfigHolder.INSTANCE.tools.voltageTierQuarkTech - 5)),
                            ConfigHolder.INSTANCE.tools.voltageTierQuarkTech)))
            .lang("Sanguine WarpTech Boots")
            .properties(p -> p.rarity(Rarity.EPIC))
            .tag(CustomTags.PPE_ARMOR)
            .register();

    // OMNIA CIRCUITS

    public static final ItemEntry<ComponentItem> OMNIA_CIRCUIT_LV = REGISTRATE
            .item("omnia_circuit_lv", ComponentItem::create)
            .lang("LV Omnia Circuit")
            .properties(p -> p.stacksTo(64))
            .tag(CustomTags.LV_CIRCUITS)
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.omnia_circuit.lv"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OMNIA_CIRCUIT_MV = REGISTRATE
            .item("omnia_circuit_mv", ComponentItem::create)
            .lang("MV Omnia Circuit")
            .properties(p -> p.stacksTo(64))
            .tag(CustomTags.MV_CIRCUITS)
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.omnia_circuit.mv"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OMNIA_CIRCUIT_HV = REGISTRATE
            .item("omnia_circuit_hv", ComponentItem::create)
            .lang("HV Omnia Circuit")
            .properties(p -> p.stacksTo(64))
            .tag(CustomTags.HV_CIRCUITS)
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.omnia_circuit.hv"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OMNIA_CIRCUIT_EV = REGISTRATE
            .item("omnia_circuit_ev", ComponentItem::create)
            .lang("EV Omnia Circuit")
            .properties(p -> p.stacksTo(64))
            .tag(CustomTags.EV_CIRCUITS)
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.omnia_circuit.ev"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OMNIA_CIRCUIT_IV = REGISTRATE
            .item("omnia_circuit_iv", ComponentItem::create)
            .lang("IV Omnia Circuit")
            .properties(p -> p.stacksTo(64))
            .tag(CustomTags.IV_CIRCUITS)
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.omnia_circuit.iv"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OMNIA_CIRCUIT_LUV = REGISTRATE
            .item("omnia_circuit_luv", ComponentItem::create)
            .lang("LuV Omnia Circuit")
            .properties(p -> p.stacksTo(64))
            .tag(CustomTags.LuV_CIRCUITS)
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.omnia_circuit.luv"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OMNIA_CIRCUIT_ZPM = REGISTRATE
            .item("omnia_circuit_zpm", ComponentItem::create)
            .lang("ZPM Omnia Circuit")
            .properties(p -> p.stacksTo(64))
            .tag(CustomTags.ZPM_CIRCUITS)
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.omnia_circuit.zpm"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OMNIA_CIRCUIT_UV = REGISTRATE
            .item("omnia_circuit_uv", ComponentItem::create)
            .lang("UV Omnia Circuit")
            .properties(p -> p.stacksTo(64))
            .tag(CustomTags.UV_CIRCUITS)
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.omnia_circuit.uv"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OMNIA_CIRCUIT_UHV = REGISTRATE
            .item("omnia_circuit_uhv", ComponentItem::create)
            .lang("UHV Omnia Circuit")
            .tag(CustomTags.UHV_CIRCUITS)
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.omnia_circuit.uhv"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OMNIA_CIRCUIT_UEV = REGISTRATE
            .item("omnia_circuit_uev", ComponentItem::create)
            .lang("UEV Omnia Circuit")
            .tag(CustomTags.UEV_CIRCUITS)
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.omnia_circuit.uev"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OMNIA_CIRCUIT_UIV = REGISTRATE
            .item("omnia_circuit_uiv", ComponentItem::create)
            .lang("UIV Omnia Circuit")
            .tag(CustomTags.UIV_CIRCUITS)
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.omnia_circuit.uiv"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OMNIA_CIRCUIT_UXV = REGISTRATE
            .item("omnia_circuit_uxv", ComponentItem::create)
            .lang("UXV Omnia Circuit")
            .tag(CustomTags.UXV_CIRCUITS)
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.omnia_circuit.uxv"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OMNIA_CIRCUIT_OPV = REGISTRATE
            .item("omnia_circuit_opv", ComponentItem::create)
            .lang("OPV Omnia Circuit")
            .tag(CustomTags.OpV_CIRCUITS)
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.omnia_circuit.opv"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RUNE_SLATE_ARKLYS = REGISTRATE
            .item("rune_slate_arklys", ComponentItem::create)
            .lang("Rune Slate [Arklys]")
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.arklys.1"));
                tooltips.add(Component.translatable("cosmiccore.arklys.2"));
                tooltips.add(Component.translatable("cosmiccore.rune_vague"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RUNE_SLATE_TYLOMIR = REGISTRATE
            .item("rune_slate_tylomir", ComponentItem::create)
            .lang("Rune Slate [Tylomir]")
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.tylomir.1"));
                tooltips.add(Component.translatable("cosmiccore.tylomir.2"));
                tooltips.add(Component.translatable("cosmiccore.rune_vague"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RUNE_SLATE_KHORUTH = REGISTRATE
            .item("rune_slate_khoruth", ComponentItem::create)
            .lang("Rune Slate [Khoruth]")
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.khoruth.1"));
                tooltips.add(Component.translatable("cosmiccore.khoruth.2"));
                tooltips.add(Component.translatable("cosmiccore.rune_vague"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RUNE_SLATE_ZELOTHAR = REGISTRATE
            .item("rune_slate_zelothar", ComponentItem::create)
            .lang("Rune Slate [Zelothar]")
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.zelothar.1"));
                tooltips.add(Component.translatable("cosmiccore.zelothar.2"));
                tooltips.add(Component.translatable("cosmiccore.rune_vague"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RUNE_SLATE_TENURA = REGISTRATE
            .item("rune_slate_tenura", ComponentItem::create)
            .lang("Rune Slate [Tenura]")
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.tenura.1"));
                tooltips.add(Component.translatable("cosmiccore.tenura.2"));
                tooltips.add(Component.translatable("cosmiccore.rune_vague"));
            })))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> RUNE_SLATE_VALDRIS = REGISTRATE
            .item("rune_slate_valdris", ComponentItem::create)
            .lang("Rune Slate [Valdris]")
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.valdris.1"));
                tooltips.add(Component.translatable("cosmiccore.valdris.2"));
                tooltips.add(Component.translatable("cosmiccore.rune_vague"));
            })))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> RUNE_CONJUNCTION_VALKRUTH = REGISTRATE
            .item("rune_conjunction_valkruth", ComponentItem::create)
            .lang("Rune Conjunction [Valkruth]")
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.conjuct_valkruth.1"));
                tooltips.add(Component.translatable("cosmiccore.conjuct_valkruth.2"));
                tooltips.add(Component.translatable("cosmiccore.conjuct_valkruth_emotion.1"));
                tooltips.add(Component.translatable("cosmiccore.rune_emotion_weak.1"));
                tooltips.add(Component.translatable("cosmiccore.rune_emotion_weak.2"));
            })))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> RUNE_CONJUNCTION_KHOLYS = REGISTRATE
            .item("rune_conjunction_kholys", ComponentItem::create)
            .lang("Rune Conjunction [Kholys]")
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.conjuct_kholys.1"));
                tooltips.add(Component.translatable("cosmiccore.conjuct_kholys.2"));
                tooltips.add(Component.translatable("cosmiccore.conjuct_kholys_emotion.1"));
                tooltips.add(Component.translatable("cosmiccore.rune_emotion_weak.1"));
                tooltips.add(Component.translatable("cosmiccore.rune_emotion_weak.2"));
            })))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> RUNE_CONJUNCTION_ARKLYTHAR = REGISTRATE
            .item("rune_conjunction_arklythar", ComponentItem::create)
            .lang("Rune Conjunction [Arklythar]")
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.conjuct_arklythar.1"));
                tooltips.add(Component.translatable("cosmiccore.conjuct_arklythar.2"));
                tooltips.add(Component.translatable("cosmiccore.conjuct_arklythar_emotion.1"));
                tooltips.add(Component.translatable("cosmiccore.rune_emotion_weak.1"));
                tooltips.add(Component.translatable("cosmiccore.rune_emotion_weak.2"));
            })))
            .defaultModel()
            .register();
    // Gravity Normalizer Item Variation
    public static final ItemEntry<ComponentItem> PORTABLE_GRAVITY_CORE = REGISTRATE
            .item("portable_gravity_core", ComponentItem::create)
            .lang("§6Portable Gravity Core")
            .tag()
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("item.cosmiccore.portable_gravity_core.tooltip"));
            })))
            .defaultModel()
            .register();
    // infinite spraycan
    public static final ItemEntry<ComponentItem> INFINITE_SPRAY_CAN = REGISTRATE
            .item("infinite_spray_can", ComponentItem::create)
            .lang("§5 Infinite_spray_can")
            .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .properties(p -> p.stacksTo(1))
            .onRegister(attach(new InfiniteSprayCanBehavior(1)))
            .onRegister(modelPredicate(CosmicCore.id("color"),
                    (itemStack) -> (float) itemStack.getOrCreateTag().getInt(InfiniteSprayCanBehavior.ColorTag)))
            .register();

    public static ItemEntry<ComponentItem> NEUTRONITE_FLUID_CELL = GTRegistration.REGISTRATE
            .item("indestructible_fluid_cell", ComponentItem::create)
            .lang("Indestructible %s Fluid Cell")
            .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .color(() -> GTItems::cellColor)
            .onRegister(attach(
                    ThermalFluidStats.create(1024000, 1000000, true, true, true, true, true),
                    new ItemFluidContainer(), cellName()))
            .register();
    // Drones
    public static final ItemEntry<ComponentItem> RUSTY_DRONE = REGISTRATE
            .item("rusty_drone", ComponentItem::create)
            .lang("Rusty Drone")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ROBUST_DRONE = REGISTRATE
            .item("robust_drone", ComponentItem::create)
            .lang("Robust Drone")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> INDUSTRIAL_DRONE = REGISTRATE
            .item("industrial_drone", ComponentItem::create)
            .lang("Industrial Drone")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> SANGUINE_DRONE = REGISTRATE
            .item("sanguine_drone", ComponentItem::create)
            .lang("Sanguine Drone")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PLASMATIC_DRONE = REGISTRATE
            .item("plasmatic_drone", ComponentItem::create)
            .lang("plasmatic_drone")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();

    // WildFire Cores
    public static final ItemEntry<ComponentItem> LV_WILDFIRE_CORE = REGISTRATE
            .item("lv_wildfire_core", ComponentItem::create)
            .lang("LV Wildfire Core")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> MV_WILDFIRE_CORE = REGISTRATE
            .item("mv_wildfire_core", ComponentItem::create)
            .lang("MV Wildfire Core")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> HV_WILDFIRE_CORE = REGISTRATE
            .item("hv_wildfire_core", ComponentItem::create)
            .lang("HV Wildfire Core")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> EV_WILDFIRE_CORE = REGISTRATE
            .item("ev_wildfire_core", ComponentItem::create)
            .lang("EV Wildfire Core")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> IV_WILDFIRE_CORE = REGISTRATE
            .item("iv_wildfire_core", ComponentItem::create)
            .lang("IV Wildfire Core")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> LUV_WILDFIRE_CORE = REGISTRATE
            .item("luv_wildfire_core", ComponentItem::create)
            .lang("LuV Wildfire Core")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ZPM_WILDFIRE_CORE = REGISTRATE
            .item("zpm_wildfire_core", ComponentItem::create)
            .lang("ZPM Wildfire Core")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> BASIC_GENE_KIT = REGISTRATE
            .item("basic_gene_kit", ComponentItem::create)
            .lang("Basic Gene Kit")
            .properties(p -> p.stacksTo(16))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> INTERMEDIATE_GENE_KIT = REGISTRATE
            .item("intermediate_gene_kit", ComponentItem::create)
            .lang("Intermediate Gene Kit")
            .properties(p -> p.stacksTo(16))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> ADVANCED_GENE_KIT = REGISTRATE
            .item("advanced_gene_kit", ComponentItem::create)
            .lang("Advanced Gene Kit")
            .properties(p -> p.stacksTo(16))
            .defaultModel()
            .register();

    // MANA WAFERS AND CHIPS
    public static final ItemEntry<ComponentItem> LATENT_CAPACITY_WAFER = REGISTRATE
            .item("latent_capacity_wafer", ComponentItem::create)
            .lang("Latent Capacity Wafer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> LATENT_EFFICACY_WAFER = REGISTRATE
            .item("latent_efficacy_wafer", ComponentItem::create)
            .lang("Latent Efficacy Wafer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> LATENT_POTENCY_WAFER = REGISTRATE
            .item("latent_potency_wafer", ComponentItem::create)
            .lang("Latent Potency Wafer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> LATENT_VERBOSITY_WAFER = REGISTRATE
            .item("latent_verbosity_wafer", ComponentItem::create)
            .lang("Latent Verbosity Wafer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    // BOULE AND WAFER
    public static final ItemEntry<ComponentItem> LIVINGROCK_ALUMINATE_BOULE = REGISTRATE
            .item("livingrock_aluminate_boule", ComponentItem::create)
            .lang("Livingrock Aluminate Boule")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> LIVINGROCK_ALUMINATE_WAFER = REGISTRATE
            .item("livirock_aluminite_wafer", ComponentItem::create)
            .lang("Livingrock Aluminate Wafer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    // CHIPS
    public static final ItemEntry<ComponentItem> CAPACITY_CHIP = REGISTRATE
            .item("capacity_chip", ComponentItem::create)
            .lang("Capacity Chip")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> EFFICACY_CHIP = REGISTRATE
            .item("efficacy_chip", ComponentItem::create)
            .lang("Efficacy Chip")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> POTENCY_CHIP = REGISTRATE
            .item("potency_chip", ComponentItem::create)
            .lang("Potency Chip")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> VERBOSITY_CHIP = REGISTRATE
            .item("verbosity_chip", ComponentItem::create)
            .lang("Verbosity Chip")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();

    // Project Star Eater

    public static final ItemEntry<ComponentItem> HAULER_PROBE_GRADE_1 = REGISTRATE
            .item("freight_beetle_grade_1", ComponentItem::create)
            .lang("Freight Beetle Mk.1")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> ARMORED_HAULER_PROBE_GRADE_1 = REGISTRATE
            .item("armored_freight_beetle_grade_1", ComponentItem::create)
            .lang("Armored Freight Beetle Mk.1")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> CRUSHER_WASPS_GRADE_1 = REGISTRATE
            .item("crusher_wasps_grade_1", ComponentItem::create)
            .lang("Crusher Wasps Mk.1")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> RAZOR_HORNET_GRADE_1 = REGISTRATE
            .item("razor_hornet_grade_1", ComponentItem::create)
            .lang("Razor Hornet Mk.1")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> PULVERIZING_BEETLE_GRADE_1 = REGISTRATE
            .item("pulverizing_beetle_grade_1", ComponentItem::create)
            .lang("Pulverizing Beetle Mk.1")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> CRUCIBLE_MANTIS_GRADE_1 = REGISTRATE
            .item("crucible_mantis_grade_1", ComponentItem::create)
            .lang("Crucible Mantis Mk.1")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> HAULER_PROBE_GRADE_2 = REGISTRATE
            .item("freight_beetle_grade_2", ComponentItem::create)
            .lang("Freight Beetle Mk.2")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> ARMORED_HAULER_PROBE_GRADE_2 = REGISTRATE
            .item("armored_freight_beetle_grade_2", ComponentItem::create)
            .lang("Armored Freight Beetle Mk.2")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> CRUSHER_WASPS_GRADE_2 = REGISTRATE
            .item("crusher_wasps_grade_2", ComponentItem::create)
            .lang("Crusher Wasps Mk.2")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> RAZOR_HORNET_GRADE_2 = REGISTRATE
            .item("razor_hornet_grade_2", ComponentItem::create)
            .lang("Razor Hornet Mk.2")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> PULVERIZING_BEETLE_GRADE_2 = REGISTRATE
            .item("pulverizing_beetle_grade_2", ComponentItem::create)
            .lang("Pulverizing Beetle Mk.2")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> CRUCIBLE_MANTIS_GRADE_2 = REGISTRATE
            .item("razor_hornet_grade_2", ComponentItem::create)
            .lang("Crucible Mantis Mk.2")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();
    // GRADE 3

    public static final ItemEntry<ComponentItem> HAULER_PROBE_GRADE_3 = REGISTRATE
            .item("freight_beetle_grade_3", ComponentItem::create)
            .lang("Freight Beetle Mk.3")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> ARMORED_HAULER_PROBE_GRADE_3 = REGISTRATE
            .item("armored_freight_beetle_grade_3", ComponentItem::create)
            .lang("Armored Freight Beetle Mk.3")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> CRUSHER_WASPS_GRADE_3 = REGISTRATE
            .item("crusher_wasps_grade_3", ComponentItem::create)
            .lang("Crusher Wasps Mk.3")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> RAZOR_HORNET_GRADE_3 = REGISTRATE
            .item("razor_hornet_grade_3", ComponentItem::create)
            .lang("Razor Hornet Mk.3")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> PULVERIZING_BEETLE_GRADE_3 = REGISTRATE
            .item("pulverizing_beetle_grade_3", ComponentItem::create)
            .lang("Pulverizing Beetle Mk.3")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> CRUCIBLE_MANTIS_GRADE_3 = REGISTRATE
            .item("razor_hornet_grade_3", ComponentItem::create)
            .lang("Crucible Mantis Mk.3")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    // GRADE 4

    public static final ItemEntry<ComponentItem> HAULER_PROBE_GRADE_4 = REGISTRATE
            .item("freight_beetle_grade_4", ComponentItem::create)
            .lang("Freight Beetle Mk.4")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> ARMORED_HAULER_PROBE_GRADE_4 = REGISTRATE
            .item("armored_freight_beetle_grade_4", ComponentItem::create)
            .lang("Armored Freight Beetle Mk.4")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> CRUSHER_WASPS_GRADE_4 = REGISTRATE
            .item("crusher_wasps_grade_4", ComponentItem::create)
            .lang("Crusher Wasps Mk.4")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> RAZOR_HORNET_GRADE_4 = REGISTRATE
            .item("razor_hornet_grade_4", ComponentItem::create)
            .lang("Razor Hornet Mk.4")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> PULVERIZING_BEETLE_GRADE_4 = REGISTRATE
            .item("pulverizing_beetle_grade_4", ComponentItem::create)
            .lang("Pulverizing Beetle Mk.4")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> CRUCIBLE_MANTIS_GRADE_4 = REGISTRATE
            .item("razor_hornet_grade_4", ComponentItem::create)
            .lang("Crucible Mantis Mk.4")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> HAULER_PROBE_GRADE_5 = REGISTRATE
            .item("freight_beetle_grade_5", ComponentItem::create)
            .lang("Freight Beetle Mk.5")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> ARMORED_HAULER_PROBE_GRADE_5 = REGISTRATE
            .item("armored_freight_beetle_grade_5", ComponentItem::create)
            .lang("Armored Freight Beetle Mk.5")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> CRUSHER_WASPS_GRADE_5 = REGISTRATE
            .item("crusher_wasps_grade_5", ComponentItem::create)
            .lang("Crusher Wasps Mk.5")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> RAZOR_HORNET_GRADE_5 = REGISTRATE
            .item("razor_hornet_grade_5", ComponentItem::create)
            .lang("Razor Hornet Mk.5")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> PULVERIZING_BEETLE_GRADE_5 = REGISTRATE
            .item("pulverizing_beetle_grade_5", ComponentItem::create)
            .lang("Pulverizing Beetle Mk.5")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> CRUCIBLE_MANTIS_GRADE_5 = REGISTRATE
            .item("razor_hornet_grade_5", ComponentItem::create)
            .lang("Crucible Mantis Mk.5")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static ItemEntry<ComponentItem> LINKED_TERMINAL = REGISTRATE
            .item("linked_terminal", ComponentItem::create)
            .lang("Linked Terminal")
            .model((ctx, prov) -> prov.generated(
                    ctx::getEntry,
                    prov.modLoc("item/terminal/linked_terminal"),
                    prov.modLoc("item/terminal/terminal_overlay")))
            .properties(p -> p.stacksTo(1))
            .onRegister(attach(new LinkedTerminalBehavior()))
            .register();

    public static final ItemEntry<AsteroidItem> CARBON_ASTEROID = REGISTRATE
            .item("carbon_asteroid_base", AsteroidItem::new)
            .lang("Carbonic Asteroid")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .onRegister(attachRenderer(() -> RadianceItemRenderer.INSTANCE))
            .register();
    public static final ItemEntry<AsteroidItem> FERRIC_ASTEROID = REGISTRATE
            .item("ferric_asteroid", AsteroidItem::new)
            .lang("Ferric Asteroid")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .onRegister(attachRenderer(() -> RadianceItemRenderer.INSTANCE))
            .register();

    public static final ItemEntry<AsteroidItem> RARE_METAL_ASTEROID = REGISTRATE
            .item("rare_metals_asteroid", AsteroidItem::new)
            .lang("Exotic Metals Asteroid")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .onRegister(attachRenderer(() -> RadianceItemRenderer.INSTANCE))
            .register();

    public static final ItemEntry<AsteroidItem> AURIC_ASTEROID = REGISTRATE
            .item("auric_asteroid", AsteroidItem::new)
            .lang("Auric Asteroid")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .onRegister(attachRenderer(() -> RadianceItemRenderer.INSTANCE))
            .register();

    public static final ItemEntry<AsteroidItem> BRIMSTONE_ASTEROID = REGISTRATE
            .item("brimstone_asteroid", AsteroidItem::new)
            .lang("Brimstone Asteroid")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .onRegister(attachRenderer(() -> RadianceItemRenderer.INSTANCE))
            .register();

    public static final ItemEntry<AsteroidItem> LITH_ASTEROID = REGISTRATE
            .item("lith_asteroid", AsteroidItem::new)
            .lang("Lith Asteroid")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .onRegister(attachRenderer(() -> RadianceItemRenderer.INSTANCE))
            .register();

    public static final ItemEntry<AsteroidItem> MAFIC_ASTEROID = REGISTRATE
            .item("mafic_asteroid", AsteroidItem::new)
            .lang("Mafic Asteroid")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .onRegister(attachRenderer(() -> RadianceItemRenderer.INSTANCE))
            .register();

    public static final ItemEntry<AsteroidItem> MOSSY_ASTEROID = REGISTRATE
            .item("mossy_asteroid", AsteroidItem::new)
            .lang("Mossy Asteroid")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .onRegister(attachRenderer(() -> RadianceItemRenderer.INSTANCE))
            .register();

    public static final ItemEntry<AsteroidItem> OCCULT_ASTEROID = REGISTRATE
            .item("occult_asteroid", AsteroidItem::new)
            .lang("Occult Asteroid")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .onRegister(attachRenderer(() -> RadianceItemRenderer.INSTANCE))
            .register();

    public static final ItemEntry<AsteroidItem> OXIDE_ASTEROID = REGISTRATE
            .item("oxide_asteroid", AsteroidItem::new)
            .lang("Oxide Asteroid")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .onRegister(attachRenderer(() -> RadianceItemRenderer.INSTANCE))
            .register();

    public static final ItemEntry<AsteroidItem> SANGUINE_ASTEROID = REGISTRATE
            .item("sanguine_asteroid", AsteroidItem::new)
            .lang("Sanguine Asteroid")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .onRegister(attachRenderer(() -> RadianceItemRenderer.INSTANCE))
            .register();

    public static final ItemEntry<AsteroidItem> WASTELAND_ASTEROID = REGISTRATE
            .item("wasteland_asteroid", AsteroidItem::new)
            .lang("Wasteland Asteroid")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .onRegister(attachRenderer(() -> RadianceItemRenderer.INSTANCE))
            .register();

    public static final ItemEntry<ComponentItem> TUNGSTENSTEEL_NANOLATTICE_SPOOL = REGISTRATE
            .item("tungstensteel_nanolattice_spool", ComponentItem::create)
            .lang("Tungstensteel Nanolattice Spool")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> TRINAVINE_NANOLATTICE_SPOOL = REGISTRATE
            .item("trinavine_nanolattice_spool", ComponentItem::create)
            .lang("Trinavine Nanolattice Spool")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    // What we'd write our NBT ON and Read in LARVA
    public static final ItemEntry<AsteroidTargetingChipItem> TARGETING_CHIP = REGISTRATE
            .item("asteroid_targeting_chip", props -> new AsteroidTargetingChipItem(props.stacksTo(1)))
            .lang("Asteroid Targeting Chip")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .register();

    public static ICustomDescriptionId cellName() {
        return new ICustomDescriptionId() {

            @Override
            public Component getItemName(ItemStack stack) {
                Component prefix = FluidUtil.getFluidContained(stack).map(FluidStack::getDisplayName)
                        .orElse(Component.translatable("gtceu.fluid.empty"));
                return Component.translatable(stack.getDescriptionId(), prefix);
            }
        };
    }

    public static <T extends ComponentItem> NonNullConsumer<T> attach(IItemComponent... components) {
        return item -> item.attachComponents(components);
    }

    public static <T extends Item> NonNullConsumer<T> modelPredicate(ResourceLocation predicate,
                                                                     Function<ItemStack, Float> property) {
        return item -> {
            if (GTCEu.isClientSide()) {
                ItemProperties.register(item, predicate, (itemStack, c, l, i) -> property.apply(itemStack));
            }
        };
    }

    public static MalumSpiritType register(MalumSpiritType spiritType) {
        SPIRITS.put(spiritType.identifier, spiritType);
        return spiritType;
    }

    public static void init() {}
}
