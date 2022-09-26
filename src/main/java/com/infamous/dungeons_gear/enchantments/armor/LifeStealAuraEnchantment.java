package com.infamous.dungeons_gear.enchantments.armor;

import com.infamous.dungeons_gear.config.DungeonsGearConfig;
import com.infamous.dungeons_gear.effects.CustomEffects;
import com.infamous.dungeons_gear.enchantments.ModEnchantmentTypes;
import com.infamous.dungeons_gear.enchantments.types.PulseEnchantment;
import com.infamous.dungeons_libraries.utils.AbilityHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.infamous.dungeons_gear.DungeonsGear.MODID;
import static com.infamous.dungeons_gear.enchantments.lists.ArmorEnchantmentList.LIFE_STEAL_AURA;
import static com.infamous.dungeons_libraries.utils.AreaOfEffectHelper.applyToNearbyEntities;

@Mod.EventBusSubscriber(modid= MODID)
public class LifeStealAuraEnchantment extends PulseEnchantment {

    public LifeStealAuraEnchantment() {
        super(Rarity.RARE, ModEnchantmentTypes.ARMOR, new EquipmentSlot[]{
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET});
    }

    public int getMaxLevel() {
        return 2;
    }

    @Override
    public boolean checkCompatibility(Enchantment enchantment) {
        return DungeonsGearConfig.ENABLE_OVERPOWERED_ENCHANTMENT_COMBOS.get() || !(enchantment instanceof PulseEnchantment);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event){
        Player player = event.player;
        if(player == null || player.isSpectator()) return;
        if(event.phase == TickEvent.Phase.START) return;
        if(player.isAlive()&&player.isEffectiveAi()){
            apply(player);
        }
    }

    @SubscribeEvent
    public static void onLivingEntityTick(LivingEvent.LivingUpdateEvent event){
        LivingEntity livingEntity = event.getEntityLiving();
        if(livingEntity == null || livingEntity instanceof Player) return;
        if(livingEntity.isAlive() && livingEntity.isEffectiveAi()){
            apply(livingEntity);
        }
    }

    private static void apply(LivingEntity entity) {
//        DualWield comboCap = DualWieldHelper.getDualWieldCapability(entity);
//        if(comboCap == null) return;
//        int burnNearbyTimer = comboCap.getBurnNearbyTimer();

        int enchantmentLevel = EnchantmentHelper.getEnchantmentLevel(LIFE_STEAL_AURA, entity);
        if(enchantmentLevel > 0){
//            if(burnNearbyTimer <= 0){
            applyToNearbyEntities(entity, 5,
                    (nearbyEntity) -> {
                        return AbilityHelper.isAlly(entity, nearbyEntity);
                    }, (LivingEntity nearbyEntity) -> {
                        MobEffectInstance speedBoost = new MobEffectInstance(CustomEffects.LIFE_STEAL, 20, enchantmentLevel - 1);
                        nearbyEntity.addEffect(speedBoost);
//                        PROXY.spawnParticles(nearbyEntity, ParticleTypes.FLAME);
                    }
            );
//                comboCap.setBurnNearbyTimer(10);
        }
//        else{
//            if(burnNearbyTimer != 10){
//                comboCap.setBurnNearbyTimer(10);
//            }
//        }
    }

}
