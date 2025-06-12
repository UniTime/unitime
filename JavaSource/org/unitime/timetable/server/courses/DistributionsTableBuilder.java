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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.query.Query;
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
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.ClassInstructorComparator;
import org.unitime.timetable.model.dao.DistributionPrefDAO;
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
	
	public TableInterface getDistPrefsTableForExam(Exam exam) {
		return createTableForExamDistributions(new TreeSet<DistributionPref>(exam.effectivePreferences(DistributionPref.class))); 
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
    	table.setId("Distributions");
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
            	if (getSessionContext().hasPermission(dp, Right.DistributionPreferenceEdit)) {
            		if (ApplicationProperty.LegacyDistributions.isTrue())
            			line.setURL("distributionPrefs.action?dp=" + dp.getUniqueId() + "&op=view");
            		else
            			line.setURL("distributionEdit?id=" + dp.getUniqueId());
            	}
            }
            
            if (isSimple()) line.addCell(prefLevel).setColor(prefColor).setNoWrap(true);
            line.addCell(distType).setColor(prefColor)
            	.setTitle(prefLevel + " " + distType)
            	.setAria(isSimple() ? distType : prefLevel + " " + distType)
            	.setNoWrap(true)
            	.setComparable(distType);
            line.addCell(groupingText).setNoWrap(true);
            line.addCell(ownerType);
            line.addCell(obj);
            
            if ("DistributionPref".equals(getBackType()) && dp.getUniqueId().toString().equals(getBackId()))
            	line.getCells().get(0).addAnchor("back");
            else if ("PreferenceGroup".equals(getBackType()) && dp.getOwner().getUniqueId().toString().equals(getBackId()))
            	line.getCells().get(0).addAnchor("back");
        }
        
        if (nrPrefs==0)
        	table.addLine().addCell(MSG.noPreferencesFound()).setColSpan(4);
        
        return table;
    }
    
    public TableInterface getExamDistPrefsTableForFilter(FilterInterface filter, ExamType examType) {
		String[] subjectAreas = filter.getParameterValue("subjectArea").split(",");
		String courseNbr = filter.getParameterValue("courseNbr");
		
		String query = "select distinct dp from DistributionPref dp " +
	            "inner join dp.distributionObjects do, Exam x inner join x.owners o " +
	            "where dp.distributionType.examPref = true and x.session.uniqueId = :sessionId " +
	            "and do.prefGroup = x and x.examType.uniqueId = :examTypeId";
		boolean hasCourseNbr = false;
		if (courseNbr != null && !courseNbr.trim().isEmpty()) {
			query += " and o.course.courseNbr like :courseNbr";
			hasCourseNbr = true;
		}
		boolean hasSubjectAreas = false;
		if (subjectAreas.length > 0 && !"-1".equals(subjectAreas[0])) {
			hasSubjectAreas = true;
			query += " and cast(o.course.subjectArea.uniqueId as string) in :subjectAreas";
		}
		
		Query<DistributionPref> q = DistributionPrefDAO.getInstance().getSession().createQuery(query, DistributionPref.class)
	            .setParameter("sessionId", getSessionContext().getUser().getCurrentAcademicSessionId())
	    		.setParameter("examTypeId", examType.getUniqueId());
		
		if (hasCourseNbr)
			q.setParameter("courseNbr", courseNbr.replace("*", "%"));
		if (hasSubjectAreas)
			q.setParameterList("subjectAreas", subjectAreas);
			
	    List<DistributionPref> prefs = q.setCacheable(true).list();
		
		String prefLevel = filter.getParameterValue("prefLevel");
		String distType = filter.getParameterValue("distType");
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
		}
		
		return createTableForExamDistributions(prefs); 
	}
    
    public TableInterface createTableForExamDistributions(Collection<DistributionPref> distPrefs) {
    	TableInterface table = new TableInterface();
    	table.setId("ExamDistributions");
    	table.setName(MSG.sectionTitleDistributionPreferences());
    	
        LineInterface header = table.addHeader();
        if (isSimple()) header.addCell(MSG.columnDistrPrefLevel());
        header.addCell(MSG.columnDistrPrefType()).setSortable(true);
        header.addCell(MSG.columnExam()).setSortable(true);
        header.addCell(MSG.columnExamClassesCourses()).setSortable(true);
    	for (CellInterface cell: header.getCells()) {
    		cell.setClassName("WebTableHeader");
    		cell.setText(cell.getText().replace("<br>", "\n"));
    		cell.addStyle("white-space: pre-wrap;");
    	}

        int nrPrefs = 0;
        
        for (DistributionPref dp: distPrefs) {
        	if (!getSessionContext().hasPermission(dp, Right.ExaminationDistributionPreferenceDetail)) continue;
        	
        	nrPrefs++;
        	
        	CellInterface examStr = new CellInterface().setNoWrap(true);
        	CellInterface objectStr = new CellInterface().setNoWrap(true);
        	
        	for (DistributionObject dO: dp.getOrderedSetOfDistributionObjects()) {
        		Exam exam = (Exam)dO.getPrefGroup();
        		examStr.add(exam.getLabel()).setInline(false);
        		boolean first = true;
        		for (ExamOwner owner: exam.getOwners()) {
        			objectStr.add(owner.getLabel()).setInline(false);
        			if (first)
        				first = false;
        			else
        				examStr.add("\u00a0").setInline(false);
        		}
        	}
             
        	String distType = dp.getDistributionType().getLabel();
            String prefLevel = dp.getPrefLevel().getPrefName();
            String prefColor = dp.getPrefLevel().prefcolor();
        	if (PreferenceLevel.sNeutral.equals(dp.getPrefLevel().getPrefProlog()))
        		prefColor = "#808080";
            
            LineInterface line = table.addLine();

            if (getSessionContext().hasPermission(dp, Right.ExaminationDistributionPreferenceEdit))
            	line.setURL(ApplicationProperty.LegacyExamDistributions.isTrue() ? 
            			"examDistributionPrefs.action?dp=" + dp.getUniqueId() + "&op=view" :
            			"examDistributionEdit?id=" + dp.getUniqueId()
            			);
            
            if (isSimple()) line.addCell(prefLevel).setColor(prefColor).setNoWrap(true);
            line.addCell(distType).setColor(prefColor)
            	.setTitle(prefLevel + " " + distType)
            	.setAria(isSimple() ? distType : prefLevel + " " + distType)
            	.setNoWrap(true)
            	.setComparable(distType);
            line.addCell(examStr);
            line.addCell(objectStr);
            
            if ("DistributionPref".equals(getBackType()) && dp.getUniqueId().toString().equals(getBackId()))
            	line.getCells().get(0).addAnchor("back");
            else if ("PreferenceGroup".equals(getBackType()) && dp.getOwner().getUniqueId().toString().equals(getBackId()))
            	line.getCells().get(0).addAnchor("back");
        }
        
        if (nrPrefs==0)
        	table.addLine().addCell(MSG.noPreferencesFound()).setColSpan(4);
        
        return table;
    }
}
