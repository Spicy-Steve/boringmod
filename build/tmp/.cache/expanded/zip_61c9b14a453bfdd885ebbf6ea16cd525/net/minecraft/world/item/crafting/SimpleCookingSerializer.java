package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public class SimpleCookingSerializer<T extends AbstractCookingRecipe> implements RecipeSerializer<T> {
    private final AbstractCookingRecipe.Factory<T> factory;
    private final Codec<T> codec;

    public SimpleCookingSerializer(AbstractCookingRecipe.Factory<T> p_312065_, int p_44331_) {
        this.factory = p_312065_;
        this.codec = RecordCodecBuilder.create(
            p_300831_ -> p_300831_.group(
                        ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter(p_300832_ -> p_300832_.group),
                        CookingBookCategory.CODEC.fieldOf("category").orElse(CookingBookCategory.MISC).forGetter(p_300828_ -> p_300828_.category),
                        Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(p_300833_ -> p_300833_.ingredient),
                        net.neoforged.neoforge.common.crafting.CraftingHelper.smeltingResultCodec().fieldOf("result").forGetter(p_300827_ -> p_300827_.result),
                        Codec.FLOAT.fieldOf("experience").orElse(0.0F).forGetter(p_300826_ -> p_300826_.experience),
                        Codec.INT.fieldOf("cookingtime").orElse(p_44331_).forGetter(p_300834_ -> p_300834_.cookingTime)
                    )
                    .apply(p_300831_, p_312065_::create)
        );
    }

    @Override
    public Codec<T> codec() {
        return this.codec;
    }

    public T fromNetwork(FriendlyByteBuf p_44345_) {
        String s = p_44345_.readUtf();
        CookingBookCategory cookingbookcategory = p_44345_.readEnum(CookingBookCategory.class);
        Ingredient ingredient = Ingredient.fromNetwork(p_44345_);
        ItemStack itemstack = p_44345_.readItem();
        float f = p_44345_.readFloat();
        int i = p_44345_.readVarInt();
        return this.factory.create(s, cookingbookcategory, ingredient, itemstack, f, i);
    }

    public void toNetwork(FriendlyByteBuf p_44335_, T p_44336_) {
        p_44335_.writeUtf(p_44336_.group);
        p_44335_.writeEnum(p_44336_.category());
        p_44336_.ingredient.toNetwork(p_44335_);
        p_44335_.writeItem(p_44336_.result);
        p_44335_.writeFloat(p_44336_.experience);
        p_44335_.writeVarInt(p_44336_.cookingTime);
    }

    public AbstractCookingRecipe create(
        String p_312671_, CookingBookCategory p_312067_, Ingredient p_312327_, ItemStack p_311758_, float p_312386_, int p_311986_
    ) {
        return this.factory.create(p_312671_, p_312067_, p_312327_, p_311758_, p_312386_, p_311986_);
    }
}
