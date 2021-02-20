package self.robin.examples.spark.sources.v2.excel;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.spark.sql.catalyst.expressions.GenericRow;
import org.apache.spark.sql.execution.datasources.CodecStreams;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @Description: ...
 * @Author: Li Yalei - Robin
 * @Date: 2021/2/20 17:59
 */
public interface SparkWorkbookHelper {

    /**
     * 获取表单猎头的表单集合
     * @param sheet 表单对象
     * @param startRow 从第几行开始（大于等于1）
     * @param startCol 从第几列开始（大于等于1）
     * @param header 首行是否是标题行（首行即为startRow那行）
     * @return 表单列头集合
     */
    static List<String> getColumnNames(Sheet sheet, int startRow, int startCol, boolean header){
        if(sheet==null){
            return new ArrayList<>();
        }

        Iterator<Row> rowIte = sheet.rowIterator();
        int rowIndex = startRow;

        while (--rowIndex>0 && rowIte.hasNext()) {
            rowIte.next();
        }

        if(!rowIte.hasNext()){
            return new ArrayList<>();
        }
        Row row = rowIte.next();
        int colIndex = startCol;

        Iterator<Cell> colIte = row.iterator();
        while (--colIndex>0 && colIte.hasNext()){
            colIte.next();
        }

        List<String> cols = new ArrayList<>();
        while (colIte.hasNext()){
            if(header){
                cols.add(colIte.next().getStringCellValue());
            } else {
                cols.add("col_"+(colIte.next().getColumnIndex()+1));
            }
        }
        return cols;
    }

    /**
     * 根据 path 和 hadoop Configuration 创建 workbook对象
     * @param path
     * @param conf
     * @return wb
     */
    static Workbook createWorkbook(String path, Configuration conf) throws IOException {

        InputStream inputStream;

        String developModel = conf.get("develop.master");
        if (developModel.startsWith("local")) {
            inputStream = new FileInputStream(path);
        } else {
            inputStream = CodecStreams.createInputStreamWithCloseResource(conf, new Path(path));
        }

        try(InputStream is = inputStream){
            if (path.endsWith(".xls")) {
                return new HSSFWorkbook(is);
            } else if (path.endsWith(".xlsx")) {
                return new XSSFWorkbook(is);
            } else {
                throw new IOException("File format is not supported");
            }
        }
    }


    /**
     * 将excel中的row 转化为 rdd中的 row
     * @param row
     * @return
     */
    static org.apache.spark.sql.Row poiRow2SparkRow(org.apache.poi.ss.usermodel.Row row) {
        Iterator<Cell> cellIte = row.cellIterator();

        List cellBuffer = new ArrayList();
        Cell cell;
        while (cellIte.hasNext()) {
            cell = cellIte.next();
            switch (cell.getCellTypeEnum()) {
                case NUMERIC:
                    cellBuffer.add(cell.getNumericCellValue());
                    break;
                case BOOLEAN:
                    cellBuffer.add(cell.getBooleanCellValue());
                    break;
                case STRING:
                    cellBuffer.add(cell.getStringCellValue());
                    break;
                case BLANK:
                    cellBuffer.add(null);
                    break;
                default:
                    throw new RuntimeException("unSupport cell type " + cell.getCellTypeEnum());
            }
        }
        return new GenericRow(cellBuffer.toArray());
    }
}
