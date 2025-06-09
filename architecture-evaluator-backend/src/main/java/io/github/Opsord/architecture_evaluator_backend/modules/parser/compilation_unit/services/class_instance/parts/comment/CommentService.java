// CommentService.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.comment;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommentService {

    public List<String> getComments(ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        List<String> comments = new ArrayList<>();
        CommentVisitor visitor = new CommentVisitor();
        classOrInterfaceDeclaration.accept(visitor, comments);
        return comments;
    }
}