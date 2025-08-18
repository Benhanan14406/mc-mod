package com.Benhanan14406.dragon.client.renderer.misc;

import com.Benhanan14406.dragon.entities.misc.ChimaeraFireBreath;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.jetbrains.annotations.NotNull;

public class FireBreathRenderer extends EntityRenderer<ChimaeraFireBreath, EntityRenderState> {
    public FireBreathRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}
