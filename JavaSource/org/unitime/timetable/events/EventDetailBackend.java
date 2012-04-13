package org.unitime.timetable.events;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import net.sf.cpsolver.coursett.model.TimeLocation;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.command.server.GwtRpcHelper;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.Conflict;
import org.unitime.timetable.gwt.shared.EventException;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.gwt.shared.EventInterface.RelatedObjectInterface;
import org.unitime.timetable.gwt.shared.EventInterface.SponsoringOrganizationInterface;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.gwt.shared.EventInterface.ContactInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EventDetailRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.NoteInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceType;
import org.unitime.timetable.model.AcademicAreaClassification;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassEvent;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseEvent;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.Event.MultiMeeting;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.EventNote;
import org.unitime.timetable.model.ExamEvent;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.RelatedCourseInfo;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.ClassEventDAO;
import org.unitime.timetable.model.dao.CourseEventDAO;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.model.dao.ExamEventDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.util.CalendarUtils;
import org.unitime.timetable.util.Constants;

public class EventDetailBackend implements GwtRpcImplementation<EventDetailRpcRequest, EventInterface> {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	private static SimpleDateFormat sDateFormat = new SimpleDateFormat("MM/dd");
	
	@Override
	public EventInterface execute(EventDetailRpcRequest request, GwtRpcHelper helper) {
		Event event = EventDAO.getInstance().get(request.getEventId());
		if (event == null)
			throw new EventException("No event with id " + request.getEventId() + " found.");
		
		checkAccess(event, helper);
		
		EventInterface detail = getEventDetail(SessionDAO.getInstance().get(request.getSessionId()), event);
		detail.setCanEdit(canEdit(event, helper));
		
		return detail;
	}
	
	public void checkAccess(Event e, GwtRpcHelper helper) throws PageAccessException {
		if (helper.getUser() == null) {
			throw new PageAccessException(helper.isHttpSessionNew() ? MESSAGES.authenticationExpired() : MESSAGES.authenticationRequired());
		}
		if (helper.getUser().getRole() == null) {
			if (e.getMainContact() == null || !helper.getUser().getId().equals(e.getMainContact().getExternalUniqueId()))
				throw new PageAccessException(MESSAGES.authenticationInsufficient());
		}
	}
	
