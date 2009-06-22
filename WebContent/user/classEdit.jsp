<%--
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008-2009, UniTime LLC
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
--%>
<%@ page language="java" autoFlush="true" errorPage="../error.jsp" %>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.form.ClassEditForm" %>
<%@ page import="org.unitime.timetable.model.DepartmentalInstructor" %>
<%@ page import="org.unitime.commons.web.Web" %>
<%@ page import="org.unitime.timetable.webutil.JavascriptFunctions" %>
<%@ page import="org.unitime.timetable.action.ClassEditAction" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.util.Vector" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<SCRIPT language="javascript">
	<!--
		<%= JavascriptFunctions.getJsConfirm(Web.getUser(session)) %>
		
		function confirmRoomSizeChange() {
			if (jsConfirm!=null && !jsConfirm)
				return true;

			return ( confirm('Are you sure you want to set room size to a value different from expected capacity? Continue?'));
		}
		
		function instructorChanged(idx, source) {
			var hadPreferences = false;
			var instrHasPrefObj = document.getElementById('instrHasPref'+idx);
			if (instrHasPrefObj!=null && instrHasPrefObj.value=='true')
				hadPreferences = true;
			var instructorId = '-';
			var instructorsObj = document.getElementById('instructors'+idx);
			if (instructorsObj!=null && instructorsObj.selectedIndex>=0)
				instructorId = instructorsObj.options[instructorsObj.selectedIndex].value;
			var hasPreferences = false;
			<%
				Vector instIdWithPrefs = (Vector)request.getAttribute(DepartmentalInstructor.INSTR_HAS_PREF_ATTR_NAME);
				if (instIdWithPrefs!=null)
					for (Enumeration e=instIdWithPrefs.elements();e.hasMoreElements();) {
						Long instrId = (Long)e.nextElement();
						out.println("if (instructorId=='"+instrId+"') hasPreferences=true;");
					}
			%>
			var instrLeadObj = document.getElementById('instrLead'+idx);
			var op2Obj = document.getElementById('op2');
			var isLead = false;
			if (instrLeadObj!=null)
				isLead = instrLeadObj.checked;
			if (instructorId=='-' && instrLeadObj!=null) {
				instrLeadObj.checked=false; isLead=false;
				if (source.id=='instrLead'+idx) {
					alert('Select an instructor');
					if (instructorsObj!=null) instructorsObj.focus();
				}
			}
			if (isLead && hasPreferences) {
				if (op2Obj!=null && <%=JavascriptFunctions.getInheritInstructorPreferencesCondition(Web.getUser(session))%>) {
					op2Obj.value='updatePref';
					document.forms[0].submit();
				}
			} else if (hadPreferences) {
				if (op2Obj!=null && <%=JavascriptFunctions.getCancelInheritInstructorPreferencesCondition(Web.getUser(session))%>) {
					op2Obj.value='updatePref';
					document.forms[0].submit();
				}
			}
		}
	// -->
</SCRIPT>

<%
	// Get Form 
	String frmName = "ClassEditForm";
	ClassEditForm frm = (ClassEditForm) request.getAttribute(frmName);

	String crsNbr = "";
	if (session.getAttribute(Constants.CRS_NBR_ATTR_NAME)!=null )
		crsNbr = session.getAttribute(Constants.CRS_NBR_ATTR_NAME).toString();
