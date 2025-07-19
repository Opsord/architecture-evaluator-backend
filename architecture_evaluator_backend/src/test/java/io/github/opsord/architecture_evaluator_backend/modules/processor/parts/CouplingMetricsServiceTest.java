package io.github.opsord.architecture_evaluator_backend.modules.processor.parts;

import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.class_instance.ClassInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.processor.dto.parts.CouplingMetricsDTO;
import io.github.opsord.architecture_evaluator_backend.modules.api.processor.services.parts.CouplingMetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CouplingMetricsServiceTest {

    private CouplingMetricsService service;

    @BeforeEach
    void setUp() {
        service = new CouplingMetricsService();
    }

    @Test
    void testNoDependenciesOrDependents() {
        ClassInstance c = new ClassInstance();
        c.setClassDependencies(null);
        c.setDependentClasses(null);

        CouplingMetricsDTO dto = service.calculateCouplingMetrics(c);
        assertEquals(0, dto.getAfferentCoupling());
        assertEquals(0, dto.getEfferentCoupling());
        assertEquals(0.0, dto.getInstability());
    }

    @Test
    void testOnlyEfferentCoupling() {
        ClassInstance c = new ClassInstance();
        c.setClassDependencies(List.of("A", "B", "C"));
        c.setDependentClasses(Collections.emptyList());

        CouplingMetricsDTO dto = service.calculateCouplingMetrics(c);
        assertEquals(0, dto.getAfferentCoupling());
        assertEquals(3, dto.getEfferentCoupling());
        assertEquals(1.0, dto.getInstability());
    }

    @Test
    void testOnlyAfferentCoupling() {
        ClassInstance c = new ClassInstance();
        c.setClassDependencies(Collections.emptyList());
        c.setDependentClasses(List.of("X", "Y"));

        CouplingMetricsDTO dto = service.calculateCouplingMetrics(c);
        assertEquals(2, dto.getAfferentCoupling());
        assertEquals(0, dto.getEfferentCoupling());
        assertEquals(0.0, dto.getInstability());
    }

    @Test
    void testBothCouplings() {
        ClassInstance c = new ClassInstance();
        c.setClassDependencies(List.of("A", "B"));
        c.setDependentClasses(List.of("X", "Y", "Z"));

        CouplingMetricsDTO dto = service.calculateCouplingMetrics(c);
        assertEquals(3, dto.getAfferentCoupling());
        assertEquals(2, dto.getEfferentCoupling());
        assertEquals(2.0 / 5.0, dto.getInstability());
    }

    @Test
    void testEmptyLists() {
        ClassInstance c = new ClassInstance();
        c.setClassDependencies(Collections.emptyList());
        c.setDependentClasses(Collections.emptyList());

        CouplingMetricsDTO dto = service.calculateCouplingMetrics(c);
        assertEquals(0, dto.getAfferentCoupling());
        assertEquals(0, dto.getEfferentCoupling());
        assertEquals(0.0, dto.getInstability());
    }
}