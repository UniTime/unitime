/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.solver.exam;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.cpsolver.exam.model.Exam;
import org.cpsolver.exam.model.ExamPeriod;
import org.cpsolver.exam.model.ExamPlacement;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.util.Callback;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.Progress;
import org.dom4j.Document;
import org.dom4j.Element;


/**
 * @author Tomas Muller
 */
public class ExamModel extends org.cpsolver.exam.model.ExamModel {
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
    
    public boolean load(Document document, Assignment<Exam, ExamPlacement> assignment) {
        return load(document, assignment, null);
    }
    
    public boolean load(Document document, Assignment<Exam, ExamPlacement> assignment, Callback saveBest) {
        if (!super.load(document, assignment, saveBest)) return false;
        if (iUnavailabilitites!=null) iUnavailabilitites.clear();
        Element elements = document.getRootElement().element("notavailable");
        if (elements!=null) {
            for (Iterator i = elements.elementIterator();i.hasNext();) {
                Element element = (Element)i.next();
                Set<Long> studentIds = new HashSet();
                Set<Long> instructorIds = new HashSet();
                for (Iterator j = element.elementIterator("student");j.hasNext();) {
                    Element e = (Element)j.next();
                    studentIds.add(Long.valueOf(e.attributeValue("id")));
                }
                for (Iterator j = element.elementIterator("instructor");j.hasNext();) {
                    Element e = (Element)j.next();
                    instructorIds.add(Long.valueOf(e.attributeValue("id")));
                }
                addUnavailability(new ExamResourceUnavailability(
                        getPeriod(Long.parseLong(element.attributeValue("period"))),
                        Long.valueOf(element.attributeValue("id")),
                        element.getName(),
                        element.attributeValue("name",""),
                        element.attributeValue("date",""),
                        element.attributeValue("time",""),
                        element.attributeValue("room",""),
                        Integer.parseInt(element.attributeValue("size","0")),
                        studentIds, instructorIds));
            }
        }
        
        Progress p = Progress.getInstance(this);
        if (p!=null) {
            p.load(document.getRootElement(), true);
            p.message(Progress.MSGLEVEL_STAGE, "Restoring from backup ...");
        }
        return true;
    }
    
    public Document save(Assignment<Exam, ExamPlacement> assignment) {
        Document document = super.save(assignment);
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
