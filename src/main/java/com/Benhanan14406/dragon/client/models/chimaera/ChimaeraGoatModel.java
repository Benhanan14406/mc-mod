package com.Benhanan14406.dragon.client.models.chimaera;

import com.Benhanan14406.dragon.BensBeastiary;
import com.Benhanan14406.dragon.entities.chimaera.ChimaeraGoat;
import com.Benhanan14406.dragon.entities.chimaera.ChimaeraLion;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animatable.processing.AnimationState;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class ChimaeraGoatModel extends DefaultedEntityGeoModel<ChimaeraGoat> {
    public ChimaeraGoatModel() {
        super(ResourceLocation.fromNamespaceAndPath(BensBeastiary.MODID, "chimaera_goat"), true);
    }

    @Override
    public ResourceLocation getModelResource(GeoRenderState renderState) {
        return ResourceLocation.fromNamespaceAndPath(BensBeastiary.MODID, "geckolib/models/entity/chimaera/chimaera_goat.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GeoRenderState renderState) {
        return ResourceLocation.fromNamespaceAndPath(BensBeastiary.MODID, "geckolib/textures/entity/chimaera/chimaera_goat.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ChimaeraGoat animatable) {
        return ResourceLocation.fromNamespaceAndPath(BensBeastiary.MODID, "geckolib/animations/entity/chimaera/chimaera_goat.animation.json");
    }

    @Override
    public void setCustomAnimations(AnimationState<ChimaeraGoat> animationState) {
        super.setCustomAnimations(animationState);
    }
}
