/*
 * Created on Sep 23, 2003 Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.rules.lang.xls;

import org.openl.conf.IConfigurableResourceContext;
import org.openl.conf.IUserContext;
import org.openl.exception.OpenLCompilationException;
import org.openl.message.OpenLMessagesUtils;
import org.openl.rules.lang.xls.syntax.*;
import org.openl.rules.table.IGridTable;
import org.openl.rules.table.ILogicalTable;
import org.openl.rules.table.openl.GridCellSourceCodeModule;
import org.openl.rules.table.syntax.GridLocation;
import org.openl.rules.table.xls.XlsSheetGridModel;
import org.openl.rules.utils.ParserUtils;
import org.openl.source.IOpenSourceCodeModule;
import org.openl.source.impl.URLSourceCodeModule;
import org.openl.syntax.code.Dependency;
import org.openl.syntax.code.DependencyType;
import org.openl.syntax.code.IDependency;
import org.openl.syntax.code.IParsedCode;
import org.openl.syntax.code.impl.ParsedCode;
import org.openl.syntax.exception.SyntaxNodeException;
import org.openl.syntax.exception.SyntaxNodeExceptionUtils;
import org.openl.syntax.impl.IdentifierNode;
import org.openl.util.PathTool;
import org.openl.util.StringTool;
import org.openl.util.StringUtils;
import org.openl.util.text.LocationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;

/**
 * @author snshor
 */
public class XlsLoader {

    private final Logger log = LoggerFactory.getLogger(XlsLoader.class);

    private Collection<String> imports = new HashSet<String>();

    private IncludeSearcher includeSeeker;

    // private IUserContext userContext;

    private OpenlSyntaxNode openl;

    private IdentifierNode vocabulary;

    private List<SyntaxNodeException> errors = new ArrayList<SyntaxNodeException>();

    private HashSet<String> preprocessedWorkBooks = new HashSet<String>();

    private List<WorkbookSyntaxNode> workbookNodes = new ArrayList<WorkbookSyntaxNode>();

    private List<IDependency> dependencies = new ArrayList<IDependency>();

    /**
     * @deprecated use {@link #XlsLoader(IncludeSearcher, IUserContext)} {}
     */
    @Deprecated
    public XlsLoader(IConfigurableResourceContext ucxt, String searchPath) {
        this.includeSeeker = new IncludeSearcher(ucxt, searchPath);
    }

    public XlsLoader(IncludeSearcher includeSeeker, IUserContext userContext) {
        this.includeSeeker = includeSeeker;
        // this.userContext = userContext;
    }

    public void addError(SyntaxNodeException error) {
        errors.add(error);
    }

    public Set<String> getPreprocessedWorkBooks() {
        return preprocessedWorkBooks;
    }

    public IParsedCode parse(IOpenSourceCodeModule source) {

        preprocessWorkbook(source);

        addInnerImports();

        WorkbookSyntaxNode[] workbooksArray = workbookNodes.toArray(new WorkbookSyntaxNode[workbookNodes.size()]);
        XlsModuleSyntaxNode syntaxNode = new XlsModuleSyntaxNode(workbooksArray,
                source,
                openl,
                vocabulary,
                Collections.unmodifiableCollection(imports)
        );

        SyntaxNodeException[] parsingErrors = errors.toArray(new SyntaxNodeException[errors.size()]);

        return new ParsedCode(syntaxNode,
                source,
                parsingErrors,
                dependencies.toArray(new IDependency[dependencies.size()]));
    }

    private void preprocessEnvironmentTable(TableSyntaxNode tableSyntaxNode, XlsSheetSourceCodeModule source) {

        ILogicalTable logicalTable = tableSyntaxNode.getTable();

        int height = logicalTable.getHeight();

        for (int i = 1; i < height; i++) {
            ILogicalTable row = logicalTable.getRow(i);

            String name = row.getColumn(0).getSource().getCell(0, 0).getStringValue();

            if (IXlsTableNames.LANG_PROPERTY.equals(name)) {
                preprocessOpenlTable(row.getSource(), source);
            } else if (IXlsTableNames.DEPENDENCY.equals(name)) {
                // process module dependency
                //
                preprocessDependency(tableSyntaxNode, row.getSource());
            } else if (IXlsTableNames.INCLUDE_TABLE.equals(name)) {
                preprocessIncludeTable(tableSyntaxNode, row.getSource(), source);
            } else if (IXlsTableNames.IMPORT_PROPERTY.equals(name)) {
                preprocessImportTable(row.getSource());

                // NOTE: A temporary implementation of multi-module feature.
                // } else if (IXlsTableNames.IMPORT_MODULE.equals(name)) {
                // preprocessModuleImportTable(row.getGridTable(), source);
            } else if (IXlsTableNames.VOCABULARY_PROPERTY.equals(name)) {
                preprocessVocabularyTable(row.getSource(), source);
            } else if (ParserUtils.isBlankOrCommented(name)) {
                // ignore comment
            } else {
                String message = String.format("Error in Environment table: unrecognized keyword '%s'", name);
                log.warn(message);
                OpenLMessagesUtils.addWarn(message, tableSyntaxNode);
            }
        }
    }

