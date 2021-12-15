package net.ldst.chatchik.exceptions;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class GeneralException extends Exception{
    private String message;

    @Override
    public String getMessage() {
        return message;
    }
}
