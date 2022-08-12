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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.util.DynamicList;
import org.unitime.timetable.util.DynamicListObjectFactory;


/** 
 * @author Tomas Muller
 */
public class DistributionPrefsForm implements UniTimeForm {
	private static final long serialVersionUID = 6316876654471770646L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	// --------------------------------------------------------- Class Variables
    public static final String SUBJ_AREA_ATTR_LIST = "subjectAreaList";
    public static final String CRS_NUM_ATTR_LIST = "courseNbrList";
    public static final String ITYPE_ATTR_LIST = "itypeList";
    public static final String CLASS_NUM_ATTR_LIST = "classNumList";
    public static final String ALL_CLASSES_SELECT = "-1";
    public static final String LIST_SIZE_ATTR = "listSize";

    // --------------------------------------------------------- Instance Variables

    /** op property */
    private String op;

    /** distributionDesc property */
    private String distType;

    /** prefLevel property */
    private String prefLevel;

    /** owner property */
    private String owner;
    
    /** distribution pref uniqueid **/
    private String distPrefId;
    
    /** distribution objects **/
    private List<String> subjectArea;
    private List<String> courseNbr;
    private List<String> itype;
    private List<String> classNumber;
    
    private String description;
    private String groupingDescription;
    
    private String grouping;
    
	private String filterSubjectAreaId;
	private Collection filterSubjectAreas;
	private String filterCourseNbr;
    

    // --------------------------------------------------------- Classes

    /** Factory to create dynamic list element for Distribution Objects */
    protected DynamicListObjectFactory factoryDistObj;
    
    public DistributionPrefsForm() {
    	factoryDistObj = new DynamicListObjectFactory() {
            public Object create() {
                return Preference.BLANK_PREF_VALUE;
            };
    	};
    	reset();
    }

    // --------------------------------------------------------- Methods

    /** 
     * Method validate
     */
    public void validate(UniTimeAction action) {
        // Distribution Type must be selected
        if (distType==null || distType.equals(Preference.BLANK_PREF_VALUE)) {
        	action.addFieldError("distType", MSG.errorSelectDistributionType());
        }
        
        // Distribution Pref Level must be selected
        if (prefLevel==null || prefLevel.equals(Preference.BLANK_PREF_VALUE)) {
        	action.addFieldError("prefLevel", MSG.errorSelectDistributionPreferenceLevel());
        }
        
        // Check duplicate / blank selections
        if (!checkClasses()) {
        	action.addFieldError("classes", MSG.errorInvalidClassSelectionDP());
        }
        
        // Save/Update clicked
        if (op.equals(MSG.accessSaveNewDistributionPreference()) || op.equals(MSG.accessUpdateDistributionPreference())) {
        	
        	// At least one row of subpart should exist
            if (subjectArea.size()==0)
            	action.addFieldError("classes", MSG.errorInvalidClassSelectionDPSubpart());

            // At least 2 rows should exist if one is a class
            if (subjectArea.size()==1 && !classNumber.get(0).toString().equals(ALL_CLASSES_SELECT))
            	action.addFieldError("classes", MSG.errorInvalidClassSelectionDPMinTwoClasses());
            
            // Class cannot be specified if its subpart is already specified
            if (subjectArea.size()>1) {
                HashMap mapSubparts = new HashMap();
                HashMap mapClasses = new HashMap();
                for (int i=0; i<subjectArea.size(); i++) {
                    String subpart = itype.get(i).toString();
                    String classNum = classNumber.get(i).toString();
                    if(classNum.equals(ALL_CLASSES_SELECT)) {
                        if(mapClasses.get(subpart)!=null) {
                        	action.addFieldError("classes", MSG.errorInvalidClassSelectionDPIndividualClass());
                	        break;
                        }
                        else 
                            mapSubparts.put(subpart, classNum);
                    }
                    else {
                        if(mapSubparts.get(subpart)!=null) {
                        	action.addFieldError("classes", MSG.errorInvalidClassSelectionDPIndividualClass());
                	        break;
                        }
                        else 
                            mapClasses.put(subpart, classNum);
                    }
                }
            }
        }
    }

