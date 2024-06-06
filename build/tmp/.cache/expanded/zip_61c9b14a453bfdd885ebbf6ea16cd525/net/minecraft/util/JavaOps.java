package net.minecraft.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.RecordBuilder.AbstractUniversalBuilder;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public class JavaOps implements DynamicOps<Object> {
    public static final JavaOps INSTANCE = new JavaOps();

    private JavaOps() {
    }

    @Override
    public Object empty() {
        return null;
    }

    @Override
    public Object emptyMap() {
        return Map.of();
    }

    @Override
    public Object emptyList() {
        return List.of();
    }

    @Override
    public <U> U convertTo(DynamicOps<U> p_304871_, Object p_304579_) {
        if (p_304579_ == null) {
            return p_304871_.empty();
        } else if (p_304579_ instanceof Map) {
            return this.convertMap(p_304871_, p_304579_);
        } else if (p_304579_ instanceof ByteList bytelist) {
            return p_304871_.createByteList(ByteBuffer.wrap(bytelist.toByteArray()));
        } else if (p_304579_ instanceof IntList intlist) {
            return p_304871_.createIntList(intlist.intStream());
        } else if (p_304579_ instanceof LongList longlist) {
            return p_304871_.createLongList(longlist.longStream());
        } else if (p_304579_ instanceof List) {
            return this.convertList(p_304871_, p_304579_);
        } else if (p_304579_ instanceof String s) {
            return p_304871_.createString(s);
        } else if (p_304579_ instanceof Boolean obool) {
            return p_304871_.createBoolean(obool);
        } else if (p_304579_ instanceof Byte obyte) {
            return p_304871_.createByte(obyte);
        } else if (p_304579_ instanceof Short oshort) {
            return p_304871_.createShort(oshort);
        } else if (p_304579_ instanceof Integer integer) {
            return p_304871_.createInt(integer);
        } else if (p_304579_ instanceof Long olong) {
            return p_304871_.createLong(olong);
        } else if (p_304579_ instanceof Float f) {
            return p_304871_.createFloat(f);
        } else if (p_304579_ instanceof Double d0) {
            return p_304871_.createDouble(d0);
        } else if (p_304579_ instanceof Number number) {
            return p_304871_.createNumeric(number);
        } else {
            throw new IllegalStateException("Don't know how to convert " + p_304579_);
        }
    }

    @Override
    public DataResult<Number> getNumberValue(Object p_304427_) {
        return p_304427_ instanceof Number number ? DataResult.success(number) : DataResult.error(() -> "Not a number: " + p_304427_);
    }

    @Override
    public Object createNumeric(Number p_304479_) {
        return p_304479_;
    }

    @Override
    public Object createByte(byte p_304455_) {
        return p_304455_;
    }

    @Override
    public Object createShort(short p_304574_) {
        return p_304574_;
    }

    @Override
    public Object createInt(int p_304438_) {
        return p_304438_;
    }

    @Override
    public Object createLong(long p_304654_) {
        return p_304654_;
    }

    @Override
    public Object createFloat(float p_304906_) {
        return p_304906_;
    }

    @Override
    public Object createDouble(double p_304746_) {
        return p_304746_;
    }

    @Override
    public DataResult<Boolean> getBooleanValue(Object p_304425_) {
        return p_304425_ instanceof Boolean obool ? DataResult.success(obool) : DataResult.error(() -> "Not a boolean: " + p_304425_);
    }

    @Override
    public Object createBoolean(boolean p_304792_) {
        return p_304792_;
    }

    @Override
    public DataResult<String> getStringValue(Object p_304765_) {
        return p_304765_ instanceof String s ? DataResult.success(s) : DataResult.error(() -> "Not a string: " + p_304765_);
    }

    @Override
    public Object createString(String p_304874_) {
        return p_304874_;
    }

    @Override
    public DataResult<Object> mergeToList(Object p_304918_, Object p_304963_) {
        if (p_304918_ == this.empty()) {
            return DataResult.success(List.of(p_304963_));
        } else if (p_304918_ instanceof List list) {
            return list.isEmpty()
                ? DataResult.success(List.of(p_304963_))
                : DataResult.success(ImmutableList.<Object>builder().addAll(list).add(p_304963_).build());
        } else {
            return DataResult.error(() -> "Not a list: " + p_304918_);
        }
    }

    @Override
    public DataResult<Object> mergeToList(Object p_304825_, List<Object> p_304752_) {
        if (p_304825_ == this.empty()) {
            return DataResult.success(p_304752_);
        } else if (p_304825_ instanceof List list) {
            return list.isEmpty() ? DataResult.success(p_304752_) : DataResult.success(ImmutableList.<Object>builder().addAll(list).addAll(p_304752_).build());
        } else {
            return DataResult.error(() -> "Not a list: " + p_304825_);
        }
    }

    @Override
    public DataResult<Object> mergeToMap(Object p_304566_, Object p_304866_, Object p_304617_) {
        if (p_304566_ == this.empty()) {
            return DataResult.success(Map.of(p_304866_, p_304617_));
        } else if (p_304566_ instanceof Map map) {
            if (map.isEmpty()) {
                return DataResult.success(Map.of(p_304866_, p_304617_));
            } else {
                Builder<Object, Object> builder = ImmutableMap.builderWithExpectedSize(map.size() + 1);
                builder.putAll(map);
                builder.put(p_304866_, p_304617_);
                return DataResult.success(builder.buildKeepingLast());
            }
        } else {
            return DataResult.error(() -> "Not a map: " + p_304566_);
        }
    }

    @Override
    public DataResult<Object> mergeToMap(Object p_304844_, Map<Object, Object> p_304663_) {
        if (p_304844_ == this.empty()) {
            return DataResult.success(p_304663_);
        } else if (p_304844_ instanceof Map map) {
            if (map.isEmpty()) {
                return DataResult.success(p_304663_);
            } else {
                Builder<Object, Object> builder = ImmutableMap.builderWithExpectedSize(map.size() + p_304663_.size());
                builder.putAll(map);
                builder.putAll(p_304663_);
                return DataResult.success(builder.buildKeepingLast());
            }
        } else {
            return DataResult.error(() -> "Not a map: " + p_304844_);
        }
    }

    private static Map<Object, Object> mapLikeToMap(MapLike<Object> p_304682_) {
        return p_304682_.entries().collect(ImmutableMap.toImmutableMap(Pair::getFirst, Pair::getSecond));
    }

    @Override
    public DataResult<Object> mergeToMap(Object p_304431_, MapLike<Object> p_304651_) {
        if (p_304431_ == this.empty()) {
            return DataResult.success(mapLikeToMap(p_304651_));
        } else if (p_304431_ instanceof Map map) {
            if (map.isEmpty()) {
                return DataResult.success(mapLikeToMap(p_304651_));
            } else {
                Builder<Object, Object> builder = ImmutableMap.builderWithExpectedSize(map.size());
                builder.putAll(map);
                p_304651_.entries().forEach(p_304552_ -> builder.put(p_304552_.getFirst(), p_304552_.getSecond()));
                return DataResult.success(builder.buildKeepingLast());
            }
        } else {
            return DataResult.error(() -> "Not a map: " + p_304431_);
        }
    }

    static Stream<Pair<Object, Object>> getMapEntries(Map<?, ?> p_304619_) {
        return p_304619_.entrySet().stream().map(p_304506_ -> Pair.of(p_304506_.getKey(), p_304506_.getValue()));
    }

    @Override
    public DataResult<Stream<Pair<Object, Object>>> getMapValues(Object p_304429_) {
        return p_304429_ instanceof Map map ? DataResult.success(getMapEntries(map)) : DataResult.error(() -> "Not a map: " + p_304429_);
    }

    @Override
    public DataResult<Consumer<BiConsumer<Object, Object>>> getMapEntries(Object p_304526_) {
        return p_304526_ instanceof Map map ? DataResult.success(map::forEach) : DataResult.error(() -> "Not a map: " + p_304526_);
    }

    @Override
    public Object createMap(Stream<Pair<Object, Object>> p_304665_) {
        return p_304665_.collect(ImmutableMap.toImmutableMap(Pair::getFirst, Pair::getSecond));
    }

    @Override
    public DataResult<MapLike<Object>> getMap(Object p_304850_) {
        return p_304850_ instanceof Map map ? DataResult.success(new MapLike<Object>() {
            @Nullable
            @Override
            public Object get(Object p_304705_) {
                return map.get(p_304705_);
            }

            @Nullable
            @Override
            public Object get(String p_304715_) {
                return map.get(p_304715_);
            }

            @Override
            public Stream<Pair<Object, Object>> entries() {
                return JavaOps.getMapEntries(map);
            }

            @Override
            public String toString() {
                return "MapLike[" + map + "]";
            }
        }) : DataResult.error(() -> "Not a map: " + p_304850_);
    }

    @Override
    public Object createMap(Map<Object, Object> p_304755_) {
        return p_304755_;
    }

    @Override
    public DataResult<Stream<Object>> getStream(Object p_304435_) {
        return p_304435_ instanceof List list
            ? DataResult.success(list.stream().map(p_304639_ -> p_304639_))
            : DataResult.error(() -> "Not an list: " + p_304435_);
    }

    @Override
    public DataResult<Consumer<Consumer<Object>>> getList(Object p_304548_) {
        return p_304548_ instanceof List list ? DataResult.success(list::forEach) : DataResult.error(() -> "Not an list: " + p_304548_);
    }

    @Override
    public Object createList(Stream<Object> p_304863_) {
        return p_304863_.toList();
    }

    @Override
    public DataResult<ByteBuffer> getByteBuffer(Object p_304876_) {
        return p_304876_ instanceof ByteList bytelist
            ? DataResult.success(ByteBuffer.wrap(bytelist.toByteArray()))
            : DataResult.error(() -> "Not a byte list: " + p_304876_);
    }

    @Override
    public Object createByteList(ByteBuffer p_304631_) {
        ByteBuffer bytebuffer = p_304631_.duplicate().clear();
        ByteArrayList bytearraylist = new ByteArrayList();
        bytearraylist.size(bytebuffer.capacity());
        bytebuffer.get(0, bytearraylist.elements(), 0, bytearraylist.size());
        return bytearraylist;
    }

    @Override
    public DataResult<IntStream> getIntStream(Object p_304602_) {
        return p_304602_ instanceof IntList intlist ? DataResult.success(intlist.intStream()) : DataResult.error(() -> "Not an int list: " + p_304602_);
    }

    @Override
    public Object createIntList(IntStream p_304627_) {
        return IntArrayList.toList(p_304627_);
    }

    @Override
    public DataResult<LongStream> getLongStream(Object p_304806_) {
        return p_304806_ instanceof LongList longlist ? DataResult.success(longlist.longStream()) : DataResult.error(() -> "Not a long list: " + p_304806_);
    }

    @Override
    public Object createLongList(LongStream p_304580_) {
        return LongArrayList.toList(p_304580_);
    }

    @Override
    public Object remove(Object p_304813_, String p_304872_) {
        if (p_304813_ instanceof Map map) {
            Map<Object, Object> map1 = new LinkedHashMap<>(map);
            map1.remove(p_304872_);
            return DataResult.success(Map.copyOf(map1));
        } else {
            return DataResult.error(() -> "Not a map: " + p_304813_);
        }
    }

    @Override
    public RecordBuilder<Object> mapBuilder() {
        return new JavaOps.FixedMapBuilder<>(this);
    }

    @Override
    public String toString() {
        return "Java";
    }

    static final class FixedMapBuilder<T> extends AbstractUniversalBuilder<T, Builder<T, T>> {
        public FixedMapBuilder(DynamicOps<T> p_304923_) {
            super(p_304923_);
        }

        protected Builder<T, T> initBuilder() {
            return ImmutableMap.builder();
        }

        protected Builder<T, T> append(T p_304524_, T p_304701_, Builder<T, T> p_304598_) {
            return p_304598_.put(p_304524_, p_304701_);
        }

        protected DataResult<T> build(Builder<T, T> p_304490_, T p_304640_) {
            ImmutableMap<T, T> immutablemap;
            try {
                immutablemap = p_304490_.buildOrThrow();
            } catch (IllegalArgumentException illegalargumentexception) {
                return DataResult.error(() -> "Can't build map: " + illegalargumentexception.getMessage());
            }

            return this.ops().mergeToMap(p_304640_, immutablemap);
        }
    }
}
