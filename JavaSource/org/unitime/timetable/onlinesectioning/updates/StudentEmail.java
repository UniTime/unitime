/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2011 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.onlinesectioning.updates;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.activation.DataSource;
import javax.imageio.ImageIO;

import org.cpsolver.ifs.util.ToolBox;
import org.unitime.commons.Email;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XFreeTimeRequest;
import org.unitime.timetable.onlinesectioning.model.XInstructor;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XRoom;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XSubpart;
import org.unitime.timetable.onlinesectioning.model.XTime;
import org.unitime.timetable.onlinesectioning.server.CheckMaster;
import org.unitime.timetable.onlinesectioning.server.CheckMaster.Master;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.Formats.Format;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;


/**
 * @author Tomas Muller
 */
@CheckMaster(Master.REQUIRED)
public class StudentEmail implements OnlineSectioningAction<Boolean> {
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private static StudentSectioningConstants CONST = Localization.create(StudentSectioningConstants.class);
	private static GwtMessages GWT = Localization.create(GwtMessages.class);

	private Date iTimeStamp = new Date();
	private static Format<Date> sTimeStampFormat = Formats.getDateFormat(Formats.Pattern.DATE_TIME_STAMP);
	private static Format<Date> sConsentApprovalDateFormat = Formats.getDateFormat(Formats.Pattern.DATE_REQUEST);
	private String iSubject = MSG.emailDeafultSubject(), iSubjectExt = null, iMessage = null, iCC = null;
	private static Hashtable<Long, String> sLastMessage = new Hashtable<Long, String>();
	private byte[] iTimetableImage = null;
	
	private Long iStudentId;
	private XOffering iOldOffering;
	private XCourseId iOldCourseId;
	private XEnrollment iOldEnrollment;
	private XStudent iOldStudent;
	private XStudent iStudent;
	
	public StudentEmail forStudent(Long studentId) {
		iStudentId = studentId;
		return this;
	}
	
	public StudentEmail oldEnrollment(XOffering oldOffering, XCourseId oldCourseId, XEnrollment oldEnrollment) {
		iOldOffering = oldOffering;
		iOldCourseId = oldCourseId;
		iOldEnrollment = oldEnrollment;
		return this;
	}
	
	public StudentEmail oldStudent(XStudent oldStudent) {
		iOldStudent = oldStudent;
		return this;
	}
	
	public Long getStudentId() { return iStudentId; }

	public Date getTimeStamp() { return iTimeStamp; }
	private String getSubject() { return iSubject; }
	private void setSubject(String subject) { iSubject = subject; }
	public String getEmailSubject() { return iSubjectExt; }
	public void setEmailSubject(String subject) { iSubjectExt = subject; }
	public String getMessage() { return iMessage; }
	public void setMessage(String message) { iMessage = message; }
	public String getCC() { return iCC; }
	public void setCC(String cc) { iCC = cc; }
	
	public XEnrollment getOldEnrollment() { return iOldEnrollment; }
	public XOffering getOldOffering() { return iOldOffering; }
	public XCourse getOldCourse() { return (iOldOffering == null || iOldCourseId == null ? null : iOldOffering.getCourse(iOldCourseId)); }
	public XStudent getOldStudent() { return iOldStudent; }
	public XStudent getStudent() { return iStudent; }
	public void setStudent(XStudent student) { iStudent = student; }

	@Override
	public Boolean execute(final OnlineSectioningServer server, final OnlineSectioningHelper helper) {
		Lock lock = server.lockStudent(getStudentId(), null, name());
		try {
			OnlineSectioningLog.Action.Builder action = helper.getAction();
			action.setStudent(
					OnlineSectioningLog.Entity.newBuilder()
					.setUniqueId(getStudentId()));
			
			if (getOldEnrollment() != null) {
				OnlineSectioningLog.Enrollment.Builder enrollment = OnlineSectioningLog.Enrollment.newBuilder();
				enrollment.setType(OnlineSectioningLog.Enrollment.EnrollmentType.PREVIOUS);
				for (XSection section: getOldOffering().getSections(getOldEnrollment()))
					enrollment.addSection(OnlineSectioningHelper.toProto(section, getOldEnrollment()));
				action.addEnrollment(enrollment);
			} else if (getOldStudent() != null) {
				OnlineSectioningLog.Enrollment.Builder enrollment = OnlineSectioningLog.Enrollment.newBuilder();
				enrollment.setType(OnlineSectioningLog.Enrollment.EnrollmentType.PREVIOUS);
				for (XRequest r: getOldStudent().getRequests()) {
					XEnrollment e = (r instanceof XCourseRequest ? ((XCourseRequest)r).getEnrollment() : null);
					if (e != null)
						for (XSection section: server.getOffering(e.getOfferingId()).getSections(e))
							enrollment.addSection(OnlineSectioningHelper.toProto(section, e));
				}
				action.addEnrollment(enrollment);
			}
			
			XStudent student = server.getStudent(getStudentId());
			if (student == null) return false;
			setStudent(student);
			action.getStudentBuilder().setUniqueId(student.getStudentId()).setExternalId(student.getExternalId()).setName(student.getName());

			OnlineSectioningLog.Enrollment.Builder enrollment = OnlineSectioningLog.Enrollment.newBuilder();
			enrollment.setType(OnlineSectioningLog.Enrollment.EnrollmentType.STORED);
			for (XRequest r: student.getRequests()) {
				action.addRequest(OnlineSectioningHelper.toProto(r));
				XEnrollment e = (r instanceof XCourseRequest ? ((XCourseRequest)r).getEnrollment() : null);
				if (e != null)
					for (XSection section: server.getOffering(e.getOfferingId()).getSections(e))
						enrollment.addSection(OnlineSectioningHelper.toProto(section, e));
			}
			action.addEnrollment(enrollment);
						
			boolean ret = false;
			
			org.unitime.timetable.model.Student dbStudent = StudentDAO.getInstance().get(getStudentId(), helper.getHibSession());
			if (dbStudent != null && dbStudent.getEmail() != null && !dbStudent.getEmail().isEmpty()) {
				action.getStudentBuilder().setName(dbStudent.getEmail());
				boolean emailEnabled = true;
				StudentSectioningStatus status = dbStudent.getSectioningStatus();
				if (status == null) status = dbStudent.getSession().getDefaultSectioningStatus();
				if (status != null && !status.hasOption(StudentSectioningStatus.Option.email)) {
					emailEnabled = false;
				}
				
				if (emailEnabled) {
					final String html = generateMessage(dbStudent, server, helper);
					if (html != null) {
						Email email = Email.createEmail();

						email.addRecipient(dbStudent.getEmail(), helper.getStudentNameFormat().format(dbStudent));
						
						if (getCC() != null && !getCC().isEmpty()) {
							helper.logOption("cc", getCC());
							for (StringTokenizer s = new StringTokenizer(getCC(), ",;"); s.hasMoreTokens(); ) {
								email.addRecipientCC(s.nextToken(), null);
							}
						}
						
						if (getEmailSubject() != null && !getEmailSubject().isEmpty()) {
							email.setSubject(getEmailSubject().replace("%session%", server.getAcademicSession().toString()));
							helper.logOption("subject", getEmailSubject().replace("%session%", server.getAcademicSession().toString()));
						} else
							email.setSubject(getSubject().replace("%session%", server.getAcademicSession().toString()));
						
						if (getMessage() != null && !getMessage().isEmpty())
							helper.logOption("message", getMessage());
						
						if (helper.getUser() != null && getOldEnrollment() == null && getOldStudent() == null) {
							TimetableManager manager = (TimetableManager)helper.getHibSession().createQuery("from TimetableManager where externalUniqueId = :id").setString("id", helper.getUser().getExternalId()).uniqueResult();
							if (manager != null && manager.getEmailAddress() != null) {
								email.setReplyTo(manager.getEmailAddress(), manager.getName());
								helper.logOption("reply-to", manager.getName() + " <" + manager.getEmailAddress() + ">");
							}
						}
						
						final StringWriter buffer = new StringWriter();
						PrintWriter out = new PrintWriter(buffer);
						generateTimetable(out, server, helper);
						out.flush(); out.close();							
						email.addAttachement(new DataSource() {
							@Override
							public OutputStream getOutputStream() throws IOException {
								throw new IOException("No output stream.");
							}
							
							@Override
							public String getName() {
								return "message.html";
							}
							
							@Override
							public InputStream getInputStream() throws IOException {
								return new ByteArrayInputStream(
										html.replace("<img src='cid:timetable.png' border='0' alt='Timetable Grid'/>", buffer.toString()).getBytes("UTF-8"));
							}
							
							@Override
							public String getContentType() {
								return "text/html; charset=UTF-8";
							}
						});
						
						if (iTimetableImage != null) {
							email.addAttachement(new DataSource() {
								@Override
								public OutputStream getOutputStream() throws IOException {
									throw new IOException("No output stream.");
								}
								
								@Override
								public String getName() {
									return "timetable.png";
								}
								
								@Override
								public InputStream getInputStream() throws IOException {
									return new ByteArrayInputStream(iTimetableImage);
								}
								
								@Override
								public String getContentType() {
									return "image/png";
								}
							});
						}
						
						try {
							final String calendar = CalendarExport.getCalendar(server, student);
							if (calendar != null)
								email.addAttachement(new DataSource() {
									@Override
									public OutputStream getOutputStream() throws IOException {
										throw new IOException("No output stream.");
									}
									
									@Override
									public String getName() {
										return "timetable.ics";
									}
									
									@Override
									public InputStream getInputStream() throws IOException {
										return new ByteArrayInputStream(calendar.getBytes("UTF-8"));
									}
									
									@Override
									public String getContentType() {
										return "text/calendar; charset=UTF-8";
									}
								});
						} catch (IOException e) {
							helper.warn("Unable to create calendar for student " + student.getStudentId() + ":" + e.getMessage());
						}
						
						String lastMessageId = sLastMessage.get(student.getStudentId());
						if (lastMessageId != null)
							email.setInReplyTo(lastMessageId);
						
						email.setHTML(html);
						
						helper.logOption("email", html.replace("<img src='cid:timetable.png' border='0' alt='Timetable Image'/>", buffer.toString()));

						email.send();
						
						String messageId = email.getMessageId();
						if (messageId != null)
							sLastMessage.put(student.getStudentId(), messageId);
						
						Date ts = new Date();
						dbStudent.setScheduleEmailedDate(ts);
						student.setEmailTimeStamp(ts);
						
						helper.getHibSession().saveOrUpdate(dbStudent);
						helper.getHibSession().flush();
						
						server.update(student, false);
						
						ret = true;
					} else {
						helper.debug("Email notification failed to generate for student " + student.getName() + ".");
					}
				} else {
					helper.debug("Email notification is disabled for student " + student.getName() + ".");
				}
			} else {
				helper.debug("Student " + student.getName() + " has no email address on file.");
			}
			
			return ret;
		} catch (Exception e) {
			if (e instanceof SectioningException)
				throw (SectioningException)e;
			throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
		} finally {
			lock.release();
		}
	}

