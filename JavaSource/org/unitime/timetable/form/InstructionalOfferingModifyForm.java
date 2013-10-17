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
package org.unitime.timetable.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.util.MessageResources;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.dao.SchedulingSubpartDAO;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.util.DynamicList;
import org.unitime.timetable.util.DynamicListObjectFactory;


/**
 * @author Stephanie Schluttenhofer, Tomas Muller, Zuzana Mullerova
 */
public class InstructionalOfferingModifyForm extends ActionForm {

	
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5412595518174343486L;
	
    // --------------------------------------------------------- Instance Variables
	private String op;
    private Integer subjectAreaId;
	private Long instrOfferingId;
    private String instrOfferingName;
	private Integer instrOffrConfigLimit;
	private Long instrOffrConfigId;
	private String origSubparts;
	private Boolean displayMaxLimit;
	private Boolean displayOptionForMaxLimit;
	private Boolean displayEnrollment;
	private String enableAllClassesForStudentScheduling;
	private String displayAllClassesInstructors;
	private Boolean displayExternalId;
	private Boolean displayDisplayInstructors;
	private Boolean displayEnabledForStudentScheduling;
	
	private List classIds;
	private List subpartIds;
	private List itypes;
	private List mustHaveChildClasses;
	private List parentClassIds;
	private List readOnlyClasses;
	private List classLabels;
	private List classLabelIndents;
	private List enrollments;
	private List minClassLimits;
	private List maxClassLimits;
	private List roomRatios;
	private List numberOfRooms;
	private List departments;
	private List datePatterns;
	private List displayInstructors;
	private List enabledForStudentScheduling;
	private List classCanMoveUp;
	private List classCanMoveDown;
	private List subtotalIndexes;
	private List subtotalLabels;
	private List subtotalValues;
	private List enableAllClassesForStudentSchedulingForSubpart;
	private List displayAllClassesInstructorsForSubpart;
	private List readOnlySubparts;
	private List times;
	private List rooms;
	private List instructors;	
	private List externalIds;
	
	private List classHasErrors;
	private Long addTemplateClassId;
	private Long moveUpClassId;
	private Long moveDownClassId;
	private Long deletedClassId;
	
	private static String CLASS_IDS_TOKEN = "classIds";
	private static String SUBPART_IDS_TOKEN = "subpartIds";
	private static String PARENT_CLASS_IDS_TOKEN = "parentClassIds";
	private static String ITYPES_TOKEN = "itypes";
	private static String MUST_HAVE_CHILD_CLASSES_TOKEN = "mustHaveChildClasses";
	private static String READ_ONLY_CLASSES_TOKEN = "readOnlyClasses";
	private static String CLASS_LABELS_TOKEN = "classLabels";
	private static String CLASS_LABEL_INDENTS_TOKEN = "classLabelIndents";
	private static String ENROLLMENTS_TOKEN = "enrollments";
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
	

    // --------------------------------------------------------- Classes

    /** Factory to create dynamic list element for Course Offerings */
    protected DynamicListObjectFactory factoryClasses = new DynamicListObjectFactory() {
        public Object create() {
            return new String(Preference.BLANK_PREF_VALUE);
        }
    };
		
