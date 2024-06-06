package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.BreezeModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BreezeEyesLayer extends RenderLayer<Breeze, BreezeModel<Breeze>> {
    private final ResourceLocation textureLoc;
    private final BreezeModel<Breeze> model;

    public BreezeEyesLayer(RenderLayerParent<Breeze, BreezeModel<Breeze>> p_312409_, EntityModelSet p_312898_, ResourceLocation p_312648_) {
        super(p_312409_);
        this.model = new BreezeModel<>(p_312898_.bakeLayer(ModelLayers.BREEZE_EYES));
        this.textureLoc = p_312648_;
    }

    public void render(
        PoseStack p_311827_,
        MultiBufferSource p_312311_,
        int p_312194_,
        Breeze p_312799_,
        float p_311984_,
        float p_312846_,
        float p_312053_,
        float p_312209_,
        float p_312003_,
        float p_312826_
    ) {
        this.model.prepareMobModel(p_312799_, p_311984_, p_312846_, p_312053_);
        this.getParentModel().copyPropertiesTo(this.model);
        VertexConsumer vertexconsumer = p_312311_.getBuffer(RenderType.breezeEyes(this.textureLoc));
        this.model.setupAnim(p_312799_, p_311984_, p_312846_, p_312209_, p_312003_, p_312826_);
        this.model.root().render(p_311827_, vertexconsumer, p_312194_, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    protected ResourceLocation getTextureLocation(Breeze p_311754_) {
        return this.textureLoc;
    }
}
