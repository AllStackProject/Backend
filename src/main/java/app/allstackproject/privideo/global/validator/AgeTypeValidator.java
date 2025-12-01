package app.allstackproject.privideo.global.validator;

import app.allstackproject.privideo.global.annotation.AgeTypeConstraint;
import app.allstackproject.privideo.domain.member.enums.AgeType;
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