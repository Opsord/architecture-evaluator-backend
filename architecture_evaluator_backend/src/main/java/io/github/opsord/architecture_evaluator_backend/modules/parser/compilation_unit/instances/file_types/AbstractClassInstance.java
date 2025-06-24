package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.file_types;

import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.JavaFileType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class AbstractClassInstance extends ClassInstance {
    @Override
    public JavaFileType getJavaFileType() {
        return JavaFileType.ABSTRACT_CLASS;
    }
}

