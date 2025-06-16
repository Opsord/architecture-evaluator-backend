package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance;

import com.github.javaparser.ast.CompilationUnit;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.ClassInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.annotation.AnnotationService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.constructor.ConstructorService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.interface_instance.InterfaceService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.method.MethodService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.statement.StatementService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.variable.VariableService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassService {

    private final MethodService methodService;
    private final StatementService statementService;
    private final VariableService variableService;
    private final ConstructorService constructorService;
    private final AnnotationService annotationService;
    private final InterfaceService interfaceService;

    public List<ClassInstance> getClassesFromCompUnit(CompilationUnit compilationUnit) {
        List<ClassInstance> classInstances = new ArrayList<>();
        ClassVisitor visitor = new ClassVisitor(
                methodService,
                statementService,
                variableService,
                constructorService,
                annotationService,
                interfaceService);
        compilationUnit.accept(visitor, classInstances);
        return classInstances;
    }
}