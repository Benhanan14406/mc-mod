package com.Benhanan14406.dragon.datagen;

import com.Benhanan14406.dragon.BensBeastiary;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = BensBeastiary.MODID, bus = EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherClientData(GatherDataEvent.Client event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

//        generator.addProvider(true , new LootTableProvider(packOutput, Collections.emptySet(),
//                List.of(new LootTableProvider.SubProviderEntry(ModBlockLootTableProvider::new, LootContextParamSets.BLOCK)), lookupProvider));
//        generator.addProvider(true, new ModRecipeProvider.Runner(packOutput, lookupProvider));
//
//        BlockTagsProvider blockTagsProvider = new ModBlockTagProvider(packOutput, lookupProvider);
//        generator.addProvider(true, blockTagsProvider);
//        generator.addProvider(true, new ModItemTagProvider(packOutput, lookupProvider));
//
//        generator.addProvider(true, new ModDataMapProvider(packOutput, lookupProvider));
//
//        generator.addProvider(true, new ModModelProvider(packOutput));

        generator.addProvider(true, new ModDatapackProvider(packOutput, lookupProvider));
//        generator.addProvider(true, new ModGlobalLootModifierProvider(packOutput, lookupProvider));
    }

    @SubscribeEvent
    public static void gatherServerData(GatherDataEvent.Server event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

//        generator.addProvider(true, new LootTableProvider(packOutput, Collections.emptySet(),
//                List.of(new LootTableProvider.SubProviderEntry(ModBlockLootTableProvider::new, LootContextParamSets.BLOCK)), lookupProvider));
//        generator.addProvider(true, new ModRecipeProvider.Runner(packOutput, lookupProvider));
//
//        BlockTagsProvider blockTagsProvider = new ModBlockTagProvider(packOutput, lookupProvider);
//        generator.addProvider(true, blockTagsProvider);
//        generator.addProvider(true, new ModItemTagProvider(packOutput, lookupProvider));
//
//        generator.addProvider(true, new ModDataMapProvider(packOutput, lookupProvider));
//
//        generator.addProvider(true, new ModModelProvider(packOutput));

        generator.addProvider(true, new ModDatapackProvider(packOutput, lookupProvider));
//        generator.addProvider(true, new ModGlobalLootModifierProvider(packOutput, lookupProvider));
    }
}