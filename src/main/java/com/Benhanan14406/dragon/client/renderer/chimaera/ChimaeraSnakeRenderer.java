package com.Benhanan14406.dragon.client.renderer.chimaera;

import com.Benhanan14406.dragon.client.models.chimaera.ChimaeraGoatModel;
import com.Benhanan14406.dragon.client.models.chimaera.ChimaeraSnakeModel;
import com.Benhanan14406.dragon.entities.chimaera.ChimaeraGoat;
import com.Benhanan14406.dragon.entities.chimaera.ChimaeraSnake;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class ChimaeraSnakeRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<ChimaeraSnake, R> {
    public ChimaeraSnakeRenderer(EntityRendererProvider.Context context) {
        super(context, new ChimaeraSnakeModel());
    }
}
