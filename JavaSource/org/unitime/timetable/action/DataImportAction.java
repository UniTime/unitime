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
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.unitime.commons.Debug;
import org.unitime.commons.Email;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.commons.web.WebTable.WebTableLine;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.dataexchange.DataExchangeHelper;
import org.unitime.timetable.dataexchange.DataExchangeHelper.LogWriter;
import org.unitime.timetable.form.DataImportForm;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.queue.QueueItem;
import org.unitime.timetable.util.queue.QueueProcessor;


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
		final DataImportForm myForm = (DataImportForm) form;

		// Read operation to be performed
		String op = (myForm.getOp() != null ? myForm.getOp() : request.getParameter("op"));

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
            QueueProcessor.getInstance().add(new ImportQueItem(Session.getCurrentAcadSession(user), manager, myForm, request));
        }
        
        File xmlFile = null; String xmlName = null;
        if ("Export".equals(op)) {
            ActionMessages errors = myForm.validate(mapping, request);
            if(errors.size() > 0) {
                saveErrors(request, errors);
                return mapping.findForward("display");
            }
            QueueProcessor.getInstance().add(new ExportQueItem(Session.getCurrentAcadSession(user), manager, myForm, request));
        }
        
        if (request.getParameter("remove") != null) {
        	QueueProcessor.getInstance().remove(Long.valueOf(request.getParameter("remove")));
        }
        
        WebTable table = getQueueTable(request, manager.getUniqueId());
        if (table != null) {
        	request.setAttribute("table", table.printTable(WebTable.getOrder(request.getSession(),"dataImport.ord")));
        }

		return mapping.findForward("display");
	}
	
	private WebTable getQueueTable(HttpServletRequest request, Long managerId) {
        WebTable.setOrder(request.getSession(),"dataImport.ord",request.getParameter("ord"),1);
		String log = request.getParameter("log");
		DateFormat df = new SimpleDateFormat("h:mma");
		List<QueueItem> queue = QueueProcessor.getInstance().getItems(null, null, "Data Exchange");
		if (queue.isEmpty()) return null;
		WebTable table = new WebTable(9, "Data exchange in progress", "dataImport.do?ord=%%",
				new String[] { "Name", "Status", "Progress", "Owner", "Session", "Created", "Started", "Finished", "Output"},
				new String[] { "left", "left", "right", "left", "left", "left", "left", "left", "center"},
				new boolean[] { true, true, true, true, true, true, true, true, true});
		Date now = new Date();
		long timeToShow = 1000 * 60 * 60;
		for (QueueItem item: queue) {
			if (item.finished() != null && now.getTime() - item.finished().getTime() > timeToShow) continue;
			String name = item.name();
			if (name.length() > 60) name = name.substring(0, 57) + "...";
			String delete = null;
			if (managerId.equals(item.getOwnerId()) && (item.started() == null || item.finished() != null)) {
				delete = "<img src='images/Delete16.gif' border='0' onClick=\"if (confirm('Do you really want to remove this data exchange?')) document.location='dataImport.do?remove="+item.getId()+"'; event.cancelBubble=true;\">";
			}
			WebTableLine line = table.addLine("onClick=\"document.location='dataImport.do?log=" + item.getId() + "';\"",
					new String[] {
						name + (delete == null ? "": " " + delete),
						item.status(),
						(item.progress() <= 0.0 || item.progress() >= 1.0 ? "" : String.valueOf(Math.round(100 * item.progress())) + "%"),
						item.getOwner().getName(),
						item.getSession().getLabel(),
						df.format(item.created()),
						item.started() == null ? "" : df.format(item.started()),
						item.finished() == null ? "" : df.format(item.finished()),
						item.hasOutput() ? "<A href='temp/"+item.output().getName()+"'>"+item.output().getName().substring(item.output().getName().lastIndexOf('.') + 1).toUpperCase()+"</A>" : ""
					},
					new Comparable[] {
						item.getId(),
						item.status(),
						item.progress(),
						item.getOwner().getName(),
						item.getSession(),
						item.created().getTime(),
						item.started() == null ? Long.MAX_VALUE : item.started().getTime(),
						item.finished() == null ? Long.MAX_VALUE : item.finished().getTime(),
						null
					});
			if (log != null && log.equals(item.getId().toString())) {
				request.setAttribute("logname", name);
				request.setAttribute("logid", item.getId().toString());
				request.setAttribute("log", item.log());
				line.setBgColor("rgb(168,187,225)");
			}
			if (log == null && item.started() != null && item.finished() == null && managerId.equals(item.getOwnerId())) {
				request.setAttribute("logname", name);
				request.setAttribute("logid", item.getId().toString());
				request.setAttribute("log", item.log());
				line.setBgColor("rgb(168,187,225)");
			}
		}
		return table;
	}
	
	public class ImportQueItem extends QueueItem implements LogWriter {
		DataImportForm iForm;
		String iUrl;
		
		public ImportQueItem(Session session, TimetableManager owner, DataImportForm form, HttpServletRequest request) {
			super(session, owner);
			iForm = (DataImportForm)form.clone();
			iUrl = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath();
		}

		@Override
		protected void execute() throws Exception {
            try {
                log("Importing "+iForm.getFile().getFileName()+" ("+iForm.getFile().getFileSize()+" bytes)...");
                Long start = System.currentTimeMillis() ;
                DataExchangeHelper.importDocument((new SAXReader()).read(iForm.getFile().getInputStream()), getOwner(), this);
                Long stop = System.currentTimeMillis() ;
                log("Import finished in "+new DecimalFormat("0.00").format((stop-start)/1000.0)+" seconds.");
            } catch (Exception e) {
                error("Unable to import "+iForm.getFile().getFileName()+": "+e.getMessage());
                Debug.error(e);
                setError(e);
            }
            if (iForm.getEmail() && iForm.getAddress()!=null && iForm.getAddress().length()>0) {
                try {
                	Email mail = new Email();
                	mail.setSubject("Data import finished.");
                	mail.setHTML(log()+"<br><br>"+
                            "This email was automatically generated at "+
                            iUrl+
                            ", by "+
                            "UniTime "+Constants.VERSION+"."+Constants.BLD_NUMBER.replaceAll("@build.number@", "?")+
                            " (Univesity Timetabling Application, http://www.unitime.org).");
                	for (StringTokenizer s=new StringTokenizer(iForm.getAddress(),";,\n\r ");s.hasMoreTokens();) 
                        mail.addRecipient(s.nextToken(), null);
                	if ("true".equals(ApplicationProperties.getProperty("unitime.email.notif.data", "false")))
                		mail.addNotifyCC();
                    mail.send();
                } catch (Exception e) {
                	error("Unable to send email: " + e.getMessage());
                    Debug.error(e);
                    setError(e);
                }
            }
		}

		@Override
		public String name() {
			return "Import of " + iForm.getFile().getFileName();
		}

		@Override
		public double progress() {
			return 0;
		}

		@Override
		public String type() {
			return "Data Exchange";
		}
		
		public void println(String message) {
			log(message);
		}
	}
	
	public class ExportQueItem extends QueueItem implements LogWriter {
		DataImportForm iForm;
	String iUrl = null;
		
		public ExportQueItem(Session session, TimetableManager owner, DataImportForm form, HttpServletRequest request) {
			super(session, owner);
			iForm = (DataImportForm)form.clone();
			iUrl = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath();
		}

		@Override
		protected void execute() throws Exception {
			String xmlName = null;
			File xmlFile = null;
            try {
                String root = (iForm.getExportCourses()?"offerings":iForm.getExportFinalExams() || iForm.getExportMidtermExams()?"exams":iForm.getExportTimetable()?"timetable":"curricula");
                Properties params = new Properties();
                xmlName = getSession().getAcademicTerm() + getSession().getSessionStartYear()+"_"+root;
                params.setProperty("tmtbl.export.exam", (iForm.getExportCourses()?"false":"true"));
                params.setProperty("tmtbl.export.timetable", (iForm.getExportTimetable()?"true":"false")); //exam only
                if (iForm.getExportFinalExams()) {
                    if (iForm.getExportMidtermExams()) {
                        params.setProperty("tmtbl.export.exam.type","all");
                    } else {
                        params.setProperty("tmtbl.export.exam.type","final");
                        xmlName+="_final";
                    }
                } else {
                    if (iForm.getExportMidtermExams()) {
                        params.setProperty("tmtbl.export.exam.type","midterm");
                        xmlName+="_midterm";
                    } else {
                        params.setProperty("tmtbl.export.exam.type","none");
                    }
                }
                log("Exporting "+root+"...");
                xmlName += ".xml";
                xmlFile = createOutput(root, "xml");
                Long start = System.currentTimeMillis() ;
                Document document = DataExchangeHelper.exportDocument(root, getSession(), params, this);
                if (document==null) {
                    error("XML document not created: unknown reason.");
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
                            error("Unable to create export file: "+e.getMessage());
                            setError(e);
                        }
                    }
                }
                Long stop = System.currentTimeMillis() ;
                log("Export finished in "+new DecimalFormat("0.00").format((stop-start)/1000.0)+" seconds.");
            } catch (Exception e) {
                error("Export failed: "+e.getMessage());
                Debug.error(e);
                setError(e);
            }
            if (iForm.getEmail() && iForm.getAddress()!=null && iForm.getAddress().length()>0) {
                try {
                	Email mail = new Email();
                	mail.setSubject("Data import finished.");
                	mail.setHTML(log()+"<br><br>"+
                            "This email was automatically generated at "+
                            iUrl+
                            ", by "+
                            "UniTime "+Constants.VERSION+"."+Constants.BLD_NUMBER.replaceAll("@build.number@", "?")+
                            " (Univesity Timetabling Application, http://www.unitime.org).");
                	for (StringTokenizer s=new StringTokenizer(iForm.getAddress(),";,\n\r ");s.hasMoreTokens();) 
                        mail.addRecipient(s.nextToken(), null);
                	if ("true".equals(ApplicationProperties.getProperty("unitime.email.notif.data", "false")))
                		mail.addNotifyCC();
                    if (xmlFile != null && xmlFile.exists())
                    	mail.addAttachement(xmlFile, xmlName);
                    mail.send();
                } catch (Exception e) {
                	error("Unable to send email: " + e.getMessage());
                    Debug.error(e);
                    setError(e);
                }
            }
		}

		@Override
		public String name() {
            return "Export of " + (iForm.getExportCourses()?"offerings":iForm.getExportFinalExams() || iForm.getExportMidtermExams()?"exams":iForm.getExportTimetable()?"timetable":"curricula");
		}

		@Override
		public double progress() {
			return 0;
		}

		@Override
		public String type() {
			return "Data Exchange";
		}
		
		public void println(String message) {
			log(message);
		}
	}

}