%>		
<tiles:importAttribute />
<html:form action="/classEdit" focus="expectedCapacity">
	<html:hidden property="classId"/>	
	<TABLE width="93%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title>
						<bean:write name='<%=frmName%>' property='className'/>
					</tt:section-title>
					<html:submit property="op" 
						styleClass="btn" accesskey="U" titleKey="title.updatePrefs" >
						<bean:message key="button.updatePrefs" />
					</html:submit> 
					&nbsp;
					<html:submit property="op" 
						styleClass="btn" accesskey="C" titleKey="title.clearClassPrefs">
						<bean:message key="button.clearClassPrefs" />
					</html:submit> 
					<logic:notEmpty name="<%=frmName%>" property="previousId">
						&nbsp;
						<html:submit property="op" 
							styleClass="btn" accesskey="P" titleKey="title.previousClassWithUpdate">
							<bean:message key="button.previousClass" />
						</html:submit> 
					</logic:notEmpty>
					<logic:notEmpty name="<%=frmName%>" property="nextId">
						&nbsp;
						<html:submit property="op" 
							styleClass="btn" accesskey="N" titleKey="title.nextClassWithUpdate">
							<bean:message key="button.nextClass" />
						</html:submit> 
					</logic:notEmpty>
					&nbsp;
					<html:submit property="op" 
						styleClass="btn" accesskey="B" titleKey="title.returnToDetail">
						<bean:message key="button.returnToDetail" />
					</html:submit>
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
			<TD>Manager:</TD>
			<TD>
				<bean:write name="<%=frmName%>" property="managingDeptLabel" />
			<TD>
		</TR>

		<logic:notEqual name="<%=frmName%>" property="parentClassName" value="-">
			<TR>
				<TD>Parent Class:</TD>
				<TD>
					<bean:write name="<%=frmName%>" property="parentClassName" />
				<TD>
			</TR>
		</logic:notEqual>

		<%--
		<TR>
			<TD>Class Division-Secion:</TD>
			<TD>
				<html:text property="classSuffix" maxlength="6" size="6" />
			<TD>
		</TR>
		--%>
		<html:hidden property="classSuffix"/>
		<logic:notEmpty name="<%=frmName%>" property="classSuffix">
			<TR>
				<TD>Class Suffix:</TD>
				<TD>
					<bean:write name="<%=frmName%>" property="classSuffix" />
				<TD>
			</TR>
		</logic:notEmpty>

		<%--
		<TR>
			<TD>Minimum Class Limit:</TD>
			<TD>
				<html:text styleId="expectedCapacity" property="expectedCapacity" maxlength="4" size="4" onchange="getMinRoomLimit();"/>
			<TD>
		</TR>

		<TR>
			<TD>Maximum Class Limit:</TD>
			<TD>
				<html:text property="maxExpectedCapacity" maxlength="4" size="4" />
			<TD>
		</TR>

		<TR>
			<TD>Number of Rooms:</TD>
			<TD>
				<html:text styleId="nbrRooms" property="nbrRooms" maxlength="4" size="4" />
			<TD>
		</TR>

		<TR>
			<TD>Room Ratio:</TD>
			<TD>
				<script language="javascript" type="text/javascript">
					function isNumeric(sText) {
					   	var validChars = "0123456789.";
					   	var c;
					
					   	for (i = 0; i < sText.length; i++) { 
					      c = sText.charAt(i); 
					      if (validChars.indexOf(c) == -1) 
					      	return false;
				      	}
					   	return true;				   
				   	}
				   	
					function getMinRoomLimit() {
						var rr = document.getElementById('roomRatio').value;
						var nr = document.getElementById('nbrRooms').value;
						var mec = document.getElementById('expectedCapacity').value;
						var mrl = document.getElementById('minRoomLimit');
						
						if (nr!=null && isNumeric(nr) && nr>0 &&
							mec!=null && isNumeric(mec) && mec>=0 &&
							rr!=null && isNumeric(rr) && rr>0 ) {
							if (mec==0)
								mrl.value = Math.ceil(rr);
							else
								mrl.value = Math.ceil(mec*rr);
						} else {
							mrl.value = '0';
						}
					}
					
				</script>
				<html:text styleId="roomRatio" property="roomRatio" maxlength="4" size="4" onchange="getMinRoomLimit();" />
				&nbsp;&nbsp;&nbsp;&nbsp; Minimum Room Capacity: <html:text readonly="true" tabindex="500" styleId="minRoomLimit" property="minRoomLimit" maxlength="4" size="4" disabled="true"/>
			<TD>
		</TR>
		--%>
		
		<html:hidden property="nbrRooms"/>
		<html:hidden property="enrollment"/>
		<html:hidden property="expectedCapacity"/>
		<html:hidden property="maxExpectedCapacity"/>
		<html:hidden property="roomRatio"/>
		<TR>
			<TD>Enrollment:</TD>
			<TD>
				<bean:write name="<%=frmName%>" property="enrollment" />
			<TD>
		</TR>
		
		<logic:notEqual name="<%=frmName%>" property="nbrRooms" value="0">
			<% if (frm.getExpectedCapacity().intValue()==frm.getMaxExpectedCapacity().intValue()) { %>
				<TR>
					<TD>Class Limit:</TD>
					<TD>
						<bean:write name="<%=frmName%>" property="expectedCapacity" />
					<TD>
				</TR>
			<% } else { %>
				<TR>
					<TD>Minimum Class Limit:</TD>
					<TD>
						<bean:write name="<%=frmName%>" property="expectedCapacity" />
					<TD>
				</TR>
				<TR>
					<TD>Maximum Class Limit:</TD>
					<TD>
						<bean:write name="<%=frmName%>" property="maxExpectedCapacity" />
					<TD>
				</TR>
			<% } %>
		</logic:notEqual>

		<TR>
			<TD>Number of Rooms:</TD>
			<TD>
				<bean:write name="<%=frmName%>" property="nbrRooms" />
			<TD>
		</TR>
		
		<logic:notEqual name="<%=frmName%>" property="nbrRooms" value="0">
			<TR>
				<TD>Room Ratio:</TD>
				<TD>
					<bean:write name="<%=frmName%>" property="roomRatio" />
					&nbsp;&nbsp;&nbsp;&nbsp; ( Minimum Room Capacity: <bean:write name="<%=frmName%>" property="minRoomLimit" /> )
				<TD>
			</TR>
		</logic:notEqual>
		

		<TR>
			<TD>Date Pattern:</TD>
			<TD>
				<html:select style="width:200;" property="datePattern">
					<html:options collection="<%=org.unitime.timetable.model.DatePattern.DATE_PATTERN_LIST_ATTR%>" property="id" labelProperty="value" />
				</html:select>
				<img style="cursor: pointer;" src="scripts/jscalendar/calendar_1.gif" border="0" onclick="window.open('user/dispDatePattern.jsp?id='+ClassEditForm.datePattern.value+'&class='+ClassEditForm.classId.value,'datepatt','width=800,height=410,resizable=no,scrollbars=no,toolbar=no,location=no,directories=no,status=no,menubar=no,copyhistory=no');">
			<TD>
		</TR>

		<TR>
			<TD>Display Instructors:</TD>
			<TD>
				<html:checkbox property="displayInstructor" />
			<TD>
		</TR>

		<TR>
			<TD>Display In Schedule Book:</TD>
			<TD>
				<html:checkbox property="displayInScheduleBook" />
			<TD>
		</TR>

		<TR>
			<TD valign="top">Student Schedule Note:</TD>
			<TD>
				<html:textarea property="schedulePrintNote" cols="70" rows="4"  />
			<TD>
		</TR>

