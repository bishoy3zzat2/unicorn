package com.loyalixa.backend.financial;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
@RestController
@RequestMapping("/api/v1/files")
public class FileController {
    private final String uploadBaseDir;
    private final String tasksUploadDir;
    public FileController(
            @Value("${file.upload.base-dir:uploads}") String uploadBaseDir,
            @Value("${file.upload.tasks-dir:tasks}") String tasksUploadDir) {
        this.uploadBaseDir = uploadBaseDir;
        this.tasksUploadDir = tasksUploadDir;
    }
    @GetMapping("/tasks/{fileName:.+}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadTaskFile(@PathVariable String fileName) {
        try {
            if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
                return ResponseEntity.status(403).build();
            }
            Path filePath = Paths.get(uploadBaseDir, tasksUploadDir, fileName).normalize();
            File file = filePath.toFile();
            Path uploadsDir = Paths.get(uploadBaseDir, tasksUploadDir).normalize().toAbsolutePath();
            if (!filePath.toAbsolutePath().startsWith(uploadsDir)) {
                return ResponseEntity.status(403).build();
            }
            if (!file.exists() || !file.isFile()) {
                return ResponseEntity.notFound().build();
            }
            Resource resource = new FileSystemResource(file);
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            String originalFileName = fileName;
            int underscoreIndex = fileName.indexOf('_');
            if (underscoreIndex > 0 && underscoreIndex < fileName.length() - 1) {
                originalFileName = fileName.substring(underscoreIndex + 1);
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originalFileName + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
