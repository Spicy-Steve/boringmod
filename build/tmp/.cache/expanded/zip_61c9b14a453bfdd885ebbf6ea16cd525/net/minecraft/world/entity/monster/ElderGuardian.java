package net.minecraft.world.entity.monster;

import java.util.List;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

public class ElderGuardian extends Guardian {
    public static final float ELDER_SIZE_SCALE = EntityType.ELDER_GUARDIAN.getWidth() / EntityType.GUARDIAN.getWidth();
    private static final int EFFECT_INTERVAL = 1200;
    private static final int EFFECT_RADIUS = 50;
    private static final int EFFECT_DURATION = 6000;
    private static final int EFFECT_AMPLIFIER = 2;
    private static final int EFFECT_DISPLAY_LIMIT = 1200;

    public ElderGuardian(EntityType<? extends ElderGuardian> p_32460_, Level p_32461_) {
        super(p_32460_, p_32461_);
        this.setPersistenceRequired();
        if (this.randomStrollGoal != null) {
            this.randomStrollGoal.setInterval(400);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Guardian.createAttributes().add(Attributes.MOVEMENT_SPEED, 0.3F).add(Attributes.ATTACK_DAMAGE, 8.0).add(Attributes.MAX_HEALTH, 80.0);
    }

    @Override
    public int getAttackDuration() {
        return 60;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isInWaterOrBubble() ? SoundEvents.ELDER_GUARDIAN_AMBIENT : SoundEvents.ELDER_GUARDIAN_AMBIENT_LAND;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource p_32468_) {
        return this.isInWaterOrBubble() ? SoundEvents.ELDER_GUARDIAN_HURT : SoundEvents.ELDER_GUARDIAN_HURT_LAND;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.isInWaterOrBubble() ? SoundEvents.ELDER_GUARDIAN_DEATH : SoundEvents.ELDER_GUARDIAN_DEATH_LAND;
    }

    @Override
    protected SoundEvent getFlopSound() {
        return SoundEvents.ELDER_GUARDIAN_FLOP;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if ((this.tickCount + this.getId()) % 1200 == 0) {
            MobEffectInstance mobeffectinstance = new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 6000, 2);
            List<ServerPlayer> list = MobEffectUtil.addEffectToPlayersAround((ServerLevel)this.level(), this, this.position(), 50.0, mobeffectinstance, 1200);
            list.forEach(
                p_308752_ -> p_308752_.connection
                        .send(new ClientboundGameEventPacket(ClientboundGameEventPacket.GUARDIAN_ELDER_EFFECT, this.isSilent() ? 0.0F : 1.0F))
            );
        }

        if (!this.hasRestriction()) {
            this.restrictTo(this.blockPosition(), 16);
        }
    }

    @Override
    protected Vector3f getPassengerAttachmentPoint(Entity p_295705_, EntityDimensions p_294936_, float p_294517_) {
        return new Vector3f(0.0F, p_294936_.height + 0.353125F * p_294517_, 0.0F);
    }
}
