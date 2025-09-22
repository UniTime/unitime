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
package org.unitime.timetable.dataexchange;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.SolverParameterDef;
import org.unitime.timetable.model.SolverParameterGroup;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.test.UpdateExamConflicts;

public class ExamImport extends CourseOfferingImport {
	protected List<Exam> exams;
	
	public ExamImport() {
		super();
		rootElementName = "exams";
	}
	
	@Override
	public void loadXml(Element rootElement) throws Exception {
		initializeTrimLeadingZeros();
		
		try {
	        if (!rootElement.getName().equalsIgnoreCase(rootElementName)) {
	        	throw new Exception("Given XML file is not an Examinations load file.");
	        }
	        beginTransaction();
	        
	        incremental = "true".equalsIgnoreCase(rootElement.attributeValue("incremental", "false"));
	        if (incremental)
	        	info("Incremental mode.");
	        
	        includeExams = rootElement.attributeValue("includeExams", rootElement.attributeValue("type", "all"));
	        if (!"none".equals(includeExams))
	        	info("Includes " + includeExams + " exams.");
	        
	        SolverParameterDef maxRoomsParam = SolverParameterDef.findByNameType(getHibSession(), "Exams.MaxRooms", SolverParameterGroup.SolverType.EXAM);
	        if (maxRoomsParam != null && maxRoomsParam.getDefault() != null) 
	        	defaultMaxNbrRooms = Integer.valueOf(maxRoomsParam.getDefault());

	        initializeLoad(rootElement, rootElementName);
	        
			preLoadAction();
			loadExams(rootElement);
			commitTransaction();
			
			beginTransaction();
			postLoadAction();
			commitTransaction();
	        
		} catch (Exception e) {
			fatal("Exception: " + e.getMessage(), e);
			rollbackTransaction();
			throw e;
		} finally {
		}
		
		if (examPeriodChanged && session!=null && ApplicationProperty.DataExchangeUpdateStudentConflictsFinal.isTrue()) {
            try {
                beginTransaction();
                for (ExamType type: ExamType.findAllOfType(ExamType.sExamTypeFinal))
                	new UpdateExamConflicts(this).update(session.getUniqueId(), type.getUniqueId(), getHibSession());
                commitTransaction();
            } catch (Exception e) {
                fatal("Exception: " + e.getMessage(), e);
                rollbackTransaction();
            }
        }

		if (examPeriodChanged && session!=null && ApplicationProperty.DataExchangeUpdateStudentConflictsMidterm.isTrue()) {
            try {
                beginTransaction();
                for (ExamType type: ExamType.findAllOfType(ExamType.sExamTypeMidterm))
                	new UpdateExamConflicts(this).update(session.getUniqueId(), type.getUniqueId(), getHibSession());
                commitTransaction();
            } catch (Exception e) {
                fatal("Exception: " + e.getMessage(), e);
                rollbackTransaction();
            }
        }
		
		addNote("Records Changed: " + changeCount);
		updateChangeList(true);
		reportMissingLocations();
		mailLoadResults();
	}
	
	@Override
	protected void preLoadAction() {
        exams = new ArrayList<Exam>(getHibSession().createQuery(
        		"select x from Exam x left join fetch x.owners o where x.session.uniqueId = :sessionId", Exam.class
        		).setParameter("sessionId", session.getUniqueId())
        		.list());
	}
	
	protected void loadExams(Element rootElement) throws Exception{    
		for ( Iterator<?> it = rootElement.elementIterator(); it.hasNext(); ) {
    		Element element = (Element) it.next();
    		try {
    			importExam(element, null, exams);   
    			flush(true);
    		} catch (Exception e) {
    			addNote("Not Loading 'exam' Error:  " + e.getMessage());
    			e.printStackTrace();
    			addNote("\t " + element.asXML());
    			updateChangeList(true);
    			rollbackTransaction(); beginTransaction();
    		}
    	}
	}
	
	@Override
	protected void postLoadAction() {
		if (!incremental)
			for(Iterator<Exam> i = exams.iterator(); i.hasNext();){
				Exam exam = i.next();
				if ("final".equals(includeExams) && exam.getExamType().getType() != ExamType.sExamTypeFinal) continue;
				if ("midterm".equals(includeExams) && exam.getExamType().getType() != ExamType.sExamTypeMidterm) continue;
				addNote("\tremoved exam: " + exam.getLabel() + " (" + exam.getExamType().getReference() + ")");
				exam = ExamDAO.getInstance().get(exam.getUniqueId(), getHibSession());
				exam.deleteDependentObjects(getHibSession(), false);
				getHibSession().remove(exam);
			}	
	}

}
