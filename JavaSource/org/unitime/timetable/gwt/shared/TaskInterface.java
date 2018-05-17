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
package org.unitime.timetable.gwt.shared;

import java.util.ArrayList;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionInfo;
import org.unitime.timetable.gwt.shared.EventInterface.ContactInterface;
import org.unitime.timetable.gwt.shared.EventInterface.SessionMonth;

import com.google.gwt.user.client.rpc.IsSerializable;
/**
 * @author Tomas Muller
 */
public class TaskInterface implements GwtRpcResponse, Comparable<TaskInterface> {
	private Long iId;
	private String iName, iEmail;
	private ScriptInterface iScript;
	private boolean iCanEdit = false, iCanView = false;
	private Date iLastExecuted;
	private ExecutionStatus iLastStatus;
	private Map<String, String> iParameters = new HashMap<String, String>();
	private ContactInterface iOwner;
	private TreeSet<TaskExecutionInterface> iExecutions;
	
	public static enum ExecutionStatus {
		CREATED,
		QUEUED,
		RUNNING,
		FINISHED,
		FAILED,
	}
	
	public TaskInterface() {}
	
	public TaskInterface(TaskInterface t) {
		iId = t.getId();
		iName = t.getName();
		iEmail = t.getEmail();
		iScript = t.getScript();
		iCanEdit = t.canEdit(); iCanView = t.canView();
		iLastExecuted = t.getLastExecuted();
		iLastStatus = t.getLastStatus();
		iParameters = (t.getParameters() == null ? null : new HashMap<String, String>(t.getParameters()));
		iOwner = t.getOwner();
		if (t.getExecutions() != null)
			for (TaskExecutionInterface e: t.getExecutions())
				addExecution(new TaskExecutionInterface(e));
	}
	
	public Long getId() { return iId; }
	public void setId(Long id) { iId = id; }
	
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	public String getEmail() { return iEmail; }
	public void setEmail(String email) { iEmail = email; }
	
	public ScriptInterface getScript() { return iScript; }
	public void setScript(ScriptInterface script) { iScript = script; }
	
	public boolean canEdit() { return iCanEdit; }
	public void setCanEdit(boolean canEdit) { iCanEdit = canEdit; }

	public boolean canView() { return iCanView; }
	public void setCanView(boolean canView) { iCanView = canView; }

	public Date getLastExecuted() { return iLastExecuted; }
	public void setLastExecuted(Date lastExecuted) { iLastExecuted = lastExecuted; }
	
	public ExecutionStatus getLastStatus() { return iLastStatus; }
	public void setLastStatus(ExecutionStatus lastStatus) { iLastStatus = lastStatus; }
	
	public void setParameter(String name, String value) { iParameters.put(name, value); }
	public String getParameter(String name) { return iParameters.get(name); }
	public Map<String, String> getParameters() { return iParameters; }
	public void clearParameters() { if (iParameters != null) iParameters.clear(); }
	
	public ContactInterface getOwner() { return iOwner; }
	public void setOwner(ContactInterface owner) { iOwner = owner; }
	
	public boolean hasExecutions() { return iExecutions != null && !iExecutions.isEmpty(); }
	public void addExecution(TaskExecutionInterface execution) {
		if (iExecutions == null) iExecutions = new TreeSet<TaskExecutionInterface>();
		iExecutions.add(execution);
	}
	public TreeSet<TaskExecutionInterface> getExecutions() { return iExecutions; }
	public void setExecutions(TreeSet<TaskExecutionInterface> Executions) { iExecutions = Executions; }
	public boolean hasExecutionsOfStatus(ExecutionStatus status) {
		if (iExecutions == null) return false;
		for (TaskExecutionInterface execution: iExecutions)
			if (status == execution.getStatus()) return true;
		return false;
	}
	public void clearExecutions() { if (iExecutions != null) iExecutions.clear(); }

