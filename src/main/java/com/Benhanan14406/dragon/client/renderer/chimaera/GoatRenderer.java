package com.Benhanan14406.dragon.client.renderer.chimaera;

import com.Benhanan14406.dragon.client.models.chimaera.GoatModel;
import com.Benhanan14406.dragon.entities.chimaera.GoatHeadEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class GoatRenderer <R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<GoatHeadEntity, R> {
    public GoatRenderer(EntityRendererProvider.Context context) {
        super(context, new GoatModel());
    }
}
