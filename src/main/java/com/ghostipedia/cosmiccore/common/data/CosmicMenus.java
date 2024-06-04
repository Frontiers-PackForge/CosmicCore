package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.client.gui.screen.ReforgingScreen;
import com.ghostipedia.cosmiccore.common.gui.ReforgingMenu;
import com.tterrag.registrate.util.entry.MenuEntry;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;

public class CosmicMenus {
    public static final MenuEntry<ReforgingMenu> GOOD_REFORING_MENU = REGISTRATE.<ReforgingMenu,ReforgingScreen>menu("good_reforging_menu",ReforgingMenu::new, () -> ReforgingScreen::new)
            .register();
}
