package io.github.opsord.architecture_evaluator_backend.modules.compilation_unit.class_instance;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.ClassInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.ClassService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.annotation.AnnotationService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.constructor.ConstructorService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.interface_instance.InterfaceService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.method.MethodService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.statement.StatementService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.variable.VariableService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClassServiceTest {
    private ClassService classService;

    @BeforeEach
    void setUp() {
        StatementService statementService = new StatementService();
        MethodService methodService = new MethodService(statementService);
        VariableService variableService = new VariableService();
        ConstructorService constructorService = new ConstructorService();
        AnnotationService annotationService = new AnnotationService();
        InterfaceService interfaceService = new InterfaceService();

        classService = new ClassService(
                methodService,
                statementService,
                variableService,
                constructorService,
                annotationService,
                interfaceService
        );
    }

    @Test
    void testGetClassesFromCompUnit_withSample01() throws Exception {
        File file = new File("src/test/java/io/github/opsord/architecture_evaluator_backend/test_codes/Sample01.java");
        String code = Files.readString(file.toPath());
        JavaParser javaParser = new JavaParser();
        CompilationUnit cu = javaParser.parse(code).getResult().orElseThrow(() -> new RuntimeException("Parsing failed"));

        List<ClassInstance> classes = classService.getClassesFromCompUnit(cu);

        assertEquals(1, classes.size());
        ClassInstance instance = classes.get(0);
        assertEquals("Sample01", instance.getName());
        assertEquals(2, instance.getMethods().size()); // foo + main
        assertTrue(instance.getLinesOfCode() > 0);
    }
}
