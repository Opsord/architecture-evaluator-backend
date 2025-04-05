package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.Comment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private static final Logger logger = LoggerFactory.getLogger(CommentService.class);

    public List<String> getComments(CompilationUnit compilationUnit) {
        logger.info("Extracting comments from compilation unit");
        return compilationUnit.getAllContainedComments().stream()
                .map(Comment::getContent)
                .collect(Collectors.toList());
    }
}