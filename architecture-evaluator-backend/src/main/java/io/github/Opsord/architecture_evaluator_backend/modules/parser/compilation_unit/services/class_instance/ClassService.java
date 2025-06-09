package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance;

import com.github.javaparser.ast.CompilationUnit;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.ClassInstance;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.annotation.AnnotationService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.constructor.ConstructorService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.interface_instance.InterfaceService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.method.MethodService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.statement.StatementService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.variable.VariableService;
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

    public List<ClassInstance> getClasses(CompilationUnit compilationUnit) {
        List<ClassInstance> classInstances = new ArrayList<>();
        ClassVisitor visitor = new ClassVisitor(
                methodService,
                statementService,
                variableService,
                constructorService,
                annotationService,
                interfaceService
                );
        compilationUnit.accept(visitor, classInstances);
        return classInstances;
    }

    public List<String> getExistingClassNames(List<ClassInstance> classInstances) {
        return classInstances.stream()
                .map(ClassInstance::getName)
                .toList();
    }

    public ClassInstance setDependencies(ClassInstance classInstance, List<ClassInstance> allClasses) {
        List<String> existingClassNames = getExistingClassNames(allClasses);

        List<String> filteredUsedClasses = classInstance.getUsedClasses().stream()
                .filter(existingClassNames::contains)
                .toList();

        classInstance.setUsedClasses(filteredUsedClasses);
        return classInstance;
    }
}