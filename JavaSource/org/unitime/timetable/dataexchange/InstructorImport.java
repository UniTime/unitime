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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.dom4j.Element;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.InstructorAttribute;
import org.unitime.timetable.model.InstructorAttributeType;
import org.unitime.timetable.model.PositionType;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.util.DateUtils;

public class InstructorImport extends PreferencesImport {
	protected boolean iIncremental = false;
	protected boolean iPreferences = false;
	
    public void loadXml(Element root) throws Exception {
        if (!root.getName().equalsIgnoreCase("instructors")) {
        	throw new Exception("Given XML file is not instructors load file.");
        }
        try {
            beginTransaction();

            String sCampus = root.attributeValue("campus");
            String sYear   = root.attributeValue("year");
            String sTerm   = root.attributeValue("term");
            String created = root.attributeValue("created");
            iDateFormat = new SimpleDateFormat(root.attributeValue("dateFormat", "yyyy/M/d"), Locale.US);
            iIncremental = "true".equalsIgnoreCase(root.attributeValue("incremental", "true"));
            iPreferences = "true".equalsIgnoreCase(root.attributeValue("preferences", "true"));
	        String timeFormat = root.attributeValue("timeFormat", "HHmm");
	        if (!"HHmm".equals(timeFormat))
	        	iTimeFormat = new SimpleDateFormat(timeFormat, Locale.US);
            iSession = Session.getSessionUsingInitiativeYearTerm(sCampus, sYear, sTerm);
            if (iSession == null)
                throw new Exception("No session found for the given campus, year, and term.");

            if (created != null) {
                ChangeLog.addChange(getHibSession(), getManager(), iSession, iSession, created, ChangeLog.Source.DATA_IMPORT_INSTRUCTORS, ChangeLog.Operation.UPDATE, null, null);
            }
            
            Map<String, InstructorAttribute> attributes = new HashMap<String, InstructorAttribute>();
            for (InstructorAttribute attribute: getHibSession().createQuery(
            		"from InstructorAttribute where session.uniqueId = :sessionId", InstructorAttribute.class)
            		.setParameter("sessionId", iSession.getUniqueId()).list()) {
            	if (attribute.getDepartment() == null)
            		attributes.put(attribute.getCode(), attribute);
            	else
            		attributes.put(attribute.getDepartment().getDeptCode() + "|" + attribute.getCode(), attribute);
            }
            Map<InstructorAttribute, String> fixAttributeParent = new HashMap<InstructorAttribute, String>();
            
            List<DepartmentalInstructor> instructors = new ArrayList<DepartmentalInstructor>(getHibSession().createQuery(
	        		"from DepartmentalInstructor di where di.department.session.uniqueId = :sessionId " +
	        		"order by di.department.deptCode, di.lastName, di.firstName", DepartmentalInstructor.class)
	        		.setParameter("sessionId", iSession.getUniqueId()).list());

            for (Iterator i = root.elementIterator("instructor"); i.hasNext(); ) {
            	Element element = (Element)i.next();
            	Department department = Department.findByDeptCode(getRequiredStringAttribute(element, "department"), iSession.getUniqueId(), getHibSession());
            	if (department == null) {
            		warn("Department " + getRequiredStringAttribute(element, "department") + " does not exist.");
            		continue;
            	}
            	DepartmentalInstructor instructor = lookupInstructor(instructors, element);
            	if (instructor == null) {
            		instructor = new DepartmentalInstructor();
            		instructor.setDepartment(department);
            		instructor.setAttributes(new HashSet<InstructorAttribute>());
            		instructor.setPreferences(new HashSet<Preference>());
            	}
            	instructor.setExternalUniqueId(getOptionalStringAttribute(element, "externalId"));
            	instructor.setFirstName(getOptionalStringAttribute(element, "firstName"));
            	instructor.setLastName(getRequiredStringAttribute(element, "lastName"));
            	instructor.setMiddleName(getOptionalStringAttribute(element, "middleName"));
            	instructor.setCareerAcct(getOptionalStringAttribute(element, "careerAcct"));
            	instructor.setAcademicTitle(getOptionalStringAttribute(element, "title"));
            	instructor.setEmail(getOptionalStringAttribute(element, "email"));
            	Element noteEl = element.element("note");
            	instructor.setNote(noteEl == null ? null : noteEl.getText());
            	instructor.setIgnoreToFar(getOptionalBooleanAttribute(element, "ignoreTooFar", false));
            	String positionRef = getOptionalStringAttribute(element, "position");
            	instructor.setPositionType(positionRef == null ? null : PositionType.findByRef(positionRef));
            	String roleRef = getOptionalStringAttribute(element, "role");
            	instructor.setRole(roleRef == null ? null : Roles.getRole(roleRef, getHibSession()));
            	if (instructor.getUniqueId() == null) {
            		getHibSession().persist(instructor);
            		debug("Created " + instructor.getName("last-middle-first") + " from " + instructor.getDepartment().getDeptCode() + ".");
            	} else {
            		debug("Updated " + instructor.getName("last-middle-first") + " from " + instructor.getDepartment().getDeptCode() + ".");
            	}

            	Set<InstructorAttribute> remainingAttributes = new HashSet<InstructorAttribute>(instructor.getAttributes());
            	for (Iterator j = element.elementIterator("attribute"); j.hasNext(); ) {
            		Element attEl = (Element)j.next();
            		InstructorAttribute attribute = lookupAttribute(attEl, attributes, fixAttributeParent);
            		if (attribute != null) {
            			if (!remainingAttributes.remove(attribute)) {
            				instructor.addToAttributes(attribute);
            				attribute.addToInstructors(instructor);
            			}
            		}
            	}
            	for (InstructorAttribute attribute: remainingAttributes) {
            		instructor.getAttributes().remove(attribute);
            		attribute.getInstructors().remove(instructor);
            	}
            	
            	if (iPreferences) {
            		Element tp = element.element("teachingPref");
            		instructor.setMaxLoad(tp == null ? null : Float.parseFloat(tp.attributeValue("maxLoad", "0.0")));
            		instructor.setTeachingPreference(tp == null ? null : PreferenceLevel.getPreferenceLevel(tp.attributeValue("level", PreferenceLevel.sProhibited)));
            		Element unavEl = element.element("unavailabilities");
            		instructor.setUnavailableOffset(null);
            		instructor.setUnavailableDays(null);
            		if (unavEl != null) {
            			BitSet weekCode = new BitSet();
                        int startMonth = instructor.getDepartment().getSession().getPatternStartMonth();
            			int endMonth = instructor.getDepartment().getSession().getPatternEndMonth();
            			int year = instructor.getDepartment().getSession().getSessionStartYear();
                		Map<Date, Integer> date2index = new HashMap<Date, Integer>();
                		int idx = 0;
                		for (int m = startMonth; m <= endMonth; m++) {
                			int daysOfMonth = DateUtils.getNrDaysOfMonth(m, year);
                			for (int d = 1; d <= daysOfMonth; d++) {
                				date2index.put(DateUtils.getDate(d, m, year), idx);
                				idx++;
                			}
                		}
                		for (Iterator dIt = unavEl.elementIterator("dates"); dIt.hasNext(); ) {
                			Element dEl = (Element)dIt.next();
                			Date sd = iDateFormat.parse(dEl.attributeValue("fromDate", dEl.attributeValue("date")));
                			Date ed = iDateFormat.parse(dEl.attributeValue("toDate", dEl.attributeValue("date")));
                			Integer i1 = date2index.get(sd);
                			Integer i2 = date2index.get(ed);
                			if (i1 != null && i2 != null)
                				for (int j = i1; j <= i2; j++)
                					weekCode.set(j, true);
                		}
                		instructor.setUnavailableBitSet(weekCode);
            		}
            		instructor.getPreferences().clear();
                	for (Iterator j = element.elementIterator(); j.hasNext(); ) {
                    	Element prefElement = (Element)j.next();
                    	Preference preference = createPreference(prefElement, instructor);
                    	if (preference == null) continue;
                    	preference.setOwner(instructor);
                    	instructor.getPreferences().add(preference);
                    }
            	}
            	
            	getHibSession().merge(instructor);
            }
            
            if (!iIncremental) {
            	info("Removing old instructors...");
            	for (DepartmentalInstructor inst: instructors) {
            		debug("Removed " + inst.getName("last-middle-first") + " from " + inst.getDepartment().getDeptCode() + ".");
                    HashSet<Class_> updatedClasses = new HashSet<Class_>();
                    for (Iterator<ClassInstructor> i=inst.getClasses().iterator();i.hasNext();) {
        	        	ClassInstructor ci = i.next();
        	        	Class_ c = ci.getClassInstructing();
        	        	updatedClasses.add(c);
        	        	c.getClassInstructors().remove(ci);
        	        	getHibSession().remove(ci);
        	        }           
                    
                    for (Iterator<Exam> i=inst.getExams().iterator();i.hasNext();) {
                        Exam exam = i.next();
                        exam.getInstructors().remove(inst);
                        getHibSession().merge(exam);
                    }
        	        
        	        for (Iterator<Assignment> i=inst.getAssignments().iterator();i.hasNext();) {
        	        	Assignment a = i.next();
        	        	a.getInstructors().remove(inst);
        	        	getHibSession().merge(a);
        	        }
        	        
        	        Department d = null;
        	        if (inst.getDepartment()!=null) {
        	        	d = inst.getDepartment();
        	        }
        	        d.getInstructors().remove(inst);
        	        
        	        getHibSession().remove(inst);
            	}
            }
            
            for (Map.Entry<InstructorAttribute, String> e: fixAttributeParent.entrySet()) {
            	InstructorAttribute attribute = e.getKey();
            	InstructorAttribute parent = attributes.get(e.getValue());
            	if (parent != null) {
            		attribute.setParentAttribute(parent);
            		parent.addToChildAttributes(attribute);
            		getHibSession().merge(attribute);
            	}
            }
            
        	info("All done.");
        	
            commitTransaction();
        } catch (Exception e) {
            fatal("Exception: " + e.getMessage(), e);
            rollbackTransaction();
            throw e;
        }
	}
    
