package com.Benhanan14406.dragon.entities.ai.goal;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;
import java.util.List;

public class EatDroppedItemGoal extends Goal {
    Animal mob;
    public EatDroppedItemGoal(Animal mob) {
        this.setFlags(EnumSet.of(Flag.MOVE));
        this.mob = mob;
    }

    public boolean canUse() {
        if (!this.mob.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
            return false;
        } else if (this.mob.getTarget() == null && this.mob.getLastHurtByMob() == null) {
            List<ItemEntity> list = this.mob.level().getEntitiesOfClass(ItemEntity.class, this.mob.getBoundingBox().inflate(8.0F, 8.0F, 8.0F), itemEntity -> this.mob.isFood(itemEntity.getItem()));
            return !list.isEmpty() && this.mob.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty();
        } else {
            return false;
        }
    }

    public void tick() {
        List<ItemEntity> list = this.mob.level().getEntitiesOfClass(ItemEntity.class, this.mob.getBoundingBox().inflate(8.0F, 8.0F, 8.0F), itemEntity -> this.mob.isFood(itemEntity.getItem()));
        ItemStack itemstack = this.mob.getItemBySlot(EquipmentSlot.MAINHAND);

        if (itemstack.isEmpty() && !list.isEmpty()) {
            this.mob.getNavigation().moveTo(list.getFirst(), 1.0F);
        } else {
            if (this.mob.getHealth() < this.mob.getMaxHealth()){
                this.mob.heal(2.0F);
            }
            this.mob.swing(this.mob.swingingArm);
            this.mob.getMainHandItem().consume(this.mob.getItemBySlot(EquipmentSlot.MAINHAND).getCount(), this.mob);
        }

    }

    public void start() {
        List<ItemEntity> list = this.mob.level().getEntitiesOfClass(ItemEntity.class, this.mob.getBoundingBox().inflate(8.0F, 8.0F, 8.0F), itemEntity -> this.mob.isFood(itemEntity.getItem()));
        if (!list.isEmpty()) {
            this.mob.getNavigation().moveTo(list.getFirst(), 1.2F);
        }
    }
}
