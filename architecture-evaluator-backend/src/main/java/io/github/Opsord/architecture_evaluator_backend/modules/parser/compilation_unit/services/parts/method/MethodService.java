// MethodService.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.method;

import com.github.javaparser.ast.CompilationUnit;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.MethodDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.statement.StatementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MethodService {

    private static final Logger logger = LoggerFactory.getLogger(MethodService.class);
    private final StatementService statementService;

    public MethodService(StatementService statementService) {
        this.statementService = statementService;
    }

    public List<MethodDTO> getMethods(CompilationUnit compilationUnit) {
        logger.info("Extracting methods from compilation unit");
        List<MethodDTO> methods = new ArrayList<>();
        MethodVisitor visitor = new MethodVisitor(statementService);
        compilationUnit.accept(visitor, methods);
        return methods;
    }
}