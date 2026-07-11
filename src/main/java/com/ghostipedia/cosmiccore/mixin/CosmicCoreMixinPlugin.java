package com.ghostipedia.cosmiccore.mixin;

import org.objectweb.asm.tree.ClassNode;
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

    private static final Map<String, Boolean> GATES = new HashMap<>();

    static {
        ClassLoader loader = CosmicCoreMixinPlugin.class.getClassLoader();
        Map<String, String> probes = Map.ofEntries(
                Map.entry(".emi.", "dev/emi/emi/api/EmiApi.class"),
                Map.entry(".jei.", "mezz/jei/library/plugins/jei/tags/TagInfoRecipeCategory.class"),
                Map.entry(".embers.", "com/rekindled/embers/worldgen/EmbersLateWorldgen.class"),
                Map.entry(".xaerominimap.", "xaero/common/minimap/render/MinimapFBORenderer.class"),
                Map.entry(".xaeroworldmap.", "xaero/map/element/MapElementRenderHandler.class"),
                Map.entry(".architectury.", "dev/architectury/impl/NetworkAggregator.class"),
                Map.entry(".aero.",
                        "dev/eriksonn/aeronautics/content/blocks/hot_air/balloon/effect/ClientBalloonEffectRenderer.class"),
                Map.entry(".sable.", "dev/ryanhcode/sable/api/block/BlockSubLevelAssemblyListener.class"),
                Map.entry(".ftbchunks.", "dev/ftb/mods/ftbchunks/client/FTBChunksClient.class"),
                Map.entry(".occultism.", "com/klikli_dev/occultism/crafting/recipe/PasteRepairItemRecipe.class"),
                Map.entry(".undergarden.", "quek/undergarden/event/UthericInfectionEvents.class"));
        probes.forEach((token, resource) -> GATES.put(token, loader.getResource(resource) != null));
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
    public void onLoad(String mixinPackage) {}

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
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