	public boolean canEdit(Event e, GwtRpcHelper helper) {
		if (helper.getUser() == null) {
			return false;
		}
		if (e.getMainContact() != null && helper.getUser().getId().equals(e.getMainContact().getExternalUniqueId()))
			return true;
		if (Roles.ADMIN_ROLE.equals(helper.getUser().getRole()) || Roles.EVENT_MGR_ROLE.equals(helper.getUser().getRole()))
			return true;
		return false;
	}
		
	
	public EventInterface getEventDetail(Session session, Event e) throws EventException {
		org.hibernate.Session hibSession = EventDAO.getInstance().getSession();
		Date now = new Date();
		
		EventInterface event = new EventInterface();
		event.setId(e.getUniqueId());
		event.setName(e.getEventName());
		event.setType(EventInterface.EventType.values()[e.getEventType()]);
		event.setCanView(true);
		event.setEmail(e.getEmail());
		event.setMaxCapacity(e.getMaxCapacity());
				
		if (e.getMainContact() != null) {
			ContactInterface contact = new ContactInterface();
			contact.setFirstName(e.getMainContact().getFirstName());
			contact.setMiddleName(e.getMainContact().getMiddleName());
			contact.setLastName(e.getMainContact().getLastName());
			contact.setExternalId(e.getMainContact().getExternalUniqueId());
			contact.setPhone(e.getMainContact().getPhone());
			contact.setEmail(e.getMainContact().getEmailAddress());
			event.setContact(contact);
		}
		
		for (EventContact c: e.getAdditionalContacts()) {
			ContactInterface contact = new ContactInterface();
			contact.setFirstName(c.getFirstName());
			contact.setMiddleName(c.getMiddleName());
			contact.setLastName(c.getLastName());
			contact.setExternalId(c.getExternalUniqueId());
			contact.setPhone(c.getPhone());
			contact.setEmail(c.getEmailAddress());
			event.addAdditionalContact(contact);
		}
		
		if (e.getSponsoringOrganization() != null) {
			SponsoringOrganizationInterface sponsor = new SponsoringOrganizationInterface();
			sponsor.setEmail(e.getSponsoringOrganization().getEmail());
			sponsor.setName(e.getSponsoringOrganization().getName());
			sponsor.setUniqueId(e.getSponsoringOrganization().getUniqueId());
			event.setSponsor(sponsor);
		}
		
		Map<Long, List<Meeting>> conflicts = null;
    	if (Event.sEventTypeClass == e.getEventType()) {
    		ClassEvent ce = (e instanceof ClassEvent ? (ClassEvent)e : ClassEventDAO.getInstance().get(e.getUniqueId(), hibSession));
    		
    		conflicts = computeConflicts(ce);
    		Class_ clazz = ce.getClazz();
    		RelatedObjectInterface related = new RelatedObjectInterface();
    		related.setType(RelatedObjectInterface.RelatedObjectType.Class);
    		related.setUniqueId(clazz.getUniqueId());
    		related.setName(clazz.getClassLabel(hibSession));
    		CourseOffering courseOffering = clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering();
    		related.addCourseName(courseOffering.getCourseName());
    		related.setSectionNumber(clazz.getSectionNumberString(hibSession));
    		if (clazz.getClassSuffix() != null)
    			related.addExternalId(clazz.getClassSuffix());
    		related.setInstruction(clazz.getSchedulingSubpart().getItype().getDesc());
    		related.setInstructionType(clazz.getSchedulingSubpart().getItype().getItype());
    		for (CourseOffering co: clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getCourseOfferings()) {
				if (!co.isIsControl()) {
					related.addCourseName(co.getCourseName());
					if (clazz.getClassSuffix(co) != null)
		    			related.addExternalId(clazz.getClassSuffix(co));
				}
    		}
    		if (clazz.getDisplayInstructor()) {
    			for (ClassInstructor i: clazz.getClassInstructors()) {
					ContactInterface instructor = new ContactInterface();
					instructor.setFirstName(i.getInstructor().getFirstName());
					instructor.setMiddleName(i.getInstructor().getMiddleName());
					instructor.setLastName(i.getInstructor().getLastName());
					instructor.setExternalId(i.getInstructor().getExternalUniqueId());
					instructor.setEmail(i.getInstructor().getEmail());
					related.addInstructor(instructor);
    			}
    		}
    		Assignment assignment = clazz.getCommittedAssignment();
    		if (assignment != null) {
    			TimeLocation time = assignment.getTimeLocation();
    			if (time != null) {
    				related.setTime(time.getDayHeader() + " " + time.getStartTimeHeader() + " - " + time.getEndTimeHeader());
    				related.setDate(time.getDatePatternName());
    			}
    			for (Location r: assignment.getRooms()) {
    				ResourceInterface location = new ResourceInterface();
    				location.setType(ResourceType.ROOM);
    				location.setId(r.getUniqueId());
    				location.setName(r.getLabel());
    				location.setHint(r.getHtmlHint());
    				location.setSize(r.getCapacity());
    				location.setRoomType(r.getRoomTypeLabel());
    				related.addLocation(location);
    			}
    		}
    		event.addRelatedObject(related);
    	} else if (Event.sEventTypeFinalExam == e.getEventType() || Event.sEventTypeMidtermExam == e.getEventType()) {
    		ExamEvent xe = (e instanceof ExamEvent ? (ExamEvent)e : ExamEventDAO.getInstance().get(e.getUniqueId(), hibSession));
    		
    		conflicts = computeConflicts(xe);
    		RelatedObjectInterface related = new RelatedObjectInterface();
    		related.setType(RelatedObjectInterface.RelatedObjectType.Examination);
    		related.setUniqueId(xe.getExam().getUniqueId());
    		related.setName(xe.getExam().getName() == null ? xe.getExam().generateName() : xe.getExam().getName());
    		related.addCourseName(related.getName());
    		related.setInstruction(e.getEventTypeAbbv());
    		related.setInstructionType(xe.getExam().getExamType());
    		related.setSectionNumber(xe.getExam().getLength().toString());
    		if (xe.getExam().getAssignedPeriod() != null) {
    			ExamPeriod period = xe.getExam().getAssignedPeriod();
    			related.setDate(period.getStartDateLabel());
    			int printOffset = (xe.getExam().getPrintOffset() == null ? 0 : xe.getExam().getPrintOffset());
    			related.setTime(period.getStartTimeLabel(printOffset) + " - " + period.getEndTimeLabel(xe.getExam().getLength(), printOffset));
    		}
    		for (Location r: xe.getExam().getAssignedRooms()) {
				ResourceInterface location = new ResourceInterface();
				location.setType(ResourceType.ROOM);
				location.setId(r.getUniqueId());
				location.setName(r.getLabel());
				location.setHint(r.getHtmlHint());
				location.setSize(r.getCapacity());
				location.setRoomType(r.getRoomTypeLabel());
				related.addLocation(location);
    		}
    		for (DepartmentalInstructor i: xe.getExam().getInstructors()) {
				ContactInterface instructor = new ContactInterface();
				instructor.setFirstName(i.getFirstName());
				instructor.setMiddleName(i.getMiddleName());
				instructor.setLastName(i.getLastName());
				instructor.setExternalId(i.getExternalUniqueId());
				instructor.setEmail(i.getEmail());
				related.addInstructor(instructor);
			}
    		event.addRelatedObject(related);
    		
			for (ExamOwner owner: new TreeSet<ExamOwner>(xe.getExam().getOwners())) {
				related = new RelatedObjectInterface();
				related.setType(RelatedObjectInterface.RelatedObjectType.values()[owner.getOwnerType()]);
				related.setUniqueId(owner.getOwnerId());
				related.setName(owner.getLabel());
				related.addCourseName(owner.getCourse().getCourseName());
				if (owner.getOwnerType() == ExamOwner.sOwnerTypeClass) {
					Class_ clazz = (Class_)owner.getOwnerObject();
					related.setSectionNumber(clazz.getSectionNumberString(hibSession));
		    		if (clazz.getClassSuffix() != null)
		    			related.addExternalId(clazz.getClassSuffix());
		    		related.setInstruction(clazz.getSchedulingSubpart().getItype().getDesc());
		    		related.setInstructionType(clazz.getSchedulingSubpart().getItype().getItype());
		    		for (CourseOffering course: owner.getCourse().getInstructionalOffering().getCourseOfferings()) {
						if (!course.isIsControl()) {
							related.addCourseName(course.getCourseName());
							if (clazz.getClassSuffix(course) != null)
				    			related.addExternalId(clazz.getClassSuffix(course));
						}
		    		}
		    		Assignment assignment = clazz.getCommittedAssignment();
		    		if (assignment != null) {
		    			TimeLocation time = assignment.getTimeLocation();
		    			if (time != null) {
		    				related.setTime(time.getDayHeader() + " " + time.getStartTimeHeader() + " - " + time.getEndTimeHeader());
		    				related.setDate(time.getDatePatternName());
		    			}
		    			for (Location r: assignment.getRooms()) {
		    				ResourceInterface location = new ResourceInterface();
		    				location.setType(ResourceType.ROOM);
		    				location.setId(r.getUniqueId());
		    				location.setName(r.getLabel());
		    				location.setHint(r.getHtmlHint());
		    				location.setSize(r.getCapacity());
		    				location.setRoomType(r.getRoomTypeLabel());
		    				related.addLocation(location);
		    			}
		    		}
				} else if (owner.getOwnerType() == ExamOwner.sOwnerTypeOffering) {
		    		for (CourseOffering course: owner.getCourse().getInstructionalOffering().getCourseOfferings()) {
		    			if (!course.isIsControl()) {
							related.addCourseName(course.getCourseName());
		    			}
		    		}
					related.setInstruction("Offering");
				} else if (owner.getOwnerType() == ExamOwner.sOwnerTypeConfig) {
					InstrOfferingConfig config = (InstrOfferingConfig)owner.getOwnerObject();
					related.setSectionNumber(config.getName());
					related.setInstruction("Configuration");
				} else {
					related.setInstruction("Course");
				}
				event.addRelatedObject(related);
			}
    	} else if (Event.sEventTypeCourse == e.getEventType()) {
    		CourseEvent ce = (e instanceof CourseEvent ? (CourseEvent)e : CourseEventDAO.getInstance().get(e.getUniqueId(), hibSession));
    		
    		conflicts = computeConflicts(ce);
			for (RelatedCourseInfo owner: new TreeSet<RelatedCourseInfo>(ce.getRelatedCourses())) {
				RelatedObjectInterface related = new RelatedObjectInterface();
				related.setType(RelatedObjectInterface.RelatedObjectType.values()[owner.getOwnerType()]);
				related.setUniqueId(owner.getOwnerId());
				related.addCourseName(owner.getCourse().getCourseName());
				related.setName(owner.getLabel());
				if (owner.getOwnerType() == ExamOwner.sOwnerTypeClass) {
					Class_ clazz = (Class_)owner.getOwnerObject();
					related.setSectionNumber(clazz.getSectionNumberString(hibSession));
		    		if (clazz.getClassSuffix() != null)
		    			related.addExternalId(clazz.getClassSuffix());
		    		related.setInstruction(clazz.getSchedulingSubpart().getItype().getDesc());
		    		related.setInstructionType(clazz.getSchedulingSubpart().getItype().getItype());
		    		for (CourseOffering course: owner.getCourse().getInstructionalOffering().getCourseOfferings()) {
						if (!course.isIsControl()) {
							related.addCourseName(course.getCourseName());
							if (clazz.getClassSuffix(course) != null)
				    			related.addExternalId(clazz.getClassSuffix(course));
						}
		    		}
		    		Assignment assignment = clazz.getCommittedAssignment();
		    		if (assignment != null) {
		    			TimeLocation time = assignment.getTimeLocation();
		    			if (time != null) {
		    				related.setTime(time.getDayHeader() + " " + time.getStartTimeHeader() + " - " + time.getEndTimeHeader());
		    				related.setDate(time.getDatePatternName());
		    			}
		    			for (Location r: assignment.getRooms()) {
		    				ResourceInterface location = new ResourceInterface();
		    				location.setType(ResourceType.ROOM);
		    				location.setId(r.getUniqueId());
		    				location.setName(r.getLabel());
		    				location.setHint(r.getHtmlHint());
		    				location.setSize(r.getCapacity());
		    				location.setRoomType(r.getRoomTypeLabel());
		    				related.addLocation(location);
		    			}
		    		}
				} else if (owner.getOwnerType() == ExamOwner.sOwnerTypeOffering) {
		    		for (CourseOffering course: owner.getCourse().getInstructionalOffering().getCourseOfferings()) {
		    			if (!course.isIsControl()) {
							related.addCourseName(course.getCourseName());
		    			}
		    		}
		    		related.setInstruction("Offering");
				} else if (owner.getOwnerType() == ExamOwner.sOwnerTypeConfig) {
					InstrOfferingConfig config = (InstrOfferingConfig)owner.getOwnerObject();
					related.setSectionNumber(config.getName());
					related.setInstruction("Configuration");
				} else {
					related.setInstruction("Course");
				}
				event.addRelatedObject(related);
			}
    		
    	}
    		
    	for (Meeting m: e.getMeetings()) {
			MeetingInterface meeting = new MeetingInterface();
			meeting.setId(m.getUniqueId());
			meeting.setMeetingDate(m.getMeetingDate());
			meeting.setDayOfWeek(Constants.getDayOfWeek(m.getMeetingDate()));
			meeting.setStartTime(m.getStartTime().getTime());
			meeting.setStopTime(m.getStopTime().getTime());
			meeting.setDayOfYear(CalendarUtils.date2dayOfYear(session.getSessionStartYear(), m.getMeetingDate()));
			meeting.setMeetingTime(m.startTime() + " - " + m.stopTime());
			meeting.setStartSlot(m.getStartPeriod());
			meeting.setEndSlot(m.getStopPeriod());
			meeting.setPast(m.getStartTime().before(now));
			if (m.isApproved())
				meeting.setApprovalDate(m.getApprovedDate());
			if (m.getLocation() != null) {
				ResourceInterface location = new ResourceInterface();
				location.setType(ResourceType.ROOM);
				location.setId(m.getLocation().getUniqueId());
				location.setName(m.getLocation().getLabel());
				location.setHint(m.getLocation().getHtmlHint());
				location.setSize(m.getLocation().getCapacity());
				location.setRoomType(m.getLocation().getRoomTypeLabel());
				meeting.setLocation(location);
			}
			event.addMeeting(meeting);
		}
    	
    	for (EventNote n: e.getNotes()) {
    		NoteInterface note = new NoteInterface();
    		note.setDate(n.getTimeStamp());
    		note.setType(NoteInterface.NoteType.values()[n.getNoteType()]);
    		note.setMeetings(n.getMeetingsHtml());
    		note.setNote(n.getTextNote());
    		note.setUser(n.getUser());
    		event.addNote(note);
    	}
    	
    	Collection<StudentClassEnrollment> enrollments = e.getStudentClassEnrollments();
    	if (enrollments != null) {
    		Map<String, String> approvedBy2name = new Hashtable<String, String>();
    		Hashtable<Long, ClassAssignmentInterface.Enrollment> student2enrollment = new Hashtable<Long, ClassAssignmentInterface.Enrollment>();
        	for (StudentClassEnrollment enrollment: enrollments) {
        		ClassAssignmentInterface.Enrollment enrl = student2enrollment.get(enrollment.getStudent().getUniqueId());
        		if (enrl == null) {
        			ClassAssignmentInterface.Student st = new ClassAssignmentInterface.Student();
        			st.setId(enrollment.getStudent().getUniqueId());
        			st.setExternalId(enrollment.getStudent().getExternalUniqueId());
        			st.setName(enrollment.getStudent().getName(ApplicationProperties.getProperty("unitime.enrollment.student.name", DepartmentalInstructor.sNameFormatLastFirstMiddle)));
        			for (AcademicAreaClassification ac: enrollment.getStudent().getAcademicAreaClassifications()) {
        				st.addArea(ac.getAcademicArea().getAcademicAreaAbbreviation());
        				st.addClassification(ac.getAcademicClassification().getCode());
        			}
        			for (PosMajor m: enrollment.getStudent().getPosMajors()) {
        				st.addMajor(m.getCode());
        			}
        			for (StudentGroup g: enrollment.getStudent().getGroups()) {
        				st.addGroup(g.getGroupAbbreviation());
        			}
        			enrl = new ClassAssignmentInterface.Enrollment();
        			enrl.setStudent(st);
        			enrl.setEnrolledDate(enrollment.getTimestamp());
        			CourseAssignment c = new CourseAssignment();
        			c.setCourseId(enrollment.getCourseOffering().getUniqueId());
        			c.setSubject(enrollment.getCourseOffering().getSubjectAreaAbbv());
        			c.setCourseNbr(enrollment.getCourseOffering().getCourseNbr());
        			enrl.setCourse(c);
        			student2enrollment.put(enrollment.getStudent().getUniqueId(), enrl);
        			if (enrollment.getCourseRequest() != null) {
        				enrl.setPriority(1 + enrollment.getCourseRequest().getCourseDemand().getPriority());
        				if (enrollment.getCourseRequest().getCourseDemand().getCourseRequests().size() > 1) {
        					CourseRequest first = null;
        					for (CourseRequest r: enrollment.getCourseRequest().getCourseDemand().getCourseRequests()) {
        						if (first == null || r.getOrder().compareTo(first.getOrder()) < 0) first = r;
        					}
        					if (!first.equals(enrollment.getCourseRequest()))
        						enrl.setAlternative(first.getCourseOffering().getCourseName());
        				}
        				if (enrollment.getCourseRequest().getCourseDemand().isAlternative()) {
        					CourseDemand first = enrollment.getCourseRequest().getCourseDemand();
        					demands: for (CourseDemand cd: enrollment.getStudent().getCourseDemands()) {
        						if (!cd.isAlternative() && cd.getPriority().compareTo(first.getPriority()) < 0 && !cd.getCourseRequests().isEmpty()) {
        							for (CourseRequest cr: cd.getCourseRequests())
        								if (cr.getClassEnrollments().isEmpty()) continue demands;
        							first = cd;
        						}
        					}
        					CourseRequest alt = null;
        					for (CourseRequest r: first.getCourseRequests()) {
        						if (alt == null || r.getOrder().compareTo(alt.getOrder()) < 0) alt = r;
        					}
        					enrl.setAlternative(alt.getCourseOffering().getCourseName());
        				}
        				enrl.setRequestedDate(enrollment.getCourseRequest().getCourseDemand().getTimestamp());
        				enrl.setApprovedDate(enrollment.getApprovedDate());
        				if (enrollment.getApprovedBy() != null) {
        					String name = approvedBy2name.get(enrollment.getApprovedBy());
        					if (name == null) {
        						TimetableManager mgr = (TimetableManager)hibSession.createQuery(
        								"from TimetableManager where externalUniqueId = :externalId")
        								.setString("externalId", enrollment.getApprovedBy())
        								.setMaxResults(1).uniqueResult();
        						if (mgr != null) {
        							name = mgr.getName();
        						} else {
        							DepartmentalInstructor instr = (DepartmentalInstructor)hibSession.createQuery(
        									"from DepartmentalInstructor where externalUniqueId = :externalId and department.session.uniqueId = :sessionId")
        									.setString("externalId", enrollment.getApprovedBy())
        									.setLong("sessionId", enrollment.getStudent().getSession().getUniqueId())
        									.setMaxResults(1).uniqueResult();
        							if (instr != null)
        								name = instr.nameLastNameFirst();
        						}
        						if (name != null)
        							approvedBy2name.put(enrollment.getApprovedBy(), name);
        					}
        					enrl.setApprovedBy(name == null ? enrollment.getApprovedBy() : name);
        				}
        			} else {
        				enrl.setPriority(-1);
        			}
        			
    				List<Meeting> conf = (conflicts == null ? null : conflicts.get(enrollment.getStudent().getUniqueId()));
    				if (conf != null) {
    					Map<Event, TreeSet<Meeting>> events = new HashMap<Event, TreeSet<Meeting>>();
    					for (Meeting m: conf) {
    						TreeSet<Meeting> ms = events.get(m.getEvent());
    						if (ms == null) { ms = new TreeSet<Meeting>(); events.put(m.getEvent(), ms); }
    						ms.add(m);
    					}
    					for (Event confEvent: new TreeSet<Event>(events.keySet())) {
    						Conflict conflict = new Conflict();
    						conflict.setName(confEvent.getEventName());
    						conflict.setType(confEvent.getEventTypeAbbv());
    						String lastDate = null, lastTime = null, lastRoom = null;
    						for (MultiMeeting mm: Event.getMultiMeetings(events.get(confEvent))) {
    							String date = sDateFormat.format(mm.getMeetings().first().getMeetingDate()) +
									(mm.getMeetings().size() == 1 ? "" : " - " + sDateFormat.format(mm.getMeetings().last().getMeetingDate()));
    							if (lastDate == null) {
    								conflict.setDate(date);
    							} else if (lastDate.equals(date)) {
    								conflict.setDate(conflict.getDate() + "<br>");
    							} else {
    								conflict.setDate(conflict.getDate() + "<br>" + date);
    							}
    							lastDate = date;
    							
    							String time = mm.getDays() + " " + (mm.getMeetings().first().isAllDay() ? "All Day" : mm.getMeetings().first().startTime() + " - " + mm.getMeetings().first().stopTime());
    							if (lastTime == null) {
    								conflict.setTime(time);
    							} else if (lastTime.equals(time)) {
    								conflict.setTime(conflict.getTime() + "<br>");
    							} else {
    								conflict.setTime(conflict.getTime() + "<br>" + time);
    							}
    							lastTime = time;
    							
    							String room = (mm.getMeetings().first().getLocation() == null ? "" : mm.getMeetings().first().getLocation().getLabel());
    							if (lastRoom == null) {
    								conflict.setRoom(room);
    							} else if (lastRoom.equals(room)) {
    								conflict.setRoom(conflict.getRoom() + "<br>");
    							} else {
    								conflict.setRoom(conflict.getRoom() + "<br>" + room);
    							}
    							lastRoom = room;
    						}
    						enrl.addConflict(conflict);
    					}
    				}
        			
        			event.addEnrollment(enrl);
        		}
        		ClassAssignmentInterface.ClassAssignment c = enrl.getCourse().addClassAssignment();
        		c.setClassId(enrollment.getClazz().getUniqueId());
        		c.setSection(enrollment.getClazz().getClassSuffix(enrollment.getCourseOffering()));
        		if (c.getSection() == null)
        			c.setSection(enrollment.getClazz().getSectionNumberString(hibSession));
        		c.setClassNumber(enrollment.getClazz().getSectionNumberString(hibSession));
        		c.setSubpart(enrollment.getClazz().getSchedulingSubpart().getItypeDesc());
        	}
    	}
    	    	
    	return event;
	}
		
