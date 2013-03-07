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
<%@page import="java.text.DecimalFormat"%>
<%@page import="org.unitime.timetable.security.rights.Right"%>
<%@ page language="java" autoFlush="true"%>
<%@ page import="java.util.*" %>
<%@ page import="org.unitime.timetable.solver.WebSolver" %>
<%@ page import="org.unitime.timetable.solver.SolverProxy" %>
<%@ page import="org.unitime.timetable.model.dao.SolutionDAO" %>
<%@ page import="org.unitime.timetable.model.Solution" %>
<%@ page import="org.hibernate.Transaction" %>
<%@ page import="org.unitime.timetable.form.ListSolutionsForm" %>
<%@ page import="net.sf.cpsolver.ifs.util.Progress" %>
<%@ page import="org.unitime.timetable.model.SolverGroup" %>
<%@ page import="org.unitime.timetable.model.dao.SolverGroupDAO" %>
<%@ page import="org.unitime.timetable.webutil.JavascriptFunctions" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<tt:back-mark back="true" clear="true" title="Timetables" uri="listSolutions.do"/>
<tt:session-context/>
<SCRIPT language="javascript">
	<!--
		<%= JavascriptFunctions.getJsConfirm(sessionContext) %>
		
		function confirmDelete() {
			if (jsConfirm!=null && !jsConfirm) return;

			if (!confirm('Do you really want to delete this solution?')) {
				listSolutionsForm.confirm.value='n';
			}
		}

		function confirmUnload() {
			if (jsConfirm!=null && !jsConfirm) return;

			if (!confirm('Do you really want to unload your current timetable? You may lose this timetable if you did not save it.')) {
				listSolutionsForm.confirm.value='n';
			}
		}

		function confirmCommit() {
			if (jsConfirm!=null && !jsConfirm) return;

			if (!confirm('Do you really want to commit this solution? This may uncommit your currently committed solution.')) {
				listSolutionsForm.confirm.value='n';
			}
		}

		function confirmUncommit() {
			if (jsConfirm!=null && !jsConfirm) return;

			if (!confirm('Do you really want to uncommit this solution?')) {
				listSolutionsForm.confirm.value='n';
			}
		}

		function confirmSave() {
			if (jsConfirm!=null && !jsConfirm) return;

			if (!confirm('Do you really want to save your current timetable? This will overwrite your previous solution.')) {
				listSolutionsForm.confirm.value='n';
			}
		}

		function confirmSaveAsNew() {
			if (jsConfirm!=null && !jsConfirm) return;

			if (!confirm('Do you really want to save your current timetable?')) {
				listSolutionsForm.confirm.value='n';
			}
		}

		function confirmSaveAndCommit() {
			if (jsConfirm!=null && !jsConfirm) return;

			if (!confirm('Do you really want to save and commit your current timetable? This will overwrite your previous solution. It may also uncommit your currently committed solution.')) {
				listSolutionsForm.confirm.value='n';
			}
		}

		function confirmSaveAsNewAndCommit() {
			if (jsConfirm!=null && !jsConfirm) return;

			if (!confirm('Do you really want to save and commit your current timetable? This may uncommit your currently committed solution.')) {
				listSolutionsForm.confirm.value='n';
			}
		}
	// -->
</SCRIPT>

<tiles:importAttribute />

<html:form action="/listSolutions">
	<input type='hidden' name='confirm' value='y'/>
	<logic:notEmpty name="listSolutionsForm" property="messages">
		<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
			<TR><TD colspan="2" align="left" class="errorCell">
				<font color="red"><B>ERRORS:</B><BR>
		<logic:iterate name="listSolutionsForm" property="messages" id="error">
			&nbsp;&nbsp;&nbsp;&nbsp;<bean:write name="error"/><BR>
		</logic:iterate>
				</font>
			</TD></TR>
		</TABLE><br>
	</logic:notEmpty>
