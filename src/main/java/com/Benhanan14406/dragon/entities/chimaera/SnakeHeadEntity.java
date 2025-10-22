package com.Benhanan14406.dragon.entities.chimaera;

import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class SnakeHeadEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private LionEntity parent;

    public SnakeHeadEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 16.0F)
                .add(Attributes.MOVEMENT_SPEED, 0.19F)
                .add(Attributes.ATTACK_DAMAGE, 4.0F)
                .add(Attributes.FOLLOW_RANGE, 20.0F);
    }

    public void setParent(LionEntity parent) {
        this.parent = parent;
    }

    @Override
    public void tick() {
        super.tick();
        if (parent == null || !parent.isAlive()) {
            this.discard();
            return;
        }

        // detect sneaky attackers behind lion
        LivingEntity target = this.level().getNearestPlayer(this, 4);
        if (target != null && isBehindParent(target)) {
            bite(target);
        }
    }

    private boolean isBehindParent(LivingEntity target) {
        double dx = target.getX() - parent.getX();
        double dz = target.getZ() - parent.getZ();
        float angleToTarget = (float)(Mth.atan2(dz, dx) * (180F / Math.PI)) - parent.getYRot();
        return angleToTarget > 130 || angleToTarget < -130; // ~100° cone
    }

    private void bite(LivingEntity target) {
        target.hurt(damageSources().mobAttack(this), 4.0F);
        target.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 1));
        target.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("controller", 5, state ->
                state.setAndContinue(RawAnimation.begin().thenLoop("idle"))
        ));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
}

