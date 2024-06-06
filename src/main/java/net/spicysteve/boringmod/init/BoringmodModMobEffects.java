
/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.spicysteve.boringmod.init;

import net.spicysteve.boringmod.potion.BoredEffectMobEffect;
import net.spicysteve.boringmod.BoringmodMod;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.core.registries.Registries;

public class BoringmodModMobEffects {
	public static final DeferredRegister<MobEffect> REGISTRY = DeferredRegister.create(Registries.MOB_EFFECT, BoringmodMod.MODID);
	public static final DeferredHolder<MobEffect, MobEffect> BORED_EFFECT = REGISTRY.register("bored_effect", () -> new BoredEffectMobEffect());
}
