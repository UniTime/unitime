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
package org.unitime.timetable.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.hibernate.Transaction;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.comparators.InstructorComparator;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.util.DynamicList;
import org.unitime.timetable.util.DynamicListObjectFactory;


/**
 * @author Stephanie Schluttenhofer, Zuzana Mullerova, Tomas Muller
 */
public class ClassInstructorAssignmentForm extends ActionForm {

	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	private String op;
    private Integer subjectAreaId;
	private Long instrOfferingId;
    private String instrOfferingName;
	private Integer instrOffrConfigLimit;
	private Long instrOffrConfigId;
	private String deletedInstrRowNum;
    private String nextId;
    private String previousId;
	private ClassAssignmentProxy proxy;
	private String addInstructorId;
	private Boolean displayExternalId;

	private List classIds;
	private List classLabels;
	private List classLabelIndents;
	private List instructorUids;
	private List percentShares;
	private List leadFlags;
	private List displayFlags;
	private List times;
	private List rooms;
	private List allowDeletes;
	private List readOnlyClasses;
	private List classHasErrors;
	private List showDisplay;
	private List externalIds;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -203441190483028649L;
	/**
	 * 
	 */

    // --------------------------------------------------------- Classes

    /** Factory to create dynamic list element for Course Offerings */
    protected DynamicListObjectFactory factoryClasses = new DynamicListObjectFactory() {
        public Object create() {
            return new String(Preference.BLANK_PREF_VALUE);
        }
    };
		
	public void reset(ActionMapping arg0, HttpServletRequest arg1) {
		op = "";
        nextId = previousId = null;
        subjectAreaId = new Integer(0);
    	instrOfferingId = new Long(0);
        instrOfferingName = null;
    	instrOffrConfigLimit = new Integer(0);
    	instrOffrConfigId = new Long(0);
    	deletedInstrRowNum = null;
    	displayExternalId = new Boolean(false);
    	proxy = null;	
    	resetLists();
	}

	private void resetLists() {
    	classIds = DynamicList.getInstance(new ArrayList(), factoryClasses);
    	classLabels = DynamicList.getInstance(new ArrayList(), factoryClasses);
    	classLabelIndents = DynamicList.getInstance(new ArrayList(), factoryClasses);
    	instructorUids = DynamicList.getInstance(new ArrayList(), factoryClasses);
    	percentShares = DynamicList.getInstance(new ArrayList(), factoryClasses);
    	leadFlags = DynamicList.getInstance(new ArrayList(), factoryClasses);
    	displayFlags = DynamicList.getInstance(new ArrayList(), factoryClasses);
    	times = DynamicList.getInstance(new ArrayList(), factoryClasses);
    	rooms = DynamicList.getInstance(new ArrayList(), factoryClasses);
    	allowDeletes = DynamicList.getInstance(new ArrayList(), factoryClasses);
    	readOnlyClasses = DynamicList.getInstance(new ArrayList(), factoryClasses);
    	classHasErrors = DynamicList.getInstance(new ArrayList(), factoryClasses);
    	showDisplay = DynamicList.getInstance(new ArrayList(), factoryClasses);
    	externalIds = DynamicList.getInstance(new ArrayList(), factoryClasses);
	}

	public ActionErrors validate(ActionMapping arg0, HttpServletRequest arg1) {
		ActionErrors errors = new ActionErrors();

        if(op.equals(MSG.actionUpdateClassInstructorsAssignment()) || op.equals(MSG.actionNextIO()) || op.equals(MSG.actionPreviousIO())) {	
            // Check Added Instructors
	        for (int i = 0; i < classIds.size(); i++) {
	        	String classId = (String) classIds.get(i);
	        	String instrUid = (String) instructorUids.get(i);
	        	for (int j = i + 1; j < classIds.size(); j++) {
	        		if (((String) instructorUids.get(j)).length() > 0) {
		        		if(classIds.get(j).equals(classId) && instructorUids.get(j).equals(instrUid)) {
		        			errors.add("duplicateInstructor", new ActionMessage("errors.generic", MSG.errorDuplicateInstructorForClass()));
		        		}
	        		}
	        	}
	        }
        }
        return errors;
	}

