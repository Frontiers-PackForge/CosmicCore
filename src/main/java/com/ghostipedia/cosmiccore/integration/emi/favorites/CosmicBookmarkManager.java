package com.ghostipedia.cosmiccore.integration.emi.favorites;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.integration.emi.CosmicFavorite;
import com.ghostipedia.cosmiccore.integration.emi.CosmicRecipeCell;
import com.ghostipedia.cosmiccore.integration.emi.CosmicRecipeFavorite;
import com.ghostipedia.cosmiccore.integration.emi.favorites.CosmicBookmarkGroup.BookmarkEntry;
import com.ghostipedia.cosmiccore.integration.emi.favorites.CosmicBookmarkGroup.GroupType;
import com.ghostipedia.cosmiccore.integration.emi.favorites.CosmicBookmarkGroup.IngredientSnapshot;
import com.ghostipedia.cosmiccore.integration.emi.favorites.CosmicBookmarkGroup.RecipeEntry;
import com.ghostipedia.cosmiccore.integration.emi.favorites.CosmicBookmarkGroup.StackEntry;

import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.runtime.EmiFavorite;
import dev.emi.emi.runtime.EmiFavorites;
import dev.emi.emi.runtime.EmiPersistentData;
import dev.emi.emi.screen.EmiScreenManager;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

public final class CosmicBookmarkManager {

    public record RecipeSidebarProjection(List<? extends EmiIngredient> stacks) {}

    private record ProjectionKey(long revision, int pageSize, List<Integer> widths) {}

    private record LegacyDocument(List<CosmicBookmarkGroup> groups, int activeIndex) {}

    private record LegacyRecipeMatch(String recipeId, boolean ambiguous) {}

    private record CachedProjection(
                                    ProjectionKey key,
                                    RecipeSidebarProjection projection,
                                    IdentityHashMap<EmiIngredient, String> entryIds) {}

    private static final int SCHEMA_VERSION = 2;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final CosmicBookmarkManager INSTANCE = new CosmicBookmarkManager();

    private final List<CosmicBookmarkGroup> groups = new ArrayList<>();
    private final Map<Object, CachedProjection> projections = new WeakHashMap<>();
    private final IdentityHashMap<EmiIngredient, String> runtimeEntryIds = new IdentityHashMap<>();
    private final IdentityHashMap<EmiIngredient, String> projectedEntryIds = new IdentityHashMap<>();
    private final Map<String, EmiFavorite> runtimeFavorites = new HashMap<>();
    private int activeIndex;
    private long revision;
    private boolean ready;
    private boolean projecting;
    private boolean saving;
    private boolean persistenceWritable = true;
    private String projectedFingerprint = "";

    private CosmicBookmarkManager() {
        groups.add(new CosmicBookmarkGroup("Default"));
    }

    public static CosmicBookmarkManager getInstance() {
        return INSTANCE;
    }

    public boolean isReady() {
        return ready;
    }

    public boolean isProjecting() {
        return projecting;
    }

    public int getActiveIndex() {
        return activeIndex;
    }

    public int getGroupCount() {
        return groups.size();
    }

    public CosmicBookmarkGroup getActiveGroup() {
        activeIndex = Math.clamp(activeIndex, 0, Math.max(0, groups.size() - 1));
        return groups.get(activeIndex);
    }

    public @Nullable CosmicBookmarkGroup getGroupAt(int index) {
        return index >= 0 && index < groups.size() ? groups.get(index) : null;
    }

    public void nextGroup() {
        if (groups.size() < 2) return;
        mutateAndCommit(() -> activeIndex = (activeIndex + 1) % groups.size());
    }

    public void prevGroup() {
        if (groups.size() < 2) return;
        mutateAndCommit(() -> activeIndex = (activeIndex - 1 + groups.size()) % groups.size());
    }

    public void setActiveIndex(int index) {
        if (index < 0 || index >= groups.size() || index == activeIndex) return;
        mutateAndCommit(() -> activeIndex = index);
    }

    public void addGroup(String name) {
        addGroup(name, GroupType.REGULAR);
    }

    public void addGroup(String name, GroupType type) {
        mutateAndCommit(() -> {
            groups.add(new CosmicBookmarkGroup(name, type));
            activeIndex = groups.size() - 1;
        });
    }

    public void removeGroup(int index) {
        if (groups.size() <= 1 || index < 0 || index >= groups.size() || groups.get(index).size() > 0) return;
        mutateAndCommit(() -> {
            groups.remove(index);
            activeIndex = Math.clamp(activeIndex, 0, groups.size() - 1);
        });
    }

    public void forceRemoveGroup(int index) {
        if (index < 0 || index >= groups.size()) return;
        mutateAndCommit(() -> {
            if (groups.size() == 1) {
                groups.set(0, new CosmicBookmarkGroup("Default"));
                activeIndex = 0;
            } else {
                groups.remove(index);
                activeIndex = Math.clamp(activeIndex, 0, groups.size() - 1);
            }
        });
    }

    public void toggleRecipe(EmiRecipe recipe) {
        if (recipe.getOutputs().isEmpty()) return;
        RecipeEntry candidate;
        try {
            candidate = createRecipeEntry(recipe);
        } catch (RuntimeException exception) {
            CosmicCore.LOGGER.error("Failed to serialize EMI recipe bookmark {}", recipe.getId(), exception);
            return;
        }
        String recipeId = recipe.getId() == null ? null : recipe.getId().toString();
        mutateAndCommit(() -> {
            int target = findOrCreateGroup(GroupType.RECIPE);
            activeIndex = target;
            CosmicBookmarkGroup group = groups.get(target);
            int existing = findRecipe(group, recipeId, candidate);
            if (existing >= 0) {
                group.getEntries().remove(existing);
                discardEmptyGroup(group);
            } else {
                group.getEntries().add(candidate);
            }
        });
    }

    public void toggleStack(EmiIngredient ingredient, @Nullable Long amountOverride, @Nullable EmiRecipe recipe) {
        JsonElement serialized = EmiIngredientSerializer.getSerialized(unwrap(ingredient));
        if (serialized == null) return;
        mutateAndCommit(() -> {
            int target = findOrCreateGroup(GroupType.REGULAR);
            activeIndex = target;
            CosmicBookmarkGroup group = groups.get(target);
            String recipeId = recipe == null || recipe.getId() == null ? null : recipe.getId().toString();
            StackEntry candidate = new StackEntry(serialized, amountOverride, recipeId);
            String key = entryKey(candidate);
            int existing = -1;
            for (int i = 0; i < group.getEntries().size(); i++) {
                if (entryKey(group.getEntries().get(i)).equals(key)) {
                    existing = i;
                    break;
                }
            }
            if (existing >= 0) {
                group.getEntries().remove(existing);
                discardEmptyGroup(group);
            } else {
                group.getEntries().add(candidate);
            }
        });
    }

    public void removeEntry(String entryId) {
        mutateAndCommit(() -> {
            CosmicBookmarkGroup group = getActiveGroup();
            if (group.getEntries().removeIf(entry -> entry.id().equals(entryId))) discardEmptyGroup(group);
        });
    }

