/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2011, UniTime LLC, and individual contributors
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
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

import javax.activation.DataSource;
import javax.imageio.ImageIO;

import org.unitime.commons.Email;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.CourseInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.model.XCourse;
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
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.Formats.Format;

import net.sf.cpsolver.ifs.util.ToolBox;

public class StudentEmail implements OnlineSectioningAction<Boolean> {
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private static StudentSectioningConstants CONST = Localization.create(StudentSectioningConstants.class);
	private static GwtMessages GWT = Localization.create(GwtMessages.class);

	private Date iTimeStamp = null;
	private static Format<Date> sTimeStampFormat = Formats.getDateFormat(Formats.Pattern.DATE_TIME_STAMP);
	private static Format<Date> sConsentApprovalDateFormat = Formats.getDateFormat(Formats.Pattern.DATE_REQUEST);
	private String iSubject = MSG.emailDeafultSubject(), iSubjectExt = null, iMessage = null, iCC = null;
	private static Hashtable<Long, String> sLastMessage = new Hashtable<Long, String>();
	private byte[] iTimetableImage = null;
	
	private Long iStudentId;
	private XOffering iOldOffering;
	private XEnrollment iOldEnrollment;
	private XStudent iOldStudent;
	private XStudent iStudent;
	
	public StudentEmail(Long studentId, XOffering oldOffering, XEnrollment oldEnrollment) {
		iStudentId = studentId;
		iOldOffering = oldOffering;
		iOldEnrollment = oldEnrollment;
	}
	
	public StudentEmail(Long studentId, XStudent oldStudent) {
		iStudentId = studentId;
		iOldStudent = oldStudent;
	}
	
