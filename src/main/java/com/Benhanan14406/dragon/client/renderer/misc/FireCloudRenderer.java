package com.Benhanan14406.dragon.client.renderer.misc;

import com.Benhanan14406.dragon.entities.misc.FireCloud;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.jetbrains.annotations.NotNull;

public class FireCloudRenderer extends EntityRenderer<FireCloud, EntityRenderState> {
    public FireCloudRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}
