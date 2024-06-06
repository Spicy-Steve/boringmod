package net.minecraft.world.level.block.entity.trialspawner;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;

public class TrialSpawnerData {
    public static final String TAG_SPAWN_DATA = "spawn_data";
    private static final String TAG_NEXT_MOB_SPAWNS_AT = "next_mob_spawns_at";
    public static MapCodec<TrialSpawnerData> MAP_CODEC = RecordCodecBuilder.mapCodec(
        p_312830_ -> p_312830_.group(
                    UUIDUtil.CODEC_SET.optionalFieldOf("registered_players", Sets.newHashSet()).forGetter(p_312495_ -> p_312495_.detectedPlayers),
                    UUIDUtil.CODEC_SET.optionalFieldOf("current_mobs", Sets.newHashSet()).forGetter(p_312798_ -> p_312798_.currentMobs),
                    Codec.LONG.optionalFieldOf("cooldown_ends_at", Long.valueOf(0L)).forGetter(p_312792_ -> p_312792_.cooldownEndsAt),
                    Codec.LONG.optionalFieldOf("next_mob_spawns_at", Long.valueOf(0L)).forGetter(p_311772_ -> p_311772_.nextMobSpawnsAt),
                    Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("total_mobs_spawned", 0).forGetter(p_312862_ -> p_312862_.totalMobsSpawned),
                    SpawnData.CODEC.optionalFieldOf("spawn_data").forGetter(p_312634_ -> p_312634_.nextSpawnData),
                    ResourceLocation.CODEC.optionalFieldOf("ejecting_loot_table").forGetter(p_312388_ -> p_312388_.ejectingLootTable)
                )
                .apply(p_312830_, TrialSpawnerData::new)
    );
    protected final Set<UUID> detectedPlayers = new HashSet<>();
    protected final Set<UUID> currentMobs = new HashSet<>();
    protected long cooldownEndsAt;
    protected long nextMobSpawnsAt;
    protected int totalMobsSpawned;
    protected Optional<SpawnData> nextSpawnData;
    protected Optional<ResourceLocation> ejectingLootTable;
    protected SimpleWeightedRandomList<SpawnData> spawnPotentials;
    @Nullable
    protected Entity displayEntity;
    protected double spin;
    protected double oSpin;

    public TrialSpawnerData() {
        this(Collections.emptySet(), Collections.emptySet(), 0L, 0L, 0, Optional.empty(), Optional.empty());
    }

    public TrialSpawnerData(
        Set<UUID> p_312283_,
        Set<UUID> p_312919_,
        long p_312537_,
        long p_311955_,
        int p_312227_,
        Optional<SpawnData> p_312562_,
        Optional<ResourceLocation> p_312406_
    ) {
        this.detectedPlayers.addAll(p_312283_);
        this.currentMobs.addAll(p_312919_);
        this.cooldownEndsAt = p_312537_;
        this.nextMobSpawnsAt = p_311955_;
        this.totalMobsSpawned = p_312227_;
        this.nextSpawnData = p_312562_;
        this.ejectingLootTable = p_312406_;
    }

    public void setSpawnPotentialsFromConfig(TrialSpawnerConfig p_312066_) {
        SimpleWeightedRandomList<SpawnData> simpleweightedrandomlist = p_312066_.spawnPotentialsDefinition();
        if (simpleweightedrandomlist.isEmpty()) {
            this.spawnPotentials = SimpleWeightedRandomList.single(this.nextSpawnData.orElseGet(SpawnData::new));
        } else {
            this.spawnPotentials = simpleweightedrandomlist;
        }
    }

    public void reset() {
        this.detectedPlayers.clear();
        this.totalMobsSpawned = 0;
        this.nextMobSpawnsAt = 0L;
        this.cooldownEndsAt = 0L;
        this.currentMobs.clear();
    }

    public boolean hasMobToSpawn() {
        boolean flag = this.nextSpawnData.isPresent() && this.nextSpawnData.get().getEntityToSpawn().contains("id", 8);
        return flag || !this.spawnPotentials.isEmpty();
    }

    public boolean hasFinishedSpawningAllMobs(TrialSpawnerConfig p_311805_, int p_312034_) {
        return this.totalMobsSpawned >= p_311805_.calculateTargetTotalMobs(p_312034_);
    }

