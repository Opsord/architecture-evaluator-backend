package io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.services.parts;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.CustomCompilationUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.parts.CouplingMetricsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CouplingMetricsService {

    /**
     * Calculate the coupling metrics for a given compilation unit.
     *
     * @param compilationUnit The compilation unit to analyze.
     * @param allUnits The list of all compilation units in the project.
     * @return The coupling metrics for the given compilation unit.
     */
    public CouplingMetricsDTO calculateCouplingMetrics(CustomCompilationUnitDTO compilationUnit, List<CustomCompilationUnitDTO> allUnits) {
        CouplingMetricsDTO metrics = new CouplingMetricsDTO();
        metrics.setAfferentCoupling(calculateAfferentCoupling(compilationUnit, allUnits));
        metrics.setEfferentCoupling(calculateEfferentCoupling(compilationUnit, allUnits));
        metrics.setInstability(calculateInstability(metrics.getAfferentCoupling(), metrics.getEfferentCoupling()));
        return metrics;
    }

    /**
     * Count the number of classes in the package of the compilation unit.
     *
     * @param compilationUnit The compilation unit to analyze.
     * @param allUnits The list of all compilation units in the project.
     * @return The number of classes in the package of the compilation unit.
     */
    public int countClassesInPackage(CustomCompilationUnitDTO compilationUnit, List<CustomCompilationUnitDTO> allUnits) {
        return (int) allUnits.stream()
                .filter(unit -> unit.getPackageName().equals(compilationUnit.getPackageName()))
                .count();
    }

    /**
     * Count the number of classes that imports a package.
     *
     * @param packageName The package name to analyze.
     * @param allUnits The list of all compilation units in the project.
     * @return The number of classes that import the package.
     */
    public int countClassesImportingPackage(String packageName, List<CustomCompilationUnitDTO> allUnits) {
        return (int) allUnits.stream()
                .filter(unit -> unit.getImportedPackages().stream()
                        .anyMatch(importedPackage -> importedPackage.startsWith(packageName)))
                .count();
    }


    /**
     * Calculate the afferent coupling for a given compilation unit.
     *
     * @param compilationUnit The compilation unit to analyze.
     * @param allUnits The list of all compilation units in the project.
     * @return The afferent coupling for the given compilation unit.
     */
    public int calculateAfferentCoupling(CustomCompilationUnitDTO compilationUnit, List<CustomCompilationUnitDTO> allUnits) {
        int packageClasses = countClassesInPackage(compilationUnit, allUnits);
        int packageImports = countClassesImportingPackage(compilationUnit.getPackageName(), allUnits);
        /// The -1 is to remove the class itself from the count
        return packageClasses + packageImports - 1;
    }

    /**
     * Calculate the efferent coupling for a given compilation unit.
     *
     * @param compilationUnit The compilation unit to analyze.
     * @param allUnits The list of all compilation units in the project.
     * @return The efferent coupling for the given compilation unit.
     */
    public int calculateEfferentCoupling(CustomCompilationUnitDTO compilationUnit, List<CustomCompilationUnitDTO> allUnits) {
        String packageName = compilationUnit.getPackageName();
        int totalPackageImports = allUnits.stream()
                .filter(unit -> unit.getImportedPackages().stream()
                        .anyMatch(importedPackage -> importedPackage.startsWith(packageName)))
                .mapToInt(unit -> countClassesInPackage(unit, allUnits))
                .sum();
        // The -1 is to remove the class itself from the count
        return totalPackageImports - 1;
    }

    public double calculateInstability(int afferentCoupling, int efferentCoupling) {
        if (afferentCoupling + efferentCoupling == 0) {
            return 0; // Avoid division by zero
        }
        return (double) efferentCoupling / (afferentCoupling + efferentCoupling);
    }
}