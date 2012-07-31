
package open.dolphin.impl.orcaapi;

import java.util.Date;
import java.util.List;
import open.dolphin.client.Chart;
import open.dolphin.client.IDiagnosisSender;
import open.dolphin.infomodel.MedicalModModel;
import open.dolphin.infomodel.RegisteredDiagnosisModel;
import open.dolphin.project.Project;

/**
 * Orca21ApiDiagnosisSender
 * 
 * @author masuda, Masuda Naika
 */
public class Orca21ApiDiagnosisSender implements IDiagnosisSender {

    private Chart context;
    
    @Override
    public Chart getContext() {
        return context;
    }

    @Override
    public void setContext(Chart context) {
        this.context = context;
    }

    @Override
    public void prepare(List<RegisteredDiagnosisModel> data) {
    }

    @Override
    public void send(List<RegisteredDiagnosisModel> rdList) {
        
        if (rdList == null || rdList.isEmpty() || context == null) {
            return;
        }
        
        // ORCA API使用しない場合はリターン
        if (!Project.getBoolean(Project.USE_ORCA_API)) {
            return;
        }
        
        MedicalModModel modModel = new MedicalModModel();
        modModel.setContext(context);
        modModel.setDepartmentCode(context.getPatientVisit().getDeptCode());
        modModel.setPhysicianCode(Project.getString(Project.ORCA_STAFF_CODE));
        modModel.setPerformDate(new Date());
        modModel.setDiagnosisList(rdList);
        
        OrcaApi.getInstance().send(modModel);
    }
    
}