	@Override
	public String name() {
		return "student-email";
	}
	
	private static String[] sColor1 = new String[] {
			"2952A3",
			//"A32929",
			"B1365F",
			"7A367A",
			"5229A3",
			"29527A",
			"1B887A",
			"28754E",
			"0D7813",
			"528800",
			"88880E",
			"AB8B00",
			"BE6D00",
			"B1440E",
			"865A5A",
			"705770",
			"4E5D6C",
			"5A6986",
			"4A716C",
			"6E6E41",
			"8D6F47"
	};
	
	private static String[] sColor2 = new String[] {
			"668CD9",
			//"D96666",
			"E67399",
			"B373B3",
			"8C66D9",
			"668CB3",
			"59BFB3",
			"65AD89",
			"4CB052",
			"8CBF40",
			"BFBF4D",
			"E0C240",
			"F2A640",
			"E6804D",
			"BE9494",
			"A992A9",
			"8997A5",
			"94A2bE",
			"85AAA5",
			"A7A77D",
			"C4A883"
	};
	
	private String generateMessage(org.unitime.timetable.model.Student student, OnlineSectioningServer server, OnlineSectioningHelper helper)  throws IOException, TemplateException {
		Configuration cfg = new Configuration();
		cfg.setClassForTemplateLoading(StudentEmail.class, "");
		cfg.setLocale(Localization.getJavaLocale());
		cfg.setOutputEncoding("utf-8");
		Template template = cfg.getTemplate("StudentEmail.ftl");
		Map<String, Object> input = new HashMap<String, Object>();
		
		input.put("msg", MSG);
		if (getEmailSubject() != null && !getEmailSubject().isEmpty())
			input.put("subject", getEmailSubject().replace("%session%", server.getAcademicSession().toString()));
		input.put("student", getStudent());
		input.put("name", helper.getStudentNameFormat().format(student));
		input.put("server", server);
		input.put("helper", helper);
		input.put("message", getMessage());
		input.put("dfConsentApproval", sConsentApprovalDateFormat);
		
		input.put("classes", generateListOfClasses(student, server, helper));

		
		if (!getStudent().getRequests().isEmpty()) {
			try {
				iTimetableImage = generateTimetableImage(server);
			} catch (Exception e) {
				helper.error("Unable to create timetable image: " + e.getMessage(), e);
				StringWriter buffer = new StringWriter();
				PrintWriter out = new PrintWriter(buffer);
				generateTimetable(out, server, helper);
				out.flush(); out.close();							
				input.put("timetable", buffer.toString());
			}
			if (iTimetableImage != null)
				input.put("timetable", "<img src='cid:timetable.png' border='0' alt='Timetable Grid'/>");
		}
		
		if (getOldOffering() != null) {
			Table listOfChanges = new Table();

			XCourseRequest newRequest = null;
			XOffering newOffering = null;
			XCourse course = (getOldEnrollment() != null ? getOldOffering().getCourse(getOldEnrollment().getCourseId()) : getOldCourse());
			for (XRequest r: getStudent().getRequests()) {
				if (r instanceof XCourseRequest && (
						(getOldCourse() == null && ((XCourseRequest)r).getCourseIdByOfferingId(getOldOffering().getOfferingId()) != null) ||
						(getOldCourse() != null && ((XCourseRequest)r).hasCourse(getOldCourse().getCourseId()))
						)) {
					newRequest = (XCourseRequest)r;
					newOffering = server.getOffering(getOldOffering().getOfferingId());
					if (newRequest.getEnrollment() != null)
						course = newOffering.getCourse(newRequest.getEnrollment().getCourseId());
					break;
				}
			}
			input.put("changedCourse", course);

			if (getOldEnrollment() == null && newRequest != null && newRequest.getEnrollment() != null) {
				setSubject(MSG.emailEnrollmentNew(course.getSubjectArea(), course.getCourseNumber()));
				XEnrollment enrollment = newRequest.getEnrollment();
				String consent = consent(server, enrollment);
				for (XSection section: newOffering.getSections(enrollment)) {
					XSection parent = (section.getParentId() == null ? null : newOffering.getSection(section.getParentId()));
					XSubpart subpart = newOffering.getSubpart(section.getSubpartId());
					String requires = null;
					if (parent != null) {
						requires = parent.getName(course.getCourseId());
					} else {
						requires = consent; consent = null;
					}
					listOfChanges.add(new TableSectionLine(newRequest, course, subpart, section, requires));
				}
				input.put("changes", listOfChanges);
			} else if (getOldEnrollment() != null && newRequest != null && newRequest.getEnrollment() != null) {
				setSubject(MSG.emailEnrollmentChanged(course.getSubjectArea(), course.getCourseNumber()));

				String consent = consent(server, newRequest.getEnrollment());
				sections: for (XSection section: newOffering.getSections(newRequest.getEnrollment())) {
					XSection parent = (section.getParentId() == null ? null : newOffering.getSection(section.getParentId()));
					XSubpart subpart = newOffering.getSubpart(section.getSubpartId());
					
					for (XSection old: getOldOffering().getSections(getOldEnrollment())) {
						if (old.getSubpartId().equals(section.getSubpartId())) {
							String requires = null;
							if (parent != null)
								requires = parent.getName(course.getCourseId());
							
							XSubpart oldSubpart = getOldOffering().getSubpart(old.getSubpartId());
							XSection oldParent = (old.getParentId() == null ? null : getOldOffering().getSection(old.getParentId()));
							String oldRequires = null;
							if (oldParent != null)
								oldRequires = oldParent.getName(course.getCourseId());
							
							if (oldRequires == null && requires == null) {
								requires = consent;
								oldRequires = consent;
								consent = null;
							}

							listOfChanges.add(new TableSectionModifiedLine(newRequest, course, oldSubpart, subpart, old, section, oldRequires, requires));
							continue sections;
						}
					}
					
					String requires = null;
					if (parent != null) {
						requires = parent.getName(course.getCourseId());
					} else {
						requires = consent; consent = null;
					}
					listOfChanges.add(new TableSectionLine(newRequest, course, subpart, section, requires));
				}
				sections: for (XSection old: getOldOffering().getSections(getOldEnrollment())) {
					for (XSection section: newOffering.getSections(newRequest.getEnrollment()))
						if (old.getSubpartId().equals(section.getSubpartId())) continue sections;
					
					XSubpart subpart = getOldOffering().getSubpart(old.getSubpartId());
					XSection parent = (old.getParentId() == null ? null : getOldOffering().getSection(old.getParentId()));
					String requires = null;
					if (parent != null)
						requires = parent.getName(course.getCourseId());
					listOfChanges.add(new TableSectionDeletedLine(newRequest, course, subpart, old, requires));
				}

				input.put("changes", listOfChanges);
			} else if (getOldEnrollment() != null && (newRequest == null || newRequest.getEnrollment() == null)) {
				setSubject(newRequest == null
						? MSG.emailCourseDropReject(course.getSubjectArea(), course.getCourseNumber())
						: MSG.emailCourseDropChange(course.getSubjectArea(), course.getCourseNumber()));
				if (newRequest !=  null && getStudent().canAssign(newRequest)) {
					input.put("changeMessage", (newRequest.isAlternative() ?
							newRequest.isWaitlist() ? MSG.emailCourseWaitListedAlternative() : MSG.emailCourseNotEnrolledAlternative() :
							newRequest.isWaitlist() ? MSG.emailCourseWaitListed() : MSG.emailCourseNotEnrolled()));
				} else if (newRequest == null && course.getConsentLabel() != null) {
					input.put("changeMessage", MSG.emailConsentRejected(course.getConsentLabel().toLowerCase()));
				}
			}
		} else if (getOldStudent() != null && !getOldStudent().getRequests().isEmpty()) {
			boolean somethingWasAssigned = false;
			for (XRequest or: getOldStudent().getRequests()) {
				if (or instanceof XCourseRequest && ((XCourseRequest)or).getEnrollment() != null) {
					somethingWasAssigned = true; break;
				}
			}
			if (somethingWasAssigned) {
				Table listOfChanges = new Table();
				requests: for (XRequest nr: getStudent().getRequests()) {
					if (nr instanceof XFreeTimeRequest) continue;
					XCourseRequest ncr = (XCourseRequest)nr;
					for (XRequest or: getOldStudent().getRequests()) {
						if (or instanceof XFreeTimeRequest) continue;
						XCourseRequest ocr = (XCourseRequest)or;
						if (or.getRequestId().equals(nr.getRequestId())) {
							if (ocr.getEnrollment() == null) {
								if (ncr.getEnrollment() == null) continue; // both unassigned
								// was assigned
								String consent = consent(server, ncr.getEnrollment());
								XOffering no = server.getOffering(ncr.getEnrollment().getOfferingId());
								XCourse course = no.getCourse(ncr.getEnrollment().getCourseId());
								for (XSection section: no.getSections(ncr.getEnrollment())) {
									XSubpart subpart = no.getSubpart(section.getSubpartId());
									XSection parent = (section.getParentId() == null ? null : no.getSection(section.getParentId()));
									String requires = null;
									if (parent != null) {
										requires = parent.getName(course.getCourseId());
									} else {
										requires = consent; consent = null;
									}
									listOfChanges.add(new TableSectionLine(ncr, course, subpart, section, requires));
								}
							} else if (ncr.getEnrollment() == null) {
								XOffering oo = server.getOffering(ocr.getEnrollment().getOfferingId());
								XCourse course = oo.getCourse(ocr.getEnrollment().getCourseId());
								// was un-assigned
								for (XSection section: oo.getSections(ocr.getEnrollment())) {
									XSubpart subpart = oo.getSubpart(section.getSubpartId());
									XSection parent = (section.getParentId() == null ? null : oo.getSection(section.getParentId()));
									String requires = null;
									if (parent != null)
										requires = parent.getName(course.getCourseId());
									listOfChanges.add(new TableSectionDeletedLine(ncr, course, subpart, section, requires));
								}
							} else {
								XOffering no = server.getOffering(ncr.getEnrollment().getOfferingId());
								XOffering oo = server.getOffering(ocr.getEnrollment().getOfferingId());
								// both assigned
								XCourse course = no.getCourse(ncr.getEnrollment().getCourseId());
								String consent = consent(server, ncr.getEnrollment());
								sections: for (XSection section: no.getSections(ncr.getEnrollment())) {
									for (XSection old: oo.getSections(ocr.getEnrollment())) {
										if (old.getSubpartId().equals(section.getSubpartId())) {
											if (equals(section, old)) continue sections;
											
											XSubpart subpart = no.getSubpart(section.getSubpartId());
											XSection parent = (section.getParentId() == null ? null : no.getSection(section.getParentId()));
											String requires = null;
											if (parent != null)
												requires = parent.getName(course.getCourseId());
											
											XSubpart oldSubpart = oo.getSubpart(old.getSubpartId());
											XSection oldParent = (old.getParentId() == null ? null : oo.getSection(old.getParentId()));
											String oldRequires = null;
											if (oldParent != null)
												oldRequires = oldParent.getName(course.getCourseId());
											
											if (oldRequires == null && requires == null) {
												requires = consent;
												oldRequires = consent;
												consent = null;
											}
											
											listOfChanges.add(new TableSectionModifiedLine(ncr, course, oldSubpart, subpart, old, section, oldRequires, requires));
											continue sections;
										}
									}
									
									XSubpart subpart = no.getSubpart(section.getSubpartId());
									XSection parent = (section.getParentId() == null ? null : no.getSection(section.getParentId()));
									String requires = null;
									if (parent != null) {
										requires = parent.getName(course.getCourseId());
									} else {
										requires = consent; consent = null;
									}
									listOfChanges.add(new TableSectionLine(ncr, course, subpart, section, requires));
								}
								course = oo.getCourse(ocr.getEnrollment().getCourseId());
								sections: for (XSection old: oo.getSections(ocr.getEnrollment())) {
									for (XSection section: no.getSections(ncr.getEnrollment()))
										if (old.getSubpartId().equals(section.getSubpartId())) continue sections;
									
									XSubpart subpart = oo.getSubpart(old.getSubpartId());
									XSection parent = (old.getParentId() == null ? null : oo.getSection(old.getParentId()));
									String requires = null;
									if (parent != null)
										requires = parent.getName(course.getCourseId());

									listOfChanges.add(new TableSectionDeletedLine(ocr, course, subpart, old, requires));
								}
							}
							continue requests;
						}
					}
					// old request not found
					if (ncr.getEnrollment() != null) {
						XOffering no = server.getOffering(ncr.getEnrollment().getOfferingId());
						XCourse course = no.getCourse(ncr.getEnrollment().getCourseId());
						String consent = consent(server, ncr.getEnrollment());
						for (XSection section: no.getSections(ncr.getEnrollment())) {
							XSubpart subpart = no.getSubpart(section.getSubpartId());
							XSection parent = (section.getParentId() == null ? null : no.getSection(section.getParentId()));
							String requires = null;
							if (parent != null) {
								requires = parent.getName(course.getCourseId());
							} else {
								requires = consent; consent = null;
							}
							listOfChanges.add(new TableSectionLine(ncr, course, subpart, section, requires));
						}
					}
				}
				requests: for (XRequest or: getOldStudent().getRequests()) {
					if (or instanceof XFreeTimeRequest || ((XCourseRequest)or).getEnrollment() == null) continue;
					for (XRequest nr: getStudent().getRequests()) {
						if (or instanceof XFreeTimeRequest) continue;
						if (or.getRequestId().equals(nr.getRequestId())) continue requests;
					}
					// new request not found
					XCourseRequest ocr = (XCourseRequest)or;
					XOffering oo = server.getOffering(ocr.getEnrollment().getOfferingId());
					XCourse course = oo.getCourse(ocr.getEnrollment().getCourseId());
					for (XSection section: oo.getSections(((XCourseRequest)or).getEnrollment())) {
						XSubpart subpart = oo.getSubpart(section.getSubpartId());
						XSection parent = (section.getParentId() == null ? null : oo.getSection(section.getParentId()));
						String requires = null;
						if (parent != null)
							requires = parent.getName(course.getCourseId());

						listOfChanges.add(new TableSectionDeletedLine(ocr, course, subpart, section, requires));
					}
				}
				
				input.put("changes", listOfChanges);				
			} else {
				setSubject(MSG.emailSubjectNotification());
			}
		} else {
			setSubject(MSG.emailSubjectNotification());
		}
		
		input.put("manager", helper.getUser() != null && helper.getUser().getType() == OnlineSectioningLog.Entity.EntityType.MANAGER);
		input.put("changed", getOldEnrollment() != null || getOldStudent() != null);
		input.put("version", GWT.pageVersion(Constants.getVersion(), Constants.getReleaseDate()));
		input.put("copyright", GWT.pageCopyright());
		input.put("ts", sTimeStampFormat.format(getTimeStamp()));
		input.put("link", ApplicationProperty.UniTimeUrl.value());
		
		StringWriter s = new StringWriter();
		template.process(input, new PrintWriter(s));
		s.flush(); s.close();

		return s.toString();
	}
	
