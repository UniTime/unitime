package org.unitime.timetable.form;

import java.util.Collection;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.unitime.commons.web.Web;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.util.ComboBoxLookup;

public class ExamListForm extends ActionForm {
    private static final long serialVersionUID = 0L;
    private String iSubjectAreaId = null;
    private String iCourseNbr = null;
    private String iOp = null;
    private Collection iSubjectAreas = null;
    private int iExamType = 0;
    private boolean iHasMidtermExams = false;
    
    public String getSubjectAreaId() { return iSubjectAreaId; }
    public void setSubjectAreaId(String subjectAreaId) { iSubjectAreaId = subjectAreaId; }
    public String getCourseNbr() { return iCourseNbr; }
    public void setCourseNbr(String courseNbr) { 
        iCourseNbr = courseNbr;
        if ("null".equals(iCourseNbr)) iCourseNbr = "";
    }
    public String getOp() { return iOp; }
    public void setOp(String op) { iOp = op; }
    public Collection getSubjectAreas() { return iSubjectAreas; }
    public void setSubjectAreas(Collection subjectAreas) { iSubjectAreas = subjectAreas; }
    
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        iSubjectAreaId = null; iCourseNbr = null; iOp = null;
        iExamType = Exam.sExamTypeFinal;
        try {
            iHasMidtermExams = Exam.hasMidtermExams(Session.getCurrentAcadSession(Web.getUser(request.getSession())).getUniqueId());
        } catch (Exception e) {}
    }
    
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        return errors;
    }
    
    public int getExamType() { return iExamType; }
    public void setExamType(int type) { iExamType = type; }
    public Collection getExamTypes() {
    	Vector ret = new Vector(Exam.sExamTypes.length);
    	for (int i=0;i<Exam.sExamTypes.length;i++) {
    	    if (i==Exam.sExamTypeMidterm && !iHasMidtermExams) continue;
    		ret.add(new ComboBoxLookup(Exam.sExamTypes[i], String.valueOf(i)));
    	}
    	return ret;
    }

}
