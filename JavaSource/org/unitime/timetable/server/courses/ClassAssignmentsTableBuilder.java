package org.unitime.timetable.server.courses;

import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.unitime.commons.Debug;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.FilterInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface.Alignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.LearningManagementSystemInfo;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.comparators.InstructorComparator;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.CachedClassAssignmentProxy;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.ClassAssignmentProxy.AssignmentInfo;
import org.unitime.timetable.solver.exam.ExamAssignmentProxy;
import org.unitime.timetable.webutil.Navigation;

public class ClassAssignmentsTableBuilder extends ClassesTableBuilder {
	
	@Override
	public String additionalNote() {
		return " " + MSG.classAssignmentsAdditionalNote();
	}
	
	public void generateTableForClassAssignments(SessionContext context,
            ClassAssignmentProxy classAssignment, 
            ExamAssignmentProxy examAssignment,
            FilterInterface filter, 
            String[] subjectAreaIds, 
            boolean displayHeader,
            List<TableInterface> tables,
            String backType,
            String backId){
		
		disabledColor="inherit";
    	setBackType(backType); setBackId(backId);
    	
        setShowLabel(true);
    	setShowDivSec(true);
    	setShowDemand(false);
		setShowProjectedDemand(false);
		setShowMinPerWk(false);
		setShowLimit(true);
		setShowSnapshotLimit(false);
		setShowRoomRatio(false);
		setShowFundingDepartment(false);
		setShowManager(false);
		setShowDatePattern(true);
		setShowTimePattern(false);
		setShowPreferences(false);
		setShowInstructor(true);
		setShowTimetable(true);
		setShowCredit(false);
		setShowSubpartCredit(false);
		setShowSchedulePrintNote(true);
		setShowNote(false);
		setShowTitle(false);
		setShowConsent(false);
		setShowExam(false);
		setShowInstructorAssignment(false);
		setShowLms(false);
		setShowWaitlistMode(false);
    	
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
        
        if (examAssignment!=null || Exam.hasTimetable(context.getUser().getCurrentAcademicSessionId())) {
            setShowExam(true);
            setShowExamTimetable(true);
            setShowExamName(false);
        }
        if (sessionHasEnrollments(context.getUser().getCurrentAcademicSessionId())) {
        	setShowDemand(true);
        }
        if (LearningManagementSystemInfo.isLmsInfoDefinedForSession(context.getUser().getCurrentAcademicSessionId())) {
     		setShowLms(true);
     	}
        
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
		        table.setName(subjectArea.getSubjectAreaAbbreviation() + " - " + subjectArea.getSession().getLabel() + additionalNote());
		        tables.add(table);
		        ct = 0;
		    }
            buildClassRow(classAssignment,examAssignment, ++ct, table, co, c, 0, context, prevLabel);
            prevLabel = c.getClassLabel(co);        	
        }
    }
	
	@Override
	protected CellInterface buildDatePatternCell(ClassAssignmentProxy classAssignment, PreferenceGroup prefGroup, boolean isEditable){
    	AssignmentInfo a = null;
		if (getDisplayTimetable() && isShowTimetable() && classAssignment!=null && prefGroup instanceof Class_) {
			try {
				a = classAssignment.getAssignment((Class_)prefGroup);
			} catch (Exception e) { 
				Debug.error(e);
			}
    	}
    	DatePattern dp = (a != null ? a.getDatePattern() : prefGroup.effectiveDatePattern());
    	CellInterface cell = null;
    	if (dp==null) {
    		cell = initNormalCell("", isEditable);
    	} else {
    		cell = initNormalCell(dp.getName(), isEditable);
    		cell.setTitle(sDateFormat.format(dp.getStartDate())+" - "+sDateFormat.format(dp.getEndDate()));
    	}
        cell.setTextAlignment(Alignment.CENTER);
        return(cell);
    }
	
	@Override
	protected CellInterface buildInstructor(PreferenceGroup prefGroup, boolean isEditable){
		super.buildInstructor(prefGroup, isEditable);
		CellInterface cell = initNormalCell(null, isEditable);
    	if (prefGroup instanceof Class_) {
    		Class_ aClass = (Class_) prefGroup;
    		if (aClass.isDisplayInstructor() && !aClass.getClassInstructors().isEmpty()) {
    			InstructorComparator ic = new InstructorComparator();
    	    	if (ApplicationProperty.InstructorsDropdownFollowNameFormatting.isTrue()) ic.setNameFormat(getInstructorNameFormat());
            	TreeSet<ClassInstructor> sortedInstructors = new TreeSet<ClassInstructor>(ic);
            	sortedInstructors.addAll(aClass.getClassInstructors());
        		for (ClassInstructor ci: sortedInstructors) {
        			CellInterface c = cell.add(ci.getInstructor().getName(getInstructorNameFormat()));
        			c.setInline(false);
        			c.setTitle(ci.getInstructor().getNameLastFirst() +
        						" (" + (ci.getResponsibility() == null ? "" : ci.getResponsibility().getLabel() + " ") +
                				ci.getPercentShare()+"%"+(ci.isLead().booleanValue()?", " + MSG.toolTipInstructorLead():"")+")");
        			// if (ci.isLead()) c.addStyle("font-weight: bold;");
        		}
    		}
    	}
    	return cell.setTextAlignment(Alignment.LEFT);
    }
}