	Table generateListOfClasses(org.unitime.timetable.model.Student student, OnlineSectioningServer server, OnlineSectioningHelper helper) {
		Table listOfClasses = new Table();
		for (XRequest request: getStudent().getRequests()) {
			if (request instanceof XCourseRequest) {
				XCourseRequest cr = (XCourseRequest)request;
				XEnrollment enrollment = cr.getEnrollment();
				if (enrollment == null) {
					if (!getStudent().canAssign(cr)) continue;
					XCourse course = server.getCourse(cr.getCourseIds().get(0).getCourseId());
					listOfClasses.add(new TableCourseLine(cr, course));
				} else {
					XOffering offering = server.getOffering(enrollment.getOfferingId());
					XCourse course = offering.getCourse(enrollment.getCourseId());
					String consent = consent(server, enrollment);
					for (XSection section: offering.getSections(enrollment)) {
						XSection parent = (section.getParentId() == null ? null : offering.getSection(section.getParentId()));
						XSubpart subpart = offering.getSubpart(section.getSubpartId());
						String requires = null;
						if (parent != null) {
							requires = parent.getName(course.getCourseId());
						} else {
							requires = consent; consent = null;
						}
						listOfClasses.add(new TableSectionLine(cr, course, subpart, section, requires));
					}
				}
			}
			if (request instanceof XFreeTimeRequest) {
				listOfClasses.add(new TableLineFreeTime((XFreeTimeRequest)request));
			}
		}
		return listOfClasses;
	}
	
