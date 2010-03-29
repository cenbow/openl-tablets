/*
 * Created on Oct 23, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.rules.data.impl;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

import org.openl.binding.IBindingContext;
import org.openl.rules.OpenlToolAdaptor;
import org.openl.rules.data.DuplicatedTableException;
import org.openl.rules.data.IDataBase;
import org.openl.rules.data.IDataTableModel;
import org.openl.rules.data.ITable;
import org.openl.rules.data.binding.DataNodeBinder;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.table.IGridTable;
import org.openl.rules.table.ILogicalTable;
import org.openl.rules.table.openl.GridCellSourceCodeModule;
import org.openl.syntax.exception.SyntaxNodeException;
import org.openl.syntax.exception.SyntaxNodeExceptionUtils;
import org.openl.types.IOpenClass;
import org.openl.util.BiMap;

/**
 * @author snshor
 *
 */
public class DataBase implements IDataBase {

    // boolean validationOccured = false;

    static class Table implements ITable {
        ILogicalTable data;
        IDataTableModel dataModel;

        String tableName;
        TableSyntaxNode tsn;

        Object ary;

        BiMap<Integer, Object> rowIndexMap = new BiMap<Integer, Object>();

        BiMap<Integer, String> primaryIndexMap = new BiMap<Integer, String>();

        /**
         *
         */
        public Table(IDataTableModel dataModel, ILogicalTable data) {
            this.dataModel = dataModel;
            this.data = data;
        }

        public Table(String tableName, TableSyntaxNode tsn) {
            this.tableName = tableName;
            this.tsn = tsn;
        }

        private void addToRowIndex(int rowIndex, Object target) {
            rowIndexMap.put(rowIndex, target);

        }

        /**
         * @throws BoundError
         *
         */

        public Object findObject(int columnIndex, String skey, IBindingContext cxt) throws SyntaxNodeException {

            Map<String, Integer> index = getUniqueIndex(columnIndex);

            Integer found = index.get(skey);

            if (found == null) {
                return null;
            }

            return Array.get(ary, found);

            // int len = getSize();
            // IColumnDescriptor descr = dataModel.getDescriptor()[columnIndex];
            // Object key = null;
            // try
            // {
            // if (descr.getConvertor() == null)
            // throw new Exception("Bad foreign key type");
            //
            // key = descr.getConvertor().parse(skey, null, cxt);
            // } catch (Throwable t)
            // {
            // // System.err.println("problem");
            // throw RuntimeExceptionWrapper.wrap(t);
            // }
            //
            // for (int i = 0; i < len; i++)
            // {
            // Object target = Array.get(ary, i);
            // // if (descr == null)
            // // return null;
            // Object test = descr.getColumnValue(target);
            // if (key.equals(test))
            // return target;
            // }
            //
            // return null;
        }

        public String getColumnDisplay(int n) {
            return dataModel.getDescriptor()[n].getDisplayName();
        }

        /**
         *
         */

        public int getColumnIndex(String columnName) {
            ColumnDescriptor[] dd = dataModel.getDescriptor();
            for (int i = 0; i < dd.length; i++) {
                if (dd[i] == null) {
                    continue;
                }
                if (dd[i].getName().equals(columnName)) {
                    return i;
                }
            }
            return -1;
        }

        /**
         *
         */

        public String getColumnName(int n) {
            return dataModel.getDescriptor()[n].getName();
        }

        public IOpenClass getColumnType(int n) {
            ColumnDescriptor colDescript = dataModel.getDescriptor()[n];
            if (!colDescript.isConstructor()) {
                return colDescript.getType();
            } else {
                return null;
            }
            
        }

        /**
         *
         */

        public Object getData(int row) {
            return Array.get(ary, row);
        }

        /**
         *
         */

        public Object getDataArray() {
            return ary;
        }

        /**
         *
         */

        public IDataTableModel getDataModel() {
            return dataModel;
        }

        public IGridTable getHeaderTable() {
            return data.getLogicalRow(0).getGridTable();
        }

        public String getName() {
            return null;
        }

        public int getNumberOfColumns() {
            return dataModel.getDescriptor().length;
        }

        public int getNumberOfRows() {
            return data.getLogicalHeight() - 1;
        }

        public String getPrimaryIndexKey(int row) {
            return primaryIndexMap.get(row);
        }

        public int getRowIndex(Object target) {
            return rowIndexMap.getKey(target);
        }

        public IGridTable getRowTable(int row) {
            return data.getLogicalRow(row + 1).getGridTable();
        }

        // public Object getFirst(Object primaryKey)
        // {
        // return null;
        // }
        /**
         *
         */

        public int getSize() {
            return Array.getLength(ary);
        }

        public TableSyntaxNode getTableSyntaxNode() {
            return tsn;
        }

        public Map<String, Integer> getUniqueIndex(int columnIndex) throws SyntaxNodeException {
            ColumnDescriptor cd = dataModel.getDescriptor()[columnIndex];

            return cd.getUniqueIndex(this, columnIndex);

        }

        public Object getValue(int col, int row) {
            Object rowObject = Array.get(getDataArray(), row);
            Object colObject = dataModel.getDescriptor()[col].getColumnValue(rowObject);
            return colObject;
        }

        public Map<String, Integer> makeUniqueIndex(int colIdx) throws SyntaxNodeException {
            int rows = data.getLogicalHeight();

            Map<String, Integer> index = new HashMap<String, Integer>();

            int startRow = 1;

            for (int i = startRow; i < rows; i++) {

                IGridTable gridTable = data.getLogicalRegion(colIdx, i, 1, 1).getGridTable();
                String key = gridTable.getCell(0, 0).getStringValue();

                if (key == null) {
                    throw SyntaxNodeExceptionUtils.createError("Empty key in an unique index", new GridCellSourceCodeModule(gridTable));
                }

                key = key.trim();

                if (index.containsKey(key)) {
                    throw SyntaxNodeExceptionUtils.createError("Duplicated key in an unique index: " + key, new GridCellSourceCodeModule(
                            gridTable));
                }

                index.put(key, i - 1);

            }

            return index;
        }

