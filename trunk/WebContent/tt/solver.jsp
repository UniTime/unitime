<%--
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
--%>
<%@page import="org.unitime.timetable.security.rights.Right"%>
<%@page import="java.text.DecimalFormat"%>
<%@page import="org.unitime.timetable.security.SessionContext"%>
<%@ page language="java" autoFlush="true"%>
<%@ page import="java.util.*" %>
<%@ page import="org.unitime.timetable.solver.WebSolver" %>
<%@ page import="org.unitime.timetable.solver.SolverProxy" %>
<%@ page import="org.unitime.timetable.model.dao.SolutionDAO" %>
<%@ page import="org.unitime.timetable.model.Solution" %>
<%@ page import="org.hibernate.Transaction" %>
<%@ page import="org.unitime.timetable.form.ListSolutionsForm" %>
<%@ page import="org.unitime.timetable.solver.ui.PropertiesInfo" %>
<%@ page import="org.cpsolver.ifs.util.Progress" %>
<%@ page import="org.unitime.timetable.solver.ui.LogInfo" %>
<%@ page import="org.unitime.timetable.form.SolverForm" %>
<%@ page import="org.unitime.timetable.webutil.JavascriptFunctions" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<tt:back-mark back="true" clear="true" title="Solver" uri="solver.do"/>
<tt:session-context/>
<SCRIPT language="javascript">
	<!--
		<%= JavascriptFunctions.getJsConfirm(sessionContext) %>
		
		function confirmUnload() {
			if (jsConfirm!=null && !jsConfirm) return;

			if (!confirm('Do you really want to unload your current timetable? You may lose this timetable if you did not save it.')) {
				solverForm.confirm.value='n';
			}
		}

		function confirmSave() {
			if (jsConfirm!=null && !jsConfirm) return;

			if (!confirm('Do you really want to save your current timetable? This will overwrite your previous solution.')) {
				solverForm.confirm.value='n';
			}
		}

		function confirmSaveAsNew() {
			if (jsConfirm!=null && !jsConfirm) return;

			if (!confirm('Do you really want to save your current timetable?')) {
				solverForm.confirm.value='n';
			}
		}

		function confirmSaveAndCommit() {
			if (jsConfirm!=null && !jsConfirm) return;

			if (!confirm('Do you really want to save and commit your current timetable? This will overwrite your previous solution. It may also uncommit your currently committed solution.')) {
				solverForm.confirm.value='n';
			}
		}

		function confirmSaveAsNewAndCommit() {
			if (jsConfirm!=null && !jsConfirm) return;

			if (!confirm('Do you really want to save and commit your current timetable? This may uncommit your currently committed solution.')) {
				solverForm.confirm.value='n';
			}
		}
	// -->
</SCRIPT>
<tiles:importAttribute />

<html:form action="/solver">
	<input type='hidden' name='confirm' value='y'/>
