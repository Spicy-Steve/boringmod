package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;

public class ClientboundExplodePacket implements Packet<ClientGamePacketListener> {
    private final double x;
    private final double y;
    private final double z;
    private final float power;
    private final List<BlockPos> toBlow;
    private final float knockbackX;
    private final float knockbackY;
    private final float knockbackZ;
    private final ParticleOptions smallExplosionParticles;
    private final ParticleOptions largeExplosionParticles;
    private final Explosion.BlockInteraction blockInteraction;
    private final SoundEvent explosionSound;

    public ClientboundExplodePacket(
        double p_132115_,
        double p_132116_,
        double p_132117_,
        float p_132118_,
        List<BlockPos> p_132119_,
        @Nullable Vec3 p_132120_,
        Explosion.BlockInteraction p_312296_,
        ParticleOptions p_312499_,
        ParticleOptions p_312203_,
        SoundEvent p_312511_
    ) {
        this.x = p_132115_;
        this.y = p_132116_;
        this.z = p_132117_;
        this.power = p_132118_;
        this.toBlow = Lists.newArrayList(p_132119_);
        this.explosionSound = p_312511_;
        if (p_132120_ != null) {
            this.knockbackX = (float)p_132120_.x;
            this.knockbackY = (float)p_132120_.y;
            this.knockbackZ = (float)p_132120_.z;
        } else {
            this.knockbackX = 0.0F;
            this.knockbackY = 0.0F;
            this.knockbackZ = 0.0F;
        }

        this.blockInteraction = p_312296_;
        this.smallExplosionParticles = p_312499_;
        this.largeExplosionParticles = p_312203_;
    }

    public ClientboundExplodePacket(FriendlyByteBuf p_178845_) {
        this.x = p_178845_.readDouble();
        this.y = p_178845_.readDouble();
        this.z = p_178845_.readDouble();
        this.power = p_178845_.readFloat();
        int i = Mth.floor(this.x);
        int j = Mth.floor(this.y);
        int k = Mth.floor(this.z);
        this.toBlow = p_178845_.readList(p_178850_ -> {
            int l = p_178850_.readByte() + i;
            int i1 = p_178850_.readByte() + j;
            int j1 = p_178850_.readByte() + k;
            return new BlockPos(l, i1, j1);
        });
        this.knockbackX = p_178845_.readFloat();
        this.knockbackY = p_178845_.readFloat();
        this.knockbackZ = p_178845_.readFloat();
        this.blockInteraction = p_178845_.readEnum(Explosion.BlockInteraction.class);
        this.smallExplosionParticles = this.readParticle(p_178845_, p_178845_.readById(BuiltInRegistries.PARTICLE_TYPE));
        this.largeExplosionParticles = this.readParticle(p_178845_, p_178845_.readById(BuiltInRegistries.PARTICLE_TYPE));
        this.explosionSound = SoundEvent.readFromNetwork(p_178845_);
    }

    public void writeParticle(FriendlyByteBuf p_315020_, ParticleOptions p_315019_) {
        p_315020_.writeId(BuiltInRegistries.PARTICLE_TYPE, p_315019_.getType());
        p_315019_.writeToNetwork(p_315020_);
    }

    private <T extends ParticleOptions> T readParticle(FriendlyByteBuf p_312342_, ParticleType<T> p_312162_) {
        return p_312162_.getDeserializer().fromNetwork(p_312162_, p_312342_);
    }

    @Override
    public void write(FriendlyByteBuf p_132129_) {
        p_132129_.writeDouble(this.x);
        p_132129_.writeDouble(this.y);
        p_132129_.writeDouble(this.z);
        p_132129_.writeFloat(this.power);
        int i = Mth.floor(this.x);
        int j = Mth.floor(this.y);
        int k = Mth.floor(this.z);
        p_132129_.writeCollection(this.toBlow, (p_293728_, p_293729_) -> {
            int l = p_293729_.getX() - i;
            int i1 = p_293729_.getY() - j;
            int j1 = p_293729_.getZ() - k;
            p_293728_.writeByte(l);
            p_293728_.writeByte(i1);
            p_293728_.writeByte(j1);
        });
        p_132129_.writeFloat(this.knockbackX);
        p_132129_.writeFloat(this.knockbackY);
        p_132129_.writeFloat(this.knockbackZ);
        p_132129_.writeEnum(this.blockInteraction);
        this.writeParticle(p_132129_, this.smallExplosionParticles);
        this.writeParticle(p_132129_, this.largeExplosionParticles);
        this.explosionSound.writeToNetwork(p_132129_);
    }

    public void handle(ClientGamePacketListener p_132126_) {
        p_132126_.handleExplosion(this);
    }

    public float getKnockbackX() {
        return this.knockbackX;
    }

    public float getKnockbackY() {
        return this.knockbackY;
    }

    public float getKnockbackZ() {
        return this.knockbackZ;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public float getPower() {
        return this.power;
    }

    public List<BlockPos> getToBlow() {
        return this.toBlow;
    }

    public Explosion.BlockInteraction getBlockInteraction() {
        return this.blockInteraction;
    }

    public ParticleOptions getSmallExplosionParticles() {
        return this.smallExplosionParticles;
    }

    public ParticleOptions getLargeExplosionParticles() {
        return this.largeExplosionParticles;
    }

    public SoundEvent getExplosionSound() {
        return this.explosionSound;
    }
}
