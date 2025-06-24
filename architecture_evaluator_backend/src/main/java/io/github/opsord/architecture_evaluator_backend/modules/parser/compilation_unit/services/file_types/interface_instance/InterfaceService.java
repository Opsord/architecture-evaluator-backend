package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_types.interface_instance;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.file_types.InterfaceInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_types.annotation_instance.AnnotationService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.method.MethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InterfaceService {

    private final AnnotationService annotationService;
    private final MethodService methodService;

    public List<String> getInterfacesFromClass(ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        // If you want to keep this for implemented interfaces, leave as is or refactor as needed
        return classOrInterfaceDeclaration.getImplementedTypes().stream()
                .map(NodeWithSimpleName::getNameAsString)
                .toList();
    }

    public List<InterfaceInstance> getInterfacesFromFile(CompilationUnit compilationUnit) {
        List<InterfaceInstance> interfaces = new ArrayList<>();
        InterfaceVisitor visitor = new InterfaceVisitor(annotationService, methodService);
        compilationUnit.accept(visitor, interfaces);
        return interfaces;
    }
}