package com.leafyjava.pannellumtourmaker.controllers;

import com.leafyjava.pannellumtourmaker.domains.TourGroup;
import com.leafyjava.pannellumtourmaker.services.TourGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/app/groups")
public class TourGroupController {

    @Value("${application.domain}")
    private String domain;

    @Value("${spring.application.path}")
    private String path;

    private TourGroupService tourGroupService;

    @Autowired
    public TourGroupController(final TourGroupService tourGroupService) {
        this.tourGroupService = tourGroupService;
    }

    @GetMapping()
    public String PagedTourGroupsView(@RequestParam(value = "terms", defaultValue = "") String terms,
                                      @PageableDefault
                                          @SortDefault.SortDefaults({
                                              @SortDefault(sort = "alias", direction = Sort.Direction.ASC),
                                              @SortDefault(sort = "name", direction = Sort.Direction.ASC)
                                          }) Pageable pageable,
                                      Model model) {
        Page<TourGroup> pagedGroups = tourGroupService.findAll(pageable);

        model.addAttribute("pagedGroups", pagedGroups);

        return "groups/list";
    }

    @GetMapping("{name}")
    public String oneTourGroupView(@PathVariable("name") String groupName,
                               Model model) {
        TourGroup group = tourGroupService.findOne(groupName);

        model.addAttribute("group", group);

        return "groups/item";
    }

    @GetMapping("create")
    public String createTourGroupView(Model model) {
        TourGroup group = new TourGroup();

        model.addAttribute("group", group);

        return "groups/create";
    }

    @GetMapping("{name}/edit")
    public String createTourGroupView(@PathVariable("name") String groupName,
                                      RedirectAttributes redirectAttributes,
                                      Model model) {
        if (!tourGroupService.existsByGroupName(groupName)) {
            redirectAttributes.addFlashAttribute("error",
                String.format("Group name '%s' doesn't exist.", groupName));
            return String.format("redirect:%s%s/groups/%s", domain, path, groupName);
        }

        TourGroup group = tourGroupService.findOne(groupName);

        model.addAttribute("group", group);

        return "groups/edit";
    }

    @PostMapping()
    public String addTourGroup(@ModelAttribute TourGroup tourGroup,
                               RedirectAttributes redirectAttributes) {
        if (tourGroupService.existsByGroupName(tourGroup.getName())) {
            redirectAttributes.addFlashAttribute("error",
                String.format("Group name '%s' was taken.", tourGroup.getName()));

            return String.format("redirect:%s%s/groups/create", domain, path);
        }

        TourGroup group = tourGroupService.insert(tourGroup);

        return String.format("redirect:%s%s/groups/%s", domain, path, group.getName());
    }

    @PutMapping("{name}")
    public String updateTourGroup(@PathVariable("name") String groupName,
                                  @ModelAttribute TourGroup tourGroup,
                                  RedirectAttributes redirectAttributes) {
        if (!groupName.equals(tourGroup.getName())) {
            redirectAttributes.addFlashAttribute("error",
                "Bad request - group name doesn't match");

            return String.format("redirect:%s%s/groups/%s/edit", domain, path, groupName);
        }

        if (!tourGroupService.existsByGroupName(groupName)) {
            redirectAttributes.addFlashAttribute("error",
                String.format("Group name '%s' doesn't exist.", groupName));
            return String.format("redirect:%s%s/groups/%s/edit", domain, path, groupName);
        }

        tourGroupService.update(tourGroup);

        return String.format("redirect:%s%s/groups/%s", domain, path, groupName);
    }

    @DeleteMapping("{name}")
    public String deleteTourGroup(@PathVariable("name") String groupName,
                                  RedirectAttributes redirectAttributes) {
        if (!tourGroupService.existsByGroupName(groupName)) {
            redirectAttributes.addFlashAttribute("error",
                String.format("Group name '%s' doesn't exist.", groupName));

            return String.format("redirect:%s%s/groups/%s", domain, path, groupName);
        }

        tourGroupService.delete(groupName);

        return String.format("redirect:%s%s/groups", domain, path);
    }

}
