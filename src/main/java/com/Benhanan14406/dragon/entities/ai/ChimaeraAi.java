package com.Benhanan14406.dragon.entities.ai;

import com.Benhanan14406.dragon.entities.chimaera.ChimaeraLion;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.warden.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

import java.util.List;

public class ChimaeraAi {
    private static final float SPEED_MULTIPLIER_WHEN_IDLING = 0.5F;
    private static final float SPEED_MULTIPLIER_WHEN_INVESTIGATING = 0.7F;
    private static final float SPEED_MULTIPLIER_WHEN_FIGHTING = 1.2F;
    private static final int MELEE_ATTACK_COOLDOWN = 18;
    private static final int DIGGING_DURATION = Mth.ceil(100.0F);
    public static final int EMERGE_DURATION = Mth.ceil(133.59999F);
    public static final int ROAR_DURATION = Mth.ceil(84.0F);
    private static final int SNIFFING_DURATION = Mth.ceil(83.2F);
    public static final int DIGGING_COOLDOWN = 1200;
    private static final int DISTURBANCE_LOCATION_EXPIRY_TIME = 100;
    private static final List<SensorType<? extends Sensor<? super ChimaeraLion>>> SENSOR_TYPES;
    private static final List<MemoryModuleType<?>> MEMORY_TYPES;
//    private static final BehaviorControl<ChimaeraLion> DIG_COOLDOWN_SETTER;