    /**
     * Check that classes are not blank and are valid
     * @return
     */
    public boolean checkClasses() {
        
        HashMap map = new HashMap();
        for(int i=0; i<subjectArea.size(); i++) {
            // Check Blanks
            if(subjectArea.get(i)==null || subjectArea.get(i).toString().equals(Preference.BLANK_PREF_VALUE))
                return false;
            if(courseNbr.get(i)==null || courseNbr.get(i).toString().equals(Preference.BLANK_PREF_VALUE))
                return false;
            if(itype.get(i)==null || itype.get(i).toString().equals(Preference.BLANK_PREF_VALUE))
                return false;
            if(classNumber.get(i)==null || classNumber.get(i).toString().equals(Preference.BLANK_PREF_VALUE))
                return false;
            
            // Check Duplicates
            String str = subjectArea.get(i).toString() + courseNbr.get(i).toString() 
            				+ itype.get(i).toString() + classNumber.get(i).toString();
            if(map.get(str)!=null)
                return false;
            
            map.put(str, "1");
        }
        
        return true;
    }

    
    /** 
     * Method reset
     */
    public void reset() {
        op="";
        distPrefId="";
        distType=Preference.BLANK_PREF_VALUE;
        prefLevel=Preference.BLANK_PREF_VALUE;
        owner="";
        description="";
        groupingDescription="";
        subjectArea = DynamicList.getInstance(new ArrayList(), factoryDistObj);    
        courseNbr = DynamicList.getInstance(new ArrayList(), factoryDistObj);    
        itype = DynamicList.getInstance(new ArrayList(), factoryDistObj);    
        classNumber = DynamicList.getInstance(new ArrayList(), factoryDistObj);    
        grouping = Preference.BLANK_PREF_VALUE;
        filterSubjectAreaId = null;
        filterCourseNbr = null; 
        filterSubjectAreas = new ArrayList();
    }

    
    /**
     * @return Returns the distPrefId.
     */
    public String getDistPrefId() {
        return distPrefId;
    }
    
    /**
     * @param distPrefId The distPrefId to set.
     */
    public void setDistPrefId(String distPrefId) {
        this.distPrefId = distPrefId;
    }
    
    /**
     * @return Returns the op.
     */
    public String getOp() {
        return op;
    }
    /**
     * @param op The op to set.
     */
    public void setOp(String op) {
        this.op = op;
    }
    
    /** 
     * Returns the prefLevel.
     * @return String
     */
    public String getPrefLevel() {
        return prefLevel;
    }

    /** 
     * Set the prefLevel.
     * @param prefLevel The prefLevel to set
     */
    public void setPrefLevel(String prefLevel) {
        this.prefLevel = prefLevel;
    }

    /** 
     * Returns the distType.
     * @return String
     */
    public String getDistType() {
        return distType;
    }

    /** 
     * Set the distType.
     * @param distType The distType to set
     */
    public void setDistType(String distType) {
        this.distType = distType;
    }

    /**
     * @return Returns the owner.
     */
    public String getOwner() {
        return owner;
    }
    /**
     * @param owner The owner to set.
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }
    
    /**
     * @return Returns the subjectArea.
     */
    public List<String> getSubjectArea() {
        return subjectArea;
    }
    
    /**
     * @param subjectArea The subjectArea to set.
     */
    public void setSubjectArea(List<String> subjectArea) {
        this.subjectArea = subjectArea;
    }
    
    /**
     * @return Returns the subjectArea.
     */
    public String getSubjectArea(int key) {
        return subjectArea.get(key);
    }
    /**
     * @param key The key to set.
     * @param value The value to set.
     */
    public void setSubjectArea(int key, String value) {
        this.subjectArea.set(key, value);
    }

    /**
     * @param value
     */
    public void addToSubjectArea(String value) {
        this.subjectArea.add(value);
    }
    
    /**
     * @return Returns the courseNbr.
     */
    public List<String> getCourseNbr() {
        return courseNbr;
    }
    
    /**
     * @param courseNbr The courseNbr to set.
     */
    public void setCourseNbr(List<String> courseNbr) {
        this.courseNbr = courseNbr;
    }
    
    /**
     * @return Returns the courseNbr.
     */
    public String getCourseNbr(int key) {
        return courseNbr.get(key);
    }
    /**
     * @param key The key to set.
     * @param value The value to set.
     */
    public void setCourseNbr(int key, String value) {
        this.courseNbr.set(key, value);
    }

    /**
     * @param value
     */
    public void addToCourseNbr(String value) {
        this.courseNbr.add(value);
    }
    
    /**
     * @return Returns the itype.
     */
    public List<String> getItype() {
        return itype;
    }
    