        public void populate(IDataBase db, IBindingContext cxt) throws Exception {
            int rows = data.getLogicalHeight();
            int columns = data.getLogicalWidth();

            int startRow = 1;

            for (int i = startRow; i < rows; i++) {
                Object target = Array.get(ary, i - startRow);

                for (int j = 0; j < columns; j++) {
                    ColumnDescriptor cd = dataModel.getDescriptor()[j];
                    if (cd != null && (cd instanceof ForeignKeyColumnDescriptor)) {
                        ForeignKeyColumnDescriptor colDescrFK = (ForeignKeyColumnDescriptor)cd;
                        if(colDescrFK.isReference()) {
                            if (cd.isConstructor()) {
                                target = colDescrFK.getLiteralByForeignKey(dataModel.getType(), data.getLogicalRegion(j, i, 1, 1), db, cxt);
                            } else {
                                colDescrFK.populateLiteralByForeignKey(target, data.getLogicalRegion(j, i, 1, 1), db, cxt);
                            }
                        }
                    }

                }
            }

        }

        public void preLoad(OpenlToolAdaptor ota) throws Exception {
            int rows = data.getLogicalHeight();
            int startRow = getStartRowForData();                        

            ary = Array.newInstance(dataModel.getInstanceClass(), rows - startRow);

            // if (dataModel.getInstanceClass().isPrimitive())
            // return;
            
            boolean constructor = isConstructor();

            for (int i = startRow; i < rows; i++) {
                Object literal = null;
                int rowIndex = i - startRow;
                if (!constructor) {
                    literal = dataModel.newInstance();
                    addToRowIndex(rowIndex, literal);
                }

                int columns = data.getLogicalWidth();

                for (int j = 0; j < columns; j++) {
                    ColumnDescriptor columnDescriptor = dataModel.getDescriptor()[j];
                    if (columnDescriptor != null && !columnDescriptor.isReference()) {
                        if (constructor) {
                            literal = columnDescriptor.getLiteral(dataModel.getType(), data.getLogicalRegion(j, i, 1, 1), ota);
                        } else {
                            columnDescriptor.populateLiteral(literal, data.getLogicalRegion(j, i, 1, 1), ota);
                        }
                    }
                }

                if (literal == null) {
                    literal = dataModel.getType().nullObject();
                }
                Array.set(ary, i - startRow, literal);
            }
        }
        
        /**         
         * @return Start row for data rows from Data_With_Titles rows. 
         * It depends on if table has or no column title row.
         * @see {@link DataNodeBinder#getDataWithTitleRows}  
         */
        private int getStartRowForData() {
            int startRow = 0;
            if (dataModel.hasColumnTytleRow()) {
                startRow = 1;
            }
            return startRow;
        }

        private boolean isConstructor() {
            boolean isConstructor = false;
            for (int i = 0; i < dataModel.getDescriptor().length; i++) {
                ColumnDescriptor columnDescriptor = dataModel.getDescriptor()[i];
                if (columnDescriptor != null && columnDescriptor.isConstructor()) {
                    isConstructor = true;
                    break;
                }
            }
            return isConstructor;
        }

        public void setData(ILogicalTable dataWithHeader) {
            data = dataWithHeader;

        }

        public void setModel(OpenlBasedDataTableModel dataModel) {
            this.dataModel = dataModel;
        }

        public synchronized void setPrimaryIndexKey(int row, String value) {
            Integer oldRow = primaryIndexMap.getKey(value);
            if (oldRow != null) {
                throw new RuntimeException("Duplicated key: " + value + "in rows " + oldRow + "," + row);
            }
            primaryIndexMap.put(row, value);
        }
    }

    Map<String, ITable> tables = new HashMap<String, ITable>();

    public DataBase() {
    }

    /**
     *
     */

    public ITable addNewTable(String tableName, TableSyntaxNode tsn) {
        ITable t = tables.get(tableName);
        if (t != null) {
            throw new DuplicatedTableException(tableName, t.getTableSyntaxNode(), tsn);
        }
        t = new Table(tableName, tsn);
        tables.put(tableName, t);
        return t;
    }

    // public synchronized void validate() throws Exception
    // {
    // if (validationOccured)
    // return;
    //
    // validateInternal();
    // }
    // protected void validateInternal() throws Exception
    // {
    // Vector errors = new Vector();
    //
    // for (Iterator iter = tables.values().iterator(); iter.hasNext();)
    // {
    // Table t = (Table) iter.next();
    //
    // try
    // {
    // t.populate(this);
    // }
    // catch (BoundError e)
    // {
    // errors.add(e);
    // }
    // }
    //
    // validationOccured = true;
    // if (errors.size() > 0)
    // {
    // BoundError[] ee = (BoundError[])errors.toArray(new BoundError[0]);
    // throw new SyntaxErrorException(null, ee);
    // }
    //
    // }
    /**
     *
     */

    public synchronized ITable getTable(String name) {
        // validate();
        Table t = (Table) tables.get(name);
        // if (t == null)
        // throw new Exception("Data table " + name + " is not found");
        return t;
    }

    public void preLoadTable(ITable t, OpenlBasedDataTableModel dataModel, ILogicalTable dataWithTitles,
            OpenlToolAdaptor ota) throws Exception {
        t.setModel(dataModel);
        t.setData(dataWithTitles);
        t.preLoad(ota);
    }

}
