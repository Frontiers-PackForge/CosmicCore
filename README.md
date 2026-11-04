The Core Mod of CosmicFrontiers, holding most things which make it work.

# Disclaimer and Mixin Transparency Statement

Generally speaking This isn't relevant, but I will write it here regardless after some conversations today that make me believe transparency is best.
<details>

I am aware that some devs may not agree regarding the use of mixins or a custom pack mod to change mod behavior, this is perfectly acceptable. So i will make it very clear to the average user as well as devs  that arrives here due to bug reports regarding this mod or theirs.
## A. Under No Circumstance is this mod Designed, Deployed, or Meant for **EXTERNAL USAGE** outside of the modpack Cosmic Frontiers
I waive the responsibilities of any mod-dev who receieves a report from a user using this mod externally due to pack changes or changes made by this mod.
This however doesn't mean you can just ignore issues, if there's an issue caused by my work and someone reports it, I'd appreciate the issue being referenced back to here so i can fix the problem or resolve the issue entirely!
In summary as long as CosmicCore is to blame for the issue, mod devs are not responsible for direct compatibility with CosmicCore as it is my job as a Pack-Dev to fix that custom content. - This does not apply to cross-mod issues excluding for (example; JEI and Generic Random Mods JEIPlugin)

## B. Any issues regarding bugs in the modpack `Cosmic Frontiers` must be reported to us firstly.
This is to verify it isn't *OUR FAULT*.
DO NOT BLAME OTHER MODS FOR CAUSING ISSUES IF THEY ARE TARGETED BY A MIXIN OF **ANY KIND** (AD ASTRA, EMI, GTM, LDLIB, TINKERS, ETC), HONESTLY DONT BLAME ANOTHER MOD AT ALL. 
Modpacks are insane abominations held together by love, passion, and excessively spent sanity. NEVER take your frustration out on another person.

In summary, I do not want my pack users to point fingers at others, and I'll take a proactive stance in saying this.
If you are a mod dev reading this due to a user report, Feel free to show them this if deemed appropriate. If you wish to then reach out to me so resolve any problems or make me aware of better solutions or just to tell me to fix it, feel free to reach out to me on my discord. 
To Conclude any help or collaboration with mods I tweak would be appreciated, this is here to shield you fron idiotic end users demanding fixes that they should be demanding me fixes for lol.
</details>


# License

## Code

CosmicCore code is licensed under LGPL-3.0 unless explicitly stated otherwise in its own notice.

## Assets

Assets have folder level `LICENSE.md` notices. Read the nearest notice and any file detail or
upstream notice before reusing an asset. These defaults apply only to CosmicCore original works.
Imported assets and derivatives retain their upstream License wherever they are stored.

Paths below are relative to `src/main/resources/assets/cosmiccore/`:

## Folder Paths and Asset Types
 
 `textures/` - All Rights Reserved 
 
 `textures/item/` - MIT 
 
 `textures/block/` - MIT except explicitly marked subfolders 
 
 `textures/block/iris/` - All Rights Reserved 
 
 `models/`, including `block/iris/` and both `unique/` folders - All Rights Reserved 
 
 `shaders/` and `pinwheel/shaders/` - All Rights Reserved 
 
 `.obj`, `.mtl`, `.fsh`, and `.vsh` files elsewhere - All Rights Reserved unless explicitly stated otherwise 
 
 `textures/aeroponics_seedlings/` - CropsNH-derived textures: LGPL-3.0, including recolours 

The same resource paths in `src/generated/resources/` inherit these notices. Existing license grants
for previously published material remain valid! These defaults do not revoke or mutate them!

An MIT folder may contain a differently licensed subtree only when that exception has its own
`LICENSE.md`, or an specific entry in the local notice. Moving or recoloring an import
does not put it under the enclosing folder's license.
See the [bundled asset policy](src/main/resources/assets/cosmiccore/LICENSE.md)
and [CropsNH provenance](src/main/resources/licenses/aeroponics/assets.json).

MIT permits reuse subject to its notice requirements. All Rights Reserved areas require permission
for reuse beyond any separate grants. Upstream licenses dictate imported assets. Please contact
the project through its issue tracker if attribution or licensing information needs correction, this is done in good faith and any corrections are welcomed!

## Code Credits
[Gregtech Modern](https://github.com/GregTechCEu/GregTech-Modern) for plenty of useful code references and invaluable knowledge in learning Java.

[Lodestone](https://github.com/LodestarMC/Lodestone) - For the amazing rendering tools it offers, we use it occasionally for visual effects

[Effortless Building](https://github.com/Requios/effortless-building-multi) - Used for backporting 26.2 Upstream changes to 1.21.1 EB as it seems no backport efforts are currently being made, as well as used for GT pipe integration.

[Embers Re-Ignited](https://www.curseforge.com/minecraft/mc-mods/embers-re-ignited) - Used to reference ember network routing and implement our own routing capable blocks/hatches

[Applied Energistics 2](https://github.com/AppliedEnergistics/Applied-Energistics-2) - Used to make general compatibility patches for AE, and overall requires sometimes modifying core AE2 classes. Granted most of this work has been offloaded outside of Core to a separate helper mod.

## Assets Credits
- [CropsNH](https://github.com/GTNewHorizons/CropsNH) - Crop textures used by the aeroponics planter overrides. sources and licenses are recorded in the bundled provenance.

- [GTCEu:Modern](https://www.curseforge.com/minecraft/mc-mods/gregtechceu-modern) - General material assets

- [Regions Unexplored](https://www.curseforge.com/minecraft/mc-mods/regions-unexplored) - Reused assets for plants, crystals, other biomatter stuff, thanks!

- [Create](https://www.curseforge.com/minecraft/mc-mods/create) Note : This strictly uses assets that fall under MIT prior to their "All Rights Reserved" Change in Sept 2025. No New ARR assets have been used. Was used to inspire some various textures in similar stiles

- [Embers Re-Ignited](https://www.curseforge.com/minecraft/mc-mods/embers-re-ignited) Used for Custom Ember Emitter/Receptor Model bases

- [Traveler's Boots](https://www.curseforge.com/minecraft/mc-mods/travelers-boots) Boot Textures

