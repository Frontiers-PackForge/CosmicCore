package com.ghostipedia.cosmiccore.client.renderer;

import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.item.PipeBlockItem;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.IToolGridHighlight;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.pipenet.IPipeType;
import com.gregtechceu.gtceu.client.util.RenderUtil;
import com.gregtechceu.gtceu.common.data.item.GTItemAbilities;
import com.gregtechceu.gtceu.common.item.behavior.CoverPlaceBehavior;
import com.gregtechceu.gtceu.common.item.tool.rotation.CustomBlockRotations;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;

import brachy.modularui.drawable.UITexture;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import org.joml.Quaterniondc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Set;
import java.util.function.Function;

import static com.gregtechceu.gtceu.utils.GTMatrixUtils.getRotation;

public final class SubLevelGridOverlayRenderer {

    private static float rColour;
    private static float gColour;
    private static float bColour;

    private SubLevelGridOverlayRenderer() {}

    public static void render(RenderHighlightEvent.Block event, BlockHitResult target, ClientSubLevel subLevel) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        Player player = mc.player;
        if (level == null || player == null) {
            return;
        }
        Camera realCamera = mc.gameRenderer.getMainCamera();
        Vec3 camPos = realCamera.getPosition();
        Pose3dc pose = subLevel.renderPose();
        Quaterniondc orientation = pose.orientation();
        MultiBufferSource bufferSource = event.getMultiBufferSource();
        PoseStack poseStack = event.getPoseStack();
        ItemStack held = player.getMainHandItem();
        BlockPos blockPos = target.getBlockPos();
        Set<GTToolType> toolType = ToolHelper.getToolTypes(held);
        BlockEntity blockEntity = level.getBlockEntity(blockPos);

        if ((!toolType.isEmpty()) || (held.isEmpty() && player.isShiftKeyDown())) {
            IToolGridHighlight gridHighlight = null;
            if (blockEntity instanceof IToolGridHighlight h) {
                gridHighlight = h;
            } else if (level.getBlockState(blockPos).getBlock() instanceof IToolGridHighlight h) {
                gridHighlight = h;
            } else if (toolType.contains(GTToolType.WRENCH) || held.canPerformAction(GTItemAbilities.WRENCH_ROTATE)) {
                var behavior = CustomBlockRotations.getCustomRotation(level.getBlockState(blockPos).getBlock());
                if (behavior != null && behavior.showGrid()) {
                    gridHighlight = new IToolGridHighlight() {

                        @Override
                        public UITexture sideTips(Player p, BlockPos pos, BlockState st, Set<GTToolType> tts,
                                                  ItemStack hh, Direction side) {
                            return behavior.showSideTip(st, side) ? GTGuiTextures.TOOL_FRONT_FACING_ROTATION : null;
                        }
                    };
                }
            }
            if (gridHighlight == null) {
                return;
            }
            BlockState state = level.getBlockState(blockPos);
            poseStack.pushPose();
            poseStack.setIdentity();
            if (gridHighlight.shouldRenderGrid(player, blockPos, state, held, toolType)) {
                final IToolGridHighlight g = gridHighlight;
                drawGridOverlays(poseStack, bufferSource, pose, orientation, camPos, target,
                        side -> g.sideTips(player, blockPos, state, toolType, held, side));
            } else {
                Direction facing = target.getDirection();
                UITexture tex = gridHighlight.sideTips(player, blockPos, state, toolType, held, facing);
                if (tex != null) {
                    RenderSystem.disableDepthTest();
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    anchorToFace(poseStack, pose, orientation, camPos, blockPos, facing,
                            facing.getAxis() == Direction.Axis.Y ? Direction.SOUTH : Direction.NORTH);
                    drawOverlayTexture(poseStack, bufferSource, tex, 0xffffffff, 4, 4, 8, 8);
                    RenderSystem.disableBlend();
                    RenderSystem.enableDepthTest();
                }
            }
            poseStack.popPose();
            return;
        }

        ICoverable coverable = GTCapabilityHelper.getCoverable(level, blockPos, target.getDirection());
        if (coverable != null && CoverPlaceBehavior.isCoverBehaviorItem(held, coverable::hasAnyCover,
                cd -> ICoverable.canPlaceCover(cd, coverable))) {
            poseStack.pushPose();
            poseStack.setIdentity();
            drawGridOverlays(poseStack, bufferSource, pose, orientation, camPos, target,
                    side -> coverable.hasCover(side) ? null : GTGuiTextures.TOOL_ATTACH_COVER);
            poseStack.popPose();
        }

