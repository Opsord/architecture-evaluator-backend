// MethodService.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.MethodDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.ParameterDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MethodService {

    private static final Logger logger = LoggerFactory.getLogger(MethodService.class);

    public List<MethodDTO> getMethods(CompilationUnit compilationUnit) {
        logger.info("Extracting methods from compilation unit");
        return compilationUnit.findAll(MethodDeclaration.class).stream()
                .map(method -> {
                    MethodDTO methodDTO = new MethodDTO();
                    methodDTO.setName(method.getNameAsString());
                    methodDTO.setAccessModifier(method.getAccessSpecifier().asString());
                    methodDTO.setReturnType(method.getType().asString());
                    methodDTO.setParameters(method.getParameters().stream()
                            .map(p -> {
                                ParameterDTO parameterDTO = new ParameterDTO();
                                parameterDTO.setName(p.getNameAsString());
                                parameterDTO.setType(p.getType().asString());
                                return parameterDTO;
                            }).collect(Collectors.toList()));
                    return methodDTO;
                }).collect(Collectors.toList());
    }
}