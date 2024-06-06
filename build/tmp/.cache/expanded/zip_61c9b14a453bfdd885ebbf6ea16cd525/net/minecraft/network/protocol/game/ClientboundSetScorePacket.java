package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.NumberFormatTypes;
import net.minecraft.network.protocol.Packet;

public record ClientboundSetScorePacket(String owner, String objectiveName, int score, @Nullable Component display, @Nullable NumberFormat numberFormat)
    implements Packet<ClientGamePacketListener> {
    public ClientboundSetScorePacket(FriendlyByteBuf p_179373_) {
        this(
            p_179373_.readUtf(),
            p_179373_.readUtf(),
            p_179373_.readVarInt(),
            p_179373_.readNullable(FriendlyByteBuf::readComponentTrusted),
            p_179373_.readNullable(NumberFormatTypes::readFromStream)
        );
    }

    @Override
    public void write(FriendlyByteBuf p_133341_) {
        p_133341_.writeUtf(this.owner);
        p_133341_.writeUtf(this.objectiveName);
        p_133341_.writeVarInt(this.score);
        p_133341_.writeNullable(this.display, FriendlyByteBuf::writeComponent);
        p_133341_.writeNullable(this.numberFormat, NumberFormatTypes::writeToStream);
    }

    public void handle(ClientGamePacketListener p_133338_) {
        p_133338_.handleSetScore(this);
    }
}
