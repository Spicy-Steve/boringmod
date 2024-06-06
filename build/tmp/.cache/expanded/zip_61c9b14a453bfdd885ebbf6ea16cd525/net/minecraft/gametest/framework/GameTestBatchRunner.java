package net.minecraft.gametest.framework;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

public class GameTestBatchRunner {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final BlockPos firstTestNorthWestCorner;
    final ServerLevel level;
    private final GameTestTicker testTicker;
    private final int testsPerRow;
    private final List<GameTestInfo> allTestInfos;
    private final List<Pair<GameTestBatch, Collection<GameTestInfo>>> batches;
    private int count;
    private AABB rowBounds;
    private final BlockPos.MutableBlockPos nextTestNorthWestCorner;

    public GameTestBatchRunner(
        Collection<GameTestBatch> p_127563_, BlockPos p_127564_, Rotation p_127565_, ServerLevel p_127566_, GameTestTicker p_127567_, int p_127568_
    ) {
        this.nextTestNorthWestCorner = p_127564_.mutable();
        this.rowBounds = new AABB(this.nextTestNorthWestCorner);
        this.firstTestNorthWestCorner = p_127564_;
        this.level = p_127566_;
        this.testTicker = p_127567_;
        this.testsPerRow = p_127568_;
        this.batches = p_127563_.stream()
            .map(
                p_177068_ -> {
                    Collection<GameTestInfo> collection = p_177068_.getTestFunctions()
                        .stream()
                        .map(p_177072_ -> new GameTestInfo(p_177072_, p_127565_, p_127566_))
                        .collect(ImmutableList.toImmutableList());
                    return Pair.of(p_177068_, collection);
                }
            )
            .collect(ImmutableList.toImmutableList());
        this.allTestInfos = this.batches.stream().flatMap(p_177074_ -> p_177074_.getSecond().stream()).collect(ImmutableList.toImmutableList());
    }

    public List<GameTestInfo> getTestInfos() {
        return this.allTestInfos;
    }

    public void start() {
        this.runBatch(0);
    }

    void runBatch(final int p_127571_) {
        if (p_127571_ < this.batches.size()) {
            Pair<GameTestBatch, Collection<GameTestInfo>> pair = this.batches.get(p_127571_);
            final GameTestBatch gametestbatch = pair.getFirst();
            Collection<GameTestInfo> collection = pair.getSecond();
            Map<GameTestInfo, BlockPos> map = this.createStructuresForBatch(collection);
            String s = gametestbatch.getName();
            LOGGER.info("Running test batch '{}' ({} tests)...", s, collection.size());
            gametestbatch.runBeforeBatchFunction(this.level);
            final MultipleTestTracker multipletesttracker = new MultipleTestTracker();
            collection.forEach(multipletesttracker::addTestToTrack);
            multipletesttracker.addListener(new GameTestListener() {
                private void testCompleted() {
                    if (multipletesttracker.isDone()) {
                        gametestbatch.runAfterBatchFunction(GameTestBatchRunner.this.level);
                        LongSet longset = new LongArraySet(GameTestBatchRunner.this.level.getForcedChunks());
                        longset.forEach(p_309120_ -> GameTestBatchRunner.this.level.setChunkForced(ChunkPos.getX(p_309120_), ChunkPos.getZ(p_309120_), false));
                        GameTestBatchRunner.this.runBatch(p_127571_ + 1);
                    }
                }

                @Override
                public void testStructureLoaded(GameTestInfo p_127590_) {
                }

                @Override
                public void testPassed(GameTestInfo p_177090_) {
                    this.testCompleted();
                }

                @Override
                public void testFailed(GameTestInfo p_127592_) {
                    this.testCompleted();
                }
            });
            collection.forEach(p_177079_ -> {
                BlockPos blockpos = map.get(p_177079_);
                GameTestRunner.runTest(p_177079_, blockpos, this.testTicker);
            });
        }
    }

    private Map<GameTestInfo, BlockPos> createStructuresForBatch(Collection<GameTestInfo> p_177076_) {
        Map<GameTestInfo, BlockPos> map = Maps.newHashMap();

        for(GameTestInfo gametestinfo : p_177076_) {
            BlockPos blockpos = new BlockPos(this.nextTestNorthWestCorner);
            StructureBlockEntity structureblockentity = StructureUtils.prepareTestStructure(gametestinfo, blockpos, gametestinfo.getRotation(), this.level);
            AABB aabb = StructureUtils.getStructureBounds(structureblockentity);
            gametestinfo.setStructureBlockPos(structureblockentity.getBlockPos());
            map.put(gametestinfo, new BlockPos(this.nextTestNorthWestCorner));
            this.rowBounds = this.rowBounds.minmax(aabb);
            this.nextTestNorthWestCorner.move((int)aabb.getXsize() + 5, 0, 0);
            if (this.count++ % this.testsPerRow == this.testsPerRow - 1) {
                this.nextTestNorthWestCorner.move(0, 0, (int)this.rowBounds.getZsize() + 6);
                this.nextTestNorthWestCorner.setX(this.firstTestNorthWestCorner.getX());
                this.rowBounds = new AABB(this.nextTestNorthWestCorner);
            }
        }

        return map;
    }
}
