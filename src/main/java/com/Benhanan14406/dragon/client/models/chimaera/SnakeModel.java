package com.Benhanan14406.dragon.client.models.chimaera;

import com.Benhanan14406.dragon.BensBeastiary;
import com.Benhanan14406.dragon.entities.chimaera.GoatHeadEntity;
import com.Benhanan14406.dragon.entities.chimaera.SnakeHeadEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class SnakeModel extends DefaultedEntityGeoModel<SnakeHeadEntity> {
    public SnakeModel() {
        super(ResourceLocation.fromNamespaceAndPath(BensBeastiary.MODID, "chimaera_snake"), true);
    }

    @Override
    public ResourceLocation getModelResource(GeoRenderState renderState) {
        return ResourceLocation.fromNamespaceAndPath(BensBeastiary.MODID, "geckolib/models/entity/chimaera/chimaera_snake.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GeoRenderState renderState) {
        return ResourceLocation.fromNamespaceAndPath(BensBeastiary.MODID, "geckolib/models/entity/chimaera/chimaera_snake.png");
    }

    @Override
    public ResourceLocation getAnimationResource(SnakeHeadEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(BensBeastiary.MODID, "geckolib/models/entity/chimaera/chimaera_snake.animation.json");
    }
}
