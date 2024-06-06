
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.spicysteve.boringmod.init;

import net.spicysteve.boringmod.BoringmodMod;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class BoringmodModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BoringmodMod.MODID);
	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BORINGTAB = REGISTRY.register("boringtab",
			() -> CreativeModeTab.builder().title(Component.translatable("item_group.boringmod.boringtab")).icon(() -> new ItemStack(BoringmodModItems.BORONROD.get())).displayItems((parameters, tabData) -> {
				tabData.accept(BoringmodModItems.BORONROD.get());
				tabData.accept(BoringmodModItems.BORINGPOWDER.get());
				tabData.accept(BoringmodModBlocks.BORINGSTAND.get().asItem());
			})

					.build());

	@SubscribeEvent
	public static void buildTabContentsVanilla(BuildCreativeModeTabContentsEvent tabData) {
		if (tabData.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
			tabData.accept(BoringmodModItems.BORE_SPAWN_EGG.get());
		}
	}
}