<%
	SolverProxy solver = WebSolver.getSolver(session);
	if (solver!=null) {
		Long[] iSolutionId = solver.getProperties().getPropertyLongArry("General.SolutionId",null);
		SolutionDAO dao = new SolutionDAO();
		org.hibernate.Session hibSession = dao.getSession();
		Transaction tx = null;
		if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
			tx = hibSession.beginTransaction();
		Solution solution[] = null;
		boolean canCommit = true, canOverwrite = true;
		if (iSolutionId!=null && iSolutionId.length>0) {
			solution = new Solution[iSolutionId.length];
			for (int i=0;i<iSolutionId.length;i++) {
				solution[i] = dao.get(iSolutionId[i],hibSession);
				if (solution[i]==null || !sessionContext.hasPermission(solution[i].getOwner(), Right.TimetablesSolutionCommit))
					canCommit = false;
				if (solution[i] != null && solution[i].getCommited())
					canOverwrite = false;
			}
		}
		SolverGroup owner[] = new SolverGroup[0];
		Long ownerId[] = solver.getProperties().getPropertyLongArry("General.SolverGroupId",null);
		if (ownerId!=null && ownerId.length>0) {
			owner = new SolverGroup[ownerId.length];
			for (int i=0;i<ownerId.length;i++) {
				owner[i] = (new SolverGroupDAO()).get(ownerId[i],hibSession);
				if (owner[i]==null || !sessionContext.hasPermission(owner[i], Right.TimetablesSolutionCommit))
					canCommit = false;
			}
		}
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
				Loaded Timetable <tt:wiki>Solution Properties</tt:wiki>
				</DIV>
			</TD>
		</TR>
		<TR><TD>Status:</TD><TD><%=status%> <tt:wiki>Solver Status</tt:wiki></TD></TR>
<%  	if (progress!=null) { %>
			<TR><TD>Progress:</TD><TD><%=progress%></TD></TR>
<%  	} %>
		<TR>
			<TD valign="top">Created:</TD><TD>
				<% for (int i=0;i<owner.length;i++) {
					if (i>0) out.print("<BR>");
					if (solution==null || solution.length<=i || solution[i]==null)
						out.print("<i>Not saved</i>");
					else
						out.print(WebSolver.sDF.format(solution[i].getCreated()));
				} %>
			</TD>
		</TR>
		<TR>
			<TD valign="top">Owner:</TD><TD>
				<% for (int i=0;i<owner.length;i++) {
					if (i>0) out.print("<BR>");
					if (owner[i]==null)
						out.print("<i>Unknown</i>");
					else
						out.print(owner[i].getName());
				} %>
			</TD>
		</TR>
		<TR>
			<TD valign="top">Commited:</TD><TD>
				<% for (int i=0;i<owner.length;i++) {
					if (i>0) out.print("<BR>");
					if (solution==null || solution.length<=i || solution[i]==null || !solution[i].isCommited().booleanValue())
						out.print("<i>Not commited</i>");
					else
						out.print(WebSolver.sDF.format(solution[i].getCommitDate()));
				} %>
			</TD>
		</TR>
		<TR>
			<TD valign="top">Note:</TD><TD><html:textarea property="solverNote" rows="4" cols="80" disabled="<%=solver.isWorking()%>"/></TD>
		</TR>
<%		Map<String,String> info = solver.currentSolutionInfo();
		List<String> keys = new ArrayList<String>(info.keySet());
		Collections.sort(keys,new ListSolutionsForm.InfoComparator());
		for (String key: keys) {
			String val = info.get(key);
%>
			<TR><TD nowrap><%=key%>:</TD><TD><%=val%></TD></TR>
<%
		}

		String log = solver.getLog(Progress.MSGLEVEL_WARN, false, "Loading input data ...");
		if (log!=null && log.length()>0) {
%>
			<TR>
				<TD valign="top">Problems: <tt:wiki>Solver Warnings</tt:wiki></TD>
				<TD><%=log%></TD>
			</TR>
<%
		}
%>
		<TR>
			<TD align="right" colspan="2">
<%
		if (!solver.isWorking()) {
			if (solution!=null && canOverwrite) {
%>
					<html:submit onclick="confirmSave();displayLoading();" property="op" value="Save"/>
<%
			}
%>
					<html:submit onclick="confirmSaveAsNew();displayLoading();" property="op" value="Save As New"/>
<%
			if (solution!=null && canCommit) {
%>
					<html:submit onclick="confirmSaveAndCommit();displayLoading();"  property="op" value="Save & Commit"/>
<%
			}
			if (canCommit) {
%>
					<html:submit onclick="confirmSaveAsNewAndCommit();displayLoading();" property="op" value="Save As New & Commit"/>
<%
			}
%>
				<html:submit onclick="displayLoading();" property="op" value="Reload Input Data"/>
				<html:submit onclick="confirmUnload();displayLoading();" property="op" value="Unload"/> 
<%
		} else {
%>
				<html:submit onclick="displayLoading();" property="op" accesskey="R" value="Refresh"/> 
<%
		}
%>
			</TD>
		</TR>
	</TABLE>
<%
		if (tx!=null) tx.commit();
	}
