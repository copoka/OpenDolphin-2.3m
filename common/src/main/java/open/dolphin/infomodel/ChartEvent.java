package open.dolphin.infomodel;

/**
 * Chart event通知用のモデル
 *
 * @author masuda, Masuda Naika
 */
public class ChartEvent {
    
    private String issuerUUID;
    private EVENT eventType;
    
    private long pvtPk;
    private int state;
    private String memo;
    private int byomeiCount;
    private int byomeiCountToday;
    private String ownerUUID;
    private String facilityId;
    
    private PatientVisitModel pvt;
    
    private long ptPk;
    private PatientModel patient;
    
    public static enum EVENT {PVT_STATE, PVT_ADD, PVT_DELETE, PVT_RENEW, PVT_MERGE, PM_MERGE};
    
    public ChartEvent(String issuerUUID) {
        this.issuerUUID = issuerUUID;
    }

    public void setParamFromPvt(PatientVisitModel pvt) {
        
        if (pvt == null) {
            return;
        }
        
        this.pvtPk = pvt.getId();
        this.state = pvt.getState();
        this.byomeiCount = pvt.getByomeiCount();
        this.byomeiCountToday = pvt.getByomeiCountToday();
        this.memo = pvt.getMemo();
        this.ownerUUID = pvt.getPatientModel().getOwnerUUID();
        this.facilityId = pvt.getFacilityId();
        this.ptPk = pvt.getPatientModel().getId();
    }
    
    public void setEventType(EVENT eventType) {
        this.eventType = eventType;
    }
    public void setPvtPk(long pk) {
        pvtPk = pk;        
    }
    public void setState(int state) {
        this.state = state;
    }
    public void setByomeiCount(int count) {
        byomeiCount = count;
    }
    public void setByomeiCountToday(int count) {
        byomeiCountToday = count;
    }
    public void setIssuerUUID(String issuer) {
        issuerUUID= issuer;
    }
    public void setOwnerUUID(String owner) {
        ownerUUID = owner;
    }
    public void setPatientVisitModel(PatientVisitModel pvt) {
        this.pvt = pvt;
    }
    public void setPtPk(long ptPk) {
        this.ptPk = ptPk;
    }
    public void setPatientModel(PatientModel patient) {
        this.patient = patient;
    }
    public void setMemo(String memo) {
        this.memo = memo;
    }
    public void setFacilityId(String fid) {
        facilityId = fid;
    }
    
    public EVENT getEventType() {
        return eventType;
    }
    public long getPvtPk() {
        return pvtPk;
    }
    public int getState() {
        return state;
    }
    public int getByomeiCount() {
        return byomeiCount;
    }
    public int getByomeiCountToday() {
        return byomeiCountToday;
    }
    public String getIssuerUUID() {
        return issuerUUID;
    }
    public String getOwnerUUID() {
        return ownerUUID;
    }
    public PatientVisitModel getPatientVisitModel() {
        return pvt;
    }
    public long getPtPk() {
        return ptPk;
    }
    public PatientModel getPatientModel() {
        return patient;
    }
    public String getMemo() {
        return memo;
    }
    public String getFacilityId() {
        return facilityId;
    }

}
