// PackageVisitor.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_instance.package_part;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.List;

public class PackageVisitor extends VoidVisitorAdapter<List<String>> {

    @Override
    public void visit(ImportDeclaration importDeclaration, List<String> collector) {
        super.visit(importDeclaration, collector);
        collector.add(importDeclaration.getName().asString());
    }
}