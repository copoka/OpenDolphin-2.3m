package open.dolphin.server.pvt;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import open.dolphin.infomodel.PatientVisitModel;
import open.dolphin.pvtclaim.PVTBuilder;
import open.dolphin.session.MasudaServiceBean;
import open.dolphin.session.PVTServiceBean;

/**
 * PvtPostTask
 * @author masuda, Masuda Naika
 */
public class PvtPostTask implements Callable {
    
    private static final String jndiNamePvt
            = "java:global/OpenDolphin-server-2.3/" + PVTServiceBean.class.getSimpleName();
    private static final String jndiNameMsd
            = "java:global/OpenDolphin-server-2.3/" + MasudaServiceBean.class.getSimpleName();
    
    private static final Logger logger = Logger.getLogger(PvtPostTask.class.getSimpleName());
    
    private String pvtXml;

    // ここはInjectionダメみたい
    private PVTServiceBean pvtServiceBean;

    private MasudaServiceBean masudaServiceBean;
    
    
    public PvtPostTask(String pvtXml) throws NamingException {
        InitialContext ic = new InitialContext();
        pvtServiceBean = (PVTServiceBean) ic.lookup(jndiNamePvt);
        masudaServiceBean = (MasudaServiceBean) ic.lookup(jndiNameMsd);
        this.pvtXml = pvtXml;
    }

    @Override
    public PatientVisitModel call() throws Exception {
        
        // pvtXmlからPatientVisitModelを作成する
        BufferedReader br = new BufferedReader(new StringReader(pvtXml));
        PVTBuilder builder = new PVTBuilder();
        builder.parse(br);
        PatientVisitModel pvt = builder.getProduct();
        
        // jmari番号に対応するfacilityIdを取得する
        String fid = masudaServiceBean.getFidFromJmari(pvt.getJmariNumber());
        pvt.setFacilityId(fid);
        
        // PVT登録するか否か
        Map<String, String> propMap = masudaServiceBean.getUserPropertyMap(fid);
        String value = propMap.get("pvtOnServer");
        boolean pvtOnServer = "true".equals(value);
        
        if (pvtOnServer) {
            pvtServiceBean.addPvt(pvt);
            StringBuilder sb = new StringBuilder();
            sb.append("PVT post: ").append(pvt.getPvtDate());
            sb.append(", Fid=").append(pvt.getFacilityId());
            sb.append(", PtID=").append(pvt.getPatientId());
            sb.append(", Name=").append(pvt.getPatientName());
            logger.info(sb.toString());
            return pvt;
        }
        return null;
    }
    
 }