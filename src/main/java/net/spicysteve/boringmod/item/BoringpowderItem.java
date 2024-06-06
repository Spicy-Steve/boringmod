
package net.spicysteve.boringmod.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class BoringpowderItem extends Item {
	public BoringpowderItem() {
		super(new Item.Properties().stacksTo(64).rarity(Rarity.RARE));
	}
}
