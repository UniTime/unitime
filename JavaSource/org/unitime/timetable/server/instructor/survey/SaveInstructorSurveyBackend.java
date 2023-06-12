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
package org.unitime.timetable.server.instructor.survey;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Transaction;
import org.unitime.commons.Email;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.Course;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.InstructorDepartment;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.InstructorSurveyData;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.InstructorSurveyRequest;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.InstructorSurveySaveRequest;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.Preferences;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.Selection;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomSharingDisplayMode;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomSharingOption;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.InstructorCourseRequirement;
import org.unitime.timetable.model.InstructorCourseRequirementNote;
import org.unitime.timetable.model.InstructorCourseRequirementType;
import org.unitime.timetable.model.InstructorSurvey;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.BuildingDAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.RoomFeatureDAO;
import org.unitime.timetable.model.dao.RoomGroupDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.DistributionTypeDAO;
import org.unitime.timetable.model.dao.InstructorSurveyDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.PreferenceLevelDAO;
import org.unitime.timetable.security.Qualifiable;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.qualifiers.SimpleQualifier;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.AccessDeniedException;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;

import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(InstructorSurveySaveRequest.class)
public class SaveInstructorSurveyBackend implements GwtRpcImplementation<InstructorSurveySaveRequest, InstructorSurveyData> {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	protected static final GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	private static Log sLog = LogFactory.getLog(SaveInstructorSurveyBackend.class);

