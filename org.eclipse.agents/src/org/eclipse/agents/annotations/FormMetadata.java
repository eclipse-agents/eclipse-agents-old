/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.agents.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to provide form/UI metadata for MCP tool parameters.
 * This follows the MCP specification for form support, allowing
 * UI hints to be provided for better user experience.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface FormMetadata {

    /**
     * The widget type for rendering this parameter.
     * Common values: "text", "textarea", "select", "checkbox", "radio",
     * "number", "date", "file", "password"
     */
    String widget() default "text";

    /**
     * For select/radio widgets, the available options
     */
    String[] options() default {};

    /**
     * Placeholder text for text inputs
     */
    String placeholder() default "";

    /**
     * Whether this field should be rendered as required in the UI
     */
    boolean required() default false;

    /**
     * Minimum value for number inputs
     */
    double min() default Double.MIN_VALUE;

    /**
     * Maximum value for number inputs
     */
    double max() default Double.MAX_VALUE;

    /**
     * Pattern for validation (regex)
     */
    String pattern() default "";

    /**
     * Help text to display with the field
     */
    String helpText() default "";
}
