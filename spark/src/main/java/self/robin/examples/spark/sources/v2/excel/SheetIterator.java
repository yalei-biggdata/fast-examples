package self.robin.examples.spark.sources.v2.excel;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.Serializable;
import java.util.Iterator;

/**
 * @Description: ...
 * @Author: Li Yalei - Robin
 * @Date: 2021/2/8 19:16
 */
public class SheetIterator implements Iterator<Row>, Serializable {

    /** startRow */
    private int startRow;

    private Iterator<Sheet> sheetIterator;

    private Iterator<Row> rowIterator;

    public SheetIterator(int startRow, Iterator<Sheet> sheetIterator){
        this.sheetIterator = sheetIterator;
    }

    @Override
    public boolean hasNext() {
        if(this.rowIterator==null || !this.rowIterator.hasNext()){
            if(this.sheetIterator==null || !this.sheetIterator.hasNext()){
                //sheetIterator is null OR sheetIterator is empty
                return false;
            }
            this.rowIterator = this.sheetIterator.next().rowIterator();
            int count = 0;
            while (count++<startRow) {
                this.rowIterator.next();
            }
        }
        return this.rowIterator.hasNext();
    }

    @Override
    public Row next() {
        return rowIterator.next();
    }

}
