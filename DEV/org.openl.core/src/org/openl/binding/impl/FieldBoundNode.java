/*
 * Created on May 19, 2003 Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.binding.impl;

import org.openl.binding.BindingDependencies;
import org.openl.binding.IBoundNode;
import org.openl.exception.OpenLRuntimeException;
import org.openl.syntax.ISyntaxNode;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenField;
import org.openl.vm.IRuntimeEnv;

/**
 * @author snshor
 * 
 */
public class FieldBoundNode extends ATargetBoundNode {

    private IOpenField boundField;

    public FieldBoundNode(ISyntaxNode syntaxNode, IOpenField field) {
        this(syntaxNode, field, null);        
    }

    public FieldBoundNode(ISyntaxNode syntaxNode, IOpenField field, IBoundNode target) {
        super(syntaxNode, new IBoundNode[0], target);
        boundField = field;
    }

    @Override
    public void assign(Object value, IRuntimeEnv env) throws OpenLRuntimeException {
        Object target = getTargetNode() == null ? env.getThis() : getTargetNode().evaluate(env);

        boundField.set(target, value, env);
    }

    public String getFieldName() {
        return boundField.getName();
    }

    public IOpenField getBoundField() {
        return boundField;
    }

    public Object evaluateRuntime(IRuntimeEnv env) throws OpenLRuntimeException {
        Object target = getTargetNode() == null ? env.getThis() : getTargetNode().evaluate(env);

        return boundField.get(target, env);
    }

    public IOpenClass getType() {
        return boundField.getType();
    }

    @Override
    public boolean isLvalue() {
        return boundField.isWritable();
    }

    @Override
    public void updateAssignFieldDependency(BindingDependencies dependencies) {
        dependencies.addAssignField(boundField, this);
    }

    @Override
    public void updateDependency(BindingDependencies dependencies) {
        dependencies.addFieldDependency(boundField, this);
    }

    @Override
    public boolean isLiteralExpressionParent() {
        return boundField.isConst();
    }
}
