// AnnotationDTO.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts;

import lombok.Data;

import java.util.Map;

@Data
public class AnnotationDTO {
    private String name;
    private Map<String, String> attributes;
}