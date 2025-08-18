package com.Benhanan14406.dragon.events;

import com.Benhanan14406.dragon.BensBeastiary;
import com.Benhanan14406.dragon.entities.Basilisk;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;

public class BasiliskSpawnEvent {
    @SubscribeEvent
    public void onEggHitsToad(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof ThrownEgg egg)) return;
        if (!(event.getRayTraceResult() instanceof EntityHitResult hit)) return;

        if (hit.getEntity() instanceof Frog frog) { // Replace Frog with ToadEntity if custom
            Level level = frog.level();
            int spawnChance = level.random.nextInt( 10);

            if (!level.isClientSide && spawnChance == 0) {
                // Spawn Basilisk
                Basilisk basilisk = BensBeastiary.BASILISK.get().create(level, EntitySpawnReason.BREEDING);
                if (basilisk != null) {
                    basilisk.snapTo(frog.blockPosition().getCenter(), level.random.nextFloat() * 360F, 0F);
                    level.addFreshEntity(basilisk);

                    basilisk.setBaby(true);

                    // Imprint to nearest player
                    Player nearestplayer = level.getNearestPlayer(frog, 5.0F);
                    if (nearestplayer != null) {
                        basilisk.tame(nearestplayer);
                        basilisk.setCanFollow(true);
                        nearestplayer.displayClientMessage(Component.literal("You have tamed a basilisk!"), true);
                    }
                }

                // Prevent the egg's normal chicken-spawning behavior
                event.setCanceled(true);
                egg.discard();
            }
        }
    }
}
