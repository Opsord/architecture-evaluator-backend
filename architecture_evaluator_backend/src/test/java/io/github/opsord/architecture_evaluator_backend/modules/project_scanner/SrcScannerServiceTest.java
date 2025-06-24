package io.github.opsord.architecture_evaluator_backend.modules.project_scanner;

import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_instance.FileInstanceService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.project_scanner.services.SrcScannerService;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SrcScannerServiceTest {

    private SrcScannerService srcScannerService;

    @BeforeEach
    void setUp() {
        FileInstanceService fileInstanceService = Mockito.mock(FileInstanceService.class);
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
}