package com.Benhanan14406.dragon.entities.basilisk;

import com.Benhanan14406.dragon.BensBeastiary;
import com.Benhanan14406.dragon.entities.ai.goal.CustomMeleeAttackGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animatable.processing.AnimationTest;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;
import java.util.Objects;

public class BasiliskChick extends TamableAnimal implements GeoAnimatable {
    private static final EntityDataAccessor<Boolean> FOLLOWING = SynchedEntityData.defineId(BasiliskChick.class, EntityDataSerializers.BOOLEAN);

    private static final int GROW_UP_TICKS = 24000 * 2; // 2 Minecraft days = 48,000 ticks
    private int ageTicks = 0;

    protected static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    protected static final RawAnimation CRAWL = RawAnimation.begin().thenLoop("crawl");
    protected static final RawAnimation SWIM = RawAnimation.begin().thenLoop("swim");
    protected static final RawAnimation BEG = RawAnimation.begin().thenLoop("beg");
    protected static final RawAnimation BITE = RawAnimation.begin().thenPlay("bite");
    protected static final RawAnimation BITE_UNDERWATER = RawAnimation.begin().thenPlay("underwater_bite");

    public final TargetingConditions.Selector PREY_SELECTOR = (entity, serverLevel) -> {
        AABB entityBB = entity.getBoundingBox();
        EntityType<?> entityType = entity.getType();
        return entityBB.getXsize() <= 0.5 &&
                entityBB.getYsize() <= 0.5 &&
                entityType != BensBeastiary.BASILISK.get() &&
                entityType != BensBeastiary.BASILISK_CHICK.get();
    };

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public BasiliskChick(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new SmoothSwimmingMoveControl(this, 60, 30, 1.5F, 1.0F, true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 8.0F)
                .add(Attributes.MOVEMENT_SPEED, 0.125F)
                .add(Attributes.FOLLOW_RANGE, 20.0F)
                .add(Attributes.ATTACK_DAMAGE, 4.0F);
    }

