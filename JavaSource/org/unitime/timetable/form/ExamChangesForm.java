package org.unitime.timetable.form;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionMapping;
import org.unitime.timetable.model.UserData;

public class ExamChangesForm extends ExamReportForm {
    private String iChage = sChangeInitial;
    public static final String sChangeInitial = "Initial";
    public static final String sChangeBest = "Best";
    public static final String[] sChanges = new String[] { sChangeInitial, sChangeBest }; 
    private boolean iReverse = false;
    private boolean iNoSolver = false;
    
    public boolean getReverse() { return iReverse; }
    public void setReverse(boolean reverse) { iReverse = reverse; }
    public String getChangeType() { return iChage; }
    public void setChangeType(String changeType) { iChage = changeType; }
    public String[] getChangeTypes() { return sChanges; }
    
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
        iChage = sChangeInitial; iReverse = false;
    }
    
    public void load(HttpSession session) {
        super.load(session);
        setReverse(UserData.getPropertyBoolean(session,"ExamChanges.reverse", false));
        setChangeType(UserData.getProperty(session,"ExamChanges.changeType", sChangeInitial));
    }
        
    public void save(HttpSession session) {
        super.save(session);
        UserData.setPropertyBoolean(session,"ExamChanges.reverse", getReverse());
        UserData.setProperty(session,"ExamChanges.changeType", getChangeType());
    }
    
    public boolean getNoSolver() { return iNoSolver;}
    public void setNoSolver(boolean noSolver) { iNoSolver = noSolver;}
}
