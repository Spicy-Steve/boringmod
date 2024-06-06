package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.NumberFormatTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class ClientboundSetObjectivePacket implements Packet<ClientGamePacketListener> {
    public static final int METHOD_ADD = 0;
    public static final int METHOD_REMOVE = 1;
    public static final int METHOD_CHANGE = 2;
    private final String objectiveName;
    private final Component displayName;
    private final ObjectiveCriteria.RenderType renderType;
    @Nullable
    private final NumberFormat numberFormat;
    private final int method;

    public ClientboundSetObjectivePacket(Objective p_133258_, int p_133259_) {
        this.objectiveName = p_133258_.getName();
        this.displayName = p_133258_.getDisplayName();
        this.renderType = p_133258_.getRenderType();
        this.numberFormat = p_133258_.numberFormat();
        this.method = p_133259_;
    }

    public ClientboundSetObjectivePacket(FriendlyByteBuf p_179306_) {
        this.objectiveName = p_179306_.readUtf();
        this.method = p_179306_.readByte();
        if (this.method != 0 && this.method != 2) {
            this.displayName = CommonComponents.EMPTY;
            this.renderType = ObjectiveCriteria.RenderType.INTEGER;
            this.numberFormat = null;
        } else {
            this.displayName = p_179306_.readComponentTrusted();
            this.renderType = p_179306_.readEnum(ObjectiveCriteria.RenderType.class);
            this.numberFormat = p_179306_.readNullable(NumberFormatTypes::readFromStream);
        }
    }

    @Override
    public void write(FriendlyByteBuf p_133268_) {
        p_133268_.writeUtf(this.objectiveName);
        p_133268_.writeByte(this.method);
        if (this.method == 0 || this.method == 2) {
            p_133268_.writeComponent(this.displayName);
            p_133268_.writeEnum(this.renderType);
            p_133268_.writeNullable(this.numberFormat, NumberFormatTypes::writeToStream);
        }
    }

    public void handle(ClientGamePacketListener p_133265_) {
        p_133265_.handleAddObjective(this);
    }

    public String getObjectiveName() {
        return this.objectiveName;
    }

    public Component getDisplayName() {
        return this.displayName;
    }

    public int getMethod() {
        return this.method;
    }

    public ObjectiveCriteria.RenderType getRenderType() {
        return this.renderType;
    }

    @Nullable
    public NumberFormat getNumberFormat() {
        return this.numberFormat;
    }
}
