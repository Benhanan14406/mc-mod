package com.Benhanan14406.dragon.client.renderer.chimaera;

import com.Benhanan14406.dragon.client.models.chimaera.LionModel;
import com.Benhanan14406.dragon.entities.chimaera.LionEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class LionRenderer <R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<LionEntity, R> {
    public LionRenderer(EntityRendererProvider.Context context) {
        super(context, new LionModel());
    }
}