    // --------------------------------------------------------- Methods
    /** 
     * Method validate
     * @param mapping
     * @param request
     * @return ActionErrors
     */
    public ActionErrors validate(
        ActionMapping mapping,
        HttpServletRequest request) {

        ActionErrors errors = new ActionErrors();

        // Get Message Resources
        MessageResources rsc = 
            (MessageResources) super.getServlet()
            	.getServletContext().getAttribute(Globals.MESSAGES_KEY);

        if (op.equals(rsc.getMessage("button.add"))) {
            // Check Added Course
	        if (this.addTemplateClassId==null || this.addTemplateClassId.longValue()<=0) {
	            errors.add("getAddTemplateClassId", new ActionMessage("errors.generic", MSG.errorRequiredClass()));            
	        }
        }
        
        if (op.equals(rsc.getMessage("button.moveUp"))) {
            // Check Course to move up
	        if (this.moveUpClassId==null || this.moveUpClassId.longValue()<=0) {
	            errors.add("getMoveUpClassId", new ActionMessage("errors.generic", MSG.errorRequiredClass()));            
	        }
        }
        
        if (op.equals(rsc.getMessage("button.moveDown"))) {
            // Check Course to move down
	        if (this.moveDownClassId==null || this.moveDownClassId.longValue()<=0) {
	            errors.add("getMoveDownClassId", new ActionMessage("errors.generic", MSG.errorRequiredClass()));            
	        }
        }
        
        if (op.equals(MSG.actionUpdateMultipleClassSetup())) {
	        // Check Instructional Offering Config
	        if (this.instrOffrConfigId==null || this.instrOffrConfigId.intValue()<=0) {
	            errors.add("instrOffrConfigId", new ActionMessage("errors.generic", MSG.errorRequiredIOConfiguration()));            
	        }
	        // Validate class limits provide space that is >= limit for the instructional offering config
	        validateChildClassExistence(errors);
	        validateClassLimits(errors);
	        validateAllSubpartsHaveAtLeastOneClass(errors);
        }
        
        return errors;
    }
    
    private void validateChildClassExistence(ActionErrors errors){
    	for(int index = 0 ; index < this.getClassIds().size(); index++){
    		if (Boolean.valueOf((String) this.getMustHaveChildClasses().get(index)).booleanValue()){
    			String classId = (String) this.getClassIds().get(index);
    			if ((index + 1) == this.getClassIds().size()){
        			errors.add("mustHaveChildClasses", 
        					new ActionMessage("errors.generic", MSG.errorClassMustHaveChildClasses((String) this.getClassLabels().get(index))));
        			this.getClassHasErrors().set(index, new Boolean(true));    				
    			} else {
	    			String parentOfNextClass = (String) this.getParentClassIds().get(index + 1);
	    			if (parentOfNextClass == null || !parentOfNextClass.equals(classId)){
	        			errors.add("mustHaveChildClasses", 
	        					new ActionMessage("errors.generic", MSG.errorClassMustHaveChildClasses((String) this.getClassLabels().get(index))));
	        			this.getClassHasErrors().set(index, new Boolean(true));    				    				
	    			}
    			}
    		}
    	}
    }
    
    private void validateAllSubpartsHaveAtLeastOneClass(ActionErrors errors){
    	
    	String[] subparts = this.getOrigSubparts().split(",");
    	for(int i = 0; i < subparts.length; i++){
    		if (!this.getSubpartIds().contains(subparts[i])){
    			errors.add("allSubpartsMustHaveAClass", new ActionMessage("errors.generic", MSG.errorEachSubpartMustHaveClass()));
    			break;
    		}
    	}
    }
    