	@Override
	public InstructorSurveyData execute(InstructorSurveySaveRequest request, SessionContext context) {
		if (!context.isAuthenticated() || context.getUser() == null || context.getUser().getCurrentAuthority() == null)
        	throw new AccessDeniedException();

		Long sessionId = request.getData().getSessionId();
		if (sessionId == null)
			sessionId = context.getUser().getCurrentAcademicSessionId();

		InstructorSurveyData survey = request.getData();
		survey.setPopupWarning(false); survey.setPopupMessage(null);
		if (context.getUser() == null || survey.getExternalId().equals(context.getUser().getExternalUserId())) {
			context.checkPermissionAnyAuthority(Right.InstructorSurvey, new Qualifiable[] { new SimpleQualifier("Session", sessionId)});
		} else {
			context.checkPermissionAnySession(Right.InstructorSurveyAdmin, new Qualifiable[] { new SimpleQualifier("Session", sessionId)});
		}
		
		ApplicationProperties.setSessionId(sessionId);
		
		org.hibernate.Session hibSession = InstructorSurveyDAO.getInstance().getSession();
		Transaction tx = hibSession.beginTransaction();
		try {
			InstructorSurvey is = (InstructorSurvey)hibSession.createQuery(
					"from InstructorSurvey where session = :sessionId and externalUniqueId = :externalId"
					).setLong("sessionId", sessionId)
					.setString("externalId", survey.getExternalId()).setMaxResults(1).uniqueResult();
			if (is == null && !request.isChanged()) {
				throw new GwtRpcException(MESSAGES.errorNoInstructorSurvey());
			}
			
			if (is == null) {
				is = new InstructorSurvey();
				is.setExternalUniqueId(survey.getExternalId());
				is.setSession(SessionDAO.getInstance().get(sessionId, hibSession));
				is.setPreferences(new HashSet<Preference>());
				is.setCourseRequirements(new HashSet<InstructorCourseRequirement>());
			} else if (request.isChanged()) {
				is.getPreferences().clear();
				for (InstructorCourseRequirement icr: is.getCourseRequirements()) {
					hibSession.delete(icr);
				}
				is.getCourseRequirements().clear();
			}
			is.setEmail(survey.getEmail());
			is.setNote(survey.getNote());
			Date ts = new Date();
			if (request.isSubmit()) {
				is.setSubmitted(ts);
				survey.setSubmitted(is.getSubmitted());
				survey.setEditable(context.hasPermissionAnySession(Right.InstructorSurveyAdmin, new Qualifiable[] { new SimpleQualifier("Session", sessionId)}));
			} else if (request.isUnsubmit()) {
				is.setSubmitted(null);
			}
			if (request.isChanged()) {
				is.setChanged(ts);
				is.setChangedBy(context.getUser().getTrueExternalUserId());
				
				if (survey.hasCourses()) {
					List<InstructorCourseRequirementType> types = (List<InstructorCourseRequirementType>)hibSession.createQuery(
							"from InstructorCourseRequirementType order by sortOrder").list();
					for (Course ci: survey.getCourses()) {
						if (!ci.hasCustomFields()) continue;
						CourseOffering co = null;
						if (ci.getId() != null)
							co = CourseOfferingDAO.getInstance().get(ci.getId(), hibSession);
						if (co != null || ci.hasCourseName()) {
							InstructorCourseRequirement icr = new InstructorCourseRequirement();
							icr.setCourseOffering(co);
							icr.setCourse(co == null ? ci.getCourseName() : co.getCourseName());
							icr.setInstructorSurvey(is);
							icr.setNotes(new HashSet<InstructorCourseRequirementNote>());
							for (InstructorCourseRequirementType type: types) {
								String note = ci.getCustomField(type.getUniqueId());
								if (note != null) {
									InstructorCourseRequirementNote n = new InstructorCourseRequirementNote();
									n.setType(type);
									n.setRequirement(icr);
									n.setNote(note);
									icr.getNotes().add(n);
								}
							}
							is.getCourseRequirements().add(icr);
						}
					}
				}
				
				if (survey.hasDistributionPreferences()) {
					Preferences p = survey.getDistributionPreferences();
					if (p.hasSelections()) {
						for (Selection selection: p.getSelections()) {
							DistributionType dt = DistributionTypeDAO.getInstance().get(selection.getItem(), hibSession);
							PreferenceLevel pl = PreferenceLevelDAO.getInstance().get(selection.getLevel(), hibSession);
							if (dt != null && pl != null) {
								DistributionPref dp = new DistributionPref();
								dp.setDistributionType(dt);
								dp.setPrefLevel(pl);
								dp.setNote(selection.getNote());
								dp.setOwner(is);
								is.getPreferences().add(dp);
							}
						}
					}
				}
				if (survey.hasRoomPreferences()) {
					for (Preferences p: survey.getRoomPreferences()) {
						if (p.hasSelections()) {
							for (Selection selection: p.getSelections()) {
								PreferenceLevel pl = PreferenceLevelDAO.getInstance().get(selection.getLevel(), hibSession);
								if (p.getId() == -1l) {
									Building b = BuildingDAO.getInstance().get(selection.getItem(), hibSession);
									if (b != null && pl != null) {
										BuildingPref bp = new BuildingPref();
										bp.setBuilding(b);
										bp.setPrefLevel(pl);
										bp.setNote(selection.getNote());
										bp.setOwner(is);is.getPreferences().add(bp);
									}
								} else if (p.getId() == -2l) {
									RoomGroup rg = RoomGroupDAO.getInstance().get(selection.getItem(), hibSession);
									if (rg != null && pl != null) {
										RoomGroupPref gp = new RoomGroupPref();
										gp.setRoomGroup(rg);
										gp.setPrefLevel(pl);
										gp.setNote(selection.getNote());
										gp.setOwner(is);is.getPreferences().add(gp);
									}
								} else if (p.getId() == -4l) {
									Location r = LocationDAO.getInstance().get(selection.getItem(), hibSession);
									if (r != null && pl != null) {
										RoomPref rp = new RoomPref();
										rp.setRoom(r);
										rp.setPrefLevel(pl);
										rp.setNote(selection.getNote());
										rp.setOwner(is);is.getPreferences().add(rp);
									}
								} else {
									RoomFeature rf = RoomFeatureDAO.getInstance().get(selection.getItem(), hibSession);
									if (rf != null && pl != null) {
										RoomFeaturePref fp = new RoomFeaturePref();
										fp.setRoomFeature(rf);
										fp.setPrefLevel(pl);
										fp.setNote(selection.getNote());
										fp.setOwner(is);is.getPreferences().add(fp);
									}
								}
							}
						}
					}
				}
				if (survey.getTimePrefs() != null && !survey.getTimePrefs().isEmpty()) {
					TimePref tp = new TimePref();
					tp.setNote(survey.getTimePrefs().getNote());
					tp.setPreference(survey.getTimePrefs().getPattern());
					tp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
					tp.setOwner(is); is.getPreferences().add(tp);
				}
			}
			
			hibSession.saveOrUpdate(is);
			survey.setId(is.getUniqueId());
			
			tx.commit(); tx = null;
			if (!request.isChanged())
				if (request.getInstructorId() != null)
					return new RequestInstructorSurveyBackend().execute(new InstructorSurveyRequest(request.getInstructorId()), context);
				else
					return new RequestInstructorSurveyBackend().execute(new InstructorSurveyRequest(request.getData().getExternalId(), sessionId.toString()), context);
			
			// Submitted by the owner -> send email
			if (survey.getSubmitted() != null && context.getUser() != null && survey.getExternalId().equals(context.getUser().getExternalUserId())) {
				try {
					if (sendEmail(context, survey))
						survey.setPopupMessage(MESSAGES.emailSent());
				} catch (Exception e) {
					survey.setPopupMessage(MESSAGES.failureSendingEmail(e.getMessage()));
					survey.setPopupWarning(true);
					sLog.error("Failed to sent an email: " + e.getMessage(), e);
				}
			}
			
			return survey;
		} catch (Exception ex) {
			if (tx != null) tx.rollback();
			if (ex instanceof GwtRpcException) throw (GwtRpcException)ex;
			throw new GwtRpcException(ex.getMessage(), ex);
		}
	}
	
