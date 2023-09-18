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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.InstructionalMethod;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.dao.SchedulingSubpartDAO;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.util.IdValue;


/**
 * @author Stephanie Schluttenhofer, Tomas Muller, Zuzana Mullerova
 */
public class InstructionalOfferingModifyForm implements UniTimeForm {
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);	
	private static final long serialVersionUID = 5412595518174343486L;
	
    // --------------------------------------------------------- Instance Variables
	private String op;
    private Integer subjectAreaId;
	private Long instrOfferingId;
    private String instrOfferingName;
    private boolean instrOffrConfigUnlimited;
	private Integer instrOffrConfigLimit;
	private boolean instrOffrConfigUnlimitedReadOnly;
	private Long instrOffrConfigId;
	private String origSubparts;
	private Boolean displayMaxLimit;
	private Boolean displayOptionForMaxLimit;
	private Boolean displayEnrollment;
	private Boolean displaySnapshotLimit;
	private String enableAllClassesForStudentScheduling;
	private String displayAllClassesInstructors;
	private Boolean displayExternalId;
	private Boolean editExternalId;
	private Boolean displayDisplayInstructors;
	private Boolean displayEnabledForStudentScheduling;
	private Long instructionalMethod;
	private String instructionalMethodDefault;
	private boolean instructionalMethodEditable;
	private Boolean editSnapshotLimits;
	private Boolean displayLms;
	
	private List<String> classIds;
	private List<String> subpartIds;
	private List<String> itypes;
	private List<Boolean> mustHaveChildClasses;
	private List<String> parentClassIds;
	private List<String> readOnlyClasses;
	private List<String> readOnlyDatePatterns;
	private List<String> classLabels;
	private List<String> classLabelIndents;
	private List<String> enrollments;
	private List<String> snapshotLimits;
	private List<String> minClassLimits;
	private List<String> maxClassLimits;
	private List<String> roomRatios;
	private List<String> numberOfRooms;
	private List<String> departments;
	private List<String> datePatterns;
	private List<String> displayInstructors;
	private List<String> enabledForStudentScheduling;
	private List<Boolean> classCanMoveUp;
	private List<Boolean> classCanMoveDown;
	private List<Integer> subtotalIndexes;
	private List<String> subtotalLabels;
	private List<Integer> subtotalValues;
	private List<Integer> subtotalSnapValues;
	private List<Boolean> enableAllClassesForStudentSchedulingForSubpart;
	private List<Boolean> displayAllClassesInstructorsForSubpart;
	private List<Boolean> readOnlySubparts;
	private List<String> times;
	private List<String> rooms;
	private List<String> instructors;	
	private List<String> externalIds;
	private List<Boolean> canDelete;
	private List<Boolean> canCancel;
	private List<Boolean> isCancelled;
	private List<String> lms;
	
	private List<Boolean> classHasErrors;
	
	private static String CLASS_IDS_TOKEN = "classIds";
	private static String SUBPART_IDS_TOKEN = "subpartIds";
	private static String PARENT_CLASS_IDS_TOKEN = "parentClassIds";
	private static String ITYPES_TOKEN = "itypes";
	private static String MUST_HAVE_CHILD_CLASSES_TOKEN = "mustHaveChildClasses";
	private static String READ_ONLY_CLASSES_TOKEN = "readOnlyClasses";
	private static String READ_ONLY_DATE_PATTERNS_TOKEN = "readOnlyDatePatterns";
	private static String CLASS_LABELS_TOKEN = "classLabels";
	private static String CLASS_LABEL_INDENTS_TOKEN = "classLabelIndents";
	private static String ENROLLMENTS_TOKEN = "enrollments";
	private static String SNAPSHOT_LIMITS_TOKEN = "snapshotLimits";
	private static String MIN_CLASS_LIMITS_TOKEN = "minClassLimits";
	private static String MAX_CLASS_LIMITS_TOKEN = "maxClassLimits";
	private static String ROOM_RATIOS_TOKEN = "roomRatios";
	private static String NUMBER_OF_ROOMS_TOKEN = "numberOfRooms";
	private static String DEPARTMENTS_TOKEN = "departments";
	private static String DATE_PATTERNS_TOKEN = "datePatterns";
	private static String DISPLAY_INSTRUCTORS_TOKEN = "displayInstructors";
	private static String ENABLED_FOR_STUDENT_SCHEDULING_TOKEN = "enabledForStudentScheduling";
	private static String DIRECTION_UP = "up";
	private static String DIRECTION_DOWN = "down";
	private static String SUBTOTAL_INDEXES_TOKEN = "subtotalIndexes";
	private static String TIMES_TOKEN = "times";
	private static String ROOMS_TOKEN = "rooms";
	private static String INSTRUCTORS_TOKEN = "instructors";
	private static String EXTERNAL_IDS_TOKEN = "externalIds";
	private static String CAN_DELETE_TOKEN = "canDelete";
	private static String CAN_CANCEL_TOKEN = "canCancel";
	private static String IS_CANCELLED_TOKEN = "isCancelled";
	private static String DISPLAY_LMS_TOKEN = "displayLms";
	private static String LMS_TOKEN = "lms";
	
    public InstructionalOfferingModifyForm() {
    	reset();
    }
		
    public void validate(UniTimeAction action) {
        if (MSG.actionUpdateMultipleClassSetup().equals(op)) {
	        // Check Instructional Offering Config
	        if (this.instrOffrConfigId==null || this.instrOffrConfigId.intValue()<=0) {
	            action.addFieldError("form.instrOffrConfigId", MSG.errorRequiredIOConfiguration());            
	        }
	        // Validate class limits provide space that is >= limit for the instructional offering config
	        validateChildClassExistence(action);
	        validateClassLimits(action);
	        validateAllSubpartsHaveAtLeastOneClass(action);
        }
    }
    
    private void validateChildClassExistence(UniTimeAction action){
    	for(int index = 0 ; index < this.getClassIds().size(); index++){
    		if (this.getMustHaveChildClasses().get(index)){
    			String classId = (String) this.getClassIds().get(index);
    			if ((index + 1) == this.getClassIds().size()){
        			action.addFieldError("mustHaveChildClasses", 
        					MSG.errorClassMustHaveChildClasses((String) this.getClassLabels().get(index)));
        			this.getClassHasErrors().set(index, Boolean.valueOf(true));    				
    			} else {
	    			String parentOfNextClass = (String) this.getParentClassIds().get(index + 1);
	    			if (parentOfNextClass == null || !parentOfNextClass.equals(classId)){
	        			action.addFieldError("mustHaveChildClasses", 
	        					MSG.errorClassMustHaveChildClasses((String) this.getClassLabels().get(index)));
	        			this.getClassHasErrors().set(index, Boolean.valueOf(true));    				    				
	    			}
    			}
    		}
    	}
    }
    
    private void validateAllSubpartsHaveAtLeastOneClass(UniTimeAction action){
    	
    	String[] subparts = this.getOrigSubparts().split(",");
    	for(int i = 0; i < subparts.length; i++){
    		if (!this.getSubpartIds().contains(subparts[i])){
    			action.addFieldError("allSubpartsMustHaveAClass", MSG.errorEachSubpartMustHaveClass());
    			break;
    		}
    	}
    }
    
    private void validateMinLessThanMaxClassLimits(UniTimeAction action){
    	Iterator it1 = this.getMinClassLimits().iterator();
    	Iterator it2 = this.getMaxClassLimits().iterator();
    	int index = 0;
    	String minLimitStr = null;
    	String maxLimitStr = null;
    	int minLimit;
    	int maxLimit;
    	for(;it1.hasNext();){
    		minLimitStr = (String) it1.next();
    		maxLimitStr = (String) it2.next();
    		if (minLimitStr != null && minLimitStr.length() > 0){
    			minLimit = Integer.parseInt(minLimitStr);
    		} else {
    			minLimit = 0;
    		}
    		if (maxLimitStr != null && maxLimitStr.length() > 0){
    			maxLimit = Integer.parseInt(maxLimitStr);
    		} else {
    			maxLimit = 0;
    		}
    		if (minLimit > maxLimit){
    			action.addFieldError("minLimitGreaterThanMaxLimit", MSG.errorMaxLessThanMinLimit((String) this.getClassLabels().get(index)));
    			this.getClassHasErrors().set(index, Boolean.valueOf(true));
    		}    		
    		index++;
    	}
    }
    
    private void validateMinOrMaxParentClassLimits(UniTimeAction action, List<String> limits, String errorName, String errorMessage){
		HashMap childClassLimits = new HashMap();
		Iterator it1 = this.getSubpartIds().iterator();
		Iterator it2 = limits.iterator();
		Iterator it3 = this.getParentClassIds().iterator();
		String subpartKey = null;
		String parentKey = null;
		int value2;
		int clsLimit;
		String tmp = null;
		HashMap subpartLimits = null;
		
		for(; it1.hasNext();){
			subpartKey = (String) it1.next();
			tmp = (String) it2.next();
			if (tmp != null && tmp.length() != 0)
				clsLimit = Integer.parseInt(tmp);
			else
				clsLimit = 0;
 			parentKey = (String) it3.next();
						
			// calculate total limit for child classes
			if (parentKey != null && parentKey.length() > 0){
				if (childClassLimits.get(parentKey) == null){
					subpartLimits = new HashMap();
					childClassLimits.put(parentKey, subpartLimits);
					value2 = 0;
				}
				else {
					subpartLimits = (HashMap) childClassLimits.get(parentKey);
					if (subpartLimits.get(subpartKey) == null){
						value2 = 0; 						
					} else {
    					value2 = ((Integer) subpartLimits.get(subpartKey)).intValue();
					}
				}
				value2 += clsLimit;
				subpartLimits.put(subpartKey, Integer.valueOf(value2));
			}
		}
				
		HashMap childClassesUnderLimit = new HashMap();
		String parentKey1 = null;
		Integer childLimit = null;
		Integer parentLimit = null;
		Iterator it4 = this.getClassIds().iterator();
		Iterator it5 = limits.iterator();
		String subpartKey2 = null;
 		
		// validate that child class limits are >= parent limit
		for (;it4.hasNext();){
			parentKey1 = (String) it4.next();
			parentLimit = Integer.valueOf((String) it5.next());
			if (childClassLimits.get(parentKey1) != null){
				subpartLimits = (HashMap) childClassLimits.get(parentKey1);
				for(Iterator it = subpartLimits.keySet().iterator(); it.hasNext();){
					subpartKey2 = (String) it.next();
    				childLimit = (Integer) subpartLimits.get(subpartKey2);
    				if (parentLimit != null && childLimit.intValue() < parentLimit.intValue()){
    					childClassesUnderLimit.put(parentKey1,subpartKey2);
    				}
				}
			}
		}
		
		// mark classes that are in error and build error messages
		if (childClassesUnderLimit.size() > 0){
			action.addFieldError(errorName, errorMessage);  			
		}
		if ((childClassesUnderLimit.size() > 0)){
			Iterator it6 = this.getParentClassIds().iterator();
			Iterator it7 = this.getSubpartIds().iterator();
			String subpartId = null;
			String parentId = null;
			int index = 0;
			for (;it6.hasNext();){
				parentId = (String) it6.next();
				subpartId = (String) it7.next();
				if (!((Boolean)this.getClassHasErrors().get(index)).booleanValue()){
					if (childClassesUnderLimit.keySet().contains(parentId) && subpartId.equals((String)childClassesUnderLimit.get(parentId))){
						this.getClassHasErrors().set(index, Boolean.valueOf(true)); 
					} 
				}
				index++;
			}
		}	
    }
    
    private void validateSubpartClassLimits(UniTimeAction action){
    	int limit = getInstrOffrConfigLimit().intValue();
    	HashMap subpartClassLimits = new HashMap();
		Iterator it1 = this.getSubpartIds().iterator();
		Iterator it2 = this.getMaxClassLimits().iterator();
		Iterator it3 = this.getParentClassIds().iterator();
		String subpartKey = null;
		String parentKey = null;
		int value1;
		int clsLimit;
		String tmp = null;
		
		for(; it1.hasNext();){
			subpartKey = (String) it1.next();
			tmp = (String) it2.next();
			if (tmp != null && tmp.length() != 0)
				clsLimit = Integer.parseInt(tmp);
			else
				clsLimit = 0;
 			parentKey = (String) it3.next();
			
			// calculate total limit for subparts
			if ( parentKey == null || parentKey.length() == 0){
    			if (subpartClassLimits.get(subpartKey) == null)
    				value1 = 0;
    			else
    				value1 = ((Integer) subpartClassLimits.get(subpartKey)).intValue();
    			
    			value1 += clsLimit;
    			subpartClassLimits.put(subpartKey, Integer.valueOf(value1));
			}
			
		}
		
		HashSet subpartsUnderLimit = new HashSet();
		String subpartKey1 = null;
		Integer subpartLimit = null;
		// validate that subpart limits are >= config limit
		for(Iterator it4 = subpartClassLimits.keySet().iterator();it4.hasNext();){
			subpartKey1 = it4.next().toString();
			subpartLimit = (Integer) subpartClassLimits.get(subpartKey1);
			if (subpartLimit != null && subpartLimit.intValue() < limit){
				subpartsUnderLimit.add(subpartKey1);
			}
		}
		
		
		// mark classes that are in error and build error messages
		if (subpartsUnderLimit.size() > 0){
			if (getDisplayMaxLimit().booleanValue()){
				action.addFieldError("maxLimit", MSG.errorMaxLimitsTotalTooLow());
			} else {
				action.addFieldError("maxLimit", MSG.errorLimitsForTopLevelClassesTooLow());
			}
		}
		if ((subpartsUnderLimit.size() > 0)){
			Iterator it7 = this.getSubpartIds().iterator();
			String subpartId = null;
			int index = 0;
			for (;it7.hasNext();){
				subpartId = (String) it7.next();
				if (!((Boolean)this.getClassHasErrors().get(index)).booleanValue()){
					if (subpartsUnderLimit.contains(subpartId)){
						this.getClassHasErrors().set(index, Boolean.valueOf(true));	                           					
					}
				}
				index++;
			}
		}
    }
    
    private void initClassHasErrorsToFalse(){
		this.setClassHasErrors(new ArrayList<Boolean>());
		for(Iterator it = this.getClassIds().iterator(); it.hasNext();){
			this.getClassHasErrors().add(Boolean.valueOf(false));
			it.next();
		}
    }
    
    private void initClassMoveDirections(){
    	this.setClassCanMoveDown(new ArrayList<Boolean>());
		this.setClassCanMoveUp(new ArrayList<Boolean>());
		for(Iterator it = this.getClassIds().iterator(); it.hasNext();){
			this.getClassCanMoveDown().add(Boolean.valueOf(false));
			this.getClassCanMoveUp().add(Boolean.valueOf(false));
			it.next();
		}
    }
    
    private void validateClassLimits(UniTimeAction action){
    	boolean unlimited = getInstrOffrConfigUnlimited();
    	int limit = getInstrOffrConfigLimit().intValue();
    	if (!unlimited && limit > 0) {
    		initClassHasErrorsToFalse();
    		validateMinLessThanMaxClassLimits(action);
    		validateMinOrMaxParentClassLimits(action, this.getMaxClassLimits(), "maxLimit", ((getDisplayMaxLimit().booleanValue())? MSG.errorTotalMaxChildrenAtLeastMaxParent():MSG.errorLimitsChildClasses()));
    		if (ApplicationProperty.ConfigEditCheckLimits.isTrue())
    			validateSubpartClassLimits(action);
    	}
    }
 
    public void reset() {
    	instrOfferingId = null;
    	instrOffrConfigLimit = null;
    	instrOffrConfigUnlimited = false;
    	instrOffrConfigUnlimitedReadOnly = false;
    	instrOffrConfigId = null;
    	instrOfferingName = "";
    	origSubparts = "";
    	displayMaxLimit = Boolean.valueOf(false);
    	displayOptionForMaxLimit = Boolean.valueOf(false);
    	displayEnrollment = Boolean.valueOf(false);
    	displaySnapshotLimit = Boolean.valueOf(false);
    	displayDisplayInstructors = Boolean.valueOf(false);
    	displayEnabledForStudentScheduling = Boolean.valueOf(false);
    	displayExternalId = Boolean.valueOf(false);
    	editExternalId = Boolean.valueOf(false);
    	editSnapshotLimits = Boolean.valueOf(false);
    	enableAllClassesForStudentScheduling = "";
    	displayAllClassesInstructors = "";
    	instructionalMethod = null;
    	instructionalMethodDefault = null;
    	instructionalMethodEditable = false;
    	displayLms = Boolean.valueOf(false);
    	resetLists();
    }
    
    private void resetLists(){
    	classIds = new ArrayList<String>();
    	subpartIds = new ArrayList<String>();
    	itypes = new ArrayList<String>();
    	mustHaveChildClasses = new ArrayList<Boolean>();
    	parentClassIds = new ArrayList<String>();
    	readOnlyClasses = new ArrayList<String>();
    	readOnlyDatePatterns = new ArrayList<String>();
       	classHasErrors = new ArrayList<Boolean>();
       	classLabels = new ArrayList<String>();
       	classLabelIndents = new ArrayList<String>();
       	enrollments = new ArrayList<String>();
       	snapshotLimits = new ArrayList<String>();
       	minClassLimits = new ArrayList<String>();
    	maxClassLimits = new ArrayList<String>();
    	roomRatios = new ArrayList<String>();
    	numberOfRooms = new ArrayList<String>();
      	departments = new ArrayList<String>();
    	datePatterns = new ArrayList<String>();
      	displayInstructors = new ArrayList<String>();
    	enabledForStudentScheduling = new ArrayList<String>();
       	subtotalIndexes = new ArrayList<Integer>();
       	subtotalLabels = new ArrayList<String>();
       	subtotalValues = new ArrayList<Integer>();
       	subtotalSnapValues = new ArrayList<Integer>();
       	enableAllClassesForStudentSchedulingForSubpart = new ArrayList<Boolean>();
       	displayAllClassesInstructorsForSubpart = new ArrayList<Boolean>();
    	classCanMoveUp = new ArrayList<Boolean>();
    	classCanMoveDown = new ArrayList<Boolean>();
    	readOnlySubparts = new ArrayList<Boolean>();
    	times = new ArrayList<String>();
    	rooms = new ArrayList<String>();
    	instructors = new ArrayList<String>();
    	externalIds = new ArrayList<String>();
    	canDelete = new ArrayList<Boolean>();
    	canCancel = new ArrayList<Boolean>();
    	isCancelled = new ArrayList<Boolean>();
    	lms = new ArrayList<String>();
}
    
