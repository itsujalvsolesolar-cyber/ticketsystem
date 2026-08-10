package com.sujal.itsm.itams.controller;

import com.sujal.itsm.itams.model.Product;
import com.sujal.itsm.itams.service.AssetCategoryService;
import com.sujal.itsm.itams.service.BrandService;
import com.sujal.itsm.itams.service.ProductService;
import com.sujal.itsm.itams.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/itams/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final AssetCategoryService categoryService;
    private final BrandService brandService;
    private final SupplierService supplierService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", productService.findAllActive());
        model.addAttribute("pageTitle", "Products");
        return "itams/products/index";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.findAllActive());
        model.addAttribute("brands", brandService.findAllActive());
        model.addAttribute("suppliers", supplierService.findAllActive());
        model.addAttribute("pageTitle", "New Product");
        return "itams/products/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("product") Product product,
                         BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("product", product);
            model.addAttribute("pageTitle", "New Product");
            model.addAttribute("categories", categoryService.findAllActive());
            model.addAttribute("brands", brandService.findAllActive());
            model.addAttribute("suppliers", supplierService.findAllActive());
            return "itams/products/form";
        }
        productService.create(product);
        redirectAttributes.addFlashAttribute("success", "Product created successfully!");
        return "redirect:/itams/products";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Product product = productService.findById(id);
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.findAllActive());
        model.addAttribute("brands", brandService.findAllActive());
        model.addAttribute("suppliers", supplierService.findAllActive());
        model.addAttribute("pageTitle", "Edit Product");
        return "itams/products/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("product") Product product,
                         BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("product", product);
            model.addAttribute("pageTitle", "Edit Product");
            model.addAttribute("categories", categoryService.findAllActive());
            model.addAttribute("brands", brandService.findAllActive());
            model.addAttribute("suppliers", supplierService.findAllActive());
            return "itams/products/form";
        }
        productService.update(id, product);
        redirectAttributes.addFlashAttribute("success", "Product updated successfully!");
        return "redirect:/itams/products";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Product deactivated successfully!");
        return "redirect:/itams/products";
    }
}