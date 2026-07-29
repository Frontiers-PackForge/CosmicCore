# Cosmic Core 0.16.0
# DO NOT MANUALLY UPDATE COSMIC CORE FOR FRONTIERS **IT WILL NOT WORK**

## New and Reworked

- Introduced Deeds and the Inner Vault, including quest-linked Deed Seals, primed quest states, cinematics, team progression, and retroactive Patient Zero eligibility and prompting.
- Added a Tier 2 Electric Blast Furnace structure with multiblock preview and terminal support. Consecutive matching recipes gain up to a 50% duration reduction.
- Simplified the Bloomwyrm campus around Bloomwyrm Charge and Biopower, removing the seasonal essence and reserve-mode systems.
- Added algae, proto-algae, Bloomscrap, and new MV component items.
- Reworked Abyssal Hollow terrain placement and ore distribution.
- Added gravity-aware Murk Kelp and corrected Murkbloom disturbance from area-of-effect tools.

## Interface and Quest Improvements

- Rebuilt EMI bookmark groups with exact stack amounts, recipe favorites, paging, and persistent groups.
- Added EMI composite-ore sorting diagrams.
- Limited custom ore chunks and purification families to bundle materials while hiding unused generated ore forms from EMI.
- Expanded FTB Quests dependency-line editing, batch painting, and quest aliases.
- Added sealed, primed, calling, and woven presentation states for Deed quests.

## Materials

- The following material renames change their registry identities rather than only their display names. Old stacks using removed identities are not migrated.
- Renamed Rogdorium to Aphotite.
- Renamed Soulshade to Bathyst.
- Renamed Duskmote to Nyctophyte.
- Renamed Shimmerbloom to Phycolite.
- Renamed Chlorophyte to Nostium.
- Added Blooming Sludge, Bloom Rich Algae Solution, and Phyto-Grease.

## Fixes

- Prevented Extraction Drills from mining and voiding ore when their output storage is full.
- Fixed GT input-bus inventory access and programmed-circuit interaction.
- Fixed KubeJS recipe replacement failures caused by immutable recipe capability data.
- Fixed CosmicCore-owned dedicated-server startup and wireless-command client-class leakage.
- Updated the embedded GregTech Modern build and removed local patches now carried upstream.
