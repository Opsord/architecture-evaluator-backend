// JavaParserController.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.controllers;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.CustomCompilationUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.CompilationUnitService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileNotFoundException;

@RestController
@RequestMapping("/api/parser")
@RequiredArgsConstructor
public class CompilationUnitController {

    private static final Logger logger = LoggerFactory.getLogger(CompilationUnitController.class);
    private final CompilationUnitService compilationUnitService;

    @PostMapping("/parse")
    public CustomCompilationUnitDTO parseJavaFile(@RequestParam String filePath) throws FileNotFoundException {
        logger.info("Received request to parse file: {}", filePath);
        File file = new File(filePath);
        CustomCompilationUnitDTO customCompilationUnitDTO = compilationUnitService.parseJavaFile(file);
        logger.info("Returning parsed AST for file: {}", filePath);
        return customCompilationUnitDTO;
    }
}