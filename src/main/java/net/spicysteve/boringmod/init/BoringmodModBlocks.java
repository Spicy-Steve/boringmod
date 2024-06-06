
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.spicysteve.boringmod.init;

import net.spicysteve.boringmod.block.BoringstandBlock;
import net.spicysteve.boringmod.BoringmodMod;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.BuiltInRegistries;

public class BoringmodModBlocks {
	public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(BuiltInRegistries.BLOCK, BoringmodMod.MODID);
	public static final DeferredHolder<Block, Block> BORINGSTAND = REGISTRY.register("boringstand", () -> new BoringstandBlock());
	// Start of user code block custom blocks
	// End of user code block custom blocks
}
