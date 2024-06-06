package net.minecraft.data.worldgen;

import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CopperBulbBlock;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProtectedBlockProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.RandomBlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public class UpdateOneTwentyOneProcessorLists {
    public static final ResourceKey<StructureProcessorList> TRIAL_CHAMBERS_COPPER_BULB_DEGRADATION = ResourceKey.create(
        Registries.PROCESSOR_LIST, new ResourceLocation("trial_chambers_copper_bulb_degradation")
    );

    public static void bootstrap(BootstapContext<StructureProcessorList> p_312160_) {
        register(
            p_312160_,
            TRIAL_CHAMBERS_COPPER_BULB_DEGRADATION,
            List.of(
                new RuleProcessor(
                    List.of(
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.WAXED_COPPER_BULB, 0.1F),
                            AlwaysTrueTest.INSTANCE,
                            Blocks.OXIDIZED_COPPER_BULB.defaultBlockState().setValue(CopperBulbBlock.LIT, Boolean.valueOf(true))
                        ),
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.WAXED_COPPER_BULB, 0.33333334F),
                            AlwaysTrueTest.INSTANCE,
                            Blocks.WEATHERED_COPPER_BULB.defaultBlockState().setValue(CopperBulbBlock.LIT, Boolean.valueOf(true))
                        ),
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.WAXED_COPPER_BULB, 0.5F),
                            AlwaysTrueTest.INSTANCE,
                            Blocks.EXPOSED_COPPER_BULB.defaultBlockState().setValue(CopperBulbBlock.LIT, Boolean.valueOf(true))
                        )
                    )
                ),
                new ProtectedBlockProcessor(BlockTags.FEATURES_CANNOT_REPLACE)
            )
        );
    }

    private static void register(
        BootstapContext<StructureProcessorList> p_312838_, ResourceKey<StructureProcessorList> p_311901_, List<StructureProcessor> p_312918_
    ) {
        p_312838_.register(p_311901_, new StructureProcessorList(p_312918_));
    }
}
