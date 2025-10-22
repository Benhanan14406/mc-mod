package com.Benhanan14406.dragon.client.renderer.chimaera;

import com.Benhanan14406.dragon.client.models.chimaera.SnakeModel;
import com.Benhanan14406.dragon.entities.chimaera.SnakeHeadEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class SnakeRenderer <R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<SnakeHeadEntity, R> {
    public SnakeRenderer(EntityRendererProvider.Context context) {
        super(context, new SnakeModel());
    }
}
