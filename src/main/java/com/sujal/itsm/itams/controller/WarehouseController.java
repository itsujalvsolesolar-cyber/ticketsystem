package com.sujal.itsm.itams.controller;

import com.sujal.itsm.itams.model.Warehouse;
import com.sujal.itsm.itams.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/itams/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("warehouses", warehouseService.findAllActive());
        model.addAttribute("pageTitle", "Warehouses");
        return "itams/warehouses/index";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("warehouse", new Warehouse());
        model.addAttribute("pageTitle", "New Warehouse");
        return "itams/warehouses/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("warehouse") Warehouse warehouse,
                         BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) return "itams/warehouses/form";
        warehouseService.create(warehouse);
        redirectAttributes.addFlashAttribute("success", "Warehouse created successfully!");
        return "redirect:/itams/warehouses";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("warehouse", warehouseService.findById(id));
        model.addAttribute("pageTitle", "Edit Warehouse");
        return "itams/warehouses/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("warehouse") Warehouse warehouse,
                         BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) return "itams/warehouses/form";
        warehouseService.update(id, warehouse);
        redirectAttributes.addFlashAttribute("success", "Warehouse updated successfully!");
        return "redirect:/itams/warehouses";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        warehouseService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Warehouse deactivated successfully!");
        return "redirect:/itams/warehouses";
    }
}