    public void adjustEntry(String entryId, long delta) {
        if (delta == 0) return;
        mutateAndCommit(() -> {
            CosmicBookmarkGroup group = getActiveGroup();
            for (int i = 0; i < group.getEntries().size(); i++) {
                BookmarkEntry entry = group.getEntries().get(i);
                if (!entry.id().equals(entryId)) continue;
                if (entry instanceof RecipeEntry recipeEntry) {
                    recipeEntry.adjustBatches(delta);
                } else if (entry instanceof StackEntry stackEntry) {
                    long current = stackEntry.amountOverride() == null ? ingredientAmount(stackEntry.ingredient()) :
                            stackEntry.amountOverride();
                    long adjusted = Math.max(1, saturatingAdd(current, delta));
                    group.getEntries().set(
                            i,
                            new StackEntry(stackEntry.id(), stackEntry.ingredient(), adjusted, stackEntry.recipeId()));
                }
                return;
            }
        });
    }

    public @Nullable String getProjectedEntryId(EmiIngredient ingredient) {
        if (ingredient instanceof CosmicRecipeFavorite recipeFavorite) return recipeFavorite.getBookmarkId();
        String id = projectedEntryIds.get(ingredient);
        return id == null ? runtimeEntryIds.get(ingredient) : id;
    }

    public boolean isRecipeEntry(String entryId) {
        for (BookmarkEntry entry : getActiveGroup().getEntries()) {
            if (entry.id().equals(entryId)) return entry instanceof RecipeEntry;
        }
        return false;
    }

    public RecipeSidebarProjection getRecipeProjection(Object owner, int pageSize, int[] widths) {
        ProjectionKey key = new ProjectionKey(revision, pageSize, Arrays.stream(widths).boxed().toList());
        CachedProjection cached = projections.get(owner);
        if (cached != null && cached.key().equals(key)) return cached.projection();
        IdentityHashMap<EmiIngredient, String> entryIds = new IdentityHashMap<>();
        RecipeSidebarProjection projection = buildRecipeProjection(pageSize, widths, entryIds);
        projections.put(owner, new CachedProjection(key, projection, entryIds));
        rebuildProjectedEntryIds();
        return projection;
    }

    public void loadAfterEmi() {
        ready = false;
        persistenceWritable = true;
        List<EmiFavorite> nativeFavorites = new ArrayList<>(EmiFavorites.favorites);
        Path path = getSavePath();
        boolean loaded = false;
        boolean migrated = false;
        if (Files.exists(path)) {
            try {
                JsonObject root = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
                if (root.has("schemaVersion")) {
                    int version = root.get("schemaVersion").getAsInt();
                    if (version != SCHEMA_VERSION) {
                        throw new IllegalStateException("Unsupported cosmic bookmark schema " + version);
                    }
                    applyDocument(root);
                    loaded = true;
                } else {
                    migrateLegacy(root, readNativeFavorites(nativeFavorites));
                    backupLegacyFiles();
                    migrated = writeDocument();
                    if (!migrated) persistenceWritable = false;
                    loaded = true;
                }
            } catch (Exception exception) {
                persistenceWritable = false;
                CosmicCore.LOGGER.error("Failed to load cosmic bookmarks without replacing the source file", exception);
            }
        } else {
            try {
                backupLegacyFiles();
                List<BookmarkEntry> adopted = new ArrayList<>();
                for (JsonObject favorite : readNativeFavorites(nativeFavorites)) {
                    BookmarkEntry entry = parseLegacyEntry(favorite);
                    if (entry != null) adopted.add(entry);
                }
                adoptEntries(resolveLegacyRecipes(adopted));
                activeIndex = 0;
                migrated = writeDocument();
                if (!migrated) persistenceWritable = false;
                loaded = true;
            } catch (Exception exception) {
                persistenceWritable = false;
                CosmicCore.LOGGER.error("Failed to adopt EMI bookmarks without replacing the source file", exception);
            }
        }
        if (!loaded) {
            groups.clear();
            CosmicBookmarkGroup group = new CosmicBookmarkGroup("Default");
            for (EmiFavorite favorite : nativeFavorites) {
                StackEntry entry = stackEntryFromFavorite(favorite, UUID.randomUUID().toString());
                if (entry != null) group.getEntries().add(entry);
            }
            groups.add(group);
            activeIndex = 0;
        }
        if (migrated) {
            CosmicCore.LOGGER.info("Migrated CosmicCore EMI bookmarks to schema {}", SCHEMA_VERSION);
        }
        ready = true;
        revision++;
        projectActiveGroup();
    }

    public void captureNativeBeforeEmiSave() {
        if (!ready || projecting || saving || getActiveGroup().isRecipeGroup()) return;
        String fingerprint = fingerprint(EmiFavorites.favorites);
        if (fingerprint.equals(projectedFingerprint)) return;
        JsonObject before = serializeDocument();
        boolean discarded;
        try {
            CosmicBookmarkGroup group = getActiveGroup();
            boolean hadEntries = group.size() > 0;
            List<BookmarkEntry> replacements = captureNativeEntries(group);
            group.replaceEntries(replacements);
            discarded = hadEntries && discardEmptyGroup(group);
        } catch (RuntimeException exception) {
            applyDocument(before);
            projectActiveGroup();
            CosmicCore.LOGGER.error("Failed to capture EMI bookmark changes", exception);
            return;
        }
        if (!writeDocument()) {
            applyDocument(before);
            projectActiveGroup();
            return;
        }
        revision++;
        invalidateProjections();
        if (discarded) {
            projectActiveGroup();
        } else {
            projectedFingerprint = fingerprint;
        }
    }

    public Path getSavePath() {
        return Minecraft.getInstance().gameDirectory.toPath()
                .resolve("config")
                .resolve("cosmiccore")
                .resolve("cosmic_bookmarks.json");
    }

    private void mutateAndCommit(Runnable mutation) {
        if (!ready) return;
        captureNativeBeforeEmiSave();
        JsonObject before = serializeDocument();
        boolean committed = false;
        try {
            mutation.run();
            activeIndex = Math.clamp(activeIndex, 0, Math.max(0, groups.size() - 1));
            committed = writeDocument();
            if (committed) revision++;
        } catch (RuntimeException exception) {
            CosmicCore.LOGGER.error("Failed to update EMI bookmarks", exception);
        }
        if (!committed) {
            applyDocument(before);
        }
        projectActiveGroup();
        projecting = true;
        try {
            EmiPersistentData.save();
        } catch (RuntimeException exception) {
            CosmicCore.LOGGER.error("Failed to save EMI bookmark projection", exception);
        } finally {
            projecting = false;
        }
        EmiScreenManager.repopulatePanels(SidebarType.FAVORITES);
    }

