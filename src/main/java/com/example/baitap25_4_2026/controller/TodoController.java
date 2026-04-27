package com.example.baitap25_4_2026.controller;

import com.example.baitap25_4_2026.model.dto.TodoDTO;
import com.example.baitap25_4_2026.service.TodoService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TodoController {
    private static final String OWNER_NAME_SESSION_KEY = "ownerName";
    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/todos";
    }

    @GetMapping("/todos")
    public String listTodos(Model model, HttpSession session) {
        String ownerName = getOwnerName(session);
        if (ownerName == null) {
            return "redirect:/owner";
        }

        model.addAttribute("ownerName", ownerName);
        model.addAttribute("todos", todoService.findAll());
        return "todos";
    }

    @GetMapping("/owner")
    public String showOwnerForm(HttpSession session, Model model) {
        Object ownerName = session.getAttribute(OWNER_NAME_SESSION_KEY);
        model.addAttribute("ownerName", ownerName == null ? "" : ownerName.toString());
        return "ownerForm";
    }

    @PostMapping("/owner")
    public String saveOwner(@RequestParam("ownerName") String ownerName, HttpSession session, Model model) {
        String normalizedOwnerName = normalizeOwnerName(ownerName);
        if (normalizedOwnerName.isEmpty()) {
            model.addAttribute("ownerName", "");
            model.addAttribute("ownerError", "Vui long nhap ten chu so huu.");
            return "ownerForm";
        }

        session.setAttribute(OWNER_NAME_SESSION_KEY, normalizedOwnerName);
        return "redirect:/todos";
    }

    @GetMapping("/add-todo")
    public String showAddTodo(Model model, HttpSession session) {
        if (getOwnerName(session) == null) {
            return "redirect:/owner";
        }

        model.addAttribute("todo", new TodoDTO());
        return "todoForm";
    }

    @PostMapping("/add-todo")
    public String addTodo(@Valid @ModelAttribute("todo") TodoDTO todoDTO,
                          BindingResult bindingResult,
                          HttpSession session) {
        if (getOwnerName(session) == null) {
            return "redirect:/owner";
        }

        if (bindingResult.hasErrors()) {
            return "todoForm";
        }
        todoService.addToDo(todoDTO);
        return "redirect:/todos";
    }

    @GetMapping("/edit-todo/{id}")
    public String showEditTodo(@PathVariable Long id, Model model, HttpSession session) {
        if (getOwnerName(session) == null) {
            return "redirect:/owner";
        }

        return todoService.findById(id)
                .map(todo -> {
                    model.addAttribute("todo", todo);
                    return "todoForm";
                })
                .orElse("redirect:/todos");
    }

    @PostMapping("/edit-todo/{id}")
    public String updateTodo(@PathVariable Long id,
                             @Valid @ModelAttribute("todo") TodoDTO todoDTO,
                             BindingResult bindingResult,
                             HttpSession session) {
        if (getOwnerName(session) == null) {
            return "redirect:/owner";
        }

        todoDTO.setId(id);
        if (bindingResult.hasErrors()) {
            return "todoForm";
        }
        todoService.updateTodo(todoDTO);
        return "redirect:/todos";
    }

    @PostMapping("/delete-todo/{id}")
    public String deleteTodo(@PathVariable Long id, HttpSession session) {
        if (getOwnerName(session) == null) {
            return "redirect:/owner";
        }

        todoService.deleteTodo(id);
        return "redirect:/todos";
    }

    private String getOwnerName(HttpSession session) {
        return normalizeOwnerName((String) session.getAttribute(OWNER_NAME_SESSION_KEY));
    }

    private String normalizeOwnerName(String ownerName) {
        if (ownerName == null) {
            return null;
        }

        String normalizedOwnerName = ownerName.trim();
        return normalizedOwnerName.isEmpty() ? null : normalizedOwnerName;
    }
}
