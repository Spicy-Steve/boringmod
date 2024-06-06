package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class InventoryChangeTrigger extends SimpleCriterionTrigger<InventoryChangeTrigger.TriggerInstance> {
    @Override
    public Codec<InventoryChangeTrigger.TriggerInstance> codec() {
        return InventoryChangeTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer p_43150_, Inventory p_43151_, ItemStack p_43152_) {
        int i = 0;
        int j = 0;
        int k = 0;

        for(int l = 0; l < p_43151_.getContainerSize(); ++l) {
            ItemStack itemstack = p_43151_.getItem(l);
            if (itemstack.isEmpty()) {
                ++j;
            } else {
                ++k;
                if (itemstack.getCount() >= itemstack.getMaxStackSize()) {
                    ++i;
                }
            }
        }

        this.trigger(p_43150_, p_43151_, p_43152_, i, j, k);
    }

    private void trigger(ServerPlayer p_43154_, Inventory p_43155_, ItemStack p_43156_, int p_43157_, int p_43158_, int p_43159_) {
        this.trigger(p_43154_, p_43166_ -> p_43166_.matches(p_43155_, p_43156_, p_43157_, p_43158_, p_43159_));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, InventoryChangeTrigger.TriggerInstance.Slots slots, List<ItemPredicate> items)
        implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<InventoryChangeTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            p_311424_ -> p_311424_.group(
                        ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(InventoryChangeTrigger.TriggerInstance::player),
                        ExtraCodecs.strictOptionalField(
                                InventoryChangeTrigger.TriggerInstance.Slots.CODEC, "slots", InventoryChangeTrigger.TriggerInstance.Slots.ANY
                            )
                            .forGetter(InventoryChangeTrigger.TriggerInstance::slots),
                        ExtraCodecs.strictOptionalField(ItemPredicate.CODEC.listOf(), "items", List.of())
                            .forGetter(InventoryChangeTrigger.TriggerInstance::items)
                    )
                    .apply(p_311424_, InventoryChangeTrigger.TriggerInstance::new)
        );

        public static Criterion<InventoryChangeTrigger.TriggerInstance> hasItems(ItemPredicate.Builder... p_298231_) {
            return hasItems(Stream.of(p_298231_).map(ItemPredicate.Builder::build).<ItemPredicate>toArray(p_297901_ -> new ItemPredicate[p_297901_]));
        }

        public static Criterion<InventoryChangeTrigger.TriggerInstance> hasItems(ItemPredicate... p_43198_) {
            return CriteriaTriggers.INVENTORY_CHANGED
                .createCriterion(
                    new InventoryChangeTrigger.TriggerInstance(Optional.empty(), InventoryChangeTrigger.TriggerInstance.Slots.ANY, List.of(p_43198_))
                );
        }

        public static Criterion<InventoryChangeTrigger.TriggerInstance> hasItems(ItemLike... p_43200_) {
            ItemPredicate[] aitempredicate = new ItemPredicate[p_43200_.length];

            for(int i = 0; i < p_43200_.length; ++i) {
                aitempredicate[i] = new ItemPredicate(
                    Optional.empty(),
                    Optional.of(HolderSet.direct(p_43200_[i].asItem().builtInRegistryHolder())),
                    MinMaxBounds.Ints.ANY,
                    MinMaxBounds.Ints.ANY,
                    List.of(),
                    List.of(),
                    Optional.empty(),
                    Optional.empty()
                );
            }

            return hasItems(aitempredicate);
        }

        public boolean matches(Inventory p_43187_, ItemStack p_43188_, int p_43189_, int p_43190_, int p_43191_) {
            if (!this.slots.matches(p_43189_, p_43190_, p_43191_)) {
                return false;
            } else if (this.items.isEmpty()) {
                return true;
            } else if (this.items.size() != 1) {
                List<ItemPredicate> list = new ObjectArrayList<>(this.items);
                int i = p_43187_.getContainerSize();

                for(int j = 0; j < i; ++j) {
                    if (list.isEmpty()) {
                        return true;
                    }

                    ItemStack itemstack = p_43187_.getItem(j);
                    if (!itemstack.isEmpty()) {
                        list.removeIf(p_43194_ -> p_43194_.matches(itemstack));
                    }
                }

                return list.isEmpty();
            } else {
                return !p_43188_.isEmpty() && this.items.get(0).matches(p_43188_);
            }
        }

        public static record Slots(MinMaxBounds.Ints occupied, MinMaxBounds.Ints full, MinMaxBounds.Ints empty) {
            public static final Codec<InventoryChangeTrigger.TriggerInstance.Slots> CODEC = RecordCodecBuilder.create(
                p_312770_ -> p_312770_.group(
                            ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "occupied", MinMaxBounds.Ints.ANY)
                                .forGetter(InventoryChangeTrigger.TriggerInstance.Slots::occupied),
                            ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "full", MinMaxBounds.Ints.ANY)
                                .forGetter(InventoryChangeTrigger.TriggerInstance.Slots::full),
                            ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "empty", MinMaxBounds.Ints.ANY)
                                .forGetter(InventoryChangeTrigger.TriggerInstance.Slots::empty)
                        )
                        .apply(p_312770_, InventoryChangeTrigger.TriggerInstance.Slots::new)
            );
            public static final InventoryChangeTrigger.TriggerInstance.Slots ANY = new InventoryChangeTrigger.TriggerInstance.Slots(
                MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY
            );

            public boolean matches(int p_312470_, int p_312809_, int p_311989_) {
                if (!this.full.matches(p_312470_)) {
                    return false;
                } else if (!this.empty.matches(p_312809_)) {
                    return false;
                } else {
                    return this.occupied.matches(p_311989_);
                }
            }
        }
    }
}
