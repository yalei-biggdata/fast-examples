package self.robin.examples.spark.sources;

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



    /** 是否首行是header */
    private boolean header;

    private Iterator<Sheet> sheetIterator;

    private Iterator<Row> rowIterator;

    public SheetIterator(boolean header, Iterator<Sheet> sheetIterator){
        this.header = header;
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
            if(header){
                //首行是标题
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
