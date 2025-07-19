package io.github.opsord.architecture_evaluator_backend.modules.api.processor.dto.parts;

import lombok.Data;

@Data
public class CohesionMetricsDTO {
    private Integer lackOfCohesion1;
    private Integer lackOfCohesion2;
    private Integer lackOfCohesion3;
    private Integer lackOfCohesion4;
    private Double lackOfCohesion5;
}
