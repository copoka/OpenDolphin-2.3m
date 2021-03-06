package open.dolphin.delegater;

import com.sun.jersey.api.client.WebResource;
import java.text.SimpleDateFormat;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import open.dolphin.client.ClientContext;
import open.dolphin.infomodel.JsonConverter;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 * Bsiness Delegater のルートクラス。
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 * @author modified by masuda, Masuda Naika
 */
public class BusinessDelegater {

    protected static final String CAMMA = ",";
    
//masuda^
    private static final String DATE_TIME_FORMAT_REST = "yyyy-MM-dd HH:mm:ss";
    protected static final SimpleDateFormat REST_DATE_FRMT = new SimpleDateFormat(DATE_TIME_FORMAT_REST);
    private static final String CHARSET_UTF8 = "; charset=UTF-8";
    protected static final String MEDIATYPE_JSON_UTF8 = MediaType.APPLICATION_JSON + CHARSET_UTF8;
    protected static final String MEDIATYPE_TEXT_UTF8 = MediaType.TEXT_PLAIN + CHARSET_UTF8;
    protected static final int HTTP200 = 200;
//masuda$
    
    protected Logger logger;

    protected boolean DEBUG;
    
    public BusinessDelegater() {
        logger = ClientContext.getDelegaterLogger();
        DEBUG = (logger.getLevel() == Level.DEBUG);
    }

    //protected WebResource.Builder getResource(String path) {
    //    return JerseyClient.getInstance().getResource(path);
    //}
    
    protected WebResource.Builder getResource(String path, MultivaluedMap<String, String> qmap) {
        return JerseyClient.getInstance().getResource(path, qmap);
    }

    protected void debug(int status, String entity) {
        logger.debug("---------------------------------------");
        logger.debug("status = " + status);
        logger.debug(entity);
    }
    
    protected JsonConverter getConverter() {
        return JsonConverter.getInstance();
    }
}
