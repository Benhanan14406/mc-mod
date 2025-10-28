package com.Benhanan14406.dragon;

import com.Benhanan14406.dragon.client.renderer.BasiliskChickRenderer;
import com.Benhanan14406.dragon.client.renderer.BasiliskRenderer;
import com.Benhanan14406.dragon.client.renderer.chimaera.GoatRenderer;
import com.Benhanan14406.dragon.client.renderer.chimaera.LionRenderer;
import com.Benhanan14406.dragon.client.renderer.chimaera.SnakeRenderer;
import com.Benhanan14406.dragon.client.renderer.misc.FireBreathRenderer;
import com.Benhanan14406.dragon.client.renderer.misc.FireCloudRenderer;
import com.Benhanan14406.dragon.entities.basilisk.Basilisk;
import com.Benhanan14406.dragon.entities.basilisk.BasiliskChick;
import com.Benhanan14406.dragon.entities.chimaera.GoatHeadEntity;
import com.Benhanan14406.dragon.entities.chimaera.LionEntity;
import com.Benhanan14406.dragon.entities.chimaera.SnakeHeadEntity;
import com.Benhanan14406.dragon.entities.misc.ChimaeraFireBreath;
import com.Benhanan14406.dragon.entities.misc.FireCloud;
import com.Benhanan14406.dragon.events.BasiliskSpawnEvent;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(BensBeastiary.MODID)
public class BensBeastiary {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "dragon";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "examplemod" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    /// Entity registry
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, MODID);;
    // Basilisk
    public static ResourceKey<EntityType<?>> BASILISK_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.withDefaultNamespace("basilisk"));
    public static final DeferredHolder<EntityType<?>, EntityType<Basilisk>> BASILISK = ENTITIES.register("basilisk",
            () -> EntityType.Builder.of(Basilisk::new, MobCategory.CREATURE).sized(1.0f, 1.0f).build(BASILISK_KEY));

    // Basilisk Chick
    public static ResourceKey<EntityType<?>> BASILISK_CHICK_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.withDefaultNamespace("basilisk_chick"));
    public static final DeferredHolder<EntityType<?>, EntityType<BasiliskChick>> BASILISK_CHICK = ENTITIES.register("basilisk_chick",
            () -> EntityType.Builder.of(BasiliskChick::new, MobCategory.CREATURE).sized(0.5f, 0.5f).build(BASILISK_CHICK_KEY));

    // Chimera - Lion
    public static ResourceKey<EntityType<?>> LION_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.withDefaultNamespace("lion"));
    public static final DeferredHolder<EntityType<?>, EntityType<LionEntity>> LION = ENTITIES.register("lion",
            () -> EntityType.Builder.of(LionEntity::new, MobCategory.CREATURE).sized(0.75f, 1.0f).build(LION_KEY));

    // Chimera - Goat
    public static ResourceKey<EntityType<?>> GOAT_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.withDefaultNamespace("goat"));
    public static final DeferredHolder<EntityType<?>, EntityType<GoatHeadEntity>> GOAT = ENTITIES.register("goat",
            () -> EntityType.Builder.of(GoatHeadEntity::new, MobCategory.MONSTER).sized(0.5f, 0.5f).build(GOAT_KEY));

    // Chimera - Lion
    public static ResourceKey<EntityType<?>> SNAKE_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.withDefaultNamespace("snake"));
    public static final DeferredHolder<EntityType<?>, EntityType<SnakeHeadEntity>> SNAKE = ENTITIES.register("snake",
            () -> EntityType.Builder.of(SnakeHeadEntity::new, MobCategory.MONSTER).sized(0.75f, 0.5f).build(SNAKE_KEY));


    // Fire Breath effect
    public static ResourceKey<EntityType<?>> FIRE_BREATH_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.withDefaultNamespace("fire_breath"));
    public static final DeferredHolder<EntityType<?>, EntityType<ChimaeraFireBreath>> FIRE_BREATH = ENTITIES.register("fire_breath",
            () -> EntityType.Builder.<ChimaeraFireBreath>of(ChimaeraFireBreath::new, MobCategory.MISC).noLootTable().fireImmune().sized(1.0F, 1.0F).clientTrackingRange(4).updateInterval(10).build(FIRE_BREATH_KEY));

    // Fire Cloud effect
    public static ResourceKey<EntityType<?>> FIRE_CLOUD_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.withDefaultNamespace("fire_cloud"));
    public static final DeferredHolder<EntityType<?>, EntityType<FireCloud>> FIRE_CLOUD = ENTITIES.register("fire_cloud",
            () -> EntityType.Builder.<FireCloud>of(FireCloud::new, MobCategory.MISC).noLootTable().fireImmune().sized(6.0F, 0.5F).clientTrackingRange(10).updateInterval(Integer.MAX_VALUE).build(FIRE_CLOUD_KEY));


    /// Block registry
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
//    public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock("example_block", BlockBehaviour.Properties.of().mapColor(MapColor.STONE));

    /// Item registry
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
//    public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK);

    // Basilisk Spawn Egg
    public static final DeferredItem<SpawnEggItem> BASILISK_SPAWN_EGG = ITEMS.registerItem("basilisk_spawn_egg",
            props -> new SpawnEggItem(BASILISK.get(), props));

    // Basilisk Blood
    public static final DeferredItem<Item> BASILISK_BLOOD = ITEMS.registerSimpleItem("basilisk_blood",
            new Item.Properties());

    // Goggles
    public static final DeferredItem<Item> GOGGLES = ITEMS.registerSimpleItem("goggles",
            new Item.Properties().equippable(EquipmentSlot.HEAD));


    // Creates a creative tab with the id "examplemod:example_tab" for the example item, that is placed after the combat tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.dragon")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> BASILISK_SPAWN_EGG.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(BASILISK_SPAWN_EGG.get());
                output.accept(BASILISK_BLOOD.get());
                output.accept(GOGGLES.get());
            }).build());

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public BensBeastiary(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks/entities/items get registered
        ENTITIES.register(modEventBus);
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);

        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        IEventBus bus = NeoForge.EVENT_BUS;
        bus.register(this);
        bus.register(new BasiliskSpawnEvent());

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
//            event.accept(EXAMPLE_BLOCK_ITEM);
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());

            // Renders entities
            EntityRenderers.register(FIRE_BREATH.get(), FireBreathRenderer::new);
            EntityRenderers.register(FIRE_CLOUD.get(), FireCloudRenderer::new);

            EntityRenderers.register(BASILISK.get(), BasiliskRenderer::new);
            EntityRenderers.register(BASILISK_CHICK.get(), BasiliskChickRenderer::new);

            EntityRenderers.register(LION.get(), LionRenderer::new);
            EntityRenderers.register(GOAT.get(), GoatRenderer::new);
            EntityRenderers.register(SNAKE.get(), SnakeRenderer::new);
        }

        @SubscribeEvent
        public static void registerAttributes(EntityAttributeCreationEvent event) {
            event.put(BASILISK.get(), Basilisk.createAttributes().build());
            event.put(BASILISK_CHICK.get(), BasiliskChick.createAttributes().build());
            event.put(LION.get(), LionEntity.createAttributes().build());
            event.put(GOAT.get(), GoatHeadEntity.createAttributes().build());
            event.put(SNAKE.get(), SnakeHeadEntity.createAttributes().build());
        }

        @SubscribeEvent
        public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
            event.register(BASILISK.get(),
                    SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    Animal::checkAnimalSpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE);

            event.register(BASILISK_CHICK.get(),
                    SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    Animal::checkAnimalSpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE);

            event.register(LION.get(),
                    SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    Monster::checkMobSpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE);

            event.register(GOAT.get(),
                    SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    Monster::checkMobSpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE);

            event.register(SNAKE.get(),
                    SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    Monster::checkMobSpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE);
        }
    }
}
