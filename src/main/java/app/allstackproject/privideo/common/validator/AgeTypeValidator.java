package app.allstackproject.privideo.common.validator;

import app.allstackproject.privideo.common.annotation.AgeTypeConstraint;
import app.allstackproject.privideo.common.enumStatus.AgeType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AgeTypeValidator implements ConstraintValidator<AgeTypeConstraint, Integer> {
    private Set<Integer> allowed;

    @Override
    public void initialize(AgeTypeConstraint constraintAnnotation) {
        allowed = Stream.of(AgeType.values())
                .map(AgeType::getValue)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        return value != null && allowed.contains(value);
    }
}