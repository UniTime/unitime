package org.unitime.timetable.server.sectioning;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.cpsolver.coursett.model.RoomLocation;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.util.CSVFile;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.studentsct.StudentSectioningModel;
import org.cpsolver.studentsct.model.AreaClassificationMajor;
import org.cpsolver.studentsct.model.CourseRequest;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.Instructor;
import org.cpsolver.studentsct.model.Offering;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.SctAssignment;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Student;
import org.cpsolver.studentsct.model.StudentGroup;
import org.cpsolver.studentsct.report.StudentSectioningReport;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.onlinesectioning.model.XConfig;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.solver.SectioningRequest.ReschedulingReason;

public class SectioningIssuesReport implements StudentSectioningReport {
	private StudentSectioningModel iModel = null;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private static CourseMessages CMSG = Localization.create(CourseMessages.class);

	public SectioningIssuesReport(StudentSectioningModel model) {
        iModel = model;
    }
	
    public StudentSectioningModel getModel() {
        return iModel;
    }

    protected String rooms(SctAssignment section) {
        if (section.getNrRooms() == 0) return "";
        String ret = "";
        for (RoomLocation r: section.getRooms())
            ret += (ret.isEmpty() ? "" : ",\n") + r.getName();
        return ret;
    }
    
    protected String curriculum(Student student) {
        String curriculum = "";
        for (AreaClassificationMajor acm: student.getAreaClassificationMajors())
                curriculum += (curriculum.isEmpty() ? "" : ",\n") + acm.toString();
        return curriculum;
    }
    
    protected String group(Student student) {
        String group = "";
        Set<String> groups = new TreeSet<String>();
        for (StudentGroup g: student.getGroups())
                groups.add(g.getReference());
        for (String g: groups)
                group += (group.isEmpty() ? "" : ",\n") + g;
        return group;           
    }
    
    protected String advisor(Student student) {
        String advisors = "";
        for (Instructor instructor: student.getAdvisors())
                advisors += (advisors.isEmpty() ? "" : ",\n") + instructor.getName();
        return advisors;
    }
    
    protected XOffering getOffering(Offering offering) {
    	return new XOffering(offering, getModel().getLinkedSections());
    }
    
    protected XOffering getOffering(Long offeringId) {
		for (Offering offering: getModel().getOfferings())
			if (offeringId.equals(offering.getId()))
				return getOffering(offering);
		return null;
    }
    
    public ReschedulingReason check(XStudent student, XOffering offering, XCourseRequest request) {
		if (request.getEnrollment() == null) return null;
		if (!offering.getOfferingId().equals(request.getEnrollment().getOfferingId())) return null;
		List<XSection> sections = offering.getSections(request.getEnrollment());
		XConfig config = offering.getConfig(request.getEnrollment().getConfigId());
		if (sections.size() != config.getSubparts().size()) {
			for (XSection s1: sections) {
				if (!offering.getSubpart(s1.getSubpartId()).getConfigId().equals(config.getConfigId())) {
					return ReschedulingReason.MULTIPLE_CONFIGS;
				}
			}
			return (sections.size() < config.getSubparts().size() ? ReschedulingReason.MISSING_CLASS : ReschedulingReason.MULTIPLE_ENRLS);
		}
		boolean ignoreBreakTime = getModel().getProperties().getPropertyBoolean("ReScheduling.IgnoreBreakTimeConflicts", false);
		for (XSection s1: sections) {
			for (XSection s2: sections) {
				if (s1.getSectionId() < s2.getSectionId() && s1.isOverlapping(offering.getDistributions(), s2, ignoreBreakTime)) {
					return ReschedulingReason.TIME_CONFLICT;
				}
				if (!s1.getSectionId().equals(s2.getSectionId()) && s1.getSubpartId().equals(s2.getSubpartId())) {
					return ReschedulingReason.CLASS_LINK;
				}
			}
			if (!offering.getSubpart(s1.getSubpartId()).getConfigId().equals(config.getConfigId())) {
				return ReschedulingReason.MULTIPLE_CONFIGS;
			}
		}
		if (!offering.isAllowOverlap(student, request.getEnrollment().getConfigId(), request.getEnrollment(), sections)) {
			for (XRequest r: student.getRequests()) {
				if (request.getPriority() <= r.getPriority()) continue; // only check time conflicts with courses of higher priority
				if (r instanceof XCourseRequest && !r.getRequestId().equals(request.getRequestId()) && ((XCourseRequest)r).getEnrollment() != null) {
					XEnrollment e = ((XCourseRequest)r).getEnrollment();
					XOffering other = getOffering(e.getOfferingId());
					if (other != null) {
						List<XSection> assignment = other.getSections(e);
						if (!other.isAllowOverlap(student, e.getConfigId(), e, assignment))
							for (XSection section: sections)
								if (section.isOverlapping(offering.getDistributions(), assignment, ignoreBreakTime)) {
									return ReschedulingReason.TIME_CONFLICT;
								}
					}
				}
			}
		}
		for (XSection section: sections)
			if (section.isCancelled())
				return ReschedulingReason.CLASS_CANCELLED;
		return null;
	}

