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
<%@ page language="java" autoFlush="true"%>
<%@ page import="org.unitime.timetable.form.ExamEditForm" %>
<%@ page import="org.unitime.timetable.webutil.JavascriptFunctions" %>
<%@ page import="org.unitime.timetable.model.DepartmentalInstructor" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<%
	// Get Form 
	String frmName = "examEditForm";	
	ExamEditForm frm = (ExamEditForm) request.getAttribute(frmName);	
%>	
<tt:session-context/>
<SCRIPT language="javascript">
	<!--
		<%= JavascriptFunctions.getJsConfirm(sessionContext) %>
	// -->
</SCRIPT>
<tt:confirm name="confirmDelete">The examination will be deleted. Continue?</tt:confirm>

<html:form action="examDetail">
	<html:hidden property="examId"/>
	<html:hidden property="nextId"/>
	<html:hidden property="previousId"/>
	<html:hidden property="op2" value=""/>
	<bean:define name='<%=frmName%>' property="examId" id="examId"/>
	
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title>
						<bean:write name='<%=frmName%>' property='label'/>
					</tt:section-title>
				<sec:authorize access="hasPermission(#examId, 'Exam', 'ExaminationEdit')">
					<html:submit property="op" 
						styleClass="btn" accesskey="E" titleKey="title.editExam" >
						<bean:message key="button.editExam" />
					</html:submit>
				</sec:authorize>
				<sec:authorize access="hasPermission(#examId, 'Exam', 'ExaminationClone')">
					<html:submit property="op" 
						styleClass="btn" accesskey="C" titleKey="title.cloneExam" >
						<bean:message key="button.cloneExam" />
					</html:submit> 
				</sec:authorize>
				<sec:authorize access="hasPermission(#examId, 'Exam', 'DistributionPreferenceExam')">
					<html:submit property="op" styleClass="btn" accesskey="A" titleKey="title.addDistPref" >
						<bean:message key="button.addDistPref" />
					</html:submit>
				</sec:authorize>
				<sec:authorize access="hasPermission(#examId, 'Exam', 'ExaminationAssignment')">
					<input type="button" value="Assign" title="Open Examination Assignment Dialog (Alt+X)" class="btn" accesskey="X"
							onClick="showGwtDialog('Examination Assignment', 'examInfo.do?examId=<%=String.valueOf(examId)%>','900','90%');"
					/>
				</sec:authorize>
				<sec:authorize access="hasPermission(#examId, 'Exam', 'ExaminationDelete')">
					<html:submit property="op" styleClass="btn" accesskey="D" titleKey="title.deleteExam" onclick="return confirmDelete();">
						<bean:message key="button.deleteExam" />
					</html:submit>
				</sec:authorize>
				<logic:greaterEqual name="<%=frmName%>" property="previousId" value="0">
					<html:submit property="op" 
							styleClass="btn" accesskey="P" titleKey="title.previousExam">
						<bean:message key="button.previousExam" />
					</html:submit> 
				</logic:greaterEqual>
				<logic:greaterEqual name="<%=frmName%>" property="nextId" value="0">
					<html:submit property="op" 
						styleClass="btn" accesskey="N" titleKey="title.nextExam">
						<bean:message key="button.nextExam" />
					</html:submit> 
				</logic:greaterEqual>
				<tt:back styleClass="btn" name="Back" title="Return to %% (Alt+B)" accesskey="B" type="PreferenceGroup">
					<bean:write name="<%=frmName%>" property="examId"/>
				</tt:back>
				</tt:section-header>
			</TD>
		</TR>
		
		<logic:messagesPresent>
		<TR>
			<TD colspan="2" align="left" class="errorCell">
					<B><U>ERRORS</U></B><BR>
				<BLOCKQUOTE>
				<UL>
				    <html:messages id="error">
				      <LI>
						${error}
				      </LI>
				    </html:messages>
			    </UL>
			    </BLOCKQUOTE>
			</TD>
		</TR>
		</logic:messagesPresent>
		
		<TR>
			<TD>Name:</TD><TD>
				<logic:notEmpty name="<%=frmName%>" property="name">
					<bean:write name="<%=frmName%>" property="name" />
				</logic:notEmpty> 
				<logic:empty name="<%=frmName%>" property="name">
					<i><bean:write name="<%=frmName%>" property="label" /></i>
				</logic:empty> 
			</TD>
		</TR>
		<TR>
			<TD>Type:</TD><TD>
				<logic:iterate scope="request" name="examTypes" id="et">
					<bean:define name="et" property="uniqueId" id="examType"/>
					<logic:equal name="<%=frmName%>" property="examType" value="<%=examType.toString()%>">
						<bean:write name="et" property="label"/>
					</logic:equal>
				</logic:iterate>
			 </TD>
		</TR>
		<TR>
			<TD>Length:</TD><TD> <bean:write name="<%=frmName%>" property="length" /></TD>
		</TR>
		<TR>
			<TD>Seating Type:</TD><TD> <bean:write name="<%=frmName%>" property="seatingType" /></TD>
		</TR>
		<TR>
			<TD nowrap>Maximum Number of Rooms:</TD><TD> <bean:write name="<%=frmName%>" property="maxNbrRooms" /></TD>
		</TR>
		<TR>
			<TD>Size:</TD><TD> <bean:write name="<%=frmName%>" property="size" /></TD>
		</TR>
		<logic:notEmpty name="<%=frmName%>" property="printOffset">
			<TR>
				<TD>Print Offset:</TD><TD> <bean:write name="<%=frmName%>" property="printOffset" /> minutes</TD>
			</TR>
		</logic:notEmpty>
		<logic:notEmpty name="<%=frmName%>" property="instructors">
			<TR>
				<TD valign="top">Instructors:</TD>
				<TD>
					<table border='0' cellspacing="0" cellpadding="3">
					<!--  <tr><td><i>Name</i></td><td><i>Email</i></td></tr> -->
					<logic:iterate name="<%=frmName%>" property="instructors" id="instructor" indexId="ctr">
						<logic:iterate scope="request" name="<%=DepartmentalInstructor.INSTR_LIST_ATTR_NAME%>" id="instr">
							<logic:equal name="instr" property="value" value="<%=(String)instructor%>">
								<tr onmouseover="this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='hand';this.style.cursor='pointer';" 
									onmouseout="this.style.backgroundColor='transparent';"
									onClick="document.location='instructorDetail.do?instructorId=<%=instructor%>';"
								>
									<td style="padding-right: 20px;">
										<bean:write name="instr" property="label"/>
									</td>
									<td>
										<bean:define name="<%=frmName%>" property='<%="email("+instructor+")"%>' id="email"/>
										<logic:notEmpty name="email"><i>
											<a href="mailto:<%=(String)email%>" onclick="event.cancelBubble=true;">
											<bean:write name="email"/>
											</a>
										</i></logic:notEmpty>
									</td>
								</tr>
							</logic:equal>
						</logic:iterate>
	   				</logic:iterate>
	   				</table>
			   	</TD>
		   	</TR>
		</logic:notEmpty>
		<logic:notEmpty name="<%=frmName%>" property="avgPeriod">
			<TR>
				<TD>Average Period:</TD><TD> <bean:write name="<%=frmName%>" property="avgPeriod" /></TD>
			</TR>
		</logic:notEmpty>
		<tt:last-change type='Exam'>
			<bean:write name="<%=frmName%>" property="examId"/>
		</tt:last-change>		
		
		<logic:notEmpty name="<%=frmName%>" property="note">
		<TR>
			<TD colspan="2" valign="middle">
				<br>
				<tt:section-title>
					Notes
				</tt:section-title>
			</TD>
		</TR>
		<TR>
			<TD colspan='2'> <bean:write name="<%=frmName%>" property="note" filter="false"/></TD>
		</TR>
		</logic:notEmpty>

		<TR>
			<TD colspan="2" valign="middle">
				<br>
				<tt:section-title>
					Classes / Courses
				</tt:section-title>
			</TD>
		</TR>
		<TR>
			<TD colspan='2'>
				<logic:empty scope="request" name="ExamDetail.table">
					<i>No relation defined for this exam.</i>
				</logic:empty>
				<logic:notEmpty scope="request" name="ExamDetail.table">
					<table border='0' cellspacing="0" cellpadding="3" width='100%'>
					<bean:write scope="request" name="ExamDetail.table" filter="false"/>
					</table>
				</logic:notEmpty>
			</TD>
		</TR>
		
		<logic:notEmpty scope="request" name="ExamDetail.assignment">
			<TR>
				<TD colspan="2" valign="middle">
					<br>
					<tt:section-title>
						Assignment
					</tt:section-title>
				</TD>
			</TR>
			<TR>
				<TD colspan='2'>
					<bean:write scope="request" name="ExamDetail.assignment" filter="false"/>
				</TD>
			</TR>
		</logic:notEmpty>
		

