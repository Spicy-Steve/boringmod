package net.minecraft.world.item.crafting;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class FireworkRocketRecipe extends CustomRecipe {
    private static final Ingredient PAPER_INGREDIENT = Ingredient.of(Items.PAPER);
    private static final Ingredient GUNPOWDER_INGREDIENT = Ingredient.of(Items.GUNPOWDER);
    private static final Ingredient STAR_INGREDIENT = Ingredient.of(Items.FIREWORK_STAR);

    public FireworkRocketRecipe(CraftingBookCategory p_250134_) {
        super(p_250134_);
    }

    public boolean matches(CraftingContainer p_43854_, Level p_43855_) {
        boolean flag = false;
        int i = 0;

        for(int j = 0; j < p_43854_.getContainerSize(); ++j) {
            ItemStack itemstack = p_43854_.getItem(j);
            if (!itemstack.isEmpty()) {
                if (PAPER_INGREDIENT.test(itemstack)) {
                    if (flag) {
                        return false;
                    }

                    flag = true;
                } else if (GUNPOWDER_INGREDIENT.test(itemstack)) {
                    if (++i > 3) {
                        return false;
                    }
                } else if (!STAR_INGREDIENT.test(itemstack)) {
                    return false;
                }
            }
        }

        return flag && i >= 1;
    }

    public ItemStack assemble(CraftingContainer p_43852_, RegistryAccess p_266791_) {
        ItemStack itemstack = new ItemStack(Items.FIREWORK_ROCKET, 3);
        CompoundTag compoundtag = itemstack.getOrCreateTagElement("Fireworks");
        ListTag listtag = new ListTag();
        int i = 0;

        for(int j = 0; j < p_43852_.getContainerSize(); ++j) {
            ItemStack itemstack1 = p_43852_.getItem(j);
            if (!itemstack1.isEmpty()) {
                if (GUNPOWDER_INGREDIENT.test(itemstack1)) {
                    ++i;
                } else if (STAR_INGREDIENT.test(itemstack1)) {
                    CompoundTag compoundtag1 = itemstack1.getTagElement("Explosion");
                    if (compoundtag1 != null) {
                        listtag.add(compoundtag1);
                    }
                }
            }
        }

        compoundtag.putByte("Flight", (byte)i);
        if (!listtag.isEmpty()) {
            compoundtag.put("Explosions", listtag);
        }

        return itemstack;
    }

    @Override
    public boolean canCraftInDimensions(int p_43844_, int p_43845_) {
        return p_43844_ * p_43845_ >= 2;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess p_267261_) {
        return new ItemStack(Items.FIREWORK_ROCKET);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.FIREWORK_ROCKET;
    }
}
