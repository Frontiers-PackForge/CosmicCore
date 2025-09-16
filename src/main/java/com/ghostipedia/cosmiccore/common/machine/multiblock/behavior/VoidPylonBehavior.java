package com.ghostipedia.cosmiccore.common.machine.multiblock.behavior;

import com.ghostipedia.cosmiccore.common.abyss.AbyssBudgetCap;
import com.ghostipedia.cosmiccore.common.abyss.AbyssRules;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;

import com.gregtechceu.gtceu.utils.GTMath;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.NotNull;

import static com.ghostipedia.cosmiccore.common.abyss.AbyssLogic.hideHUD;
import static com.ghostipedia.cosmiccore.common.abyss.AbyssLogic.sendHUD;

public class VoidPylonBehavior extends WorkableElectricMultiblockMachine {

    @NotNull
    private AABB killzone = new AABB(BlockPos.ZERO);
    private TickableSubscription hurtSub;
    private int purify = 1;

    public VoidPylonBehavior(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    private void updateBounds(int multiplier) {
        var flt = RelativeDirection.offsetPos(getPos(), getFrontFacing(), getUpwardsFacing(), isFlipped(), 3, 14, -14);
        var brb = RelativeDirection.offsetPos(getPos(), getFrontFacing(), getUpwardsFacing(), isFlipped(), -14, -14,
                14);
        killzone = new AABB(flt, brb);
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
        for (Entity entity : getLevel().getEntities(null, killzone)) {
            if (entity instanceof Player player) {

                player.getCapability(AbyssBudgetCap.CAP).ifPresent(cap -> {

                    var level = getLevel();


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

}
