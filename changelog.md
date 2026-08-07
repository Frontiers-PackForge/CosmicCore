# Cosmic Core 0.17.1
# DO NOT MANUALLY UPDATE COSMIC CORE FOR FRONTIERS **IT WILL NOT WORK**

## New

- Added the Firmament 
- Added permit-gated Firmament travel through an Occultism ascension ritual, plus "Set with the Sun" hold-sneak return sequence.
- Added tiered Crystallizers from MV through UIV with systematic ore crystallization and gem-growth slurry processing.
- Added Cannon Powder, Nyctophyte exchange media, Pyroltic contact catalysts, the Polarized Bathyst Electret, and Utherium ceramic components for new processing chains.
- Added the Lightweight Stainless Steel Casing.

## Improvements

- Renamed the Abyss crystal and flora families to Arcanite, Bloomrot, Ripe Abyss Vine, Anchored Bloomrot, and Lantern Bulb while preserving old registry IDs through aliases.
- Rebuilt the Large Arcanite Cluster as a two-block crossed model with a matching collision shape.
- Added translated names for CosmicCore recipe maps
- Added a dedicated two-by-two item and fluid layout for Roaster recipe displays.
- Added Sterling Silver foil and Terrasteel fine-wire material forms.
- Preserved intrinsic armor, toughness, and knockback attributes when pack scripts add temperature or soul-ward attributes to armor.
- Updated the embedded GregTech Modern
- Removed the obsolete Aether-specific travel and Curios compatibility layer now that the Firmament is fully in-house.

## Fixes

- Fixed the Debug Structure Writer collapsing rotations to six facings instead of preserving all twenty-four cube orientations.
- Fixed older saved GregTech recipes failing to load when their serialized data predates parallel-tracking fields.
- Removed stale pack recipes that duplicated CosmicCore's systematic flawless-gem implosion family.
- Fixed Dreamer's Basin attaching recipe-thread traits after machine load and losing their progress across saves.
- Fixed duplicate invalid-structure text in CosmicCore's generic MUI multiblock panel.
- Fixed Firmament storm animation stepping, visual noise, sunset distance banding, and the near-camera lighting dead zone.
- Fixed LP and HP Steam Benders generating without their directional steam-vent overlay.
- Fixed Hard Hammer drop conversion using the wrong loop recipe, sharing mutable output stacks, and ignoring configured drop chance.
- Fixed Large Miner post-processing attempting invalid zero-slot recipes or selecting recipes above the miner's voltage tier.
- Fixed multiblocks repeating a recipe from the previous recipe-map mode, including after restarting during an active craft.
- Fixed multithreaded machines discarding scored recipe failures before they could report the most relevant failure reason.
- Restored the Assembly Line's environment requirement while applying CosmicCore recipe modifiers.
- Added compatibility aliases for renamed Buzzsaw Blade and isotope material IDs used by older saves and progression data.
