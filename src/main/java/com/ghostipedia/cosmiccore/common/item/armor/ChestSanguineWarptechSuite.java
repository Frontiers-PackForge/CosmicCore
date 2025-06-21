package com.ghostipedia.cosmiccore.common.item.armor;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.item.armor.AdvancedQuarkTechSpaceSuite;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;
import com.gregtechceu.gtceu.core.IFireImmuneEntity;
import com.gregtechceu.gtceu.utils.input.KeyBind;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntList;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import java.util.List;

public class ChestSanguineWarptechSuite extends AdvancedQuarkTechSpaceSuite {

    public static final String SANGUINE_SHIELD_NBT_KEY = CosmicCore.MOD_ID + ":sanguine_shield";
    public static final int SANGUINE_SHIELD_DRAIN_PER_SECOND = 10;
    public static final int SECONDS_PER_UPDATE = 1;
    // A replacement for checking the current world time, to get around the gamerule that stops it
    private long timer = 0L;
    private List<Pair<NonNullList<ItemStack>, IntList>> inventoryIndexMap;

    public ChestSanguineWarptechSuite(int energyPerUse, long capacity, int tier) {
        super(energyPerUse, capacity, tier);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawHUD(ItemStack item, GuiGraphics guiGraphics) {
        addCapacityHUD(item, this.HUD);
        addSanguineHUD(item, this.HUD);
        this.HUD.draw(guiGraphics);
        this.HUD.reset();
    }

    @OnlyIn(Dist.CLIENT)
    protected static void addSanguineHUD(ItemStack stack, ArmorUtils.ModularHUD hud) {
        if (stack == null) return;
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains("currentLP")) {
            long currentLP = tag.getLong("currentLP");
            hud.newString(Component.translatable("cosmiccore.armor.sanguinewarptech.hud.LP", currentLP));
        }
        if (tag.contains("isSanguineShieldOn")) {
            boolean isSanguineShieldOn = tag.getBoolean("isSanguineShieldOn");
            hud.newString(Component.translatable("cosmiccore.armor.sanguinewarptech.hud.shieldstate",
                    (isSanguineShieldOn ? "ON" : "OFF")));
        }
    }

    // Mostly copied from AdvancedQuarkTechSpaceSuite.java
    @Override
    public void onArmorTick(Level world, Player player, ItemStack item) {
        IElectricItem cont = GTCapabilityHelper.getElectricItem(item);
        if (cont == null) {
            return;
        }

        CompoundTag data = item.getOrCreateTag();
        // Assume no tags exist if we don't see the toggleTimer tag
        if (!data.contains("toggleTimer")) {
            data.putByte("toggleTimer", (byte) 0);
            data.putBoolean("canShare", false);
            data.putBoolean("isSanguineShieldOn", false);
            data.putLong("currentLP", 0);
        }

        byte toggleTimer = data.getByte("toggleTimer");
        boolean canShare = data.getBoolean("canShare");

        // Handle toggle keypresses
        String messageKey = null;
        if (toggleTimer == 0) {
            if (KeyBind.ARMOR_CHARGING.isKeyDown(player)) {
                canShare = !canShare;
                if (canShare && cont.getCharge() == 0) { // Only allow for charging to be enabled if charge is nonzero
                    messageKey = "metaarmor.qts.share.error";
                    canShare = false;
                } else {
                    messageKey = "metaarmor.qts.share." + (canShare ? "enable" : "disable");
                }
                data.putBoolean("canShare", canShare);
            }

            if (messageKey != null) {
                toggleTimer = 5;
                if (!world.isClientSide) player.displayClientMessage(Component.translatable(messageKey), true);
            }
        }

        if (toggleTimer > 0) toggleTimer--;
        data.putByte("toggleTimer", toggleTimer);

        if (type == ArmorItem.Type.CHESTPLATE && !player.fireImmune()) {
            ((IFireImmuneEntity) player).gtceu$setFireImmune(true);
            if (player.isOnFire()) player.extinguishFire();
        }

        // Toggle flight
        Abilities abilities = player.getAbilities();
        if (!abilities.mayfly) {
            abilities.mayfly = true;
            if (!world.isClientSide && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundPlayerAbilitiesPacket(abilities));
            }
        }

        // Sanguine shield, update every 10 seconds
        if (!world.isClientSide && timer % (20 * SECONDS_PER_UPDATE) == 0) {
            SoulNetwork network = NetworkHelper.getSoulNetwork(player);
            boolean isSanguineShieldOn;
            if (network.getCurrentEssence() < SANGUINE_SHIELD_DRAIN_PER_SECOND * SECONDS_PER_UPDATE) {
                isSanguineShieldOn = false;
            } else {
                network.syphon(new SoulTicket(SANGUINE_SHIELD_DRAIN_PER_SECOND * SECONDS_PER_UPDATE));
                isSanguineShieldOn = true;
            }

            data.putBoolean("isSanguineShieldOn", isSanguineShieldOn);
            data.putLong("currentLP", network.getCurrentEssence());
            player.getPersistentData().putBoolean(SANGUINE_SHIELD_NBT_KEY, isSanguineShieldOn);
        }

        // Charging mechanics
        if (canShare && !world.isClientSide) {
            // Check for new things to charge every 5 seconds
            if (timer % 100 == 0)
                inventoryIndexMap = ArmorUtils.getChargeableItem(player, cont.getTier());

            if (inventoryIndexMap != null && !inventoryIndexMap.isEmpty()) {
                // Charge all inventory slots
                for (int i = 0; i < inventoryIndexMap.size(); i++) {
                    Pair<NonNullList<ItemStack>, IntList> inventoryMap = inventoryIndexMap.get(i);
                    var inventoryIterator = inventoryMap.getSecond().iterator();
                    while (inventoryIterator.hasNext()) {
                        int slot = inventoryIterator.nextInt();
                        IElectricItem chargable = GTCapabilityHelper.getElectricItem(inventoryMap.getFirst().get(slot));

                        // Safety check the null, it should not actually happen. Also don't try and charge itself
                        if (chargable == null || chargable == cont) {
                            inventoryIterator.remove();
                            continue;
                        }

                        long attemptedChargeAmount = chargable.getTransferLimit() * 10;

                        // Accounts for tick differences when charging items
                        if (chargable.getCharge() < chargable.getMaxCharge() && cont.canUse(attemptedChargeAmount) &&
                                timer % 10 == 0) {
                            long delta = chargable.charge(attemptedChargeAmount, cont.getTier(), true, false);
                            if (delta > 0) {
                                cont.discharge(delta, cont.getTier(), true, false, false);
                            }
                            if (chargable.getCharge() == chargable.getMaxCharge()) {
                                inventoryIterator.remove();
                            }
                            player.inventoryMenu.sendAllDataToRemote();
                        }
                    }

                    if (inventoryMap.getSecond().isEmpty())
                        inventoryIndexMap.remove(inventoryMap);
                }
            }
        }

        timer++;
        if (timer == Long.MAX_VALUE)
            timer = 0;
    }

    @Override
    public int damageArmor(LivingEntity entity, ItemStack itemStack, DamageSource source, int damage,
                           EquipmentSlot equipmentSlot) {
        IElectricItem item = GTCapabilityHelper.getElectricItem(itemStack);
        if (item != null) {
            item.discharge(energyPerUse / 100L * damage, item.getTier(), true, false, false);
        }
        return 1;
    }
}
