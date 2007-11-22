package org.openl.rules.repository.jcr;

import java.util.LinkedList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

import org.openl.rules.repository.RVersion;
import org.openl.rules.repository.exceptions.RRepositoryException;

public class JcrCommonArtefact {
    private String name;
    private Node node;
    
    protected JcrCommonArtefact(Node node) throws RepositoryException {
        this.node = node;
        
        name = node.getName();
    }
    
    public String getName() {
        return name;
    }
    
    public void delete() throws RRepositoryException {
        try {
            Node n = node();

            NodeUtil.smartCheckout(n, true);

            n.remove();
        } catch (RepositoryException e) {
            throw new RRepositoryException("Failed to Delete", e);
        }
    }

    public RVersion getBaseVersion() {
        try {
            Version v = node().getBaseVersion();
            RVersion result = new JcrVersion(v);
            return result;
        } catch (RepositoryException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<RVersion> getVersionHistory() throws RRepositoryException {
        try {
            VersionHistory vh = node().getVersionHistory();
            VersionIterator vi = vh.getAllVersions();
            LinkedList<RVersion> result = new LinkedList<RVersion>();
            while (vi.hasNext()) {
                Version v = vi.nextVersion();

                if (NodeUtil.isRootVersion(v)) {
                    //TODO Shall we add first (0) version? (It is marker like, no real values)
                } else {
                    JcrVersion jvi = new JcrVersion(v);
                    result.add(jvi);
                }
            }
            return result;
        } catch (RepositoryException e) {
            throw new RRepositoryException("Failed to get Version History", e);
        }
    }

    // --- protected 
    
    protected Node node() {
        return node;
    }

    /**
     * Checks whether type of the JCR node is correct.
     *
     * @param nodeType expected node type
     * @throws RepositoryException if failed
     */
    protected void checkNodeType(String nodeType) throws RepositoryException {
        if (!node.isNodeType(nodeType)) {
            throw new RepositoryException("Invalid NodeType. Expects " + nodeType);
        }
    }
}
