package app.allstackproject.privideo.common.annotation;

import app.allstackproject.privideo.common.validator.EnumValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = EnumValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumConstraint {

    /**
     * 특정 enum 클래스의 값만 허용하도록 검증하는 커스텀 어노테이션
     */

    String message() default "유효하지 않은 Enum 값입니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    Class<? extends Enum<?>> enumClass();
}