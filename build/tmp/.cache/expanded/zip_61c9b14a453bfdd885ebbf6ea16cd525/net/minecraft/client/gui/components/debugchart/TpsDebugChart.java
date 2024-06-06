package net.minecraft.client.gui.components.debugchart;

import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.SampleLogger;
import net.minecraft.util.TimeUtil;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TpsDebugChart extends AbstractDebugChart {
    private static final int RED = -65536;
    private static final int YELLOW = -256;
    private static final int GREEN = -16711936;
    private final Supplier<Float> msptSupplier;

    public TpsDebugChart(Font p_299254_, SampleLogger p_299229_, Supplier<Float> p_309098_) {
        super(p_299254_, p_299229_);
        this.msptSupplier = p_309098_;
    }

    @Override
    protected void renderAdditionalLinesAndLabels(GuiGraphics p_298653_, int p_298791_, int p_298387_, int p_298869_) {
        float f = (float)TimeUtil.MILLISECONDS_PER_SECOND / this.msptSupplier.get();
        this.drawStringWithShade(p_298653_, String.format("%.1f TPS", f), p_298791_ + 1, p_298869_ - 60 + 1);
    }

    @Override
    protected String toDisplayString(double p_298403_) {
        return String.format(Locale.ROOT, "%d ms", (int)Math.round(toMilliseconds(p_298403_)));
    }

    @Override
    protected int getSampleHeight(double p_299161_) {
        return (int)Math.round(toMilliseconds(p_299161_) * 60.0 / (double)this.msptSupplier.get().floatValue());
    }

    @Override
    protected int getSampleColor(long p_299243_) {
        float f = this.msptSupplier.get();
        return this.getSampleColor(toMilliseconds((double)p_299243_), 0.0, -16711936, (double)f / 2.0, -256, (double)f, -65536);
    }

    private static double toMilliseconds(double p_298223_) {
        return p_298223_ / 1000000.0;
    }
}