	protected boolean sendEmail(SessionContext context, InstructorSurveyData survey) throws Exception {
		if (ApplicationProperty.InstructorSurveyEmailConfirmation.isFalse())
			return false;
		org.hibernate.Session hibSession = InstructorSurveyDAO.getInstance().getSession();
		Set<TimetableManager> managers = new HashSet<TimetableManager>();
		if (survey.hasDepartments())
			for (InstructorDepartment dept: survey.getDepartments()) {
				managers.addAll(hibSession.createQuery(
						"select m from TimetableManager m inner join m.departments d inner join m.managerRoles r " +
						"where d.uniqueId = :departmentId and r.receiveEmails = true and m.emailAddress is not null " +
						"and :adminRight in elements(r.role.rights) and :deptRight not in elements(r.role.rights)"
						).setLong("departmentId", dept.getId())
						.setString("adminRight", "InstructorSurveyAdmin")
						.setString("deptRight", "DepartmentIndependent")
						.list());
			}
		if (managers.isEmpty() && !survey.hasEmail())
			return false;
		
		String nameFormat = UserProperty.NameFormat.get(context.getUser());
		Email email = Email.createEmail();
		if (survey.hasEmail()) {
			email.addRecipientCC(survey.getEmail(), survey.getFormattedName());
			email.setReplyTo(survey.getEmail(), survey.getFormattedName());
		}
		for (TimetableManager m: managers)
			if (m.getEmailAddress() != null && !m.getEmailAddress().isEmpty())
				email.addRecipient(m.getEmailAddress(), m.getName(nameFormat));
		String acadSession = SessionDAO.getInstance().get(survey.getSessionId()).getLabel();
		String subject = MESSAGES.instructorSurveyEmailSubject(survey.getFormattedName(), acadSession);
		email.setSubject(subject);
		
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_0);
		cfg.setClassForTemplateLoading(SaveInstructorSurveyBackend.class, "");
		cfg.setLocale(Localization.getJavaLocale());
		cfg.setOutputEncoding("utf-8");
		Template template = cfg.getTemplate("confirmation.ftl");
		Map<String, Object> input = new HashMap<String, Object>();
		input.put("msg", MESSAGES);
		input.put("const", CONSTANTS);
		input.put("subject", subject);
		input.put("survey", survey);
		input.put("version", MESSAGES.pageVersion(Constants.getVersion(), Constants.getReleaseDate()));
		input.put("ts", new Date());
		input.put("link", ApplicationProperty.UniTimeUrl.value());
		input.put("submitted", Formats.getDateFormat(CONSTANTS.timeStampFormat()).format(survey.getSubmitted()));
		input.put("academicSession", acadSession);
		if (!survey.getTimePrefs().isEmpty())
			input.put("timePrefs", generateTimePreferencesTable(survey));

