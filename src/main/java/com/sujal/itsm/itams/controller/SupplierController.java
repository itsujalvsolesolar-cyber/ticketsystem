package com.sujal.itsm.itams.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sujal.itsm.itams.model.Supplier;
import com.sujal.itsm.itams.service.SupplierService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/itams/suppliers")
@RequiredArgsConstructor
public class SupplierController {

  private final SupplierService supplierService;

  @GetMapping
  public String list(Model model) {
    model.addAttribute("suppliers", supplierService.findAll());
    model.addAttribute("totalSuppliers", supplierService.count());
    model.addAttribute("activeSuppliers", supplierService.countActive());
    return "itams/suppliers/index";
  }

  @GetMapping("/new")
  public String showCreateForm(Model model) {
    model.addAttribute("supplier", new Supplier());
    model.addAttribute("pageTitle", "New Supplier");
    return "itams/suppliers/form";
  }

  @PostMapping
  public String create(
      @Valid @ModelAttribute("supplier") Supplier supplier,
      BindingResult result,
      RedirectAttributes redirectAttributes,
      Model model) {
    if (result.hasErrors()) {
      return "itams/suppliers/form";
    }

    try {
      supplierService.create(supplier);
      redirectAttributes.addFlashAttribute("success", "Supplier created successfully!");
      return "redirect:/itams/suppliers";
    } catch (IllegalArgumentException e) {
      model.addAttribute("error", e.getMessage());
      return "itams/suppliers/form";
    }
  }

  @GetMapping("/{id}/edit")
  public String showEditForm(@PathVariable Long id, Model model) {
    Supplier supplier = supplierService.findById(id);
    model.addAttribute("supplier", supplier);
    model.addAttribute("pageTitle", "Edit Supplier");
    return "itams/suppliers/form";
  }

  @PostMapping("/{id}")
  public String update(
      @PathVariable Long id,
      @Valid @ModelAttribute("supplier") Supplier supplier,
      BindingResult result,
      RedirectAttributes redirectAttributes,
      Model model) {
    if (result.hasErrors()) {
      return "itams/suppliers/form";
    }

    try {
      supplierService.update(id, supplier);
      redirectAttributes.addFlashAttribute("success", "Supplier updated successfully!");
      return "redirect:/itams/suppliers";
    } catch (IllegalArgumentException e) {
      model.addAttribute("error", e.getMessage());
      return "itams/suppliers/form";
    }
  }

  @PostMapping("/{id}/delete")
  public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    try {
      supplierService.delete(id);
      redirectAttributes.addFlashAttribute("success", "Supplier deactivated successfully!");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "Failed to delete supplier: " + e.getMessage());
    }
    return "redirect:/itams/suppliers";
  }
}
