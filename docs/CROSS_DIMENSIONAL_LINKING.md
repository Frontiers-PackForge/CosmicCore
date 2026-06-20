# Cross-Dimensional Multiblock Linking System

## Current Status: **IMPLEMENTED & TESTED**

Last updated: Session implementing partner query utilities and recipe conditions.

---

## 1. Overview

This system enables multiblock machines to communicate across dimensions using GTCEu's datastick as the linking mechanism. Links are persisted in SavedData and support role-based access control.

### Use Cases
- **Star Ladder**: Manufacturing chains spanning multiple dimensions
- **Cross-dimensional recipes**: "Recipe requires partner in Sun Orbit with Solar Plasma"
- **Remote resource access**: Query partner's inventory/fluids/energy
- **Dimension-gated progression**: Certain recipes only available when linked to specific dimensions

---

## 2. What's Implemented

### 2.1 Core Infrastructure

| Component | File | Status |
|-----------|------|--------|
| `ILinkedMultiblock` | `api/capability/ILinkedMultiblock.java` | ✅ Complete |
| `LinkEntry` | `api/data/savedData/LinkEntry.java` | ✅ Complete |
| `LinkedMultiblockSavedData` | `api/data/savedData/LinkedMultiblockSavedData.java` | ✅ Complete |
| `LinkedMultiblockHelper` | `common/machine/multiblock/LinkedMultiblockHelper.java` | ✅ Complete |
| `LinkedWorkableElectricMultiblockMachine` | `api/machine/multiblock/LinkedWorkableElectricMultiblockMachine.java` | ✅ Complete |

### 2.2 Test Multiblock

| Component | File | Status |
|-----------|------|--------|
| `LinkTestStation` | `common/machine/multiblock/multi/LinkTestStation.java` | ✅ Complete |
| `LinkTestStationMachine` | `common/machine/multiblock/multi/logic/LinkTestStationMachine.java` | ✅ Complete |

### 2.3 Recipe Conditions

| Condition | File | Description |
|-----------|------|-------------|
| `LinkedPartnerCondition` | `common/recipe/condition/LinkedPartnerCondition.java` | Requires N linked partners, optionally formed/working |
| `LinkedPartnerDimensionCondition` | `common/recipe/condition/LinkedPartnerDimensionCondition.java` | Requires partner in specific dimension |
| `LinkedPartnerDimensionItemCondition` | `common/recipe/condition/LinkedPartnerDimensionItemCondition.java` | Requires partner in dimension with specific item |
| `LinkedPartnerDimensionFluidCondition` | `common/recipe/condition/LinkedPartnerDimensionFluidCondition.java` | Requires partner in dimension with specific fluid |

### 2.4 Partner Query Utilities

Methods in `LinkedMultiblockHelper`:
- `queryPartner()` - Generic query with chunk loading
- `getPartnerItemHandlers()` / `getPartnerFluidHandlers()` / `getPartnerEnergyHandlers()`
- `partnerHasItem()` / `partnerHasFluid()`
- `getPartnerEnergyStored()`
- `isPartnerFormed()` / `isPartnerWorking()`

Convenience methods in `LinkedWorkableElectricMultiblockMachine`:
- `partnerHasItem()` / `partnerHasFluid()` / `getPartnerEnergyStored()`
- `isPartnerFormed()` / `isPartnerWorking()`
- `anyPartnerHasItem()` / `anyPartnerHasFluid()` / `anyPartnerWorking()`
- `countFormedPartners()`

---

## 3. How to Use

### 3.1 Linking Machines

1. **Copy link data**: Shift+right-click a linkable multiblock with a datastick
2. **Paste/establish link**: Right-click another linkable multiblock with the datastick
3. The system validates ownership, roles, and compatibility before establishing the link

### 3.2 Creating a Linkable Multiblock

Extend `LinkedWorkableElectricMultiblockMachine`:

```java
public class MyLinkedMachine extends LinkedWorkableElectricMultiblockMachine {

    public MyLinkedMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public LinkRole getLinkRole() {
        return LinkRole.PEER; // or CONTROLLER, REMOTE
    }

    @Override
    public int getMaxPartners() {
        return 4; // default
    }

    @Override
    public boolean canLinkTo(GlobalPos partner, ILinkedMultiblock partnerMachine) {
        // Custom validation (e.g., only link to specific machine types)
        return true;
    }

    @Override
    public void onLinkEstablished(GlobalPos partner) {
        super.onLinkEstablished(partner);
        // React to new link
    }

    @Override
    public void onLinkBroken(GlobalPos partner) {
        super.onLinkBroken(partner);
        // Cleanup when link breaks
    }
}
```

