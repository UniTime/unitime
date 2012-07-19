package org.unitime.timetable.gwt.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.timetable.export.Exporter.Printer;
import org.unitime.timetable.export.hql.SavedHqlExportToCSV;
import org.unitime.timetable.gwt.services.SavedHQLService;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.gwt.shared.SavedHQLException;
import org.unitime.timetable.gwt.shared.SavedHQLInterface;
import org.unitime.timetable.gwt.shared.SavedHQLInterface.Query;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.SavedHQL;
import org.unitime.timetable.model.dao.SavedHQLDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.Navigation;

@Service("hql.gwt")
public class SavedHQLServlet implements SavedHQLService {
	private static Logger sLog = Logger.getLogger(SavedHQLServlet.class);
	
	private @Autowired SessionContext sessionContext;
	private SessionContext getSessionContext() { return sessionContext; }

	@Override
	public List<SavedHQLInterface.Flag> getFlags() throws SavedHQLException, PageAccessException {
		UserContext user = getSessionContext().getUser();
		if (user == null) throw new PageAccessException(
				getSessionContext().isHttpSessionNew() ? "Your timetabling session has expired. Please log in again." : "Login is required to use this page.");
		if (user.getCurrentRole() == null) throw new PageAccessException("Insufficient user privileges.");
		List<SavedHQLInterface.Flag> ret = new ArrayList<SavedHQLInterface.Flag>();
		for (SavedHQL.Flag f: SavedHQL.Flag.values()) {
			SavedHQLInterface.Flag flag = new SavedHQLInterface.Flag();
			flag.setValue(f.flag());
			flag.setText(f.description());
			ret.add(flag);
		}
		return ret;
	}

	@Override
	public List<SavedHQLInterface.Option> getOptions() throws SavedHQLException, PageAccessException {
		UserContext user = getSessionContext().getUser();
		if (user == null) throw new PageAccessException(
				getSessionContext().isHttpSessionNew() ? "Your timetabling session has expired. Please log in again." : "Login is required to use this page.");
		if (user.getCurrentRole() == null) throw new PageAccessException("Insufficient user privileges.");
		List<SavedHQLInterface.Option> ret  = new ArrayList<SavedHQLInterface.Option>();
		for (SavedHQL.Option o: SavedHQL.Option.values()) {
			if (!o.allowSingleSelection() && !o.allowMultiSelection()) continue;
			SavedHQLInterface.Option option = new SavedHQLInterface.Option();
			option.setMultiSelect(o.allowMultiSelection());
			option.setName(o.text());
			option.setType(o.name());
			Map<Long, String> values = o.values(user);
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
	public Boolean editable() throws SavedHQLException, PageAccessException {
		UserContext user = getSessionContext().getUser();
		return (user != null && Roles.ADMIN_ROLE.equals(user.getCurrentRole()));
	}

	@Override
	public List<SavedHQLInterface.Query> queries(String appearance) throws SavedHQLException, PageAccessException {
		UserContext user = getSessionContext().getUser();
		if (user == null) throw new PageAccessException(
				getSessionContext().isHttpSessionNew() ? "Your timetabling session has expired. Please log in again." : "Login is required to use this page.");
		if (user.getCurrentRole() == null) throw new PageAccessException("Insufficient user privileges.");
		SavedHQL.Flag ap = SavedHQL.Flag.valueOf("APPEARANCE_" + (appearance == null ? "ADMINISTRATION" : appearance.toUpperCase()));
		List<SavedHQLInterface.Query> ret = new ArrayList<SavedHQLInterface.Query>(); 
		for (SavedHQL hql: SavedHQL.listAll(null, ap, Roles.ADMIN_ROLE.equals(user.getCurrentRole()))) {
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
	public List<String[]> execute(SavedHQLInterface.Query query, List<SavedHQLInterface.IdValue> options, int fromRow, int maxRows) throws SavedHQLException, PageAccessException {
		try {
			UserContext user = getSessionContext().getUser();
			if (user == null) throw new PageAccessException(
					getSessionContext().isHttpSessionNew() ? "Your timetabling session has expired. Please log in again." : "Login is required to use this page.");
			if (user.getCurrentRole() == null) throw new PageAccessException("Insufficient user privileges.");

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
			
			SavedHqlExportToCSV.execute(user, out, query, options, fromRow, maxRows);
		
			return ret;
		} catch (PageAccessException e) {
			throw e;
		} catch (SavedHQLException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new SavedHQLException("Execution failed: " + e.getMessage() + (e.getCause() == null ? "" : " (" + e.getCause().getMessage() + ")"));
		}
	}
	
	@Override
	public Long store(Query query) throws SavedHQLException, PageAccessException {
		UserContext user = getSessionContext().getUser();
		if (user == null) throw new PageAccessException(
				getSessionContext().isHttpSessionNew() ? "Your timetabling session has expired. Please log in again." : "Login is required to use this page.");
		if (user.getCurrentRole() == null) throw new PageAccessException("Insufficient user privileges.");
		if (!Roles.ADMIN_ROLE.equals(user.getCurrentRole())) throw new PageAccessException("Only administrator can save a query.");
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
	public Boolean delete(Long id) throws SavedHQLException, PageAccessException {
		if (id == null) throw new SavedHQLException("No report provided.");
		UserContext user = getSessionContext().getUser();
		if (user == null) throw new PageAccessException(
				getSessionContext().isHttpSessionNew() ? "Your timetabling session has expired. Please log in again." : "Login is required to use this page.");
		if (user.getCurrentRole() == null) throw new PageAccessException("Insufficient user privileges.");
		if (!Roles.ADMIN_ROLE.equals(user.getCurrentRole())) throw new PageAccessException("Only administrator can delete a query.");
		org.hibernate.Session hibSession = SavedHQLDAO.getInstance().getSession();
		SavedHQL hql = SavedHQLDAO.getInstance().get(id, hibSession);
		if (hql != null) {
			hibSession.delete(hql);
			hibSession.flush();
		}
		return hql != null;
	}
	
	@Override
	public Boolean setBack(String appearance, String history, List<Long> ids, String type) throws SavedHQLException, PageAccessException {
		String title = "Reports";
		switch (SavedHQL.Flag.valueOf("APPEARANCE_" + (appearance == null ? "ADMINISTRATION" : appearance.toUpperCase()))) {
		case APPEARANCE_COURSES:
			title = "Course Reports"; break;
		case APPEARANCE_EXAMS:
			title = "Examination Reports"; break;
		case APPEARANCE_SECTIONING:
			title = "Student Sectioning Reports"; break;
		case APPEARANCE_EVENTS:
			title = "Event Reports"; break;
		case APPEARANCE_ADMINISTRATION:
			title = "Administration Reports"; break;
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
