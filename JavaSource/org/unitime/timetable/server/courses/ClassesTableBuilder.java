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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.cpsolver.coursett.model.Placement;
import org.cpsolver.coursett.model.RoomLocation;
import org.cpsolver.coursett.model.TimeLocation;
import org.hibernate.query.Query;
import org.unitime.commons.Debug;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.FilterInterface;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalMethod;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.comparators.ClassCourseComparator;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.CachedClassAssignmentProxy;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.ClassAssignmentProxy.AssignmentInfo;
import org.unitime.timetable.solver.exam.ExamAssignmentProxy;
import org.unitime.timetable.webutil.Navigation;

public class ClassesTableBuilder extends InstructionalOfferingTableBuilder {
	
	protected String additionalNote() {
		return "";
	}
	
	public void generateTableForClasses(SessionContext context,
            ClassAssignmentProxy classAssignment, 
            ExamAssignmentProxy examAssignment,
            FilterInterface filter, 
            String[] subjectAreaIds, 
            boolean displayHeader,
            List<TableInterface> tables,
            String backType,
            String backId){
    	
    	setBackType(backType); setBackId(backId);
    	
    	this.setVisibleColumns(filter);
		setShowProjectedDemand(false);
		setShowMinPerWk(false);
		setShowCredit(false);
		setShowSubpartCredit(false);
		setShowConsent(false);
		setShowTitle(false);
    	
		TreeSet classes = getClasses(filter, subjectAreaIds, classAssignment);
		Navigation.set(context, Navigation.sClassLevel, classes);

    	if (isShowTimetable()) {
    		boolean hasTimetable = false;
    		if (context.hasPermission(Right.ClassAssignments) && classAssignment != null) {
    			try {
                	if (classAssignment instanceof CachedClassAssignmentProxy) {
                		((CachedClassAssignmentProxy)classAssignment).setCache(classes);
                	}
    				for (Iterator i=classes.iterator();i.hasNext();) {
    					Object[] o = (Object[])i.next(); Class_ clazz = (Class_)o[0];
    					if (classAssignment.getAssignment(clazz)!=null) {
        					hasTimetable = true; break;
        				}
    				}
    			}  catch (Exception e) {}
    		}
    		setDisplayTimetable(hasTimetable);
    	}
        setUserSettings(context.getUser());
        
        if (isShowExam())
            setShowExamTimetable(examAssignment!=null || Exam.hasTimetable(context.getUser().getCurrentAcademicSessionId()));
        
        TableInterface table = null;
        SubjectArea subjectArea = null;
        String prevLabel = null;
        int ct = 0;
        for (Object o: classes) {
        	Class_ c = (Class_)((Object[])o)[0];
        	CourseOffering co = (CourseOffering)((Object[])o)[1];
            if (subjectArea == null || !subjectArea.getUniqueId().equals(co.getSubjectArea().getUniqueId())){
            	subjectArea = co.getSubjectArea();
		        table = initTable(context.getUser().getCurrentAcademicSessionId());
		        table.setName(subjectArea.getSubjectAreaAbbreviation() +
		        		(filter.getParameterValue("courseNbr", "").isEmpty() ? "" : " " + filter.getParameterValue("courseNbr", ""))
		        		+ " - " + subjectArea.getSession().getLabel() + additionalNote());
		        tables.add(table);
		        ct = 0;
		    }		        
            buildClassRow(classAssignment,examAssignment, ++ct, table, co, c, 0, context, prevLabel);
            prevLabel = c.getClassLabel(co);        	
        }
    }
	
