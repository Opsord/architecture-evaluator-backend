package io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.parts.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DetailedMethodDTO {

    // Basic info
    private BasicInfo basicInfo;

    // Statement info
    private StatementInfo statementInfo;

    // Detailed parameters
    private DetailedParameters detailedParameters;

    // Metrics
    private MethodMetrics methodMetrics;

}