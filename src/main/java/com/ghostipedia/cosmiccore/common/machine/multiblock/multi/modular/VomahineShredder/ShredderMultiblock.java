package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.modular.VomahineShredder;

import com.ghostipedia.cosmiccore.mixin.accessor.WorkableElectricMultiblockMachineMixin;
import com.ghostipedia.cosmiccore.mixin.accessor.WorkableMultiblockMachineMixin;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.registry.registrate.MultiblockMachineBuilder;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiFunction;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;

public class ShredderMultiblock extends WorkableElectricMultiblockMachine {

    private final Map<BlockPos, ShredderModule> modules = new Object2ReferenceOpenHashMap<>();

    private AABB bounds;

    public ShredderMultiblock(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
        updateBounds();
    }

    @Override
    public void setFrontFacing(Direction facing) {
        super.setFrontFacing(facing);
        updateBounds();
    }

    @Override
    public void setUpwardsFacing(@NotNull Direction upwardsFacing) {
        super.setUpwardsFacing(upwardsFacing);
        updateBounds();
    }

    public void updateBounds() {
        var right = getFrontFacing().getCounterClockWise();
        var scale = 4;
        var r = getPos().offset(getFrontFacing().getOpposite().getNormal()) // right end
                .offset(right.getNormal().getX() * scale,
                right.getNormal().getY() * scale, right.getNormal().getZ() * scale);
        var l = getPos().offset(getFrontFacing().getOpposite().getNormal()) // left end
                .offset(right.getNormal().getX(), right.getNormal().getY(), right.getNormal().getZ());
        bounds = new AABB(l, r);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();

        var bes = getBlockEntitiesInAABB(bounds, getLevel());
        for (var br : bes) {
            if(br instanceof MetaMachineBlockEntity blockEntity && blockEntity.metaMachine instanceof ShredderModule module) {
                if(module.isFormed()) {
                    module.setShredderMultiblock(this);
//                    ((WorkableElectricMultiblockMachineMixin)module).cosCore$setOverclockTier(this.getOverclockTier());
//                    ((WorkableElectricMultiblockMachineMixin)module).cosCore$setEnergyContainer(this.getEnergyContainer());

//                    RecipeHandlerList itemHandlerInput = RecipeHandlerList.of(IO.IN,this.getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP));
//                    RecipeHandlerList itemHandlerOutput = RecipeHandlerList.of(IO.IN,this.getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP));
//                    RecipeHandlerList fluidHandler = RecipeHandlerList.of(IO.IN,this.getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP));
//
//                    module.addHandlerList(itemHandlerInput);
//                    ((WorkableMultiblockMachineMixin)module).cosCore$getTraitSubs().add(itemHandlerInput.subscribe(module.getRecipeLogic()::updateTickSubscription));
//                    module.addHandlerList(itemHandlerOutput);
//                    ((WorkableMultiblockMachineMixin)module).cosCore$getTraitSubs().add(itemHandlerOutput.subscribe(module.getRecipeLogic()::updateTickSubscription));
//                    module.addHandlerList(fluidHandler);
//                    ((WorkableMultiblockMachineMixin)module).cosCore$getTraitSubs().add(fluidHandler.subscribe(module.getRecipeLogic()::updateTickSubscription));
//                    module.recipeLogic.updateTickSubscription();
//
                    modules.put(module.getPos(), module);
                    GTCEu.LOGGER.info("ShredderModule added: {}, {}, {}, {}, size: {}", module.getPos().getX(),
                            module.getPos().getY(), module.getPos().getZ(), module.getValue(), modules.size());
                }
            }
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        modules.clear();
    }

    public static ModifierFunction recipeModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if(!(machine instanceof ShredderMultiblock shredder)) return RecipeModifier.nullWrongType(ShredderMultiblock.class, machine);

        for (var module : shredder.modules.values()) {
            // do shit here for recipe modification
        }

        return ModifierFunction.NULL;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateBounds();
        subscribeServerTick(this::myTick);
    }

    public void myTick() {
        for (var module : modules.values()) {
            // do shit but on server tick :okay:
        }
    }

    public List<BlockEntity> getBlockEntitiesInAABB(AABB aabb, Level level) {
        List<BlockEntity> bes = new ArrayList<>();
        for (int x = (int)aabb.minX; x <= (int)aabb.maxX; x++) {
            for (int y = (int)aabb.minY; y <= (int)aabb.maxY; y++) {
                for (int z = (int)aabb.minZ; z <= (int)aabb.maxZ; z++) {
                    var be = level.getBlockEntity(new BlockPos(x, y, z));
                    if (be != null) bes.add(be);
                }
            }
        }
        return bes;
    }


