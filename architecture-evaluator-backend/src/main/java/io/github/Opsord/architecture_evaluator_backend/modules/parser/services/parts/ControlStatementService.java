// ControlStatementService.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.services.parts;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.dto.parts.ControlStatementDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ControlStatementService {

    private static final Logger logger = LoggerFactory.getLogger(ControlStatementService.class);

    public List<ControlStatementDTO> getControlStatements(CompilationUnit compilationUnit) {
        logger.info("Extracting control statements from compilation unit");
        List<ControlStatementDTO> controlStatements = new ArrayList<>();

        for (IfStmt ifStmt : compilationUnit.findAll(IfStmt.class)) {
            ControlStatementDTO controlStatementDTO = new ControlStatementDTO();
            controlStatementDTO.setType("if");
            controlStatementDTO.setStructure(ifStmt.toString());
            controlStatements.add(controlStatementDTO);
        }

        for (ForStmt forStmt : compilationUnit.findAll(ForStmt.class)) {
            ControlStatementDTO controlStatementDTO = new ControlStatementDTO();
            controlStatementDTO.setType("for");
            controlStatementDTO.setStructure(forStmt.toString());
            controlStatements.add(controlStatementDTO);
        }

        for (WhileStmt whileStmt : compilationUnit.findAll(WhileStmt.class)) {
            ControlStatementDTO controlStatementDTO = new ControlStatementDTO();
            controlStatementDTO.setType("while");
            controlStatementDTO.setStructure(whileStmt.toString());
            controlStatements.add(controlStatementDTO);
        }

        return controlStatements;
    }
}