package com.leafyjava.pannellumtourmaker.controllers;

import com.leafyjava.pannellumtourmaker.domains.Task;
import com.leafyjava.pannellumtourmaker.domains.Tour;
import com.leafyjava.pannellumtourmaker.domains.TourMessage;
import com.leafyjava.pannellumtourmaker.exceptions.InvalidTourException;
import com.leafyjava.pannellumtourmaker.exceptions.TourAlreadyExistsException;
import com.leafyjava.pannellumtourmaker.exceptions.TourNotFoundException;
import com.leafyjava.pannellumtourmaker.exceptions.UnsupportedFileExtensionException;
import com.leafyjava.pannellumtourmaker.services.AsyncTourService;
import com.leafyjava.pannellumtourmaker.services.TaskService;
import com.leafyjava.pannellumtourmaker.services.TourService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/v1/public/guest/tours")
public class TourRestController {
    private TourService tourService;
    private AsyncTourService asyncTourService;
    private TaskService taskService;

    @Autowired
    public TourRestController(final TourService tourService,
                              final AsyncTourService asyncTourService,
                              final TaskService taskService) {
        this.tourService = tourService;
        this.asyncTourService = asyncTourService;
        this.taskService = taskService;
    }

    @GetMapping()
    public List<Tour> getTours() {
        return tourService.findAllTours();
    }

    @GetMapping("/names")
    public List<String> getTourNames() {
        return tourService.findAllTourNames();
    }

    @PostMapping()
    public void uploadNewTour(@RequestParam("name") String name,
                           @RequestParam("file") MultipartFile tourFile,
                           @RequestParam(value = "map", required = false) MultipartFile mapFile,
                           @RequestParam(value = "northOffset", required = false, defaultValue = "0") Integer northOffset) {
        if (!Pattern.matches("[a-zA-Z\\d]*", name)) {
            throw new InvalidTourException("Tour name can only contain letters and numbers. " +
                "No special characters or space is allowed.");
        }
        if (tourService.findOne(name) != null) {
            throw new TourAlreadyExistsException("Tour " + name + " already exists.");
        }

        if (!StringUtils.getFilenameExtension(tourFile.getOriginalFilename()).equalsIgnoreCase("zip")) {
            throw new UnsupportedFileExtensionException("The uploaded file must be a zip file.");
        }

        if (mapFile != null) {
            String mapFileExtension = StringUtils.getFilenameExtension(mapFile.getOriginalFilename());
            if (!(mapFileExtension.equalsIgnoreCase("jpg") || mapFileExtension.equalsIgnoreCase("png"))) {
                throw new UnsupportedFileExtensionException("The uploaded file must be a jpg or png file.");
            }
        }

        TourMessage tourMessage = new TourMessage();

        File tempTourFile = tourService.createTempFileFromMultipartFile(tourFile);
        File tempMapFile = mapFile != null ? tourService.createTempFileFromMultipartFile(mapFile) : null;
        tourMessage.setName(name);
        tourMessage.setMapFile(tempMapFile);
        tourMessage.setTourFile(tempTourFile);
        tourMessage.setNorthOffset(northOffset);
        Task task = new Task(name);
        taskService.save(task);
        tourMessage.setTask(task);

        asyncTourService.sendToToursNew(tourMessage);
    }

    @PostMapping("/exist")
    public void uploadToTour(@RequestParam("name") String name,
                              @RequestParam("file") MultipartFile tourFile,
                              @RequestParam(value = "northOffset", required = false, defaultValue = "0") Integer northOffset) {
        if (tourService.findOne(name) == null) {
            throw new TourAlreadyExistsException("Tour " + name + " does not exist.");
        }

        if (!StringUtils.getFilenameExtension(tourFile.getOriginalFilename()).equalsIgnoreCase("zip")) {
            throw new UnsupportedFileExtensionException("The uploaded file must be a zip file.");
        }

        TourMessage tourMessage = new TourMessage();

        File tempTourFile = tourService.createTempFileFromMultipartFile(tourFile);
        tourMessage.setName(name);
        tourMessage.setTourFile(tempTourFile);
        tourMessage.setNorthOffset(northOffset);
        Task task = new Task(name);
        taskService.save(task);
        tourMessage.setTask(task);

        asyncTourService.sendToToursAddScene(tourMessage);
    }

    @GetMapping("/{name}")
    public Tour getTourByName(@PathVariable("name") String name) {
        return tourService.findOne(name);
    }

    @PutMapping("/{name}")
    public Tour saveTourByName(@PathVariable("name") String name, @RequestBody Tour tour) {
        if (!name.equalsIgnoreCase(tour.getName()))
            throw new InvalidTourException("Tour name must match in the path and the request body.");

        return tourService.save(tour);
    }

    @DeleteMapping("/{name}/scenes/{sceneId:.+}")
    public void deleteScene(@PathVariable("name") String name, @PathVariable("sceneId") String sceneId) {
        Tour tour = tourService.findOne(name);
        if (tour == null) {
            throw new TourNotFoundException("Tour " + name + " does not exist.");
        }
        if (tour.getScenes().stream().noneMatch(scene -> scene.getId().equalsIgnoreCase(sceneId))) {
            throw new InvalidTourException("Scene " + sceneId + " was not found in Tour " + name);
        }

        tourService.deleteScene(name, sceneId);

        TourMessage tourMessage = new TourMessage();
        tourMessage.setName(name);
        tourMessage.setDeletedSceneId(sceneId);
        Task task = new Task(name);
        taskService.save(task);
        tourMessage.setTask(task);

        asyncTourService.sendToToursDeleteSceneFiles(tourMessage);
    }

    @DeleteMapping("/{name}")
    public void deleteScene(@PathVariable("name") String name) {
        Tour tour = tourService.findOne(name);

        if (tour == null) {
            throw new TourNotFoundException("Tour " + name + " does not exist.");
        }

        tourService.delete(tour);

        TourMessage tourMessage = new TourMessage();
        tourMessage.setName(name);
        Task task = new Task(name);
        taskService.save(task);
        tourMessage.setTask(task);

        asyncTourService.sendToToursDeleteFiles(tourMessage);
    }

}
