package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.ClassInstance;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.parts.JavaFileType;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.annotation.AnnotationService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.constructor.ConstructorService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.interface_instance.InterfaceService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.method.MethodService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.statement.StatementService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.variable.VariableService;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ClassVisitor extends VoidVisitorAdapter<List<ClassInstance>> {

    private final MethodService methodService;
    private final StatementService statementService;
    private final VariableService variableService;
    private final ConstructorService constructorService;
    private final AnnotationService annotationService;
    private final InterfaceService interfaceService;

    @Override
    public void visit(ClassOrInterfaceDeclaration declaration, List<ClassInstance> collector) {
        super.visit(declaration, collector);

        ClassInstance instance = new ClassInstance();
        instance.setName(declaration.getNameAsString());

        // Set JavaFileType
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

        // Annotations
        instance.setAnnotations(annotationService.getAnnotationsFromClass(declaration));
        // Superclasses
        instance.setSuperClasses(
                declaration.getExtendedTypes().stream()
                        .map(NodeWithSimpleName::getNameAsString)
                        .collect(Collectors.toList())
        );
        // Implemented interfaces
        instance.setImplementedInterfaces(
                interfaceService.getImplementedInterfaces(declaration)
        );
        // Methods
        instance.setMethods(methodService.getMethods(declaration));
        // Statements
        instance.setStatements(statementService.getStatements(declaration));
        // Variables
        instance.setClassVariables(variableService.getVariables(declaration));
        // Constructors
        instance.setConstructors(constructorService.getConstructors(declaration));
        // Inner classes
        List<ClassInstance> innerClasses = new ArrayList<>();
        declaration.getMembers().forEach(member -> {
            if (member instanceof ClassOrInterfaceDeclaration inner) {
                inner.accept(this, innerClasses);
            }
        });
        instance.setInnerClasses(innerClasses);

        // Used classes
        instance.setUsedClasses(collectUsedClasses(declaration));

        collector.add(instance);
    }

    private List<String> collectUsedClasses(ClassOrInterfaceDeclaration declaration) {
        Set<String> usedClasses = new HashSet<>();

        // Superclasses and interfaces
        declaration.getExtendedTypes().forEach(type -> usedClasses.add(type.getNameAsString()));
        declaration.getImplementedTypes().forEach(type -> usedClasses.add(type.getNameAsString()));

        // Field types
        declaration.getFields().forEach(field -> {
            String elementType = field.getElementType().asString();
            usedClasses.add(elementType);
        });

        // Method return types and parameter types
        declaration.getMethods().forEach(method -> {
            usedClasses.add(method.getType().asString());
            method.getParameters().forEach(param -> usedClasses.add(param.getType().asString()));
        });

        // Constructor parameter types
        declaration.getConstructors().forEach(constructor ->
                constructor.getParameters().forEach(param -> usedClasses.add(param.getType().asString()))
        );

        // Convert Set to List for the result
        return new ArrayList<>(usedClasses);
    }
}