	public static boolean equals(XSection a, XSection b) {
		return
			ToolBox.equals(a.getName(), b.getName()) &&
			ToolBox.equals(a.getTime(), b.getTime()) &&
			ToolBox.equals(a.getRooms(), b.getRooms()) && 
			ToolBox.equals(a.getInstructors(), b.getInstructors()) &&
			ToolBox.equals(a.getParentId(), b.getParentId());
	}
	
	private static String startTime(XTime time) {
		return OnlineSectioningHelper.getTimeString(time.getSlot());
	}
	
	private static String endTime(XTime time) {
		return OnlineSectioningHelper.getTimeString(time.getSlot() + time.getLength(), time.getBreakTime());
	}

	private String consent(OnlineSectioningServer server, XEnrollment enrollment) {
		if (enrollment == null || enrollment.getCourseId() == null) return null;
		XCourse info = server.getCourse(enrollment.getCourseId());
		if (info == null || info.getConsentLabel() == null) return null;
		if (enrollment.getApproval() == null)
			return MSG.consentWaiting(info.getConsentLabel().toLowerCase());
		else 
			return MSG.consentApproved(sConsentApprovalDateFormat.format(enrollment.getApproval().getTimeStamp()));
	}
	
	public void generateTimetable(PrintWriter out, OnlineSectioningServer server, OnlineSectioningHelper helper) {
		int nrDays = 5, firstHour = 7, lastHour = 18;
		boolean hasSat = false, hasSun = false;
		List<XSection> table[][] = new List[Constants.NR_DAYS][Constants.SLOTS_PER_DAY];
		for (XRequest request: getStudent().getRequests()) {
			if (request instanceof XFreeTimeRequest) {
				XFreeTimeRequest ft = (XFreeTimeRequest)request;
				int dayCode = ft.getTime().getDays();
				if ((dayCode & Constants.DAY_CODES[Constants.DAY_SAT]) != 0) hasSat = true;
				if ((dayCode & Constants.DAY_CODES[Constants.DAY_SUN]) != 0) hasSun = true;
				int startHour = (ft.getTime().getSlot() * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN) / 60;
				if (startHour < firstHour) firstHour = startHour;
				int endHour = ((ft.getTime().getSlot() + ft.getTime().getLength()) * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN + 59) / 60;
				if (endHour > lastHour) lastHour = endHour;
			} else if (((XCourseRequest)request).getEnrollment() != null) {
				XOffering offering = server.getOffering(((XCourseRequest)request).getEnrollment().getOfferingId());
				for (XSection section: offering.getSections(((XCourseRequest)request).getEnrollment())) {
					if (section.getTime() == null) continue;
					int dayCode = section.getTime().getDays();
					if ((dayCode & Constants.DAY_CODES[Constants.DAY_SAT]) != 0) hasSat = true;
					if ((dayCode & Constants.DAY_CODES[Constants.DAY_SUN]) != 0) hasSun = true;
					int startHour = (section.getTime().getSlot() * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN) / 60;
					if (startHour < firstHour) firstHour = startHour;
					int endHour = ((section.getTime().getSlot() + section.getTime().getLength()) * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN + 59) / 60;
					if (endHour > lastHour) lastHour = endHour;
					for (Enumeration<Integer> e = section.getTime().getSlots(); e.hasMoreElements(); ) {
						int slot = e.nextElement();
						int day = slot / Constants.SLOTS_PER_DAY;
						int time = slot % Constants.SLOTS_PER_DAY;
						if (table[day][time] == null)
							table[day][time] = new ArrayList<XSection>();
						table[day][time].add(section);
					}
				}				
			}
		}
		if (hasSat) nrDays = 6;
		if (hasSun) nrDays = 7;
		
		out.println("<table cellspacing='0' cellpadding='0'>");
		
		out.println("<tr><td align='left' style='vertical-align: top;'><table cellspacing='0' cellpadding='0'><tr>");
		
		out.println("<td align='left' style='vertical-align: top;'><div style='font-size: x-small; text-align: center; color: #6991CE; display: block; width: 30px;'></div></td>");
		
		for (int i = 0; i < nrDays; i++) {
			out.println("<td align='center' style='vertical-align: top;'><div style='font-size: x-small; text-align: center; color: #6991CE; display: block; width: 180px; '>" +
					DayCode.values()[i].getName() +  "</div></td>");
		}
		
		out.println("</tr></table></td></tr>");
		
		out.println("<tr><td align='left' style='vertical-align: top;'><div style='width: " + (35 + 180 * nrDays) + "px; '>");
		
		out.println("<table cellspacing='0' cellpadding='0'><tr>");
		
		out.println("<td align='left' style='vertical-align: top; '><div style='position: relative; overflow-x: hidden; overflow-y: hidden; width: 30px; height: " + (50 * (lastHour - firstHour)) + "px; '>");
		for (int h = firstHour; h < lastHour; h++) {
			int top = 50 * (h - firstHour);
			out.println("<div style='font-size: x-small; text-align: center; padding-right: 2px; color: #6991CE; display: block; border-top: 1px solid transparent; height: 100%; width: 28px; white-space: nowrap; " +
				"position: absolute; left: 0px; top: " + top + "px;'>" +  (CONST.useAmPm() ? (h > 12 ? h - 12 : h) + (h < 12 ? "am" : "pm") : String.valueOf(h)) + "</div>");
			out.println("<div style='font-size: x-small; text-align: center; padding-right: 2px; color: #6991CE; display: block; border-top: 1px solid transparent; height: 100%; width: 28px;" +
				"position: absolute; left: 0px; top: " + (25 + top) + "px; '></div>");
		}
		out.println("</div></td>");

		out.println("<td align='left' style='vertical-align: top; '>");
		out.println("<div style='border-bottom: 1px solid #DDDDDD; position: relative; overflow-x: hidden; overflow-y: hidden; width: " + (5 + 180 * nrDays) + "px; height: " + (50 * (lastHour - firstHour)) + "px; '>");
		
		out.println("<div style='position: relative; overflow-x: hidden; overflow-y: hidden; width: 100%; height: 100%; '>");
		
		// working hours
		out.println("<div style='background: #FFFDDD; width: " + (2 + 180 * nrDays) + "px; height: 500px; position: absolute; left: 0px; top: " + (25  + 50 * (7 - firstHour)) + "px;'></div>");
		
		for (XRequest request: getStudent().getRequests()) {
			if (request instanceof XFreeTimeRequest) {
				XFreeTimeRequest fr = (XFreeTimeRequest)request;
				for (DayCode dow: DayCode.toDayCodes(fr.getTime().getDays())) {
					if (dow.getIndex() >= nrDays) continue;
					if (fr.getTime().getSlot() + fr.getTime().getLength() < 12 * firstHour) continue;
					if (fr.getTime().getSlot() > 12 * lastHour) continue;
					out.println("<div style='background: #FFE1DD; width: 100%; color: #BA5353; font-size: x-small; text-align: left; white-space: nowrap; overflow: hidden;" +
							"width: 183px; height: " + (125 * fr.getTime().getLength() / 30) + "px; " +
							"position: absolute; left: " + (180 * dow.getIndex()) + "px;" +
							"top: " + (125 * fr.getTime().getSlot() / 30 - 50 * firstHour) + "px; '>");
					out.println("<div style='padding-left: 5px; white-space: nowrap; '>Free " +
							DayCode.toString(fr.getTime().getDays()) + " " + startTime(fr.getTime()) + " - " + endTime(fr.getTime()) + "</div>");
					out.println("</div>");
				}
			}
		}
		
		for (int h = firstHour; h < lastHour; h++) {
			int top = 50 * (h - firstHour);
			out.println("<div style='display: block; border-top: 1px solid #DDDDDD; width: 100%; position: absolute; left: 0px; top: " + top + "px; '></div>");
			out.println("<div style='display: block; border-top: 1px dotted #DDDDDD; width: 100%; position: absolute; left: 0px; top: " + (25 + top) + "px; '></div>");
		}

		for (int i = 0; i <= nrDays; i++) {
			int left = 180 * i;
			out.println("<div style='height: 100%; position: absolute; top: 0px; left: 0%; border-left: 1px solid #DDDDDD; border-right: 1px solid #DDDDDD; width: 2px; " +
					"position: absolute; left: " + left + "px; top: 0px; '></div>");
		}
		
		
		
		out.println("</div>");
		
		int color = 0;
		for (XRequest request: getStudent().getRequests()) {
			if (request instanceof XCourseRequest && ((XCourseRequest)request).getEnrollment() != null) {
				XOffering offering = server.getOffering(((XCourseRequest)request).getEnrollment().getOfferingId());
				XCourse course = offering.getCourse(((XCourseRequest)request).getEnrollment().getCourseId());
				for (XSection section: offering.getSections(((XCourseRequest)request).getEnrollment())) {
					if (section.getTime() == null) continue;
					for (DayCode dow: DayCode.toDayCodes(section.getTime().getDays())) {
						int col = 0;
						int index = 0;
						for (int i = 0; i < section.getTime().getLength(); i++) {
							col = Math.max(col, table[dow.getIndex()][section.getTime().getSlot() + i].size());
							index = Math.max(index, table[dow.getIndex()][section.getTime().getSlot() + i].indexOf(section));
						}
						int w =  174 / col + (index + 1 != col && col > 1 ? -3 : 0);
						int h = 125 * section.getTime().getLength() / 30 - 3;
						int l = 4 + 180 * dow.getIndex() + index * 174 / col;
						int t = 1 + 125 * section.getTime().getSlot() / 30 - 50 * firstHour;
						out.println("<div style='overflow-x: hidden; overflow-y: hidden; width: " + w + "px; height: " + h + "px; position: absolute; left: " + l + "px; top: " + t + "px; " +
								"position: absolute; font-size: x-small; font-family: arial; overflow: hidden; -webkit-border-radius: 6px; -moz-border-radius: 6px; color: #FFFFFF; " +
								"border: 1px solid #" + sColor1[color] + "; background: #" + sColor2[color] + ";'>");
						out.println("<table cellspacing='0' cellpadding='0' style='padding-left: 4px; padding-right: 4px; padding-bottom: 2px; padding-top: 2px; width: 100%; -webkit-border-top-left-radius: 5px; -webkit-border-top-right-radius: 5px; -moz-border-radius-topleft: 5px; -moz-border-radius-topright: 5px;" +
								"background: #" + sColor1[color] + ";'><tr><td align='left' style='vertical-align: top; '>");
						out.println("<div style='padding-left: 2px; width: 100%; font-size: x-small; white-space: nowrap; overflow: hidden; color: #FFFFFF;'>" +
								MSG.course(course.getSubjectArea(), course.getCourseNumber()) + " " +
								section.getSubpartName() + "</div></td></tr></tbody></table>");
						out.println("<div style='font-size: x-small; padding-left: 4px; white-space: wrap; -webkit-border-bottom-left-radius: 5px; -webkit-border-bottom-right-radius: 5px; -moz-border-radius-bottomleft: 5px; -moz-border-radius-bottomright: 5px;'>");
						if (section.getRooms() != null)
							for (XRoom room: section.getRooms()) {
								out.println("<span style='white-space: nowrap'>" + room.getName() + ",</span>");
							}
						for (XInstructor instructor: section.getInstructors()) {
							out.println("<span style='white-space: nowrap'>" + instructor.getName() + ",</span>");
						}
						if (section.getTime().getDatePatternName() != null && !section.getTime().getDatePatternName().isEmpty()) {
							out.println("<span style='white-space: nowrap'>" + section.getTime().getDatePatternName() + "</span>");
						}
						if (course.getNote() != null && !course.getNote().isEmpty())
							out.println("<br>" + course.getNote().replace("\n", "<br>"));
						if (section.getNote() != null && !section.getNote().isEmpty())
							out.println("<br>" + section.getNote().replace("\n", "<br>"));
						out.println("</div></div>");
					}
				}
				color = (1 + color) % sColor1.length;
			}
		}
		
		out.println("</div></td></tr></table></div></td></tr>");
		
		/*
		out.println("	<tr><td style=\"font-size: 9pt; font-style: italic; color: #9CB0CE; text-align: right; margin-top: -2px; white-space: nowrap;\">");
		out.println("		If the timetable is not displayed correctly, please check out the attached file.");
		out.println("	</td></tr>");
		 */
		
		out.println("</table>");
	}
	
