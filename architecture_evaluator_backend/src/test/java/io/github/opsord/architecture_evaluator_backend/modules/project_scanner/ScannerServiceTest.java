package io.github.opsord.architecture_evaluator_backend.modules.project_scanner;

import io.github.opsord.architecture_evaluator_backend.modules.parser.project_scanner.services.ScannerService;
import org.junit.jupiter.api.*;
import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class ScannerServiceTest {

    private ScannerService scannerService;

    @BeforeEach
    void setUp() {
        scannerService = new ScannerService();
    }

    @Test
    void testFindProjectRoot_withPomAndSrc() throws Exception {
        File tempDir = Files.createTempDirectory("project").toFile();
        new File(tempDir, "src").mkdir();
        new File(tempDir, "pom.xml").createNewFile();

        File root = scannerService.findProjectRoot(tempDir);
        assertNotNull(root);
        assertEquals(tempDir.getAbsolutePath(), root.getAbsolutePath());
    }

    @Test
    void testFindPomFile_found() throws Exception {
        File tempDir = Files.createTempDirectory("project").toFile();
        File pom = new File(tempDir, "pom.xml");
        pom.createNewFile();

        File found = scannerService.findPomFile(tempDir);
        assertNotNull(found);
        assertEquals(pom.getAbsolutePath(), found.getAbsolutePath());
    }

    @Test
    void testFindPomFile_notFound() throws Exception {
        File tempDir = Files.createTempDirectory("project").toFile();
        File found = scannerService.findPomFile(tempDir);
        assertNull(found);
    }
}