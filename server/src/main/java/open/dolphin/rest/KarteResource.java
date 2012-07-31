package open.dolphin.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.*;
import open.dolphin.infomodel.*;
import open.dolphin.session.KarteServiceBean;

/**
 * KarteResource
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 * @author modified by masuda, Masuda Naika
 */
@Path("karte")
public class KarteResource extends AbstractResource {

    private static final boolean debug = false;
    
    @Inject
    private KarteServiceBean karteServiceBean;

    /** Creates a new instance of KarteResource */
    public KarteResource() {
    }

    @GET
    @Path("{ptId}")
    @Produces(MEDIATYPE_JSON_UTF8)
    public String getKarte(@PathParam("ptId") Long patientPK, 
            @QueryParam("fromDate") String fromDateStr) {

        Date fromDate = parseDate(fromDateStr);

        KarteBean result = karteServiceBean.getKarte(patientPK, fromDate);
        
        String json = getConverter().toJson(result);
        debug(json);
        
        return json;
    }

    //-------------------------------------------------------

    @GET
    @Path("docinfo/{id}")
    @Produces(MEDIATYPE_JSON_UTF8)
    public String getDocumentList(@PathParam("id") Long karteId, 
            @QueryParam("fromDate") String fromDateStr, 
            @QueryParam("inculdeModified") Boolean includeModified) {

        Date fromDate = parseDate(fromDateStr);

        List<DocInfoModel> result = karteServiceBean.getDocumentList(karteId, fromDate, includeModified);

        String json = getConverter().toJson(result);
        debug(json);
        
        return json;
    }


    @GET
    @Path("document")
    @Produces(MEDIATYPE_JSON_UTF8)
    public String getDocuments(@QueryParam("ids") String ids) {

        List<Long> list = toLongList(ids);

        List<DocumentModel> result = karteServiceBean.getDocuments(list);

        String json = getConverter().toJson(result);
        debug(json);
        
        return json;
    }


    @POST
    @Path("document")
    @Consumes(MEDIATYPE_JSON_UTF8)
    @Produces(MEDIATYPE_TEXT_UTF8)
    public String postDocument(String json) {

        DocumentModel document = (DocumentModel) 
                getConverter().fromJson(json, DocumentModel.class);
        
        // 関係を構築する
        List<ModuleModel> modules = document.getModules();
        if (modules!=null && modules.size()>0) {
            for (ModuleModel m : modules) {
                m.setDocumentModel(document);
            }
        }
        List<SchemaModel> schemas = document.getSchema();
        if (schemas!=null && schemas.size()>0) {
            for (SchemaModel m : schemas) {
                m.setDocumentModel(document);
            }
        }

        long result = karteServiceBean.addDocument(document);
        String pkStr = String.valueOf(result);
        debug(pkStr);

        return pkStr;
    }

    
    @PUT
    @Path("document/{id}")
    @Consumes(MEDIATYPE_TEXT_UTF8)
    @Produces(MEDIATYPE_TEXT_UTF8)
    public String putTitle(@PathParam("id") String idStr, String title) {

        long id = Long.valueOf(idStr);

        int result = karteServiceBean.updateTitle(id, title);

        return String.valueOf(result);
    }


    @DELETE
    @Path("document/{id}")
    public void deleteDocument(@PathParam("id") String idStr) {

        long id = Long.valueOf(idStr);

        int cnt = karteServiceBean.deleteDocument(id);
        String cntStr = String.valueOf(cnt);
        debug(cntStr);
    }

    //-------------------------------------------------------

    @GET
    @Path("modules/{id}")
    @Produces(MEDIATYPE_JSON_UTF8)
    public String getModules(@PathParam("id") Long karteId,
            @QueryParam("froms") String fromStr,
            @QueryParam("tos") String toStr,
            @QueryParam("entity") String entity) {

        List<Date> fromList = new ArrayList<Date>();
        List<Date> toList = new ArrayList<Date>();
        String[] froms = fromStr.split(CAMMA);
        for (String str : froms) {
            fromList.add(parseDate(str));
        }
        String[] tos = toStr.split(CAMMA);
        for (String str : tos) {
            toList.add(parseDate(str));
        }

        List<List<ModuleModel>> result = karteServiceBean.getModules(karteId, entity, fromList, toList);

        String json = getConverter().toJson(result);
        debug(json);
        
        return json;
    }

/*
    @GET
    @Path("iamges/{id}")
    @Produces(MEDIATYPE_JSON_UTF8)
    public String getImages(@PathParam("id") Long karteId,
            @QueryParam("froms") String fromStr,
            @QueryParam("tos") String toStr) {

        List<Date> fromList = new ArrayList<Date>();
        List<Date> toList = new ArrayList<Date>();
        String[] froms = fromStr.split(CAMMA);
        for (String str : froms) {
            fromList.add(parseDate(str));
        }
        String[] tos = tos.split(CAMMA);
        for (String str : tos) {
            toList.add(parseDate(str));
        }

        List<List<SchemaModel>> result = karteServiceBean.getImages(karteId, fromList, toList);

        String json = getConverter().toJson(result);
        debug(json);
        
        return json;
    }
*/
    
