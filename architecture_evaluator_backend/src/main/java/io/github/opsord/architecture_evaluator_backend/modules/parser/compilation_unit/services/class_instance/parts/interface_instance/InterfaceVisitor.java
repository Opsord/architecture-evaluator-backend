// InterfaceVisitor.java
package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.interface_instance;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.List;

public class InterfaceVisitor extends VoidVisitorAdapter<List<String>> {

    @Override
    public void visit(ClassOrInterfaceDeclaration declaration, List<String> collector) {
        super.visit(declaration, collector);
        if (declaration.isInterface()) {
            collector.add(declaration.getNameAsString());
        } else {
            declaration.getImplementedTypes().stream()
                    .map(ClassOrInterfaceType::getNameAsString)
                    .forEach(collector::add);
        }
    }
}