    /**
     * @param itype The itype to set.
     */
    public void setItype(List<String> itype) {
        this.itype = itype;
    }
    
    /**
     * @return Returns the itype.
     */
    public String getItype(int key) {
        return itype.get(key);
    }
    /**
     * @param key The key to set.
     * @param value The value to set.
     */
    public void setItype(int key, String value) {
        this.itype.set(key, value);
    }

    /**
     * @param value
     */
    public void addToItype(String value) {
        this.itype.add(value);
    }
    
    /**
     * @return Returns the classNumber.
     */
    public List<String> getClassNumber() {
        return classNumber;
    }
    
    /**
     * @param classNumber The classNumber to set.
     */
    public void setClassNumber(List<String> classNumber) {
        this.classNumber = classNumber;
    }
    
    /**
     * @return Returns the classNumber.
     */
    public String getClassNumber(int key) {
        return classNumber.get(key);
    }
    /**
     * @param key The key to set.
     * @param value The value to set.
     */
    public void setClassNumber(int key, String value) {
        this.classNumber.set(key, value);
    }

    /**
     * @param value
     */
    public void addToClassNumber(String value) {
        this.classNumber.add(value);
    }
    
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getGroupingDescription() {
		return groupingDescription;
	}
	public void setGroupingDescription(String groupingDescription) {
		this.groupingDescription = groupingDescription;
	}

	public String getGrouping() { return grouping; }
	public DistributionPref.Structure getStructure() {
		for (DistributionPref.Structure structure: DistributionPref.Structure.values()) {
			if (structure.getName().equals(grouping)) return structure;
		}
		return DistributionPref.Structure.AllClasses;
	}
	public void setGrouping(String grouping) { this.grouping = grouping; }
	public void setStructure(DistributionPref.Structure structure) { this.grouping = (structure == null ? DistributionPref.Structure.AllClasses.getName() : structure.getName()); }
	public String[] getGroupings() {
		String[] ret = new String[DistributionPref.Structure.values().length];
		for (int i = 0; i < DistributionPref.Structure.values().length; i++)
			ret[i] = DistributionPref.Structure.values()[i].getName();
		return ret;
	}

    /**
     * @param subjectAreaId Subject area of the new class (optional, null otherwise)
     * Add a blank row
     */
    public void addNewClass(String subjectAreaId) {
        if(subjectAreaId==null || subjectAreaId.trim().length()==0)
            addToSubjectArea(Preference.BLANK_PREF_VALUE);
        else
            addToSubjectArea(subjectAreaId);
        
        addToCourseNbr(Preference.BLANK_PREF_VALUE);
        addToItype(Preference.BLANK_PREF_VALUE);
        addToClassNumber(Preference.BLANK_PREF_VALUE);
    }
    
    /**
     * Remove object specified by the index from the lists
     * @param key
     */
    public void removeFromLists(int key) {
        this.subjectArea.remove(key);
        this.courseNbr.remove(key);
        this.itype.remove(key);
        this.classNumber.remove(key);
    }

    /**
     * Swaps two list elements for the specified indexes
     * @param index
     * @param index2
     */
    public void swap(int index, int index2) {
    	String objSa = subjectArea.get(index);
        String objCo = courseNbr.get(index);
        String objIt = itype.get(index);
        String objCl = classNumber.get(index);
        
        String objSa2 = subjectArea.get(index2);
        String objCo2 = courseNbr.get(index2);
        String objIt2 = itype.get(index2);
        String objCl2 = classNumber.get(index2);
        
        subjectArea.set(index, objSa2);
        subjectArea.set(index2, objSa);
        courseNbr.set(index, objCo2);
        courseNbr.set(index2, objCo);
        itype.set(index, objIt2);
        itype.set(index2, objIt);
        classNumber.set(index, objCl2);
        classNumber.set(index2, objCl);
    }
    
    public String getFilterSubjectAreaId() { return filterSubjectAreaId; }
    public void setFilterSubjectAreaId(String filterSubjectAreaId) { this.filterSubjectAreaId = filterSubjectAreaId; }
    public String getFilterCourseNbr() { return filterCourseNbr; }
    public void setFilterCourseNbr(String filterCourseNbr) { this.filterCourseNbr = filterCourseNbr; }
    public Collection getFilterSubjectAreas() { return filterSubjectAreas; }
    public void setFilterSubjectAreas(Collection filterSubjectAreas) { this.filterSubjectAreas = filterSubjectAreas;}
}
