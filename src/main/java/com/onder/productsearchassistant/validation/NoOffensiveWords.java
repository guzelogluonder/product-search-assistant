package com.onder.productsearchassistant.validation;


import java.lang.annotation.*;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = NoOffensiveWordsValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NoOffensiveWords {
    String message() default "Query contains inappropriate language";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
