package com.ghostipedia.cosmiccore.common.machine.multiblock.behavior;

import com.ghostipedia.cosmiccore.common.abyss.AbyssBudgetCap;
import com.ghostipedia.cosmiccore.common.abyss.AbyssRules;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.BlockLightEngine;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.NotNull;

import static com.ghostipedia.cosmiccore.common.abyss.AbyssLogic.sendHUD;

public class VoidPylonBehavior extends WorkableElectricMultiblockMachine {

    @NotNull
    private AABB cleanAura = new AABB(BlockPos.ZERO);
    private TickableSubscription hurtSub;
    private boolean createLight = true;

    public VoidPylonBehavior(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    private void updateBounds(int multiplier) {
        var flt = RelativeDirection.offsetPos(getPos(), getFrontFacing(), getUpwardsFacing(), isFlipped(), 3, 14, -14);
        var brb = RelativeDirection.offsetPos(getPos(), getFrontFacing(), getUpwardsFacing(), isFlipped(), -14, -14,
                14);
        cleanAura = new AABB(flt, brb);
    }

    @Override
    public void onPartUnload() {
        super.onPartUnload();
        unsubscribe(hurtSub);
        hurtSub = null;
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        unsubscribe(hurtSub);
        hurtSub = null;
        createLight = false;
        removeLight();
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        hurtSub = subscribeServerTick(this::cleanse);
        updateBounds(1);
    }

    private void cleanse() {
        if (getOffsetTimer() % 35 != 0) return;
        if (!this.isFormed) return;
        if (isRemote() || getLevel() == null) return;
        emitLight();
        for (Entity entity : getLevel().getEntities(null, cleanAura)) {
            if (entity instanceof Player player) {
                // gets the cab
                player.getCapability(AbyssBudgetCap.CAP).ifPresent(cap -> {
                    var level = getLevel();

                    //checks if ur in the abyss and if so set the remaining tick and sync the hud :ta7:
                    if (level.dimension().equals(AbyssRules.DIM)) {
                        cap.setRemainingTicks(AbyssRules.DIM, (long) Mth.clamp( (cap.getRemainingTicks(AbyssRules.DIM) + 40), 0, AbyssRules.MAX_TICKS));
                        if (player instanceof ServerPlayer sPlayer) {
                            sendHUD(sPlayer, cap.getRemainingTicks(AbyssRules.DIM), AbyssRules.MAX_TICKS);
                        }
                    }
                });

            }
        }
    }

    private void emitLight() {
        if (getLevel() == null || isRemote()) return;
        if (!(getLevel() instanceof ServerLevel level)) return;
        if(!createLight) return;

        double cx = (cleanAura.minX + cleanAura.maxX) / 2.0;
        double cz = (cleanAura.minZ + cleanAura.maxZ) / 2.0;
        int maxLight = 15;
        double maxDistance = Math.max(cleanAura.getXsize(), cleanAura.getZsize()) / 2.0;

        for (int x = Mth.floor(cleanAura.minX); x <= Mth.ceil(cleanAura.maxX); x++) {
            for (int z = Mth.floor(cleanAura.minZ); z <= Mth.ceil(cleanAura.maxZ); z++) {
                BlockPos pos = new BlockPos(x, this.getPos().getY(), z);

                double dx = x + 0.5 - cx;
                double dz = z + 0.5 - cz;
                double distance = Math.sqrt(dx * dx + dz * dz);

                if (distance <= maxDistance) {
                    // exponential falloff
                    double factor = Math.exp(-distance / maxDistance);
                    int lightLevel = Mth.clamp(
                            (int) Math.ceil(maxLight * factor),
                            0, maxLight
                    );

                    if (lightLevel > 0) {
                        BlockState state = Blocks.LIGHT.defaultBlockState()
                                .setValue(net.minecraft.world.level.block.LightBlock.LEVEL, lightLevel);

                        BlockState current = level.getBlockState(pos);
                        if (current.isAir() || current.is(Blocks.LIGHT)) {
                            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
                        }
                    }
                }
            }
        }
        createLight = false;
    }

    private void removeLight() {
        if (getLevel() == null || isRemote()) return;
        if (!(getLevel() instanceof ServerLevel level)) return;
        if (createLight) return;

        for (int x = Mth.floor(cleanAura.minX); x <= Mth.ceil(cleanAura.maxX); x++) {
            for (int z = Mth.floor(cleanAura.minZ); z <= Mth.ceil(cleanAura.maxZ); z++) {
                BlockPos pos = new BlockPos(x, this.getPos().getY(), z);

                BlockState current = level.getBlockState(pos);
                if (current.is(Blocks.LIGHT)) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
        }
    }



}
