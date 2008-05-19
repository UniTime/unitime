package org.unitime.timetable.solver.exam;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.dom4j.Document;
import org.dom4j.Element;

import net.sf.cpsolver.exam.model.ExamPeriod;
import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.ifs.util.Progress;

public class ExamModel extends net.sf.cpsolver.exam.model.ExamModel {
    private Hashtable<ExamPeriod, Vector<ExamResourceUnavailability>>  iUnavailabilitites = null;
    
    public ExamModel(DataProperties properties) {
        super(properties);
    }
    
    public Vector<ExamResourceUnavailability> getUnavailabilities(ExamPeriod period) {
        return (iUnavailabilitites==null?null:iUnavailabilitites.get(period));
    }
    
    public void addUnavailability(ExamResourceUnavailability unavailability) {
        if (unavailability.getStudentIds().isEmpty() && unavailability.getInstructorIds().isEmpty()) return;
        if (iUnavailabilitites==null) iUnavailabilitites = new Hashtable();
        Vector<ExamResourceUnavailability> unavailabilities = iUnavailabilitites.get(unavailability.getPeriod());
        if (unavailabilities==null) {
            unavailabilities = new Vector<ExamResourceUnavailability>();
            iUnavailabilitites.put(unavailability.getPeriod(), unavailabilities);
        }
        unavailabilities.add(unavailability);
    }
    
    public boolean load(Document document) {
        if (!super.load(document)) return false;
        if (iUnavailabilitites!=null) iUnavailabilitites.clear();
        Element elements = document.getRootElement().element("notavailable");
        if (elements!=null) {
            for (Iterator i = elements.elementIterator();i.hasNext();) {
                Element element = (Element)i.next();
                ExamResourceUnavailability unavailability = new ExamResourceUnavailability(
                        getPeriod(Long.parseLong(element.attributeValue("period"))),
                        Long.valueOf(element.attributeValue("id")),
                        element.getName(),
                        element.attributeValue("name",""),
                        element.attributeValue("date",""),
                        element.attributeValue("time",""),
                        element.attributeValue("room",""),
                        Integer.parseInt(element.attributeValue("size","0")));
                for (Iterator j = element.elementIterator("student");j.hasNext();) {
                    Element e = (Element)j.next();
                    unavailability.getStudentIds().add(Long.valueOf(e.attributeValue("id")));
                }
                for (Iterator j = element.elementIterator("instructor");j.hasNext();) {
                    Element e = (Element)j.next();
                    unavailability.getInstructorIds().add(Long.valueOf(e.attributeValue("id")));
                }
                addUnavailability(unavailability);
            }
        }
        
        Progress p = Progress.getInstance(this);
        if (p!=null) {
            p.load(document.getRootElement(), true);
            p.message(Progress.MSGLEVEL_STAGE, "Restoring from backup ...");
        }
        return true;
    }
    
    public Document save() {
        Document document = super.save();
        if (document==null) return null;
        if (iUnavailabilitites!=null) {
            Element elements = document.getRootElement().addElement("notavailable");
            for (Vector<ExamResourceUnavailability> unavailabilties : iUnavailabilitites.values()) {
                for (ExamResourceUnavailability unavailability : unavailabilties) {
                    Element element = elements
                        .addElement(unavailability.getType())
                        .addAttribute("period", unavailability.getPeriod().getId().toString())
                        .addAttribute("id", unavailability.getId().toString())
                        .addAttribute("name", unavailability.getName())
                        .addAttribute("date", unavailability.getDate())
                        .addAttribute("time", unavailability.getTime())
                        .addAttribute("room", unavailability.getRoom())
                        .addAttribute("size", String.valueOf(unavailability.getSize()));
                    for (Long studentId : unavailability.getStudentIds())
                        element.addElement("student").addAttribute("id", studentId.toString());
                    for (Long studentId : unavailability.getInstructorIds())
                        element.addElement("instructor").addAttribute("id", studentId.toString());
                }
            }
        }
        Progress p = Progress.getInstance(this);
        if (p!=null) {
            Progress.getInstance(this).save(document.getRootElement());
        }
        return document;
    }
    
}
