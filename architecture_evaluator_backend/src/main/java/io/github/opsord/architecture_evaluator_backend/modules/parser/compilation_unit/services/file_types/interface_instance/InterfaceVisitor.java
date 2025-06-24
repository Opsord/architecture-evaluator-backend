// File: architecture_evaluator_backend/src/main/java/io/github/opsord/architecture_evaluator_backend/modules/parser/compilation_unit/services/file_types/interface_instance/InterfaceVisitor.java
package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_types.interface_instance;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.file_types.InterfaceInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.parts.LayerAnnotation;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_types.annotation_instance.AnnotationService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.method.MethodService;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class InterfaceVisitor extends VoidVisitorAdapter<List<InterfaceInstance>> {

    private final AnnotationService annotationService;
    private final MethodService methodService;

    private static final List<String> ENTITY_ANNOTATIONS = List.of("Entity");
    private static final List<String> DOCUMENT_ANNOTATIONS = List.of("Document");
    private static final List<String> SERVICE_ANNOTATIONS = List.of("Service");
    private static final List<String> REPOSITORY_ANNOTATIONS = List.of("Repository");
    private static final List<String> CONTROLLER_ANNOTATIONS = List.of("Controller", "RestController");
    private static final List<String> TESTING_ANNOTATIONS = List.of("SpringBootTest", "Test");

    @Override
    public void visit(ClassOrInterfaceDeclaration declaration, List<InterfaceInstance> collector) {
        if (declaration.isInterface()) {
            InterfaceInstance instance = new InterfaceInstance();
            instance.setName(declaration.getNameAsString());
            setAnnotationsAndLayer(declaration, instance);
            instance.setAnnotations(annotationService.getAnnotationsFromClass(declaration));
            instance.setExtendedInterfaces(
                    declaration.getExtendedTypes().stream()
                            .map(NodeWithSimpleName::getNameAsString)
                            .toList()
            );
            instance.setMethods(methodService.getMethods(declaration));
            instance.setUsedClasses(collectUsedClasses(declaration));
            instance.setLinesOfCode(
                    declaration.getRange().map(r -> r.end.line - r.begin.line + 1).orElse(0)
            );
            collector.add(instance);
        }
        super.visit(declaration, collector);
    }

    /**
     * Extracts annotations from the class and determines its layer annotation.
     * @param declaration The class or interface declaration node.
     * @param instance The ClassInstance to update.
     */
    private void setAnnotationsAndLayer(ClassOrInterfaceDeclaration declaration, InterfaceInstance instance) {
        instance.setAnnotations(annotationService.getAnnotationsFromClass(declaration));
        List<String> annotations = instance.getAnnotations();
        if (annotations != null) {
            if (annotations.stream().anyMatch(ENTITY_ANNOTATIONS::contains)) {
                instance.setLayerAnnotation(LayerAnnotation.ENTITY);
            } else if (annotations.stream().anyMatch(DOCUMENT_ANNOTATIONS::contains)) {
                instance.setLayerAnnotation(LayerAnnotation.DOCUMENT);
            } else if (annotations.stream().anyMatch(SERVICE_ANNOTATIONS::contains)) {
                instance.setLayerAnnotation(LayerAnnotation.SERVICE);
            } else if (annotations.stream().anyMatch(REPOSITORY_ANNOTATIONS::contains)) {
                instance.setLayerAnnotation(LayerAnnotation.REPOSITORY);
            } else if (annotations.stream().anyMatch(CONTROLLER_ANNOTATIONS::contains)) {
                instance.setLayerAnnotation(LayerAnnotation.CONTROLLER);
            } else if (annotations.stream().anyMatch(TESTING_ANNOTATIONS::contains)) {
                instance.setLayerAnnotation(LayerAnnotation.TESTING);
            } else {
                instance.setLayerAnnotation(LayerAnnotation.OTHER);
            }
        } else {
            instance.setLayerAnnotation(LayerAnnotation.UNKNOWN);
        }
    }

    /**
     * Collects all class names used in the interface (extended interfaces, method return/param types).
     */
    private List<String> collectUsedClasses(ClassOrInterfaceDeclaration declaration) {
        var usedClasses = new java.util.HashSet<String>();

        // Extended interfaces
        declaration.getExtendedTypes().forEach(type -> usedClasses.add(type.getNameAsString()));

        // Method return types and parameter types
        declaration.getMethods().forEach(method -> {
            usedClasses.addAll(extractClassNames(method.getType().asString()));
            method.getParameters().forEach(param -> usedClasses.addAll(extractClassNames(param.getType().asString())));
        });

        return new ArrayList<>(usedClasses);
    }

    /**
     * Utility to extract class names from a type string, including generics.
     */
    private List<String> extractClassNames(String type) {
        List<String> result = new ArrayList<>();
        if (type == null)
            return result;
        int genericStart = type.indexOf('<');
        if (genericStart > 0) {
            result.add(type.substring(0, genericStart));
            String inner = type.substring(genericStart + 1, type.lastIndexOf('>'));
            for (String part : inner.split(",")) {
                result.addAll(extractClassNames(part.trim()));
            }
        } else {
            result.add(type);
        }
        return result;
    }
}