		StringWriter s = new StringWriter();
		template.process(input, new PrintWriter(s));
		s.flush(); s.close();
		
		email.setHTML(s.toString());
		
		email.send();
		return true;
	}
	
	public String generateTimePreferencesTable(InstructorSurveyData survey) {
		RoomSharingDisplayMode mode = survey.getTimePrefs().getModes().get(0);
		P panel = new P("font-family: Verdana, sans-serif", "font-size: 8pt", "font-weight: 400", "font-style: normal",
				"color: #333333", "border-spacing: 0px;");
		int split = 24;
		P previousTable = null;
		for (int page = 0; page == 0 || (split > 0 && mode.getFirstSlot() + split * page * mode.getStep() < mode.getLastSlot()); page++) {
			P table = new P("vertical-align: top");
			panel.add(table);
			P box = new P("display: table", "overflow: hidden");
			table.add(box);
			P header = new P("display: table-row");
			box.add(header);
			P corner = new P("display: table-cell", "width: 43px", "height: 25px", "vertical-align: middle",
					"border-right: 1px solid black", "border-bottom: 1px solid black", "text-align: right", "font-size: xx-small", "font-weight: bold");
			corner.setHTML(MESSAGES.roomSharingCorner());
			header.add(corner);
			
			int first = mode.getFirstSlot();
			int last = mode.getLastSlot();
			if (split > 0) {
				first = mode.getFirstSlot() + split * page * mode.getStep();
				last = Math.min(first + split * mode.getStep(), mode.getLastSlot());
				if (previousTable != null)
					previousTable.addStyle("display: block");
				previousTable = table;
			}
			
			final List<List<Cell>> thisTime = new ArrayList<List<Cell>>();
			for (int slot = first; slot < last; slot += mode.getStep()) {
				P p = new P("display: table-cell", "width: 43px", "height: 25px", "vertical-align: middle",
						"border-top: 1px solid black", "border-right: 1px solid black", "border-bottom: 1px solid black",
						"background-color: #E0E0E0;", "text-align: center", "font-size: xx-small", "font-weight: bold",
						"padding-left: 1px", "padding-right: 1px");
				p.setHTML(MESSAGES.roomSharingTimeHeader(slot2short(slot), slot2short(slot + mode.getStep())));
				final List<Cell> t = new ArrayList<Cell>();
				thisTime.add(t);
				header.add(p);
			}
			
			final List<Cell> thisPage = new ArrayList<Cell>();
			int day = mode.getFirstDay();
			while (true) {
				P line = new P("display: table-row");
				box.add(line);
				P d = new P("display: table-cell", "width: 43px", "height: 25px", "vertical-align: middle",
						"border-right: 1px solid black", "border-left: 1px solid black", "border-bottom: 1px solid black", "background-color: #E0E0E0",
						"text-align: center", "font-weight: bold");
				d.setHTML(CONSTANTS.days()[day % 7]);
				line.add(d);
				final List<Cell> thisDay = new ArrayList<Cell>();
				for (int slot = first; slot < last; slot += mode.getStep()) {
					Cell p = new Cell(day, slot, survey.getTimePrefs().getOption(day, slot), mode);
					line.add(p);
					thisDay.add(p);
					thisPage.add(p);
					thisTime.get((slot - first) / mode.getStep()).add(p);
				}
				if (day == mode.getLastDay()) break;
				day = (1 + day) % 7;
			}
		}
				
		P legend = new P("display: -moz-inline-box", "display: inline-block", "padding: 3px 5px 3px 5px", "vertical-align: middle");
		panel.add(legend);
		
		final P box = new P("display: table", "overflow: hidden");
		legend.add(box);
		
		for (final RoomSharingOption option: survey.getTimePrefs().getOptions()) {
			final P line = new P("display: table-row");
			
			final P icon = new P("display: table-cell", "width: 43px", "height: 25px", "font-size: x-small", "vertical-align: middle",
					"border-bottom: 1px solid black", "border-left: 1px solid black", "border-right: 1px solid black");
			if (box.getWidgetCount() == 0) icon.addStyle("border-top: 1px solid black");
			if (option.getCode() != null && !option.getCode().isEmpty()) icon.setHTML(option.getCode());
			icon.addStyle("background-color: " + option.getColor());
			line.add(icon);
			
			final P title = new P("display: table-cell", "font-weight: bold", "vertical-align: middle", "padding-bottom: 2px", "padding-left: 5px");
			title.setHTML(option.getName());
			line.add(title);
			
			box.add(line);
		}
		return panel.toString();
	}
	
	public static String slot2short(int slot) {
		int h = slot / 12;
        int m = 5 * (slot % 12);
        if (CONSTANTS.useAmPm())
        	return (h > 12 ? h - 12 : h) + ":" + (m < 10 ? "0" : "") + m + (h == 24 ? "a" : h >= 12 ? "p" : "a");
        else
			return h + ":" + (m < 10 ? "0" : "") + m;
	}
		
	protected static class P {
		List<String> iStyles = new ArrayList<String>();
		List<P> iPanels = new ArrayList<P>();
		private String iHTML = null;
		private String iTitle = null;
		
		P(String... styles) {
			if (styles != null)
				for (String style: styles)
					iStyles.add(style);
		}
		
		public void addStyle(String style) {
			iStyles.add(style);
		}
		
		void setHTML(String html) {
			iHTML = html;
		}
		
		void setTitle(String title) {
			iTitle = title;
		}
		
		void add(P panel) {
			iPanels.add(panel);
		}
		
		int getWidgetCount() { return iPanels.size(); }
		
		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("<span style='");
			for (String style: iStyles)
				sb.append(style + ";");
			sb.append("'");
			if (iTitle != null)
				sb.append(" title='" + iTitle + "'");
			sb.append(">");
			if (iHTML != null)
				sb.append(iHTML);
			for (P p: iPanels)
				sb.append(p.toString());
			sb.append("</span>");
			return sb.toString();
		}
	}
	
	protected static class Cell extends P {
		private int iDay, iSlot;
		
		Cell(int day, int slot, RoomSharingOption option, RoomSharingDisplayMode mode) {
			super("display: table-cell", "width: 43px", "height: 25px", "font-size: x-small", "vertical-align: middle",
					"border-right: 1px solid black", "border-bottom: 1px solid black", "text-align: center", "vertical-align: middle",
					"font-size: x-small", "overflow: hidden", "white-space: nowrap", "max-width: 38px", "color: white");
			iDay = day; iSlot = slot;
			if (option == null) {
				setHTML("");
			} else {
				addStyle("background-color: " + option.getColor());
				setHTML(option.getCode() == null ? "" : option.getCode());
				setTitle(CONSTANTS.longDays()[day] + " " + slot2short(slot) + " - " + slot2short(slot + mode.getStep()) + ": " + option.getName());
			}
		}
		
		public int getDay() { return iDay; }
		public int getSlot() { return iSlot; }
	}
}
