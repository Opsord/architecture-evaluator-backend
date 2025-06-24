package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances;

import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.parts.LayerAnnotation;

public interface JavaTypeContent {
    String getName();
    JavaFileType getJavaFileType();
    Integer getLinesOfCode();

    LayerAnnotation getLayerAnnotation();
}