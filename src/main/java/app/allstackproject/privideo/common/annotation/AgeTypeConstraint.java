package app.allstackproject.privideo.common.annotation;

import app.allstackproject.privideo.common.validator.AgeTypeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AgeTypeValidator.class)
@Documented
public @interface AgeTypeConstraint {
    String message() default "나이는 10, 20, 30, 40, 50, 60 중 하나여야 합니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
