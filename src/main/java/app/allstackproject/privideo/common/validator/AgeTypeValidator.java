package app.allstackproject.privideo.common.validator;

import app.allstackproject.privideo.common.annotation.AgeTypeConstraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Set;

public class AgeTypeValidator implements ConstraintValidator<AgeTypeConstraint, Integer> {
    private static final Set<Integer> ALLOWED_AGES = Set.of(10, 20, 30, 40, 50);

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        return value != null && ALLOWED_AGES.contains(value);
    }
}
