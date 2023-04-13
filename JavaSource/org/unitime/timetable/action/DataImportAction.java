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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.cpsolver.ifs.util.Progress;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.unitime.commons.Debug;
import org.unitime.commons.Email;
import org.unitime.commons.web.WebTable;
import org.unitime.commons.web.WebTable.WebTableLine;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
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
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.queue.QueueItem;


/** 
 * @author Tomas Muller
 */
@Action(value="dataImport", results = {
		@Result(name="display", type = "tiles", location="dataImport.tiles"),
		@Result(name="input", type = "tiles", location="dataImport.tiles"),
	}, interceptorRefs = {
		@InterceptorRef(value = "fileUpload"),
		@InterceptorRef(value = "defaultStack")
	})
@TilesDefinition(name = "dataImport.tiles", extend = "baseLayout", putAttributes = {
		@TilesPutAttribute(name = "title", value = "Data Exchange"),
		@TilesPutAttribute(name = "body", value = "/admin/dataImport.jsp")
	})
public class DataImportAction extends UniTimeAction<DataImportForm> {
	private static final long serialVersionUID = 3163553928537551939L;
	protected static CourseMessages MSG = Localization.create(CourseMessages.class);
	private String log;
	private String remove;
	private String ord;
	
	public String getLog() { return log; }
	public void setLog(String log) { this.log = log; }
	
	public String getRemove() { return remove; }
	public void setRemove(String remove) { this.remove = remove; }
	
	public String getOrd() { return ord; }
	public void setOrd(String ord) { this.ord = ord; }
	
	public String execute() throws Exception {
		if (form == null) {
			form = new DataImportForm();
			form.setAddress(sessionContext.getUser().getEmail());
		}
		
		sessionContext.checkPermission(Right.DataExchange);
		
		Session session = SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId());
		
		if (MSG.actionImport().equals(form.getOp())) {
			form.validate(this);
			if (!hasFieldErrors())
				getSolverServerService().getQueueProcessor().add(new ImportQueItem(session, sessionContext.getUser(), form, request));
        }
        
        if (MSG.actionExport().equals(form.getOp())) {
        	form.validate(this);
			if (!hasFieldErrors())
				getSolverServerService().getQueueProcessor().add(new ExportQueItem(session, sessionContext.getUser(), form, request));
        }
        
        if (getRemove() != null) {
        	getSolverServerService().getQueueProcessor().remove(getRemove());
        }
        
        WebTable table = getQueueTable(request);
        if (table != null) {
        	request.setAttribute("table", table.printTable(WebTable.getOrder(sessionContext,"dataImport.ord")));
        }
		
		return "display";
	}
	
	private WebTable getQueueTable(HttpServletRequest request) {
        WebTable.setOrder(sessionContext, "dataImport.ord", getOrd(), 1);
		String log = request.getParameter("log");
		Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.TIME_SHORT);
		List<QueueItem> queue = getSolverServerService().getQueueProcessor().getItems(null, null, "Data Exchange");
		if (queue.isEmpty()) return null;
		WebTable table = new WebTable(9, MSG.sectionDataExchangeQueue(), "dataImport.action?ord=%%",
				new String[] { MSG.fieldQueueName(), MSG.fieldQueueStatus(), MSG.fieldQueueProgress(), MSG.fieldQueueOwner(),
						MSG.fieldQueueSession(), MSG.fieldQueueCreated(), MSG.fieldQueueStarted(), MSG.fieldQueueFinished(),
						MSG.fieldQueueOutput()},
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
				delete = "<img src='images/action_delete.png' border='0' onClick=\"if (confirm('" + MSG.questionDeleteDataExchangeItem() + "')) document.location='dataImport.action?remove="+item.getId()+"'; event.cancelBubble=true;\">";
			}
			WebTableLine line = table.addLine("onClick=\"document.location='dataImport.action?log=" + item.getId() + "';\"",
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
		String iFileName = null;
		
		public DataExchangeQueueItem(Session session, UserContext owner, DataImportForm form, HttpServletRequest request, boolean isImport) {
			super(session, owner);
			iForm = (DataImportForm)form.clone();
			iUrl = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath();
			iImport = isImport;
			iSessionName = session.getAcademicTerm() + session.getAcademicYear() + session.getAcademicInitiative();
			if (iForm.getFile() != null) iFileName = iForm.getFileFileName();
		}
				
		@Override
		public String type() {
			return "Data Exchange";
		}
		
		@Override
		public String name() {
			return (iImport ? GWT_MSG.itemImportActionName(iFileName) : GWT_MSG.itemExportActionName(iForm.getExportType().getLabel()));
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
                log(iImport ? "Importing "+iForm.getFileFileName()+" ("+iForm.getFile().length()+" bytes)..." : "Exporting " + iForm.getExportType().getType() + "...");
            	Long start = System.currentTimeMillis() ;
            	executeDataExchange();
                Long stop = System.currentTimeMillis() ;
                log((iImport ? "Import" : "Export") + " finished in "+new DecimalFormat("0.00").format((stop-start)/1000.0)+" seconds.");
            } catch (Exception e) {
                error("Unable to " + (iImport ? "import " + iForm.getFileFileName() : "export") + ": " + e.getMessage());
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
			FileInputStream fis = new FileInputStream(iForm.getFile());
			try {
			if (iForm.getFileFileName().toLowerCase().endsWith(".dat")) {
				SessionRestoreInterface restore = (SessionRestoreInterface)Class.forName(ApplicationProperty.SessionRestoreInterface.value()).getConstructor().newInstance();
				restore.restore(fis, this);
			} else if (iForm.getFileFileName().toLowerCase().endsWith(".dat.gz") || iForm.getFileFileName().toLowerCase().endsWith(".zdat")) {
				SessionRestoreInterface restore = (SessionRestoreInterface)Class.forName(ApplicationProperty.SessionRestoreInterface.value()).getConstructor().newInstance();
				GZIPInputStream gzipInput = new GZIPInputStream(fis);
				restore.restore(gzipInput, this);
				gzipInput.close();
			} else if (iForm.getFileFileName().toLowerCase().endsWith(".xml.gz") || iForm.getFileFileName().toLowerCase().endsWith(".zxml")) {
				GZIPInputStream gzipInput = new GZIPInputStream(fis);
				DataExchangeHelper.importDocument((new SAXReader()).read(gzipInput), getOwnerId(), this);
				gzipInput.close();
			} else if (iForm.getFileFileName().toLowerCase().endsWith(".zip")) {
				ZipInputStream zipInput = new ZipInputStream(fis);
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
				DataExchangeHelper.importDocument((new SAXReader()).read(fis), getOwnerId(), this);
			}
			} finally {
				fis.close();
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
        		GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(createOutput("session", "dat.gz")));
    			try {
    				SessionBackupInterface backup = (SessionBackupInterface)Class.forName(ApplicationProperty.SessionBackupInterface.value()).getConstructor().newInstance();
    				backup.backup(out, this, getSessionId());
    			} finally {
    				out.flush();
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