    public boolean haveAllCurrentMobsDied() {
        return this.currentMobs.isEmpty();
    }

    public boolean isReadyToSpawnNextMob(ServerLevel p_311818_, TrialSpawnerConfig p_312100_, int p_312550_) {
        return p_311818_.getGameTime() >= this.nextMobSpawnsAt && this.currentMobs.size() < p_312100_.calculateTargetSimultaneousMobs(p_312550_);
    }

    public int countAdditionalPlayers(BlockPos p_312262_) {
        if (this.detectedPlayers.isEmpty()) {
            Util.logAndPauseIfInIde("Trial Spawner at " + p_312262_ + " has no detected players");
        }

        return Math.max(0, this.detectedPlayers.size() - 1);
    }

    public void tryDetectPlayers(ServerLevel p_311852_, BlockPos p_312503_, PlayerDetector p_312592_, int p_312480_) {
        List<UUID> list = p_312592_.detect(p_311852_, p_312503_, p_312480_);
        boolean flag = this.detectedPlayers.addAll(list);
        if (flag) {
            this.nextMobSpawnsAt = Math.max(p_311852_.getGameTime() + 40L, this.nextMobSpawnsAt);
            p_311852_.levelEvent(3013, p_312503_, this.detectedPlayers.size());
        }
    }

    public boolean isReadyToOpenShutter(ServerLevel p_312291_, TrialSpawnerConfig p_312182_, float p_312417_) {
        long i = this.cooldownEndsAt - (long)p_312182_.targetCooldownLength();
        return (float)p_312291_.getGameTime() >= (float)i + p_312417_;
    }

    public boolean isReadyToEjectItems(ServerLevel p_312692_, TrialSpawnerConfig p_312027_, float p_312374_) {
        long i = this.cooldownEndsAt - (long)p_312027_.targetCooldownLength();
        return (float)(p_312692_.getGameTime() - i) % p_312374_ == 0.0F;
    }

    public boolean isCooldownFinished(ServerLevel p_312743_) {
        return p_312743_.getGameTime() >= this.cooldownEndsAt;
    }

    public void setEntityId(TrialSpawner p_312044_, RandomSource p_312864_, EntityType<?> p_312415_) {
        this.getOrCreateNextSpawnData(p_312044_, p_312864_).getEntityToSpawn().putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(p_312415_).toString());
    }

    protected SpawnData getOrCreateNextSpawnData(TrialSpawner p_312745_, RandomSource p_312242_) {
        if (this.nextSpawnData.isPresent()) {
            return this.nextSpawnData.get();
        } else {
            this.nextSpawnData = Optional.of(this.spawnPotentials.getRandom(p_312242_).map(WeightedEntry.Wrapper::getData).orElseGet(SpawnData::new));
            p_312745_.markUpdated();
            return this.nextSpawnData.get();
        }
    }

    @Nullable
    public Entity getOrCreateDisplayEntity(TrialSpawner p_312366_, Level p_312148_, TrialSpawnerState p_311790_) {
        if (p_312366_.canSpawnInLevel(p_312148_) && p_311790_.hasSpinningMob()) {
            if (this.displayEntity == null) {
                CompoundTag compoundtag = this.getOrCreateNextSpawnData(p_312366_, p_312148_.getRandom()).getEntityToSpawn();
                if (compoundtag.contains("id", 8)) {
                    this.displayEntity = EntityType.loadEntityRecursive(compoundtag, p_312148_, Function.identity());
                }
            }

            return this.displayEntity;
        } else {
            return null;
        }
    }

    public CompoundTag getUpdateTag(TrialSpawnerState p_312104_) {
        CompoundTag compoundtag = new CompoundTag();
        if (p_312104_ == TrialSpawnerState.ACTIVE) {
            compoundtag.putLong("next_mob_spawns_at", this.nextMobSpawnsAt);
        }

        this.nextSpawnData
            .ifPresent(
                p_312575_ -> compoundtag.put(
                        "spawn_data",
                        SpawnData.CODEC.encodeStart(NbtOps.INSTANCE, p_312575_).result().orElseThrow(() -> new IllegalStateException("Invalid SpawnData"))
                    )
            );
        return compoundtag;
    }

    public double getSpin() {
        return this.spin;
    }

    public double getOSpin() {
        return this.oSpin;
    }
}
