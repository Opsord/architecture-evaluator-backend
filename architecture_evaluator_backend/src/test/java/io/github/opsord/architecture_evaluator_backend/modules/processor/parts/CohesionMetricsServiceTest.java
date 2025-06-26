package io.github.opsord.architecture_evaluator_backend.modules.processor.parts;

import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.ClassInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.parts.VariableInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.parts.method.MethodInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.processor.dto.parts.CohesionMetricsDTO;
import io.github.opsord.architecture_evaluator_backend.modules.parser.processor.services.parts.CohesionMetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class CohesionMetricsServiceTest {

    private CohesionMetricsService service;

    @BeforeEach
    void setUp() {
        service = new CohesionMetricsService();
    }

    private VariableInstance variableInstanceTest(String name) {
        VariableInstance v = new VariableInstance();
        v.setName(name);
        v.setScope("instance");
        return v;
    }

    private MethodInstance method(String name, VariableInstance... usedVars) {
        MethodInstance m = new MethodInstance();
        m.setName(name);
        m.setMethodVariables(Arrays.asList(usedVars));
        return m;
    }

    private ClassInstance classInstance(List<VariableInstance> vars, List<MethodInstance> methods) {
        ClassInstance c = new ClassInstance();
        c.setClassVariables(vars);
        c.setMethods(methods);
        return c;
    }

    @Test
    void testEmptyClass() {
        ClassInstance c = classInstance(Collections.emptyList(), Collections.emptyList());
        CohesionMetricsDTO dto = service.calculateCohesionMetrics(c);
        assertEquals(0, dto.getLackOfCohesion1());
        assertEquals(0.0, dto.getLackOfCohesion2());
        assertEquals(0, dto.getLackOfCohesion3());
        assertEquals(0, dto.getLackOfCohesion4());
        assertEquals(0.0, dto.getLackOfCohesion5());
    }

    @Test
    void testSingleMethodNoVars() {
        MethodInstance m1 = method("m1");
        ClassInstance c = classInstance(Collections.emptyList(), List.of(m1));
        CohesionMetricsDTO dto = service.calculateCohesionMetrics(c);
        assertEquals(0, dto.getLackOfCohesion1());
        assertEquals(0.0, dto.getLackOfCohesion2());
        assertEquals(0, dto.getLackOfCohesion3());
        assertEquals(0, dto.getLackOfCohesion4());
        assertEquals(0.0, dto.getLackOfCohesion5());
    }

    @Test
    void testTwoMethodsNoSharedVars() {
        VariableInstance v1 = variableInstanceTest("a");
        VariableInstance v2 = variableInstanceTest("b");
        MethodInstance m1 = method("m1", v1);
        MethodInstance m2 = method("m2", v2);
        ClassInstance c = classInstance(List.of(v1, v2), List.of(m1, m2));
        CohesionMetricsDTO dto = service.calculateCohesionMetrics(c);
        assertTrue(dto.getLackOfCohesion1() > 0);
        assertTrue(dto.getLackOfCohesion2() > 0.0);
        assertTrue(dto.getLackOfCohesion3() > 0);
        assertTrue(dto.getLackOfCohesion4() >= 0);
        assertTrue(dto.getLackOfCohesion5() > 0.0);
    }

    @Test
    void testTwoMethodsSharedVar() {
        VariableInstance v1 = variableInstanceTest("a");
        MethodInstance m1 = method("m1", v1);
        MethodInstance m2 = method("m2", v1);
        ClassInstance c = classInstance(List.of(v1), List.of(m1, m2));
        CohesionMetricsDTO dto = service.calculateCohesionMetrics(c);
        assertEquals(0, dto.getLackOfCohesion1());
        assertEquals(0.0, dto.getLackOfCohesion2());
        assertEquals(0, dto.getLackOfCohesion3());
        assertEquals(0, dto.getLackOfCohesion4());
        assertEquals(0.0, dto.getLackOfCohesion5());
    }

    @Test
    void testThreeMethodsPartialSharing() {
        VariableInstance v1 = variableInstanceTest("a");
        VariableInstance v2 = variableInstanceTest("b");
        MethodInstance m1 = method("m1", v1);
        MethodInstance m2 = method("m2", v1, v2);
        MethodInstance m3 = method("m3", v2);
        ClassInstance c = classInstance(List.of(v1, v2), List.of(m1, m2, m3));
        CohesionMetricsDTO dto = service.calculateCohesionMetrics(c);
        assertTrue(dto.getLackOfCohesion1() >= 0);
        assertTrue(dto.getLackOfCohesion2() >= 0.0);
        assertTrue(dto.getLackOfCohesion3() >= 0);
        assertTrue(dto.getLackOfCohesion4() >= 0);
        assertTrue(dto.getLackOfCohesion5() >= 0.0);
    }

    @Test
    void testNoInstanceVars() {
        MethodInstance m1 = method("m1");
        MethodInstance m2 = method("m2");
        ClassInstance c = classInstance(Collections.emptyList(), List.of(m1, m2));
        CohesionMetricsDTO dto = service.calculateCohesionMetrics(c);
        assertEquals(1, dto.getLackOfCohesion1());
        assertEquals(0.0, dto.getLackOfCohesion2());
        assertEquals(1, dto.getLackOfCohesion3());
        assertEquals(1, dto.getLackOfCohesion4());
        assertEquals(0.0, dto.getLackOfCohesion5());
    }
}