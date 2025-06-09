// GenericUsageDTO.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.parts;

import lombok.Data;

import java.util.List;

@Data
public class GenericUsageInstance {
    private String type;
    private List<String> genericTypes;
}