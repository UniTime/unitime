package org.unitime.timetable.onlinesectioning.updates;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.activation.DataSource;

import org.unitime.commons.Email;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.SectioningExceptionType;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.util.Constants;

import net.sf.cpsolver.coursett.model.RoomLocation;
import net.sf.cpsolver.studentsct.model.Assignment;
import net.sf.cpsolver.studentsct.model.Course;
import net.sf.cpsolver.studentsct.model.CourseRequest;
import net.sf.cpsolver.studentsct.model.FreeTimeRequest;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;
import net.sf.cpsolver.studentsct.model.Student;

public class StudentEmail implements OnlineSectioningAction<Boolean> {
	private Long iStudentId = null;
	private List<Request> iOldRequests = null, iNewRequests = null;
	private Date iTimeStamp = null;
	private static SimpleDateFormat sDateFormat = new SimpleDateFormat("MMMM dd, yyyy hh:mm:ss aa");
	
	public StudentEmail(Long studentId, List<Request> oldRequests, List<Request> newRequests) {
		iStudentId = studentId; iOldRequests = oldRequests; iNewRequests = newRequests;
		iTimeStamp = new Date();
	}
	
	public Long getStudentId() { return iStudentId; }
	public List<Request> getOldRequests() { return iOldRequests; }
	public List<Request> getNewRequests() { return iNewRequests; }
	public Date getTimeStamp() { return iTimeStamp; }

	@Override
	public Boolean execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		Lock lock = server.lockStudent(getStudentId(), null, true);
		try {
			Student student = server.getStudent(getStudentId());
			if (student == null) return false;
			
			helper.beginTransaction();
			try {
				org.unitime.timetable.model.Student dbStudent = StudentDAO.getInstance().get(getStudentId());
				if (dbStudent != null && dbStudent.getEmail() != null && !dbStudent.getEmail().isEmpty()) {
					Email email = new Email();

					email.addRecipient(dbStudent.getEmail(), dbStudent.getName(DepartmentalInstructor.sNameFormatLastFirstMiddle));
					
					if (getOldRequests() == null)
						email.setSubject("Class schedule notification for " + server.getAcademicSession().toString());
					else
						email.setSubject("Class schedule change for " + server.getAcademicSession().toString());
					final String html = generateMessage(dbStudent, server, helper);

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
							return new ByteArrayInputStream(html.getBytes());
						}
						
						@Override
						public String getContentType() {
							return "text/html";
						}
					});
					
