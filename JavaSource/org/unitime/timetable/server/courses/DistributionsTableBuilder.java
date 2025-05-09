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
package org.unitime.timetable.server.courses;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.FilterInterface;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.ClassInstructorComparator;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

public class DistributionsTableBuilder extends TableBuilder {
	
	public DistributionsTableBuilder(SessionContext context, String backType, String backId) {
    	super(context, backType, backId);
    }
	
	public TableInterface getDistPrefsTableForInstructionalOffering(InstructionalOffering instructionalOffering) {

		Set<DepartmentalInstructor> leadInstructors = new HashSet<DepartmentalInstructor>();
		Set<DistributionPref> prefs = new TreeSet<DistributionPref>();
		for (InstrOfferingConfig config: instructionalOffering.getInstrOfferingConfigs()) {
			for (SchedulingSubpart subpart: config.getSchedulingSubparts()) {
				prefs.addAll(subpart.getDistributionPreferences());
				for (Class_ clazz: subpart.getClasses()) {
					prefs.addAll(clazz.getDistributionPreferences());
					leadInstructors.addAll(clazz.getLeadInstructors());
				}
			}
		}

		for (DepartmentalInstructor instructor: leadInstructors) {
			prefs.addAll(instructor.getDistributionPreferences());
		}
		
		return createTableForDistributions(prefs); 
	}
	
	public TableInterface getDistPrefsTableForSchedulingSupart(SchedulingSubpart subpart) {

		Set<DepartmentalInstructor> leadInstructors = new HashSet<DepartmentalInstructor>();
		Set<DistributionPref> prefs = new TreeSet<DistributionPref>();
		prefs.addAll(subpart.getDistributionPreferences());
		for (Class_ clazz: subpart.getClasses()) {
			prefs.addAll(clazz.getDistributionPreferences());
			leadInstructors.addAll(clazz.getLeadInstructors());
		}

		for (DepartmentalInstructor instructor: leadInstructors) {
			prefs.addAll(instructor.getDistributionPreferences());
		}
		
		return createTableForDistributions(prefs); 
	}
	
	public TableInterface getDistPrefsTableForClass(Class_ clazz) {

		Set<DepartmentalInstructor> leadInstructors = new HashSet<DepartmentalInstructor>();
		Set<DistributionPref> prefs = new TreeSet<DistributionPref>();
		prefs.addAll(clazz.getSchedulingSubpart().getDistributionPreferences());
		prefs.addAll(clazz.getDistributionPreferences());
		leadInstructors.addAll(clazz.getLeadInstructors());

		for (DepartmentalInstructor instructor: leadInstructors) {
			prefs.addAll(instructor.getDistributionPreferences());
		}
		
		return createTableForDistributions(prefs); 
	}
	
	public TableInterface getDistPrefsTableForFilter(FilterInterface filter, Long subjAreaId) {
		Set<DistributionPref> prefs = new TreeSet<DistributionPref>();
		
		String courseNbr = filter.getParameterValue("courseNbr");
		for (Department d: Department.getUserDepartments(getSessionContext().getUser())) {
            prefs.addAll(DistributionPref.getPreferences(getSessionContext().getUser().getCurrentAcademicSessionId(),
            		d.getUniqueId(), true, null, subjAreaId, (courseNbr==null || courseNbr.length()==0 ? null : courseNbr)));
            prefs.addAll(DistributionPref.getInstructorPreferences(getSessionContext().getUser().getCurrentAcademicSessionId(),
            		d.getUniqueId(),subjAreaId, (courseNbr==null || courseNbr.length()==0 ? null : courseNbr)));
        }
		
		String prefLevel = filter.getParameterValue("prefLevel");
		String distType = filter.getParameterValue("distType");
		String structure = filter.getParameterValue("structure");
		for (Iterator<DistributionPref> i = prefs.iterator(); i.hasNext(); ) {
			DistributionPref dp = i.next();
			if (prefLevel != null && !prefLevel.isEmpty()) {
				boolean match = false;
				for (String prefId: prefLevel.split(","))
					if (dp.getPrefLevel().getUniqueId().toString().equals(prefId)) {
						match = true;
						break;
					}
				if (!match) {
					i.remove();
					continue;
				}
			}
			if (distType != null && !distType.isEmpty()) {
				boolean match = false;
				for (String typeId: distType.split(","))
					if (dp.getDistributionType().getUniqueId().toString().equals(typeId)) {
						match = true;
						break;
					}
				if (!match) {
					i.remove();
					continue;
				}
			}
			if (structure != null && !structure.isEmpty()) {
				boolean match = false;
				for (String structureName: structure.split(",")) {
					if ("instructor".equals(structureName) && dp.getStructure() == null) {
						match = true;
						break;
					} else if (dp.getStructure() != null && dp.getStructure().name().equals(structureName)) {
						match = true;
						break;
					}
				}
				if (!match) {
					i.remove();
					continue;
				}
			}
		}
		
		return createTableForDistributions(prefs); 
	}
	
