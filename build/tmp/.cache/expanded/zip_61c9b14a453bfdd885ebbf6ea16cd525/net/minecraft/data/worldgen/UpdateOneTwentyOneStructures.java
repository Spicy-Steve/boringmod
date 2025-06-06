package net.minecraft.data.worldgen;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;

public class UpdateOneTwentyOneStructures {
    public static void bootstrap(BootstapContext<Structure> p_312245_) {
        HolderGetter<Biome> holdergetter = p_312245_.lookup(Registries.BIOME);
        HolderGetter<StructureTemplatePool> holdergetter1 = p_312245_.lookup(Registries.TEMPLATE_POOL);
        p_312245_.register(
            BuiltinStructures.TRIAL_CHAMBERS,
            new JigsawStructure(
                Structures.structure(
                    holdergetter.getOrThrow(BiomeTags.HAS_TRIAL_CHAMBERS),
                    Arrays.stream(MobCategory.values())
                        .collect(
                            Collectors.toMap(
                                p_311885_ -> p_311885_,
                                p_311971_ -> new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.PIECE, WeightedRandomList.create())
                            )
                        ),
                    GenerationStep.Decoration.UNDERGROUND_STRUCTURES,
                    TerrainAdjustment.BURY
                ),
                holdergetter1.getOrThrow(TrialChambersStructurePools.START),
                Optional.empty(),
                20,
                UniformHeight.of(VerticalAnchor.absolute(-40), VerticalAnchor.absolute(-20)),
                false,
                Optional.empty(),
                116,
                TrialChambersStructurePools.ALIAS_BINDINGS
            )
        );
    }
}
