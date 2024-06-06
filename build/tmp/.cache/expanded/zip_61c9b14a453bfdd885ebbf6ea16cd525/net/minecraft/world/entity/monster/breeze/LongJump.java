package net.minecraft.world.entity.monster.breeze;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.LongJumpUtil;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class LongJump extends Behavior<Breeze> {
    private static final int REQUIRED_AIR_BLOCKS_ABOVE = 4;
    private static final double MAX_LINE_OF_SIGHT_TEST_RANGE = 50.0;
    private static final int JUMP_COOLDOWN_TICKS = 10;
    private static final int JUMP_COOLDOWN_WHEN_HURT_TICKS = 2;
    private static final int INHALING_DURATION_TICKS = Math.round(10.0F);
    private static final float MAX_JUMP_VELOCITY = 1.4F;
    private static final ObjectArrayList<Integer> ALLOWED_ANGLES = new ObjectArrayList<>(Lists.newArrayList(40, 55, 60, 75, 80));

    @VisibleForTesting
    public LongJump() {
        super(
            Map.of(
                MemoryModuleType.ATTACK_TARGET,
                MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.BREEZE_JUMP_COOLDOWN,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.BREEZE_JUMP_INHALING,
                MemoryStatus.REGISTERED,
                MemoryModuleType.BREEZE_JUMP_TARGET,
                MemoryStatus.REGISTERED,
                MemoryModuleType.BREEZE_SHOOT,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.WALK_TARGET,
                MemoryStatus.VALUE_ABSENT
            ),
            200
        );
    }

    protected boolean checkExtraStartConditions(ServerLevel p_312131_, Breeze p_312686_) {
        if (!p_312686_.onGround() && !p_312686_.isInWater()) {
            return false;
        } else if (p_312686_.getBrain().checkMemory(MemoryModuleType.BREEZE_JUMP_TARGET, MemoryStatus.VALUE_PRESENT)) {
            return true;
        } else {
            LivingEntity livingentity = p_312686_.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
            if (livingentity == null) {
                return false;
            } else if (outOfAggroRange(p_312686_, livingentity)) {
                p_312686_.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
                return false;
            } else if (tooCloseForJump(p_312686_, livingentity)) {
                return false;
            } else if (!canJumpFromCurrentPosition(p_312131_, p_312686_)) {
                return false;
            } else {
                BlockPos blockpos = snapToSurface(p_312686_, randomPointBehindTarget(livingentity, p_312686_.getRandom()));
                if (blockpos == null) {
                    return false;
                } else if (!hasLineOfSight(p_312686_, blockpos.getCenter()) && !hasLineOfSight(p_312686_, blockpos.above(4).getCenter())) {
                    return false;
                } else {
                    p_312686_.getBrain().setMemory(MemoryModuleType.BREEZE_JUMP_TARGET, blockpos);
                    return true;
                }
            }
        }
    }

    protected boolean canStillUse(ServerLevel p_312482_, Breeze p_312019_, long p_312448_) {
        return p_312019_.getPose() != Pose.STANDING && !p_312019_.getBrain().hasMemoryValue(MemoryModuleType.BREEZE_JUMP_COOLDOWN);
    }

    protected void start(ServerLevel p_312817_, Breeze p_311902_, long p_312420_) {
        if (p_311902_.getBrain().checkMemory(MemoryModuleType.BREEZE_JUMP_INHALING, MemoryStatus.VALUE_ABSENT)) {
            p_311902_.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_JUMP_INHALING, Unit.INSTANCE, (long)INHALING_DURATION_TICKS);
        }

        p_311902_.setPose(Pose.INHALING);
        p_311902_.getBrain()
            .getMemory(MemoryModuleType.BREEZE_JUMP_TARGET)
            .ifPresent(p_312818_ -> p_311902_.lookAt(EntityAnchorArgument.Anchor.EYES, p_312818_.getCenter()));
    }

    protected void tick(ServerLevel p_312091_, Breeze p_312923_, long p_312404_) {
        if (finishedInhaling(p_312923_)) {
            Vec3 vec3 = p_312923_.getBrain()
                .getMemory(MemoryModuleType.BREEZE_JUMP_TARGET)
                .flatMap(p_311796_ -> calculateOptimalJumpVector(p_312923_, p_312923_.getRandom(), Vec3.atBottomCenterOf(p_311796_)))
                .orElse(null);
            if (vec3 == null) {
                p_312923_.setPose(Pose.STANDING);
                return;
            }

            p_312923_.playSound(SoundEvents.BREEZE_JUMP, 1.0F, 1.0F);
            p_312923_.setPose(Pose.LONG_JUMPING);
            p_312923_.setYRot(p_312923_.yBodyRot);
            p_312923_.setDiscardFriction(true);
            p_312923_.setDeltaMovement(vec3);
        } else if (finishedJumping(p_312923_)) {
            p_312923_.playSound(SoundEvents.BREEZE_LAND, 1.0F, 1.0F);
            p_312923_.setPose(Pose.STANDING);
            p_312923_.setDiscardFriction(false);
            boolean flag = p_312923_.getBrain().hasMemoryValue(MemoryModuleType.HURT_BY);
            p_312923_.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_JUMP_COOLDOWN, Unit.INSTANCE, flag ? 2L : 10L);
            p_312923_.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_SHOOT, Unit.INSTANCE, 100L);
        }
    }

    protected void stop(ServerLevel p_312766_, Breeze p_312924_, long p_312793_) {
        if (p_312924_.getPose() == Pose.LONG_JUMPING || p_312924_.getPose() == Pose.INHALING) {
            p_312924_.setPose(Pose.STANDING);
        }

        p_312924_.getBrain().eraseMemory(MemoryModuleType.BREEZE_JUMP_TARGET);
        p_312924_.getBrain().eraseMemory(MemoryModuleType.BREEZE_JUMP_INHALING);
    }

    private static boolean finishedInhaling(Breeze p_312012_) {
        return p_312012_.getBrain().getMemory(MemoryModuleType.BREEZE_JUMP_INHALING).isEmpty() && p_312012_.getPose() == Pose.INHALING;
    }

    private static boolean finishedJumping(Breeze p_311829_) {
        return p_311829_.getPose() == Pose.LONG_JUMPING && p_311829_.onGround();
    }

    private static Vec3 randomPointBehindTarget(LivingEntity p_311877_, RandomSource p_312549_) {
        int i = 90;
        float f = p_311877_.yHeadRot + 180.0F + (float)p_312549_.nextGaussian() * 90.0F / 2.0F;
        float f1 = Mth.lerp(p_312549_.nextFloat(), 4.0F, 8.0F);
        Vec3 vec3 = Vec3.directionFromRotation(0.0F, f).scale((double)f1);
        return p_311877_.position().add(vec3);
    }

    @Nullable
    private static BlockPos snapToSurface(LivingEntity p_311909_, Vec3 p_312597_) {
        ClipContext clipcontext = new ClipContext(
            p_312597_, p_312597_.relative(Direction.DOWN, 10.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p_311909_
        );
        HitResult hitresult = p_311909_.level().clip(clipcontext);
        if (hitresult.getType() == HitResult.Type.BLOCK) {
            return BlockPos.containing(hitresult.getLocation()).above();
        } else {
            ClipContext clipcontext1 = new ClipContext(
                p_312597_, p_312597_.relative(Direction.UP, 10.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p_311909_
            );
            HitResult hitresult1 = p_311909_.level().clip(clipcontext1);
            return hitresult1.getType() == HitResult.Type.BLOCK ? BlockPos.containing(hitresult.getLocation()).above() : null;
        }
    }

    @VisibleForTesting
    public static boolean hasLineOfSight(Breeze p_312681_, Vec3 p_311816_) {
        Vec3 vec3 = new Vec3(p_312681_.getX(), p_312681_.getY(), p_312681_.getZ());
        if (p_311816_.distanceTo(vec3) > 50.0) {
            return false;
        } else {
            return p_312681_.level().clip(new ClipContext(vec3, p_311816_, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p_312681_)).getType()
                == HitResult.Type.MISS;
        }
    }

    private static boolean outOfAggroRange(Breeze p_312045_, LivingEntity p_312043_) {
        return !p_312043_.closerThan(p_312045_, 24.0);
    }

    private static boolean tooCloseForJump(Breeze p_312356_, LivingEntity p_312654_) {
        return p_312654_.distanceTo(p_312356_) - 4.0F <= 0.0F;
    }

    private static boolean canJumpFromCurrentPosition(ServerLevel p_312261_, Breeze p_312824_) {
        BlockPos blockpos = p_312824_.blockPosition();

        for(int i = 1; i <= 4; ++i) {
            BlockPos blockpos1 = blockpos.relative(Direction.UP, i);
            if (!p_312261_.getBlockState(blockpos1).isAir() && !p_312261_.getFluidState(blockpos1).is(FluidTags.WATER)) {
                return false;
            }
        }

        return true;
    }

    private static Optional<Vec3> calculateOptimalJumpVector(Breeze p_312651_, RandomSource p_312364_, Vec3 p_312758_) {
        for(int i : Util.shuffledCopy(ALLOWED_ANGLES, p_312364_)) {
            Optional<Vec3> optional = LongJumpUtil.calculateJumpVectorForAngle(p_312651_, p_312758_, 1.4F, i, false);
            if (optional.isPresent()) {
                return optional;
            }
        }

        return Optional.empty();
    }
}
