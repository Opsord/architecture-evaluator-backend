// InterfaceVisitor.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.interface_instance;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.List;
import java.util.Set;

public class InterfaceVisitor extends VoidVisitorAdapter<List<String>> {

    private static final Set<String> REPOSITORY_INTERFACES = Set.of(
            "JpaRepository", "CrudRepository", "PagingAndSortingRepository"
            // Add more repository interfaces as needed
    );

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