    public static void updateActivity(ChimaeraLion chimaera) {
        chimaera.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.EMERGE, Activity.DIG, Activity.ROAR, Activity.FIGHT, Activity.INVESTIGATE, Activity.SNIFF, Activity.IDLE));
    }

    public static Brain<?> makeBrain(ChimaeraLion chimaera, Dynamic<?> ops) {
        Brain.Provider<ChimaeraLion> provider = Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
        Brain<ChimaeraLion> brain = provider.makeBrain(ops);
//        initCoreActivity(brain);
//        initEmergeActivity(brain);
//        initDiggingActivity(brain);
//        initIdleActivity(brain);
//        initRoarActivity(brain);
//        initFightActivity(chimaera, brain);
//        initInvestigateActivity(brain);
//        initSniffingActivity(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

//    private static void initCoreActivity(Brain<ChimaeraLion> brain) {
//        brain.addActivity(Activity.CORE, 0, ImmutableList.of(new Swim(0.8F), SetWardenLookTarget.create(), new LookAtTargetSink(45, 90), new MoveToTargetSink()));
//    }
//
//    private static void initEmergeActivity(Brain<ChimaeraLion> brain) {
//        brain.addActivityAndRemoveMemoryWhenStopped(Activity.EMERGE, 5, ImmutableList.of(new Emerging(EMERGE_DURATION)), MemoryModuleType.IS_EMERGING);
//    }
//
//    private static void initDiggingActivity(Brain<ChimaeraLion> brain) {
//        brain.addActivityWithConditions(Activity.DIG, ImmutableList.of(Pair.of(0, new ForceUnmount()), Pair.of(1, new Digging(DIGGING_DURATION))), ImmutableSet.of(Pair.of(MemoryModuleType.ROAR_TARGET, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.DIG_COOLDOWN, MemoryStatus.VALUE_ABSENT)));
//    }
//
//    private static void initIdleActivity(Brain<ChimaeraLion> brain) {
//        brain.addActivity(Activity.IDLE, 10, ImmutableList.of(SetRoarTarget.create(ChimaeraLion::getEntityAngryAt), TryToSniff.create(), new RunOne(ImmutableMap.of(MemoryModuleType.IS_SNIFFING, MemoryStatus.VALUE_ABSENT), ImmutableList.of(Pair.of(RandomStroll.stroll(0.5F), 2), Pair.of(new DoNothing(30, 60), 1)))));
//    }
//
//    private static void initInvestigateActivity(Brain<ChimaeraLion> brain) {
//        brain.addActivityAndRemoveMemoryWhenStopped(Activity.INVESTIGATE, 5, ImmutableList.of(SetRoarTarget.create(Warden::getEntityAngryAt), GoToTargetLocation.create(MemoryModuleType.DISTURBANCE_LOCATION, 2, 0.7F)), MemoryModuleType.DISTURBANCE_LOCATION);
//    }
//
//    private static void initSniffingActivity(Brain<ChimaeraLion> brain) {
//        brain.addActivityAndRemoveMemoryWhenStopped(Activity.SNIFF, 5, ImmutableList.of(SetRoarTarget.create(Warden::getEntityAngryAt), new Sniffing(SNIFFING_DURATION)), MemoryModuleType.IS_SNIFFING);
//    }
//
//    private static void initRoarActivity(Brain<ChimaeraLion> brain) {
//        brain.addActivityAndRemoveMemoryWhenStopped(Activity.ROAR, 10, ImmutableList.of(new Roar()), MemoryModuleType.ROAR_TARGET);
//    }
//
//    private static void initFightActivity(ChimaeraLion chimaera, Brain<ChimaeraLion> brain) {
//        brain.addActivityAndRemoveMemoryWhenStopped(
//                Activity.FIGHT, 10,
//                ImmutableList.of(DIG_COOLDOWN_SETTER,
//                        StopAttackingIfTargetInvalid.create((p_376299_, p_219540_) ->
//                                !chimaera.getAngerLevel().isAngry() ||
//                                        !chimaera.canTargetEntity(p_219540_),
//                                ChimaeraAi::onTargetInvalid, false),
//                        SetEntityLookTarget.create((p_219535_) ->
//                                isTarget(chimaera, p_219535_),
//                                (float)chimaera.getAttributeValue(Attributes.FOLLOW_RANGE)),
//                        SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.2F), new SonicBoom(), MeleeAttack.create(18)),
//                MemoryModuleType.ATTACK_TARGET);
//    }
//
//    private static boolean isTarget(ChimaeraLion chimaera, LivingEntity entity) {
//        return chimaera.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).filter((p_219509_) -> p_219509_ == entity).isPresent();
//    }
//
//    private static void onTargetInvalid(ServerLevel level, ChimaeraLion chimaera, LivingEntity target) {
//        if (!chimaera.canTargetEntity(target)) {
//            chimaera.clearAnger(target);
//        }
//
//        setDigCooldown(chimaera);
//    }
//
//    public static void setDigCooldown(LivingEntity entity) {
//        if (entity.getBrain().hasMemoryValue(MemoryModuleType.DIG_COOLDOWN)) {
//            entity.getBrain().setMemoryWithExpiry(MemoryModuleType.DIG_COOLDOWN, Unit.INSTANCE, 1200L);
//        }
//
//    }
//
//    public static void setDisturbanceLocation(ChimaeraLion chimaera, BlockPos disturbanceLocation) {
//        if (chimaera.level().getWorldBorder().isWithinBounds(disturbanceLocation) && !chimaera.getEntityAngryAt().isPresent() && chimaera.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).isEmpty()) {
//            setDigCooldown(chimaera);
//            chimaera.getBrain().setMemoryWithExpiry(MemoryModuleType.SNIFF_COOLDOWN, Unit.INSTANCE, 100L);
//            chimaera.getBrain().setMemoryWithExpiry(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(disturbanceLocation), 100L);
//            chimaera.getBrain().setMemoryWithExpiry(MemoryModuleType.DISTURBANCE_LOCATION, disturbanceLocation, 100L);
//            chimaera.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
//        }
//
//    }
//
    static {
        SENSOR_TYPES = List.of(SensorType.NEAREST_LIVING_ENTITIES);
        MEMORY_TYPES = List.of(MemoryModuleType.NEAREST_LIVING_ENTITIES,
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                MemoryModuleType.NEAREST_VISIBLE_PLAYER,
                MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
                MemoryModuleType.NEAREST_VISIBLE_NEMESIS,
                MemoryModuleType.LOOK_TARGET,
                MemoryModuleType.WALK_TARGET,
                MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
                MemoryModuleType.PATH,
                MemoryModuleType.ATTACK_TARGET,
                MemoryModuleType.ATTACK_COOLING_DOWN,
                MemoryModuleType.NEAREST_ATTACKABLE,
                MemoryModuleType.ROAR_TARGET,
                MemoryModuleType.ROAR_SOUND_DELAY,
                MemoryModuleType.ROAR_SOUND_COOLDOWN);
    }
}
