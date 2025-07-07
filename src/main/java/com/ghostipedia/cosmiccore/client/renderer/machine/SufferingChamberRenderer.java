package com.ghostipedia.cosmiccore.client.renderer.machine;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.ModelUtils;
import com.gregtechceu.gtceu.client.util.RenderBufferHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;

public class SufferingChamberRenderer extends DynamicRender<WorkableElectricMultiblockMachine, SufferingChamberRenderer> {

    public static  final SufferingChamberRenderer INSTANCE = new SufferingChamberRenderer();
    public static final Codec<SufferingChamberRenderer> CODEC = Codec.unit(INSTANCE);
    public static final DynamicRenderType<WorkableElectricMultiblockMachine, SufferingChamberRenderer> TYPE = new DynamicRenderType<>(
            SufferingChamberRenderer.CODEC);

    public static final ResourceLocation pentagram = CosmicCore.id("block/iris/testagram");


    private static TextureAtlasSprite pentagramSprite = null;
    public static boolean  isEventListenerRegistered = false;

    private SufferingChamberRenderer() {

        if(!isEventListenerRegistered) {

            ModelUtils.registerAtlasStitchedEventListener(TextureAtlas.LOCATION_BLOCKS, event -> {
                pentagramSprite = event.getAtlas().getSprite(pentagram);
            });

        }

    }

    @Override
    public DynamicRenderType<WorkableElectricMultiblockMachine, SufferingChamberRenderer> getType() {
        return TYPE;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRenderOffScreen(WorkableElectricMultiblockMachine machine) {
        return true;
    }

    @Override
    public void render(WorkableElectricMultiblockMachine machine, float partialTick, PoseStack poseStack,                       MultiBufferSource buffer, int packedLight, int packedOverlay) {

        float tickvalue = (Minecraft.getInstance().level.getGameTime() + partialTick);
        if (machine.isFormed()){

            renderPentagram(machine, poseStack, buffer, tickvalue);

        }

    }

                       @OnlyIn(Dist.CLIENT)
    public void renderPentagram(MultiblockControllerMachine machine, PoseStack stack, MultiBufferSource source, float totalTick){

        stack.pushPose();

        Direction front = machine.getFrontFacing();
        Direction upwards = machine.getUpwardsFacing();

        boolean flipped = machine.isFlipped();
        Vec3i up = RelativeDirection.UP.getRelative(front, upwards, flipped).getNormal();
        Vec3i back = RelativeDirection.BACK.getRelative(front, upwards, flipped).getNormal();
        Direction.Axis leftAxis = RelativeDirection.LEFT.getRelative(front, upwards, flipped).getAxis();

        float x0 = 0, y0 = 0, z0 = 0;


        // go to center of multi
        for (Direction.Axis axis : Direction.Axis.VALUES) {

            int upOffset = axis.choose(up.getX(), up.getY(), up.getZ());
            int backOffset = axis.choose(back.getX(), back.getY(), back.getZ());


            // yoinked omers magic numbers from Hemophagic blahblahlbah
            float offset = upOffset * (4.0f + (upOffset * 0.5f)) +
                    backOffset * (5.0f + (backOffset * 0.5f));
            switch (axis) {
                case X -> x0 = offset;
                case Y -> y0 = offset;
                case Z -> z0 = offset;
            }
        }
        x0 -= 1.0f;
            stack.translate(
                    x0 + (leftAxis == Direction.Axis.X ? 0.5f : 0.0f),
                    y0 + (leftAxis == Direction.Axis.Y ? 0.5f : 0.0f),
                    z0 + (leftAxis == Direction.Axis.Z ? 0.5f : 0.0f));

            //do the rotaty thingy yee
            Quaternionf rot = new Quaternionf()
                    .rotateY(totalTick / 30 );
            stack.mulPose(rot);

            var consumer = source.getBuffer(Sheets.translucentCullBlockSheet());
                           RenderBufferHelper.renderCubeFace(
                                   consumer,
                                   stack.last(),
                                   0x8888FFFF,
                                   LightTexture.FULL_BRIGHT,
                                   Direction.UP,
                                   -3.5f, 0, -3.5f, pentagramSprite.getU0(), pentagramSprite.getV1(),
                                   -3.5f, 0,  3.5f, pentagramSprite.getU0(), pentagramSprite.getV0(),
                                   3.5f, 0,  3.5f, pentagramSprite.getU1(), pentagramSprite.getV0(),
                                   3.5f, 0, -3.5f, pentagramSprite.getU1(), pentagramSprite.getV1()
                           );

                           stack.popPose();



    }
}