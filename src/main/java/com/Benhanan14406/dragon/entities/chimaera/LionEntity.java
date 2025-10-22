package com.Benhanan14406.dragon.entities.chimaera;

import com.Benhanan14406.dragon.BensBeastiary;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class LionEntity extends Monster implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private GoatHeadEntity goat;
    private SnakeHeadEntity snake;

    public LionEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 16.0F)
                .add(Attributes.MOVEMENT_SPEED, 0.19F)
                .add(Attributes.ATTACK_DAMAGE, 4.0F)
                .add(Attributes.FOLLOW_RANGE, 20.0F);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData spawnGroupData) {
        return super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.4, false));
        this.goalSelector.addGoal(2, new RandomStrollGoal(this, 1.0));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();

        // ensure goat & snake exist
        if (!level().isClientSide) {

            goat = BensBeastiary.GOAT.get().create(level(), EntitySpawnReason.JOCKEY);
            if (goat != null && !goat.isAlive()) {
                goat.setParent(this);
                level().addFreshEntity(goat);
            }

            snake = BensBeastiary.SNAKE.get().create(level(), EntitySpawnReason.JOCKEY);
            if (snake != null && !snake.isAlive()) {
                snake.setParent(this);
                level().addFreshEntity(snake);
            }

            // update positions
            goat.setPos(this.getX(), this.getY() + 1.2, this.getZ() - 0.5);
            snake.setPos(this.getX(), this.getY() + 0.4, this.getZ() + 1.2);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("controller", 5, state ->
                state.setAndContinue(RawAnimation.begin().thenLoop("walk"))
        ));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
}

