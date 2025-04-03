package org.guilherme.authapi.util;

import org.passay.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class PasswordValidator {
    private final org.passay.PasswordValidator validator;

    public PasswordValidator() {
        validator = new org.passay.PasswordValidator(Arrays.asList(
                new LengthRule(8, 30),
                new CharacterRule(EnglishCharacterData.UpperCase, 1),
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                new CharacterRule(EnglishCharacterData.Digit, 1),
                new CharacterRule(EnglishCharacterData.Special, 1),
                new WhitespaceRule(),
                new IllegalSequenceRule(EnglishSequenceData.Alphabetical, 5, false),
                new IllegalSequenceRule(EnglishSequenceData.Numerical, 5, false)
        ));
    }

    public List<String> validate(String password) {
        RuleResult result = validator.validate(new PasswordData(password));
        if (result.isValid()) {
            return List.of();
        }
        return validator.getMessages(result);
    }
}
