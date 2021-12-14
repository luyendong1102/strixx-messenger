package net.ldst.chatchik.controllers;

import net.ldst.chatchik.exceptions.ExceedMemberException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

public class ErrorHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    public String exceedMember (Exception e) {
        return "redirect:/error/" + e.getMessage();
    }

}
