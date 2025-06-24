package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_types.enum_instance;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.EnumDeclaration;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EnumService {

    public List<String> getEnumsFromFile(CompilationUnit compilationUnit) {
        List<String> enums = new ArrayList<>();
        EnumVisitor visitor = new EnumVisitor();
        compilationUnit.accept(visitor, enums);
        return enums;
    }

    public List<String> getEnumsFromEnum(EnumDeclaration enumDeclaration) {
        List<String> enums = new ArrayList<>();
        EnumVisitor visitor = new EnumVisitor();
        enumDeclaration.accept(visitor, enums);
        return enums;
    }
}