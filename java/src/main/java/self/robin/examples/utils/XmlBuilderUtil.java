package self.robin.examples.utils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.Reader;
import java.io.StringReader;

public class XmlBuilderUtil {

    public static <T> T xmlStrToObject(Class<T> clazz, String xmlStr) throws Exception {
        T xmlObject = null;
        Reader reader = null;
        //利用JAXBContext将类转为一个实例
        JAXBContext context = JAXBContext.newInstance(clazz);
        //XMl 转为对象的接口
        Unmarshaller unmarshaller = context.createUnmarshaller();
        reader = new StringReader(xmlStr);
        xmlObject = (T)unmarshaller.unmarshal(reader);
        if (reader != null) {
            reader.close();
        }
        return xmlObject;
    }

}