    public static TreeSet getClasses(FilterInterface filter, String[] subjectIds, ClassAssignmentProxy classAssignmentProxy) {
		org.hibernate.Session hibSession = (InstructionalOfferingDAO.getInstance()).getSession();

		
		Set<Long> filterManagers = new HashSet<Long>();
		if (!filter.getParameterValue("filterManager", "").isEmpty()) {
			for (String filterManager: filter.getParameterValue("filterManager").split(","))
				filterManagers.add(Long.valueOf(filterManager));
		}
        
		if (subjectIds != null && subjectIds.length > 0){
			StringBuffer query = new StringBuffer();
			query.append("select c, co from Class_ as c ");
			query.append("left join fetch c.childClasses as cc ");
			query.append("left join fetch c.schedulingSubpart as ss ");
			query.append("left join fetch ss.childSubparts as css ");
			query.append("left join fetch ss.instrOfferingConfig as ioc ");
			query.append("left join fetch ioc.instructionalOffering as io ");
			query.append("left join fetch io.courseOfferings as cox ");
			query.append("inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings as co ");
			query.append(" where co.subjectArea.uniqueId in ( ");
			boolean first = true;
			for(int i = 0; i < subjectIds.length; i++){
				if (!first){
					query.append(", ");
				} else {
					first = false;
				}
				query.append(subjectIds[i]);
			}
			query.append(") ");
			String courseNbr = filter.getParameterValue("courseNbr");
			if (ApplicationProperty.CourseOfferingTitleSearch.isTrue() && courseNbr != null && courseNbr.length() > 2) {
				if (courseNbr.indexOf('*') >= 0) {
					query.append(" and (co.courseNbr like :courseNbr or lower(co.title) like lower(:courseNbr))");
				} else {
					query.append(" and (co.courseNbr = :courseNbr or lower(co.title) like ('%' || lower(:courseNbr) || '%'))");
				}
			} else if (courseNbr != null && courseNbr.length() > 0){
				if (courseNbr.indexOf('*') >= 0) {
					query.append(" and co.courseNbr like :courseNbr ");
				} else {
					query.append(" and co.courseNbr = :courseNbr ");
				}
	        }

			boolean hasDeptIds = false;
	        if (!filterManagers.isEmpty()) {
	        	if (filterManagers.contains(-2l) && filterManagers.size() == 1) {
	        		query.append(" and c.managingDept = co.subjectArea.department");
	        	} else if (filterManagers.contains(-2l)) {
	        		query.append(" and (c.managingDept = co.subjectArea.department or c.managingDept.uniqueId in :deptIds)");
	        		hasDeptIds = true;
	        	} else {
	        		query.append(" and c.managingDept.uniqueId in :deptIds");
	        		hasDeptIds = true;
	        	}
	        }
	        
	        if (!"1".equals(filter.getParameterValue("showCrossListedClasses"))) {
	        	query.append(" and co.isControl = true ");
	        }
	        if (!"1".equals(filter.getParameterValue("includeCancelledClasses")) || "1".equals(filter.getParameterValue("filterNeedInstructor"))) {
	        	query.append(" and c.cancelled = false");
	        }
	        if ("1".equals(filter.getParameterValue("filterNeedInstructor"))) {
	        	query.append(" and (select sum(tr.teachingRequest.nbrInstructors) from TeachingClassRequest tr where tr.assignInstructor = true and  tr.teachingClass = c) > 0");
	        }
			Query<Object[]> q = hibSession.createQuery(query.toString(), Object[].class);
			q.setFetchSize(1000);
			if (courseNbr != null && courseNbr.length() > 0) {
				if (ApplicationProperty.CourseOfferingNumberUpperCase.isTrue())
	            	courseNbr = courseNbr.toUpperCase();
				q.setParameter("courseNbr", courseNbr.replace('*', '%'));
			}
			if (hasDeptIds)
				q.setParameterList("deptIds", filterManagers);
			q.setCacheable(true);
	        TreeSet ts = new TreeSet(new ClassCourseComparator(
	        		filter.getParameterValue("sortBy", "NAME"),
	        		classAssignmentProxy,
	        		"1".equals(filter.getParameterValue("sortByKeepSubparts"))));
			long sTime = new java.util.Date().getTime();
			
			boolean doFilterInstructor = !filter.getParameterValue("filterInstructor","").isEmpty();
			String filterInstructor = (doFilterInstructor ? filter.getParameterValue("filterInstructor","").toUpperCase() : null);
			
			boolean doFilterAssignedRoom = !filter.getParameterValue("filterAssignedRoom","").isEmpty();
			String filterAssignedRoom = (doFilterAssignedRoom ? filter.getParameterValue("filterAssignedRoom","").toUpperCase() : null);
			
			boolean doFilterIType = !filter.getParameterValue("filterIType","").isEmpty();
			String filterIType = (doFilterIType ? filter.getParameterValue("filterIType","") : null);
			
			boolean doFilterAssignedTime = !"0".equals(filter.getParameterValue("filterDateCode","0")) ||
					!filter.getParameterValue("filterStartTime","").isEmpty() ||
					!filter.getParameterValue("filterEndTime","").isEmpty();
					;
			TimeLocation filterAssignedTime = null;
			if (doFilterAssignedTime) {
				int dayCode = Integer.parseInt(filter.getParameterValue("filterDateCode","255"));
				String filterStartTime = filter.getParameterValue("filterStartTime","");
				int startTime = filterStartTime.isEmpty() ? 0 : Integer.parseInt(filterStartTime);
				String filterEndTime = filter.getParameterValue("filterEndTime","");
				int endTime = filterEndTime.isEmpty() ? 288 : Integer.parseInt(filterEndTime);
				filterAssignedTime = new TimeLocation(
						dayCode, startTime, endTime - startTime,
						0,0,null,null,null,0);
			}
			// days, start time & length selected -> create appropriate time location
			// days, start time selected -> create appropriate time location with 1 slot length
			// start time & length selected -> create time location all days with given start time and length
            // only start time selected -> create time location all days with given start time and 1 slot length
			// only days selected -> create time location of given days all day long (all location assigned in the given days overlap)
			
			Debug.debug(" --- Filter classes ---");
			for (Object[] o: q.list()) {
				Class_ c = (Class_)o[0];
				if (doFilterInstructor) {
					boolean filterLine = true;
					for (Iterator j=c.getClassInstructors().iterator();j.hasNext();) {
						ClassInstructor ci = (ClassInstructor)j.next();
						StringTokenizer stk = new StringTokenizer(filterInstructor," ,");
						boolean containsInstructor = true;
						while (stk.hasMoreTokens()) {
							String token = stk.nextToken();
							boolean containsToken = false;
							if (ci.getInstructor().getFirstName()!=null && ci.getInstructor().getFirstName().toUpperCase().indexOf(token)>=0)
								containsToken = true;
							if (!containsToken && ci.getInstructor().getMiddleName()!=null && ci.getInstructor().getMiddleName().toUpperCase().indexOf(token)>=0)
								containsToken = true;
							if (!containsToken && ci.getInstructor().getLastName()!=null && ci.getInstructor().getLastName().toUpperCase().indexOf(token)>=0)
								containsToken = true;
							if (!containsToken) {
								containsInstructor = false; break;
							}
						}
						if (containsInstructor) {
							filterLine = false; break;
						}
					}
					if (filterLine) {
						continue;
					}
				}
				
				if (doFilterIType) {
				    ItypeDesc itype = c.getSchedulingSubpart().getItype();
				    boolean match=false;
				    while (!match && itype!=null) {
				        match = itype.getItype().toString().equals(filterIType);
				        itype = itype.getParent();
				    }
					if (!match) {
						continue;
					}
				}
				
				if (doFilterAssignedTime) {
					try {
						AssignmentInfo a = classAssignmentProxy.getAssignment(c);
						if (a==null) {
							continue;
						}
						Placement p = a.getPlacement();
						if (p==null) {
							continue;
						}
						TimeLocation t = p.getTimeLocation();
						if (t==null) {
							continue;
						}
						boolean overlap = t.shareDays(filterAssignedTime) && t.shareHours(filterAssignedTime);
						if (!overlap) {
							continue;
						}
					} catch (Exception e) {
						continue;
					}
				}
				
				if (doFilterAssignedRoom) {
					try {
						AssignmentInfo a = classAssignmentProxy.getAssignment(c);
						if (a==null) {
							continue;
						}
						Placement p = a.getPlacement();
						if (p==null || p.getNrRooms()<=0) {
							continue;
						}
						boolean filterLine = true;
						if (p.isMultiRoom()) {
							for (RoomLocation r: p.getRoomLocations()) {
								if (r.getName().toUpperCase().indexOf(filterAssignedRoom)>=0) {
									filterLine = false;
									break;
								}
							}
						} else {
							if (p.getRoomLocation().getName().toUpperCase().indexOf(filterAssignedRoom)>=0) {
								filterLine = false;
							}
						}
						if (filterLine) {
							continue;
						}
					} catch (Exception e) {
						continue;
					}
				}
				
				ts.add(o);
			}
			
			long eTime = new java.util.Date().getTime();
	        Debug.debug("fetch time = " + (eTime - sTime));
	        Debug.debug("rows = " + ts.size());
	        return (ts);
		} else {
	        	return (new TreeSet());
	    }
    }
    
