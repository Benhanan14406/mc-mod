package com.Benhanan14406.dragon.entities.misc;

import com.Benhanan14406.dragon.BensBeastiary;
import com.google.common.collect.Maps;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class FireCloud extends Entity implements TraceableEntity {
    private static final int TIME_BETWEEN_APPLICATIONS = 5;
    private static final EntityDataAccessor<Float> DATA_RADIUS;
    private static final EntityDataAccessor<Boolean> DATA_WAITING;
    private static final EntityDataAccessor<ParticleOptions> DATA_PARTICLE;
    private static final float MAX_RADIUS = 32.0F;
    private static final int DEFAULT_AGE = 0;
    private static final int DEFAULT_DURATION_ON_USE = 0;
    private static final float DEFAULT_RADIUS_ON_USE = 0.0F;
    private static final float DEFAULT_RADIUS_PER_TICK = 0.0F;
    private static final float DEFAULT_POTION_DURATION_SCALE = 1.0F;
    private static final float MINIMAL_RADIUS = 0.5F;
    private static final float DEFAULT_RADIUS = 3.0F;
    public static final float DEFAULT_WIDTH = 6.0F;
    public static final float HEIGHT = 0.5F;
    public static final int INFINITE_DURATION = -1;
    public static final int DEFAULT_LINGERING_DURATION = 600;
    private static final int DEFAULT_WAIT_TIME = 20;
    private static final int DEFAULT_REAPPLICATION_DELAY = 20;
    private static final ColorParticleOption DEFAULT_PARTICLE;
    @Nullable
    private ParticleOptions customParticle;
    private final Map<Entity, Integer> victims;
    private int duration;
    private int waitTime;
    private int reapplicationDelay;
    private int durationOnUse;
    private float radiusOnUse;
    private float radiusPerTick;
    @Nullable
    private EntityReference<LivingEntity> owner;

    public FireCloud(EntityType<? extends FireCloud> type, Level level) {
        super(type, level);
        this.victims = Maps.newHashMap();
        this.duration = -1;
        this.waitTime = 20;
        this.reapplicationDelay = 20;
        this.durationOnUse = 0;
        this.radiusOnUse = 0.0F;
        this.radiusPerTick = 0.0F;
        this.noPhysics = true;
    }

    public FireCloud(Level level, double x, double y, double z) {
        this(BensBeastiary.FIRE_CLOUD.get(), level);
        this.setPos(x, y, z);
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_RADIUS, 3.0F);
        builder.define(DATA_WAITING, false);
        builder.define(DATA_PARTICLE, DEFAULT_PARTICLE);
    }

    public void setRadius(float radius) {
        if (!this.level().isClientSide) {
            this.getEntityData().set(DATA_RADIUS, Mth.clamp(radius, 0.0F, 32.0F));
        }

    }

    public void refreshDimensions() {
        double d0 = this.getX();
        double d1 = this.getY();
        double d2 = this.getZ();
        super.refreshDimensions();
        this.setPos(d0, d1, d2);
    }

    public float getRadius() {
        return this.getEntityData().get(DATA_RADIUS);
    }

    public void setCustomParticle(@Nullable ParticleOptions options) {
        this.customParticle = options;
        this.updateParticle();
    }
    private void updateParticle() {
        this.entityData.set(DATA_PARTICLE, Objects.requireNonNullElseGet(this.customParticle, () -> ColorParticleOption.create(DEFAULT_PARTICLE.getType(), ARGB.opaque(PotionContents.EMPTY.getColor()))));
    }

    public ParticleOptions getParticle() {
        return this.getEntityData().get(DATA_PARTICLE);
    }

    protected void setWaiting(boolean waiting) {
        this.getEntityData().set(DATA_WAITING, waiting);
    }

    public boolean isWaiting() {
        return this.getEntityData().get(DATA_WAITING);
    }

    public int getDuration() {
        return this.duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void tick() {
        super.tick();
        Level var2 = this.level();
        if (var2 instanceof ServerLevel serverlevel) {
            this.serverTick(serverlevel);
        } else {
            this.clientTick();
        }

        for (Entity victim : this.victims.keySet()) {
            if (victim != Objects.requireNonNull(this.getOwner()).getVehicle()
                    || victim != Objects.requireNonNull(Objects.requireNonNull(this.getOwner()).getVehicle()).getPassengers().stream().filter(passenger -> passenger != this.getOwner())
                    || victim != this.getOwner())
                victim.igniteForSeconds(1.0F);
        }

    }

    private void clientTick() {
        boolean flag = this.isWaiting();
        float f = this.getRadius();
        if (!flag || !this.random.nextBoolean()) {
            ParticleOptions particleoptions = this.getParticle();
            int i;
            float f1;
            if (flag) {
                i = 2;
                f1 = 0.2F;
            } else {
                i = Mth.ceil((float)Math.PI * f * f);
                f1 = f;
            }

            for(int j = 0; j < i; ++j) {
                float f2 = this.random.nextFloat() * ((float)Math.PI * 2F);
                float f3 = Mth.sqrt(this.random.nextFloat()) * f1;
                double d0 = this.getX() + (double)(Mth.cos(f2) * f3);
                double d1 = this.getY();
                double d2 = this.getZ() + (double)(Mth.sin(f2) * f3);
                if (particleoptions.getType() == ParticleTypes.ENTITY_EFFECT) {
                    if (flag && this.random.nextBoolean()) {
                        this.level().addAlwaysVisibleParticle(ParticleTypes.ASH, d0, d1, d2, 0.0F, 0.0F, 0.0F);
                    } else {
                        this.level().addAlwaysVisibleParticle(particleoptions, d0, d1, d2, 0.0F, 0.0F, 0.0F);
                    }
                } else if (flag) {
                    this.level().addAlwaysVisibleParticle(particleoptions, d0, d1, d2, 0.0F, 0.0F, 0.0F);
                } else {
                    this.level().addAlwaysVisibleParticle(particleoptions, d0, d1, d2, ((double)0.5F - this.random.nextDouble()) * 0.15, 0.01F, ((double)0.5F - this.random.nextDouble()) * 0.15);
                }
            }
        }

    }

    private void serverTick(ServerLevel level) {
        if (this.duration != -1 && this.tickCount - this.waitTime >= this.duration) {
            this.discard();
        } else {
            boolean flag = this.isWaiting();
            boolean flag1 = this.tickCount < this.waitTime;
            if (flag != flag1) {
                this.setWaiting(flag1);
            }

            if (!flag1) {
                float f = this.getRadius();
                if (this.radiusPerTick != 0.0F) {
                    f -= this.radiusPerTick;
                    if (f < 0.5F) {
                        this.discard();
                        return;
                    }

                    this.setRadius(f);
                }

                if (this.tickCount % 5 == 0) {
                    this.victims.entrySet().removeIf((filter) -> this.tickCount >= filter.getValue());
                }
            }
        }

    }

    public float getRadiusOnUse() {
        return this.radiusOnUse;
    }

    public void setRadiusOnUse(float radiusOnUse) {
        this.radiusOnUse = radiusOnUse;
    }

    public float getRadiusPerTick() {
        return this.radiusPerTick;
    }

    public void setRadiusPerTick(float radiusPerTick) {
        this.radiusPerTick = radiusPerTick;
    }

    public int getDurationOnUse() {
        return this.durationOnUse;
    }

    public void setDurationOnUse(int durationOnUse) {
        this.durationOnUse = durationOnUse;
    }

    public int getWaitTime() {
        return this.waitTime;
    }

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

    public void setOwner(@Nullable LivingEntity owner) {
        this.owner = owner != null ? new EntityReference<>(owner) : null;
    }

    @Nullable
    public LivingEntity getOwner() {
        return EntityReference.get(this.owner, this.level(), LivingEntity.class);
    }

    protected void readAdditionalSaveData(ValueInput valueInput) {
        this.tickCount = valueInput.getIntOr("Age", 0);
        this.duration = valueInput.getIntOr("Duration", -1);
        this.waitTime = valueInput.getIntOr("WaitTime", 20);
        this.reapplicationDelay = valueInput.getIntOr("ReapplicationDelay", 20);
        this.durationOnUse = valueInput.getIntOr("DurationOnUse", 0);
        this.radiusOnUse = valueInput.getFloatOr("RadiusOnUse", 0.0F);
        this.radiusPerTick = valueInput.getFloatOr("RadiusPerTick", 0.0F);
        this.setRadius(valueInput.getFloatOr("Radius", 3.0F));
        this.owner = EntityReference.read(valueInput, "Owner");
        this.setCustomParticle(valueInput.read("custom_particle", ParticleTypes.CODEC).orElse(null));
   }

    protected void addAdditionalSaveData(ValueOutput p_421745_) {
        p_421745_.putInt("Age", this.tickCount);
        p_421745_.putInt("Duration", this.duration);
        p_421745_.putInt("WaitTime", this.waitTime);
        p_421745_.putInt("ReapplicationDelay", this.reapplicationDelay);
        p_421745_.putInt("DurationOnUse", this.durationOnUse);
        p_421745_.putFloat("RadiusOnUse", this.radiusOnUse);
        p_421745_.putFloat("RadiusPerTick", this.radiusPerTick);
        p_421745_.putFloat("Radius", this.getRadius());
        p_421745_.storeNullable("custom_particle", ParticleTypes.CODEC, this.customParticle);
        EntityReference.store(this.owner, p_421745_, "Owner");
    }

    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key) {
        if (DATA_RADIUS.equals(key)) {
            this.refreshDimensions();
        }

        super.onSyncedDataUpdated(key);
    }

    public @NotNull PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    public @NotNull EntityDimensions getDimensions(@NotNull Pose pose) {
        return EntityDimensions.scalable(this.getRadius() * 2.0F, 0.5F);
    }

    public final boolean hurtServer(@NotNull ServerLevel serverLevel, @NotNull DamageSource source, float damage) {
        return false;
    }

    protected void applyImplicitComponents(@NotNull DataComponentGetter componentGetter) {
        super.applyImplicitComponents(componentGetter);
    }

    protected <T> boolean applyImplicitComponent(@NotNull DataComponentType<T> componentType, @NotNull T value) {
        return super.applyImplicitComponent(componentType, value);
    }

    static {
        DATA_RADIUS = SynchedEntityData.defineId(FireCloud.class, EntityDataSerializers.FLOAT);
        DATA_WAITING = SynchedEntityData.defineId(FireCloud.class, EntityDataSerializers.BOOLEAN);
        DATA_PARTICLE = SynchedEntityData.defineId(FireCloud.class, EntityDataSerializers.PARTICLE);
        DEFAULT_PARTICLE = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0);
    }
}
