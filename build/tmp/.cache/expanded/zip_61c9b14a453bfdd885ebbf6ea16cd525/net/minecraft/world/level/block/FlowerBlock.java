package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FlowerBlock extends BushBlock implements SuspiciousEffectHolder {
    protected static final MapCodec<List<SuspiciousEffectHolder.EffectEntry>> EFFECTS_FIELD = SuspiciousEffectHolder.EffectEntry.LIST_CODEC
        .fieldOf("suspicious_stew_effects");
    public static final MapCodec<FlowerBlock> CODEC = RecordCodecBuilder.mapCodec(
        p_308824_ -> p_308824_.group(EFFECTS_FIELD.forGetter(FlowerBlock::getSuspiciousEffects), propertiesCodec()).apply(p_308824_, FlowerBlock::new)
    );
    protected static final float AABB_OFFSET = 3.0F;
    protected static final VoxelShape SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 10.0, 11.0);
    private final java.util.function.Supplier<java.util.List<net.minecraft.world.level.block.SuspiciousEffectHolder.EffectEntry>> suspiciousStewEffectSupplier;

    public FlowerBlock(java.util.function.Supplier<net.minecraft.world.effect.MobEffect> effectSupplier, int p_53513_, BlockBehaviour.Properties p_53514_) {
        super(p_53514_);
        this.suspiciousStewEffectSupplier = () -> makeEffectList(effectSupplier.get(), p_53513_);
    }

    @Override
    public MapCodec<? extends FlowerBlock> codec() {
        return CODEC;
    }

    /** @deprecated FORGE: Use supplier version instead */
    @Deprecated
    public FlowerBlock(MobEffect p_53512_, int p_53513_, BlockBehaviour.Properties p_53514_) {
        this(makeEffectList(p_53512_, p_53513_), p_53514_);
    }

    public FlowerBlock(List<SuspiciousEffectHolder.EffectEntry> p_304513_, BlockBehaviour.Properties p_304822_) {
        super(p_304822_);
        this.suspiciousStewEffectSupplier = () -> p_304513_;
    }

    protected static List<SuspiciousEffectHolder.EffectEntry> makeEffectList(MobEffect p_304981_, int p_304406_) {
        int i;
        if (p_304981_.isInstantenous()) {
            i = p_304406_;
        } else {
            i = p_304406_ * 20;
        }

        return List.of(new SuspiciousEffectHolder.EffectEntry(p_304981_, i));
    }

    @Override
    public VoxelShape getShape(BlockState p_53517_, BlockGetter p_53518_, BlockPos p_53519_, CollisionContext p_53520_) {
        Vec3 vec3 = p_53517_.getOffset(p_53518_, p_53519_);
        return SHAPE.move(vec3.x, vec3.y, vec3.z);
    }

    @Override
    public List<SuspiciousEffectHolder.EffectEntry> getSuspiciousEffects() {
        return this.suspiciousStewEffectSupplier.get();
    }
}
