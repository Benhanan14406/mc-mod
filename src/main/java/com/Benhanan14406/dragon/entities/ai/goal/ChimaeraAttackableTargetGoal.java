package com.Benhanan14406.dragon.entities.ai.goal;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class ChimaeraAttackableTargetGoal<T extends LivingEntity> extends TargetGoal {
    private static final int DEFAULT_RANDOM_INTERVAL = 10;
    protected final Class<T> targetType;
    protected final int randomInterval;
    @Nullable
    protected LivingEntity target;
    protected TargetingConditions targetConditions;

    public ChimaeraAttackableTargetGoal(Mob mob, Class<T> targetType, boolean mustSee) {
        this(mob, targetType, 10, mustSee, false, null);
    }

    public ChimaeraAttackableTargetGoal(Mob mob, Class<T> targetType, boolean mustSee, TargetingConditions.Selector selector) {
        this(mob, targetType, 10, mustSee, false, selector);
    }

    public ChimaeraAttackableTargetGoal(Mob mob, Class<T> targetType, boolean mustSee, boolean mustReach) {
        this(mob, targetType, 10, mustSee, mustReach, null);
    }

    public ChimaeraAttackableTargetGoal(Mob mob, Class<T> targetType, int interval, boolean mustSee, boolean mustReach, @Nullable TargetingConditions.Selector selector) {
        super(mob, mustSee, mustReach);
        this.targetType = targetType;
        this.randomInterval = reducedTickDelay(interval);
        this.setFlags(EnumSet.of(Flag.TARGET));
        this.targetConditions = TargetingConditions.forCombat().range(this.getFollowDistance());
    }

    public boolean canUse() {
        if (this.randomInterval > 0 && this.mob.getRandom().nextInt(this.randomInterval) != 0) {
            return false;
        } else {
            this.findTarget();
            return this.target != null;
        }
    }

    public List<LivingEntity> getNearbyEntities() {
        BlockPos pos = this.mob.getOnPos();
        List<LivingEntity> nearbyEntities = new ArrayList<>();
        Level level = this.mob.level();
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(getFollowDistance()));
        entities.removeIf(entity -> !this.mob.hasLineOfSight(entity));

        for (LivingEntity entity : entities) {
            double distance = entity.distanceToSqr(pos.getCenter());
            if (distance <= getFollowDistance()) {
                nearbyEntities.add(entity);
            }
        }

        return nearbyEntities;
    }

    protected void findTarget() {
        List<LivingEntity> entities = getNearbyEntities();
        entities.removeIf(livingEntity -> livingEntity.equals(this.mob));
        this.mob.getPassengers().forEach(entities::remove);
        if (this.mob.getVehicle() != null) {
            if (entities.size() > 1) entities.remove(((Mob) this.mob.getVehicle()).getTarget());
            this.mob.getVehicle().getPassengers().forEach(entities::remove);
            entities.remove((LivingEntity) this.mob.getVehicle());
        }

        List<LivingEntity> players = new ArrayList<>(entities.stream().filter(livingEntity -> livingEntity instanceof Player).toList());

        if (!players.isEmpty()) {
            this.target = players.getFirst();
        } else if (!entities.isEmpty()) {
            this.target = entities.getFirst();
        } else {
            this.target = null;
        }
    }

    public void start() {
        this.mob.setTarget(this.target);
        super.start();
    }
}
