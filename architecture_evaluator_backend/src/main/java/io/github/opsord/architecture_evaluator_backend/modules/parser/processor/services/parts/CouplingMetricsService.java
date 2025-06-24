package io.github.opsord.architecture_evaluator_backend.modules.parser.processor.services.parts;

import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.JavaTypeInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.file_types.ClassInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.processor.dto.parts.CouplingMetricsDTO;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CouplingMetricsService {

    /**
     * Calculates the coupling metrics for a given Java type instance.
     */
    public CouplingMetricsDTO calculateCouplingMetrics(JavaTypeInstance javaTypeInstance) {
        if (javaTypeInstance == null || javaTypeInstance.getType() == null || javaTypeInstance.getContent() == null) {
            return new CouplingMetricsDTO();
        }
        if (javaTypeInstance.getType().isClassType()) {
            ClassInstance classInstance = (ClassInstance) javaTypeInstance.getContent();
            int afferentCoupling = calculateAfferentCoupling(classInstance);
            int efferentCoupling = calculateEfferentCoupling(classInstance);
            double instability = calculateInstability(afferentCoupling, efferentCoupling);

            CouplingMetricsDTO couplingMetrics = new CouplingMetricsDTO();
            couplingMetrics.setAfferentCoupling(afferentCoupling);
            couplingMetrics.setEfferentCoupling(efferentCoupling);
            couplingMetrics.setInstability(instability);

            return couplingMetrics;
        }
        return new CouplingMetricsDTO();
    }

    /**
     * Efferent coupling: number of classes this class depends on.
     */
    public int calculateEfferentCoupling(ClassInstance classInstance) {
        List<String> dependencies = classInstance.getClassDependencies();
        if (dependencies == null)
            return 0;
        return dependencies.size();
    }

    /**
     * Afferent coupling: number of classes that depend on this class.
     */
    public int calculateAfferentCoupling(ClassInstance classInstance) {
        List<String> dependentClasses = classInstance.getDependentClasses();
        if (dependentClasses == null)
            return 0;
        return dependentClasses.size();
    }

    /**
     * Instability = Ce / (Ca + Ce)
     */
    public double calculateInstability(int afferentCoupling, int efferentCoupling) {
        if (afferentCoupling + efferentCoupling == 0) {
            return 0;
        }
        return (double) efferentCoupling / (efferentCoupling + afferentCoupling);
    }
}