	public StudentEmail(Long studentId) {
		iStudentId = studentId;
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
	public XStudent getOldStudent() { return iOldStudent; }
	public XStudent getStudent() { return iStudent; }
	public void setStudent(XStudent student) { iStudent = student; }

	@Override
	public Boolean execute(final OnlineSectioningServer server, final OnlineSectioningHelper helper) {
		Lock lock = server.lockStudent(getStudentId(), null, true);
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
			
			helper.beginTransaction();
			try {
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

							email.addRecipient(dbStudent.getEmail(), dbStudent.getName(DepartmentalInstructor.sNameFormatLastFirstMiddle));
							
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
											html.replace("<img src='cid:timetable.png' border='0' alt='Timetable Image'/>", buffer.toString()).getBytes("UTF-8"));
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
							
							server.update(student, false);
							
							ret = true;
						} else {
							helper.info("Email notification failed to generate for student " + student.getName() + ".");
						}
					} else {
						helper.info("Email notification is disabled for student " + student.getName() + ".");
					}
				} else {
					helper.info("Student " + student.getName() + " has no email address on file.");
				}
				helper.commitTransaction();
			} catch (Exception e) {
				helper.rollbackTransaction();
				throw e;
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
	
	private String generateMessage(org.unitime.timetable.model.Student student, OnlineSectioningServer server, OnlineSectioningHelper helper) {
		StringWriter buffer = new StringWriter();
		PrintWriter out = new PrintWriter(buffer);
		
		out.println("<html>");
		out.println("<head>");
		out.println("  <meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>");
		if (getEmailSubject() == null || getEmailSubject().isEmpty()) {
			out.println("	<title>" + MSG.emailDeafultTitle() + "</title>");
		} else {
			out.println("	<title>" + getEmailSubject().replace("%session%", server.getAcademicSession().toString()) + "</title>");
		}
		out.println("</head>");
		out.println("<body style=\"font-family: sans-serif, verdana, arial;\">");
		out.println("	<table style=\"border: 1px solid #9CB0CE; padding: 5px; margin-top: 10px; min-width: 800px;\" align=\"center\">");
		out.println("		<tr><td><table width=\"100%\">");
		out.println("			<tr>");
		out.println("				<td rowspan=\"2\"><img src=\"http://www.unitime.org/include/unitime.png\" border=\"0\" height=\"100px\"/></td>");
		if (getEmailSubject() == null || getEmailSubject().isEmpty()) {
			out.println("				<td colspan=\"2\" style=\"font-size: x-large; font-weight: bold; color: #333333; text-align: right; padding: 20px 30px 10px 10px;\">" + MSG.emailDeafultTitle() + "</td>");
		} else {
			out.println("				<td colspan=\"2\" style=\"font-size: x-large; font-weight: bold; color: #333333; text-align: right; padding: 20px 30px 10px 10px;\">" + getEmailSubject().replace("%session%", server.getAcademicSession().toString()) + "</td>");
		}
		out.println("			</tr>");
		out.println("			<tr>");
		out.println("				<td style=\"color: #333333; text-align: right; vertical-align: top; padding: 10px 5px 5px 5px;\">" + 
				student.getName(DepartmentalInstructor.sNameFormatLastFirstMiddle) + "</td>");
		out.println("				<td style=\"color: #333333; text-align: right; vertical-align: top; padding: 10px 5px 5px 5px;\">" + 
				server.getAcademicSession().getTerm() + " " + server.getAcademicSession().getYear() + " (" + server.getAcademicSession().getCampus() + ")</td>");
		out.println("			</tr>");
		out.println("		</table></td></tr>");
		if (getMessage() != null && !getMessage().isEmpty()) {
			out.println("		<tr><td " +
					"style=\"width: 100%; border-bottom: 1px solid #9CB0CE; padding-top: 5px; font-size: large; font-weight: bold; color: black; text-align: left;\">" +
					MSG.emailMessage() + "</td></tr>");
			out.println("		<tr><td>");
			out.println(getMessage().replace("\n", "<br>"));
			out.println("		</td></tr>");
		}
		generateChange(out, server, helper);
		out.println("		<tr><td " +
				"style=\"width: 100%; border-bottom: 1px solid #9CB0CE; padding-top: 5px; font-size: large; font-weight: bold; color: black; text-align: left;\">" +
				MSG.emailClassList() + "</td></tr>");
		out.println("		<tr><td>");
		
		generateListOfClasses(out, server, helper);
		
		out.println("		</td></tr>");
		
		if (getStudent().getRequests().isEmpty()) {

			out.println("		<tr><td " +
					"style=\"width: 100%; border-bottom: 1px solid #9CB0CE; padding-top: 5px; font-size: large; font-weight: bold; color: black; text-align: left;\">" +
					MSG.emailTimetable() + "</td></tr>");
			out.println("		<tr><td>");
			
			try {
				iTimetableImage = generateTimetableImage(server);
			} catch (Exception e) {
				helper.error("Unable to create PDF timetable: " + e.getMessage(), e);
				generateTimetable(out, server, helper);
			}
			if (iTimetableImage != null)
				out.println("<img src='cid:timetable.png' border='0' alt='Timetable Image'/>");
			
			out.println("		</td></tr>");
		}
		
		if (helper.getUser() != null && helper.getUser().getType() == OnlineSectioningLog.Entity.EntityType.MANAGER) {
			if (getOldEnrollment() == null && getOldStudent() == null)
				out.println("		<tr><td>" + MSG.emailSentBy(helper.getUser().getName()) + "</td></tr>");
			else
				out.println("		<tr><td>" + MSG.emailChangesMadeBy(helper.getUser().getName()) + "</td></tr>");
		}

		out.println("	</table>");
		out.println("	<table style=\"width: 800px; margin-top: -3px;\" align=\"center\">");
		out.println("		<tr>");
		out.println("			<td width=\"33%\" align=\"left\" style=\"font-size: 9pt; vertical-align: top; font-style: italic; color: #9CB0CE; white-space: nowrap;\">" +
				GWT.pageVersion(Constants.getVersion(), Constants.getReleaseDate()) + "</td>");
		out.println("			<td width=\"34%\" align=\"center\" style=\"font-size: 9pt; vertical-align: top; font-style: italic; color: #9CB0CE; white-space: nowrap;\">" +
				GWT.pageCopyright() + "</td>");
		out.println("			<td width=\"33%\" align=\"right\" style=\"font-size: 9pt; vertical-align: top; font-style: italic; color: #9CB0CE; white-space: nowrap;\">" +
				sTimeStampFormat.format(getTimeStamp()) + "</td>");
		out.println("		</tr>");
		out.println("	</table>");
		out.println("</body>");
		out.println("</html>");
		
		out.flush(); out.close();
		return buffer.getBuffer().toString();
	}
	
	private void generateListOfClassesHeader(PrintWriter out) {
		out.println("<table width=\"100%\">");
		out.println("<tr>");
		String style = "white-space: nowrap; font-weight: bold; padding-top: 5px;";
		out.println("	<td style=\"" + style + "\">" + MSG.colSubject() + "</td>");
		out.println("	<td style=\"" + style + "\">" + MSG.colCourse() + "</td>");
		out.println("	<td style=\"" + style + "\">" + MSG.colSubpart() + "</td>");
		out.println("	<td style=\"" + style + "\">" + MSG.colClass() + "</td>");
		out.println("	<td style=\"" + style + "\">" + MSG.colDays() + "</td>");
		out.println("	<td style=\"" + style + "\">" + MSG.colStart() + "</td>");
		out.println("	<td style=\"" + style + "\">" + MSG.colEnd() + "</td>");
		out.println("	<td style=\"" + style + "\">" + MSG.colDate() + "</td>");
		out.println("	<td style=\"" + style + "\">" + MSG.colRoom() + "</td>");
		out.println("	<td style=\"" + style + "\">" + MSG.colInstructor() + "</td>");
		out.println("	<td style=\"" + style + "\">" + MSG.colParent() + "</td>");
		out.println("	<td style=\"" + style + "\">" + MSG.colNote() + "</td>");
		out.println("	<td style=\"" + style + "\">" + MSG.colCredit() + "</td>");
		out.println("</tr>");
	}
	
	private void generateListOfClassesFooter(PrintWriter out, boolean link) {
		if (link) {
			String url = ApplicationProperties.getProperty("unitime.url");
			if (url != null) {
				out.println("	<tr><td colspan=\"12\" style=\"font-size: 9pt; font-style: italic; color: #9CB0CE; text-align: right; margin-top: -2px; white-space: nowrap;\">");
				out.println("		" + MSG.emailLinkToUniTime(url));
				out.println("	</td></tr>");
			}
		}
		out.println("</table>");
	}
	
	public static boolean equals(XSection a, XSection b) {
		return
			ToolBox.equals(a.getName(), b.getName()) &&
			ToolBox.equals(a.getTime(), b.getTime()) &&
			ToolBox.equals(a.getRooms(), b.getRooms()) && 
			ToolBox.equals(a.getInstructors(), b.getInstructors()) &&
			ToolBox.equals(a.getParentId(), b.getParentId());
	}
	
	private String time(int slot) {
        int h = slot / 12;
        int m = 5 * (slot % 12);
        if (CONST.useAmPm())
        	return (h > 12 ? h - 12 : h) + ":" + (m < 10 ? "0" : "") + m + (h == 24 ? "a" : h >= 12 ? "p" : "a");
        else
			return h + ":" + (m < 10 ? "0" : "") + m;
	}
	
	private String startTime(XTime time) {
		return time(time.getSlot());
	}
	
	private String endTime(XTime time) {
		return time(time.getSlot() + time.getLength() - time.getBreakTime());
	}

	private void generateListOfClassesLine(PrintWriter out, XOffering offering, XCourse course, XSection section, String style, String consent) {
		out.println("<tr>");
		out.println("	<td style= \"white-space: nowrap; " + style + "\">" + course.getSubjectArea() + "</td>");
		out.println("	<td style= \"white-space: nowrap; " + style + "\">" + course.getCourseNumber() + "</td>");
		out.println("	<td style= \"white-space: nowrap; " + style + "\">" + section.getSubpartName() + "</td>");
		out.println("	<td style= \"white-space: nowrap; " + style + "\">" + section.getName(course.getCourseId()) + "</td>");
		if (section.getTime() == null) {
			out.println("	<td style= \"white-space: nowrap; " + style + "\" colspan=\"4\">" + MSG.emailArrangeHours() + "</td>");
		} else {
			out.println("	<td style= \"white-space: nowrap; " + style + "\">" + DayCode.toString(section.getTime().getDays()) + "</td>");
			out.println("	<td style= \"white-space: nowrap; " + style + "\">" + startTime(section.getTime()) + "</td>");
			out.println("	<td style= \"white-space: nowrap; " + style + "\">" + endTime(section.getTime()) + "</td>");
			out.println("	<td style= \"white-space: nowrap; " + style + "\">" + section.getTime().getDatePatternName() + "</td>");
		}
		if (section.getRooms() == null || section.getRooms().isEmpty()) {
			out.println("	<td style= \"white-space: nowrap; " + style + "\">&nbsp;</td>");
		} else {
			String rooms = "";
			for (XRoom room: section.getRooms()) {
				if (!rooms.isEmpty()) rooms += ", ";
				rooms += room.getName();
			}
			out.println("	<td style= \"white-space: nowrap; " + style + "\">" + rooms + "</td>");
		}
		if (section.getInstructors().isEmpty()) {
			out.println("	<td style= \"white-space: nowrap; " + style + "\">&nbsp;</td>");
		} else {
			String html = "";
			for (XInstructor instructor: section.getInstructors()) {
				if (!html.isEmpty()) html += ", ";
				if (instructor.getEmail() == null) {
					html += instructor.getName();
				} else {
					html += "<a href='mailto:" + instructor.getEmail() + "' style=\"color: inherit; background-color : transparent; text-decoration: none;\">" + instructor.getName()+ "</a>";
				}
			}
			out.println("	<td style= \"" + style + "\">" + html + "</td>");
		}
		XSection parent = (section.getParentId() == null ? null : offering.getSection(section.getParentId()));
		out.println("	<td style= \"" + style + "\">" + (parent == null ? consent == null ? "&nbsp;" : consent : parent.getName(course.getCourseId())) + "</td>");
		String note = course.getNote();
		if (section.getNote() != null) note = (note == null || note.isEmpty() ? "" : note + "<br>") + section.getNote();
		out.println("	<td style= \"" + style + "\">" + (note == null ? "&nbsp;" : note.replace("\n", "<br>")) + "</td>");
		XSubpart subpart = offering.getSubpart(section.getSubpartId());
		out.println("	<td style= \"" + style + "\" title= \"" +  creditText(subpart) + "\">" + creditAbbv(subpart) + "</td>");
		out.println("</tr>");
	}
	
	private String diff(String a, String b) {
		if (a == null || a.isEmpty())
			return (b == null || b.isEmpty() ? "<span style='text-decoration: none;'>&nbsp;</span>" : b);
		if (b == null || b.isEmpty())
			return "<span style='text-decoration: line-through;'>" + a + "</span>";
		if (a.equals(b))
			return a;
		return "<span style='text-decoration: line-through;'>" + a + "</span> &rarr; " + b;
	}
	
	private void generateListOfClassesDiff(PrintWriter out, XOffering oldOffering, XOffering offering, XCourse course, XSection old, XSection section, String style, String consent) {
		out.println("<tr>");
		out.println("	<td style= \"white-space: nowrap; " + style + "\">" + course.getSubjectArea() + "</td>");
		out.println("	<td style= \"white-space: nowrap; " + style + "\">" + course.getCourseNumber() + "</td>");
		out.println("	<td style= \"white-space: nowrap; " + style + "\">" + section.getSubpartName() + "</td>");
		out.println("	<td style= \"white-space: nowrap; " + style + "\">" + diff(old.getName(course.getCourseId()), section.getName(course.getCourseId())) + "</td>");
		if (section.getTime() == null) {
			out.println("	<td style= \"white-space: nowrap; " + style + "\" colspan=\"4\">" +
					diff(old.getTime() == null ? MSG.emailArrangeHours() : DayCode.toString(old.getTime().getDays()) + " " + startTime(old.getTime()),
					MSG.emailArrangeHours()) + "</td>");
		} else {
			out.println("	<td style= \"white-space: nowrap; " + style + "\">" + 
					diff(old.getTime() == null ? null : DayCode.toString(old.getTime().getDays()), DayCode.toString(section.getTime().getDays())) +
					"</td>");
			out.println("	<td style= \"white-space: nowrap; " + style + "\">" + 
					diff(old.getTime() == null ? null : startTime(old.getTime()), startTime(section.getTime())) +
					"</td>");
			out.println("	<td style= \"white-space: nowrap; " + style + "\">" + 
					diff(old.getTime() == null ? null : endTime(old.getTime()), endTime(section.getTime())) +
					"</td>");
			out.println("	<td style= \"white-space: nowrap; " + style + "\">" +
					diff(old.getTime() == null ? null : old.getTime().getDatePatternName(), section.getTime().getDatePatternName()) +
					"</td>");
		}
	
		String oldRooms = "";
		if (old.getRooms() != null && !old.getRooms().isEmpty()) {
			for (XRoom room: old.getRooms()) {
				if (!oldRooms.isEmpty()) oldRooms += ", ";
				oldRooms += room.getName();
			}
		}
		
		String rooms = "";
		if (section.getRooms() != null && !section.getRooms().isEmpty()) {
			for (XRoom room: section.getRooms()) {
				if (!rooms.isEmpty()) rooms += ", ";
				rooms += room.getName();
			}
		}
		out.println("	<td style= \"white-space: nowrap; " + style + "\">" + diff(oldRooms, rooms) + "</td>");
		
		String oldInstructors = "";
		if (!old.getInstructors().isEmpty()) {
			for (XInstructor instructor: old.getInstructors()) {
				if (!oldInstructors.isEmpty()) oldInstructors += ", ";
				if (instructor.getEmail() == null) {
					oldInstructors += instructor.getName();
				} else {
					oldInstructors += "<a href='mailto:" + instructor.getEmail() + "' style=\"color: inherit; background-color : transparent; text-decoration: none;\">" + instructor.getName()+ "</a>";
				}
			}
		}
		String instructors = "";
		if (!section.getInstructors().isEmpty()) {
			for (XInstructor instructor: section.getInstructors()) {
				if (!instructors.isEmpty()) instructors += ", ";
				if (instructor.getEmail() == null) {
					instructors += instructor.getName();
				} else {
					instructors += "<a href='mailto:" + instructor.getEmail() + "' style=\"color: inherit; background-color : transparent; text-decoration: none;\">" + instructor.getName()+ "</a>";
				}
			}
		}
		out.println("	<td style= \"white-space: nowrap; " + style + "\">" + diff(oldInstructors, instructors) + "</td>");

		XSection oldParent = (old.getParentId() == null ? null : oldOffering.getSection(old.getParentId()));
		XSection parent = (section.getParentId() == null ? null : offering.getSection(section.getParentId()));
		out.println("	<td style= \"" + style + "\">" + (oldParent == null && parent == null ? consent == null ? "&nbsp;" : consent : diff(oldParent == null ? null : oldParent.getName(course.getCourseId()), parent == null ? null : parent.getName(course.getCourseId()))) + "</td>");
		out.println("	<td style= \"" + style + "\">" +
				(course.getNote() == null ? "" : course.getNote().replace("\n", "<br>") + "<br>") +
				diff(old.getNote(), section.getNote()).replace("\n", "<br>") + "</td>");
		XSubpart oldSubpart = oldOffering.getSubpart(old.getSectionId());
		XSubpart subpart = offering.getSubpart(section.getSubpartId());
		out.println("	<td style= \"" + style + "\" title= \"" +  creditText(subpart) + "\">" + diff(creditAbbv(oldSubpart), creditAbbv(subpart)) + "</td>");
		out.println("</tr>");
	}
	
	private String creditAbbv(XSubpart subpart) {
		String credit = (subpart == null ? null : subpart.getCredit());
		return credit == null ? "" : credit.indexOf('|') < 0 ? credit : credit.substring(0, credit.indexOf('|')); 
	}

	private String creditText(XSubpart subpart) {
		String credit = (subpart == null ? null : subpart.getCredit());
		return credit == null ? "" : credit.indexOf('|') < 0 ? credit : credit.substring(1 + credit.indexOf('|')); 
	}

	private void generateListOfClassesLine(PrintWriter out, XRequest request, String consent, OnlineSectioningServer server) {
		XEnrollment enrollment = (request instanceof XCourseRequest ? ((XCourseRequest)request).getEnrollment() : null);
		if (enrollment == null) {
			if (request instanceof XCourseRequest) {
				XCourseRequest cr = (XCourseRequest)request;
				if (!getStudent().canAssign(cr)) return;
				CourseInfo course = server.getCourseInfo(cr.getCourseIds().get(0).getCourseId());
				out.println("<tr>");
				String style = "color: red; border-top: 1px dashed #9CB0CE;";
				out.println("	<td style= \"white-space: nowrap; " + style + "\">" + course.getSubjectArea() + "</td>");
				out.println("	<td style= \"white-space: nowrap; " + style + "\">" + course.getCourseNbr() + "</td>");
				out.println("	<td style= \"white-space: nowrap; " + style + "\">&nbsp;</td>");
				out.println("	<td style= \"white-space: nowrap; " + style + "\">&nbsp;</td>");
				if (request.isAlternative())
					out.println("	<td style= \"white-space: nowrap; " + style + "\" colspan=\"9\" align=\"center\">" + (cr.isWaitlist() ? MSG.emailWaitListedAlternativeRequest() : MSG.emailNotEnrolledAlternativeRequest()) + "</td>");
				else
					out.println("	<td style= \"white-space: nowrap; " + style + "\" colspan=\"9\" align=\"center\">" + (cr.isWaitlist() ? MSG.emailWaitListedRequest() : MSG.emailNotEnrolledRequest()) + "</td>");
				out.println("</tr>");
			}
			return;
		} else {
			XOffering offering = server.getOffering(enrollment.getOfferingId());
			XCourse course = offering.getCourse(enrollment.getCourseId());
			boolean first = true, firstNoParent = true;
			for (XSection section: offering.getSections(enrollment)) {
				String style = (first ? " border-top: 1px dashed #9CB0CE;" : "");
				generateListOfClassesLine(out, offering, course, section, style, firstNoParent ? consent : null);
				first = false;
				if (section.getParentId() == null) firstNoParent = false;
			}
		}
		if (request instanceof XFreeTimeRequest) {
			XFreeTimeRequest fr = (XFreeTimeRequest)request;
			String style = "border-top: 1px dashed #9CB0CE;";
			out.println("<tr>");
			out.println("	<td style= \"white-space: nowrap; " + style + "\">" + MSG.freeTimeSubject() + "</td>");
			out.println("	<td style= \"white-space: nowrap; " + style + "\">" + MSG.freeTimeCourse() + "</td>");
			out.println("	<td style= \"white-space: nowrap; " + style + "\">&nbsp;</td>");
			out.println("	<td style= \"white-space: nowrap; " + style + "\">&nbsp;</td>");
			out.println("	<td style= \"white-space: nowrap; " + style + "\">" + DayCode.toString(fr.getTime().getDays()) + "</td>");
			out.println("	<td style= \"white-space: nowrap; " + style + "\">" + startTime(fr.getTime()) + "</td>");
			out.println("	<td style= \"white-space: nowrap; " + style + "\">" + endTime(fr.getTime()) + "</td>");
			out.println("	<td style= \"white-space: nowrap; " + style + "\" colspan=\"6\">&nbsp;</td>");
			out.println("</tr>");
			return;
		}
	}

	
	protected void generateListOfClasses(PrintWriter out, OnlineSectioningServer server, OnlineSectioningHelper helper) {
		if (getStudent().getRequests().isEmpty()) {
			out.println("<table width=\"100%\"><tr><td class=\"unitime-ErrorMessage\">" + MSG.emailNoSchedule() + "</td></tr></table>");
			return;
		}
		generateListOfClassesHeader(out);
		for (XRequest request: getStudent().getRequests()) {
			XEnrollment enrollment = (request instanceof XCourseRequest ? ((XCourseRequest)request).getEnrollment() : null);
			generateListOfClassesLine(out, request, consent(server, enrollment), server);
		}
		generateListOfClassesFooter(out, true);
	}
	
	private String consent(OnlineSectioningServer server, XEnrollment enrollment) {
		if (enrollment == null || enrollment.getCourseId() == null) return null;
		CourseInfo info = server.getCourseInfo(enrollment.getCourseId());
		if (info == null || info.getConsent() == null) return null;
		if (enrollment.getApproval() == null)
			return MSG.consentWaiting(info.getConsent().toLowerCase());
		else 
			return MSG.consentApproved(sConsentApprovalDateFormat.format(enrollment.getApproval().getTimeStamp()));
	}
	
	public void generateChange(PrintWriter out, OnlineSectioningServer server, OnlineSectioningHelper helper) {
		if (getOldOffering() != null) {
			XCourseRequest newRequest = null;
			XOffering newOffering = null;
			for (XRequest r: getStudent().getRequests()) {
				if (r instanceof XCourseRequest && ((XCourseRequest)r).getCourseIdByOfferingId(getOldOffering().getOfferingId()) != null) {
					newRequest = (XCourseRequest)r;
					newOffering = server.getOffering(getOldOffering().getOfferingId());
					break;
				}
			}
			if (getOldEnrollment() == null && newRequest != null && newRequest.getEnrollment() != null) {
				XCourse course = newOffering.getCourse(newRequest.getEnrollment().getCourseId());
				setSubject(MSG.emailEnrollmentNew(course.getSubjectArea(), course.getCourseNumber()));
				out.println("		<tr><td " +
						"style=\"width: 100%; border-bottom: 1px solid #9CB0CE; padding-top: 5px; font-size: large; font-weight: bold; color: black; text-align: left;\">" +
						MSG.emailCourseEnrollment(course.getSubjectArea(), course.getCourseNumber()) + "</td></tr>");
				out.println("		<tr><td>");
				generateListOfClassesHeader(out);
				generateListOfClassesLine(out, newRequest, consent(server, newRequest.getEnrollment()), server);
				generateListOfClassesFooter(out, false);
				out.println("		</td></tr>");
			} else if (getOldEnrollment() != null && newRequest != null && newRequest.getEnrollment() != null) {
				XCourse course = newOffering.getCourse(newRequest.getEnrollment().getCourseId());
				setSubject(MSG.emailEnrollmentChanged(course.getSubjectArea(), course.getCourseNumber()));
				out.println("		<tr><td " +
						"style=\"width: 100%; border-bottom: 1px solid #9CB0CE; padding-top: 5px; font-size: large; font-weight: bold; color: black; text-align: left;\">" +
						MSG.emailCourseEnrollment(course.getSubjectArea(), course.getCourseNumber()) + "</td></tr>");
				out.println("		<tr><td>");
				generateListOfClassesHeader(out);
				boolean first = true, firstWithNoParent = true;
				sections: for (XSection section: newOffering.getSections(newRequest.getEnrollment())) {
					for (XSection old: getOldOffering().getSections(getOldEnrollment())) {
						if (old.getSubpartId().equals(section.getSubpartId())) {
							String style = (first ? " border-top: 1px dashed #9CB0CE;" : "");
							generateListOfClassesDiff(out, getOldOffering(), newOffering, course, old, section, style, firstWithNoParent ? consent(server, newRequest.getEnrollment()) : null);
							first = false;
							if (section.getParentId() == null && old.getParentId() == null) firstWithNoParent = false;
							continue sections;
						}
					}
					String style = (first ? " border-top: 1px dashed #9CB0CE;" : "");
					generateListOfClassesLine(out, newOffering, course, section, style, firstWithNoParent ? consent(server, newRequest.getEnrollment()) : null);
					first = false;
					if (section.getParentId() == null) firstWithNoParent = false;
				}
				sections: for (XSection old: getOldOffering().getSections(getOldEnrollment())) {
					for (XSection section: newOffering.getSections(newRequest.getEnrollment()))
						if (old.getSubpartId().equals(section.getSubpartId())) continue sections;
					String style = "text-decoration: line-through;" + (first ? " border-top: 1px dashed #9CB0CE;" : "");
					generateListOfClassesLine(out, getOldOffering(), course, old, style, null);
					first = false;
				}
				generateListOfClassesFooter(out, false);
				out.println("		</td></tr>");
			} else if (getOldEnrollment() != null && (newRequest == null || newRequest.getEnrollment() == null)) {
				XCourse course = getOldOffering().getCourse(getOldEnrollment().getCourseId());
				setSubject(newRequest == null
						? MSG.emailCourseDropReject(course.getSubjectArea(), course.getCourseNumber())
						: MSG.emailCourseDropChange(course.getSubjectArea(), course.getCourseNumber()));
				out.println("		<tr><td " +
						"style=\"width: 100%; border-bottom: 1px solid #9CB0CE; padding-top: 5px; font-size: large; font-weight: bold; color: black; text-align: left;\">" +
						MSG.emailCourseEnrollment(course.getSubjectArea(), course.getCourseNumber()) + "</td></tr>");
				out.println("		<tr><td>");
				CourseInfo info = server.getCourseInfo(course.getCourseId());
				String consent = (info == null ? null : info.getConsent());
				if (newRequest !=  null && getStudent().canAssign(newRequest))
					out.println("<table width=\"100%\"><tr><td class=\"unitime-ErrorMessage\">" + 
							(newRequest.isAlternative() ?
									newRequest.isWaitlist() ? MSG.emailCourseWaitListedAlternative() : MSG.emailCourseNotEnrolledAlternative() :
									newRequest.isWaitlist() ? MSG.emailCourseWaitListed() : MSG.emailCourseNotEnrolled()) + "</td></tr></table>");
				else if (newRequest == null && consent != null) {
					out.println("<table width=\"100%\"><tr><td class=\"unitime-ErrorMessage\">" + MSG.emailConsentRejected(consent.toLowerCase()) + "</td></tr></table>");
				}
				out.println("		</td></tr>");
			}
		} else if (getOldStudent() != null && !getOldStudent().getRequests().isEmpty()) {
			boolean somethingWasAssigned = false;
			for (XRequest or: getOldStudent().getRequests()) {
				if (or instanceof XCourseRequest && ((XCourseRequest)or).getEnrollment() != null) {
					somethingWasAssigned = true; break;
				}
			}
			if (somethingWasAssigned) {
				out.println("		<tr><td " +
						"style=\"width: 100%; border-bottom: 1px solid #9CB0CE; padding-top: 5px; font-size: large; font-weight: bold; color: black; text-align: left;\">" +
						MSG.emailEnrollmentChanges() + "</td></tr>");
				out.println("		<tr><td>");
				int nrLines = 0;
				generateListOfClassesHeader(out);
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
								boolean first = true, firstWithNoParent = true;
								XOffering no = server.getOffering(ncr.getEnrollment().getOfferingId());
								for (XSection section: no.getSections(ncr.getEnrollment())) {
									String style = (first ? " border-top: 1px dashed #9CB0CE;" : "");
									generateListOfClassesLine(out, no, no.getCourse(ncr.getEnrollment().getCourseId()), section, style, firstWithNoParent ? consent(server, ncr.getEnrollment()) : null);
									nrLines++;
									first = false;
									if (section.getParentId() == null) firstWithNoParent = false;
								}
							} else if (ncr.getEnrollment() == null) {
								XOffering oo = server.getOffering(ocr.getEnrollment().getOfferingId());
								// was un-assigned
								boolean first = true;
								for (XSection section: oo.getSections(ocr.getEnrollment())) {
									String style = "text-decoration: line-through;" + (first ? " border-top: 1px dashed #9CB0CE;" : "");
									generateListOfClassesLine(out, oo, oo.getCourse(ocr.getEnrollment().getCourseId()), section, style, null);
									nrLines++;
									first = false;
								}
							} else {
								XOffering no = server.getOffering(ncr.getEnrollment().getOfferingId());
								XOffering oo = server.getOffering(ocr.getEnrollment().getOfferingId());
								// both assigned
								boolean first = true, firstWithNoParent = true;
								sections: for (XSection section: no.getSections(ncr.getEnrollment())) {
									for (XSection old: oo.getSections(ocr.getEnrollment())) {
										if (old.getSubpartId().equals(section.getSubpartId())) {
											if (equals(section, old)) continue sections;
											String style = (first ? " border-top: 1px dashed #9CB0CE;" : "");
											generateListOfClassesDiff(out, oo, no, no.getCourse(ncr.getEnrollment().getCourseId()), old, section, style, firstWithNoParent ? consent(server, ncr.getEnrollment()) : null);
											nrLines++;
											first = false;
											if (section.getParentId() == null && old.getParentId() == null) firstWithNoParent = false;
											continue sections;
										}
									}
									String style = (first ? " border-top: 1px dashed #9CB0CE;" : "");
									generateListOfClassesLine(out, no, no.getCourse(ncr.getEnrollment().getCourseId()), section, style, firstWithNoParent ? consent(server, ncr.getEnrollment()) : null);
									nrLines++;
									first = false;
									if (section.getParentId() == null) firstWithNoParent = false;
								}
								sections: for (XSection old: oo.getSections(ocr.getEnrollment())) {
									for (XSection section: no.getSections(ncr.getEnrollment()))
										if (old.getSubpartId().equals(section.getSubpartId())) continue sections;
									String style = "text-decoration: line-through;" + (first ? " border-top: 1px dashed #9CB0CE;" : "");
									generateListOfClassesLine(out, oo, oo.getCourse(ocr.getEnrollment().getCourseId()), old, style, null);
									nrLines++;
									first = false;
								}
							}
							continue requests;
						}
					}
					// old request not found
					if (ncr.getEnrollment() != null) {
						XOffering no = server.getOffering(ncr.getEnrollment().getOfferingId());
						boolean first = true, firstWithNoParent = true;
						for (XSection section: no.getSections(ncr.getEnrollment())) {
							String style = (first ? " border-top: 1px dashed #9CB0CE;" : "");
							generateListOfClassesLine(out, no, no.getCourse(ncr.getEnrollment().getCourseId()), section, style, firstWithNoParent ? consent(server, ncr.getEnrollment()) : null);
							nrLines++;
							first = false;
							if (section.getParentId() == null) firstWithNoParent = false;
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
					boolean first = true, firstWithNoParent = true;
					XOffering oo = server.getOffering(((XCourseRequest)or).getEnrollment().getOfferingId());
					for (XSection section: oo.getSections(((XCourseRequest)or).getEnrollment())) {
						String style = "text-decoration: line-through; " + (first ? " border-top: 1px dashed #9CB0CE;" : "");
						generateListOfClassesLine(out, oo, oo.getCourse(((XCourseRequest)or).getEnrollment().getCourseId()), section, style, firstWithNoParent ? consent(server, ((XCourseRequest)or).getEnrollment()) : null);
						nrLines++;
						first = false;
						if (section.getParentId() == null) firstWithNoParent = false;
					}
				}
				if (nrLines == 0) {
					if (getMessage() == null || getMessage().isEmpty())
						out.println("<tr><td colspan='11'><i>" + MSG.emailNoChange() + "</i></td></tr>");
				}
				generateListOfClassesFooter(out, false);
				out.println("		</td></tr>");
			} else {
				setSubject(MSG.emailSubjectNotification());
			}
		} else {
			setSubject(MSG.emailSubjectNotification());
		}
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
	
}
