package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_types.class_instance;

import com.github.javaparser.ast.CompilationUnit;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.file_types.ClassInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_types.annotation_instance.AnnotationService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.constructor.ConstructorService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.exception_handler.ExceptionHandlerService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_types.interface_instance.InterfaceService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.method.MethodService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.statement.StatementService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.variable.VariableService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassService {

    private final MethodService methodService;
    private final StatementService statementService;
    private final VariableService variableService;
    private final ConstructorService constructorService;
    private final AnnotationService annotationService;
    private final InterfaceService interfaceService;
    private final ExceptionHandlerService exceptionHandlerService;

    public List<ClassInstance> getClassesFromCompUnit(CompilationUnit compilationUnit) {
        List<ClassInstance> classInstances = new ArrayList<>();
        ClassVisitor visitor = new ClassVisitor(
                methodService,
                statementService,
                variableService,
                constructorService,
                annotationService,
                interfaceService,
                exceptionHandlerService);
        compilationUnit.accept(visitor, classInstances);
        return classInstances;
    }
}