					email.setHTML(html);
					email.send();
				}
				helper.commitTransaction();
			} catch (Exception e) {
				helper.rollbackTransaction();
				throw e;
			}
			
			return true;
		} catch (Exception e) {
			if (e instanceof SectioningException)
				throw (SectioningException)e;
			throw new SectioningException(SectioningExceptionType.UNKNOWN, e);
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
		out.println("	<title>Class Schedule</title>");
		out.println("</head>");
		out.println("<body style=\"font-family: sans-serif, verdana, arial;\">");
		out.println("	<table style=\"border: 1px solid #9CB0CE; padding: 5px; margin-top: 10px; width: 800px;\" align=\"center\">");
		out.println("		<tr><td><table width=\"100%\">");
		out.println("			<tr>");
		out.println("				<td rowspan=\"2\"><img src=\"http://www.unitime.org/include/unitime.png\" border=\"0\" height=\"100px\"/></td>");
		out.println("				<td colspan=\"2\" style=\"font-size: x-large; font-weight: bold; color: #333333; text-align: right; padding: 20px 30px 10px 10px;\">Class Schedule</td>");
		out.println("			</tr>");
		out.println("			<tr>");
		out.println("				<td style=\"color: #333333; text-align: right; vertical-align: top; padding: 10px 5px 5px 5px;\">" + 
				student.getName(DepartmentalInstructor.sNameFormatLastFirstMiddle) + "</td>");
		out.println("				<td style=\"color: #333333; text-align: right; vertical-align: top; padding: 10px 5px 5px 5px;\">" + 
				server.getAcademicSession().getTerm() + " " + server.getAcademicSession().getYear() + " (" + server.getAcademicSession().getCampus() + ")</td>");
		out.println("			</tr>");
		out.println("		</table></td></tr>");
		out.println("		<tr><td " +
				"style=\"width: 100%; border-bottom: 1px solid #9CB0CE; padding-top: 5px; font-size: large; font-weight: bold; color: black; text-align: left;\">" +
				"List of Classes</td></tr>");
		out.println("		<tr><td>");
		
		generateListOfClasses(out, server, helper);
		
		out.println("		</td></tr>");
		
		if (getNewRequests() != null && !getNewRequests().isEmpty()) {

			out.println("		<tr><td " +
					"style=\"width: 100%; border-bottom: 1px solid #9CB0CE; padding-top: 5px; font-size: large; font-weight: bold; color: black; text-align: left;\">" +
					"Timetable</td></tr>");
			out.println("		<tr><td>");
			
			generateTimetable(out, server, helper);
			
			out.println("		</td></tr>");
		}

		out.println("	</table>");
		out.println("	<table style=\"width: 800px; margin-top: -3px;\" align=\"center\">");
		out.println("		<tr>");
		out.println("			<td width=\"33%\" align=\"left\" style=\"font-size: 9pt; vertical-align: top; font-style: italic; color: #9CB0CE; white-space: nowrap;\">" +
				"Version " + Constants.VERSION + "." + Constants.BLD_NUMBER.replaceAll("@build.number@","?") + " built on " + Constants.REL_DATE.replaceAll("@build.date@", "?") + "</td>");
		out.println("			<td width=\"34%\" align=\"center\" style=\"font-size: 9pt; vertical-align: top; font-style: italic; color: #9CB0CE; white-space: nowrap;\">" +
				"&copy; 2008 - 2011 UniTIme LLC,<br>distributed under GNU General Public License.</td>");
		out.println("			<td width=\"33%\" align=\"right\" style=\"font-size: 9pt; vertical-align: top; font-style: italic; color: #9CB0CE; white-space: nowrap;\">" +
				sDateFormat.format(getTimeStamp()) + "</td>");
		out.println("		</tr>");
		out.println("	</table>");
		out.println("</body>");
		out.println("</html>");
		
		out.flush(); out.close();
		return buffer.getBuffer().toString();
	}
	
	protected void generateListOfClasses(PrintWriter out, OnlineSectioningServer server, OnlineSectioningHelper helper) {
		if (getNewRequests() == null && getNewRequests().isEmpty()) {
			out.println("<table width=\"100%\"><tr><td class=\"unitime-ErrorMessage\">No class schedule.</td></tr></table>");
			return;
		}
		out.println("<table width=\"100%\">");
		out.println("<tr>");
		String style = "font-weight: bold; padding-top: 5px;";
		out.println("	<td style=\"" + style + "\">Subject</td>");
		out.println("	<td style=\"" + style + "\">Course</td>");
		out.println("	<td style=\"" + style + "\">Type</td>");
		out.println("	<td style=\"" + style + "\">Section</td>");
		out.println("	<td style=\"" + style + "\">Days</td>");
		out.println("	<td style=\"" + style + "\">Start</td>");
		out.println("	<td style=\"" + style + "\">End</td>");
		out.println("	<td style=\"" + style + "\">Date</td>");
		out.println("	<td style=\"" + style + "\">Room</td>");
		out.println("	<td style=\"" + style + "\">Instructor</td>");
		out.println("	<td style=\"" + style + "\">Requires</td>");
		out.println("</tr>");
		for (Request request: getNewRequests()) {
			if (request.getAssignment() == null) {
				if (request instanceof CourseRequest) {
					CourseRequest cr = (CourseRequest)request;
					if (!cr.getStudent().canAssign(cr)) continue;
					Course course = cr.getCourses().get(0);
					out.println("<tr>");
					style = "color: red; white-space: nowrap; border-top: 1px dashed #9CB0CE;";
					out.println("	<td style= \"" + style + "\">" + course.getSubjectArea() + "</td>");
					out.println("	<td style= \"" + style + "\">" + course.getCourseNumber() + "</td>");
					out.println("	<td style= \"" + style + "\">&nbsp;</td>");
					out.println("	<td style= \"" + style + "\">&nbsp;</td>");
					out.println("	<td style= \"" + style + "\" colspan=\"7\" align=\"center\">wait-listed</td>");
					out.println("</tr>");
				}
				continue;
			}
			if (request instanceof FreeTimeRequest) {
				FreeTimeRequest fr = (FreeTimeRequest)request;
				style = "white-space: nowrap; border-top: 1px dashed #9CB0CE;";
				out.println("<tr>");
				out.println("	<td style= \"" + style + "\">Free</td>");
				out.println("	<td style= \"" + style + "\">Time</td>");
				out.println("	<td style= \"" + style + "\">&nbsp;</td>");
				out.println("	<td style= \"" + style + "\">&nbsp;</td>");
				out.println("	<td style= \"" + style + "\">" + DayCode.toString(fr.getTime().getDayCode()) + "</td>");
				out.println("	<td style= \"" + style + "\">" + fr.getTime().getStartTimeHeader() + "</td>");
				out.println("	<td style= \"" + style + "\">" + fr.getTime().getEndTimeHeader() + "</td>");
				out.println("	<td style= \"" + style + "\" colspan=\"4\">&nbsp;</td>");
				out.println("</tr>");
				continue;
			}
			boolean first = true;
			for (Section section: request.getAssignment().getSections()) {
				style = "white-space: nowrap;" + (first ? " border-top: 1px dashed #9CB0CE;" : "");
				out.println("<tr>");
				out.println("	<td style= \"" + style + "\">" + request.getAssignment().getCourse().getSubjectArea() + "</td>");
				out.println("	<td style= \"" + style + "\">" + request.getAssignment().getCourse().getCourseNumber() + "</td>");
				out.println("	<td style= \"" + style + "\">" + section.getSubpart().getName() + "</td>");
				out.println("	<td style= \"" + style + "\">" + section.getName() + "</td>");
				if (section.getTime() == null) {
					out.println("	<td style= \"" + style + "\" colspan=\"4\">Arrange Hours</td>");
				} else {
					out.println("	<td style= \"" + style + "\">" + DayCode.toString(section.getTime().getDayCode()) + "</td>");
					out.println("	<td style= \"" + style + "\">" + section.getTime().getStartTimeHeader() + "</td>");
					out.println("	<td style= \"" + style + "\">" + section.getTime().getEndTimeHeader() + "</td>");
					out.println("	<td style= \"" + style + "\">" + section.getTime().getDatePatternName() + "</td>");
				}
				if (section.getRooms() == null || section.getRooms().isEmpty()) {
					out.println("	<td style= \"" + style + "\">&nbsp;</td>");
				} else {
					String rooms = "";
					for (RoomLocation room: section.getRooms()) {
						if (!rooms.isEmpty()) rooms += ", ";
						rooms += room.getName();
					}
					out.println("	<td style= \"" + style + "\">" + rooms + "</td>");
				}
				if (section.getChoice().getInstructorNames() == null|| section.getChoice().getInstructorNames().isEmpty()) {
					out.println("	<td style= \"" + style + "\">&nbsp;</td>");
				} else {
					String[] instructors = section.getChoice().getInstructorNames().split(":");
					String html = "";
					for (String instructor: instructors) {
						String[] nameEmail = instructor.split("\\|");
						if (!html.isEmpty()) html += ", ";
						if (nameEmail.length < 2) {
							html += nameEmail[0];
						} else {
							html += "<a href='mailto:" + nameEmail[1] + "' style=\"color: inherit; background-color : transparent; text-decoration: none;\">" + nameEmail[0]+ "</a>";
						}
					}
					out.println("	<td style= \"" + style + "\">" + html + "</td>");
				}
				out.println("	<td style= \"" + style + "\">" + (section.getParent() == null ? "&nbsp;" : section.getParent().getName()) + "</td>");
				out.println("</tr>");
				first = false;
			}
			first = true;
		}
		String url = ApplicationProperties.getProperty("unitime.url");
		if (url != null) {
			out.println("	<tr><td colspan=\"11\" style=\"font-size: 9pt; font-style: italic; color: #9CB0CE; text-align: right; margin-top: -2px; white-space: nowrap;\">");
			out.println("		For an up to date schedule, please visit " +
					"<a href='" + url + "/gwt.jsp?page=sectioning' style=\"color: inherit; background-color : transparent;\">" + url+ "</a>.");
			out.println("	</td></tr>");
		}
		out.println("</table>");
	}
	
	public void generateTimetable(PrintWriter out, OnlineSectioningServer server, OnlineSectioningHelper helper) {
		int nrDays = 5, firstHour = 7, lastHour = 18;
		boolean hasSat = false, hasSun = false;
		List<Assignment> table[][] = new List[Constants.NR_DAYS][Constants.SLOTS_PER_DAY];
		for (Request request: getNewRequests()) {
			if (request.getAssignment() == null) continue;
			for (Assignment assignment: request.getAssignment().getAssignments()) {
				if (assignment.getTime() == null) continue;
				int dayCode = assignment.getTime().getDayCode();
				if ((dayCode & Constants.DAY_CODES[Constants.DAY_SAT]) != 0) hasSat = true;
				if ((dayCode & Constants.DAY_CODES[Constants.DAY_SUN]) != 0) hasSun = true;
				int startHour = (assignment.getTime().getStartSlot() * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN) / 60;
				if (startHour < firstHour) firstHour = startHour;
				int endHour = ((assignment.getTime().getStartSlot() + assignment.getTime().getLength()) * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN + 59) / 60;
				if (endHour > lastHour) lastHour = endHour;
				for (Enumeration<Integer> e = assignment.getTime().getSlots(); e.hasMoreElements(); ) {
					int slot = e.nextElement();
					int day = slot / Constants.SLOTS_PER_DAY;
					int time = slot % Constants.SLOTS_PER_DAY;
					if (table[day][time] == null)
						table[day][time] = new ArrayList<Assignment>();
					table[day][time].add(assignment);
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
				"position: absolute; left: 0px; top: " + top + "px;'>" +  (h > 12 ? h - 12 : h) + (h < 12 ? "am" : "pm") + "</div>");
			out.println("<div style='font-size: x-small; text-align: center; padding-right: 2px; color: #6991CE; display: block; border-top: 1px solid transparent; height: 100%; width: 28px;" +
				"position: absolute; left: 0px; top: " + (25 + top) + "px; '></div>");
		}
		out.println("</div></td>");

		out.println("<td align='left' style='vertical-align: top; '>");
		out.println("<div style='border-bottom: 1px solid #DDDDDD; position: relative; overflow-x: hidden; overflow-y: hidden; width: " + (5 + 180 * nrDays) + "px; height: " + (50 * (lastHour - firstHour)) + "px; '>");
		
		out.println("<div style='position: relative; overflow-x: hidden; overflow-y: hidden; width: 100%; height: 100%; '>");
		
		// working hours
		out.println("<div style='background: #FFFDDD; width: " + (2 + 180 * nrDays) + "px; height: 500px; position: absolute; left: 0px; top: " + (25  + 50 * (7 - firstHour)) + "px;'></div>");
		
		for (Request request: getNewRequests()) {
			if (request instanceof FreeTimeRequest) {
				FreeTimeRequest fr = (FreeTimeRequest)request;
				for (DayCode dow: DayCode.toDayCodes(fr.getTime().getDayCode())) {
					out.println("<div style='background: #FFE1DD; width: 100%; color: #BA5353; font-size: x-small; text-align: left; white-space: nowrap; overflow: hidden;" +
							"width: 183px; height: " + (125 * fr.getTime().getLength() / 30) + "px; " +
							"position: absolute; left: " + (180 * dow.getIndex()) + "px;" +
							"top: " + (125 * fr.getTime().getStartSlot() / 30 - 50 * firstHour) + "px; '>");
					out.println("<div style='padding-left: 5px; white-space: nowrap; '>Free " +
							DayCode.toString(fr.getTime().getDayCode()) + " " + fr.getTime().getStartTimeHeader() + " - " + fr.getTime().getEndTimeHeader() + "</div>");
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
		for (Request request: getNewRequests()) {
			if (request instanceof CourseRequest && request.getAssignment() != null) {
				for (Section section: request.getAssignment().getSections()) {
					if (section.getTime() == null) continue;
					for (DayCode dow: DayCode.toDayCodes(section.getTime().getDayCode())) {
						int col = 0;
						int index = 0;
						for (int i = 0; i < section.getTime().getLength(); i++) {
							col = Math.max(col, table[dow.getIndex()][section.getTime().getStartSlot() + i].size());
							index = Math.max(index, table[dow.getIndex()][section.getTime().getStartSlot() + i].indexOf(section));
						}
						int w =  174 / col + (index + 1 != col && col > 1 ? -3 : 0);
						int h = 125 * section.getTime().getLength() / 30 - 3;
						int l = 4 + 180 * dow.getIndex() + index * 174 / col;
						int t = 1 + 125 * section.getTime().getStartSlot() / 30 - 50 * firstHour;
						out.println("<div style='overflow-x: hidden; overflow-y: hidden; width: " + w + "px; height: " + h + "px; position: absolute; left: " + l + "px; top: " + t + "px; " +
								"position: absolute; font-size: x-small; font-family: arial; overflow: hidden; -webkit-border-radius: 6px; -moz-border-radius: 6px; color: #FFFFFF; " +
								"border: 1px solid #" + sColor1[color] + "; background: #" + sColor2[color] + ";'>");
						out.println("<table cellspacing='0' cellpadding='0' style='padding-left: 4px; padding-right: 4px; padding-bottom: 2px; padding-top: 2px; width: 100%; -webkit-border-top-left-radius: 5px; -webkit-border-top-right-radius: 5px; -moz-border-radius-topleft: 5px; -moz-border-radius-topright: 5px;" +
								"background: #" + sColor1[color] + ";'><tr><td align='left' style='vertical-align: top; '>");
						out.println("<div style='padding-left: 2px; width: 100%; font-size: x-small; white-space: nowrap; overflow: hidden; color: #FFFFFF;'>" +
								request.getAssignment().getCourse().getSubjectArea() + " " + 
								request.getAssignment().getCourse().getCourseNumber() + " " +
								section.getSubpart().getName() + "</div></td></tr></tbody></table>");
						out.println("<div style='font-size: x-small; padding-left: 4px; white-space: wrap; -webkit-border-bottom-left-radius: 5px; -webkit-border-bottom-right-radius: 5px; -moz-border-radius-bottomleft: 5px; -moz-border-radius-bottomright: 5px;'>");
						if (section.getRooms() != null)
							for (RoomLocation room: section.getRooms()) {
								out.println("<span style='white-space: nowrap'>" + room.getName() + ",</span>");
							}
						if (section.getChoice().getInstructorNames() != null && !section.getChoice().getInstructorNames().isEmpty()) {
							String[] instructors = section.getChoice().getInstructorNames().split(":");
							for (String instructor: instructors) {
								String[] nameEmail = instructor.split("\\|");
								out.println("<span style='white-space: nowrap'>" + nameEmail[0] + ",</span>");
							}
						}
						if (section.getTime().getDatePatternName() != null && !section.getTime().getDatePatternName().isEmpty()) {
							out.println("<span style='white-space: nowrap'>" + section.getTime().getDatePatternName() + "</span>");
						}
						out.println("</div></div>");
					}
				}
				color = (1 + color) % sColor1.length;
			}
		}
		
		out.println("</div></td></tr></table></div></td></tr>");
		
		out.println("	<tr><td style=\"font-size: 9pt; font-style: italic; color: #9CB0CE; text-align: right; margin-top: -2px; white-space: nowrap;\">");
		out.println("		If the timetable is not displayed correctly, please check out the attached file.");
		out.println("	</td></tr>");

		
		out.println("</table>");
		
	}

}
