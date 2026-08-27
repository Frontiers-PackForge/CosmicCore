package com.ghostipedia.cosmiccore.client.compat.create;

import com.ghostipedia.cosmiccore.common.compat.create.FluidGaugeSetItemMenuExtension;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.FactoryGaugeFluidSelectionPacket;

import net.liukrast.deployer.lib.helper.client.FluidGhostSlot;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.fluids.FluidStack;

public final class FluidGaugeGhostSlot extends FluidGhostSlot {

    private final FluidGaugeSetItemMenuExtension menuExtension;

    public FluidGaugeGhostSlot(
                               int x,
                               int y,
                               AbstractContainerMenu menu,
                               FluidStack fluid,
                               FluidGaugeSetItemMenuExtension menuExtension) {
        super(x, y, menu, fluid);
        this.menuExtension = menuExtension;
    }

    @Override
    public void setGhostStack(FluidStack ghostStack) {
        super.setGhostStack(ghostStack);
        cosmiccore$syncFluid();
    }

    private void cosmiccore$syncFluid() {
        FluidStack fluid = getGhostStack();
        menuExtension.cosmiccore$setFluid(fluid);
        CCoreNetwork.sendToServer(new FactoryGaugeFluidSelectionPacket(menu.containerId, fluid));
    }
}
