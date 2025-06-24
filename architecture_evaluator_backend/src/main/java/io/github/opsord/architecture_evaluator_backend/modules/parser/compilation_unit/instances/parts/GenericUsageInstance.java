// GenericUsageDTO.java
package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.parts;

import lombok.Data;

import java.util.List;

@Data
public class GenericUsageInstance {
    private String type;
    private List<String> genericTypes;
}