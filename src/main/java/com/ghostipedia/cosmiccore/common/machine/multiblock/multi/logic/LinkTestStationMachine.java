package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.capability.ILinkedMultiblock;
import com.ghostipedia.cosmiccore.api.machine.multiblock.LinkedWorkableElectricMultiblockMachine;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Set;

/**
 * Simple test multiblock for verifying cross-dimensional linking.
 * Displays linked partners in UI and logs link events.
 */
public class LinkTestStationMachine extends LinkedWorkableElectricMultiblockMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            LinkTestStationMachine.class,
            LinkedWorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    public LinkTestStationMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    // ==================== Link Configuration ====================

    @Override
    public LinkRole getLinkRole() {
        // Test station is a peer - can link bidirectionally
        return LinkRole.PEER;
    }

    @Override
    public int getMaxPartners() {
        // Allow up to 4 partners for testing
        return 4;
    }

    @Override
    public boolean canLinkTo(GlobalPos partner, ILinkedMultiblock partnerMachine) {
        // Accept links from any ILinkedMultiblock for testing
        return true;
    }

    // ==================== Link Lifecycle ====================

    @Override
    public void onLinkEstablished(GlobalPos partner) {
        super.onLinkEstablished(partner);
        CosmicCore.LOGGER.info("[LinkTestStation] Link established to {} in {}",
                partner.pos(), partner.dimension().location());
    }

    @Override
    public void onLinkBroken(GlobalPos partner) {
        super.onLinkBroken(partner);
        CosmicCore.LOGGER.info("[LinkTestStation] Link broken to {} in {}",
                partner.pos(), partner.dimension().location());
    }

    // ==================== Display ====================

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);

        if (!isFormed()) return;

        // Show link status
        Set<GlobalPos> partners = getLinkedPartners();
        if (partners.isEmpty()) {
            textList.add(Component.literal("No linked partners")
                    .withStyle(ChatFormatting.GRAY));
            textList.add(Component.literal("Use datastick to link")
                    .withStyle(ChatFormatting.DARK_GRAY));
        } else {
            textList.add(Component.literal("Linked Partners: " + partners.size())
                    .withStyle(ChatFormatting.GREEN));

            for (GlobalPos partner : partners) {
                String dim = partner.dimension().location().toString();
                String pos = String.format("[%d, %d, %d]",
                        partner.pos().getX(),
                        partner.pos().getY(),
                        partner.pos().getZ());

                // Show if partner is online
                boolean online = getPartnerMachine(partner) != null;
                ChatFormatting color = online ? ChatFormatting.GREEN : ChatFormatting.RED;
                String status = online ? "[+]" : "[-]";

                textList.add(Component.literal("  " + status + " " + dim + " " + pos)
                        .withStyle(color));
            }
        }

        // Show effective roles
        LinkRole myRole = getLinkRole();
        textList.add(Component.literal("Role: " + myRole.name())
                .withStyle(ChatFormatting.AQUA));
    }
}
