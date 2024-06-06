package net.minecraft.world.level.block.entity;

import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.ticks.ContainerSingleItem;

public class DecoratedPotBlockEntity extends BlockEntity implements RandomizableContainer, ContainerSingleItem {
    public static final String TAG_SHERDS = "sherds";
    public static final String TAG_ITEM = "item";
    public static final int EVENT_POT_WOBBLES = 1;
    public long wobbleStartedAtTick;
    @Nullable
    public DecoratedPotBlockEntity.WobbleStyle lastWobbleStyle;
    private DecoratedPotBlockEntity.Decorations decorations;
    private ItemStack item = ItemStack.EMPTY;
    @Nullable
    protected ResourceLocation lootTable;
    protected long lootTableSeed;

    public DecoratedPotBlockEntity(BlockPos p_273660_, BlockState p_272831_) {
        super(BlockEntityType.DECORATED_POT, p_273660_, p_272831_);
        this.decorations = DecoratedPotBlockEntity.Decorations.EMPTY;
    }

    @Override
    protected void saveAdditional(CompoundTag p_272957_) {
        super.saveAdditional(p_272957_);
        this.decorations.save(p_272957_);
        if (!this.trySaveLootTable(p_272957_) && !this.item.isEmpty()) {
            p_272957_.put("item", this.item.save(new CompoundTag()));
        }
    }

    @Override
    public void load(CompoundTag p_272924_) {
        super.load(p_272924_);
        this.decorations = DecoratedPotBlockEntity.Decorations.load(p_272924_);
        if (!this.tryLoadLootTable(p_272924_)) {
            if (p_272924_.contains("item", 10)) {
                this.item = ItemStack.of(p_272924_.getCompound("item"));
            } else {
                this.item = ItemStack.EMPTY;
            }
        }
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public Direction getDirection() {
        return this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
    }

    public DecoratedPotBlockEntity.Decorations getDecorations() {
        return this.decorations;
    }

    public void setFromItem(ItemStack p_273109_) {
        this.decorations = DecoratedPotBlockEntity.Decorations.load(BlockItem.getBlockEntityData(p_273109_));
    }

    public ItemStack getPotAsItem() {
        return createDecoratedPotItem(this.decorations);
    }

    public static ItemStack createDecoratedPotItem(DecoratedPotBlockEntity.Decorations p_294162_) {
        ItemStack itemstack = Items.DECORATED_POT.getDefaultInstance();
        CompoundTag compoundtag = p_294162_.save(new CompoundTag());
        BlockItem.setBlockEntityData(itemstack, BlockEntityType.DECORATED_POT, compoundtag);
        return itemstack;
    }

    @Nullable
    @Override
    public ResourceLocation getLootTable() {
        return this.lootTable;
    }

    @Override
    public void setLootTable(@Nullable ResourceLocation p_309557_) {
        this.lootTable = p_309557_;
    }

    @Override
    public long getLootTableSeed() {
        return this.lootTableSeed;
    }

    @Override
    public void setLootTableSeed(long p_309580_) {
        this.lootTableSeed = p_309580_;
    }

    @Override
    public ItemStack getTheItem() {
        this.unpackLootTable(null);
        return this.item;
    }

    @Override
    public ItemStack splitTheItem(int p_305991_) {
        this.unpackLootTable(null);
        ItemStack itemstack = this.item.split(p_305991_);
        if (this.item.isEmpty()) {
            this.item = ItemStack.EMPTY;
        }

        return itemstack;
    }

    @Override
    public void setTheItem(ItemStack p_305817_) {
        this.unpackLootTable(null);
        this.item = p_305817_;
    }

    @Override
    public BlockEntity getContainerBlockEntity() {
        return this;
    }

    public void wobble(DecoratedPotBlockEntity.WobbleStyle p_305984_) {
        if (this.level != null && !this.level.isClientSide()) {
            this.level.blockEvent(this.getBlockPos(), this.getBlockState().getBlock(), 1, p_305984_.ordinal());
        }
    }

    @Override
    public boolean triggerEvent(int p_306146_, int p_305858_) {
        if (this.level != null && p_306146_ == 1 && p_305858_ >= 0 && p_305858_ < DecoratedPotBlockEntity.WobbleStyle.values().length) {
            this.wobbleStartedAtTick = this.level.getGameTime();
            this.lastWobbleStyle = DecoratedPotBlockEntity.WobbleStyle.values()[p_305858_];
            return true;
        } else {
            return super.triggerEvent(p_306146_, p_305858_);
        }
    }

    public static record Decorations(Item back, Item left, Item right, Item front) {
        public static final DecoratedPotBlockEntity.Decorations EMPTY = new DecoratedPotBlockEntity.Decorations(
            Items.BRICK, Items.BRICK, Items.BRICK, Items.BRICK
        );

        public CompoundTag save(CompoundTag p_285011_) {
            if (this.equals(EMPTY)) {
                return p_285011_;
            } else {
                ListTag listtag = new ListTag();
                this.sorted().forEach(p_285298_ -> listtag.add(StringTag.valueOf(BuiltInRegistries.ITEM.getKey(p_285298_).toString())));
                p_285011_.put("sherds", listtag);
                return p_285011_;
            }
        }

        public Stream<Item> sorted() {
            return Stream.of(this.back, this.left, this.right, this.front);
        }

        public static DecoratedPotBlockEntity.Decorations load(@Nullable CompoundTag p_284959_) {
            if (p_284959_ != null && p_284959_.contains("sherds", 9)) {
                ListTag listtag = p_284959_.getList("sherds", 8);
                return new DecoratedPotBlockEntity.Decorations(
                    itemFromTag(listtag, 0), itemFromTag(listtag, 1), itemFromTag(listtag, 2), itemFromTag(listtag, 3)
                );
            } else {
                return EMPTY;
            }
        }

        private static Item itemFromTag(ListTag p_285179_, int p_285060_) {
            if (p_285060_ >= p_285179_.size()) {
                return Items.BRICK;
            } else {
                Tag tag = p_285179_.get(p_285060_);
                return BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(tag.getAsString()));
            }
        }
    }

    public static enum WobbleStyle {
        POSITIVE(7),
        NEGATIVE(10);

        public final int duration;

        private WobbleStyle(int p_305780_) {
            this.duration = p_305780_;
        }
    }
}
