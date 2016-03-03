/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.webutil.csv;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.TreeSet;

import org.cpsolver.ifs.util.CSVFile;
import org.cpsolver.ifs.util.CSVFile.CSVField;
import org.unitime.timetable.form.ClassListForm;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalMethod;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.CachedClassAssignmentProxy;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.exam.ExamAssignmentProxy;


/**
 * @author Tomas Muller
 */
public class CsvClassListTableBuilder extends CsvInstructionalOfferingTableBuilder {

	public CsvClassListTableBuilder() {
		super();
	}
	
	protected String additionalNote(){
		return(new String());
	}
	
	protected String labelForTable(SubjectArea subjectArea){
		StringBuffer sb = new StringBuffer();
		sb.append(subjectArea.getSubjectAreaAbbreviation());
		sb.append(" - ");
		sb.append(subjectArea.getSession().getLabel());
		sb.append(additionalNote());
		return(sb.toString());		
	}
	
	
	public void csvTableForClasses(PrintWriter out, ClassAssignmentProxy classAssignment, ExamAssignmentProxy examAssignment, ClassListForm form, SessionContext context) throws Exception {
		setVisibleColumns(form);
        
		TreeSet classes = (TreeSet) form.getClasses();
		if (isShowTimetable()) {
			boolean hasTimetable = false;
			if (context.hasPermission(Right.ClassAssignments) && classAssignment != null) {
            	if (classAssignment instanceof CachedClassAssignmentProxy) {
            		((CachedClassAssignmentProxy)classAssignment).setCache(classes);
            	}
				for (Iterator i=classes.iterator();i.hasNext();) {
					Object[] o = (Object[])i.next(); Class_ clazz = (Class_)o[0];
						if (classAssignment.getAssignment(clazz)!=null) {
						hasTimetable = true; break;
						}	
				}
			}
			setDisplayTimetable(hasTimetable);
		}
		setUserSettings(context.getUser());
		
        if (isShowExam())
            setShowExamTimetable(examAssignment != null || Exam.hasTimetable(context.getUser().getCurrentAcademicSessionId()));
    
        iFile = new CSVFile();
		csvBuildTableHeader(context.getUser().getCurrentAcademicSessionId());
		int ct = 0;
		Iterator it = classes.iterator();
		String prevLabel = null;
		while (it.hasNext()){
			Object[] o = (Object[])it.next(); Class_ c = (Class_)o[0]; CourseOffering co = (CourseOffering)o[1];
			csvBuildClassRow(classAssignment, examAssignment, ++ct, co, c, "", context, prevLabel);
			prevLabel = c.getClassLabel(co);
		}
		
		save(out);
    }
	
    protected CSVField csvBuildPrefGroupLabel(CourseOffering co, PreferenceGroup prefGroup, String indentSpaces, boolean isEditable, String prevLabel) {
    	if (prefGroup instanceof Class_) {
    		String label = prefGroup.toString();
        	Class_ aClass = (Class_) prefGroup;
        	label = aClass.getClassLabel(co);
        	if (prevLabel != null && label.equals(prevLabel)){
        		label = "";
        	}
        	CSVField cell = createCell();
        	addText(cell, indentSpaces+label, true);
	        InstructionalMethod im = aClass.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalMethod();
	        if (aClass.isCancelled()) {
	        	if (im != null)
	        		addText(cell, " (" + MSG.statusCancelled() + ", " + im.getReference() + ")", false);
	        	else
	        		addText(cell, " (" + MSG.statusCancelled() + ")", false);
	        } else if (im != null)
	        	addText(cell, " (" + im.getReference() + ")", false);
        	return cell;
    	} else return super.csvBuildPrefGroupLabel(co, prefGroup, indentSpaces, isEditable, null);
    }
    
    protected TreeSet getExams(Class_ clazz) {
        //exams directly attached to the given class
        TreeSet ret = new TreeSet(Exam.findAll(ExamOwner.sOwnerTypeClass, clazz.getUniqueId()));
        //check whether the given class is of the first subpart of the config
        SchedulingSubpart subpart = clazz.getSchedulingSubpart();
        if (subpart.getParentSubpart()!=null) return ret; 
        InstrOfferingConfig config = subpart.getInstrOfferingConfig();
        SchedulingSubpartComparator cmp = new SchedulingSubpartComparator();
        for (Iterator i=config.getSchedulingSubparts().iterator();i.hasNext();) {
            SchedulingSubpart s = (SchedulingSubpart)i.next();
            if (cmp.compare(s,subpart)<0) return ret;
        }
        InstructionalOffering offering = config.getInstructionalOffering();
        //check passed -- add config/offering/course exams to the class exams
        ret.addAll(Exam.findAll(ExamOwner.sOwnerTypeConfig, config.getUniqueId()));
        ret.addAll(Exam.findAll(ExamOwner.sOwnerTypeOffering, offering.getUniqueId()));
        for (Iterator i=offering.getCourseOfferings().iterator();i.hasNext();) {
            CourseOffering co = (CourseOffering)i.next();
            ret.addAll(Exam.findAll(ExamOwner.sOwnerTypeCourse, co.getUniqueId()));
        }
        return ret;
    }

}
