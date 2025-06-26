package io.github.opsord.architecture_evaluator_backend.modules.orchestrator;

import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.ClassInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.parts.LayerAnnotation;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.file_instance.FileInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_instance.FileInstanceService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.orchestrator.instances.ProjectAnalysisInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.orchestrator.services.OrchestratorService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.processor.dto.ProcessedClassInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.processor.services.FileAnalysisService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.project_scanner.services.GradleScannerService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.project_scanner.services.PomScannerService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.project_scanner.services.ScannerService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.project_scanner.services.SrcScannerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrchestratorServiceTest {

    private ScannerService scannerService;
    private PomScannerService pomScannerService;
    private GradleScannerService gradleScannerService;
    private SrcScannerService srcScannerService;
    private FileAnalysisService analysisService;
    private FileInstanceService fileInstanceService;
    private OrchestratorService orchestratorService;

    @BeforeEach
    void setUp() {
        scannerService = mock(ScannerService.class);
        pomScannerService = mock(PomScannerService.class);
        gradleScannerService = mock(GradleScannerService.class);
        srcScannerService = mock(SrcScannerService.class);
        analysisService = mock(FileAnalysisService.class);
        fileInstanceService = mock(FileInstanceService.class);

        orchestratorService = new OrchestratorService(
                scannerService,
                pomScannerService,
                gradleScannerService,
                srcScannerService,
                analysisService,
                fileInstanceService
        );
    }

    @Test
    void testOrchestrateProjectAnalysis_success() throws IOException {
        File projectRoot = new File("testProject");
        File srcDir = new File(projectRoot, "src");
        FileInstance fileInstance = new FileInstance();
        ClassInstance classInstance = new ClassInstance();
        classInstance.setLayerAnnotation(LayerAnnotation.SERVICE);
        classInstance.setClassDependencies(List.of());
        classInstance.setDependentClasses(List.of());
        classInstance.setName("TestClass");
        fileInstance.setClasses(List.of(classInstance));
        List<FileInstance> fileInstances = List.of(fileInstance);
        List<File> srcFiles = List.of(new File(srcDir, "A.java"));
        ProcessedClassInstance processed = mock(ProcessedClassInstance.class);
        when(processed.getClassInstance()).thenReturn(classInstance);

        when(scannerService.findProjectRoot(any())).thenReturn(projectRoot);
        when(srcScannerService.scanSrcFolder(any())).thenReturn(srcFiles);
        when(srcScannerService.parseJavaFiles(eq(srcFiles), eq(projectRoot))).thenReturn(fileInstances);
        when(pomScannerService.scanPomFile(any())).thenReturn(Optional.empty());
        when(gradleScannerService.scanGradleFile(any())).thenReturn(Optional.empty());
        when(analysisService.analyseFileInstance(any(), any(), any())).thenReturn(List.of(processed));
        when(fileInstanceService.getDependentClassNamesFromClass(any(), any())).thenReturn(List.of());

        ProjectAnalysisInstance result = orchestratorService.orchestrateProjectAnalysis("testProject");

        assertNotNull(result);
        assertEquals("testProject", result.getProjectName());
        assertEquals(1, result.getServices().size());
    }

    @Test
    void testOrchestrateProjectAnalysis_projectRootNotFound() {
        when(scannerService.findProjectRoot(any())).thenReturn(null);
        IOException ex = assertThrows(IOException.class, () ->
                orchestratorService.orchestrateProjectAnalysis("missingProject"));
        assertEquals("Project root not found", ex.getMessage());
    }

    @Test
    void testOrchestrateProjectAnalysis_noJavaFiles() throws IOException {
        File projectRoot = new File("testProject");
        when(scannerService.findProjectRoot(any())).thenReturn(projectRoot);
        when(srcScannerService.scanSrcFolder(any())).thenReturn(List.of());
        when(srcScannerService.parseJavaFiles(any(), any())).thenReturn(List.of());

        IOException ex = assertThrows(IOException.class, () ->
                orchestratorService.orchestrateProjectAnalysis("testProject"));
        assertEquals("No Java files found in the project", ex.getMessage());
    }

    @Test
    void testOrganizeProjectAnalysis_groupsByLayerAnnotation() {
        ProcessedClassInstance entity = mock(ProcessedClassInstance.class);
        ProcessedClassInstance repo = mock(ProcessedClassInstance.class);
        ProcessedClassInstance service = mock(ProcessedClassInstance.class);
        ProcessedClassInstance controller = mock(ProcessedClassInstance.class);
        ProcessedClassInstance test = mock(ProcessedClassInstance.class);
        ProcessedClassInstance other = mock(ProcessedClassInstance.class);

        ClassInstance entityCls = new ClassInstance(); entityCls.setLayerAnnotation(LayerAnnotation.ENTITY);
        ClassInstance repoCls = new ClassInstance(); repoCls.setLayerAnnotation(LayerAnnotation.REPOSITORY);
        ClassInstance serviceCls = new ClassInstance(); serviceCls.setLayerAnnotation(LayerAnnotation.SERVICE);
        ClassInstance controllerCls = new ClassInstance(); controllerCls.setLayerAnnotation(LayerAnnotation.CONTROLLER);
        ClassInstance testCls = new ClassInstance(); testCls.setLayerAnnotation(LayerAnnotation.TESTING);
        ClassInstance otherCls = new ClassInstance(); otherCls.setLayerAnnotation(LayerAnnotation.OTHER);

        when(entity.getClassInstance()).thenReturn(entityCls);
        when(repo.getClassInstance()).thenReturn(repoCls);
        when(service.getClassInstance()).thenReturn(serviceCls);
        when(controller.getClassInstance()).thenReturn(controllerCls);
        when(test.getClassInstance()).thenReturn(testCls);
        when(other.getClassInstance()).thenReturn(otherCls);

        List<ProcessedClassInstance> all = List.of(entity, repo, service, controller, test, other);

        ProjectAnalysisInstance result = orchestratorService.organizeProjectAnalysis(all);

        assertEquals(1, result.getEntities().size());
        assertEquals(1, result.getRepositories().size());
        assertEquals(1, result.getServices().size());
        assertEquals(1, result.getControllers().size());
        assertEquals(1, result.getTestClasses().size());
        assertEquals(1, result.getOtherClasses().size());
    }

    @Test
    void testPopulateClassDependencies_setsDependenciesAndDependents() {
        ClassInstance a = new ClassInstance(); a.setName("A");
        ClassInstance b = new ClassInstance(); b.setName("B");
        FileInstance fileA = new FileInstance(); fileA.setClasses(List.of(a));
        FileInstance fileB = new FileInstance(); fileB.setClasses(List.of(b));
        List<FileInstance> allFiles = List.of(fileA, fileB);

        when(fileInstanceService.getDependentClassNamesFromClass(eq(a), eq(allFiles))).thenReturn(List.of("B"));
        when(fileInstanceService.getDependentClassNamesFromClass(eq(b), eq(allFiles))).thenReturn(List.of());

        orchestratorService.populateClassDependencies(allFiles);

        assertEquals(List.of("B"), a.getClassDependencies());
        assertEquals(List.of("A"), b.getDependentClasses());
    }
}