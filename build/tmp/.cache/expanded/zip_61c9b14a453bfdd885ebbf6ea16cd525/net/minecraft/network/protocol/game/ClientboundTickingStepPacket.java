package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.TickRateManager;

public record ClientboundTickingStepPacket(int tickSteps) implements Packet<ClientGamePacketListener> {
    public ClientboundTickingStepPacket(FriendlyByteBuf p_309129_) {
        this(p_309129_.readVarInt());
    }

    public static ClientboundTickingStepPacket from(TickRateManager p_308913_) {
        return new ClientboundTickingStepPacket(p_308913_.frozenTicksToRun());
    }

    @Override
    public void write(FriendlyByteBuf p_309179_) {
        p_309179_.writeVarInt(this.tickSteps);
    }

    public void handle(ClientGamePacketListener p_309086_) {
        p_309086_.handleTickingStep(this);
    }
}
