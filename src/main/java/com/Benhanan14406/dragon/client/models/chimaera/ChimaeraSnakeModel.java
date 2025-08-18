package com.Benhanan14406.dragon.client.models.chimaera;

import com.Benhanan14406.dragon.BensBeastiary;
import com.Benhanan14406.dragon.entities.chimaera.ChimaeraSnake;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animatable.processing.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;


public class ChimaeraSnakeModel extends DefaultedEntityGeoModel<ChimaeraSnake> {
    public ChimaeraSnakeModel() {
        super(ResourceLocation.fromNamespaceAndPath(BensBeastiary.MODID, "chimaera_snake"), false);
    }

    @Override
    public ResourceLocation getModelResource(GeoRenderState renderState) {
        return ResourceLocation.fromNamespaceAndPath(BensBeastiary.MODID, "geckolib/models/entity/chimaera/chimaera_snake.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GeoRenderState renderState) {
        return ResourceLocation.fromNamespaceAndPath(BensBeastiary.MODID, "geckolib/textures/entity/chimaera/chimaera_snake.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ChimaeraSnake animatable) {
        return ResourceLocation.fromNamespaceAndPath(BensBeastiary.MODID, "geckolib/animations/entity/chimaera/chimaera_snake.animation.json");
    }

    @Override
    public void setCustomAnimations(AnimationState<ChimaeraSnake> animationState) {
        super.setCustomAnimations(animationState);

        if (animationState == null) return;

        GeoBone body2Rot = getAnimationProcessor().getBone("body2_rot");
        GeoBone body1Rot = getAnimationProcessor().getBone("body1_rot");
        GeoBone headRot = getAnimationProcessor().getBone("head_rot");

        float pitch = animationState.getData(DataTickets.ENTITY_PITCH);
        float yaw = animationState.getData(DataTickets.ENTITY_YAW);

        body2Rot.setRotX((float) (pitch * Mth.DEG_TO_RAD * 0.2));
        body2Rot.setRotY((float) (yaw * Mth.DEG_TO_RAD * 0.2));
        body1Rot.setRotX((float) (pitch * Mth.DEG_TO_RAD * 0.4));
        body1Rot.setRotY((float) (yaw * Mth.DEG_TO_RAD  * 0.4));
        headRot.setRotX((float) (pitch * Mth.DEG_TO_RAD * 0.4));
        headRot.setRotY((float) (yaw * Mth.DEG_TO_RAD  * 0.4));
    }
}
