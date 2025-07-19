package io.github.opsord.architecture_evaluator_backend.modules.project_scanner;

import io.github.opsord.architecture_evaluator_backend.modules.api.project_scanner.instances.pom.PomFileInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.project_scanner.services.PomScannerService;
import io.github.opsord.architecture_evaluator_backend.modules.api.project_scanner.services.ScannerService;
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

    @Test
    void testScanPomFile_withParentSection() throws Exception {
        File tempDir = java.nio.file.Files.createTempDirectory("project").toFile();
        File pom = new File(tempDir, "pom.xml");
        try (FileWriter fw = new FileWriter(pom)) {
            fw.write("""
            <project>
              <parent>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-parent</artifactId>
                <version>2.7.0</version>
              </parent>
              <groupId>com.example</groupId>
              <artifactId>demo</artifactId>
              <version>1.0.0</version>
            </project>
            """);
        }

        Optional<PomFileInstance> result = pomScannerService.scanPomFile(tempDir);
        assertTrue(result.isPresent());
        assertNotNull(result.get().getParentSection());
        assertEquals("org.springframework.boot", result.get().getParentSection().getGroupId());
        assertEquals("spring-boot-starter-parent", result.get().getParentSection().getArtifactId());
        assertEquals("2.7.0", result.get().getParentSection().getVersion());
    }

    @Test
    void testScanPomFile_withDevelopersAndLicense() throws Exception {
        File tempDir = java.nio.file.Files.createTempDirectory("project").toFile();
        File pom = new File(tempDir, "pom.xml");
        try (FileWriter fw = new FileWriter(pom)) {
            fw.write("""
            <project>
              <groupId>com.example</groupId>
              <artifactId>demo</artifactId>
              <version>1.0.0</version>
              <license>
                <name>Apache License 2.0</name>
              </license>
              <developers>
                <developer>
                  <name>John Doe</name>
                </developer>
                <developer>
                  <name>Jane Smith</name>
                </developer>
              </developers>
            </project>
            """);
        }

        Optional<PomFileInstance> result = pomScannerService.scanPomFile(tempDir);
        assertTrue(result.isPresent());
        assertEquals("Apache License 2.0", result.get().getLicense());
        assertEquals(2, result.get().getDevelopers().size());
        assertEquals("John Doe", result.get().getDevelopers().get(0));
        assertEquals("Jane Smith", result.get().getDevelopers().get(1));
    }

    @Test
    void testScanPomFile_withJavaVersion() throws Exception {
        File tempDir = java.nio.file.Files.createTempDirectory("project").toFile();
        File pom = new File(tempDir, "pom.xml");
        try (FileWriter fw = new FileWriter(pom)) {
            fw.write("""
            <project>
              <groupId>com.example</groupId>
              <artifactId>demo</artifactId>
              <version>1.0.0</version>
              <java.version>17</java.version>
            </project>
            """);
        }

        Optional<PomFileInstance> result = pomScannerService.scanPomFile(tempDir);
        assertTrue(result.isPresent());
        assertEquals("17", result.get().getJavaVersion());
    }

    @Test
    void testScanPomFile_multipleDependencies() throws Exception {
        File tempDir = java.nio.file.Files.createTempDirectory("project").toFile();
        File pom = new File(tempDir, "pom.xml");
        try (FileWriter fw = new FileWriter(pom)) {
            fw.write("""
            <project>
              <groupId>com.example</groupId>
              <artifactId>demo</artifactId>
              <version>1.0.0</version>
              <dependencies>
                <dependency>
                  <groupId>org.springframework.boot</groupId>
                  <artifactId>spring-boot-starter-web</artifactId>
                </dependency>
                <dependency>
                  <groupId>org.springframework.boot</groupId>
                  <artifactId>spring-boot-starter-data-jpa</artifactId>
                  <version>2.7.0</version>
                </dependency>
                <dependency>
                  <groupId>org.projectlombok</groupId>
                  <artifactId>lombok</artifactId>
                  <version>1.18.24</version>
                </dependency>
              </dependencies>
            </project>
            """);
        }

        Optional<PomFileInstance> result = pomScannerService.scanPomFile(tempDir);
        assertTrue(result.isPresent());
        assertEquals(3, result.get().getDependencies().size());

        // Check each dependency
        var deps = result.get().getDependencies();
        assertEquals("org.springframework.boot:spring-boot-starter-web", deps.get(0).toKey());
        assertEquals("org.springframework.boot:spring-boot-starter-data-jpa", deps.get(1).toKey());
        assertEquals("2.7.0", deps.get(1).getVersion());
        assertEquals("org.projectlombok", deps.get(2).getBasePackage());
    }

    @Test
    void testScanPomFile_invalidXml() throws Exception {
        File tempDir = java.nio.file.Files.createTempDirectory("project").toFile();
        File pom = new File(tempDir, "pom.xml");
        try (FileWriter fw = new FileWriter(pom)) {
            fw.write("""
            <project>
              <groupId>com.example</groupId>
              <artifactId>demo</artifactId>
              <version>1.0.0
            </project>
            """);  // Invalid XML - unclosed version tag
        }

        Optional<PomFileInstance> result = pomScannerService.scanPomFile(tempDir);
        assertTrue(result.isEmpty());
    }

    @Test
    void testScanPomFile_nullDirectory() {
        Optional<PomFileInstance> result = pomScannerService.scanPomFile(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void testScanPomFile_completeProjectInfo() throws Exception {
        File tempDir = java.nio.file.Files.createTempDirectory("project").toFile();
        File pom = new File(tempDir, "pom.xml");
        try (FileWriter fw = new FileWriter(pom)) {
            fw.write("""
            <project>
              <groupId>com.example</groupId>
              <artifactId>demo</artifactId>
              <version>1.0.0-SNAPSHOT</version>
              <description>Demo project for testing</description>
              <url>https://github.com/example/demo</url>
              <license>
                <name>MIT License</name>
              </license>
              <developers>
                <developer>
                  <name>Developer Name</name>
                </developer>
              </developers>
              <java.version>11</java.version>
            </project>
            """);
        }

        Optional<PomFileInstance> result = pomScannerService.scanPomFile(tempDir);
        assertTrue(result.isPresent());
        PomFileInstance pomFileInstance = result.get();
        assertEquals("com.example", pomFileInstance.getGroupId());
        assertEquals("demo", pomFileInstance.getArtifactId());
        assertEquals("1.0.0-SNAPSHOT", pomFileInstance.getVersion());
        assertEquals("Demo project for testing", pomFileInstance.getDescription());
        assertEquals("https://github.com/example/demo", pomFileInstance.getUrl());
        assertEquals("MIT License", pomFileInstance.getLicense());
        assertEquals(1, pomFileInstance.getDevelopers().size());
        assertEquals("Developer Name", pomFileInstance.getDevelopers().get(0));
        assertEquals("11", pomFileInstance.getJavaVersion());
    }
}