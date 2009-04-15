package org.openl.rules.ui.tablewizard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openl.rules.lang.xls.XlsSheetSourceCodeModule;
import org.openl.rules.lang.xls.XlsWorkbookSourceCodeModule;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import static org.openl.rules.ui.tablewizard.WizardUtils.getMetaInfo;
import org.openl.rules.ui.tablewizard.jsf.BaseWizardBean;

/**
 * @author Aliaksandr Antonik.
 */
public abstract class WizardBase extends BaseWizardBean {
    private static final String SHEET_EXSISTING = "existing";
    private static final String SHEET_NEW = "new";
    private String workbook;
    private Integer worksheetIndex;
    private Map<String, XlsWorkbookSourceCodeModule> workbooks;
    private boolean newWorksheet;
    private String newWorksheetName;

    protected void reset() {
        worksheetIndex = 0;
        workbooks = null;
        newWorksheet = false;
        newWorksheetName = StringUtils.EMPTY;
    }

    protected void initWorkbooks() {
        workbooks = new HashMap<String, XlsWorkbookSourceCodeModule>();

        TableSyntaxNode[] syntaxNodes = getMetaInfo().getXlsModuleNode().getXlsTableSyntaxNodes();
        for (TableSyntaxNode node : syntaxNodes) {
            XlsWorkbookSourceCodeModule module = ((XlsSheetSourceCodeModule) node.getModule()).getWorkbookSource();
            workbooks.put(module.getUri(), module);
        }

        if (workbooks.size() > 0) {
            workbook = workbooks.keySet().iterator().next();
        }
    }

    public String getWorkbook() {
        return workbook;
    }

    public void setWorkbook(String workbook) {
        this.workbook = workbook;
    }

    public Integer getWorksheetIndex() {
        return worksheetIndex;
    }

    public void setWorksheetIndex(Integer worksheetIndex) {
        this.worksheetIndex = worksheetIndex;
    }

    public List<SelectItem> getWorkbooks() {
        List<SelectItem> items = new ArrayList<SelectItem>(workbooks.size());
        for (String wbURI : workbooks.keySet()) {
            String[] parts = wbURI.split("/");
            items.add(new SelectItem(wbURI, parts[parts.length - 1]));
        }

        return items;
    }

    public List<SelectItem> getWorksheets() {
        if (workbook == null || workbooks == null) {
            return Collections.EMPTY_LIST;
        }

        XlsWorkbookSourceCodeModule currentSheet = workbooks.get(workbook);
        if (currentSheet == null) {
            return Collections.EMPTY_LIST;
        }

        Workbook hssfWorkbook = currentSheet.getWorkbook();
        List<SelectItem> items = new ArrayList<SelectItem>(hssfWorkbook.getNumberOfSheets());
        for (int i = 0; i < hssfWorkbook.getNumberOfSheets(); ++i) {
            items.add(new SelectItem(i, hssfWorkbook.getSheetName(i)));
        }
        return items;
    }

    public String getNewWorksheet() {
        return newWorksheet ? SHEET_NEW : SHEET_EXSISTING;
    }

    public void setNewWorksheet(String value) {
        newWorksheet = SHEET_NEW.equals(value);
    }

    public String getNewWorksheetName() {
        return newWorksheetName;
    }

    public void setNewWorksheetName(String newWorksheetName) {
        this.newWorksheetName = newWorksheetName;
    }

    protected XlsSheetSourceCodeModule getDestinationSheet() {
        XlsSheetSourceCodeModule sourceCodeModule;
        XlsWorkbookSourceCodeModule module = workbooks.get(workbook);
        if (newWorksheet) {
            Sheet sheet = module.getWorkbook().createSheet(getNewWorksheetName());
            sourceCodeModule = new XlsSheetSourceCodeModule(sheet, getNewWorksheetName(), module);
        } else {
            Sheet sheet = module.getWorkbook().getSheetAt(getWorksheetIndex());
            sourceCodeModule = new XlsSheetSourceCodeModule(sheet, module.getWorkbook().getSheetName(getWorksheetIndex()), module);
        }
        return sourceCodeModule;
    }
}
