/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.util.MessageResources;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.util.DynamicList;
import org.unitime.timetable.util.DynamicListObjectFactory;


/** 
 * MyEclipse Struts
 * Creation date: 12-14-2005
 * 
 * XDoclet definition:
 * @struts:form name="distributionPrefsForm"
 */
public class DistributionPrefsForm extends ActionForm {

	private static final long serialVersionUID = 6316876654471770646L;
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
    private List subjectArea;
    private List courseNbr;
    private List itype;
    private List classNumber;
    
    private String description;
    private String groupingDescription;
    
    private String grouping;
    
	private String filterSubjectAreaId;
	private Collection filterSubjectAreas;
	private String filterCourseNbr;
    

    // --------------------------------------------------------- Classes

    /** Factory to create dynamic list element for Distribution Objects */
    protected DynamicListObjectFactory factoryDistObj = new DynamicListObjectFactory() {
        public Object create() {
            return Preference.BLANK_PREF_VALUE;
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

        // Distribution Type must be selected
        if(distType==null || distType.equals(Preference.BLANK_PREF_VALUE)) {
	        errors.add("distType", 
	                new ActionMessage(
	                        "errors.generic", "Select a distribution type. ") );
        }
        
        /*
        // Distribution Type must be selected
        if(grouping==null || grouping.equals(Preference.BLANK_PREF_VALUE)) {
	        errors.add("grouping", 
	                new ActionMessage(
	                        "errors.generic", "Select a structure. ") );
        }
        */

        // Distribution Pref Level must be selected
        if(prefLevel==null || prefLevel.equals(Preference.BLANK_PREF_VALUE)) {
	        errors.add("prefLevel", 
	                new ActionMessage(
	                        "errors.generic", "Select a preference level. ") );
        }
        
        // Check duplicate / blank selections
        if(!checkClasses()) {
	        errors.add("classes", 
	                new ActionMessage(
	                        "errors.generic", 
	                        "Invalid class selections: Check for duplicate / blank selection. ") );
        }
        
        // Save/Update clicked
        if(op.equals(rsc.getMessage("button.addNew"))
                || op.equals(rsc.getMessage("button.update")) ) {
            
            // At least one row of subpart should exist
            if(subjectArea.size()==0)
    	        errors.add("classes", 
    	                new ActionMessage(
    	                        "errors.generic", "Invalid class selections: Select at least one subpart. ") );

            // At least 2 rows should exist if one is a class
            if(subjectArea.size()==1 && !classNumber.get(0).toString().equals(ALL_CLASSES_SELECT))
    	        errors.add("classes", 
    	                new ActionMessage(
    	                        "errors.generic", "Invalid class selections: Select at least two classes. ") );
            
            // Class cannot be specified if its subpart is already specified
            if(subjectArea.size()>1) {
                HashMap mapSubparts = new HashMap();
                HashMap mapClasses = new HashMap();
                for (int i=0; i<subjectArea.size(); i++) {
                    String subpart = itype.get(i).toString();
                    String classNum = classNumber.get(i).toString();
                    if(classNum.equals(ALL_CLASSES_SELECT)) {
                        if(mapClasses.get(subpart)!=null) {
                	        errors.add("classes", 
                	                new ActionMessage(
                	                        "errors.generic", 
                	                        "Invalid class selections: An individual class cannot be selected if the entire subpart has been selected . ") );
                	        break;
                        }
                        else 
                            mapSubparts.put(subpart, classNum);
                    }
                    else {
                        if(mapSubparts.get(subpart)!=null) {
                	        errors.add("classes", 
                	                new ActionMessage(
                	                        "errors.generic", 
                	                        "Invalid class selections: An individual class cannot be selected if the entire subpart has been selected . ") );
                	        break;
                        }
                        else 
                            mapClasses.put(subpart, classNum);
                    }
                }
            }
        }

        return errors;
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
     * @param mapping
     * @param request
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) {
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
    public List getSubjectArea() {
        return subjectArea;
    }
    
    /**
     * @param subjectArea The subjectArea to set.
     */
    public void setSubjectArea(List subjectArea) {
        this.subjectArea = subjectArea;
    }
    
    /**
     * @return Returns the subjectArea.
     */
    public String getSubjectArea(int key) {
        return subjectArea.get(key).toString();
    }
    /**
     * @param key The key to set.
     * @param value The value to set.
     */
    public void setSubjectArea(int key, Object value) {
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
    public List getCourseNbr() {
        return courseNbr;
    }
    
    /**
     * @param courseNbr The courseNbr to set.
     */
    public void setCourseNbr(List courseNbr) {
        this.courseNbr = courseNbr;
    }
    
    /**
     * @return Returns the courseNbr.
     */
    public String getCourseNbr(int key) {
        return courseNbr.get(key).toString();
    }
    /**
     * @param key The key to set.
     * @param value The value to set.
     */
    public void setCourseNbr(int key, Object value) {
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
    public List getItype() {
        return itype;
    }
    
    /**
     * @param itype The itype to set.
     */
    public void setItype(List itype) {
        this.itype = itype;
    }
    
    /**
     * @return Returns the itype.
     */
    public String getItype(int key) {
        return itype.get(key).toString();
    }
    /**
     * @param key The key to set.
     * @param value The value to set.
     */
    public void setItype(int key, Object value) {
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
    public List getClassNumber() {
        return classNumber;
    }
    
    /**
     * @param classNumber The classNumber to set.
     */
    public void setClassNumber(List classNumber) {
        this.classNumber = classNumber;
    }
    
    /**
     * @return Returns the classNumber.
     */
    public String getClassNumber(int key) {
        return classNumber.get(key).toString();
    }
    /**
     * @param key The key to set.
     * @param value The value to set.
     */
    public void setClassNumber(int key, Object value) {
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
	public int getGroupingInt() {
		for (int i=0;i<DistributionPref.sGroupings.length;i++)
			if (DistributionPref.sGroupings[i].equals(grouping)) return i;
		return DistributionPref.sGroupingNone;
	}
	public void setGrouping(String grouping) { this.grouping = grouping; }
	public void setGroupingInt(int grouping) { this.grouping = DistributionPref.sGroupings[grouping]; }
	public String[] getGroupings() { return DistributionPref.sGroupings; }

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
        Object objSa = subjectArea.get(index);
        Object objCo = courseNbr.get(index);
        Object objIt = itype.get(index);
        Object objCl = classNumber.get(index);
        
        Object objSa2 = subjectArea.get(index2);
        Object objCo2 = courseNbr.get(index2);
        Object objIt2 = itype.get(index2);
        Object objCl2 = classNumber.get(index2);
        
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
