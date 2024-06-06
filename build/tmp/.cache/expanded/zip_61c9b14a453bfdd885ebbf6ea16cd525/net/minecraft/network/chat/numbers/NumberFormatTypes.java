package net.minecraft.network.chat.numbers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;

public class NumberFormatTypes {
    public static final MapCodec<NumberFormat> MAP_CODEC = BuiltInRegistries.NUMBER_FORMAT_TYPE
        .byNameCodec()
        .dispatchMap(NumberFormat::type, p_313782_ -> p_313782_.mapCodec().codec());
    public static final Codec<NumberFormat> CODEC = MAP_CODEC.codec();

    public static NumberFormatType<?> bootstrap(Registry<NumberFormatType<?>> p_313781_) {
        NumberFormatType<?> numberformattype = Registry.register(p_313781_, "blank", BlankFormat.TYPE);
        Registry.register(p_313781_, "styled", StyledFormat.TYPE);
        Registry.register(p_313781_, "fixed", FixedFormat.TYPE);
        return numberformattype;
    }

    public static <T extends NumberFormat> void writeToStream(FriendlyByteBuf p_313772_, T p_313732_) {
        NumberFormatType<T> numberformattype = (NumberFormatType<T>)p_313732_.type();
        p_313772_.writeId(BuiltInRegistries.NUMBER_FORMAT_TYPE, numberformattype);
        numberformattype.writeToStream(p_313772_, p_313732_);
    }

    public static NumberFormat readFromStream(FriendlyByteBuf p_313691_) {
        NumberFormatType<?> numberformattype = p_313691_.readById(BuiltInRegistries.NUMBER_FORMAT_TYPE);
        return numberformattype.readFromStream(p_313691_);
    }
}
