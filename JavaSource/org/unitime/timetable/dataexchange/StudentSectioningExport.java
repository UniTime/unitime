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
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.TreeSet;

import org.dom4j.Document;
import org.dom4j.Element;
import org.unitime.timetable.model.AdvisorClassPref;
import org.unitime.timetable.model.AdvisorCourseRequest;
import org.unitime.timetable.model.AdvisorInstrMthPref;
import org.unitime.timetable.model.AdvisorSectioningPref;
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
	protected static Formats.Format<Date> sDateFormat = Formats.getDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
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
	        
	        for (Student student: getHibSession().createQuery(
	        		"select s from Student s where s.session.uniqueId = :sessionId", Student.class)
	        		.setParameter("sessionId", session.getUniqueId(), org.hibernate.type.LongType.INSTANCE).list()) {
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
	        		Element majorEl = acadAreaEl.addElement("major").addAttribute("code", acm.getMajor().getCode());
	        		if (acm.getConcentration() != null)
	        			majorEl.addAttribute("concentration", acm.getConcentration().getCode());
	        		if (acm.getDegree() != null)
	        			majorEl.addAttribute("degree", acm.getDegree().getReference());
	        		if (acm.getProgram() != null)
	        			majorEl.addAttribute("program", acm.getProgram().getReference());
	        		if (acm.getCampus() != null)
	        			majorEl.addAttribute("campus", acm.getCampus().getReference());
	        		if (acm.getWeight() != null && acm.getWeight() != 1.0)
	        			majorEl.addAttribute("weight", acm.getWeight().toString());
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
	        				if (first && cd.getNoSub() != null && cd.getNoSub().booleanValue())
	        					courseOfferingEl.addAttribute("nosub", "true");
	        				if (first && cd.isAlternative())
	        					courseOfferingEl.addAttribute("alternative", "true");
	        				if (first && cd.getCritical() != null)
	        					courseOfferingEl.addAttribute("critical", CourseDemand.Critical.values()[cd.getCritical()].name().toLowerCase());
	        				if (first && cd.getCriticalOverride() != null)
	        					courseOfferingEl.addAttribute("criticalOverride", CourseDemand.Critical.values()[cd.getCriticalOverride()].name().toLowerCase());
	        				if (cr.getCredit() != null && cr.getCredit() != 0)
	        					courseOfferingEl.addAttribute("credit", String.valueOf(cr.getCredit()));
	        				if (first && cd.getWaitlistedTimeStamp() != null)
        						courseOfferingEl.addAttribute("waitlisted", sDateFormat.format(cd.getWaitlistedTimeStamp()));
	        				if (first && cd.getTimestamp() != null)
	        					courseOfferingEl.addAttribute("requested", sDateFormat.format(cd.getTimestamp()));
	        				for (StudentClassEnrollment enrollment: cr.getClassEnrollments()) {
	        	        		Element classEl = courseOfferingEl.addElement("class");
	        	        		Class_ clazz = enrollment.getClazz();
	        	        		String extId = clazz.getExternalId(cr.getCourseOffering());
	        	        		if (extId != null && !extId.isEmpty())
	        	        			classEl.addAttribute("externalId", extId);
	        	        		classEl.addAttribute("type", clazz.getSchedulingSubpart().getItypeDesc().trim());
	        	        		classEl.addAttribute("suffix", getClassSuffix(clazz));
	        	        		if (enrollment.getTimestamp() != null)
	        	        			classEl.addAttribute("enrolled", sDateFormat.format(enrollment.getTimestamp()));
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
	        	
	        	// Advisor recommendations
	        	Element recommendationsEl = studentEl.addElement("updateAdvisorRecommendations");
	        	Element recEl = null;
	        	for (AdvisorCourseRequest acr: new TreeSet<AdvisorCourseRequest>(student.getAdvisorCourseRequests())) {
	        		Element acrEl = null;
	        		if (acr.getPriority() == -1) {
	        			if (acr.getNotes() != null)
	        				recommendationsEl.addAttribute("notes",  acr.getNotes());
	        			if (acr.getTimestamp() != null)
	        				recommendationsEl.addAttribute("recommended", sDateFormat.format(acr.getTimestamp()));
	        			continue;
	        		} else if (acr.getAlternative() == 0) {
	        			recEl = recommendationsEl.addElement("recommendation");
	        			if (acr.isSubstitute())
	        				recEl.addAttribute("substitute", "true");
	        			acrEl = recEl;
	        		} else {
	        			acrEl = recEl.addElement("alternative");
	        		}
	        		if (acr.getCredit() != null) acrEl.addAttribute("credit", acr.getCredit());
	        		if (acr.getWaitlist() != null) acrEl.addAttribute("waitlist", acr.getWaitlist() ? "true" : "false");
	        		if (acr.getNoSub() != null) acrEl.addAttribute("nosub", acr.getNoSub() ? "true" : "false");
	        		if (acr.getNotes() != null) acrEl.addAttribute("notes", acr.getNotes());
	        		if (acr.getCourse() != null) acrEl.addAttribute("course", acr.getCourse());
	        		if (acr.getFreeTime() != null) {
	        			Element freeTimeEl = acrEl.addElement("freeTime");
	        			String days = "";
	        	        for (int i=0;i<Constants.DAY_NAMES_SHORT.length;i++) {
	        	        	if ((acr.getFreeTime().getDayCode() & Constants.DAY_CODES[i]) != 0) {
	        	        		days += Constants.DAY_NAMES_SHORT[i];
	        	        	}
	        	        }
	        	        freeTimeEl.addAttribute("days", days);
	        	        freeTimeEl.addAttribute("startTime", startSlot2startTime(acr.getFreeTime().getStartSlot()));
	        	        freeTimeEl.addAttribute("endTime", startSlot2startTime(acr.getFreeTime().getStartSlot() + acr.getFreeTime().getLength()));
	        	        freeTimeEl.addAttribute("length", String.valueOf(Constants.SLOT_LENGTH_MIN * acr.getFreeTime().getLength()));
	        		}
        			if (acr.getTimestamp() != null)
        				acrEl.addAttribute("recommended", sDateFormat.format(acr.getTimestamp()));
	        		if (acr.getCourseOffering() != null) {
	        			acrEl.addAttribute("subjectArea", acr.getCourseOffering().getSubjectAreaAbbv());
	        			acrEl.addAttribute("courseNumber", acr.getCourseOffering().getCourseNbr());
        				if (acr.getCritical() != null)
        					acrEl.addAttribute("critical", CourseDemand.Critical.values()[acr.getCritical()].name().toLowerCase());
        				if (acr.getPreferences() != null && !acr.getPreferences().isEmpty()) {
        					Element prefEl = acrEl.addElement("preferences");
        					for (AdvisorSectioningPref p: acr.getPreferences()) {
        						if (p instanceof AdvisorClassPref) {
        							AdvisorClassPref scp = (AdvisorClassPref)p;
        							Element classEl = prefEl.addElement("class");
        							String extId = scp.getClazz().getExternalId(acr.getCourseOffering());
    	        	        		if (extId != null && !extId.isEmpty())
    	        	        			classEl.addAttribute("externalId", extId);
    	        	        		classEl.addAttribute("type", scp.getClazz().getSchedulingSubpart().getItypeDesc().trim());
    	        	        		classEl.addAttribute("suffix", getClassSuffix(scp.getClazz()));
    	        	        		if (scp.isRequired()) classEl.addAttribute("required", "true");
        						} else if (p instanceof AdvisorInstrMthPref) {
        							AdvisorInstrMthPref imp = (AdvisorInstrMthPref)p;
        							Element imEl = prefEl.addElement("instructional-method");
        							imEl.addAttribute("externalId", imp.getInstructionalMethod().getReference());
        							imEl.addAttribute("name", imp.getInstructionalMethod().getLabel());
        							if (imp.isRequired()) imEl.addAttribute("required", "true");
        						}
        					}
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