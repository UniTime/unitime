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

import java.util.List;
import java.util.Properties;
import java.util.TreeSet;

import org.dom4j.Document;
import org.dom4j.Element;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.CourseRequestOption;
import org.unitime.timetable.model.InstructionalMethod;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.StudentAreaClassificationMajor;
import org.unitime.timetable.model.StudentAreaClassificationMinor;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.InstructionalMethodDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;

import com.google.protobuf.InvalidProtocolBufferException;

public class StudentSectioningExport extends BaseExport {
	protected static Formats.Format<Number> sTwoNumbersDF = Formats.getNumberFormat("00");
	
	@Override
	public void saveXml(Document document, Session session, Properties parameters) throws Exception {
		try {
			beginTransaction();
			
			Element root = document.addElement("request");
	        root.addAttribute("campus", session.getAcademicInitiative());
	        root.addAttribute("year", session.getAcademicYear());
	        root.addAttribute("term", session.getAcademicTerm());
	        
	        document.addDocType("request", "-//UniTime//UniTime Student Sectioning DTD/EN", "http://www.unitime.org/interface/StudentSectioning.dtd");
	        
	        for (Student student: (List<Student>)getHibSession().createQuery(
	        		"select s from Student s where s.session.uniqueId = :sessionId")
	        		.setLong("sessionId", session.getUniqueId()).list()) {
	        	Element studentEl = root.addElement("student");
	        	studentEl.addAttribute("key", student.getExternalUniqueId() == null || student.getExternalUniqueId().isEmpty() ? student.getUniqueId().toString() : student.getExternalUniqueId());
	        	
	        	// Student demographics
	        	Element demographicsEl = studentEl.addElement("updateDemographics");
	        	Element nameEl = demographicsEl.addElement("name");
	        	if (student.getFirstName() != null)
	        		nameEl.addAttribute("first", student.getFirstName());
	        	if (student.getMiddleName() != null)
	        		nameEl.addAttribute("middle", student.getMiddleName());
	        	if (student.getLastName() != null)
	        		nameEl.addAttribute("last", student.getLastName());
	        	for (StudentAreaClassificationMajor acm: student.getAreaClasfMajors()) {
	        		Element acadAreaEl = demographicsEl.addElement("acadArea");
	        		acadAreaEl.addAttribute("abbv", acm.getAcademicArea().getAcademicAreaAbbreviation());
	        		acadAreaEl.addAttribute("classification", acm.getAcademicClassification().getCode());
	        		acadAreaEl.addElement("major").addAttribute("code", acm.getMajor().getCode());
	        	}
	        	for (StudentAreaClassificationMinor acm: student.getAreaClasfMinors()) {
	        		Element acadAreaEl = demographicsEl.addElement("acadArea");
	        		acadAreaEl.addAttribute("abbv", acm.getAcademicArea().getAcademicAreaAbbreviation());
	        		acadAreaEl.addAttribute("classification", acm.getAcademicClassification().getCode());
	        		acadAreaEl.addElement("minor").addAttribute("code", acm.getMinor().getCode());
	        	}
	        	for (StudentGroup group: student.getGroups())
	        		demographicsEl.addElement("groupAffiliation").addAttribute("code", group.getGroupAbbreviation());
	        	for (StudentAccomodation acc: student.getAccomodations())
	        		demographicsEl.addElement("disability").addAttribute("code", acc.getAbbreviation());
	        	
	        	// Course requests
	        	Element requestsEl = studentEl.addElement("updateCourseRequests").addAttribute("commit", "true");
	        	for (CourseDemand cd: new TreeSet<CourseDemand>(student.getCourseDemands())) {
	        		if (cd.getFreeTime() != null) {
	        			Element freeTimeEl = requestsEl.addElement("freeTime");
	        			String days = "";
	        	        for (int i=0;i<Constants.DAY_NAMES_SHORT.length;i++) {
	        	        	if ((cd.getFreeTime().getDayCode() & Constants.DAY_CODES[i]) != 0) {
	        	        		days += Constants.DAY_NAMES_SHORT[i];
	        	        	}
	        	        }
	        	        freeTimeEl.addAttribute("days", days);
	        	        freeTimeEl.addAttribute("startTime", startSlot2startTime(cd.getFreeTime().getStartSlot()));
	        	        freeTimeEl.addAttribute("endTime", startSlot2startTime(cd.getFreeTime().getStartSlot() + cd.getFreeTime().getLength()));
	        	        freeTimeEl.addAttribute("length", String.valueOf(Constants.SLOT_LENGTH_MIN * cd.getFreeTime().getLength()));
	        		}
	        		if (!cd.getCourseRequests().isEmpty()) {
	        			Element courseOfferingEl = null;
	        			boolean first = true;
	        			for (CourseRequest cr: new TreeSet<CourseRequest>(cd.getCourseRequests())) {
	        				courseOfferingEl = (courseOfferingEl == null ? requestsEl.addElement("courseOffering") : courseOfferingEl.addElement("alternative"));
	        				courseOfferingEl.addAttribute("subjectArea", cr.getCourseOffering().getSubjectAreaAbbv());
	        				courseOfferingEl.addAttribute("courseNumber", cr.getCourseOffering().getCourseNbr());
	        				if (first && cd.isWaitlist())
	        					courseOfferingEl.addAttribute("waitlist", "true");
	        				if (first && cd.isAlternative())
	        					courseOfferingEl.addAttribute("alternative", "true");
	        				if (cr.getCredit() != null && cr.getCredit() != 0)
	        					courseOfferingEl.addAttribute("credit", String.valueOf(cr.getCredit()));
	        				for (StudentClassEnrollment enrollment: cr.getClassEnrollments()) {
	        	        		Element classEl = courseOfferingEl.addElement("class");
	        	        		Class_ clazz = enrollment.getClazz();
	        	        		String extId = clazz.getExternalId(cr.getCourseOffering());
	        	        		if (extId != null && !extId.isEmpty())
	        	        			classEl.addAttribute("externalId", extId);
	        	        		classEl.addAttribute("type", clazz.getSchedulingSubpart().getItypeDesc().trim());
	        	        		classEl.addAttribute("suffix", getClassSuffix(clazz));
	        				}
	        				for (CourseRequestOption option: cr.getCourseRequestOptions()) {
	        	        		if (OnlineSectioningLog.CourseRequestOption.OptionType.REQUEST_PREFERENCE.getNumber() == option.getOptionType()) {
	        	        			try {
	        	        				OnlineSectioningLog.CourseRequestOption pref = option.getOption();
	        	        				Element prefEl = courseOfferingEl.addElement("preferences");
	        	        				if (pref.getInstructionalMethodCount() > 0)
	        	        					for (OnlineSectioningLog.Entity im: pref.getInstructionalMethodList()) {
	        	        						InstructionalMethod meth = InstructionalMethodDAO.getInstance().get(im.getUniqueId(), getHibSession());
	        	        						if (meth != null) {
	        	        							Element imEl = prefEl.addElement("instructional-method");
	        	        							imEl.addAttribute("externalId", meth.getReference());
	        	        							imEl.addAttribute("name", meth.getLabel());
	        	        						} else {
		        	        						Element imEl = prefEl.addElement("instructional-method");
		        	        						if (im.hasExternalId())
		        	        							imEl.addAttribute("externalId", im.getExternalId());
		        	        						if (im.hasName())
		        	        							imEl.addAttribute("name", im.getName());
	        	        						}
	        	        					}
	        	        				if (pref.getSectionCount() > 0)
	        	        					for (OnlineSectioningLog.Section s: pref.getSectionList()) {
	        	        						Class_ clazz = Class_DAO.getInstance().get(s.getClazz().getUniqueId(), getHibSession());
	        	        						if (clazz != null) {
	        	        							Element classEl = prefEl.addElement("class");
	        	        							String extId = clazz.getExternalId(cr.getCourseOffering());
	        	    	        	        		if (extId != null && !extId.isEmpty())
	        	    	        	        			classEl.addAttribute("externalId", extId);
	        	    	        	        		classEl.addAttribute("type", clazz.getSchedulingSubpart().getItypeDesc().trim());
	        	    	        	        		classEl.addAttribute("suffix", getClassSuffix(clazz));
	        	        						} else {
		        	        						Element classEl = prefEl.addElement("class");
		        	        						classEl.addAttribute("suffix", s.getClazz().getExternalId());
		        	        						if (s.getSubpart().hasName())
		        	        							classEl.addAttribute("type", s.getSubpart().getName());
		        	        						else if (s.getSubpart().hasExternalId())
		        	        							classEl.addAttribute("type", s.getSubpart().getExternalId().trim());
	        	        						}
	        	        					}
	        	            		} catch (InvalidProtocolBufferException e) {}
	        	        		}
	        	        	}
	        				first = false;
	        			}
	        		}
	        	}
	        }
	        
            commitTransaction();
        } catch (Exception e) {
            fatal("Exception: "+e.getMessage(),e);
            rollbackTransaction();
		}
	}
	
    private static String startSlot2startTime(int startSlot) {
        int minHrs = startSlot * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
        return sTwoNumbersDF.format(minHrs/60)+sTwoNumbersDF.format(minHrs%60);
    }

}