//    private int numberOfClassesOfSubpartWithParentClassId(String parentClassId, String classSubpartId){
//    	if (parentClassId == null || parentClassId.length() == 0 ||
//    		classSubpartId == null || classSubpartId.length() == 0) {
//    		return(0);
//    	}
//    	int count = 0;
//    	Iterator it1 = this.getParentClassIds().iterator();
//    	Iterator it2 = this.getSubpartIds().iterator();
//    	String parentId = null;
//    	String subpartId = null;
//    	
//    	while(it1.hasNext()){
//    		parentId = (String) it1.next();
//    		subpartId = (String) it2.next();
//    		if (parentId == null || parentId.length() == 0){
//    			continue;
//    		}
//    		if (subpartId == null || subpartId.length() == 0){
//    			continue;
//    		}
//    		if (parentClassId.equals(parentId) && classSubpartId.equals(subpartId)){
//    			count++;
//    		}
//    	}
//    	return(count);
//    }
    
    private void setClassCannotMove(int classIndex){
    	this.getClassCanMoveDown().set(classIndex,Boolean.valueOf(false));    
		this.getClassCanMoveUp().set(classIndex,Boolean.valueOf(false)); 
    }
    
    public void setDirectionsClassesCanMove(){
    	String parentClassId = null;
    	String classItype = null;
    	int parentClassIndex;
    	String parentSubpartId = null;
    	String parentClassItype = null;
    	int lowestParentSubpartIdIndex;
    	int highestParentSubpartIdIndex;
    	initClassMoveDirections();
    	for(int classIndex = 0; classIndex < this.getClassIds().size(); classIndex++){
	    	parentClassId = (String) this.getParentClassIds().get(classIndex);
	    	if (parentClassId == null || parentClassId.length() == 0){
	    		setClassCannotMove(classIndex);
	    		continue;
	    	}
	    	
	    	parentClassIndex = this.getClassIds().indexOf(parentClassId);
	    	if (parentClassIndex < 0){
	    		setClassCannotMove(classIndex);
	    		continue;
	    	}
	    	
	    	classItype = (String) this.getItypes().get(classIndex);
	    	parentClassItype = (String) this.getItypes().get(parentClassIndex);
	    	if (classItype != null && parentClassItype != null && classItype.equals(parentClassItype)){
	    		setClassCannotMove(classIndex);
	    		continue;
	    	}
	    	
	    	parentSubpartId = (String) this.getSubpartIds().get(parentClassIndex);
	    	if (parentSubpartId == null || parentSubpartId.length() == 0){
	    		setClassCannotMove(classIndex);
	    		continue;
	    	}
	    	
	    	lowestParentSubpartIdIndex = this.getSubpartIds().indexOf(parentSubpartId);
	    	if (lowestParentSubpartIdIndex < parentClassIndex){
	    		this.getClassCanMoveUp().set(classIndex,Boolean.valueOf(true));    
	    	}
	    	
	    	highestParentSubpartIdIndex = this.getSubpartIds().lastIndexOf(parentSubpartId);
	    	if (highestParentSubpartIdIndex > parentClassIndex){
	    		this.getClassCanMoveDown().set(classIndex, Boolean.valueOf(true));
	    	}
    	}
   }
    
    public void initializeOrigSubparts(){
    	TreeSet ts = new TreeSet();
    	for(Iterator it = this.getSubpartIds().iterator(); it.hasNext();){
    		ts.add((String) it.next());
    	}
    	StringBuffer sb = new StringBuffer();
    	boolean first = true;
    	for(Iterator it = ts.iterator(); it.hasNext();){
    		if (!first){
    			sb.append(",");
    		} else {
    			first = false;
    		}
    		sb.append((String) it.next());
    	}
    	this.setOrigSubparts(sb.toString());
    }
    
    public void initializeEnableAllClassesForStudentScheduling(){
    	if (this.getClassLabels().size() > this.getEnabledForStudentScheduling().size()){
    		setEnableAllClassesForStudentScheduling("false");
    		return;
    	}
    	String display = "true";
    	for (Iterator it = this.getEnabledForStudentScheduling().iterator(); it.hasNext();){
    		String value = (String) it.next();
	   		if (value == null || (!value.equals("on") && !(Boolean.parseBoolean(value)))){
	    			display = "false";
	    			break;
	    		}
	    	}
    	setEnableAllClassesForStudentScheduling(display);
    }

    public void initializeDisplayAllClassInstructors(){
    	if (this.getClassLabels().size() > this.getDisplayInstructors().size()){
    		setDisplayAllClassesInstructors("false");
    		return;
    	}
    	String display = "true";
    	for (Iterator it = this.getDisplayInstructors().iterator(); it.hasNext();){
    		String value = (String) it.next();
	   		if (value == null || (!value.equals("on") && !(Boolean.parseBoolean(value)))){
	    			display = "false";
	    			break;
	    		}
	    	}
    	setDisplayAllClassesInstructors(display);
    }

    public void initalizeSubpartSubtotalsAndDisplayFlags(){
		HashMap subpartToIndex = new HashMap();
    	this.setSubtotalIndexes(new ArrayList<Integer>());
		this.setSubtotalLabels(new ArrayList<String>());
		this.setSubtotalValues(new ArrayList<Integer>());
		this.setSubtotalSnapValues(new ArrayList<Integer>());
		this.setEnableAllClassesForStudentSchedulingForSubpart(new ArrayList<Boolean>());
		this.setDisplayAllClassesInstructorsForSubpart(new ArrayList<Boolean>());
		this.setReadOnlySubparts(new ArrayList<Boolean>());
		SchedulingSubpartDAO ssDao = new SchedulingSubpartDAO();
		SchedulingSubpart ss = null;
    	int i = 0;
    	int cnt = 0;
    	Iterator ssIt = this.getSubpartIds().iterator();
    	Iterator limitIt = this.getMinClassLimits().iterator();
    	Iterator cancelIt = this.getIsCancelled().iterator();
    	Iterator snapLimitIt = (this.getEditSnapshotLimits() ? this.getSnapshotLimits().iterator() : null);

    	Boolean enableForScheduling = null;
    	Boolean displayInstructor = null;
    	Boolean readOnlySubpart = null;
    	while (ssIt.hasNext() && limitIt.hasNext() && cancelIt.hasNext()){
    		Long subpartId = Long.valueOf((String) ssIt.next());
    		Integer limit = Integer.valueOf((String) limitIt.next());
    		Integer snapshotLimit = null;
    		try {
    			snapshotLimit = (snapLimitIt == null ? null : Integer.valueOf((String)snapLimitIt.next()));
    		} catch (NumberFormatException e) {}
    		boolean cancelled = "true".equals(cancelIt.next());
     		enableForScheduling = Boolean.valueOf(determineBooleanValueAtIndex(this.getEnabledForStudentScheduling(), cnt));    	   	
     		displayInstructor = Boolean.valueOf(determineBooleanValueAtIndex(this.getDisplayInstructors(), cnt));
       	   	readOnlySubpart = Boolean.valueOf(determineBooleanValueAtIndex(this.getReadOnlyClasses(), cnt));
     	   	
    		int addLimit = (limit == null || cancelled)?0:limit.intValue();
    		int snapLimit = (snapshotLimit == null || cancelled? 0 : snapshotLimit.intValue());
    		Integer subtotalIndex = null;

    		if (!subpartToIndex.containsKey(subpartId)) {
     			ss = ssDao.get(subpartId);
    			getSubtotalValues().add(addLimit);
    			getSubtotalSnapValues().add(snapLimit);
    			getEnableAllClassesForStudentSchedulingForSubpart().add(enableForScheduling);
    			getDisplayAllClassesInstructorsForSubpart().add(displayInstructor);
    			getReadOnlySubparts().add(readOnlySubpart);
    			String label = ss.getItypeDesc() + ss.getSchedulingSubpartSuffix();
    			getSubtotalLabels().add(label);
     			subpartToIndex.put(subpartId, Integer.valueOf(i));
     			subtotalIndex = Integer.valueOf(i);
   	   			i++;
    		} else {
        		subtotalIndex = (Integer) subpartToIndex.get(subpartId);
        		int oldSubtotal = ((Integer) this.getSubtotalValues().get(subtotalIndex)).intValue();
        		int newSubtotal = oldSubtotal + addLimit;
        		this.getSubtotalValues().set(subtotalIndex.intValue(), newSubtotal);
        		int oldSnapSubtotal = ((Integer) this.getSubtotalSnapValues().get(subtotalIndex)).intValue();
        		int newSnapSubtotal = oldSnapSubtotal + snapLimit;
        		this.getSubtotalSnapValues().set(subtotalIndex.intValue(), newSnapSubtotal);
        		boolean oldEnableForScheduling = ((Boolean) this.getEnableAllClassesForStudentSchedulingForSubpart().get(subtotalIndex)).booleanValue();
        		boolean newEnableForScheduling = oldEnableForScheduling && enableForScheduling.booleanValue();
        		this.getEnableAllClassesForStudentSchedulingForSubpart().set(subtotalIndex, Boolean.valueOf(newEnableForScheduling));
        		boolean oldDisplayInstructor = ((Boolean) this.getDisplayAllClassesInstructorsForSubpart().get(subtotalIndex)).booleanValue();
        		boolean newDisplayInstructor = oldDisplayInstructor && displayInstructor.booleanValue();
        		this.getDisplayAllClassesInstructorsForSubpart().set(subtotalIndex, newDisplayInstructor);
        		boolean oldReadOnlySubpart = ((Boolean) this.getReadOnlySubparts().get(subtotalIndex)).booleanValue();
        		boolean newReadOnlySubpart = oldReadOnlySubpart && readOnlySubpart.booleanValue();
        		this.getReadOnlySubparts().set(subtotalIndex, newReadOnlySubpart);
    		}
    		
    		getSubtotalIndexes().add(subtotalIndex);
    		cnt++;
    	}

    }

    private boolean determineBooleanValueAtIndex(List<?> l, int index){
    	if (l == null){
    		return(false);
    	}
    	if (l.size() == 0){
    		return(false);
    	}
    	if (l.size() < (index + 1)){
    		return(false);
    	}
    	if (l.get(index) == null){
    		return(false);
    	}
    	if (l.get(index) instanceof Boolean) {
			Boolean value = (Boolean) l.get(index);
			return(value.booleanValue());
		}
    	if (l.get(index) instanceof String) {
			String str_value = (String) l.get(index);
			if (str_value.equals("on")){
				return(true);
			} else if (str_value.equals("yes")){
				return(true);
			} else {
				return(Boolean.parseBoolean(str_value));
			}
		}
    	return(false);
    }
	public List<String> getClassIds() {
		return classIds;
	}
	public void setClassIds(List<String> classIds) {
		this.classIds = classIds;
	}
	public List<Boolean> getClassHasErrors() {
		return classHasErrors;
	}
	public void setClassHasErrors(List<Boolean> classHasErrors) {
		this.classHasErrors = classHasErrors;
	}
	public List<String> getClassLabels() {
		return classLabels;
	}
	public void setClassLabels(List<String> classLabels) {
		this.classLabels = classLabels;
	}
	public List<String> getMinClassLimits() {
		return minClassLimits;
	}
	public void setMinClassLimits(List<String> classLimits) {
		this.minClassLimits = classLimits;
	}
	public List<String> getDatePatterns() {
		return datePatterns;
	}
	public void setDatePatterns(List<String> datePatterns) {
		this.datePatterns = datePatterns;
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
	public boolean getInstrOffrConfigUnlimited() {
		return instrOffrConfigUnlimited;
	}
	public void setInstrOffrConfigUnlimited(boolean instrOffrConfigUnlimited) {
		this.instrOffrConfigUnlimited = instrOffrConfigUnlimited;
	}
	public boolean getInstrOffrConfigUnlimitedReadOnly() {
		return instrOffrConfigUnlimitedReadOnly;
	}
	public void setInstrOffrConfigUnlimitedReadOnly(boolean instrOffrConfigUnlimitedReadOnly) {
		this.instrOffrConfigUnlimitedReadOnly = instrOffrConfigUnlimitedReadOnly;
	}
	public List<String> getDepartments() {
		return departments;
	}
	public void setDepartments(List<String> managers) {
		this.departments = managers;
	}
	public List<String> getNumberOfRooms() {
		return numberOfRooms;
	}
	public void setNumberOfRooms(List<String> numberOfRooms) {
		this.numberOfRooms = numberOfRooms;
	}
	public String getOp() {
		return op;
	}
	public void setOp(String op) {
		this.op = op;
	}
	public List<String> getParentClassIds() {
		return parentClassIds;
	}
	public void setParentClassIds(List<String> parentClassIds) {
		this.parentClassIds = parentClassIds;
	}
	public List<String> getMaxClassLimits() {
		return maxClassLimits;
	}
	public void setMaxClassLimits(List<String> roomLimits) {
		this.maxClassLimits = roomLimits;
	}
	public List<String> getSubpartIds() {
		return subpartIds;
	}
	public void setSubpartIds(List<String> subpartIds) {
		this.subpartIds = subpartIds;
	}
	public List<String> getReadOnlyClasses() {
		return readOnlyClasses;
	}
	public void setReadOnlyClasses(List<String> readOnlyClasses) {
		this.readOnlyClasses = readOnlyClasses;
	}
	public List<String> getReadOnlyDatePatterns() {
		return readOnlyDatePatterns;
	}
	public void setReadOnlyDatePatterns(List<String> readOnlyDatePatterns) {
		this.readOnlyDatePatterns = readOnlyDatePatterns;
	}
	public Long getInstrOfferingId() {
		return instrOfferingId;
	}
	public void setInstrOfferingId(Long instrOfferingId) {
		this.instrOfferingId = instrOfferingId;
	}

	public void removeFromClasses(String classId){
		removeClass(classId);
		initClassHasErrorsToFalse();
		setDirectionsClassesCanMove();
	}
	private void removeClass(String classId){
		ArrayList classesToDel = new ArrayList<String>();
		Iterator it1 = this.classIds.listIterator();
		Iterator it2 = this.subpartIds.listIterator();
		Iterator it3 = this.parentClassIds.listIterator();
		Iterator it4 = this.readOnlyClasses.listIterator();
		Iterator it5 = this.minClassLimits.listIterator();
		Iterator it6 = this.departments.listIterator();
		Iterator it7 = this.datePatterns.listIterator();
		Iterator it8 = this.numberOfRooms.listIterator();
		Iterator it9 = this.maxClassLimits.listIterator();
		Iterator it10 = this.roomRatios.listIterator();
		Iterator it11 = this.classLabels.listIterator();
		Iterator it12 = this.classLabelIndents.listIterator();
		Iterator it13 = this.displayInstructors.listIterator();
		Iterator it14 = this.enabledForStudentScheduling.listIterator();
		Iterator it15 = this.itypes.listIterator();
		Iterator it16 = this.mustHaveChildClasses.listIterator();
		Iterator it17 = this.enrollments.listIterator();
		Iterator it18 = this.times.listIterator();
		Iterator it19 = this.rooms.listIterator();
		Iterator it20 = this.instructors.listIterator();
		Iterator it21 = this.externalIds.listIterator();
		Iterator it22 = this.canDelete.listIterator();
		Iterator it23 = this.canCancel.listIterator();
		Iterator it24 = this.isCancelled.listIterator();
		Iterator it25 = this.snapshotLimits.listIterator();
		Iterator it26 = this.readOnlyDatePatterns.listIterator();
		boolean canRemoveFromDisplayInstructors;
		boolean canRemoveFromEnableForStudentScheduling;
		boolean canRemoveFromEnrollment;
		
		for (; it1.hasNext(); ){
			String cls1 = it1.next().toString();
			it2.next();
			String pCls1 = it3.next().toString();
			it4.next();
			it5.next();
			it6.next();
			it7.next();
			it8.next();
			it9.next();
			it10.next();
			it11.next();
			it12.next();
			if (it13.hasNext()){
				it13.next();
				canRemoveFromDisplayInstructors = true;
			} else {
				canRemoveFromDisplayInstructors = false;
			}
			if (it14.hasNext()){
				it14.next();
				canRemoveFromEnableForStudentScheduling = true;
			} else {
				canRemoveFromEnableForStudentScheduling = false;
			}
			it15.next();
			it16.next();
			if (it17.hasNext()){
				it17.next();
				canRemoveFromEnrollment = true;
			} else {
				canRemoveFromEnrollment = false;
			}
			it18.next();
			it19.next();
			it20.next();
			it21.next();
			it22.next();
			it23.next();
			it24.next();
			it25.next();
			it26.next();
			if (cls1.equals(classId)){				
				it1.remove();
				it2.remove();
				it3.remove();
				it4.remove();
				it5.remove();
				it6.remove();
				it7.remove();
				it8.remove();
				it9.remove();
				it10.remove();
				it11.remove();
				it12.remove();
				if (canRemoveFromDisplayInstructors)
					it13.remove();
				if (canRemoveFromEnableForStudentScheduling)
					it14.remove();
				it15.remove();
				it16.remove();
				if (canRemoveFromEnrollment){
					it17.remove();
				}
				it18.remove();
				it19.remove();
				it20.remove();
				it21.remove();
				it22.remove();
				it23.remove();
				it24.remove();
				it25.remove();
				it26.remove();
			} else if (pCls1.equals(classId)){
				classesToDel.add(cls1);
			}
		}
		if (classesToDel.size() > 0){
			for(Iterator delIt = classesToDel.iterator(); delIt.hasNext();){
				removeFromClasses(delIt.next().toString());
			}
		}
	}
	    
	public void addToClasses(Class_ cls, Boolean isReadOnly, String indent, ClassAssignmentProxy proxy, 
			String nameFormat, boolean canDelete, boolean canCancel, Boolean isReadOnlyDatePattern){
		this.classLabels.add(cls.htmlLabel());
		this.classLabelIndents.add(indent);
		this.classIds.add(cls.getUniqueId().toString());
		this.subpartIds.add(cls.getSchedulingSubpart().getUniqueId().toString());
		this.itypes.add(cls.getSchedulingSubpart().getItype().getItype().toString());
		if (cls.getSchedulingSubpart().getChildSubparts() != null && cls.getSchedulingSubpart().getChildSubparts().size() > 0){
			this.mustHaveChildClasses.add(Boolean.valueOf(true));
		} else {
			this.mustHaveChildClasses.add(Boolean.valueOf(false));			
		}
		this.readOnlyClasses.add(isReadOnly.toString());
		this.readOnlyDatePatterns.add(isReadOnlyDatePattern.toString());
		this.classHasErrors.add(Boolean.valueOf(false));	
		this.enrollments.add(Boolean.TRUE.equals(displayEnrollment)?(cls.getEnrollment()==null?"0":cls.getEnrollment().toString()):"");
		if(getInstrOffrConfigUnlimited()) {
			this.snapshotLimits.add("0");
		} else {
			this.snapshotLimits.add(cls.getSnapshotLimit() == null ? "" : cls.getSnapshotLimit().toString());	
		}
		if (getInstrOffrConfigUnlimited())
			this.minClassLimits.add("0");
		else
			this.minClassLimits.add(cls.getExpectedCapacity().toString());
		if (getInstrOffrConfigUnlimited())
			this.numberOfRooms.add("1");
		else
			this.numberOfRooms.add(cls.getNbrRooms().toString());
		this.displayInstructors.add(cls.isDisplayInstructor().toString());
		this.enabledForStudentScheduling.add(cls.isEnabledForStudentScheduling().toString());

		if (getInstrOffrConfigUnlimited()) 
			this.maxClassLimits.add("0");
		else if(cls.getMaxExpectedCapacity() != null)
			this.maxClassLimits.add(cls.getMaxExpectedCapacity().toString());
		else
			this.maxClassLimits.add("");
		if (getInstrOffrConfigUnlimited())
			this.roomRatios.add("1.0");
		else if(cls.getRoomRatio() != null)
			this.roomRatios.add(cls.getRoomRatio().toString());
		else
			this.roomRatios.add("");
		if (cls.getParentClass() != null)
			this.parentClassIds.add(cls.getParentClass().getUniqueId().toString());
		else
			this.parentClassIds.add("");
		if (cls.getManagingDept() != null)
			if (cls.getManagingDept().getUniqueId().equals(cls.getControllingDept().getUniqueId())){
				this.departments.add("-1");
			} else {
				this.departments.add(cls.getManagingDept().getUniqueId().toString());
			}
		else
			this.departments.add("-1");
		if (cls.getDatePattern() != null)
			this.datePatterns.add(cls.getDatePattern().getUniqueId().toString());
		else
			this.datePatterns.add("");
		if ((cls.getExpectedCapacity() != null && cls.getMaxExpectedCapacity() != null && cls.getExpectedCapacity().equals(cls.getMaxExpectedCapacity()))
				|| (cls.getExpectedCapacity() == null && cls.getMaxExpectedCapacity() == null) ){
			// leave display max limit alone.
		} else {
			setDisplayMaxLimit(Boolean.valueOf(true));
		}
		if (getDisplayMaxLimit().booleanValue()){
			setDisplayOptionForMaxLimit(Boolean.valueOf(true));
		}
		this.times.add(cls.buildAssignedTimeHtml(proxy));
		this.rooms.add(cls.buildAssignedRoomHtml(proxy));
		this.instructors.add(cls.buildInstructorHtml(nameFormat));
		this.externalIds.add(cls.getClassSuffix() == null?"":cls.getClassSuffix());
		this.canDelete.add(canDelete);
		this.canCancel.add(canCancel);
		this.isCancelled.add(cls.isCancelled());
		if (cls.getLms() != null) {
			this.lms.add(cls.getLms().getUniqueId().toString());
		} else {
			this.lms.add("");
		}
	}
	
	private int indexOfLastChildClass(String classId){
		
		int clsIndex = this.getClassIds().indexOf(classId);
		int index = clsIndex + 1;
		while (index < (this.getClassIds().size())){
			if (this.getClassLabelIndents().get(index).toString().equals(this.getClassLabelIndents().get(clsIndex).toString())){
					break;
			} else {
				if (this.getClassLabelIndents().get(index).toString().length() < this.getClassLabelIndents().get(clsIndex).toString().length()){
					break;
				}
			}
			index++;
		}
		index--;
		return index;
	}
	
	public void shiftClass(String clsId){
		
	}
	
	private HashMap buildOriginalListValuesMap(){
		HashMap hm = new HashMap();
		hm.put(CLASS_IDS_TOKEN, this.getClassIds());
		hm.put(SUBPART_IDS_TOKEN, this.getSubpartIds());
		hm.put(ITYPES_TOKEN, this.getItypes());
		hm.put(MUST_HAVE_CHILD_CLASSES_TOKEN, this.getMustHaveChildClasses());
		hm.put(CLASS_LABELS_TOKEN, this.getClassLabels());
		hm.put(CLASS_LABEL_INDENTS_TOKEN, this.getClassLabelIndents());
		hm.put(PARENT_CLASS_IDS_TOKEN, this.getParentClassIds());
		hm.put(READ_ONLY_CLASSES_TOKEN, this.getReadOnlyClasses());
		hm.put(READ_ONLY_DATE_PATTERNS_TOKEN, this.getReadOnlyDatePatterns());
		hm.put(ENROLLMENTS_TOKEN, this.getEnrollments());
		hm.put(SNAPSHOT_LIMITS_TOKEN, this.getSnapshotLimits());
		hm.put(MIN_CLASS_LIMITS_TOKEN, this.getMinClassLimits());
		hm.put(MAX_CLASS_LIMITS_TOKEN, this.getMaxClassLimits());
		hm.put(ROOM_RATIOS_TOKEN, this.getRoomRatios());
		hm.put(NUMBER_OF_ROOMS_TOKEN, this.getNumberOfRooms());
		hm.put(DEPARTMENTS_TOKEN, this.getDepartments());
		hm.put(DATE_PATTERNS_TOKEN, this.getDatePatterns());
		hm.put(DISPLAY_INSTRUCTORS_TOKEN, this.getDisplayInstructors());
		hm.put(ENABLED_FOR_STUDENT_SCHEDULING_TOKEN, this.getEnabledForStudentScheduling());
		hm.put(SUBTOTAL_INDEXES_TOKEN, this.getSubtotalIndexes());
		hm.put(TIMES_TOKEN, this.getTimes());
		hm.put(ROOMS_TOKEN, this.getRooms());
		hm.put(INSTRUCTORS_TOKEN, this.getInstructors());
		hm.put(EXTERNAL_IDS_TOKEN, this.getExternalIds());
		hm.put(CAN_DELETE_TOKEN, this.getCanDelete());
		hm.put(CAN_CANCEL_TOKEN, this.getCanCancel());
		hm.put(IS_CANCELLED_TOKEN, this.getIsCancelled());
		hm.put(DISPLAY_LMS_TOKEN, this.getDisplayLms());
		hm.put(LMS_TOKEN, this.getLms());
		
		return(hm);
	}
	
	
	private Object getObjectFromListMapAtIndex(HashMap<String, List> hm, String key, int index){
		if (hm == null || key == null || key.length() == 0 || index < 0){
			return(null);
		}
		List list = hm.get(key);
		if (list == null || list.size() == 0) {
			return(null);
		}
		
		if (index >= list.size())
			return(null);
		else
			return(list.get(index));
	}
	private void addToClassesFromOrigClassesIndex(HashMap<String, List> originalClassesMap, int classIndex){
		this.getClassIds().add((String) getObjectFromListMapAtIndex(originalClassesMap, CLASS_IDS_TOKEN, classIndex));
		this.getSubpartIds().add((String) getObjectFromListMapAtIndex(originalClassesMap, SUBPART_IDS_TOKEN, classIndex));
		this.getItypes().add((String) getObjectFromListMapAtIndex(originalClassesMap, ITYPES_TOKEN, classIndex));
		this.getMustHaveChildClasses().add((Boolean) getObjectFromListMapAtIndex(originalClassesMap, MUST_HAVE_CHILD_CLASSES_TOKEN, classIndex));
		this.getClassLabels().add((String) getObjectFromListMapAtIndex(originalClassesMap, CLASS_LABELS_TOKEN, classIndex));
		this.getClassLabelIndents().add((String) getObjectFromListMapAtIndex(originalClassesMap, CLASS_LABEL_INDENTS_TOKEN, classIndex));
		this.getParentClassIds().add((String) getObjectFromListMapAtIndex(originalClassesMap, PARENT_CLASS_IDS_TOKEN, classIndex));
		this.getReadOnlyClasses().add((String) getObjectFromListMapAtIndex(originalClassesMap, READ_ONLY_CLASSES_TOKEN, classIndex));
		this.getReadOnlyDatePatterns().add((String) getObjectFromListMapAtIndex(originalClassesMap, READ_ONLY_DATE_PATTERNS_TOKEN, classIndex));
		this.getEnrollments().add((String) getObjectFromListMapAtIndex(originalClassesMap, ENROLLMENTS_TOKEN, classIndex));
		this.getSnapshotLimits().add((String) getObjectFromListMapAtIndex(originalClassesMap, SNAPSHOT_LIMITS_TOKEN, classIndex));
		this.getMinClassLimits().add((String) getObjectFromListMapAtIndex(originalClassesMap, MIN_CLASS_LIMITS_TOKEN, classIndex));
		this.getMaxClassLimits().add((String) getObjectFromListMapAtIndex(originalClassesMap, MAX_CLASS_LIMITS_TOKEN, classIndex));
		this.getRoomRatios().add((String) getObjectFromListMapAtIndex(originalClassesMap, ROOM_RATIOS_TOKEN, classIndex));
		this.getNumberOfRooms().add((String) getObjectFromListMapAtIndex(originalClassesMap, NUMBER_OF_ROOMS_TOKEN, classIndex));

		this.getDepartments().add((String) getObjectFromListMapAtIndex(originalClassesMap, DEPARTMENTS_TOKEN, classIndex));
		this.getDatePatterns().add((String) getObjectFromListMapAtIndex(originalClassesMap, DATE_PATTERNS_TOKEN, classIndex));
		this.getDisplayInstructors().add((String) getObjectFromListMapAtIndex(originalClassesMap, DISPLAY_INSTRUCTORS_TOKEN, classIndex));
		this.getEnabledForStudentScheduling().add((String) getObjectFromListMapAtIndex(originalClassesMap, ENABLED_FOR_STUDENT_SCHEDULING_TOKEN, classIndex));
		this.getSubtotalIndexes().add((Integer) getObjectFromListMapAtIndex(originalClassesMap, SUBTOTAL_INDEXES_TOKEN, classIndex));
		this.getTimes().add((String) getObjectFromListMapAtIndex(originalClassesMap, TIMES_TOKEN, classIndex));
		this.getRooms().add((String) getObjectFromListMapAtIndex(originalClassesMap, ROOMS_TOKEN, classIndex));
		this.getInstructors().add((String) getObjectFromListMapAtIndex(originalClassesMap, INSTRUCTORS_TOKEN, classIndex));
		this.getExternalIds().add((String) getObjectFromListMapAtIndex(originalClassesMap, EXTERNAL_IDS_TOKEN, classIndex));
		this.getCanDelete().add((Boolean) getObjectFromListMapAtIndex(originalClassesMap, CAN_DELETE_TOKEN, classIndex));
		this.getCanCancel().add((Boolean) getObjectFromListMapAtIndex(originalClassesMap, CAN_CANCEL_TOKEN, classIndex));
		this.getIsCancelled().add((Boolean) getObjectFromListMapAtIndex(originalClassesMap, IS_CANCELLED_TOKEN, classIndex));
		this.getLms().add((String) getObjectFromListMapAtIndex(originalClassesMap, LMS_TOKEN, classIndex));
	}
	
	public void addNewClassesBasedOnTemplate(String clsId){
		int addNewAfterIndex = indexOfLastChildClass(clsId);
		int originalListSize = this.getClassIds().size();

		HashMap origListsMap = buildOriginalListValuesMap();
		this.resetLists();
		
		for(int i = 0; i <= addNewAfterIndex; i++){
			this.addToClassesFromOrigClassesIndex(origListsMap, i);
		}
		String parentClassId = this.getParentClassIds().get(this.getClassIds().indexOf(clsId.toString())).toString();
		addNewClassesBasedOnTemplate(clsId, nextTmpClassId((List<String>) origListsMap.get(CLASS_IDS_TOKEN)).toString(), (parentClassId.length() == 0)?null:parentClassId);			
		for(int i = (addNewAfterIndex + 1); i < originalListSize; i++){
			this.addToClassesFromOrigClassesIndex(origListsMap, i);
		}
		initClassHasErrorsToFalse();
		setDirectionsClassesCanMove();
	}
	
	public void moveClassUp(String clsId){
		int indexOfFirstClassToMove = this.getClassIds().indexOf(clsId);
		int indexOfLastClassToMove = this.indexOfLastChildClass(clsId);
		String newParentClassId = this.findNewParentClassId(clsId, DIRECTION_UP);
		if (newParentClassId != null){
			int indexOfClassToMoveAfter = this.indexOfLastChildClass(newParentClassId);
			int origListSize = this.getClassIds().size();
			this.getParentClassIds().set(indexOfFirstClassToMove, newParentClassId);
			
			HashMap origLists = buildOriginalListValuesMap();
			resetLists();
			for(int i = 0; i <= indexOfClassToMoveAfter; i++){
				this.addToClassesFromOrigClassesIndex(origLists, i);
			}
			for(int i = indexOfFirstClassToMove; i <= indexOfLastClassToMove; i++){
				this.addToClassesFromOrigClassesIndex(origLists, i);
			}
			for(int i = (indexOfClassToMoveAfter + 1); i < indexOfFirstClassToMove; i++){
				this.addToClassesFromOrigClassesIndex(origLists, i);
			}
			for(int i = (indexOfLastClassToMove + 1); i < origListSize; i++){
				this.addToClassesFromOrigClassesIndex(origLists, i);			
			}
			
			initClassHasErrorsToFalse();
			setDirectionsClassesCanMove();
		}
	}
	
	public void moveClassDown(String clsId){
		int indexOfFirstClassToMove = this.getClassIds().indexOf(clsId);
		int indexOfLastClassToMove = this.indexOfLastChildClass(clsId);
		String newParentClassId = this.findNewParentClassId(clsId, DIRECTION_DOWN);
		if (newParentClassId != null){
			int indexOfClassToMoveAfter = this.indexOfLastChildClass(newParentClassId);
			int origListSize = this.getClassIds().size();
			this.getParentClassIds().set(indexOfFirstClassToMove, newParentClassId);
			
			HashMap origLists = buildOriginalListValuesMap();
			resetLists();
			
			for(int i = 0; i < indexOfFirstClassToMove; i++){
				this.addToClassesFromOrigClassesIndex(origLists, i);
			}
			for(int i = (indexOfLastClassToMove + 1); i <= indexOfClassToMoveAfter; i++){
				this.addToClassesFromOrigClassesIndex(origLists, i);
			}
			for(int i = indexOfFirstClassToMove; i <= indexOfLastClassToMove; i++){
				this.addToClassesFromOrigClassesIndex(origLists, i);
			}
			for(int i = (indexOfClassToMoveAfter + 1); i < origListSize; i++){
				this.addToClassesFromOrigClassesIndex(origLists, i);
			}					
			initClassHasErrorsToFalse();
			setDirectionsClassesCanMove();
		}	
	}
	
	private String findNewParentClassId(String clsId, String direction) {
		int clsIndex = this.getClassIds().indexOf(clsId);
		String clsParentId = (String) this.getParentClassIds().get(clsIndex);
		if (clsParentId == null || clsParentId.length() == 0){
			return(null);
		}
		int parentClassIndex = this.getClassIds().indexOf(clsParentId);
		String parentClassSubpartId = (String) this.getSubpartIds().get(parentClassIndex);
		if (parentClassSubpartId == null || parentClassSubpartId.length() == 0){
			return(null);
		}
		
		String subpartId = null;
		String parentId = null;
		
		if(direction.equals(DIRECTION_UP)){
			int i = parentClassIndex - 1;
			while (i >= 0){
				subpartId = (String)this.getSubpartIds().get(i);
				if (subpartId != null && subpartId.length() > 0 && subpartId.equals(parentClassSubpartId)){
					parentId = (String) this.getClassIds().get(i);
					if (parentId != null && parentId.length() > 0 && !parentId.equals(clsParentId)){
						break;
					}
				}				
				i--;
			}
			if (i >= 0){
				return(parentId);
			}
		} else if (direction.equals(DIRECTION_DOWN)){
			int i = clsIndex + 1;
			while (i < this.getClassIds().size()){
				subpartId = (String)this.getSubpartIds().get(i);
				if (subpartId != null && subpartId.length() > 0 && subpartId.equals(parentClassSubpartId)){
					parentId = (String) this.getClassIds().get(i);
					if (parentId != null && parentId.length() > 0 && !parentId.equals(clsParentId)){
						break;
					}
				}								
				i++;
			}
			if (i < this.getClassIds().size()){
				return(parentId);
			}
			
		}
		return (null);
	}

	private void addNewClassesBasedOnTemplate(String clsId, String tmpClassId, String parentClassId){
		int index = this.getClassIds().indexOf(clsId);
		String label = this.getClassLabels().get(index).toString();
		this.classLabels.add((label.substring(0,label.indexOf(" ")))+" New" + tmpClassId);
		this.classLabelIndents.add(this.getClassLabelIndents().get(index).toString());
		this.classIds.add(tmpClassId);
		this.subpartIds.add(this.getSubpartIds().get(index).toString());
		this.itypes.add(this.getItypes().get(index).toString());
		this.mustHaveChildClasses.add(this.getMustHaveChildClasses().get(index));
		this.parentClassIds.add((parentClassId != null)?parentClassId.toString():"");
		this.readOnlyClasses.add(Boolean.valueOf(false).toString());
		this.readOnlyDatePatterns.add(Boolean.valueOf(false).toString());
		this.enrollments.add("");
		this.snapshotLimits.add("");
		this.minClassLimits.add(this.getMinClassLimits().get(index));
		this.departments.add(this.getDepartments().get(index));
		this.datePatterns.add(this.getDatePatterns().get(index));
		this.numberOfRooms.add(this.getNumberOfRooms().get(index));
		this.maxClassLimits.add(this.getMaxClassLimits().get(index));
		this.roomRatios.add(this.getRoomRatios().get(index));
		this.displayInstructors.add(this.getDisplayInstructors().get(index));
		this.enabledForStudentScheduling.add(this.getEnabledForStudentScheduling().get(index));
		this.subtotalIndexes.add(this.getSubtotalIndexes().get(index));
		this.times.add("");
		this.rooms.add("");
		this.instructors.add("");
		this.externalIds.add("");
		ArrayList<String> childClasses = new ArrayList<String>();
		for(int i = (index + 1); i < this.getClassIds().size(); i++){
			if (this.getParentClassIds().get(i).toString().equals(clsId))
				childClasses.add(this.getClassIds().get(i));
		}
		for (Iterator<String> it = childClasses.iterator(); it.hasNext();){
			addNewClassesBasedOnTemplate(it.next(), nextTmpClassId(null).toString(), tmpClassId);
		}
		this.canDelete.add(true);
		this.canCancel.add(false);
		this.isCancelled.add(false);
		this.lms.add(this.getLms().get(index));
	}
	
	private Long nextTmpClassId(List<String> origClassIds){
		long nextId = -1;
		long id;
		if (origClassIds != null){
			for (Iterator it = origClassIds.iterator(); it.hasNext();){
				id = Long.parseLong(it.next().toString());
				if (nextId >= id){
					nextId = id - 1;
				}
			}
		}
		for (Iterator it = this.classIds.iterator(); it.hasNext();){
			id = Long.parseLong(it.next().toString());
			if (nextId >= id){
				nextId = id - 1;
			}
		}
		return (Long.valueOf(nextId));
	}

	public Integer getSubjectAreaId() {
		return subjectAreaId;
	}

	public void setSubjectAreaId(Integer subjectAreaId) {
		this.subjectAreaId = subjectAreaId;
	}

	public String getInstrOfferingName() {
		return instrOfferingName;
	}

	public void setInstrOfferingName(String instrOfferingName) {
		this.instrOfferingName = instrOfferingName;
	}

	public List<String> getClassLabelIndents() {
		return classLabelIndents;
	}

	public void setClassLabelIndents(List<String> classLabelIndents) {
		this.classLabelIndents = classLabelIndents;
	}

	public List<String> getRoomRatios() {
		return roomRatios;
	}

	public void setRoomRatios(List<String> roomRatios) {
		this.roomRatios = roomRatios;
	}

	public List<String> getEnabledForStudentScheduling() {
		return enabledForStudentScheduling;
	}

	public void setEnabledForStudentScheduling(List<String> enabledForStudentScheduling) {
		this.enabledForStudentScheduling = enabledForStudentScheduling;
	}

	public List<String> getDisplayInstructors() {
		return displayInstructors;
	}

	public void setDisplayInstructors(List<String> displayInstructors) {
		this.displayInstructors = displayInstructors;
	}

	public List<Boolean> getClassCanMoveDown() {
		return classCanMoveDown;
	}

	public void setClassCanMoveDown(List<Boolean> classCanMoveDown) {
		this.classCanMoveDown = classCanMoveDown;
	}

	public List<Boolean> getClassCanMoveUp() {
		return classCanMoveUp;
	}

	public void setClassCanMoveUp(List<Boolean> classCanMoveUp) {
		this.classCanMoveUp = classCanMoveUp;
	}

	public List<String> getItypes() {
		return itypes;
	}

	public void setItypes(List<String> itypes) {
		this.itypes = itypes;
	}

	public List<Boolean> getMustHaveChildClasses() {
		return mustHaveChildClasses;
	}

	public void setMustHaveChildClasses(List<Boolean> mustHaveChildClasses) {
		this.mustHaveChildClasses = mustHaveChildClasses;
	}

	public String getOrigSubparts() {
		return origSubparts;
	}

	public void setOrigSubparts(String origSubparts) {
		this.origSubparts = origSubparts;
	}

	public Boolean getDisplayMaxLimit() {
		return displayMaxLimit;
	}

	public void setDisplayMaxLimit(Boolean displayMaxLimit) {
		this.displayMaxLimit = displayMaxLimit;
	}

	public Boolean getDisplayOptionForMaxLimit() {
		return displayOptionForMaxLimit;
	}

	public void setDisplayOptionForMaxLimit(Boolean displayOptionForMaxLimit) {
		this.displayOptionForMaxLimit = displayOptionForMaxLimit;
	}

	public boolean maxLimitCanBeHidden() {
		Iterator it1 = getMinClassLimits().iterator();
		Iterator it2 = getMaxClassLimits().iterator();
		String min;
		String max;
		while (it1.hasNext() && it2.hasNext()){
			min = (String) it1.next();
			max = (String) it2.next();
			if (min == null && max == null){
				// max limits can be hidden
			} else if (min != null && max != null && min.equals(max)){
				// max limits can be hidden
			} else {
				return (false);
			}
		}
		return (true);
	}

	public List<Integer> getSubtotalIndexes() {
		return subtotalIndexes;
	}

	public void setSubtotalIndexes(List<Integer> subtotalIndexes) {
		this.subtotalIndexes = subtotalIndexes;
	}

	public List<String> getSubtotalLabels() {
		return subtotalLabels;
	}

	public void setSubtotalLabels(List<String> subtotalLabels) {
		this.subtotalLabels = subtotalLabels;
	}

	public List<Integer> getSubtotalValues() {
		return subtotalValues;
	}

	public void setSubtotalValues(List<Integer> subtotalValues) {
		this.subtotalValues = subtotalValues;
	}

	public List<Integer> getSubtotalSnapValues() {
		return subtotalSnapValues;
	}

	public void setSubtotalSnapValues(List<Integer> subtotalSnapValues) {
		this.subtotalSnapValues = subtotalSnapValues;
	}

	public String getEnableAllClassesForStudentScheduling() {
		return enableAllClassesForStudentScheduling;
	}

	public void setEnableAllClassesForStudentScheduling(String enableAllClassesForStudentScheduling) {
		this.enableAllClassesForStudentScheduling = enableAllClassesForStudentScheduling;
	}

	public String getDisplayAllClassesInstructors() {
		return displayAllClassesInstructors;
	}

	public void setDisplayAllClassesInstructors(String displayAllClassesInstructors) {
		this.displayAllClassesInstructors = displayAllClassesInstructors;
	}

	public List<Boolean> getEnableAllClassesForStudentSchedulingForSubpart() {
		return enableAllClassesForStudentSchedulingForSubpart;
	}

	public void setEnableAllClassesForStudentSchedulingForSubpart(
			List<Boolean> enableAllClassesForStudentSchedulingForSubpart) {
		this.enableAllClassesForStudentSchedulingForSubpart = enableAllClassesForStudentSchedulingForSubpart;
	}

	public List<Boolean> getDisplayAllClassesInstructorsForSubpart() {
		return displayAllClassesInstructorsForSubpart;
	}

	public void setDisplayAllClassesInstructorsForSubpart(
			List<Boolean> displayAllClassesInstructorsForSubpart) {
		this.displayAllClassesInstructorsForSubpart = displayAllClassesInstructorsForSubpart;
	}

	public List<Boolean> getReadOnlySubparts() {
		return readOnlySubparts;
	}

	public void setReadOnlySubparts(List<Boolean> readOnlySubparts) {
		this.readOnlySubparts = readOnlySubparts;
	}

	public List<String> getEnrollments() {
		return enrollments;
	}

	public void setEnrollments(List<String> enrollments) {
		this.enrollments = enrollments;
	}

	public List<String> getSnapshotLimits() {
		return snapshotLimits;
	}

	public void setSnapshotLimits(List<String> snapshotLimits) {
		this.snapshotLimits = snapshotLimits;
	}

	public Boolean getDisplayEnrollment() {
		return displayEnrollment;
	}

	public void setDisplayEnrollment(Boolean displayEnrollment) {
		this.displayEnrollment = displayEnrollment;
	}

	public Boolean getDisplaySnapshotLimit() {
		return displaySnapshotLimit;
	}

	public void setDisplaySnapshotLimit(Boolean displaySnapshotLimit) {
		this.displaySnapshotLimit = displaySnapshotLimit;
	}

	public List<String> getTimes() {
		return times;
	}

	public void setTimes(List<String> times) {
		this.times = times;
	}

	public List<String> getRooms() {
		return rooms;
	}

	public void setRooms(List<String> rooms) {
		this.rooms = rooms;
	}

	public List<String> getInstructors() {
		return instructors;
	}

	public void setInstructors(List<String> instructors) {
		this.instructors = instructors;
	}

	public Boolean getDisplayExternalId() {
		return displayExternalId;
	}

	public void setDisplayExternalId(Boolean displayExternalId) {
		this.displayExternalId = displayExternalId;
	}
	
	public Boolean getEditExternalId() {
		return editExternalId;
	}

	public void setEditExternalId(Boolean editExternalId) {
		this.editExternalId = editExternalId;
	}
	
	public Boolean getEditSnapshotLimits() {
		return editSnapshotLimits;
	}
	
	public void setEditSnapshotLimits(Boolean editSnapshotLimits) {
		this.editSnapshotLimits = editSnapshotLimits;
	}

	public Boolean getDisplayDisplayInstructors() {
		return displayDisplayInstructors;
	}

	public void setDisplayDisplayInstructors(Boolean displayDisplayInstructors) {
		this.displayDisplayInstructors = displayDisplayInstructors;
	}

	public Boolean getDisplayEnabledForStudentScheduling() {
		return displayEnabledForStudentScheduling;
	}

	public void setDisplayEnabledForStudentScheduling(Boolean displayEnabledForStudentScheduling) {
		this.displayEnabledForStudentScheduling = displayEnabledForStudentScheduling;
	}

	public List<String> getExternalIds() {
		return externalIds;
	}

	public void setExternalIds(List<String> externalIds) {
		this.externalIds = externalIds;
	}
	
	public Boolean getDisplayLms() {
		return displayLms;
	}

	public void setDisplayLms(Boolean displayLms) {
		this.displayLms = displayLms;
	}

	public List<String> getLms() {
		return lms;
	}

	public void setLms(List<String> lms) {
		this.lms = lms;
	}

	public List<Boolean> getCanDelete() { return canDelete; }
	public void setCanDelete(List<Boolean> canDelete) { this.canDelete = canDelete; }
	public List<Boolean> getCanCancel() { return canCancel; }
	public void setCanCancel(List<Boolean> canCancel) { this.canCancel = canCancel; }
	public List<Boolean> getIsCancelled() { return isCancelled; }
	public void setIsCancelled(List<Boolean> isCancelled) { this.isCancelled = isCancelled; }
	public void setCancelled(String classId, boolean cancelled) {
		if (classId == null || classId.isEmpty()) return;
		if (cancelled && Long.valueOf(classId) < 0) {
			removeFromClasses(classId);
			return;
		}
		for (int i = 0; i < classIds.size(); i++) {
    		if (classId.equals(classIds.get(i))) {
    			boolean wasCancelled = isCancelled.get(i);
    			isCancelled.set(i, cancelled);
    			if (wasCancelled && !cancelled) setCancelled((String)parentClassIds.get(i), false);
    			if (!wasCancelled && cancelled) {
    				boolean allCancelled = true;
    				for (int j = 0; j < subpartIds.size(); j++) {
    					if (i != j && subpartIds.get(i).equals(subpartIds.get(j)) && parentClassIds.get(i).equals(parentClassIds.get(j)) && !"true".equals(isCancelled.get(j))) {
    						allCancelled = false; break;
    					}
    				}
    				if (allCancelled)
    					setCancelled((String)parentClassIds.get(i), true);
    			}
    		}
    	}
		for (int i = 0; i < parentClassIds.size(); i++) {
    		if (classId.equals(parentClassIds.get(i))) {
    			setCancelled((String)classIds.get(i), cancelled);
    		}
    	}
	}
	
    public Long getInstructionalMethod() { return instructionalMethod; }
    public void setInstructionalMethod(Long instructionalMethod) { this.instructionalMethod = instructionalMethod; }
    public String getInstructionalMethodDefault() { return instructionalMethodDefault; }
    public void setInstructionalMethodDefault(String instructionalMethodDefault) { this.instructionalMethodDefault = instructionalMethodDefault; }
    public List<IdValue> getInstructionalMethods() {
    	List<IdValue> ret = new ArrayList<IdValue>();
    	for (InstructionalMethod type: InstructionalMethod.findAll())
    		if (type.isVisible() || type.getUniqueId().equals(instructionalMethod))
    			ret.add(new IdValue(type.getUniqueId(), type.getLabel()));
    	return ret;
    }
    public boolean getInstructionalMethodEditable() { return instructionalMethodEditable; }
    public void setInstructionalMethodEditable(boolean instructionalMethodEditable) { this.instructionalMethodEditable = instructionalMethodEditable; }
    public String getInstructionalMethodLabel() {
    	if (instructionalMethod != null) {
    		for (InstructionalMethod type: InstructionalMethod.findAll())
        		if (type.getUniqueId().equals(instructionalMethod))
        			return type.getLabel();
    		
    	}
		if (instructionalMethodDefault == null) return MSG.selectNoInstructionalMethod();
		else return MSG.defaultInstructionalMethod(instructionalMethodDefault);
    }
}