	private String where(int type, int idx) {
		switch (type) {
		case ExamOwner.sOwnerTypeClass:
			return " and o" + idx + ".ownerType = " + type + " and o" + idx + ".ownerId = s" + idx + ".clazz.uniqueId";
		case ExamOwner.sOwnerTypeConfig:
			return " and o" + idx + ".ownerType = " + type + " and o" + idx + ".ownerId = s" + idx + ".clazz.schedulingSubpart.instrOfferingConfig.uniqueId";
		case ExamOwner.sOwnerTypeCourse:
			return " and o" + idx + ".ownerType = " + type + " and o" + idx + ".ownerId = s" + idx + ".courseOffering.uniqueId";
		case ExamOwner.sOwnerTypeOffering:
			return " and o" + idx + ".ownerType = " + type + " and o" + idx + ".ownerId = s" + idx + ".courseOffering.instructionalOffering.uniqueId";
		default:
			return "";
		}
	}
	
	private Map<Long, List<Meeting>> computeConflicts(ClassEvent event) {
		Map<Long, List<Meeting>> conflicts = new HashMap<Long, List<Meeting>>();
		
        org.hibernate.Session hibSession = EventDAO.getInstance().getSession();

		// class events
    	for (Object[] o: (List<Object[]>)hibSession.createQuery(
    			"select s1.student.uniqueId, m1" +
    			" from StudentClassEnrollment s1, ClassEvent e1 inner join e1.meetings m1, ClassEvent e2 inner join e2.meetings m2, StudentClassEnrollment s2" +
    			" where e2.uniqueId = :eventId and e1.uniqueId != e2.uniqueId and e1.clazz = s1.clazz and e2.clazz = s2.clazz and s1.student = s2.student" +
    			" and m1.meetingDate = m2.meetingDate and m1.startPeriod < m2.stopPeriod and m2.startPeriod < m1.stopPeriod")
    			.setLong("eventId", event.getUniqueId()).list()) {
    		Long studentId = (Long)o[0];
    		Meeting meeting = (Meeting)o[1];
    		List<Meeting> meetings = conflicts.get(studentId);
    		if (meetings == null) {
    			meetings = new ArrayList<Meeting>(); conflicts.put(studentId, meetings);
    		}
    		meetings.add(meeting);
    	}
    	
    	// examination events
    	for (int t1 = 0; t1 < ExamOwner.sOwnerTypes.length; t1++) {
        	for (Object[] o: (List<Object[]>)hibSession.createQuery(
        			"select s1.student.uniqueId, m1" +
        			" from StudentClassEnrollment s1, ExamEvent e1 inner join e1.meetings m1 inner join e1.exam.owners o1, ClassEvent e2 inner join e2.meetings m2, StudentClassEnrollment s2" +
        			" where e2.uniqueId = :eventId and e1.uniqueId != e2.uniqueId and e2.clazz = s2.clazz and s1.student = s2.student" +
        			where(t1, 1) +
        			" and m1.meetingDate = m2.meetingDate and m1.startPeriod < m2.stopPeriod and m2.startPeriod < m1.stopPeriod")
        			.setLong("eventId", event.getUniqueId()).list()) {
        		Long studentId = (Long)o[0];
        		Meeting meeting = (Meeting)o[1];
        		List<Meeting> meetings = conflicts.get(studentId);
        		if (meetings == null) {
        			meetings = new ArrayList<Meeting>(); conflicts.put(studentId, meetings);
        		}
        		meetings.add(meeting);
        	}    		
    	}
    	
    	// course events
    	for (int t1 = 0; t1 < ExamOwner.sOwnerTypes.length; t1++) {
        	for (Object[] o: (List<Object[]>)hibSession.createQuery(
        			"select s1.student.uniqueId, m1" +
        			" from StudentClassEnrollment s1, CourseEvent e1 inner join e1.meetings m1 inner join e1.relatedCourses o1, ClassEvent e2 inner join e2.meetings m2, StudentClassEnrollment s2" +
        			" where e2.uniqueId = :eventId and e1.uniqueId != e2.uniqueId and e2.clazz = s2.clazz and s1.student = s2.student" +
        			where(t1, 1) +
        			" and m1.meetingDate = m2.meetingDate and m1.startPeriod < m2.stopPeriod and m2.startPeriod < m1.stopPeriod")
        			.setLong("eventId", event.getUniqueId()).list()) {
        		Long studentId = (Long)o[0];
        		Meeting meeting = (Meeting)o[1];
        		List<Meeting> meetings = conflicts.get(studentId);
        		if (meetings == null) {
        			meetings = new ArrayList<Meeting>(); conflicts.put(studentId, meetings);
        		}
        		meetings.add(meeting);
        	}
    	}
    	
    	return conflicts;
	}
	
