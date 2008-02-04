package org.unitime.timetable.form;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.util.DynamicList;
import org.unitime.timetable.util.DynamicListObjectFactory;

public class ExamEditForm extends PreferencesForm {
    private String examId;
    private String label;
    private String name;
    private String note;
    
    private Integer maxNbrRooms;
    private Integer length;
    private String seatingType;
    
    private List instructors;
    
    public String getExamId() { return examId; }
    public void setExamId(String examId) { this.examId = examId; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Integer getMaxNbrRooms() { return maxNbrRooms; }
    public void setMaxNbrRooms(Integer maxNbrRooms) { this.maxNbrRooms = maxNbrRooms; }
    public Integer getLength() { return length; }
    public void setLength(Integer length) { this.length = length; }
    public String getSeatingType() { return seatingType; }
    public void setSeatingType(String seatingType) { this.seatingType = seatingType; }
    public String[] getSeatingTypes() { return Exam.sSeatingTypes; }
    public int getSeatingTypeIdx() {
        for (int i=0;i<Exam.sSeatingTypes.length;i++)
            if (Exam.sSeatingTypes[i].equals(seatingType)) return i;
        return Exam.sSeatingTypeExam;
    }

    protected DynamicListObjectFactory factoryInstructors = new DynamicListObjectFactory() {
        public Object create() {
            return new String(Preference.BLANK_PREF_VALUE);
        }
    };

    public void reset(ActionMapping mapping, HttpServletRequest request) {
        examId = null;
        name = null;
        note = null;
        maxNbrRooms = 1;
        length = 120;
        seatingType = Exam.sSeatingTypes[Exam.sSeatingTypeExam];
        instructors = DynamicList.getInstance(new ArrayList(), factoryInstructors);
        
        super.reset(mapping, request);
    }

    public List getInstructors() { return instructors; }
    public String getInstructors(int key) { return instructors.get(key).toString(); }
    public void setInstructors(int key, Object value) { this.instructors.set(key, value); }
    public void setInstructors(List instructors) { this.instructors = instructors; }
    
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        
        ActionErrors errors = new ActionErrors();
        
        if(maxNbrRooms!=null && maxNbrRooms.intValue()<0)
            errors.add("maxNbrRooms", new ActionMessage("errors.integerGtEq", "Maximal Number of Rooms", "0") );
        
        if (length==null || length.intValue()<0)
            errors.add("length", new ActionMessage("errors.integerGtEq", "Length", "0") );
        
        // Notes has 1000 character limit
        if(note!=null && note.length()>999)
            errors.add("note", new ActionMessage("errors.maxlength", "Note", "999") );
        
        // At least one instructor is selected
        if (instructors.size()>0) {
            
            // Check no duplicates or blank instructors
            if(!super.checkPrefs(instructors))
                errors.add("instrPrefs",
                        new ActionMessage(
                                "errors.generic",
                                "Invalid instructor preference: Check for duplicate / blank selection. ") );
        }        
        
        // Check Other Preferences
        errors.add(super.validate(mapping, request));
        
        return errors;
    }
}
