package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class ArrowFireEnchantment extends Enchantment {
    public ArrowFireEnchantment(Enchantment.Rarity p_44576_, EquipmentSlot... p_44577_) {
        super(p_44576_, EnchantmentCategory.BOW, p_44577_);
    }

    @Override
    public int getMinCost(int p_44580_) {
        return 20;
    }

    @Override
    public int getMaxCost(int p_44582_) {
        return 50;
    }
}