    @GET
    @Path("image/{id}")
    @Produces(MEDIATYPE_JSON_UTF8)
    public String getImage(@PathParam("param") Long karteId) {

        SchemaModel result = karteServiceBean.getImage(karteId);

        String json = getConverter().toJson(result);
        debug(json);
        
        return json;
    }

    //-------------------------------------------------------

    @GET
    @Path("diagnosis/{id}")
    @Produces(MEDIATYPE_JSON_UTF8)
    public String getDiagnosis(@PathParam("id") Long karteId, 
            @QueryParam("fromDate") String fromDateStr, 
            @QueryParam("activeOnly") Boolean activeOnly) {
        
        Date fromDate = parseDate(fromDateStr);

        List<RegisteredDiagnosisModel> list = karteServiceBean.getDiagnosis(karteId, fromDate, activeOnly);

        String json = getConverter().toJson(list);
        debug(json);
        
        return json;
    }

    @POST
    @Path("diagnosis")
    @Consumes(MEDIATYPE_JSON_UTF8)
    @Produces(MEDIATYPE_TEXT_UTF8)
    public String postDiagnosis(String json) {
        
        TypeReference typeRef = new TypeReference<List<RegisteredDiagnosisModel>>(){};
        List<RegisteredDiagnosisModel> list = (List<RegisteredDiagnosisModel>)
                getConverter().fromJsonTypeRef(json, typeRef);
        
        List<Long> result = karteServiceBean.addDiagnosis(list);
        String text = fromLongList(result);
        debug(text);

        return text;
    }

    @PUT
    @Path("diagnosis")
    @Consumes(MEDIATYPE_JSON_UTF8)
    @Produces(MEDIATYPE_TEXT_UTF8)
    public String putDiagnosis(String json) {

        TypeReference typeRef = new TypeReference<List<RegisteredDiagnosisModel>>(){};
        List<RegisteredDiagnosisModel> list = (List<RegisteredDiagnosisModel>)
                getConverter().fromJsonTypeRef(json, typeRef);

        int result = karteServiceBean.updateDiagnosis(list);
        String text = String.valueOf(result);
        debug(text);

        return text;
    }

    @DELETE
    @Path("diagnosis")
    public void deleteDiagnosis(@QueryParam("ids") String ids) {

        List<Long> list = toLongList(ids);
        int result = karteServiceBean.removeDiagnosis(list);

        debug(String.valueOf(result));
    }


    @POST
    @Path("observations")
    @Consumes(MEDIATYPE_JSON_UTF8)
    @Produces(MEDIATYPE_TEXT_UTF8)
    public String postObservations(String json) {
        
        TypeReference typeRef = new TypeReference<List<ObservationModel>>(){};
        List<ObservationModel> list = (List<ObservationModel>)
                getConverter().fromJsonTypeRef(json, typeRef);

        List<Long> result = karteServiceBean.addObservations(list);

        String text = fromLongList(result);
        debug(text);

        return text;
    }

    @DELETE
    @Path("observations")
    public void deleteObservations(@QueryParam("ids") String ids) {

        List<Long> list = toLongList(ids);
        int result = karteServiceBean.removeObservations(list);

        debug(String.valueOf(result));
    }

    @PUT
    @Path("memo")
    @Consumes(MEDIATYPE_JSON_UTF8)
    @Produces(MEDIATYPE_TEXT_UTF8)
    public String putPatientMemo(String json) {

        PatientMemoModel memo = (PatientMemoModel)
                getConverter().fromJson(json, PatientMemoModel.class);

        int result = karteServiceBean.updatePatientMemo(memo);
        String text = String.valueOf(result);
        debug(text);

        return text;
    }

    @GET
    @Path("appo/{id}")
    @Produces(MEDIATYPE_JSON_UTF8)
    public String getAppoinmentList(@PathParam("id") Long karteId,
            @QueryParam("froms") String fromStr,
            @QueryParam("tos") String toStr) {

        List<Date> fromList = new ArrayList<Date>();
        List<Date> toList = new ArrayList<Date>();
        String[] froms = fromStr.split(CAMMA);
        for (String str : froms) {
            fromList.add(parseDate(str));
        }
        String[] tos = toStr.split(CAMMA);
        for (String str : tos) {
            toList.add(parseDate(str));
        }

        List<List<AppointmentModel>> result = karteServiceBean.getAppointmentList(karteId, fromList, toList);

        String json = getConverter().toJson(result);
        debug(json);

        return json;
    }

    @Override
    protected void debug(String msg) {
        if (debug || DEBUG) {
            super.debug(msg);
        }
    }
}
