package net.minecraft.network.chat.numbers;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.FriendlyByteBuf;

public interface NumberFormatType<T extends NumberFormat> {
    MapCodec<T> mapCodec();

    void writeToStream(FriendlyByteBuf p_313774_, T p_313723_);

    T readFromStream(FriendlyByteBuf p_313914_);
}
