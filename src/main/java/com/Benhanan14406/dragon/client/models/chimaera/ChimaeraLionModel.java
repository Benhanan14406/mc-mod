package com.Benhanan14406.dragon.client.models.chimaera;

import com.Benhanan14406.dragon.BensBeastiary;
import com.Benhanan14406.dragon.entities.chimaera.ChimaeraLion;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animatable.processing.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class ChimaeraLionModel extends DefaultedEntityGeoModel<ChimaeraLion> {
    public ChimaeraLionModel() {
        super(ResourceLocation.fromNamespaceAndPath(BensBeastiary.MODID, "chimaera_lion"), false);
    }

    @Override
    public ResourceLocation getModelResource(GeoRenderState renderState) {
        return ResourceLocation.fromNamespaceAndPath(BensBeastiary.MODID, "geckolib/models/entity/chimaera/chimaera_lion.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GeoRenderState renderState) {
        return ResourceLocation.fromNamespaceAndPath(BensBeastiary.MODID, "geckolib/textures/entity/chimaera/chimaera_lion.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ChimaeraLion animatable) {
        return ResourceLocation.fromNamespaceAndPath(BensBeastiary.MODID, "geckolib/animations/entity/chimaera/chimaera_lion.animation.json");
    }

    @Override
    public void setCustomAnimations(AnimationState<ChimaeraLion> animationState) {
        super.setCustomAnimations(animationState);

        if (animationState == null) return;

        GeoBone head = getAnimationProcessor().getBone("head");

        if (head != null) {
            float pitch = animationState.getData(DataTickets.ENTITY_PITCH);
            float yaw = animationState.getData(DataTickets.ENTITY_YAW);

            head.setRotX((float) (pitch * Mth.DEG_TO_RAD * 0.5));
            head.setRotY((float) (yaw * Mth.DEG_TO_RAD * 0.5));
        }
    }
}
