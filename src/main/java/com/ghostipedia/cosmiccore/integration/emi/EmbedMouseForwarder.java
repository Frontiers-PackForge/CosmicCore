package com.ghostipedia.cosmiccore.integration.emi;

public interface EmbedMouseForwarder {

    boolean cosmiccore$mouseScrolled(double scrollX, double scrollY);

    boolean cosmiccore$mouseDragged(int button, double dragX, double dragY);

    boolean cosmiccore$mouseReleased(int button);
}
