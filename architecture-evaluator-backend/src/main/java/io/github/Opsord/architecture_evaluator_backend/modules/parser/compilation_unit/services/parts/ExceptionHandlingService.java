// ExceptionHandlingService.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.stmt.TryStmt;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.ExceptionHandlingDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExceptionHandlingService {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlingService.class);

    public List<ExceptionHandlingDTO> getExceptionHandling(CompilationUnit compilationUnit) {
        logger.info("Extracting exception handling from compilation unit");
        List<ExceptionHandlingDTO> exceptionHandling = new ArrayList<>();
        for (TryStmt tryStmt : compilationUnit.findAll(TryStmt.class)) {
            ExceptionHandlingDTO exceptionHandlingDTO = new ExceptionHandlingDTO();
            exceptionHandlingDTO.setTryBlock(tryStmt.getTryBlock().toString());
            exceptionHandlingDTO.setCatchBlocks(tryStmt.getCatchClauses().stream()
                    .map(Node::toString)
                    .collect(Collectors.toList()));
            exceptionHandlingDTO.setFinallyBlock(tryStmt.getFinallyBlock().map(finallyBlock -> finallyBlock.toString()).orElse(null));
            exceptionHandling.add(exceptionHandlingDTO);
        }
        return exceptionHandling;
    }
}