package net.minecraft.client.gui.components;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarrationSupplier;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.BelowOrAboveWidgetTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.MenuTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Tooltip implements NarrationSupplier {
    private static final int MAX_WIDTH = 170;
    private final Component message;
    @Nullable
    private List<FormattedCharSequence> cachedTooltip;
    @Nullable
    private final Component narration;
    private int msDelay;
    private long hoverOrFocusedStartTime;
    private boolean wasHoveredOrFocused;

    private Tooltip(Component p_260262_, @Nullable Component p_260005_) {
        this.message = p_260262_;
        this.narration = p_260005_;
    }

    public void setDelay(int p_306340_) {
        this.msDelay = p_306340_;
    }

    public static Tooltip create(Component p_259571_, @Nullable Component p_259174_) {
        return new Tooltip(p_259571_, p_259174_);
    }

    public static Tooltip create(Component p_259142_) {
        return new Tooltip(p_259142_, p_259142_);
    }

    @Override
    public void updateNarration(NarrationElementOutput p_260330_) {
        if (this.narration != null) {
            p_260330_.add(NarratedElementType.HINT, this.narration);
        }
    }

    public List<FormattedCharSequence> toCharSequence(Minecraft p_260243_) {
        if (this.cachedTooltip == null) {
            this.cachedTooltip = splitTooltip(p_260243_, this.message);
        }

        return this.cachedTooltip;
    }

    public static List<FormattedCharSequence> splitTooltip(Minecraft p_259133_, Component p_260172_) {
        return p_259133_.font.split(p_260172_, 170);
    }

    public void refreshTooltipForNextRenderPass(boolean p_306201_, boolean p_306100_, ScreenRectangle p_305970_) {
        boolean flag = p_306201_ || p_306100_ && Minecraft.getInstance().getLastInputType().isKeyboard();
        if (flag != this.wasHoveredOrFocused) {
            if (flag) {
                this.hoverOrFocusedStartTime = Util.getMillis();
            }

            this.wasHoveredOrFocused = flag;
        }

        if (flag && Util.getMillis() - this.hoverOrFocusedStartTime > (long)this.msDelay) {
            Screen screen = Minecraft.getInstance().screen;
            if (screen != null) {
                screen.setTooltipForNextRenderPass(this, this.createTooltipPositioner(p_306201_, p_306100_, p_305970_), p_306100_);
            }
        }
    }

    protected ClientTooltipPositioner createTooltipPositioner(boolean p_306065_, boolean p_306270_, ScreenRectangle p_305822_) {
        return (ClientTooltipPositioner)(!p_306065_ && p_306270_ && Minecraft.getInstance().getLastInputType().isKeyboard()
            ? new BelowOrAboveWidgetTooltipPositioner(p_305822_)
            : new MenuTooltipPositioner(p_305822_));
    }
}
