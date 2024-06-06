
package net.spicysteve.boringmod.potion;

import net.spicysteve.boringmod.procedures.BoredEffectEffectStartedappliedProcedure;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;

public class BoredEffectMobEffect extends MobEffect {
	public BoredEffectMobEffect() {
		super(MobEffectCategory.HARMFUL, -13369600);
	}

	@Override
	public void onEffectStarted(LivingEntity entity, int amplifier) {
		BoredEffectEffectStartedappliedProcedure.execute(entity.level(), entity.getX(), entity.getY(), entity.getZ(), entity);
	}
}
