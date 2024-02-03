package com.ghostipedia.cosmiccore.gtbridge.pipenet;

import com.lowdragmc.lowdraglib.pipelike.Node;
import com.lowdragmc.lowdraglib.pipelike.PipeNetWalker;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

public class PressureNetWalker extends PipeNetWalker<PressurePipeData, PressurePipeNet> {

    private double pressure = -1;

    public static void checkPressure(PressurePipeNet net, BlockPos start, double pressure) {
        PressureNetWalker walker = new PressureNetWalker(net, start, 0);
        walker.pressure = pressure;
        walker.traversePipeNet();
    }

    protected PressureNetWalker(PressurePipeNet world, BlockPos sourcePipe, int walkedBlocks) {
        super(world, sourcePipe, walkedBlocks);
    }

    @NotNull
    @Override
    protected PipeNetWalker<PressurePipeData, PressurePipeNet> createSubWalker(PressurePipeNet pipeNet, BlockPos nextPos, int walkedBlocks) {
        PressureNetWalker walker = new PressureNetWalker(pipeNet, nextPos, walkedBlocks);
        walker.pressure = pressure;
        return walker;
    }

    @Override
    protected boolean checkPipe(Node<PressurePipeData> pipeNode, BlockPos pos) {
        return pipeNode.data.checkPressure(pressure, this.pipeNet, pos);
    }
}