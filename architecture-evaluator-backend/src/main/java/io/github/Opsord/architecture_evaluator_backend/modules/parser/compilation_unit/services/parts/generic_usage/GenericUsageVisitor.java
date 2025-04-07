// GenericUsageVisitor.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.generic_usage;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.GenericUsageDTO;

import java.util.List;
import java.util.stream.Collectors;

public class GenericUsageVisitor extends VoidVisitorAdapter<List<GenericUsageDTO>> {

    @Override
    public void visit(ClassOrInterfaceType type, List<GenericUsageDTO> collector) {
        super.visit(type, collector);
        if (type.getTypeArguments().isPresent()) {
            GenericUsageDTO genericUsageDTO = new GenericUsageDTO();
            genericUsageDTO.setType(type.getNameAsString());
            genericUsageDTO.setGenericTypes(type.getTypeArguments().get().stream()
                    .map(Node::toString)
                    .collect(Collectors.toList()));
            collector.add(genericUsageDTO);
        }
    }
}