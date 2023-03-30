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
package org.unitime.timetable.action;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.dom4j.Document;
import org.hibernate.MappingException;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.exception.SQLGrammarException;
import org.hibernate.hql.internal.QueryExecutionRequestException;
import org.hibernate.hql.internal.ast.ASTQueryTranslatorFactory;
import org.hibernate.hql.spi.QueryTranslator;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.CollectionType;
import org.hibernate.type.Type;
import org.unitime.commons.Debug;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.export.BufferedPrinter;
import org.unitime.timetable.export.CSVPrinter;
import org.unitime.timetable.export.hql.TestHqlExportToCSV;
import org.unitime.timetable.form.HibernateQueryTestForm;
import org.unitime.timetable.model.SavedHQL;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.Navigation;


/** 
 *  @author Tomas Muller
 */
@Action(value = "hibernateQueryTest", results = {
		@Result(name = "displayQueryForm", type = "tiles", location = "hibernateQueryTest.tiles")
	})
@TilesDefinition(name = "hibernateQueryTest.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Test HQL"),
		@TilesPutAttribute(name = "body", value = "/admin/hibernateQueryTest.jsp")
	})

public class HibernateQueryTestAction extends UniTimeAction<HibernateQueryTestForm> {
	private static final long serialVersionUID = 4379704237143143345L;
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);

	@Override
    public String execute() throws Exception {
    	sessionContext.checkPermission(Right.TestHQL);
    	if (form == null)
    		form = new HibernateQueryTestForm();
    	
    	if (op == null)
    		return "displayQueryForm";
    	
    	if (MSG.actionClearCache().equals(op)) {
    		HibernateUtil.clearCache();
		    return "displayQueryForm";
		}

        if ("Back".equals(op) || MSG.actionBackToDetail().equals(op)) {
        	if (form.getQuery() == null || form.getQuery().isEmpty()) {
        		String q = (String)request.getSession().getAttribute("TestHQL.LastQuery");
        		if (q != null) form.setQuery(q);
        	}
        }
		
        form.validate(this);
        
        if (MSG.actionNextQueryResults().equals(op)) {
        	form.setStart(form.getStart() + ApplicationProperty.TestHQLMaxLines.intValue());
        }
        if (MSG.actionPreviousQueryResults().equals(op)) {
        	form.setStart(Math.max(0, form.getStart() - ApplicationProperty.TestHQLMaxLines.intValue()));
        }
        if (MSG.actionSubmitQuery().equals(op)) {
        	form.setStart(0);
        }
        
        if (MSG.actionExportCsv().equals(op) && !hasFieldErrors()) {
        	String query = form.getQuery();
	        for (SavedHQL.Option o: SavedHQL.Option.values()) {
				if (query.indexOf("%" + o.name() + "%") >= 0) {
					String value = null;
					if (value == null || value.isEmpty()) {
						Map<Long, String> vals = o.values(sessionContext.getUser());
						if (vals == null || vals.isEmpty()) {
							addFieldError("form.query", MSG.errorCannotSetQueryParameterNoValues());
					        return "displayQueryForm";
						}
						value = "";
						for (Long id: vals.keySet()) {
							if (!value.isEmpty()) value += ",";
							value += id.toString();
						}
					}
					query = query.replace("%" + o.name() + "%", "(" + value + ")");
				}
	        }
	        if (query.indexOf("%USER%") >= 0)
	        	query = query.replace("%USER%", HibernateUtil.escapeSql(sessionContext.getUser().getExternalUserId()));
	        
        	BufferedPrinter out = new BufferedPrinter(new CSVPrinter(response.getWriter(), false));
        	response.setContentType(out.getContentType() + "; charset=UTF-8");
        	response.setCharacterEncoding("UTF-8");
        	response.setHeader("Pragma", "no-cache" );
    		response.addHeader("Cache-Control", "must-revalidate" );
    		response.addHeader("Cache-Control", "no-cache" );
    		response.addHeader("Cache-Control", "no-store" );
    		response.setDateHeader("Date", new Date().getTime());
    		response.setDateHeader("Expires", 0);
    		response.setHeader("Content-Disposition", "attachment; filename=\"hql-test.csv\"" );
        	TestHqlExportToCSV.execute(sessionContext.getUser(), out, query, 0, -1);
        	out.close();
        	return null;
        }
        
        form.setExport(false);
        
        if (!hasFieldErrors()) {
            try {
            	int limit = ApplicationProperty.TestHQLMaxLines.intValue();
		        String query = form.getQuery();
		        for (SavedHQL.Option o: SavedHQL.Option.values()) {
					if (query.indexOf("%" + o.name() + "%") >= 0) {
						String value = null;
						if (value == null || value.isEmpty()) {
							Map<Long, String> vals = o.values(sessionContext.getUser());
							if (vals == null || vals.isEmpty()) {
								addFieldError("form.query", MSG.errorCannotSetQueryParameterNoValues());
						        return "displayQueryForm";
							}
							value = "";
							for (Long id: vals.keySet()) {
								if (!value.isEmpty()) value += ",";
								value += id.toString();
							}
						}
						query = query.replace("%" + o.name() + "%", "(" + value + ")");
					}
		        }
		        if (query.indexOf("%USER%") >= 0)
		        	query = query.replace("%USER%", HibernateUtil.escapeSql(sessionContext.getUser().getExternalUserId()));
		        _RootDAO rdao = new _RootDAO();
		        Session hibSession = rdao.getSession();
		        Query q = hibSession.createQuery(query);
		        
		        try {
		        	String hqlQueryString = q.getQueryString();
		        	ASTQueryTranslatorFactory queryTranslatorFactory = new ASTQueryTranslatorFactory();
		        	QueryTranslator queryTranslator = queryTranslatorFactory.createQueryTranslator("", hqlQueryString, java.util.Collections.EMPTY_MAP, (SessionFactoryImplementor) hibSession.getSessionFactory(), null);
		        	queryTranslator.compile(java.util.Collections.EMPTY_MAP, false);
		        	request.setAttribute("sql", queryTranslator.getSQLString());
		        } catch (Exception e) {
		        	Debug.error(e);
		        }
		        
		        q.setFirstResult(form.getStart());
		        if (limit > 0) q.setMaxResults(limit + 1);
		        try {
	                List l = q.list();
	                String[] alias = q.getReturnAliases();
			        List<Long> ids = new ArrayList<Long>();
	                StringBuffer s = new StringBuffer();
	                int line = 0;
	                for (Iterator i=l.iterator();i.hasNext();line++) {
	                    if (limit > 0 && line >= limit) {
	                        // s.append("<tr><td>...</td></tr>");
	                        break;
	                    }
	                    Object o = i.next();
	                    if (s.length()==0) printHeader(s, o, alias);
	                    printLine(s, o, (SessionImplementor)hibSession, alias);
	                    if (o != null && o instanceof Object[] && ((Object[])o).length > 0 && ((Object[])o)[0] != null && ((Object[])o)[0] instanceof Long)
	                    	ids.add((Long)((Object[])o)[0]);
	                }
	                if (s.length()>0) {
	                    printFooter(s);
	                    request.setAttribute("result", s.toString());
	                }
	                if (form.getStart() == 0) {
	                	if (limit > 0 && l.size() > limit) {
	                		form.setListSize(limit + "+ " + MSG.queryLines());
	                	} else {
	                		form.setListSize(l.size() + " " + MSG.queryLines());
	                	}
	                } else {
	                	if (limit > 0 && l.size() > limit) {
	                		form.setListSize(MSG.queryLines() + " " + form.getStart() + " ... " + (form.getStart() + limit));
	                	} else {
	                		form.setListSize(MSG.queryLines() + " " + form.getStart() + " ... " + (form.getStart() + l.size()));
	                	}
	                }
	                form.setExport(!l.isEmpty());
	                form.setNext(limit > 0 && l.size() > limit);
			        if (!ids.isEmpty() && alias != null && alias.length > 0 && alias[0].startsWith("__")) {
			        	if ("__Class".equals(alias[0]))
			    			Navigation.set(sessionContext, Navigation.sClassLevel, ids);
			    		else if ("__Offering".equals(alias[0]))
			    			Navigation.set(sessionContext, Navigation.sInstructionalOfferingLevel, ids);
			    		else if ("__Subpart".equals(alias[0]))
			    			Navigation.set(sessionContext, Navigation.sSchedulingSubpartLevel, ids);
			    		else if ("__Room".equals(alias[0]))
			    			Navigation.set(sessionContext, Navigation.sInstructionalOfferingLevel, ids);
			    		else if ("__Instructor".equals(alias[0]))
			    			Navigation.set(sessionContext, Navigation.sInstructionalOfferingLevel, ids);
			    		else if ("__Exam".equals(alias[0]))
			    			Navigation.set(sessionContext, Navigation.sInstructionalOfferingLevel, ids);
			    		else if ("__Event".equals(alias[0]))
			    			Navigation.set(sessionContext, Navigation.sInstructionalOfferingLevel, ids);
			        }
		        } catch (QueryExecutionRequestException e) {
		            Transaction tx = null;
		            try {
		                tx = hibSession.beginTransaction();
		                int i = q.executeUpdate();
	                    request.setAttribute("result", MSG.queryLinesUpdated(i));
	                    form.setListSize(MSG.queryLinesUpdated(i));
		                tx.commit();
		            } catch (Exception ex) {
		                if (tx!=null && tx.isActive()) tx.rollback();
		                throw ex;
		            }
		            hibSession.flush();
		            HibernateUtil.clearCache();
		        }
            } catch (SQLGrammarException e) {
            	if (e.getSQLException() != null)
            		addFieldError("form.query", e.getSQLException().getMessage());
            	else
            		addFieldError("form.query", e.getMessage());
            } catch (Exception e) {
            	addFieldError("form.query", e.getMessage());
                Debug.error(e);
            }
        }
        
        String url = "hibernateQueryTest.action?form.query="+URLEncoder.encode(form.getQuery(), "utf-8")+"&form.start="+form.getStart()+"&op=Back";
        if (url.length() <= 2000) {
        	request.getSession().removeAttribute("TestHQL.LastQuery");
            BackTracker.markForBack(
    				request, 
    				url, "HQL", 
    				true, true);
        } else {
        	request.getSession().setAttribute("TestHQL.LastQuery", form.getQuery());
        	BackTracker.markForBack(
        			request,
        			"hibernateQueryTest.action?form.start="+form.getStart()+"&op=Back", "HQL", 
    				true, true);
        }
        	

        return "displayQueryForm";
    }
    
    private void header(StringBuffer s, int idx, String text) {
        s.append("<td class='WebTableHeader'><i>");
        if (text==null || text.length()<=0)
            s.append("Col "+idx);
        else {
        	try {
        		s.append("Col " + (1 + Integer.parseInt(text)));
        	} catch (NumberFormatException e) {
                s.append(text.substring(0,1).toUpperCase());
                if (text.length()>1) s.append(text.substring(1).replace('_', ' '));
        	}
        }
        s.append("</i></td>");
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
    
    public void printHeader(StringBuffer s, Object o, String[] alias) {
        s.append("<table width='100%' border='0' cellspacing='0' cellpadding='3' class='unitime-HQLTable'>");
        s.append("<tr align='left'>");
        SessionFactory hibSessionFactory = new _RootDAO().getSession().getSessionFactory();
        boolean hasLink = false;
    	if (alias != null && alias.length > 0 && alias[0] != null && alias[0].startsWith("__")) {
    		if ("__Class".equals(alias[0])) hasLink = true;
			else if ("__Offering".equals(alias[0])) hasLink = true;
			else if ("__Subpart".equals(alias[0])) hasLink = true;
			else if ("__Room".equals(alias[0])) hasLink = true;
			else if ("__Instructor".equals(alias[0])) hasLink = true;
			else if ("__Exam".equals(alias[0])) hasLink = true;
			else if ("__Event".equals(alias[0])) hasLink = true;
    	}
        int idx=1;
        if (o==null) {
            header(s, idx++, (alias != null && alias.length > 0 && alias[0] != null && !alias[0].isEmpty() ? alias[0] : null));
        } else if (o instanceof Object[]) {
            Object[] x = (Object[])o;
            for (int i=0;i<x.length;i++) {
            	if (hasLink && i == 0) continue;
            	String a = (alias != null && alias.length > i && alias[i] != null && !alias[i].isEmpty() ? alias[i] : null);
                if (x[i]==null) {
                    header(s,idx++,a);
                } else {
    				ClassMetadata meta = null;
            		try {
            			meta = hibSessionFactory.getClassMetadata(x[i].getClass());
            		} catch (MappingException e) {}
                    if (meta==null) {
                        header(s,idx++,a);
                    } else {
                        header(s,idx++,meta.getIdentifierPropertyName());
                        for (int j=0;j<meta.getPropertyNames().length;j++) {
                            if (!skip(meta.getPropertyTypes()[j], meta.getPropertyLaziness()[j]))
                                header(s,idx++,meta.getPropertyNames()[j]);
                        }
                    }
                }
            }
        } else {
			ClassMetadata meta = null;
    		try {
    			meta = hibSessionFactory.getClassMetadata(o.getClass());
    		} catch (MappingException e) {}
            if (meta==null) {
                header(s,idx++,(alias != null && alias.length > 0 && alias[0] != null && !alias[0].isEmpty() ? alias[0] : null));
            } else {
                header(s,idx++,meta.getIdentifierPropertyName());
                for (int i=0;i<meta.getPropertyNames().length;i++) {
                    if (!skip(meta.getPropertyTypes()[i], meta.getPropertyLaziness()[i]))
                        header(s,idx++,meta.getPropertyNames()[i]);
                }
            }
        }
        s.append("</tr>");
    }
    
    private void line(StringBuffer s, Object text) {
        s.append("<td>");
        if (text!=null) {
        	if (text instanceof Document) {
        		s.append(StringEscapeUtils.escapeHtml4(((Document)text).asXML()));
        	} else {
        		s.append(text.toString());
        	}
        }
        s.append("</td>");
    }
    
    
    public void printLine(StringBuffer s, Object o, SessionImplementor session, String[] alias) {
    	String link = null;
    	if (alias != null && alias.length > 0 && alias[0] != null && alias[0].startsWith("__") && o != null && (o instanceof Object[])) {
    		if ("__Class".equals(alias[0]))
    			link = "classDetail.action?cid=" + ((Object[])o)[0];
			else if ("__Offering".equals(alias[0]))
				link = "instructionalOfferingDetail.action?op=view&io=" + ((Object[])o)[0];
			else if ("__Subpart".equals(alias[0]))
				link = "schedulingSubpartDetail.action?ssuid=" + ((Object[])o)[0];
			else if ("__Room".equals(alias[0]))
				link = "gwt.jsp?page=rooms&back=1&id=" + ((Object[])o)[0];
			else if ("__Instructor".equals(alias[0]))
				link = "instructorDetail.action?instructorId=" + ((Object[])o)[0];
			else if ("__Exam".equals(alias[0]))
				link = "examDetail.action?examId=" + ((Object[])o)[0];
			else if ("__Event".equals(alias[0]))
				link = "gwt.jsp?page=events#event=" + ((Object[])o)[0];
    	}
        s.append("<tr align='left' onmouseover=\"this.style.backgroundColor='rgb(223,231,242)';\" onmouseout=\"this.style.backgroundColor='transparent';\" " + (link == null ? "" : "onClick=\"document.location='" + link + "';\"") + ">");
        SessionFactory hibSessionFactory = new _RootDAO().getSession().getSessionFactory();
        if (o==null) {
            line(s,null);
        } else if (o instanceof Object[]) {
            Object[] x = (Object[])o;
            for (int i=0;i<x.length;i++) {
            	if (link != null && i == 0) continue;
                if (x[i]==null) {
                    line(s,null);
                } else {
    				ClassMetadata meta = null;
            		try {
            			meta = hibSessionFactory.getClassMetadata(x[i].getClass());
            		} catch (MappingException e) {}
                    if (meta==null) {
                        line(s,x[i]);
                    } else {
                        line(s,meta.getIdentifier(x[i], session));
                        for (int j=0;j<meta.getPropertyNames().length;j++) 
                            if (!skip(meta.getPropertyTypes()[j], meta.getPropertyLaziness()[j]))
                                line(s,meta.getPropertyValue(x[i], meta.getPropertyNames()[j]));
                    }
                }
            }
        } else {
			ClassMetadata meta = null;
    		try {
    			meta = hibSessionFactory.getClassMetadata(o.getClass());
    		} catch (MappingException e) {}
            if (meta==null) {
                line(s,o);
            } else {
                line(s,meta.getIdentifier(o, session));
                for (int i=0;i<meta.getPropertyNames().length;i++) 
                    if (!skip(meta.getPropertyTypes()[i],meta.getPropertyLaziness()[i]))
                        line(s,meta.getPropertyValue(o, meta.getPropertyNames()[i]));
            }
        }
        s.append("</tr>");
    }
    
    public void printFooter(StringBuffer s) {
        s.append("</table>");
    }

}
