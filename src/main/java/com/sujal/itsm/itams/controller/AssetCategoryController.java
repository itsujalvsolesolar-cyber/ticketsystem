package com.sujal.itsm.itams.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sujal.itsm.itams.model.AssetCategory;
import com.sujal.itsm.itams.service.AssetCategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/itams/categories")
@RequiredArgsConstructor
public class AssetCategoryController {

  private final AssetCategoryService categoryService;

  @GetMapping
  public String list(Model model) {
    model.addAttribute("categories", categoryService.findAll());
    model.addAttribute("totalCategories", categoryService.count());
    model.addAttribute("activeCategories", categoryService.countActive());
    return "itams/categories/index";
  }

  @GetMapping("/new")
  public String showCreateForm(Model model) {
    model.addAttribute("category", new AssetCategory());
    model.addAttribute("pageTitle", "New Category");
    return "itams/categories/form";
  }

  @PostMapping
  public String create(
      @Valid @ModelAttribute("category") AssetCategory category,
      BindingResult result,
      RedirectAttributes redirectAttributes,
      Model model) {
    if (result.hasErrors()) {
      return "itams/categories/form";
    }

    try {
      categoryService.create(category);
      redirectAttributes.addFlashAttribute("success", "Asset category created successfully!");
      return "redirect:/itams/categories";
    } catch (IllegalArgumentException e) {
      model.addAttribute("error", e.getMessage());
      return "itams/categories/form";
    }
  }

  @GetMapping("/{id}/edit")
  public String showEditForm(@PathVariable Long id, Model model) {
    AssetCategory category = categoryService.findById(id);
    model.addAttribute("category", category);
    model.addAttribute("pageTitle", "Edit Category");
    return "itams/categories/form";
  }

  @PostMapping("/{id}")
  public String update(
      @PathVariable Long id,
      @Valid @ModelAttribute("category") AssetCategory category,
      BindingResult result,
      RedirectAttributes redirectAttributes,
      Model model) {
    if (result.hasErrors()) {
      return "itams/categories/form";
    }

    try {
      categoryService.update(id, category);
      redirectAttributes.addFlashAttribute("success", "Asset category updated successfully!");
      return "redirect:/itams/categories";
    } catch (IllegalArgumentException e) {
      model.addAttribute("error", e.getMessage());
      return "itams/categories/form";
    }
  }

  @PostMapping("/{id}/delete")
  public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    try {
      categoryService.delete(id);
      redirectAttributes.addFlashAttribute("success", "Asset category deactivated successfully!");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "Failed to delete category: " + e.getMessage());
    }
    return "redirect:/itams/categories";
  }
}
