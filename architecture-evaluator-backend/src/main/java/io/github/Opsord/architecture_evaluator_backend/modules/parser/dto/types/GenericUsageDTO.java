// GenericUsageDTO.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.dto.types;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GenericUsageDTO {
    private String type;
    private List<String> genericTypes;
}