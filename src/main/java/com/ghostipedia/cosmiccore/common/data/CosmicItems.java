package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.api.registries.CosmicRegistries;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistries.COSMIC_REGISTRATE;


@SuppressWarnings("Convert2MethodRef")
public class CosmicItems  {
    static {
        CosmicRegistries.COSMIC_REGISTRATE.creativeModeTab(() -> CosmicCreativeModeTabs.COSMIC_CORE);
    }
//Literally Random shit
    public static final ItemEntry<ComponentItem> DONK = COSMIC_REGISTRATE.item("donk", ComponentItem::create)
            .lang("Donk")
            .properties(p -> p.stacksTo(16))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> WAXED_LEATHER = COSMIC_REGISTRATE.item("waxed_leather", ComponentItem::create)
            .lang("Waxed Leather")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ENGOURGED_SPORE = COSMIC_REGISTRATE.item("engourged_spore", ComponentItem::create)
            .lang("Engourged Spore")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> BLACKSTONE_PUSTULE = COSMIC_REGISTRATE.item("blackstone_pustule", ComponentItem::create)
            .lang("Blackstone Pustule")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();


 //New Circuits
 //Echo (ZPM-UEV)
    public static final ItemEntry<ComponentItem> SONAR_PROCESSOR = COSMIC_REGISTRATE.item("echo_processor", ComponentItem::create)
            .lang("Echo Processor")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> SONAR_PROCESSOR_ASSEMBLY = COSMIC_REGISTRATE.item("echo_processor_assembly", ComponentItem::create)
            .lang("Echo Processor Assembly")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> SONAR_PROCESSOR_SUPERCOMPUTER = COSMIC_REGISTRATE.item("echo_processor_supercomputer", ComponentItem::create)
            .lang("Echo Processor Supercomputer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> SONAR_PROCESSOR_MAINFRAME = COSMIC_REGISTRATE.item("echo_processor_mainframe", ComponentItem::create)
            .lang("Echo Processor Mainframe")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
//Optical (UV-UIV)
    public static final ItemEntry<ComponentItem> OPTICAL_PROCESSOR = COSMIC_REGISTRATE.item("optical_processor", ComponentItem::create)
            .lang("Optical Processor")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OPTICAL_PROCESSOR_ASSEMBLY = COSMIC_REGISTRATE.item("optical_processor_assembly", ComponentItem::create)
            .lang("Optical Processor Assembly")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OPTICAL_PROCESSOR_SUPERCOMPUTER = COSMIC_REGISTRATE.item("optical_processor_supercomputer", ComponentItem::create)
            .lang("Optical Processor Supercomputer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OPTICAL_PROCESSOR_MAINFRAME = COSMIC_REGISTRATE.item("optical_processor_mainframe", ComponentItem::create)
            .lang("Optical Processor Mainframe")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
//Cosmic (UHV-UXV)
    public static final ItemEntry<ComponentItem> COSMIC_PROCESSOR = COSMIC_REGISTRATE.item("cosmic_processor", ComponentItem::create)
            .lang("Cosmic Processor")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> COSMIC_PROCESSOR_ASSEMBLY = COSMIC_REGISTRATE.item("cosmic_processor_assembly", ComponentItem::create)
            .lang("Cosmic Processor Assembly")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> COSMIC_PROCESSOR_SUPERCOMPUTER = COSMIC_REGISTRATE.item("cosmic_processor_supercomputer", ComponentItem::create)
            .lang("Cosmic Processor Supercomputer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> COSMIC_PROCESSOR_MAINFRAME = COSMIC_REGISTRATE.item("cosmic_processor_mainframe", ComponentItem::create)
            .lang("Cosmic Processor Mainframe")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
//Psionic Circuit (UEV-OPV)
    public static final ItemEntry<ComponentItem> PSIONIC_PROCESSOR = COSMIC_REGISTRATE.item("psionic_processor", ComponentItem::create)
            .lang("Psionic Processor")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PSIONIC_PROCESSOR_ASSEMBLY = COSMIC_REGISTRATE.item("psionic_processor_assembly", ComponentItem::create)
            .lang("Psionic Processor Assembly")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PSIONIC_PROCESSOR_SUPERCOMPUTER = COSMIC_REGISTRATE.item("psionic_processor_supercomputer", ComponentItem::create)
            .lang("Psionic Processor Supercomputer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PSIONIC_PROCESSOR_MAINFRAME = COSMIC_REGISTRATE.item("psionic_processor_mainframe", ComponentItem::create)
            .lang("Psionic Processor Mainframe")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    //Macroverse (UIV-MAX)
    public static final ItemEntry<ComponentItem> MACROVERSE_PROCESSOR = COSMIC_REGISTRATE.item("macroverse_processor", ComponentItem::create)
            .lang("Macroverse Processor")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> MACROVERSE_PROCESSOR_ASSEMBLY = COSMIC_REGISTRATE.item("macroverse_processor_assembly", ComponentItem::create)
            .lang("Macroverse Processor Assembly")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> MACROVERSE_PROCESSOR_SUPERCOMPUTER = COSMIC_REGISTRATE.item("macroverse_processor_supercomputer", ComponentItem::create)
            .lang("Macroverse Processor Supercomputer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> MACROVERSE_PROCESSOR_MAINFRAME = COSMIC_REGISTRATE.item("macroverse_processor_mainframe", ComponentItem::create)
            .lang("Macroverse Processor Mainframe")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();

    public static <T extends ComponentItem> NonNullConsumer<T> attach(IItemComponent... components) {
        return item -> item.attachComponents(components);
    }

    public static void init() {

    }
}