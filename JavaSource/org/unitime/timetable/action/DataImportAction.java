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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.cpsolver.ifs.util.Progress;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.commons.Email;
import org.unitime.commons.web.WebTable;
import org.unitime.commons.web.WebTable.WebTableLine;
import org.unitime.timetable.backup.BackupProgress;
import org.unitime.timetable.backup.SessionBackupInterface;
import org.unitime.timetable.backup.SessionRestoreInterface;
import org.unitime.timetable.dataexchange.DataExchangeHelper;
import org.unitime.timetable.dataexchange.DataExchangeHelper.LogWriter;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.form.DataImportForm;
import org.unitime.timetable.form.DataImportForm.ExportType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.service.SolverServerService;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.queue.QueueItem;


/** 
 * MyEclipse Struts
 * Creation date: 01-24-2007
 * 
 * XDoclet definition:
 * @struts.action path="/dataImport" name="dataImportForm" input="/form/dataImport.jsp" scope="request" validate="true"
 *
 * @author Tomas Muller
 */
@Service("/dataImport")
public class DataImportAction extends Action {
	
	@Autowired SessionContext sessionContext;
	
	@Autowired SolverServerService solverServerService;

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
		
		sessionContext.checkPermission(Right.DataExchange);
		
		if (op == null)
			myForm.setAddress(sessionContext.getUser().getEmail());
		
