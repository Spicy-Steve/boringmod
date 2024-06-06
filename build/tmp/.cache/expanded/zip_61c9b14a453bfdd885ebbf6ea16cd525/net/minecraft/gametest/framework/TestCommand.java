package net.minecraft.gametest.framework;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.FileUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.structures.NbtToSnbt;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class TestCommand {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int DEFAULT_CLEAR_RADIUS = 200;
    private static final int MAX_CLEAR_RADIUS = 1024;
    private static final int STRUCTURE_BLOCK_NEARBY_SEARCH_RADIUS = 15;
    private static final int STRUCTURE_BLOCK_FULL_SEARCH_RADIUS = 200;
    private static final int TEST_POS_Z_OFFSET_FROM_PLAYER = 3;
    private static final int SHOW_POS_DURATION_MS = 10000;
    private static final int DEFAULT_X_SIZE = 5;
    private static final int DEFAULT_Y_SIZE = 5;
    private static final int DEFAULT_Z_SIZE = 5;

    public static void register(CommandDispatcher<CommandSourceStack> p_127947_) {
        p_127947_.register(
            Commands.literal("test")
                .then(
                    Commands.literal("runthis")
                        .executes(p_304148_ -> runNearbyTest(p_304148_.getSource(), false))
                        .then(Commands.literal("untilFailed").executes(p_304147_ -> runNearbyTest(p_304147_.getSource(), true)))
                )
                .then(Commands.literal("resetthis").executes(p_313466_ -> resetNearbyTest(p_313466_.getSource())))
                .then(Commands.literal("runthese").executes(p_304153_ -> runAllNearbyTests(p_304153_.getSource(), false)))
                .then(
                    Commands.literal("runfailed")
                        .executes(p_128053_ -> runLastFailedTests(p_128053_.getSource(), false, 0, 8))
                        .then(
                            Commands.argument("onlyRequiredTests", BoolArgumentType.bool())
                                .executes(
                                    p_128051_ -> runLastFailedTests(p_128051_.getSource(), BoolArgumentType.getBool(p_128051_, "onlyRequiredTests"), 0, 8)
                                )
                                .then(
                                    Commands.argument("rotationSteps", IntegerArgumentType.integer())
                                        .executes(
                                            p_128049_ -> runLastFailedTests(
                                                    p_128049_.getSource(),
                                                    BoolArgumentType.getBool(p_128049_, "onlyRequiredTests"),
                                                    IntegerArgumentType.getInteger(p_128049_, "rotationSteps"),
                                                    8
                                                )
                                        )
                                        .then(
                                            Commands.argument("testsPerRow", IntegerArgumentType.integer())
                                                .executes(
                                                    p_128047_ -> runLastFailedTests(
                                                            p_128047_.getSource(),
                                                            BoolArgumentType.getBool(p_128047_, "onlyRequiredTests"),
                                                            IntegerArgumentType.getInteger(p_128047_, "rotationSteps"),
                                                            IntegerArgumentType.getInteger(p_128047_, "testsPerRow")
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .then(
                    Commands.literal("run")
                        .then(
                            Commands.argument("testName", TestFunctionArgument.testFunctionArgument())
                                .executes(p_128045_ -> runTest(p_128045_.getSource(), TestFunctionArgument.getTestFunction(p_128045_, "testName"), 0))
                                .then(
                                    Commands.argument("rotationSteps", IntegerArgumentType.integer())
                                        .executes(
                                            p_128043_ -> runTest(
                                                    p_128043_.getSource(),
                                                    TestFunctionArgument.getTestFunction(p_128043_, "testName"),
                                                    IntegerArgumentType.getInteger(p_128043_, "rotationSteps")
                                                )
                                        )
                                )
                        )
                )
                .then(
                    Commands.literal("runall")
                        .executes(p_128041_ -> runAllTests(p_128041_.getSource(), 0, 8))
                        .then(
                            Commands.argument("testClassName", TestClassNameArgument.testClassName())
                                .executes(
                                    p_128039_ -> runAllTestsInClass(
                                            p_128039_.getSource(), TestClassNameArgument.getTestClassName(p_128039_, "testClassName"), 0, 8
                                        )
                                )
                                .then(
                                    Commands.argument("rotationSteps", IntegerArgumentType.integer())
                                        .executes(
                                            p_128037_ -> runAllTestsInClass(
                                                    p_128037_.getSource(),
                                                    TestClassNameArgument.getTestClassName(p_128037_, "testClassName"),
                                                    IntegerArgumentType.getInteger(p_128037_, "rotationSteps"),
                                                    8
                                                )
                                        )
                                        .then(
                                            Commands.argument("testsPerRow", IntegerArgumentType.integer())
                                                .executes(
                                                    p_128035_ -> runAllTestsInClass(
                                                            p_128035_.getSource(),
                                                            TestClassNameArgument.getTestClassName(p_128035_, "testClassName"),
                                                            IntegerArgumentType.getInteger(p_128035_, "rotationSteps"),
                                                            IntegerArgumentType.getInteger(p_128035_, "testsPerRow")
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(
                            Commands.argument("rotationSteps", IntegerArgumentType.integer())
                                .executes(p_128033_ -> runAllTests(p_128033_.getSource(), IntegerArgumentType.getInteger(p_128033_, "rotationSteps"), 8))
                                .then(
                                    Commands.argument("testsPerRow", IntegerArgumentType.integer())
                                        .executes(
                                            p_128031_ -> runAllTests(
                                                    p_128031_.getSource(),
                                                    IntegerArgumentType.getInteger(p_128031_, "rotationSteps"),
                                                    IntegerArgumentType.getInteger(p_128031_, "testsPerRow")
                                                )
                                        )
                                )
                        )
                )
                .then(
                    Commands.literal("export")
                        .then(
                            Commands.argument("testName", StringArgumentType.word())
                                .executes(p_128029_ -> exportTestStructure(p_128029_.getSource(), StringArgumentType.getString(p_128029_, "testName")))
                        )
                )
                .then(Commands.literal("exportthis").executes(p_128027_ -> exportNearestTestStructure(p_128027_.getSource())))
                .then(Commands.literal("exportthese").executes(p_293702_ -> exportAllNearbyTests(p_293702_.getSource())))
                .then(
                    Commands.literal("import")
                        .then(
                            Commands.argument("testName", StringArgumentType.word())
                                .executes(p_128025_ -> importTestStructure(p_128025_.getSource(), StringArgumentType.getString(p_128025_, "testName")))
                        )
                )
                .then(
                    Commands.literal("pos")
                        .executes(p_128023_ -> showPos(p_128023_.getSource(), "pos"))
                        .then(
                            Commands.argument("var", StringArgumentType.word())
                                .executes(p_128021_ -> showPos(p_128021_.getSource(), StringArgumentType.getString(p_128021_, "var")))
                        )
                )
                .then(
                    Commands.literal("create")
                        .then(
                            Commands.argument("testName", StringArgumentType.word())
                                .executes(p_128019_ -> createNewStructure(p_128019_.getSource(), StringArgumentType.getString(p_128019_, "testName"), 5, 5, 5))
                                .then(
                                    Commands.argument("width", IntegerArgumentType.integer())
                                        .executes(
                                            p_128014_ -> createNewStructure(
                                                    p_128014_.getSource(),
                                                    StringArgumentType.getString(p_128014_, "testName"),
                                                    IntegerArgumentType.getInteger(p_128014_, "width"),
                                                    IntegerArgumentType.getInteger(p_128014_, "width"),
                                                    IntegerArgumentType.getInteger(p_128014_, "width")
                                                )
                                        )
                                        .then(
                                            Commands.argument("height", IntegerArgumentType.integer())
                                                .then(
                                                    Commands.argument("depth", IntegerArgumentType.integer())
                                                        .executes(
                                                            p_128007_ -> createNewStructure(
                                                                    p_128007_.getSource(),
                                                                    StringArgumentType.getString(p_128007_, "testName"),
                                                                    IntegerArgumentType.getInteger(p_128007_, "width"),
                                                                    IntegerArgumentType.getInteger(p_128007_, "height"),
                                                                    IntegerArgumentType.getInteger(p_128007_, "depth")
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .then(
                    Commands.literal("clearall")
                        .executes(p_128000_ -> clearAllTests(p_128000_.getSource(), 200))
                        .then(
                            Commands.argument("radius", IntegerArgumentType.integer())
                                .executes(p_127949_ -> clearAllTests(p_127949_.getSource(), IntegerArgumentType.getInteger(p_127949_, "radius")))
                        )
                )
        );
    }

    private static int createNewStructure(CommandSourceStack p_127968_, String p_127969_, int p_127970_, int p_127971_, int p_127972_) {
        if (p_127970_ <= 48 && p_127971_ <= 48 && p_127972_ <= 48) {
            ServerLevel serverlevel = p_127968_.getLevel();
            BlockPos blockpos = createTestPositionAround(p_127968_).below();
            StructureUtils.createNewEmptyStructureBlock(
                p_127969_.toLowerCase(), blockpos, new Vec3i(p_127970_, p_127971_, p_127972_), Rotation.NONE, serverlevel
            );

            for(int i = 0; i < p_127970_; ++i) {
                for(int j = 0; j < p_127972_; ++j) {
                    BlockPos blockpos1 = new BlockPos(blockpos.getX() + i, blockpos.getY() + 1, blockpos.getZ() + j);
                    Block block = Blocks.POLISHED_ANDESITE;
                    BlockInput blockinput = new BlockInput(block.defaultBlockState(), Collections.emptySet(), null);
                    blockinput.place(serverlevel, blockpos1, 2);
                }
            }

            StructureUtils.addCommandBlockAndButtonToStartTest(blockpos, new BlockPos(1, 0, -1), Rotation.NONE, serverlevel);
            return 0;
        } else {
            throw new IllegalArgumentException("The structure must be less than 48 blocks big in each axis");
        }
    }

    private static int showPos(CommandSourceStack p_127960_, String p_127961_) throws CommandSyntaxException {
        BlockHitResult blockhitresult = (BlockHitResult)p_127960_.getPlayerOrException().pick(10.0, 1.0F, false);
        BlockPos blockpos = blockhitresult.getBlockPos();
        ServerLevel serverlevel = p_127960_.getLevel();
        Optional<BlockPos> optional = StructureUtils.findStructureBlockContainingPos(blockpos, 15, serverlevel);
        if (optional.isEmpty()) {
            optional = StructureUtils.findStructureBlockContainingPos(blockpos, 200, serverlevel);
        }

        if (optional.isEmpty()) {
            p_127960_.sendFailure(Component.literal("Can't find a structure block that contains the targeted pos " + blockpos));
            return 0;
        } else {
            StructureBlockEntity structureblockentity = (StructureBlockEntity)serverlevel.getBlockEntity(optional.get());
            BlockPos blockpos1 = blockpos.subtract(optional.get());
            String s = blockpos1.getX() + ", " + blockpos1.getY() + ", " + blockpos1.getZ();
            String s1 = structureblockentity.getMetaData();
            Component component = Component.literal(s)
                .setStyle(
                    Style.EMPTY
                        .withBold(true)
                        .withColor(ChatFormatting.GREEN)
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to copy to clipboard")))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "final BlockPos " + p_127961_ + " = new BlockPos(" + s + ");"))
                );
            p_127960_.sendSuccess(() -> Component.literal("Position relative to " + s1 + ": ").append(component), false);
            DebugPackets.sendGameTestAddMarker(serverlevel, new BlockPos(blockpos), s, -2147418368, 10000);
            return 1;
        }
    }

    private static int runNearbyTest(CommandSourceStack p_127951_, boolean p_304385_) {
        BlockPos blockpos = BlockPos.containing(p_127951_.getPosition());
        ServerLevel serverlevel = p_127951_.getLevel();
        BlockPos blockpos1 = StructureUtils.findNearestStructureBlock(blockpos, 15, serverlevel);
        if (blockpos1 == null) {
            say(serverlevel, "Couldn't find any structure block within 15 radius", ChatFormatting.RED);
            return 0;
        } else {
            GameTestRunner.clearMarkers(serverlevel);
            runTest(serverlevel, blockpos1, null, p_304385_);
            return 1;
        }
    }

    private static int resetNearbyTest(CommandSourceStack p_313951_) {
        BlockPos blockpos = BlockPos.containing(p_313951_.getPosition());
        ServerLevel serverlevel = p_313951_.getLevel();
        BlockPos blockpos1 = StructureUtils.findNearestStructureBlock(blockpos, 15, serverlevel);
        if (blockpos1 == null) {
            say(serverlevel, "Couldn't find any structure block within 15 radius", ChatFormatting.RED);
            return 0;
        } else {
            StructureBlockEntity structureblockentity = (StructureBlockEntity)serverlevel.getBlockEntity(blockpos1);
            structureblockentity.placeStructure(serverlevel);
            String s = structureblockentity.getMetaData();
            TestFunction testfunction = GameTestRegistry.getTestFunction(s);
            say(serverlevel, "Reset succeded for: " + testfunction, ChatFormatting.GREEN);
            return 1;
        }
    }

    private static int runAllNearbyTests(CommandSourceStack p_128002_, boolean p_304443_) {
        BlockPos blockpos = BlockPos.containing(p_128002_.getPosition());
        ServerLevel serverlevel = p_128002_.getLevel();
        Collection<BlockPos> collection = StructureUtils.findStructureBlocks(blockpos, 200, serverlevel);
        if (collection.isEmpty()) {
            say(serverlevel, "Couldn't find any structure blocks within 200 block radius", ChatFormatting.RED);
            return 1;
        } else {
            GameTestRunner.clearMarkers(serverlevel);
            say(p_128002_, "Running " + collection.size() + " tests...");
            MultipleTestTracker multipletesttracker = new MultipleTestTracker();
            collection.forEach(p_304152_ -> runTest(serverlevel, p_304152_, multipletesttracker, p_304443_));
            return 1;
        }
    }

    private static void runTest(ServerLevel p_127930_, BlockPos p_127931_, @Nullable MultipleTestTracker p_127932_, boolean p_304927_) {
        StructureBlockEntity structureblockentity = (StructureBlockEntity)p_127930_.getBlockEntity(p_127931_);
        String s = structureblockentity.getMetaData().isBlank() ? structureblockentity.getStructureName() : structureblockentity.getMetaData(); // Neo: use the metadata for the structure name
        Optional<TestFunction> optional = GameTestRegistry.findTestFunction(s);
        if (optional.isEmpty()) {
            say(p_127930_, "Test function for test " + s + " could not be found", ChatFormatting.RED);
        } else {
            TestFunction testfunction = optional.get();
            GameTestInfo gametestinfo = new GameTestInfo(testfunction, structureblockentity.getRotation(), p_127930_);
            gametestinfo.setRerunUntilFailed(p_304927_);
            if (p_127932_ != null) {
                p_127932_.addTestToTrack(gametestinfo);
                gametestinfo.addListener(new TestCommand.TestSummaryDisplayer(p_127930_, p_127932_));
            }

            if (verifyStructureExists(p_127930_, gametestinfo)) {
                runTestPreparation(testfunction, p_127930_);
                BoundingBox boundingbox = StructureUtils.getStructureBoundingBox(structureblockentity);
                BlockPos blockpos = new BlockPos(boundingbox.minX(), boundingbox.minY(), boundingbox.minZ());
                GameTestRunner.runTest(gametestinfo, blockpos, GameTestTicker.SINGLETON);
            }
        }
    }

    private static boolean verifyStructureExists(ServerLevel p_309700_, GameTestInfo p_309551_) {
        if (p_309700_.getStructureManager().get(new ResourceLocation(p_309551_.getStructureName())).isEmpty()) {
            say(p_309700_, "Test structure " + p_309551_.getStructureName() + " could not be found", ChatFormatting.RED);
            return false;
        } else {
            return true;
        }
    }

    static void showTestSummaryIfAllDone(ServerLevel p_127997_, MultipleTestTracker p_127998_) {
        if (p_127998_.isDone()) {
            say(p_127997_, "GameTest done! " + p_127998_.getTotalCount() + " tests were run", ChatFormatting.WHITE);
            if (p_127998_.hasFailedRequired()) {
                say(p_127997_, p_127998_.getFailedRequiredCount() + " required tests failed :(", ChatFormatting.RED);
            } else {
                say(p_127997_, "All required tests passed :)", ChatFormatting.GREEN);
            }

            if (p_127998_.hasFailedOptional()) {
                say(p_127997_, p_127998_.getFailedOptionalCount() + " optional tests failed", ChatFormatting.GRAY);
            }
        }
    }

    private static int clearAllTests(CommandSourceStack p_127953_, int p_127954_) {
        ServerLevel serverlevel = p_127953_.getLevel();
        GameTestRunner.clearMarkers(serverlevel);
        BlockPos blockpos = BlockPos.containing(
            p_127953_.getPosition().x,
            (double)p_127953_.getLevel().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, BlockPos.containing(p_127953_.getPosition())).getY(),
            p_127953_.getPosition().z
        );
        GameTestRunner.clearAllTests(serverlevel, blockpos, GameTestTicker.SINGLETON, Mth.clamp(p_127954_, 0, 1024));
        return 1;
    }

    private static int runTest(CommandSourceStack p_127979_, TestFunction p_127980_, int p_127981_) {
        ServerLevel serverlevel = p_127979_.getLevel();
        BlockPos blockpos = createTestPositionAround(p_127979_);
        GameTestRunner.clearMarkers(serverlevel);
        runTestPreparation(p_127980_, serverlevel);
        Rotation rotation = StructureUtils.getRotationForRotationSteps(p_127981_);
        GameTestInfo gametestinfo = new GameTestInfo(p_127980_, rotation, serverlevel);
        if (!verifyStructureExists(serverlevel, gametestinfo)) {
            return 0;
        } else {
            GameTestRunner.runTest(gametestinfo, blockpos, GameTestTicker.SINGLETON);
            return 1;
        }
    }

    private static BlockPos createTestPositionAround(CommandSourceStack p_308921_) {
        BlockPos blockpos = BlockPos.containing(p_308921_.getPosition());
        int i = p_308921_.getLevel().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, blockpos).getY();
        return new BlockPos(blockpos.getX(), i + 1, blockpos.getZ() + 3);
    }

    private static void runTestPreparation(TestFunction p_127994_, ServerLevel p_127995_) {
        Consumer<ServerLevel> consumer = GameTestRegistry.getBeforeBatchFunction(p_127994_.getBatchName());
        if (consumer != null) {
            consumer.accept(p_127995_);
        }
    }

    private static int runAllTests(CommandSourceStack p_127956_, int p_127957_, int p_127958_) {
        GameTestRunner.clearMarkers(p_127956_.getLevel());
        Collection<TestFunction> collection = GameTestRegistry.getAllTestFunctions();
        say(p_127956_, "Running all " + collection.size() + " tests...");
        GameTestRegistry.forgetFailedTests();
        runTests(p_127956_, collection, p_127957_, p_127958_);
        return 1;
    }

    private static int runAllTestsInClass(CommandSourceStack p_127963_, String p_127964_, int p_127965_, int p_127966_) {
        Collection<TestFunction> collection = GameTestRegistry.getTestFunctionsForClassName(p_127964_);
        GameTestRunner.clearMarkers(p_127963_.getLevel());
        say(p_127963_, "Running " + collection.size() + " tests from " + p_127964_ + "...");
        GameTestRegistry.forgetFailedTests();
        runTests(p_127963_, collection, p_127965_, p_127966_);
        return 1;
    }

    private static int runLastFailedTests(CommandSourceStack p_127983_, boolean p_127984_, int p_127985_, int p_127986_) {
        Collection<TestFunction> collection;
        if (p_127984_) {
            collection = GameTestRegistry.getLastFailedTests().stream().filter(TestFunction::isRequired).collect(Collectors.toList());
        } else {
            collection = GameTestRegistry.getLastFailedTests();
        }

        if (collection.isEmpty()) {
            say(p_127983_, "No failed tests to rerun");
            return 0;
        } else {
            GameTestRunner.clearMarkers(p_127983_.getLevel());
            say(p_127983_, "Rerunning " + collection.size() + " failed tests (" + (p_127984_ ? "only required tests" : "including optional tests") + ")");
            runTests(p_127983_, collection, p_127985_, p_127986_);
            return 1;
        }
    }

    private static void runTests(CommandSourceStack p_127974_, Collection<TestFunction> p_127975_, int p_127976_, int p_127977_) {
        BlockPos blockpos = createTestPositionAround(p_127974_);
        ServerLevel serverlevel = p_127974_.getLevel();
        Rotation rotation = StructureUtils.getRotationForRotationSteps(p_127976_);
        Collection<GameTestInfo> collection = GameTestRunner.runTests(p_127975_, blockpos, rotation, serverlevel, GameTestTicker.SINGLETON, p_127977_);
        MultipleTestTracker multipletesttracker = new MultipleTestTracker(collection);
        multipletesttracker.addListener(new TestCommand.TestSummaryDisplayer(serverlevel, multipletesttracker));
        multipletesttracker.addFailureListener(p_127992_ -> GameTestRegistry.rememberFailedTest(p_127992_.getTestFunction()));
    }

    private static void say(CommandSourceStack p_128004_, String p_128005_) {
        p_128004_.sendSuccess(() -> Component.literal(p_128005_), false);
    }

    private static int exportNearestTestStructure(CommandSourceStack p_128009_) {
        BlockPos blockpos = BlockPos.containing(p_128009_.getPosition());
        ServerLevel serverlevel = p_128009_.getLevel();
        BlockPos blockpos1 = StructureUtils.findNearestStructureBlock(blockpos, 15, serverlevel);
        if (blockpos1 == null) {
            say(serverlevel, "Couldn't find any structure block within 15 radius", ChatFormatting.RED);
            return 0;
        } else {
            StructureBlockEntity structureblockentity = (StructureBlockEntity)serverlevel.getBlockEntity(blockpos1);
            return saveAndExportTestStructure(p_128009_, structureblockentity);
        }
    }

    private static int exportAllNearbyTests(CommandSourceStack p_296438_) {
        BlockPos blockpos = BlockPos.containing(p_296438_.getPosition());
        ServerLevel serverlevel = p_296438_.getLevel();
        Collection<BlockPos> collection = StructureUtils.findStructureBlocks(blockpos, 200, serverlevel);
        if (collection.isEmpty()) {
            say(serverlevel, "Couldn't find any structure blocks within 200 block radius", ChatFormatting.RED);
            return 1;
        } else {
            boolean flag = true;

            for(BlockPos blockpos1 : collection) {
                StructureBlockEntity structureblockentity = (StructureBlockEntity)serverlevel.getBlockEntity(blockpos1);
                if (saveAndExportTestStructure(p_296438_, structureblockentity) != 0) {
                    flag = false;
                }
            }

            return flag ? 0 : 1;
        }
    }

    private static int saveAndExportTestStructure(CommandSourceStack p_309558_, StructureBlockEntity p_309587_) {
        String s = p_309587_.getStructureName();
        if (!p_309587_.saveStructure(true)) {
            say(p_309558_, "Failed to save structure " + s);
        }

        return exportTestStructure(p_309558_, s);
    }

    private static int exportTestStructure(CommandSourceStack p_128011_, String p_128012_) {
        Path path = Paths.get(StructureUtils.testStructuresDir);
        ResourceLocation resourcelocation = new ResourceLocation(p_128012_);
        Path path1 = p_128011_.getLevel().getStructureManager().getPathToGeneratedStructure(resourcelocation, ".nbt");
        Path path2 = NbtToSnbt.convertStructure(CachedOutput.NO_CACHE, path1, resourcelocation.getPath(), path);
        if (path2 == null) {
            say(p_128011_, "Failed to export " + path1);
            return 1;
        } else {
            try {
                FileUtil.createDirectoriesSafe(path2.getParent());
            } catch (IOException ioexception) {
                say(p_128011_, "Could not create folder " + path2.getParent());
                LOGGER.error("Could not create export folder", (Throwable)ioexception);
                return 1;
            }

            say(p_128011_, "Exported " + p_128012_ + " to " + path2.toAbsolutePath());
            return 0;
        }
    }

    private static int importTestStructure(CommandSourceStack p_128016_, String p_128017_) {
        ResourceLocation resourcelocation = new ResourceLocation(p_128017_);
        Path path = Paths.get(StructureUtils.testStructuresDir, resourcelocation.getPath() + ".snbt");
        Path path1 = p_128016_.getLevel().getStructureManager().getPathToGeneratedStructure(resourcelocation, ".nbt");

        try {
            BufferedReader bufferedreader = Files.newBufferedReader(path);
            String s = IOUtils.toString(bufferedreader);
            Files.createDirectories(path1.getParent());

            try (OutputStream outputstream = Files.newOutputStream(path1)) {
                NbtIo.writeCompressed(NbtUtils.snbtToStructure(s), outputstream);
            }

            say(p_128016_, "Imported to " + path1.toAbsolutePath());
            return 0;
        } catch (CommandSyntaxException | IOException ioexception) {
            LOGGER.error("Failed to load structure {}", p_128017_, ioexception);
            return 1;
        }
    }

    private static void say(ServerLevel p_127934_, String p_127935_, ChatFormatting p_127936_) {
        p_127934_.getPlayers(p_127945_ -> true).forEach(p_313469_ -> p_313469_.sendSystemMessage(Component.literal(p_127935_).withStyle(p_127936_)));
    }

    static class TestSummaryDisplayer implements GameTestListener {
        private final ServerLevel level;
        private final MultipleTestTracker tracker;

        public TestSummaryDisplayer(ServerLevel p_128061_, MultipleTestTracker p_128062_) {
            this.level = p_128061_;
            this.tracker = p_128062_;
        }

        @Override
        public void testStructureLoaded(GameTestInfo p_128064_) {
        }

        @Override
        public void testPassed(GameTestInfo p_177797_) {
            TestCommand.showTestSummaryIfAllDone(this.level, this.tracker);
        }

        @Override
        public void testFailed(GameTestInfo p_128066_) {
            TestCommand.showTestSummaryIfAllDone(this.level, this.tracker);
        }
    }
}
