package com.Benhanan14406.dragon.client.models.chimaera;

import com.Benhanan14406.dragon.BensBeastiary;
import com.Benhanan14406.dragon.entities.chimaera.LionEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class LionModel extends DefaultedEntityGeoModel<LionEntity> {
    public LionModel() {
        super(ResourceLocation.fromNamespaceAndPath(BensBeastiary.MODID, "chimaera_lion"), true);
    }

    @Override
    public ResourceLocation getModelResource(GeoRenderState renderState) {
        return ResourceLocation.fromNamespaceAndPath(BensBeastiary.MODID, "geckolib/models/entity/chimaera/chimaera_lion.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GeoRenderState renderState) {
        return ResourceLocation.fromNamespaceAndPath(BensBeastiary.MODID, "geckolib/models/entity/chimaera/chimaera_lion.png");
    }

    @Override
    public ResourceLocation getAnimationResource(LionEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(BensBeastiary.MODID, "geckolib/models/entity/chimaera/chimaera_lion.animation.json");
    }
}
