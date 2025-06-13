package io.github.opsord.architecture_evaluator_backend.modules.parser.processor.dto.parts;

import lombok.Data;

@Data
public class CohesionMetricsDTO {
    private Integer lackOfCohesion1;
    private Double lackOfCohesion2;
    private Integer lackOfCohesion3;
    private Integer lackOfCohesion4;
    private Double lackOfCohesion5;
}
