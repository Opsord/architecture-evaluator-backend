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
    void testFindProjectRoot_withGradleKtsAndSrc() throws Exception {
        File tempDir = Files.createTempDirectory("project").toFile();
        new File(tempDir, "src").mkdir();
        new File(tempDir, "build.gradle.kts").createNewFile();

        File root = scannerService.findProjectRoot(tempDir);
        assertNotNull(root);
        assertEquals(tempDir.getAbsolutePath(), root.getAbsolutePath());
    }

    @Test
    void testFindProjectRoot_noSrc() throws Exception {
        File tempDir = Files.createTempDirectory("project").toFile();
        new File(tempDir, "pom.xml").createNewFile();

        File root = scannerService.findProjectRoot(tempDir);
        assertNull(root);
    }

    @Test
    void testFindProjectRoot_onlySrc() throws Exception {
        File tempDir = Files.createTempDirectory("project").toFile();
        new File(tempDir, "src").mkdir();

        File root = scannerService.findProjectRoot(tempDir);
        assertNull(root);
    }

    @Test
    void testFindProjectRoot_onlyPom() throws Exception {
        File tempDir = Files.createTempDirectory("project").toFile();
        new File(tempDir, "pom.xml").createNewFile();
        // Add an extra file to differentiate from testFindProjectRoot_noSrc
        new File(tempDir, "README.md").createNewFile();

        File root = scannerService.findProjectRoot(tempDir);
        assertNull(root);
    }

    @Test
    void testFindProjectRoot_ignoredFolder() throws Exception {
        File parent = Files.createTempDirectory("parent").toFile();
        File ignored = new File(parent, "target");
        ignored.mkdir();
        new File(ignored, "src").mkdir();
        new File(ignored, "pom.xml").createNewFile();

        File root = scannerService.findProjectRoot(ignored);
        assertNull(root);
    }

    @Test
    void testFindProjectRoot_nullFile() {
        File root = scannerService.findProjectRoot(null);
        assertNull(root);
    }

    @Test
    void testFindProjectRoot_notADirectory() throws Exception {
        File tempFile = Files.createTempFile("file", ".txt").toFile();
        File root = scannerService.findProjectRoot(tempFile);
        assertNull(root);
    }

    @Test
    void testFindProjectRoot_inSubdirectory() throws Exception {
        File parent = Files.createTempDirectory("parent").toFile();
        File sub = new File(parent, "sub");
        sub.mkdir();
        new File(sub, "src").mkdir();
        new File(sub, "pom.xml").createNewFile();

        File root = scannerService.findProjectRoot(parent);
        assertNotNull(root);
        assertEquals(sub.getAbsolutePath(), root.getAbsolutePath());
    }

    @Test
    void testFindProjectRoot_noProjectAnywhere() throws Exception {
        File parent = Files.createTempDirectory("parent").toFile();
        File sub = new File(parent, "sub");
        sub.mkdir();

        File root = scannerService.findProjectRoot(parent);
        assertNull(root);
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

    @Test
    void testFindPomFile_nullDirectory() {
        File found = scannerService.findPomFile(null);
        assertNull(found);
    }

    @Test
    void testFindPomFile_notADirectory() throws Exception {
        File tempFile = Files.createTempFile("file", ".txt").toFile();
        File found = scannerService.findPomFile(tempFile);
        assertNull(found);
    }
}