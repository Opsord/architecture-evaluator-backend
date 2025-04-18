// CommentVisitor.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.comment;

import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.List;

public class CommentVisitor extends VoidVisitorAdapter<List<String>> {

    @Override
    public void visit(BlockComment comment, List<String> collector) {
        super.visit(comment, collector);
        collector.add(comment.getContent());
    }
}