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
package org.unitime.timetable.server.administration.dataexchange;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Properties;
import java.util.zip.GZIPOutputStream;

import org.cpsolver.ifs.util.Progress;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.commons.Debug;
import org.unitime.commons.Email;
import org.unitime.timetable.backup.SessionBackupInterface;
import org.unitime.timetable.dataexchange.DataExchangeHelper;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.client.admin.DataExchangePage.DataExportRequest;
import org.unitime.timetable.gwt.client.admin.DataExchangePage.DataImportExportResponse;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.service.SolverServerService;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.queue.QueueItem;

@GwtRpcImplements(DataExportRequest.class)
public class DataExportBackend implements GwtRpcImplementation<DataExportRequest, DataImportExportResponse>{
	@Autowired
	SolverServerService solverServerService;

	@Override
	public DataImportExportResponse execute(DataExportRequest request, SessionContext context) {
		context.checkPermission(Right.DataExchange);
		
		QueueItem queue = solverServerService.getQueueProcessor().add(new ExportQueItem(context, request));
		
		return new DataImportExportResponse(queue.getId());
	}
	
	public static class ExportQueItem extends DataExchangeQueueItem {
		private static final long serialVersionUID = 1L;
		private ExportType iType;
		private String iEmail;
		
		public ExportQueItem(SessionContext context, DataExportRequest request) {
			super(context);
			iType = ExportType.valueOf(request.getExportType());
			iEmail = request.getEmail();
		}

		@Override
		protected void executeDataExchange() throws Exception {
        	if (iType == ExportType.SESSION) {
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
                iType.setOptions(params);
                Document document = DataExchangeHelper.exportDocument(iType.getType(), getSession(), params, this);
                if (document==null) {
                    error("XML document not created: unknown reason.");
                } else {
                	OutputStream fos = null;
                	if (ApplicationProperty.DataExchangeExportTypeGzip.isTrue(iType.getType())) {
                		fos = new GZIPOutputStream(new FileOutputStream(createOutput(iType.getType(), "xml.gz")));
                	} else {
                		fos = new FileOutputStream(createOutput(iType.getType(), "xml"));
                	}
                    try {
                        (new XMLWriter(fos,OutputFormat.createPrettyPrint())).write(document);
                        fos.flush();
                    } finally {
                    	fos.close();
                    }
                }
        	}
		}
		
		@Override
		public String name() {
			return GWT_MSG.itemExportActionName(iType.getLabel());
		}
		
		@Override
		protected void execute() throws Exception {
            try {
                log("Exporting " + iType.getType() + "...");
            	Long start = System.currentTimeMillis() ;
            	executeDataExchange();
                Long stop = System.currentTimeMillis() ;
                log("Export finished in "+new DecimalFormat("0.00").format((stop-start)/1000.0)+" seconds.");
            } catch (Exception e) {
                error("Unable to export: " + e.getMessage());
                Debug.error(e);
                setError(e);
            } finally {
            	Progress.removeInstance(this);
            }
            if (iEmail != null && !iEmail.isEmpty()) {
            	try {
                	Email mail = Email.createEmail();
                	mail.setSubject("Data export finished.");
                	String url = ApplicationProperty.UniTimeUrl.value();
                	if (url == null || url.isEmpty()) {
                    	mail.setHTML(log()+"<br><br>"+
                                "This email was automatically generated by "+
                                "UniTime "+Constants.getVersion()+
                                " (Univesity Timetabling Application, http://www.unitime.org).");
                	} else {
                    	mail.setHTML(log()+"<br><br>"+
                                "This email was automatically generated at " + url + ", by "+
                                "UniTime "+Constants.getVersion()+
                                " (Univesity Timetabling Application, http://www.unitime.org).");
                	}
                	mail.addRecipient(iEmail, getOwnerName());
                	if (ApplicationProperty.EmailNotificationDataExchange.isTrue())
                		mail.addNotifyCC();
                	if (hasOutput())
                		mail.addAttachment(output(), getSession().getLabel() + "_" + iType.getType() + "." + output().getName().substring(output().getName().lastIndexOf('.') + 1));
                    mail.send();
                } catch (Exception e) {
                	error("Unable to send email: " + e.getMessage());
                    Debug.error(e);
                    setError(e);
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
