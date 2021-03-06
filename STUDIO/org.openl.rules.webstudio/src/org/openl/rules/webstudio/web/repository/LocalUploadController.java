package org.openl.rules.webstudio.web.repository;

import org.openl.commons.web.jsf.FacesUtils;
import org.openl.rules.common.ProjectException;
import org.openl.rules.ui.WebStudio;
import org.openl.rules.webstudio.util.NameChecker;
import org.openl.rules.webstudio.web.servlet.RulesUserSession;
import org.openl.rules.webstudio.web.util.WebStudioUtils;
import org.openl.rules.workspace.WorkspaceException;
import org.openl.rules.workspace.dtr.DesignTimeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

@ManagedBean(name = "localUpload")
@RequestScoped
public class LocalUploadController {
    private boolean selectAll = false;

    public static class UploadBean {
        private String projectName;

        private boolean selected;

        public UploadBean() {
        }

        public UploadBean(String projectName) {
            this.projectName = projectName;
        }

        public String getProjectName() {
            return projectName;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setProjectName(String projectName) {
            this.projectName = projectName;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }
    }

    private final Logger log = LoggerFactory.getLogger(LocalUploadController.class);

    private List<UploadBean> uploadBeans;

    private void createProject(File baseFolder, RulesUserSession rulesUserSession) throws ProjectException,
            WorkspaceException, FileNotFoundException {
        if (!baseFolder.isDirectory()) {
            throw new FileNotFoundException(baseFolder.getName());
        }

        rulesUserSession.getUserWorkspace().uploadLocalProject(baseFolder.getName());
    }

    public List<UploadBean> getProjects4Upload() {
        if (uploadBeans == null) {
            uploadBeans = new ArrayList<UploadBean>();
            RulesUserSession userRules = getRules();
            WebStudio webStudio = WebStudioUtils.getWebStudio();
            if (webStudio != null && userRules != null) {
                DesignTimeRepository dtr;
                try {
                    dtr = userRules.getUserWorkspace().getDesignTimeRepository();
                } catch (Exception e) {
                    log.error("Cannot get DTR!", e);
                    return null;
                }

                List<File> projects = webStudio.getProjectResolver().listOpenLFolders();
                for (File f : projects) {
                    try {
                        if (!dtr.hasProject(f.getName())) {
                            uploadBeans.add(new UploadBean(f.getName()));
                        }
                    } catch (Exception e) {
                        log.error("Failed to list projects for upload!", e);
                        FacesUtils.addErrorMessage(e.getMessage());
                    }
                }
            }
        }
        return uploadBeans;
    }

    private RulesUserSession getRules() {
        HttpSession session = FacesUtils.getSession();
        return WebStudioUtils.getRulesUserSession(session);
    }

    public String upload() {
        String workspacePath = WebStudioUtils.getWebStudio().getWorkspacePath();
        RulesUserSession rulesUserSession = getRules();

        List<UploadBean> beans = uploadBeans;
        uploadBeans = null; // force re-read.

        if (beans != null) {
            for (UploadBean bean : beans) {
                if (bean.isSelected()) {
                    try {
                        createProject(new File(workspacePath, bean.getProjectName()), rulesUserSession);
                        FacesUtils.addInfoMessage("Project " + bean.getProjectName()
                                + " was created successfully");
                    } catch (Exception e) {
                        if (!NameChecker.checkName(bean.getProjectName())) {
                            String msg = "Failed to create the project '" + bean.getProjectName() + "'! " + NameChecker.BAD_PROJECT_NAME_MSG;
                            FacesUtils.addErrorMessage(msg);
                        } else {
                            String msg = "Failed to create the project '" + bean.getProjectName() + "'!";
                            log.error(msg, e);
                            FacesUtils.addErrorMessage(msg, e.getMessage());
                        }

                    }
                }
            }
        }

        return null;
    }

    public boolean isSelectAll() {
        return false;
    }

    public void setSelectAll(boolean selectAll) {
        this.selectAll = selectAll;
    }
}
