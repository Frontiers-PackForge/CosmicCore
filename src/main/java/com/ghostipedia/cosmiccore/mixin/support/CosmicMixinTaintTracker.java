package com.ghostipedia.cosmiccore.mixin.support;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public final class CosmicMixinTaintTracker {

    public static final String CRASH_NOTICE = "This crash report is from the modpack, Cosmic Frontiers. Report issues " +
            "to Cosmic Frontiers before reporting to external mod repositories. Cosmic Frontiers heavily modifies " +
            "mods with scripts, mixins, and other behaviors that may introduce non-standard bugs outside of the " +
            "scope of a mod-dev to fix.";
    public static final String LOG_NOTICE = "This log is from the modpack, Cosmic Frontiers. Report issues to Cosmic " +
            "Frontiers before reporting to external mod repositories. Cosmic Frontiers heavily modifies mods with " +
            "scripts, mixins, and other behaviors that may introduce non-standard bugs outside of the scope of a " +
            "mod-dev to fix.";

    private static final String MIXIN_MERGED = "Lorg/spongepowered/asm/mixin/transformer/meta/MixinMerged;";
    private static final Logger LOGGER = LoggerFactory.getLogger("CosmicCore/MixinTaint");
    private static final Map<Domain, Map<String, Set<String>>> TAINTS = new EnumMap<>(Domain.class);

    private CosmicMixinTaintTracker() {}

    public static void printLogNotice() {
        LOGGER.warn("================================================================================");
        LOGGER.warn(LOG_NOTICE);
        LOGGER.warn("================================================================================");
    }

    public static synchronized void record(String targetClassName, ClassNode targetClass, String mixinClassName) {
        Domain domain = Domain.forClass(targetClassName);
        if (domain == null) {
            return;
        }
        Map<String, Set<String>> targets = TAINTS.computeIfAbsent(domain, ignored -> new TreeMap<>());
        Set<String> mixins = targets.computeIfAbsent(targetClassName, ignored -> new TreeSet<>());
        mixins.add(mixinClassName);
        for (MethodNode method : targetClass.methods) {
            collect(method.visibleAnnotations, mixins);
            collect(method.invisibleAnnotations, mixins);
        }
        targetClass.sourceFile = "Cosmic Frontiers transformed " + domain.label;
        targetClass.sourceDebug = String.join("\n", mixins);
        LOGGER.info("Cosmic Frontiers mixin taint: {} target {} via {}", domain.label, targetClassName,
                String.join(", ", mixins));
    }

    public static synchronized String crashHeader() {
        StringBuilder text = new StringBuilder();
        text.append("================================================================================\n");
        text.append(CRASH_NOTICE).append("\n\n");
        text.append("CosmicCore mixin taint summary:\n");
        if (TAINTS.isEmpty()) {
            text.append("  No tracked GTM, EMI, or AE2 target classes were recorded before this crash.\n");
        } else {
            for (Domain domain : Domain.values()) {
                Map<String, Set<String>> targets = TAINTS.get(domain);
                if (targets == null || targets.isEmpty()) {
                    continue;
                }
                text.append("  ").append(domain.label).append(": ").append(targets.size())
                        .append(" transformed classes\n");
                for (Map.Entry<String, Set<String>> entry : targets.entrySet()) {
                    text.append("    ").append(entry.getKey()).append(" <- ")
                            .append(String.join(", ", entry.getValue())).append("\n");
                }
            }
        }
        text.append("================================================================================\n\n");
        return text.toString();
    }

    public static synchronized Map<String, List<String>> snapshot() {
        Map<String, List<String>> snapshot = new TreeMap<>();
        TAINTS.values().forEach(targets -> targets.forEach((target, mixins) -> snapshot.put(target,
                Collections.unmodifiableList(new ArrayList<>(mixins)))));
        return Collections.unmodifiableMap(snapshot);
    }

    private static void collect(List<AnnotationNode> annotations, Set<String> mixins) {
        if (annotations == null) {
            return;
        }
        for (AnnotationNode annotation : annotations) {
            if (!MIXIN_MERGED.equals(annotation.desc) || annotation.values == null) {
                continue;
            }
            for (int index = 0; index + 1 < annotation.values.size(); index += 2) {
                if ("mixin".equals(annotation.values.get(index)) &&
                        annotation.values.get(index + 1) instanceof String name) {
                    mixins.add(name);
                }
            }
        }
    }

    private enum Domain {

        GTM("GTM", "com.gregtechceu.gtceu."),
        EMI("EMI", "dev.emi.emi."),
        AE2("AE2", "appeng.");

        private final String label;
        private final String classPrefix;

        Domain(String label, String classPrefix) {
            this.label = label;
            this.classPrefix = classPrefix;
        }

        private static Domain forClass(String className) {
            for (Domain domain : values()) {
                if (className.startsWith(domain.classPrefix)) {
                    return domain;
                }
            }
            return null;
        }
    }
}
