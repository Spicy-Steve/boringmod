package net.minecraft.network.protocol;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;

public abstract class BundlePacket<T extends PacketListener> implements Packet<T> {
    private final Iterable<Packet<? super T>> packets;

    protected BundlePacket(Iterable<Packet<? super T>> p_265290_) {
        this.packets = net.neoforged.neoforge.network.bundle.BundlePacketUtils.flatten(p_265290_);
    }

    public final Iterable<Packet<? super T>> subPackets() {
        return this.packets;
    }

    @Override
    public final void write(FriendlyByteBuf p_265519_) {
    }
}
