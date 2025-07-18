package io.github.opsord.architecture_evaluator_backend.modules.api.processor.services.parts;

import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.class_instance.ClassInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.processor.dto.parts.CouplingMetricsDTO;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CouplingMetricsService {

    /**
     * Calculates the coupling metrics for a given class.
     */
    public CouplingMetricsDTO calculateCouplingMetrics(ClassInstance classInstance) {
        int afferentCoupling = calculateAfferentCoupling(classInstance);
        int efferentCoupling = calculateEfferentCoupling(classInstance);
        double instability = calculateInstability(afferentCoupling, efferentCoupling);

        CouplingMetricsDTO couplingMetrics = new CouplingMetricsDTO();
        couplingMetrics.setAfferentCoupling(afferentCoupling);
        couplingMetrics.setEfferentCoupling(efferentCoupling);
        couplingMetrics.setInstability(instability);

        return couplingMetrics;
    }

    /**
     * Efferent coupling: number of classes this class depends on.
     */
    private int calculateEfferentCoupling(ClassInstance classInstance) {
        List<String> dependencies = classInstance.getClassDependencies();
        if (dependencies == null)
            return 0;
        return dependencies.size();
    }

    /**
     * Afferent coupling: number of classes that depend on this class.
     */
    private int calculateAfferentCoupling(ClassInstance classInstance) {
        List<String> dependentClasses = classInstance.getDependentClasses();
        if (dependentClasses == null)
            return 0;
        return dependentClasses.size();
    }

    /**
     * Instability = Ce / (Ca + Ce)
     */
    private double calculateInstability(int afferentCoupling, int efferentCoupling) {
        if (afferentCoupling + efferentCoupling == 0) {
            return 0;
        }
        return (double) efferentCoupling / (efferentCoupling + afferentCoupling);
    }
}