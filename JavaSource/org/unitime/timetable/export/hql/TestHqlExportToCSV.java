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
package org.unitime.timetable.export.hql;

import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.hibernate.MappingException;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.CollectionType;
import org.hibernate.type.Type;
import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.events.EventAction.EventContext;
import org.unitime.timetable.export.BufferedPrinter;
import org.unitime.timetable.export.CSVPrinter;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.Exporter;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.gwt.shared.SavedHQLException;
import org.unitime.timetable.model.SavedHQL;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SavedHQLDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("org.unitime.timetable.export.Exporter:hql-test.csv")
public class TestHqlExportToCSV implements Exporter {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	private static Logger sLog = Logger.getLogger(TestHqlExportToCSV.class);
	
	@Override
	public String reference() {
		return "hql-test.csv";
	}

	@Override
	public void export(ExportHelper helper) throws IOException {
		Long sessionId = helper.getAcademicSessionId();
		if (sessionId == null)
			throw new IllegalArgumentException("Academic session not provided, please set the term parameter.");
		
		Session session = SessionDAO.getInstance().get(sessionId);
		if (session == null)
			throw new IllegalArgumentException("Given academic session no longer exists.");

		// Check rights
		SessionContext context = new EventContext(helper.getSessionContext(), helper.getAcademicSessionId());
		context.checkPermission(helper.getAcademicSessionId(), Right.TestHQL);
		
		// Retrive report
		String hql = helper.getParameter("hql");
		if (hql == null) throw new IllegalArgumentException("No query provided, please set the hql parameter.");
		for (SavedHQL.Option o: SavedHQL.Option.values()) {
			if (hql.indexOf("%" + o.name() + "%") >= 0) {
				String value = null;
				if (value == null || value.isEmpty()) {
					Map<Long, String> vals = o.values(helper.getSessionContext().getUser());
					if (vals == null || vals.isEmpty())
						throw new IllegalArgumentException("Unable to set parameter " + o.name() + ": no available values.");
					value = "";
					for (Long id: vals.keySet()) {
						if (!value.isEmpty()) value += ",";
						value += id.toString();
					}
				}
				hql = hql.replace("%" + o.name() + "%", "(" + value + ")");
			}
        }

		BufferedPrinter out = new BufferedPrinter(new CSVPrinter(helper.getWriter(), false));
		helper.setup(out.getContentType(), reference(), false);
		
		execute(context.getUser(), out, hql, 0, ApplicationProperty.TestHQLMaxLines.intValue());
		
		out.close();
	}
	
	public static void execute(UserContext user, Printer out, String hql, int fromRow, int maxRows) throws SavedHQLException, PageAccessException {
		try {
			org.hibernate.Session hibSession = SavedHQLDAO.getInstance().getSession();
			org.hibernate.Query q = hibSession.createQuery(hql);
			if (maxRows > 0)
				q.setMaxResults(maxRows);
			if (fromRow > 0)
				q.setFirstResult(fromRow);
			q.setCacheable(true);
			int len = -1;
			for (Object o: q.list()) {
				if (len < 0) {
					len = length(o);
					String[] line = new String[len];
					header(line, o, q.getReturnAliases());
					if (line.length > 0 && line[0].startsWith("__")) out.hideColumn(0);
					out.printHeader(line);
				}
				String[] line = new String[len];
				line(line, o, (SessionImplementor)hibSession);
				out.printLine(line);
				out.flush();
			}
		} catch (PageAccessException e) {
			throw e;
		} catch (SavedHQLException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new SavedHQLException(MESSAGES.failedExecution(e.getMessage() + (e.getCause() == null ? "" : " (" + e.getCause().getMessage() + ")")));
		}
	}
	
	private static boolean skip(Type t, boolean lazy) {
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
	
    private static int length(Object o) {
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
    
    private static String format(String column) {
    	if (column == null || column.isEmpty()) return "?";
    	return column.substring(0, 1).toUpperCase() + column.substring(1);
    }
	
	private static void header(String[] ret, Object o, String[] alias) {
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
	
	private static String toString(Object o) {
		if (o != null && o instanceof Document) return ((Document)o).asXML();
		return (o == null ? "" : o.toString());
	}
	
	private static void line(String[] ret, Object o, SessionImplementor session) {
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
		                    	ret[idx++] = toString(meta.getPropertyValue(x, meta.getPropertyNames()[i]));
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
                    	ret[idx++] = toString(meta.getPropertyValue(o, meta.getPropertyNames()[i]));
                }
			}
		}
	}

}
