// ExceptionHandlerService.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.exception_handler;

import com.github.javaparser.ast.CompilationUnit;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.ExceptionHandlingDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ExceptionHandlerService {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlerService.class);

    public List<ExceptionHandlingDTO> getExceptionHandling(CompilationUnit compilationUnit) {
//        logger.info("Extracting exception handling from compilation unit");
        List<ExceptionHandlingDTO> exceptionHandling = new ArrayList<>();
        ExceptionHandlerVisitor visitor = new ExceptionHandlerVisitor();
        compilationUnit.accept(visitor, exceptionHandling);
        return exceptionHandling;
    }
}