%>

<logic:notEmpty name="listSolutionsForm" property="solutionBeans">
	<html:hidden property="selectedSolutionBean"/><html:errors property="selectedSolutionBean"/>
	<html:hidden property="note"/>
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR><TD colspan='2'>&nbsp;</TD></TR>
	<logic:iterate name="listSolutionsForm" property="solutionBeans" id="sb" indexId="idx">
		<TR>
			<TD colspan="2">
				<DIV class="WelcomeRowHead">
				Selected Timetable - <bean:write name="sb" property="owner" /> <tt:wiki>Solution Properties</tt:wiki>
				</DIV>
			</TD>
		</TR>
		<TR>
			<TD>Created:</TD><TD><bean:write name="sb" property="created" /></TD>
		</TR>
		<TR>
			<TD>Owner:</TD><TD><bean:write name="sb" property="owner" /></TD>
		</TR>
		<TR>
			<TD>Valid:</TD><TD><html:checkbox name="sb" property="valid" disabled="true"/></TD>
		</TR>
		<TR>
			<TD>Commited:</TD><TD>
				<bean:write name="sb" property="commited" />
				<logic:equal name="sb" property="commited" value="">
					<i>Not commited</i>
				</logic:equal>
			</TD>
		</TR>
		<sec:authorize access="hasPermission(#sb.uniqueId, 'Solution', 'TimetablesSolutionChangeNote')">
			<TR>
				<TD valign="top">Note:</TD><TD>
					<textarea id="note<%=idx%>" name="note<%=idx%>" cols="80" rows="4"><bean:write name="sb" property="note" /></textarea>
				</TD>
			</TR>
		</sec:authorize>
		<sec:authorize access="hasPermission(#sb.uniqueId, 'Solution', 'TimetablesSolutionChangeNote')">
			<logic:notEmpty name="sb" property="note">
				<TR>
					<TD valign="top">Note:</TD><TD>
						<bean:write name="sb" property="note" />
					</TD>
				</TR>
			</logic:notEmpty>
			</sec:authorize>
		</TR>
		<logic:iterate name="sb" property="infos" id="info">
			<TR>
				<TD nowrap><%=info%>:</TD><TD><bean:write name="sb" property='<%="info("+info+")"%>' /></TD>
			</TR>
		</logic:iterate>
		<logic:notEmpty name="sb" property="log">
			<TR>
				<TD valign="top">Problems: <tt:wiki>Solver Warnings</tt:wiki></TD><TD><bean:write name="sb" property="log" filter="false"/></TD>
			</TR>
		</logic:notEmpty>
		<TR>
			<TD align="right" colspan="2">
				<html:submit onclick="<%=\"selectedSolutionBean.value='\"+idx+\"';\"%>" property="op" value="Deselect"/>
					<sec:authorize access="hasPermission(#sb.uniqueId, 'Solution', 'TimetablesSolutionChangeNote')">
						<html:submit onclick="<%=\"displayLoading();selectedSolutionBean.value='\"+idx+\"';note.value=document.getElementById('note\"+idx+\"').value;\"%>" property="op" value="Update Note"/>
					</sec:authorize>
					<sec:authorize access="hasPermission(#sb.ownerId, 'SolverGroup', 'TimetablesSolutionCommit')">
						<logic:equal name="sb" property="commited" value="">
							<html:submit onclick="<%=\"confirmCommit();displayLoading();selectedSolutionBean.value='\"+idx+\"';\"%>" property="op" value="Commit"/>
						</logic:equal>
						<logic:notEqual name="sb" property="commited" value="">
							<html:submit onclick="<%=\"confirmUncommit();displayLoading();selectedSolutionBean.value='\"+idx+\"';\"%>" property="op" value="Uncommit"/>
						</logic:notEqual>
					</sec:authorize>
					<sec:authorize access="hasPermission(#sb.uniqueId, 'Solution', 'TimetablesSolutionExportCsv')">
						<html:submit onclick="<%=\"selectedSolutionBean.value='\"+idx+\"';\"%>" property="op" value="Export Solution"/>
					</sec:authorize>
					<logic:equal name="sb" property="commited" value="">
						<sec:authorize access="hasPermission(#sb.uniqueId, 'Solution', 'TimetablesSolutionDelete')">
							<html:submit onclick="<%=\"confirmDelete();displayLoading();selectedSolutionBean.value='\"+idx+\"';\"%>" property="op" value="Delete"/> 
						</sec:authorize>
					</logic:equal>
			</TD>
		</TR>
	</logic:iterate>
