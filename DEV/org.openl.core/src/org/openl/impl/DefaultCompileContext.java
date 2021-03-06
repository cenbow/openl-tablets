package org.openl.impl;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.openl.ICompileContext;
import org.openl.validation.IOpenLValidator;

/**
 * Default implementation of the {@link ICompileContext} interface.
 * 
 */
public class DefaultCompileContext implements ICompileContext {

    /**
     * Flag that indicates that validation is enabled.
     */
    private boolean validationEnabled = true;

    /**
     * Set of validators that will be used in validation process.
     */
    private Set<IOpenLValidator> validators = new CopyOnWriteArraySet<IOpenLValidator>();

    /**
     * {@inheritDoc}
     */
    public void addValidator(IOpenLValidator validator) {
        validators.add(validator);
    }

    /**
     * {@inheritDoc}
     */
    public void addValidators(List<IOpenLValidator> validators) {

        for (IOpenLValidator validator : validators) {
            addValidator(validator);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeValidator(IOpenLValidator validator) {
        validators.remove(validator);
    }

    /**
     * {@inheritDoc}
     */
    public void removeValidators() {
        validators = new CopyOnWriteArraySet<IOpenLValidator>();
    }

    /**
     * {@inheritDoc}
     */
    public Set<IOpenLValidator> getValidators() {
        return validators;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isValidationEnabled() {
        return validationEnabled;
    }

    /**
     * {@inheritDoc}
     */
    public void setValidationEnabled(boolean validationEnabled) {
        this.validationEnabled = validationEnabled;
    }

}
