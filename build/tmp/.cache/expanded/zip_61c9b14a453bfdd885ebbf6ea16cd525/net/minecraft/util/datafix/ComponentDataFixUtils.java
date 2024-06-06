package net.minecraft.util.datafix;

import com.google.gson.JsonObject;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.util.GsonHelper;

public class ComponentDataFixUtils {
    private static final String EMPTY_CONTENTS = createTextComponentJson("");

    public static <T> Dynamic<T> createPlainTextComponent(DynamicOps<T> p_304546_, String p_304390_) {
        String s = createTextComponentJson(p_304390_);
        return new Dynamic<>(p_304546_, p_304546_.createString(s));
    }

    public static <T> Dynamic<T> createEmptyComponent(DynamicOps<T> p_304990_) {
        return new Dynamic<>(p_304990_, p_304990_.createString(EMPTY_CONTENTS));
    }

    private static String createTextComponentJson(String p_304837_) {
        JsonObject jsonobject = new JsonObject();
        jsonobject.addProperty("text", p_304837_);
        return GsonHelper.toStableString(jsonobject);
    }

    public static <T> Dynamic<T> createTranslatableComponent(DynamicOps<T> p_304499_, String p_304830_) {
        JsonObject jsonobject = new JsonObject();
        jsonobject.addProperty("translate", p_304830_);
        return new Dynamic<>(p_304499_, p_304499_.createString(GsonHelper.toStableString(jsonobject)));
    }

    public static <T> Dynamic<T> wrapLiteralStringAsComponent(Dynamic<T> p_304540_) {
        return DataFixUtils.orElse(p_304540_.asString().map(p_304989_ -> createPlainTextComponent(p_304540_.getOps(), p_304989_)).result(), p_304540_);
    }
}