    //
    // // Shredder Modules
    //
    // public static final MultiblockMachineDefinition[] SHREDDER_MODULE = registerTieredMultis("shredder_module",
    // WorkableElectricMultiblockMachine::new, (tier, builder) -> builder
    // .rotationState(RotationState.ALL)
    // .langValue("Shredder Module MK %s".formatted(toRomanNumeral(tier - 5)))
    // .recipeType(GTRecipeTypes.MACERATOR_RECIPES)
    // .recipeModifiers(GTRecipeModifiers.DEFAULT_ENVIRONMENT_REQUIREMENT,
    // FusionReactorMachine::recipeModifier)
    // .appearanceBlock(() -> FusionReactorMachine.getCasingState(tier))
    // .pattern((definition) -> {
    // var casing = blocks(FusionReactorMachine.getCasingState(tier));
    // return FactoryBlockPattern.start()
    // .aisle("AAAAAAAAAAAAA", "ADDDDDDDDDDDA", "ADDDDDDDDDDDA", "AAAAAAADDDDDA",
    // " AAAAAAA")
    // .aisle("ACCCCCCCCCCCA", "ACCCCCCCCCCCB", "ACCCCCCCCCCCB", "ACCCCCCCCCCCB",
    // " CCCCCCA")
    // .aisle("ACCCCCCCCCCCA", "ACDDDDCEEEEC ", "ACDDDDCEEEEC ", "ACCCCCCEEEEC ",
    // " C CA")
    // .aisle("ACCCCCCCCCCCA", "ACCCCCCEEEEC ", "ACCCCCCEEEEC ", "ACCCCCCEEEEC ",
    // " C CA")
    // .aisle("AAAAAACCCCCCA", "AAAAAACEEEEC ", "AAAAAACEEEEC ", "ABBBBBCEEEEC ",
    // " C CA")
    // .aisle("AAAAAACCCCCCA", "AFFFFACCCCCCB", "AAAAAACCCCCCB", "A BCCCCCCB",
    // " CCCCCCA")
    // .aisle("AAAAAAAAAAAAA", "G AB BA", "A AB BA", "A AB BA",
    // " AAAAAAA")
    // .where(' ', any())
    // .where("F", controller(blocks(definition.getBlock()))
    // .or(blocks(CASING_ATOMIC.get()).setMaxGlobalLimited(4)))
    // .where('A', blocks(CYCLOZINE_CHEMICALLY_REPELLING_CASING.get()))
    // .where('B', blocks(MULTIPURPOSE_INTERSTELLAR_GRADE_CASING.get()))
    // .where('C', blocks(MULTIPURPOSE_INTERSTELLAR_GRADE_CASING.get()))
    // .where('D', blocks(CASING_ATOMIC.get()))
    // .where('E', blocks(ULTRA_POWERED_CASING.get())
    // .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(16))
    // .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(16))
    // .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2))
    // .or(Predicates.abilities(PartAbility.INPUT_LASER).setMaxGlobalLimited(1))
    // .or(Predicates.abilities(PartAbility.OUTPUT_LASER).setMaxGlobalLimited(1))
    // .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(16))
    // .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(16)))
    // .where('G', blocks(definition.getBlock()))
    // .build();
    // })
    // .workableCasingRenderer(
    // CosmicCore.id("block/casings/solid/vomahine_certified_chemically_resistant_casing"),
    // GTCEu.id("block/multiblock/fusion_reactor"))
    // .hasTESR(true)
    // .register(),
    // ZPM, UV, UHV);

    public static MultiblockMachineDefinition[] registerTieredMultis(String name,
                                                                     BiFunction<IMachineBlockEntity, Integer, MultiblockControllerMachine> factory,
                                                                     BiFunction<Integer, MultiblockMachineBuilder, MultiblockMachineDefinition> builder,
                                                                     int... tiers) {
        MultiblockMachineDefinition[] definitions = new MultiblockMachineDefinition[GTValues.TIER_COUNT];
        for (int tier : tiers) {
            var register = REGISTRATE
                    .multiblock(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name,
                            holder -> factory.apply(holder, tier))
                    .tier(tier);
            definitions[tier] = builder.apply(tier, register);
        }
        return definitions;
    }

    public static void init() {}
}