	private Map<Long, List<Meeting>> computeConflicts(ExamEvent event) {
		Map<Long, List<Meeting>> conflicts = new HashMap<Long, List<Meeting>>();
		
        org.hibernate.Session hibSession = EventDAO.getInstance().getSession();
        
		// class events
        for (int t2 = 0; t2 < ExamOwner.sOwnerTypes.length; t2++) {
        	for (Object[] o: (List<Object[]>)hibSession.createQuery(
        			"select s1.student.uniqueId, m1" +
        			" from StudentClassEnrollment s1, ClassEvent e1 inner join e1.meetings m1, ExamEvent e2 inner join e2.meetings m2 inner join e2.exam.owners o2, StudentClassEnrollment s2" +
        			" where e2.uniqueId = :eventId and e1.uniqueId != e2.uniqueId and e1.clazz = s1.clazz and s1.student = s2.student" +
        			where(t2, 2) + 
        			" and m1.meetingDate = m2.meetingDate and m1.startPeriod < m2.stopPeriod and m2.startPeriod < m1.stopPeriod")
        			.setLong("eventId", event.getUniqueId()).list()) {
        		Long studentId = (Long)o[0];
        		Meeting meeting = (Meeting)o[1];
        		List<Meeting> meetings = conflicts.get(studentId);
        		if (meetings == null) {
        			meetings = new ArrayList<Meeting>(); conflicts.put(studentId, meetings);
        		}
        		meetings.add(meeting);
        	}
        }
        
    	// examination events
        for (int t1 = 0; t1 < ExamOwner.sOwnerTypes.length; t1++) {
            for (int t2 = 0; t2 < ExamOwner.sOwnerTypes.length; t2++) {
            	for (Object[] o: (List<Object[]>)hibSession.createQuery(
            			"select s1.student.uniqueId, m1" +
            			" from StudentClassEnrollment s1, ExamEvent e1 inner join e1.meetings m1 inner join e1.exam.owners o1, ExamEvent e2 inner join e2.meetings m2 inner join e2.exam.owners o2, StudentClassEnrollment s2" +
            			" where e2.uniqueId = :eventId and e1.uniqueId != e2.uniqueId and s1.student = s2.student" +
            			where(t1, 1) + where(t2, 2) +
            			" and m1.meetingDate = m2.meetingDate and m1.startPeriod < m2.stopPeriod and m2.startPeriod < m1.stopPeriod")
            			.setLong("eventId", event.getUniqueId()).list()) {
            		Long studentId = (Long)o[0];
            		Meeting meeting = (Meeting)o[1];
            		List<Meeting> meetings = conflicts.get(studentId);
            		if (meetings == null) {
            			meetings = new ArrayList<Meeting>(); conflicts.put(studentId, meetings);
            		}
            		meetings.add(meeting);
            	}
            }
        }
        
    	// course events
        for (int t1 = 0; t1 < ExamOwner.sOwnerTypes.length; t1++) {
            for (int t2 = 0; t2 < ExamOwner.sOwnerTypes.length; t2++) {
            	for (Object[] o: (List<Object[]>)hibSession.createQuery(
            			"select s1.student.uniqueId, m1" +
            			" from StudentClassEnrollment s1, CourseEvent e1 inner join e1.meetings m1 inner join e1.relatedCourses o1, ExamEvent e2 inner join e2.meetings m2 inner join e2.exam.owners o2, StudentClassEnrollment s2" +
            			" where e2.uniqueId = :eventId and e1.uniqueId != e2.uniqueId and s1.student = s2.student" +
            			where(t1, 1) + where(t2, 2) +
            			" and m1.meetingDate = m2.meetingDate and m1.startPeriod < m2.stopPeriod and m2.startPeriod < m1.stopPeriod")
            			.setLong("eventId", event.getUniqueId()).list()) {
            		Long studentId = (Long)o[0];
            		Meeting meeting = (Meeting)o[1];
            		List<Meeting> meetings = conflicts.get(studentId);
            		if (meetings == null) {
            			meetings = new ArrayList<Meeting>(); conflicts.put(studentId, meetings);
            		}
            		meetings.add(meeting);
            	}
            }
        }
    	
    	return conflicts;
	}
	