### 3.3 Using Recipe Conditions

```java
// Requires at least 1 linked partner
.addCondition(new LinkedPartnerCondition(1))

// Requires 2 partners, at least 1 formed
.addCondition(new LinkedPartnerCondition(2, true, false))

// Requires partner in specific dimension
.addCondition(new LinkedPartnerDimensionCondition("frontiers:sun_orbit"))

// Requires partner in dimension with specific item
.addCondition(new LinkedPartnerDimensionItemCondition("frontiers:sun_orbit", Items.BUCKET, 1))

// Requires partner in dimension with specific fluid (1000mB)
.addCondition(new LinkedPartnerDimensionFluidCondition("frontiers:sun_orbit", SolarPlasma.getFluid(), 1000))
```

### 3.4 Querying Partner Resources

From within a `LinkedWorkableElectricMultiblockMachine`:

```java
// Check if any partner has a specific item
if (anyPartnerHasItem(stack -> stack.is(Items.DIAMOND))) {
    // ...
}

// Check specific partner
for (GlobalPos partner : getLinkedPartners()) {
    if (partnerHasFluid(partner, fluid -> fluid.getFluid().is(Fluids.LAVA))) {
        // Partner has lava
    }

    long energy = getPartnerEnergyStored(partner);
    boolean working = isPartnerWorking(partner);
}

// Custom queries
String partnerName = queryPartner(partner, machine ->
    machine.getDefinition().getName());
```

---

## 4. Link Roles

### Role Types

| Role | Can Query Partners | Can Be Queried |
|------|-------------------|----------------|
| `PEER` | ✅ | ✅ |
| `CONTROLLER` | ✅ | ❌ |
| `REMOTE` | ❌ | ✅ |

### Role Negotiation

When two machines link, their declared roles are negotiated:

| A's Role | B's Role | Result | A's Effective | B's Effective |
|----------|----------|--------|---------------|---------------|
| PEER | PEER | ✅ Valid | PEER | PEER |
| PEER | CONTROLLER | ✅ Valid | REMOTE | CONTROLLER |
| PEER | REMOTE | ✅ Valid | CONTROLLER | REMOTE |
| CONTROLLER | REMOTE | ✅ Valid | CONTROLLER | REMOTE |
| CONTROLLER | CONTROLLER | ❌ Reject | - | - |
| REMOTE | REMOTE | ❌ Reject | - | - |

---

## 5. Test Recipes

The Link Test Station includes these recipes for testing:

| Recipe | Input | Output | Condition |
|--------|-------|--------|-----------|
| `link_test_basic` | 1x Iron Ingot | 9x Iron Nugget | None |
| `link_test_linked` | 1x Gold Ingot | 1x Diamond | 1 linked partner |
| `link_test_formed_partner` | 1x Emerald | 1x Nether Star | 1 formed partner |
| `link_test_moon_partner` | 4x Lapis | 1x Ender Pearl | Partner in `ad_astra:moon` |
| `link_test_overworld_partner` | 4x Redstone | 4x Glowstone | Partner in `minecraft:overworld` |
| `link_test_dimension_item` | 8x Coal | 1x Diamond | Partner in Overworld with Diamond |
| `link_test_dimension_fluid` | 1x Sponge | 1x Wet Sponge | Partner in Overworld with Water |

---

## 6. Translation Keys

