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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.dom4j.Document;
import org.springframework.stereotype.Service;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.events.EventAction.EventContext;
import org.unitime.timetable.export.BufferedPrinter;
import org.unitime.timetable.export.CSVPrinter;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.Exporter;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.gwt.shared.SavedHQLException;
import org.unitime.timetable.gwt.shared.SavedHQLInterface;
import org.unitime.timetable.model.SavedHQL;
import org.unitime.timetable.model.SavedHQLParameter;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SavedHQLDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.AccessDeniedException;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller
 */
@Service("org.unitime.timetable.export.Exporter:hql-report.csv")
public class SavedHqlExportToCSV implements Exporter {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	private static Log sLog = LogFactory.getLog(SavedHqlExportToCSV.class);
	
	@Override
	public String reference() {
		return "hql-report.csv";
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
		context.checkPermission(helper.getAcademicSessionId(), Right.HQLReports);
		
		// Retrive report
		String report = helper.getParameter("report");
		if (report == null) throw new IllegalArgumentException("No report provided, please set the report parameter.");
		SavedHQL hql = null;
		try {
			hql = SavedHQLDAO.getInstance().get(Long.valueOf(report));
		} catch (NumberFormatException e) {}
		if (hql == null)
			hql = SavedHQLDAO.getInstance().getSession().createQuery("from SavedHQL where name = :name", SavedHQL.class).setParameter("name", report).setMaxResults(1).uniqueResult();
		if (hql == null) throw new IllegalArgumentException("Report " + report + " does not exist.");
				
		List<SavedHQLInterface.IdValue> params = new ArrayList<SavedHQLInterface.IdValue>();
		if (helper.getParameter("params") != null) {
			String[] p = helper.getParameter("params").split(":");
			int i = 0;
			for (SavedHQL.Option o: SavedHQL.Option.values()) {
				if (!o.allowSingleSelection() && !o.allowMultiSelection()) continue;
				if (hql.getQuery().contains("%" + o.name() + "%")) {
					SavedHQLInterface.IdValue v = new SavedHQLInterface.IdValue();
					v.setValue(o.name());
					v.setText(i < p.length ? p[i] : "");
					params.add(v);
					i++;
				}
			}
		}
		for (SavedHQL.Option option: SavedHQL.Option.values()) {
			if (!hql.getQuery().contains("%" + option.name() + "%")) continue;
			if (option.allowMultiSelection()) {
				String[] values = helper.getParameterValues(option.name());
				if (values != null && values.length > 0) {
					SavedHQLInterface.IdValue v = new SavedHQLInterface.IdValue();
					v.setValue(option.name());
					String text = "";
					for (String value: values) {
						Long id = option.lookupValue(context.getUser(), value);
						if (id == null) {
							try {
								id = Long.valueOf(value);
							} catch (NumberFormatException e) {}
						}
						if (id != null)
							text += (text.isEmpty() ? "" : ",") + id;
					}
					v.setText(text);
					params.add(v);
				}
			} else {
				String value = helper.getParameter(option.name());
				if (value != null) {
					SavedHQLInterface.IdValue v = new SavedHQLInterface.IdValue();
					v.setValue(option.name());
					Long id = option.lookupValue(context.getUser(), value);
					if (id == null) {
						try {
							id = Long.valueOf(value);
						} catch (NumberFormatException e) {}
					}
					v.setText(id == null ? "" : id.toString());
					params.add(v);
				}
			}
		}
		for (SavedHQLParameter p: hql.getParameters()) {
			SavedHQLInterface.IdValue v = new SavedHQLInterface.IdValue();
			v.setValue(p.getName());
			String value = helper.getParameter(p.getName());
			v.setText(value == null ? p.getDefaultValue() : value);
			params.add(v);
		}
		
		boolean hasAppearancePermission = false;
		for (SavedHQL.Flag flag: SavedHQL.Flag.values()) {
			if (hql.isSet(flag)) {
				if (flag.getAppearance() != null && !hasAppearancePermission && (flag.getPermission() == null || context.hasPermission(flag.getPermission())))
					hasAppearancePermission = true;
				if (flag.getAppearance() == null && flag.getPermission() != null)
					context.checkPermission(flag.getPermission());
			}
		}
		if (!hasAppearancePermission) throw new AccessDeniedException();
		
		BufferedPrinter out = new BufferedPrinter(new CSVPrinter(helper, false));
		helper.setup(out.getContentType(), hql.getName().replace('/', '-').replace('\\', '-').replace(':', '-') + ".csv", false);
		
		execute(context.getUser(), out, hql.getQuery(), params, 0, -1, hql.getParameters());
		
		String sort = helper.getParameter("sort");
		if (sort != null && !"0".equals(sort)) {
			final boolean asc = Integer.parseInt(sort) > 0;
			final int col = Math.abs(Integer.parseInt(sort)) - 1;
			Collections.sort(out.getBuffer(), new Comparator<String[]>() {
				int compare(String[] a, String[] b, int col) {
					for (int i = 0; i < a.length; i++) {
						int c = (col + i) % a.length;
						try {
							int cmp = Double.valueOf(a[c] == null ? "0" : a[c]).compareTo(Double.valueOf(b[c] == null ? "0" : b[c]));
							if (cmp != 0) return cmp;
						} catch (NumberFormatException e) {
							int cmp = (a[c] == null ? "" : a[c]).compareTo(b[c] == null ? "" : b[c]);
							if (cmp != 0) return cmp;
						}
					}
					return 0;
				}	
				@Override
				public int compare(String[] a, String[] b) {
					return asc ? compare(a, b, col) : compare(b, a, col);
				}
			});
		}
		
		out.close();
	}
	
