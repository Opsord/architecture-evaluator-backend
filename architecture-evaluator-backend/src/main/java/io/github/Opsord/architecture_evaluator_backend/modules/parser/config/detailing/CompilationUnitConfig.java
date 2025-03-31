package io.github.Opsord.architecture_evaluator_backend.modules.parser.config.detailing;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompilationUnitConfig {
    private DetailLevel packageName = DetailLevel.DETAILED;
    private DetailLevel classNames = DetailLevel.DETAILED;
    private DetailLevel interfaceNames = DetailLevel.DETAILED;
    private DetailLevel methods = DetailLevel.DETAILED;
    private DetailLevel variables = DetailLevel.DETAILED;
    private DetailLevel importedPackages = DetailLevel.DETAILED;
    private DetailLevel comments = DetailLevel.DETAILED;
    private DetailLevel controlStatements = DetailLevel.DETAILED;
    private DetailLevel exceptionHandling = DetailLevel.DETAILED;
    private DetailLevel superClasses = DetailLevel.DETAILED;
    private DetailLevel implementedInterfaces = DetailLevel.DETAILED;
    private DetailLevel annotations = DetailLevel.DETAILED;
    private DetailLevel genericUsages = DetailLevel.DETAILED;
}