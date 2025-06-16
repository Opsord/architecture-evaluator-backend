// ExceptionHandlingDTO.java
package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.parts;

import lombok.Data;

import java.util.List;

@Data
public class ExceptionHandlingInstance {
    private String tryBlock;
    private List<String> catchBlocks;
    private String finallyBlock;
}