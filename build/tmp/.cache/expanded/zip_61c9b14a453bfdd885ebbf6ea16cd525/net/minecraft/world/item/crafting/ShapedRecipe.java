package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ShapedRecipe implements CraftingRecipe, net.neoforged.neoforge.common.crafting.IShapedRecipe<net.minecraft.world.inventory.CraftingContainer> {
    final ShapedRecipePattern pattern;
    final ItemStack result;
    final String group;
    final CraftingBookCategory category;
    final boolean showNotification;

    public ShapedRecipe(String p_272759_, CraftingBookCategory p_273506_, ShapedRecipePattern p_312827_, ItemStack p_272852_, boolean p_312010_) {
        this.group = p_272759_;
        this.category = p_273506_;
        this.pattern = p_312827_;
        this.result = p_272852_;
        this.showNotification = p_312010_;
    }

    public ShapedRecipe(String p_250221_, CraftingBookCategory p_250716_, ShapedRecipePattern p_312814_, ItemStack p_248581_) {
        this(p_250221_, p_250716_, p_312814_, p_248581_, true);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SHAPED_RECIPE;
    }

    @Override
    public String getGroup() {
        return this.group;
    }

    @Override
    public int getRecipeWidth() {
        return getWidth();
    }

    @Override
    public CraftingBookCategory category() {
        return this.category;
    }

    @Override
    public int getRecipeHeight() {
        return getHeight();
    }

    @Override
    public ItemStack getResultItem(RegistryAccess p_266881_) {
        return this.result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return this.pattern.ingredients();
    }

    @Override
    public boolean showNotification() {
        return this.showNotification;
    }

    @Override
    public boolean canCraftInDimensions(int p_44161_, int p_44162_) {
        return p_44161_ >= this.pattern.width() && p_44162_ >= this.pattern.height();
    }

    public boolean matches(CraftingContainer p_44176_, Level p_44177_) {
        return this.pattern.matches(p_44176_);
    }

    public ItemStack assemble(CraftingContainer p_266686_, RegistryAccess p_266725_) {
        return this.getResultItem(p_266725_).copy();
    }

    public int getWidth() {
        return this.pattern.width();
    }

    public int getHeight() {
        return this.pattern.height();
    }

    @Override
    public boolean isIncomplete() {
        NonNullList<Ingredient> nonnulllist = this.getIngredients();
        return nonnulllist.isEmpty() || nonnulllist.stream().filter(p_151277_ -> !p_151277_.isEmpty()).anyMatch(net.neoforged.neoforge.common.CommonHooks::hasNoElements);
    }

    public static class Serializer implements RecipeSerializer<ShapedRecipe> {
        public static final Codec<ShapedRecipe> CODEC = RecordCodecBuilder.create(
            p_311728_ -> p_311728_.group(
                        ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter(p_311729_ -> p_311729_.group),
                        CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(p_311732_ -> p_311732_.category),
                        ShapedRecipePattern.MAP_CODEC.forGetter(p_311733_ -> p_311733_.pattern),
                        ItemStack.ITEM_WITH_COUNT_CODEC.fieldOf("result").forGetter(p_311730_ -> p_311730_.result),
                        ExtraCodecs.strictOptionalField(Codec.BOOL, "show_notification", true).forGetter(p_311731_ -> p_311731_.showNotification)
                    )
                    .apply(p_311728_, ShapedRecipe::new)
        );

        @Override
        public Codec<ShapedRecipe> codec() {
            return CODEC;
        }

        public ShapedRecipe fromNetwork(FriendlyByteBuf p_44240_) {
            String s = p_44240_.readUtf();
            CraftingBookCategory craftingbookcategory = p_44240_.readEnum(CraftingBookCategory.class);
            ShapedRecipePattern shapedrecipepattern = ShapedRecipePattern.fromNetwork(p_44240_);
            ItemStack itemstack = p_44240_.readItem();
            boolean flag = p_44240_.readBoolean();
            return new ShapedRecipe(s, craftingbookcategory, shapedrecipepattern, itemstack, flag);
        }

        public void toNetwork(FriendlyByteBuf p_44227_, ShapedRecipe p_44228_) {
            p_44227_.writeUtf(p_44228_.group);
            p_44227_.writeEnum(p_44228_.category);
            p_44228_.pattern.toNetwork(p_44227_);
            p_44227_.writeItem(p_44228_.result);
            p_44227_.writeBoolean(p_44228_.showNotification);
        }
    }
}
