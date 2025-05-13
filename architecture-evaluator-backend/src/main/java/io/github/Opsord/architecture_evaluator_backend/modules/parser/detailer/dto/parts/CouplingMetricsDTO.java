package io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.parts;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CouplingMetricsDTO {
    private int afferentCoupling; // Ca: number of classes that depend on this class
    private int efferentCoupling; // Ce: number of classes that this class depends on
    private double instability;  // I = Ce / (Ca + Ce)
}