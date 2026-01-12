package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.ghostipedia.cosmiccore.client.gui.widget.stellar.StellarFancyUIWidget;
import com.ghostipedia.cosmiccore.client.gui.widget.stellar.StellarIrisWidget;
import com.ghostipedia.cosmiccore.common.data.CosmicSounds;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.sound.AutoReleasedSound;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.UpdateListener;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine.Stage.BLACK_HOLE;
import static com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine.Stage.DEATH;

@Getter
public class IrisMultiblockMachine extends WorkableElectricMultiblockMachine {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            IrisMultiblockMachine.class, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Getter
    @Persisted
    private final NotifiableItemStackHandler inventory;

    @Getter
    protected boolean ignite;
    @Getter
    protected boolean isFuelable;
    protected Object workingSound;

    @Setter
    @Persisted
    @DescSynced
    @UpdateListener(methodName = "onStatusSynced")
    private Stage stage = Stage.EMPTY;

    public enum Stage {
        EMPTY,
        GROWING,
        STAR,
        SUPERSTAR,
        BLACK_HOLE,
        DEATH,
        DEATH_GRACEFUL;
    }

    public IrisMultiblockMachine(IMachineBlockEntity holder) {
        super(holder);
        this.inventory = new NotifiableItemStackHandler(this, 1, IO.NONE, IO.BOTH);
    }

    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("unused")
    protected void onStatusSynced(RecipeLogic.Status newValue, RecipeLogic.Status oldValue) {
        this.scheduleRenderUpdate();
        soundTick();
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
    }

    public void setStarStage() {
        Stage[] values = Stage.values();
        int nextVal = (getStage().ordinal() + 1) % values.length;
        setStage(values[nextVal]);
    }

    @Override
    public void clientTick() {
        super.clientTick();
        this.soundTick();
    }

    @OnlyIn(Dist.CLIENT)
    public void soundTick() {
        if (isFormed) {
            var sound = CosmicSounds.CHEMVAT;
            if (stage == DEATH) {
                sound = CosmicSounds.STELLAR_BODY_DYING;
            }
            if (stage == BLACK_HOLE) {
                sound = CosmicSounds.BLACK_HOLE_CRY;
            }

            if (workingSound instanceof AutoReleasedSound soundEntry) {
                if (soundEntry.soundEntry == sound && !soundEntry.isStopped()) {
                    return;
                }
                soundEntry.release();
                workingSound = null;
            }
            if (sound != null) {
                workingSound = sound.playAutoReleasedSound(
                        () -> this.shouldWorkingPlaySound() && !this.isInValid() &&
                                this.getLevel().isLoaded(this.getPos()) &&
                                MetaMachine.getMachine(this.getLevel(), this.getPos()) == this,
                        RelativeDirection.offsetPos(this.getPos(), getFrontFacing(), getUpwardsFacing(), isFlipped, 0,
                                0, -47),
                        true, 0, 1, 1);
            }

        } else if (workingSound instanceof AutoReleasedSound soundEntry) {
            soundEntry.release();
            workingSound = null;
        }
    }

    @Override
    public Widget createUIWidget() {
        return new StellarIrisWidget(() -> this);
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        if (isFormed()) {
            textList.add(Component.translatable(stage.toString()));
            textList.add(Component.translatable("cosmiccore.multiblock.iris.star_stage_sustain"));
        }
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(176, 166, this, entityPlayer)
                .widget(new StellarFancyUIWidget(this, 176, 166, this::getStage));
    }
}
