package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.constructor;

import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.parts.ConstructorInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.parts.ParameterInstance;

import java.util.List;

public class ConstructorVisitor extends VoidVisitorAdapter<List<ConstructorInstance>> {

    @Override
    public void visit(ConstructorDeclaration constructor, List<ConstructorInstance> collector) {
        super.visit(constructor, collector);

        ConstructorInstance ci = new ConstructorInstance();
        ci.setName(constructor.getNameAsString());
        ci.setParameters(
                constructor.getParameters().stream().map(p -> {
                    ParameterInstance param = new ParameterInstance();
                    param.setName(p.getNameAsString());
                    param.setType(p.getType().asString());
                    param.setAnnotations(
                            p.getAnnotations().stream().map(NodeWithName::getNameAsString)
                                    .toList());
                    return param;
                }).toList());
        ci.setAnnotations(
                constructor.getAnnotations().stream().map(NodeWithName::getNameAsString).toList());
        ci.setModifiers(constructor.getModifiers().stream().map(Object::toString).toList());
        ci.setThrownExceptions(
                constructor.getThrownExceptions().stream().map(Object::toString).toList());
        ci.setBody(constructor.getBody().toString());
        ci.setLineCount(constructor.getBody().getStatements().size());
        // Comments can be added if needed
        collector.add(ci);
    }
}