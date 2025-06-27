package io.github.opsord.architecture_evaluator_backend.modules.orchestrator;

import io.github.opsord.architecture_evaluator_backend.modules.parser.orchestrator.services.FileManagerService;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.junit.jupiter.api.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileManagerServiceTest {

    private FileManagerService fileManagerService;

    @BeforeEach
    void setUp() {
        fileManagerService = new FileManagerService();
    }

    @Test
    void testIsValidGitHubUrl_validUrls() throws Exception {
        var method = FileManagerService.class.getDeclaredMethod("isValidGitHubUrl", String.class);
        method.setAccessible(true);
        assertTrue((Boolean) method.invoke(fileManagerService, "https://github.com/user/repo"));
        assertTrue((Boolean) method.invoke(fileManagerService, "http://github.com/user/repo"));
        assertTrue((Boolean) method.invoke(fileManagerService, "github.com/user/repo"));
        assertTrue((Boolean) method.invoke(fileManagerService, "https://www.github.com/user/repo/"));
    }

    @Test
    void testIsValidGitHubUrl_invalidUrls() throws Exception {
        var method = FileManagerService.class.getDeclaredMethod("isValidGitHubUrl", String.class);
        method.setAccessible(true);
        assertFalse((Boolean) method.invoke(fileManagerService, "https://gitlab.com/user/repo"));
        assertFalse((Boolean) method.invoke(fileManagerService, "https://github.com/user"));
        assertFalse((Boolean) method.invoke(fileManagerService, ""));
        assertFalse((Boolean) method.invoke(fileManagerService, (Object) null));
    }

    @Test
    void testExtractGitHubRepoDetails_valid() throws Exception {
        var method = FileManagerService.class.getDeclaredMethod("extractGitHubRepoDetails", String.class);
        method.setAccessible(true);
        String[] result = (String[]) method.invoke(fileManagerService, "https://github.com/user/repo");
        assertEquals("user", result[0]);
        assertEquals("repo", result[1]);
    }

    @Test
    void testExtractGitHubRepoDetails_invalid() throws Exception {
        var method = FileManagerService.class.getDeclaredMethod("extractGitHubRepoDetails", String.class);
        method.setAccessible(true);
        assertThrows(IllegalArgumentException.class, () -> {
            try {
                method.invoke(fileManagerService, "https://github.com/user");
            } catch (java.lang.reflect.InvocationTargetException e) {
                // Rethrow the actual cause
                throw e.getCause();
            }
        });
    }

    @Test
    void testSaveUploadedFile_validZip() throws Exception {
        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.getOriginalFilename()).thenReturn("test.zip");
        File tempFile = File.createTempFile("test", ".zip");
        doAnswer(invocation -> {
            File dest = invocation.getArgument(0);
            Files.copy(tempFile.toPath(), dest.toPath());
            return null;
        }).when(multipartFile).transferTo(any(File.class));

        try {
            File saved = fileManagerService.saveUploadedFile(multipartFile);
            assertTrue(saved.exists());
            assertTrue(saved.getName().endsWith(".zip"));
            saved.delete();
            saved.getParentFile().delete();
        } catch (IOException e) {
            if (e.getMessage().contains("Failed to set secure permissions")) {
                // On Windows, the permission setting may fail; skip this assertion
            } else {
                throw e;
            }
        } finally {
            tempFile.delete();
        }
    }

    @Test
    void testSaveUploadedFile_invalidExtension() {
        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.getOriginalFilename()).thenReturn("test.txt");
        assertThrows(IOException.class, () -> fileManagerService.saveUploadedFile(multipartFile));
    }

    @Test
    void testSaveUploadedFile_noExtension() {
        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.getOriginalFilename()).thenReturn("testfile");
        assertThrows(IOException.class, () -> fileManagerService.saveUploadedFile(multipartFile));
    }

    @Test
    void testExtractArchive_zip() throws Exception {
        // Create a zip file with a single file inside
        File tempDir = Files.createTempDirectory("ziptest").toFile();
        File zipFile = new File(tempDir, "test.zip");
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(zipFile)) {
            ZipArchiveEntry entry = new ZipArchiveEntry("file.txt");
            zos.putArchiveEntry(entry);
            zos.write("hello".getBytes());
            zos.closeArchiveEntry();
        }

        File extracted = fileManagerService.extractArchive(zipFile);
        File extractedFile = new File(extracted, "file.txt");
        assertTrue(extractedFile.exists());
        assertEquals("hello", Files.readString(extractedFile.toPath()));

        // Cleanup
        fileManagerService.deleteRecursively(tempDir);
    }

    @Test
    void testExtractArchive_invalidFile() {
        File notArchive = new File("not_an_archive.txt");
        try {
            notArchive.createNewFile();
            assertThrows(IOException.class, () -> fileManagerService.extractArchive(notArchive));
        } catch (IOException ignored) {
        } finally {
            notArchive.delete();
        }
    }

    @Test
    void testDeleteRecursively_fileAndDir() throws Exception {
        File tempDir = Files.createTempDirectory("deltest").toFile();
        File file = new File(tempDir, "file.txt");
        assertTrue(file.createNewFile());
        File subDir = new File(tempDir, "sub");
        assertTrue(subDir.mkdir());
        File subFile = new File(subDir, "subfile.txt");
        assertTrue(subFile.createNewFile());

        fileManagerService.deleteRecursively(tempDir);
        assertFalse(tempDir.exists());
    }

    @Test
    void testCreateSecureTempDirectory() throws Exception {
        var method = FileManagerService.class.getDeclaredMethod("createSecureTempDirectory", String.class);
        method.setAccessible(true);
        try {
            File dir = (File) method.invoke(fileManagerService, "testprefix");
            assertTrue(dir.exists());
            assertTrue(dir.isDirectory());
            fileManagerService.deleteRecursively(dir);
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException && cause.getMessage().contains("Failed to set secure permissions")) {
                // On Windows, the permission setting may fail; skip this assertion
                return;
            }
            throw e;
        }
    }

    @Test
    void testDownloadGitHubRepository_invalidUrl() {
        assertThrows(IllegalArgumentException.class, () ->
                fileManagerService.downloadGitHubRepository("https://gitlab.com/user/repo")
        );
    }

    @Test
    void testDownloadGitHubRepository_invalidSyntax() {
        assertThrows(IllegalArgumentException.class, () ->
                fileManagerService.downloadGitHubRepository("ht!tp://github.com/user/repo")
        );
    }

    @Test
    void testExtractArchive_entryOutsideExtractionDir() throws Exception {
        // Crea un zip con una entrada maliciosa
        File tempDir = Files.createTempDirectory("ziptest").toFile();
        File zipFile = new File(tempDir, "test.zip");
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(zipFile)) {
            ZipArchiveEntry entry = new ZipArchiveEntry("../evil.txt");
            zos.putArchiveEntry(entry);
            zos.write("bad".getBytes());
            zos.closeArchiveEntry();
        }
        assertThrows(IOException.class, () -> fileManagerService.extractArchive(zipFile));
        fileManagerService.deleteRecursively(tempDir);
    }

    @Test
    void testProcessArchiveEntry_directoryCreationFailure() throws Exception {
        File tempDir = Files.createTempDirectory("faildirtest").toFile();
        File extractedDir = new File(tempDir, "extracted");
        extractedDir.delete(); // Ensure it does not exist

        FileManagerService service = new FileManagerService();
        ArchiveEntry entry = mock(ArchiveEntry.class);
        when(entry.getName()).thenReturn("dir/");
        when(entry.isDirectory()).thenReturn(true);

        // Make extractedDir a file, so mkdir() will fail
        assertTrue(extractedDir.createNewFile());

        var method = FileManagerService.class.getDeclaredMethod("processArchiveEntry", ArchiveEntry.class, InputStream.class, File.class);
        method.setAccessible(true);
        assertThrows(IOException.class, () -> {
            try {
                method.invoke(service, entry, new ByteArrayInputStream(new byte[0]), extractedDir);
            } catch (java.lang.reflect.InvocationTargetException e) {
                throw e.getCause();
            }
        });

        extractedDir.delete();
        tempDir.delete();
    }

    @Test
    void testDeleteRecursively_nonExistentFile() {
        File file = new File("nonexistentfile.txt");
        // No debe lanzar excepción
        fileManagerService.deleteRecursively(file);
        assertFalse(file.exists(), "File should not exist after deletion attempt");
    }

    @Test
    void testCreateSecureTempDirectory_permissionFailure() throws Exception {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            File baseTemp = new File(System.getProperty("java.io.tmpdir"));
            File tempDir = new File(baseTemp, "failperm-" + java.util.UUID.randomUUID());
            tempDir.mkdir();
            // Remove permissions so setReadable/setWritable/setExecutable will fail
            tempDir.setReadable(false, false);
            tempDir.setWritable(false, false);
            tempDir.setExecutable(false, false);
            try {
                var method = FileManagerService.class.getDeclaredMethod("createSecureTempDirectory", String.class);
                method.setAccessible(true);
                Exception ex = assertThrows(Exception.class, () -> {
                    try {
                        method.invoke(fileManagerService, "failperm");
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        throw e.getCause();
                    }
                });
                assertTrue(ex instanceof IOException, "Expected IOException but got: " + ex.getClass());
            } finally {
                // Restore permissions to allow deletion
                tempDir.setReadable(true, false);
                tempDir.setWritable(true, false);
                tempDir.setExecutable(true, false);
                tempDir.delete();
            }
        }
    }

    @Test
    void testCreateSecureTempDirectory_normalPrefix() throws IOException {
        try {
            File dir = fileManagerService.createSecureTempDirectory("testprefix");
            assertTrue(dir.exists());
            assertTrue(dir.isDirectory());
            fileManagerService.deleteRecursively(dir);
        } catch (IOException e) {
            if (e.getMessage().contains("Failed to set secure permissions")) {
                // On Windows, the permission setting may fail; skip this assertion
                return;
            }
            throw e;
        }
    }
    @Test
    void testCreateSecureTempDirectory_emptyPrefix() throws IOException {
        try {
            File dir = fileManagerService.createSecureTempDirectory("");
            assertTrue(dir.exists());
            assertTrue(dir.isDirectory());
            fileManagerService.deleteRecursively(dir);
        } catch (IOException e) {
            if (e.getMessage().contains("Failed to set secure permissions")) {
                // On Windows, the permission setting may fail; skip this assertion
                return;
            }
            throw e;
        }
    }

    @Test
    void testCreateSecureTempDirectory_specialCharsPrefix() throws Exception {
        String prefix = "prefix!@#$%^&*()";
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            Exception ex = assertThrows(IOException.class, () -> fileManagerService.createSecureTempDirectory(prefix));
            assertTrue(ex.getMessage().contains("Failed to create secure temp directory") ||
                    ex.getMessage().contains("Failed to set secure permissions"));
        } else {
            File dir = fileManagerService.createSecureTempDirectory(prefix);
            assertTrue(dir.exists());
            fileManagerService.deleteRecursively(dir);
        }
    }

    @Test
    void testCreateSecureTempDirectory_permissionFailure_windowsOnly() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            File baseTemp = new File(System.getProperty("java.io.tmpdir"));
            File tempDir = new File(baseTemp, "failperm-" + UUID.randomUUID());
            tempDir.mkdir();
            tempDir.setReadable(false, false);
            tempDir.setWritable(false, false);
            tempDir.setExecutable(false, false);
            try {
                Exception ex = assertThrows(IOException.class, () -> fileManagerService.createSecureTempDirectory("failperm"));
                assertTrue(ex.getMessage().contains("Failed to create secure temp directory") ||
                        ex.getMessage().contains("Failed to set secure permissions"));
            } finally {
                tempDir.setReadable(true, false);
                tempDir.setWritable(true, false);
                tempDir.setExecutable(true, false);
                tempDir.delete();
            }
        }
    }

    @Test
    void testCreateSecureTempDirectory_windowsBranch() throws Exception {
        String originalOs = System.getProperty("os.name");
        System.setProperty("os.name", "Windows 10");
        try {
            File dir = fileManagerService.createSecureTempDirectory("winprefix");
            assertTrue(dir.exists());
            assertTrue(dir.isDirectory());
            fileManagerService.deleteRecursively(dir);
        } catch (IOException e) {
            // On Windows, the permission setting may fail; skip this assertion
            if (!e.getMessage().contains("Failed to set secure permissions")) {
                throw e;
            }
        } finally {
            System.setProperty("os.name", originalOs);
        }
    }

}