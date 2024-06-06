package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum CraftingBookCategory implements StringRepresentable {
    BUILDING("building"),
    REDSTONE("redstone"),
    EQUIPMENT("equipment"),
    MISC("misc");

    public static final Codec<CraftingBookCategory> CODEC = StringRepresentable.fromEnum(CraftingBookCategory::values);
    private final String name;

    private CraftingBookCategory(String p_249346_) {
        this.name = p_249346_;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