    private int findOrCreateGroup(GroupType type) {
        if (getActiveGroup().getType() == type) return activeIndex;
        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i).getType() == type) return i;
        }
        String name = type == GroupType.RECIPE ? "Recipe " + (groups.size() + 1) : "Group " + (groups.size() + 1);
        groups.add(new CosmicBookmarkGroup(name, type));
        return groups.size() - 1;
    }

    private boolean discardEmptyGroup(CosmicBookmarkGroup group) {
        if (groups.size() <= 1 || group.size() > 0) return false;
        int index = groups.indexOf(group);
        if (index < 0) return false;
        groups.remove(index);
        if (activeIndex > index) {
            activeIndex--;
        } else if (activeIndex == index) {
            activeIndex = Math.min(index, groups.size() - 1);
        }
        return true;
    }

    private int findRecipe(CosmicBookmarkGroup group, @Nullable String recipeId, RecipeEntry candidate) {
        for (int i = 0; i < group.getEntries().size(); i++) {
            BookmarkEntry entry = group.getEntries().get(i);
            if (!(entry instanceof RecipeEntry recipeEntry)) continue;
            if (recipeId != null && recipeId.equals(recipeEntry.recipeId())) return i;
            if (recipeId == null && recipeEntry.recipeId() == null &&
                    entryKey(recipeEntry).equals(entryKey(candidate))) {
                return i;
            }
        }
        return -1;
    }

    private RecipeEntry createRecipeEntry(EmiRecipe recipe) {
        List<IngredientSnapshot> inputs = mergeSnapshots(snapshots(recipe.getInputs()), false);
        List<IngredientSnapshot> catalysts = mergeSnapshots(snapshots(recipe.getCatalysts()), true);
        List<IngredientSnapshot> outputs = mergeSnapshots(snapshots(new ArrayList<>(recipe.getOutputs())), false);
        IngredientSnapshot selectedOutput = selectOutput(snapshot(recipe.getOutputs().get(0)), outputs);
        String recipeId = recipe.getId() == null ? null : recipe.getId().toString();
        return new RecipeEntry(recipeId, selectedOutput, inputs, catalysts, outputs);
    }

    private RecipeSidebarProjection buildRecipeProjection(int pageSize, int[] widths,
                                                          IdentityHashMap<EmiIngredient, String> entryIds) {
        if (!getActiveGroup().isRecipeGroup() || pageSize <= 0) return new RecipeSidebarProjection(List.of());
        List<EmiIngredient> cells = new ArrayList<>();
        for (BookmarkEntry entry : getActiveGroup().getEntries()) {
            padToRow(cells, pageSize, widths);
            if (entry instanceof RecipeEntry recipeEntry) {
                CosmicRecipeFavorite output = (CosmicRecipeFavorite) runtimeFavorites.get(recipeEntry.id());
                if (output == null) continue;
                List<EmiIngredient> recipeCells = new ArrayList<>();
                recipeCells.add(output);
                for (CosmicRecipeFavorite.InputEntry input : output.getInputs()) {
                    recipeCells.add(new CosmicRecipeCell(
                            input.stack(),
                            input.amount(),
                            output.getRecipe()));
                }
                for (IngredientSnapshot catalyst : recipeEntry.catalysts()) {
                    recipeCells.add(new CosmicRecipeCell(
                            deserialize(catalyst.ingredient(), 1L),
                            1,
                            output.getRecipe()));
                }
                for (int i = 1; i < recipeEntry.outputs().size(); i++) {
                    IngredientSnapshot secondaryOutput = recipeEntry.outputs().get(i);
                    long amount = multiply(secondaryOutput.amount(), recipeEntry.batches());
                    recipeCells.add(new CosmicRecipeCell(
                            deserialize(secondaryOutput.ingredient(), amount),
                            amount,
                            output.getRecipe()));
                }
                int pageOffset = cells.size() % pageSize;
                if (recipeCells.size() <= pageSize && pageOffset > 0 && recipeCells.size() > pageSize - pageOffset) {
                    padToPage(cells, pageSize);
                }
                for (int i = 0; i < recipeCells.size(); i++) {
                    if (i > 0 && cells.size() % pageSize == 0 && pageSize > 1) {
                        cells.add(output);
                        entryIds.put(output, recipeEntry.id());
                    }
                    EmiIngredient cell = recipeCells.get(i);
                    cells.add(cell);
                    entryIds.put(cell, recipeEntry.id());
                }
            } else {
                EmiFavorite favorite = runtimeFavorites.get(entry.id());
                if (favorite != null) {
                    cells.add(favorite);
                    entryIds.put(favorite, entry.id());
                }
            }
            padToRow(cells, pageSize, widths);
        }
        return new RecipeSidebarProjection(List.copyOf(cells));
    }

    private static void padToRow(List<EmiIngredient> cells, int pageSize, int[] widths) {
        int offset = cells.size() % pageSize;
        if (offset == 0) return;
        int rowEnd = 0;
        for (int width : widths) {
            rowEnd += width;
            if (offset == rowEnd) return;
            if (offset < rowEnd) {
                while (offset++ < rowEnd) cells.add(EmiStack.EMPTY);
                return;
            }
        }
        padToPage(cells, pageSize);
    }

    private static void padToPage(List<EmiIngredient> cells, int pageSize) {
        while (cells.size() % pageSize != 0) cells.add(EmiStack.EMPTY);
    }

    private void projectActiveGroup() {
        invalidateProjections();
        runtimeEntryIds.clear();
        runtimeFavorites.clear();
        List<EmiFavorite> favorites = new ArrayList<>();
        for (BookmarkEntry entry : getActiveGroup().getEntries()) {
            EmiFavorite favorite;
            try {
                favorite = resolveFavorite(entry);
            } catch (RuntimeException exception) {
                CosmicCore.LOGGER.error("Failed to resolve EMI bookmark entry {}", entry.id(), exception);
                continue;
            }
            if (favorite == null) continue;
            favorites.add(favorite);
            runtimeFavorites.put(entry.id(), favorite);
            runtimeEntryIds.put(favorite, entry.id());
        }
        projecting = true;
        try {
            EmiFavorites.favorites.clear();
            EmiFavorites.favorites.addAll(favorites);
        } finally {
            projecting = false;
        }
        projectedFingerprint = fingerprint(EmiFavorites.favorites);
    }

    private @Nullable EmiFavorite resolveFavorite(BookmarkEntry entry) {
        if (entry instanceof StackEntry stackEntry) {
            EmiIngredient ingredient = deserialize(stackEntry.ingredient(), null);
            EmiRecipe recipe = resolveRecipe(stackEntry.recipeId());
            if (stackEntry.amountOverride() != null) {
                return new CosmicFavorite(ingredient, stackEntry.amountOverride(), recipe);
            }
            return new EmiFavorite(ingredient, recipe);
        }
        if (entry instanceof RecipeEntry recipeEntry) {
            EmiRecipe recipe = resolveRecipe(recipeEntry.recipeId());
            long batches = recipeEntry.batches();
            EmiIngredient output = deserialize(
                    recipeEntry.selectedOutput().ingredient(),
                    multiply(recipeEntry.selectedOutput().amount(), batches));
            List<CosmicRecipeFavorite.InputEntry> inputs = new ArrayList<>();
            for (IngredientSnapshot snapshot : recipeEntry.inputs()) {
                long amount = scaledInputAmount(snapshot, batches);
                inputs.add(new CosmicRecipeFavorite.InputEntry(deserialize(snapshot.ingredient(), amount), amount));
            }
            return new CosmicRecipeFavorite(
                    recipeEntry.id(),
                    recipeEntry.recipeId(),
                    output,
                    multiply(recipeEntry.selectedOutput().amount(), batches),
                    inputs,
                    recipe);
        }
        return null;
    }

    private EmiIngredient deserialize(JsonElement serialized, @Nullable Long amount) {
        EmiIngredient ingredient = EmiIngredientSerializer.getDeserialized(serialized);
        if (ingredient.isEmpty()) return missingIngredient();
        EmiIngredient copy = ingredient.copy();
        if (amount != null) copy.setAmount(Math.max(1, amount));
        return copy;
    }

    private EmiIngredient missingIngredient() {
        ItemStack stack = Items.BARRIER.getDefaultInstance();
        stack.set(DataComponents.CUSTOM_NAME,
                Component.translatable("cosmiccore.emi.bookmarks.ingredient_unavailable"));
        return EmiStack.of(stack);
    }

    private @Nullable EmiRecipe resolveRecipe(@Nullable String recipeId) {
        if (recipeId == null) return null;
        ResourceLocation id = ResourceLocation.tryParse(recipeId);
        return id == null ? null : EmiApi.getRecipeManager().getRecipe(id);
    }

    private void invalidateProjections() {
        projections.clear();
        projectedEntryIds.clear();
    }

    private void rebuildProjectedEntryIds() {
        projections.size();
        projectedEntryIds.clear();
        for (CachedProjection projection : projections.values()) {
            projectedEntryIds.putAll(projection.entryIds());
        }
    }

    private List<BookmarkEntry> captureNativeEntries(CosmicBookmarkGroup group) {
        Map<String, Deque<String>> reusableIds = new HashMap<>();
        Map<String, BookmarkEntry> originals = new HashMap<>();
        for (BookmarkEntry entry : group.getEntries()) {
            reusableIds.computeIfAbsent(entryKey(entry), ignored -> new ArrayDeque<>()).add(entry.id());
            originals.put(entry.id(), entry);
        }
        IdentityHashMap<EmiIngredient, String> priorIds = new IdentityHashMap<>(runtimeEntryIds);
        Set<String> originallyProjected = new HashSet<>(priorIds.values());
        Set<String> usedIds = new HashSet<>();
        List<BookmarkEntry> entries = new ArrayList<>();
        runtimeEntryIds.clear();
        for (EmiFavorite favorite : EmiFavorites.favorites) {
            String priorId = priorIds.get(favorite);
            BookmarkEntry original = priorId == null ? null : originals.get(priorId);
            if (original != null && usedIds.add(priorId)) {
                BookmarkEntry preserved = original;
                if (original instanceof StackEntry stackEntry && favorite instanceof CosmicFavorite cosmicFavorite) {
                    preserved = new StackEntry(
                            stackEntry.id(),
                            stackEntry.ingredient(),
                            cosmicFavorite.getAmount(),
                            stackEntry.recipeId());
                }
                entries.add(preserved);
                runtimeEntryIds.put(favorite, priorId);
                continue;
            }
            StackEntry probe = stackEntryFromFavorite(favorite, UUID.randomUUID().toString());
            if (probe == null) continue;
            Deque<String> ids = reusableIds.get(entryKey(probe));
            String id = probe.id();
            if (ids != null) {
                while (!ids.isEmpty() && usedIds.contains(ids.peekFirst())) ids.removeFirst();
                if (!ids.isEmpty()) id = ids.removeFirst();
            }
            usedIds.add(id);
            StackEntry entry = new StackEntry(id, probe.ingredient(), probe.amountOverride(), probe.recipeId());
            entries.add(entry);
            runtimeEntryIds.put(favorite, id);
        }
        for (BookmarkEntry original : group.getEntries()) {
            if (!originallyProjected.contains(original.id()) && usedIds.add(original.id())) {
                entries.add(original);
            }
        }
        return entries;
    }

    private @Nullable StackEntry stackEntryFromFavorite(EmiFavorite favorite, String id) {
        JsonElement serialized = EmiIngredientSerializer.getSerialized(favorite.getStack());
        if (serialized == null) return null;
        Long amount = favorite instanceof CosmicFavorite cosmicFavorite ? cosmicFavorite.getAmount() : null;
        String recipe = favorite.getRecipe() == null || favorite.getRecipe().getId() == null ? null :
                favorite.getRecipe().getId().toString();
        return new StackEntry(id, serialized, amount, recipe);
    }

    private void migrateLegacy(JsonObject root, List<JsonObject> nativeFavorites) {
        LegacyDocument legacy = parseLegacyDocument(root);
        List<CosmicBookmarkGroup> replacements = new ArrayList<>(legacy.groups());
        List<BookmarkEntry> nativeEntries = new ArrayList<>();
        for (JsonObject favorite : nativeFavorites) {
            BookmarkEntry entry = parseLegacyEntry(favorite);
            if (entry != null) nativeEntries.add(entry);
        }
        boolean needsRecipeIndex = nativeEntries.stream().anyMatch(CosmicBookmarkManager::needsLegacyRecipeInference);
        for (CosmicBookmarkGroup group : replacements) {
            if (group.getEntries().stream().anyMatch(CosmicBookmarkManager::needsLegacyRecipeInference)) {
                needsRecipeIndex = true;
                break;
            }
        }
        Map<String, LegacyRecipeMatch> recipeIndex = needsRecipeIndex ? buildLegacyRecipeIndex() : Map.of();
        for (CosmicBookmarkGroup group : replacements) {
            group.replaceEntries(resolveLegacyRecipes(group.getEntries(), recipeIndex));
        }
        nativeEntries = resolveLegacyRecipes(nativeEntries, recipeIndex);
        normalizeGroupLayouts(replacements);
        int replacementActiveIndex = Math.clamp(legacy.activeIndex(), 0, replacements.size() - 1);
        CosmicBookmarkGroup active = replacements.get(replacementActiveIndex);
        Map<String, Integer> unmatched = new HashMap<>();
        for (BookmarkEntry entry : active.getEntries()) unmatched.merge(legacyProjectionKey(entry), 1, Integer::sum);
        for (BookmarkEntry entry : nativeEntries) {
            String key = legacyProjectionKey(entry);
            int count = unmatched.getOrDefault(key, 0);
            if (count > 0) {
                unmatched.put(key, count - 1);
            } else {
                groupFor(replacements, entry).getEntries().add(entry);
            }
        }
        groups.clear();
        groups.addAll(replacements);
        activeIndex = replacementActiveIndex;
    }

    private LegacyDocument parseLegacyDocument(JsonObject root) {
        if (root.has("activeGroupId") || !root.has("activeGroup") ||
                !root.get("activeGroup").isJsonPrimitive() ||
                !root.get("activeGroup").getAsJsonPrimitive().isNumber() ||
                !root.has("groups") || !root.get("groups").isJsonArray() ||
                root.getAsJsonArray("groups").isEmpty()) {
            throw new IllegalStateException("Unrecognized legacy cosmic bookmark document");
        }
        List<CosmicBookmarkGroup> replacements = new ArrayList<>();
        for (JsonElement groupElement : root.getAsJsonArray("groups")) {
            if (!groupElement.isJsonObject()) throw new IllegalStateException("Invalid legacy cosmic bookmark group");
            JsonObject object = groupElement.getAsJsonObject();
            if (object.has("id") || object.has("layout") || object.has("entries") ||
                    !object.has("name") || !object.get("name").isJsonPrimitive() ||
                    !object.get("name").getAsJsonPrimitive().isString() ||
                    !object.has("type") || !object.get("type").isJsonPrimitive() ||
                    !object.get("type").getAsJsonPrimitive().isString() ||
                    !object.has("favorites") || !object.get("favorites").isJsonArray()) {
                throw new IllegalStateException("Incomplete legacy cosmic bookmark group");
            }
            GroupType type = parseLegacyGroupType(object);
            CosmicBookmarkGroup group = new CosmicBookmarkGroup(object.get("name").getAsString(), type);
            for (JsonElement favoriteElement : object.getAsJsonArray("favorites")) {
                if (!favoriteElement.isJsonObject()) {
                    throw new IllegalStateException("Invalid legacy cosmic bookmark entry");
                }
                JsonObject favorite = favoriteElement.getAsJsonObject();
                validateLegacyEntry(favorite);
                BookmarkEntry entry = parseLegacyEntry(favorite);
                if (entry == null) throw new IllegalStateException("Incomplete legacy cosmic bookmark entry");
                group.getEntries().add(entry);
            }
            replacements.add(group);
        }
        return new LegacyDocument(replacements, root.get("activeGroup").getAsInt());
    }

    private static void validateLegacyEntry(JsonObject object) {
        if (!object.has("stack") || object.get("stack").isJsonNull()) {
            throw new IllegalStateException("Legacy cosmic bookmark entry has no stack");
        }
        validateLegacyString(object, "recipe");
        validateLegacyString(object, "cosmicType");
        validateLegacyBoolean(object, "isRecipe");
        validateLegacyNumber(object, "outputAmount");
        validateLegacyNumber(object, "cosmicOutputAmount");
        validateLegacyNumber(object, "cosmicAmount");
        validateLegacyInputs(object, "inputs");
        validateLegacyInputs(object, "cosmicInputs");
        boolean recipe = object.has("isRecipe") && object.get("isRecipe").getAsBoolean() ||
                object.has("cosmicType") && "recipe".equals(object.get("cosmicType").getAsString());
        if (recipe && !object.has("outputAmount") && !object.has("cosmicOutputAmount")) {
            throw new IllegalStateException("Legacy recipe bookmark has no output amount");
        }
        if (recipe && !object.has("inputs") && !object.has("cosmicInputs")) {
            throw new IllegalStateException("Legacy recipe bookmark has no inputs");
        }
    }

    private static void validateLegacyString(JsonObject object, String key) {
        if (object.has(key) && (!object.get(key).isJsonPrimitive() ||
                !object.get(key).getAsJsonPrimitive().isString())) {
            throw new IllegalStateException("Invalid legacy cosmic bookmark " + key);
        }
    }

    private static void validateLegacyBoolean(JsonObject object, String key) {
        if (object.has(key) && (!object.get(key).isJsonPrimitive() ||
                !object.get(key).getAsJsonPrimitive().isBoolean())) {
            throw new IllegalStateException("Invalid legacy cosmic bookmark " + key);
        }
    }

    private static void validateLegacyNumber(JsonObject object, String key) {
        if (object.has(key) && (!object.get(key).isJsonPrimitive() ||
                !object.get(key).getAsJsonPrimitive().isNumber())) {
            throw new IllegalStateException("Invalid legacy cosmic bookmark " + key);
        }
    }

    private static void validateLegacyInputs(JsonObject object, String key) {
        if (!object.has(key)) return;
        if (!object.get(key).isJsonArray()) {
            throw new IllegalStateException("Invalid legacy cosmic bookmark " + key);
        }
        for (JsonElement element : object.getAsJsonArray(key)) {
            if (!element.isJsonObject()) throw new IllegalStateException("Invalid legacy recipe input");
            JsonObject input = element.getAsJsonObject();
            if (!input.has("stack") || input.get("stack").isJsonNull()) {
                throw new IllegalStateException("Legacy recipe input has no stack");
            }
            validateLegacyNumber(input, "amount");
        }
    }

    private void adoptEntries(List<BookmarkEntry> entries) {
        groups.clear();
        List<BookmarkEntry> regular = entries.stream().filter(StackEntry.class::isInstance).toList();
        List<BookmarkEntry> recipes = entries.stream().filter(RecipeEntry.class::isInstance).toList();
        if (!regular.isEmpty() || recipes.isEmpty()) {
            groups.add(new CosmicBookmarkGroup(
                    UUID.randomUUID().toString(),
                    "Default",
                    GroupType.REGULAR,
                    regular));
        }
        if (!recipes.isEmpty()) {
            groups.add(new CosmicBookmarkGroup(
                    UUID.randomUUID().toString(),
                    "Recipe " + (groups.size() + 1),
                    GroupType.RECIPE,
                    recipes));
        }
    }

    private static void normalizeGroupLayouts(List<CosmicBookmarkGroup> targetGroups) {
        List<BookmarkEntry> misplaced = new ArrayList<>();
        for (CosmicBookmarkGroup group : targetGroups) {
            List<BookmarkEntry> matching = group.getEntries().stream()
                    .filter(entry -> group.isRecipeGroup() == (entry instanceof RecipeEntry))
                    .toList();
            if (matching.size() != group.size()) {
                for (BookmarkEntry entry : group.getEntries()) {
                    if (!matching.contains(entry)) misplaced.add(entry);
                }
                group.replaceEntries(matching);
            }
        }
        for (BookmarkEntry entry : misplaced) groupFor(targetGroups, entry).getEntries().add(entry);
    }

    private static CosmicBookmarkGroup groupFor(List<CosmicBookmarkGroup> targetGroups, BookmarkEntry entry) {
        GroupType type = entry instanceof RecipeEntry ? GroupType.RECIPE : GroupType.REGULAR;
        for (CosmicBookmarkGroup group : targetGroups) {
            if (group.getType() == type) return group;
        }
        CosmicBookmarkGroup group = new CosmicBookmarkGroup(
                type == GroupType.RECIPE ? "Recipe " + (targetGroups.size() + 1) :
                        "Group " + (targetGroups.size() + 1),
                type);
        targetGroups.add(group);
        return group;
    }

    private GroupType parseLegacyGroupType(JsonObject object) {
        return GroupType.valueOf(object.get("type").getAsString());
    }

    private @Nullable BookmarkEntry parseLegacyEntry(JsonObject object) {
        if (!object.has("stack")) return null;
        JsonElement stack = object.get("stack").deepCopy();
        String recipeId = object.has("recipe") ? object.get("recipe").getAsString() : null;
        if ((object.has("isRecipe") && object.get("isRecipe").getAsBoolean()) ||
                (object.has("cosmicType") && "recipe".equals(object.get("cosmicType").getAsString()))) {
            long outputAmount = object.has("outputAmount") ? object.get("outputAmount").getAsLong() :
                    object.has("cosmicOutputAmount") ? object.get("cosmicOutputAmount").getAsLong() : 1;
            List<IngredientSnapshot> inputs = new ArrayList<>();
            JsonArray inputArray = object.has("inputs") ? object.getAsJsonArray("inputs") :
                    object.has("cosmicInputs") ? object.getAsJsonArray("cosmicInputs") : new JsonArray();
            for (JsonElement inputElement : inputArray) {
                if (!inputElement.isJsonObject()) continue;
                JsonObject input = inputElement.getAsJsonObject();
                if (!input.has("stack")) continue;
                long amount = input.has("amount") ? input.get("amount").getAsLong() : 1;
                inputs.add(new IngredientSnapshot(input.get("stack"), amount));
            }
            IngredientSnapshot output = new IngredientSnapshot(stack, outputAmount);
            return new RecipeEntry(recipeId, output, mergeSnapshots(inputs, false), List.of(), List.of(output));
        }
        Long amount = object.has("cosmicAmount") ? object.get("cosmicAmount").getAsLong() : null;
        return new StackEntry(stack, amount, recipeId);
    }

    private List<BookmarkEntry> resolveLegacyRecipes(List<BookmarkEntry> entries) {
        if (entries.stream().noneMatch(CosmicBookmarkManager::needsLegacyRecipeInference)) {
            return List.copyOf(entries);
        }
        return resolveLegacyRecipes(entries, buildLegacyRecipeIndex());
    }

    private static List<BookmarkEntry> resolveLegacyRecipes(
                                                            List<BookmarkEntry> entries,
                                                            Map<String, LegacyRecipeMatch> recipeIndex) {
        List<BookmarkEntry> resolved = new ArrayList<>(entries.size());
        for (BookmarkEntry entry : entries) resolved.add(inferRecipe(entry, recipeIndex));
        return List.copyOf(resolved);
    }

    private Map<String, LegacyRecipeMatch> buildLegacyRecipeIndex() {
        Map<String, LegacyRecipeMatch> matches = new HashMap<>();
        for (EmiRecipe recipe : EmiApi.getRecipeManager().getRecipes()) {
            if (recipe.getId() == null || recipe.getOutputs().isEmpty()) continue;
            try {
                String signature = legacyRecipeSignature(
                        snapshot(recipe.getOutputs().get(0)),
                        snapshots(recipe.getInputs()));
                String recipeId = recipe.getId().toString();
                matches.compute(signature, (key, existing) -> existing == null ?
                        new LegacyRecipeMatch(recipeId, false) :
                        new LegacyRecipeMatch(existing.recipeId(), true));
            } catch (RuntimeException exception) {
                CosmicCore.LOGGER.debug("Skipped unserializable EMI recipe {} during bookmark migration",
                        recipe.getId());
            }
        }
        return Map.copyOf(matches);
    }

    private static boolean needsLegacyRecipeInference(BookmarkEntry entry) {
        return entry instanceof RecipeEntry recipeEntry && recipeEntry.recipeId() == null;
    }

    private static BookmarkEntry inferRecipe(
                                             BookmarkEntry entry,
                                             Map<String, LegacyRecipeMatch> recipeIndex) {
        if (!(entry instanceof RecipeEntry recipeEntry) || recipeEntry.recipeId() != null) return entry;
        LegacyRecipeMatch match = recipeIndex.get(legacyRecipeSignature(
                recipeEntry.selectedOutput(),
                recipeEntry.inputs()));
        if (match == null || match.ambiguous()) return entry;
        return new RecipeEntry(
                recipeEntry.id(),
                match.recipeId(),
                recipeEntry.selectedOutput(),
                recipeEntry.inputs(),
                recipeEntry.catalysts(),
                recipeEntry.outputs(),
                recipeEntry.batches());
    }

    private static String legacyRecipeSignature(
                                                IngredientSnapshot output,
                                                List<IngredientSnapshot> inputs) {
        StringBuilder builder = new StringBuilder();
        appendSignaturePart(builder, snapshotKey(output));
        builder.append(inputs.size()).append(':');
        for (IngredientSnapshot input : inputs) appendSignaturePart(builder, snapshotKey(input));
        return builder.toString();
    }

    private static void appendSignaturePart(StringBuilder builder, String part) {
        builder.append(part.length()).append(':').append(part);
    }

    private List<JsonObject> readNativeFavorites(List<EmiFavorite> runtimeFavorites) {
        Path path = Minecraft.getInstance().gameDirectory.toPath().resolve("emi.json");
        if (Files.exists(path)) {
            try {
                JsonObject root = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
                if (root.has("favorites") && root.get("favorites").isJsonArray()) {
                    List<JsonObject> result = new ArrayList<>();
                    for (JsonElement element : root.getAsJsonArray("favorites")) {
                        if (element.isJsonObject()) result.add(element.getAsJsonObject());
                    }
                    return result;
                }
            } catch (Exception exception) {
                CosmicCore.LOGGER.warn("Failed to read EMI favorites during bookmark migration", exception);
            }
        }
        List<JsonObject> result = new ArrayList<>();
        for (EmiFavorite favorite : runtimeFavorites) {
            JsonElement stack = EmiIngredientSerializer.getSerialized(favorite.getStack());
            if (stack == null) continue;
            JsonObject object = new JsonObject();
            object.add("stack", stack);
            if (favorite.getRecipe() != null && favorite.getRecipe().getId() != null) {
                object.addProperty("recipe", favorite.getRecipe().getId().toString());
            }
            result.add(object);
        }
        return result;
    }

    private void backupLegacyFiles() throws IOException {
        Path savePath = getSavePath();
        Path backupDirectory = savePath.getParent().resolve("backups");
        Files.createDirectories(backupDirectory);
        Path cosmicBackup = backupDirectory.resolve("cosmic_bookmarks.v1.json");
        if (Files.exists(savePath) && !Files.exists(cosmicBackup)) {
            Files.copy(savePath, cosmicBackup, StandardCopyOption.COPY_ATTRIBUTES);
        }
        Path emiPath = Minecraft.getInstance().gameDirectory.toPath().resolve("emi.json");
        Path emiBackup = backupDirectory.resolve("emi.v1.json");
        if (Files.exists(emiPath) && !Files.exists(emiBackup)) {
            Files.copy(emiPath, emiBackup, StandardCopyOption.COPY_ATTRIBUTES);
        }
    }

    private JsonObject serializeDocument() {
        JsonObject root = new JsonObject();
        root.addProperty("schemaVersion", SCHEMA_VERSION);
        root.addProperty("activeGroupId", getActiveGroup().getId());
        JsonArray groupArray = new JsonArray();
        for (CosmicBookmarkGroup group : groups) {
            JsonObject groupObject = new JsonObject();
            groupObject.addProperty("id", group.getId());
            groupObject.addProperty("name", group.getName());
            groupObject.addProperty("layout", group.isRecipeGroup() ? "recipe_rows" : "grid");
            JsonArray entries = new JsonArray();
            for (BookmarkEntry entry : group.getEntries()) entries.add(serializeEntry(entry));
            groupObject.add("entries", entries);
            groupArray.add(groupObject);
        }
        root.add("groups", groupArray);
        return root;
    }

    private JsonObject serializeEntry(BookmarkEntry entry) {
        JsonObject object = new JsonObject();
        object.addProperty("id", entry.id());
        if (entry instanceof StackEntry stackEntry) {
            object.addProperty("kind", "stack");
            object.add("ingredient", stackEntry.ingredient());
            if (stackEntry.amountOverride() != null) {
                object.addProperty("amountOverride", stackEntry.amountOverride());
            }
            if (stackEntry.recipeId() != null) object.addProperty("recipeId", stackEntry.recipeId());
        } else if (entry instanceof RecipeEntry recipeEntry) {
            object.addProperty("kind", "recipe");
            if (recipeEntry.recipeId() != null) object.addProperty("recipeId", recipeEntry.recipeId());
            object.addProperty("batches", recipeEntry.batches());
            object.add("selectedOutput", serializeSnapshot(recipeEntry.selectedOutput()));
            JsonObject snapshot = new JsonObject();
            snapshot.add("inputs", serializeSnapshots(recipeEntry.inputs()));
            snapshot.add("catalysts", serializeSnapshots(recipeEntry.catalysts()));
            snapshot.add("outputs", serializeSnapshots(recipeEntry.outputs()));
            object.add("snapshot", snapshot);
        }
        return object;
    }

    private void applyDocument(JsonObject root) {
        if (!root.has("activeGroupId") || !root.get("activeGroupId").isJsonPrimitive() ||
                !root.has("groups") || !root.get("groups").isJsonArray()) {
            throw new IllegalStateException("Invalid cosmic bookmark document");
        }
        List<CosmicBookmarkGroup> replacements = new ArrayList<>();
        Set<String> groupIds = new HashSet<>();
        Set<String> entryIds = new HashSet<>();
        String activeGroupId = root.get("activeGroupId").getAsString();
        for (JsonElement groupElement : root.getAsJsonArray("groups")) {
            if (!groupElement.isJsonObject()) throw new IllegalStateException("Invalid cosmic bookmark group");
            JsonObject object = groupElement.getAsJsonObject();
            if (!object.has("id") || !object.has("name") || !object.has("layout") ||
                    !object.has("entries") || !object.get("entries").isJsonArray()) {
                throw new IllegalStateException("Incomplete cosmic bookmark group");
            }
            String id = object.get("id").getAsString();
            if (!groupIds.add(id)) throw new IllegalStateException("Duplicate cosmic bookmark group id " + id);
            String name = object.get("name").getAsString();
            String layout = object.get("layout").getAsString();
            GroupType type = switch (layout) {
                case "grid" -> GroupType.REGULAR;
                case "recipe_rows" -> GroupType.RECIPE;
                default -> throw new IllegalStateException("Unknown cosmic bookmark layout " + layout);
            };
            List<BookmarkEntry> entries = new ArrayList<>();
            for (JsonElement entryElement : object.getAsJsonArray("entries")) {
                if (!entryElement.isJsonObject()) throw new IllegalStateException("Invalid cosmic bookmark entry");
                BookmarkEntry entry = parseEntry(entryElement.getAsJsonObject());
                if (groupTypeDoesNotMatchEntry(type, entry)) {
                    throw new IllegalStateException("Cosmic bookmark entry kind does not match group layout");
                }
                if (!entryIds.add(entry.id())) {
                    throw new IllegalStateException("Duplicate cosmic bookmark entry id " + entry.id());
                }
                entries.add(entry);
            }
            replacements.add(new CosmicBookmarkGroup(id, name, type, entries));
        }
        if (replacements.isEmpty() || !groupIds.contains(activeGroupId)) {
            throw new IllegalStateException("Invalid active cosmic bookmark group");
        }
        groups.clear();
        groups.addAll(replacements);
        activeIndex = 0;
        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i).getId().equals(activeGroupId)) {
                activeIndex = i;
                break;
            }
        }
    }

    private static boolean groupTypeDoesNotMatchEntry(GroupType type, BookmarkEntry entry) {
        return (type == GroupType.RECIPE) != (entry instanceof RecipeEntry);
    }

    private BookmarkEntry parseEntry(JsonObject object) {
        if (!object.has("id") || !object.has("kind")) {
            throw new IllegalStateException("Incomplete cosmic bookmark entry");
        }
        String id = object.get("id").getAsString();
        String kind = object.get("kind").getAsString();
        String recipeId = object.has("recipeId") ? object.get("recipeId").getAsString() : null;
        if ("stack".equals(kind)) {
            if (!object.has("ingredient")) throw new IllegalStateException("Stack bookmark has no ingredient");
            Long amount = object.has("amountOverride") ? object.get("amountOverride").getAsLong() : null;
            return new StackEntry(id, object.get("ingredient"), amount, recipeId);
        }
        if ("recipe".equals(kind)) {
            if (!object.has("selectedOutput") || !object.has("snapshot") ||
                    !object.get("snapshot").isJsonObject()) {
                throw new IllegalStateException("Recipe bookmark has no snapshot");
            }
            IngredientSnapshot selected = parseSnapshot(object.getAsJsonObject("selectedOutput"));
            long batches = object.has("batches") ? object.get("batches").getAsLong() : 1;
            JsonObject snapshot = object.getAsJsonObject("snapshot");
            List<IngredientSnapshot> outputs = mergeSnapshots(parseSnapshots(snapshot, "outputs"), false);
            return new RecipeEntry(
                    id,
                    recipeId,
                    selectOutput(selected, outputs),
                    mergeSnapshots(parseSnapshots(snapshot, "inputs"), false),
                    mergeSnapshots(parseSnapshots(snapshot, "catalysts"), true),
                    outputs,
                    batches);
        }
        throw new IllegalStateException("Unknown cosmic bookmark entry kind " + kind);
    }

    private boolean writeDocument() {
        if (!persistenceWritable) return false;
        if (saving) return true;
        saving = true;
        Path path = getSavePath();
        Path temp = path.resolveSibling(path.getFileName() + ".tmp");
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(temp, GSON.toJson(serializeDocument()));
            try {
                Files.move(
                        temp,
                        path,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (Exception exception) {
            CosmicCore.LOGGER.error("Failed to save cosmic bookmarks", exception);
            try {
                Files.deleteIfExists(temp);
            } catch (IOException cleanupException) {
                exception.addSuppressed(cleanupException);
            }
            return false;
        } finally {
            saving = false;
        }
    }

    private static JsonObject serializeSnapshot(IngredientSnapshot snapshot) {
        JsonObject object = new JsonObject();
        object.add("ingredient", snapshot.ingredient());
        object.addProperty("amount", snapshot.amount());
        return object;
    }

    private static JsonArray serializeSnapshots(List<IngredientSnapshot> snapshots) {
        JsonArray array = new JsonArray();
        for (IngredientSnapshot snapshot : snapshots) array.add(serializeSnapshot(snapshot));
        return array;
    }

    private static IngredientSnapshot parseSnapshot(JsonObject object) {
        if (!object.has("ingredient")) throw new IllegalStateException("Bookmark snapshot has no ingredient");
        long amount = object.has("amount") ? object.get("amount").getAsLong() : 1;
        return new IngredientSnapshot(object.get("ingredient"), amount);
    }

    private static List<IngredientSnapshot> parseSnapshots(JsonObject object, String key) {
        if (!object.has(key) || !object.get(key).isJsonArray()) {
            throw new IllegalStateException("Bookmark recipe snapshot has no " + key);
        }
        List<IngredientSnapshot> result = new ArrayList<>();
        for (JsonElement element : object.getAsJsonArray(key)) {
            if (!element.isJsonObject()) throw new IllegalStateException("Invalid bookmark recipe snapshot");
            result.add(parseSnapshot(element.getAsJsonObject()));
        }
        return result;
    }

    private static IngredientSnapshot snapshot(EmiIngredient ingredient) {
        long amount = Math.max(1, ingredient.getAmount());
        JsonElement serialized = EmiIngredientSerializer.getSerialized(ingredient.copy().setAmount(1));
        if (serialized == null) throw new IllegalArgumentException("Cannot serialize empty EMI ingredient");
        return new IngredientSnapshot(serialized, amount);
    }

    private static List<IngredientSnapshot> snapshots(List<? extends EmiIngredient> ingredients) {
        List<IngredientSnapshot> result = new ArrayList<>();
        for (EmiIngredient ingredient : ingredients) {
            if (!ingredient.isEmpty()) result.add(snapshot(ingredient));
        }
        return result;
    }

    private static IngredientSnapshot normalizeSnapshot(IngredientSnapshot snapshot) {
        EmiIngredient ingredient = EmiIngredientSerializer.getDeserialized(snapshot.ingredient());
        if (ingredient.isEmpty()) return snapshot;
        JsonElement serialized = EmiIngredientSerializer.getSerialized(ingredient.copy().setAmount(1));
        return serialized == null ? snapshot : new IngredientSnapshot(serialized, snapshot.amount());
    }

    private static List<IngredientSnapshot> mergeSnapshots(List<IngredientSnapshot> snapshots,
                                                           boolean forceNonConsumable) {
        Map<String, IngredientSnapshot> merged = new LinkedHashMap<>();
        for (IngredientSnapshot original : snapshots) {
            IngredientSnapshot snapshot = normalizeSnapshot(original);
            String key = canonicalJson(snapshot.ingredient());
            IngredientSnapshot existing = merged.get(key);
            boolean nonConsumable = forceNonConsumable || isNonConsumable(snapshot);
            long amount = nonConsumable ? 1 : snapshot.amount();
            if (existing != null && !nonConsumable) {
                amount = saturatingAdd(existing.amount(), amount);
            }
            merged.put(key, new IngredientSnapshot(snapshot.ingredient(), amount));
        }
        return List.copyOf(merged.values());
    }

    private static IngredientSnapshot selectOutput(IngredientSnapshot selected,
                                                   List<IngredientSnapshot> outputs) {
        IngredientSnapshot normalized = normalizeSnapshot(selected);
        String key = canonicalJson(normalized.ingredient());
        for (IngredientSnapshot output : outputs) {
            if (canonicalJson(output.ingredient()).equals(key)) return output;
        }
        return normalized;
    }

    private static long scaledInputAmount(IngredientSnapshot snapshot, long batches) {
        return isNonConsumable(snapshot) ? 1 : multiply(snapshot.amount(), batches);
    }

    private static boolean isNonConsumable(IngredientSnapshot snapshot) {
        EmiIngredient ingredient = EmiIngredientSerializer.getDeserialized(snapshot.ingredient());
        if (ingredient.isEmpty()) return false;
        if (ingredient.getChance() <= 0) return true;
        boolean found = false;
        for (EmiStack stack : ingredient.getEmiStacks()) {
            if (stack.isEmpty()) continue;
            found = true;
            EmiStack remainder = stack.getRemainder();
            if (remainder.isEmpty() || !stack.getKey().equals(remainder.getKey())) return false;
        }
        return found;
    }

    private static String snapshotKey(IngredientSnapshot snapshot) {
        return canonicalJson(snapshot.ingredient()) + "@" + snapshot.amount();
    }

    private static String entryKey(BookmarkEntry entry) {
        if (entry instanceof StackEntry stackEntry) {
            return "stack|" + canonicalJson(stackEntry.ingredient()) + "|" + stackEntry.amountOverride() + "|" +
                    stackEntry.recipeId();
        }
        RecipeEntry recipeEntry = (RecipeEntry) entry;
        StringBuilder builder = new StringBuilder("recipe|")
                .append(recipeEntry.recipeId())
                .append('|')
                .append(snapshotKey(recipeEntry.selectedOutput()));
        for (IngredientSnapshot input : recipeEntry.inputs()) builder.append('|').append(snapshotKey(input));
        builder.append("|catalysts");
        for (IngredientSnapshot catalyst : recipeEntry.catalysts()) builder.append('|').append(snapshotKey(catalyst));
        builder.append("|outputs");
        for (IngredientSnapshot output : recipeEntry.outputs()) builder.append('|').append(snapshotKey(output));
        return builder.toString();
    }

    private static String legacyProjectionKey(BookmarkEntry entry) {
        if (entry instanceof StackEntry stackEntry) {
            return canonicalJson(stackEntry.ingredient()) + "|" + stackEntry.recipeId();
        }
        RecipeEntry recipeEntry = (RecipeEntry) entry;
        return canonicalJson(recipeEntry.selectedOutput().ingredient()) + "|" + recipeEntry.recipeId();
    }

    private static String canonicalJson(JsonElement element) {
        if (element == null || element.isJsonNull()) return "null";
        if (element.isJsonPrimitive()) return element.toString();
        if (element.isJsonArray()) {
            StringBuilder builder = new StringBuilder("[");
            boolean first = true;
            for (JsonElement child : element.getAsJsonArray()) {
                if (!first) builder.append(',');
                builder.append(canonicalJson(child));
                first = false;
            }
            return builder.append(']').toString();
        }
        JsonObject object = element.getAsJsonObject();
        List<String> keys = new ArrayList<>(object.keySet());
        keys.sort(String::compareTo);
        StringBuilder builder = new StringBuilder("{");
        for (int i = 0; i < keys.size(); i++) {
            if (i > 0) builder.append(',');
            String key = keys.get(i);
            builder.append(GSON.toJson(key)).append(':').append(canonicalJson(object.get(key)));
        }
        return builder.append('}').toString();
    }

    private static EmiIngredient unwrap(EmiIngredient ingredient) {
        return ingredient instanceof EmiFavorite favorite ? favorite.getStack() : ingredient;
    }

    private static long ingredientAmount(JsonElement serialized) {
        EmiIngredient ingredient = EmiIngredientSerializer.getDeserialized(serialized);
        return Math.max(1, ingredient.getAmount());
    }

    private static long multiply(long value, long multiplier) {
        if (value > 0 && multiplier > Long.MAX_VALUE / value) return Long.MAX_VALUE;
        return Math.max(1, value * multiplier);
    }

    private static long saturatingAdd(long value, long delta) {
        if (delta > 0 && value > Long.MAX_VALUE - delta) return Long.MAX_VALUE;
        if (delta < 0 && value < Long.MIN_VALUE - delta) return Long.MIN_VALUE;
        return value + delta;
    }

    private static String fingerprint(List<EmiFavorite> favorites) {
        JsonArray array = new JsonArray();
        for (EmiFavorite favorite : favorites) {
            JsonObject object = new JsonObject();
            JsonElement stack = EmiIngredientSerializer.getSerialized(favorite.getStack());
            if (stack == null) continue;
            object.add("stack", stack);
            if (favorite.getRecipe() != null && favorite.getRecipe().getId() != null) {
                object.addProperty("recipe", favorite.getRecipe().getId().toString());
            }
            if (favorite instanceof CosmicFavorite cosmicFavorite) {
                object.addProperty("amount", cosmicFavorite.getAmount());
            }
            if (favorite instanceof CosmicRecipeFavorite recipeFavorite) {
                object.addProperty("bookmark", recipeFavorite.getBookmarkId());
            }
            array.add(object);
        }
        return canonicalJson(array);
    }
}