	public void addToClasses(Class_ cls, Boolean isReadOnly, String indent){
		ArrayList instructors = new ArrayList(cls.getClassInstructors());
		Collections.sort(instructors, new InstructorComparator());
		ClassInstructor instructor = null;
		int i = 0;
		do {
			if(instructors.size() > 0) {
				instructor = (ClassInstructor) instructors.get(i);
			}
			// Only display the class name and display flag for the first instructor
			if(i == 0) {
				this.classLabels.add(cls.htmlLabel());
				this.showDisplay.add(new Boolean(true));
				this.times.add(cls.buildAssignedTimeHtml(getProxy()));
				this.rooms.add(cls.buildAssignedRoomHtml(getProxy()));
				this.externalIds.add(cls.getClassSuffix() == null?"":cls.getClassSuffix());
			}
			else {
				this.classLabels.add("");
				this.showDisplay.add(new Boolean(false));
				this.times.add("");
				this.rooms.add("");
				this.externalIds.add("");
			}
			this.classLabelIndents.add(indent);
			this.classIds.add(cls.getUniqueId().toString());
			this.readOnlyClasses.add(isReadOnly.toString());
			this.classHasErrors.add(new Boolean(false));
			this.displayFlags.add(cls.isDisplayInstructor());
	
			if(instructors.size() > 0) {
				this.instructorUids.add(instructor.getInstructor().getUniqueId().toString());
				this.percentShares.add(instructor.getPercentShare().toString());
				this.leadFlags.add(instructor.isLead());
			}
			else {
				this.instructorUids.add("");
				this.percentShares.add("100");
				this.leadFlags.add(new Boolean(true));
			}
			
			this.allowDeletes.add(new Boolean(instructors.size() > 1));
		} while (++i < instructors.size());
	}

	public void deleteInstructor() {
		int index = Integer.parseInt(deletedInstrRowNum);
		int firstIndex = index;
		while (firstIndex>0 && classIds.get(firstIndex-1).equals(classIds.get(index)))
			firstIndex--;
		int lastIndex = index;
		while (lastIndex+1<classIds.size() && classIds.get(lastIndex+1).equals(classIds.get(index)))
			lastIndex++;
		classIds.remove(index);
		classLabels.remove(index==firstIndex?index+1:index);
		classLabelIndents.remove(index==firstIndex?index+1:index);
		classHasErrors.remove(index);
		instructorUids.remove(index);
		percentShares.remove(index);
		if (index<leadFlags.size())
			leadFlags.remove(index);
		times.remove(index==firstIndex?index+1:index);
		rooms.remove(index==firstIndex?index+1:index);
		allowDeletes.remove(index);
		if (firstIndex+1==lastIndex) {
			allowDeletes.set(firstIndex, Boolean.FALSE);
		}
		if ((index==firstIndex?index+1:index)<displayFlags.size())
			displayFlags.remove(index==firstIndex?index+1:index);
		showDisplay.remove(index==firstIndex?index+1:index);
		readOnlyClasses.remove(index);
		externalIds.remove(index==firstIndex?index+1:index);
	}

	public void addInstructor() {
		int pos = new Integer(this.getAddInstructorId()).intValue();
		this.classLabels.add(pos + 1, "");
		this.showDisplay.add(pos + 1, Boolean.FALSE);
		this.times.add(pos + 1, "");
		this.rooms.add(pos + 1, "");
		this.classLabelIndents.add(pos + 1, this.classLabelIndents.get(pos));
		this.classIds.add(pos + 1, this.classIds.get(pos));
		this.readOnlyClasses.add(pos + 1, this.readOnlyClasses.get(pos));
		this.classHasErrors.add(pos + 1, Boolean.FALSE);
		this.displayFlags.add(pos + 1, this.displayFlags.get(pos));
		this.instructorUids.add(pos + 1, "");
		this.percentShares.add(pos + 1, "0");
		this.allowDeletes.set(pos, Boolean.TRUE);
		this.allowDeletes.add(pos + 1, Boolean.TRUE);
		this.externalIds.add(pos + 1, "");
		this.leadFlags.add(pos + 1, this.leadFlags.get(pos));
	}