    public TableInterface createTableForDistributions(Collection<DistributionPref> distPrefs) {
    	TableInterface table = new TableInterface();
    	table.setName(MSG.sectionTitleDistributionPreferences());
    	
        LineInterface header = table.addHeader();
        if (isSimple()) header.addCell(MSG.columnDistrPrefLevel());
        header.addCell(MSG.columnDistrPrefType());
        header.addCell(MSG.columnDistrPrefStructure());
        header.addCell(MSG.columnDistrPrefOwner());
        header.addCell(MSG.columnDistrPrefClass());
    	for (CellInterface cell: header.getCells()) {
    		cell.setClassName("WebTableHeader");
    		cell.setText(cell.getText().replace("<br>", "\n"));
    		cell.addStyle("white-space: pre-wrap;");
    	}


        int nrPrefs = 0;
        
        boolean suffix = ApplicationProperty.DistributionsShowClassSufix.isTrue();

        for (DistributionPref dp: distPrefs) {
        	if (!getSessionContext().hasPermission(dp, Right.DistributionPreferenceDetail)) continue;

        	nrPrefs++;
        	
        	CellInterface obj = new CellInterface().setNoWrap(true);
        	CellInterface ownerType = new CellInterface().setText(MSG.ownerUnknown());
        	
        	PreferenceGroup pg = dp.getOwner();
        	
        	if (pg instanceof Department) {
        		Department d = (Department)pg;
        		ownerType.setText(d.getShortLabel()).setTitle(d.getHtmlTitle());
        	}
        	
        	for (DistributionObject dO: dp.getOrderedSetOfDistributionObjects())
        		obj.add(dO.preferenceText(suffix)).setInline(false);

            String groupingText = dp.getStructureName();

            if (pg instanceof DepartmentalInstructor) {
        		DepartmentalInstructor instructor = (DepartmentalInstructor)pg;
        		Set<Department> owners = new TreeSet<Department>();
        		TreeSet<ClassInstructor> classes = new TreeSet(new ClassInstructorComparator(new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY)));
        		classes.addAll(instructor.getClasses());
        		for (ClassInstructor clazz: classes) {
        			if (!clazz.isLead().booleanValue()) continue;
        			obj.add(clazz.getClassInstructing().getClassLabel(suffix)).setInline(false).setNoWrap(true);
        			Department dept = clazz.getClassInstructing().getManagingDept();
            		if (dept.isInheritInstructorPreferences()) owners.add(dept);
        		}
        		ownerType.setText((String)null);
        		for (Department owner: owners)
        			ownerType.add(owner.getShortLabel()).setTitle(owner.getHtmlTitle()).setInline(false).setNoWrap(true);
        		groupingText = MSG.columnInstructor() + " "+instructor.getName(getInstructorNameFormat());
        		if (owners.isEmpty()) continue;
        	}
             
        	String distType = dp.getDistributionType().getLabel();
            String prefLevel = dp.getPrefLevel().getPrefName();
            String prefColor = dp.getPrefLevel().prefcolor();
        	if (PreferenceLevel.sNeutral.equals(dp.getPrefLevel().getPrefProlog()))
        		prefColor = "#808080";
            
            LineInterface line = table.addLine();
            
            if (pg instanceof DepartmentalInstructor) {
            	if (getSessionContext().hasPermission(pg, Right.InstructorDetail))
            		line.setURL("instructorDetail.action?instructorId=" + dp.getOwner().getUniqueId() + "&op=Show%20Instructor%20Preferences");
            } else {
            	if (getSessionContext().hasPermission(dp, Right.DistributionPreferenceEdit))
            		line.setURL("distributionPrefs.action?dp=" + dp.getUniqueId() + "&op=view");
            }
            
            if ("PreferenceGroup".equals(getBackType()) && dp.getUniqueId().toString().equals(getBackId()))
            	line.setAnchor("back");
            
            if (isSimple()) line.addCell(prefLevel).setColor(prefColor).setNoWrap(true);
            line.addCell(distType).setColor(prefColor).setTitle(prefLevel + " " + distType).setAria(prefLevel + " " + distType).setNoWrap(true);
            line.addCell(groupingText).setNoWrap(true);
            line.addCell(ownerType);
            line.addCell(obj);
        }
        
        if (nrPrefs==0)
        	table.addLine().addCell(MSG.noPreferencesFound()).setColSpan(4);
        
        return table;
    }
}
