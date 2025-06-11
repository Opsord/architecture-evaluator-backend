// AnnotationService.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.annotation;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AnnotationService {

    public List<String> getAnnotationsFromClass(ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        List<String> annotations = new ArrayList<>();
        AnnotationVisitor visitor = new AnnotationVisitor();
        classOrInterfaceDeclaration.accept(visitor, annotations);
        return annotations;
    }

    public List<String> getAnnotationsFromFile(CompilationUnit compilationUnit) {
        List<String> annotations = new ArrayList<>();
        AnnotationVisitor visitor = new AnnotationVisitor();
        compilationUnit.accept(visitor, annotations);
        return annotations;
    }
}