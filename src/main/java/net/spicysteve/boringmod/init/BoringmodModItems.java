
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.spicysteve.boringmod.init;

import net.spicysteve.boringmod.item.BoronrodItem;
import net.spicysteve.boringmod.item.BoringpowderItem;
import net.spicysteve.boringmod.BoringmodMod;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.bus.api.IEventBus;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.core.registries.BuiltInRegistries;

public class BoringmodModItems {
	public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(BuiltInRegistries.ITEM, BoringmodMod.MODID);
	public static final DeferredHolder<Item, Item> BORONROD = REGISTRY.register("boronrod", () -> new BoronrodItem());
	public static final DeferredHolder<Item, Item> BORE_SPAWN_EGG = REGISTRY.register("bore_spawn_egg", () -> new DeferredSpawnEggItem(BoringmodModEntities.BORE, -16711936, -16751104, new Item.Properties()));
	public static final DeferredHolder<Item, Item> BORINGPOWDER = REGISTRY.register("boringpowder", () -> new BoringpowderItem());
	public static final DeferredHolder<Item, Item> BORINGSTAND = block(BoringmodModBlocks.BORINGSTAND);

	// Start of user code block custom items
	// End of user code block custom items
	public static void register(IEventBus bus) {
		REGISTRY.register(bus);
	}

	private static DeferredHolder<Item, Item> block(DeferredHolder<Block, Block> block) {
		return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
	}
}
