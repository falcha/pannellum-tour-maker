package com.leafyjava.pannellumtourmaker.controllers;

import com.leafyjava.pannellumtourmaker.domains.Tour;
import com.leafyjava.pannellumtourmaker.domains.UploadedFile;
import com.leafyjava.pannellumtourmaker.exceptions.UnsupportedFileExtensionException;
import com.leafyjava.pannellumtourmaker.services.FileUploadService;
import com.leafyjava.pannellumtourmaker.services.TourService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/guest/file-upload")
public class FileUploadController {

    private FileUploadService fileUploadService;
    private TourService tourService;

    @Autowired
    public FileUploadController(final FileUploadService fileUploadService,
                                final TourService tourService) {
        this.fileUploadService = fileUploadService;
        this.tourService = tourService;
    }

    @GetMapping("")
    public List<UploadedFile> getUploadedFiles() {
        return fileUploadService.getUploadedFiles();
    }

    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        Resource resource = fileUploadService.loadAsResource(filename);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
            .body(resource);
    }

    @PostMapping("/tours")
    public void uploadFile(@RequestParam("name") String name, @RequestParam("file") MultipartFile file) {
        if (!StringUtils.getFilenameExtension(file.getOriginalFilename()).equalsIgnoreCase("zip")) {
            throw new UnsupportedFileExtensionException("The uploaded file must be a zip file.");
        }
        fileUploadService.store(name, file);
        tourService.createTourFromFiles(name);
    }

    @GetMapping("/tours")
    public List<Tour> getTours() {
        return tourService.findAllTours();
    }

    @GetMapping("/tours/{name}")
    public Tour getTourByName(@PathVariable(value = "name") String name) {
        return tourService.findOne(name);
    }

}
