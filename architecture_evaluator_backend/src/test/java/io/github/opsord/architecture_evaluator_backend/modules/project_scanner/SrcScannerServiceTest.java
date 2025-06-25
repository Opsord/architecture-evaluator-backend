package io.github.opsord.architecture_evaluator_backend.modules.project_scanner;

import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.file_instance.FileInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_instance.FileInstanceService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.project_scanner.services.SrcScannerService;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SrcScannerServiceTest {

    private SrcScannerService srcScannerService;
    private final FileInstanceService fileInstanceService = mock(FileInstanceService.class);

    @BeforeEach
    void setUp() {
        srcScannerService = new SrcScannerService(fileInstanceService);
    }

    @Test
    void testScanSrcFolder_findsJavaFiles() throws Exception {
        File tempDir = java.nio.file.Files.createTempDirectory("src").toFile();
        File javaFile = new File(tempDir, "Test.java");
        try (FileWriter fw = new FileWriter(javaFile)) {
            fw.write("public class Test {}");
        }
        List<File> files = srcScannerService.scanSrcFolder(tempDir);
        assertEquals(1, files.size());
        assertTrue(files.get(0).getName().endsWith(".java"));
    }

    @Test
    void testScanSrcFolder_invalidDir() throws Exception {
        File notExist = new File("not_exist_dir");
        List<File> files = srcScannerService.scanSrcFolder(notExist);
        assertTrue(files.isEmpty());
    }

    @Test
    void testParseJavaFiles_successfulParse() throws Exception {
        File javaFile = new File("Test.java");
        File projectRoot = new File("root");
        FileInstance fileInstance = mock(FileInstance.class);

        when(fileInstanceService.parseJavaFile(javaFile, projectRoot)).thenReturn(fileInstance);

        List<FileInstance> result = srcScannerService.parseJavaFiles(List.of(javaFile), projectRoot);

        assertEquals(1, result.size());
        assertSame(fileInstance, result.get(0));
        verify(fileInstanceService, times(1)).parseJavaFile(javaFile, projectRoot);
    }

    @Test
    void testParseJavaFiles_emptyList() {
        File projectRoot = new File("root");
        List<FileInstance> result = srcScannerService.parseJavaFiles(List.of(), projectRoot);
        assertTrue(result.isEmpty());
        verifyNoInteractions(fileInstanceService);
    }

    @Test
    void testParseJavaFiles_parseThrowsException() throws Exception {
        File javaFile = new File("Test.java");
        File projectRoot = new File("root");

        when(fileInstanceService.parseJavaFile(javaFile, projectRoot)).thenThrow(new RuntimeException("Parse error"));

        List<FileInstance> result = srcScannerService.parseJavaFiles(List.of(javaFile), projectRoot);

        assertTrue(result.isEmpty());
        verify(fileInstanceService, times(1)).parseJavaFile(javaFile, projectRoot);
    }
}