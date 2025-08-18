package com.Benhanan14406.dragon.entities.chimaera;

import com.Benhanan14406.dragon.entities.ai.goal.ChimaeraAttackableTargetGoal;
import com.Benhanan14406.dragon.entities.ai.goal.ChimaeraLookGoal;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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

public class ChimaeraSnake extends Animal implements GeoEntity {
    protected static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    protected static final RawAnimation STRIKE = RawAnimation.begin().thenPlay("strike");
    protected static final RawAnimation ROAR = RawAnimation.begin().thenPlay("roar");

    private int targetDuration = 100;
    private int lookDirection1 = 100;
    private int lookDirection2 = 80;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public ChimaeraSnake(EntityType<? extends ChimaeraSnake> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 100.0F)
                .add(Attributes.ATTACK_DAMAGE, 3.0F)
                .add(Attributes.FOLLOW_RANGE, 70.0F);

    }

    protected void registerGoals() {
        this.targetSelector.addGoal(1, new ChimaeraAttackableTargetGoal<>(this, LivingEntity.class, true));
        this.goalSelector.addGoal(3, new ChimaeraLookGoal(this, LivingEntity.class, 10.0F));
        this.goalSelector.addGoal(2, new StrikeGoal(this));
        this.goalSelector.addGoal(10, new ChimaeraLookAroundGoal(this));
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
    }

    @Override
    protected void addAdditionalSaveData(@NotNull ValueOutput output) {
        super.addAdditionalSaveData(output);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull ValueInput input) {
        super.readAdditionalSaveData(input);
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
        event.setControllerSpeed(1.0F);
        return event.setAndContinue(IDLE);
    }

    private <E extends GeoAnimatable> PlayState swingPredicate(final AnimationTest<E> event) {
        if (this.swinging && event.controller().getAnimationState().equals(AnimationController.State.STOPPED)) {
            event.controller().forceAnimationReset();
            event.setControllerSpeed(1.25F);
            event.setAndContinue(STRIKE);
            this.swinging = false;
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public int getMaxHeadYRot() {
        return 45;
    }

    @Override
    protected void clampHeadRotationToBody() {
        super.clampHeadRotationToBody();
    }

    @Override
    public void tick() {
        super.tick();

        if (lookDirection1 > 0) --lookDirection1;
        else lookDirection2 = 80;

        if (lookDirection2 > 0) --lookDirection2;
        else lookDirection1 = 100;

        if (this.getTarget() != null && targetDuration > 0) {
            --targetDuration;
        } else {
            this.setTarget(null);
            this.targetDuration = 100;
        }

        if (this.getVehicle() != null) {
            LivingEntity vehicle = (LivingEntity) this.getVehicle();
            float vehilceYRotChange = vehicle.getYRot() - vehicle.yRotO;
            this.setYRot(vehicle.getYRot() - 180.0F);
            this.setYBodyRot(vehicle.getYRot() - 180.0F);
            if (this.getTarget() == null) this.setYHeadRot(this.yHeadRot + vehilceYRotChange);
            this.setYHeadRot(Mth.clamp(vehicle.getYRot() - 180.0F, -10, 10));
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
        return this.getAttackBoundingBox().intersects(entity.getHitbox().inflate(3.0F, 3.0F, 3.0F));
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

    static class StrikeGoal extends Goal {
        private final ChimaeraSnake goat;
        private int ticksUntilNextAttack;
        private final int attackInterval = 40;
        private long lastCanUseCheck;
        private static final long COOLDOWN_BETWEEN_CAN_USE_CHECKS = 20L;

        public StrikeGoal(ChimaeraSnake goat) {
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
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 20, 2));
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 10, 1));
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

    static class ChimaeraLookAroundGoal extends Goal {
        private final ChimaeraSnake mob;
        private double relX;
        private double relZ;
        private int lookTime;

        public ChimaeraLookAroundGoal(ChimaeraSnake mob) {
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
                this.mob.setYHeadRot(this.mob.getVehicle().getYRot() - 180.0F);
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