<!-- Preferences -->		
		<TR>
			<TD colspan="2" valign="middle">
				<br>
				<tt:section-title>
					Preferences
				</tt:section-title>
			</TD>
		</TR>
		
		<jsp:include page="preferencesDetail.jspf">
			<jsp:param name="frmName" value="<%=frmName%>"/>
			<jsp:param name="timePref" value="false"/>
		</jsp:include>
		
		<TR>
			<TD colspan="2">
				<div id='UniTimeGWT:ExamEnrollments' style="display: none;"><bean:write name="<%=frmName%>" property="examId"/></div>
			</TD>
		</TR>		
	
		<TR>
			<TD colspan="2" class="WelcomeRowHead">
				&nbsp;
			</TD>
		</TR>

		<TR align="right">
			<TD valign="middle" colspan='2'>
				<sec:authorize access="hasPermission(#examId, 'Exam', 'ExaminationEdit')">
					<html:submit property="op" 
						styleClass="btn" accesskey="E" titleKey="title.editExam" >
						<bean:message key="button.editExam" />
					</html:submit>
				</sec:authorize>
				<sec:authorize access="hasPermission(#examId, 'Exam', 'ExaminationClone')">
					<html:submit property="op" 
						styleClass="btn" accesskey="C" titleKey="title.cloneExam" >
						<bean:message key="button.cloneExam" />
					</html:submit> 
				</sec:authorize>
				<sec:authorize access="hasPermission(#examId, 'Exam', 'DistributionPreferenceExam')">
					<html:submit property="op" styleClass="btn" accesskey="A" titleKey="title.addDistPref" >
						<bean:message key="button.addDistPref" />
					</html:submit>
				</sec:authorize>
				<sec:authorize access="hasPermission(#examId, 'Exam', 'ExaminationAssignment')">
					<input type="button" value="Assign" title="Open Examination Assignment Dialog (Alt+X)" class="btn" accesskey="X"
							onClick="showGwtDialog('Examination Assignment', 'examInfo.do?examId=<%=String.valueOf(examId)%>','900','90%');"
					/>
				</sec:authorize>
				<sec:authorize access="hasPermission(#examId, 'Exam', 'ExaminationDelete')">
					<html:submit property="op" styleClass="btn" accesskey="D" titleKey="title.deleteExam" onclick="return confirmDelete();">
						<bean:message key="button.deleteExam" />
					</html:submit>
				</sec:authorize>
				<logic:greaterEqual name="<%=frmName%>" property="previousId" value="0">
					<html:submit property="op" 
							styleClass="btn" accesskey="P" titleKey="title.previous">
						<bean:message key="button.previousExam" />
					</html:submit> 
				</logic:greaterEqual>
				<logic:greaterEqual name="<%=frmName%>" property="nextId" value="0">
					<html:submit property="op" 
						styleClass="btn" accesskey="N" titleKey="title.next">
						<bean:message key="button.nextExam" />
					</html:submit> 
				</logic:greaterEqual>
				<tt:back styleClass="btn" name="Back" title="Return to %% (Alt+B)" accesskey="B" type="PreferenceGroup">
					<bean:write name="<%=frmName%>" property="examId"/>
				</tt:back>
			</TD>
		</TR>
		
	
	</TABLE>
</html:form>

