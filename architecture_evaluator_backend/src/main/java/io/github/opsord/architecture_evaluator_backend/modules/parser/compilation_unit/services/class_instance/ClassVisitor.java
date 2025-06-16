package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.ClassInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.parts.JavaFileType;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.parts.LayerAnnotation;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.annotation.AnnotationService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.constructor.ConstructorService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.interface_instance.InterfaceService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.method.MethodService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.statement.StatementService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.variable.VariableService;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class ClassVisitor extends VoidVisitorAdapter<List<ClassInstance>> {

    private final MethodService methodService;
    private final StatementService statementService;
    private final VariableService variableService;
    private final ConstructorService constructorService;
    private final AnnotationService annotationService;
    private final InterfaceService interfaceService;

    private static final List<String> ENTITY_ANNOTATIONS = List.of("Entity");
    private static final List<String> DOCUMENT_ANNOTATIONS = List.of("Document");
    private static final List<String> SERVICE_ANNOTATIONS = List.of("Service");
    private static final List<String> REPOSITORY_ANNOTATIONS = List.of("Repository");
    private static final List<String> CONTROLLER_ANNOTATIONS = List.of("Controller", "RestController");
    private static final List<String> TESTING_ANNOTATIONS = List.of("SpringBootTest", "Test");

    /**
     * Visits a class or interface declaration and populates a ClassInstance with its details.
     * @param declaration The class or interface declaration node.
     * @param collector The list to collect ClassInstance objects.
     */
    @Override
    public void visit(ClassOrInterfaceDeclaration declaration, List<ClassInstance> collector) {
        super.visit(declaration, collector);
        ClassInstance instance = new ClassInstance();
        instance.setName(declaration.getNameAsString());

        setJavaFileType(declaration, instance);
        setAnnotationsAndLayer(declaration, instance);
        setInheritance(declaration, instance);
        setMembers(declaration, instance);
        setInnerClasses(declaration, instance);
        instance.setUsedClasses(collectUsedClasses(declaration));
        setLinesOfCode(declaration, instance);

        collector.add(instance);
    }

    /**
     * Sets the Java file type (class, interface, enum, etc.) for the given instance.
     * @param declaration The class or interface declaration node.
     * @param instance The ClassInstance to update.
     */
    private void setJavaFileType(ClassOrInterfaceDeclaration declaration, ClassInstance instance) {
        if (declaration.isInterface()) {
            instance.setJavaFileType(JavaFileType.INTERFACE);
        } else if (declaration.isEnumDeclaration()) {
            instance.setJavaFileType(JavaFileType.ENUM);
        } else if (declaration.isAnnotationDeclaration()) {
            instance.setJavaFileType(JavaFileType.ANNOTATION);
        } else if (declaration.isRecordDeclaration()) {
            instance.setJavaFileType(JavaFileType.RECORD);
        } else if (declaration.isAbstract()) {
            instance.setJavaFileType(JavaFileType.ABSTRACT_CLASS);
        } else {
            instance.setJavaFileType(JavaFileType.CLASS);
        }
    }

    /**
     * Extracts annotations from the class and determines its layer annotation.
     * @param declaration The class or interface declaration node.
     * @param instance The ClassInstance to update.
     */
    private void setAnnotationsAndLayer(ClassOrInterfaceDeclaration declaration, ClassInstance instance) {
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
     * Sets the superclasses and implemented interfaces for the given instance.
     * @param declaration The class or interface declaration node.
     * @param instance The ClassInstance to update.
     */
    private void setInheritance(ClassOrInterfaceDeclaration declaration, ClassInstance instance) {
        instance.setSuperClasses(
                declaration.getExtendedTypes().stream()
                        .map(NodeWithSimpleName::getNameAsString)
                        .toList());
        instance.setImplementedInterfaces(
                interfaceService.getImplementedInterfaces(declaration));
    }

    /**
     * Sets the members (methods, statements, variables, constructors) for the given instance.
     * @param declaration The class or interface declaration node.
     * @param instance The ClassInstance to update.
     */
    private void setMembers(ClassOrInterfaceDeclaration declaration, ClassInstance instance) {
        instance.setMethods(methodService.getMethods(declaration));
        instance.setStatements(statementService.getStatements(declaration));
        instance.setClassVariables(variableService.getVariables(declaration));
        instance.setConstructors(constructorService.getConstructors(declaration));
    }

    /**
     * Recursively collects and sets inner classes for the given instance.
     * @param declaration The class or interface declaration node.
     * @param instance The ClassInstance to update.
     */
    private void setInnerClasses(ClassOrInterfaceDeclaration declaration, ClassInstance instance) {
        List<ClassInstance> innerClasses = new ArrayList<>();
        declaration.getMembers().forEach(member -> {
            if (member instanceof ClassOrInterfaceDeclaration inner) {
                inner.accept(this, innerClasses);
            }
        });
        instance.setInnerClasses(innerClasses);
    }

    /**
     * Sets the lines of code metric for the given instance.
     * @param declaration The class or interface declaration node.
     * @param instance The ClassInstance to update.
     */
    private void setLinesOfCode(ClassOrInterfaceDeclaration declaration, ClassInstance instance) {
        int linesOfCode = declaration.getRange()
                .map(range -> range.end.line - range.begin.line + 1)
                .orElse(0);
        instance.setLinesOfCode(linesOfCode);
    }

    /**
     * Collects all class names used in the declaration (superclasses, interfaces, fields, methods, constructors).
     * @param declaration The class or interface declaration node.
     * @return List of used class names.
     */
    private List<String> collectUsedClasses(ClassOrInterfaceDeclaration declaration) {
        Set<String> usedClasses = new HashSet<>();

        // Superclasses and interfaces
        declaration.getExtendedTypes().forEach(type -> usedClasses.addAll(extractClassNames(type.getNameAsString())));
        declaration.getImplementedTypes()
                .forEach(type -> usedClasses.addAll(extractClassNames(type.getNameAsString())));

        // Field types
        declaration.getFields().forEach(field -> {
            String elementType = field.getElementType().asString();
            usedClasses.addAll(extractClassNames(elementType));
        });

        // Method return types and parameter types
        declaration.getMethods().forEach(method -> {
            usedClasses.addAll(extractClassNames(method.getType().asString()));
            method.getParameters().forEach(param -> usedClasses.addAll(extractClassNames(param.getType().asString())));
        });

        // Constructor parameter types
        declaration.getConstructors().forEach(constructor -> constructor.getParameters()
                .forEach(param -> usedClasses.addAll(extractClassNames(param.getType().asString()))));

        return new ArrayList<>(usedClasses);
    }

    /**
     * Utility to extract class names from a type string, including generics.
     * For example, "List<InstallmentEntity>" returns ["List", "InstallmentEntity"].
     * @param type The type string.
     * @return List of class names.
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