<%
	if (request.getAttribute("Suggestions.assignmentInfo")!=null) {
%>
		<TR>
			<TD colspan="2" align="left">
				&nbsp;<BR>
				<DIV class="WelcomeRowHead">Timetable</DIV>
			</TD>
		</TR>
		<%=request.getAttribute("Suggestions.assignmentInfo")%>
<%
	}
%>
<!-- Requests / Notes -->
		<TR>
			<TD colspan="2" align="left">
				&nbsp;<BR>
				<tt:section-title>Requests / Notes to Schedule Manager</tt:section-title>
			</TD>
		</TR>

		<TR>
			<TD colspan="2" align="left">
			<html:textarea property="notes" rows="3" cols="80"></html:textarea>
			<TD>
		</TR>

<!-- Instructors -->
		<TR><TD colspan='2'>&nbsp;</TD></TR>
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header title="<A name='InstructorPref'>Instructors</A>">
					<html:submit property="op" 
						styleId="addInstructor" 
						styleClass="btn" accesskey="I" titleKey="title.addInstructor">
						<bean:message key="button.addInstructor" />
					</html:submit> 			
				</tt:section-header>
			</TD>
		</TR>
		<TR>
			<TD colspan="2" align="left">
				<INPUT type="hidden" id="instrListTypeAction" name="instrListTypeAction" value="">				
				<TABLE align="left" cellspacing="0" cellpadding="2" border="0">
					<TR>
						<TD><I>Name</I></TD>
						<TD>&nbsp;<I>% Share</I>&nbsp;</TD>
						<TD>&nbsp;<I>Check Conflicts</I>&nbsp;</TD>
						<TD>&nbsp;</TD>
					</TR>
					
					<html:hidden property="op2" value="" styleId="op2"/>
					<logic:iterate name="<%=frmName%>" property="instructors" id="instructor" indexId="ctr">
						<TR>
							<TD align="left" nowrap>	
								<html:select style="width:250;" 
									styleId='<%= "instructors" + ctr %>' 
									property='<%= "instructors[" + ctr + "]" %>'
									onchange='<%= "instructorChanged("+ctr+", this);"%>'
									onkeypress="return selectSearch(event, this);" 
									onkeydown="return checkKey(event, this);" >														
									<html:option value="-">-</html:option>
									<html:options collection="<%=DepartmentalInstructor.INSTR_LIST_ATTR_NAME + ctr%>" property="value" labelProperty="label" />
								</html:select>
							</TD>
							<html:hidden property='<%="instrHasPref["+ctr+"]" %>' styleId='<%="instrHasPref"+ctr%>'/>
							<TD nowrap align="center">
								<html:text property='<%= "instrPctShare[" + ctr + "]" %>' size="3" maxlength="3" />
							</TD>
							<TD nowrap align="center">
								<html:checkbox property='<%="instrLead[" + ctr + "]"%>' 
									styleId='<%= "instrLead" + ctr %>' 
									onclick='<%= "instructorChanged("+ctr+", this);"%>'
								/>
							</TD>
							<TD nowrap>
								<html:submit property="op" 
									styleClass="btn"
									onclick="<%= \"javascript: doDel('instructor', '\" + ctr + \"');\"%>">
									<bean:message key="button.delete" />
								</html:submit> 			
							</TD>
						</TR>
				   	</logic:iterate>
					
				</TABLE>
			<TD>
		</TR>

