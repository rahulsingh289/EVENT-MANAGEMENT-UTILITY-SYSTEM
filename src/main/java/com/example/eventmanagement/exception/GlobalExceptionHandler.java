package com.example.eventmanagement.exception;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@Controller
public class GlobalExceptionHandler implements ErrorController {

    // ── Replaces Spring's whitelabel /error page ──────────────────────────
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object errorMessage = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        int status = statusCode != null ? Integer.parseInt(statusCode.toString()) : 500;

        String message;
        if (status == 404) {
            message = "The page you're looking for doesn't exist.";
        } else if (errorMessage != null && !errorMessage.toString().isBlank()) {
            message = errorMessage.toString();
        } else if (exception instanceof Throwable t && t.getMessage() != null) {
            message = t.getMessage();
        } else {
            message = "Something went wrong. Please try again.";
        }

        model.addAttribute("status", status);
        model.addAttribute("message", message);
        return "error";
    }

    // ── @ControllerAdvice handlers for exceptions thrown in controllers ────
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleRuntimeException(RuntimeException ex, Model model) {
        model.addAttribute("status", 500);
        model.addAttribute("message", ex.getMessage() != null ? ex.getMessage() : "Something went wrong.");
        return "error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception ex, Model model) {
        model.addAttribute("status", 500);
        model.addAttribute("message", "An unexpected error occurred. Please try again.");
        return "error";
    }
}
