/*
 * Created on May 19, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.binding.impl;

import org.openl.binding.IBindingContext;
import org.openl.binding.IBoundNode;
import org.openl.syntax.ISyntaxNode;
import org.openl.types.IMethodCaller;
import org.openl.types.IOpenClass;

/**
 * @author snshor
 */

public class BinaryOperatorOrNodeBinder extends BinaryOperatorNodeBinder {

    @Override
    public IBoundNode bind(ISyntaxNode node, IBindingContext bindingContext) throws Exception {

        if (node.getNumberOfChildren() != 2) {
            throw new BoundError(node, "Binary node must have 2 subnodes", null);
        }

        IBoundNode[] children = bindChildren(node, bindingContext);

        IOpenClass[] types = getTypes(children);

        if ((types[0].getInstanceClass() == boolean.class || types[0].getInstanceClass() == Boolean.class)
                && (types[1].getInstanceClass() == boolean.class || types[1].getInstanceClass() == Boolean.class)) {
            return new BinaryOpNodeOr(node, children);
        }

        int index = node.getType().lastIndexOf('.');

        String methodName = node.getType().substring(index + 1);

        IMethodCaller om = findBinaryOperatorMethodCaller(methodName, types, bindingContext);

        if (om == null) {
            throw new BoundError(node, errorMsg(methodName, types[0], types[1]));
        }

        return new BinaryOpNode(node, children, om);

    }

}