<!-- Preferences -->
		<%
			boolean roomGroupDisabled = false;
			boolean roomPrefDisabled = false;
			boolean bldgPrefDisabled = false;
			boolean roomFeaturePrefDisabled = false;
			boolean distPrefDisabled = true;
			boolean restorePrefsDisabled = false;
			boolean timePrefDisabled = false;
			boolean periodPrefDisabled = true;
			
			if (frm.getUnlimitedEnroll().booleanValue()) {
				roomGroupDisabled = true;
				//roomPrefDisabled = true;
				bldgPrefDisabled = true;
				roomFeaturePrefDisabled = true;
			}
			if (frm.getNbrRooms().intValue()==0) {
				roomGroupDisabled = true;
				//roomPrefDisabled = true;
				bldgPrefDisabled = true;
				roomFeaturePrefDisabled = true;
			}
		%>
		<%@ include file="preferencesEdit.jspf" %>
	</TABLE>
</html:form>

<SCRIPT type="text/javascript" language="javascript">
	function jumpToAnchor() {
    <% if (request.getAttribute(ClassEditAction.HASH_ATTR) != null) { %>
  		location.hash = "<%=request.getAttribute(ClassEditAction.HASH_ATTR)%>";
	<% } %>
	    self.focus();
  	}
</SCRIPT>
