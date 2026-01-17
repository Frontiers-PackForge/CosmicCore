# EMI Favorites with Custom Amounts - Progress Summary

## Goal
Add the ability to pin EMI stacks with custom amounts using CTRL+A, and adjust amounts with CTRL+scroll.

## What Was Implemented

### 1. CosmicFavorite Class
**File:** `src/main/java/com/ghostipedia/cosmiccore/integration/emi/CosmicFavorite.java`

Extended `EmiFavorite` to store custom amounts and render them with a scaled-down overlay:
- Stores `amount` field
- Custom `render()` method that draws the stack without its default amount, then draws a half-scale (0.5f) amount overlay
- `formatCompact()` for K/M/B suffixes (items) and B/KB/MB/BB (fluids in buckets)
- `adjustAmount()` for CTRL+scroll adjustment

### 2. EmiScreenManagerMixin
**File:** `src/main/java/com/ghostipedia/cosmiccore/mixin/emi/EmiScreenManagerMixin.java`

Injects into EMI's screen manager:
- **CTRL+A pinning:** Detects favorite keybind + CTRL, gets hovered stack (from sidebar, RecipeScreen, or stack providers), creates CosmicFavorite with amount
- **CTRL+scroll:** Adjusts amount on hovered CosmicFavorite (shift for big steps: 64 for items, 1000 for fluids)
- **Recalculate guard:** Prevents NPE when `currentBase` or its screen is null

### 3. EmiExclusionAreasMixin
**File:** `src/main/java/com/ghostipedia/cosmiccore/mixin/emi/EmiExclusionAreasMixin.java`

Guards against NPE in `getExclusion()` when screen is null during reload.

### 4. RecipeScreenMixin
**File:** `src/main/java/com/ghostipedia/cosmiccore/mixin/emi/RecipeScreenMixin.java`

Adds public `getHoveredStack()` method to RecipeScreen for detecting what the user is hovering over.

## Current Status

### Working
- CTRL+A key detection is working (logs show keyCode=65, ctrl=true, isFavKey=true)
- EmiExclusionAreas guard is in place
- EmiScreenManager.recalculate guard is in place (fixed to use parameterless method signature)

### Not Yet Tested
- Actual pinning from recipe screens (was crashing before guards were fixed)
- Scaled-down amount rendering
- CTRL+scroll amount adjustment

## Recent Crashes Fixed

1. **NPE in EmiExclusionAreas.getExclusion** - Fixed with EmiExclusionAreasMixin guard
2. **NPE in EmiScreenManager.recalculate** - Fixed with guard, but had version mismatch issue
3. **Invalid descriptor on guardRecalculate** - Fixed by removing EmiScreenBase parameter (dev EMI has parameterless `recalculate()`)

## Files Modified

- `src/main/java/com/ghostipedia/cosmiccore/integration/emi/CosmicFavorite.java`
- `src/main/java/com/ghostipedia/cosmiccore/mixin/emi/EmiScreenManagerMixin.java`
- `src/main/java/com/ghostipedia/cosmiccore/mixin/emi/EmiExclusionAreasMixin.java`
- `src/main/java/com/ghostipedia/cosmiccore/mixin/emi/RecipeScreenMixin.java`
- `src/main/resources/cosmiccore.mixins.json` (client mixins list)

## Next Steps

1. Test that the game launches without crashes
2. Test CTRL+A pinning from recipe screens
3. Test that amounts display with scaled-down rendering
4. Test CTRL+scroll amount adjustment
5. Remove debug logging once everything works

## Key EMI Classes Referenced

- `dev.emi.emi.screen.EmiScreenManager` - Main screen management
- `dev.emi.emi.screen.EmiScreenBase` - Wrapper around Minecraft Screen
- `dev.emi.emi.screen.RecipeScreen` - Recipe viewing screen
- `dev.emi.emi.runtime.EmiFavorites` - Favorites management
- `dev.emi.emi.registry.EmiExclusionAreas` - Exclusion zone calculations
- `dev.emi.emi.registry.EmiStackProviders` - Stack detection from screens
- `dev.emi.emi.api.stack.EmiStackInteraction` - Hovered stack info
- `dev.emi.emi.config.EmiConfig.favorite` - Favorite keybind config
