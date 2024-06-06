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
public class BreezeWindLayer extends RenderLayer<Breeze, BreezeModel<Breeze>> {
    private static final float TOP_PART_ALPHA = 1.0F;
    private static final float MIDDLE_PART_ALPHA = 1.0F;
    private static final float BOTTOM_PART_ALPHA = 1.0F;
    private final ResourceLocation textureLoc;
    private final BreezeModel<Breeze> model;

    public BreezeWindLayer(RenderLayerParent<Breeze, BreezeModel<Breeze>> p_312625_, EntityModelSet p_312909_, ResourceLocation p_312538_) {
        super(p_312625_);
        this.model = new BreezeModel<>(p_312909_.bakeLayer(ModelLayers.BREEZE_WIND));
        this.textureLoc = p_312538_;
    }

    public void render(
        PoseStack p_312822_,
        MultiBufferSource p_312869_,
        int p_311783_,
        Breeze p_312046_,
        float p_312170_,
        float p_311773_,
        float p_312428_,
        float p_312287_,
        float p_312118_,
        float p_312531_
    ) {
        float f = (float)p_312046_.tickCount + p_312428_;
        this.model.prepareMobModel(p_312046_, p_312170_, p_311773_, p_312428_);
        this.getParentModel().copyPropertiesTo(this.model);
        VertexConsumer vertexconsumer = p_312869_.getBuffer(RenderType.breezeWind(this.getTextureLocation(p_312046_), this.xOffset(f) % 1.0F, 0.0F));
        this.model.setupAnim(p_312046_, p_312170_, p_311773_, p_312287_, p_312118_, p_312531_);
        this.model.windTop().skipDraw = true;
        this.model.windMiddle().skipDraw = true;
        this.model.windBottom().skipDraw = false;
        this.model.root().render(p_312822_, vertexconsumer, p_311783_, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        this.model.windTop().skipDraw = true;
        this.model.windMiddle().skipDraw = false;
        this.model.windBottom().skipDraw = true;
        this.model.root().render(p_312822_, vertexconsumer, p_311783_, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        this.model.windTop().skipDraw = false;
        this.model.windMiddle().skipDraw = true;
        this.model.windBottom().skipDraw = true;
        this.model.root().render(p_312822_, vertexconsumer, p_311783_, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    private float xOffset(float p_312086_) {
        return p_312086_ * 0.02F;
    }

    protected ResourceLocation getTextureLocation(Breeze p_312458_) {
        return this.textureLoc;
    }
}
