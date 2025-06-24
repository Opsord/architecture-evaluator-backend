package io.github.opsord.architecture_evaluator_backend.modules.project_scanner;

import io.github.opsord.architecture_evaluator_backend.modules.parser.project_scanner.instances.pom.PomFileInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.project_scanner.services.PomScannerService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.project_scanner.services.ScannerService;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileWriter;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PomScannerServiceTest {

    private PomScannerService pomScannerService;

    @BeforeEach
    void setUp() {
        ScannerService scannerService = Mockito.spy(new ScannerService());
        pomScannerService = new PomScannerService(scannerService);
    }

    @Test
    void testScanPomFile_validPom() throws Exception {
        File tempDir = java.nio.file.Files.createTempDirectory("project").toFile();
        File pom = new File(tempDir, "pom.xml");
        try (FileWriter fw = new FileWriter(pom)) {
            fw.write("""
                <project>
                  <groupId>com.example</groupId>
                  <artifactId>demo</artifactId>
                  <version>1.0.0</version>
                  <description>Test project</description>
                  <dependencies>
                    <dependency>
                      <groupId>org.springframework</groupId>
                      <artifactId>spring-core</artifactId>
                      <version>5.3.0</version>
                    </dependency>
                  </dependencies>
                </project>
                """);
        }
        Optional<PomFileInstance> result = pomScannerService.scanPomFile(tempDir);
        assertTrue(result.isPresent());
        PomFileInstance pomFileInstance = result.get();
        assertEquals("com.example", pomFileInstance.getGroupId());
        assertEquals("demo", pomFileInstance.getArtifactId());
        assertEquals("1.0.0", pomFileInstance.getVersion());
        assertEquals("Test project", pomFileInstance.getDescription());
        assertNotNull(pomFileInstance.getDependencies());
        assertFalse(pomFileInstance.getDependencies().isEmpty());
        assertEquals("org.springframework", pomFileInstance.getDependencies().get(0).getGroupId());
    }

    @Test
    void testScanPomFile_noPom() {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        Optional<PomFileInstance> result = pomScannerService.scanPomFile(tempDir);
        assertTrue(result.isEmpty());
    }
}