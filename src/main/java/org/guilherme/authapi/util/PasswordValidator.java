package org.guilherme.authapi.util;

import org.guilherme.authapi.config.AppConfig;
import org.passay.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class PasswordValidator {
    private final org.passay.PasswordValidator validator;

    @Autowired
    public PasswordValidator(AppConfig appConfig) {
        AppConfig.Security security = appConfig.getSecurity();
        validator = new org.passay.PasswordValidator(Arrays.asList(
                new LengthRule(security.getPasswordMinLength(), security.getPasswordMaxLength()),
                new CharacterRule(EnglishCharacterData.UpperCase, security.getPasswordMinUppercase()),
                new CharacterRule(EnglishCharacterData.LowerCase, security.getPasswordMinLowercase()),
                new CharacterRule(EnglishCharacterData.Digit, security.getPasswordMinDigit()),
                new CharacterRule(EnglishCharacterData.Special, security.getPasswordMinSpecial()),
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
