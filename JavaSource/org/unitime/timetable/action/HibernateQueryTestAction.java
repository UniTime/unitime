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

import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.dom4j.Document;
import org.hibernate.MappingException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.engine.jdbc.internal.BasicFormatterImpl;
import org.hibernate.engine.jdbc.internal.Formatter;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.hql.internal.QueryExecutionRequestException;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.CollectionType;
import org.hibernate.type.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.form.HibernateQueryTestForm;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;


/** 
 * MyEclipse Struts
 * Creation date: 12-16-2005
 * 
 * XDoclet definition:
 * @struts:action path="/hibernateQueryTest" name="hibernateQueryTestForm" input="/form/hibernateQueryTest.jsp" scope="request"
 *
 * @author Tomas Muller
 */
@Service("/hibernateQueryTest")
public class HibernateQueryTestAction extends Action {
	
	private static Level iOriginalLevel = null;
	
	@Autowired SessionContext sessionContext;

    // --------------------------------------------------------- Instance Variables

    // --------------------------------------------------------- Methods

    /** 
     * Method execute
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     */
    public ActionForward execute(
        ActionMapping mapping,
        ActionForm form,
        HttpServletRequest request,
        HttpServletResponse response) throws Exception {

    	sessionContext.checkPermission(Right.TestHQL);

		String op = request.getParameter("op");
		if(op==null || !op.equals("Submit")) {
		    if ("Clear Cache".equals(op)) 
		        HibernateUtil.clearCache();
		    return mapping.findForward("displayQueryForm");
		}
		
        HibernateQueryTestForm frm = (HibernateQueryTestForm) form;
        ActionMessages errors =  frm.validate(mapping, request);
        
        Logger sqlLog = Logger.getLogger("org.hibernate.SQL");
        if (iOriginalLevel == null)
        	iOriginalLevel = sqlLog.getLevel();
        sqlLog.setLevel(Level.DEBUG);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Appender myAppender = new WriterAppender(new PatternLayout("%m%n"), out);
        sqlLog.addAppender(myAppender);
        
        if(errors.size()==0) {
            try {
            	int limit = ApplicationProperty.TestHQLMaxLines.intValue();
		        String query = frm.getQuery();	        
		        _RootDAO rdao = new _RootDAO();
		        Session hibSession = rdao.getSession();	        
		        Query q = hibSession.createQuery(query);
		        try {
	                List l = q.list();
	                StringBuffer s = new StringBuffer();
	                int line = 0;
	                for (Iterator i=l.iterator();i.hasNext();line++) {
	                    if (limit > 0 && line >= limit) {
	                        s.append("<tr><td>...</td></tr>"); break;
	                    }
	                    Object o = i.next();
	                    if (s.length()==0) printHeader(s, o);
	                    printLine(s, o, (SessionImplementor)hibSession);
	                }
	                if (s.length()>0) {
	                    printFooter(s);
	                    request.setAttribute("result", s.toString());
	                }
	                frm.setListSize(String.valueOf(l.size()));
		        } catch (QueryExecutionRequestException e) {
		            Transaction tx = null;
		            try {
		                tx = hibSession.beginTransaction();
		                int i = q.executeUpdate();
	                    request.setAttribute("result", i+" lines updated.");
	                    frm.setListSize(String.valueOf(i));
		                tx.commit();
		            } catch (Exception ex) {
		                if (tx!=null && tx.isActive()) tx.rollback();
		                throw ex;
		            }
		            hibSession.flush();
		            HibernateUtil.clearCache();
		        }
            }
            catch (Exception e) {
                errors.add("query", 
                        	new ActionMessage("errors.generic", e.getMessage()));
                Debug.error(e);
            }
        }
        
        sqlLog.removeAppender(myAppender);
        sqlLog.setLevel(iOriginalLevel == null ? Level.INFO : iOriginalLevel);
        out.flush(); out.close();
        String sql = "";
        for (StringTokenizer stk = new StringTokenizer(new String(out.toByteArray()),"\n");stk.hasMoreTokens();) {
            String line = (String)stk.nextToken();
            String comment = null; 
            if (line.indexOf("/*")>=0 && line.indexOf("/*")<line.indexOf("*/")) {
                comment = line.substring(line.indexOf("/*")+2, line.indexOf("*/"));
                line = line.substring(0, line.indexOf("/*")) + line.substring(line.indexOf("*/")+2);
            }
            if (sql.length()>0) sql+="<br><br>";
            if (comment!=null)
                sql += "<font color='gray'>-- "+comment+"</font>";
            Formatter f = new BasicFormatterImpl();
            sql += f.format(line).replaceAll("\n", "<br>").replaceAll(" ", "&nbsp;");
        }
        if (sql.length()>0)
            request.setAttribute("sql",sql);

        saveErrors(request, errors);        
        return mapping.findForward("displayQueryForm");
        
    }
    
    private void header(StringBuffer s, int idx, String text) {
        s.append("<td class='WebTableHeader'><i>");
        if (text==null || text.length()<=0)
            s.append("Col "+idx);
        else {
            s.append(text.substring(0,1).toUpperCase());
            if (text.length()>1) s.append(text.substring(1));
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
    
    public void printHeader(StringBuffer s, Object o) {
        s.append("<table width='100%' border='0' cellspacing='0' cellpadding='3'>");
        s.append("<tr align='left'>");
        SessionFactory hibSessionFactory = new _RootDAO().getSession().getSessionFactory();
        int idx=1;
        if (o==null) {
            header(s,idx++,null);
        } else if (o instanceof Object[]) {
            Object[] x = (Object[])o;
            for (int i=0;i<x.length;i++) {
                if (x[i]==null) {
                    header(s,idx++,null);
                } else {
                    ClassMetadata meta = hibSessionFactory.getClassMetadata(x[i].getClass());
                    if (meta==null) {
                        header(s,idx++,null);
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
            ClassMetadata meta = hibSessionFactory.getClassMetadata(o.getClass());
            if (meta==null) {
                header(s,idx++,null);
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
        		s.append(((Document)text).asXML().replace("<", "&lt;").replace(">", "&gt;"));
        	} else {
        		s.append(text.toString());
        	}
        }
        s.append("</td>");
    }
    
    
    public void printLine(StringBuffer s, Object o, SessionImplementor session) {
        s.append("<tr align='left' onmouseover=\"this.style.backgroundColor='rgb(223,231,242)';\" onmouseout=\"this.style.backgroundColor='transparent';\" >");
        SessionFactory hibSessionFactory = new _RootDAO().getSession().getSessionFactory();
        if (o==null) {
            line(s,null);
        } else if (o instanceof Object[]) {
            Object[] x = (Object[])o;
            for (int i=0;i<x.length;i++) {
                if (x[i]==null) {
                    line(s,null);
                } else {
                    ClassMetadata meta = hibSessionFactory.getClassMetadata(x[i].getClass());
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
            ClassMetadata meta = hibSessionFactory.getClassMetadata(o.getClass());
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
