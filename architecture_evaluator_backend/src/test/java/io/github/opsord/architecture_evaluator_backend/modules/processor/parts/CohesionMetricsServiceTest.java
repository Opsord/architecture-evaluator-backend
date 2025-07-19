package io.github.opsord.architecture_evaluator_backend.modules.processor.parts;

import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.class_instance.ClassInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.class_instance.parts.VariableInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.class_instance.parts.method.MethodInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.processor.dto.parts.CohesionMetricsDTO;
import io.github.opsord.architecture_evaluator_backend.modules.api.processor.services.parts.CohesionMetricsService;
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
        assertEquals(0, dto.getLackOfCohesion2());
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
        assertEquals(0, dto.getLackOfCohesion2());
        assertEquals(1, dto.getLackOfCohesion3());
        assertEquals(1, dto.getLackOfCohesion4());
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
        assertEquals(1, dto.getLackOfCohesion1()); // 2 methods => 1 pair => no shared var
        assertEquals(1, dto.getLackOfCohesion2()); // 1 disconnected - 0 connected
        assertEquals(2, dto.getLackOfCohesion3()); // 2 isolated nodes
        assertEquals(2, dto.getLackOfCohesion4()); // no calls => same as LCOM3
        assertEquals(0.5, dto.getLackOfCohesion5()); // (1 + 1) / (2*2) = 0.5 => 1 - 0.5 = 0.5
    }

    @Test
    void testTwoMethodsSharedVar() {
        VariableInstance v1 = variableInstanceTest("a");
        MethodInstance m1 = method("m1", v1);
        MethodInstance m2 = method("m2", v1);
        ClassInstance c = classInstance(List.of(v1), List.of(m1, m2));
        CohesionMetricsDTO dto = service.calculateCohesionMetrics(c);
        assertEquals(0, dto.getLackOfCohesion1()); // shared var
        assertEquals(0, dto.getLackOfCohesion2()); // 0 disconnected - 1 connected => 0
        assertEquals(1, dto.getLackOfCohesion3()); // one connected component
        assertEquals(1, dto.getLackOfCohesion4()); // no calls, still one part
        assertEquals(0.0, dto.getLackOfCohesion5()); // both methods access the same var => full coverage
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
        assertEquals(1, dto.getLackOfCohesion1()); // all pairs connected through shared vars
        assertEquals(0, dto.getLackOfCohesion2()); // 0 disconnected - 3 connected = 0
        assertEquals(1, dto.getLackOfCohesion3()); // full-connected graph
        assertEquals(1, dto.getLackOfCohesion4()); // no method calls, same as LCOM3
        assertEquals(0.333, dto.getLackOfCohesion5(), 0.001); // (1+2+1) / 6 = 0.666 => 1 - 0.666 = 0.333
    }

    @Test
    void testNoInstanceVars() {
        MethodInstance m1 = method("m1");
        MethodInstance m2 = method("m2");
        ClassInstance c = classInstance(Collections.emptyList(), List.of(m1, m2));
        CohesionMetricsDTO dto = service.calculateCohesionMetrics(c);
        assertEquals(1, dto.getLackOfCohesion1()); // no shared vars
        assertEquals(1, dto.getLackOfCohesion2()); // 1 disconnected - 0 connected
        assertEquals(2, dto.getLackOfCohesion3()); // 2 components
        assertEquals(2, dto.getLackOfCohesion4()); // no calls
        assertEquals(0.0, dto.getLackOfCohesion5()); // no attrs => return 0
    }
}
