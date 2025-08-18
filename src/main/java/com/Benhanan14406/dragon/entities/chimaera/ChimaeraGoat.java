package com.Benhanan14406.dragon.entities.chimaera;

import com.Benhanan14406.dragon.entities.ai.goal.ChimaeraAttackableTargetGoal;
import com.Benhanan14406.dragon.entities.ai.goal.ChimaeraLookGoal;
import com.Benhanan14406.dragon.entities.misc.ChimaeraFireBreath;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animatable.processing.AnimationTest;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

public class ChimaeraGoat extends Animal implements GeoEntity {
    private static final EntityDataAccessor<Boolean> FIRE_BREATHING = SynchedEntityData.defineId(ChimaeraGoat.class, EntityDataSerializers.BOOLEAN);

    private int fireBreathCooldown = 60;
    private int targetDuration = 100;
    private int lookDirection1 = 100;
    private int lookDirection2 = 80;

    protected static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    protected static final RawAnimation FIRE_BREATH = RawAnimation.begin().thenLoop("fire_breath");
    protected static final RawAnimation SMACK = RawAnimation.begin().thenPlay("smack");
    protected static final RawAnimation ROAR = RawAnimation.begin().thenPlay("roar");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public ChimaeraGoat(EntityType<? extends ChimaeraGoat> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 100.0F)
                .add(Attributes.ATTACK_DAMAGE, 3.0F)
                .add(Attributes.ATTACK_KNOCKBACK, 10.0F)
                .add(Attributes.FOLLOW_RANGE, 90.0F);

    }

    protected void registerGoals() {
        this.targetSelector.addGoal(1, new ChimaeraAttackableTargetGoal<>(this, LivingEntity.class, true));
        this.goalSelector.addGoal(4, new ChimaeraLookGoal(this, LivingEntity.class, 10.0F));
        if (!this.isFireBreathing()) this.goalSelector.addGoal(2, new SmackGoal(this));
        this.goalSelector.addGoal(3, new FireBreathGoal(this));
        this.goalSelector.addGoal(10, new ChimaeraLookAroundGoal(this));
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FIRE_BREATHING, false);
    }

    @Override
    protected void addAdditionalSaveData(@NotNull ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("FireBreath", this.isFireBreathing());
    }

    @Override
    protected void readAdditionalSaveData(@NotNull ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setFireBreathing(input.getBooleanOr("FireBreath", false));
    }

    @Override
    public boolean isFood(@NotNull ItemStack itemStack) {
        return false;
    }

    @Override
    public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("predicate", 10, this::predicate));
        controllers.add(new AnimationController<>("swingPredicate", 3, this::swingPredicate));
    }

    protected  <E extends GeoAnimatable> PlayState predicate(final AnimationTest<E> event) {
        if (this.isFireBreathing()) {
            event.setControllerSpeed(1.0F);
            return event.setAndContinue(FIRE_BREATH);
        } else {
            if (this.getVehicle() != null) {
                if (this.getVehicle().getDeltaMovement().horizontalDistance() > 1.06E-6) {
                    if (this.getVehicle().isSprinting()) {
                        event.setControllerSpeed(2.0F);
                    } else {
                        event.setControllerSpeed(1.2F);
                    }
                } else {
                    event.setControllerSpeed(1.0F);
                }
            }
            return event.setAndContinue(IDLE);
        }
    }

    private <E extends GeoAnimatable> PlayState swingPredicate(final AnimationTest<E> event) {
        if (this.swinging && event.controller().getAnimationState().equals(AnimationController.State.STOPPED)) {
            event.controller().forceAnimationReset();
            event.setControllerSpeed(1.25F);
            event.setAndContinue(SMACK);
            this.swinging = false;
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.GOAT_PREPARE_RAM;
    }

    @Override
    protected void playAttackSound() {
        super.playAttackSound();
        this.playSound(SoundEvents.GOAT_RAM_IMPACT, 0.5F, 1.0F);
    }

    @Override
    public void tick() {
        super.tick();

        if (lookDirection1 > 0) --lookDirection1;
        else lookDirection2 = 80;

        if (lookDirection2 > 0) --lookDirection2;
        else lookDirection1 = 100;

        if (fireBreathCooldown > 0) --fireBreathCooldown;

        if (this.getTarget() != null && targetDuration > 0) --targetDuration;
        else {
            this.setTarget(null);
            this.targetDuration = 100;
        }

        if (this.getVehicle() != null) {
            LivingEntity vehicle = (LivingEntity) this.getVehicle();
            float vehilceYRotChange = vehicle.getYRot() - vehicle.yRotO;
            this.setYRot(vehicle.getYRot());
            this.setYBodyRot(vehicle.getYRot());
            if (this.getTarget() == null) this.setYHeadRot(this.yHeadRot + vehilceYRotChange);
            this.setYHeadRot(Mth.clamp(vehicle.getYRot(), -45, 45));
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.isAlive()) this.setAggressive(true);

        if (this.getVehicle() != null) {
            this.setHealth(((LivingEntity) this.getVehicle()).getHealth());
        } else {
            this.die(this.damageSources().genericKill());
        }
    }

    @Override
    public boolean isWithinMeleeAttackRange(@NotNull LivingEntity entity) {
        return this.getAttackBoundingBox().intersects(entity.getHitbox().inflate(2.0F, 1.5F, 2.0F));
    }

    @Override
    public void onDamageTaken(@NotNull DamageContainer damageContainer) {
        super.onDamageTaken(damageContainer);
        if (this.getVehicle() != null) this.getVehicle().hurtOrSimulate(damageContainer.getSource(), damageContainer.getOriginalDamage());
    }

    @Override
    public boolean isInvulnerableTo(@NotNull ServerLevel level, @NotNull DamageSource source) {
        return (source.is(DamageTypes.WIND_CHARGE)
                || source.is(DamageTypes.CACTUS)
                || source.is(DamageTypes.CAMPFIRE)
                || source.is(DamageTypes.FREEZE)
                || source.is(DamageTypes.IN_WALL)
                || source.is(DamageTypes.FALL)
                || source.is(DamageTypes.FALLING_ANVIL)
                || source.is(DamageTypes.FALLING_BLOCK)
                || source.is(DamageTypes.FALLING_STALACTITE)
                || source.is(DamageTypes.HOT_FLOOR)
                || source.is(DamageTypes.SWEET_BERRY_BUSH));
    }
    public boolean isFireBreathing() {
        return this.entityData.get(FIRE_BREATHING);
    }

    public void setFireBreathing(boolean isFireBreathing) {
        this.entityData.set(FIRE_BREATHING, isFireBreathing);
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Override
    public boolean dismountsUnderwater() {
        return false;
    }

    @Override
    public boolean canControlVehicle() {
        return false;
    }

    public boolean isEntityInFront(Entity targetEntity, float fieldOfViewDegrees) {
        Vec3 lookDirection = this.getLookAngle(); // 1.0F for full tick
        Vec3 entityPosition = this.getEyePosition();
        Vec3 targetPosition = targetEntity.getEyePosition();

        Vec3 toTarget = targetPosition.subtract(entityPosition);
        toTarget = toTarget.normalize();

        double dotProduct = lookDirection.dot(toTarget);
        double angle = Math.acos(Math.clamp(dotProduct, -1.0, 1.0)); // Handle potential floating-point errors
        double angleInDegrees = Math.toDegrees(angle);

        return angleInDegrees <= fieldOfViewDegrees / 2.0; // Check if within half the FOV
    }

    @Override
    public boolean hasLineOfSight(@NotNull Entity entity) {
        return isEntityInFront(entity, 100);
    }

    static class SmackGoal extends Goal {
        private final ChimaeraGoat goat;
        private int ticksUntilNextAttack;
        private final int attackInterval = 30;
        private long lastCanUseCheck;
        private static final long COOLDOWN_BETWEEN_CAN_USE_CHECKS = 20L;

        public SmackGoal(ChimaeraGoat goat) {
            this.goat = goat;
            this.setFlags(EnumSet.of(Flag.LOOK));
        }

        public boolean canUse() {
            LivingEntity livingentity = this.goat.getTarget();
            return livingentity != null && livingentity.isAlive() && this.goat.canAttack(livingentity) && this.goat.distanceToSqr(livingentity) < 2.0F;
        }

        public void start() {
            this.ticksUntilNextAttack = 0;
        }

        public void stop() {
            LivingEntity livingentity = this.goat.getTarget();
            if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(livingentity)) {
                this.goat.setTarget(null);
            }
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        public void tick() {
            LivingEntity livingentity = this.goat.getTarget();
            if (livingentity != null) {
                this.goat.getLookControl().setLookAt(livingentity, 30.0F, 30.0F);
                this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
                this.checkAndPerformAttack(livingentity);
            }

        }

        protected void checkAndPerformAttack(LivingEntity target) {
            if (this.canPerformAttack(target)) {
                this.resetAttackCooldown();
                this.goat.swing(InteractionHand.MAIN_HAND);
                this.goat.doHurtTarget(getServerLevel(this.goat), target);
            }

        }

        protected void resetAttackCooldown() {
            this.ticksUntilNextAttack = this.adjustedTickDelay(20);
        }

        protected boolean isTimeToAttack() {
            return this.ticksUntilNextAttack <= 0;
        }

        protected boolean canPerformAttack(LivingEntity entity) {
            return this.isTimeToAttack() && this.goat.isWithinMeleeAttackRange(entity) && this.goat.getSensing().hasLineOfSight(entity);
        }

        protected int getTicksUntilNextAttack() {
            return this.ticksUntilNextAttack;
        }

        protected int getAttackInterval() {
            return this.adjustedTickDelay(20);
        }
    }

    static class FireBreathGoal extends Goal {
        private final ChimaeraGoat goat;
        private int count = 20;

        public FireBreathGoal(ChimaeraGoat goat) {
            this.goat = goat;
            this.setFlags(EnumSet.of(Flag.LOOK));
        }

        public boolean canUse() {
            LivingEntity livingentity = this.goat.getTarget();
            return livingentity != null
                    && livingentity.isAlive()
                    && this.goat.canAttack(livingentity)
                    && this.goat.getSensing().hasLineOfSight(livingentity)
                    && this.goat.isEntityInFront(this.goat.getTarget(), 30.0F)
                    && this.goat.fireBreathCooldown > 0;
        }

        @Override
        public boolean canContinueToUse() {
            return this.count > 0
                    && this.goat.getTarget() != null
                    && this.goat.isEntityInFront(this.goat.getTarget(), 60.0F);
        }

        @Override
        public void start() {
            super.start();
            this.count = 20;
            this.goat.playSound(SoundEvents.ENDER_DRAGON_SHOOT);
            this.goat.setFireBreathing(true);
        }

        @Override
        public void stop() {
            super.stop();
            this.goat.setFireBreathing(false);
            this.goat.fireBreathCooldown = 50 + this.goat.getRandom().nextInt(20);
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        public void tick() {
            --count;
            LivingEntity livingentity = this.goat.getTarget();
            if (livingentity != null) {
                double d1 = livingentity.getX() - this.goat.getX();
                double d2 = livingentity.getY(0.5F) - this.goat.getY(0.5F);
                double d3 = livingentity.getZ() - this.goat.getZ();
                double d4 = Math.sqrt(Math.sqrt(this.goat.distanceToSqr(livingentity))) * (double) 0.5F;
                Vec3 vec3 = new Vec3(this.goat.getRandom().triangle(d1, 2.297 * d4), d2, this.goat.getRandom().triangle(d3, 2.297 * d4));
                ChimaeraFireBreath fireBreath = new ChimaeraFireBreath(this.goat.level(), this.goat, vec3.normalize());
                fireBreath.setPos(fireBreath.getX(), this.goat.getY(), fireBreath.getZ());
                this.goat.level().addFreshEntity(fireBreath);

                double x = this.goat.getX() + (this.goat.getLookAngle().x * 1.5) + (this.goat.getRandom().nextGaussian() * 0.5);
                double y = this.goat.getY() + this.goat.getBbHeight() * 0.6;
                double z = this.goat.getZ() + (this.goat.getLookAngle().z * 1.5) + (this.goat.getRandom().nextGaussian() * 0.5);
                this.goat.level().addParticle(ParticleTypes.FLAME, x, y, z, 0, 0.01, 0);

            }

        }
    }

    static class ChimaeraLookAroundGoal extends Goal {
        private final ChimaeraGoat mob;
        private double relX;
        private double relZ;
        private int lookTime;

        public ChimaeraLookAroundGoal(ChimaeraGoat mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        public boolean canUse() {
            return this.mob.getRandom().nextFloat() < 0.02F;
        }

        public boolean canContinueToUse() {
            return this.lookTime >= 0;
        }

        public void start() {
            double d0 = Math.PI * this.mob.getRandom().nextDouble();
            this.relX = Math.cos(d0);
            this.relZ = Math.sin(d0);
            this.lookTime = 20 + this.mob.getRandom().nextInt(20);
        }

        @Override
        public void stop() {
            if (this.mob.getVehicle() != null) {
                this.mob.setYHeadRot(this.mob.getVehicle().getYRot());
            }
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        public void tick() {
            --this.lookTime;
            this.mob.getLookControl().setLookAt(this.mob.getX() + this.relX, this.mob.getEyeY(), this.mob.getZ() + this.relZ);
        }
    }
}
