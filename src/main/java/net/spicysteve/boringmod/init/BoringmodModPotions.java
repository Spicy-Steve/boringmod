
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.spicysteve.boringmod.init;

import net.spicysteve.boringmod.BoringmodMod;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.registries.Registries;

public class BoringmodModPotions {
	public static final DeferredRegister<Potion> REGISTRY = DeferredRegister.create(Registries.POTION, BoringmodMod.MODID);
	public static final DeferredHolder<Potion, Potion> BORINGPOTION = REGISTRY.register("boringpotion", () -> new Potion(new MobEffectInstance(BoringmodModMobEffects.BORED_EFFECT.get(), 18000, 0, false, true)));
}
