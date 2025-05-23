package io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.parts;

import lombok.Data;

@Data
public class CohesionMetricsDTO {
    private Integer couplingBetweenObjects; // CBO: number of classes that this class depends on
    private Integer lackOfCohesion1;
    private Integer lackOfCohesion2;
    private Integer lackOfCohesion4;
    private Double cohesionAmongMethods;
}
