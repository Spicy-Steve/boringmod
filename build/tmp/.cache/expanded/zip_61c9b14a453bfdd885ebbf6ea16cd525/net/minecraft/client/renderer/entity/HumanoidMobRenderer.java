package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.Mob;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class HumanoidMobRenderer<T extends Mob, M extends HumanoidModel<T>> extends MobRenderer<T, M> {
    public HumanoidMobRenderer(EntityRendererProvider.Context p_174169_, M p_174170_, float p_174171_) {
        this(p_174169_, p_174170_, p_174171_, 1.0F, 1.0F, 1.0F);
    }

    public HumanoidMobRenderer(EntityRendererProvider.Context p_174173_, M p_174174_, float p_174175_, float p_174176_, float p_174177_, float p_174178_) {
        super(p_174173_, p_174174_, p_174175_);
        this.addLayer(new CustomHeadLayer<>(this, p_174173_.getModelSet(), p_174176_, p_174177_, p_174178_, p_174173_.getItemInHandRenderer()));
        this.addLayer(new ElytraLayer<>(this, p_174173_.getModelSet()));
        this.addLayer(new ItemInHandLayer<>(this, p_174173_.getItemInHandRenderer()));
    }
}
