// CommentService.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.comment;

import com.github.javaparser.ast.CompilationUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommentService {

    private static final Logger logger = LoggerFactory.getLogger(CommentService.class);

    public List<String> getComments(CompilationUnit compilationUnit) {
//        logger.info("Extracting comments from compilation unit");
        List<String> comments = new ArrayList<>();
        CommentVisitor visitor = new CommentVisitor();
        compilationUnit.accept(visitor, comments);
        return comments;
    }
}