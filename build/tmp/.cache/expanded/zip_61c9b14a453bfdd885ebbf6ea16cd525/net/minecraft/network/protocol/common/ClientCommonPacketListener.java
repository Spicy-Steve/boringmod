package net.minecraft.network.protocol.common;

import net.minecraft.network.ClientboundPacketListener;

public interface ClientCommonPacketListener extends ClientboundPacketListener, net.neoforged.neoforge.common.extensions.IClientCommonPacketListenerExtension {
    void handleKeepAlive(ClientboundKeepAlivePacket p_295236_);

    void handlePing(ClientboundPingPacket p_296451_);

    void handleCustomPayload(ClientboundCustomPayloadPacket p_294344_);

    void handleDisconnect(ClientboundDisconnectPacket p_294847_);

    void handleResourcePackPush(ClientboundResourcePackPushPacket p_314475_);

    void handleResourcePackPop(ClientboundResourcePackPopPacket p_314490_);

    void handleUpdateTags(ClientboundUpdateTagsPacket p_294883_);
}
