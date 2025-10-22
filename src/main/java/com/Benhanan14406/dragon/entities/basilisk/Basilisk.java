package com.Benhanan14406.dragon.entities.basilisk;

import com.Benhanan14406.dragon.BensBeastiary;
import com.Benhanan14406.dragon.entities.FollowWanderSitAnimal;
import com.Benhanan14406.dragon.entities.ai.goal.CustomMeleeAttackGoal;
import com.Benhanan14406.dragon.entities.ai.goal.EatDroppedItemGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Basilisk extends FollowWanderSitAnimal implements NeutralMob, GeoAnimatable {
    private static final ResourceLocation SPEED_MODIFIER_ATTACKING_ID = ResourceLocation.withDefaultNamespace("attacking");
    private static final AttributeModifier SPEED_MODIFIER_ATTACKING = new AttributeModifier(SPEED_MODIFIER_ATTACKING_ID, 0.15F, AttributeModifier.Operation.ADD_VALUE);

    private static final EntityDataAccessor<Integer> DATA_REMAINING_ANGER_TIME = SynchedEntityData.defineId(Basilisk.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_REMAINING_SLEEP_TIME = SynchedEntityData.defineId(Basilisk.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_REMAINING_SLEEP_COOLDOWN = SynchedEntityData.defineId(Basilisk.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BASILISK_TYPE = SynchedEntityData.defineId(Basilisk.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DECAPITATED = SynchedEntityData.defineId(Basilisk.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HAS_GOGGLES = SynchedEntityData.defineId(Basilisk.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SLEEPING = SynchedEntityData.defineId(Basilisk.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> VOCALIZING = SynchedEntityData.defineId(Basilisk.class, EntityDataSerializers.BOOLEAN);

    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private static final UniformInt SLEEP_TIME = TimeUtil.rangeOfSeconds(20, 60);
    private static final UniformInt SLEEP_COOLDOWN = TimeUtil.rangeOfSeconds(45, 60);
    int remainingLife = 1200;
    @javax.annotation.Nullable
    private UUID persistentAngerTarget;

    protected static final RawAnimation FLY = RawAnimation.begin().thenLoop("flying");
    protected static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    protected static final RawAnimation SIT = RawAnimation.begin().thenLoop("sitting");
    protected static final RawAnimation SLEEP = RawAnimation.begin().thenLoop("sleeping");
    protected static final RawAnimation WALK = RawAnimation.begin().thenLoop("walking");
    protected static final RawAnimation RUN = RawAnimation.begin().thenLoop("running");
    protected static final RawAnimation SWIM = RawAnimation.begin().thenLoop("swimming");
    protected static final RawAnimation BITE = RawAnimation.begin().thenPlay("bite");
    protected static final RawAnimation BITE_UNDERWATER = RawAnimation.begin().thenPlay("bite_underwater");
    protected static final RawAnimation VOCALIZE = RawAnimation.begin().thenPlay("vocalize");

    public final TargetingConditions.Selector PREY_SELECTOR = (entity, serverLevel) -> {
        EntityType<?> entityType = entity.getType();
        AABB entityBB = entity.getBoundingBox();
        boolean validTarget;
        if (entity.isBaby()) {
            validTarget = true;
        } else if (entity instanceof TamableAnimal && Objects.equals(((TamableAnimal) entity).getOwner(), this.getOwner())) {
            validTarget = false;
        } else validTarget = entityBB.getXsize() <= 1 && entityBB.getYsize() <= 1 && entityType != BensBeastiary.BASILISK.get() && entityType != BensBeastiary.BASILISK_CHICK.get();

        return !this.isFollowing() && !this.isSitting() && !this.isDecapitated() && !this.isSleeping() && validTarget;
    };

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public Basilisk(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.navigation = new BasiliskPathNavigation(this, level());
        this.moveControl = new SmoothSwimmingMoveControl(this, 75, 45, 1.5F, 1.0F, true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 16.0F)
                .add(Attributes.MOVEMENT_SPEED, 0.19F)
                .add(Attributes.FLYING_SPEED, 0.5F)
                .add(Attributes.ATTACK_DAMAGE, 4.0F)
                .add(Attributes.FOLLOW_RANGE, 20.0F);
    }

    @Override
    public @NotNull SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull EntitySpawnReason spawnType, @Nullable SpawnGroupData spawnGroupData) {
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(@NotNull ServerLevel serverLevel, @NotNull AgeableMob ageableMob) {
        return BensBeastiary.BASILISK_CHICK.get().create(serverLevel, EntitySpawnReason.BREEDING);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(1, new SentryGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new FreezeWhenLookedAt(this));
        this.goalSelector.addGoal(5, new ToggleableFollowOwnerGoal(this, 1.0F, 10.0F, 2.0F));
//        this.goalSelector.addGoal(6, new BasiliskSleepGoal(this));
        this.goalSelector.addGoal(7, new EatDroppedItemGoal(this));
        this.goalSelector.addGoal(8, new BasiliskRandomStrollGoal(this, 1.0F, this.random.nextInt(30, 61)));
        this.goalSelector.addGoal(9, new BasiliskLookAtPlayerGoal(this, Player.class, 20.0F));
        this.goalSelector.addGoal(10, new BasiliskLookAroundGoal(this));
        this.targetSelector.addGoal(8, new ResetUniversalAngerTargetGoal<>(this, true));

        if (!this.isFollowing() && !this.isSitting() && !this.isDecapitated()) {
            this.goalSelector.addGoal(4, new CustomMeleeAttackGoal(this, 1.0F, true));
            this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
            this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
            this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, false, false, this::isAngryAt));
            this.targetSelector.addGoal(7, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 60 + random.nextInt(41), false, true, PREY_SELECTOR));

        }

    }

    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_REMAINING_ANGER_TIME, 0);
        builder.define(DATA_REMAINING_SLEEP_TIME, 0);
        builder.define(DATA_REMAINING_SLEEP_COOLDOWN, 0);
        builder.define(BASILISK_TYPE, 0);
        builder.define(DECAPITATED, false);
        builder.define(HAS_GOGGLES, false);
        builder.define(SLEEPING, false);
        builder.define(VOCALIZING, false);
    }

    protected void addAdditionalSaveData(@NotNull ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        this.addPersistentAngerSaveData(valueOutput);
    }

    protected void readAdditionalSaveData(@NotNull ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.readPersistentAngerSaveData(this.level(), valueInput);
    }

    @Override
    public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("movementPredicate", 10, this::movementPredicate));
        controllers.add(new AnimationController<>("randomPredicate", 3, this::randomPredicate));
        controllers.add(new AnimationController<>("swingPredicate", 3, this::swingPredicate));
    }

    private PlayState randomPredicate(AnimationTest<GeoAnimatable> test) {
        if (this.isVocalizing()) {
            test.setControllerSpeed(1.75F);
            return test.setAndContinue(VOCALIZE);
        }
        return PlayState.STOP;
    }

    protected PlayState movementPredicate(AnimationTest<GeoAnimatable> test) {
        if (!this.isInWater() && !this.onGround()) {
            test.setControllerSpeed(1.75F);
            return test.setAndContinue(FLY);
        } else {
            if (this.isSleeping()) {
                test.setControllerSpeed(1.0F);
                return test.setAndContinue(SLEEP);
            } else if (this.isSitting()) {
                test.setControllerSpeed(1.0F);
                return test.setAndContinue(SIT);
            } else {
                if (test.isMoving()) {
                    if (this.isInWater()) {
                        test.setControllerSpeed(2.0F);
                        return test.setAndContinue(SWIM);
                    } else {
                        if (this.isSprinting()) {
                            test.setControllerSpeed(3.0F);
                            return test.setAndContinue(RUN);
                        } else {
                            test.setControllerSpeed(2.0F);
                            return test.setAndContinue(WALK);
                        }
                    }
                } else {
                    test.setControllerSpeed(1.25F);
                    return test.setAndContinue(IDLE);
                }
            }
        }
    }

    private PlayState swingPredicate(AnimationTest<GeoAnimatable> test) {
        if (this.swinging && test.controller().getAnimationState().equals(AnimationController.State.STOPPED)) {
            test.controller().forceAnimationReset();
            if (this.isInWater()) {
                test.setControllerSpeed(2.0F);
                test.controller().setAnimation(BITE_UNDERWATER);
            } else {
                test.setControllerSpeed(1.25F);
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
            if (this.isFood(itemstack) && this.getHealth() < this.getMaxHealth()) {
                this.heal(2.0F);
                this.usePlayerItem(player, hand, itemstack);
                this.gameEvent(GameEvent.EAT);
            } else if (itemstack.is(BensBeastiary.GOGGLES)) {
                this.usePlayerItem(player, hand, itemstack);
                this.setHasGoggles(true);
            } else {
                this.cycleMode(player);
            }
            return InteractionResult.SUCCESS;
        } else if (this.isDecapitated() && itemstack.is(Items.GLASS_BOTTLE)) {
            this.usePlayerItem(player, hand, itemstack);
            player.addItem(new ItemStack(BensBeastiary.BASILISK_BLOOD.get()));
            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.FAIL;
        }
    }

    public void aiStep() {
        super.aiStep();

        List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(10.0F));

        // Agro and petrify on being stared
        for (LivingEntity entity : nearbyEntities) {
            if (!this.isDecapitated() && !this.hasGoggles() && this.isBeingStaredBy(entity) && entity.isLookingAtMe(this, 0.025, true, false, entity.getEyeY())) {
                if (!(entity instanceof Basilisk)) {
                    // Can't petrify is player is wearing goggles
                    if (!(entity instanceof Player && entity.getItemBySlot(EquipmentSlot.HEAD).is(BensBeastiary.GOGGLES.get()))) {
                        this.petrify(entity);
                    }
                }

                // Angry at entities looking at it
                if (!(!this.isFollowing() && this.getPersistentAngerTarget() == null && this.getTarget() == null)
                    && !(entity instanceof TamableAnimal && ((TamableAnimal) entity).getOwner() == this.getOwner())
                    && !((entity == this.getOwner()))) {

                    this.setPersistentAngerTarget(entity.getUUID());
                    this.startPersistentAngerTimer();
                }
            }
        }

        Vec3 vec3 = this.getDeltaMovement();
        if (!this.onGround() && vec3.y < (double)0.0F) {
            this.setDeltaMovement(vec3.multiply(1.0F, 0.6, 1.0F));
        }

        if (this.horizontalCollision) {
            this.jumpFromGround();
        }

        if (this.getTarget() != null) {
            this.setSprinting(false);
        }

        if (this.isTame()) {
            if (this.isFollowing() && this.isSitting()) {
                this.setFollow(false);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            this.updatePersistentAnger((ServerLevel)this.level(), true);
        }

        if (this.isDecapitated()) {
            remainingLife--;
        }

        if (remainingLife == 0) {
            this.hurt(damageSources().genericKill(), this.getHealth());
        }

        int sleepTime = this.getRemainingSleepTime();
        if (this.isSleeping() && sleepTime > 0) {
            this.setRemainingSleepTime(sleepTime - 1);
        }

        int sleepCooldown = this.getRemainingSleepCooldown();
        if (sleepCooldown > 0) {
            this.setRemainingSleepCooldown(sleepCooldown - 1);
        }
    }

    @Override
    public double getTick(@Nullable Object object) {
        return 0;
    }

    public void petrify(LivingEntity petrifiedEntity) {
        petrifiedEntity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 5));
        petrifiedEntity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 5));
        petrifiedEntity.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 100, 3));
        petrifiedEntity.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 1));
        petrifiedEntity.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1));
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.CHICKEN_AMBIENT;
    }

    @Override
    public void playAmbientSound() {
        this.setVocalizing(true);
        SoundEvent ambientSound = this.getAmbientSound();
        if (ambientSound != null) {
            this.playSound(ambientSound, 1.0F, 0.5F + this.random.nextFloat() * 0.3F);
        }
        this.setVocalizing(false);
    }

    @Override
    protected void playStepSound(@NotNull BlockPos pos, @NotNull BlockState blockIn) {
        if (this.isSprinting()) {
            this.playSound(SoundEvents.STRIDER_STEP, 2.0F, 1.0F);
        } else {
            this.playSound(SoundEvents.STRIDER_STEP, 1.0F, 1.0F);
        }
    }

    @Override
    protected void playAttackSound() {
        this.playSound(SoundEvents.PHANTOM_BITE, 0.25F, 0.5F);
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
            this.playSound(hurtSound, 1.0F, 0.5F + this.random.nextFloat() * 0.3F);
        }
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return SoundEvents.FROG_DEATH;
    }

    @Override
    public void setTarget(@javax.annotation.Nullable LivingEntity entity) {
        AttributeInstance attributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
        assert attributeinstance != null;
        if (entity != null && !this.isDecapitated()) {
            super.setTarget(entity);
            this.setSprinting(true);
            if (!attributeinstance.hasModifier(SPEED_MODIFIER_ATTACKING_ID)) {
                attributeinstance.addTransientModifier(SPEED_MODIFIER_ATTACKING);
            }
        } else {
            super.setTarget(null);
            this.setSprinting(false);
            attributeinstance.removeModifier(SPEED_MODIFIER_ATTACKING_ID);
        }
    }

    @Override
    public void die(@NotNull DamageSource cause) {
        super.die(cause);

        if (this.hasGoggles()) {
            this.drop(new ItemStack(Items.GLASS), false, false);
        }

        // Natural drops
        ItemStack feathers = new ItemStack(Items.FEATHER);
        feathers.setCount(random.nextInt(2, 6));
        this.drop(feathers, false, false);

        ItemStack chicken = new ItemStack(Items.CHICKEN);
        feathers.setCount(random.nextInt(1, 3));
        this.drop(chicken, false, false);
    }

    @Override
    protected void actuallyHurt(@NotNull ServerLevel serverLevel, @NotNull DamageSource damageSource, float amount) {
        super.actuallyHurt(serverLevel, damageSource, amount);

        ItemStack weapon = damageSource.getWeaponItem();
        if (weapon != null && (weapon.is(ItemTags.SWORDS) || weapon.is(ItemTags.AXES)) && !this.isDecapitated()) {
            int decapitationChance = random.nextInt(5);
            if (decapitationChance == 0) this.setDecapitated();
        }
    }

    protected void checkFallDamage(double y, boolean onGround, @NotNull BlockState state, @NotNull BlockPos pos) {
    }

    public void setBasiliskType(int type) {
        if (1 <= type && type <= 3) {
            this.entityData.set(BASILISK_TYPE, type);
        } else {
            this.entityData.set(BASILISK_TYPE, 1);
        }
    }

    public int getBasiliskType() {
        return this.entityData.get(BASILISK_TYPE);
    }

    public int getRemainingPersistentAngerTime() {
        return this.entityData.get(DATA_REMAINING_ANGER_TIME);
    }

    public void setRemainingPersistentAngerTime(int angerTime) {
        if (this.isDecapitated()) {
            this.entityData.set(DATA_REMAINING_ANGER_TIME, 0);
        } else {
            this.entityData.set(DATA_REMAINING_ANGER_TIME, angerTime);
        }
    }

    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    public int getRemainingSleepTime() {
        return this.entityData.get(DATA_REMAINING_SLEEP_TIME);
    }

    public void setRemainingSleepTime(int sleepTime) {
        this.entityData.set(DATA_REMAINING_SLEEP_TIME, sleepTime);
    }

    public void startSleepTimer() {
        this.setRemainingSleepTime(SLEEP_TIME.sample(this.random));
    }

    public int getRemainingSleepCooldown() {
        return this.entityData.get(DATA_REMAINING_SLEEP_COOLDOWN);
    }

    public void setRemainingSleepCooldown(int sleepCooldown) {
        this.entityData.set(DATA_REMAINING_SLEEP_COOLDOWN, sleepCooldown);
    }

    public void startSleepCooldown() {
        this.setRemainingPersistentAngerTime(SLEEP_COOLDOWN.sample(this.random));
    }

    @javax.annotation.Nullable
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    public void setPersistentAngerTarget(@javax.annotation.Nullable UUID uuid) {
        if (!this.isDecapitated()) {
            AttributeInstance attributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
            assert attributeinstance != null;
            if (uuid == null) {
                this.setSprinting(false);
                attributeinstance.removeModifier(SPEED_MODIFIER_ATTACKING_ID);
            } else {
                this.setSprinting(true);
                if (!attributeinstance.hasModifier(SPEED_MODIFIER_ATTACKING_ID)) {
                    attributeinstance.addTransientModifier(SPEED_MODIFIER_ATTACKING);
                }
            }
            this.persistentAngerTarget = uuid;
        } else {
            this.persistentAngerTarget = null;
        }
    }

    public boolean isDecapitated() {
        return this.entityData.get(DECAPITATED);
    }

    private void setDecapitated() {
        this.entityData.set(DECAPITATED, true);

        if (this.hasGoggles()) {
            this.setHasGoggles(false);
            this.drop(new ItemStack(BensBeastiary.GOGGLES.get()), false, false);
        }
    }

    public boolean isSleeping() {
        return this.entityData.get(SLEEPING);
    }

    private void setSleeping(boolean sleeping) {
        this.entityData.set(SLEEPING, sleeping);
    }

    public boolean hasGoggles() {
        return this.entityData.get(HAS_GOGGLES);
    }

    private void setHasGoggles(boolean hasGoggles) {
        this.entityData.set(HAS_GOGGLES, hasGoggles);
    }

    public boolean isVocalizing() {
        return this.entityData.get(VOCALIZING);
    }

    private void setVocalizing(boolean vocalize) {
        this.entityData.set(VOCALIZING, vocalize);
    }

    @Override
    protected int getBaseExperienceReward(@NotNull ServerLevel serverLevel) {
        return 3;
    }

    boolean isBeingStaredBy(LivingEntity player) {
        return LivingEntity.PLAYER_NOT_WEARING_DISGUISE_ITEM_FOR_TARGET.test(player, this) && this.isLookingAtMe(player, 0.025, true, false, this.getEyeY());
    }

    public boolean isValidSleepSpot(BlockPos pos) {
        Level level = this.level();

        if (!level.getBlockState(pos).isAir() ||
                level.getBlockState(pos.below()).isAir() ||
                level.getBlockState(pos.above()).isAir()
        ) return false;

        return level.noCollision(this, this.getBoundingBox().move(
                pos.getX() + 0.5 - this.getX(),
                pos.getY() - this.getY(),
                pos.getZ() + 0.5 - this.getZ()
        ));
    }

    static class BasiliskPathNavigation extends AmphibiousPathNavigation{
        public BasiliskPathNavigation(Mob mob, Level level) {
            super(mob, level);
        }

        @Override
        protected boolean canMoveDirectly(@NotNull Vec3 start, @NotNull Vec3 end) {
            return super.canMoveDirectly(start, end);
        }
    }

    static class BasiliskRandomStrollGoal extends Goal {
        private final Basilisk basilisk;
        private final double speedModifier;
        private final int interval;
        private double x;
        private double y;
        private double z;

        public BasiliskRandomStrollGoal(Basilisk basilisk, double speedModifier, int interval) {
            this.basilisk = basilisk;
            this.speedModifier = speedModifier;
            this.interval = interval;
        }

        @Override
        public boolean canUse() {
            if (basilisk.isFollowing() || basilisk.isSleeping() || basilisk.isSitting()) return false;
            if (basilisk.getNavigation().isInProgress()) return false;

            RandomSource random = basilisk.getRandom();
            if (random.nextInt(interval) != 0) return false;

            Vec3 landTarget = DefaultRandomPos.getPos(basilisk, 10, 10);
            Vec3 waterTarget = getWaterPos(random);

            Vec3 chosen = basilisk.random.nextInt(2) == 0 ? waterTarget : landTarget;
            if (landTarget == null) chosen = waterTarget;
            if (waterTarget == null) chosen = landTarget;
            if (chosen == null) return false;

            this.x = chosen.x;
            this.y = chosen.y;
            this.z = chosen.z;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return !basilisk.getNavigation().isDone();
        }

        @Override
        public void start() {
            basilisk.getNavigation().moveTo(x, y, z, speedModifier);
        }

        private Vec3 getWaterPos(RandomSource random) {
            int radius = 15;
            for (int i = 0; i < 20; i++) {
                int dx = random.nextInt(radius * 2) - radius;
                int dy = random.nextInt(6) - 3;
                int dz = random.nextInt(radius * 2) - radius;

                var pos = basilisk.blockPosition().offset(dx, dy, dz);
                if (basilisk.level().getBlockState(pos).liquid()) {
                    return new Vec3(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
                }
            }
            return null;
        }
    }


    static class FreezeWhenLookedAt extends Goal {
        private final Basilisk basilisk;
        @javax.annotation.Nullable
        private LivingEntity target;

        public FreezeWhenLookedAt(Basilisk basilisk) {
            this.basilisk = basilisk;
            this.setFlags(EnumSet.of(Flag.JUMP, Flag.MOVE));
        }

        public boolean canUse() {
            this.target = this.basilisk.getTarget();
            LivingEntity var2 = this.target;
            if (var2 instanceof Player player) {
                double d0 = this.target.distanceToSqr(this.basilisk);
                return !(d0 > (double) 256.0F) && this.basilisk.isBeingStaredBy(player);
            } else {
                return false;
            }
        }

        public void start() {
            this.basilisk.getNavigation().stop();
        }

        @Override
        public void stop() {
            this.basilisk.getNavigation().recomputePath();
        }

        public void tick() {
            assert this.target != null;
            this.basilisk.getLookControl().setLookAt(this.target.getX(), this.target.getEyeY(), this.target.getZ());
        }
    }

    static class BasiliskSleepGoal extends Goal {
        private final Basilisk basilisk;
        private BlockPos targetPos;

        public BasiliskSleepGoal(Basilisk basilisk) {
            this.basilisk = basilisk;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (basilisk.getTarget() != null ||
                    this.basilisk.isSitting() ||
                    this.basilisk.isFollowing() ||
                    this.basilisk.getRemainingSleepCooldown() > 0
            ) return false;

            BlockPos mobPos = basilisk.blockPosition();

            // Scan in a radius around the Basilisk
            for (BlockPos pos : BlockPos.betweenClosed(mobPos.offset(-8, -2, -8), mobPos.offset(8, 2, 8))) {
                if (basilisk.isValidSleepSpot(pos)) {
                    targetPos = pos.immutable();
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return !basilisk.isFollowing() &&
                    !this.basilisk.isSitting() &&
                    targetPos != null &&
                    basilisk.distanceToSqr(Vec3.atCenterOf(targetPos)) > 1.0D &&
                    this.basilisk.getRemainingSleepTime() > 0;
        }

        @Override
        public void start() {
            if (targetPos != null) {
                basilisk.getNavigation().moveTo(
                        targetPos.getX(),
                        targetPos.getY(),
                        targetPos.getZ(),
                        1.0
                );
            }
        }

        @Override
        public void tick() {
            if (targetPos != null && basilisk.distanceToSqr(Vec3.atCenterOf(targetPos)) < 1.5D && !basilisk.level().getBlockState(basilisk.getOnPos().above()).isAir()) {
                basilisk.setSleeping(true);
                basilisk.startSleepTimer();
            }
        }

        @Override
        public void stop() {
            basilisk.setSleeping(false);
            basilisk.startSleepCooldown();
            targetPos = null;
        }
    }

    static class SentryGoal extends Goal {
        private final Basilisk basilisk;
        private double relX;
        private double relZ;
        private int lookTime;

        public SentryGoal(Basilisk basilisk) {
            this.basilisk = basilisk;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        public boolean canUse() {
            return this.basilisk.getRandom().nextFloat() < 0.02F && this.basilisk.isSitting();
        }

        public boolean canContinueToUse() {
            return this.lookTime >= 0 && this.basilisk.isSitting();
        }

        public void start() {
            double d0 = (Math.PI * 2D) * this.basilisk.getRandom().nextDouble();
            this.relX = Math.cos(d0);
            this.relZ = Math.sin(d0);
            this.lookTime = 20 + this.basilisk.getRandom().nextInt(20);
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        public void tick() {
            --this.lookTime;

            // Get nearby non-affiliated entities
            List<LivingEntity> nearbyEntities = this.basilisk.level().getEntitiesOfClass(LivingEntity.class, this.basilisk.getBoundingBox().inflate(20.0F));
            nearbyEntities = nearbyEntities.stream().filter(entity -> {
                if ((entity instanceof TamableAnimal) && Objects.equals(((TamableAnimal) entity).getOwner(), this.basilisk.getOwner())) {
                    return false;
                } else return !entity.equals(this.basilisk.getOwner());
            }).toList();

            nearbyEntities = nearbyEntities.stream().filter(entity -> !(entity instanceof Basilisk) && !(entity instanceof BasiliskChick) && !entity.getItemBySlot(EquipmentSlot.HEAD).is(BensBeastiary.GOGGLES.get())).toList();
            if (!nearbyEntities.isEmpty()) {
                LivingEntity lookTarget = nearbyEntities.getFirst();
                this.basilisk.getLookControl().setLookAt(lookTarget.getX(), lookTarget.getEyeY(), lookTarget.getZ());
                this.basilisk.petrify(lookTarget);
            } else {
                this.basilisk.getLookControl().setLookAt(this.basilisk.getX() + this.relX, this.basilisk.getEyeY(), this.basilisk.getZ() + this.relZ);
            }
        }
    }

    static class BasiliskLookAtPlayerGoal extends LookAtPlayerGoal {
        Basilisk basilisk;

        public BasiliskLookAtPlayerGoal(Basilisk basilisk, Class<? extends LivingEntity> lookAtType, float lookDistance) {
            super(basilisk, lookAtType, lookDistance);
            this.basilisk = basilisk;
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !this.basilisk.isSleeping() && !this.basilisk.isDecapitated();
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && !this.basilisk.isSleeping() && !this.basilisk.isDecapitated();
        }
    }

    static class BasiliskLookAroundGoal extends RandomLookAroundGoal {
        Basilisk basilisk;

        public BasiliskLookAroundGoal(Basilisk basilisk) {
            super(basilisk);
            this.basilisk = basilisk;
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !this.basilisk.isSleeping() && !this.basilisk.isDecapitated();
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && !this.basilisk.isSleeping() && !this.basilisk.isDecapitated();
        }
    }
}