<%
try {
%>
<%
	SolverForm frm = (SolverForm)request.getAttribute("solverForm");

	SolverProxy solver = WebSolver.getSolver(session);
	String status = null;
	String progress = null;
	if (solver!=null) {
		Map p = solver.getProgress();
		status = (String)p.get("STATUS");
		long progressMax = ((Long)p.get("MAX_PROGRESS")).longValue();
		if (progressMax>0) {
			progress = (String)p.get("PHASE");
			long progressCur = ((Long)p.get("PROGRESS")).longValue();
			double progressPercent = 100.0*((double)(progressCur<progressMax?progressCur:progressMax))/((double)progressMax);
			progress+=" ("+new DecimalFormat("0.0").format(progressPercent)+"%)";
		}
	}
	if (status==null)
		status = "Solver not started.";
%>
<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
	<TR>
		<TD colspan="2">
			<DIV class="WelcomeRowHead">
			Solver
			</DIV>
		</TD>
	</TR>
<%
	if (solver!=null && solver.getLoadedDate()!=null) {
%>
		<TR><TD>Input data loaded:</TD><TD><%=WebSolver.sDF.format(solver.getLoadedDate())%></TD></TR>
<%
	}
%>
	<TR><TD>Status:</TD><TD><%=status%> <tt:wiki>Solver Status</tt:wiki></TD></TR>
<%  if (progress!=null) { %>
		<TR><TD>Progress:</TD><TD><%=progress%></TD></TR>
<%  } %>
<%  
	boolean disabled = (solver!=null && solver.isWorking());
%>
   	<TR><TD>Solver configuration:</TD>
		<TD>
			<html:select property="setting" onchange="submit();" disabled="<%=disabled%>">
				<html:optionsCollection name="solverForm" property="settings" label="value" value="id"/>
			</html:select>
			&nbsp;<html:errors property="setting"/>
		</TD>
	</TR>
	<logic:iterate id="parameter" name="solverForm" property="parameters" type="org.unitime.timetable.model.SolverPredefinedSetting.IdValue">
		<TR>
			<TD><bean:write name="parameter" property="value"/>:</TD>
			<TD>
			<logic:equal name="parameter" property="type" value="boolean">
				<html:checkbox property='<%= "parameterValue["+parameter.getId()+"]"%>' disabled="<%=disabled || parameter.getDisabled()%>"/>
  				&nbsp;<html:errors property='<%= "parameterValue["+parameter.getId()+"]"%>'/>
			</logic:equal>
			<% if (parameter.getType().startsWith("enum(") && parameter.getType().endsWith(")")) { %>
				<html:select property='<%="parameterValue["+parameter.getId()+"]"%>' disabled="<%=disabled || parameter.getDisabled()%>">
					<html:options property='<%=parameter.getType()%>'/>
				</html:select>
				&nbsp;<html:errors property='<%="parameterValue["+parameter.getId()+"]"%>'/>
			<% } %>
			<logic:equal name="parameter" property="type" value="double">
				<html:text property='<%="parameterValue["+parameter.getId()+"]"%>' size="10" maxlength="10" disabled="<%=disabled || parameter.getDisabled()%>"/>
  				&nbsp;<html:errors property='<%="parameterValue["+parameter.getId()+"]"%>'/>
  			</logic:equal>
			<logic:equal name="parameter" property="type" value="integer">
				<html:text property='<%="parameterValue["+parameter.getId()+"]"%>' size="10" maxlength="10" disabled="<%=disabled || parameter.getDisabled()%>"/>
  				&nbsp;<html:errors property='<%="parameterValue["+parameter.getId()+"]"%>'/>
  			</logic:equal>
			<logic:equal name="parameter" property="type" value="long">
				<html:text property='<%="parameterValue["+parameter.getId()+"]"%>' size="10" maxlength="10" disabled="<%=disabled || parameter.getDisabled()%>"/>
  				&nbsp;<html:errors property='<%="parameterValue["+parameter.getId()+"]"%>'/>
  			</logic:equal>
			<logic:equal name="parameter" property="type" value="text">
				<html:text property='<%="parameterValue["+parameter.getId()+"]"%>' size="30" maxlength="100" disabled="<%=disabled || parameter.getDisabled()%>"/>
  				&nbsp;<html:errors property='<%="parameterValue["+parameter.getId()+"]"%>'/>
			</logic:equal>	
			</TD>		
		</TR>
	</logic:iterate>
	<logic:notEmpty name="owners" scope="request">
		<bean:define id="owners" name="owners" scope="request" type="java.util.Collection"/>
		<TR>
			<TD valign="top">Owner:</TD>
			<TD>
				<html:select property="owner" disabled="<%=disabled%>" multiple="true" size='<%=""+Math.min(5,owners.size())%>'>
					<html:optionsCollection name="owners" label="value" value="id"/>
				</html:select>
				&nbsp;<html:errors property="owner"/>
			</TD>
		</TR>
	</logic:notEmpty>
	<logic:empty name="owners" scope="request">
		<html:hidden property="owner"/>
	</logic:empty>
	<logic:empty name="hosts" scope="request">
		<html:hidden property="host"/>
	</logic:empty>
	<logic:notEmpty name="hosts" scope="request">
	<bean:define id="hosts" name="hosts" scope="request"/>
	   	<TR><TD>Host:</TD>
			<TD>
				<html:select property="host" disabled="<%=(solver!=null)%>">
					<html:options name="hosts"/>
				</html:select>
				&nbsp;<html:errors property="host"/>
			</TD>
		</TR>
	</logic:notEmpty>
	<TR>
		<TD align="right" colspan="2">
<% if (solver==null) { %>
			<html:submit onclick="displayLoading();" property="op" value="Load"/>
<% }
   if (solver==null || !solver.isWorking()) { %>
			<html:submit onclick="displayLoading();" property="op" value="Start"/>
<% }
   if (solver!=null && solver.isRunning()) { %>
			<html:submit onclick="displayLoading();" property="op" value="Stop"/>
<% }
   if (solver!=null && !solver.isWorking()) { %>
		<% if (solver.hasFinalSectioning()) { %>   
			<html:submit onclick="displayLoading();" property="op" value="Student Sectioning"/>
		<% } %>
			<html:submit onclick="displayLoading();" property="op" value="Reload Input Data"/>
			<html:submit onclick="confirmUnload();displayLoading();" property="op" value="Unload"/>
			<sec:authorize access="hasPermission(#solverForm.owner, 'SolverGroup', 'SolverSolutionExportCsv')">
				<html:submit property="op" value="Export Solution"/>
			</sec:authorize>
<% } %>
			<html:submit onclick="displayLoading();" property="op" accesskey="R" value="Refresh"/>
		</TD>
	</TR>
</TABLE>
<BR><BR>
<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
<%
	if (solver==null) {
		String id = (String)request.getSession().getAttribute("Solver.selectedSolutionId");
		if (id==null || id.length()==0) {
%>
			<TR>
				<TD colspan="2">
					<DIV class="WelcomeRowHead">
					Current Timetable
					</DIV>
				</TD>
			</TR>
			<TR>
				<TD>
				<I>No timetable is selected or loaded.</I>
				</TD>
			</TR>
<%
 		} else { 
 			int idx=0;
 			for (StringTokenizer s = new StringTokenizer(id,",");s.hasMoreTokens();idx++) {
 				Solution solution = (new SolutionDAO()).get(Long.valueOf(s.nextToken()));
 				if (solution==null) continue;
 				if (idx>0) {
%>
					<TR><TD colspan="2">&nbsp;</TD></TR>
<%
 				}
%>
				<TR>
					<TD colspan="2">
						<DIV class="WelcomeRowHead">
						Current Timetable - <%=solution.getOwner().getName()%> <tt:wiki>Solution Properties</tt:wiki>
						</DIV>
					</TD>
				</TR>
<%
	 			PropertiesInfo info = (PropertiesInfo)solution.getInfo("GlobalInfo");
				Vector keys = new Vector(info.keySet());
				Collections.sort(keys,new ListSolutionsForm.InfoComparator());
				for (Enumeration e=keys.elements();e.hasMoreElements();) {
					String key = (String)e.nextElement();
					String val = info.get(key).toString();
%>
					<TR><TD><%=key%>:</TD><TD><%=val%></TD></TR>
<%
				}
			
				LogInfo logInfo = (LogInfo)solution.getInfo("LogInfo");
				String log = (logInfo==null?null:logInfo.getHtmlLog(Progress.MSGLEVEL_WARN, false, "Loading input data ..."));
				if (log!=null && log.length()>0) {
%>
					<TR><TD colspan=2>&nbsp;</TD></TR>
					<TR>
						<TD colspan="2">
							<DIV class="WelcomeRowHead">
							Problems <tt:wiki>Solver Warnings</tt:wiki>
							</DIV>
						</TD>
					</TR>
					<TR><TD colspan='2'><%=log%></TD></TR>
<%
				}
			}
		}
		
	} else {
		Long[] iSolutionIds = solver.getProperties().getPropertyLongArry("General.SolutionId",null);
		SolutionDAO dao = new SolutionDAO();
		org.hibernate.Session hibSession = dao.getSession();
		Transaction tx = null;
		if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
			tx = hibSession.beginTransaction();
		boolean hasSolution = false;
		boolean canOverwrite = true;
		if (iSolutionIds!=null && iSolutionIds.length>0) {
			for (int i=0;i<iSolutionIds.length;i++) {
				Solution solution = (iSolutionIds[i] == null ? null : dao.get(iSolutionIds[i],hibSession));
				if (solution != null) {
					hasSolution=true;
					if (solution.getCommited()) canOverwrite = false;
				}
			}
		}
		Map<String,String> info = solver.bestSolutionInfo();
		if (info!=null) {
%>
			<TR>
				<TD colspan="2">
					<DIV class="WelcomeRowHead">
					Best Timetable Found So Far <tt:wiki>Solution Properties</tt:wiki>
					</DIV>
				</TD>
			</TR>
<%
			List<String> keys = new ArrayList<String>(info.keySet());
			Collections.sort(keys,new ListSolutionsForm.InfoComparator());
			for (String key: keys) {
				String val = info.get(key);
%>
				<TR><TD><%=key%>:</TD><TD><%=val%></TD></TR>
<%
			}
			if (!solver.isWorking())  {
%>
			<sec:authorize access="hasPermission(#solverForm.owner, 'SolverGroup', 'SolverSolutionSave')">
				<TR>
					<TD align="right" colspan="2">
<%
				if (hasSolution && canOverwrite) {
%>
						<html:submit onclick="confirmSave();displayLoading();" property="op" value="Save"/>
<%
				}
%>
						<html:submit onclick="confirmSaveAsNew();displayLoading();" property="op" value="Save As New"/>
					<sec:authorize access="hasPermission(#solverForm.owner, 'SolverGroup', 'TimetablesSolutionCommit')">
<%
					if (hasSolution) {
%>
						<html:submit onclick="confirmSaveAndCommit();displayLoading();" property="op" value="Save & Commit"/>
<%
					}
%>
						<html:submit onclick="confirmSaveAsNewAndCommit();displayLoading();" property="op" value="Save As New & Commit"/>
					</sec:authorize>

					<sec:authorize access="hasPermission(#solverForm.owner, 'SolverGroup', 'SolverSolutionExportXml')">
						<html:submit property="op" value="Export XML"/>
					</sec:authorize>
					</TD>
				</TR>
			</sec:authorize>
<%
			}
%>
		<TR><TD colspan=2>&nbsp;</TD></TR>
<%
		}
%>
		<TR>
			<TD colspan="2">
				<DIV class="WelcomeRowHead">
				Current Timetable <tt:wiki>Solution Properties</tt:wiki>
				</DIV>
			</TD>
		</TR>
<%
		info = solver.currentSolutionInfo();
		List<String> keys = new ArrayList<String>(info.keySet());
		Collections.sort(keys,new ListSolutionsForm.InfoComparator());
		for (String key: keys) {
			String val = info.get(key);
%>
			<TR><TD><%=key%>:</TD><TD><%=val%></TD></TR>
<%
		}
		if (!solver.isWorking()) 
		{
%>
			<TR>
				<TD align="right" colspan="2">
<%
			if (solver.bestSolutionInfo()!=null) {
%>
						<html:submit onclick="displayLoading();" property="op" value="Restore From Best"/>
<%
			}
%>
					<html:submit onclick="displayLoading();" property="op" value="Save To Best"/>
				</TD>
			</TR>
<%
		}

		String log = solver.getLog(Progress.MSGLEVEL_WARN, false, "Loading input data ...");
		if (log!=null && log.length()>0) {
%>
			<TR><TD colspan=2>&nbsp;</TD></TR>
			<TR>
				<TD colspan="2">
					<DIV class="WelcomeRowHead">
					Problems <tt:wiki>Solver Warnings</tt:wiki>
					</DIV>
				</TD>
			</TR>
			<TR><TD colspan='2'><%=log%></TD></TR>
<%
		}
		if (tx!=null) tx.commit();
	}
%>
	</TABLE>
<%
} catch (Exception e) {
	e.printStackTrace();
}
%>
</html:form>
