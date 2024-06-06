package net.spicysteve.boringmod.procedures;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.core.BlockPos;

public class BoredEffectEffectExpiresProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z) {
		world.setBlock(BlockPos.containing(x, y - 1, z), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x - 1, y - 1, z + 1), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x - 1, y - 1, z - 1), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x - 1, y - 1, z), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x, y - 1, z - 1), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x + 1, y - 1, z - 1), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x + 1, y - 1, z), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x + 1, y - 1, z + 1), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x, y - 1, z + 1), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x, y - 1, z), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x + 2, y, z), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x - 2, y, z), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x + 2, y, z + 1), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x + 1, y, z + 2), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x + 1, y, z - 2), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x - 1, y, z + 2), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x - 1, y, z - 2), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x, y, z - 2), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x, y, z + 2), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x + 2, y, z - 1), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x - 2, y, z - 1), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x + 2, y + 1, z), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x - 2, y + 1, z), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x + 2, y + 1, z + 1), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x + 1, y + 1, z + 2), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x + 1, y + 1, z - 2), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x - 1, y + 1, z + 2), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x - 1, y + 1, z - 2), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x, y + 1, z - 2), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x, y + 1, z + 2), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x + 2, y + 1, z - 1), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x - 2, y + 1, z - 1), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x - 2, y + 1, z + 1), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x - 2, y, z + 1), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x, y + 2, z), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x - 1, y + 2, z + 1), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x - 1, y + 2, z - 1), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x - 1, y + 2, z), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x, y + 2, z - 1), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x + 1, y + 2, z - 1), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x + 1, y + 2, z), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x + 1, y + 2, z + 1), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x, y + 2, z + 1), Blocks.AIR.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x, y + 2, z), Blocks.AIR.defaultBlockState(), 3);
	}
}
