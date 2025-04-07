package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.variable;

import com.github.javaparser.ast.CompilationUnit;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.VariableDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class VariableService {

    private static final Logger logger = LoggerFactory.getLogger(VariableService.class);

    public List<VariableDTO> getVariables(CompilationUnit compilationUnit) {
        logger.info("Extracting variables from compilation unit");
        List<VariableDTO> variables = new ArrayList<>();
        VariableVisitor visitor = new VariableVisitor();
        compilationUnit.accept(visitor, variables);
        return variables;
    }
}