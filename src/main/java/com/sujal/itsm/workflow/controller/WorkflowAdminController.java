package com.sujal.itsm.workflow.controller;

import com.sujal.itsm.core.user.repository.RoleRepository;
import com.sujal.itsm.workflow.enums.WorkflowModuleType;
import com.sujal.itsm.workflow.model.WorkflowDefinition;
import com.sujal.itsm.workflow.model.WorkflowStep;
import com.sujal.itsm.workflow.repository.WorkflowDefinitionRepository;
import com.sujal.itsm.workflow.repository.WorkflowStepRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/workflows")
@RequiredArgsConstructor
public class WorkflowAdminController {

    private final WorkflowDefinitionRepository workflowDefinitionRepository;
    private final WorkflowStepRepository workflowStepRepository;
    private final RoleRepository roleRepository;

    @GetMapping
    public String listWorkflows(Model model) {
        model.addAttribute("workflows", workflowDefinitionRepository.findAll());
        model.addAttribute("pageTitle", "Workflow Builder");
        return "admin/workflows/index";
    }

    @GetMapping("/new")
    public String createWorkflowForm(Model model) {
        model.addAttribute("workflow", new WorkflowDefinition());
        model.addAttribute("moduleTypes", WorkflowModuleType.values());
        model.addAttribute("roles", roleRepository.findAll());

        List<WorkflowStep> steps = new ArrayList<>();
        steps.add(new WorkflowStep());
        model.addAttribute("steps", steps);
        model.addAttribute("pageTitle", "New Workflow");

        return "admin/workflows/form";
    }

    @PostMapping("/new")
    public String saveWorkflow(@RequestParam String name,
                               @RequestParam WorkflowModuleType moduleType,
                               @RequestParam(required = false) String description,
                               @RequestParam(required = false) List<String> stepRoles,
                               RedirectAttributes redirectAttributes) {
        try {
            WorkflowDefinition definition = WorkflowDefinition.builder()
                    .name(name)
                    .moduleType(moduleType)
                    .description(description)
                    .isActive(true)
                    .build();

            definition = workflowDefinitionRepository.save(definition);

            if (stepRoles != null) {
                for (int i = 0; i < stepRoles.size(); i++) {
                    String role = stepRoles.get(i);
                    if (role != null && !role.trim().isEmpty()) {
                        WorkflowStep step = WorkflowStep.builder()
                                .workflowDefinition(definition)
                                .stepOrder(i + 1)
                                .approverRole(role)
                                .isRequired(true)
                                .build();
                        workflowStepRepository.save(step);
                    }
                }
            }
            redirectAttributes.addFlashAttribute("success", "Workflow created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create workflow: " + e.getMessage());
        }
        return "redirect:/admin/workflows";
    }

    @GetMapping("/{id}/edit")
    public String editWorkflowForm(@PathVariable Long id, Model model) {
        WorkflowDefinition workflow = workflowDefinitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workflow not found"));
        model.addAttribute("workflow", workflow);
        model.addAttribute("moduleTypes", WorkflowModuleType.values());
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("steps", workflow.getSteps() != null ? workflow.getSteps() : new ArrayList<>());
        model.addAttribute("pageTitle", "Edit Workflow");
        return "admin/workflows/form";
    }

    @PostMapping("/{id}")
    public String updateWorkflow(@PathVariable Long id,
                                 @RequestParam String name,
                                 @RequestParam WorkflowModuleType moduleType,
                                 @RequestParam(required = false) String description,
                                 @RequestParam(required = false) List<String> stepRoles,
                                 RedirectAttributes redirectAttributes) {
        try {
            WorkflowDefinition definition = workflowDefinitionRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Workflow not found"));

            definition.setName(name);
            definition.setModuleType(moduleType);
            definition.setDescription(description);
            workflowDefinitionRepository.save(definition);

            if (definition.getSteps() != null) {
                workflowStepRepository.deleteAll(definition.getSteps());
                definition.getSteps().clear();
            }

            if (stepRoles != null) {
                for (int i = 0; i < stepRoles.size(); i++) {
                    String role = stepRoles.get(i);
                    if (role != null && !role.trim().isEmpty()) {
                        WorkflowStep step = WorkflowStep.builder()
                                .workflowDefinition(definition)
                                .stepOrder(i + 1)
                                .approverRole(role)
                                .isRequired(true)
                                .build();
                        workflowStepRepository.save(step);
                    }
                }
            }
            redirectAttributes.addFlashAttribute("success", "Workflow updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update workflow: " + e.getMessage());
        }
        return "redirect:/admin/workflows";
    }

    @PostMapping("/{id}/toggle")
    public String toggleWorkflow(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        WorkflowDefinition workflow = workflowDefinitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workflow not found"));
        workflow.setIsActive(!workflow.getIsActive());
        workflowDefinitionRepository.save(workflow);
        redirectAttributes.addFlashAttribute("success", "Workflow status updated!");
        return "redirect:/admin/workflows";
    }
}