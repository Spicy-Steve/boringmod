package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.TickRateManager;

public record ClientboundTickingStatePacket(float tickRate, boolean isFrozen) implements Packet<ClientGamePacketListener> {
    public ClientboundTickingStatePacket(FriendlyByteBuf p_309182_) {
        this(p_309182_.readFloat(), p_309182_.readBoolean());
    }

    public static ClientboundTickingStatePacket from(TickRateManager p_308984_) {
        return new ClientboundTickingStatePacket(p_308984_.tickrate(), p_308984_.isFrozen());
    }

    @Override
    public void write(FriendlyByteBuf p_309152_) {
        p_309152_.writeFloat(this.tickRate);
        p_309152_.writeBoolean(this.isFrozen);
    }

    public void handle(ClientGamePacketListener p_309074_) {
        p_309074_.handleTickingState(this);
    }
}
