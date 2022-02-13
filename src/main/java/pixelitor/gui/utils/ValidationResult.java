/*
 * Copyright 2021 Laszlo Balazs-Csiki and Contributors
 *
 * This file is part of Pixelitor. Pixelitor is free software: you
 * can redistribute it and/or modify it under the terms of the GNU
 * General Public License, version 3 as published by the Free
 * Software Foundation.
 *
 * Pixelitor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Pixelitor. If not, see <http://www.gnu.org/licenses/>.
 */

package pixelitor.gui.utils;

import java.awt.Component;

import static java.lang.String.format;

/**
 * Represents the result of a validation.
 * This is an immutable object.
 */
public class ValidationResult {
    private final boolean valid;
    private final String errorMsg;

    // The OK result has no error message, therefore it can be shared
    private static final ValidationResult okInstance
        = new ValidationResult(true, null);

    private ValidationResult(boolean valid, String errorMsg) {
        this.valid = valid;
        this.errorMsg = errorMsg;

        if (!valid && errorMsg == null) {
            throw new IllegalArgumentException("missing error message");
        }
    }

    /**
     * Factory method for the OK result
     */
    public static ValidationResult ok() {
        return okInstance;
    }

    /**
     * Factory method for an error
     */
    public static ValidationResult error(String msg) {
        return new ValidationResult(false, msg);
    }

    /**
     * Returns a composed result that represents a
     * logical AND of this result and the given one.
     */
    public ValidationResult and(ValidationResult other) {
        if (valid) {
            assert this == okInstance;
            if (other.isOK()) {
                return okInstance; // both are OK
            } else {
                return other; // with the other's error message
            }
        } else {
            if (other.isOK()) {
                return this; // with our error message
            } else {
                return error(composeMessages(errorMsg, other.errorMsg));
            }
        }
    }

    public ValidationResult addError(String msg) {
        if (valid) {
            assert this == okInstance;
            return new ValidationResult(false, msg);
        } else {
            return new ValidationResult(false, composeMessages(errorMsg, msg));
        }
    }

    public ValidationResult addErrorIfNot(boolean condition, String msg) {
        return addErrorIf(!condition, msg);
    }

    public ValidationResult addErrorIf(boolean condition, String msg) {
        if (valid) {
            assert this == okInstance;
            if (condition) {
                return error(msg);
            } else {
                return okInstance;
            }
        } else {
            if (condition) {
                return error(composeMessages(errorMsg, msg));
            } else {
                return this;
            }
        }
    }

    public boolean isOK() {
        return valid;
    }

    public void showErrorDialog(Component dialogParent) {
        assert !errorMsg.startsWith("<html>") : "errorMsg = " + errorMsg;
        Dialogs.showErrorDialog(dialogParent, "Error", "<html>" + errorMsg);
    }

    public ValidationResult addErrorIfZero(int value, String fieldName) {
        if (value == 0) {
            return addError(format("<b>%s</b> can't be zero.", fieldName));
        } else {
            return this;
        }
    }

    public ValidationResult addErrorIfNegative(int value, String fieldName) {
        if (value < 0) {
            return addError(format("<b>%s</b> must be positive.", fieldName));
        } else {
            return this;
        }
    }

    private static String composeMessages(String first, String second) {
        return first + "<br>" + second;
    }
}