```properties
# Link operations
cosmiccore.datastick.link_copied=Link: %s
cosmiccore.link.copied=Link data copied from %s
cosmiccore.link.established=Link established: %s ↔ %s

# Errors
cosmiccore.link.not_ready=Machine not ready for linking
cosmiccore.link.invalid_data=Invalid link data on datastick
cosmiccore.link.cannot_self_link=Cannot link a machine to itself
cosmiccore.link.partner_not_loaded=Partner machine must be loaded to establish link
cosmiccore.link.partner_missing=Partner machine no longer exists
cosmiccore.link.not_linkable=Target machine does not support linking
cosmiccore.link.different_owner=Cannot link machines owned by different teams
cosmiccore.link.incompatible_roles=Incompatible link roles: %s cannot link to %s
cosmiccore.link.limit_reached_self=This machine has reached its link limit
cosmiccore.link.limit_reached_partner=Partner machine has reached its link limit
cosmiccore.link.incompatible_self=This machine cannot link to that type
cosmiccore.link.incompatible_partner=Partner machine cannot link to this type
cosmiccore.link.already_linked=These machines are already linked

# Recipe conditions
cosmiccore.recipe.condition.linked_partner.tooltip=Requires %s linked partner(s)
cosmiccore.recipe.condition.linked_partner.formed=Requires %s linked partner(s) with valid structure
cosmiccore.recipe.condition.linked_partner.working=Requires %s linked partner(s) actively working
cosmiccore.recipe.condition.linked_partner_dimension.tooltip=Requires linked partner in %s
cosmiccore.recipe.condition.linked_partner_dimension_item.tooltip=Requires %sx %s in partner in %s
cosmiccore.recipe.condition.linked_partner_dimension_fluid.tooltip=Requires %smB %s in partner in %s
```

---

## 7. File Structure

```
src/main/java/com/ghostipedia/cosmiccore/
├── api/
│   ├── capability/
│   │   └── ILinkedMultiblock.java           # Interface for linkable machines
│   ├── data/savedData/
│   │   ├── LinkEntry.java                   # Single link record
│   │   └── LinkedMultiblockSavedData.java   # Persistence layer
│   └── machine/multiblock/
│       └── LinkedWorkableElectricMultiblockMachine.java  # Base class
├── common/
│   ├── machine/multiblock/
│   │   ├── LinkedMultiblockHelper.java      # Utilities, chunk loading, queries
│   │   └── multi/
│   │       ├── LinkTestStation.java         # Test multiblock registration
│   │       └── logic/
│   │           └── LinkTestStationMachine.java  # Test multiblock logic
│   └── recipe/condition/
│       ├── CosmicConditions.java            # Condition registration
│       ├── LinkedPartnerCondition.java
│       ├── LinkedPartnerDimensionCondition.java
│       ├── LinkedPartnerDimensionItemCondition.java
│       └── LinkedPartnerDimensionFluidCondition.java
└── gtbridge/
    ├── CosmicRecipeTypes.java               # LINK_TEST_RECIPES type
    └── CosmicCoreRecipes.java               # Test recipes
```

---

## 8. Security Notes

1. **Ownership is always verified at runtime** - Never trust datastick NBT for ownership
2. **Partner must be loaded for link validation** - Prevents linking to arbitrary positions
3. **Role negotiation prevents privilege escalation** - CONTROLLER+CONTROLLER rejected
4. **Chunk loading is capped** - MAX_FORCED_CHUNKS_PER_MACHINE = 4

---

## 9. Known Limitations

1. **Partner must be loaded to establish link** - No config option for force-load during linking yet
2. **No GUI for link management** - Links can only be viewed via machine display text
3. **No visual feedback** - No particles/beams between linked machines
4. **No admin commands** - No way to inspect/remove links via commands

---

## 10. Future Work

### Phase 2: Recipe Integration
- [ ] Cross-dimensional ingredient consumption (consume from partner's inputs)
- [ ] Cross-dimensional output insertion (insert into partner's outputs)
- [ ] Recipe modifier based on partner state

### Phase 3: Quality of Life
- [ ] Config option for force-load during linking
- [ ] Link management GUI
- [ ] Visual feedback (particles, beams)
- [ ] Admin commands (`/cosmiccore link list/remove/info`)

### Phase 4: Advanced Features
- [ ] Energy/fluid transfer between linked machines
- [ ] Item teleportation through links
- [ ] Wireless redstone/data through links

---

## 11. Testing Checklist

- [x] Basic linking between two machines (same dimension)
- [x] Cross-dimensional linking (Overworld ↔ Moon)
- [x] Link persistence across server restart
- [x] Link broken when machine destroyed
- [x] Role negotiation (PEER+PEER, PEER+CONTROLLER, etc.)
- [x] Partner limit enforcement
- [x] Recipe condition: LinkedPartnerCondition
- [x] Recipe condition: LinkedPartnerDimensionCondition
- [x] Recipe condition: LinkedPartnerDimensionItemCondition
- [x] Recipe condition: LinkedPartnerDimensionFluidCondition
- [x] Partner query utilities (items, fluids, energy, formed, working)
