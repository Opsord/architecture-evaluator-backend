package io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.parts;

import lombok.Data;

@Data
public class CohesionMetricsDTO {
    private Integer couplingBetweenObjects; // CBO: number of classes that this class depends on
    private Double lackOfCohesion1;
    private Double lackOfCohesion2;
    private Double cohesionAmongMethods;
}
