package com.infamous.dungeons_gear.utilties;

import com.infamous.dungeons_gear.config.DungeonsGearConfig;
import com.infamous.dungeons_gear.enchantments.lists.ArmorEnchantmentList;
import com.infamous.dungeons_gear.enchantments.melee_ranged.DynamoEnchantment;
import com.infamous.dungeons_libraries.capabilities.minionmaster.Master;
import com.infamous.dungeons_libraries.summon.SummonHelper;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

import java.util.Optional;

import static com.infamous.dungeons_gear.registry.ItemRegistry.*;
import static com.infamous.dungeons_libraries.capabilities.minionmaster.MinionMasterHelper.getMasterCapability;

public class ArmorEffectHelper {
    public static void summonOrTeleportBat(Player playerEntity, Level world) {
        Master summonerCap = getMasterCapability(playerEntity);
        Optional<Entity> batPet = summonerCap.getSummonedMobs().stream().filter(entity -> entity.getType() == EntityType.BAT).findFirst();
        if(!batPet.isPresent()){
            SummonHelper.summonEntity(playerEntity, playerEntity.blockPosition(), EntityType.BAT);
            SoundHelper.playCreatureSound(playerEntity, SoundEvents.BAT_AMBIENT);
        } else{
            if(world instanceof ServerLevel){
                batPet.get().teleportToWithTicket(playerEntity.getX() + playerEntity.getEyeHeight(), playerEntity.getY() + playerEntity.getEyeHeight(), playerEntity.getZ() + playerEntity.getEyeHeight());
            }
        }
    }

    public static void teleportOnHit(LivingEntity livingEntity){
        Level world = livingEntity.getCommandSenderWorld();
        if (!world.isClientSide) {

            for(int i = 0; i < 16; ++i) {
                double teleportX = livingEntity.getX() + (livingEntity.getRandom().nextDouble() - 0.5D) * 16.0D;
                double teleportY = Mth.clamp(livingEntity.getY() + (double)(livingEntity.getRandom().nextInt(16) - 8), 0.0D, (double)(world.getHeight() - 1));
                double teleportZ = livingEntity.getZ() + (livingEntity.getRandom().nextDouble() - 0.5D) * 16.0D;
                if (livingEntity.isPassenger()) {
                    livingEntity.stopRiding();
                }

                if (livingEntity.randomTeleport(teleportX, teleportY, teleportZ, true)) {
                    SoundEvent soundEvent = livingEntity instanceof Fox ? SoundEvents.FOX_TELEPORT : SoundEvents.CHORUS_FRUIT_TELEPORT;
                    world.playSound((Player)null, livingEntity.blockPosition(), soundEvent, SoundSource.PLAYERS, 1.0F, 1.0F);
                    livingEntity.playSound(soundEvent, 1.0F, 1.0F);
                    break;
                }
            }
        }
    }

//    public static void handleInvulnerableJump(Player playerEntity, ItemStack helmet, ItemStack chestplate) {
//        boolean invulnerableJump = helmet.getItem() == SHADOW_WALKER_HOOD.get();
//        boolean invulnerableJump2 = chestplate.getItem() == SHADOW_WALKER.get();
//        boolean doInvulnerableJump = invulnerableJump || invulnerableJump2;
//
//        if (doInvulnerableJump) {
//            MobEffectInstance resistance = new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 4);
//            playerEntity.addEffect(resistance);
//        }
//    }
//
//    public static void handleJumpBoost(Player playerEntity, ItemStack helmet, ItemStack chestplate) {
//        float jumpBoost = helmet.getItem() == OCELOT_ARMOR_HOOD.get() || helmet.getItem() == SHADOW_WALKER_HOOD.get() ? 25 : 0;
//        float jumpBoost2 = chestplate.getItem() == OCELOT_ARMOR.get() || chestplate.getItem() == SHADOW_WALKER.get() ? 25 : 0;
//        float totalJumpBoost = jumpBoost * 0.002F + jumpBoost2 * 0.002F;
//
//        if (totalJumpBoost > 0) {
//            playerEntity.setDeltaMovement(playerEntity.getDeltaMovement().add(0, totalJumpBoost, 0));
//        }
//    }

    public static void handleJumpEnchantments(Player playerEntity, ItemStack helmet, ItemStack chestplate) {
        if (ModEnchantmentHelper.hasEnchantment(playerEntity, ArmorEnchantmentList.ELECTRIFIED)) {
            SoundHelper.playLightningStrikeSounds(playerEntity);
            AreaOfEffectHelper.electrifyNearbyEnemies(playerEntity, 5, 5, 3);
        }

        if (ModEnchantmentHelper.hasEnchantment(playerEntity, ArmorEnchantmentList.FIRE_TRAIL)) {
            int fireTrailLevel = EnchantmentHelper.getEnchantmentLevel(ArmorEnchantmentList.FIRE_TRAIL, playerEntity);
            AreaOfEffectHelper.burnNearbyEnemies(playerEntity, 1.0F * fireTrailLevel, 1.5F);
        }

        // TODO: Beenest Armor and Buzzynest Armor
        if (ModEnchantmentHelper.hasEnchantment(playerEntity, ArmorEnchantmentList.TUMBLEBEE)) {
            int tumblebeeLevel = EnchantmentHelper.getEnchantmentLevel(ArmorEnchantmentList.TUMBLEBEE, playerEntity);

            float tumblebeeRand = playerEntity.getRandom().nextFloat();
            if (tumblebeeRand <= DungeonsGearConfig.TUMBLE_BEE_CHANCE_PER_LEVEL.get() * tumblebeeLevel) {
                if(SummonHelper.summonEntity(playerEntity, playerEntity.blockPosition(), EntityType.BEE) != null) {
                    SoundHelper.playCreatureSound(playerEntity, SoundEvents.BEE_LOOP);
                }
            }
        }

        if (ModEnchantmentHelper.hasEnchantment(playerEntity, ArmorEnchantmentList.SWIFTFOOTED)) {
            int swiftfootedLevel = EnchantmentHelper.getEnchantmentLevel(ArmorEnchantmentList.SWIFTFOOTED, playerEntity);
            MobEffectInstance speedBoost = new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, swiftfootedLevel - 1);
            playerEntity.addEffect(speedBoost);
        }

        DynamoEnchantment.handleAddDynamoEnchantment(playerEntity);
    }


}