	public byte[] generateTimetableImage(OnlineSectioningServer server) throws IOException {
		int nrDays = 5, firstHour = 7, lastHour = 18;
		boolean hasSat = false, hasSun = false;
		List<XSection> table[][] = new List[Constants.NR_DAYS][Constants.SLOTS_PER_DAY];
		for (XRequest request: getStudent().getRequests()) {
			if (request instanceof XFreeTimeRequest) {
				XFreeTimeRequest ft = (XFreeTimeRequest)request;
				int dayCode = ft.getTime().getDays();
				if ((dayCode & Constants.DAY_CODES[Constants.DAY_SAT]) != 0) hasSat = true;
				if ((dayCode & Constants.DAY_CODES[Constants.DAY_SUN]) != 0) hasSun = true;
				int startHour = (ft.getTime().getSlot() * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN) / 60;
				if (startHour < firstHour) firstHour = startHour;
				int endHour = ((ft.getTime().getSlot() + ft.getTime().getLength()) * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN + 59) / 60;
				if (endHour > lastHour) lastHour = endHour;
			} else if (((XCourseRequest)request).getEnrollment() != null) {
				XOffering offering = server.getOffering(((XCourseRequest)request).getEnrollment().getOfferingId());
				for (XSection section: offering.getSections(((XCourseRequest)request).getEnrollment())) {
					if (section.getTime() == null) continue;
					int dayCode = section.getTime().getDays();
					if ((dayCode & Constants.DAY_CODES[Constants.DAY_SAT]) != 0) hasSat = true;
					if ((dayCode & Constants.DAY_CODES[Constants.DAY_SUN]) != 0) hasSun = true;
					int startHour = (section.getTime().getSlot() * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN) / 60;
					if (startHour < firstHour) firstHour = startHour;
					int endHour = ((section.getTime().getSlot() + section.getTime().getLength()) * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN + 59) / 60;
					if (endHour > lastHour) lastHour = endHour;
					for (Enumeration<Integer> e = section.getTime().getSlots(); e.hasMoreElements(); ) {
						int slot = e.nextElement();
						int day = slot / Constants.SLOTS_PER_DAY;
						int time = slot % Constants.SLOTS_PER_DAY;
						if (table[day][time] == null)
							table[day][time] = new ArrayList<XSection>();
						table[day][time].add(section);
					}
				}				
			}
		}
		if (hasSat) nrDays = 6;
		if (hasSun) nrDays = 7;
		
        BufferedImage image = new BufferedImage(39 + 180 * nrDays, 21 + 50 * (lastHour - firstHour), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        g.setFont(new Font("Sans Serif", Font.TRUETYPE_FONT, 11));
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0xff, 0xff, 0xff));
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        
        int fh = g.getFontMetrics().getHeight();
        
