package net.minecraft.network.protocol.common;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.resources.ResourceLocation;

public record ServerboundCustomPayloadPacket(CustomPacketPayload payload) implements Packet<ServerCommonPacketListener> {
    private static final int MAX_PAYLOAD_SIZE = 32767;
    public static final Map<ResourceLocation, FriendlyByteBuf.Reader<? extends CustomPacketPayload>> KNOWN_TYPES = ImmutableMap.<ResourceLocation, FriendlyByteBuf.Reader<? extends CustomPacketPayload>>builder(
            
        )
        .put(BrandPayload.ID, BrandPayload::new)
        .build();

    /**
     * Creates a new packet with a custom payload from the network.
     * @param p_296108_ The buffer to read the packet from.
     * @deprecated Use {@link #ServerboundCustomPayloadPacket(FriendlyByteBuf, io.netty.channel.ChannelHandlerContext, net.minecraft.network.ConnectionProtocol)} instead, as this variant can only read vanilla payloads.
     */
    @Deprecated
    public ServerboundCustomPayloadPacket(FriendlyByteBuf p_296108_) {
        this(readPayload(p_296108_.readResourceLocation(), p_296108_));
    }

    /**
     * Creates a new packet with a custom payload from the network.
     *
     * @param p_296108_ The buffer to read the packet from.
     * @param context The context of the channel handler
     * @param protocol The protocol of the connection
     */
    public ServerboundCustomPayloadPacket(FriendlyByteBuf p_296108_, io.netty.channel.ChannelHandlerContext context, net.minecraft.network.ConnectionProtocol protocol) {
        this(readPayload(p_296108_.readResourceLocation(), p_296108_, context, protocol));
    }

    /**
     * Reads the payload from the given buffer.
     *
     * @param p_294367_ The id of the payload
     * @param p_294321_ The buffer to read from
     * @param context The context of the channel handler
     * @param protocol The protocol of the connection
     * @return The payload
     */
    private static CustomPacketPayload readPayload(ResourceLocation p_294367_, FriendlyByteBuf p_294321_, io.netty.channel.ChannelHandlerContext context, net.minecraft.network.ConnectionProtocol protocol) {
        FriendlyByteBuf.Reader<? extends CustomPacketPayload> reader = net.neoforged.neoforge.network.registration.NetworkRegistry.getInstance().getReader(p_294367_, context, protocol, KNOWN_TYPES);
        return (CustomPacketPayload)(reader != null ? reader.apply(p_294321_) : readUnknownPayload(p_294367_, p_294321_));
    }

    /**
     * Reads the payload from the given buffer.
     * @param p_294367_ The id of the payload
     * @param p_294321_ The buffer to read from
     * @return The payload
     * @deprecated Use {@link #readPayload(ResourceLocation, FriendlyByteBuf, io.netty.channel.ChannelHandlerContext, net.minecraft.network.ConnectionProtocol)} instead, as this variant can only read vanilla payloads.
     */
    @Deprecated
    private static CustomPacketPayload readPayload(ResourceLocation p_294367_, FriendlyByteBuf p_294321_) {
        FriendlyByteBuf.Reader<? extends CustomPacketPayload> reader = KNOWN_TYPES.get(p_294367_);
        return (CustomPacketPayload)(reader != null ? reader.apply(p_294321_) : readUnknownPayload(p_294367_, p_294321_));
    }

    private static DiscardedPayload readUnknownPayload(ResourceLocation p_294973_, FriendlyByteBuf p_296037_) {
        int i = p_296037_.readableBytes();
        if (i >= 0 && i <= 32767) {
            p_296037_.skipBytes(i);
            return new DiscardedPayload(p_294973_);
        } else {
            throw new IllegalArgumentException("Payload may not be larger than 32767 bytes");
        }
    }

    @Override
    public void write(FriendlyByteBuf p_295621_) {
        p_295621_.writeResourceLocation(this.payload.id());
        this.payload.write(p_295621_);
    }

    public void handle(ServerCommonPacketListener p_295862_) {
        p_295862_.handleCustomPayload(this);
    }
}