	public void updateClasses() throws Exception {
	    Class_DAO cdao = new Class_DAO();
	    for (int i = 0; i < classIds.size(); ) {
	    	if ("true".equals(getReadOnlyClasses().get(i))) {
	    		i++;
	    		continue;
	    	}
	    	
			String classId = (String) classIds.get(i);
		    Class_ c = cdao.get(new Long(classId));

		    org.hibernate.Session hibSession = cdao.getSession();
        	Transaction tx = hibSession.beginTransaction();

            // Class all instructors
            Set classInstrs = c.getClassInstructors();
            for (Iterator iter=classInstrs.iterator(); iter.hasNext() ;) {
                ClassInstructor ci = (ClassInstructor) iter.next();
                DepartmentalInstructor instr = ci.getInstructor();
                instr.getClasses().remove(ci);
                hibSession.saveOrUpdate(instr);
                hibSession.delete(ci);
            }
            
            classInstrs.clear();

            c.setDisplayInstructor(new Boolean("on".equals(getDisplayFlags().get(i))));

            // Save instructor data to class
            for ( ; i < classIds.size(); i++) {
            	boolean sameClass = ((String) classIds.get(i)).equals(classId);
            	if (!sameClass)	{
            		break;
            	}
                String instrId = (String) getInstructorUids().get(i);
                if (instrId.length() > 0  && !("-".equals(instrId))) {
	                String pctShare = (String) getPercentShares().get(i);
	                Boolean lead = new Boolean("on".equals(getLeadFlags().get(i)));
	                
	                DepartmentalInstructor deptInstr =  new DepartmentalInstructorDAO().get(new Long(instrId));
	                ClassInstructor classInstr = new ClassInstructor();
	                classInstr.setClassInstructing(c);
	                classInstr.setInstructor(deptInstr);
	                classInstr.setLead(lead);
	                classInstr.setTentative(false);
	                classInstr.setPercentShare(new Integer(pctShare));
	                
	                deptInstr.getClasses().add(classInstr);
	                hibSession.saveOrUpdate(deptInstr);
	                
	                classInstrs.add(classInstr);
	            };
            };
            
            c.setClassInstructors(classInstrs);

        	try {
                hibSession.saveOrUpdate(c);
	            tx.commit();
        	} catch (Exception e) {
        		tx.rollback(); throw e;
        	}
		}
	}

	public void unassignAllInstructors() throws Exception {
	    Class_DAO cdao = new Class_DAO();
	    for (int i = 0; i < classIds.size(); i++ ) {
	    	if ("true".equals(getReadOnlyClasses().get(i))) {
	    		i++;
	    		continue;
	    	}
	    	
			String classId = (String) classIds.get(i);
		    Class_ c = cdao.get(new Long(classId));

		    org.hibernate.Session hibSession = cdao.getSession();
        	Transaction tx = hibSession.beginTransaction();
        	try {
    		    c.deleteClassInstructors(hibSession);
                hibSession.saveOrUpdate(c);
	            tx.commit();
        	} catch (Exception e) {
        		tx.rollback(); throw e;
        	}
		}
	    this.getInstructorUids().clear();
	}

	public String getOp() {
		return op;
	}

	public void setOp(String op) {
		this.op = op;
	}

	public List getAllowDeletes() {
		return allowDeletes;
	}

	public void setAllowDeletes(List allowDeletes) {
		this.allowDeletes = allowDeletes;
	}

	public List getClassIds() {
		return classIds;
	}

	public void setClassIds(List classIds) {
		this.classIds = classIds;
	}

	public List getClassLabelIndents() {
		return classLabelIndents;
	}