    private void validateMinLessThanMaxClassLimits(ActionErrors errors){
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
    			errors.add("minLimitGreaterThanMaxLimit", new ActionMessage("errors.generic", MSG.errorMaxLessThanMinLimit((String) this.getClassLabels().get(index))));
    			this.getClassHasErrors().set(index, new Boolean(true));
    		}    		
    		index++;
    	}
    }
    
    private void validateMinOrMaxParentClassLimits(ActionErrors errors, List limits, String errorName, String errorMessage){
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
				subpartLimits.put(subpartKey, new Integer(value2));
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
			parentLimit = new Integer((String) it5.next());
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
			errors.add(errorName, new ActionMessage("errors.generic", errorMessage));  			
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
						this.getClassHasErrors().set(index, new Boolean(true)); 
					} 
				}
				index++;
			}
		}	
    }
    
    private void validateSubpartClassLimits(ActionErrors errors){
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
    			subpartClassLimits.put(subpartKey, new Integer(value1));
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
				errors.add("maxLimit", new ActionMessage("errors.generic", MSG.errorMaxLimitsTotalTooLow()));
			} else {
				errors.add("maxLimit", new ActionMessage("errors.generic", MSG.errorLimitsForTopLevelClassesTooLow()));
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
						this.getClassHasErrors().set(index, new Boolean(true));	                           					
					}
				}
				index++;
			}
		}
    }
    
    private void initClassHasErrorsToFalse(){
		this.setClassHasErrors(DynamicList.getInstance(new ArrayList(), factoryClasses));
		for(Iterator it = this.getClassIds().iterator(); it.hasNext();){
			this.getClassHasErrors().add(new Boolean(false));
			it.next();
		}
    }
    
    private void initClassMoveDirections(){
    	this.setClassCanMoveDown(DynamicList.getInstance(new ArrayList(), factoryClasses));
		this.setClassCanMoveUp(DynamicList.getInstance(new ArrayList(), factoryClasses));
		for(Iterator it = this.getClassIds().iterator(); it.hasNext();){
			this.getClassCanMoveDown().add(new Boolean(false));
			this.getClassCanMoveUp().add(new Boolean(false));
			it.next();
		}
    }
    
    private void validateClassLimits(ActionErrors errors){
    	int limit = getInstrOffrConfigLimit().intValue();
    	if (limit > 0) {
    		initClassHasErrorsToFalse();
    		validateMinLessThanMaxClassLimits(errors);
    		validateMinOrMaxParentClassLimits(errors, this.getMaxClassLimits(), "maxLimit", ((getDisplayMaxLimit().booleanValue())? MSG.errorTotalMaxChildrenAtLeastMaxParent():MSG.errorLimitsChildClasses()));
    		if ("true".equals(ApplicationProperties.getProperty("unitime.instrOfferingConfig.checkConfigLimit", "true")))
    			validateSubpartClassLimits(errors);
    	}
    }
 
    /** 
     * Method reset
     * @param mapping
     * @param request
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) {
    	instrOfferingId = null;
    	instrOffrConfigLimit = null;
    	instrOffrConfigId = null;
    	instrOfferingName = "";
    	origSubparts = "";
    	displayMaxLimit = new Boolean(false);
    	displayOptionForMaxLimit = new Boolean(false);
    	displayEnrollment = new Boolean(false);
    	displayDisplayInstructors = new Boolean(false);
    	displayEnabledForStudentScheduling = new Boolean(false);
    	displayExternalId = new Boolean(false);
    	enableAllClassesForStudentScheduling = "";
    	displayAllClassesInstructors = "";
    	resetLists();
    }
    
    private void resetLists(){
    	classIds = DynamicList.getInstance(new ArrayList(), factoryClasses);
    	subpartIds = DynamicList.getInstance(new ArrayList(), factoryClasses);
    	itypes = DynamicList.getInstance(new ArrayList(), factoryClasses);
    	mustHaveChildClasses = DynamicList.getInstance(new ArrayList(), factoryClasses);
    	parentClassIds = DynamicList.getInstance(new ArrayList(), factoryClasses);
    	readOnlyClasses = DynamicList.getInstance(new ArrayList(), factoryClasses);
       	classHasErrors = DynamicList.getInstance(new ArrayList(), factoryClasses);
       	classLabels = DynamicList.getInstance(new ArrayList(), factoryClasses);
       	classLabelIndents = DynamicList.getInstance(new ArrayList(), factoryClasses);
       	enrollments = DynamicList.getInstance(new ArrayList(), factoryClasses);
       	minClassLimits = DynamicList.getInstance(new ArrayList(), factoryClasses);
    	maxClassLimits = DynamicList.getInstance(new ArrayList(), factoryClasses);
    	roomRatios = DynamicList.getInstance(new ArrayList(), factoryClasses);
    	numberOfRooms = DynamicList.getInstance(new ArrayList(), factoryClasses);
      	departments = DynamicList.getInstance(new ArrayList(), factoryClasses);
    	datePatterns = DynamicList.getInstance(new ArrayList(), factoryClasses);
      	displayInstructors = DynamicList.getInstance(new ArrayList(), factoryClasses);
    	enabledForStudentScheduling = DynamicList.getInstance(new ArrayList(), factoryClasses);
       	subtotalIndexes = DynamicList.getInstance(new ArrayList(), factoryClasses);
       	subtotalLabels = DynamicList.getInstance(new ArrayList(), factoryClasses);
       	subtotalValues = DynamicList.getInstance(new ArrayList(), factoryClasses);
       	enableAllClassesForStudentSchedulingForSubpart = DynamicList.getInstance(new ArrayList(), factoryClasses);
       	displayAllClassesInstructorsForSubpart = DynamicList.getInstance(new ArrayList(), factoryClasses);
    	classCanMoveUp = DynamicList.getInstance(new ArrayList(), factoryClasses);
    	classCanMoveDown = DynamicList.getInstance(new ArrayList(), factoryClasses);
    	readOnlySubparts = DynamicList.getInstance(new ArrayList(), factoryClasses);
    	times = DynamicList.getInstance(new ArrayList(), factoryClasses);
    	rooms = DynamicList.getInstance(new ArrayList(), factoryClasses);
    	instructors = DynamicList.getInstance(new ArrayList(), factoryClasses);
    	externalIds = DynamicList.getInstance(new ArrayList(), factoryClasses);
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
    	this.getClassCanMoveDown().set(classIndex,new Boolean(false));    
		this.getClassCanMoveUp().set(classIndex,new Boolean(false)); 
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
	    		this.getClassCanMoveUp().set(classIndex,new Boolean(true));    
	    	}
	    	
	    	highestParentSubpartIdIndex = this.getSubpartIds().lastIndexOf(parentSubpartId);
	    	if (highestParentSubpartIdIndex > parentClassIndex){
	    		this.getClassCanMoveDown().set(classIndex, new Boolean(true));
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
    	this.setSubtotalIndexes(DynamicList.getInstance(new ArrayList(), factoryClasses));
		this.setSubtotalLabels(DynamicList.getInstance(new ArrayList(), factoryClasses));
		this.setSubtotalValues(DynamicList.getInstance(new ArrayList(), factoryClasses));
		this.setEnableAllClassesForStudentSchedulingForSubpart(DynamicList.getInstance(new ArrayList(), factoryClasses));
		this.setDisplayAllClassesInstructorsForSubpart(DynamicList.getInstance(new ArrayList(), factoryClasses));
		this.setReadOnlySubparts(DynamicList.getInstance(new ArrayList(), factoryClasses));
		SchedulingSubpartDAO ssDao = new SchedulingSubpartDAO();
		SchedulingSubpart ss = null;
    	int i = 0;
    	int cnt = 0;
    	Iterator ssIt = this.getSubpartIds().iterator();
    	Iterator limitIt = this.getMinClassLimits().iterator();

    	Boolean enableForScheduling = null;
    	Boolean displayInstructor = null;
    	Boolean readOnlySubpart = null;
    	while (ssIt.hasNext() && limitIt.hasNext()){
    		Long subpartId = Long.valueOf((String) ssIt.next());
    		Integer limit = new Integer((String) limitIt.next());
     		enableForScheduling = new Boolean(determineBooleanValueAtIndex(this.getEnabledForStudentScheduling(), cnt));    	   	
     		displayInstructor = new Boolean(determineBooleanValueAtIndex(this.getDisplayInstructors(), cnt));
       	   	readOnlySubpart = new Boolean(determineBooleanValueAtIndex(this.getReadOnlyClasses(), cnt));
     	   	
    		int addLimit = (limit == null)?0:limit.intValue();
    		Integer subtotalIndex = null;

    		if (!subpartToIndex.containsKey(subpartId)) {
     			ss = ssDao.get(subpartId);
    			getSubtotalValues().add(addLimit);
    			getEnableAllClassesForStudentSchedulingForSubpart().add(enableForScheduling);
    			getDisplayAllClassesInstructorsForSubpart().add(displayInstructor);
    			getReadOnlySubparts().add(readOnlySubpart);
    			String label = ss.getItypeDesc() + ss.getSchedulingSubpartSuffix();
    			getSubtotalLabels().add(label);
     			subpartToIndex.put(subpartId, new Integer(i));
     			subtotalIndex = new Integer(i);
   	   			i++;
    		} else {
        		subtotalIndex = (Integer) subpartToIndex.get(subpartId);
        		int oldSubtotal = ((Integer) this.getSubtotalValues().get(subtotalIndex)).intValue();
        		int newSubtotal = oldSubtotal + addLimit;
        		this.getSubtotalValues().set(subtotalIndex.intValue(), newSubtotal);
        		boolean oldEnableForScheduling = ((Boolean) this.getEnableAllClassesForStudentSchedulingForSubpart().get(subtotalIndex)).booleanValue();
        		boolean newEnableForScheduling = oldEnableForScheduling && enableForScheduling.booleanValue();
        		this.getEnableAllClassesForStudentSchedulingForSubpart().set(subtotalIndex, new Boolean(newEnableForScheduling));
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

    private boolean determineBooleanValueAtIndex(List l, int index){
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
	public List getClassIds() {
		return classIds;
	}
	public void setClassIds(List classIds) {
		this.classIds = classIds;
	}
	public List getClassHasErrors() {
		return classHasErrors;
	}
	public void setClassHasErrors(List classHasErrors) {
		this.classHasErrors = classHasErrors;
	}
	public List getClassLabels() {
		return classLabels;
	}
	public void setClassLabels(List classLabels) {
		this.classLabels = classLabels;
	}
	public List getMinClassLimits() {
		return minClassLimits;
	}
	public void setMinClassLimits(List classLimits) {
		this.minClassLimits = classLimits;
	}
	public List getDatePatterns() {
		return datePatterns;
	}
	public void setDatePatterns(List datePatterns) {
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
	public List getDepartments() {
		return departments;
	}
	public void setDepartments(List managers) {
		this.departments = managers;
	}
	public List getNumberOfRooms() {
		return numberOfRooms;
	}
	public void setNumberOfRooms(List numberOfRooms) {
		this.numberOfRooms = numberOfRooms;
	}
	public String getOp() {
		return op;
	}
	public void setOp(String op) {
		this.op = op;
	}
	public List getParentClassIds() {
		return parentClassIds;
	}
	public void setParentClassIds(List parentClassIds) {
		this.parentClassIds = parentClassIds;
	}
	public List getMaxClassLimits() {
		return maxClassLimits;
	}
	public void setMaxClassLimits(List roomLimits) {
		this.maxClassLimits = roomLimits;
	}
	public List getSubpartIds() {
		return subpartIds;
	}
	public void setSubpartIds(List subpartIds) {
		this.subpartIds = subpartIds;
	}
	public List getReadOnlyClasses() {
		return readOnlyClasses;
	}
	public void setReadOnlyClasses(List readOnlyClasses) {
		this.readOnlyClasses = readOnlyClasses;
	}	
	public Long getInstrOfferingId() {
		return instrOfferingId;
	}
	public void setInstrOfferingId(Long instrOfferingId) {
		this.instrOfferingId = instrOfferingId;
	}
	public Long getAddTemplateClassId() {
		return addTemplateClassId;
	}
	public void setAddTemplateClassId(Long addTemplateClassId) {
		this.addTemplateClassId = addTemplateClassId;
	}

	public void removeFromClasses(String classId){
		removeClass(classId);
		initClassHasErrorsToFalse();
		setDirectionsClassesCanMove();
	}
	private void removeClass(String classId){
		ArrayList classesToDel = new ArrayList();
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
	    
	public void addToClasses(Class_ cls, Boolean isReadOnly, String indent, ClassAssignmentProxy proxy, String nameFormat){
		this.classLabels.add(cls.htmlLabel());
		this.classLabelIndents.add(indent);
		this.classIds.add(cls.getUniqueId().toString());
		this.subpartIds.add(cls.getSchedulingSubpart().getUniqueId().toString());
		this.itypes.add(cls.getSchedulingSubpart().getItype().getItype().toString());
		if (cls.getSchedulingSubpart().getChildSubparts() != null && cls.getSchedulingSubpart().getChildSubparts().size() > 0){
			this.mustHaveChildClasses.add(new Boolean(true));
		} else {
			this.mustHaveChildClasses.add(new Boolean(false));			
		}
		this.readOnlyClasses.add(isReadOnly.toString());
		this.classHasErrors.add(new Boolean(false).toString());	
		this.enrollments.add(StudentClassEnrollment.sessionHasEnrollments(cls.getSessionId())?(cls.getEnrollment()==null?"0":cls.getEnrollment().toString()):"");
		this.minClassLimits.add(cls.getExpectedCapacity().toString());
		this.numberOfRooms.add(cls.getNbrRooms().toString());
		this.displayInstructors.add(cls.isDisplayInstructor().toString());
		this.enabledForStudentScheduling.add(cls.isEnabledForStudentScheduling().toString());

		if(cls.getMaxExpectedCapacity() != null)
			this.maxClassLimits.add(cls.getMaxExpectedCapacity().toString());
		else
			this.maxClassLimits.add("");
		if(cls.getRoomRatio() != null)
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
			setDisplayMaxLimit(new Boolean(true));
		}
		if (getDisplayMaxLimit().booleanValue()){
			setDisplayOptionForMaxLimit(new Boolean(true));
		}
		this.times.add(cls.buildAssignedTimeHtml(proxy));
		this.rooms.add(cls.buildAssignedRoomHtml(proxy));
		this.instructors.add(cls.buildInstructorHtml(nameFormat));
		this.externalIds.add(cls.getClassSuffix() == null?"":cls.getClassSuffix());
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
		hm.put(ENROLLMENTS_TOKEN, this.getEnrollments());
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
		
		return(hm);
	}
	
	
	private Object getObjectFromListMapAtIndex(HashMap hm, String key, int index){
		if (hm == null || key == null || key.length() == 0 || index < 0){
			return(null);
		}
		List list = (List) hm.get(key);
		if (list == null || list.size() == 0) {
			return(null);
		}
		
		if (index >= list.size())
			return(null);
		else
			return(list.get(index));
	}
	private void addToClassesFromOrigClassesIndex(HashMap originalClassesMap, int classIndex){
		this.getClassIds().add((String) getObjectFromListMapAtIndex(originalClassesMap, CLASS_IDS_TOKEN, classIndex));
		this.getSubpartIds().add((String) getObjectFromListMapAtIndex(originalClassesMap, SUBPART_IDS_TOKEN, classIndex));
		this.getItypes().add((String) getObjectFromListMapAtIndex(originalClassesMap, ITYPES_TOKEN, classIndex));
		this.getMustHaveChildClasses().add((String) getObjectFromListMapAtIndex(originalClassesMap, MUST_HAVE_CHILD_CLASSES_TOKEN, classIndex));
		this.getClassLabels().add((String) getObjectFromListMapAtIndex(originalClassesMap, CLASS_LABELS_TOKEN, classIndex));
		this.getClassLabelIndents().add((String) getObjectFromListMapAtIndex(originalClassesMap, CLASS_LABEL_INDENTS_TOKEN, classIndex));
		this.getParentClassIds().add((String) getObjectFromListMapAtIndex(originalClassesMap, PARENT_CLASS_IDS_TOKEN, classIndex));
		this.getReadOnlyClasses().add((String) getObjectFromListMapAtIndex(originalClassesMap, READ_ONLY_CLASSES_TOKEN, classIndex));
		this.getEnrollments().add((String) getObjectFromListMapAtIndex(originalClassesMap, ENROLLMENTS_TOKEN, classIndex));
		this.getMinClassLimits().add((String) getObjectFromListMapAtIndex(originalClassesMap, MIN_CLASS_LIMITS_TOKEN, classIndex));
		this.getMaxClassLimits().add((String) getObjectFromListMapAtIndex(originalClassesMap, MAX_CLASS_LIMITS_TOKEN, classIndex));
		this.getRoomRatios().add((String) getObjectFromListMapAtIndex(originalClassesMap, ROOM_RATIOS_TOKEN, classIndex));
		this.getNumberOfRooms().add((String) getObjectFromListMapAtIndex(originalClassesMap, NUMBER_OF_ROOMS_TOKEN, classIndex));

		this.getDepartments().add((String) getObjectFromListMapAtIndex(originalClassesMap, DEPARTMENTS_TOKEN, classIndex));
		this.getDatePatterns().add((String) getObjectFromListMapAtIndex(originalClassesMap, DATE_PATTERNS_TOKEN, classIndex));
		this.getDisplayInstructors().add((String) getObjectFromListMapAtIndex(originalClassesMap, DISPLAY_INSTRUCTORS_TOKEN, classIndex));
		this.getEnabledForStudentScheduling().add((String) getObjectFromListMapAtIndex(originalClassesMap, ENABLED_FOR_STUDENT_SCHEDULING_TOKEN, classIndex));
		this.getSubtotalIndexes().add((String) getObjectFromListMapAtIndex(originalClassesMap, SUBTOTAL_INDEXES_TOKEN, classIndex));
		this.getTimes().add((String) getObjectFromListMapAtIndex(originalClassesMap, TIMES_TOKEN, classIndex));
		this.getRooms().add((String) getObjectFromListMapAtIndex(originalClassesMap, ROOMS_TOKEN, classIndex));
		this.getInstructors().add((String) getObjectFromListMapAtIndex(originalClassesMap, INSTRUCTORS_TOKEN, classIndex));
		this.getExternalIds().add((String) getObjectFromListMapAtIndex(originalClassesMap, EXTERNAL_IDS_TOKEN, classIndex));
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
		addNewClassesBasedOnTemplate(clsId, nextTmpClassId((List) origListsMap.get(CLASS_IDS_TOKEN)).toString(), (parentClassId.length() == 0)?null:parentClassId);			
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
		this.mustHaveChildClasses.add(this.getMustHaveChildClasses().get(index).toString());
		this.parentClassIds.add((parentClassId != null)?parentClassId.toString():"");
		this.readOnlyClasses.add(new Boolean(false).toString());
		this.enrollments.add("");
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
		ArrayList childClasses = new ArrayList();
		for(int i = (index + 1); i < this.getClassIds().size(); i++){
			if (this.getParentClassIds().get(i).toString().equals(clsId))
				childClasses.add(this.getClassIds().get(i));
		}
		for(Iterator it = childClasses.iterator(); it.hasNext();){
			addNewClassesBasedOnTemplate(it.next().toString(), nextTmpClassId(null).toString(), tmpClassId);
		}
	}
	
	private Long nextTmpClassId(List origClassIds){
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
		return (new Long(nextId));
	}

	public Long getDeletedClassId() {
		return deletedClassId;
	}

	public void setDeletedClassId(Long deletedClassId) {
		this.deletedClassId = deletedClassId;
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

	public List getClassLabelIndents() {
		return classLabelIndents;
	}

	public void setClassLabelIndents(List classLabelIndents) {
		this.classLabelIndents = classLabelIndents;
	}

	public List getRoomRatios() {
		return roomRatios;
	}

	public void setRoomRatios(List roomRatios) {
		this.roomRatios = roomRatios;
	}

	public List getEnabledForStudentScheduling() {
		return enabledForStudentScheduling;
	}

	public void setEnabledForStudentScheduling(List enabledForStudentScheduling) {
		this.enabledForStudentScheduling = enabledForStudentScheduling;
	}

	public List getDisplayInstructors() {
		return displayInstructors;
	}

	public void setDisplayInstructors(List displayInstructors) {
		this.displayInstructors = displayInstructors;
	}

	public List getClassCanMoveDown() {
		return classCanMoveDown;
	}

	public void setClassCanMoveDown(List classCanMoveDown) {
		this.classCanMoveDown = classCanMoveDown;
	}

	public List getClassCanMoveUp() {
		return classCanMoveUp;
	}

	public void setClassCanMoveUp(List classCanMoveUp) {
		this.classCanMoveUp = classCanMoveUp;
	}

	public List getItypes() {
		return itypes;
	}

	public void setItypes(List itypes) {
		this.itypes = itypes;
	}

	public Long getMoveUpClassId() {
		return moveUpClassId;
	}

	public void setMoveUpClassId(Long moveUpClassId) {
		this.moveUpClassId = moveUpClassId;
	}

	public Long getMoveDownClassId() {
		return moveDownClassId;
	}

	public void setMoveDownClassId(Long moveDownClassId) {
		this.moveDownClassId = moveDownClassId;
	}

	public List getMustHaveChildClasses() {
		return mustHaveChildClasses;
	}

	public void setMustHaveChildClasses(List mustHaveChildClasses) {
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

	public List getSubtotalIndexes() {
		return subtotalIndexes;
	}

	public void setSubtotalIndexes(List subtotalIndexes) {
		this.subtotalIndexes = subtotalIndexes;
	}

	public List getSubtotalLabels() {
		return subtotalLabels;
	}

	public void setSubtotalLabels(List subtotalLabels) {
		this.subtotalLabels = subtotalLabels;
	}

	public List getSubtotalValues() {
		return subtotalValues;
	}

	public void setSubtotalValues(List subtotalValues) {
		this.subtotalValues = subtotalValues;
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

	public List getEnableAllClassesForStudentSchedulingForSubpart() {
		return enableAllClassesForStudentSchedulingForSubpart;
	}

	public void setEnableAllClassesForStudentSchedulingForSubpart(
			List enableAllClassesForStudentSchedulingForSubpart) {
		this.enableAllClassesForStudentSchedulingForSubpart = enableAllClassesForStudentSchedulingForSubpart;
	}

	public List getDisplayAllClassesInstructorsForSubpart() {
		return displayAllClassesInstructorsForSubpart;
	}

	public void setDisplayAllClassesInstructorsForSubpart(
			List displayAllClassesInstructorsForSubpart) {
		this.displayAllClassesInstructorsForSubpart = displayAllClassesInstructorsForSubpart;
	}

	public List getReadOnlySubparts() {
		return readOnlySubparts;
	}

	public void setReadOnlySubparts(List readOnlySubparts) {
		this.readOnlySubparts = readOnlySubparts;
	}

	public List getEnrollments() {
		return enrollments;
	}

	public void setEnrollments(List enrollments) {
		this.enrollments = enrollments;
	}

	public Boolean getDisplayEnrollment() {
		return displayEnrollment;
	}

	public void setDisplayEnrollment(Boolean displayEnrollment) {
		this.displayEnrollment = displayEnrollment;
	}

	public List getTimes() {
		return times;
	}

	public void setTimes(List times) {
		this.times = times;
	}

	public List getRooms() {
		return rooms;
	}

	public void setRooms(List rooms) {
		this.rooms = rooms;
	}

	public List getInstructors() {
		return instructors;
	}

	public void setInstructors(List instructors) {
		this.instructors = instructors;
	}

	public Boolean getDisplayExternalId() {
		return displayExternalId;
	}

	public void setDisplayExternalId(Boolean displayExternalId) {
		this.displayExternalId = displayExternalId;
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

	public List getExternalIds() {
		return externalIds;
	}

	public void setExternalIds(List externalIds) {
		this.externalIds = externalIds;
	}

}
