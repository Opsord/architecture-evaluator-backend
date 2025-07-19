package io.github.opsord.architecture_evaluator_backend.modules.project_scanner;

import io.github.opsord.architecture_evaluator_backend.modules.api.project_scanner.instances.gradle.GradleFileInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.project_scanner.services.GradleScannerService;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FileWriter;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class GradleScannerServiceTest {

    private GradleScannerService gradleScannerService;

    @BeforeEach
    void setUp() {
        gradleScannerService = new GradleScannerService();
    }

    @Test
    void testScanGradleFile_validBuildGradle() throws Exception {
        File tempDir = java.nio.file.Files.createTempDirectory("gradleproject").toFile();
        File gradle = new File(tempDir, "build.gradle");
        try (FileWriter fw = new FileWriter(gradle)) {
            fw.write("""
                group = 'com.example'
                version = '1.0.0'
                description = 'Test Gradle Project'
                dependencies {
                  implementation 'org.springframework:spring-core:5.3.0'
                }
                """);
        }
        Optional<GradleFileInstance> result = gradleScannerService.scanGradleFile(tempDir);
        assertTrue(result.isPresent());
        GradleFileInstance gradleFileInstance = result.get();
        assertEquals("com.example", gradleFileInstance.getGroup());
        assertEquals("1.0.0", gradleFileInstance.getVersion());
        assertEquals("Test Gradle Project", gradleFileInstance.getDescription());
        assertNotNull(gradleFileInstance.getDependencies());
        assertFalse(gradleFileInstance.getDependencies().isEmpty());
        assertEquals("org.springframework", gradleFileInstance.getDependencies().get(0).getGroup());
    }

    @Test
    void testScanGradleFile_noGradleFile() {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        Optional<GradleFileInstance> result = gradleScannerService.scanGradleFile(tempDir);
        assertTrue(result.isEmpty());
    }
}