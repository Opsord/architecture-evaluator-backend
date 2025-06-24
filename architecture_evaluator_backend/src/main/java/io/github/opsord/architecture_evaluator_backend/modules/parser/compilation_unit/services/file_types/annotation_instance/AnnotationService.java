package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_types.annotation_instance;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
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

    public List<String> getAnnotationsFromCompUnit(CompilationUnit compilationUnit) {
        List<String> annotations = new ArrayList<>();
        AnnotationVisitor visitor = new AnnotationVisitor();
        compilationUnit.accept(visitor, annotations);
        return annotations;
    }

    public List<String> getAnnotationsFromEnum(EnumDeclaration enumDeclaration) {
        List<String> annotations = new ArrayList<>();
        AnnotationVisitor visitor = new AnnotationVisitor();
        enumDeclaration.accept(visitor, annotations);
        return annotations;
    }

    public List<String> getAnnotationsFromAnnotation(AnnotationDeclaration annotationDeclaration) {
        List<String> annotations = new ArrayList<>();
        AnnotationVisitor visitor = new AnnotationVisitor();
        annotationDeclaration.accept(visitor, annotations);
        return annotations;
    }
}