    @Override
    protected CellInterface buildPrefGroupLabel(CourseOffering co, PreferenceGroup prefGroup, int indentSpaces, boolean isEditable, String prevLabel){
        if (prefGroup instanceof Class_) {
        	CellInterface cell = new CellInterface();
        	if (indentSpaces > 0)
        		cell.setIndent(indentSpaces);
    		Class_ aClass = (Class_) prefGroup;
	    	if (!isEditable)
	    		cell.setColor(disabledColor);
	    	if ("PreferenceGroup".equals(getBackType()) && prefGroup.getUniqueId().toString().equals(getBackId()))
	    		cell.addAnchor("back");
	    	if (co.isIsControl()) cell.addStyle("font-weight: bold;");
	        String label = aClass.getClassLabel(co);
	        String title = aClass.getClassLabelWithTitle(co);
	        if (prevLabel != null && label.equals(prevLabel)) {
	        	label = "";
	        }
			if (!aClass.isEnabledForStudentScheduling()){
				title += MSG.titleSuffixDoNotDisplay();
				cell.addStyle("font-style: italic;");
			}
			cell.setText(label);
	        cell.setTitle(title);
	        cell.setNoWrap(true);
	        InstructionalMethod im = aClass.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalMethod();
        	if (im != null)
        		cell.add(" (" + im.getReference() + ")").setTitle(im.getLabel());
        	return cell;
        } else {
        	return super.buildPrefGroupLabel(co, prefGroup, indentSpaces, isEditable, prevLabel);
        }
    }
    
