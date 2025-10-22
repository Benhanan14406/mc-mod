package com.Benhanan14406.dragon.entities.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;

import java.util.EnumSet;

public class NoTPFollowOwnerGoal extends Goal {
    private final TamableAnimal tamable;
    private LivingEntity followTarget;
    private final double speedModifier;
    private final PathNavigation navigation;
    private int timeToRecalcPath;
    private final float stopDistance;
    private final float startDistance;

    public NoTPFollowOwnerGoal(TamableAnimal tamable, LivingEntity followTarget, double speedModifier, float startDistance, float stopDistance) {
        this.tamable = tamable;
        this.followTarget = followTarget;
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
        LivingEntity livingentity = this.followTarget;
        if (livingentity == null) {
            return false;
        } else {
            return !(this.tamable.distanceToSqr(livingentity) < (double) (this.startDistance * this.startDistance));
        }
    }

    public boolean canContinueToUse() {
        if (this.navigation.isDone()) {
            return false;
        } else {
            return !this.tamable.unableToMoveToOwner() && !(this.tamable.distanceToSqr(this.followTarget) <= (double) (this.stopDistance * this.stopDistance));
        }
    }

    public void start() {
        this.timeToRecalcPath = 0;
    }

    public void stop() {
        this.followTarget = null;
        this.navigation.stop();
    }

    public void tick() {
        if (this.tamable.distanceToSqr(this.followTarget) < (double)144.0F) {
            this.tamable.getLookControl().setLookAt(this.followTarget, 10.0F, (float)this.tamable.getMaxHeadXRot());
        }

        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(10);
            if (this.tamable.distanceToSqr(this.followTarget) >= (double)144.0F) {
                assert this.followTarget != null;
                this.navigation.moveTo(this.followTarget, this.speedModifier);
            }
        }

    }
}
