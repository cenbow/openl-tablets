package org.openl.rules.webstudio.web;

import static org.openl.rules.security.AccessManager.isGranted;
import static org.openl.rules.security.DefaultPrivileges.PRIVILEGE_RUN;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.openl.classloader.ClassLoaderCloserFactory;
import org.openl.classloader.SimpleBundleClassLoader;
import org.openl.rules.extension.instantiation.ExtensionDescriptorFactory;
import org.openl.rules.lang.xls.XlsNodeTypes;
import org.openl.rules.project.model.Module;
import org.openl.rules.testmethod.TestSuiteMethod;
import org.openl.rules.ui.WebStudio;
import org.openl.rules.ui.tree.richfaces.ProjectTreeBuilder;
import org.openl.rules.webstudio.web.util.WebStudioUtils;
import org.openl.syntax.ISyntaxNode;
import org.openl.util.CollectionUtils;
import org.openl.util.tree.ITreeElement;
import org.richfaces.model.TreeNode;
import org.richfaces.model.TreeNodeImpl;

/**
 * Request scope managed bean providing logic for tree page of OpenL Studio.
 */
@ManagedBean
@SessionScoped
public class TreeBean {

    private boolean hideUtilityTables = true;

    public void setHideUtilityTables(boolean hideUtilityTables) {
        this.hideUtilityTables = hideUtilityTables;
    }

    public boolean isHideUtilityTables() {
        return hideUtilityTables;
    }

    public void setCurrentView(String currentView) throws Exception {
        WebStudio studio = WebStudioUtils.getWebStudio();
        studio.setTreeView(currentView);
    }

    public boolean getCanRun() {
        return isGranted(PRIVILEGE_RUN);
    }

    public int getProjectTestsCount() {
        WebStudio studio = WebStudioUtils.getWebStudio();
        TestSuiteMethod[] allTestMethods = studio.getModel().getAllTestMethods();
        return CollectionUtils.isNotEmpty(allTestMethods) ? allTestMethods.length : 0;
    }

    public TreeNode getTree() {
        WebStudio studio = WebStudioUtils.getWebStudio();
        ITreeElement<?> tree = studio.getModel().getProjectTree();
        if (tree != null) {
            Module module = studio.getCurrentModule();
            CollectionUtils.Predicate<ITreeElement> utilityTablePredicate = null;
            if (hideUtilityTables) {
                utilityTablePredicate = getUtilityTablePredicate(studio, module);
            }

            return new ProjectTreeBuilder(utilityTablePredicate).build(tree);
        }
        // Empty tree
        return new TreeNodeImpl();
    }

    private CollectionUtils.Predicate<ITreeElement> getUtilityTablePredicate(WebStudio studio, Module module) {
        CollectionUtils.Predicate<ITreeElement> utilityTablePredicate;
        if (module.getExtension() == null) {
            utilityTablePredicate = new OtherTablePredicate();
        } else {
            ClassLoader classLoader = null;
            try {
                classLoader = new SimpleBundleClassLoader(Thread.currentThread().getContextClassLoader());
                utilityTablePredicate = ExtensionDescriptorFactory.getExtensionDescriptor(
                        module.getExtension(), classLoader
                ).getUtilityTablePredicate(studio.getModel().getXlsModuleNode());
            } finally {
                ClassLoaderCloserFactory.getClassLoaderCloser().close(classLoader);
            }
        }
        return utilityTablePredicate;
    }

    private static class OtherTablePredicate implements CollectionUtils.Predicate<ITreeElement> {
        @Override
        public boolean evaluate(ITreeElement tableNode) {
            if (tableNode.isLeaf() && tableNode.getObject() instanceof ISyntaxNode) {
                String tableType = ((ISyntaxNode) tableNode.getObject()).getType();
                return XlsNodeTypes.XLS_OTHER.toString().equals(tableType);
            }

            return false;
        }
    }
}
