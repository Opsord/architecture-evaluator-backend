package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.file_types;

import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.JavaFileType;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.JavaTypeContent;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.parts.LayerAnnotation;
import lombok.Data;

@Data
public class UnknownInstance implements JavaTypeContent {

    private LayerAnnotation layerAnnotation;

    @Override
    public String getName() {
        return "Unknown";
    }

    @Override
    public JavaFileType getJavaFileType() {
        return JavaFileType.UNKNOWN;
    }

    @Override
    public Integer getLinesOfCode() {
        return 0;
    }
}