        g.setColor(new Color(0x69, 0x91, 0xce));
		for (int i = 0; i < nrDays; i++) {
	        g.drawString(DayCode.values()[i].getName(), 40 + i * 180, 17);
		}
		
		for (int h = firstHour; h < lastHour; h++) {
			int top = 20 + 50 * (h - firstHour);
			g.drawString(CONST.useAmPm() ? (h > 12 ? h - 12 : h) + (h < 12 ? "am" : "pm") : String.valueOf(h), 2, top + fh);
		}
		
		g.setColor(new Color(0xff, 0xfd, 0xdd));
		g.fillRect(35, 20 + 25  + 50 * (7 - firstHour), 5 + 180 * nrDays, 501);
		
		Stroke noStroke = g.getStroke();
		Stroke dotted = new BasicStroke(1f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1f, new float[] {2f, 2f}, 0f);
		g.setColor(new Color(0xdd, 0xdd, 0xdd));
		for (int h = firstHour; h < lastHour; h++) {
			int top = 20 + 50 * (h - firstHour);
			g.setStroke(noStroke);
			g.drawLine(35, top, (39 + 180 * nrDays), top);
			g.setStroke(dotted);
			g.drawLine(35, top + 25, (39 + 180 * nrDays), top + 25);
		}
		g.setStroke(noStroke);
		g.drawLine(35, 20 + 50 * (lastHour - firstHour), (39 + 180 * nrDays), 20 + 50 * (lastHour - firstHour));
		
		g.setColor(new Color(0xff, 0xe1, 0xdd));
		for (XRequest request: getStudent().getRequests()) {
			if (request instanceof XFreeTimeRequest) {
				XFreeTimeRequest fr = (XFreeTimeRequest)request;
				for (DayCode dow: DayCode.toDayCodes(fr.getTime().getDays())) {
					g.fillRect(36 + 180 * dow.getIndex(), 21 + 125 * fr.getTime().getSlot() / 30 - 50 * firstHour, 182, 125 * fr.getTime().getLength() / 30 - 1);
				}
			}
		}

		g.setColor(new Color(0xdd, 0xdd, 0xdd));
		for (int i = 0; i <= nrDays; i++) {
			g.drawLine(35 + 180 * i, 20, 35 + 180 * i, 20 + 50 * (lastHour - firstHour));
			g.drawLine(38 + 180 * i, 20, 38 + 180 * i, 20 + 50 * (lastHour - firstHour));
		}
		
		g.setColor(new Color(0xba, 0x53, 0x53));
		for (XRequest request: getStudent().getRequests()) {
			if (request instanceof XFreeTimeRequest) {
				XFreeTimeRequest fr = (XFreeTimeRequest)request;
				for (DayCode dow: DayCode.toDayCodes(fr.getTime().getDays())) {
					g.drawString(OnlineSectioningHelper.toString(fr),
							42 + 180 * dow.getIndex(),
							20 + 125 * fr.getTime().getSlot() / 30 - 50 * firstHour + fh);
				}
			}
		}
		
