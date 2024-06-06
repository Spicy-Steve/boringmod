package net.minecraft.world.entity.projectile;

import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class WindCharge extends AbstractHurtingProjectile implements ItemSupplier {
    public static final WindCharge.WindChargeExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new WindCharge.WindChargeExplosionDamageCalculator();

    public WindCharge(EntityType<? extends WindCharge> p_312199_, Level p_311945_) {
        super(p_312199_, p_311945_);
    }

    public WindCharge(EntityType<? extends WindCharge> p_312465_, Breeze p_312912_, Level p_312819_) {
        super(p_312465_, p_312912_.getX(), p_312912_.getSnoutYPosition(), p_312912_.getZ(), p_312819_);
        this.setOwner(p_312912_);
    }

    @Override
    protected AABB makeBoundingBox() {
        float f = this.getType().getDimensions().width / 2.0F;
        float f1 = this.getType().getDimensions().height;
        float f2 = 0.15F;
        return new AABB(
            this.position().x - (double)f,
            this.position().y - 0.15F,
            this.position().z - (double)f,
            this.position().x + (double)f,
            this.position().y - 0.15F + (double)f1,
            this.position().z + (double)f
        );
    }

    @Override
    protected float getEyeHeight(Pose p_313776_, EntityDimensions p_313862_) {
        return 0.0F;
    }

    @Override
    public boolean canCollideWith(Entity p_311815_) {
        return p_311815_ instanceof WindCharge ? false : super.canCollideWith(p_311815_);
    }

    @Override
    protected boolean canHitEntity(Entity p_312502_) {
        return p_312502_ instanceof WindCharge ? false : super.canHitEntity(p_312502_);
    }

    @Override
    protected void onHitEntity(EntityHitResult p_312305_) {
        super.onHitEntity(p_312305_);
        if (!this.level().isClientSide) {
            Entity entity1 = p_312305_.getEntity();
            Entity entity = this.getOwner();
            entity1.hurt(this.damageSources().mobProjectile(this, entity instanceof LivingEntity livingentity ? livingentity : null), 1.0F);
            this.explode();
        }
    }

    private void explode() {
        this.level()
            .explode(
                this,
                null,
                EXPLOSION_DAMAGE_CALCULATOR,
                this.getX(),
                this.getY(),
                this.getZ(),
                (float)(3.0 + this.random.nextDouble()),
                false,
                Level.ExplosionInteraction.BLOW,
                ParticleTypes.GUST,
                ParticleTypes.GUST_EMITTER,
                SoundEvents.WIND_BURST
            );
    }

    @Override
    protected void onHitBlock(BlockHitResult p_312630_) {
        super.onHitBlock(p_312630_);
        this.explode();
        this.discard();
    }

    @Override
    protected void onHit(HitResult p_312747_) {
        super.onHit(p_312747_);
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    public ItemStack getItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected float getInertia() {
        return 1.0F;
    }

    @Override
    protected float getLiquidInertia() {
        return this.getInertia();
    }

    @Nullable
    @Override
    protected ParticleOptions getTrailParticle() {
        return null;
    }

    @Override
    protected ClipContext.Block getClipType() {
        return ClipContext.Block.OUTLINE;
    }

    public static final class WindChargeExplosionDamageCalculator extends ExplosionDamageCalculator {
        @Override
        public boolean shouldDamageEntity(Explosion p_314513_, Entity p_314456_) {
            return false;
        }
    }
}
