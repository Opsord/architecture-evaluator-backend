// ClassService.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.class_part;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClassService {

    private static final Logger logger = LoggerFactory.getLogger(ClassService.class);

    public List<String> getClassNames(CompilationUnit compilationUnit) {
//        logger.info("Extracting class names from compilation unit");
        List<String> classNames = new ArrayList<>();
        ClassVisitor visitor = new ClassVisitor();
        compilationUnit.accept(visitor, classNames);
        return classNames;
    }

    public List<String> getSuperClasses(CompilationUnit compilationUnit) {
//        logger.info("Extracting super classes from compilation unit");
        return compilationUnit.findAll(ClassOrInterfaceDeclaration.class).stream()
                .filter(c -> !c.getExtendedTypes().isEmpty())
                .map(c -> c.getExtendedTypes().get(0).getNameAsString())
                .collect(Collectors.toList());
    }
}