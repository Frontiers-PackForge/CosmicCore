package com.ghostipedia.cosmiccore.client.murkbloom;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.dev.AbyssDevView;
import com.ghostipedia.cosmiccore.common.data.worldgen.abyss.AbyssRegions;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

import com.mojang.blaze3d.shaders.FogShape;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, value = Dist.CLIENT)
public final class AbyssClientFog {

    private AbyssClientFog() {}

    public static final ResourceKey<Level> HOLLOW_DIM = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath("undergarden", "undergarden"));
    public static final int ENTRY_Y = -60;

    private static final float[][] LAYER_FOG = {
            { 0x14 / 255f, 0x28 / 255f, 0x26 / 255f },
            { 0x0B / 255f, 0x0F / 255f, 0x16 / 255f },
            { 0x07 / 255f, 0x06 / 255f, 0x0C / 255f },
            { 0x06 / 255f, 0x05 / 255f, 0x07 / 255f },
            { 0x10 / 255f, 0x04 / 255f, 0x07 / 255f },
    };

    private static final float FOG_NEAR = 12f;
    private static final float FOG_FAR = 112f;
    private static final float HELMET_FAR_BONUS = 1.25f;
    private static final float STIR_FAR_PULL = 0.45f;
    private static final int EDGE_BLEND = 24;

    private static final String COPPER_DIVING_HELMET = "create:copper_diving_helmet";
    private static final String NETHERITE_DIVING_HELMET = "create:netherite_diving_helmet";

    public static boolean inHollowWater(Minecraft mc) {
        if (mc.level == null || mc.player == null) return false;
        if (!mc.level.dimension().equals(HOLLOW_DIM)) return false;
        if (mc.gameRenderer.getMainCamera().getPosition().y > ENTRY_Y) return false;
        return mc.gameRenderer.getMainCamera().getFluidInCamera() == FogType.WATER;
    }

    public static float[] fogColorAt(double y) {
        int layer = AbyssRegions.layer((int) Math.floor(y));
        float[] base = LAYER_FOG[Math.min(layer, LAYER_FOG.length - 1)];
        float r = base[0];
        float g = base[1];
        float b = base[2];
        for (int i = 0; i < AbyssRegions.LAYER_EDGES.length; i++) {
            int edge = AbyssRegions.LAYER_EDGES[i];
            double d = y - edge;
            if (Math.abs(d) < EDGE_BLEND) {
                float t = (float) (0.5 - d / (2.0 * EDGE_BLEND));
                float[] above = LAYER_FOG[i];
                float[] below = LAYER_FOG[Math.min(i + 1, LAYER_FOG.length - 1)];
                r = Mth.lerp(t, above[0], below[0]);
                g = Mth.lerp(t, above[1], below[1]);
                b = Mth.lerp(t, above[2], below[2]);
                break;
            }
        }
        return new float[] { r, g, b };
    }

    private static final float VANILLA_FAR_BASELINE = 26f;
    private static final float VANILLA_NEAR_BASELINE = 1f;
    private static final int GATE_START_ABOVE = 20;
    private static final int GATE_FULL_BELOW = 4;

    public static float entryGate(Minecraft mc) {
        if (mc.level == null || !mc.level.dimension().equals(HOLLOW_DIM)) return 0f;
        if (mc.gameRenderer.getMainCamera().getFluidInCamera() != FogType.WATER) return 0f;
        double camY = mc.gameRenderer.getMainCamera().getPosition().y;
        float t = (float) ((ENTRY_Y + GATE_START_ABOVE - camY) / (GATE_START_ABOVE + GATE_FULL_BELOW));
        t = Mth.clamp(t, 0f, 1f);
        return t * t * (3f - 2f * t);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (AbyssDevView.stripFog) return;
        Minecraft mc = Minecraft.getInstance();
        float gate = entryGate(mc);
        if (gate <= 0f) return;

        float far = FOG_FAR;
        if (wearsDivingHelmet(mc.player)) far *= HELMET_FAR_BONUS;
        float stir = MurkbloomClientState.intensity();
        far *= 1f - STIR_FAR_PULL * stir;
        far *= 1f + 0.06f * stir * (float) Math.sin(MurkbloomClientState.ticks() * 0.017);
        float near = FOG_NEAR * (1f - 0.5f * stir);

        event.setNearPlaneDistance(Mth.lerp(gate, VANILLA_NEAR_BASELINE, near));
        event.setFarPlaneDistance(Mth.lerp(gate, VANILLA_FAR_BASELINE, far));
        event.setFogShape(FogShape.CYLINDER);
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        if (AbyssDevView.stripFog) return;
        Minecraft mc = Minecraft.getInstance();
        float gate = entryGate(mc);
        if (gate <= 0f) return;

        double y = mc.gameRenderer.getMainCamera().getPosition().y;
        float[] color = fogColorAt(y);
        float murk = 1f - 0.35f * MurkbloomClientState.intensity();
        event.setRed(Mth.lerp(gate, (float) event.getRed(), color[0] * murk));
        event.setGreen(Mth.lerp(gate, (float) event.getGreen(), color[1] * murk));
        event.setBlue(Mth.lerp(gate, (float) event.getBlue(), color[2] * murk));
    }

    private static boolean wearsDivingHelmet(LivingEntity entity) {
        ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (head.isEmpty()) return false;
        String id = head.getItem().builtInRegistryHolder().key().location().toString();
        return COPPER_DIVING_HELMET.equals(id) || NETHERITE_DIVING_HELMET.equals(id);
    }
}
