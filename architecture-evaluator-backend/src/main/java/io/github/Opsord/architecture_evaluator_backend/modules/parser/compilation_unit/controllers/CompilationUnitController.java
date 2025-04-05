// JavaParserController.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.controllers;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.CompilationUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.CompilationUnitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileNotFoundException;

@RestController
@RequestMapping("/api/parser")
public class CompilationUnitController {

    private static final Logger logger = LoggerFactory.getLogger(CompilationUnitController.class);
    private final CompilationUnitService compilationUnitService;

    public CompilationUnitController(CompilationUnitService compilationUnitService) {
        this.compilationUnitService = compilationUnitService;
    }

    @GetMapping("/parse")
    public CompilationUnitDTO parseJavaFile(@RequestParam String filePath) throws FileNotFoundException {
        logger.info("Received request to parse file: {}", filePath);
        File file = new File(filePath);
        CompilationUnitDTO compilationUnitDTO = compilationUnitService.parseJavaFile(file);
        logger.info("Returning parsed AST for file: {}", filePath);
        return compilationUnitDTO;
    }
}