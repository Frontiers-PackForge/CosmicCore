package com.ghostipedia.cosmiccore.mixin;

import com.ghostipedia.cosmiccore.mixin.support.CosmicMixinTaintTracker;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HexFormat;
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
    private static final String QUALITY_FOOD_MARKER = "de/cadentem/quality_food/util/QualityUtils.class";
    private static final String ULTIMINE_CROP_MARKER = "dev/ftb/mods/ftbultimine/crops/VanillaCropLikeHandler.class";
    private static final List<String> EFFORTLESS_BUILDING_BASELINE_CLASSES = List.of(
            "neoforge/nl/requios/effortlessbuilding/buildpipeline/BuildPipelineClient.class",
            "neoforge/nl/requios/effortlessbuilding/render/BlockPreviewRenderer.class",
            "neoforge/nl/requios/effortlessbuilding/buildmode/BuildModes.class",
            "neoforge/nl/requios/effortlessbuilding/utilities/ItemUsageTracker.class",
            "neoforge/nl/requios/effortlessbuilding/render/ModifierRenderer.class",
            "neoforge/nl/requios/effortlessbuilding/network/PacketHandler.class",
            "neoforge/nl/requios/effortlessbuilding/screen/RadialMenu.class",
            "neoforge/nl/requios/effortlessbuilding/render/RenderHandler.class",
            "neoforge/nl/requios/effortlessbuilding/utilities/UndoManager.class",
            "neoforge/nl/requios/effortlessbuilding/utilities/UndoManager$UndoEntry.class",
            "neoforge/nl/requios/effortlessbuilding/utilities/UndoManager$BlockChange.class");
    private static final String EFFORTLESS_BUILDING_BASELINE_SHA256 = "8249009DB3A70B150BFD876A389529C796B7FA58E9B0C933A3024312D30B4F5F";
    private static final Map<String, Boolean> GATES = new HashMap<>();

    static {
        ClassLoader loader = CosmicCoreMixinPlugin.class.getClassLoader();
        Map<String, String> probes = Map.ofEntries(
                Map.entry(".emi.", "dev/emi/emi/api/EmiApi.class"),
                Map.entry(".ae2.", "appeng/integration/modules/itemlists/EncodingHelper.class"),
                Map.entry(".jei.", "mezz/jei/library/plugins/jei/tags/TagInfoRecipeCategory.class"),
                Map.entry(".embers.", "com/rekindled/embers/worldgen/EmbersLateWorldgen.class"),
                Map.entry(".xaerominimap.", "xaero/common/minimap/render/MinimapFBORenderer.class"),
                Map.entry(".xaeroworldmap.", "xaero/map/element/MapElementRenderHandler.class"),
                Map.entry(".architectury.", "dev/architectury/impl/NetworkAggregator.class"),
                Map.entry(".aero.", AERONAUTICS_MARKER),
                Map.entry(".malum.", "com/sammy/malum/client/renderer/renderpass/ParallelWorldRenderer.class"),
                Map.entry(".iris.", "net/irisshaders/iris/api/v0/IrisApi.class"),
                Map.entry(".sodium.",
                        "net/caffeinemc/mods/sodium/client/render/chunk/terrain/material/DefaultMaterials.class"),
                Map.entry(".simulated.",
                        "dev/simulated_team/simulated/content/blocks/physics_assembler/PhysicsAssemblerBlockEntity.class"),
                Map.entry(".spacegravity.", "com/spacegravity/spacegravity/SpaceGravityState.class"),
                Map.entry(".sable.", "dev/ryanhcode/sable/api/block/BlockSubLevelAssemblyListener.class"),
                Map.entry(".ftbchunks.", "dev/ftb/mods/ftbchunks/client/FTBChunksClient.class"),
                Map.entry(".ftbquests.", "dev/ftb/mods/ftbquests/quest/Quest.class"),
                Map.entry(".occultism.", "com/klikli_dev/occultism/crafting/recipe/PasteRepairItemRecipe.class"),
                Map.entry(".terrablender.", "terrablender/worldgen/surface/NamespacedSurfaceRuleSource.class"),
                Map.entry(".drippy.",
                        "de/keksuccino/drippyloadingscreen/mixin/mixins/common/client/MixinLoadingOverlay.class"),
                Map.entry(".undergarden.", "quek/undergarden/event/UthericInfectionEvents.class"));
        probes.forEach((token, resource) -> GATES.put(token, loader.getResource(resource) != null));
        GATES.put(
                ".deployer.",
                loader.getResource("net/liukrast/deployer/lib/logistics/board/AbstractPanelBehaviour.class") != null);
        GATES.put(
                ".repackaged.",
                loader.getResource("net/liukrast/repackaged/content/fluid/FluidPanelBehaviour.class") != null);
        GATES.put(".aeroschema.", loader.getResource(AERONAUTICS_MARKER) != null);
        GATES.put(
                ".qualityfoodultimine.",
                loader.getResource(QUALITY_FOOD_MARKER) != null &&
                        loader.getResource(ULTIMINE_CROP_MARKER) != null);
        GATES.put(".ebfix.", matchesEffortlessBuildingBaseline(loader));
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

    public static boolean isAeronauticsSchemaBypassAvailable() {
        return GATES.getOrDefault(".aeroschema.", false);
    }

    private static boolean matchesEffortlessBuildingBaseline(ClassLoader loader) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (String resource : EFFORTLESS_BUILDING_BASELINE_CLASSES) {
                try (InputStream input = loader.getResourceAsStream(resource)) {
                    if (input == null) return false;
                    digest.update(input.readAllBytes());
                }
            }
            String hash = HexFormat.of().withUpperCase().formatHex(digest.digest());
            return EFFORTLESS_BUILDING_BASELINE_SHA256.equals(hash);
        } catch (IOException | NoSuchAlgorithmException ignored) {
            return false;
        }
    }

    @Override
    public void onLoad(String mixinPackage) {
        CosmicMixinTaintTracker.printLogNotice();
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        CosmicMixinTaintTracker.record(targetClassName, targetClass, mixinClassName);
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