		int color = 0;
		for (XRequest request: getStudent().getRequests()) {
			if (request instanceof XCourseRequest && ((XCourseRequest)request).getEnrollment() != null) {
				XOffering offering = server.getOffering(((XCourseRequest)request).getEnrollment().getOfferingId());
				XCourse course = offering.getCourse(((XCourseRequest)request).getEnrollment().getCourseId());
				for (XSection section: offering.getSections(((XCourseRequest)request).getEnrollment())) {
					if (section.getTime() == null) continue;
					for (DayCode dow: DayCode.toDayCodes(section.getTime().getDays())) {
						int col = 0;
						int index = 0;
						for (int i = 0; i < section.getTime().getLength(); i++) {
							col = Math.max(col, table[dow.getIndex()][section.getTime().getSlot() + i].size());
							index = Math.max(index, table[dow.getIndex()][section.getTime().getSlot() + i].indexOf(section));
						}
						int w = 176 / col + (index + 1 < col ? -2 : 0);
						int h = 125 * section.getTime().getLength() / 30 - 1;
						int l = 39 + 180 * dow.getIndex() + index * 174 / col;
						int t = 21 + 125 * section.getTime().getSlot() / 30 - 50 * firstHour;
						
						g.setColor(new Color(Integer.valueOf(sColor2[color], 16)));
						g.fillRoundRect(l, t, w, h, 6, 6);
						
						g.setColor(new Color(Integer.valueOf(sColor1[color], 16)));
						g.drawRoundRect(l, t, w, h, 6, 6);
						g.fillRoundRect(l, t, w, 2 + fh, 6, 6);
						g.fillRect(l, t + fh - 2, w, 4);
						
				        g.setColor(new Color(0xff, 0xff, 0xff));
				        String text = MSG.course(course.getSubjectArea(), course.getCourseNumber()) + " " + section.getSubpartName();
				        while (g.getFontMetrics().stringWidth(text) > w - 10)
				        	text = text.substring(0, text.length() - 1);
				        g.drawString(text, l + 5, t + fh - 2);
				        
				        List<String> texts = new ArrayList<String>();
						if (section.getRooms() != null)
							for (XRoom room: section.getRooms())
								texts.add(room.getName());
						for (XInstructor instructor: section.getInstructors()) {
							texts.add(instructor.getName());
						}
						if (section.getTime().getDatePatternName() != null && !section.getTime().getDatePatternName().isEmpty())
							texts.add(section.getTime().getDatePatternName());
						
						if (course.getNote() != null && !course.getNote().isEmpty())
							texts.add(course.getNote().replace("\n", "; "));
						
						if (section.getNote() != null && !section.getNote().isEmpty())
							texts.add(section.getNote().replace("\n", "; "));
						
						int tt = t + fh; 
						String next = "";
						int idx = 0;
						while (idx < texts.size() || !next.isEmpty()) {
							if (idx < texts.size()) {
								next += texts.get(idx++);
								if (idx < texts.size()) next += ", ";
							}
							while (g.getFontMetrics().stringWidth(next.trim()) < w - 10 && idx < texts.size()) {
								if (g.getFontMetrics().stringWidth(next + texts.get(idx) + ",") < w - 10) {
									next += texts.get(idx++);
									if (idx < texts.size()) next += ", ";
								} else  break;
							}
							text = next; next = "";
					        while (g.getFontMetrics().stringWidth(text.trim()) > w - 10) {
					        	int sp = text.lastIndexOf(' ');
					        	if (sp >= 0 && g.getFontMetrics().stringWidth(text.substring(sp)) < w - 10) {
					        		next = text.substring(sp);
					        		text = text.substring(0, sp);
					        	} else {
					        		next = text.substring(text.length() - 1, text.length()) + next;
					        		text = text.substring(0, text.length() - 1);
					        	}
					        }
					        if (tt + fh - 2 > t + h) break;
					        g.drawString(text.trim(), l + 5, tt + fh - 2);
					        tt += fh;
						}
					}
				}
				color = (1 + color) % sColor1.length;
			}
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(image, "png", out);
		out.flush();out.close();
		return out.toByteArray();
	}
	
	public static interface TableLine {
		public XRequest getRequest();
		
		public String getSubject();
		public String getCourseNumber();
		public String getType();
		public String getName();
		
		public XTime getTime();
		public String getRooms();
		public String getInstructors();
		
		public boolean isAssigned();
		public boolean isFreeTime();
		
		public String getArrangeHours();
		public String getDays();
		public String getStart();
		public String getEnd();
		public String getDate();

		public String getCredit();
		public String getNote();
		public String getCourseNote();
		public String getRequires();
		
		public void setTable(Table table);
		public boolean isLast();
		public boolean isFirst();
	}
	
	public static class TableCourseLine implements TableLine {
		protected XCourseRequest iRequest;
		protected XCourse iCourse;
		protected Table iTable;
		
		public TableCourseLine(XCourseRequest request, XCourse course) {
			iRequest = request;
			iCourse = course;
		}

		public XRequest getRequest() { return iRequest; }
		public XCourse getCourse() { return iCourse; }

		@Override
		public String getSubject() { return getCourse().getSubjectArea(); }

		@Override
		public String getCourseNumber() { return getCourse().getCourseNumber(); }
		
		@Override
		public String getType() { return null; }

		@Override
		public String getName() { return null; }

		@Override
		public XTime getTime() { return null; }
		
		@Override
		public boolean isAssigned() { return false; }
		
		@Override
		public String getCredit() { return null; }

		@Override
		public String getNote() {
			if (iRequest.isAlternative())
				return (iRequest.isWaitlist() ? MSG.emailWaitListedAlternativeRequest() : MSG.emailNotEnrolledAlternativeRequest());
			else
				return (iRequest.isWaitlist() ? MSG.emailWaitListedRequest() : MSG.emailNotEnrolledRequest());
		}
		
		@Override
		public String getCourseNote() {
			return null;
		}

		@Override
		public String getRequires() {
			return null;
		}
		
		@Override
		public String getRooms() { return null; }
		
		@Override
		public String getInstructors() { return null; }

		@Override
		public boolean isFreeTime() { return false; }

		@Override
		public String getArrangeHours() { return null; }

		@Override
		public String getDays() { return null; }

		@Override
		public String getStart() { return null; }

		@Override
		public String getEnd() { return null; }

		@Override
		public String getDate() { return null; }

		@Override
		public void setTable(Table table) { iTable = table; }
		
		@Override
		public boolean isLast() { return iTable.isLast(this); }
		
		@Override
		public boolean isFirst() { return iTable.isFirst(this); }
	}
	
	public static class TableSectionLine extends TableCourseLine {
		protected XSection iSection;
		protected XSubpart iSubpart;
		protected String iRequires;
		
		public TableSectionLine(XCourseRequest request, XCourse course, XSubpart subpart, XSection section, String requires) {
			super(request, course);
			iCourse = course;
			iSubpart = subpart;
			iSection = section;
			iRequires = requires;
		}
		
		@Override
		public String getType() { return iSection.getSubpartName(); }

		@Override
		public String getName() { return iSection.getName(iCourse.getCourseId()); }

		
		public XSection getSection() { return iSection; }
		public XSubpart getSubpart() { return iSubpart; }
		
		@Override
		public String getRequires() { return iRequires; }

		@Override
		public String getNote() {
			return iSection.getNote();
		}
		
		@Override
		public String getCourseNote() {
			return iCourse.getNote();
		}
		
		@Override
		public String getCredit() { return iSubpart == null ? null : iSubpart.getCreditAbbv(iCourse.getCourseId()); }
		
		@Override
		public XTime getTime() { return iSection.getTime(); }
		
		@Override
		public boolean isAssigned() { return true; }
		
		@Override
		public String getRooms() {
			String rooms = "";
			if (iSection.getRooms() != null && !iSection.getRooms().isEmpty()) {
				for (XRoom room: iSection.getRooms()) {
					if (!rooms.isEmpty()) rooms += ", ";
					rooms += room.getName();
				}
			}
			return rooms;
		}
		
