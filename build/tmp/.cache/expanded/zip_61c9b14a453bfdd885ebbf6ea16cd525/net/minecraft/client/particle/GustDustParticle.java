package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class GustDustParticle extends TextureSheetParticle {
    private final Vector3f fromColor = new Vector3f(0.5F, 0.5F, 0.5F);
    private final Vector3f toColor = new Vector3f(1.0F, 1.0F, 1.0F);

    GustDustParticle(ClientLevel p_311891_, double p_311768_, double p_312326_, double p_312901_, double p_312528_, double p_312121_, double p_312155_) {
        super(p_311891_, p_311768_, p_312326_, p_312901_);
        this.hasPhysics = false;
        this.xd = p_312528_ + (double)Mth.randomBetween(this.random, -0.4F, 0.4F);
        this.zd = p_312155_ + (double)Mth.randomBetween(this.random, -0.4F, 0.4F);
        double d0 = Math.random() * 2.0;
        double d1 = Math.sqrt(this.xd * this.xd + this.yd * this.yd + this.zd * this.zd);
        this.xd = this.xd / d1 * d0 * 0.4F;
        this.zd = this.zd / d1 * d0 * 0.4F;
        this.quadSize *= 2.5F;
        this.xd *= 0.08F;
        this.zd *= 0.08F;
        this.lifetime = 18 + this.random.nextInt(4);
    }

    @Override
    public void render(VertexConsumer p_311997_, Camera p_311802_, float p_312602_) {
        this.lerpColors(p_312602_);
        super.render(p_311997_, p_311802_, p_312602_);
    }

    private void lerpColors(float p_312133_) {
        float f = ((float)this.age + p_312133_) / (float)(this.lifetime + 1);
        Vector3f vector3f = new Vector3f(this.fromColor).lerp(this.toColor, f);
        this.rCol = vector3f.x();
        this.gCol = vector3f.y();
        this.bCol = vector3f.z();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.xo = this.x;
            this.zo = this.z;
            this.move(this.xd, 0.0, this.zd);
            this.xd *= 0.99;
            this.zd *= 0.99;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class GustDustParticleProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public GustDustParticleProvider(SpriteSet p_312281_) {
            this.sprite = p_312281_;
        }

        public Particle createParticle(
            SimpleParticleType p_311869_,
            ClientLevel p_312177_,
            double p_312545_,
            double p_312424_,
            double p_312020_,
            double p_311833_,
            double p_312021_,
            double p_312381_
        ) {
            GustDustParticle gustdustparticle = new GustDustParticle(p_312177_, p_312545_, p_312424_, p_312020_, p_311833_, p_312021_, p_312381_);
            gustdustparticle.pickSprite(this.sprite);
            return gustdustparticle;
        }
    }
}
