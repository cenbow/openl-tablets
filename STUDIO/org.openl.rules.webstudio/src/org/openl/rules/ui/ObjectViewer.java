/**
 * Created Jan 5, 2007
 */
package org.openl.rules.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openl.rules.calc.SpreadsheetResult;
import org.openl.rules.table.IGridTable;
import org.openl.rules.table.ILogicalTable;
import org.openl.rules.table.Point;
import org.openl.rules.table.ui.filters.ExpectedResultFilter;
import org.openl.rules.table.ui.filters.IGridFilter;
import org.openl.rules.tableeditor.model.ui.TableModel;
import org.openl.rules.tableeditor.renderkit.HTMLRenderer;
import org.openl.rules.testmethod.result.ComparedResult;

/**
 * @author snshor
 * 
 * @deprecated
 */
public final class ObjectViewer {

    private ObjectViewer() {
    }

    /** Display SpreadsheetResult with added filter for given fields as expected result and passed/failed icon**/
    public static String displaySpreadsheetResult(final SpreadsheetResult res, Map<Point, ComparedResult> spreadsheetCellsForTest) {
        return display(res, spreadsheetCellsForTest, true);
    }

    /** Display SpreadsheetResult with filter for links to explanation for values*/
    public static String displaySpreadsheetResult(final SpreadsheetResult res) {
        return display(res, null, true);
    }

    /** Display SpreadsheetResult without any filters in the table**/
    public static String displaySpreadsheetResultNoFilters(final SpreadsheetResult res) {
        return display(res, null, false);
    }

    private static String display(final SpreadsheetResult res, Map<Point, ComparedResult> spreadsheetCellsForTest, boolean filter) {
        List<IGridFilter> filters = new ArrayList<IGridFilter>();
        filters.add(new TableValueFilter(res));
        filters.add(CollectionCellFilter.INSTANCE);

        if (filter) {
            filters.add(LinkMaker.INSTANCE);

            // Check if the cells for test are initialized,
            // Means Spreadsheet should be displayed with expected values for tests
            //
            if (spreadsheetCellsForTest != null) {
                ExpectedResultFilter expResFilter = new ExpectedResultFilter(spreadsheetCellsForTest);
                filters.add(expResFilter);
            }
        }

        ILogicalTable table = res.getLogicalTable();
        IGridTable gridtable = table.getSource();
        TableModel tableModel = TableModel.initializeTableModel(gridtable, filters.toArray(new IGridFilter[filters.size()]));
        return new HTMLRenderer.TableRenderer(tableModel).render(false);
    }

}
