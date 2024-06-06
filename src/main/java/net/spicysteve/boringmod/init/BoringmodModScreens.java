
/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.spicysteve.boringmod.init;

import net.spicysteve.boringmod.client.gui.BoringstandGUIScreen;

import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class BoringmodModScreens {
	@SubscribeEvent
	public static void clientLoad(RegisterMenuScreensEvent event) {
		event.register(BoringmodModMenus.BORINGSTAND_GUI.get(), BoringstandGUIScreen::new);
	}
}