	private Map<Long, List<Meeting>> computeConflicts(CourseEvent event) {
		Map<Long, List<Meeting>> conflicts = new HashMap<Long, List<Meeting>>();
		
        org.hibernate.Session hibSession = EventDAO.getInstance().getSession();
        
		// class events
        for (int t2 = 0; t2 < ExamOwner.sOwnerTypes.length; t2++) {
        	for (Object[] o: (List<Object[]>)hibSession.createQuery(
        			"select s1.student.uniqueId, m1" +
        			" from StudentClassEnrollment s1, ClassEvent e1 inner join e1.meetings m1, CourseEvent e2 inner join e2.meetings m2 inner join e2.relatedCourses o2, StudentClassEnrollment s2" +
        			" where e2.uniqueId = :eventId and e1.uniqueId != e2.uniqueId and e1.clazz = s1.clazz and s1.student = s2.student" +
        			where(t2, 2) + 
        			" and m1.meetingDate = m2.meetingDate and m1.startPeriod < m2.stopPeriod and m2.startPeriod < m1.stopPeriod")
        			.setLong("eventId", event.getUniqueId()).list()) {
        		Long studentId = (Long)o[0];
        		Meeting meeting = (Meeting)o[1];
        		List<Meeting> meetings = conflicts.get(studentId);
        		if (meetings == null) {
        			meetings = new ArrayList<Meeting>(); conflicts.put(studentId, meetings);
        		}
        		meetings.add(meeting);
        	}
        }
        
    	// examination events
        for (int t1 = 0; t1 < ExamOwner.sOwnerTypes.length; t1++) {
            for (int t2 = 0; t2 < ExamOwner.sOwnerTypes.length; t2++) {
            	for (Object[] o: (List<Object[]>)hibSession.createQuery(
            			"select s1.student.uniqueId, m1" +
            			" from StudentClassEnrollment s1, ExamEvent e1 inner join e1.meetings m1 inner join e1.exam.owners o1, CourseEvent e2 inner join e2.meetings m2 inner join e2.relatedCourses o2, StudentClassEnrollment s2" +
            			" where e2.uniqueId = :eventId and e1.uniqueId != e2.uniqueId and s1.student = s2.student" +
            			where(t1, 1) + where(t2, 2) +
            			" and m1.meetingDate = m2.meetingDate and m1.startPeriod < m2.stopPeriod and m2.startPeriod < m1.stopPeriod")
            			.setLong("eventId", event.getUniqueId()).list()) {
            		Long studentId = (Long)o[0];
            		Meeting meeting = (Meeting)o[1];
            		List<Meeting> meetings = conflicts.get(studentId);
            		if (meetings == null) {
            			meetings = new ArrayList<Meeting>(); conflicts.put(studentId, meetings);
            		}
            		meetings.add(meeting);
            	}
            }
        }
        
    	// course events
        for (int t1 = 0; t1 < ExamOwner.sOwnerTypes.length; t1++) {
            for (int t2 = 0; t2 < ExamOwner.sOwnerTypes.length; t2++) {
            	for (Object[] o: (List<Object[]>)hibSession.createQuery(
            			"select s1.student.uniqueId, m1" +
            			" from StudentClassEnrollment s1, CourseEvent e1 inner join e1.meetings m1 inner join e1.relatedCourses o1, CourseEvent e2 inner join e2.meetings m2 inner join e2.relatedCourses o2, StudentClassEnrollment s2" +
            			" where e2.uniqueId = :eventId and e1.uniqueId != e2.uniqueId and s1.student = s2.student" +
            			where(t1, 1) + where(t2, 2) +
            			" and m1.meetingDate = m2.meetingDate and m1.startPeriod < m2.stopPeriod and m2.startPeriod < m1.stopPeriod")
            			.setLong("eventId", event.getUniqueId()).list()) {
            		Long studentId = (Long)o[0];
            		Meeting meeting = (Meeting)o[1];
            		List<Meeting> meetings = conflicts.get(studentId);
            		if (meetings == null) {
            			meetings = new ArrayList<Meeting>(); conflicts.put(studentId, meetings);
            		}
            		meetings.add(meeting);
            	}
            }
        }
    	
    	return conflicts;
	}
}
