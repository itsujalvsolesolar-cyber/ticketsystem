package com.sujal.itsm.itams.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sujal.itsm.itams.model.Brand;
import com.sujal.itsm.itams.service.BrandService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/itams/brands")
@RequiredArgsConstructor
public class BrandController {

  private final BrandService brandService;

  @GetMapping
  public String list(Model model) {
    model.addAttribute("brands", brandService.findAll());
    model.addAttribute("totalBrands", brandService.count());
    model.addAttribute("activeBrands", brandService.countActive());
    return "itams/brands/index";
  }

  @GetMapping("/new")
  public String showCreateForm(Model model) {
    model.addAttribute("brand", new Brand());
    model.addAttribute("pageTitle", "New Brand");
    return "itams/brands/form";
  }

  @PostMapping
  public String create(
      @Valid @ModelAttribute("brand") Brand brand,
      BindingResult result,
      RedirectAttributes redirectAttributes,
      Model model) {
    if (result.hasErrors()) {
      return "itams/brands/form";
    }

    try {
      brandService.create(brand);
      redirectAttributes.addFlashAttribute("success", "Brand created successfully!");
      return "redirect:/itams/brands";
    } catch (IllegalArgumentException e) {
      model.addAttribute("error", e.getMessage());
      return "itams/brands/form";
    }
  }

  @GetMapping("/{id}/edit")
  public String showEditForm(@PathVariable Long id, Model model) {
    Brand brand = brandService.findById(id);
    model.addAttribute("brand", brand);
    model.addAttribute("pageTitle", "Edit Brand");
    return "itams/brands/form";
  }

  @PostMapping("/{id}")
  public String update(
      @PathVariable Long id,
      @Valid @ModelAttribute("brand") Brand brand,
      BindingResult result,
      RedirectAttributes redirectAttributes,
      Model model) {
    if (result.hasErrors()) {
      return "itams/brands/form";
    }

    try {
      brandService.update(id, brand);
      redirectAttributes.addFlashAttribute("success", "Brand updated successfully!");
      return "redirect:/itams/brands";
    } catch (IllegalArgumentException e) {
      model.addAttribute("error", e.getMessage());
      return "itams/brands/form";
    }
  }

  @PostMapping("/{id}/delete")
  public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    try {
      brandService.delete(id);
      redirectAttributes.addFlashAttribute("success", "Brand deactivated successfully!");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "Failed to delete brand: " + e.getMessage());
    }
    return "redirect:/itams/brands";
  }
}
