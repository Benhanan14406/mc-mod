package com.Benhanan14406.dragon.entities.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

public class ChimaeraLookGoal extends Goal {
    public static final float DEFAULT_PROBABILITY = 0.02F;
    protected final Mob mob;
    @javax.annotation.Nullable
    protected Entity lookAt;
    protected final float lookDistance;
    private int lookTime;
    protected final float probability;
    private final boolean onlyHorizontal;
    protected final Class<? extends LivingEntity> lookAtType;
    protected final TargetingConditions lookAtContext;

    public ChimaeraLookGoal(Mob mob, Class<? extends LivingEntity> lookAtType, float lookDistance) {
        this(mob, lookAtType, lookDistance, 0.02F);
    }

    public ChimaeraLookGoal(Mob mob, Class<? extends LivingEntity> lookAtType, float lookDistance, float probability) {
        this(mob, lookAtType, lookDistance, probability, false);
    }

    public ChimaeraLookGoal(Mob mob, Class<? extends LivingEntity> lookAtType, float lookDistance, float probability, boolean onlyHorizontal) {
        this.mob = mob;
        this.lookAtType = lookAtType;
        this.lookDistance = lookDistance;
        this.probability = probability;
        this.onlyHorizontal = onlyHorizontal;
        this.setFlags(EnumSet.of(Flag.LOOK));
        if (lookAtType == LivingEntity.class) {
            Predicate<Entity> predicate = EntitySelector.notRiding(mob);
            this.lookAtContext = TargetingConditions.forNonCombat().range(lookDistance).selector((p_375729_, p_375730_) -> predicate.test(p_375729_));
        } else {
            this.lookAtContext = TargetingConditions.forNonCombat().range(lookDistance);
        }

    }

    public LivingEntity findNearestEntity() {
        BlockPos pos = this.mob.getOnPos();
        AABB searchBox = new AABB(pos).inflate(this.lookDistance, 5.0F, this.lookDistance);
        List<LivingEntity> entities = this.mob.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        entities.removeIf(entity -> !this.mob.hasLineOfSight(entity));
        this.mob.getPassengers().forEach(entities::remove);
        if (this.mob.getVehicle() != null) {
            if (entities.size() > 1) entities.remove(((Mob) this.mob.getVehicle()).getTarget());
            this.mob.getVehicle().getPassengers().forEach(entities::remove);
            entities.remove((LivingEntity) this.mob.getVehicle());
        }


        LivingEntity nearestEntity = null;
        double minDistance = Double.MAX_VALUE;

        Vec3 targetPosition = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        for (LivingEntity entity : entities) {
            double distance = entity.distanceToSqr(targetPosition);
            if (distance < minDistance) {
                minDistance = distance;
                nearestEntity = entity;
            }
        }

        return nearestEntity;
    }

    public boolean canUse() {
        if (this.mob.getRandom().nextFloat() >= this.probability) {
            return false;
        } else {
            if (this.mob.getTarget() != null) {
                this.lookAt = this.mob.getTarget();
            }
            this.lookAt = findNearestEntity();

            return this.lookAt != null;
        }
    }

    public boolean canContinueToUse() {
        assert this.lookAt != null;
        if (!this.lookAt.isAlive()) {
            return false;
        } else {
            return !(this.mob.distanceToSqr(this.lookAt) > (double) (this.lookDistance * this.lookDistance)) && this.lookTime > 0;
        }
    }

    public void start() {
        this.lookTime = this.adjustedTickDelay(40 + this.mob.getRandom().nextInt(40));
    }

    public void stop() {
        this.lookAt = null;
    }

    public void tick() {
        assert this.lookAt != null;
        if (this.lookAt.isAlive()) {
            double d0 = this.onlyHorizontal ? this.mob.getEyeY() : this.lookAt.getEyeY();
            this.mob.getLookControl().setLookAt(this.lookAt.getX(), d0, this.lookAt.getZ());
            --this.lookTime;
        }

    }
}
