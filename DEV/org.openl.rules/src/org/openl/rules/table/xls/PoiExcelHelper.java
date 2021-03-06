package org.openl.rules.table.xls;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFOptimiser;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Color;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;

public class PoiExcelHelper {

    /** For more information, see {@link HSSFWorkbook#MAX_STYLES} */
    private static final short MAX_STYLES = 4030;

    public static void copyCellValue(Cell cellFrom, Cell cellTo) {
        cellTo.setCellType(Cell.CELL_TYPE_BLANK);
        switch (cellFrom.getCellType()) {
            case Cell.CELL_TYPE_BLANK:
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                cellTo.setCellValue(cellFrom.getBooleanCellValue());
                break;
            case Cell.CELL_TYPE_FORMULA:
                cellTo.setCellFormula(cellFrom.getCellFormula());
                try {
                    evaluateFormula(cellTo);
                } catch (Exception ignored) {
                }
                break;
            case Cell.CELL_TYPE_NUMERIC:
                cellTo.setCellValue(cellFrom.getNumericCellValue());
                break;
            case Cell.CELL_TYPE_STRING:
                cellTo.setCellValue(cellFrom.getStringCellValue());
                break;
            default:
                throw new RuntimeException("Unknown cell type: " + cellFrom.getCellType());
        }
    }

    public static void copyCellStyle(Cell cellFrom, Cell cellTo, Sheet sheetTo) {
        CellStyle styleFrom = cellFrom.getCellStyle();
        try {
            cellTo.setCellStyle(styleFrom);
        } catch (IllegalArgumentException e) { // copy cell style to cell of
                                               // another workbook
            CellStyle styleTo = createCellStyle(sheetTo.getWorkbook());
            styleTo.cloneStyleFrom(styleFrom);
            cellTo.setCellStyle(styleTo);
        }
    }

    /*public static void copyCellComment(Cell cellFrom, Cell cellTo) {
        Comment from = cellFrom.getCellComment();
        Comment to = null;

        if (from != null) {
            Drawing drawing = cellTo.getSheet().createDrawingPatriarch();
            to = drawing.createCellComment(new HSSFClientAnchor(0, 0, 0, 0, (short) 4, 2, (short) 6, 5));
            CreationHelper creationHelper = cellTo.getSheet().getWorkbook().getCreationHelper();
            RichTextString commentStr = creationHelper.createRichTextString(from.getString().getString());
            to.setString(commentStr);
            to.setRow(cellTo.getRowIndex());
            to.setColumn(cellTo.getColumnIndex());
            cellTo.setCellComment(to);
        }
    }*/

    public static Cell getCell(int colIndex, int rowIndex, Sheet sheet) {
        Row row = sheet.getRow(rowIndex);
        if (row != null) {
            return row.getCell(colIndex, Row.RETURN_NULL_AND_BLANK);
        }
        return null;
    }
        
