package io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.controllers;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.CustomCompilationUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.CompilationUnitService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.DetailedMethodDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.services.DetailingService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

@RestController
@RequestMapping("/api/parser-detailed")
@RequiredArgsConstructor
public class DetailingController {

    private static final Logger logger = LoggerFactory.getLogger(DetailingController.class);
    private final CompilationUnitService compilationUnitService;
    private final DetailingService detailingService;

    @PostMapping("/parse")
    public List<DetailedMethodDTO> parseJavaFile(@RequestParam String filePath) throws FileNotFoundException {
        logger.info("Received request to parse with details file: {}", filePath);
        File file = new File(filePath);
        CustomCompilationUnitDTO customCompilationUnitDTO = compilationUnitService.parseJavaFile(file);
        logger.info("Returning parsed AST for file: {}", filePath);
        List<DetailedMethodDTO> detailedMethods = detailingService.generateDetailedMethods(customCompilationUnitDTO);
        logger.info("Returning detailed methods for file: {}", filePath);
        return detailedMethods;
    }
}
