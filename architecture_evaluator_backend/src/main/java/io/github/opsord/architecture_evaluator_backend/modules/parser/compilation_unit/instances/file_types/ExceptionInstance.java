package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.file_types;

import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.JavaFileType;
import lombok.Data;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(callSuper = true)
@Data
public class ExceptionInstance extends ClassInstance {

    @Override
    public JavaFileType getJavaFileType() {
        return JavaFileType.EXCEPTION;
    }
}

