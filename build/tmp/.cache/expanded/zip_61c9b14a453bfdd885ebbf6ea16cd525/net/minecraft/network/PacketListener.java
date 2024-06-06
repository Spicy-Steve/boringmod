package net.minecraft.network;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;

public interface PacketListener {
    PacketFlow flow();

    ConnectionProtocol protocol();

    void onDisconnect(Component p_130552_);

    boolean isAcceptingMessages();

    default boolean shouldHandleMessage(Packet<?> p_295210_) {
        return this.isAcceptingMessages();
    }

    default boolean shouldPropagateHandlingExceptions() {
        return true;
    }

    default void fillCrashReport(CrashReport p_314927_) {
        CrashReportCategory crashreportcategory = p_314927_.addCategory("Connection");
        crashreportcategory.setDetail("Protocol", () -> this.protocol().id());
        crashreportcategory.setDetail("Flow", () -> this.flow().toString());
        this.fillListenerSpecificCrashDetails(crashreportcategory);
    }

    default void fillListenerSpecificCrashDetails(CrashReportCategory p_314965_) {
    }
}