	public void setClassLabelIndents(List classLabelIndents) {
		this.classLabelIndents = classLabelIndents;
	}

	public List getClassLabels() {
		return classLabels;
	}

	public void setClassLabels(List classLabels) {
		this.classLabels = classLabels;
	}

	public List getDisplayFlags() {
		return displayFlags;
	}

	public void setDisplayFlags(List displayFlags) {
		this.displayFlags = displayFlags;
	}

	public List getInstructorUids() {
		return instructorUids;
	}

	public void setInstructorUids(List instructorUids) {
		this.instructorUids = instructorUids;
	}

	public List getLeadFlags() {
		return leadFlags;
	}

	public void setLeadFlags(List leadFlags) {
		this.leadFlags = leadFlags;
	}

	public List getPercentShares() {
		return percentShares;
	}

	public void setPercentShares(List percentShares) {
		this.percentShares = percentShares;
	}

	public List getRooms() {
		return rooms;
	}

	public void setRooms(List rooms) {
		this.rooms = rooms;
	}

	public List getTimes() {
		return times;
	}

	public void setTimes(List times) {
		this.times = times;
	}

	public Long getInstrOfferingId() {
		return instrOfferingId;
	}

	public void setInstrOfferingId(Long instrOfferingId) {
		this.instrOfferingId = instrOfferingId;
	}

	public String getInstrOfferingName() {
		return instrOfferingName;
	}

	public void setInstrOfferingName(String instrOfferingName) {
		this.instrOfferingName = instrOfferingName;
	}

	public Long getInstrOffrConfigId() {
		return instrOffrConfigId;
	}

	public void setInstrOffrConfigId(Long instrOffrConfigId) {
		this.instrOffrConfigId = instrOffrConfigId;
	}

	public Integer getInstrOffrConfigLimit() {
		return instrOffrConfigLimit;
	}

	public void setInstrOffrConfigLimit(Integer instrOffrConfigLimit) {
		this.instrOffrConfigLimit = instrOffrConfigLimit;
	}

	public List getReadOnlyClasses() {
		return readOnlyClasses;
	}

	public void setReadOnlyClasses(List readOnlyClasses) {
		this.readOnlyClasses = readOnlyClasses;
	}

	public List getClassHasErrors() {
		return classHasErrors;
	}

	public void setClassHasErrors(List classHasErrors) {
		this.classHasErrors = classHasErrors;
	}

	public Integer getSubjectAreaId() {
		return subjectAreaId;
	}

	public void setSubjectAreaId(Integer subjectAreaId) {
		this.subjectAreaId = subjectAreaId;
	}

	public ClassAssignmentProxy getProxy() {
		return proxy;
	}

	public void setProxy(ClassAssignmentProxy proxy) {
		this.proxy = proxy;
	}

	public String getNextId() {
		return nextId;
	}

	public void setNextId(String nextId) {
		this.nextId = nextId;
	}

	public String getPreviousId() {
		return previousId;
	}

	public void setPreviousId(String previousId) {
		this.previousId = previousId;
	}

	public String getDeletedInstrRowNum() {
		return deletedInstrRowNum;
	}

	public void setDeletedInstrRowNum(String deletedInstrRowNum) {
		this.deletedInstrRowNum = deletedInstrRowNum;
	}

	public List getShowDisplay() {
		return showDisplay;
	}

	public void setShowDisplay(List showDisplay) {
		this.showDisplay = showDisplay;
	}

	public String getAddInstructorId() {
		return addInstructorId;
	}

	public void setAddInstructorId(String addInstructorId) {
		this.addInstructorId = addInstructorId;
	}

	public List getExternalIds() {
		return externalIds;
	}

	public void setExternalIds(List externalIds) {
		this.externalIds = externalIds;
	}

	public Boolean getDisplayExternalId() {
		return displayExternalId;
	}

	public void setDisplayExternalId(Boolean displayExternalId) {
		this.displayExternalId = displayExternalId;
	}
}