    @Override
    public @NotNull SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull EntitySpawnReason spawnType, @Nullable SpawnGroupData spawnGroupData) {
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(@NotNull ServerLevel serverLevel, @NotNull AgeableMob ageableMob) {
        return null;
    }

    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FOLLOWING, false);
    }

    @Override
    protected void addAdditionalSaveData(@NotNull ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("AgeTicks", ageTicks);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull ValueInput input) {
        super.readAdditionalSaveData(input);
        if (input.getInt("AgeTicks").isPresent()) {
            ageTicks = input.getInt("AgeTicks").get();
        }
    }

    protected void registerGoals() {
//        this.goalSelector.addGoal(2, new BasiliskChickEatItemGoal(this));
        this.goalSelector.addGoal(3, new FollowOwnerVisibleGoal(this, 1.0F, 0.5F, 10.0F));
        this.goalSelector.addGoal(4, new BasiliskChickRandomStrollGoal(this, 1.0F, 40));

        if (!this.isFollowing()) {
            this.goalSelector.addGoal(1, new CustomMeleeAttackGoal(this, 1.0F, true));
            this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 20.0F));
            this.targetSelector.addGoal(6, new NearestAttackableTargetGoal<>(this, LivingEntity.class, random.nextInt(61), true, true, PREY_SELECTOR));
        }
    }

    @Override
    public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("predicate", 3, this::predicate));
        controllers.add(new AnimationController<>("movementPredicate", 5, this::movementPredicate));
        controllers.add(new AnimationController<>("swingPredicate", 3, this::swingPredicate));
    }

    private PlayState predicate(AnimationTest<GeoAnimatable> test) {
        if (this.isFollowing() && !this.swinging) {
            test.setControllerSpeed(1.75F);
            return test.setAndContinue(BEG);
        }
        return PlayState.STOP;
    }

    private PlayState movementPredicate(AnimationTest<GeoAnimatable> test) {
        if (test.isMoving()) {
            if (this.isInWater()) {
                test.setControllerSpeed(2.0F);
                return test.setAndContinue(SWIM);
            } else {
                test.setControllerSpeed(2.25F);
                return test.setAndContinue(CRAWL);
            }
        } else {
            test.setControllerSpeed(1.0F);
            return test.setAndContinue(IDLE);
        }
    }

    private PlayState swingPredicate(AnimationTest<GeoAnimatable> test) {
        if (this.swinging && test.controller().getAnimationState().equals(AnimationController.State.STOPPED)) {
            test.controller().forceAnimationReset();
            if (this.isInWater()) {
                test.setControllerSpeed(1.0F);
                test.controller().setAnimation(BITE_UNDERWATER);
            } else {
                test.setControllerSpeed(1.0F);
                test.controller().setAnimation(BITE);
            }
            this.swinging = false;
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public double getTick(@Nullable Object object) {
        return 0;
    }

    @Override
    public boolean isFood(@NotNull ItemStack itemStack) {
        return itemStack.is(Items.BEEF)
                || itemStack.is(Items.CHICKEN)
                || itemStack.is(Items.ROTTEN_FLESH)
                || itemStack.is(Items.MUTTON)
                || itemStack.is(Items.PORKCHOP)
                || itemStack.is(Items.WHEAT_SEEDS)
                || itemStack.is(Items.BEETROOT_SEEDS)
                || itemStack.is(Items.MELON_SEEDS)
                || itemStack.is(Items.PUMPKIN_SEEDS)
                || itemStack.is(Items.APPLE);
    }

    public @NotNull InteractionResult mobInteract(Player player, @NotNull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (this.isTame()) {
            if (this.isFood(itemstack)) {
                // Heal if fed
                if (this.getHealth() < this.getMaxHealth()){
                    this.heal(2.0F);
                }

                // Age up if fed
                this.ageTicks += 500;
                this.usePlayerItem(player, hand, itemstack);
                this.swing(this.getUsedItemHand());
                this.gameEvent(GameEvent.EAT);
                this.playSound(SoundEvents.PARROT_EAT);
            }
            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.FAIL;
        }
    }

    @Override
    public @NotNull PathNavigation getNavigation() {
        AmphibiousPathNavigation nav = new AmphibiousPathNavigation(this, level());
        nav.setCanOpenDoors(false);
        nav.setCanFloat(false);
        return nav;
    }

    @Override
    public boolean canDrownInFluidType(@NotNull FluidType type) {
        return false;
    }

    public boolean isFollowing() {
        return this.entityData.get(FOLLOWING);
    }

    public void setFollowing(boolean following) {
        this.entityData.set(FOLLOWING, following);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            ageTicks++;
            if (ageTicks >= GROW_UP_TICKS) {
                Level var2 = this.level();
                if (var2 instanceof ServerLevel serverLevel) {
                    this.convertTo(BensBeastiary.BASILISK.get(), ConversionParams.single(this, false, false), (basilisk) -> {
                        EventHooks.onLivingConvert(this, basilisk);
                        basilisk.finalizeSpawn(serverLevel, this.level().getCurrentDifficultyAt(basilisk.blockPosition()), EntitySpawnReason.CONVERSION, null);
                        basilisk.setPersistenceRequired();
                        basilisk.fudgePositionAfterSizeChange(this.getDimensions(this.getPose()));
                        this.playSound(SoundEvents.TADPOLE_GROW_UP, 0.15F, 1.0F);

                        // Set owner
                        LivingEntity owner = this.getOwner();
                        if (owner != null) {
                            basilisk.tame((Player) owner);
                            basilisk.setFollow(false);
                            basilisk.setOrderedToSit(false);
                            Objects.requireNonNull(basilisk.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(20.0F);
                            Objects.requireNonNull(basilisk.getAttribute(Attributes.ATTACK_DAMAGE)).setBaseValue(5.0F);
                            basilisk.setYHeadRot(20.0F);
                        }
                    });
                }
            }
        }

        if (this.isFollowing()) {
            if (random.nextInt(5) == 0) {
                this.playAmbientSound();
            }
        }
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        if (this.isFollowing()) {
            return SoundEvents.PARROT_AMBIENT;
        } else {
            return null;
        }
    }

    @Override
    public void playAmbientSound() {
        SoundEvent ambientSound = this.getAmbientSound();
        if (ambientSound != null) {
            this.playSound(ambientSound, 1.5F, 2.0F);
        }
    }

    @Override
    protected void playStepSound(@NotNull BlockPos pos, @NotNull BlockState blockIn) {
        this.playSound(SoundEvents.FROG_STEP, 1.0F, 1.5F);
    }

    @Override
    protected @NotNull SoundEvent getSwimSound() {
        return SoundEvents.AXOLOTL_SWIM;
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return SoundEvents.CHICKEN_HURT;
    }

    @Override
    protected void playHurtSound(@NotNull DamageSource source) {
        SoundEvent hurtSound = this.getHurtSound(source);
        if (hurtSound != null) {
            this.playSound(hurtSound, 1.0F, 1.5f);
        }
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return SoundEvents.AXOLOTL_DEATH;
    }

    static class FollowOwnerVisibleGoal extends Goal {
        private final BasiliskChick chick;
        private LivingEntity owner;
        private final double speed;
        private final float stopDistance;
        private final float followDistance;

        public FollowOwnerVisibleGoal(BasiliskChick chick, double speed, float stopDistance, float followDistance) {
            this.chick = chick;
            this.speed = speed;
            this.stopDistance = stopDistance;
            this.followDistance = followDistance;
        }

        @Override
        public boolean canUse() {
            this.owner = this.chick.getOwner();
            return owner != null && !owner.isInvisible() && !owner.isSpectator() && chick.distanceTo(owner) > stopDistance && chick.distanceTo(owner) < followDistance && chick.hasLineOfSight(owner);
        }

        @Override
        public boolean canContinueToUse() {
            this.owner = this.chick.getOwner();
            return owner != null && !owner.isInvisible() && !owner.isSpectator() && chick.distanceTo(owner) > stopDistance && chick.distanceTo(owner) < followDistance && chick.hasLineOfSight(owner);
        }

        @Override
        public void start() {
            super.start();
            chick.setFollowing(true);
        }

        @Override
        public void stop() {
            super.stop();
            chick.setFollowing(false);
        }

        @Override
        public void tick() {
            this.owner = this.chick.getOwner();
            if (owner != null) {
                chick.getNavigation().moveTo(owner, speed);
            }
        }
    }

    static class BasiliskChickRandomStrollGoal extends Goal {
        protected final BasiliskChick mob;
        protected final double speedModifier;
        protected final int interval; // ticks between new movement choices
        protected double x, y, z;

        public BasiliskChickRandomStrollGoal(BasiliskChick mob, double speedModifier, int interval) {
            this.mob = mob;
            this.speedModifier = speedModifier;
            this.interval = interval;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            // Only try sometimes
            if (this.mob.getRandom().nextInt(interval) != 0 || !this.mob.isFollowing()) {
                return false;
            }

            // Semi-aquatic check: pick land or water position depending on state
            Vec3 target;
            if (mob.isInWater()) {
                target = DefaultRandomPos.getPos(this.mob, 8, 3); // swim wander
            } else {
                target = DefaultRandomPos.getPos(this.mob, 5, 2); // slither wander
            }

            if (target == null) {
                return false;
            } else {
                this.x = target.x;
                this.y = target.y;
                this.z = target.z;
                return true;
            }
        }

        @Override
        public boolean canContinueToUse() {
            return !this.mob.getNavigation().isDone() && !this.mob.isFollowing();
        }

        @Override
        public void start() {
            this.mob.getNavigation().moveTo(this.x, this.y, this.z, this.speedModifier);
        }

        @Override
        public void stop() {
            this.mob.navigation.stop();
        }
    }

}
