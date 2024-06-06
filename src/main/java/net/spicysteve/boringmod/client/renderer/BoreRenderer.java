
package net.spicysteve.boringmod.client.renderer;

import net.spicysteve.boringmod.entity.BoreEntity;
import net.spicysteve.boringmod.client.model.Modelbore;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class BoreRenderer extends MobRenderer<BoreEntity, Modelbore<BoreEntity>> {
	public BoreRenderer(EntityRendererProvider.Context context) {
		super(context, new Modelbore(context.bakeLayer(Modelbore.LAYER_LOCATION)), 0.5f);
	}

	@Override
	public ResourceLocation getTextureLocation(BoreEntity entity) {
		return new ResourceLocation("boringmod:textures/entities/bore.png");
	}
}
