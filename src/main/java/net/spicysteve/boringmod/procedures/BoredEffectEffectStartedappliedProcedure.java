package net.spicysteve.boringmod.procedures;

import net.spicysteve.boringmod.BoringmodMod;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.BlockPos;

public class BoredEffectEffectStartedappliedProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		world.setBlock(BlockPos.containing(x, y - 1, z), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x - 1, y - 1, z + 1), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x - 1, y - 1, z - 1), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x - 1, y - 1, z), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x, y - 1, z - 1), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x + 1, y - 1, z - 1), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x + 1, y - 1, z), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x + 1, y - 1, z + 1), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x, y - 1, z + 1), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x, y - 1, z), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x + 2, y, z), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x - 2, y, z), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x + 2, y, z + 1), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x + 1, y, z + 2), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x + 1, y, z - 2), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x - 1, y, z + 2), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x - 1, y, z - 2), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x, y, z - 2), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x, y, z + 2), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x + 2, y, z - 1), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x - 2, y, z - 1), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x + 2, y + 1, z), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x - 2, y + 1, z), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x + 2, y + 1, z + 1), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x + 1, y + 1, z + 2), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x + 1, y + 1, z - 2), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x - 1, y + 1, z + 2), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x - 1, y + 1, z - 2), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x, y + 1, z - 2), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x, y + 1, z + 2), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x + 2, y + 1, z - 1), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x - 2, y + 1, z - 1), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x - 2, y + 1, z + 1), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x - 2, y, z + 1), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x, y + 2, z), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x - 1, y + 2, z + 1), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x - 1, y + 2, z - 1), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x - 1, y + 2, z), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x, y + 2, z - 1), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x + 1, y + 2, z - 1), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x + 1, y + 2, z), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x + 1, y + 2, z + 1), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x, y + 2, z + 1), Blocks.BEDROCK.defaultBlockState(), 3);
		world.setBlock(BlockPos.containing(x, y + 2, z), Blocks.BEDROCK.defaultBlockState(), 3);
		if (entity instanceof Player _player)
			_player.causeFoodExhaustion(1);
		BoringmodMod.queueServerWork(2400, () -> {
			entity.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.GENERIC)), 5);
			if (entity instanceof Player _player)
				_player.causeFoodExhaustion(2);
		});
		BoringmodMod.queueServerWork(2400, () -> {
			entity.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.GENERIC)), 10);
			if (entity instanceof Player _player)
				_player.causeFoodExhaustion(4);
		});
		BoringmodMod.queueServerWork(7200, () -> {
			entity.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.GENERIC)), 20);
		});
	}
}
