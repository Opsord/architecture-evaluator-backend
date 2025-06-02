package io.github.Opsord.architecture_evaluator_backend.modules.parser.orchestrator.dto;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto.analysis.AnalysedCompUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto.summary.CompUnitSummaryDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompUnitWithAnalysisDTO {
    private CompUnitSummaryDTO compUnitSummaryDTO;
    private AnalysedCompUnitDTO analysis;
}