    public CSVFile createTable(Assignment<Request, Enrollment> assignment, boolean includeLastLikeStudents, boolean includeRealStudents, boolean useAmPm) {
        CSVFile csv = new CSVFile();
        csv.setHeader(new CSVFile.CSVField[] {
                new CSVFile.CSVField("__Student"),
                new CSVFile.CSVField(MSG.reportStudentId()), new CSVFile.CSVField(MSG.reportStudentName()),
                new CSVFile.CSVField(MSG.reportStudentCurriculum()), new CSVFile.CSVField(MSG.reportStudentGroup()), new CSVFile.CSVField(MSG.reportStudentAdvisor()),
                new CSVFile.CSVField(MSG.reportCourse()), new CSVFile.CSVField(MSG.colSubpart()),
                new CSVFile.CSVField(MSG.reportClass()), new CSVFile.CSVField(MSG.reportMeetingTime()),
                new CSVFile.CSVField(MSG.reportDatePattern()), new CSVFile.CSVField(MSG.colRoom()),
                new CSVFile.CSVField(MSG.reportProblem()),  new CSVFile.CSVField(MSG.reportWaitListing())
                });
        
        for (Student student: getModel().getStudents()) {
        	if (student.isDummy() && !includeLastLikeStudents) continue;
            if (!student.isDummy() && !includeRealStudents) continue;
            XStudent xStudent = new XStudent(student, assignment);
            
        	for (Request request: student.getRequests()) {
        		Enrollment enrollment = assignment.getValue(request);
    			if (enrollment == null || !enrollment.isCourseRequest()) continue;
    			
    			ReschedulingReason reason = check(xStudent, getOffering(enrollment.getCourse().getOffering()), new XCourseRequest((CourseRequest)request, enrollment));
    			if (reason != null) {
    				List<CSVFile.CSVField> line = new ArrayList<CSVFile.CSVField>();
		            line.add(new CSVFile.CSVField(student.getId()));
		            line.add(new CSVFile.CSVField(student.getExternalId()));
		            line.add(new CSVFile.CSVField(student.getName()));
		            line.add(new CSVFile.CSVField(curriculum(student)));
		            line.add(new CSVFile.CSVField(group(student)));
		            line.add(new CSVFile.CSVField(advisor(student)));
		            line.add(new CSVFile.CSVField(enrollment.getCourse().getName()));
		            String type = "", section = "", time = "", room = "", date = "";
		            for (Section s: enrollment.getSections()) {
		            	type += (type.isEmpty() ? "" : "\n") + (s.getSubpart().getName() == null ? s.getSubpart().getInstructionalType() : s.getSubpart().getName());
		            	section += (section.isEmpty() ? "" : "\n") + s.getName(enrollment.getCourse().getId());
		            	time += (time.isEmpty() ? "" : "\n") + (s.getTime() == null || s.getTime().getDayCode() == 0 ? "" : s.getTime().getDayHeader() + " " + s.getTime().getStartTimeHeader(useAmPm) + " - " + s.getTime().getEndTimeHeader(useAmPm));
		            	date += (date.isEmpty() ? "" : "\n") + (s.getTime() == null || s.getTime().getDatePatternName() == null ? "" : s.getTime().getDatePatternName());
		            	room += (room.isEmpty() ? "" : "\n") + rooms(s);
		            }
		            line.add(new CSVFile.CSVField(type));
		            line.add(new CSVFile.CSVField(section));
		            line.add(new CSVFile.CSVField(time));
		            line.add(new CSVFile.CSVField(date));
		            line.add(new CSVFile.CSVField(room));
		            String r = reason.name();
		            switch (reason) {
		            case CLASS_CANCELLED:
						r = MSG.reschedulingReasonCancelledClass();
						break;
					case TIME_CONFLICT:
						r = MSG.reschedulingReasonTimeConflict();
						break;
					case CLASS_LINK:
						r = MSG.reschedulingReasonClassLink();
						break;
					case MISSING_CLASS:
						r = MSG.reschedulingReasonMissingClass();
						break;
					case MULTIPLE_CONFIGS:
						r = MSG.reschedulingReasonMultipleConfigs();
						break;
					case MULTIPLE_ENRLS:
						r = MSG.reschedulingReasonMultipleClasses();
						break;
					case NO_REQUEST:
						r = MSG.reschedulingReasonNoRequest();
						break;
					}
		            line.add(new CSVFile.CSVField(r));
		            InstructionalOffering io = InstructionalOfferingDAO.getInstance().get(enrollment.getCourse().getOffering().getId());
		            line.add(new CSVFile.CSVField(io.effectiveWaitList() ? CMSG.waitListEnabledShort() : io.effectiveReSchedule() ? CMSG.waitListRescheduleShort() : CMSG.waitListDisabledShort()));
		            csv.addLine(line);
    			}
        	}
        }
        return csv;
    }

    @Override
    public CSVFile create(Assignment<Request, Enrollment> assignment, DataProperties properties) {
        return createTable(assignment, properties.getPropertyBoolean("lastlike", false), properties.getPropertyBoolean("real", true), properties.getPropertyBoolean("useAmPm", true));
    }
}
