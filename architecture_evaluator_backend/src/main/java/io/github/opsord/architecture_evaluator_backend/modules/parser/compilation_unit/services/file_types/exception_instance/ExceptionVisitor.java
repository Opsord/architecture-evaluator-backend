package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_types.exception_instance;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.List;

public class ExceptionVisitor extends VoidVisitorAdapter<List<String>> {

    @Override
    public void visit(ClassOrInterfaceDeclaration declaration, List<String> collector) {
        if (!declaration.isInterface()) { // Fixed condition to check for classes
            declaration.getExtendedTypes().forEach(extendedType -> {
                String name = extendedType.getNameAsString();
                if (name.equals("Exception") || name.equals("RuntimeException")) {
                    collector.add(declaration.getNameAsString());
                }
            });
        }
        super.visit(declaration, collector);
    }
}