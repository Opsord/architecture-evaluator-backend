// GenericUsageVisitor.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.generic_usage;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.parts.GenericUsageInstance;

import java.util.List;
import java.util.stream.Collectors;

public class GenericUsageVisitor extends VoidVisitorAdapter<List<GenericUsageInstance>> {

    @Override
    public void visit(ClassOrInterfaceType type, List<GenericUsageInstance> collector) {
        super.visit(type, collector);
        if (type.getTypeArguments().isPresent()) {
            GenericUsageInstance genericUsageInstance = new GenericUsageInstance();
            genericUsageInstance.setType(type.getNameAsString());
            genericUsageInstance.setGenericTypes(type.getTypeArguments().get().stream()
                    .map(Node::toString)
                    .collect(Collectors.toList()));
            collector.add(genericUsageInstance);
        }
    }
}