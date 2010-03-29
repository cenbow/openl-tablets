package org.openl.syntax.exception;

import org.openl.exception.OpenLCompilationException;
import org.openl.main.SourceCodeURLTool;
import org.openl.source.IOpenSourceCodeModule;
import org.openl.syntax.ISyntaxNode;
import org.openl.util.text.ILocation;

public class SyntaxNodeException extends OpenLCompilationException {

    private static final long serialVersionUID = 4448924727461016950L;

    private ISyntaxNode syntaxNode;
    private ISyntaxNode topLevelSyntaxNode;
//    private String phase;

    public SyntaxNodeException(String message, Throwable cause, ILocation location, IOpenSourceCodeModule source) {
        super(message, cause, location, source);
    }

    public SyntaxNodeException(String message, Throwable cause, ISyntaxNode syntaxNode) {
        this(message, cause, syntaxNode == null ? null : syntaxNode.getSourceLocation(), null);

        this.syntaxNode = syntaxNode;
    }

    public IOpenSourceCodeModule getSourceModule() {

        IOpenSourceCodeModule source = super.getSourceModule();

        if (source != null) {
            return source;
        } else if (syntaxNode != null) {
            return syntaxNode.getModule();
        }

        return null;
    }

    public ISyntaxNode getSyntaxNode() {
        return syntaxNode;
    }

    public void setTopLevelSyntaxNode(ISyntaxNode topLevelSyntaxNode) {
        this.topLevelSyntaxNode = topLevelSyntaxNode;
    }

    public ISyntaxNode getTopLevelSyntaxNode() {
        return topLevelSyntaxNode;
    }

    public String getUri() {
        return SourceCodeURLTool.makeSourceLocationURL(getLocation(), getSourceModule(), "");
    }
}