    private void preprocessDependency(TableSyntaxNode tableSyntaxNode, IGridTable gridTable) {

        int height = gridTable.getHeight();

        for (int i = 0; i < height; i++) {

            String dependency = gridTable.getCell(1, i).getStringValue();
            if (StringUtils.isNotBlank(dependency)) {
                dependency = dependency.trim();

                IdentifierNode node = new IdentifierNode(IXlsTableNames.DEPENDENCY,
                        LocationUtils.createTextInterval(dependency),
                        dependency,
                        new GridCellSourceCodeModule(gridTable, 1, i, null));
                node.setParent(tableSyntaxNode);
                Dependency moduleDependency = new Dependency(DependencyType.MODULE, node);
                dependencies.add(moduleDependency);
            }
        }
    }

    private void preprocessImportTable(IGridTable table) {
        int height = table.getHeight();

        for (int i = 0; i < height; i++) {
            String singleImport = table.getCell(1, i).getStringValue();
            if (StringUtils.isNotBlank(singleImport)) {
                singleImport = singleImport.trim();
            }
            if (StringUtils.isNotEmpty(singleImport)) {
                addImport(singleImport);
            }
        }
    }

    private void addImport(String singleImport) {
        imports.add(singleImport);
    }

    private void addInnerImports() {
        addImport("org.openl.rules.enumeration");
    }

    private void preprocessIncludeTable(TableSyntaxNode tableSyntaxNode,
                                        IGridTable table,
                                        XlsSheetSourceCodeModule sheetSource) {

        int height = table.getHeight();

        for (int i = 0; i < height; i++) {

            String include = table.getCell(1, i).getStringValue();

            if (StringUtils.isNotBlank(include)) {
                include = include.trim();
                IOpenSourceCodeModule src;

                if (include.startsWith("<")) {
                    src = includeSeeker.findInclude(StringTool.openBrackets(include, '<', '>', "")[0]);

                    if (src == null) {
                        registerIncludeError(tableSyntaxNode, table, i, include, null);
                        continue;
                    }
                } else {
                    try {
                        String newURL = PathTool.mergePath(sheetSource.getWorkbookSource().getUri(0), include);
                        src = new URLSourceCodeModule(new URL(newURL));
                    } catch (Throwable t) {
                        registerIncludeError(tableSyntaxNode, table, i, include, t);
                        continue;
                    }
                }

                try {
                    preprocessWorkbook(src);
                } catch (Throwable t) {
                    registerIncludeError(tableSyntaxNode, table, i, include, t);
                    continue;
                }
            }
        }
    }

    private void registerIncludeError(TableSyntaxNode tableSyntaxNode,
                                      IGridTable table,
                                      int i,
                                      String include,
                                      Throwable t) {
        SyntaxNodeException se = SyntaxNodeExceptionUtils.createError("Include " + include + " not found",
                t,
                LocationUtils.createTextInterval(include),
                new GridCellSourceCodeModule(table, 1, i, null));
        addError(se);
        tableSyntaxNode.addError(se);
        OpenLMessagesUtils.addError(se);
    }

    private void preprocessOpenlTable(IGridTable table, XlsSheetSourceCodeModule source) {

        String openlName = table.getCell(1, 0).getStringValue();

        setOpenl(new OpenlSyntaxNode(openlName, new GridLocation(table), source));
    }

