// GenericUsageDTO.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts;

import lombok.Data;

import java.util.List;

@Data
public class GenericUsageDTO {
    private String type;
    private List<String> genericTypes;
}