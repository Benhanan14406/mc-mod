package com.Benhanan14406.dragon.entities.misc;

import com.Benhanan14406.dragon.BensBeastiary;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class ChimaeraFireBreath extends AbstractHurtingProjectile {
    private int lifespan = 30;
    public static final float SPLASH_RANGE = 1.0F;

    public ChimaeraFireBreath(EntityType<? extends ChimaeraFireBreath> type, Level level) { super(type, level); }

    public ChimaeraFireBreath(Level level, LivingEntity owner, Vec3 movement) { super(BensBeastiary.FIRE_BREATH.get(), owner, movement, level); }

    protected void onHit(@NotNull HitResult result) {
        super.onHit(result);
        if ((result.getType() != HitResult.Type.ENTITY || !this.ownedBy(((EntityHitResult)result).getEntity())) && !this.level().isClientSide) {
            List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(4.0F, 2.0F, 4.0F));

            FireCloud fireCloud = new FireCloud(this.level(), this.getX(), this.getY(), this.getZ());
            Entity entity = this.getOwner();
            if (entity instanceof LivingEntity) {
                fireCloud.setOwner((LivingEntity) entity);
            }

            fireCloud.setCustomParticle(ParticleTypes.FLAME);
            fireCloud.setRadius(2.0F);
            fireCloud.setDuration(100);
            fireCloud.setRadiusPerTick((7.0F - fireCloud.getRadius()) / (float)fireCloud.getDuration());

            if (!list.isEmpty()) {
                for(LivingEntity livingentity : list) {
                    double d0 = this.distanceToSqr(livingentity);
                    if (d0 < (double)16.0F) {
                        if (livingentity != this.getOwner() || livingentity != this.getVehicle() || livingentity != Objects.requireNonNull(this.getOwner().getVehicle()).getPassengers().stream().filter(passenger -> passenger != this.getOwner()).toList().getFirst()){
                            fireCloud.setPos(livingentity.getX(), livingentity.getY(), livingentity.getZ());
                            livingentity.hurtOrSimulate(this.damageSources().magic(), 1.0F);
                            livingentity.knockback(1, 1, 0);
                            livingentity.igniteForSeconds(5.0F);
                            break;
                        }
                    }
                }
            }

            this.level().levelEvent(2006, this.blockPosition(), this.isSilent() ? -1 : 1);
            this.level().addFreshEntity(fireCloud);
            this.discard();
        }
    }

    protected ParticleOptions getTrailParticle() {
        return ParticleTypes.FLAME;
    }

    protected boolean shouldBurn() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();

        --lifespan;
        if (lifespan == 0) {
            this.discard();

            FireCloud fireCloud = new FireCloud(this.level(), this.getX(), this.getY(), this.getZ());
            Entity entity = this.getOwner();
            if (entity instanceof LivingEntity) {
                fireCloud.setOwner((LivingEntity) entity);
            }

            fireCloud.setCustomParticle(ParticleTypes.SMALL_FLAME);
            fireCloud.setRadius(2.0F);
            fireCloud.setDuration(50);
            fireCloud.setRadiusPerTick((7.0F - fireCloud.getRadius()) / (float)fireCloud.getDuration());
            this.level().levelEvent(2006, this.blockPosition(), this.isSilent() ? -1 : 1);
            this.level().addFreshEntity(fireCloud);
        }
    }
}
