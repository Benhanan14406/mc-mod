package com.Benhanan14406.dragon.client.renderer;

import com.Benhanan14406.dragon.client.models.BasiliskChickModel;
import com.Benhanan14406.dragon.client.models.BasiliskModel;
import com.Benhanan14406.dragon.entities.basilisk.Basilisk;
import com.Benhanan14406.dragon.entities.basilisk.BasiliskChick;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class BasiliskChickRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<BasiliskChick, R> {
    public BasiliskChickRenderer(EntityRendererProvider.Context context) {
        super(context, new BasiliskChickModel());
    }
}