    protected InstructorAttribute lookupAttribute(Element el, Map<String, InstructorAttribute> attributes, Map<InstructorAttribute, String> fixAttributeParents) throws Exception {
    	String code = getRequiredStringAttribute(el, "code");
    	String deptCode = getOptionalStringAttribute(el, "department");
    	InstructorAttribute attribute = attributes.get(deptCode == null ? code : (deptCode + "|" + code));
    	if (attribute != null) return attribute;
    	String type = getOptionalStringAttribute(el, "type");
    	if (type == null) return null;
    	InstructorAttributeType atType = getHibSession()
    			.createQuery("from InstructorAttributeType where reference = :reference", InstructorAttributeType.class)
    			.setParameter("reference", type).uniqueResult();
    	if (atType == null) {
    		warn("Attribute type " + type + " does not exist.");
    		return null;
    	}
    	attribute = new InstructorAttribute();
    	attribute.setCode(code);
    	attribute.setType(atType);
    	if (deptCode != null)
    		attribute.setDepartment(Department.findByDeptCode(deptCode, iSession.getUniqueId()));
    	attribute.setName(getOptionalStringAttribute(el, "name"));
    	if (deptCode != null)
    		attribute.setDepartment(Department.findByDeptCode(deptCode, iSession.getUniqueId()));
    	attribute.setSession(iSession);
    	getHibSession().persist(attribute);
    	attributes.put(deptCode == null ? code : (deptCode + "|" + code), attribute);
    	String parent = getOptionalStringAttribute(el, "parent");
    	if (parent != null)
    		fixAttributeParents.put(attribute, deptCode == null ? parent : (deptCode + "|" + parent));
    	return attribute;
    }
    