    @Override
    protected TreeSet getExams(Class_ clazz) {
        //exams directly attached to the given class
        TreeSet ret = new TreeSet(Exam.findAll(ExamOwner.sOwnerTypeClass, clazz.getUniqueId()));
        //check whether the given class is of the first subpart of the config
        SchedulingSubpart subpart = clazz.getSchedulingSubpart();
        if (subpart.getParentSubpart()!=null) return ret; 
        InstrOfferingConfig config = subpart.getInstrOfferingConfig();
        SchedulingSubpartComparator cmp = new SchedulingSubpartComparator();
        for (Iterator i=config.getSchedulingSubparts().iterator();i.hasNext();) {
            SchedulingSubpart s = (SchedulingSubpart)i.next();
            if (cmp.compare(s,subpart)<0) return ret;
        }
        InstructionalOffering offering = config.getInstructionalOffering();
        //check passed -- add config/offering/course exams to the class exams
        ret.addAll(Exam.findAll(ExamOwner.sOwnerTypeConfig, config.getUniqueId()));
        ret.addAll(Exam.findAll(ExamOwner.sOwnerTypeOffering, offering.getUniqueId()));
        for (Iterator i=offering.getCourseOfferings().iterator();i.hasNext();) {
            CourseOffering co = (CourseOffering)i.next();
            ret.addAll(Exam.findAll(ExamOwner.sOwnerTypeCourse, co.getUniqueId()));
        }
        return ret;
    }
}
