package com.ghostipedia.cosmiccore.api.item.component;

import com.gregtechceu.gtceu.api.item.component.IItemComponent;

import com.lowdragmc.lowdraglib.client.renderer.IRenderer;

// Replaces GTCEu's removed api.item.component.ICustomRenderer (8.0.0). LDLib IRenderer swap pending cosmiccore-11.
@FunctionalInterface
public interface ICustomRenderer extends IItemComponent {

    IRenderer getRenderer();
}
