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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.activation.DataSource;
import javax.imageio.ImageIO;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

import org.cpsolver.ifs.util.ToolBox;
import org.unitime.commons.Email;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.ErrorMessage;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.CheckCoursesResponse;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.FreeTime;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.Preference;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.Request;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourseStatus;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.AdvisingStudentDetails;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.StudentStatusInfo;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.WaitListMode;
import org.unitime.timetable.model.Advisor;
import org.unitime.timetable.model.AdvisorCourseRequest;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.StudentEnrollmentMessage;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.StudentSectioningStatus.NotificationType;
import org.unitime.timetable.model.StudentSectioningStatus.Option;
import org.unitime.timetable.model.dao.CourseDemandDAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.advisors.AdvisorConfirmationPDF;
import org.unitime.timetable.onlinesectioning.advisors.AdvisorGetCourseRequests;
import org.unitime.timetable.onlinesectioning.basic.GetRequest;
import org.unitime.timetable.onlinesectioning.custom.CourseUrlProvider;
import org.unitime.timetable.onlinesectioning.custom.CustomStudentEnrollmentHolder;
import org.unitime.timetable.onlinesectioning.custom.Customization;
import org.unitime.timetable.onlinesectioning.custom.StudentEmailProvider;
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
import org.unitime.timetable.onlinesectioning.model.XCourseRequest.XPreference;
import org.unitime.timetable.onlinesectioning.solver.SectioningRequest.ReschedulingReason;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.NameFormat;
import org.unitime.timetable.util.Formats.Format;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;


