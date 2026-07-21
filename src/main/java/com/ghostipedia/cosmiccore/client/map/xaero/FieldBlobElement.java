package com.ghostipedia.cosmiccore.client.map.xaero;

import com.ghostipedia.cosmiccore.client.map.RevealedField;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record FieldBlobElement(ResourceKey<Level> dimension, RevealedField field) {}
