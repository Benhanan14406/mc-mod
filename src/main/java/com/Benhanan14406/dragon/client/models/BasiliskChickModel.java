package com.Benhanan14406.dragon.client.models;

import com.Benhanan14406.dragon.BensBeastiary;
import com.Benhanan14406.dragon.entities.basilisk.BasiliskChick;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animatable.processing.AnimationState;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class BasiliskChickModel extends DefaultedEntityGeoModel<BasiliskChick> {
     public BasiliskChickModel() {
        super(ResourceLocation.fromNamespaceAndPath(BensBeastiary.MODID, "basilisk_chick"), true);
    }

    @Override
    public ResourceLocation getTextureResource(GeoRenderState renderState) {
        return ResourceLocation.fromNamespaceAndPath(BensBeastiary.MODID, "geckolib/textures/entity/basilisk/basilisk_chick.png");
    }

    @Override
    public void setCustomAnimations(AnimationState<BasiliskChick> animationState) {
        super.setCustomAnimations(animationState);

        this.getBone("head").ifPresent(head -> {
            head.setScaleX(1.01F);
            head.setScaleY(1.01F);
            head.setScaleZ(1.01F);
        });

    }
}
