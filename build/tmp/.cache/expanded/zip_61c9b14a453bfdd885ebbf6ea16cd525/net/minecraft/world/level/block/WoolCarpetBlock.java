package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class WoolCarpetBlock extends CarpetBlock {
    public static final MapCodec<WoolCarpetBlock> CODEC = RecordCodecBuilder.mapCodec(
        p_308855_ -> p_308855_.group(DyeColor.CODEC.fieldOf("color").forGetter(WoolCarpetBlock::getColor), propertiesCodec())
                .apply(p_308855_, WoolCarpetBlock::new)
    );
    private final DyeColor color;

    @Override
    public MapCodec<WoolCarpetBlock> codec() {
        return CODEC;
    }

    public WoolCarpetBlock(DyeColor p_58291_, BlockBehaviour.Properties p_58292_) {
        super(p_58292_);
        this.color = p_58291_;
    }

    public DyeColor getColor() {
        return this.color;
    }
}
