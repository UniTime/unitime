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

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.fileupload2.core.FileItem;
import org.cpsolver.ifs.util.Progress;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.commons.Debug;
import org.unitime.commons.Email;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.action.DataImportAction.NotClosingInputStream;
import org.unitime.timetable.backup.SessionRestoreInterface;
import org.unitime.timetable.dataexchange.DataExchangeHelper;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.gwt.client.admin.DataExchangePage.DataImportExportResponse;
import org.unitime.timetable.gwt.client.admin.DataExchangePage.DataImportRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.service.SolverServerService;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.queue.QueueItem;

@GwtRpcImplements(DataImportRequest.class)
public class DataImportBackend implements GwtRpcImplementation<DataImportRequest, DataImportExportResponse>{
	protected static CourseMessages MSG = Localization.create(CourseMessages.class);

	@Autowired
	SolverServerService solverServerService;

	@Override
	public DataImportExportResponse execute(DataImportRequest request, SessionContext context) {
		context.checkPermission(Right.DataExchange);
		
		FileItem file = (FileItem)context.getAttribute(SessionAttribute.LastUploadedFile);
		if (file == null)
			throw new GwtRpcException(MSG.errorRequiredField(MSG.fieldFile()));
		
		QueueItem queue = solverServerService.getQueueProcessor().add(new ImportQueueItem(context, request, file));
		context.setAttribute(SessionAttribute.LastUploadedFile, null);
		
		return new DataImportExportResponse(queue.getId());
	}
	
	public static class ImportQueueItem extends DataExchangeQueueItem {
		private static final long serialVersionUID = 1L;
		private transient FileItem iFile;
		private String iEmail;
		private String iFileName;
		
		public ImportQueueItem(SessionContext context, DataImportRequest request, FileItem file) {
			super(context);
			iEmail = request.getEmail();
			iFile = file;
			iFileName = file.getName();
		}

		@Override
		protected void executeDataExchange() throws Exception {
			InputStream fis = iFile.getInputStream();
			try {
			if (iFileName.toLowerCase().endsWith(".dat")) {
				SessionRestoreInterface restore = (SessionRestoreInterface)Class.forName(ApplicationProperty.SessionRestoreInterface.value()).getConstructor().newInstance();
				restore.restore(fis, this);
			} else if (iFileName.toLowerCase().endsWith(".dat.gz") || iFileName.toLowerCase().endsWith(".zdat")) {
				SessionRestoreInterface restore = (SessionRestoreInterface)Class.forName(ApplicationProperty.SessionRestoreInterface.value()).getConstructor().newInstance();
				GZIPInputStream gzipInput = new GZIPInputStream(fis);
				restore.restore(gzipInput, this);
				gzipInput.close();
			} else if (iFileName.toLowerCase().endsWith(".xml.gz") || iFileName.toLowerCase().endsWith(".zxml")) {
				GZIPInputStream gzipInput = new GZIPInputStream(fis);
				DataExchangeHelper.importDocument((new SAXReader()).read(gzipInput), getOwnerId(), this);
				gzipInput.close();
			} else if (iFileName.toLowerCase().endsWith(".zip")) {
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
				iFile.delete();
			}
		}
		
		@Override
		public String name() {
			return GWT_MSG.itemImportActionName(iFileName);
		}
		
		@Override
		protected void execute() throws Exception {
            try {
                log("Importing " + iFileName + "...");
            	Long start = System.currentTimeMillis() ;
            	executeDataExchange();
                Long stop = System.currentTimeMillis() ;
                log("Import finished in "+new DecimalFormat("0.00").format((stop-start)/1000.0)+" seconds.");
            } catch (Exception e) {
                error("Unable to import: " + e.getMessage());
                Debug.error(e);
                setError(e);
            } finally {
            	Progress.removeInstance(this);
            }
            if (iEmail != null && !iEmail.isEmpty()) {
            	try {
                	Email mail = Email.createEmail();
                	mail.setSubject("Data import finished.");
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
