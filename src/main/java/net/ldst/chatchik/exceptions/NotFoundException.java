package net.ldst.chatchik.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class NotFoundException extends Exception{

    private String message;

    @Override
    public String getMessage() {
        return message;
    }
}