		Session session = SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId());
		
		if ("Import".equals(op)) {
            // Validate input
            ActionMessages errors = myForm.validate(mapping, request);
            if(errors.size() > 0) {
                saveErrors(request, errors);
                return mapping.findForward("display");
            }
            solverServerService.getQueueProcessor().add(new ImportQueItem(session, sessionContext.getUser(), myForm, request));
        }
        
        if ("Export".equals(op)) {
            ActionMessages errors = myForm.validate(mapping, request);
            if(errors.size() > 0) {
                saveErrors(request, errors);
                return mapping.findForward("display");
            }
            solverServerService.getQueueProcessor().add(new ExportQueItem(session, sessionContext.getUser(), myForm, request));
        }
        
        if (request.getParameter("remove") != null) {
        	solverServerService.getQueueProcessor().remove(request.getParameter("remove"));
        }
        
        WebTable table = getQueueTable(request);
        if (table != null) {
        	request.setAttribute("table", table.printTable(WebTable.getOrder(sessionContext,"dataImport.ord")));
        }

		return mapping.findForward("display");
	}
	
	private WebTable getQueueTable(HttpServletRequest request) {
        WebTable.setOrder(sessionContext,"dataImport.ord",request.getParameter("ord"),1);
		String log = request.getParameter("log");
		Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.TIME_SHORT);
		List<QueueItem> queue = solverServerService.getQueueProcessor().getItems(null, null, "Data Exchange");
		if (queue.isEmpty()) return null;
		WebTable table = new WebTable(9, "Data exchange in progress", "dataImport.do?ord=%%",
				new String[] { "Name", "Status", "Progress", "Owner", "Session", "Created", "Started", "Finished", "Output"},
				new String[] { "left", "left", "right", "left", "left", "left", "left", "left", "center"},
				new boolean[] { true, true, true, true, true, true, true, true, true});
		Date now = new Date();
		long timeToShow = 1000 * 60 * 60;
		for (QueueItem item: queue) {
			if (item.finished() != null && now.getTime() - item.finished().getTime() > timeToShow) continue;
			if (item.getSession() == null) continue;
			String name = item.name();
			if (name.length() > 60) name = name.substring(0, 57) + "...";
			String delete = null;
			if (sessionContext.getUser().getExternalUserId().equals(item.getOwnerId()) && (item.started() == null || item.finished() != null)) {
				delete = "<img src='images/action_delete.png' border='0' onClick=\"if (confirm('Do you really want to remove this data exchange?')) document.location='dataImport.do?remove="+item.getId()+"'; event.cancelBubble=true;\">";
			}
			WebTableLine line = table.addLine("onClick=\"document.location='dataImport.do?log=" + item.getId() + "';\"",
					new String[] {
						name + (delete == null ? "": " " + delete),
						item.status(),
						(item.progress() <= 0.0 || item.progress() >= 1.0 ? "" : String.valueOf(Math.round(100 * item.progress())) + "%"),
						item.getOwnerName(),
						item.getSession().getLabel(),
						df.format(item.created()),
						item.started() == null ? "" : df.format(item.started()),
						item.finished() == null ? "" : df.format(item.finished()),
						item.hasOutput() ? "<A href='"+item.getOutputLink()+"'>"+item.getOutputName().substring(item.getOutputName().lastIndexOf('.') + 1).toUpperCase()+"</A>" : ""
					},
					new Comparable[] {
						item.created().getTime(),
						item.status(),
						item.progress(),
						item.getOwnerName(),
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
			if (log == null && item.started() != null && item.finished() == null && sessionContext.getUser().getExternalUserId().equals(item.getOwnerId())) {
				request.setAttribute("logname", name);
				request.setAttribute("logid", item.getId().toString());
				request.setAttribute("log", item.log());
				line.setBgColor("rgb(168,187,225)");
			}
		}
		return table;
	}
	
	public static abstract class DataExchangeQueueItem extends QueueItem implements LogWriter, BackupProgress {
		private static final long serialVersionUID = 1L;
		DataImportForm iForm;
		String iUrl;
		boolean iImport;
		String iSessionName;
		
		public DataExchangeQueueItem(Session session, UserContext owner, DataImportForm form, HttpServletRequest request, boolean isImport) {
			super(session, owner);
			iForm = (DataImportForm)form.clone();
			iUrl = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath();
			iImport = isImport;
			iSessionName = session.getAcademicTerm() + session.getAcademicYear() + session.getAcademicInitiative();
		}
				
		@Override
		public String type() {
			return "Data Exchange";
		}
		
		@Override
		public String name() {
			return (iImport ? "Import of " + iForm.getFile().getFileName() : "Export of " + iForm.getExportType().getLabel());
		}
		
		public void println(String message) {
			log(message);
		}
		
		abstract void executeDataExchange() throws Exception;
		
		@Override
		public void info(String message) {
			super.info(message);
		}
		
		@Override
		public void warn(String message) {
			super.warn(message);
		}
		
		@Override
		public void error(String message) {
			super.error(message);
		}
		
		@Override
		public void setPhase(String phase, double max) {
			super.setStatus(phase, max);
		}
		
		@Override
		protected void execute() throws Exception {
            try {
                log(iImport ? "Importing "+iForm.getFile().getFileName()+" ("+iForm.getFile().getFileSize()+" bytes)..." : "Exporting " + iForm.getExportType().getType() + "...");
            	Long start = System.currentTimeMillis() ;
            	executeDataExchange();
                Long stop = System.currentTimeMillis() ;
                log((iImport ? "Import" : "Export") + " finished in "+new DecimalFormat("0.00").format((stop-start)/1000.0)+" seconds.");
            } catch (Exception e) {
                error("Unable to " + (iImport ? "import " + iForm.getFile().getFileName() : "export") + ": " + e.getMessage());
                Debug.error(e);
                setError(e);
            } finally {
            	Progress.removeInstance(this);
            }
            if (iForm.getEmail()) {
            	String address = iForm.getAddress();
            	if (address == null || address.isEmpty()) address = getOwnerEmail();
            	if (address != null && !address.isEmpty()) {
                    try {
                    	Email mail = Email.createEmail();
                    	mail.setSubject("Data " + (iImport ? "import" : "export") + " finished.");
                    	mail.setHTML(log()+"<br><br>"+
                                "This email was automatically generated at "+
                                iUrl+
                                ", by "+
                                "UniTime "+Constants.getVersion()+
                                " (Univesity Timetabling Application, http://www.unitime.org).");
                    	mail.addRecipient(address, getOwnerName());
                    	if (ApplicationProperty.EmailNotificationDataExchange.isTrue())
                    		mail.addNotifyCC();
                        if (!iImport && hasOutput() && output().exists()) 
                        	mail.addAttachment(output(), iSessionName + "_" + iForm.getExportType().getType() + "." + output().getName().substring(output().getName().lastIndexOf('.') + 1));
                        mail.send();
                    } catch (Exception e) {
                    	error("Unable to send email: " + e.getMessage());
                        Debug.error(e);
                        setError(e);
                    }
            	}
            }
		}
	}
	
	public static class ImportQueItem extends DataExchangeQueueItem {
		private static final long serialVersionUID = 1L;

		public ImportQueItem(Session session, UserContext owner, DataImportForm form, HttpServletRequest request) {
			super(session, owner, form, request, true);
		}

		@Override
		protected void executeDataExchange() throws Exception {
			if (iForm.getFile().getFileName().toLowerCase().endsWith(".dat")) {
				SessionRestoreInterface restore = (SessionRestoreInterface)Class.forName(ApplicationProperty.SessionRestoreInterface.value()).getConstructor().newInstance();
				restore.restore(iForm.getFile().getInputStream(), this);
			} else if (iForm.getFile().getFileName().toLowerCase().endsWith(".dat.gz") || iForm.getFile().getFileName().toLowerCase().endsWith(".zdat")) {
				SessionRestoreInterface restore = (SessionRestoreInterface)Class.forName(ApplicationProperty.SessionRestoreInterface.value()).getConstructor().newInstance();
				GZIPInputStream gzipInput = new GZIPInputStream(iForm.getFile().getInputStream());
				restore.restore(gzipInput, this);
				gzipInput.close();
			} else if (iForm.getFile().getFileName().toLowerCase().endsWith(".xml.gz") || iForm.getFile().getFileName().toLowerCase().endsWith(".zxml")) {
				GZIPInputStream gzipInput = new GZIPInputStream(iForm.getFile().getInputStream());
				DataExchangeHelper.importDocument((new SAXReader()).read(gzipInput), getOwnerId(), this);
				gzipInput.close();
			} else if (iForm.getFile().getFileName().toLowerCase().endsWith(".zip")) {
				ZipInputStream zipInput = new ZipInputStream(iForm.getFile().getInputStream());
				ZipEntry ze = null;
				while ((ze = zipInput.getNextEntry()) != null) {
					if (ze.isDirectory()) continue;
					setStatus("Importing " + ze.getName() + "...");
					if (ze.getName().endsWith(".dat")) {
						SessionRestoreInterface restore = (SessionRestoreInterface)Class.forName(ApplicationProperty.SessionRestoreInterface.value()).getConstructor().newInstance();
						restore.restore(zipInput, this);
					} else {
						DataExchangeHelper.importDocument((new SAXReader()).read(new NotClosingInputStream(zipInput)), getOwnerId(), this);
					}
				}
				zipInput.close();
			} else {
				DataExchangeHelper.importDocument((new SAXReader()).read(iForm.getFile().getInputStream()), getOwnerId(), this);
			}
		}

	}
	
	public static class ExportQueItem extends DataExchangeQueueItem {
		private static final long serialVersionUID = 1L;
		
		public ExportQueItem(Session session, UserContext owner, DataImportForm form, HttpServletRequest request) {
			super(session, owner, form, request, false);
		}

		@Override
		protected void executeDataExchange() throws Exception {
        	ExportType type = iForm.getExportType();
        	if (type == ExportType.SESSION) {
    			FileOutputStream out = new FileOutputStream(createOutput("session", "dat"));
    			try {
    				SessionBackupInterface backup = (SessionBackupInterface)Class.forName(ApplicationProperty.SessionBackupInterface.value()).getConstructor().newInstance();
    				backup.backup(out, this, getSessionId());
    			} finally {
    				out.close();
    			}
        	} else {
                Properties params = new Properties();
                type.setOptions(params);
                Document document = DataExchangeHelper.exportDocument(type.getType(), getSession(), params, this);
                if (document==null) {
                    error("XML document not created: unknown reason.");
                } else {
                    FileOutputStream fos = new FileOutputStream(createOutput(type.getType(), "xml"));
                    try {
                        (new XMLWriter(fos,OutputFormat.createPrettyPrint())).write(document);
                        fos.flush();
                    } finally {
                    	fos.close();
                    }
                }
        	}
		}
	}
	
	public static class NotClosingInputStream extends InputStream {
		private InputStream iParent;
		
		public NotClosingInputStream(InputStream in) { iParent = in; }

		@Override
		public int read() throws IOException { return iParent.read(); }
		
		@Override
		public int read(byte b[], int off, int len) throws IOException { return iParent.read(b, off, len); }
		
		@Override
		public long skip(long n) throws IOException { return iParent.skip(n); }
		
		@Override
		public int available() throws IOException { return iParent.available(); }
		
		@Override
		public void close() throws IOException {}
	}
}