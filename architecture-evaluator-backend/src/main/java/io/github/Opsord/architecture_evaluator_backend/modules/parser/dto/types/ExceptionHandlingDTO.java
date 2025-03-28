// ExceptionHandlingDTO.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.dto.types;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ExceptionHandlingDTO {
    private String tryBlock;
    private List<String> catchBlocks;
    private String finallyBlock;
}