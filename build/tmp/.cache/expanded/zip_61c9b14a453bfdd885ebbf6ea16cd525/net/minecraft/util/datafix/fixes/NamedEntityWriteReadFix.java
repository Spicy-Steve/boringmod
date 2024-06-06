package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.Util;

public abstract class NamedEntityWriteReadFix extends DataFix {
    private final String name;
    private final String entityName;
    private final TypeReference type;

    public NamedEntityWriteReadFix(Schema p_307236_, boolean p_307467_, String p_307246_, TypeReference p_307497_, String p_307636_) {
        super(p_307236_, p_307467_);
        this.name = p_307246_;
        this.type = p_307497_;
        this.entityName = p_307636_;
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(this.type);
        Type<?> type1 = this.getInputSchema().getChoiceType(this.type, this.entityName);
        Type<?> type2 = this.getOutputSchema().getType(this.type);
        Type<?> type3 = this.getOutputSchema().getChoiceType(this.type, this.entityName);
        OpticFinder<?> opticfinder = DSL.namedChoice(this.entityName, type1);
        return this.fixTypeEverywhereTyped(
            this.name,
            type,
            type2,
            p_307259_ -> p_307259_.updateTyped(opticfinder, type3, p_311565_ -> Util.writeAndReadTypedOrThrow(p_311565_, type3, this::fix))
        );
    }

    protected abstract <T> Dynamic<T> fix(Dynamic<T> p_307318_);
}
