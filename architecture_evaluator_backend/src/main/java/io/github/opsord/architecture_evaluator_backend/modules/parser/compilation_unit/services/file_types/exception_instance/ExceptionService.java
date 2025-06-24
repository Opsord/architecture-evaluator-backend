package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_types.exception_instance;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ExceptionService {

    public List<String> getExceptionsFromCompUnit(CompilationUnit compilationUnit) {
        List<String> exceptions = new ArrayList<>();
        ExceptionVisitor visitor = new ExceptionVisitor();
        compilationUnit.accept(visitor, exceptions);
        return exceptions;
    }

    public List<String> getExceptionsFromClass(ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        List<String> exceptions = new ArrayList<>();
        ExceptionVisitor visitor = new ExceptionVisitor();
        classOrInterfaceDeclaration.accept(visitor, exceptions);
        return exceptions;
    }
}