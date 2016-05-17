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
package org.unitime.timetable.gwt.client.solver;

import java.util.Date;
import java.util.List;

import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.SolverInterface.ProgressLogLevel;
import org.unitime.timetable.gwt.shared.SolverInterface.ProgressMessage;
import org.unitime.timetable.gwt.shared.SolverInterface.SolutionLog;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverLogPageRequest;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverLogPageResponse;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;

/**
 * @author Tomas Muller
 */
public class SolverLogPage extends SimpleForm {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static final GwtResources RESOURCES = GWT.create(GwtResources.class);
	protected static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	protected static DateTimeFormat sLogDateFormat = DateTimeFormat.getFormat(CONSTANTS.timeStampFormatSolverLog());
	
	private SolverType iType;
	private UniTimeHeaderPanel iHeader, iFooter;
	private ProgressLog iLog;
	private ListBox iLevel;
	private int iLevelRow = -1;
	private Timer iTimer = null;
	
	public SolverLogPage() {
		addStyleName("unitime-SolverPage");
		iType = SolverType.valueOf(Location.getParameter("type").toUpperCase());
		switch (iType) {
		case COURSE:
			UniTimePageLabel.getInstance().setPageName(MESSAGES.pageCourseTimetablingSolverLog());
			break;
		case EXAM:
			UniTimePageLabel.getInstance().setPageName(MESSAGES.pageExaminationTimetablingSolverLog());
			break;
		case INSTRUCTOR:
			UniTimePageLabel.getInstance().setPageName(MESSAGES.pageInstructorSchedulingSolverLog());
			break;
		case STUDENT:
			UniTimePageLabel.getInstance().setPageName(MESSAGES.pageStudentSchedulingSolverLog());
			break;
		}
		
		iHeader = new UniTimeHeaderPanel(MESSAGES.sectSolverLog());
		iHeader.addButton("refresh", MESSAGES.opSolverLogRefresh(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				refresh();
			}
		});
		addHeaderRow(iHeader);
		
		iLevel = new ListBox();
		for (String level: CONSTANTS.progressLogLevel())
			iLevel.addItem(level);
		iLevel.setSelectedIndex(SolverCookie.getInstance().getLogLevel());
		iLevelRow = addRow(MESSAGES.propSolverLogLevel(), iLevel);
		getCellFormatter().getElement(iLevelRow, 1).getStyle().setWidth(100, Unit.PCT);
		iLevel.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				refresh();
			}
		});
		
		iFooter = iHeader.clonePanel("");
		
		iTimer = new Timer() {
			@Override
			public void run() {
				if (iLog != null && iLog.getLastDate() != null) {
					int level = SolverCookie.getInstance().getLogLevel();
					RPC.execute(new SolverLogPageRequest(iType, ProgressLogLevel.values()[level], iLog.getLastDate()), new AsyncCallback<SolverLogPageResponse>() {
						@Override
						public void onFailure(Throwable caught) {}
						
						@Override
						public void onSuccess(SolverLogPageResponse result) {
							if (result.hasLog())
								iLog.append(result.getLog());
						}
					});
				}
			}
		};
		
		refresh();
	}
	
	public void refresh() {
		int level = iLevel.getSelectedIndex();
		SolverCookie.getInstance().setLogLevel(level);
		iHeader.showLoading();
		iTimer.cancel();
		RPC.execute(new SolverLogPageRequest(iType, ProgressLogLevel.values()[level], null), new AsyncCallback<SolverLogPageResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				iHeader.setErrorMessage(caught.getMessage());
			}

			@Override
			public void onSuccess(SolverLogPageResponse result) {
				iHeader.clearMessage();
				for (int row = getRowCount() - 1; row > iLevelRow; row--)
					removeRow(row);
				if (result.hasLog()) {
					iLog = new ProgressLog(result.getLog());
					addRow(iLog);
					addBottomRow(iFooter);
					iTimer.scheduleRepeating(1000);
				} else if (result.hasSolutionLogs()) {
					for (SolutionLog log: result.getSolutionLogs()) {
						if (log.hasLog()) {
							addHeaderRow(log.getOwner());
							addRow(new ProgressLog(log.getLog()));
						}
					}
					addBottomRow(iFooter);
				}
			}
		});
	}
	
	public static class ProgressLog extends HTML implements TakesValue<List<ProgressMessage>>{
		private List<ProgressMessage> iLog;
		
		public ProgressLog(List<ProgressMessage> log) {
			setValue(log);
		}
		
		public static String htmlTraceLog(ProgressMessage m) {
			String trace[] = m.getStackTrace();
			if (trace == null || trace.length == 0) return "";
			StringBuffer ret = new StringBuffer("<BR>" + trace[0]);
            for (int i = 1; i < trace.length; i++)
                ret.append("<BR>&nbsp;&nbsp;&nbsp;&nbsp;at " + trace[i]);
            return ret.toString();
		}
		
		public static String toString(ProgressMessage m) {
			switch (m.getLevel()) {
			case TRACE:
				return sLogDateFormat.format(m.getDate()) + " &nbsp;&nbsp;&nbsp;&nbsp;-- " + m.getMessage() + htmlTraceLog(m);
			case DEBUG:
				return sLogDateFormat.format(m.getDate()) + " &nbsp;&nbsp;-- " + m.getMessage() + htmlTraceLog(m);
			case PROGRESS:
			case INFO:
				return sLogDateFormat.format(m.getDate()) + " " + m.getMessage() + htmlTraceLog(m);
			case STAGE:
				return "<br>" + sLogDateFormat.format(m.getDate()) + " <span style='font-weight:bold;'>" + m.getMessage() + "</span>" + htmlTraceLog(m);
			case WARN:
				return sLogDateFormat.format(m.getDate()) + " <span style='color:orange;font-weight:bold;'>" + CONSTANTS.progressLogLevel()[m.getLevel().ordinal()].toUpperCase() + ":</span> " + m.getMessage() + htmlTraceLog(m);
			case ERROR:
				return sLogDateFormat.format(m.getDate()) + " <span style='color:red;font-weight:bold;'>" + CONSTANTS.progressLogLevel()[m.getLevel().ordinal()].toUpperCase() + ":</span> " + m.getMessage() + htmlTraceLog(m);
			case FATAL:
				return sLogDateFormat.format(m.getDate()) + " <span style='color:red;font-weight:bold;'>&gt;&gt;&gt;" + CONSTANTS.progressLogLevel()[m.getLevel().ordinal()].toUpperCase() + ": " + m.getMessage() + " &lt;&lt;&lt;</span>" + htmlTraceLog(m);
			default:
				return sLogDateFormat.format(m.getDate()) + " " + m.getMessage() + htmlTraceLog(m);
			}
		}

		@Override
		public void setValue(List<ProgressMessage> value) {
			iLog = value;
			StringBuffer s = new StringBuffer();
			if (iLog != null) {
				for (ProgressMessage m: iLog) {
					if (s.length() > 0) s.append("<br>");
					s.append(toString(m));
				}
			}
			setHTML(s.toString());
		}

		@Override
		public List<ProgressMessage> getValue() {
			return iLog;
		}
		
		public Date getLastDate() {
			if (iLog == null || iLog.isEmpty()) return null;
			return iLog.get(iLog.size() - 1).getDate();
		}
		
		public void append(List<ProgressMessage> log) {
			StringBuffer s = new StringBuffer(getHTML());
			for (ProgressMessage m: log) {
				iLog.add(m);
				if (s.length() > 0) s.append("<br>");
				s.append(toString(m));
			}
			setHTML(s.toString());
		}
	}

}
