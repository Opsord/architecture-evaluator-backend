package io.github.opsord.architecture_evaluator_backend.modules.processor.parts;

import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.class_instance.ClassInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.class_instance.parts.ParameterInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.class_instance.parts.method.MethodInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.class_instance.parts.method.parts.statement.StatementInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.class_instance.parts.method.parts.StatementsInfo;
import io.github.opsord.architecture_evaluator_backend.modules.api.processor.dto.parts.ComplexityMetricsDTO;
import io.github.opsord.architecture_evaluator_backend.modules.api.processor.services.parts.ComplexityMetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ComplexityMetricsServiceTest {

    private ComplexityMetricsService service;

    @BeforeEach
    void setUp() {
        service = new ComplexityMetricsService();
    }

    // Helper to create a MethodInstance with the given properties
    private MethodInstance method(int numStatements, int numControlStatements, int numInputs, int numOutputs) {
        MethodInstance methodInstance = new MethodInstance();
        StatementsInfo statementsInfo = new StatementsInfo();
        statementsInfo.setStatements(Collections.nCopies(numStatements, new StatementInstance()));
        statementsInfo.setNumberOfControlStatements(numControlStatements);
        methodInstance.setStatementsInfo(statementsInfo);
        methodInstance.setInputParameters(numInputs > 0 ? Collections.nCopies(numInputs, new ParameterInstance()) : null);
        methodInstance.setOutputParameters(numOutputs > 0 ? Collections.nCopies(numOutputs, new ParameterInstance()) : null);
        return methodInstance;
    }

    @Test
    void testEmptyClass() {
        ClassInstance classInstance = new ClassInstance();
        classInstance.setMethods(Collections.emptyList());
        classInstance.setStatements(Collections.emptyList());
        classInstance.setLinesOfCode(0);

        ComplexityMetricsDTO dto = service.calculateComplexityMetrics(classInstance);
        assertEquals(0, dto.getApproxMcCabeCC());
        assertEquals(0.0, dto.getImprovedCC());
    }

    @Test
    void testSingleMethodNoStatements() {
        MethodInstance m = method(0, 0, 0, 0);
        ClassInstance classInstance = new ClassInstance();
        classInstance.setMethods(List.of(m));
        classInstance.setStatements(Collections.emptyList());
        classInstance.setLinesOfCode(10);

        ComplexityMetricsDTO dto = service.calculateComplexityMetrics(classInstance);
        assertEquals(1, dto.getApproxMcCabeCC());
        assertEquals(0.1, dto.getImprovedCC());
    }

    @Test
    void testMultipleMethodsWithStatements() {
        MethodInstance m1 = method(3, 1, 2, 1); // 3 stmts, 1 control, 2 in, 1 out
        MethodInstance m2 = method(2, 0, 1, 0); // 2 stmts, 0 control, 1 in, 0 out
        ClassInstance classInstance = new ClassInstance();
        classInstance.setMethods(List.of(m1, m2));
        classInstance.setStatements(Collections.nCopies(5, new StatementInstance()));
        classInstance.setLinesOfCode(20);

        ComplexityMetricsDTO dto = service.calculateComplexityMetrics(classInstance);
        assertEquals(3, dto.getApproxMcCabeCC());
        assertEquals(0.5, dto.getImprovedCC());
    }

    @Test
    void testMethodWithOnlyControlStatements() {
        MethodInstance m = method(0, 3, 0, 0); // 0 stmts, 3 control stmts
        ClassInstance classInstance = new ClassInstance();
        classInstance.setMethods(List.of(m));
        classInstance.setStatements(Collections.nCopies(3, new StatementInstance()));
        classInstance.setLinesOfCode(15);

        ComplexityMetricsDTO dto = service.calculateComplexityMetrics(classInstance);
        assertEquals(4, dto.getApproxMcCabeCC());
        // improvedCC = (1+3+0+0)/15 = 0.266...
        assertEquals((1+3+0+0)/15.0, dto.getImprovedCC());
    }

    @Test
    void testMethodWithInputsAndOutputs() {
        MethodInstance m = method(1, 0, 4, 2); // 1 stmt, 0 control, 4 in, 2 out
        ClassInstance classInstance = new ClassInstance();
        classInstance.setMethods(List.of(m));
        classInstance.setStatements(Collections.nCopies(1, new StatementInstance()));
        classInstance.setLinesOfCode(7);

        ComplexityMetricsDTO dto = service.calculateComplexityMetrics(classInstance);
        assertEquals(1, dto.getApproxMcCabeCC());
        // improvedCC = (1+1+4+2)/7 = 8/7
        assertEquals(8.0/7.0, dto.getImprovedCC());
    }
}