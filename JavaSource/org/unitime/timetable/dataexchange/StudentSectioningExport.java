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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.TreeSet;

import org.dom4j.Document;
import org.dom4j.Element;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.StudentAreaClassificationMajor;
import org.unitime.timetable.model.StudentAreaClassificationMinor;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentClassPref;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.StudentInstrMthPref;
import org.unitime.timetable.model.StudentSectioningPref;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;

public class StudentSectioningExport extends BaseExport {
	protected static Formats.Format<Number> sTwoNumbersDF = Formats.getNumberFormat("00");
	protected DecimalFormat iCreditDF = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.US));
	
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
	        	if (student.getSectioningStatus() != null)
	        		studentEl.addAttribute("status", student.getSectioningStatus().getReference());
	        	
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
	        	if (student.getMinCredit() != null)
	        		demographicsEl.addAttribute("minCredit", iCreditDF.format(student.getMinCredit()));
	        	if (student.getMaxCredit() != null)
	        		demographicsEl.addAttribute("maxCredit", iCreditDF.format(student.getMaxCredit()));
	        	
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
	        				if (first && cd.getCritical() != null)
	        					courseOfferingEl.addAttribute("critical", cd.isCritical() ? "true" : "false");
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
	        				if (cr.getPreferences() != null && !cr.getPreferences().isEmpty()) {
	        					Element prefEl = courseOfferingEl.addElement("preferences");
	        					for (StudentSectioningPref p: cr.getPreferences()) {
	        						if (p instanceof StudentClassPref) {
	        							StudentClassPref scp = (StudentClassPref)p;
	        							Element classEl = prefEl.addElement("class");
	        							String extId = scp.getClazz().getExternalId(cr.getCourseOffering());
	    	        	        		if (extId != null && !extId.isEmpty())
	    	        	        			classEl.addAttribute("externalId", extId);
	    	        	        		classEl.addAttribute("type", scp.getClazz().getSchedulingSubpart().getItypeDesc().trim());
	    	        	        		classEl.addAttribute("suffix", getClassSuffix(scp.getClazz()));
	    	        	        		if (scp.isRequired()) classEl.addAttribute("required", "true");
	        						} else if (p instanceof StudentInstrMthPref) {
	        							StudentInstrMthPref imp = (StudentInstrMthPref)p;
	        							Element imEl = prefEl.addElement("instructional-method");
	        							imEl.addAttribute("externalId", imp.getInstructionalMethod().getReference());
	        							imEl.addAttribute("name", imp.getInstructionalMethod().getLabel());
	        							if (imp.isRequired()) imEl.addAttribute("required", "true");
	        						}
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