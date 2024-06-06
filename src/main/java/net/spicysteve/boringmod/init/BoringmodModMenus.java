
/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.spicysteve.boringmod.init;

import net.spicysteve.boringmod.world.inventory.BoringstandGUIMenu;
import net.spicysteve.boringmod.BoringmodMod;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.core.registries.Registries;

public class BoringmodModMenus {
	public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(Registries.MENU, BoringmodMod.MODID);
	public static final DeferredHolder<MenuType<?>, MenuType<BoringstandGUIMenu>> BORINGSTAND_GUI = REGISTRY.register("boringstand_gui", () -> IMenuTypeExtension.create(BoringstandGUIMenu::new));
}