    protected DepartmentalInstructor lookupInstructor(List<DepartmentalInstructor> instructors, Element el) throws Exception {
    	String deptCode = getRequiredStringAttribute(el, "department");
    	String externalId = getOptionalStringAttribute(el, "externalId");
    	if (externalId != null && !externalId.isEmpty()) {
    		for (Iterator<DepartmentalInstructor> i = instructors.iterator(); i.hasNext(); ) {
    			DepartmentalInstructor di = i.next();
    			if (di.getDepartment().getDeptCode().equals(deptCode) && externalId.equals(di.getExternalUniqueId())) {
    				i.remove();
    				return di;
    			}
    		}
    	} else {
    		String email = getOptionalStringAttribute(el, "email");
    		if (email != null && !email.isEmpty()) {
    			for (Iterator<DepartmentalInstructor> i = instructors.iterator(); i.hasNext(); ) {
    				DepartmentalInstructor di = i.next();
    				if (di.getDepartment().getDeptCode().equals(deptCode) && email.equals(di.getEmail())) {
    					i.remove();
    					return di;
    				}
    			}
    		}
	    	String lastName = getRequiredStringAttribute(el, "lastName");
	    	String firstName = getOptionalStringAttribute(el, "firstName");
	    	if (firstName == null) firstName = "";
	    	for (Iterator<DepartmentalInstructor> i = instructors.iterator(); i.hasNext(); ) {
				DepartmentalInstructor di = i.next();
				if (di.getDepartment().getDeptCode().equals(deptCode) && di.getExternalUniqueId() == null && lastName.equals(di.getLastName()) && firstName.equals(di.getFirstName())) {
					i.remove();
					return di;
				}
			}
    	}
    	return null;
    }

}
