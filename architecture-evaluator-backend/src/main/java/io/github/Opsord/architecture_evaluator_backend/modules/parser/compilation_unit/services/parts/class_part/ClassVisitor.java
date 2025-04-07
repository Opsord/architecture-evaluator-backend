// ClassVisitor.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.class_part;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.List;

public class ClassVisitor extends VoidVisitorAdapter<List<String>> {

    @Override
    public void visit(ClassOrInterfaceDeclaration declaration, List<String> collector) {
        super.visit(declaration, collector);
        if (!declaration.isInterface()) {
            collector.add(declaration.getNameAsString());
        }
    }
}