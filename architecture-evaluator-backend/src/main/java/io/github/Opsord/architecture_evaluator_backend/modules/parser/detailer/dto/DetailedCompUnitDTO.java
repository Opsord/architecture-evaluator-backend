package io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.method.DetailedMethodDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DetailedCompUnitDTO {
    private int classCount;
    private List<DetailedMethodDTO> methods;
    private int numberOfMethods;
    private int linesOfCode;
}
