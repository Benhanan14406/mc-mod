package com.Benhanan14406.dragon.entities;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class FollowWanderSitAnimal extends TamableAnimal {
    private static final EntityDataAccessor<Boolean> FOLLOW_MODE = SynchedEntityData.defineId(FollowWanderSitAnimal.class, EntityDataSerializers.BOOLEAN);

    protected FollowWanderSitAnimal(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    public boolean isFood(@NotNull ItemStack itemStack) {
        return false;
    }

    public @Nullable AgeableMob getBreedOffspring(@NotNull ServerLevel serverLevel, @NotNull AgeableMob ageableMob) {
        return null;
    }

    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FOLLOW_MODE, false);
    }

    public boolean canFollow() {
        return this.entityData.get(FOLLOW_MODE);
    }

    public void setCanFollow(boolean follow) {
        this.entityData.set(FOLLOW_MODE, follow);
    }

    static  class ToggleableFollowOwnerGoal extends Goal {
        private final FollowWanderSitAnimal tamable;
        @javax.annotation.Nullable
        private LivingEntity owner;
        private final double speedModifier;
        private final PathNavigation navigation;
        private int timeToRecalcPath;
        private final float stopDistance;
        private final float startDistance;
        private float oldWaterCost;

        public ToggleableFollowOwnerGoal(FollowWanderSitAnimal tamable, double speedModifier, float startDistance, float stopDistance) {
            this.tamable = tamable;
            this.speedModifier = speedModifier;
            this.navigation = tamable.getNavigation();
            this.startDistance = startDistance;
            this.stopDistance = stopDistance;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
            if (!(tamable.getNavigation() instanceof GroundPathNavigation) && !(tamable.getNavigation() instanceof FlyingPathNavigation)) {
                throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
            }
        }

        public boolean canUse() {
            LivingEntity livingentity = this.tamable.getOwner();
            if (livingentity == null) {
                return false;
            } else if (this.tamable.unableToMoveToOwner()) {
                return false;
            } else if (this.tamable.distanceToSqr(livingentity) < (double)(this.startDistance * this.startDistance)) {
                return false;
            } else {
                this.owner = livingentity;
                return this.tamable.canFollow();
            }
        }

        public boolean canContinueToUse() {
            if (this.navigation.isDone() || !this.tamable.canFollow()) {
                return false;
            } else {
                if (this.tamable.unableToMoveToOwner()) return false;
                assert this.owner != null;
                return !(this.tamable.distanceToSqr(this.owner) <= (double) (this.stopDistance * this.stopDistance));
            }
        }

        public void start() {
            this.timeToRecalcPath = 0;
            this.oldWaterCost = this.tamable.getPathfindingMalus(PathType.WATER);
            this.tamable.setPathfindingMalus(PathType.WATER, 0.0F);
        }

        public void stop() {
            this.owner = null;
            this.navigation.stop();
            this.tamable.setPathfindingMalus(PathType.WATER, this.oldWaterCost);
        }

        public void tick() {
            boolean flag = this.tamable.shouldTryTeleportToOwner();
            if (!flag) {
                assert this.owner != null;
                this.tamable.getLookControl().setLookAt(this.owner, 10.0F, (float)this.tamable.getMaxHeadXRot());
            }

            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = this.adjustedTickDelay(10);
                if (flag) {
                    this.tamable.tryToTeleportToOwner();
                } else {
                    this.navigation.moveTo(this.owner, this.speedModifier);
                }
            }

        }
    }
}
