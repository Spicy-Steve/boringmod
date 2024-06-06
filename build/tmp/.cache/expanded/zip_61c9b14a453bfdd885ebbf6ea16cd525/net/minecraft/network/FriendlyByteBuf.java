package net.minecraft.network;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import io.netty.util.ByteProcessor;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMap;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class FriendlyByteBuf extends ByteBuf implements net.neoforged.neoforge.common.extensions.IFriendlyByteBufExtension {
    public static final int DEFAULT_NBT_QUOTA = 2097152;
    private final ByteBuf source;
    public static final short MAX_STRING_LENGTH = 32767;
    public static final int MAX_COMPONENT_STRING_LENGTH = 262144;
    private static final int PUBLIC_KEY_SIZE = 256;
    private static final int MAX_PUBLIC_KEY_HEADER_SIZE = 256;
    private static final int MAX_PUBLIC_KEY_LENGTH = 512;
    private static final Gson GSON = new Gson();

    public FriendlyByteBuf(ByteBuf p_130051_) {
        this.source = p_130051_;
    }

    @Deprecated
    public <T> T readWithCodecTrusted(DynamicOps<Tag> p_295347_, Codec<T> p_296304_) {
        return this.readWithCodec(p_295347_, p_296304_, NbtAccounter.unlimitedHeap());
    }

    @Deprecated
    public <T> T readWithCodec(DynamicOps<Tag> p_266903_, Codec<T> p_267107_, NbtAccounter p_295027_) {
        Tag tag = this.readNbt(p_295027_);
        return Util.getOrThrow(p_267107_.parse(p_266903_, tag), p_261423_ -> new DecoderException("Failed to decode: " + p_261423_ + " " + tag));
    }

    @Deprecated
    public <T> FriendlyByteBuf writeWithCodec(DynamicOps<Tag> p_266702_, Codec<T> p_267245_, T p_266783_) {
        Tag tag = Util.getOrThrow(
            p_267245_.encodeStart(p_266702_, p_266783_), p_272384_ -> new EncoderException("Failed to encode: " + p_272384_ + " " + p_266783_)
        );
        this.writeNbt(tag);
        return this;
    }

    public <T> T readJsonWithCodec(Codec<T> p_273318_) {
        JsonElement jsonelement = GsonHelper.fromJson(GSON, this.readUtf(), JsonElement.class);
        DataResult<T> dataresult = p_273318_.parse(JsonOps.INSTANCE, jsonelement);
        return Util.getOrThrow(dataresult, p_272382_ -> new DecoderException("Failed to decode json: " + p_272382_));
    }

    public <T> void writeJsonWithCodec(Codec<T> p_273285_, T p_272770_) {
        DataResult<JsonElement> dataresult = p_273285_.encodeStart(JsonOps.INSTANCE, p_272770_);
        this.writeUtf(GSON.toJson(Util.getOrThrow(dataresult, p_261421_ -> new EncoderException("Failed to encode: " + p_261421_ + " " + p_272770_))));
    }

    public <T> void writeId(IdMap<T> p_236819_, T p_236820_) {
        int i = p_236819_.getId(p_236820_);
        if (i == -1) {
            throw new IllegalArgumentException("Can't find id for '" + p_236820_ + "' in map " + p_236819_);
        } else {
            this.writeVarInt(i);
        }
    }

    public <T> void writeId(IdMap<Holder<T>> p_263337_, Holder<T> p_263384_, FriendlyByteBuf.Writer<T> p_263358_) {
        switch(p_263384_.kind()) {
            case REFERENCE:
                int i = p_263337_.getId(p_263384_);
                if (i == -1) {
                    throw new IllegalArgumentException("Can't find id for '" + p_263384_.value() + "' in map " + p_263337_);
                }

                this.writeVarInt(i + 1);
                break;
            case DIRECT:
                this.writeVarInt(0);
                p_263358_.accept(this, p_263384_.value());
        }
    }

    @Nullable
    public <T> T readById(IdMap<T> p_236817_) {
        int i = this.readVarInt();
        return p_236817_.byId(i);
    }

    public <T> Holder<T> readById(IdMap<Holder<T>> p_263401_, FriendlyByteBuf.Reader<T> p_263374_) {
        int i = this.readVarInt();
        if (i == 0) {
            return Holder.direct(p_263374_.apply(this));
        } else {
            Holder<T> holder = p_263401_.byId(i - 1);
            if (holder == null) {
                throw new IllegalArgumentException("Can't find element with id " + i);
            } else {
                return holder;
            }
        }
    }

    public static <T> IntFunction<T> limitValue(IntFunction<T> p_182696_, int p_182697_) {
        return p_182686_ -> {
            if (p_182686_ > p_182697_) {
                throw new DecoderException("Value " + p_182686_ + " is larger than limit " + p_182697_);
            } else {
                return p_182696_.apply(p_182686_);
            }
        };
    }

    public <T, C extends Collection<T>> C readCollection(IntFunction<C> p_236839_, FriendlyByteBuf.Reader<T> p_236840_) {
        int i = this.readVarInt();
        C c = p_236839_.apply(i);

        for(int j = 0; j < i; ++j) {
            c.add(p_236840_.apply(this));
        }

        return c;
    }

    public <T> void writeCollection(Collection<T> p_236829_, FriendlyByteBuf.Writer<T> p_236830_) {
        this.writeVarInt(p_236829_.size());

        for(T t : p_236829_) {
            p_236830_.accept(this, t);
        }
    }

    public <T> List<T> readList(FriendlyByteBuf.Reader<T> p_236846_) {
        return this.readCollection(Lists::newArrayListWithCapacity, p_236846_);
    }

    public IntList readIntIdList() {
        int i = this.readVarInt();
        IntList intlist = new IntArrayList();

        for(int j = 0; j < i; ++j) {
            intlist.add(this.readVarInt());
        }

        return intlist;
    }

    public void writeIntIdList(IntList p_178346_) {
        this.writeVarInt(p_178346_.size());
        p_178346_.forEach(this::writeVarInt);
    }

    public <K, V, M extends Map<K, V>> M readMap(IntFunction<M> p_236842_, FriendlyByteBuf.Reader<K> p_236843_, FriendlyByteBuf.Reader<V> p_236844_) {
        int i = this.readVarInt();
        M m = p_236842_.apply(i);

        for(int j = 0; j < i; ++j) {
            K k = p_236843_.apply(this);
            V v = p_236844_.apply(this);
            m.put(k, v);
        }

        return m;
    }

    public <K, V> Map<K, V> readMap(FriendlyByteBuf.Reader<K> p_236848_, FriendlyByteBuf.Reader<V> p_236849_) {
        return this.readMap(Maps::newHashMapWithExpectedSize, p_236848_, p_236849_);
    }

    public <K, V> void writeMap(Map<K, V> p_236832_, FriendlyByteBuf.Writer<K> p_236833_, FriendlyByteBuf.Writer<V> p_236834_) {
        this.writeVarInt(p_236832_.size());
        p_236832_.forEach((p_236856_, p_236857_) -> {
            p_236833_.accept(this, p_236856_);
            p_236834_.accept(this, p_236857_);
        });
    }

    public void readWithCount(Consumer<FriendlyByteBuf> p_178365_) {
        int i = this.readVarInt();

        for(int j = 0; j < i; ++j) {
            p_178365_.accept(this);
        }
    }

    public <E extends Enum<E>> void writeEnumSet(EnumSet<E> p_250400_, Class<E> p_250673_) {
        E[] ae = p_250673_.getEnumConstants();
        BitSet bitset = new BitSet(ae.length);

        for(int i = 0; i < ae.length; ++i) {
            bitset.set(i, p_250400_.contains(ae[i]));
        }

        this.writeFixedBitSet(bitset, ae.length);
    }

    public <E extends Enum<E>> EnumSet<E> readEnumSet(Class<E> p_251289_) {
        E[] ae = p_251289_.getEnumConstants();
        BitSet bitset = this.readFixedBitSet(ae.length);
        EnumSet<E> enumset = EnumSet.noneOf(p_251289_);

        for(int i = 0; i < ae.length; ++i) {
            if (bitset.get(i)) {
                enumset.add(ae[i]);
            }
        }

        return enumset;
    }

    public <T> void writeOptional(Optional<T> p_236836_, FriendlyByteBuf.Writer<T> p_236837_) {
        if (p_236836_.isPresent()) {
            this.writeBoolean(true);
            p_236837_.accept(this, p_236836_.get());
        } else {
            this.writeBoolean(false);
        }
    }

    public <T> Optional<T> readOptional(FriendlyByteBuf.Reader<T> p_236861_) {
        return this.readBoolean() ? Optional.of(p_236861_.apply(this)) : Optional.empty();
    }

    @Nullable
    public <T> T readNullable(FriendlyByteBuf.Reader<T> p_236869_) {
        return this.readBoolean() ? p_236869_.apply(this) : null;
    }

    public <T> void writeNullable(@Nullable T p_236822_, FriendlyByteBuf.Writer<T> p_236823_) {
        if (p_236822_ != null) {
            this.writeBoolean(true);
            p_236823_.accept(this, p_236822_);
        } else {
            this.writeBoolean(false);
        }
    }

    public <L, R> void writeEither(Either<L, R> p_236811_, FriendlyByteBuf.Writer<L> p_236812_, FriendlyByteBuf.Writer<R> p_236813_) {
        p_236811_.ifLeft(p_293716_ -> {
            this.writeBoolean(true);
            p_236812_.accept(this, p_293716_);
        }).ifRight(p_293718_ -> {
            this.writeBoolean(false);
            p_236813_.accept(this, p_293718_);
        });
    }

    public <L, R> Either<L, R> readEither(FriendlyByteBuf.Reader<L> p_236863_, FriendlyByteBuf.Reader<R> p_236864_) {
        return this.readBoolean() ? Either.left(p_236863_.apply(this)) : Either.right(p_236864_.apply(this));
    }

    public byte[] readByteArray() {
        return this.readByteArray(this.readableBytes());
    }

    public FriendlyByteBuf writeByteArray(byte[] p_130088_) {
        this.writeVarInt(p_130088_.length);
        this.writeBytes(p_130088_);
        return this;
    }

    public byte[] readByteArray(int p_130102_) {
        int i = this.readVarInt();
        if (i > p_130102_) {
            throw new DecoderException("ByteArray with size " + i + " is bigger than allowed " + p_130102_);
        } else {
            byte[] abyte = new byte[i];
            this.readBytes(abyte);
            return abyte;
        }
    }

    public FriendlyByteBuf writeVarIntArray(int[] p_130090_) {
        this.writeVarInt(p_130090_.length);

        for(int i : p_130090_) {
            this.writeVarInt(i);
        }

        return this;
    }

    public int[] readVarIntArray() {
        return this.readVarIntArray(this.readableBytes());
    }

    public int[] readVarIntArray(int p_130117_) {
        int i = this.readVarInt();
        if (i > p_130117_) {
            throw new DecoderException("VarIntArray with size " + i + " is bigger than allowed " + p_130117_);
        } else {
            int[] aint = new int[i];

            for(int j = 0; j < aint.length; ++j) {
                aint[j] = this.readVarInt();
            }

            return aint;
        }
    }

    public FriendlyByteBuf writeLongArray(long[] p_130092_) {
        this.writeVarInt(p_130092_.length);

        for(long i : p_130092_) {
            this.writeLong(i);
        }

        return this;
    }

    public long[] readLongArray() {
        return this.readLongArray(null);
    }

    public long[] readLongArray(@Nullable long[] p_130106_) {
        return this.readLongArray(p_130106_, this.readableBytes() / 8);
    }

    public long[] readLongArray(@Nullable long[] p_130094_, int p_130095_) {
        int i = this.readVarInt();
        if (p_130094_ == null || p_130094_.length != i) {
            if (i > p_130095_) {
                throw new DecoderException("LongArray with size " + i + " is bigger than allowed " + p_130095_);
            }

            p_130094_ = new long[i];
        }

        for(int j = 0; j < p_130094_.length; ++j) {
            p_130094_[j] = this.readLong();
        }

        return p_130094_;
    }

    public BlockPos readBlockPos() {
        return BlockPos.of(this.readLong());
    }

    public FriendlyByteBuf writeBlockPos(BlockPos p_130065_) {
        this.writeLong(p_130065_.asLong());
        return this;
    }

    public ChunkPos readChunkPos() {
        return new ChunkPos(this.readLong());
    }

    public FriendlyByteBuf writeChunkPos(ChunkPos p_178342_) {
        this.writeLong(p_178342_.toLong());
        return this;
    }

    public SectionPos readSectionPos() {
        return SectionPos.of(this.readLong());
    }

    public FriendlyByteBuf writeSectionPos(SectionPos p_178344_) {
        this.writeLong(p_178344_.asLong());
        return this;
    }

    public GlobalPos readGlobalPos() {
        ResourceKey<Level> resourcekey = this.readResourceKey(Registries.DIMENSION);
        BlockPos blockpos = this.readBlockPos();
        return GlobalPos.of(resourcekey, blockpos);
    }

    public void writeGlobalPos(GlobalPos p_236815_) {
        this.writeResourceKey(p_236815_.dimension());
        this.writeBlockPos(p_236815_.pos());
    }

    public Vector3f readVector3f() {
        return new Vector3f(this.readFloat(), this.readFloat(), this.readFloat());
    }

    public void writeVector3f(Vector3f p_270985_) {
        this.writeFloat(p_270985_.x());
        this.writeFloat(p_270985_.y());
        this.writeFloat(p_270985_.z());
    }

    public Quaternionf readQuaternion() {
        return new Quaternionf(this.readFloat(), this.readFloat(), this.readFloat(), this.readFloat());
    }

    public void writeQuaternion(Quaternionf p_270141_) {
        this.writeFloat(p_270141_.x);
        this.writeFloat(p_270141_.y);
        this.writeFloat(p_270141_.z);
        this.writeFloat(p_270141_.w);
    }

    public Vec3 readVec3() {
        return new Vec3(this.readDouble(), this.readDouble(), this.readDouble());
    }

    public void writeVec3(Vec3 p_296062_) {
        this.writeDouble(p_296062_.x());
        this.writeDouble(p_296062_.y());
        this.writeDouble(p_296062_.z());
    }

    public Component readComponent() {
        return this.readWithCodec(NbtOps.INSTANCE, ComponentSerialization.CODEC, NbtAccounter.create(2097152L));
    }

    public Component readComponentTrusted() {
        return this.readWithCodecTrusted(NbtOps.INSTANCE, ComponentSerialization.CODEC);
    }

    public FriendlyByteBuf writeComponent(Component p_130084_) {
        return this.writeWithCodec(NbtOps.INSTANCE, ComponentSerialization.CODEC, p_130084_);
    }

    public <T extends Enum<T>> T readEnum(Class<T> p_130067_) {
        return p_130067_.getEnumConstants()[this.readVarInt()];
    }

    public FriendlyByteBuf writeEnum(Enum<?> p_130069_) {
        return this.writeVarInt(p_130069_.ordinal());
    }

    public <T> T readById(IntFunction<T> p_295187_) {
        int i = this.readVarInt();
        return p_295187_.apply(i);
    }

    public <T> FriendlyByteBuf writeById(ToIntFunction<T> p_295233_, T p_294192_) {
        int i = p_295233_.applyAsInt(p_294192_);
        return this.writeVarInt(i);
    }

    public int readVarInt() {
        return VarInt.read(this.source);
    }

    public long readVarLong() {
        return VarLong.read(this.source);
    }

    public FriendlyByteBuf writeUUID(UUID p_130078_) {
        this.writeLong(p_130078_.getMostSignificantBits());
        this.writeLong(p_130078_.getLeastSignificantBits());
        return this;
    }

    public UUID readUUID() {
        return new UUID(this.readLong(), this.readLong());
    }

    public FriendlyByteBuf writeVarInt(int p_130131_) {
        VarInt.write(this.source, p_130131_);
        return this;
    }

    public FriendlyByteBuf writeVarLong(long p_130104_) {
        VarLong.write(this.source, p_130104_);
        return this;
    }

    public FriendlyByteBuf writeNbt(@Nullable Tag p_296432_) {
        if (p_296432_ == null) {
            p_296432_ = EndTag.INSTANCE;
        }

        try {
            NbtIo.writeAnyTag(p_296432_, new ByteBufOutputStream(this));
            return this;
        } catch (IOException ioexception) {
            throw new EncoderException(ioexception);
        }
    }

    @Nullable
    public CompoundTag readNbt() {
        Tag tag = this.readNbt(NbtAccounter.create(2097152L));
        if (tag != null && !(tag instanceof CompoundTag)) {
            throw new DecoderException("Not a compound tag: " + tag);
        } else {
            return (CompoundTag)tag;
        }
    }

    @Nullable
    public Tag readNbt(NbtAccounter p_130082_) {
        try {
            Tag tag = NbtIo.readAnyTag(new ByteBufInputStream(this), p_130082_);
            return tag.getId() == 0 ? null : tag;
        } catch (IOException ioexception) {
            throw new EncoderException(ioexception);
        }
    }

    public FriendlyByteBuf writeItem(ItemStack p_130056_) {
        if (p_130056_.isEmpty()) {
            this.writeBoolean(false);
        } else {
            this.writeBoolean(true);
            Item item = p_130056_.getItem();
            this.writeId(BuiltInRegistries.ITEM, item);
            this.writeByte(p_130056_.getCount());
            CompoundTag compoundtag = null;
            if (item.isDamageable(p_130056_) || item.shouldOverrideMultiplayerNbt()) {
                compoundtag = p_130056_.getTag();
            }
            compoundtag = net.neoforged.neoforge.attachment.AttachmentInternals.addAttachmentsToTag(compoundtag, p_130056_, false);

            this.writeNbt(compoundtag);
        }

        return this;
    }

    public ItemStack readItem() {
        if (!this.readBoolean()) {
            return ItemStack.EMPTY;
        } else {
            Item item = this.readById(BuiltInRegistries.ITEM);
            int i = this.readByte();
            return net.neoforged.neoforge.attachment.AttachmentInternals.reconstructItemStack(item, i, this.readNbt());
        }
    }

    public String readUtf() {
        return this.readUtf(32767);
    }

    public String readUtf(int p_130137_) {
        return Utf8String.read(this.source, p_130137_);
    }

    public FriendlyByteBuf writeUtf(String p_130071_) {
        return this.writeUtf(p_130071_, 32767);
    }

    public FriendlyByteBuf writeUtf(String p_130073_, int p_130074_) {
        Utf8String.write(this.source, p_130073_, p_130074_);
        return this;
    }

    public ResourceLocation readResourceLocation() {
        return new ResourceLocation(this.readUtf(32767));
    }

    public FriendlyByteBuf writeResourceLocation(ResourceLocation p_130086_) {
        this.writeUtf(p_130086_.toString());
        return this;
    }

    public <T> ResourceKey<T> readResourceKey(ResourceKey<? extends Registry<T>> p_236802_) {
        ResourceLocation resourcelocation = this.readResourceLocation();
        return ResourceKey.create(p_236802_, resourcelocation);
    }

    public void writeResourceKey(ResourceKey<?> p_236859_) {
        this.writeResourceLocation(p_236859_.location());
    }

    public <T> ResourceKey<? extends Registry<T>> readRegistryKey() {
        ResourceLocation resourcelocation = this.readResourceLocation();
        return ResourceKey.createRegistryKey(resourcelocation);
    }

    public Date readDate() {
        return new Date(this.readLong());
    }

    public FriendlyByteBuf writeDate(Date p_130076_) {
        this.writeLong(p_130076_.getTime());
        return this;
    }

    public Instant readInstant() {
        return Instant.ofEpochMilli(this.readLong());
    }

    public void writeInstant(Instant p_236827_) {
        this.writeLong(p_236827_.toEpochMilli());
    }

    public PublicKey readPublicKey() {
        try {
            return Crypt.byteToPublicKey(this.readByteArray(512));
        } catch (CryptException cryptexception) {
            throw new DecoderException("Malformed public key bytes", cryptexception);
        }
    }

    public FriendlyByteBuf writePublicKey(PublicKey p_236825_) {
        this.writeByteArray(p_236825_.getEncoded());
        return this;
    }

    public BlockHitResult readBlockHitResult() {
        BlockPos blockpos = this.readBlockPos();
        Direction direction = this.readEnum(Direction.class);
        float f = this.readFloat();
        float f1 = this.readFloat();
        float f2 = this.readFloat();
        boolean flag = this.readBoolean();
        return new BlockHitResult(
            new Vec3((double)blockpos.getX() + (double)f, (double)blockpos.getY() + (double)f1, (double)blockpos.getZ() + (double)f2),
            direction,
            blockpos,
            flag
        );
    }

    public void writeBlockHitResult(BlockHitResult p_130063_) {
        BlockPos blockpos = p_130063_.getBlockPos();
        this.writeBlockPos(blockpos);
        this.writeEnum(p_130063_.getDirection());
        Vec3 vec3 = p_130063_.getLocation();
        this.writeFloat((float)(vec3.x - (double)blockpos.getX()));
        this.writeFloat((float)(vec3.y - (double)blockpos.getY()));
        this.writeFloat((float)(vec3.z - (double)blockpos.getZ()));
        this.writeBoolean(p_130063_.isInside());
    }

    public BitSet readBitSet() {
        return BitSet.valueOf(this.readLongArray());
    }

    public void writeBitSet(BitSet p_178351_) {
        this.writeLongArray(p_178351_.toLongArray());
    }

    public BitSet readFixedBitSet(int p_249113_) {
        byte[] abyte = new byte[Mth.positiveCeilDiv(p_249113_, 8)];
        this.readBytes(abyte);
        return BitSet.valueOf(abyte);
    }

    public void writeFixedBitSet(BitSet p_248698_, int p_248869_) {
        if (p_248698_.length() > p_248869_) {
            throw new EncoderException("BitSet is larger than expected size (" + p_248698_.length() + ">" + p_248869_ + ")");
        } else {
            byte[] abyte = p_248698_.toByteArray();
            this.writeBytes(Arrays.copyOf(abyte, Mth.positiveCeilDiv(p_248869_, 8)));
        }
    }

    public GameProfile readGameProfile() {
        UUID uuid = this.readUUID();
        String s = this.readUtf(16);
        GameProfile gameprofile = new GameProfile(uuid, s);
        gameprofile.getProperties().putAll(this.readGameProfileProperties());
        return gameprofile;
    }

    public void writeGameProfile(GameProfile p_236804_) {
        this.writeUUID(p_236804_.getId());
        this.writeUtf(p_236804_.getName());
        this.writeGameProfileProperties(p_236804_.getProperties());
    }

    public PropertyMap readGameProfileProperties() {
        PropertyMap propertymap = new PropertyMap();
        this.readWithCount(p_293720_ -> {
            Property property = this.readProperty();
            propertymap.put(property.name(), property);
        });
        return propertymap;
    }

    public void writeGameProfileProperties(PropertyMap p_248638_) {
        this.writeCollection(p_248638_.values(), FriendlyByteBuf::writeProperty);
    }

    public Property readProperty() {
        String s = this.readUtf();
        String s1 = this.readUtf();
        String s2 = this.readNullable(FriendlyByteBuf::readUtf);
        return new Property(s, s1, s2);
    }

    public void writeProperty(Property p_236806_) {
        this.writeUtf(p_236806_.name());
        this.writeUtf(p_236806_.value());
        this.writeNullable(p_236806_.signature(), FriendlyByteBuf::writeUtf);
    }

    @Override
    public boolean isContiguous() {
        return this.source.isContiguous();
    }

    @Override
    public int maxFastWritableBytes() {
        return this.source.maxFastWritableBytes();
    }

    @Override
    public int capacity() {
        return this.source.capacity();
    }

    public FriendlyByteBuf capacity(int p_295325_) {
        this.source.capacity(p_295325_);
        return this;
    }

    @Override
    public int maxCapacity() {
        return this.source.maxCapacity();
    }

    @Override
    public ByteBufAllocator alloc() {
        return this.source.alloc();
    }

    @Override
    public ByteOrder order() {
        return this.source.order();
    }

    @Override
    public ByteBuf order(ByteOrder p_130280_) {
        return this.source.order(p_130280_);
    }

    @Override
    public ByteBuf unwrap() {
        return this.source;
    }

    @Override
    public boolean isDirect() {
        return this.source.isDirect();
    }

    @Override
    public boolean isReadOnly() {
        return this.source.isReadOnly();
    }

    @Override
    public ByteBuf asReadOnly() {
        return this.source.asReadOnly();
    }

    @Override
    public int readerIndex() {
        return this.source.readerIndex();
    }

    public FriendlyByteBuf readerIndex(int p_295519_) {
        this.source.readerIndex(p_295519_);
        return this;
    }

    @Override
    public int writerIndex() {
        return this.source.writerIndex();
    }

    public FriendlyByteBuf writerIndex(int p_294907_) {
        this.source.writerIndex(p_294907_);
        return this;
    }

    public FriendlyByteBuf setIndex(int p_296056_, int p_295480_) {
        this.source.setIndex(p_296056_, p_295480_);
        return this;
    }

    @Override
    public int readableBytes() {
        return this.source.readableBytes();
    }

    @Override
    public int writableBytes() {
        return this.source.writableBytes();
    }

    @Override
    public int maxWritableBytes() {
        return this.source.maxWritableBytes();
    }

    @Override
    public boolean isReadable() {
        return this.source.isReadable();
    }

    @Override
    public boolean isReadable(int p_130254_) {
        return this.source.isReadable(p_130254_);
    }

    @Override
    public boolean isWritable() {
        return this.source.isWritable();
    }

    @Override
    public boolean isWritable(int p_130257_) {
        return this.source.isWritable(p_130257_);
    }

    public FriendlyByteBuf clear() {
        this.source.clear();
        return this;
    }

    public FriendlyByteBuf markReaderIndex() {
        this.source.markReaderIndex();
        return this;
    }

    public FriendlyByteBuf resetReaderIndex() {
        this.source.resetReaderIndex();
        return this;
    }

    public FriendlyByteBuf markWriterIndex() {
        this.source.markWriterIndex();
        return this;
    }

    public FriendlyByteBuf resetWriterIndex() {
        this.source.resetWriterIndex();
        return this;
    }

    public FriendlyByteBuf discardReadBytes() {
        this.source.discardReadBytes();
        return this;
    }

    public FriendlyByteBuf discardSomeReadBytes() {
        this.source.discardSomeReadBytes();
        return this;
    }

    public FriendlyByteBuf ensureWritable(int p_294903_) {
        this.source.ensureWritable(p_294903_);
        return this;
    }

    @Override
    public int ensureWritable(int p_130141_, boolean p_130142_) {
        return this.source.ensureWritable(p_130141_, p_130142_);
    }

    @Override
    public boolean getBoolean(int p_130159_) {
        return this.source.getBoolean(p_130159_);
    }

    @Override
    public byte getByte(int p_130161_) {
        return this.source.getByte(p_130161_);
    }

    @Override
    public short getUnsignedByte(int p_130225_) {
        return this.source.getUnsignedByte(p_130225_);
    }

    @Override
    public short getShort(int p_130221_) {
        return this.source.getShort(p_130221_);
    }

    @Override
    public short getShortLE(int p_130223_) {
        return this.source.getShortLE(p_130223_);
    }

    @Override
    public int getUnsignedShort(int p_130235_) {
        return this.source.getUnsignedShort(p_130235_);
    }

    @Override
    public int getUnsignedShortLE(int p_130237_) {
        return this.source.getUnsignedShortLE(p_130237_);
    }

    @Override
    public int getMedium(int p_130217_) {
        return this.source.getMedium(p_130217_);
    }

    @Override
    public int getMediumLE(int p_130219_) {
        return this.source.getMediumLE(p_130219_);
    }

    @Override
    public int getUnsignedMedium(int p_130231_) {
        return this.source.getUnsignedMedium(p_130231_);
    }

    @Override
    public int getUnsignedMediumLE(int p_130233_) {
        return this.source.getUnsignedMediumLE(p_130233_);
    }

    @Override
    public int getInt(int p_130209_) {
        return this.source.getInt(p_130209_);
    }

    @Override
    public int getIntLE(int p_130211_) {
        return this.source.getIntLE(p_130211_);
    }

    @Override
    public long getUnsignedInt(int p_130227_) {
        return this.source.getUnsignedInt(p_130227_);
    }

    @Override
    public long getUnsignedIntLE(int p_130229_) {
        return this.source.getUnsignedIntLE(p_130229_);
    }

    @Override
    public long getLong(int p_130213_) {
        return this.source.getLong(p_130213_);
    }

    @Override
    public long getLongLE(int p_130215_) {
        return this.source.getLongLE(p_130215_);
    }

    @Override
    public char getChar(int p_130199_) {
        return this.source.getChar(p_130199_);
    }

    @Override
    public float getFloat(int p_130207_) {
        return this.source.getFloat(p_130207_);
    }

    @Override
    public double getDouble(int p_130205_) {
        return this.source.getDouble(p_130205_);
    }

    public FriendlyByteBuf getBytes(int p_296115_, ByteBuf p_295805_) {
        this.source.getBytes(p_296115_, p_295805_);
        return this;
    }

    public FriendlyByteBuf getBytes(int p_294876_, ByteBuf p_295864_, int p_294524_) {
        this.source.getBytes(p_294876_, p_295864_, p_294524_);
        return this;
    }

    public FriendlyByteBuf getBytes(int p_294107_, ByteBuf p_296448_, int p_295017_, int p_294733_) {
        this.source.getBytes(p_294107_, p_296448_, p_295017_, p_294733_);
        return this;
    }

    public FriendlyByteBuf getBytes(int p_294557_, byte[] p_295660_) {
        this.source.getBytes(p_294557_, p_295660_);
        return this;
    }

    public FriendlyByteBuf getBytes(int p_295128_, byte[] p_296337_, int p_294659_, int p_294779_) {
        this.source.getBytes(p_295128_, p_296337_, p_294659_, p_294779_);
        return this;
    }

    public FriendlyByteBuf getBytes(int p_296377_, ByteBuffer p_295139_) {
        this.source.getBytes(p_296377_, p_295139_);
        return this;
    }

    public FriendlyByteBuf getBytes(int p_294277_, OutputStream p_296069_, int p_296395_) throws IOException {
        this.source.getBytes(p_294277_, p_296069_, p_296395_);
        return this;
    }

    @Override
    public int getBytes(int p_130187_, GatheringByteChannel p_130188_, int p_130189_) throws IOException {
        return this.source.getBytes(p_130187_, p_130188_, p_130189_);
    }

    @Override
    public int getBytes(int p_130182_, FileChannel p_130183_, long p_130184_, int p_130185_) throws IOException {
        return this.source.getBytes(p_130182_, p_130183_, p_130184_, p_130185_);
    }

    @Override
    public CharSequence getCharSequence(int p_130201_, int p_130202_, Charset p_130203_) {
        return this.source.getCharSequence(p_130201_, p_130202_, p_130203_);
    }

    public FriendlyByteBuf setBoolean(int p_295254_, boolean p_295072_) {
        this.source.setBoolean(p_295254_, p_295072_);
        return this;
    }

    public FriendlyByteBuf setByte(int p_294776_, int p_295119_) {
        this.source.setByte(p_294776_, p_295119_);
        return this;
    }

    public FriendlyByteBuf setShort(int p_295713_, int p_296093_) {
        this.source.setShort(p_295713_, p_296093_);
        return this;
    }

    public FriendlyByteBuf setShortLE(int p_295748_, int p_294784_) {
        this.source.setShortLE(p_295748_, p_294784_);
        return this;
    }

    public FriendlyByteBuf setMedium(int p_295726_, int p_296052_) {
        this.source.setMedium(p_295726_, p_296052_);
        return this;
    }

    public FriendlyByteBuf setMediumLE(int p_296476_, int p_295239_) {
        this.source.setMediumLE(p_296476_, p_295239_);
        return this;
    }

    public FriendlyByteBuf setInt(int p_295466_, int p_295351_) {
        this.source.setInt(p_295466_, p_295351_);
        return this;
    }

    public FriendlyByteBuf setIntLE(int p_296041_, int p_295343_) {
        this.source.setIntLE(p_296041_, p_295343_);
        return this;
    }

    public FriendlyByteBuf setLong(int p_295413_, long p_294370_) {
        this.source.setLong(p_295413_, p_294370_);
        return this;
    }

    public FriendlyByteBuf setLongLE(int p_294242_, long p_295126_) {
        this.source.setLongLE(p_294242_, p_295126_);
        return this;
    }

    public FriendlyByteBuf setChar(int p_295571_, int p_295295_) {
        this.source.setChar(p_295571_, p_295295_);
        return this;
    }

    public FriendlyByteBuf setFloat(int p_295320_, float p_296028_) {
        this.source.setFloat(p_295320_, p_296028_);
        return this;
    }

    public FriendlyByteBuf setDouble(int p_294330_, double p_295264_) {
        this.source.setDouble(p_294330_, p_295264_);
        return this;
    }

    public FriendlyByteBuf setBytes(int p_294134_, ByteBuf p_294238_) {
        this.source.setBytes(p_294134_, p_294238_);
        return this;
    }

    public FriendlyByteBuf setBytes(int p_295824_, ByteBuf p_294581_, int p_295654_) {
        this.source.setBytes(p_295824_, p_294581_, p_295654_);
        return this;
    }

    public FriendlyByteBuf setBytes(int p_296236_, ByteBuf p_295709_, int p_294354_, int p_295897_) {
        this.source.setBytes(p_296236_, p_295709_, p_294354_, p_295897_);
        return this;
    }

    public FriendlyByteBuf setBytes(int p_295696_, byte[] p_295990_) {
        this.source.setBytes(p_295696_, p_295990_);
        return this;
    }

    public FriendlyByteBuf setBytes(int p_295030_, byte[] p_295276_, int p_295073_, int p_295926_) {
        this.source.setBytes(p_295030_, p_295276_, p_295073_, p_295926_);
        return this;
    }

    public FriendlyByteBuf setBytes(int p_294827_, ByteBuffer p_295408_) {
        this.source.setBytes(p_294827_, p_295408_);
        return this;
    }

    @Override
    public int setBytes(int p_130380_, InputStream p_130381_, int p_130382_) throws IOException {
        return this.source.setBytes(p_130380_, p_130381_, p_130382_);
    }

    @Override
    public int setBytes(int p_130392_, ScatteringByteChannel p_130393_, int p_130394_) throws IOException {
        return this.source.setBytes(p_130392_, p_130393_, p_130394_);
    }

    @Override
    public int setBytes(int p_130387_, FileChannel p_130388_, long p_130389_, int p_130390_) throws IOException {
        return this.source.setBytes(p_130387_, p_130388_, p_130389_, p_130390_);
    }

    public FriendlyByteBuf setZero(int p_295002_, int p_295655_) {
        this.source.setZero(p_295002_, p_295655_);
        return this;
    }

    @Override
    public int setCharSequence(int p_130407_, CharSequence p_130408_, Charset p_130409_) {
        return this.source.setCharSequence(p_130407_, p_130408_, p_130409_);
    }

    @Override
    public boolean readBoolean() {
        return this.source.readBoolean();
    }

    @Override
    public byte readByte() {
        return this.source.readByte();
    }

    @Override
    public short readUnsignedByte() {
        return this.source.readUnsignedByte();
    }

    @Override
    public short readShort() {
        return this.source.readShort();
    }

    @Override
    public short readShortLE() {
        return this.source.readShortLE();
    }

    @Override
    public int readUnsignedShort() {
        return this.source.readUnsignedShort();
    }

    @Override
    public int readUnsignedShortLE() {
        return this.source.readUnsignedShortLE();
    }

    @Override
    public int readMedium() {
        return this.source.readMedium();
    }

    @Override
    public int readMediumLE() {
        return this.source.readMediumLE();
    }

    @Override
    public int readUnsignedMedium() {
        return this.source.readUnsignedMedium();
    }

    @Override
    public int readUnsignedMediumLE() {
        return this.source.readUnsignedMediumLE();
    }

    @Override
    public int readInt() {
        return this.source.readInt();
    }

    @Override
    public int readIntLE() {
        return this.source.readIntLE();
    }

    @Override
    public long readUnsignedInt() {
        return this.source.readUnsignedInt();
    }

    @Override
    public long readUnsignedIntLE() {
        return this.source.readUnsignedIntLE();
    }

    @Override
    public long readLong() {
        return this.source.readLong();
    }

    @Override
    public long readLongLE() {
        return this.source.readLongLE();
    }

    @Override
    public char readChar() {
        return this.source.readChar();
    }

    @Override
    public float readFloat() {
        return this.source.readFloat();
    }

    @Override
    public double readDouble() {
        return this.source.readDouble();
    }

    @Override
    public ByteBuf readBytes(int p_130287_) {
        return this.source.readBytes(p_130287_);
    }

    @Override
    public ByteBuf readSlice(int p_130332_) {
        return this.source.readSlice(p_130332_);
    }

    @Override
    public ByteBuf readRetainedSlice(int p_130328_) {
        return this.source.readRetainedSlice(p_130328_);
    }

    public FriendlyByteBuf readBytes(ByteBuf p_296080_) {
        this.source.readBytes(p_296080_);
        return this;
    }

    public FriendlyByteBuf readBytes(ByteBuf p_296068_, int p_295259_) {
        this.source.readBytes(p_296068_, p_295259_);
        return this;
    }

    public FriendlyByteBuf readBytes(ByteBuf p_295701_, int p_294970_, int p_294610_) {
        this.source.readBytes(p_295701_, p_294970_, p_294610_);
        return this;
    }

    public FriendlyByteBuf readBytes(byte[] p_295557_) {
        this.source.readBytes(p_295557_);
        return this;
    }

    public FriendlyByteBuf readBytes(byte[] p_294152_, int p_294331_, int p_295481_) {
        this.source.readBytes(p_294152_, p_294331_, p_295481_);
        return this;
    }

    public FriendlyByteBuf readBytes(ByteBuffer p_294641_) {
        this.source.readBytes(p_294641_);
        return this;
    }

    public FriendlyByteBuf readBytes(OutputStream p_296425_, int p_295786_) throws IOException {
        this.source.readBytes(p_296425_, p_295786_);
        return this;
    }

    @Override
    public int readBytes(GatheringByteChannel p_130307_, int p_130308_) throws IOException {
        return this.source.readBytes(p_130307_, p_130308_);
    }

    @Override
    public CharSequence readCharSequence(int p_130317_, Charset p_130318_) {
        return this.source.readCharSequence(p_130317_, p_130318_);
    }

    @Override
    public int readBytes(FileChannel p_130303_, long p_130304_, int p_130305_) throws IOException {
        return this.source.readBytes(p_130303_, p_130304_, p_130305_);
    }

    public FriendlyByteBuf skipBytes(int p_296015_) {
        this.source.skipBytes(p_296015_);
        return this;
    }

    public FriendlyByteBuf writeBoolean(boolean p_295682_) {
        this.source.writeBoolean(p_295682_);
        return this;
    }

    public FriendlyByteBuf writeByte(int p_295618_) {
        this.source.writeByte(p_295618_);
        return this;
    }

    public FriendlyByteBuf writeShort(int p_294734_) {
        this.source.writeShort(p_294734_);
        return this;
    }

    public FriendlyByteBuf writeShortLE(int p_295772_) {
        this.source.writeShortLE(p_295772_);
        return this;
    }

    public FriendlyByteBuf writeMedium(int p_296189_) {
        this.source.writeMedium(p_296189_);
        return this;
    }

    public FriendlyByteBuf writeMediumLE(int p_295508_) {
        this.source.writeMediumLE(p_295508_);
        return this;
    }

    public FriendlyByteBuf writeInt(int p_296090_) {
        this.source.writeInt(p_296090_);
        return this;
    }

    public FriendlyByteBuf writeIntLE(int p_294626_) {
        this.source.writeIntLE(p_294626_);
        return this;
    }

    public FriendlyByteBuf writeLong(long p_295423_) {
        this.source.writeLong(p_295423_);
        return this;
    }

    public FriendlyByteBuf writeLongLE(long p_294730_) {
        this.source.writeLongLE(p_294730_);
        return this;
    }

    public FriendlyByteBuf writeChar(int p_295369_) {
        this.source.writeChar(p_295369_);
        return this;
    }

    public FriendlyByteBuf writeFloat(float p_294332_) {
        this.source.writeFloat(p_294332_);
        return this;
    }

    public FriendlyByteBuf writeDouble(double p_295463_) {
        this.source.writeDouble(p_295463_);
        return this;
    }

    public FriendlyByteBuf writeBytes(ByteBuf p_295011_) {
        this.source.writeBytes(p_295011_);
        return this;
    }

    public FriendlyByteBuf writeBytes(ByteBuf p_295384_, int p_295311_) {
        this.source.writeBytes(p_295384_, p_295311_);
        return this;
    }

    public FriendlyByteBuf writeBytes(ByteBuf p_295348_, int p_294807_, int p_294737_) {
        this.source.writeBytes(p_295348_, p_294807_, p_294737_);
        return this;
    }

    public FriendlyByteBuf writeBytes(byte[] p_296194_) {
        this.source.writeBytes(p_296194_);
        return this;
    }

    public FriendlyByteBuf writeBytes(byte[] p_294409_, int p_295416_, int p_294380_) {
        this.source.writeBytes(p_294409_, p_295416_, p_294380_);
        return this;
    }

    public FriendlyByteBuf writeBytes(ByteBuffer p_295937_) {
        this.source.writeBytes(p_295937_);
        return this;
    }

    @Override
    public int writeBytes(InputStream p_130481_, int p_130482_) throws IOException {
        return this.source.writeBytes(p_130481_, p_130482_);
    }

    @Override
    public int writeBytes(ScatteringByteChannel p_130490_, int p_130491_) throws IOException {
        return this.source.writeBytes(p_130490_, p_130491_);
    }

    @Override
    public int writeBytes(FileChannel p_130486_, long p_130487_, int p_130488_) throws IOException {
        return this.source.writeBytes(p_130486_, p_130487_, p_130488_);
    }

    public FriendlyByteBuf writeZero(int p_295016_) {
        this.source.writeZero(p_295016_);
        return this;
    }

    @Override
    public int writeCharSequence(CharSequence p_130501_, Charset p_130502_) {
        return this.source.writeCharSequence(p_130501_, p_130502_);
    }

    @Override
    public int indexOf(int p_130244_, int p_130245_, byte p_130246_) {
        return this.source.indexOf(p_130244_, p_130245_, p_130246_);
    }

    @Override
    public int bytesBefore(byte p_130108_) {
        return this.source.bytesBefore(p_130108_);
    }

    @Override
    public int bytesBefore(int p_130110_, byte p_130111_) {
        return this.source.bytesBefore(p_130110_, p_130111_);
    }

    @Override
    public int bytesBefore(int p_130113_, int p_130114_, byte p_130115_) {
        return this.source.bytesBefore(p_130113_, p_130114_, p_130115_);
    }

    @Override
    public int forEachByte(ByteProcessor p_130150_) {
        return this.source.forEachByte(p_130150_);
    }

    @Override
    public int forEachByte(int p_130146_, int p_130147_, ByteProcessor p_130148_) {
        return this.source.forEachByte(p_130146_, p_130147_, p_130148_);
    }

    @Override
    public int forEachByteDesc(ByteProcessor p_130156_) {
        return this.source.forEachByteDesc(p_130156_);
    }

    @Override
    public int forEachByteDesc(int p_130152_, int p_130153_, ByteProcessor p_130154_) {
        return this.source.forEachByteDesc(p_130152_, p_130153_, p_130154_);
    }

    @Override
    public ByteBuf copy() {
        return this.source.copy();
    }

    @Override
    public ByteBuf copy(int p_130128_, int p_130129_) {
        return this.source.copy(p_130128_, p_130129_);
    }

    @Override
    public ByteBuf slice() {
        return this.source.slice();
    }

    @Override
    public ByteBuf retainedSlice() {
        return this.source.retainedSlice();
    }

    @Override
    public ByteBuf slice(int p_130450_, int p_130451_) {
        return this.source.slice(p_130450_, p_130451_);
    }

    @Override
    public ByteBuf retainedSlice(int p_130359_, int p_130360_) {
        return this.source.retainedSlice(p_130359_, p_130360_);
    }

    @Override
    public ByteBuf duplicate() {
        return this.source.duplicate();
    }

    @Override
    public ByteBuf retainedDuplicate() {
        return this.source.retainedDuplicate();
    }

    @Override
    public int nioBufferCount() {
        return this.source.nioBufferCount();
    }

    @Override
    public ByteBuffer nioBuffer() {
        return this.source.nioBuffer();
    }

    @Override
    public ByteBuffer nioBuffer(int p_130270_, int p_130271_) {
        return this.source.nioBuffer(p_130270_, p_130271_);
    }

    @Override
    public ByteBuffer internalNioBuffer(int p_130248_, int p_130249_) {
        return this.source.internalNioBuffer(p_130248_, p_130249_);
    }

    @Override
    public ByteBuffer[] nioBuffers() {
        return this.source.nioBuffers();
    }

    @Override
    public ByteBuffer[] nioBuffers(int p_130275_, int p_130276_) {
        return this.source.nioBuffers(p_130275_, p_130276_);
    }

    @Override
    public boolean hasArray() {
        return this.source.hasArray();
    }

    @Override
    public byte[] array() {
        return this.source.array();
    }

    @Override
    public int arrayOffset() {
        return this.source.arrayOffset();
    }

    @Override
    public boolean hasMemoryAddress() {
        return this.source.hasMemoryAddress();
    }

    @Override
    public long memoryAddress() {
        return this.source.memoryAddress();
    }

    @Override
    public String toString(Charset p_130458_) {
        return this.source.toString(p_130458_);
    }

    @Override
    public String toString(int p_130454_, int p_130455_, Charset p_130456_) {
        return this.source.toString(p_130454_, p_130455_, p_130456_);
    }

    @Override
    public int hashCode() {
        return this.source.hashCode();
    }

    @Override
    public boolean equals(Object p_130144_) {
        return this.source.equals(p_130144_);
    }

    @Override
    public int compareTo(ByteBuf p_130123_) {
        return this.source.compareTo(p_130123_);
    }

    @Override
    public String toString() {
        return this.source.toString();
    }

    public FriendlyByteBuf retain(int p_294685_) {
        this.source.retain(p_294685_);
        return this;
    }

    public FriendlyByteBuf retain() {
        this.source.retain();
        return this;
    }

    public FriendlyByteBuf touch() {
        this.source.touch();
        return this;
    }

    public FriendlyByteBuf touch(Object p_296360_) {
        this.source.touch(p_296360_);
        return this;
    }

    @Override
    public int refCnt() {
        return this.source.refCnt();
    }

    @Override
    public boolean release() {
        return this.source.release();
    }

    @Override
    public boolean release(int p_130347_) {
        return this.source.release(p_130347_);
    }

    @FunctionalInterface
    public interface Reader<T> extends Function<FriendlyByteBuf, T> {
        default FriendlyByteBuf.Reader<Optional<T>> asOptional() {
            return p_236878_ -> p_236878_.readOptional(this);
        }
    }

    @FunctionalInterface
    public interface Writer<T> extends BiConsumer<FriendlyByteBuf, T> {
        default FriendlyByteBuf.Writer<Optional<T>> asOptional() {
            return (p_236881_, p_236882_) -> p_236881_.writeOptional(p_236882_, this);
        }
    }
}
