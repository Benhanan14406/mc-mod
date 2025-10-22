package com.Benhanan14406.dragon.entities;

import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class FollowWanderSitAnimal extends TamableAnimal {
    private static final EntityDataAccessor<Boolean> FOLLOW = SynchedEntityData.defineId(FollowWanderSitAnimal.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SIT = SynchedEntityData.defineId(FollowWanderSitAnimal.class, EntityDataSerializers.BOOLEAN);
    int mode;

    protected FollowWanderSitAnimal(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.mode = 3;
    }

    public boolean isFood(@NotNull ItemStack itemStack) {
        return false;
    }

    public @Nullable AgeableMob getBreedOffspring(@NotNull ServerLevel serverLevel, @NotNull AgeableMob ageableMob) {
        return null;
    }

    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FOLLOW, false);
        builder.define(SIT, false);
    }

    @Override
    protected void addAdditionalSaveData(@NotNull ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("isFollowing", this.isFollowing());
        output.putBoolean("isSitting", this.isSitting());
    }

    @Override
    protected void readAdditionalSaveData(@NotNull ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setFollow(input.getBooleanOr("isFollowing", this.isFollowing()));
        this.setSit(input.getBooleanOr("isSitting", this.isSitting()));
    }

    public boolean isFollowing() {
        return this.entityData.get(FOLLOW);
    }

    public void setFollow(boolean follow) {
        this.entityData.set(FOLLOW, follow);
    }

    public boolean isSitting() {
        return this.entityData.get(SIT);
    }

    public void setSit(boolean sit) {
        this.entityData.set(SIT, sit);
    }

    public void cycleMode(Player player) {
        if (mode == 3) {
            mode = 1;
        } else {
            mode += 1;
        }

        switch(mode) {
            case(1) -> {
                this.setFollow(true);
                this.setSit(false);
            }
            case(2) -> {
                this.setFollow(false);
                this.setSit(true);
            }
            case(3) -> {
                this.setFollow(false);
                this.setSit(false);
            }
        }

        this.getDisplayName();
        if (this.isFollowing() && !this.isSitting()) {
            player.displayClientMessage(Component.literal(this.getDisplayName().getString() + " is following"), true);
        } else if (!this.isFollowing() && this.isSitting()) {
            player.displayClientMessage(Component.literal(this.getDisplayName().getString() + " is sitting"), true);
        } else if (!this.isFollowing() && !this.isSitting()) {
            player.displayClientMessage(Component.literal(this.getDisplayName().getString() + " is wandering"), true);
        } else {
            this.setFollow(true);
            this.setSit(false);
            player.displayClientMessage(Component.literal(this.getDisplayName().getString() + " is following"), true);
        }
    }

    public static  class ToggleableFollowOwnerGoal extends Goal {
        private final FollowWanderSitAnimal tamable;
        @javax.annotation.Nullable
        private LivingEntity owner;
        private final double speedModifier;
        private final PathNavigation navigation;
        private int timeToRecalcPath;
        private final float stopDistance;
        private final float startDistance;

        public ToggleableFollowOwnerGoal(FollowWanderSitAnimal tamable, double speedModifier, float startDistance, float stopDistance) {
            this.tamable = tamable;
            this.speedModifier = speedModifier;
            this.navigation = tamable.getNavigation();
            this.startDistance = startDistance;
            this.stopDistance = stopDistance;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        public boolean canUse() {
            LivingEntity livingentity = this.tamable.getOwner();
            if (livingentity == null) {
                return false;
            } else if (this.tamable.unableToMoveToOwner()) {
                return false;
            } else if (this.tamable.isSitting()) {
                return false;
            } else if (this.tamable.distanceToSqr(livingentity) < (double)(this.startDistance * this.startDistance)) {
                return false;
            } else {
                this.owner = livingentity;
                return this.tamable.isFollowing();
            }
        }

        public boolean canContinueToUse() {
            if (this.navigation.isDone() || !this.tamable.isFollowing() || this.tamable.isSitting()) {
                return false;
            } else {
                if (this.tamable.unableToMoveToOwner()) return false;
                if (this.owner != null) {
                    return !(this.tamable.distanceToSqr(this.owner) <= (double) (this.stopDistance * this.stopDistance));
                } else {
                    return false;
                }
            }
        }

        public void start() {
            this.timeToRecalcPath = 0;
            if (this.owner != null) {
                this.tamable.getNavigation().moveTo(this.owner, this.speedModifier);
            }
        }

        public void stop() {
            this.navigation.stop();
        }

        public void tick() {
            boolean flag = this.tamable.shouldTryTeleportToOwner();
            if (!flag) {
                assert this.owner != null;
                this.tamable.getLookControl().setLookAt(this.owner, 10.0F, (float)this.tamable.getMaxHeadXRot());
            }

            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = this.adjustedTickDelay(5);
                if (flag) {
                    this.tamable.tryToTeleportToOwner();
                } else {
                    this.navigation.moveTo(this.owner, this.speedModifier);
                }
            }

        }
    }
}
