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
package org.unitime.timetable.onlinesectioning.custom.purdue;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.resource.ClientResource;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.NoteInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceType;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLogger;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog.Entity;
import org.unitime.timetable.onlinesectioning.custom.CustomClassAttendanceProvider;
import org.unitime.timetable.onlinesectioning.custom.ExternalTermProvider;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.util.CalendarUtils;
import org.unitime.timetable.util.DateUtils;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.Formats.Format;
import org.unitime.timetable.util.NameFormat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * @author Tomas Muller
 */
public class PurdueClassAttendance implements CustomClassAttendanceProvider {
	private static Log sLog = LogFactory.getLog(PurdueClassAttendance.class);
	private static GwtConstants CONST = Localization.create(GwtConstants.class);
	
	private Client iClient;
	private ExternalTermProvider iExternalTermProvider;
	
	public PurdueClassAttendance() {
		List<Protocol> protocols = new ArrayList<Protocol>();
		protocols.add(Protocol.HTTP);
		protocols.add(Protocol.HTTPS);
		iClient = new Client(protocols);
		Context cx = new Context();
		cx.getParameters().add("readTimeout", getClassAttendanceApiReadTimeout());
		iClient.setContext(cx);
		try {
			String clazz = ApplicationProperty.CustomizationExternalTerm.value();
			if (clazz == null || clazz.isEmpty())
				iExternalTermProvider = new BannerTermProvider();
			else
				iExternalTermProvider = (ExternalTermProvider)Class.forName(clazz).getConstructor().newInstance();
		} catch (Exception e) {
			sLog.error("Failed to create external term provider, using the default one instead.", e);
			iExternalTermProvider = new BannerTermProvider();
		}
	}
	
	protected String getClassAttendanceApiReadTimeout() {
		return ApplicationProperties.getProperty("purdue.classAttendance.readTimeout", "60000");
	}
	
	protected String getClassAttendanceApiSite() {
		return ApplicationProperties.getProperty("purdue.classAttendance.site");
	}
	
	protected String getClassAttendanceApiKey() {
		return ApplicationProperties.getProperty("purdue.classAttendance.apiKey");
	}
	
	protected String getStudentClassAttendanceApiSite() {
		return ApplicationProperties.getProperty("purdue.classAttendance.getStudentMeetPlan", getClassAttendanceApiSite() + "/getStudentMeetPlan");
	}
	
	protected String getBannerId(Student student) {
		String id = student.getExternalUniqueId();
		while (id.length() < 9) id = "0" + id;
		return id;
	}
	
	protected String getBannerTerm(AcademicSessionInfo session) {
		return iExternalTermProvider.getExternalTerm(session);
	}
	
	protected String getBannerCampus(AcademicSessionInfo session) {
		return iExternalTermProvider.getExternalCampus(session);
	}
	
