// Made with Blockbench 4.10.2
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

public class Modelbore<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in
	// the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			new ResourceLocation("modid", "bore"), "main");
	private final ModelPart head;
	private final ModelPart lowerRods;
	private final ModelPart topRods;
	private final ModelPart middleRods;

	public Modelbore(ModelPart root) {
		this.head = root.getChild("head");
		this.lowerRods = root.getChild("lowerRods");
		this.topRods = root.getChild("topRods");
		this.middleRods = root.getChild("middleRods");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(
				-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition lowerRods = partdefinition.addOrReplaceChild("lowerRods",
				CubeListBuilder.create().texOffs(0, 16)
						.addBox(-4.0F, -4.0F, -4.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 16)
						.addBox(2.0F, -4.0F, -4.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 16)
						.addBox(2.0F, -4.0F, 2.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 16)
						.addBox(-4.0F, -4.0F, 2.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 14.0F, 0.0F));

		PartDefinition topRods = partdefinition.addOrReplaceChild("topRods",
				CubeListBuilder.create().texOffs(0, 16)
						.addBox(-8.0F, -6.0F, -8.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 16)
						.addBox(6.0F, -6.0F, -8.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 16)
						.addBox(6.0F, -6.0F, 6.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 16)
						.addBox(-8.0F, -6.0F, 6.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition middleRods = partdefinition.addOrReplaceChild("middleRods",
				CubeListBuilder.create().texOffs(0, 16)
						.addBox(-6.0F, -22.0F, -6.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 16)
						.addBox(4.0F, -22.0F, -6.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 16)
						.addBox(4.0F, -22.0F, 4.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 16)
						.addBox(-6.0F, -22.0F, 4.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 32);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		lowerRods.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		topRods.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		middleRods.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch) {
		this.head.yRot = netHeadYaw / (180F / (float) Math.PI);
		this.head.xRot = headPitch / (180F / (float) Math.PI);
		this.lowerRods.yRot = ageInTicks / 20.f;
		this.middleRods.yRot = ageInTicks;
		this.topRods.yRot = ageInTicks;
	}
}