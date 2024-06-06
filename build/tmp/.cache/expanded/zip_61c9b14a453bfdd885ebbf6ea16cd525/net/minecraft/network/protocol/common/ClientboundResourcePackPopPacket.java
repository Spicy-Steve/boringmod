package net.minecraft.network.protocol.common;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ClientboundResourcePackPopPacket(Optional<UUID> id) implements Packet<ClientCommonPacketListener> {
    public ClientboundResourcePackPopPacket(FriendlyByteBuf p_314659_) {
        this(p_314659_.readOptional(FriendlyByteBuf::readUUID));
    }

    @Override
    public void write(FriendlyByteBuf p_314564_) {
        p_314564_.writeOptional(this.id, FriendlyByteBuf::writeUUID);
    }

    public void handle(ClientCommonPacketListener p_314440_) {
        p_314440_.handleResourcePackPop(this);
    }
}