	protected Gson getGson(OnlineSectioningHelper helper) {
		GsonBuilder builder = new GsonBuilder()
		.registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
			@Override
			public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
				return new JsonPrimitive(new SimpleDateFormat("yyyy-MM-dd").format(src));
			}
		})
		.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
			@Override
			public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
				try {
					return new SimpleDateFormat("yyyy-MM-dd").parse(json.getAsJsonPrimitive().getAsString());
				} catch (ParseException e) {
					throw new JsonParseException(e.getMessage(), e);
				}
			}
		});
		if (helper.isDebugEnabled())
			builder.setPrettyPrinting();
		return builder.create();
	}

	@Override
	public StudentClassAttendance getCustomClassAttendanceForStudent(Student student, OnlineSectioningHelper helper, SessionContext context) {
		if (student == null) return null;
		boolean logHelper = false;
		if (helper == null) {
			OnlineSectioningLog.Entity.Builder user = Entity.newBuilder().setExternalId(context == null || context.getUser() == null ? "System" : context.getUser().getExternalUserId()).setType(Entity.EntityType.MANAGER);
			if (context != null && context.getUser() != null)
				user.setName(context.getUser().getName());
			if (context != null && context.getUser() != null && context.getUser().getExternalUserId().equals(student.getExternalUniqueId()))
				user.setType(Entity.EntityType.STUDENT);
			helper = new OnlineSectioningHelper(SessionDAO.getInstance().getSession(), user.build());
			logHelper = true;
			OnlineSectioningLog.Action.Builder action = helper.getAction();
	    	action.setOperation("check-attendance");
			action.setSession(OnlineSectioningLog.Entity.newBuilder()
	    			.setUniqueId(student.getSession().getUniqueId())
	    			.setName(student.getSession().getReference())
	    			);
	    	action.setStartTime(System.currentTimeMillis());
	    	action.setUser(user);
	    	action.setStudent(OnlineSectioningLog.Entity.newBuilder()
					.setUniqueId(student.getUniqueId())
					.setExternalId(student.getExternalUniqueId())
					.setName(NameFormat.LAST_FIRST_MIDDLE.format(student))
					.setType(OnlineSectioningLog.Entity.EntityType.STUDENT));
		}
		long c0 = OnlineSectioningHelper.getCpuTime();
		ClientResource resource = null;
		try {
			Gson gson = getGson(helper);
			resource = new ClientResource(getStudentClassAttendanceApiSite());
			resource.setNext(iClient);
			
			AcademicSessionInfo session = new AcademicSessionInfo(student.getSession());
			String term = getBannerTerm(session);
			String campus = getBannerCampus(session);
			resource.addQueryParameter("term", term);
			resource.addQueryParameter("campus", campus);
			resource.addQueryParameter("studentId", getBannerId(student));
			resource.addQueryParameter("apiKey", getClassAttendanceApiKey());
			helper.getAction().addOptionBuilder().setKey("term").setValue(term);
			helper.getAction().addOptionBuilder().setKey("campus").setValue(campus);
			helper.getAction().addOptionBuilder().setKey("studentId").setValue(getBannerId(student));
			
			long t1 = System.currentTimeMillis();
			
			resource.get(MediaType.APPLICATION_JSON);
			
			long t2 = System.currentTimeMillis();
			
			StudentAttendancePlanResponse response = (StudentAttendancePlanResponse)new GsonRepresentation<StudentAttendancePlanResponse>(resource.getResponseEntity(), StudentAttendancePlanResponse.class).getObject();
			
			sLog.debug("Response for " + getBannerId(student) + " [" + (t2 - t1) + " ms]: " + gson.toJson(response));
			
			helper.getAction().addOptionBuilder().setKey("response").setValue(gson.toJson(response));
			
			if (response.data != null && "success".equals(response.status)) {
				return new PurdueStudentClassAttendance(response.data, student.getSession());
			} else if (response.message != null) {
				helper.warn(response.message);
			}
			
			return null;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			if (e.getMessage() != null)
				helper.error(e.getMessage());
			return null;
		} finally {
			if (resource != null) {
				if (resource.getResponse() != null) resource.getResponse().release();
				resource.release();
			}
			if (logHelper) {
				helper.getAction().setCpuTime(OnlineSectioningHelper.getCpuTime() - c0);
				helper.getAction().setEndTime(System.currentTimeMillis());
				OnlineSectioningLogger.getInstance().record(OnlineSectioningLog.Log.newBuilder().addAction(helper.getAction()).build());
			}
		}
	}
	
	public static enum DayOfWeek {
		M(Calendar.MONDAY),
		T(Calendar.TUESDAY),
		W(Calendar.WEDNESDAY),
		R(Calendar.THURSDAY),
		F(Calendar.FRIDAY),
		S(Calendar.SATURDAY),
		U(Calendar.SUNDAY),
		;
		private int iDayOfWeek;
		private DayOfWeek(int dow) {
			iDayOfWeek = dow;
		}
		public int getDayOfWeek() { return iDayOfWeek; }
	}
	
	public static class StudentAttendancePlanResponse {
		StudentMeetings data;
		String message;
		String status;
	}
	
	public static class StudentMeetings {
		String puid;
		List<StudentSectionMeetings> sectionMeetings;
	}
	
	public static class StudentSectionMeetings {
		Set<String> crns;
		List<MeetingDetail> meetings;
	}
	
	public static class MeetingDetail {
		Date startDate;
		Date endDate;
		String groupName;
		DayOfWeek meetDay;
		String bldg;
		String room;
		Integer startTime;
		Integer endTime;
		String occurance;
	}
	
	public static class PurdueStudentClassAttendance implements StudentClassAttendance {
		private StudentMeetings iMeetings;
		private int iStartYear;
		private int iStartMonth;
		private String iHolidays = null;
		private boolean iSkipBreaks;
		
		PurdueStudentClassAttendance(StudentMeetings meetings, Session session) {
			iMeetings = meetings;
			iStartYear = session.getSessionStartYear();
			iStartMonth = session.getStartMonth();
			iHolidays = session.getHolidays();
			iSkipBreaks = "true".equalsIgnoreCase(ApplicationProperties.getProperty("purdue.classAttendance.skipBreaks", "true"));
		}
		
		protected boolean isHoliday(Date date) {
			if (!iSkipBreaks) return false;
			try {
				if (iHolidays == null) return false;
				int idx = CalendarUtils.date2dayOfYear(iStartYear, date) - DateUtils.getDayOfYear(1, iStartMonth, iStartYear) - 1;
				if (idx < 0 || idx >= iHolidays.length()) return false;
				return ((int)(iHolidays.charAt(idx) - '0')) != 0;
			} catch (IndexOutOfBoundsException e) {
				return false;
			}
		}
		
		protected StudentSectionMeetings getMeetings(String externalId) {
			if (externalId == null) return null;
			if (externalId.indexOf('-') > 0) externalId = externalId.substring(0, externalId.indexOf('-'));
			if (iMeetings == null || iMeetings.sectionMeetings == null) return null;
			for (StudentSectionMeetings ssm: iMeetings.sectionMeetings) {
				if (ssm.crns != null && ssm.crns.contains(externalId) && ssm.meetings != null && !ssm.meetings.isEmpty())
					return ssm;
			}
			return null;
		}
		
		protected StudentSectionMeetings getMeetings(EventInterface event) {
			if (event.hasExternalIds())
				for (String externalId: event.getExternalIds()) {
					StudentSectionMeetings ssm = getMeetings(externalId);
					if (ssm != null) return ssm;
				}
			return null;
		}
		
		public String getTime(int time) {
			int min = 60 * (time / 100) + (time % 100);
			int h = min / 60;
	        int m = min % 60;
	        if (min == 0)
	        	return CONST.timeMidnight();
	        if (min == 720)
	        	return CONST.timeNoon();
	        if (CONST.useAmPm()) {
	        	return (h > 12 ? h - 12 : h) + ":" + (m < 10 ? "0" : "") + m + (h == 24 ? CONST.timeShortAm() : h >= 12 ? CONST.timeShortPm() : CONST.timeShortAm());
			} else {
				return h + ":" + (m < 10 ? "0" : "") + m;
			}
		}
		
		protected Date getFirstMeetingDate(MeetingDetail m, EventInterface event) {
			Calendar c = Calendar.getInstance(); c.setTime(m.startDate);
			int index = 0;
			int startSlot = 12 * (m.startTime / 100) + (m.startTime % 100) / 5;
			int endSlot =  12 * (m.endTime / 100) + (m.endTime % 100) / 5;
			while (!c.getTime().after(m.endDate)) {
				if (c.get(Calendar.DAY_OF_WEEK) == m.meetDay.getDayOfWeek()) {
					if (event != null) {
						boolean match = false;
						for (MeetingInterface meeting: event.getMeetings()) {
							if (meeting.getDayOfYear() == CalendarUtils.date2dayOfYear(iStartYear, c.getTime()) &&
								startSlot < meeting.getEndSlot() && meeting.getStartSlot() < endSlot &&
								meeting.getLocationName().equalsIgnoreCase(("OFFCAMP".equals(m.bldg) ? "" : m.bldg + " ") + m.room)) {
								match = true;
								break;
							}
						}
						if (!match) {
							c.add(Calendar.DAY_OF_YEAR, 1);
							continue;
						}
					} else {
						if (isHoliday(c.getTime())) {
							c.add(Calendar.DAY_OF_YEAR, 1);
							continue;
						}
					}
					index ++;
					boolean meet = true;
					if ("Every Other Week".equals(m.occurance)) {
						meet = ((index % 2) == 1);
					} else if ("One Time".equals(m.occurance)) {
						meet = (index <= 1);
					}
					if (meet) return c.getTime();
				}
				c.add(Calendar.DAY_OF_YEAR, 1);
			}
			return null;
		}
		
		protected Date getLastMeetingDate(MeetingDetail m, EventInterface event) {
			Calendar c = Calendar.getInstance(); c.setTime(m.startDate);
			int index = 0;
			Date last = null;
			int startSlot = 12 * (m.startTime / 100) + (m.startTime % 100) / 5;
			int endSlot =  12 * (m.endTime / 100) + (m.endTime % 100) / 5;
			while (!c.getTime().after(m.endDate)) {
				if (c.get(Calendar.DAY_OF_WEEK) == m.meetDay.getDayOfWeek()) {
					if (event != null) {
						boolean match = false;
						for (MeetingInterface meeting: event.getMeetings()) {
							if (meeting.getDayOfYear() == CalendarUtils.date2dayOfYear(iStartYear, c.getTime()) &&
								startSlot < meeting.getEndSlot() && meeting.getStartSlot() < endSlot &&
								meeting.getLocationName().equalsIgnoreCase(("OFFCAMP".equals(m.bldg) ? "" : m.bldg + " ") + m.room)) {
								match = true;
								break;
							}
						}
						if (!match) {
							c.add(Calendar.DAY_OF_YEAR, 1);
							continue;
						}
					} else {
						if (isHoliday(c.getTime())) {
							c.add(Calendar.DAY_OF_YEAR, 1);
							continue;
						}
					}
					index ++;
					boolean meet = true;
					if ("Every Other Week".equals(m.occurance)) {
						meet = ((index % 2) == 1);
					} else if ("One Time".equals(m.occurance)) {
						meet = (index <= 1);
					}
					if (meet) last = c.getTime();
				}
				c.add(Calendar.DAY_OF_YEAR, 1);
			}
			return last;
		}
		
		protected String toString(MeetingDetail m, EventInterface classEvent) {
			Date first = getFirstMeetingDate(m, classEvent);
			Date last = getLastMeetingDate(m, classEvent);
			if (classEvent != null && first == null) return null;
			Format<Date> f = Formats.getDateFormat(Formats.Pattern.DATE_EVENT_SHORT);
			return m.occurance + " " + CONST.days()[m.meetDay.ordinal()] + " " +
				getTime(m.startTime) + " - " + getTime(m.endTime) + "\n    " +
				(first == null ? "" : first.equals(last) ? f.format(first) + " " : f.format(first) + " - " + f.format(last) + " ") +
				("OFFCAMP".equals(m.bldg) ? "" : m.bldg + " ") + m.room;
		}
		
		protected List<MeetingInterface> toMeetings(StudentSectionMeetings ssm, EventInterface event) {
			List<MeetingInterface> meetings = new ArrayList<MeetingInterface>();
			for (MeetingDetail m: ssm.meetings) {
				int startSlot = 12 * (m.startTime / 100) + (m.startTime % 100) / 5;
				int endSlot =  12 * (m.endTime / 100) + (m.endTime % 100) / 5;
				int startOffset = (m.startTime % 5);
				int endOffset = (m.endTime % 5);
				Calendar c = Calendar.getInstance(); c.setTime(m.startDate);
				int index = 0;
				while (!c.getTime().after(m.endDate)) {
					if (c.get(Calendar.DAY_OF_WEEK) == m.meetDay.getDayOfWeek()) {
						if (event != null) {
							boolean match = false;
							for (MeetingInterface meeting: event.getMeetings()) {
								if (meeting.getDayOfYear() == CalendarUtils.date2dayOfYear(iStartYear, c.getTime()) &&
									startSlot < meeting.getEndSlot() && meeting.getStartSlot() < endSlot &&
									meeting.getLocationName().equalsIgnoreCase(("OFFCAMP".equals(m.bldg) ? "" : m.bldg + " ") + m.room)) {
									match = true;
									break;
								}
							}
							if (!match) {
								c.add(Calendar.DAY_OF_YEAR, 1);
								continue;
							}
						}
						index ++;
						boolean meet = true;
						if ("Every Other Week".equals(m.occurance)) {
							meet = ((index % 2) == 1);
						} else if ("One Time".equals(m.occurance)) {
							meet = (index <= 1);
						}
						if (meet) {
							MeetingInterface meeting = new MeetingInterface();
							meeting.setDayOfWeek(m.meetDay.ordinal());
							meeting.setMeetingDate(c.getTime());
							meeting.setStartSlot(startSlot);
							meeting.setEndSlot(endSlot);
							meeting.setStartOffset(startOffset);
							meeting.setEndOffset(endOffset);
							meeting.setDayOfYear(CalendarUtils.date2dayOfYear(iStartYear, c.getTime()));
							ResourceInterface room = new ResourceInterface();
							room.setName(("OFFCAMP".equals(m.bldg) ? "" : m.bldg + " ") + m.room);
							meeting.setLocation(room);
							meetings.add(meeting);
						}
					}
					c.add(Calendar.DAY_OF_YEAR, 1);
				}				
			}
			return meetings;
		}
		
		protected String getMessage(StudentSectionMeetings ssm, EventInterface classEvent) {
			String message = ApplicationProperties.getProperty("purdue.classAttendance.messageFace2FacePlan", "In-person course meetings ({group}):").replace("{group}", ssm.meetings.get(0).groupName);
			boolean hasMessage = false;
			for (MeetingDetail m: ssm.meetings) {
				String msg = toString(m, classEvent);
				if (msg != null) {
					message += "\n  " + msg;
					hasMessage = true;
				}
			}
			if (hasMessage) {
				if (classEvent == null)
					message += ApplicationProperties.getProperty("purdue.classAttendance.footerMessage", "\nFor more details, see your <a href='gwt.jsp?page=personal' target='_blank'>Personal Schedule</a>.");
				return message;
			}
			return null;
		}

		@Override
		public String getClassNote(String externalId) {
			StudentSectionMeetings ssm = getMeetings(externalId);
			if (ssm != null) return getMessage(ssm, null);
			return null;
		}
			
		protected MeetingInterface match(List<MeetingInterface> meetings, MeetingInterface meeting) {
			for (MeetingInterface m: meetings) {
				if (m.overlapsWith(meeting) && m.getLocationName().equals(meeting.getLocationName())) {
					return m;
				}
			}
			return null;
		}

		@Override
		public void updateAttendance(EventInterface classEvent) {
			StudentSectionMeetings ssm = getMeetings(classEvent);
			if (ssm != null) {
				ResourceInterface onl = new ResourceInterface();
				onl.setName(ApplicationProperties.getProperty("purdue.classAttendance.onlineRoom", "Remote"));
				onl.setType(ResourceType.ROOM);
				Location online = Location.findByName(SessionDAO.getInstance().getSession(), classEvent.getSessionId(), onl.getName());
				if (online != null) {
					onl.setId(online.getUniqueId());
					onl.setName(online.getLabel());
					onl.setSize(online.getCapacity());
					onl.setRoomType(online.getRoomTypeLabel());
					onl.setBreakTime(online.getEffectiveBreakTime());
					onl.setMessage(online.getEventMessage());
					onl.setIgnoreRoomCheck(online.isIgnoreRoomCheck());
					onl.setDisplayName(online.getDisplayName());
					onl.setPartitionParentId(online.getPartitionParentId());
					onl.setEventEmail(online.effectiveEventEmail());
				}
				String note = getMessage(ssm, iSkipBreaks ? classEvent : null);
				if (note != null) {
					NoteInterface n = new NoteInterface();
					n.setDate(new Date());
					n.setNote(note);
					classEvent.addNote(n);
				}
				long id = 0;
				List<MeetingInterface> meetings = toMeetings(ssm, iSkipBreaks ? classEvent : null);
				for (MeetingInterface m: new ArrayList<MeetingInterface>(classEvent.getMeetings())) {
					MeetingInterface match = match(meetings, m);
					if (match == null) {
						m.setLocation(onl);
						m.setStyle("online-meeting");
					} else if (match.getStartSlot() == m.getStartSlot() && (5 * m.getEndSlot() + m.getEndOffset()) == 5 * match.getEndSlot()) {
						m.setStyle("f2f-meeting");
					} else {
						m.setStyle("f2f-meeting");
						if (m.getStartSlot() < match.getStartSlot()) {
							MeetingInterface before = new MeetingInterface(m);
							before.setLocation(onl);
							before.setEndSlot(match.getStartSlot()); before.setEndOffset(match.getStartOffset());
							before.setId(--id);
							before.setStyle("online-meeting");
							m.setStartSlot(match.getStartSlot()); m.setStartOffset(match.getStartOffset());
							classEvent.addMeeting(before);
						}
						if ((5 * m.getEndSlot() + m.getEndOffset()) > 5 * match.getEndSlot()) {
							MeetingInterface after = new MeetingInterface(m);
							after.setLocation(onl);
							after.setStartSlot(match.getEndSlot()); after.setStartOffset(match.getEndOffset());
							after.setId(--id);
							after.setStyle("online-meeting");
							m.setEndSlot(match.getEndSlot()); m.setEndOffset(match.getEndOffset());
							classEvent.addMeeting(after);
						}
					}
				}
			}
		}
	}
	
}
