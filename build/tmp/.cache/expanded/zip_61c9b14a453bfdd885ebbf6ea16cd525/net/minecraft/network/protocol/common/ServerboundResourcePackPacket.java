package net.minecraft.network.protocol.common;

import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ServerboundResourcePackPacket(UUID id, ServerboundResourcePackPacket.Action action) implements Packet<ServerCommonPacketListener> {
    public ServerboundResourcePackPacket(FriendlyByteBuf p_295986_) {
        this(p_295986_.readUUID(), p_295986_.readEnum(ServerboundResourcePackPacket.Action.class));
    }

    @Override
    public void write(FriendlyByteBuf p_295360_) {
        p_295360_.writeUUID(this.id);
        p_295360_.writeEnum(this.action);
    }

    public void handle(ServerCommonPacketListener p_296386_) {
        p_296386_.handleResourcePackResponse(this);
    }

    public static enum Action {
        SUCCESSFULLY_LOADED,
        DECLINED,
        FAILED_DOWNLOAD,
        ACCEPTED,
        DOWNLOADED,
        INVALID_URL,
        FAILED_RELOAD,
        DISCARDED;

        public boolean isTerminal() {
            return this != ACCEPTED && this != DOWNLOADED;
        }
    }
}
