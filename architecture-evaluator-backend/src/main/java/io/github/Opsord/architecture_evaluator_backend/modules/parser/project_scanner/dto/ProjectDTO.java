package io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.CustomCompilationUnitDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProjectDTO {
    private List<CustomCompilationUnitDTO> entities;
    private List<CustomCompilationUnitDTO> repositories;
    private List<CustomCompilationUnitDTO> services;
    private List<CustomCompilationUnitDTO> controllers;
    private List<CustomCompilationUnitDTO> documents;
    private PomFileDTO pomFile;
}