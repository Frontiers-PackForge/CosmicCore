package com.ghostipedia.cosmiccore.gtbridge.pipenet.blockentities;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.gtbridge.pipenet.LevelPressureNet;
import com.ghostipedia.cosmiccore.gtbridge.pipenet.PressurePipeData;
import com.ghostipedia.cosmiccore.gtbridge.pipenet.PressurePipeType;
import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.client.model.PipeModel;
import com.gregtechceu.gtceu.client.renderer.block.PipeBlockRenderer;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PressurePipeBlock extends PipeBlock<PressurePipeType, PressurePipeData, LevelPressureNet> {

    public final PipeBlockRenderer renderer;
    @Getter
    public final PipeModel pipeModel;

    public PressurePipeBlock(Properties properties, PressurePipeType pressurePipeType) {
        super(properties, pressurePipeType);
        this.pipeModel = new PipeModel(pressurePipeType.getThickness(), () -> CosmicCore.id("block/pipe/pressure_pipe_side"), () -> CosmicCore.id("block/pipe/pressure_pipe_open"), null, null);
        this.renderer = new PipeBlockRenderer(this.pipeModel);
    }

    @Override
    public LevelPressureNet getWorldPipeNet(ServerLevel level) {
        return LevelPressureNet.getOrCreate(level);
    }

    @Override
    public BlockEntityType<? extends PipeBlockEntity<PressurePipeType, PressurePipeData>> getBlockEntityType() {
        return CosmicBlockEntities.PRESSURE_PIPE.get();
    }

    @Override
    public PressurePipeData createRawData(BlockState pState, @Nullable ItemStack pStack) {
        return new PressurePipeData(this.pipeType.getMinPressure(), this.pipeType.getMaxPressure(), this.pipeType.getVolume(), (byte) 0);
    }

    @Override
    public PressurePipeData getFallbackType() {
        return PressurePipeData.EMPTY;
    }

    @Override
    public @Nullable PipeBlockRenderer getRenderer(BlockState state) {
        return renderer;
    }
}
