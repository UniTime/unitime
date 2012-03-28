package org.unitime.timetable.events;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TreeSet;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.command.server.GwtRpcHelper;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.EventException;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.SponsoringOrganizationInterface;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.gwt.shared.EventInterface.ContactInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EventDetailRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.NoteInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceType;
import org.unitime.timetable.model.ClassEvent;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseEvent;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.EventNote;
import org.unitime.timetable.model.ExamEvent;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.RelatedCourseInfo;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.ClassEventDAO;
import org.unitime.timetable.model.dao.CourseEventDAO;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.model.dao.ExamEventDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.util.Constants;

public class EventDetailBackend implements GwtRpcImplementation<EventDetailRpcRequest, EventInterface> {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);

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
		boolean suffix = "true".equals(ApplicationProperties.getProperty("tmtbl.exam.report.suffix","false"));
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
		
    	if (Event.sEventTypeClass == e.getEventType()) {
    		ClassEvent ce = (e instanceof ClassEvent ? (ClassEvent)e : ClassEventDAO.getInstance().get(e.getUniqueId(), hibSession));
    		Class_ clazz = ce.getClazz();
    		if (clazz.getDisplayInstructor()) {
    			for (ClassInstructor i: clazz.getClassInstructors()) {
					ContactInterface instructor = new ContactInterface();
					instructor.setFirstName(i.getInstructor().getFirstName());
					instructor.setMiddleName(i.getInstructor().getMiddleName());
					instructor.setLastName(i.getInstructor().getLastName());
					instructor.setExternalId(i.getInstructor().getExternalUniqueId());
					instructor.setEmail(i.getInstructor().getEmail());
					event.addInstructor(instructor);
    			}
    		}
    		CourseOffering courseOffering = clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering();
    		event.addCourseName(courseOffering.getCourseName());
    		event.setInstruction(clazz.getSchedulingSubpart().getItype().getDesc());
    		event.setInstructionType(clazz.getSchedulingSubpart().getItype().getItype());
    		String section = (suffix && clazz.getClassSuffix(courseOffering) != null ? clazz.getClassSuffix(courseOffering) : clazz.getSectionNumberString(hibSession));
    		event.addExternalId(section);
    		if (clazz.getClassSuffix(courseOffering) == null) {
	    		event.setName(clazz.getClassLabel(courseOffering));
    		} else {
    			event.setName(courseOffering.getCourseName() + " " + section);
    		}
			for (CourseOffering co: clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getCourseOfferings()) {
				if (!co.isIsControl()) {
					event.addCourseName(co.getCourseName());
					event.addExternalId(suffix && clazz.getClassSuffix(co) != null ? clazz.getClassSuffix(co) : clazz.getSectionNumberString(hibSession));
				}
			}
    	} else if (Event.sEventTypeFinalExam == e.getEventType() || Event.sEventTypeMidtermExam == e.getEventType()) {
    		ExamEvent xe = (e instanceof ExamEvent ? (ExamEvent)e : ExamEventDAO.getInstance().get(e.getUniqueId(), hibSession));
			for (DepartmentalInstructor i: xe.getExam().getInstructors()) {
				ContactInterface instructor = new ContactInterface();
				instructor.setFirstName(i.getFirstName());
				instructor.setMiddleName(i.getMiddleName());
				instructor.setLastName(i.getLastName());
				instructor.setExternalId(i.getExternalUniqueId());
				instructor.setEmail(i.getEmail());
				event.addInstructor(instructor);
			}
			for (ExamOwner owner: new TreeSet<ExamOwner>(xe.getExam().getOwners())) {
				for(CourseOffering course: owner.getCourse().getInstructionalOffering().getCourseOfferings()) {
    				String courseName = owner.getCourse().getCourseName();
    				String label = owner.getLabel();
    				if (label.startsWith(courseName)) {
    					label = label.substring(courseName.length());
    				}
    				event.addCourseName(course.getCourseName());
    				event.addExternalId(label.trim());
				}
			}
    	} else if (Event.sEventTypeCourse == e.getEventType()) {
    		CourseEvent ce = (e instanceof CourseEvent ? (CourseEvent)e : CourseEventDAO.getInstance().get(e.getUniqueId(), hibSession));
			for (RelatedCourseInfo owner: new TreeSet<RelatedCourseInfo>(ce.getRelatedCourses())) {
				for(CourseOffering course: owner.getCourse().getInstructionalOffering().getCourseOfferings()) {
    				String courseName = owner.getCourse().getCourseName();
    				String label = owner.getLabel();
    				if (label.startsWith(courseName)) {
    					label = label.substring(courseName.length());
    				}
    				event.addCourseName(course.getCourseName());
    				event.addExternalId(label.trim());
				}
			}
    		
    	}
    		
    	for (Meeting m: e.getMeetings()) {
			MeetingInterface meeting = new MeetingInterface();
			meeting.setId(m.getUniqueId());
			meeting.setMeetingDate(new SimpleDateFormat("MM/dd").format(m.getMeetingDate()));
			meeting.setDayOfWeek(Constants.getDayOfWeek(m.getMeetingDate()));
			meeting.setStartTime(m.getStartTime().getTime());
			meeting.setStopTime(m.getStopTime().getTime());
			Calendar c = Calendar.getInstance(Locale.US);
			c.setTime(m.getMeetingDate());
			int dayOfYear = c.get(Calendar.DAY_OF_YEAR);
			int sessionYear = session.getSessionStartYear();
			if (c.get(Calendar.YEAR) < sessionYear) {
				Calendar x = Calendar.getInstance(Locale.US);
			    x.set(c.get(Calendar.YEAR),11,31,0,0,0);
			    dayOfYear -= x.get(Calendar.DAY_OF_YEAR);
			} else if (c.get(Calendar.YEAR) > sessionYear) {
				Calendar x = Calendar.getInstance(Locale.US);
			    x.set(sessionYear,11,31,0,0,0);
			    dayOfYear += x.get(Calendar.DAY_OF_YEAR);
			}
			meeting.setDayOfYear(dayOfYear);
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
    	
    	return event;
	}

}