	public static void execute(UserContext user, Printer out, String hql, List<SavedHQLInterface.IdValue> options, int fromRow, int maxRows, Collection<SavedHQLParameter> parameters) throws SavedHQLException, PageAccessException {
		try {
			for (SavedHQL.Option o: SavedHQL.Option.values()) {
				if (hql.indexOf("%" + o.name() + "%") >= 0) {
					String value = null;
					for (SavedHQLInterface.IdValue v: options)
						if (o.name().equals(v.getValue())) { value = v.getText(); break; }
					if (value == null || value.isEmpty()) {
						Map<Long, String> vals = o.values(user);
						if (vals == null || vals.isEmpty())
							throw new SavedHQLException(MESSAGES.errorUnableToSetParameterNoValues(o.name()));
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
				hql = hql.replace("%USER%", HibernateUtil.escapeSql(user.getExternalUserId()));
			org.hibernate.Session hibSession = SavedHQLDAO.getInstance().getSession();
			org.hibernate.query.Query<Tuple> q = hibSession.createQuery(hql, Tuple.class);
			if (maxRows > 0)
				q.setMaxResults(maxRows);
			if (fromRow > 0)
				q.setFirstResult(fromRow);
			q.setCacheable(true);
			if (parameters != null && !parameters.isEmpty()) {
				parameters: for (SavedHQLParameter parameter: parameters) {
					String value = parameter.getDefaultValue();
					for (SavedHQLInterface.IdValue v: options)
						if (parameter.getName().equals(v.getValue())) { value = v.getText(); break; }
					if (parameter.getType().equalsIgnoreCase("boolean")) {
						q.setParameter(parameter.getName(), value == null ? null : Boolean.valueOf("true".equalsIgnoreCase(value)));
					} else if (parameter.getType().equalsIgnoreCase("long")) {
						q.setParameter(parameter.getName(), value == null || value.isEmpty() ? null : Long.valueOf(value));
					} else if (parameter.getType().equalsIgnoreCase("int") || parameter.getType().equalsIgnoreCase("integer") || parameter.getType().equalsIgnoreCase("slot") || parameter.getType().equalsIgnoreCase("time")) {
						q.setParameter(parameter.getName(), value == null || value.isEmpty() ? null : Integer.valueOf(value));
					} else if (parameter.getType().equalsIgnoreCase("double")) {
						q.setParameter(parameter.getName(), value == null || value.isEmpty() ? null : Double.valueOf(value));
					} else if (parameter.getType().equalsIgnoreCase("float")) {
						q.setParameter(parameter.getName(), value == null || value.isEmpty() ? null : Float.valueOf(value));
					} else if (parameter.getType().equalsIgnoreCase("short")) {
						q.setParameter(parameter.getName(), value == null || value.isEmpty() ? null : Short.valueOf(value));
					} else if (parameter.getType().equalsIgnoreCase("byte")) {
						q.setParameter(parameter.getName(), value == null || value.isEmpty() ? null : Byte.valueOf(value));
					} else if (parameter.getType().equalsIgnoreCase("date")) {
						Formats.Format<Date> dateFormat = Formats.getDateFormat(Formats.Pattern.DATE_EVENT);
						q.setParameter(parameter.getName(), value == null || value.isEmpty() ? null : dateFormat.parse(value));
					} else if (parameter.getType().equalsIgnoreCase("datetime") || parameter.getType().equalsIgnoreCase("timestamp")) {
						Formats.Format<Date> dateFormat = Formats.getDateFormat(Formats.Pattern.DATE_TIME_STAMP);
						q.setParameter(parameter.getName(), value == null || value.isEmpty() ? null : dateFormat.parse(value));
					} else {
						for (SavedHQL.Option option: SavedHQL.Option.values()) {
							if (parameter.getType().equalsIgnoreCase(option.name())) {
								if (option.allowMultiSelection()) {
									List<Long> ids = new ArrayList<Long>();
									if (value != null && !value.isEmpty()) {
										for (String idStr: value.split(",")) {
											try {
												ids.add(Long.parseLong(idStr));
											} catch (NumberFormatException e) {
												Long id = option.lookupValue(user, idStr);
												if (id != null) ids.add(id);
											}
										}
									} else {
										Map<Long, String> vals = option.values(user);
										if (vals != null)
											ids.addAll(vals.keySet());
									}
									q.setParameterList(parameter.getName(), ids);
								} else {
									Long id = null;
									try {
										id = Long.parseLong(value);
									} catch (NumberFormatException e) {
										id  =  option.lookupValue(user, value);
									}
									q.setParameter(parameter.getName(), id == null ? -1l : id);
								}
								continue parameters;
							}
						}
						q.setParameter(parameter.getName(), value);
					}
				}
			}
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
    			len++;
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