/**
 * @author Tomas Muller
 */
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
	private byte[] iAdvisorRequestsPDF = null;
	
	private Long iStudentId;
	private XOffering iOldOffering;
	private XCourseId iOldCourseId;
	private XEnrollment iOldEnrollment;
	private XStudent iOldStudent;
	private XOffering iFailedOffering;
	private XCourseId iFailedCourseId;
	private XEnrollment iFailedEnrollment;
	private XEnrollment iDropEnrollment;
	private SectioningException iFailure;
	private XStudent iStudent;
	private CourseUrlProvider iCourseUrlProvider = null;
	private boolean iPermisionCheck = true;
	private boolean iIncludeCourseRequests = true;
	private boolean iIncludeClassSchedule = true;
	private boolean iIncludeAdvisorRequests = false;
	private boolean iIncludeAdvisorRequestsPDF = false;
	private Boolean iOptional = null;
	private String iSourceAction = "not-set";
	private ReschedulingReason iReason = null;
	private NotificationType iNotificationType;
	private boolean iSkipWhenNoChange = false;
	
	public StudentEmail forStudent(Long studentId) {
		iStudentId = studentId;
		return this;
	}
	
	public StudentEmail forStudent(XStudent student) {
		iStudent = student;
		iStudentId = student.getStudentId();
		return this;
	}
	
	public StudentEmail fromAction(String actionName) {
		iSourceAction = actionName;
		return this;
	}
	
	public StudentEmail withType(NotificationType notificationType) {
		iNotificationType = notificationType;
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
	
	public StudentEmail failedEnrollment(XOffering failedOffering, XCourseId failedCourseId, XEnrollment failedEnrollment, SectioningException failure) {
		iFailedOffering = failedOffering;
		iFailedCourseId = failedCourseId;
		iFailedEnrollment = failedEnrollment;
		iFailure = failure;
		return this;
	}
	
	public StudentEmail dropEnrollment(XEnrollment dropEnrollment) {
		iDropEnrollment = dropEnrollment;
		return this;
	}

	
	public StudentEmail overridePermissions(boolean courseRequests, boolean classSchedule, boolean advisorRequests) {
		iPermisionCheck = false;
		iIncludeCourseRequests = courseRequests;
		iIncludeClassSchedule = classSchedule;
		iIncludeAdvisorRequests = advisorRequests;
		return this;
	}
	
	public StudentEmail includeAdvisorRequestsPDF() {
		iIncludeAdvisorRequestsPDF = true;
		return this;
	}
	
	public StudentEmail setOptional(Boolean optional) {
		iOptional = optional;
		return this;
	}
	
	public StudentEmail rescheduling(ReschedulingReason reason) {
		iReason = reason;
		return this;
	}
	
	public StudentEmail skipWhenNoChange(boolean skipWhenNoChange) {
		iSkipWhenNoChange = skipWhenNoChange;
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
	
	public XEnrollment getFailedEnrollment() { return iFailedEnrollment; }
	public XOffering getFailedOffering() { return iFailedOffering; }
	public XCourse getFailedCourse() { return (iFailedOffering == null || iFailedCourseId == null ? null : iFailedOffering.getCourse(iFailedCourseId)); }
	
	public XEnrollment getDropEnrollment() { return iDropEnrollment; }
	
	public XStudent getStudent() { return iStudent; }
	public void setStudent(XStudent student) { iStudent = student; }

	@Override
	public Boolean execute(final OnlineSectioningServer server, final OnlineSectioningHelper helper) {
		try {
			String providerClass = ApplicationProperty.CustomizationCourseLink.value();
			if (providerClass != null)
				iCourseUrlProvider = (CourseUrlProvider)Class.forName(providerClass).getDeclaredConstructor().newInstance();
		} catch (Exception e) {}
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
				if (getDropEnrollment() != null && !getDropEnrollment().getCourseId().equals(getOldCourse() == null ? null : getOldCourse().getCourseId())) {
					XOffering dropOffering = server.getOffering(getDropEnrollment().getOfferingId());
					if (dropOffering != null)
						for (XSection section: dropOffering.getSections(getDropEnrollment()))
							enrollment.addSection(OnlineSectioningHelper.toProto(section, getDropEnrollment()));
				}
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
			} else if (getDropEnrollment() != null) {
				OnlineSectioningLog.Enrollment.Builder enrollment = OnlineSectioningLog.Enrollment.newBuilder();
				enrollment.setType(OnlineSectioningLog.Enrollment.EnrollmentType.PREVIOUS);
				XOffering dropOffering = server.getOffering(getDropEnrollment().getOfferingId());
				if (dropOffering != null)
					for (XSection section: dropOffering.getSections(getDropEnrollment()))
						enrollment.addSection(OnlineSectioningHelper.toProto(section, getDropEnrollment()));
				action.addEnrollment(enrollment);
			}
			
			if (getFailedEnrollment() != null) {
				OnlineSectioningLog.Enrollment.Builder enrollment = OnlineSectioningLog.Enrollment.newBuilder();
				enrollment.setType(OnlineSectioningLog.Enrollment.EnrollmentType.COMPUTED);
				for (XSection section: getFailedOffering().getSections(getFailedEnrollment()))
					enrollment.addSection(OnlineSectioningHelper.toProto(section, getFailedEnrollment()));
				action.addEnrollment(enrollment);
			}
						
			final XStudent student = (getStudent() != null ? getStudent() : server.getStudent(getStudentId()));
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
				action.getStudentBuilder().setName(helper.getStudentNameFormat().format(dbStudent));
				boolean emailEnabled = true;
				if (iPermisionCheck) {
					StudentSectioningStatus status = dbStudent.getEffectiveStatus();
					if (status != null && !status.hasOption(StudentSectioningStatus.Option.email)) {
						emailEnabled = false;
					}
					if (status != null && iNotificationType != null && !status.hasNotification(iNotificationType)) {
						emailEnabled = false;	
					}
					if (iIncludeClassSchedule && status != null && !status.hasOption(StudentSectioningStatus.Option.enabled))
						iIncludeClassSchedule = false;
					if (iIncludeCourseRequests && status != null && !status.hasOption(StudentSectioningStatus.Option.registration))
						iIncludeCourseRequests = false;
					if (iIncludeClassSchedule && !ApplicationProperty.OnlineSchedulingEmailConfirmationOverride.isTrue(iSourceAction + ".classes", true))
						iIncludeClassSchedule = false;
					if (iIncludeCourseRequests && !ApplicationProperty.OnlineSchedulingEmailConfirmationOverride.isTrue(iSourceAction + ".requests", true))
						iIncludeCourseRequests = false;
				} else {
					emailEnabled = true;
				}
				
				String failureMessage = (iFailedCourseId == null || iFailure == null || iFailure.getMessage().isEmpty() ? null : iFailure.getMessage());
				if (failureMessage != null && failureMessage.length() > 255)
					failureMessage = failureMessage.substring(0, 252) + "...";
				if (failureMessage != null) {
					helper.logOption("failed-course", iFailedCourseId.getCourseName());
					helper.logOption("failed-error", failureMessage);
				}

				if (emailEnabled && iFailure != null) {
					String skipOnErrorCodes = ApplicationProperty.OnlineSchedulingEmailSkipOnErrorCodes.value();
					if (skipOnErrorCodes != null && !skipOnErrorCodes.isEmpty() && iFailure.hasErrors()) {
						for (ErrorMessage em: iFailure.getErrors())
							if (em.getCode() != null && em.getCode().matches(skipOnErrorCodes)) {
								helper.logOption("skip-on-error", em.toString());
								emailEnabled = false;
							}
					}
					String skipOnErrorMessage = ApplicationProperty.OnlineSchedulingEmailSkipOnErrorMessage.value();
					if (skipOnErrorMessage != null && !skipOnErrorMessage.isEmpty() && iFailure.getMessage() != null && iFailure.getMessage().matches(skipOnErrorMessage)) {
						helper.logOption("skip-on-error", iFailure.getMessage());
						emailEnabled = false;
					}
				}
				
				if (emailEnabled && failureMessage != null) {
					XCourseRequest cr = student.getRequestForCourse(iFailedCourseId.getCourseId());
					if (cr != null && failureMessage.equals(cr.getEnrollmentMessage())) {
						emailEnabled = false;
						student.markFailedWaitList(iFailedCourseId);
						server.update(student, false);
					}
				}
				
				if (iSourceAction != null)
					helper.logOption("source-action", iSourceAction);
				if (iNotificationType != null)
					helper.logOption("type", iNotificationType.name());
				if (iSkipWhenNoChange)
					helper.logOption("skipWhenNoChange", "true");
				
				if (emailEnabled) {
					StudentEmailProvider emailProvider = null;
					if (Customization.StudentEmailProvider.hasProvider())
						emailProvider = Customization.StudentEmailProvider.getProvider();
					boolean plainText = ApplicationProperty.OnlineSchedulingEmailPlainText.isTrue();
					if (emailProvider != null) {
						Boolean epPlainText = emailProvider.isPlainText(server, helper, iOptional, iSourceAction);
						if (epPlainText != null)
							plainText = epPlainText.booleanValue();
					}
					final String html = generateMessage(dbStudent, server, helper, plainText);
					if (html != null) {
						Email email = null;
						if (emailProvider != null) {
							email = emailProvider.createEmail(server, helper, iOptional, iSourceAction);
						} else {
							email = Email.createEmail();
						}

						email.addRecipient(dbStudent.getEmail(), helper.getStudentNameFormat().format(dbStudent));
						helper.logOption("recipient", dbStudent.getEmail());
						
						String firstCarbonCopy = null;
						if (getCC() != null && !getCC().isEmpty()) {
							String suffix = ApplicationProperty.EmailDefaultAddressSuffix.value();
							for (StringTokenizer s = new StringTokenizer(getCC(), ",;\n"); s.hasMoreTokens(); ) {
								String address = s.nextToken().trim();
								if (address.isEmpty()) continue;
								if (suffix != null && address.indexOf('@') < 0)
									address += suffix;
								try {
									new InternetAddress(address, true);
								} catch (AddressException e) {
									helper.warn(GWT.badEmailAddress(address, e.getMessage()));
									continue;
								}
								email.addRecipientCC(address, null);
								helper.logOption("cc", address);
								if (firstCarbonCopy == null) firstCarbonCopy = address;
							}
						}
						
						if (getEmailSubject() != null && !getEmailSubject().isEmpty()) {
							email.setSubject(getEmailSubject().replace("%session%", server.getAcademicSession().toString()));
							helper.logOption("subject", getEmailSubject().replace("%session%", server.getAcademicSession().toString()));
						} else {
							email.setSubject(getSubject().replace("%session%", server.getAcademicSession().toString()));
							helper.logOption("subject", getSubject().replace("%session%", server.getAcademicSession().toString()));
						}
						
						if (getMessage() != null && !getMessage().isEmpty())
							helper.logOption("message", getMessage());
						
						if (helper.getUser() != null && getOldOffering() == null && getOldStudent() == null) {
							TimetableManager manager = helper.getHibSession().createQuery("from TimetableManager where externalUniqueId = :id", TimetableManager.class).setParameter("id", helper.getUser().getExternalId()).uniqueResult();
							Advisor advisor = null;
							if (manager == null || manager.getEmailAddress() == null)
								advisor = helper.getHibSession().createQuery("from Advisor where externalUniqueId = :externalId and session.uniqueId = :sessionId", Advisor.class)
										.setParameter("externalId", helper.getUser().getExternalId()).setParameter("sessionId", server.getAcademicSession().getUniqueId()).setMaxResults(1).uniqueResult();
							if (manager != null && manager.getEmailAddress() != null) {
								email.setReplyTo(manager.getEmailAddress(), helper.getInstructorNameFormat().format(manager));
								helper.logOption("reply-to", helper.getInstructorNameFormat().format(manager) + " <" + manager.getEmailAddress() + ">");
							} else if (advisor != null && advisor.getEmail() != null) {
								email.setReplyTo(advisor.getEmail(), helper.getInstructorNameFormat().format(advisor));
								helper.logOption("reply-to", helper.getInstructorNameFormat().format(advisor) + " <" + advisor.getEmail() + ">");
							} else if (firstCarbonCopy != null) {
								email.setReplyTo(firstCarbonCopy, null);
								helper.logOption("reply-to", firstCarbonCopy);
							}
						}
						
						String additionalCC = ApplicationProperty.OnlineSchedulingEmailCarbonCopy.value();
						if (additionalCC != null) {
							String suffix = ApplicationProperty.EmailDefaultAddressSuffix.value();
							for (String address: additionalCC.split("[\n,]")) {
								String cc = address.trim();
								if (cc.isEmpty()) continue;
								if (suffix != null && cc.indexOf('@') < 0)
									cc += suffix;
								email.addRecipientCC(cc, null);
								helper.logOption("cc", cc);
							}
						}
						
						if (ApplicationProperty.OnlineSchedulingEmailCCAdvisors.isTrue(iSourceAction)) {
							for (Advisor advisor: dbStudent.getAdvisors()) {
								if (advisor.getEmail() != null && !advisor.getEmail().isEmpty()) {
									email.addRecipientCC(advisor.getEmail(), helper.getInstructorNameFormat().format(advisor));
									helper.logOption("cc", advisor.getEmail());
								}
							}
						}
						
						final StringWriter buffer = new StringWriter();
						if (ApplicationProperty.OnlineSchedulingEmailIncludeMessage.isTrue()) {
							PrintWriter out = new PrintWriter(buffer);
							generateTimetable(out, server, helper);
							out.flush(); out.close();
							email.addAttachment(new DataSource() {
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
						}
						
						if (iTimetableImage != null) {
							email.addAttachment(new DataSource() {
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
						
						if (iAdvisorRequestsPDF != null) {
							email.addAttachment(new DataSource() {
								@Override
								public OutputStream getOutputStream() throws IOException {
									throw new IOException("No output stream.");
								}
								
								@Override
								public String getName() {
									return "recommendations-" + server.getAcademicSession().getTerm() + server.getAcademicSession().getYear() + "-" + 
											student.getName().replaceAll("[&$\\+,/:;=\\?@<>\\[\\]\\{\\}\\|\\^\\~%#`\\t\\s\\n\\r \\\\]", "") + ".pdf";
								}
								
								@Override
								public InputStream getInputStream() throws IOException {
									return new ByteArrayInputStream(iAdvisorRequestsPDF);
								}
								
								@Override
								public String getContentType() {
									return "application/pdf";
								}
							});
						}

						if (ApplicationProperty.OnlineSchedulingEmailICalendar.isTrue() && iIncludeClassSchedule) {
							try {
								final String calendar = CalendarExport.getCalendar(server, helper, student);
								if (calendar != null)
									email.addAttachment(new DataSource() {
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
						}
						
						String lastMessageId = sLastMessage.get(student.getStudentId());
						if (lastMessageId != null)
							email.setInReplyTo(lastMessageId);
						
						if (plainText)
							email.setText(html);
						else
							email.setHTML(html);
						
						helper.logOption("email", html.replace("<img src='cid:timetable.png' border='0' alt='Timetable Image'/>", buffer.toString()));

						email.send();
						
						String messageId = email.getMessageId();
						if (messageId != null)
							sLastMessage.put(student.getStudentId(), messageId);
						
						Date ts = new Date();
						dbStudent.setScheduleEmailedDate(ts);
						student.setEmailTimeStamp(ts);
						if (iFailedCourseId != null) {
							student.markFailedWaitList(iFailedCourseId);
							if (failureMessage != null) {
								XCourseRequest cr = student.getRequestForCourse(iFailedCourseId.getCourseId());
								if (cr != null) {
									cr.setEnrollmentMessage(failureMessage);
									CourseDemand cd = CourseDemandDAO.getInstance().get(cr.getRequestId());
									if (cd != null) {
										if (cd.getEnrollmentMessages() != null)
											for (Iterator<StudentEnrollmentMessage> i = cd.getEnrollmentMessages().iterator(); i.hasNext(); ) {
												StudentEnrollmentMessage message = i.next();
												helper.getHibSession().remove(message);
												i.remove();
											}
										StudentEnrollmentMessage m = new StudentEnrollmentMessage();
										m.setCourseDemand(cd);
										m.setLevel(0);
										m.setType(0);
										m.setTimestamp(ts);
										m.setMessage(failureMessage);
										m.setOrder(0);
										cd.getEnrollmentMessages().add(m);
										helper.getHibSession().persist(m);
									}
								}
							}
						}
						
						helper.getHibSession().merge(dbStudent);
						helper.getHibSession().flush();
						
						server.update(student, false);
						
						ret = true;
					} else {
						if (iSkipWhenNoChange)
							helper.debug(MSG.emailNoChange());
						else
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
	
	protected URL getCourseUrl(AcademicSessionInfo session, XCourse course) {
		if (iCourseUrlProvider == null) return null;
		return iCourseUrlProvider.getCourseUrl(session, course.getSubjectArea(), course.getCourseNumber());
	}
	
	protected URL getCourseUrl(OnlineSectioningServer server, RequestedCourse rc) {
		if (iCourseUrlProvider == null || rc == null || !rc.hasCourseId()) return null;
		XCourse course = server.getCourse(rc.getCourseId());
		if (course == null) return null;
		return iCourseUrlProvider.getCourseUrl(server.getAcademicSession(), course.getSubjectArea(), course.getCourseNumber());
	}
	
	private String generateMessage(org.unitime.timetable.model.Student student, OnlineSectioningServer server, OnlineSectioningHelper helper, boolean plainText)  throws IOException, TemplateException {
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_0);
		cfg.setClassForTemplateLoading(StudentEmail.class, "/");
		cfg.setLocale(Localization.getJavaLocale());
		cfg.setOutputEncoding("utf-8");
		Template template = cfg.getTemplate(plainText ? ApplicationProperty.OnlineSchedulingEmailPlainTextTemplate.value() : ApplicationProperty.OnlineSchedulingEmailTemplate.value());
		Map<String, Object> input = new HashMap<String, Object>();
		
		input.put("msg", MSG);
		input.put("student", getStudent());
		input.put("name", helper.getStudentNameFormat().format(student));
		input.put("server", server);
		input.put("helper", helper);
		input.put("message", getMessage());
		input.put("dfConsentApproval", sConsentApprovalDateFormat);
		input.put("source", iSourceAction);
		
		StudentSectioningStatus status = student.getEffectiveStatus();
		WaitListMode wlMode = WaitListMode.None;
		if (CustomStudentEnrollmentHolder.isAllowWaitListing() && (status == null || status.hasOption(Option.waitlist))) {
			wlMode = WaitListMode.WaitList;
		} else if (status != null && status.hasOption(Option.nosubs)) {
			wlMode = WaitListMode.NoSubs;
		}
		input.put("wlMode", wlMode.name());
		String advWlMode = ApplicationProperty.AdvisorRecommendationsWaitListMode.value(student.getSession());
		WaitListMode awlMode = WaitListMode.None;
		if ("Student".equalsIgnoreCase(advWlMode))
			awlMode = wlMode;
		else
			awlMode = WaitListMode.valueOf(advWlMode);
		input.put("awlMode", awlMode.name());
		
		if (iIncludeCourseRequests) {
			helper.getAction().clearRequest();
			CourseRequestInterface requests = server.createAction(GetRequest.class)
					.forStudent(student.getUniqueId())
					.withCustomValidation(status != null && status.hasOption(StudentSectioningStatus.Option.reqval))
					.withWaitListValidation(status != null && status.hasOption(StudentSectioningStatus.Option.specreg))
					.withCustomRequest(false)
					.withAdvisorRequests(false)
					.withWaitListMode(wlMode)
					.execute(server, helper);
			input.put("requests", generateCourseRequests(student, requests, server, helper, wlMode));
		}
		
		if (iIncludeAdvisorRequests) {
			CourseRequestInterface requests = server.createAction(AdvisorGetCourseRequests.class)
					.forStudent(student.getUniqueId())
					.checkDemands(false)
					.execute(server, helper);
			if (requests != null && (!requests.isEmpty() || requests.hasCreditNote())) {
				input.put("advisor", generateAdvisorRequests(student, requests, server, helper, awlMode));
				String disclaimer = ApplicationProperty.AdvisorCourseRequestsPDFDisclaimer.value();
				if (disclaimer != null && !disclaimer.isEmpty()) {
					input.put("disclaimer", disclaimer);
				}
				if (iIncludeAdvisorRequestsPDF) {
					AdvisingStudentDetails details = new AdvisingStudentDetails();
					details.setSessionId(server.getAcademicSession().getUniqueId());
					details.setStudentId(student.getUniqueId());
					details.setStudentName(student.getName(NameFormat.LAST_FIRST_MIDDLE.reference()));
					details.setSessionName(student.getSession().getLabel());
					details.setWaitListMode(awlMode);
					Advisor advisor = Advisor.findByExternalId(helper.getUser().getExternalId(), server.getAcademicSession().getUniqueId());
					if (advisor != null)
						details.setAdvisorEmail(advisor.getEmail());
					if (!details.hasAdvisorEmail()) {
						AdvisorCourseRequest lastAcr = null;
						for (AdvisorCourseRequest acr: student.getAdvisorCourseRequests()) {
							if (lastAcr == null || lastAcr.getTimestamp().before(acr.getTimestamp())) lastAcr = acr;
						}
						if (lastAcr != null) {
							advisor = Advisor.findByExternalId(lastAcr.getChangedBy(), server.getAcademicSession().getUniqueId());
							if (advisor != null)
								details.setAdvisorEmail(advisor.getEmail());
						}
					}
					if (!details.hasAdvisorEmail()) {
						String email = null;
						for (Advisor a: student.getAdvisors()) {
							if (a.getEmail() != null && !a.getEmail().isEmpty()) {
								email = (email == null ? "" : email + "\n") + a.getEmail();
							}
						}
						details.setAdvisorEmail(email);;
					}
					if (!details.hasAdvisorEmail()) {
						TimetableManager manager = TimetableManager.findByExternalId(helper.getUser().getExternalId());
						if (manager != null)
							details.setAdvisorEmail(manager.getEmailAddress());
					}
					if (student.getSectioningStatus() != null) {
						StudentStatusInfo info = new StudentStatusInfo();
						info.setUniqueId(student.getSectioningStatus().getUniqueId());
						info.setReference(student.getSectioningStatus().getReference());
						info.setLabel(student.getSectioningStatus().getLabel());
						details.setStatus(info);
					} else if (student.getSession().getDefaultSectioningStatus() != null) {
						StudentStatusInfo info = new StudentStatusInfo();
						info.setUniqueId(null);
						info.setReference("");
						info.setLabel(MSG.studentStatusSessionDefault(student.getSession().getDefaultSectioningStatus().getLabel()));
						info.setEffectiveStart(null); info.setEffectiveStop(null);
						details.setStatus(info);
					} else {
						StudentStatusInfo info = new StudentStatusInfo();
						info.setReference("");
						info.setLabel(MSG.studentStatusSystemDefault());
						info.setAllEnabled();
						details.setStatus(info);
					}
					details.setRequest(requests);
					try {
						ByteArrayOutputStream bytes = new ByteArrayOutputStream();
						new AdvisorConfirmationPDF(details).generatePdfConfirmation(bytes);
						bytes.flush(); bytes.close();
						iAdvisorRequestsPDF = bytes.toByteArray();
					} catch (Exception e) {
						helper.error("Failed to generate PDF confirmation: " + e.getMessage(), e);
					}
				}
			}
		}
		
		if (iIncludeClassSchedule) {
			Table classes = generateListOfClasses(student, server, helper, wlMode, plainText);
			input.put("classes", classes);
			
			// Total credit
			float totalCredit = 0f;
			Pattern pattern = Pattern.compile("\\d+\\.?\\d*");
			for (TableLine line: classes) {
				String credit = line.getCredit();
				if (credit == null) continue;
				Matcher m = pattern.matcher(credit);
				if (m.find())
					totalCredit += Float.parseFloat(m.group());
			}
			input.put("credit", totalCredit);
			
			if (!getStudent().getRequests().isEmpty() && ApplicationProperty.OnlineSchedulingEmailIncludeImage.isTrue()) {
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
		}
		
		if (iReason != null) {
			switch (iReason) {
			case CLASS_CANCELLED:
				input.put("reason", MSG.reschedulingReasonCancelledClass());
				break;
			case TIME_CONFLICT:
				input.put("reason", MSG.reschedulingReasonTimeConflict());
				break;
			case CLASS_LINK:
				input.put("reason", MSG.reschedulingReasonClassLink());
				break;
			case MISSING_CLASS:
				input.put("reason", MSG.reschedulingReasonMissingClass());
				break;
			case MULTIPLE_CONFIGS:
				input.put("reason", MSG.reschedulingReasonMultipleConfigs());
				break;
			case MULTIPLE_ENRLS:
				input.put("reason", MSG.reschedulingReasonMultipleClasses());
				break;
			case NO_REQUEST:
				input.put("reason", MSG.reschedulingReasonNoRequest());
				break;
			}
		}
		
		AcademicSessionInfo session = server.getAcademicSession();
		if (getFailedOffering() != null) {
			
			Table listOfChanges = new Table();
			XCourseRequest newRequest = null;
			XOffering newOffering = null;
			XCourse course = (getFailedEnrollment() != null ? getFailedOffering().getCourse(getFailedEnrollment().getCourseId()) : getFailedCourse());
			if (course == null)
				course = (getFailedCourse() == null ? getFailedOffering().getControllingCourse() : getFailedCourse());
			for (XRequest r: getStudent().getRequests()) {
				if (r instanceof XCourseRequest && (
						(getFailedCourse() == null && ((XCourseRequest)r).getCourseIdByOfferingId(getFailedOffering().getOfferingId()) != null) ||
						(getFailedCourse() != null && ((XCourseRequest)r).hasCourse(getFailedCourse().getCourseId()))
						)) {
					newRequest = (XCourseRequest)r;
					newOffering = server.getOffering(getFailedOffering().getOfferingId());
					if (newRequest.getEnrollment() != null)
						course = newOffering.getCourse(newRequest.getEnrollment().getCourseId());
					break;
				}
			}
			input.put("changedCourse", course);
			setSubject(MSG.emailEnrollmentFailed(course.getSubjectArea(), course.getCourseNumber(), iFailure == null ? null : iFailure.getMessage()));
			if (newRequest != null && newRequest.isWaitlist())
				setSubject(MSG.emailEnrollmentFailedWaitListed(course.getSubjectArea(), course.getCourseNumber()));
			
			if (getDropEnrollment() != null && !getDropEnrollment().getCourseId().equals(getFailedCourse().getCourseId())) {
				XOffering dropOffering = server.getOffering(getDropEnrollment().getOfferingId());
				if (dropOffering != null) {
					XCourse dropCourse = dropOffering.getCourse(getDropEnrollment().getCourseId());
					XCourseRequest dropRequest = getStudent().getRequestForCourse(dropCourse.getCourseId());
					for (XSection old: dropOffering.getSections(getDropEnrollment())) {
						XSubpart subpart = dropOffering.getSubpart(old.getSubpartId());
						XSection parent = (old.getParentId() == null ? null : dropOffering.getSection(old.getParentId()));
						String requires = null;
						if (parent != null)
							requires = parent.getName(dropCourse.getCourseId());
						listOfChanges.add(new TableSectionDeletedLine(dropRequest, dropCourse, subpart, old, requires, getCourseUrl(session, dropCourse), plainText));
					}
				}
			}
			
			if (getFailedEnrollment() != null && (newRequest == null || newRequest.getEnrollment() == null)) {
				for (XSection section: getFailedOffering().getSections(getFailedEnrollment())) {
					XSection parent = (section.getParentId() == null ? null : getFailedOffering().getSection(section.getParentId()));
					XSubpart subpart = getFailedOffering().getSubpart(section.getSubpartId());
					String requires = null;
					if (parent != null) {
						requires = parent.getName(course.getCourseId());
					} else {
						requires = null;
					}
					listOfChanges.add(new TableSectionLine(newRequest, course, subpart, section, requires, getCourseUrl(session, course), plainText));
				}
				input.put("changes", listOfChanges);
			} else if (getFailedEnrollment() != null && newRequest != null && newRequest.getEnrollment() != null) {
				String consent = consent(server, newRequest.getEnrollment());
				sections: for (XSection section: newOffering.getSections(newRequest.getEnrollment())) {
					XSection parent = (section.getParentId() == null ? null : newOffering.getSection(section.getParentId()));
					XSubpart subpart = newOffering.getSubpart(section.getSubpartId());
					
					for (XSection failed: getFailedOffering().getSections(getFailedEnrollment())) {
						if (failed.getSubpartId().equals(section.getSubpartId())) {
							String requires = null;
							if (parent != null)
								requires = parent.getName(course.getCourseId());
							
							XSubpart failedSubpart = getFailedOffering().getSubpart(failed.getSubpartId());
							XSection failedParent = (failed.getParentId() == null ? null : getFailedOffering().getSection(failed.getParentId()));
							String failedRequires = null;
							if (failedParent != null)
								failedRequires = failedParent.getName(course.getCourseId());
							
							if (failedRequires == null && requires == null) {
								requires = consent;
								failedRequires = consent;
								consent = null;
							}

							listOfChanges.add(new TableSectionModifiedLine(newRequest, course, subpart, failedSubpart, section, failed, requires, failedRequires, getCourseUrl(session, course), plainText));
							continue sections;
						}
					}
					
					String requires = null;
					if (parent != null) {
						requires = parent.getName(course.getCourseId());
					} else {
						requires = consent; consent = null;
					}
					listOfChanges.add(new TableSectionDeletedLine(newRequest, course, subpart, section, requires, getCourseUrl(session, course), plainText));
				}
				sections: for (XSection failed: getFailedOffering().getSections(getFailedEnrollment())) {
					for (XSection section: newOffering.getSections(newRequest.getEnrollment()))
						if (failed.getSubpartId().equals(section.getSubpartId())) continue sections;
					
					XSubpart subpart = getFailedOffering().getSubpart(failed.getSubpartId());
					XSection parent = (failed.getParentId() == null ? null : getFailedOffering().getSection(failed.getParentId()));
					String requires = null;
					if (parent != null)
						requires = parent.getName(course.getCourseId());
					listOfChanges.add(new TableSectionLine(newRequest, course, subpart, failed, requires, getCourseUrl(session, course), plainText));
				}

				input.put("changes", listOfChanges);
			} else {
				if (getOldOffering() != null && getOldEnrollment() != null) {
					for (XSection old: getOldOffering().getSections(getOldEnrollment())) {
						XSubpart subpart = getOldOffering().getSubpart(old.getSubpartId());
						XSection parent = (old.getParentId() == null ? null : getOldOffering().getSection(old.getParentId()));
						String requires = null;
						if (parent != null)
							requires = parent.getName(course.getCourseId());
						listOfChanges.add(new TableSectionDeletedLine(newRequest, course, subpart, old, requires, getCourseUrl(session, course), plainText));
					}
					input.put("changes", listOfChanges);
				}
				setSubject(MSG.emailDropFailed(course.getSubjectArea(), course.getCourseNumber(), iFailure == null ? null : iFailure.getMessage()));
			}
			if (iFailure != null) {
				String message = MSG.emailEnrollmentFailedMessage(iFailure.getMessage());
				if (iFailure.hasErrors())
					for (ErrorMessage error: iFailure.getErrors()) {
						//if (course.getCourseName().startsWith(error.getCourse()))
						message += "<br>" + error;
					}
				input.put("changeMessage", message);
			}
		} else if (getOldOffering() != null) {
			Table listOfChanges = new Table();

			XCourseRequest newRequest = null;
			XOffering newOffering = null;
			XCourse course = (getOldEnrollment() != null ? getOldOffering().getCourse(getOldEnrollment().getCourseId()) : getOldCourse());
			XCourse oldCourse = course;
			for (XRequest r: getStudent().getRequests()) {
				if (r instanceof XCourseRequest && (
						(getOldCourse() == null && ((XCourseRequest)r).getCourseIdByOfferingId(getOldOffering().getOfferingId()) != null) ||
						(getOldCourse() != null && ((XCourseRequest)r).hasCourse(getOldCourse().getCourseId()))
						)) {
					newRequest = (XCourseRequest)r;
					if (newRequest.getEnrollment() != null) {
						newOffering = server.getOffering(newRequest.getEnrollment().getOfferingId());
						course = newOffering.getCourse(newRequest.getEnrollment().getCourseId());
					} else {
						newOffering = server.getOffering(getOldOffering().getOfferingId());
					}
					break;
				}
			}
			input.put("changedCourse", course);
			
			if (getDropEnrollment() != null && !course.equals(getDropEnrollment())) {
				XOffering dropOffering = server.getOffering(getDropEnrollment().getOfferingId());
				if (dropOffering != null) {
					XCourse dropCourse = dropOffering.getCourse(getDropEnrollment().getCourseId());
					XCourseRequest dropRequest = getStudent().getRequestForCourse(dropCourse.getCourseId());
					for (XSection old: dropOffering.getSections(getDropEnrollment())) {
						XSubpart subpart = dropOffering.getSubpart(old.getSubpartId());
						XSection parent = (old.getParentId() == null ? null : dropOffering.getSection(old.getParentId()));
						String requires = null;
						if (parent != null)
							requires = parent.getName(dropCourse.getCourseId());
						listOfChanges.add(new TableSectionDeletedLine(dropRequest, dropCourse, subpart, old, requires, getCourseUrl(session, dropCourse), plainText));
					}
				}
			}

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
					listOfChanges.add(new TableSectionLine(newRequest, course, subpart, section, requires, getCourseUrl(session, course), plainText));
				}
				input.put("changes", listOfChanges);
			} else if (getOldEnrollment() != null && newRequest != null && newRequest.getEnrollment() != null) {
				if (oldCourse.equals(course)) {
					setSubject(MSG.emailEnrollmentChanged(course.getSubjectArea(), course.getCourseNumber()));
				} else { 
					setSubject(MSG.emailEnrollmentNew(course.getSubjectArea(), course.getCourseNumber()));
				}

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

							listOfChanges.add(new TableSectionModifiedLine(newRequest, course, oldSubpart, subpart, old, section, oldRequires, requires, getCourseUrl(session, course), plainText));
							continue sections;
						}
					}
					
					String requires = null;
					if (parent != null) {
						requires = parent.getName(course.getCourseId());
					} else {
						requires = consent; consent = null;
					}
					listOfChanges.add(new TableSectionLine(newRequest, course, subpart, section, requires, getCourseUrl(session, course), plainText));
				}
				sections: for (XSection old: getOldOffering().getSections(getOldEnrollment())) {
					for (XSection section: newOffering.getSections(newRequest.getEnrollment()))
						if (old.getSubpartId().equals(section.getSubpartId())) continue sections;
					
					XSubpart subpart = getOldOffering().getSubpart(old.getSubpartId());
					XSection parent = (old.getParentId() == null ? null : getOldOffering().getSection(old.getParentId()));
					String requires = null;
					if (parent != null)
						requires = parent.getName(oldCourse.getCourseId());
					listOfChanges.add(new TableSectionDeletedLine(newRequest, oldCourse, subpart, old, requires, getCourseUrl(session, course), plainText));
				}

				input.put("changes", listOfChanges);
			} else if (getOldEnrollment() != null && (newRequest == null || newRequest.getEnrollment() == null)) {
				setSubject(newRequest == null
						? MSG.emailCourseDropReject(course.getSubjectArea(), course.getCourseNumber())
						: MSG.emailCourseDropChange(course.getSubjectArea(), course.getCourseNumber()));
				if (newRequest !=  null && getStudent().canAssign(newRequest, wlMode)) {
					input.put("changeMessage", (newRequest.isAlternative() ?
							newRequest.isWaitlist() ? MSG.emailCourseWaitListedAlternative() : MSG.emailCourseNotEnrolledAlternative() :
							newRequest.isWaitlist() ? MSG.emailCourseWaitListed() : MSG.emailCourseNotEnrolled()));
				} else if (newRequest == null && course.getConsentLabel() != null) {
					input.put("changeMessage", MSG.emailConsentRejected(course.getConsentLabel().toLowerCase()));
				}
				if (getOldOffering() != null && getOldEnrollment() != null) {
					for (XSection old: getOldOffering().getSections(getOldEnrollment())) {
						XSubpart subpart = getOldOffering().getSubpart(old.getSubpartId());
						XSection parent = (old.getParentId() == null ? null : getOldOffering().getSection(old.getParentId()));
						String requires = null;
						if (parent != null)
							requires = parent.getName(course.getCourseId());
						listOfChanges.add(new TableSectionDeletedLine(newRequest, course, subpart, old, requires, getCourseUrl(session, course), plainText));
					}
					input.put("changes", listOfChanges);
				}
			}
		} else if (getOldStudent() != null) {
			boolean somethingWasOrIsAssigned = false;
			for (XRequest or: getOldStudent().getRequests()) {
				if (or instanceof XCourseRequest && ((XCourseRequest)or).getEnrollment() != null) {
					somethingWasOrIsAssigned = true; break;
				}
			}
			if (!iIncludeClassSchedule)
				for (XRequest nr: getStudent().getRequests()) {
					if (nr instanceof XCourseRequest && ((XCourseRequest)nr).getEnrollment() != null) {
						somethingWasOrIsAssigned = true; break;
					}
				}
			if (somethingWasOrIsAssigned) {
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
									listOfChanges.add(new TableSectionLine(ncr, course, subpart, section, requires, getCourseUrl(session, course), plainText));
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
									listOfChanges.add(new TableSectionDeletedLine(ncr, course, subpart, section, requires, getCourseUrl(session, course), plainText));
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
											
											listOfChanges.add(new TableSectionModifiedLine(ncr, course, oldSubpart, subpart, old, section, oldRequires, requires, getCourseUrl(session, course), plainText));
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
									listOfChanges.add(new TableSectionLine(ncr, course, subpart, section, requires, getCourseUrl(session, course), plainText));
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

									listOfChanges.add(new TableSectionDeletedLine(ocr, course, subpart, old, requires, getCourseUrl(session, course), plainText));
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
							listOfChanges.add(new TableSectionLine(ncr, course, subpart, section, requires, getCourseUrl(session, course), plainText));
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

						listOfChanges.add(new TableSectionDeletedLine(ocr, course, subpart, section, requires, getCourseUrl(session, course), plainText));
					}
				}
				if (iSkipWhenNoChange && listOfChanges.isEmpty()) return null;
				input.put("changes", listOfChanges);				
			} else {
				if (iSkipWhenNoChange) return null;
				if (iIncludeClassSchedule)
					setSubject(MSG.emailSubjectNotificationClassSchedule());
				else if (iIncludeCourseRequests)
					setSubject(MSG.emailSubjectNotificationCourseRequests());
				else
					setSubject(MSG.emailSubjectNotification());
			}
		} else {
			if (iIncludeClassSchedule)
				setSubject(MSG.emailSubjectNotificationClassSchedule());
			else if (iIncludeCourseRequests)
				setSubject(MSG.emailSubjectNotificationCourseRequests());
			else
				setSubject(MSG.emailSubjectNotification());
		}
		
		if (getEmailSubject() != null && !getEmailSubject().isEmpty())
			input.put("subject", getEmailSubject().replace("%session%", server.getAcademicSession().toString()));
		else if (getSubject() != null && !getSubject().isEmpty())
			input.put("subject", getSubject().replace("%session%", server.getAcademicSession().toString()));
		
		if (!iPermisionCheck) {
			input.put("manager", true);
		} else if ("not-set".equals(iSourceAction) || "enroll".equals(iSourceAction) || "advisor-submit".equals(iSourceAction)
			|| "mass-cancel".equals(iSourceAction) || "reject-enrollments".equals(iSourceAction) || "approve-enrollments".equals(iSourceAction)
			|| "save-request".equals(iSourceAction)) {
			input.put("manager", (helper.getUser() != null && helper.getUser().getType() == OnlineSectioningLog.Entity.EntityType.MANAGER));
		} else {
			input.put("manager", false);
		}
		input.put("changed", getOldEnrollment() != null || getOldStudent() != null);
		input.put("version", GWT.unitimeVersion(Constants.getVersion()));
		input.put("copyright", GWT.pageCopyright());
		input.put("ts", sTimeStampFormat.format(getTimeStamp()));
		if (ApplicationProperty.OnlineSchedulingEmailIncludeLink.isTrue())
			input.put("link", ApplicationProperty.UniTimeUrl.value());
		StringWriter s = new StringWriter();
		template.process(input, new PrintWriter(s));
		s.flush(); s.close();

		return s.toString();
	}
	
	CourseRequestsTable generateCourseRequests(org.unitime.timetable.model.Student student, CourseRequestInterface requests, OnlineSectioningServer server, OnlineSectioningHelper helper, WaitListMode wlMode) {
		if (requests.getWaitListMode() == null) requests.setWaitListMode(wlMode);
		Set<Long> advisorWaitListedCourseIds = (iStudent == null ? null : iStudent.getAdvisorWaitListedCourseIds(server));
		CourseRequestsTable courseRequests = new CourseRequestsTable();
		Format<Number> df = Formats.getNumberFormat("0.#");
		CheckCoursesResponse check = new CheckCoursesResponse(requests.getConfirmations());
		courseRequests.hasWarn = requests.hasConfirmations();
		int priority = 1;
		for (Request request: requests.getCourses()) {
			if (!request.hasRequestedCourse()) continue;
			boolean first = true; int idx = 0;
			String firstChoice = null;
			if (request.isWaitlistOrNoSub(wlMode)) courseRequests.hasWait = true;
			for (RequestedCourse rc: request.getRequestedCourse()) {
				if (rc.isCourse()) {
					String icon = null; String iconText = null;
					String msg = check.getMessage(rc.getCourseName(), "\n", "CREDIT");
					if (check.isError(rc.getCourseName()) && (rc.getStatus() == null || rc.getStatus() != RequestedCourseStatus.OVERRIDE_REJECTED)) {
						icon = "stop.png";
						iconText = (msg);
					} else if (rc.getStatus() != null) {
						switch (rc.getStatus()) {
						case ENROLLED:
							icon = "login.png";
							iconText = (MSG.enrolled(rc.getCourseName()));
							break;
						case OVERRIDE_NEEDED:
							icon = "attention.png";
							iconText = (MSG.overrideNeeded(msg));
							break;
						case SAVED:
							icon = "action_check.png";
							iconText = ((msg == null ? "" : MSG.requestWarnings(msg) + "\n\n") + MSG.requested(rc.getCourseName()));
							break;
						case OVERRIDE_REJECTED:
							icon = "stop.png";
							iconText = ((msg == null ? "" : MSG.requestWarnings(msg) + "\n\n") + MSG.overrideRejected(rc.getCourseName()));
							break;
						case OVERRIDE_PENDING:
							icon = "time.png";
							iconText = ((msg == null ? "" : MSG.requestWarnings(msg) + "\n\n") + MSG.overridePending(rc.getCourseName()));
							break;
						case OVERRIDE_CANCELLED:
							icon = "attention.png";
							iconText = ((msg == null ? "" : MSG.requestWarnings(msg) + "\n\n") + MSG.overrideCancelled(rc.getCourseName()));
							break;
						case OVERRIDE_APPROVED:
							icon = "action_check.png";
							iconText = ((msg == null ? "" : MSG.requestWarnings(msg) + "\n\n") + MSG.overrideApproved(rc.getCourseName()));
							break;
						case OVERRIDE_NOT_NEEDED:
							icon = "action_check_gray.png";
							iconText = ((msg == null ? "" : MSG.requestWarnings(msg) + "\n\n") + MSG.overrideNotNeeded(rc.getCourseName()));
							break;
						case WAITLIST_INACTIVE:
							icon = "action_check_gray.png";
							iconText = ((msg == null ? "" : MSG.requestWarnings(msg) + "\n\n") + MSG.waitListInactive(rc.getCourseName()));
							break;
						default:
							if (check.isError(rc.getCourseName()))
								icon = "stop.png";
							iconText = (msg);
						}
					}
					if (rc.hasStatusNote()) iconText += "\n" + MSG.overrideNote(rc.getStatusNote());
					Collection<Preference> prefs = null;
					if (rc.hasSelectedIntructionalMethods()) {
						if (rc.hasSelectedClasses()) {
							prefs = new ArrayList<Preference>(rc.getSelectedIntructionalMethods().size() + rc.getSelectedClasses().size());
							prefs.addAll(new TreeSet<Preference>(rc.getSelectedIntructionalMethods()));
							prefs.addAll(new TreeSet<Preference>(rc.getSelectedClasses()));
						} else {
							prefs = new TreeSet<Preference>(rc.getSelectedIntructionalMethods());
						}
					} else if (rc.hasSelectedClasses()) {
						prefs = new TreeSet<Preference>(rc.getSelectedClasses());
					}
					String status = "";
					if (rc.getStatus() != null) {
						switch (rc.getStatus()) {
						case ENROLLED: status = MSG.reqStatusEnrolled(); break;
						case OVERRIDE_APPROVED: status = MSG.reqStatusApproved(); break;
						case OVERRIDE_CANCELLED: status = MSG.reqStatusCancelled(); break;
						case OVERRIDE_PENDING: status = MSG.reqStatusPending(); break;
						case OVERRIDE_REJECTED: status = MSG.reqStatusRejected(); break;
						case OVERRIDE_NOT_NEEDED: status = MSG.reqStatusNotNeeded(); break;
						}
					}
					if (status.isEmpty()) {
						if (request.isWaitList())
							status = MSG.reqStatusWaitListed();
						else
							status = MSG.reqStatusRegistered();
					}
					if (prefs != null) courseRequests.hasPref = true;
					String credit = (rc.hasCredit() ? (rc.getCreditMin().equals(rc.getCreditMax()) ? df.format(rc.getCreditMin()) : df.format(rc.getCreditMin()) + " - " + df.format(rc.getCreditMax())) : "");
					String note = null;
					if (check != null) note = check.getMessageWithColor(rc.getCourseName(), "<br>", "CREDIT");
					if (rc.hasStatusNote()) note = (note == null ? "" : note + "<br>") + rc.getStatusNote();
					CourseRequestLine line = new CourseRequestLine();
					line.priority = (first ? MSG.courseRequestsPriority(priority) : "");
					line.courseName = rc.getCourseName();
					line.courseTitle = (rc.hasCourseTitle() ? rc.getCourseTitle() : "");
					line.url = getCourseUrl(server, rc);
					line.credit = credit;
					line.prefs = toString(prefs);
					line.note = note;
					line.icon = icon;
					line.iconText = iconText;
					line.status = status;
					line.waitlist = (first && request.isWaitlistOrNoSub(wlMode));
					if (first && request.isWaitList() && wlMode == WaitListMode.WaitList && request.getWaitListedTimeStamp() != null)
						line.waitListDate = sTimeStampFormat.format(request.getWaitListedTimeStamp());
					line.first = (priority > 1 && first);
					line.idx = idx;
					if (firstChoice == null) firstChoice = rc.getCourseName();
					line.firstChoice = firstChoice;
					courseRequests.add(line);
				} else if (rc.isFreeTime()) {
					CourseRequestLine line = new CourseRequestLine();
					String  free = "";
					for (FreeTime ft: rc.getFreeTime()) {
						if (!free.isEmpty()) free += ", ";
						free += ft.toString(CONST.shortDays(), CONST.useAmPm());
					}
					String note = null;
					line.priority = (first ? MSG.courseRequestsPriority(priority) : "");
					line.courseName = CONST.freePrefix() + free;
					line.courseTitle = "";
					line.credit = "";
					line.prefs = "";
					line.note = note;
					line.icon = "action_check.png";
					line.iconText = MSG.requested(free);
					line.status = MSG.reqStatusRegistered();
					line.waitlist = false;
					line.first = (priority > 1 && first);
					line.idx = idx;
					if (firstChoice == null) firstChoice = CONST.freePrefix() + free;
					line.firstChoice = firstChoice;
					courseRequests.add(line);
				}
				first = false; idx ++;
			}
			priority ++;
		}
		
		priority = 1;
		for (Request request: requests.getAlternatives()) {
			if (!request.hasRequestedCourse()) continue;
			boolean first = true; int idx = 0; String firstChoice = null;
			if (request.isWaitlistOrNoSub(wlMode)) courseRequests.hasWait = true;
			for (RequestedCourse rc: request.getRequestedCourse()) {
				if (rc.isCourse()) {
					String icon = null; String iconText = null;
					String msg = check.getMessage(rc.getCourseName(), "\n", "CREDIT");
					if (check.isError(rc.getCourseName()) && (rc.getStatus() == null || rc.getStatus() != RequestedCourseStatus.OVERRIDE_REJECTED)) {
						icon = "stop.png";
						iconText = (msg);
					} else if (rc.getStatus() != null) {
						switch (rc.getStatus()) {
						case ENROLLED:
							icon = "login.png";
							iconText = (MSG.enrolled(rc.getCourseName()));
							break;
						case OVERRIDE_NEEDED:
							icon = "attention.png";
							iconText = (MSG.overrideNeeded(msg));
							break;
						case SAVED:
							icon = "action_check.png";
							iconText = ((msg == null ? "" : MSG.requestWarnings(msg) + "\n\n") + MSG.requested(rc.getCourseName()));
							break;
						case OVERRIDE_REJECTED:
							icon = "stop.png";
							iconText = ((msg == null ? "" : MSG.requestWarnings(msg) + "\n\n") + MSG.overrideRejected(rc.getCourseName()));
							break;
						case OVERRIDE_PENDING:
							icon = "time.png";
							iconText = ((msg == null ? "" : MSG.requestWarnings(msg) + "\n\n") + MSG.overridePending(rc.getCourseName()));
							break;
						case OVERRIDE_CANCELLED:
							icon = "attention.png";
							iconText = ((msg == null ? "" : MSG.requestWarnings(msg) + "\n\n") + MSG.overrideCancelled(rc.getCourseName()));
							break;
						case OVERRIDE_APPROVED:
							icon = "action_check.png";
							iconText = ((msg == null ? "" : MSG.requestWarnings(msg) + "\n\n") + MSG.overrideApproved(rc.getCourseName()));
							break;
						case OVERRIDE_NOT_NEEDED:
							icon = "action_check_gray.png";
							iconText = ((msg == null ? "" : MSG.requestWarnings(msg) + "\n\n") + MSG.overrideNotNeeded(rc.getCourseName()));
							break;
						case WAITLIST_INACTIVE:
							icon = "action_check_gray.png";
							iconText = ((msg == null ? "" : MSG.requestWarnings(msg) + "\n\n") + MSG.waitListInactive(rc.getCourseName()));
							break;
						default:
							if (check.isError(rc.getCourseName()))
								icon = "stop.png";
							iconText = (msg);
						}
					}
					if (rc.hasStatusNote()) iconText += "\n" + MSG.overrideNote(rc.getStatusNote());
					Collection<Preference> prefs = null;
					if (rc.hasSelectedIntructionalMethods()) {
						if (rc.hasSelectedClasses()) {
							prefs = new ArrayList<Preference>(rc.getSelectedIntructionalMethods().size() + rc.getSelectedClasses().size());
							prefs.addAll(new TreeSet<Preference>(rc.getSelectedIntructionalMethods()));
							prefs.addAll(new TreeSet<Preference>(rc.getSelectedClasses()));
						} else {
							prefs = new TreeSet<Preference>(rc.getSelectedIntructionalMethods());
						}
					} else if (rc.hasSelectedClasses()) {
						prefs = new TreeSet<Preference>(rc.getSelectedClasses());
					}
					String status = "";
					if (rc.getStatus() != null) {
						switch (rc.getStatus()) {
						case ENROLLED: status = MSG.reqStatusEnrolled(); break;
						case OVERRIDE_APPROVED: status = MSG.reqStatusApproved(); break;
						case OVERRIDE_CANCELLED: status = MSG.reqStatusCancelled(); break;
						case OVERRIDE_PENDING: status = MSG.reqStatusPending(); break;
						case OVERRIDE_REJECTED: status = MSG.reqStatusRejected(); break;
						case OVERRIDE_NOT_NEEDED: status = MSG.reqStatusNotNeeded(); break;
						}
					}
					if (status.isEmpty()) status = MSG.reqStatusRegistered();
					if (prefs != null) courseRequests.hasPref = true;
					String credit = (rc.hasCredit() ? (rc.getCreditMin().equals(rc.getCreditMax()) ? df.format(rc.getCreditMin()) : df.format(rc.getCreditMin()) + " - " + df.format(rc.getCreditMax())) : "");
					String note = null;
					if (check != null) note = check.getMessageWithColor(rc.getCourseName(), "<br>", "CREDIT");
					if (rc.hasStatusNote()) note = (note == null ? "" : note + "<br>") + rc.getStatusNote();
					CourseRequestLine line = new CourseRequestLine();
					line.priority = (first ? MSG.courseRequestsAlternate(priority) : "");
					line.courseName = rc.getCourseName();
					line.courseTitle = (rc.hasCourseTitle() ? rc.getCourseTitle() : "");
					line.url = getCourseUrl(server, rc);
					line.credit = credit;
					line.prefs = toString(prefs);
					line.note = note;
					line.icon = icon;
					line.iconText = iconText;
					line.status = status;
					line.waitlist = (first && request.isWaitlistOrNoSub(wlMode));
					if (first && request.isWaitList() && wlMode == WaitListMode.WaitList && request.getWaitListedTimeStamp() != null)
						line.waitListDate = sTimeStampFormat.format(request.getWaitListedTimeStamp());
					line.first = first;
					line.idx = idx;
					line.firstalt = (first && priority == 1);
					if (firstChoice == null) firstChoice = rc.getCourseName();
					line.firstChoice = firstChoice;
					courseRequests.add(line);
				} else if (rc.isFreeTime()) {
					CourseRequestLine line = new CourseRequestLine();
					String free = "";
					for (FreeTime ft: rc.getFreeTime()) {
						if (!free.isEmpty()) free += ", ";
						free += ft.toString(CONST.shortDays(), CONST.useAmPm());
					}
					line.priority = (first ? MSG.courseRequestsAlternate(priority) : "");
					line.courseName = CONST.freePrefix() + free;
					line.courseTitle = "";
					line.credit = "";
					line.prefs = "";
					line.icon = "action_check.png";
					line.iconText = MSG.requested(free);
					line.status = MSG.reqStatusRegistered();
					line.waitlist = false;
					line.first = first;
					line.idx = idx;
					line.firstalt = (first && priority == 1);
					if (firstChoice == null) firstChoice = CONST.freePrefix() + free;
					line.firstChoice = firstChoice;
					courseRequests.add(line);
				}
				first = false; idx ++;
			}
			priority ++;
		}
		
		if (requests.getMaxCreditOverrideStatus() != null) {
			String icon = null;
			String status = "";
			String note = null;
			String iconText = null;
			if (requests.hasCreditWarning()) {
				note = requests.getCreditWarning();
				iconText = requests.getCreditWarning();
				courseRequests.hasWarn = true;
			}
			switch (requests.getMaxCreditOverrideStatus()) {
			case CREDIT_HIGH:
				icon = "attention.png";
				status = MSG.reqStatusWarning();
				iconText += "\n" + MSG.creditStatusTooHigh();
				break;
			case OVERRIDE_REJECTED:
				icon = "stop.png";
				status = MSG.reqStatusRejected();
				iconText += "\n" + MSG.creditStatusDenied();
				break;
			case CREDIT_LOW:
			case OVERRIDE_NEEDED:
				icon = "attention.png";
				status = MSG.reqStatusWarning();
				break;
			case OVERRIDE_CANCELLED:
				icon = "attention.png";
				status = MSG.reqStatusCancelled();
				iconText += "\n" + MSG.creditStatusCancelled();
				break;
			case OVERRIDE_PENDING:
				icon = "time.png";
				status = MSG.reqStatusPending();
				iconText += "\n" + MSG.creditStatusPending();
				break;
			case OVERRIDE_APPROVED:
				icon = "action_check.png";
				status = MSG.reqStatusApproved();
				iconText += (iconText == null ? "" : iconText + "\n") + MSG.creditStatusApproved();
				break;
			case SAVED:
				icon = "action_check.png";
				status = MSG.reqStatusRegistered();
				break;
			}
			if (requests.hasCreditNote()) {
				note = (note == null ? "" : note + "\n") + requests.getCreditNote();
				courseRequests.hasWarn = true;
			}
			float[] range = requests.getCreditRange(advisorWaitListedCourseIds);
			String credit = (range != null ? range[0] < range[1] ? df.format(range[0]) + " - " + df.format(range[1]) : df.format(range[0]) : "");
			CourseRequestLine line = new CourseRequestLine();
			line.priority = "";
			line.courseName = MSG.rowRequestedCredit();
			line.courseTitle = "";
			line.credit = credit;
			line.prefs = "";
			line.note = note;
			line.icon = icon;
			line.iconText = iconText;
			line.status = status;
			line.last = true;
			courseRequests.add(line);
		} else {
			float[] range = requests.getCreditRange(advisorWaitListedCourseIds);
			if (range != null && range[1] > 0f) {
				String credit = (range != null ? range[0] < range[1] ? df.format(range[0]) + " - " + df.format(range[1]) : df.format(range[0]) : "");
				CourseRequestLine line = new CourseRequestLine();
				line.priority = "";
				line.courseName = MSG.rowRequestedCredit();
				line.courseTitle = "";
				line.credit = credit;
				line.prefs = "";
				line.note = null;
				line.icon = "";
				line.iconText = "";
				line.status = "";
				line.last = true;
				courseRequests.add(line);
			}
		}

		return courseRequests;
	}
	
	CourseRequestsTable generateAdvisorRequests(org.unitime.timetable.model.Student student, CourseRequestInterface requests, OnlineSectioningServer server, OnlineSectioningHelper helper, WaitListMode wlMode) {
		CourseRequestsTable courseRequests = new CourseRequestsTable();
		Format<Number> df = Formats.getNumberFormat("0.#");
		int priority = 1;
		Integer critical = null;
		if ("Critical".equals(ApplicationProperty.AdvisorCourseRequestsAllowCritical.value())) {
			critical = CourseDemand.Critical.CRITICAL.ordinal();
			courseRequests.criticalColumn = MSG.opSetCritical();
			courseRequests.criticalColumnDescription = MSG.descriptionRequestCritical();
		} else if ("Vital".equals(ApplicationProperty.AdvisorCourseRequestsAllowCritical.value())) {
			critical = CourseDemand.Critical.VITAL.ordinal();
			courseRequests.criticalColumn = MSG.opSetVital();
			courseRequests.criticalColumnDescription = MSG.descriptionRequestVital();
		} else if ("Important".equals(ApplicationProperty.AdvisorCourseRequestsAllowCritical.value())) {
			critical = CourseDemand.Critical.IMPORTANT.ordinal();
			courseRequests.criticalColumn = MSG.opSetImportant();
			courseRequests.criticalColumnDescription = MSG.descriptionRequestImportant();
		}
		for (Request request: requests.getCourses()) {
			if (request.hasRequestedCourse()) {
				boolean first = true; int idx = 0; String firstChoice = null;
				if (request.isWaitlistOrNoSub(wlMode)) courseRequests.hasWait = true;
				for (RequestedCourse rc: request.getRequestedCourse()) {
					if (rc.isCourse()) {
						Collection<Preference> prefs = null;
						if (rc.hasSelectedIntructionalMethods()) {
							if (rc.hasSelectedClasses()) {
								prefs = new ArrayList<Preference>(rc.getSelectedIntructionalMethods().size() + rc.getSelectedClasses().size());
								prefs.addAll(new TreeSet<Preference>(rc.getSelectedIntructionalMethods()));
								prefs.addAll(new TreeSet<Preference>(rc.getSelectedClasses()));
							} else {
								prefs = new TreeSet<Preference>(rc.getSelectedIntructionalMethods());
							}
						} else if (rc.hasSelectedClasses()) {
							prefs = new TreeSet<Preference>(rc.getSelectedClasses());
						}
						if (prefs != null) courseRequests.hasPref = true;
						String credit = (first && request.hasAdvisorCredit() ? request.getAdvisorCredit() : "");
						String note = (first && request.hasAdvisorNote() ? request.getAdvisorNote() : "");
						CourseRequestLine line = new CourseRequestLine();
						line.priority = (first ? MSG.courseRequestsPriority(priority) : "");
						line.courseName = rc.getCourseName();
						line.courseTitle = (rc.hasCourseTitle() ? rc.getCourseTitle() : "");
						line.url = getCourseUrl(server, rc);
						line.credit = credit;
						line.prefs = toString(prefs);
						line.note = note;
						line.first = (priority > 1 && first);
						line.rows = (first ? request.getRequestedCourse().size() : 0);
						line.idx = idx;
						line.waitlist = (first && request.isWaitlistOrNoSub(wlMode));
						if (first && critical != null && critical.equals(request.getCritical())) {
							line.critical = true;
							courseRequests.hasCritical = true;
						}
						if (firstChoice == null) firstChoice = rc.getCourseName();
						line.firstChoice = firstChoice;
						courseRequests.add(line);
					} else if (rc.isFreeTime()) {
						String  free = "";
						for (FreeTime ft: rc.getFreeTime()) {
							if (!free.isEmpty()) free += ", ";
							free += ft.toString(CONST.shortDays(), CONST.useAmPm());
						}
						String credit = (first && request.hasAdvisorCredit() ? request.getAdvisorCredit() : "");
						String note = (first && request.hasAdvisorNote() ? request.getAdvisorNote() : "");
						CourseRequestLine line = new CourseRequestLine();
						line.priority = (first ? MSG.courseRequestsPriority(priority) : "");
						line.courseName = CONST.freePrefix() + free;
						line.courseTitle = "";
						line.credit = credit;
						line.note = note;
						line.prefs = "";
						line.first = (priority > 1 && first);
						line.idx = idx;
						line.rows = (first ? request.getRequestedCourse().size() : 0);
						line.waitlist = false;
						if (firstChoice == null) firstChoice = CONST.freePrefix() + free;
						line.firstChoice = firstChoice;
						courseRequests.add(line);
					}
					first = false; idx ++;
				}
			} else {
				String credit = (request.hasAdvisorCredit() ? request.getAdvisorCredit() : "");
				String note = (request.hasAdvisorNote() ? request.getAdvisorNote() : "");
				CourseRequestLine line = new CourseRequestLine();
				line.priority = MSG.courseRequestsPriority(priority);
				line.courseName = "";
				line.courseTitle = "";
				line.credit = credit;
				line.note = note;
				line.prefs = "";
				line.first = (priority > 1);
				line.idx = 0;
				courseRequests.add(line);
			}
			priority ++;				
		}
		priority = 1;
		for (Request request: requests.getAlternatives()) {
			if (request.hasRequestedCourse()) {
				boolean first = true; int idx = 0; String firstChoice = null;
				if (request.isWaitlistOrNoSub(wlMode)) courseRequests.hasWait = true;
				for (RequestedCourse rc: request.getRequestedCourse()) {
					if (rc.isCourse()) {
						Collection<Preference> prefs = null;
						if (rc.hasSelectedIntructionalMethods()) {
							if (rc.hasSelectedClasses()) {
								prefs = new ArrayList<Preference>(rc.getSelectedIntructionalMethods().size() + rc.getSelectedClasses().size());
								prefs.addAll(new TreeSet<Preference>(rc.getSelectedIntructionalMethods()));
								prefs.addAll(new TreeSet<Preference>(rc.getSelectedClasses()));
							} else {
								prefs = new TreeSet<Preference>(rc.getSelectedIntructionalMethods());
							}
						} else if (rc.hasSelectedClasses()) {
							prefs = new TreeSet<Preference>(rc.getSelectedClasses());
						}
						if (prefs != null) courseRequests.hasPref = true;
						String credit = (first && request.hasAdvisorCredit() ? request.getAdvisorCredit() : "");
						String note = (first && request.hasAdvisorNote() ? request.getAdvisorNote() : "");
						CourseRequestLine line = new CourseRequestLine();
						line.priority = (first ? MSG.courseRequestsAlternate(priority) : "");
						line.courseName = rc.getCourseName();
						line.courseTitle = (rc.hasCourseTitle() ? rc.getCourseTitle() : "");
						line.credit = credit;
						line.prefs = toString(prefs);
						line.note = note;
						line.first = first;
						line.idx = idx;
						line.firstalt = (first && priority == 1);
						line.rows = (first ? request.getRequestedCourse().size() : 0);
						line.waitlist = (first && request.isWaitlistOrNoSub(wlMode));
						if (firstChoice == null) firstChoice = rc.getCourseName();
						line.firstChoice = firstChoice;
						courseRequests.add(line);
					} else if (rc.isFreeTime()) {
						CourseRequestLine line = new CourseRequestLine();
						String free = "";
						for (FreeTime ft: rc.getFreeTime()) {
							if (!free.isEmpty()) free += ", ";
							free += ft.toString(CONST.shortDays(), CONST.useAmPm());
						}
						String credit = (first && request.hasAdvisorCredit() ? request.getAdvisorCredit() : "");
						String note = (first && request.hasAdvisorNote() ? request.getAdvisorNote() : "");
						line.priority = (first ? MSG.courseRequestsAlternate(priority) : "");
						line.courseName = CONST.freePrefix() + free;
						line.courseTitle = "";
						line.credit = credit;
						line.prefs = "";
						line.note = note;
						line.first = first;
						line.idx = idx;
						line.firstalt = (first && priority == 1);
						line.rows = (first ? request.getRequestedCourse().size() : 0);
						line.waitlist = false;
						if (firstChoice == null) firstChoice = CONST.freePrefix() + free;
						line.firstChoice = firstChoice;
						courseRequests.add(line);
					}
					first = false; idx ++;
				}
			} else {
				String credit = (request.hasAdvisorCredit() ? request.getAdvisorCredit() : "");
				String note = (request.hasAdvisorNote() ? request.getAdvisorNote() : "");
				CourseRequestLine line = new CourseRequestLine();
				line.priority = MSG.courseRequestsAlternate(priority);
				line.courseName = "";
				line.courseTitle = "";
				line.credit = credit;
				line.note = note;
				line.prefs = "";
				line.first = (priority > 1);
				line.idx = 0;
				courseRequests.add(line);
			}
			priority ++;
		}
		
		float[] minMax = requests.getAdvisorCreditRange();
		String note = (requests.hasCreditNote() ? requests.getCreditNote() : "");
		String credit = (minMax[0] < minMax[1] ? df.format(minMax[0]) + " - " + df.format(minMax[1]) : df.format(minMax[0]));
		CourseRequestLine line = new CourseRequestLine();
		line.priority = "";
		line.courseName = MSG.rowTotalPriorityCreditHours();
		line.courseTitle = "";
		line.credit = credit;
		line.prefs = "";
		line.note = note;
		line.last = true;
		courseRequests.add(line);

		return courseRequests;
	}
	
	Table generateListOfClasses(org.unitime.timetable.model.Student student, OnlineSectioningServer server, OnlineSectioningHelper helper, WaitListMode wlMode, boolean plainText) {
		Table listOfClasses = new Table();
		AcademicSessionInfo session = server.getAcademicSession();
		for (XRequest request: getStudent().getRequests()) {
			if (request instanceof XCourseRequest) {
				XCourseRequest cr = (XCourseRequest)request;
				XEnrollment enrollment = cr.getEnrollment();
				if (enrollment == null) {
					if (!getStudent().canAssign(cr, wlMode)) continue;
					if (!cr.isWaitlist(wlMode)) continue;
					XCourse course = server.getCourse(cr.getCourseIds().get(0).getCourseId());
					XCourse swapCourse = null;
					if (cr.getWaitListSwapWithCourseOffering() != null && wlMode == WaitListMode.WaitList && cr.isWaitlist(wlMode)) {
						XCourseRequest swap = getStudent().getRequestForCourse(cr.getWaitListSwapWithCourseOffering().getCourseId());
						if (swap != null && swap.getEnrollment() != null && swap.getEnrollment().getCourseId().equals(cr.getWaitListSwapWithCourseOffering().getCourseId()))
							swapCourse = server.getCourse(swap.getEnrollment().getCourseId());
					}
					listOfClasses.add(new TableCourseLine(cr, course, getCourseUrl(session, course), wlMode == WaitListMode.WaitList, swapCourse, plainText));
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
						listOfClasses.add(new TableSectionLine(cr, course, subpart, section, requires, getCourseUrl(session, course), plainText));
					}
					if (cr.isWaitlist(wlMode) && getStudent().canAssign(cr, wlMode) && enrollment.equals(cr.getWaitListSwapWithCourseOffering()) && !cr.isRequired(enrollment, offering)) {
						listOfClasses.add(new TableCourseLine(cr, course, getCourseUrl(session, course), true, null, plainText));
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
		public String getCourseTitle();
		public String getType();
		public String getName();
		public String getUrl();
		
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
		public boolean isWaitList();
		public boolean isCancelled();
		public boolean isPlainText();
	}
	
	public static class TableCourseLine implements TableLine {
		protected XCourseRequest iRequest;
		protected XCourse iCourse;
		protected Table iTable;
		protected String iUrl;
		protected boolean iWaitListEnabled;
		protected XCourse iSwapCourse;
		protected boolean iPlainText;
		
		public TableCourseLine(XCourseRequest request, XCourse course, URL url, boolean waitlistEnabled, XCourse swapCourse, boolean plainText) {
			iRequest = request;
			iCourse = course;
			iUrl = (url == null ? null : url.toString());
			iWaitListEnabled = waitlistEnabled;
			iSwapCourse = swapCourse;
			iPlainText = plainText;
		}
		
		public TableCourseLine(XCourseRequest request, XCourse course, URL url, boolean plainText) {
			this(request, course, url, false, null, plainText);
		}

		public XRequest getRequest() { return iRequest; }
		public XCourse getCourse() { return iCourse; }
		
		@Override
		public boolean isPlainText() { return iPlainText; }

		@Override
		public String getSubject() { return getCourse().getSubjectArea(); }

		@Override
		public String getCourseNumber() { return getCourse().getCourseNumber(); }
		
		@Override
		public String getCourseTitle() { return getCourse().getTitle(); }
		
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
		public boolean isWaitList() { return iRequest.isWaitlist() && iWaitListEnabled && iRequest.getWaitListedTimeStamp() != null; }

		@Override
		public String getNote() {
			if (iRequest.isWaitlist() && iWaitListEnabled && iRequest.getWaitListedTimeStamp() != null) {
				String note = MSG.conflictWaitListed(sTimeStampFormat.format(iRequest.getWaitListedTimeStamp()));
				if (iSwapCourse != null)
					note += " " + MSG.conflictWaitListSwapWithNoCourseOffering(iSwapCourse.getCourseName());
				List<XPreference> pref = iRequest.getPreferences(iCourse);
				if (pref != null) {
					Set<String> prefs = new TreeSet<String>();
					for (XPreference p: pref) {
						if (p.isRequired())
							prefs.add(p.getLabel());
					}
					if (!prefs.isEmpty())
						note += " " + MSG.conflictRequiredPreferences(StudentEmail.toString(prefs));
				}
				Integer status = iRequest.getOverrideStatus(getCourse());
				if (status == null) {
				} else if (status == org.unitime.timetable.model.CourseRequest.CourseRequestOverrideStatus.APPROVED.ordinal()) {
					note += " " + MSG.overrideApproved(iCourse.getCourseName());
				} else if (status == org.unitime.timetable.model.CourseRequest.CourseRequestOverrideStatus.REJECTED.ordinal()) {
					note += " " + MSG.overrideRejectedWaitList(iCourse.getCourseName());
				} else if (status == org.unitime.timetable.model.CourseRequest.CourseRequestOverrideStatus.CANCELLED.ordinal()) {
					note += " " + MSG.overrideCancelledWaitList(iCourse.getCourseName());
				} else if (status == org.unitime.timetable.model.CourseRequest.CourseRequestOverrideStatus.NOT_CHECKED.ordinal()) {
					note += " " + MSG.overrideNotRequested();
				} else if (status == org.unitime.timetable.model.CourseRequest.CourseRequestOverrideStatus.PENDING.ordinal()) {
					note += " " + MSG.overridePending(iCourse.getCourseName());
				} else if (status == org.unitime.timetable.model.CourseRequest.CourseRequestOverrideStatus.NOT_NEEDED.ordinal()) {
					note += " " + MSG.overrideNotNeeded(iCourse.getCourseName());
				}
				return note;
			}
			if (iRequest.isAlternative())
				return (iRequest.isWaitlist() && iWaitListEnabled ? MSG.emailWaitListedAlternativeRequest() : MSG.emailNotEnrolledAlternativeRequest());
			else
				return (iRequest.isWaitlist() && iWaitListEnabled ? MSG.emailWaitListedRequest() : MSG.emailNotEnrolledRequest());
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

		@Override
		public String getUrl() { return iUrl; }
		
		@Override
		public boolean isCancelled() { return false; }
	}
	
	public static class TableSectionLine extends TableCourseLine {
		protected XSection iSection;
		protected XSubpart iSubpart;
		protected String iRequires;
		
		public TableSectionLine(XCourseRequest request, XCourse course, XSubpart subpart, XSection section, String requires, URL url, boolean plainText) {
			super(request, course, url, plainText);
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
			if (iSection.isCancelled()) return MSG.classCancelled(getName());
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
					if (isPlainText()) {
						instructors += instructor.getName();
					} else if (instructor.getEmail() == null) {
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
		
		@Override
		public boolean isWaitList() { return false; }
		
		@Override
		public boolean isCancelled() { return iSection.isCancelled(); }
	}
	
	public static class TableLineFreeTime implements TableLine {
		private XFreeTimeRequest iRequest;
		
		public TableLineFreeTime(XFreeTimeRequest request) {
			iRequest = request;
		}

		public XRequest getRequest() { return iRequest; }
		
		@Override
		public boolean isPlainText() { return true; }
		
		@Override
		public String getSubject() { return MSG.freeTimeCourse(); }

		@Override
		public String getCourseNumber() { return MSG.freeTimeSubject(); }
		
		@Override
		public String getCourseTitle() { return null; }
		
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
		
		@Override
		public String getUrl() { return null; }
		
		@Override
		public boolean isWaitList() { return false; }
		
		@Override
		public boolean isCancelled() { return false; }
	}
	
	public static class TableSectionDeletedLine extends TableSectionLine {
		
		public TableSectionDeletedLine(XCourseRequest request, XCourse course, XSubpart subpart, XSection section, String requires, URL url, boolean plainText) {
			super(request, course, subpart, section, requires, url, plainText);
		}
		
		@Override
		public String getCourseNote() { return null; }
	}
	
	public static class TableSectionModifiedLine extends TableSectionLine {
		protected XSubpart iOldSubpart;
		protected XSection iOldSection;
		protected String iOldRequires;
		
		public TableSectionModifiedLine(XCourseRequest request, XCourse course, XSubpart oldSubpart, XSubpart subpart, XSection oldSection, XSection section, String oldRequires, String requires, URL url, boolean plainText) {
			super(request, course, subpart, section, requires, url, plainText);
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
			if (isPlainText()) {
				if (a == null || a.isEmpty())
					return (b == null || b.isEmpty() ? "" : b);
				if (b == null || b.isEmpty())
					return MSG.textDiff(MSG.textNotApplicable(), a);
				if (a.equals(b))
					return a;
				return MSG.textDiff(b, a);
			} else {
				if (a == null || a.isEmpty())
					return (b == null || b.isEmpty() ? "<span style='text-decoration: none;'>&nbsp;</span>" : b);
				if (b == null || b.isEmpty())
					return "<br><span style='font-style: italic; color: gray; text-decoration: line-through;'>" + a + "</span>";
				if (a.equals(b))
					return a;
				return b + "<br><span style='font-style: italic; color: gray; text-decoration: line-through;'>" + a + "</span>";
			}
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
					if (isPlainText()) {
						oldInstructors += instructor.getName();
					} else if (instructor.getEmail() == null) {
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
					if (isPlainText()) {
						instructors += instructor.getName();
					} else if (instructor.getEmail() == null) {
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
			if (iSection.isCancelled()) return MSG.classCancelled(getName());
			return diff(iOldSection.getNote(), iSection.getNote());
		}
		
		@Override
		public String getCourseNote() {
			return null;
		}
				
		@Override
		public String getArrangeHours() { 
			return diff(getOldTime() == null || getOldTime().getDays() == 0 ? MSG.emailArrangeHours() : DayCode.toString(getOldTime().getDays()) + " " + startTime(getOldTime()), MSG.emailArrangeHours());
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
	
	public static class Table extends ArrayList<TableLine> {
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
	
	public static String toString(Collection<?> items) {
		if (items == null || items.isEmpty()) return "";
		if (items.size() == 1) return items.iterator().next().toString();
		if (items.size() == 2) {
			Iterator<?> i = items.iterator();
			return GWT.itemSeparatorPair(i.next().toString(), i.next().toString());
		} else {
			Iterator<?> i = items.iterator();
			String list = i.next().toString();
			while (i.hasNext()) {
				String item = i.next().toString();
				if (i.hasNext())
					list = GWT.itemSeparatorMiddle(list, item);
				else
					list = GWT.itemSeparatorLast(list, item);
			}
			return list;
		}
	}
	
	public class CourseRequestsTable {
		List<CourseRequestLine> lines = new ArrayList<CourseRequestLine>();
		boolean hasPref = false, hasWarn = false, hasWait = false;
		String credit = "";
		boolean hasCritical = false;
		String criticalColumn = null;
		String criticalColumnDescription = null;
		
		void add(CourseRequestLine line) { lines.add(line); }
		
		public List<CourseRequestLine> getLines() { return lines; }
		public boolean getHasPref() { return hasPref; }
		public boolean getHasWarn() { return hasWarn; }
		public boolean getHasWait() { return hasWait; }
		public String getCredit() { return credit; }
		public boolean getHasCritical() { return hasCritical; }
		public String getCriticalColumn() { return criticalColumn; }
		public String getCriticalColumnDescription() { return criticalColumnDescription; }
	}
	
	public class CourseRequestLine {
		public String priority;
		public String courseName;
		public String courseTitle;
		public String credit;
		public String prefs;
		public String note;
		public String status;
		public String icon;
		public String iconText;
		public boolean waitlist = false;
		public boolean first = false;
		public boolean firstalt = false;
		public boolean last = false;
		public boolean critical = false;
		public int rows = 1;
		public String waitListDate = null;
		public URL url;
		public int idx = 0;
		String firstChoice = null;
		
		public String getPriority() { return priority; }
		public String getCourseName() { return courseName; }
		public String getCourseTitle() { return courseTitle; }
		public String getCredit() { return credit; }
		public String getPrefs() { return prefs; }
		public String getNote() { return note; }
		public String getStatus() { return status; }
		public String getIcon() { return icon; }
		public String getIconText() { return iconText; }
		public boolean isWaitlist() { return waitlist; }
		public String getWaitListDate() { return waitListDate; }
		public boolean isFirst() { return first; }
		public boolean isFirstalt() { return firstalt; }
		public boolean isLast() { return last; }
		public boolean isCritical() { return critical; }
		public int getRows() { return rows; }
		public URL getUrl() { return url; }
		public int getIdx() { return idx; }
		public String getFirstChoice() { return firstChoice; }
	}
}
