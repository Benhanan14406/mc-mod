package com.Benhanan14406.dragon.client.models;

import com.Benhanan14406.dragon.BensBeastiary;
import com.Benhanan14406.dragon.entities.basilisk.Basilisk;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.processing.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

import java.util.Objects;

public class BasiliskModel extends DefaultedEntityGeoModel<Basilisk> {
    ResourceLocation TEMPERATE_NORMAL = ResourceLocation.fromNamespaceAndPath(BensBeastiary.MODID, "geckolib/textures/entity/basilisk/temperate_basilisk.png");
    ResourceLocation TEMPERATE_HAS_GOGGLES = ResourceLocation.fromNamespaceAndPath(BensBeastiary.MODID, "geckolib/textures/entity/basilisk/temperate_basilisk_has_goggles.png");
    ResourceLocation TEMPERATE_DECAPITATED = ResourceLocation.fromNamespaceAndPath(BensBeastiary.MODID, "geckolib/textures/entity/basilisk/temperate_basilisk_decapitated.png");
    ResourceLocation WARM_NORMAL = ResourceLocation.fromNamespaceAndPath(BensBeastiary.MODID, "geckolib/textures/entity/basilisk/warm_basilisk.png");
    ResourceLocation WARM_HAS_GOGGLES = ResourceLocation.fromNamespaceAndPath(BensBeastiary.MODID, "geckolib/textures/entity/basilisk/warm_basilisk_has_goggles.png");
    ResourceLocation WARM_DECAPITATED = ResourceLocation.fromNamespaceAndPath(BensBeastiary.MODID, "geckolib/textures/entity/basilisk/warm_basilisk_decapitated.png");
    ResourceLocation COLD_NORMAL = ResourceLocation.fromNamespaceAndPath(BensBeastiary.MODID, "geckolib/textures/entity/basilisk/cold_basilisk.png");
    ResourceLocation COLD_HAS_GOGGLES = ResourceLocation.fromNamespaceAndPath(BensBeastiary.MODID, "geckolib/textures/entity/basilisk/cold_basilisk_has_goggles.png");
    ResourceLocation COLD_DECAPITATED = ResourceLocation.fromNamespaceAndPath(BensBeastiary.MODID, "geckolib/textures/entity/basilisk/cold_basilisk_decapitated.png");

    public BasiliskModel() {
        super(ResourceLocation.fromNamespaceAndPath(BensBeastiary.MODID, "basilisk"), false);
    }

    @Override
    public void prepareForRenderPass(Basilisk animatable, GeoRenderState renderState) {
        super.prepareForRenderPass(animatable, renderState);

        renderState.addGeckolibData(DataTickets.SPRINTING, animatable.isDecapitated());
        renderState.addGeckolibData(DataTickets.IS_CROUCHING, animatable.hasGoggles());
        renderState.addGeckolibData(DataTickets.MAX_DURABILITY, animatable.getBasiliskType());
    }

    @Override
    public ResourceLocation getTextureResource(GeoRenderState renderState) {
        if (Objects.equals(renderState.getGeckolibData(DataTickets.MAX_DURABILITY), 3)) {
            if (Boolean.TRUE.equals(renderState.getGeckolibData(DataTickets.SPRINTING))) {
                return COLD_DECAPITATED;
            } else if (Boolean.TRUE.equals(renderState.getGeckolibData(DataTickets.IS_CROUCHING))) {
                return COLD_HAS_GOGGLES;
            } else {
                return COLD_NORMAL;
            }
        } else if (Objects.equals(renderState.getGeckolibData(DataTickets.MAX_DURABILITY), 2)) {
            if (Boolean.TRUE.equals(renderState.getGeckolibData(DataTickets.SPRINTING))) {
                return WARM_DECAPITATED;
            } else if (Boolean.TRUE.equals(renderState.getGeckolibData(DataTickets.IS_CROUCHING))) {
                return WARM_HAS_GOGGLES;
            } else {
                return WARM_NORMAL;
            }
        } else {
            if (Boolean.TRUE.equals(renderState.getGeckolibData(DataTickets.SPRINTING))) {
                return TEMPERATE_DECAPITATED;
            } else if (Boolean.TRUE.equals(renderState.getGeckolibData(DataTickets.IS_CROUCHING))) {
                return TEMPERATE_HAS_GOGGLES;
            } else {
                return TEMPERATE_NORMAL;
            }
        }
    }

    @Override
    public @Nullable RenderType getRenderType(GeoRenderState renderState, ResourceLocation texture) {
        return RenderType.ENTITY_TRANSLUCENT.apply(texture, true);
    }

    @Override
    public void setCustomAnimations(AnimationState<Basilisk> animationState) {
        super.setCustomAnimations(animationState);

        if (animationState == null) return;

        GeoBone head = getAnimationProcessor().getBone("head");
        GeoBone neck = getAnimationProcessor().getBone("neck");

        float pitch = animationState.getData(DataTickets.ENTITY_PITCH);
        float yaw = animationState.getData(DataTickets.ENTITY_YAW);

        if (head != null) {
            head.setRotX(- pitch * Mth.DEG_TO_RAD * 0.5F);
            head.setRotY(- yaw * Mth.DEG_TO_RAD);
        }

        if (neck != null) {
            neck.setRotX(- pitch * Mth.DEG_TO_RAD * 0.25F);
            neck.setRotY(- yaw * Mth.DEG_TO_RAD * 0.5F);
        }
    }
}
