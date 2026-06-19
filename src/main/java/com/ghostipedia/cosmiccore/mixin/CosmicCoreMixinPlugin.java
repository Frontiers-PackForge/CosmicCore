package com.ghostipedia.cosmiccore.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Mixin plugin to conditionally load mixins based on mod presence.
 * Prevents EMI mixins from loading when EMI is not present.
 */
public class CosmicCoreMixinPlugin implements IMixinConfigPlugin {

    private static final boolean EMI_LOADED;
    private static final boolean JEI_LOADED;

    static {
        // Check if EMI is present by looking for a resource file, not a class
        // This avoids loading any class too early which would break mixins
        EMI_LOADED = CosmicCoreMixinPlugin.class.getClassLoader()
                .getResource("dev/emi/emi/api/EmiApi.class") != null;
        JEI_LOADED = CosmicCoreMixinPlugin.class.getClassLoader()
                .getResource("mezz/jei/library/plugins/jei/tags/TagInfoRecipeCategory.class") != null;
    }

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        // Skip EMI mixins if EMI is not loaded
        if (mixinClassName.contains(".emi.") && !EMI_LOADED) {
            return false;
        }
        // Skip JEI mixins if JEI is not loaded
        if (mixinClassName.contains(".jei.") && !JEI_LOADED) {
            return false;
        }

        return true;
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
