# Cosmic Core 0.16.1
# DO NOT MANUALLY UPDATE COSMIC CORE FOR FRONTIERS **IT WILL NOT WORK**

## New

- Added dedicated LV, MV, and HV Bloomwyrm Power Roots. Each supplies four amps to the Bloomwyrm Heart without allowing its voltage tier to be bypassed.
- Added standard EV+ quad and 16-amp energy hatch support to the Bloomwyrm Heart.
- Restored tiered Chemical Dehydrators and Laminators as CosmicCore-owned processing machines.
- Added HV Cladding, Modular Frameworks, and Cogwork Magicapacitors for the next component tier.

## Improvements

- The Bloomwyrm Heart now displays its available EU/t and physical input voltage separately.
- Existing LV, MV, and HV Bloomwyrm Hearts may continue using ordinary energy hatches while Power Root acquisition is introduced.
- Composite Ore Sorter recipes now consume four refinement-stage inputs per output batch, reducing excessive mineral-chunk multiplication while preserving the higher-tier yield curve.
- Ore chunks can be milled into dust slowly in a Macerator at 2 EU/t or twice as quickly in a Powderizer at 8 EU/t.
- Pyroltic sorting now exposes Sphalerite at tier two and Emberite at tier three, while Monazite Salts remains focused on its two rare-earth minerals.

## Fixes

- Wireless chargers now charge NeoForge Energy items, including EnderIO tools, while preserving native GregTech electric-item handling.
- Updated the embedded GregTech Modern build so active tiered machines can draw enough power to run while recovering a low internal energy buffer.
- Fixed automatically generated Distillery recipes producing their input fluid instead of the selected Distillation Tower output.
- Restored safe EMI previews for huge multiblocks when Aeronautics is installed, with a counts-only fallback if the fake-world bypass is unavailable.
