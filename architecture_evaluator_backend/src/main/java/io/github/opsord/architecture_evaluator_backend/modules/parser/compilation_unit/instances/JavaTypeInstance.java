package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances;

import lombok.Data;

@Data
public class JavaTypeInstance {
    private JavaFileType type;
    private JavaTypeContent content;
}
