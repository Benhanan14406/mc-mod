package com.Benhanan14406.dragon.entities.chimaera;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class GoatHeadEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private LionEntity parent;
    private int fireCooldown = 0;

    public GoatHeadEntity(EntityType<? extends PathfinderMob> type, Level level) {
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

        // independent targeting
        if (fireCooldown-- <= 0) {
            LivingEntity target = this.level().getNearestPlayer(this, 12);
            if (target != null && target != parent.getTarget()) {
                shootFireballAt(target);
                fireCooldown = 80;
            }
        }
    }

    private void shootFireballAt(LivingEntity target) {
        double dx = target.getX() - this.getX();
        double dy = target.getY(0.5D) - this.getY(0.5D);
        double dz = target.getZ() - this.getZ();
        Vec3 vec3 = new Vec3(dx, dy, dz);
        SmallFireball fireball = new SmallFireball(this.level(), dx, dy, dz, vec3.normalize());
        fireball.setPos(this.getX(), this.getY() + 0.5, this.getZ());
        this.level().addFreshEntity(fireball);
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

