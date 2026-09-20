package com.rohith.rohithmart.util;

import com.rohith.rohithmart.exception.ValidationException;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Accumulator for field-level validation errors.
 * Enables collecting multiple errors across complex forms before failing.
 */
public class ValidationResult {
    private final Map<String, String> errors = new LinkedHashMap<>();

    public ValidationResult addError(String field, String message) {
        if (field != null && message != null) {
            errors.put(field, message);
        }
        return this;
    }

    public ValidationResult check(boolean condition, String field, String message) {
        if (!condition) {
            addError(field, message);
        }
        return this;
    }

    public ValidationResult checkNotBlank(String field, String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            addError(field, message);
        }
        return this;
    }

    public ValidationResult checkNotNull(String field, Object value, String message) {
        if (value == null) {
            addError(field, message);
        }
        return this;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public Map<String, String> getErrors() {
        return Collections.unmodifiableMap(errors);
    }

    public void throwIfInvalid() throws ValidationException {
        if (hasErrors()) {
            throw new ValidationException("Validation failed with " + errors.size() + " error(s)", errors);
        }
    }

    public void throwIfInvalid(String mainMessage) throws ValidationException {
        if (hasErrors()) {
            throw new ValidationException(mainMessage, errors);
        }
    }
}
