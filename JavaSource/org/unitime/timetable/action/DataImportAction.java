/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.dataexchange.DataExchangeHelper;
import org.unitime.timetable.form.DataImportForm;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.util.Constants;


/** 
 * MyEclipse Struts
 * Creation date: 01-24-2007
 * 
 * XDoclet definition:
 * @struts.action path="/dataImport" name="dataImportForm" input="/form/dataImport.jsp" scope="request" validate="true"
 */
public class DataImportAction extends Action {

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
	public ActionForward execute(ActionMapping mapping,	ActionForm form, HttpServletRequest request, HttpServletResponse response) {
		DataImportForm myForm = (DataImportForm) form;

		// Read operation to be performed
		String op = (myForm.getOp() != null ? myForm.getOp() : request.getParameter("op"));

		StringWriter log = new StringWriter();
		PrintWriter out = new PrintWriter(log);
		
		String userId = (String)request.getSession().getAttribute("authUserExtId");
		User user = Web.getUser(request.getSession());
		TimetableManager manager = null;
		if (userId!=null) {
		    manager = TimetableManager.findByExternalId(userId);
		}
		if (manager==null && user!=null) {
		    manager = TimetableManager.getManager(user);
		}
		
		if ("Import".equals(op)) {
            // Validate input
            ActionMessages errors = myForm.validate(mapping, request);
            if(errors.size() > 0) {
                saveErrors(request, errors);
                return mapping.findForward("display");
            }
            try {
                out.println("Importing "+myForm.getFile().getFileName()+" ("+myForm.getFile().getFileSize()+" bytes)...<br>");
                Long start = System.currentTimeMillis() ;
                DataExchangeHelper.importDocument((new SAXReader()).read(myForm.getFile().getInputStream()), manager, out);
                Long stop = System.currentTimeMillis() ;
                out.println("Import finished in "+new DecimalFormat("0.00").format((stop-start)/1000.0)+" seconds.<br>");
            } catch (Exception e) {
                out.println("<font color='red'><b>Unable to import "+myForm.getFile().getFileName()+": "+e.getMessage()+"</b></font><br>");
                Debug.error(e);
                errors.add("document", new ActionMessage("errors.generic", e.getMessage()));
                saveErrors(request, errors);
            }
        }
        
        File xmlFile = null; String xmlName = null;
        if ("Export".equals(op)) {
            ActionMessages errors = myForm.validate(mapping, request);
            if(errors.size() > 0) {
                saveErrors(request, errors);
                return mapping.findForward("display");
            }
            try {
                Session session = Session.getCurrentAcadSession(Web.getUser(request.getSession()));
                String root = (myForm.getExportCourses()?"offerings":myForm.getExportFinalExams() || myForm.getExportMidtermExams()?"exams":"timetable");
                Properties params = new Properties();
                //TODO: checked OK, tested OK
                xmlName = session.getAcademicTerm()+session.getSessionStartYear()+"_"+root;
                params.setProperty("tmtbl.export.exam", (myForm.getExportCourses()?"false":"true"));
                params.setProperty("tmtbl.export.timetable", (myForm.getExportTimetable()?"true":"false")); //exam only
                if (myForm.getExportFinalExams()) {
                    if (myForm.getExportMidtermExams()) {
                        params.setProperty("tmtbl.export.exam.type","all");
                    } else {
                        params.setProperty("tmtbl.export.exam.type","final");
                        xmlName+="_final";
                    }
                } else {
                    if (myForm.getExportMidtermExams()) {
                        params.setProperty("tmtbl.export.exam.type","midterm");
                        xmlName+="_midterm";
                    } else {
                        params.setProperty("tmtbl.export.exam.type","none");
                    }
                }
                out.println("Exporting "+root+"...<br>");
                xmlName += ".xml";
                xmlFile = ApplicationProperties.getTempFile(root, "xml");
                Long start = System.currentTimeMillis() ;
                Document document = DataExchangeHelper.exportDocument(root, session, params, out);
                if (document==null) {
                    out.println("<font color='red'><b>XML document not created: unknown reason.</b></font><br>");
                } else {
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(xmlFile);
                        (new XMLWriter(fos,OutputFormat.createPrettyPrint())).write(document);
                        fos.flush();fos.close();fos=null;
                    } finally {
                        try {
                            if (fos!=null) fos.close();
                        } catch (IOException e) {
                            out.println("<font color='red'><b>Unable to create export file: "+e.getMessage()+"</b></font><br>");
                        }
                    }
                }
                Long stop = System.currentTimeMillis() ;
                if (xmlFile.exists()) {
                    request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+xmlFile.getName());
                }
                out.println("Export finished in "+new DecimalFormat("0.00").format((stop-start)/1000.0)+" seconds.<br>");
            } catch (Exception e) {
                out.println("<font color='red'><b>Export failed: "+e.getMessage()+"</b></font><br>");
                Debug.error(e);
                errors.add("export", new ActionMessage("errors.generic", e.getMessage()));
                saveErrors(request, errors);
            }
        }
        
        out.flush();out.close();
        myForm.setLog(log.toString());
        
        if (op!=null && myForm.getEmail() && myForm.getAddress()!=null && myForm.getAddress().length()>0) {
            myForm.setEmail(false);
            try {
                InternetAddress from = 
                    (manager.getEmailAddress()==null?
                            new InternetAddress(
                                    ApplicationProperties.getProperty("tmtbl.inquiry.sender",ApplicationProperties.getProperty("tmtbl.contact.email")),
                                    ApplicationProperties.getProperty("tmtbl.inquiry.sender.name")):
                            new InternetAddress(manager.getEmailAddress(),manager.getName()));
                Properties p = ApplicationProperties.getProperties();
                if (p.getProperty("mail.smtp.host")==null && p.getProperty("tmtbl.smtp.host")!=null)
                    p.setProperty("mail.smtp.host", p.getProperty("tmtbl.smtp.host"));
                Authenticator a = null;
                if (ApplicationProperties.getProperty("tmtbl.mail.user")!=null && ApplicationProperties.getProperty("tmtbl.mail.pwd")!=null) {
                    a = new Authenticator() {
                        public PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(
                                    ApplicationProperties.getProperty("tmtbl.mail.user"),
                                    ApplicationProperties.getProperty("tmtbl.mail.pwd"));
                        }
                    };
                }
                javax.mail.Session mailSession = javax.mail.Session.getDefaultInstance(p, a);
                MimeMessage mail = new MimeMessage(mailSession);
                mail.setSubject("Data exchange finished.");
                Multipart body = new MimeMultipart();
                MimeBodyPart text = new MimeBodyPart();
                text.setContent(log.toString()+"<br><br>"+
                        "This email was automatically generated at "+
                        request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+
                        ", by "+
                        "UniTime "+Constants.VERSION+"."+Constants.BLD_NUMBER.replaceAll("@build.number@", "?")+
                        " (Univesity Timetabling Application, http://www.unitime.org).","text/html");
                body.addBodyPart(text);
                if (myForm.getAddress()!=null) for (StringTokenizer s=new StringTokenizer(myForm.getAddress(),";,\n\r ");s.hasMoreTokens();) 
                    mail.addRecipient(RecipientType.TO, new InternetAddress(s.nextToken()));
                if (from!=null)
                    mail.setFrom(from);
                if (xmlFile!=null) {
                    BodyPart attachement = new MimeBodyPart();
                    attachement.setDataHandler(new DataHandler(new FileDataSource(xmlFile)));
                    attachement.setFileName(xmlName);
                    body.addBodyPart(attachement);
                }
                mail.setSentDate(new Date());
                mail.setContent(body);
                Transport.send(mail);
            } catch (Exception e) {
                Debug.error(e);
                ActionMessages errors = new ActionErrors();
                errors.add("email", new ActionMessage("errors.generic", e.getMessage()));
                saveErrors(request, errors);
            }
        }

		return mapping.findForward("display");
	}
}

