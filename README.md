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

## C. For Ease of Developer Access and time saving, below is a short list of mods that are using mixins in cosmiccore. 
These devs may take the time to observe how poor of a programmer I am and laugh at my choices. Any help you give can be out of good will and isn't required. If you spot someone using this mod externailly in a random pack that IS NOT CosmicFrontiers and You're the owner of one of these mods, refer them to here.
 
 `The Big Scary Mixins that'll probably get me glared at, if you're the owner of one of these mods and have a better solution, do tell either via an issue or shoot me a message in my discord!`
 
 `This section mostly applies to things that modify varibles or overwrites methods entirely`
 - AE2 ; Mixins for Style Guide redirections for custom widgets and UI overwrites as well as PatternProvider Behaviors. I unfortunately was not able to find an elegant solution to this within their API, either by my own lack of skill or their lack of extensibility(or readability).
 - EMI ; We Directly Mixin into `RecipeScreen.class` to change the size limit of catalyst slots as well as `EmiApi.class` to Alias Buckets/Fluid Containers to Show their Fluid Stack when clicked/checked for recipes on
 - Apotheosis ; Hardcoded Recipes for many things that really shouldn't be but I could be wrong - We remove those recipes and replace them with our own.
 - Legendary Survival Overhaul ; We mixin into the way damage scaling is handled by Frostbite and Heat Stroke
   
 `Less Scary Mixins that are less likely to be massive 'toe steppers' as one would like to reference`
 - Create ; We mixin into the Diving Helmet to work with "Thin Air".
 - Ad Astra ; We Mixin into spacesuits to apply oxygen tanks to our own gear. We Also Hide the "LAND" button for planets that don't need them (Gas Giants, ETC)
 - GTCEu:Modern ; We Mixin unto GTValues to change some color values.
 - Tinkers Construct ; We Mixin into `ModifiableItem.class` to allow Tinkers Tools to work in GTCEu:Modern Crafting Recipes and consume durability when said recipe is crafted. Prior to this tools just got consumed.

 To Conclude any help or collaboration with mods I tweak would be appreciated, this is here to shield you fron idiotic end users demanding fixes that they should be demanding me fixes for lol.
</details>


# License
## Code
All Code is issued under LGPL-3.0 unless explicitly stated within the file header
## Assets
### All Files with the extension `.mtl`, `.obj`,`.fsh`, and `.vsh` are All Rights reserved.
### All assets in `resources/assets/cosmiccore/textures` are licensed under All Rights Reserved by default
Exclusions to this rule are as follows  
 - All files contained in the subfolder(s) `item` are licensed as MIT
 - All files contained in the subfolder(s) `block` EXCLUDING `iris` are licensed as MIT

```
Human readable format:
You may use code freely under LGPL-3.0, and most assets are licensed under MIT.
You are NOT allowed to use my .mtl, .obj, .fsh, or .vsh files or associated assets linked to them for any reason.
Other Assets from Different Mods are used and in good faith have I tried to make sure I follow their respective licenses , please reach out to me if something needs to be removed, thanks!
```

Using code/assets from this project requires attribution in both cases and requires particular licensing, please keep that in mind.

## Code Credits
[Gregtech Modern](https://github.com/GregTechCEu/GregTech-Modern) for plenty of useful code references and invaluable knowledge in learning Java.
[Lodestone](https://github.com/LodestarMC/Lodestone) - For the amazing rendering tools it offers, we use it occasionally for visual effects
[Effortless Building](https://github.com/Requios/effortless-building-multi) - Used for backporting 26.2 Upstream changes to 1.21.1 EB as it seems no backport efforts are currently being made, as well as used for GT pipe integration.
[Embers Re-Ignited](https://www.curseforge.com/minecraft/mc-mods/embers-re-ignited) - Used to reference ember network routing and implement our own routing capable blocks/hatches
[Applied Energistics 2](https://github.com/AppliedEnergistics/Applied-Energistics-2) - Used to make general compatibility patches for AE, and overall requires sometimes modifying core AE2 classes. Granted most of this work has been offloaded outside of Core to a separate helper mod.

## Assets Credits
- [GTCEu:Modern](https://www.curseforge.com/minecraft/mc-mods/gregtechceu-modern) - General material assets

- [Regions Unexplored](https://www.curseforge.com/minecraft/mc-mods/regions-unexplored) - Reused assets for plants, crystals, other biomatter stuff, thanks!

- [Create](https://www.curseforge.com/minecraft/mc-mods/create) Note : This strictly uses assets that fall under MIT prior to their "All Rights Reserved" Change in Sept 2025. No New ARR assets have been used. Was used to inspire some various textures in similar stiles

- [Embers Re-Ignited](https://www.curseforge.com/minecraft/mc-mods/embers-re-ignited) Used for Custom Ember Emitter/Receptor Model bases

- [Traveler's Boots](https://www.curseforge.com/minecraft/mc-mods/travelers-boots) Boot Textures

