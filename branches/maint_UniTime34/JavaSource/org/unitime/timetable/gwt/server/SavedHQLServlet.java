package org.unitime.timetable.gwt.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.export.Exporter.Printer;
import org.unitime.timetable.export.hql.SavedHqlExportToCSV;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.services.SavedHQLService;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.gwt.shared.SavedHQLException;
import org.unitime.timetable.gwt.shared.SavedHQLInterface;
import org.unitime.timetable.gwt.shared.SavedHQLInterface.Query;
import org.unitime.timetable.model.SavedHQL;
import org.unitime.timetable.model.dao.SavedHQLDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.Navigation;

@Service("hql.gwt")
public class SavedHQLServlet implements SavedHQLService {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	private static Logger sLog = Logger.getLogger(SavedHQLServlet.class);
	
	private @Autowired SessionContext sessionContext;
	private SessionContext getSessionContext() { return sessionContext; }

	@Override
	@PreAuthorize("checkPermission('HQLReports')")
	public List<SavedHQLInterface.Flag> getFlags() throws SavedHQLException, PageAccessException {
		List<SavedHQLInterface.Flag> ret = new ArrayList<SavedHQLInterface.Flag>();
		for (SavedHQL.Flag f: SavedHQL.Flag.values()) {
			SavedHQLInterface.Flag flag = new SavedHQLInterface.Flag();
			flag.setValue(f.flag());
			flag.setText(getLocalizedDescription(f));
			flag.setAppearance(f.getAppearance());
			ret.add(flag);
		}
		return ret;
	}

	@Override
	@PreAuthorize("checkPermission('HQLReports')")
	public List<SavedHQLInterface.Option> getOptions() throws SavedHQLException, PageAccessException {
		List<SavedHQLInterface.Option> ret  = new ArrayList<SavedHQLInterface.Option>();
		for (SavedHQL.Option o: SavedHQL.Option.values()) {
			if (!o.allowSingleSelection() && !o.allowMultiSelection()) continue;
			SavedHQLInterface.Option option = new SavedHQLInterface.Option();
			option.setMultiSelect(o.allowMultiSelection());
			option.setName(getLocalizedText(o));
			option.setType(o.name());
			Map<Long, String> values = o.values(getSessionContext().getUser());
			if (values == null || values.isEmpty()) continue;
			for (Map.Entry<Long, String> e: values.entrySet()) {
				SavedHQLInterface.IdValue v = new SavedHQLInterface.IdValue();
				v.setText(e.getValue());
				v.setValue(e.getKey().toString());
				option.values().add(v);
			}
			Collections.sort(option.values());
			ret.add(option);
		}
		return ret;
	}

	@Override
	@PreAuthorize("checkPermission('HQLReports') and checkPermission('HQLReportAdd')")
	public Boolean editable() throws SavedHQLException, PageAccessException {
		return true;
	}

	@Override
	@PreAuthorize("checkPermission('HQLReports')")
	public List<SavedHQLInterface.Query> queries(String appearance) throws SavedHQLException, PageAccessException {
		SavedHQL.Flag ap = getAppearanceFlag(appearance);
		switch (ap) {
		case APPEARANCE_ADMINISTRATION:
			getSessionContext().checkPermission(Right.HQLReportsAdministration); break;
		case APPEARANCE_COURSES:
			getSessionContext().checkPermission(Right.HQLReportsCourses); break;
		case APPEARANCE_EXAMS:
			getSessionContext().checkPermission(Right.HQLReportsExaminations); break;
		case APPEARANCE_SECTIONING:
			getSessionContext().checkPermission(Right.HQLReportsStudents); break;
		case APPEARANCE_EVENTS:
			getSessionContext().checkPermission(Right.HQLReportsEvents); break;
		}
		List<SavedHQLInterface.Query> ret = new ArrayList<SavedHQLInterface.Query>(); 
		for (SavedHQL hql: SavedHQL.listAll(null, ap, getSessionContext().hasPermission(Right.HQLReportsAdminOnly))) {
			SavedHQLInterface.Query query = new SavedHQLInterface.Query();
			query.setName(hql.getName());
			query.setDescription(hql.getDescription());
			query.setQuery(hql.getQuery());
			query.setFlags(hql.getType());
			query.setId(hql.getUniqueId());
			ret.add(query);
		}
		return ret;
	}

	@Override
	@PreAuthorize("checkPermission('HQLReports')")
	public List<String[]> execute(SavedHQLInterface.Query query, List<SavedHQLInterface.IdValue> options, int fromRow, int maxRows) throws SavedHQLException, PageAccessException {
		try {
			final List<String[]> ret = new ArrayList<String[]>();
			Printer out = new Printer() {
				
				@Override
				public void printLine(String... fields) throws IOException {
					ret.add(fields);
				}
				
				@Override
				public void printHeader(String... fields) throws IOException {
					ret.add(fields);
				}
				
				@Override
				public void hideColumn(int col) {}
				
				@Override
				public String getContentType() { return null; }
				
				@Override
				public void flush() throws IOException {}
				
				@Override
				public void close() throws IOException {}
			};
			
			SavedHqlExportToCSV.execute(getSessionContext().getUser(), out, query, options, fromRow, maxRows);
		
			return ret;
		} catch (PageAccessException e) {
			throw e;
		} catch (SavedHQLException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new SavedHQLException(MESSAGES.failedExecution(e.getMessage() + (e.getCause() == null ? "" : " (" + e.getCause().getMessage() + ")")));
		}
	}
	
