package io.github.opsord.architecture_evaluator_backend.modules.processor.parts;

import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.ClassInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.parts.ParameterInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.parts.method.MethodInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.statement.StatementService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.processor.dto.parts.ProgramMetricsDTO;
import io.github.opsord.architecture_evaluator_backend.modules.parser.processor.services.parts.ProgramMetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProgramMetricsServiceTest {

    private StatementService statementService;
    private ProgramMetricsService service;

    @BeforeEach
    void setUp() {
        statementService = mock(StatementService.class);
        service = new ProgramMetricsService(statementService);
    }

    @Test
    void testGenerateProgramMetrics_basic() {
        // Method 1: 2 input, 1 output
        MethodInstance m1 = new MethodInstance();
        m1.setInputParameters(List.of(new ParameterInstance(), new ParameterInstance()));
        m1.setOutputParameters(List.of(new ParameterInstance()));

        // Method 2: 1 input, 3 output
        MethodInstance m2 = new MethodInstance();
        m2.setInputParameters(List.of(new ParameterInstance()));
        m2.setOutputParameters(List.of(new ParameterInstance(), new ParameterInstance(), new ParameterInstance()));

        ClassInstance c = new ClassInstance();
        c.setMethods(List.of(m1, m2));
        c.setStatements(Collections.emptyList());
        c.setLinesOfCode(123);

        when(statementService.countExecutableStatements(anyList())).thenReturn(7);

        ProgramMetricsDTO dto = service.generateProgramMetrics(c);

        assertEquals(2, dto.getNumberOfMethods());
        assertEquals(7, dto.getSumOfExecutableStatements());
        assertEquals(2, dto.getMaxInputParameters());
        assertEquals(3, dto.getMaxOutputParameters());
        assertEquals(123, dto.getLinesOfCode());
    }

    @Test
    void testGenerateProgramMetrics_emptyMethods() {
        ClassInstance c = new ClassInstance();
        c.setMethods(Collections.emptyList());
        c.setStatements(Collections.emptyList());
        c.setLinesOfCode(0);

        when(statementService.countExecutableStatements(anyList())).thenReturn(0);

        ProgramMetricsDTO dto = service.generateProgramMetrics(c);

        assertEquals(0, dto.getNumberOfMethods());
        assertEquals(0, dto.getSumOfExecutableStatements());
        assertEquals(0, dto.getMaxInputParameters());
        assertEquals(0, dto.getMaxOutputParameters());
        assertEquals(0, dto.getLinesOfCode());
    }

    @Test
    void testGenerateProgramMetrics_nullInputOutputParameters() {
        MethodInstance m = new MethodInstance();
        m.setInputParameters(null);
        m.setOutputParameters(null);

        ClassInstance c = new ClassInstance();
        c.setMethods(List.of(m));
        c.setStatements(Collections.emptyList());
        c.setLinesOfCode(10);

        when(statementService.countExecutableStatements(anyList())).thenReturn(2);

        ProgramMetricsDTO dto = service.generateProgramMetrics(c);

        assertEquals(1, dto.getNumberOfMethods());
        assertEquals(2, dto.getSumOfExecutableStatements());
        assertEquals(0, dto.getMaxInputParameters());
        assertEquals(0, dto.getMaxOutputParameters());
        assertEquals(10, dto.getLinesOfCode());
    }
}