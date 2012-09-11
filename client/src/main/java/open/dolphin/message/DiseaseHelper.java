package open.dolphin.message;

import java.util.List;

/**
 * DiseaseHelper
 *
 * @author Kazushi Minagawa
 * @author modified by masuda, Masuda Naika
 */
public final class DiseaseHelper implements IMessageHelper {
        
    private String patientId;
    private String confirmDate;
    private String groupId;
    private String department;
    private String departmentDesc;
    private String creatorName;
    private String creatorId;
    private String creatorLicense;
    private String facilityName;
    private String jmariCode;
    private List<DiagnosisModuleItem> diagnosisModuleItems;
    
//masuda^
    private boolean useDefaultDept;
    
    public void setUseDefalutDept(boolean b) {
        useDefaultDept = b;
    }
    public boolean isUseDefaultDept() {
        return useDefaultDept;
    }
    
    private static final String TEMPLATE = "diseaseHelper";
    
    @Override
    public String getTemplateName() {
        return TEMPLATE;
    }
//masuda$
    
    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getConfirmDate() {
        return confirmDate;
    }

    public void setConfirmDate(String confirmDate) {
        this.confirmDate = confirmDate;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDepartmentDesc() {
        return departmentDesc;
    }

    public void setDepartmentDesc(String departmentDesc) {
        this.departmentDesc = departmentDesc;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getCreatorLicense() {
        return creatorLicense;
    }

    public void setCreatorLicense(String creatorLicense) {
        this.creatorLicense = creatorLicense;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    public String getJmariCode() {
        return jmariCode;
    }

    public void setJmariCode(String jmariCode) {
        this.jmariCode = jmariCode;
    }

    public List<DiagnosisModuleItem> getDiagnosisModuleItems() {
        return diagnosisModuleItems;
    }

    public void setDiagnosisModuleItems(List<DiagnosisModuleItem> diagnosisModuleItems) {
        this.diagnosisModuleItems = diagnosisModuleItems;
    }
}