	@Override
	public int compareTo(TaskInterface o) {
		return getName().compareTo(o.getName());
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	public static class TaskExecutionInterface implements IsSerializable, Comparable<TaskExecutionInterface> {
		private Long iId;
		private Integer iDayOfYear;
		private Integer iSlot;
		private ExecutionStatus iStatus;
		private Date iCreated, iQueued, iStarted, iFinished;
		private String iOutput;
		private Date iExecutionDate;
		private int iDayOfWeek;
		private String iStatusMessage;
		
		public TaskExecutionInterface() {}
		
		public TaskExecutionInterface(TaskExecutionInterface e) {
			iId = e.getId();
			iDayOfYear = e.getDayOfYear();
			iSlot = e.getSlot();
			iStatus = e.getStatus();
			iCreated = e.getCreated(); iQueued = e.getQueued(); iStarted = e.getStarted(); iFinished = e.getFinished();
			iOutput = e.getOutput();
			iExecutionDate = e.getExecutionDate();
			iDayOfWeek = e.getDayOfWeek();
		}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		
		public Integer getDayOfYear() { return iDayOfYear; }
		public boolean hasDayOfYear() { return iDayOfYear != null; }
		public void setDayOfYear(Integer dayOfYear) { iDayOfYear = dayOfYear; }
		
		public Integer getSlot() { return iSlot; }
		public boolean hasSlot() { return iSlot != null; }
		public void setSlot(Integer slot) { iSlot = slot; }
		
		public boolean hasStatus() { return iStatus != null; }
		public ExecutionStatus getStatus() { return iStatus; }
		public void setStatus(ExecutionStatus status) { iStatus = status; }

		public boolean hasCreated() { return iCreated != null; }
		public Date getCreated() { return iCreated; }
		public void setCreated(Date date) { iCreated = date; }
		
		public boolean hasQueued() { return iQueued != null; }
		public Date getQueued() { return iQueued; }
		public void setQueued(Date date) { iQueued = date; }
		
		public boolean hasStarted() { return iStarted != null; }
		public Date getStarted() { return iStarted; }
		public void setStarted(Date date) { iStarted = date; }
		
		public boolean hasFinished() { return iFinished != null; }
		public Date getFinished() { return iFinished; }
		public void setFinished(Date date) { iFinished = date; }
		
		public boolean hasOutput() { return iOutput != null && !iOutput.isEmpty(); }
		public String getOutput() { return iOutput; }
		public void setOutput(String output) { iOutput = output; }
		
		public Date getExecutionDate() { return iExecutionDate; }
		public void setExecutionDate(Date date) { iExecutionDate = date; }

		public int getDayOfWeek() { return iDayOfWeek; }
		public void setDayOfWeek(int dayOfWeek) { iDayOfWeek = dayOfWeek; }
		
		public boolean hasStatusMessage() { return iStatusMessage != null && !iStatusMessage.isEmpty(); }
		public void setStatusMessage(String statusMessage) { iStatusMessage = statusMessage; }
		public String getStatusMessage() { return iStatusMessage; }
		
		public boolean isPast() {
			return hasFinished();
		}
		
		public String getExecutionTime(GwtConstants constants) {
			int min = 5 * iSlot;
			int h = min / 60;
	        int m = min % 60;
	        if (constants != null && min == 0)
	        	return constants.timeMidnight();
	        if (constants != null && min == 720)
	        	return constants.timeNoon();
	        if (constants == null || constants.useAmPm()) {
	        	return (h > 12 ? h - 12 : h) + ":" + (m < 10 ? "0" : "") + m + (h == 24 ? "a" : h >= 12 ? "p" : "a");
			} else {
				return h + ":" + (m < 10 ? "0" : "") + m;
			}
		}

		@Override
		public int compareTo(TaskExecutionInterface exec) {
			int cmp = getDayOfYear().compareTo(exec.getDayOfYear());
			if (cmp != 0) return cmp;
			cmp = new Integer(getSlot()).compareTo(exec.getSlot());
			if (cmp != 0) return cmp;
			return (getId() == null ? exec.getId() == null ? 0 : -1 : exec.getId() == null ? 1 : getId().compareTo(exec.getId()));
		}
	}
	
	public static class MultiExecutionInterface implements Comparable<MultiExecutionInterface>, IsSerializable {
	    private TreeSet<TaskExecutionInterface> iExecutions;
	    private boolean iPast = false;
	    
	    public MultiExecutionInterface(TreeSet<TaskExecutionInterface> executions, boolean past) {
	    	iExecutions = executions;
	        iPast = past;
	    }
	    
	    public boolean isPast() { return iPast; }
	    
	    public TreeSet<TaskExecutionInterface> getExecutions() { return iExecutions; }

	    public int compareTo(MultiExecutionInterface m) {
	        return getExecutions().first().compareTo(m.getExecutions().first());
	    }
	    
	    public String getDays() {
	        return getDays(0, new String[] {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"}, new String[] {"M", "T", "W", "Th", "F", "S", "Su"}, "Daily");
	    }
	    
	    public String getDays(int firstDayOfWeek, GwtConstants constants) {
	    	return getDays(firstDayOfWeek, constants.days(), constants.shortDays(), constants.daily());
	    }
	    
	    public String getDays(int firstDayOfWeek, String[] dayNames, String[] shortDyNames, String daily) {
	    	int nrDays = 0;
	        int dayCode = 0;
	        for (TaskExecutionInterface meeting : getExecutions()) {
	        	int dc = (1 << meeting.getDayOfWeek());
	            if ((dayCode & dc)==0) nrDays++;
	            dayCode |= dc;
	        }
	        if (nrDays == 7) return daily;
	        String ret = "";
	        for (int i = 0; i < 7; i++) {
	        	int d = (i + firstDayOfWeek) % 7;
	        	if ((dayCode & (1 << d)) != 0)
	        		ret += (nrDays == 1 ? dayNames : shortDyNames)[d];
	        }
	        return ret;
	    }
	    
	    public Date getFirstExecutionDate() {
	    	return iExecutions.first().getExecutionDate();
	    }
	    
	    public Date getLastExecutionDate() {
	    	return iExecutions.last().getExecutionDate();
	    }
	    
	    public String getExecutionTime(GwtConstants constants) {
	    	return iExecutions.first().getExecutionTime(constants);
		}
	    
		public int getNrMeetings() {
	    	return iExecutions.size();
	    }
		
		public ExecutionStatus getStatus() {
			return iExecutions.first().getStatus();
		}
	}
	
    public static TreeSet<MultiExecutionInterface> getMultiExecutions(Collection<TaskExecutionInterface> executions, boolean checkPast) {
        TreeSet<MultiExecutionInterface> ret = new TreeSet<MultiExecutionInterface>();
        HashSet<TaskExecutionInterface> executionSet = new HashSet<TaskExecutionInterface>(executions);
        while (!executionSet.isEmpty()) {
        	TaskExecutionInterface execution = null;
            for (TaskExecutionInterface t : executionSet)
                if (execution==null || execution.compareTo(t) > 0)
                	execution = t;
            executionSet.remove(execution);
            HashMap<Integer,TaskExecutionInterface> similar = new HashMap<Integer, TaskExecutionInterface>(); 
            TreeSet<Integer> dow = new TreeSet<Integer>(); dow.add(execution.getDayOfWeek());
            for (TaskExecutionInterface m : executionSet) {
            	if (m.getExecutionTime(null).equals(execution.getExecutionTime(null)) &&
            		(!checkPast || m.isPast() == execution.isPast()) && 
            		(m.getStatus() == execution.getStatus())) {
            		if (m.getDayOfYear() - execution.getDayOfYear() < 7) dow.add(m.getDayOfWeek());
                    similar.put(m.getDayOfYear(),m);
                }
            }
            TreeSet<TaskExecutionInterface> multi = new TreeSet<TaskExecutionInterface>(); multi.add(execution);
            if (!similar.isEmpty()) {
            	int w = execution.getDayOfWeek();
            	int y = execution.getDayOfYear();
            	while (true) {
            		do {
            			y ++;
            			w = (w + 1) % 7;
            		} while (!dow.contains(w));
            		TaskExecutionInterface m = similar.get(y);
            		if (m == null) break;
            		multi.add(m);
            		executionSet.remove(m);
            	}
            }
            ret.add(new MultiExecutionInterface(multi, execution.isPast()));
        }
        return ret;
    }
    
    public static class TaskOptionsInterface implements GwtRpcResponse {
		private boolean iCanAdd;
		private ContactInterface iManager;
		private List<ScriptInterface> iScripts = new ArrayList<ScriptInterface>();
		private AcademicSessionInfo iSession;
		private List<SessionMonth> iMonths = new ArrayList<SessionMonth>();
		private int iFirstDayOfWeek = 0;
		
		public TaskOptionsInterface() {
		}
		
		public void addScript(ScriptInterface script) { iScripts.add(script); }
		public List<ScriptInterface> getScripts() { return iScripts; }
		
		public void setSession(AcademicSessionInfo session) { iSession = session; }
		public AcademicSessionInfo getSession() { return iSession; }
		
		public boolean canAdd() { return iCanAdd; }
		public void setCanAdd(boolean canAdd) { iCanAdd = canAdd; }
		
		public ContactInterface getManager() { return iManager; }
		public void setManager(ContactInterface manager) { iManager = manager; }
		
		public void setSessionMonth(List<SessionMonth> months) { iMonths = months; }
		public List<SessionMonth> getSessionMonths() { return iMonths; }
		
		public int getFirstDayOfWeek() { return iFirstDayOfWeek; }
		public void setFirstDayOfWeek(int firstDayOfWeek) { iFirstDayOfWeek = firstDayOfWeek; }
	}
    
    public static class GetTasksRpcRequest implements GwtRpcRequest<GwtRpcResponseList<TaskInterface>> {
    	public GetTasksRpcRequest() {}
    }
    
    public static class GetTaskExecutionLogRpcRequest implements GwtRpcRequest<TaskExecutionLogInterface> {
    	private Long iTaskExecutionId;
    	
    	public GetTaskExecutionLogRpcRequest() {}
    	public GetTaskExecutionLogRpcRequest(Long execId) { iTaskExecutionId = execId; }
    	
    	public Long getTaskExecutionId() { return iTaskExecutionId; }
    	public void setTaskExecutionId(Long id) { iTaskExecutionId = id; }
    }
    
    public static class TaskExecutionLogInterface implements GwtRpcResponse {
    	String iLog;
    	
    	public TaskExecutionLogInterface() {}
    	public TaskExecutionLogInterface(String log) { iLog = log; }
    	
    	public boolean hasLog() { return iLog != null && !iLog.isEmpty(); }
    	public String getLog() { return iLog; }
    	public void setLog(String log) { iLog = log; }
    }
    
    public static class SaveTaskDetailsRpcRequest implements GwtRpcRequest<TaskInterface> {
    	private TaskInterface iTask;
    	
    	public SaveTaskDetailsRpcRequest() {}
    	public SaveTaskDetailsRpcRequest(TaskInterface task) { iTask = task; }
    	
    	public TaskInterface getTask() { return iTask; }
    	public void setTask(TaskInterface task) { iTask = task; }
    }
    
    public static class DeleteTaskDetailsRpcRequest implements GwtRpcRequest<TaskInterface> {
    	private Long iTaskId;
    	
    	public DeleteTaskDetailsRpcRequest() {}
    	public DeleteTaskDetailsRpcRequest(Long taskId) { iTaskId = taskId; }
    	
    	public Long getTaskId() { return iTaskId; }
    	public void setTaskId(Long id) { iTaskId = id; }
    }
    
    public static class GetTaskOptionsRpcRequest implements GwtRpcRequest<TaskOptionsInterface> {
		public GetTaskOptionsRpcRequest() {}
	}
}
