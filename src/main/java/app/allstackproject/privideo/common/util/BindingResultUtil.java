package app.allstackproject.privideo.common.util;

import org.springframework.validation.BindingResult;

public class BindingResultUtil {

    public static String getErrorMessage(BindingResult bindingResult) {
        StringBuilder errorMessage = new StringBuilder();
        bindingResult.getAllErrors().forEach(error -> errorMessage.append(error.getDefaultMessage()).append(" "));
        return errorMessage.toString();
    }
}
