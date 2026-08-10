package com.sujal.itsm.itams.controller;

import com.sujal.itsm.itams.enums.TransactionType;
import com.sujal.itsm.itams.model.Product;
import com.sujal.itsm.itams.model.StockTransaction;
import com.sujal.itsm.itams.model.Warehouse;
import com.sujal.itsm.itams.service.ProductService;
import com.sujal.itsm.itams.service.StockService;
import com.sujal.itsm.itams.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/itams/stock")
@RequiredArgsConstructor
public class StockTransactionController {

    private final StockService stockService;
    private final ProductService productService;
    private final WarehouseService warehouseService;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("transactions", stockService.getAllTransactions()); // ✅ Uncommented and fixed
        model.addAttribute("pageTitle", "Stock Movements");
        return "itams/stock/index";
    }

    @GetMapping("/new")
    public String showForm(Model model) {
        model.addAttribute("products", productService.findAllActive());
        model.addAttribute("warehouses", warehouseService.findAllActive());
        model.addAttribute("types", TransactionType.values());
        model.addAttribute("transaction", new StockTransaction()); // For form binding
        model.addAttribute("pageTitle", "Record Stock Movement");
        return "itams/stock/form";
    }

    @PostMapping
    public String processForm(@RequestParam TransactionType type,
                              @RequestParam Long productId,
                              @RequestParam Integer quantity,
                              @RequestParam(required = false) Long fromWarehouseId,
                              @RequestParam(required = false) Long toWarehouseId,
                              @RequestParam(required = false) String reference,
                              @RequestParam(required = false) String notes,
                              RedirectAttributes redirectAttributes) {
        try {
            stockService.recordTransaction(type, productId, quantity, fromWarehouseId, toWarehouseId, reference, notes);
            redirectAttributes.addFlashAttribute("success", "Stock movement recorded successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/itams/stock/new";
    }


}