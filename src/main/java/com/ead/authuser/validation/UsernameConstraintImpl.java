package com.ead.authuser.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UsernameConstraintImpl implements ConstraintValidator<UsernameConstraint, String> {

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        return isUsernameValid(username);
    }

    private boolean isUsernameValid(String username){
        return username != null && !username.trim().isEmpty() && !username.contains(" ");
    }
}
