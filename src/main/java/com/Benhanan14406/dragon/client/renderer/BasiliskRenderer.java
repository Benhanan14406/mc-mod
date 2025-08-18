package com.Benhanan14406.dragon.client.renderer;

import com.Benhanan14406.dragon.client.models.BasiliskModel;
import com.Benhanan14406.dragon.entities.Basilisk;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class BasiliskRenderer <R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<Basilisk, R> {
    public BasiliskRenderer(EntityRendererProvider.Context context) {
        super(context, new BasiliskModel());
    }
}
