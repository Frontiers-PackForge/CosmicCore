package com.ghostipedia.cosmiccore.mixin;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Applies integration mixins only when the optional mod they target is present. Presence is detected by probing for a
 * marker resource (a class file) instead of loading the class, so no integration class is touched too early. Each
 * entry maps a mixin-package token to its marker resource; adding a gated integration is one map entry.
 */
public class CosmicCoreMixinPlugin implements IMixinConfigPlugin {

    private static final String DRIPPY_SCALE_MIXIN = ".drippy.DrippyLoadingOverlayScaleFixMixin";
    private static final String FANCY_MENU_UI_BASE = "de/keksuccino/fancymenu/util/rendering/ui/UIBase";
    private static final String POSE_STACK = "com/mojang/blaze3d/vertex/PoseStack";
    private static final String DRIPPY_SCALE_COMPAT = "com/ghostipedia/cosmiccore/client/compat/drippy/DrippyRenderScaleCompat";
    private static final String AERONAUTICS_MARKER = "dev/eriksonn/aeronautics/content/blocks/hot_air/balloon/effect/ClientBalloonEffectRenderer.class";
    private static final String MIXIN_SQUARED_TARGET_HANDLER = "com/bawnorton/mixinsquared/TargetHandler.class";
    private static final String MODULAR_UI_MIXIN_PACKAGE = "brachy.modularui.core.mixins";
    private static final String MODULAR_UI_SLOT_ACCESSOR = "client.SlotAccessor";
    private static final String QUALITY_FOOD_MARKER = "de/cadentem/quality_food/util/QualityUtils.class";
    private static final String ULTIMINE_CROP_MARKER = "dev/ftb/mods/ftbultimine/crops/VanillaCropLikeHandler.class";
    private static final Map<String, Boolean> GATES = new HashMap<>();

    private String mixinPackage;

    static {
        ClassLoader loader = CosmicCoreMixinPlugin.class.getClassLoader();
        Map<String, String> probes = Map.ofEntries(
                Map.entry(".emi.", "dev/emi/emi/api/EmiApi.class"),
                Map.entry(".jei.", "mezz/jei/library/plugins/jei/tags/TagInfoRecipeCategory.class"),
                Map.entry(".embers.", "com/rekindled/embers/worldgen/EmbersLateWorldgen.class"),
                Map.entry(".xaerominimap.", "xaero/common/minimap/render/MinimapFBORenderer.class"),
                Map.entry(".xaeroworldmap.", "xaero/map/element/MapElementRenderHandler.class"),
                Map.entry(".architectury.", "dev/architectury/impl/NetworkAggregator.class"),
                Map.entry(".aero.", AERONAUTICS_MARKER),
                Map.entry(".simulated.",
                        "dev/simulated_team/simulated/content/blocks/physics_assembler/PhysicsAssemblerBlockEntity.class"),
                Map.entry(".sable.", "dev/ryanhcode/sable/api/block/BlockSubLevelAssemblyListener.class"),
                Map.entry(".ftbchunks.", "dev/ftb/mods/ftbchunks/client/FTBChunksClient.class"),
                Map.entry(".occultism.", "com/klikli_dev/occultism/crafting/recipe/PasteRepairItemRecipe.class"),
                Map.entry(".terrablender.", "terrablender/worldgen/surface/NamespacedSurfaceRuleSource.class"),
                Map.entry(".drippy.",
                        "de/keksuccino/drippyloadingscreen/mixin/mixins/common/client/MixinLoadingOverlay.class"),
                Map.entry(".undergarden.", "quek/undergarden/event/UthericInfectionEvents.class"));
        probes.forEach((token, resource) -> GATES.put(token, loader.getResource(resource) != null));
        GATES.put(
                ".aeroschema.",
                loader.getResource(AERONAUTICS_MARKER) != null &&
                        loader.getResource(MIXIN_SQUARED_TARGET_HANDLER) != null);
        GATES.put(
                ".qualityfoodultimine.",
                loader.getResource(QUALITY_FOOD_MARKER) != null &&
                        loader.getResource(ULTIMINE_CROP_MARKER) != null);
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        for (Map.Entry<String, Boolean> gate : GATES.entrySet()) {
            if (mixinClassName.contains(gate.getKey()) && !gate.getValue()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onLoad(String mixinPackage) {
        this.mixinPackage = mixinPackage;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        if (mixinPackage != null && mixinPackage.startsWith(MODULAR_UI_MIXIN_PACKAGE) &&
                MixinEnvironment.getCurrentEnvironment().getSide() == MixinEnvironment.Side.SERVER) {
            return List.of(MODULAR_UI_SLOT_ACCESSOR);
        }
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if (!mixinClassName.endsWith(DRIPPY_SCALE_MIXIN)) {
            return;
        }

        for (MethodNode method : targetClass.methods) {
            boolean drippyRenderMethod = false;
            for (AbstractInsnNode instruction : method.instructions) {
                if (instruction instanceof MethodInsnNode call && call.owner.equals(FANCY_MENU_UI_BASE) &&
                        call.name.equals("calculateFixedRenderScale") && call.desc.equals("(F)F")) {
                    drippyRenderMethod = true;
                    break;
                }
            }
            if (!drippyRenderMethod) {
                continue;
            }
            for (AbstractInsnNode instruction : method.instructions) {
                if (instruction instanceof MethodInsnNode call && call.owner.equals(POSE_STACK) &&
                        call.name.equals("scale") && call.desc.equals("(FFF)V")) {
                    method.instructions.set(
                            call,
                            new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    DRIPPY_SCALE_COMPAT,
                                    "applyUntrackedScale",
                                    "(Lcom/mojang/blaze3d/vertex/PoseStack;FFF)V",
                                    false));
                    return;
                }
            }
        }
        throw new IllegalStateException("Drippy loading-overlay scale call was not found");
    }
}
