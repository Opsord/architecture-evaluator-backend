package io.github.opsord.architecture_evaluator_backend.modules.api.processor.dto.parts;

import lombok.Data;

@Data
public class CouplingMetricsDTO {
    private int afferentCoupling; // Ca: number of classes that depend on this class
    private int efferentCoupling; // Ce: number of classes that this class depends on
    private double instability; // I = Ce / (Ca + Ce)
}