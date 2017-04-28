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
package org.unitime.timetable.export.pointintimedata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.MappingException;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.CollectionType;
import org.hibernate.type.Type;
import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.events.EventAction.EventContext;
import org.unitime.timetable.export.BufferedPrinter;
import org.unitime.timetable.export.CSVPrinter;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.Exporter;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.gwt.shared.PointInTimeDataReportsException;
import org.unitime.timetable.gwt.shared.PointInTimeDataReportsInterface;
import org.unitime.timetable.model.dao.SavedHQLDAO;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.reports.pointintimedata.BasePointInTimeDataReports;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Stephanie Schluttenhofer
 */
@Service("org.unitime.timetable.export.Exporter:pitd-report.csv")
public class PointInTimeDataReportsExportToCSV implements Exporter {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	private static Logger sLog = Logger.getLogger(PointInTimeDataReportsExportToCSV.class);
	
	@Override
	public String reference() {
		return "pidt-report.csv";
	}

	@Override
	public void export(ExportHelper helper) throws IOException {
		// Check rights
		SessionContext context = new EventContext(helper.getSessionContext(), helper.getAcademicSessionId());
		context.checkPermission(helper.getAcademicSessionId(), Right.PointInTimeDataReports);

		// Retrieve report
		String report = helper.getParameter("report");
		if (report == null) throw new IllegalArgumentException("No report provided, please set the report parameter.");
		Class rptCls = BasePointInTimeDataReports.sPointInTimeDataReportRegister.get(report);
		BasePointInTimeDataReports rpt = null;
		try {
			rpt = (BasePointInTimeDataReports) rptCls.newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (rpt == null) throw new IllegalArgumentException("Report " + report + " does not exist.");
		
		PointInTimeDataReportsInterface.Report r = new PointInTimeDataReportsInterface.Report();
		r.setName(rpt.reportName());
		r.setId(report);
		r.setDescription(rpt.reportDescription());
		
		List<PointInTimeDataReportsInterface.IdValue> params = new ArrayList<PointInTimeDataReportsInterface.IdValue>();
		if (helper.getParameter("params") != null) {
			String[] p = helper.getParameter("params").split(":");
			int i = 0;
			for (BasePointInTimeDataReports.Parameter parameter: BasePointInTimeDataReports.Parameter.values()) {
				if (!parameter.allowSingleSelection() && !parameter.allowMultiSelection()) continue;
				if (rpt.getParameters().contains(parameter)) {
					PointInTimeDataReportsInterface.IdValue v = new PointInTimeDataReportsInterface.IdValue();
					v.setValue(parameter.name());
					v.setText(i < p.length ? p[i] : "");
					params.add(v);
					i++;
				}
			}
		}
		
		BufferedPrinter out = new BufferedPrinter(new CSVPrinter(helper.getWriter(), false));
		helper.setup(out.getContentType(), r.getName().replace('/', '-').replace('\\', '-').replace(':', '-') + ".csv", false);
		
		execute(helper.getSessionContext().getUser(), out, r, params);
		
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
	
	public static void execute(UserContext user, Printer out, PointInTimeDataReportsInterface.Report report, List<PointInTimeDataReportsInterface.IdValue> options) throws PointInTimeDataReportsException, PageAccessException {
		try {
			Class rptCls = BasePointInTimeDataReports.sPointInTimeDataReportRegister.get(report.getId());
			BasePointInTimeDataReports rpt = null;
			try {
				rpt = (BasePointInTimeDataReports) rptCls.newInstance();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (rpt == null) throw new IllegalArgumentException("Report " + report + " does not exist.");

			HashMap<BasePointInTimeDataReports.Parameter, String> parameterValues = new HashMap<BasePointInTimeDataReports.Parameter, String>();
			for (BasePointInTimeDataReports.Parameter o: BasePointInTimeDataReports.Parameter.values()) {
				if (rpt.getParameters().contains(o)) {
					String value = null;
					for (PointInTimeDataReportsInterface.IdValue v: options)
						if (o.name().equals(v.getValue())) { value = v.getText(); break; }
					if (value == null || value.isEmpty()) {
						Map<Long, String> vals = o.values(user);
						if (vals == null || vals.isEmpty())
							throw new PointInTimeDataReportsException(MESSAGES.errorUnableToSetParameterNoValues(o.name()));
						value = "";
						for (Long id: vals.keySet()) {
							if (!value.isEmpty()) value += ",";
							value += id.toString();
						}
					}
					parameterValues.put(o, value);
				}
			}
			org.hibernate.Session hibSession = SavedHQLDAO.getInstance().getSession();
			ArrayList<String[]> reportOutput = rpt.execute(parameterValues, hibSession);
			int len = -1;
			for (String[] line: reportOutput) {
				if (len < 0) {
					len = length(line);
					if (line.length > 0 && line[0].startsWith("__")) out.hideColumn(0);
					out.printHeader(line);
				} else {
					out.printLine(line);
				}
				out.flush();
			}
		} catch (PageAccessException e) {
			throw e;
		} catch (PointInTimeDataReportsException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new PointInTimeDataReportsException(MESSAGES.failedExecution(e.getMessage() + (e.getCause() == null ? "" : " (" + e.getCause().getMessage() + ")")));
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
}