		@Override
		public String getInstructors() {
			String instructors = "";
			if (!iSection.getInstructors().isEmpty()) {
				for (XInstructor instructor: iSection.getInstructors()) {
					if (!instructors.isEmpty()) instructors += ", ";
					if (instructor.getEmail() == null) {
						instructors += "<span style='white-space: nowrap;'>" + instructor.getName() + "</span>";
					} else {
						instructors += "<a href='mailto:" + instructor.getEmail() + "' style=\"color: inherit; background-color : transparent; text-decoration: none; white-space: nowrap;\">" + instructor.getName()+ "</a>";
					}
				}
			}
			return instructors;
		}
		
		@Override
		public String getArrangeHours() { return MSG.emailArrangeHours(); }

		@Override
		public String getDays() { return DayCode.toString(getTime().getDays()); }

		@Override
		public String getStart() { return startTime(getTime()); }

		@Override
		public String getEnd() { return endTime(getTime()); }

		@Override
		public String getDate() { return getTime().getDatePatternName(); }
	}
	
	public static class TableLineFreeTime implements TableLine {
		private XFreeTimeRequest iRequest;
		
		public TableLineFreeTime(XFreeTimeRequest request) {
			iRequest = request;
		}

		public XRequest getRequest() { return iRequest; }
		
		@Override
		public String getSubject() { return MSG.freeTimeCourse(); }

		@Override
		public String getCourseNumber() { return MSG.freeTimeSubject(); }
		
		@Override
		public String getType() { return null; }

		@Override
		public String getName() { return null; }
		
		@Override
		public XTime getTime() { return iRequest.getTime(); }
		
		@Override
		public boolean isAssigned() { return true; }
		
		@Override
		public String getCredit() { return null; }

		@Override
		public String getNote() { return null; }
		
		@Override
		public String getCourseNote() { return null; }

		@Override
		public String getRequires() { return null; }
		
		@Override
		public String getRooms() { return ""; }
		
		@Override
		public String getInstructors() { return ""; }
		
		@Override
		public boolean isFreeTime() { return true; }
		
		@Override
		public String getArrangeHours() { return MSG.emailArrangeHours(); }

		@Override
		public String getDays() { return DayCode.toString(getTime().getDays()); }

		@Override
		public String getStart() { return startTime(getTime()); }

		@Override
		public String getEnd() { return endTime(getTime()); }

		@Override
		public String getDate() { return getTime().getDatePatternName(); }
		
		@Override
		public void setTable(Table table) {}
		
		@Override
		public boolean isLast() { return true; }
		
		@Override
		public boolean isFirst() { return true; }
	}
	
	public static class TableSectionDeletedLine extends TableSectionLine {
		
		public TableSectionDeletedLine(XCourseRequest request, XCourse course, XSubpart subpart, XSection section, String requires) {
			super(request, course, subpart, section, requires);
		}
		
		@Override
		public String getCourseNote() { return null; }
	}
	
	public static class TableSectionModifiedLine extends TableSectionLine {
		private XSubpart iOldSubpart;
		private XSection iOldSection;
		private String iOldRequires;
		
		public TableSectionModifiedLine(XCourseRequest request, XCourse course, XSubpart oldSubpart, XSubpart subpart, XSection oldSection, XSection section, String oldRequires, String requires) {
			super(request, course, subpart, section, requires);
			iOldSection = oldSection;
			iOldSubpart = oldSubpart;
			iOldRequires = oldRequires;
		}
		
		public XSubpart getOldSubpart() { return iOldSubpart; }
		public XSection getOldSection() { return iOldSection; }
		public XTime getOldTime() { return iOldSection.getTime(); }
		
		@Override
		public String getName() {
			return diff(iOldSection.getName(iCourse.getCourseId()), iSection.getName(iCourse.getCourseId()));
		}
		
		public String diff(String a, String b) {
			if (a == null || a.isEmpty())
				return (b == null || b.isEmpty() ? "<span style='text-decoration: none;'>&nbsp;</span>" : b);
			if (b == null || b.isEmpty())
				return "<br><span style='font-style: italic; color: gray; text-decoration: line-through;'>" + a + "</span>";
			if (a.equals(b))
				return a;
			return b + "<br><span style='font-style: italic; color: gray; text-decoration: line-through;'>" + a + "</span>";
		}
		
		@Override
		public String getRequires() {
			return diff(iOldRequires, iRequires);
		}
		
		@Override
		public String getInstructors() {
			String oldInstructors = "";
			if (!iOldSection.getInstructors().isEmpty()) {
				for (XInstructor instructor: iOldSection.getInstructors()) {
					if (!oldInstructors.isEmpty()) oldInstructors += ", ";
					if (instructor.getEmail() == null) {
						oldInstructors += instructor.getName();
					} else {
						oldInstructors += "<a href='mailto:" + instructor.getEmail() + "' style=\"color: inherit; background-color : transparent; text-decoration: none;\">" + instructor.getName()+ "</a>";
					}
				}
			}
			String instructors = "";
			if (!iSection.getInstructors().isEmpty()) {
				for (XInstructor instructor: iSection.getInstructors()) {
					if (!instructors.isEmpty()) instructors += ", ";
					if (instructor.getEmail() == null) {
						instructors += instructor.getName();
					} else {
						instructors += "<a href='mailto:" + instructor.getEmail() + "' style=\"color: inherit; background-color : transparent; text-decoration: none;\">" + instructor.getName()+ "</a>";
					}
				}
			}
			return diff(oldInstructors, instructors);
		}
		
		@Override
		public String getRooms() {
			String oldRooms = "";
			if (iOldSection.getRooms() != null && !iOldSection.getRooms().isEmpty()) {
				for (XRoom room: iOldSection.getRooms()) {
					if (!oldRooms.isEmpty()) oldRooms += ", ";
					oldRooms += room.getName();
				}
			}
			
			String rooms = "";
			if (iSection.getRooms() != null && !iSection.getRooms().isEmpty()) {
				for (XRoom room: iSection.getRooms()) {
					if (!rooms.isEmpty()) rooms += ", ";
					rooms += room.getName();
				}
			}
			return diff(oldRooms, rooms);
		}
		
		@Override
		public String getNote() {
			return diff(iOldSection.getNote(), iSection.getNote());
		}
		
		@Override
		public String getCourseNote() {
			return null;
		}
				
		@Override
		public String getArrangeHours() { 
			return diff(getOldTime() == null ? MSG.emailArrangeHours() : DayCode.toString(getOldTime().getDays()) + " " + startTime(getOldTime()), MSG.emailArrangeHours());
		}

		@Override
		public String getDays() { 
			return diff(getOldTime() == null ? null : DayCode.toString(getOldTime().getDays()), DayCode.toString(getTime().getDays()));
		}

		@Override
		public String getStart() {
			return diff(getOldTime() == null ? null : startTime(getOldTime()), startTime(getTime()));
		}

		@Override
		public String getEnd() {
			return diff(getOldTime() == null ? null : endTime(getOldTime()), endTime(getTime()));
		}

		@Override
		public String getDate() { 
			return diff(getOldTime() == null ? null : getOldTime().getDatePatternName(), getTime().getDatePatternName());
		}
	}
	
	public class Table extends ArrayList<TableLine> {
		private static final long serialVersionUID = 1L;
		
		public boolean sameCourse(TableLine a, TableLine b) {
			if (a instanceof TableCourseLine && b instanceof TableCourseLine) {
				return ((TableCourseLine)a).getCourse().equals(((TableCourseLine)b).getCourse());
			} else {
				return false;
			}
		}
		
		public boolean isFirst(TableLine line) {
			int index = indexOf(line);
			return (index <= 0 || !sameCourse(line, get(index - 1)));
		}
		
		public boolean isLast(TableLine line) {
			int index = indexOf(line);
			return (index + 1 >= size() || !sameCourse(line, get(index + 1)));
		}
		
		@Override
		public boolean add(TableLine line) {
			line.setTable(this);
			return super.add(line);
		}
	}
}
