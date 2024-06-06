package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

public class ValidationContext {
    private final ProblemReporter reporter;
    private final LootContextParamSet params;
    private final LootDataResolver resolver;
    private final Set<LootDataId<?>> visitedElements;

    public ValidationContext(ProblemReporter p_311875_, LootContextParamSet p_279485_, LootDataResolver p_279476_) {
        this(p_311875_, p_279485_, p_279476_, Set.of());
    }

    private ValidationContext(ProblemReporter p_312319_, LootContextParamSet p_279447_, LootDataResolver p_279446_, Set<LootDataId<?>> p_311760_) {
        this.reporter = p_312319_;
        this.params = p_279447_;
        this.resolver = p_279446_;
        this.visitedElements = p_311760_;
    }

    public ValidationContext forChild(String p_79366_) {
        return new ValidationContext(this.reporter.forChild(p_79366_), this.params, this.resolver, this.visitedElements);
    }

    public ValidationContext enterElement(String p_279180_, LootDataId<?> p_279438_) {
        ImmutableSet<LootDataId<?>> immutableset = ImmutableSet.<LootDataId<?>>builder().addAll(this.visitedElements).add(p_279438_).build();
        return new ValidationContext(this.reporter.forChild(p_279180_), this.params, this.resolver, immutableset);
    }

    public boolean hasVisitedElement(LootDataId<?> p_279178_) {
        return this.visitedElements.contains(p_279178_);
    }

    public void reportProblem(String p_79358_) {
        this.reporter.report(p_79358_);
    }

    public void validateUser(LootContextUser p_79354_) {
        this.params.validateUser(this, p_79354_);
    }

    public LootDataResolver resolver() {
        return this.resolver;
    }

    public ValidationContext setParams(LootContextParamSet p_79356_) {
        return new ValidationContext(this.reporter, p_79356_, this.resolver, this.visitedElements);
    }
}
