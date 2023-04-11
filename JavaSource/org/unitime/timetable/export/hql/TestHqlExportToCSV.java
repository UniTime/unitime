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
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeSet;

import javax.persistence.Tuple;
import javax.persistence.TupleElement;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.dom4j.Document;
import org.springframework.stereotype.Service;
import org.unitime.commons.hibernate.util.HibernateUtil;
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
	private static Log sLog = LogFactory.getLog(TestHqlExportToCSV.class);
	
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
        if (hql.indexOf("%USER%") >= 0)
        	hql = hql.replace("%USER%", HibernateUtil.escapeSql(helper.getSessionContext().getUser().getExternalUserId()));

		BufferedPrinter out = new BufferedPrinter(new CSVPrinter(helper, false));
		helper.setup(out.getContentType(), reference(), false);
		
		execute(context.getUser(), out, hql, 0, ApplicationProperty.TestHQLMaxLines.intValue());
		
		out.close();
	}
	
	public static void execute(UserContext user, Printer out, String hql, int fromRow, int maxRows) throws SavedHQLException, PageAccessException {
		try {
			org.hibernate.Session hibSession = SavedHQLDAO.getInstance().getSession();
			org.hibernate.query.Query<Tuple> q = hibSession.createQuery(hql, Tuple.class);
			if (maxRows > 0)
				q.setMaxResults(maxRows);
			if (fromRow > 0)
				q.setFirstResult(fromRow);
			q.setCacheable(true);
			int len = -1;
			for (Tuple o: q.list()) {
				if (len < 0) {
					len = length(o);
					String[] line = new String[len];
					header(line, o);
					if (line.length > 0 && line[0].startsWith("__")) out.hideColumn(0);
					out.printHeader(line);
				}
				String[] line = new String[len];
				line(line, o);
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
	
	private static boolean skip(Attribute t) {
        try {
            Class ts = t.getJavaType().getMethod("toString", new Class[]{}).getDeclaringClass();
            return (ts.equals(Object.class) || ts.getName().startsWith("org.unitime.timetable.model.base.Base"));
        } catch (NoSuchMethodException e) {
            return true;
        }
    }
	
    private static int length(Tuple o) {
    	if (o == null) return 1;
    	int len = 0;
    	for (TupleElement te: o.getElements()) {
    		Object x = o.get(te);
    		if (x == null) {
    			len ++;
    		} else {
            	EntityType et = null;
            	try {
            		et = new _RootDAO().getSession().getMetamodel().entity(x.getClass());
            	} catch (IllegalArgumentException e) {}
            	if (et == null) {
            		len++;
            	} else {
            		TreeSet<Attribute> attributes = new TreeSet<Attribute>(new AttributeComparator());
            		attributes.addAll(et.getSingularAttributes());
            		for (Attribute sa: attributes) {
            			if (!skip(sa))
            				len++;
            		}
            	}
    		}
    	}
    	return len;
    }
    
    private static String format(String column) {
    	if (column == null || column.isEmpty()) return "?";
    	return column.substring(0, 1).toUpperCase() + column.substring(1);
    }
	
    private static void header(String[] ret, Tuple o) {
		int idx = 0;
		for (TupleElement te: o.getElements()) {
        	Object x = o.get(te);
        	if (x == null) {
        		if (te.getAlias() == null || te.getAlias().isEmpty()) {
        			if (o.getElements().size() == 1)
        				ret[idx++] = "Result";
        			else
        				ret[idx++] = "Column" + (idx ++);
        		} else
        			ret[idx++] = te.getAlias();
    		} else {
            	EntityType et = null;
            	try {
            		et = new _RootDAO().getSession().getMetamodel().entity(x.getClass());
            	} catch (IllegalArgumentException e) {}
            	if (et == null) {
            		if (te.getAlias() == null || te.getAlias().isEmpty()) {
            			if (o.getElements().size() == 1)
            				ret[idx++] = "Result";
            			else
            				ret[idx++] = "Column" + (idx ++);
            		} else
            			ret[idx++] = te.getAlias();
            	} else {
            		TreeSet<Attribute> attributes = new TreeSet<Attribute>(new AttributeComparator());
            		attributes.addAll(et.getSingularAttributes());
            		for (Attribute sa: attributes) {
            			if (!skip(sa))
            				ret[idx++] = format(sa.getName());
            		}
            	}
    		}
        }
	}
	
	private static String toString(Object o) {
		if (o != null && o instanceof Document) return ((Document)o).asXML();
		return (o == null ? "" : o.toString());
	}
	
	private static void line(String[] ret, Tuple o) {
		int idx = 0;
		for (TupleElement te: o.getElements()) {
        	Object x = o.get(te);
        	if (x == null) {
        		ret[idx++] = "";
        	} else {
            	EntityType et = null;
            	try {
            		et = new _RootDAO().getSession().getMetamodel().entity(x.getClass());
            	} catch (IllegalArgumentException e) {}
            	if (et == null) {
            		ret[idx++] = toString(x);
            	} else {
            		TreeSet<Attribute> attributes = new TreeSet<Attribute>(new AttributeComparator());
            		attributes.addAll(et.getSingularAttributes());
            		for (Attribute sa: attributes) {
            			if (!skip(sa))
                			try {
                				ret[idx++] = toString(((Method)sa.getJavaMember()).invoke(x));
                			} catch (Exception e) {
                				ret[idx++] = "";
                			}
            		}
            	}
        	}
        }
	}

	static class AttributeComparator implements Comparator<Attribute> {
		@Override
		public int compare(Attribute a1, Attribute a2) {
			boolean id1 = (a1 instanceof SingularAttribute && ((SingularAttribute)a1).isId());
			boolean id2 = (a2 instanceof SingularAttribute && ((SingularAttribute)a2).isId());
			if (id1 != id2) return (id1 ? -1 : 1);
			return a1.getName().compareTo(a2.getName());
		}
	}
}
