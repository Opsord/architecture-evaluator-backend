package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_types.abstract_class_instance;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AbstractClassService {

    public List<String> getAbstractClassesFromCompUnit(CompilationUnit compilationUnit) {
        List<String> abstractClasses = new ArrayList<>();
        AbstractClassVisitor visitor = new AbstractClassVisitor();
        compilationUnit.accept(visitor, abstractClasses);
        return abstractClasses;
    }

    public List<String> getAbstractClassesFromClass(ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        List<String> abstractClasses = new ArrayList<>();
        AbstractClassVisitor visitor = new AbstractClassVisitor();
        classOrInterfaceDeclaration.accept(visitor, abstractClasses);
        return abstractClasses;
    }
}