    public static Cell getOrCreateCell(int colIndex, int rowIndex, Sheet sheet) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }
        return row.getCell(colIndex, Row.CREATE_NULL_AS_BLANK);
    }
    
    /**
     * Some magic numbers here What is column width???
     */
    public static int getColumnWidth(int col, Sheet sheet) {
        int w = sheet.getColumnWidth((short) col);
        if (w == sheet.getDefaultColumnWidth()) {
            return 79;
        }
        return w / 40;
    }
    
    /**
     * Returns the index of the column. After that column there is no more
     * filled cells on the sheet in given row.
     * 
     * @param rownum index of the row on the sheet
     */
    public static int getMaxColumnIndex(int rownum, Sheet sheet) {
        Row row = sheet.getRow(rownum);
        return row == null ? 0 : row.getLastCellNum();
    }
    
    public static int getMaxRowIndex(Sheet sheet) {
        return sheet.getLastRowNum();
    }
    
    /**
     * Returns the index of the column, the next column will be the first cell
     * with data in given row.
     * 
     */
    public static int getMinColumnIndex(int rownum, Sheet sheet) {
        Row row = sheet.getRow(rownum);
        return row == null ? 0 : row.getFirstCellNum();
    }
    
    public static int getNumberOfMergedRegions(Sheet sheet) {
        try {
            return sheet.getNumMergedRegions();
        } catch (NullPointerException e) {
            return 0;
        }
    }
    
    public static int getMinRowIndex(Sheet sheet) {
        return sheet.getFirstRowNum();
    }

    public static int getLastRowNum(Sheet sheet) {
        return sheet.getLastRowNum();
    }

    public static void setCellStringValue(int col, int row, String value, Sheet sheet) {
        Cell cell = getOrCreateCell(col, row, sheet);
        cell.setCellType(Cell.CELL_TYPE_STRING);
        cell.setCellValue(value);
    }
 
    public static CellRangeAddress getMergedRegionAt(int index, Sheet sheet) {
        return sheet.getMergedRegion(index);
    }

    /**
     * Evaluates formula in the cell to get new cell value.
     */
    public static void evaluateFormula(Cell cell) throws Exception {
        FormulaEvaluator formulaEvaluator = cell.getSheet().getWorkbook()
            .getCreationHelper().createFormulaEvaluator();
        formulaEvaluator.evaluateFormulaCell(cell);
    }

    public static <T extends CellStyle> T createCellStyle(Workbook workbook) {
        if (workbook instanceof HSSFWorkbook) {
            if (workbook.getNumCellStyles() == MAX_STYLES) {
                HSSFOptimiser.optimiseCellStyles((HSSFWorkbook) workbook);
            }
            @SuppressWarnings("unchecked")
            T style = (T) workbook.createCellStyle();
            return style;
        } else {
            @SuppressWarnings("unchecked")
            T style = (T) workbook.createCellStyle();
            return style;
        }
    }

    public static CellStyle cloneStyleFrom(Cell cell) {
        CellStyle newStyle = null;
        if (cell != null) {
            Sheet sheet = cell.getSheet();
            newStyle = createCellStyle(sheet.getWorkbook());
            CellStyle fromStyle = cell.getCellStyle();
            newStyle.cloneStyleFrom(fromStyle);
        }
        return newStyle;
    }

    public static Font cloneFontFrom(Cell cell) {
        Font newFont = null;
        if (cell != null) {
            Workbook workbook = cell.getSheet().getWorkbook();
            newFont = workbook.createFont();
            short fontIndex = cell.getCellStyle().getFontIndex();
            Font fromFont = workbook.getFontAt(fontIndex);

            newFont.setBoldweight(fromFont.getBoldweight());
            newFont.setColor(fromFont.getColor());
            newFont.setFontHeight(fromFont.getFontHeight());
            newFont.setFontName(fromFont.getFontName());
            newFont.setItalic(fromFont.getItalic());
            newFont.setStrikeout(fromFont.getStrikeout());
            newFont.setTypeOffset(fromFont.getTypeOffset());
            newFont.setUnderline(fromFont.getUnderline());
            newFont.setCharSet(fromFont.getCharSet());
        }
        return newFont;
    }

    public static Font getCellFont(Cell cell) {
        Font font = null;
        if (cell != null) {
            CellStyle style = cell.getCellStyle();
            int fontIndex = style.getFontIndex();
            font = cell.getSheet().getWorkbook().getFontAt((short) fontIndex);
        }
        return font;
    }

    public static void setCellFont(Cell cell,
            short boldWeight, short color, short fontHeight, String name, boolean italic,
            boolean strikeout, short typeOffset, byte underline) {
        if (cell != null) {
            Workbook workbook = cell.getSheet().getWorkbook();
            Font font = workbook.findFont(
                    boldWeight, color, fontHeight, name, italic, strikeout, typeOffset, underline);
            if (font == null) { // Create new font
                font = cell.getSheet().getWorkbook().createFont();
                font.setBoldweight(boldWeight);
                font.setColor(color);
                font.setFontHeight(fontHeight);
                font.setFontName(name);
                font.setItalic(italic);
                font.setStrikeout(strikeout);
                font.setTypeOffset(typeOffset);
                font.setUnderline(underline);
            }
            CellUtil.setFont(cell, workbook, font);
        }
    }

    public static void setCellFontBold(Cell cell, short boldweight) {
        Font font = getCellFont(cell);
        setCellFont(cell,
                boldweight, font.getColor(), font.getFontHeight(), font.getFontName(), font.getItalic(),
                font.getStrikeout(), font.getTypeOffset(), font.getUnderline());
    }

    public static void setCellFontItalic(Cell cell, boolean italic) {
        Font font = getCellFont(cell);
        setCellFont(cell,
                font.getBoldweight(), font.getColor(), font.getFontHeight(), font.getFontName(), italic,
                font.getStrikeout(), font.getTypeOffset(), font.getUnderline());
    }

    public static void setCellFontUnderline(Cell cell, byte underline) {
        Font font = getCellFont(cell);
        setCellFont(cell,
                font.getBoldweight(), font.getColor(), font.getFontHeight(), font.getFontName(), font.getItalic(),
                font.getStrikeout(), font.getTypeOffset(), underline);
    }

    public static short[] toRgb(Color color) {
        if (color == null) {
            return null;
        }

        if (color instanceof HSSFColor) {
            return ((HSSFColor) color).getTriplet();

        } else if (color instanceof XSSFColor) {
            byte[] rgb = ((XSSFColor) color).getRgb();

            // Byte to short
            if (rgb != null) {
                return applyTint(rgb, ((XSSFColor) color).getTint());
            }
        }

        return null;
    }

    private static short[] applyTint(byte[] rgb, double tint) {

        short red = toShort(rgb[0]);
        short green = toShort(rgb[1]);
        short blue = toShort(rgb[2]);

        if (tint == 0.0) { // no changes
            return new short[] { red, green, blue };
        }

        if (red == green && green == blue) { // achromatic
            final double newLum = calculateLum(red, tint);
            short v = toShort(newLum);
            return new short[] { v, v, v };
        }

        // Find brightest and darkest components
        short max = green;
        short min = red;
        if (red > green) {
            max = red;
            min = green;
        }
        if (blue > max) {
            max = blue;
        } else if (blue < min) {
            min = blue;
        }

        // Calculate colors metrics
        int chroma = max - min;
        int lum = max + min;
        final double newLum = calculateLum(lum / 2, tint) * 2;
        // new amount of chroma
        double x = (255 - Math.abs(newLum - 255)) / (255 - Math.abs(lum - 255));
        // new amount of white color
        double m = (newLum - x * chroma) / 2;

        // Adjusted RGB
        short r = toShort((red - min) * x + m);
        short g = toShort((green - min) * x + m);
        short b = toShort((blue - min) * x + m);

        return new short[] { r, g, b };

    }

    private static double calculateLum(int lum , double tint) {
        if (tint < 0) {
            return lum * (1.0 + tint);
        } else {
            return (lum - 255) * (1.0 - tint) + 255 ;
        }
    }

    private static short toShort(double value) {
        if (value >= 255) {
            return 255;
        } else if (value <= 0) {
            return 0;
        } else {
            return (short) Math.round(value);
        }
    }

    private static short toShort(byte value) {
        return (short) (value & 0xFF);
    }

    public static short[] toRgb(short colorIndex, HSSFWorkbook workbook) {
        HSSFColor cc = workbook.getCustomPalette().getColor(colorIndex);
        return toRgb(cc);
    }

    public static short[] getFontColor(Font font, Workbook workbook) {
        if (font instanceof XSSFFont) {
            XSSFColor color = ((XSSFFont) font).getXSSFColor();
            return toRgb(color);
        } else {
            short x = font.getColor();
            return toRgb(x, (HSSFWorkbook) workbook);
        }
    }

    public static short[][] getCellBorderColors(CellStyle style, Workbook workbook) {
        short[][] colors = new short[4][];

        if (style instanceof HSSFCellStyle) {
            HSSFWorkbook hssfWorkbook = (HSSFWorkbook) workbook;
            colors[0] = toRgb(style.getTopBorderColor(), hssfWorkbook);
            colors[1] = toRgb(style.getRightBorderColor(), hssfWorkbook);
            colors[2] = toRgb(style.getBottomBorderColor(), hssfWorkbook);
            colors[3] = toRgb(style.getLeftBorderColor(), hssfWorkbook);

        } else if(style instanceof XSSFCellStyle) {
            XSSFCellStyle xssfStyle = (XSSFCellStyle) style;
            colors[0] = toRgb(xssfStyle.getTopBorderXSSFColor());
            colors[1] = toRgb(xssfStyle.getRightBorderXSSFColor());
            colors[2] = toRgb(xssfStyle.getBottomBorderXSSFColor());
            colors[3] = toRgb(xssfStyle.getLeftBorderXSSFColor());
        }

        return colors;
    }

    public static short[] getCellBorderStyles(CellStyle style) {
        short[] styles = new short[4];

        styles[0] = style.getBorderTop();
        styles[1] = style.getBorderRight();
        styles[2] = style.getBorderBottom();
        styles[3] = style.getBorderLeft();

        return styles;
    }

    public static void setCellBorderColors(CellStyle style, short[][] colors, Workbook workbook) {
        if (style instanceof HSSFCellStyle) {
            HSSFWorkbook hssfWorkbook = (HSSFWorkbook) workbook;
            if (colors[0] != null) {
                style.setTopBorderColor(getOrAddColorIndex(colors[0], hssfWorkbook));
            }
            if (colors[1] != null) {
                style.setRightBorderColor(getOrAddColorIndex(colors[1], hssfWorkbook));
            }
            if (colors[2] != null) {
                style.setBottomBorderColor(getOrAddColorIndex(colors[2], hssfWorkbook));
            }
            if (colors[3] != null) {
                style.setLeftBorderColor(getOrAddColorIndex(colors[3], hssfWorkbook));
            }
        } else if(style instanceof XSSFCellStyle) {
            XSSFCellStyle xssfStyle = (XSSFCellStyle) style;
            if (colors[0] != null) {
                xssfStyle.setTopBorderColor(new XSSFColor(shortToByte(colors[0])));
            }
            if (colors[1] != null) {
                xssfStyle.setRightBorderColor(new XSSFColor(shortToByte(colors[1])));
            }
            if (colors[2] != null) {
                xssfStyle.setBottomBorderColor(new XSSFColor(shortToByte(colors[2])));
            }
            if (colors[3] != null) {
                xssfStyle.setLeftBorderColor(new XSSFColor(shortToByte(colors[3])));
            }
        }
    }

    private static byte[] shortToByte(short[] shortRgb) {
        byte rgb[] = new byte[3];
        for (int i = 0; i < 3; i++) {
            rgb[i] = (byte) (shortRgb[i] & 0xFF);
        }
        return rgb;
    }

    private static short getOrAddColorIndex(short[] rgb, HSSFWorkbook wb) {
        HSSFPalette palette = wb.getCustomPalette();
        HSSFColor color = palette.findColor((byte) rgb[0], (byte) rgb[1], (byte) rgb[2]);

        if (color == null) {
            try {
                color = palette.addColor((byte) rgb[0], (byte) rgb[1], (byte) rgb[2]);
            } catch (RuntimeException e) {
                // Could not find free color index
                color = palette.findSimilarColor(rgb[0], rgb[1], rgb[2]);
            }
        }

        return color.getIndex();
    }

}
