package com.infamous.dungeons_gear.entities;

import com.infamous.dungeons_gear.effects.CustomEffects;
import com.infamous.dungeons_libraries.entities.TotemBaseEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.IPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.List;

import static com.infamous.dungeons_gear.DungeonsGear.PROXY;
import static com.infamous.dungeons_libraries.utils.AreaOfEffectHelper.*;

public class TotemOfRegenerationEntity extends TotemBaseEntity implements IAnimatable {

    AnimationFactory factory = new AnimationFactory(this);

    public TotemOfRegenerationEntity(EntityType<?> p_i48580_1_, World p_i48580_2_) {
        super(p_i48580_1_, p_i48580_2_, 240, 2);
    }

    @Override
    protected void applyTotemEffect() {
        applyToNearbyEntities(getOwner(), 8,
                getCanHealPredicate(getOwner()), (LivingEntity nearbyEntity) -> {
                    EffectInstance effectInstance = new EffectInstance(Effects.REGENERATION, 10);
                    nearbyEntity.addEffect(new EffectInstance(effectInstance));
                }
        );
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "controller", 5, this::predicate));
    }

    private <P extends IAnimatable> PlayState predicate(AnimationEvent<P> event) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.totem_of_regeneration.spawn", true));
        return PlayState.CONTINUE;
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }
}
