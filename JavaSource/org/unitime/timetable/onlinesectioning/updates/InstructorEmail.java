package org.unitime.timetable.onlinesectioning.updates;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cpsolver.ifs.util.ToolBox;
import org.unitime.commons.Email;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.CourseUrlProvider;
import org.unitime.timetable.onlinesectioning.model.XConfig;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XInstructor;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XSubpart;
import org.unitime.timetable.onlinesectioning.updates.StudentEmail.Table;
import org.unitime.timetable.onlinesectioning.updates.StudentEmail.TableSectionLine;
import org.unitime.timetable.onlinesectioning.updates.StudentEmail.TableSectionModifiedLine;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.Formats.Format;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class InstructorEmail implements OnlineSectioningAction<Boolean> {
	private static final long serialVersionUID = -6796065761126518698L;
	
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private static GwtMessages GWT = Localization.create(GwtMessages.class);
	
	private static Format<Date> sTimeStampFormat = Formats.getDateFormat(Formats.Pattern.DATE_TIME_STAMP);
	
	private Date iTimeStamp = new Date();
	private InstructorChange iChange;
	private CourseUrlProvider iCourseUrlProvider = null;
	
	public InstructorEmail forChange(InstructorChange ic) {
		iChange = ic;
		return this;
	}
	public InstructorChange getInstructorChange() { return iChange; }
	public Date getTimeStamp() { return iTimeStamp; }
	
	@Override
	public Boolean execute(final OnlineSectioningServer server, final OnlineSectioningHelper helper) {
		try {
			String providerClass = ApplicationProperty.CustomizationCourseLink.value();
			if (providerClass != null)
				iCourseUrlProvider = (CourseUrlProvider)Class.forName(providerClass).getDeclaredConstructor().newInstance();
		} catch (Exception e) {}
		
		XInstructor instructor = getInstructorChange().getInstructor();
		OnlineSectioningLog.Action.Builder action = helper.getAction();
		action.setStudent(
				OnlineSectioningLog.Entity.newBuilder()
				.setExternalId(instructor.getExternalId())
				.setName(helper.getInstructorNameFormat().format(instructor))
				);
		
		if (getInstructorChange().getOldOffering() != null) {
			OnlineSectioningLog.Enrollment.Builder enrollment = OnlineSectioningLog.Enrollment.newBuilder();
			enrollment.setType(OnlineSectioningLog.Enrollment.EnrollmentType.PREVIOUS);
			for (XSection section: getInstructorChange().getOldSections())
				enrollment.addSection(OnlineSectioningHelper.toProto(section, null));
		}
		if (getInstructorChange().getNewOffering() != null) {
			OnlineSectioningLog.Enrollment.Builder enrollment = OnlineSectioningLog.Enrollment.newBuilder();
			enrollment.setType(OnlineSectioningLog.Enrollment.EnrollmentType.STORED);
			for (XSection section: getInstructorChange().getNewSections())
				enrollment.addSection(OnlineSectioningHelper.toProto(section, null));
		}
		
		try {
			Email email = Email.createEmail();

			email.addRecipient(instructor.getEmail(), helper.getInstructorNameFormat().format(instructor));
			helper.logOption("recipient", instructor.getEmail());
			
			String subject = MSG.emailInstructorChangeSubject().replace("%session%", server.getAcademicSession().toString());
			email.setSubject(subject);
			helper.logOption("subject", subject);

			if (helper.getUser() != null) {
				TimetableManager manager = helper.getHibSession().createQuery("from TimetableManager where externalUniqueId = :id", TimetableManager.class).setParameter("id", helper.getUser().getExternalId()).uniqueResult();
				if (manager != null && manager.getEmailAddress() != null) {
					email.setReplyTo(manager.getEmailAddress(), helper.getInstructorNameFormat().format(manager));
					helper.logOption("reply-to", helper.getInstructorNameFormat().format(manager) + " <" + manager.getEmailAddress() + ">");
				}
			}

			String html = generateMessage(server, helper);
			email.setHTML(html);
			
			helper.logOption("email", html);

			email.send();
			
			return true;
		} catch (Exception e) {
			if (e instanceof SectioningException)
				throw (SectioningException)e;
			throw new SectioningException(e.getMessage(), e);
		}
	}
	
	protected String generateMessage(OnlineSectioningServer server, OnlineSectioningHelper helper) throws IOException, TemplateException {
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_0);
		cfg.setClassForTemplateLoading(StudentEmail.class, "/");
		cfg.setLocale(Localization.getJavaLocale());
		cfg.setOutputEncoding("utf-8");
		Template template = cfg.getTemplate(ApplicationProperty.NotificationsInstructorChangeEmailTemplate.value());
		Map<String, Object> input = new HashMap<String, Object>();
		
		input.put("msg", MSG);
		input.put("instructor", getInstructorChange().getInstructor());
		input.put("course", getInstructorChange().getCourse());
		input.put("name", helper.getInstructorNameFormat().format(getInstructorChange().getInstructor()));
		input.put("server", server);
		input.put("helper", helper);
		boolean checkAssignment = ApplicationProperty.NotificationsInstructorChangesCheckShare.isTrue();
		input.put("showAssignmentColumn", checkAssignment);
		
		Table listOfChanges = new Table();
		List<XSection> remaining = new ArrayList<XSection>(getInstructorChange().getNewSections());
		String instructorId = getInstructorChange().getInstructor().getExternalId();
		old: for (XSection oldSection: getInstructorChange().getOldSections()) {
			for (Iterator<XSection> i = remaining.iterator(); i.hasNext(); ) {
				XSection newSection = i.next();
				if (newSection.getSectionId().equals(oldSection.getSectionId())) {
					String requiresOld = null;
					XSection parentOld = (oldSection.getParentId() == null ? null : getInstructorChange().getOldOffering().getSection(oldSection.getParentId()));
					if (parentOld != null)
						requiresOld = parentOld.getName(getInstructorChange().getOldOffering().getControllingCourse().getCourseId());
					String requires = null;
					XSection parent = (newSection.getParentId() == null ? null : getInstructorChange().getNewOffering().getSection(newSection.getParentId()));
					if (parent != null)
						requires = parent.getName(getInstructorChange().getNewOffering().getControllingCourse().getCourseId());
					i.remove();
					if (oldSection.isCancelled() && newSection.isCancelled()) continue old;
					listOfChanges.add(new InstructorTableSectionModifiedLine(
							getInstructorChange().getCourse(),
							instructorId,
							getInstructorChange().getOldOffering().getSubpart(oldSection.getSubpartId()),
							getInstructorChange().getNewOffering().getSubpart(newSection.getSubpartId()),
							oldSection,
							newSection,
							requiresOld, requires, 
							getCourseUrl(server.getAcademicSession(), getInstructorChange().getCourse())
							));
					continue old;
				}
			}
			String requires = null;
			XSection parent = (oldSection.getParentId() == null ? null : getInstructorChange().getOldOffering().getSection(oldSection.getParentId()));
			if (parent != null)
				requires = parent.getName(getInstructorChange().getOldOffering().getControllingCourse().getCourseId());
			listOfChanges.add(new InstructorTableSectionDeletedLine(
					instructorId,
					getInstructorChange().getCourse(),
					getInstructorChange().getOldOffering().getSubpart(oldSection.getSubpartId()),
					oldSection,
					requires,
					getCourseUrl(server.getAcademicSession(), getInstructorChange().getCourse())
					));
		}
		for (XSection newSection: remaining) {
			String requires = null;
			XSection parent = (newSection.getParentId() == null ? null : getInstructorChange().getNewOffering().getSection(newSection.getParentId()));
			if (parent != null)
				requires = parent.getName(getInstructorChange().getNewOffering().getControllingCourse().getCourseId());
			listOfChanges.add(new InstructorTableSectionAddedLine(
					instructorId,
					getInstructorChange().getCourse(),
					getInstructorChange().getNewOffering().getSubpart(newSection.getSubpartId()),
					newSection,
					requires,
					getCourseUrl(server.getAcademicSession(), getInstructorChange().getCourse())
					));
		}
		input.put("changes", listOfChanges);
		
		input.put("subject", MSG.emailInstructorChangeSubject().replace("%session%", server.getAcademicSession().toString()));
		
		if (ApplicationProperty.NotificationsInstructorChangesIncludeSchedule.isTrue()) {
			Table classes = new Table();
			Collection<Long> offerings = server.getInstructedOfferings(instructorId);
			if (offerings != null)
				for (Long offeringId: offerings) {
					XOffering offering = server.getOffering(offeringId);
					if (offering != null)
						for (XConfig config: offering.getConfigs())
							for (XSubpart subpart: config.getSubparts())
								for (XSection section: subpart.getSections()) {
									if (section.isCancelled()) continue;
									for (XInstructor instructor: section.getAllInstructors())
										if (instructorId.equals(instructor.getExternalId())) {
											String requires = null;
											XSection parent = (section.getParentId() == null ? null : offering.getSection(section.getParentId()));
											if (parent != null)
												requires = parent.getName(offering.getControllingCourse().getCourseId());
											classes.add(new InstructorTableSectionLine(
													instructorId,
													offering.getControllingCourse(),
													subpart,
													section,
													requires,
													getCourseUrl(server.getAcademicSession(), getInstructorChange().getCourse())
													));
										}
								}
				}
			input.put("classes", classes);
		}
		
		input.put("version", GWT.pageVersion(Constants.getVersion(), Constants.getReleaseDate()));
		input.put("copyright", GWT.pageCopyright());
		input.put("ts", sTimeStampFormat.format(getTimeStamp()));
		input.put("link", ApplicationProperty.UniTimeUrl.value());
		
		StringWriter s = new StringWriter();
		template.process(input, new PrintWriter(s));
		s.flush(); s.close();

		return s.toString();
	}
	
	public static class InstructorTableSectionLine extends TableSectionLine {
		protected String iInstructorId;
		
		public InstructorTableSectionLine(String externalId, XCourse course, XSubpart subpart, XSection section, String requires, URL url) {
			super(null, course, subpart, section, requires, url);
			iInstructorId = externalId;
		}
		
		@Override
		public String getInstructors() {
			XInstructor instructor = (iInstructorId == null ? null : iSection.getInstructor(iInstructorId));
			if (instructor == null) return "";
			if (instructor.isAllowOverlap()) {
				return "<i>" + (instructor.getResponsibility() == null ? "" : instructor.getResponsibility() + " ") + instructor.getPercentShare() + "%</i>";
			} else {
				return (instructor.getResponsibility() == null ? "" : instructor.getResponsibility() + " ") + instructor.getPercentShare() + "%";
			}
		}
	}
	
	public static class InstructorTableSectionAddedLine extends InstructorTableSectionLine {
		public InstructorTableSectionAddedLine(String externalId, XCourse course, XSubpart subpart, XSection section, String requires, URL url) {
			super(externalId, course, subpart, section, requires, url);
		}
		
		@Override
		public String getNote() {
			if (super.getNote() == null || super.getNote().isEmpty())
				return MSG.emailClassAssigned();
			else
				return MSG.emailClassAssigned() + " " + super.getNote();
		}
	}
	
	public static class InstructorTableSectionDeletedLine extends InstructorTableSectionLine {
		
		public InstructorTableSectionDeletedLine(String externalId, XCourse course, XSubpart subpart, XSection section, String requires, URL url) {
			super(externalId, course, subpart, section, requires, url);
		}
		
		@Override
		public String getCourseNote() { return null; }
		
		@Override
		public String getNote() {
			return MSG.emailClassUnassigned();
		}
	}
	
	public static class InstructorTableSectionModifiedLine extends TableSectionModifiedLine {
		protected String iInstructorId;
		
		public InstructorTableSectionModifiedLine(XCourse course, String instructorId, XSubpart oldSubpart, XSubpart subpart, XSection oldSection, XSection section, String oldRequires, String requires, URL url) {
			super(null, course, oldSubpart, subpart, oldSection, section, oldRequires, requires, url);
			iInstructorId = instructorId;
		}
		
		@Override
		public String getInstructors() {
			XInstructor instructor = (iInstructorId == null ? null : iSection.getInstructor(iInstructorId));
			XInstructor oldInstructor = (iInstructorId == null ? null : iOldSection.getInstructor(iInstructorId));
			String oldass = "";
			
			if (oldInstructor != null) {
				if (oldInstructor.isAllowOverlap()) {
					oldass = "<i>" + (oldInstructor.getResponsibility() == null ? "" : oldInstructor.getResponsibility() + " ") + oldInstructor.getPercentShare() + "%</i>";
				} else {
					oldass = (oldInstructor.getResponsibility() == null ? "" : oldInstructor.getResponsibility() + " ") + oldInstructor.getPercentShare() + "%";
				}
			}
			String newass = "";
			if (instructor != null) {
				if (instructor.isAllowOverlap()) {
					newass = "<i>" + (instructor.getResponsibility() == null ? "" : instructor.getResponsibility() + " ") + instructor.getPercentShare() + "%</i>";
				} else {
					newass = (instructor.getResponsibility() == null ? "" : instructor.getResponsibility() + " ") + instructor.getPercentShare() + "%";
				}
			}
			return diff(oldass, newass);
		}
		
		@Override
		public String getNote() {
			if (iSection.isCancelled()) return MSG.emailClassCancelled();
			if (!iSection.isCancelled() && iOldSection.isCancelled()) return MSG.emailClassReopened();
			return diff(iOldSection.getNote(), iSection.getNote());
		}
		
	}
	
	protected URL getCourseUrl(AcademicSessionInfo session, XCourse course) {
		if (iCourseUrlProvider == null) return null;
		return iCourseUrlProvider.getCourseUrl(session, course.getSubjectArea(), course.getCourseNumber());
	}
	
	@Override
	public String name() {
		return "instructor-email";
	}
	
    
    public static boolean sameAssignment(boolean checkAssignment, XInstructor i1, XInstructor i2) {
    	if (!checkAssignment) return true;
    	return ToolBox.equals(i1.getPercentShare(), i2.getPercentShare()) && ToolBox.equals(i1.getResponsibility(), i2.getResponsibility()) && i1.isAllowOverlap() == i2.isAllowOverlap();
    }
    
	public static class InstructorChange implements Serializable {
		private static final long serialVersionUID = -4307771609804985028L;
		XInstructor iOldInstructor, iNewInstructor;
		XOffering iOldOffering, iNewOffering;
		List<XSection> iOldSections = new ArrayList<XSection>();
		List<XSection> iNewSections = new ArrayList<XSection>();
		
		InstructorChange() {}
		
		public void setOldInstructor(XInstructor instructor) { iOldInstructor = instructor; }
		public XInstructor getOldInstructor() { return iOldInstructor; }
		public void setNewInstructor(XInstructor instructor) { iNewInstructor = instructor; }
		public XInstructor getNewInstructor() { return iNewInstructor; }
		public XInstructor getInstructor() { return iNewInstructor != null ? iNewInstructor : iOldInstructor; }
		
		public void setOldOffering(XOffering offering) { iOldOffering = offering; }
		public XOffering getOldOffering() { return iOldOffering; }
		public void setNewOffering(XOffering offering) { iNewOffering = offering; }
		public XOffering getNewOffering() { return iNewOffering; }

		public XOffering getOffering() { return iNewOffering != null ? iNewOffering : iOldOffering; }
		public XCourse getCourse() { return getOffering().getControllingCourse(); }

		public void addOldSection(XSection section) { iOldSections.add(section); }
		public List<XSection> getOldSections() { return iOldSections; }
		public void addNewSection(XSection section) { iNewSections.add(section); }
		public List<XSection> getNewSections() { return iNewSections; }
		
		public boolean hasEmail() {
			XInstructor instructor = getInstructor();
			return instructor.getEmail() != null && !instructor.getEmail().isEmpty();
		}
		
		public boolean hasChange(boolean checkAssignment) {
			if (iOldSections.size() != iNewSections.size()) return true;
			XCourseId course = getCourse();
			old: for (XSection oldSection: iOldSections) {
				for (XSection newSection: iNewSections) {
					if (ReloadOfferingAction.sameName(course.getCourseId(), newSection, oldSection) &&
							ReloadOfferingAction.sameTime(newSection, oldSection.getTime()) &&
							ReloadOfferingAction.sameRooms(newSection, oldSection.getRooms()) &&
							sameAssignment(checkAssignment, oldSection.getInstructor(iOldInstructor.getExternalId()), newSection.getInstructor(iNewInstructor.getExternalId())) &&
							newSection.isCancelled() == oldSection.isCancelled())
						continue old;
				}
				return true;
			}
			return false;
		}
	}
}
