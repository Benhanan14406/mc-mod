package com.Benhanan14406.dragon.events;

import com.Benhanan14406.dragon.BensBeastiary;
import com.Benhanan14406.dragon.entities.basilisk.Basilisk;
import com.Benhanan14406.dragon.entities.basilisk.BasiliskChick;
import net.minecraft.core.particles.ParticleTypes;
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
                BasiliskChick basiliskChick = BensBeastiary.BASILISK_CHICK.get().create(level, EntitySpawnReason.BREEDING);
                if (basiliskChick != null) {
                    basiliskChick.snapTo(frog.blockPosition().getCenter(), level.random.nextFloat() * 360F, 0F);
                    level.addFreshEntity(basiliskChick);
                    basiliskChick.level().addParticle(ParticleTypes.EGG_CRACK, frog.blockPosition().getCenter().x, frog.blockPosition().getCenter().y, frog.blockPosition().getCenter().z, 1.0F, 1.0F, 1.0F);
                    basiliskChick.level().addParticle(ParticleTypes.MYCELIUM, frog.blockPosition().getCenter().x, frog.blockPosition().getCenter().y, frog.blockPosition().getCenter().z, 1.0F, 1.0F, 1.0F);
                    basiliskChick.setBaby(true);

                    // Imprint to nearest player
                    Player nearestplayer = level.getNearestPlayer(frog, 5.0F);
                    if (nearestplayer != null) {
                        basiliskChick.tame(nearestplayer);
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
