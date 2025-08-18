package com.Benhanan14406.dragon.entities.chimaera;

import com.Benhanan14406.dragon.BensBeastiary;
import com.Benhanan14406.dragon.entities.ai.ChimaeraAi;
import com.Benhanan14406.dragon.entities.ai.CustomGroundPathNavigation;
import com.Benhanan14406.dragon.entities.ai.goal.ChimaeraAttackableTargetGoal;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Contract;
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

public class ChimaeraLion extends Animal implements GeoEntity {
    protected static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    protected static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    protected static final RawAnimation RUN = RawAnimation.begin().thenLoop("run");
    protected static final RawAnimation ROAR = RawAnimation.begin().thenPlay("roar");
    protected static final RawAnimation BITE = RawAnimation.begin().thenPlay("bite");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public ChimaeraLion(EntityType<? extends ChimaeraLion> type, Level level) {
        super(type, level);
        this.xpReward = 5;
        this.getNavigation().setCanFloat(true);
        this.setPathfindingMalus(PathType.UNPASSABLE_RAIL, 0.0F);
        this.setPathfindingMalus(PathType.DAMAGE_OTHER, 8.0F);
        this.setPathfindingMalus(PathType.POWDER_SNOW, 8.0F);
        this.setPathfindingMalus(PathType.LAVA, 8.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0F);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 100.0F)
                .add(Attributes.MOVEMENT_SPEED, 0.15F)
                .add(Attributes.ATTACK_DAMAGE, 5.0F)
                .add(Attributes.ARMOR, 10.0F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 2.0F)
                .add(Attributes.FOLLOW_RANGE, 100.0F);

    }

    protected void registerGoals() {
        this.targetSelector.addGoal(2, new ChimaeraAttackableTargetGoal<>(this, LivingEntity.class, false));
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 2.5D, true));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, (double)1.0F));

    }

    @Override
    public @NotNull SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull EntitySpawnReason spawnReason, @Nullable SpawnGroupData spawnGroupData) {
        spawnGroupData = super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);

        ChimaeraGoat goat = new ChimaeraGoat(BensBeastiary.CHIMAERA_GOAT.get(), this.level());
        goat.finalizeSpawn(level, difficulty, EntitySpawnReason.JOCKEY, null);
        goat.startRiding(this, true);

        ChimaeraSnake snake = new ChimaeraSnake(BensBeastiary.CHIMAERA_SNAKE.get(), this.level());
        snake.finalizeSpawn(level, difficulty, EntitySpawnReason.JOCKEY, null);
        snake.startRiding(this, true);

        return spawnGroupData;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(@NotNull ServerLevel serverLevel, @NotNull AgeableMob ageableMob) {
        return null;
    }

    @Override
    public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("predicate", 10, this::predicate));
        controllers.add(new AnimationController<>("swingPredicate", 3, this::swingPredicate));
    }

    protected  <E extends GeoAnimatable> PlayState predicate(final AnimationTest<E> event) {
        if (event.isMoving()) {
            if (this.getTarget() != null) {
                event.setControllerSpeed(3F);
                event.setAndContinue(RUN);
            } else {
                event.setControllerSpeed(1.15F);
                event.setAndContinue(WALK);
            }
        } else if (this.getTarget() != null) {
            event.setControllerSpeed(1.0F);
            event.setAndContinue(ROAR);
        } else {
            event.setControllerSpeed(1.0F);
            event.setAndContinue(IDLE);
        }
        return PlayState.CONTINUE;
    }

    private <E extends GeoAnimatable> PlayState swingPredicate(final AnimationTest<E> event) {
        if (this.swinging && event.controller().getAnimationState().equals(AnimationController.State.STOPPED)) {
            event.controller().forceAnimationReset();
            event.setControllerSpeed(1.25F);
            event.setAndContinue(BITE);
            this.swinging = false;
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    protected @NotNull Brain<?> makeBrain(@NotNull Dynamic<?> ops) {
        return ChimaeraAi.makeBrain(this, ops);
    }

    public @NotNull Brain<?> getBrain() {
        return super.getBrain();
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        return new CustomGroundPathNavigation(this, level);
    }

    @Override
    public @NotNull LookControl getLookControl() {
        return new SmoothSwimmingLookControl(this, 30);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.HORSE_BREATHE;
    }

    @Override
    protected void playStepSound(@NotNull BlockPos pos, @NotNull BlockState state) {
        super.playStepSound(pos, state);
        this.playSound(SoundEvents.GOAT_STEP, 0.15F, 1.0F);
    }

    @Override
    protected void playAttackSound() {
        super.playAttackSound();
        this.playSound(SoundEvents.RAVAGER_ROAR, 0.15F, 1.0F);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getMoveControl().hasWanted()) {
            this.setSprinting(this.getMoveControl().getSpeedModifier() >= 2D && this.onGround());
        } else {
            this.setSprinting(false);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.dead) {
            for (Entity heads : this.getPassengers())
                heads.hurtOrSimulate(this.damageSources().genericKill(), Float.MAX_VALUE);
        }

        if (this.getMoveControl().hasWanted()) {
            this.setSprinting(this.getMoveControl().getSpeedModifier() >= 2D && this.onGround());
        } else {
            this.setSprinting(false);
        }
    }

    @Override
    protected void customServerAiStep(@NotNull ServerLevel level) {
        super.customServerAiStep(level);

        if (this.getMoveControl().hasWanted()) {
            this.setSprinting(this.getMoveControl().getSpeedModifier() >= 2D && this.onGround());
        } else {
            this.setSprinting(false);
        }
    }

    @Override
    public boolean isFood(@NotNull ItemStack itemStack) {
        return false;
    }

    @Override
    public @Nullable LivingEntity getControllingPassenger() {
        return null;
    }

    protected boolean canAddPassenger(@NotNull Entity entity) {
        return this.getPassengers().size() < 2;
    }

    @Override
    protected @NotNull Vec3 getPassengerAttachmentPoint(@NotNull Entity entity, @NotNull EntityDimensions dimensions, float p_376713_) {
        if (entity instanceof ChimaeraGoat) {
            return (new Vec3(0.0F, 1.0F, -0.15F)).yRot(-this.getYRot() * ((float) Math.PI / 180F));
        } else {
            return (new Vec3(0.0F, 0.95F, -0.65F)).yRot(-this.getYRot() * ((float) Math.PI / 180F));
        }
    }
}