	@Override
	@PreAuthorize("(#query.id != null and checkPermission(#query.id, 'SavedHQL', 'HQLReportEdit')) or (#query.id == null and checkPermission('HQLReportAdd'))")
	public Long store(Query query) throws SavedHQLException, PageAccessException {
		if (SavedHQL.Flag.ADMIN_ONLY.isSet(query.getFlags()))
			getSessionContext().checkPermission(Right.HQLReportsAdminOnly);
		org.hibernate.Session hibSession = SavedHQLDAO.getInstance().getSession();
		SavedHQL hql = null;
		if (query.getId() != null) {
			hql = SavedHQLDAO.getInstance().get(query.getId(), hibSession);
		}
		if (hql == null) {
			hql = new SavedHQL();
		}
		hql.setName(query.getName());
		hql.setDescription(query.getDescription());
		hql.setType(query.getFlags());
		hql.setQuery(query.getQuery());
		hibSession.saveOrUpdate(hql);
		hibSession.flush();
		hibSession.refresh(hql);
		return hql.getUniqueId();
	}

	@Override
	@PreAuthorize("checkPermission(#id, 'SavedHQL', 'HQLReportDelete')")
	public Boolean delete(Long id) throws SavedHQLException, PageAccessException {
		if (id == null) throw new SavedHQLException(MESSAGES.errorNoReportProvided());
		org.hibernate.Session hibSession = SavedHQLDAO.getInstance().getSession();
		SavedHQL hql = SavedHQLDAO.getInstance().get(id, hibSession);
		if (hql != null) {
			hibSession.delete(hql);
			hibSession.flush();
		}
		return hql != null;
	}
	
	private SavedHQL.Flag getAppearanceFlag(String appearance) {
		for (SavedHQL.Flag flag: SavedHQL.Flag.values())
			if (flag.getAppearance() != null && flag.getAppearance().equalsIgnoreCase(appearance))
				return flag;
		return SavedHQL.Flag.APPEARANCE_COURSES;
	}
	
	private String getLocalizedDescription(SavedHQL.Flag flag) {
		switch (flag) {
		case APPEARANCE_COURSES:
			return MESSAGES.flagAppearanceCourses();
		case APPEARANCE_EXAMS:
			return MESSAGES.flagAppearanceExaminations();
		case APPEARANCE_EVENTS:
			return MESSAGES.flagAppearanceEvents();
		case APPEARANCE_SECTIONING:
			return MESSAGES.flagAppearanceStudentSectioning();
		case APPEARANCE_ADMINISTRATION:
			return MESSAGES.flagAppearanceAdministration();
		case ADMIN_ONLY:
			return MESSAGES.flagRestrictionsAdministratorOnly();
		default:
			return flag.description();
		}
	}
	
	private String getLocalizedText(SavedHQL.Option option) {
		switch (option) {
		case BUILDING:
			return MESSAGES.optionBuilding();
		case BUILDINGS:
			return MESSAGES.optionBuildings();
		case DEPARTMENT:
			return MESSAGES.optionDepartment();
		case DEPARTMENTS:
			return MESSAGES.optionDepartments();
		case ROOM:
			return MESSAGES.optionRoom();
		case ROOMS:
			return MESSAGES.optionRooms();
		case SESSION:
			return MESSAGES.optionAcademicSession();
		case SUBJECT:
			return MESSAGES.optionSubjectArea();
		case SUBJECTS:
			return MESSAGES.optionSubjectAreas();
		default:
			return option.text();
		}
		
	}
	
	@Override
	@PreAuthorize("checkPermission('HQLReports')")
	public Boolean setBack(String appearance, String history, List<Long> ids, String type) throws SavedHQLException, PageAccessException {
		String title = MESSAGES.pageCourseReports();
		switch (getAppearanceFlag(appearance)) {
		case APPEARANCE_COURSES:
			title = MESSAGES.pageCourseReports(); break;
		case APPEARANCE_EXAMS:
			title = MESSAGES.pageExaminationReports(); break;
		case APPEARANCE_SECTIONING:
			title = MESSAGES.pageStudentSectioningReports(); break;
		case APPEARANCE_EVENTS:
			title = MESSAGES.pageEventReports(); break;
		case APPEARANCE_ADMINISTRATION:
			title = MESSAGES.pageAdministrationReports(); break;
		}
		BackTracker.markForBack(getSessionContext(), "gwt.jsp?page=hql&appearance=" + appearance + "#" + history, title, true, true);
		if ("__Class".equals(type))
			Navigation.set(getSessionContext(), Navigation.sClassLevel, ids);
		else if ("__Offering".equals(type))
			Navigation.set(getSessionContext(), Navigation.sInstructionalOfferingLevel, ids);
		else if ("__Subpart".equals(type))
			Navigation.set(getSessionContext(), Navigation.sSchedulingSubpartLevel, ids);
		else if ("__Room".equals(type))
			Navigation.set(getSessionContext(), Navigation.sInstructionalOfferingLevel, ids);
		else if ("__Instructor".equals(type))
			Navigation.set(getSessionContext(), Navigation.sInstructionalOfferingLevel, ids);
		else if ("__Exam".equals(type))
			Navigation.set(getSessionContext(), Navigation.sInstructionalOfferingLevel, ids);
		else if ("__Event".equals(type))
			Navigation.set(getSessionContext(), Navigation.sInstructionalOfferingLevel, ids);
		return true;
	}

}
