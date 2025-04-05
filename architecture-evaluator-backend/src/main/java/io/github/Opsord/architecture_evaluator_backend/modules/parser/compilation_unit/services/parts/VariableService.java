// VariableService.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.VariableDeclarator;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.VariableDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VariableService {

    private static final Logger logger = LoggerFactory.getLogger(VariableService.class);

    public List<VariableDTO> getVariables(CompilationUnit compilationUnit) {
        logger.info("Extracting variables from compilation unit");
        return compilationUnit.findAll(VariableDeclarator.class).stream()
                .map(variable -> {
                    VariableDTO variableDTO = new VariableDTO();
                    variableDTO.setName(variable.getNameAsString());
                    variableDTO.setType(variable.getType().asString());
                    return variableDTO;
                }).collect(Collectors.toList());
    }
}