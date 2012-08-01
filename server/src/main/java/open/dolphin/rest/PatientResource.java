package open.dolphin.rest;

import java.util.List;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import open.dolphin.infomodel.PatientModel;
import open.dolphin.session.PatientServiceBean;

/**
 * PatientResource
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 * @author modified by masuda, Masuda Naika
 */

@Path("patient")
public class PatientResource extends AbstractResource {

    private static final boolean debug = false;
    
    @Inject
    private PatientServiceBean patientServiceBean;
    
    @Context
    private HttpServletRequest servletReq;
    
    public PatientResource() {
    }


    @GET
    @Path("name/{param}/")
    @Produces(MEDIATYPE_JSON_UTF8)
    public String getPatientsByName(@PathParam("param") String param) {

        String fid = getRemoteFacility(servletReq.getRemoteUser());
        String name = param;

        List<PatientModel> patients = patientServiceBean.getPatientsByName(fid, name);

        String json = getConverter().toJson(patients);
        debug(json);
        
        return json;
    }


    @GET
    @Path("kana/{param}/")
    @Produces(MEDIATYPE_JSON_UTF8)
    public String getPatientsByKana(@PathParam("param") String param) {

        String fid = getRemoteFacility(servletReq.getRemoteUser());
        String kana = param;

        List<PatientModel> patients = patientServiceBean.getPatientsByKana(fid, kana);

        String json = getConverter().toJson(patients);
        debug(json);
        
        return json;
    }
    

    @GET
    @Path("digit/{param}/")
    @Produces(MEDIATYPE_JSON_UTF8)
    public String getPatientsByDigit(@PathParam("param") String param) {

        String fid = getRemoteFacility(servletReq.getRemoteUser());
        String digit = param;
        debug(fid);
        debug(digit);

        List<PatientModel> patients = patientServiceBean.getPatientsByDigit(fid, digit);

        String json = getConverter().toJson(patients);
        debug(json);
        
        return json;
    }


    @GET
    @Path("id/{param}/")
    @Produces(MEDIATYPE_JSON_UTF8)
    public String getPatientById(@PathParam("param") String param) {

        String fid = getRemoteFacility(servletReq.getRemoteUser());
        String pid = param;

        PatientModel patient = patientServiceBean.getPatientById(fid, pid);

        String json = getConverter().toJson(patient);
        debug(json);
        
        return json;
    }

    @GET
    @Path("pvt/{param}/")
    @Produces(MEDIATYPE_JSON_UTF8)
    public String getPatientsByPvt(@PathParam("param") String param) {

        String fid = getRemoteFacility(servletReq.getRemoteUser());
        String pvtDate = param;

        List<PatientModel> patients = patientServiceBean.getPatientsByPvtDate(fid, pvtDate);

        String json = getConverter().toJson(patients);
        debug(json);
        
        return json;
    }


    @POST
    @Consumes(MEDIATYPE_JSON_UTF8)
    @Produces(MEDIATYPE_TEXT_UTF8)
    public String postPatient(String json) {

        String fid = getRemoteFacility(servletReq.getRemoteUser());

        PatientModel patient = (PatientModel)
                getConverter().fromJson(json, PatientModel.class);
        patient.setFacilityId(fid);

        long pk = patientServiceBean.addPatient(patient);
        String pkStr = String.valueOf(pk);
        debug(pkStr);

        return pkStr;
    }


    @PUT
    @Consumes(MEDIATYPE_JSON_UTF8)
    @Produces(MEDIATYPE_TEXT_UTF8)
    public String putPatient(String json) {

        String fid = getRemoteFacility(servletReq.getRemoteUser());

        PatientModel patient = (PatientModel)
                getConverter().fromJson(json, PatientModel.class);
        patient.setFacilityId(fid);

        int cnt = patientServiceBean.update(patient);
        String pkStr = String.valueOf(cnt);
        debug(pkStr);

        return pkStr;
    }


    @Override
    protected void debug(String msg) {
        if (debug || DEBUG) {
            super.debug(msg);
        }
    }
}
