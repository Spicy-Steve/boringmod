package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ClientboundResetScorePacket(String owner, @Nullable String objectiveName) implements Packet<ClientGamePacketListener> {
    public ClientboundResetScorePacket(FriendlyByteBuf p_313852_) {
        this(p_313852_.readUtf(), p_313852_.readNullable(FriendlyByteBuf::readUtf));
    }

    @Override
    public void write(FriendlyByteBuf p_313825_) {
        p_313825_.writeUtf(this.owner);
        p_313825_.writeNullable(this.objectiveName, FriendlyByteBuf::writeUtf);
    }

    public void handle(ClientGamePacketListener p_313694_) {
        p_313694_.handleResetScore(this);
    }
}
