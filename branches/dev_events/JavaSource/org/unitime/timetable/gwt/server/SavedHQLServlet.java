package org.unitime.timetable.gwt.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.hibernate.EntityMode;
import org.hibernate.MappingException;
import org.hibernate.SessionFactory;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.CollectionType;
import org.hibernate.type.Type;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.gwt.services.SavedHQLService;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.gwt.shared.SavedHQLException;
import org.unitime.timetable.gwt.shared.SavedHQLInterface;
import org.unitime.timetable.gwt.shared.SavedHQLInterface.Query;
import org.unitime.timetable.model.SavedHQL;
import org.unitime.timetable.model.dao.SavedHQLDAO;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.Navigation;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class SavedHQLServlet extends RemoteServiceServlet implements SavedHQLService {
	private static final long serialVersionUID = -5724832564531363833L;
	private static Logger sLog = Logger.getLogger(SavedHQLServlet.class);

	@Override
	public List<SavedHQLInterface.Flag> getFlags() throws SavedHQLException, PageAccessException {
		User user = Web.getUser(getThreadLocalRequest().getSession());
		if (user == null) throw new PageAccessException(
				getThreadLocalRequest().getSession().isNew() ? "Your timetabling session has expired. Please log in again." : "Login is required to use this page.");
		if (user.getRole() == null) throw new PageAccessException("Insufficient user privileges.");
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
		User user = Web.getUser(getThreadLocalRequest().getSession());
		if (user == null) throw new PageAccessException(
				getThreadLocalRequest().getSession().isNew() ? "Your timetabling session has expired. Please log in again." : "Login is required to use this page.");
		if (user.getRole() == null) throw new PageAccessException("Insufficient user privileges.");
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
		User user = Web.getUser(getThreadLocalRequest().getSession());
		return (user != null && user.isAdmin());
	}

	@Override
	public List<SavedHQLInterface.Query> queries(String appearance) throws SavedHQLException, PageAccessException {
		User user = Web.getUser(getThreadLocalRequest().getSession());
		if (user == null) throw new PageAccessException(
				getThreadLocalRequest().getSession().isNew() ? "Your timetabling session has expired. Please log in again." : "Login is required to use this page.");
		if (user.getRole() == null) throw new PageAccessException("Insufficient user privileges.");
		SavedHQL.Flag ap = SavedHQL.Flag.valueOf("APPEARANCE_" + (appearance == null ? "ADMINISTRATION" : appearance.toUpperCase()));
		List<SavedHQLInterface.Query> ret = new ArrayList<SavedHQLInterface.Query>(); 
		for (SavedHQL hql: SavedHQL.listAll(null, ap, user.isAdmin())) {
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
			User user = Web.getUser(getThreadLocalRequest().getSession());
			if (user == null) throw new PageAccessException(
					getThreadLocalRequest().getSession().isNew() ? "Your timetabling session has expired. Please log in again." : "Login is required to use this page.");
			if (user.getRole() == null) throw new PageAccessException("Insufficient user privileges.");
			String hql = query.getQuery();
			for (SavedHQL.Option o: SavedHQL.Option.values()) {
				if (hql.indexOf("%" + o.name() + "%") >= 0) {
					String value = null;
					for (SavedHQLInterface.IdValue v: options)
						if (o.name().equals(v.getValue())) { value = v.getText(); break; }
					if (value == null || value.isEmpty()) {
						Map<Long, String> vals = o.values(user);
						if (vals == null || vals.isEmpty())
							throw new SavedHQLException("Unable to set parameter " + o.name() + ": no available values.");
						value = "";
						for (Long id: vals.keySet()) {
							if (!value.isEmpty()) value += ",";
							value += id.toString();
						}
					}
					hql = hql.replace("%" + o.name() + "%", "(" + value + ")");
				}
			}
			org.hibernate.Session hibSession = SavedHQLDAO.getInstance().getSession();
			org.hibernate.Query q = hibSession.createQuery(hql);
			if (maxRows > 0)
				q.setMaxResults(maxRows);
			if (fromRow > 0)
				q.setFirstResult(fromRow);
			q.setCacheable(true);
			List<String[]> ret = new ArrayList<String[]>();
			int len = -1;
			for (Object o: q.list()) {
				if (len < 0) {
					len = length(o);
					String[] line = new String[len];
					header(line, o, q.getReturnAliases());
					ret.add(line);
				}
				String[] line = new String[len];
				line(line, o, (SessionImplementor)hibSession);
				ret.add(line);
			}
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
	
    private boolean skip(Type t, boolean lazy) {
        try {
            if (t.isCollectionType()) {
                if (!lazy) return true;
                SessionFactory hibSessionFactory = new _RootDAO().getSession().getSessionFactory();
                Type w = ((CollectionType)t).getElementType((SessionFactoryImplementor)hibSessionFactory);
                Class ts = w.getReturnedClass().getMethod("toString", new Class[]{}).getDeclaringClass();
                return (ts.equals(Object.class) || ts.getName().startsWith("org.unitime.timetable.model.base.Base"));
            }
        } catch (MappingException e) {
            return true;
        } catch (NoSuchMethodException e) {
            return true;
        }
        try {
            Class ts = t.getReturnedClass().getMethod("toString", new Class[]{}).getDeclaringClass();
            return (ts.equals(Object.class) || ts.getName().startsWith("org.unitime.timetable.model.base.Base"));
        } catch (NoSuchMethodException e) {
            return true;
        }
    }
    
    private int length(Object o) {
    	if (o == null) return 1;
    	int len = 0;
    	if (o instanceof Object[]) {
    		for (Object x: (Object[])o) {
    			if (x == null) {
    				len++;
    			} else {
            		ClassMetadata meta = SavedHQLDAO.getInstance().getSession().getSessionFactory().getClassMetadata(x.getClass());
            		if (meta == null) {
            			len++;
            		} else {
            			if (meta.getIdentifierPropertyName() != null) len++;
            			for (int i=0;i<meta.getPropertyNames().length;i++) {
                            if (!skip(meta.getPropertyTypes()[i], meta.getPropertyLaziness()[i]))
                            	len++;
            			}
            		}
    			}
    		}
    	} else {
    		ClassMetadata meta = SavedHQLDAO.getInstance().getSession().getSessionFactory().getClassMetadata(o.getClass());
    		if (meta == null) {
    			len++;
    		} else {
    			if (meta.getIdentifierPropertyName() != null) len++;
    			for (int i=0;i<meta.getPropertyNames().length;i++) {
                    if (!skip(meta.getPropertyTypes()[i], meta.getPropertyLaziness()[i]))
                    	len++;
    			}
    		}
    	}
    	return len;
    }
    
    private String format(String column) {
    	if (column == null || column.isEmpty()) return "?";
    	return column.substring(0, 1).toUpperCase() + column.substring(1);
    }
	
	private void header(String[] ret, Object o, String[] alias) {
		if (o == null) {
			if (alias != null && alias.length >= 1 && alias[0] != null && !alias[0].isEmpty())
				ret[0] = alias[0];
			else
				ret[0] = "Result";
		} else if (o instanceof Object[]) {
			int a = 0, idx = 0;
			for (Object x: (Object[])o) {
				if (x == null) {
					if (alias != null && alias.length > a && alias[a] != null && !alias[a].isEmpty())
						ret[idx++] = alias[a];
					else
						ret[idx++] = "Column" + (a + 1);
				} else {
					ClassMetadata meta = SavedHQLDAO.getInstance().getSession().getSessionFactory().getClassMetadata(x.getClass());
					if (meta == null) {
						if (alias != null && alias.length > a && alias[a] != null && !alias[a].isEmpty())
							ret[idx++] = alias[a];
						else
							ret[idx++] = "Column" + (a + 1);
					} else {
						if (meta.getIdentifierPropertyName() != null)
							ret[idx++] = meta.getIdentifierPropertyName();
		                for (int i = 0; i < meta.getPropertyNames().length; i ++) {
		                    if (!skip(meta.getPropertyTypes()[i], meta.getPropertyLaziness()[i]))
		                    	ret[idx++] = format(meta.getPropertyNames()[i]);
		                }
					}
				}
				a++;					
			}
		} else {
			ClassMetadata meta = SavedHQLDAO.getInstance().getSession().getSessionFactory().getClassMetadata(o.getClass());
			if (meta == null) {
				if (alias != null && alias.length >= 1 && alias[0] != null && !alias[0].isEmpty())
					ret[0] = alias[0];
				else
					ret[0] = "Result";
			} else {
				int idx = 0;
				if (meta.getIdentifierPropertyName() != null)
					ret[idx++] = meta.getIdentifierPropertyName();
                for (int i = 0; i < meta.getPropertyNames().length; i ++) {
                    if (!skip(meta.getPropertyTypes()[i], meta.getPropertyLaziness()[i]))
                    	ret[idx++] = format(meta.getPropertyNames()[i]);
                }
			}
		}
	}
	
	private String toString(Object o) {
		if (o != null && o instanceof Document) return ((Document)o).asXML();
		return (o == null ? "" : o.toString());
	}
	
	private void line(String[] ret, Object o, SessionImplementor session) {
		if (o == null) {
			ret[0] = "";
		} else if (o instanceof Object[]) {
			int idx = 0;
			for (Object x: (Object[])o) {
				if (x == null) {
					ret[idx++] = "";
				} else {
					ClassMetadata meta = SavedHQLDAO.getInstance().getSession().getSessionFactory().getClassMetadata(x.getClass());
					if (meta == null) {
						ret[idx++] = toString(x);
					} else {
						if (meta.getIdentifierPropertyName() != null)
							ret[idx++] = toString(meta.getIdentifier(x, session));
		                for (int i = 0; i < meta.getPropertyNames().length; i ++) {
		                    if (!skip(meta.getPropertyTypes()[i], meta.getPropertyLaziness()[i]))
		                    	ret[idx++] = toString(meta.getPropertyValue(x, meta.getPropertyNames()[i], EntityMode.POJO));
		                }
					}
				}
			}
		} else {
			ClassMetadata meta = SavedHQLDAO.getInstance().getSession().getSessionFactory().getClassMetadata(o.getClass());
			if (meta == null) {
				ret[0] = toString(o);
			} else {
				int idx = 0;
				if (meta.getIdentifierPropertyName() != null)
					ret[idx++] = toString(meta.getIdentifier(o, session));
                for (int i = 0; i < meta.getPropertyNames().length; i ++) {
                    if (!skip(meta.getPropertyTypes()[i], meta.getPropertyLaziness()[i]))
                    	ret[idx++] = toString(meta.getPropertyValue(o, meta.getPropertyNames()[i], EntityMode.POJO));
                }
			}
		}
	}

	@Override
	public Long store(Query query) throws SavedHQLException, PageAccessException {
		User user = Web.getUser(getThreadLocalRequest().getSession());
		if (user == null) throw new PageAccessException(
				getThreadLocalRequest().getSession().isNew() ? "Your timetabling session has expired. Please log in again." : "Login is required to use this page.");
		if (user.getRole() == null) throw new PageAccessException("Insufficient user privileges.");
		if (!user.isAdmin()) throw new PageAccessException("Only administrator can save a query.");
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
		User user = Web.getUser(getThreadLocalRequest().getSession());
		if (user == null) throw new PageAccessException(
				getThreadLocalRequest().getSession().isNew() ? "Your timetabling session has expired. Please log in again." : "Login is required to use this page.");
		if (user.getRole() == null) throw new PageAccessException("Insufficient user privileges.");
		if (!user.isAdmin()) throw new PageAccessException("Only administrator can delete a query.");
		org.hibernate.Session hibSession = SavedHQLDAO.getInstance().getSession();
		SavedHQL hql = SavedHQLDAO.getInstance().get(id, hibSession);
		if (hql != null) {
			hibSession.delete(hql);
			hibSession.flush();
		}
		return hql != null;
	}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if ("1".equals(request.getParameter("csv")) && request.getParameter("report") != null) {
			SavedHQL hql = SavedHQLDAO.getInstance().get(Long.valueOf(request.getParameter("report")));
			if (hql == null) throw new SavedHQLException("No report provided.");
			SavedHQLInterface.Query q = new SavedHQLInterface.Query();
			q.setName(hql.getName());
			q.setId(hql.getUniqueId());
			q.setQuery(hql.getQuery());
			q.setFlags(hql.getType());
			q.setDescription(hql.getDescription());
			List<SavedHQLInterface.IdValue> params = new ArrayList<SavedHQLInterface.IdValue>();
			if (request.getParameter("params") != null) {
				String[] p = request.getParameter("params").split(":");
				int i = 0;
				for (SavedHQL.Option o: SavedHQL.Option.values()) {
					if (!o.allowSingleSelection() && !o.allowMultiSelection()) continue;
					SavedHQLInterface.IdValue v = new SavedHQLInterface.IdValue();
					v.setValue(o.name());
					v.setText(i < p.length ? p[i] : "");
					params.add(v);
					i++;
				}
			}
			perThreadRequest.set(request);
			perThreadResponse.set(response);
			List<String[]> report = execute(q, params, 0, -1);
			
			response.setContentType("application/csv; charset=UTF-8");
			response.setCharacterEncoding("UTF-8");
			response.setHeader( "Content-Disposition", "attachment; filename=\"" + q.getName() + ".csv\"" );
	        
			PrintWriter out = response.getWriter();
			try {
				boolean skipFirstColumn = false;
				for (int i = 0; i < report.size(); i++) {
					String[] line = report.get(i);
					for (int j = 0; j < line.length; j++) {
						String field = (line[j] == null ? "" : line[j]);
						if (i == 0 && j == 0 && field.startsWith("__")) skipFirstColumn = true;
						if (skipFirstColumn && j == 0) continue;
						if (i == 0) field = field.replace('_', ' ').trim();
						out.print((j > (skipFirstColumn ? 1 : 0) ? "," : "") + "\"" + field.replace("\"", "\"\"") + "\"");
					}
					out.println();
				}
				out.flush();
			} finally {
				out.close();
			}
		} else {
			super.doGet(request, response);
		}
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
		BackTracker.markForBack(getThreadLocalRequest(), "gwt.jsp?page=hql&appearance=" + appearance + "#" + history, title, true, true);
		if ("__Class".equals(type))
			Navigation.set(getThreadLocalRequest().getSession(), Navigation.sClassLevel, ids);
		else if ("__Offering".equals(type))
			Navigation.set(getThreadLocalRequest().getSession(), Navigation.sInstructionalOfferingLevel, ids);
		else if ("__Subpart".equals(type))
			Navigation.set(getThreadLocalRequest().getSession(), Navigation.sSchedulingSubpartLevel, ids);
		else if ("__Room".equals(type))
			Navigation.set(getThreadLocalRequest().getSession(), Navigation.sInstructionalOfferingLevel, ids);
		else if ("__Instructor".equals(type))
			Navigation.set(getThreadLocalRequest().getSession(), Navigation.sInstructionalOfferingLevel, ids);
		else if ("__Exam".equals(type))
			Navigation.set(getThreadLocalRequest().getSession(), Navigation.sInstructionalOfferingLevel, ids);
		else if ("__Event".equals(type))
			Navigation.set(getThreadLocalRequest().getSession(), Navigation.sInstructionalOfferingLevel, ids);
		return true;
	}

}
