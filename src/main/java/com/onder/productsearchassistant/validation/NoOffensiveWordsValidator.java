package com.onder.productsearchassistant.validation;

import java.util.List;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NoOffensiveWordsValidator implements ConstraintValidator<NoOffensiveWords, String> {

    private static final List<String> OFFENSIVE_WORDS = List.of("badword1", "badword2");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return OFFENSIVE_WORDS.stream().noneMatch(value.toLowerCase()::contains);
    }
}
