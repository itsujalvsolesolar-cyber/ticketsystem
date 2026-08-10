package com.sujal.itsm.itams.controller;

import com.sujal.itsm.itams.model.SoftwareCatalog;
import com.sujal.itsm.itams.service.SoftwareCatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/itams/software-catalog")
@RequiredArgsConstructor
public class SoftwareCatalogController {

    private final SoftwareCatalogService catalogService;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("catalogs", catalogService.findAllActive());
        model.addAttribute("pageTitle", "Software Catalog");
        return "itams/software-catalog/index";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("catalog", new SoftwareCatalog());
        model.addAttribute("pageTitle", "Add Software to Catalog");
        return "itams/software-catalog/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("catalog") SoftwareCatalog catalog,
                         BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Add Software to Catalog");
            return "itams/software-catalog/form";
        }
        catalogService.create(catalog);
        redirectAttributes.addFlashAttribute("success", "Software added to catalog successfully!");
        return "redirect:/itams/software-catalog";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("catalog", catalogService.findById(id));
        model.addAttribute("pageTitle", "Edit Software Catalog");
        return "itams/software-catalog/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("catalog") SoftwareCatalog catalog,
                         BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Edit Software Catalog");
            return "itams/software-catalog/form";
        }
        catalogService.update(id, catalog);
        redirectAttributes.addFlashAttribute("success", "Software catalog updated successfully!");
        return "redirect:/itams/software-catalog";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        catalogService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Software removed from catalog successfully!");
        return "redirect:/itams/software-catalog";
    }
}