<% if (solver==null || !solver.isWorking()) { %>
		<sec:authorize access="hasPermission(#listSolutionsForm.solutionBeans, 'Solution', 'TimetablesSolutionLoad')">
		<logic:notEmpty name="listSolutionsForm" property="settings">
			<TR>
				<TD nowrap>Load into interactive solver:</TD>
				<TD align="right" nowrap>
					Configuration:
					<html:select property="setting">
						<html:optionsCollection name="listSolutionsForm" property="settings" label="value" value="id"/>
					</html:select>
					<logic:empty name="hosts" scope="request">
						<html:hidden property="host"/>
					</logic:empty>
					<logic:notEmpty name="hosts" scope="request">
						<bean:define id="hosts" name="hosts" scope="request"/> 
						&nbsp;&nbsp;&nbsp;&nbsp;Host:
						<html:select property="host">
							<html:options name="hosts"/>
						</html:select>
					</logic:notEmpty>
					&nbsp;&nbsp;&nbsp;&nbsp;
					<html:submit onclick="displayLoading();" property="op" value="Load"/> 
				</TD>
			</TR>
		</logic:notEmpty>
		</sec:authorize>
<% } %>
	</TABLE>
</logic:notEmpty>
<BR>
<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
	<%= request.getAttribute("ListSolutions.table") %> 
</TABLE>
<sec:authorize access="hasPermission(null, 'SolverGroup', 'TimetablesSolutionLoadEmpty')">
<% if (solver==null) { %>
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD nowrap>
			Load into interactive solver:
			</TD>
			<TD align="right" nowrap>
				<logic:notEmpty name="owners" scope="request">
					<bean:define id="owners" name="owners" scope="request"/>
					Owner:
					<html:select property="ownerId">
						<html:optionsCollection name="owners" label="value" value="id"/>
					</html:select>
					&nbsp;&nbsp;&nbsp;&nbsp;
				</logic:notEmpty>
				<logic:empty name="owners" scope="request">
					<html:hidden property="ownerId"/>
				</logic:empty>
				Configuration:
				<html:select property="emptySetting">
					<html:optionsCollection name="listSolutionsForm" property="settings" label="value" value="id"/>
				</html:select>
				<logic:empty name="hosts" scope="request">
					<html:hidden property="hostEmpty"/>
				</logic:empty>
				<logic:notEmpty name="hosts" scope="request">
					<bean:define id="hosts" name="hosts" scope="request"/> 
					&nbsp;&nbsp;&nbsp;&nbsp;Host:
					<html:select property="hostEmpty">
						<html:options name="hosts"/>
					</html:select>
				</logic:notEmpty>
				&nbsp;&nbsp;&nbsp;&nbsp;
				<html:submit onclick="displayLoading();" property="op" value="Load Empty Solution"/>
			</TD>
		</TR>
	</TABLE>
<% } %>
</sec:authorize>
</html:form>