    private TableSyntaxNode preprocessTable(IGridTable table,
                                            XlsSheetSourceCodeModule source,
                                            TablePartProcessor tablePartProcessor) throws OpenLCompilationException {

        TableSyntaxNode tsn = XlsHelper.createTableSyntaxNode(table, source);

        String type = tsn.getType();
        if (type.equals(XlsNodeTypes.XLS_ENVIRONMENT.toString())) {
            preprocessEnvironmentTable(tsn, source);
        } else if (type.equals(XlsNodeTypes.XLS_TABLEPART.toString())) {
            try {
                tablePartProcessor.register(table, source);
            } catch (Throwable t) {
                tsn = new TableSyntaxNode(XlsNodeTypes.XLS_OTHER.toString(),
                        tsn.getGridLocation(),
                        source,
                        table,
                        tsn.getHeader());
                SyntaxNodeException sne = SyntaxNodeExceptionUtils.createError(t, tsn);
                addError(sne);
                tsn.addError(sne);
                OpenLMessagesUtils.addError(sne);
            }
        }

        return tsn;
    }

    private void preprocessVocabularyTable(IGridTable table, XlsSheetSourceCodeModule source) {

        String vocabularyStr = table.getCell(1, 0).getStringValue();

        setVocabulary(new IdentifierNode(IXlsTableNames.VOCABULARY_PROPERTY,
                new GridLocation(table),
                vocabularyStr,
                source));
    }

    private WorkbookSyntaxNode preprocessWorkbook(IOpenSourceCodeModule source) {

        String uri = source.getUri(0);

        if (preprocessedWorkBooks.contains(uri)) {
            return null;
        }

        preprocessedWorkBooks.add(uri);

        XlsWorkbookSourceCodeModule workbookSourceModule = new XlsWorkbookSourceCodeModule(source);
        int nsheets = workbookSourceModule.getWorkbookLoader().getNumberOfSheets();
        WorksheetSyntaxNode[] sheetNodes = new WorksheetSyntaxNode[nsheets];
        TablePartProcessor tablePartProcessor = new TablePartProcessor();

        for (int i = 0; i < nsheets; i++) {
            XlsSheetSourceCodeModule sheetSource = new XlsSheetSourceCodeModule(i, workbookSourceModule);
            sheetNodes[i] = createWorksheetSyntaxNode(sheetSource, tablePartProcessor);
        }

        TableSyntaxNode[] mergedNodes = {};
        try {
            List<TablePart> tableParts = tablePartProcessor.mergeAllNodes();
            int n = tableParts.size();
            mergedNodes = new TableSyntaxNode[n];
            for (int i = 0; i < n; i++) {
                mergedNodes[i] = preprocessTable(tableParts.get(i).getTable(),
                        tableParts.get(i).getSource(),
                        tablePartProcessor);
            }
        } catch (OpenLCompilationException e) {
            OpenLMessagesUtils.addError(e);
        }

        WorkbookSyntaxNode workbookNode = new WorkbookSyntaxNode(sheetNodes, mergedNodes, workbookSourceModule);
        workbookNodes.add(workbookNode);

        return workbookNode;
    }

    private WorksheetSyntaxNode createWorksheetSyntaxNode(XlsSheetSourceCodeModule sheetSource,
            TablePartProcessor tablePartProcessor) {
        IGridTable[] tables = getAllGridTables(sheetSource);
        List<TableSyntaxNode> tableNodes = new ArrayList<TableSyntaxNode>();

        for (IGridTable table : tables) {

            TableSyntaxNode tsn;

            try {
                tsn = preprocessTable(table, sheetSource, tablePartProcessor);
                tableNodes.add(tsn);
            } catch (OpenLCompilationException e) {
                OpenLMessagesUtils.addError(e);
            }
        }

        return new WorksheetSyntaxNode(tableNodes.toArray(new TableSyntaxNode[tableNodes
                .size()]), sheetSource);
    }

    /**
     * Gets all grid tables from the sheet.
     */
    private IGridTable[] getAllGridTables(XlsSheetSourceCodeModule sheetSource) {

        XlsSheetGridModel xlsGrid = new XlsSheetGridModel(sheetSource);

        return xlsGrid.getTables();
    }

    private void setOpenl(OpenlSyntaxNode openl) {

        if (this.openl == null) {
            this.openl = openl;
        } else {
            if (!this.openl.getOpenlName().equals(openl.getOpenlName())) {
                SyntaxNodeException error = SyntaxNodeExceptionUtils.createError("Only one openl statement is allowed",
                        null,
                        openl);
                OpenLMessagesUtils.addError(error);
                addError(error);
            }
        }
    }

    private void setVocabulary(IdentifierNode vocabulary) {

        if (this.vocabulary == null) {
            this.vocabulary = vocabulary;
        } else {
            SyntaxNodeException error = SyntaxNodeExceptionUtils.createError("Only one vocabulary is allowed",
                    null,
                    vocabulary);
            OpenLMessagesUtils.addError(error);
            addError(error);
        }
    }

}