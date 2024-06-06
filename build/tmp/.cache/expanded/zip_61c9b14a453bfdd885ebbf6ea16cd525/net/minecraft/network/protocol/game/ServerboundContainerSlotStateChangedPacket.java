package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ServerboundContainerSlotStateChangedPacket(int slotId, int containerId, boolean newState) implements Packet<ServerGamePacketListener> {
    public ServerboundContainerSlotStateChangedPacket(FriendlyByteBuf p_307271_) {
        this(p_307271_.readVarInt(), p_307271_.readVarInt(), p_307271_.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf p_307330_) {
        p_307330_.writeVarInt(this.slotId);
        p_307330_.writeVarInt(this.containerId);
        p_307330_.writeBoolean(this.newState);
    }

    public void handle(ServerGamePacketListener p_307397_) {
        p_307397_.handleContainerSlotStateChanged(this);
    }
}