        var pipeType = held.getItem() instanceof PipeBlockItem pbi ? pbi.getBlock().pipeType : null;
        if (pipeType instanceof IPipeType<?> type && blockEntity instanceof PipeBlockEntity<?, ?> pbe &&
                pbe.getPipeType().type().equals(type.type())) {
            poseStack.pushPose();
            poseStack.setIdentity();
            drawGridOverlays(poseStack, bufferSource, pose, orientation, camPos, target,
                    side -> level.isEmptyBlock(blockPos.relative(side)) ? pbe.getPipeTexture(true) : null);
            poseStack.popPose();
        }
    }

    private static void drawGridOverlays(PoseStack poseStack, MultiBufferSource bufferSource, Pose3dc pose,
                                         Quaterniondc orientation, Vec3 camPos, BlockHitResult hit,
                                         Function<Direction, UITexture> texture) {
        rColour = gColour = 0.2F + (float) Math.sin((System.currentTimeMillis() % (Mth.PI * 800)) / 800) / 2;
        bColour = 1f;
        BlockPos blockPos = hit.getBlockPos();
        float minX = 0f;
        float maxX = 1f;
        float minY = 0f;
        float maxY = 1f;
        float maxZ = 1.01f;
        Direction attachSide = ICoverable.traceCoverSide(hit);
        Vector3f topRight = new Vector3f(maxX, maxY, maxZ);
        Vector3f bottomRight = new Vector3f(maxX, minY, maxZ);
        Vector3f bottomLeft = new Vector3f(minX, minY, maxZ);
        Vector3f topLeft = new Vector3f(minX, maxY, maxZ);
        Vector3f shiftX = new Vector3f(0.25f, 0, 0);
        Vector3f shiftY = new Vector3f(0, 0.25f, 0);
        Vector3f localCenter = new Vector3f(0.5f, 0.5f, 0.5f);
        topRight.sub(localCenter);
        bottomRight.sub(localCenter);
        bottomLeft.sub(localCenter);
        topLeft.sub(localCenter);
        Direction front = hit.getDirection();
        Direction back = front.getOpposite();
        Direction left = RelativeDirection.LEFT.applyDirection(front);
        Direction right = RelativeDirection.RIGHT.applyDirection(front);
        Direction top = RelativeDirection.UP.applyDirection(front);
        Direction bottom = RelativeDirection.DOWN.applyDirection(front);
        Quaternionfc rotation = getRotation(Direction.SOUTH, front);
        topRight.rotate(rotation);
        bottomRight.rotate(rotation);
        bottomLeft.rotate(rotation);
        topLeft.rotate(rotation);
        shiftX.rotate(rotation);
        shiftY.rotate(rotation);
        UITexture leftBlocked = texture.apply(left);
        UITexture rightBlocked = texture.apply(right);
        UITexture topBlocked = texture.apply(top);
        UITexture bottomBlocked = texture.apply(bottom);
        UITexture frontBlocked = texture.apply(front);
        UITexture backBlocked = texture.apply(back);
        topRight.add(localCenter);
        bottomRight.add(localCenter);
        bottomLeft.add(localCenter);
        topLeft.add(localCenter);

        PoseStack.Pose last = poseStack.last();
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.lines());
        RenderSystem.lineWidth(3);
        double ox = blockPos.getX();
        double oy = blockPos.getY();
        double oz = blockPos.getZ();
        drawLine(last, buffer, pose, camPos, ox, oy, oz, sub(topRight, shiftX), sub(bottomRight, shiftX));
        drawLine(last, buffer, pose, camPos, ox, oy, oz, add(bottomLeft, shiftX), add(topLeft, shiftX));
        drawLine(last, buffer, pose, camPos, ox, oy, oz, sub(topLeft, shiftY), sub(topRight, shiftY));
        drawLine(last, buffer, pose, camPos, ox, oy, oz, add(bottomLeft, shiftY), add(bottomRight, shiftY));

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        anchorToFace(poseStack, pose, orientation, camPos, blockPos, front, Direction.SOUTH);
        float margin = 0.2f;
        if (leftBlocked != null) {
            drawOverlayTextureWithMargin(poseStack, bufferSource, leftBlocked,
                    attachSide == left ? 0xffffffff : 0x44ffffff, 0, 6, margin);
        }
        if (topBlocked != null) {
            drawOverlayTextureWithMargin(poseStack, bufferSource, topBlocked,
                    attachSide == top ? 0xffffffff : 0x44ffffff, 6, 12, margin);
        }
        if (rightBlocked != null) {
            drawOverlayTextureWithMargin(poseStack, bufferSource, rightBlocked,
                    attachSide == right ? 0xffffffff : 0x44ffffff, 12, 6, margin);
        }
        if (bottomBlocked != null) {
            drawOverlayTextureWithMargin(poseStack, bufferSource, bottomBlocked,
                    attachSide == bottom ? 0xffffffff : 0x44ffffff, 6, 0, margin);
        }
        if (frontBlocked != null) {
            drawOverlayTextureWithMargin(poseStack, bufferSource, frontBlocked,
                    attachSide == front ? 0xffffffff : 0x44ffffff, 6, 6, margin);
        }
        if (backBlocked != null) {
            int color = attachSide == back ? 0xffffffff : 0x44ffffff;
            drawOverlayTextureWithMargin(poseStack, bufferSource, backBlocked, color, 0, 0, margin);
            drawOverlayTextureWithMargin(poseStack, bufferSource, backBlocked, color, 12, 0, margin);
            drawOverlayTextureWithMargin(poseStack, bufferSource, backBlocked, color, 0, 12, margin);
            drawOverlayTextureWithMargin(poseStack, bufferSource, backBlocked, color, 12, 12, margin);
        }
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    private static void anchorToFace(PoseStack poseStack, Pose3dc pose, Quaterniondc orientation, Vec3 camPos,
                                     BlockPos blockPos, Direction face, Direction spinReference) {
        double ax = blockPos.getX() + 0.5 + face.getStepX() * 0.51;
        double ay = blockPos.getY() + 0.5 + face.getStepY() * 0.51;
        double az = blockPos.getZ() + 0.5 + face.getStepZ() * 0.51;
        Vec3 world = pose.transformPosition(new Vec3(ax, ay, az));
        poseStack.translate(world.x - camPos.x, world.y - camPos.y, world.z - camPos.z);
        poseStack.mulPose(new Quaternionf().set(orientation));
        RenderUtil.rotateToFace(poseStack, face, spinReference);
        poseStack.scale(1f / 16, 1f / 16, 0);
        poseStack.translate(-8, -8, 0);
    }

    private static Vector3f sub(Vector3fc a, Vector3fc b) {
        return new Vector3f(a).sub(b);
    }

    private static Vector3f add(Vector3fc a, Vector3fc b) {
        return new Vector3f(a).add(b);
    }

    private static void drawLine(PoseStack.Pose pose, VertexConsumer buffer, Pose3dc subPose, Vec3 camPos,
                                 double ox, double oy, double oz, Vector3fc fromLocal, Vector3fc toLocal) {
        Vec3 from = camRel(subPose, camPos, ox + fromLocal.x(), oy + fromLocal.y(), oz + fromLocal.z());
        Vec3 to = camRel(subPose, camPos, ox + toLocal.x(), oy + toLocal.y(), oz + toLocal.z());
        float nx = (float) (from.x - to.x);
        float ny = (float) (from.y - to.y);
        float nz = (float) (from.z - to.z);
        buffer.addVertex(pose.pose(), (float) from.x, (float) from.y, (float) from.z)
                .setColor(rColour, gColour, bColour, 1f).setNormal(nx, ny, nz);
        buffer.addVertex(pose.pose(), (float) to.x, (float) to.y, (float) to.z)
                .setColor(rColour, gColour, bColour, 1f).setNormal(nx, ny, nz);
    }

    private static Vec3 camRel(Pose3dc subPose, Vec3 camPos, double px, double py, double pz) {
        Vec3 world = subPose.transformPosition(new Vec3(px, py, pz));
        return new Vec3(world.x - camPos.x, world.y - camPos.y, world.z - camPos.z);
    }

    private static void drawOverlayTexture(PoseStack poseStack, MultiBufferSource bufferSource, UITexture texture,
                                           int color, float x, float y, float w, float h) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.text(texture.location));
        var p = poseStack.last().pose();
        float u0 = texture.u0;
        float v0 = texture.v0;
        float u1 = texture.u1;
        float v1 = texture.v1;
        consumer.addVertex(p, x, y + h, 0).setColor(color).setUv(u0, v0 + v1).setLight(LightTexture.FULL_BRIGHT);
        consumer.addVertex(p, x + w, y + h, 0).setColor(color).setUv(u0 + u1, v0 + v1)
                .setLight(LightTexture.FULL_BRIGHT);
        consumer.addVertex(p, x + w, y, 0).setColor(color).setUv(u0 + u1, v0).setLight(LightTexture.FULL_BRIGHT);
        consumer.addVertex(p, x, y, 0).setColor(color).setUv(u0, v0).setLight(LightTexture.FULL_BRIGHT);
    }

    private static void drawOverlayTextureWithMargin(PoseStack poseStack, MultiBufferSource bufferSource,
                                                     UITexture texture, int color, float x, float y, float m) {
        drawOverlayTexture(poseStack, bufferSource, texture, color, x + m, y + m, 4 - 2 * m, 4 - 2 * m);
    }
}
