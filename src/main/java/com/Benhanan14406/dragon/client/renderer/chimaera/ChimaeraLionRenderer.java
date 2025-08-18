package com.Benhanan14406.dragon.client.renderer.chimaera;

import com.Benhanan14406.dragon.client.models.chimaera.ChimaeraLionModel;
import com.Benhanan14406.dragon.entities.chimaera.ChimaeraLion;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class ChimaeraLionRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<ChimaeraLion, R> {
    public ChimaeraLionRenderer(EntityRendererProvider.Context context) {
        super(context, new ChimaeraLionModel());
    }
}
