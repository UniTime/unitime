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
package org.unitime.timetable.server.pointintimedata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.PointInTimeDataReportsInterface;
import org.unitime.timetable.gwt.shared.PointInTimeDataReportsInterface.PITDQueriesRpcRequest;
import org.unitime.timetable.gwt.shared.PointInTimeDataReportsInterface.Parameter;
import org.unitime.timetable.gwt.shared.PointInTimeDataReportsInterface.Report;
import org.unitime.timetable.reports.pointintimedata.BasePointInTimeDataReports;
import org.unitime.timetable.security.SessionContext;

/**
 * @author Stephanie Schluttenhofer
 */
@GwtRpcImplements(PITDQueriesRpcRequest.class)
public class PITDQueriesBackend implements GwtRpcImplementation<PITDQueriesRpcRequest, GwtRpcResponseList<Report>>{
	@Autowired 
	private SessionContext sessionContext;
	
	@Override
	@PreAuthorize("checkPermission('PointInTimeDataReports')")
	public GwtRpcResponseList<Report> execute(PITDQueriesRpcRequest request, SessionContext context) {
		GwtRpcResponseList<PointInTimeDataReportsInterface.Report> ret = new GwtRpcResponseList<PointInTimeDataReportsInterface.Report>(); 
		for (String key: BasePointInTimeDataReports.sPointInTimeDataReportRegister.keySet()) {
			Class rptCls = BasePointInTimeDataReports.sPointInTimeDataReportRegister.get(key);
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
			if (rpt != null) {
				PointInTimeDataReportsInterface.Report report = new PointInTimeDataReportsInterface.Report();
				report.setName(rpt.reportName());
				report.setDescription(rpt.reportDescription());
				for (org.unitime.timetable.reports.pointintimedata.BasePointInTimeDataReports.Parameter parameter : rpt.getParameters()) {
					Parameter rptParam = new Parameter();
					rptParam.setType(parameter.name());
					rptParam.setDefaultTextValue(parameter.defaultValue(sessionContext.getUser()));
					rptParam.setMultiSelect(parameter.allowMultiSelection());
					rptParam.setName(parameter.name());
					rptParam.setTextField(parameter.isTextField());
					report.addParameter(rptParam);
				}
				report.setFlags(255);
				report.setId(key);
				ret.add(report);
			}
		}
		return ret;
	}

}
