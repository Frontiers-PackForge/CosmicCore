# Cargo Moths System

## Current Status: **IN PROGRESS**

Last updated: Debug output removed, core functionality working.

---

## 1. Overview

The Cargo Moths system provides local (same-dimension) item and fluid transport using a whimsical moth-based logistics network. Unlike the cross-dimensional linking system, Cargo Moths are designed for short-range automation within a single dimension.

### Design Philosophy
- **No power required** - Moths work for free!
- **Scalable capacity** - Add more moth homes to increase throughput
- **Tiered progression** - Different beehive types provide different speeds/capacities
- **Feeding bonuses** - Optional honey/oil feeding for multipliers

---

## 2. What's Implemented

### 2.1 Core Components

| Component | File | Status |
|-----------|------|--------|
| `MothCargoStation` | `common/machine/multiblock/multi/MothCargoStation.java` | ✅ Complete |
| `MothCargoStationMachine` | `common/machine/multiblock/multi/logic/MothCargoStationMachine.java` | ✅ Complete |
| `MothCargoDropOff` | `common/machine/multiblock/multi/MothCargoDropOff.java` | ✅ Complete |
| `MothCargoDropOffMachine` | `common/machine/multiblock/multi/logic/MothCargoDropOffMachine.java` | ✅ Complete |
| `LinkedWorkableMultiblockMachine` | `api/machine/multiblock/LinkedWorkableMultiblockMachine.java` | ✅ Complete |

### 2.2 Feature Checklist

- [x] Multiblock structure definitions
- [x] Datastick-based linking (reuses cross-dimensional linking infrastructure)
- [x] Same-dimension restriction
- [x] Item transfer from Station to Drop Off
- [x] Fluid transfer from Station to Drop Off
- [x] Tiered moth homes (Forestry beehives)
- [x] Cycle time based on moth tier
- [x] Capacity based on moth count × tier multiplier
- [x] Distribution modes (DIRECT, FILL_FIRST, ROUND_ROBIN)
- [x] GUI display (moth homes, cycle time, capacity, linked drop-offs)
- [x] Screwdriver to cycle distribution mode
- [ ] Feeding bonuses (honey/oil multipliers)

---

## 3. Multiblock Structures

### 3.1 Moth Cargo Station (Sender)

Tower structure: 3×3 footprint, 6 blocks tall

```
Layer 0 (bottom):  Layer 1-4:        Layer 5 (top):
C C C              C M C              C C C
C C C              C M C              C C C
C Q C              C M C              C C C
```

Where:
- `C` = Steel Solid Casing (or input/output buses/hatches, maintenance hatch)
- `M` = Moth Home (Forestry beehive) OR Steel Solid Casing
- `Q` = Controller

**Moth homes only go in the center column** (up to 4 can be placed).

Allowed hatches:
- 1 Maintenance Hatch (required)
- Up to 4 Item Input Buses
- Up to 4 Item Output Buses
- Up to 4 Fluid Input Hatches
- Up to 4 Fluid Output Hatches

### 3.2 Moth Cargo Drop Off (Receiver)

Simple 3×3×2 structure:

```
Layer 0 (bottom):  Layer 1 (top):
C C C              C C C
C Q C              C C C
C C C              C C C
```

Where:
- `C` = Steel Solid Casing (or output buses/hatches, maintenance hatch)
- `Q` = Controller

Allowed hatches:
- 1 Maintenance Hatch (required)
- Up to 4 Item Output Buses
- Up to 4 Fluid Output Hatches

---

## 4. Moth Home Tiers

Moth homes use Forestry beehive blocks:

| Tier | Block | Cycle Time | Moths per Home |
|------|-------|------------|----------------|
| T1 | `forestry:beehive_forest` | 60s | 1 |
| T2 | `forestry:beehive_lush` | 30s | 2 |
| T3 | `forestry:beehive_desert` | 15s | 4 |
| T4 | `forestry:beehive_end` | 5s | 8 |

**All moth homes must be the same tier.** Mixed tiers will trigger a warning.

### Capacity Calculation

```
Items per cycle = Total Moths × 64 × Feeding Multiplier
Fluids per cycle = Total Moths × 1000mB × Feeding Multiplier
Total Moths = Moth Home Count × Moths per Home (by tier)
```

Example: 4× T3 beehives = 4 × 4 = 16 moths = 1024 items per cycle (every 15s)

---

## 5. Distribution Modes

Cycle through modes with screwdriver on the controller.

| Mode | Behavior |
|------|----------|
| `DIRECT` | Ships to first linked drop-off only (1:1) |
| `FILL_FIRST` | Fills each drop-off in order until full, then moves to next |
| `ROUND_ROBIN` | Distributes evenly across all linked drop-offs |

---

## 6. Feeding Bonuses (TODO)

Planned multipliers for feeding moths:

| Feed Item | Multiplier |
|-----------|------------|
| Regular Honey | 2× |
| Lofty Honey | 4× |
| Pale Oil | 8× |

Feed is consumed per cycle from the Station's input bus.

---

## 7. GUI Information

The Station GUI displays:
- Moth Homes count and tier
- Total Moths
- Cycle Time (seconds)
- Distribution Mode
- Linked Drop-Offs count
- Capacity per cycle (items and fluids)

The Drop Off GUI displays:
- Structure status
- Number of stations linked to it

---

## 8. Linking

Uses the same datastick-based linking as the cross-dimensional system:

1. Shift+right-click the Moth Cargo Station with a datastick to copy link data
2. Right-click a Moth Cargo Drop Off to establish the link

**Restrictions:**
- Same dimension only (moths can't fly between dimensions!)
- Station can link to up to 16 Drop Offs (1:N)
- Drop Off can receive from up to 16 Stations (N:1)

---

## 9. How It Works

1. Every tick, the Station checks if enough time has passed since the last cycle
2. When cycle time is reached:
   - Get all linked, formed Drop Offs
   - Calculate item/fluid capacity based on moths and feeding multiplier
   - Extract items from Station's input buses (using internal methods to bypass IO checks)
   - Insert items into Drop Off's output buses (using internal methods to bypass IO checks)
   - Same process for fluids
   - Consume feeding materials (TODO)

The internal extraction/insertion methods bypass GTCEu's IO direction checks, which is the same pattern used by DroneStationMachine.

---

## 10. File Structure

```
src/main/java/com/ghostipedia/cosmiccore/
├── api/machine/multiblock/
│   └── LinkedWorkableMultiblockMachine.java    # Base class (no power requirement)
└── common/machine/multiblock/multi/
    ├── MothCargoStation.java                   # Station multiblock definition
    ├── MothCargoDropOff.java                   # Drop Off multiblock definition
    └── logic/
        ├── MothCargoStationMachine.java        # Station logic (shipping cycles)
        └── MothCargoDropOffMachine.java        # Drop Off logic (receives items)
```

---

## 11. Known Limitations

1. **Forestry dependency** - Falls back to vanilla beehive if Forestry not loaded
2. **No visual feedback** - No moth entity/particle flying between stations
3. **No feeding implementation** - Multiplier is always 1× currently
4. **No chunk loading** - Both Station and Drop Off must be loaded

---

## 12. Future Work

- [ ] Implement feeding bonuses (honey/oil consumption and multipliers)
- [ ] Add moth particle effects during transfers
- [ ] Consider cross-dimension variant (interdimensional moths?)
- [ ] Add JEI/EMI integration showing capacity calculations
- [ ] Custom textures/models for the multiblocks
