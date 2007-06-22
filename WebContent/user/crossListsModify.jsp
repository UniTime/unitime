<%--
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org
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
<%@ page import="org.unitime.timetable.model.CourseOffering" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<tiles:importAttribute />
<%
	String crsNbr = "";
	if (session.getAttribute(Constants.CRS_NBR_ATTR_NAME)!=null )
		crsNbr = session.getAttribute(Constants.CRS_NBR_ATTR_NAME).toString();
%>

<SCRIPT language="javascript">
	<!--
	var origTotal = 0;
	
	function updateResvTotal() {
		i=0;
		total = 0;
		while ( (o = document.getElementById("reserved_" + i++)) !=null ) {
			if (o.value == parseInt(o.value) && parseInt(o.value)>=0) {
				total += parseInt(o.value);
			}
			else {
				document.getElementById("resvTotal").innerHTML = "<font color='red'><b>ERR</b></font>";
				return;
			}
		}
		if (total<origTotal) {
			str = "<img height='15' align='bottom' src='images/down_arrow_red.jpg'> <font color='red'><b>" + (origTotal-total) + "</b></font>&nbsp;&nbsp;";
			document.getElementById("resvTotal").innerHTML = str.blink();
			str = "<font color='red'><b>" + total + "</b></font>&nbsp;&nbsp;";
			setTimeout("document.getElementById('resvTotal').innerHTML = str;",1500);
		}
		if (total>origTotal) {
			str = "<img height='15' align='bottom' src='images/up_arrow_green.jpg'> <font color='green'><b>" + (total-origTotal) + "</b></font>&nbsp;&nbsp;";
			document.getElementById("resvTotal").innerHTML = str.blink();
			str = "<font color='green'><b>" + total + "</b></font>&nbsp;&nbsp;";
			setTimeout("document.getElementById('resvTotal').innerHTML = str;",1500);
		}
		if (total==origTotal)
			document.getElementById("resvTotal").innerHTML = total + "&nbsp;&nbsp;";
	}
	
	// -->
</SCRIPT>
<%
	int resvTotal = 0;
	int projTotal = 0;
	int lastTermTotal = 0;
%>
					
	
<html:form action="/crossListsModify">
	<html:hidden property="instrOfferingId"/>
	<html:hidden property="instrOfferingName"/>
	<html:hidden property="readOnlyCrsOfferingId"/>
	<html:hidden property="originalOfferings"/>
	<html:hidden property="ownedInstrOffr"/>
	<html:hidden property="ioLimit"/>
	<html:hidden property="unlimited"/>
	<INPUT type="hidden" name="hdnOp" value = "">
	<INPUT type="hidden" name="deletedCourseOfferingId" value = "">

	<TABLE width="93%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan="2" valign="middle">
				<DIV class="WelcomeRowHead">
					<A  title="Back to Instructional Offering List (Alt+I)"
						accesskey="I"
						class="l7"
						href="instructionalOfferingShowSearch.do?doit=Search&subjectAreaId=<bean:write name="crossListsModifyForm" property="subjectAreaId" />&courseNbr=<%=crsNbr%>#A<bean:write name="crossListsModifyForm" property="instrOfferingId" />"
					><bean:write name="crossListsModifyForm" property="instrOfferingName" /></A>
				</DIV>
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
			<TD>Instructional Offering Limit: </TD>
			<TD align="left">
				<logic:equal name="crossListsModifyForm" property="unlimited" value="true">
					<img src='images/infinity.gif' alt='Unlimited Enrollment' title='Unlimited Enrollment' border='0' align='top'>
				</logic:equal>
				<logic:notEqual name="crossListsModifyForm" property="unlimited" value="true">
					<bean:write name="crossListsModifyForm" property="ioLimit"/>
				</logic:notEqual>
			</TD>
		</TR>

		<TR>
			<TD>Course Offerings: </TD>
			<TD>
				<html:select
					name="crossListsModifyForm"
					property="addCourseOfferingId"
					onfocus="setUp();"
					onkeypress="return selectSearch(event, this);"
					onkeydown="return checkKey(event, this);" >
					<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
					<html:options collection="<%=CourseOffering.CRS_OFFERING_LIST_ATTR_NAME%>" property="uniqueId" labelProperty="courseName" />
				</html:select>
				&nbsp;
				<html:submit property="op" styleClass="btn" accesskey="A"
					title="Add course offering to the instructional offering (ALT+A)">
					<bean:message key="button.add" />
				</html:submit>
			</TD>
		</TR>

		<TR>
			<TD>&nbsp;</TD>
			<TD align="left">
				<bean:define id="cos" name="crossListsModifyForm" property="courseOfferingIds" />
				<TABLE align="left" border="0" cellspacing="0" cellpadding="3">
					<TR>
						<TD align="left" class="WebTableHeader"> Offering </TD>
						<TD align="center" class="WebTableHeader"> Controlling </TD>
						<% if ( ((java.util.List)cos).size()>1 ) { %>
						<TD align="center" class="WebTableHeader"> Reserved </TD>
						<TD align="right" class="WebTableHeader"><!-- I> Requested </I --></TD>
						<TD align="right" class="WebTableHeader"> Projected </TD>
						<TD align="right" class="WebTableHeader"> Last Term </TD>
						<% } %>
						<TD>&nbsp;</TD>
					</TR>

					<logic:iterate name="crossListsModifyForm" property="courseOfferingIds" id="co" indexId="ctr">
					<TR>
						<TD class="BottomBorderGray">
							<html:hidden property="<%= "courseOfferingIds[" + ctr + "]" %>"/>
							<html:hidden property="<%= "courseOfferingNames[" + ctr + "]" %>"/>
							<html:hidden property="<%= "ownedCourse[" + ctr + "]" %>"/>
						<% if ( ((java.util.List)cos).size()==1 ) { %>
							<html:hidden property="<%= "limits[" + ctr + "]" %>"/>
						<% } %>
							<bean:write name="crossListsModifyForm" property="<%= "courseOfferingNames[" + ctr + "]" %>"/> &nbsp;
						</TD>
						<TD align="center" class="BottomBorderGray">
							&nbsp;
							<logic:equal name="crossListsModifyForm" property="<%= "ownedCourse[" + ctr + "]" %>" value="true" >
								<html:radio name="crossListsModifyForm" property="ctrlCrsOfferingId" value="<%= co.toString() %>" />
							</logic:equal>
							&nbsp;
						</TD>
						<% if ( ((java.util.List)cos).size()>1 ) { %>
						<TD align="center" class="BottomBorderGray">
							&nbsp;
							<html:hidden property="<%= "resvId[" + ctr + "]" %>"/>
							<logic:equal name="crossListsModifyForm" property="ownedInstrOffr" value="true" >
								<html:text name="crossListsModifyForm" styleId="<%= "reserved_" + ctr %>" onchange="updateResvTotal();" property="<%= "limits[" + ctr + "]" %>" size="4" maxlength="4" />
							</logic:equal>
							<logic:notEqual name="crossListsModifyForm" property="ownedInstrOffr" value="true" >
								<bean:write name="crossListsModifyForm" property="<%= "limits[" + ctr + "]" %>" />
								<html:hidden property="<%= "limits[" + ctr + "]" %>" />
							</logic:notEqual>
							<bean:define id="resvSpace" name="crossListsModifyForm" property="<%= "limits[" + ctr + "]" %>"/>							
							<% if (resvSpace!=null && resvSpace.toString().length()>0 && Constants.isInteger(resvSpace.toString())) 
								resvTotal += Integer.parseInt((String) resvSpace); %>
							&nbsp;
						</TD>
						<TD align="right" class="BottomBorderGray">
							&nbsp;
							<html:hidden property="<%= "requested[" + ctr + "]" %>"/>
							<bean:write name="crossListsModifyForm" property="<%= "requested[" + ctr + "]" %>" />
							&nbsp;
						</TD>
						<TD align="right" class="BottomBorderGray">
							&nbsp;
							<html:hidden property="<%= "projected[" + ctr + "]" %>"/>
							<bean:write name="crossListsModifyForm" property="<%= "projected[" + ctr + "]" %>" />
							<bean:define id="projSpace" name="crossListsModifyForm" property="<%= "projected[" + ctr + "]" %>"/>							
							<% if ( projSpace!=null && projSpace.toString().length()>0 && Constants.isInteger(projSpace.toString())) 
								projTotal += Integer.parseInt((String) projSpace); 
							   else 
							   	out.print("-"); %>&nbsp;
						</TD>
						<TD align="right" class="BottomBorderGray">
							&nbsp;
							<html:hidden property="<%= "lastTerm[" + ctr + "]" %>"/>
							<bean:write name="crossListsModifyForm" property="<%= "lastTerm[" + ctr + "]" %>" />
							<bean:define id="lastTermSpace" name="crossListsModifyForm" property="<%= "lastTerm[" + ctr + "]" %>"/>							
							<% if (lastTermSpace!=null && lastTermSpace.toString().length()>0 && Constants.isInteger(lastTermSpace.toString())) 
								lastTermTotal += Integer.parseInt((String) lastTermSpace); 
							   else 
							   	out.print("-"); %>&nbsp;
						</TD>
						<% } %>
						<TD align="center" nowrap class="BottomBorderGray">
							&nbsp;
							<logic:notEqual name="crossListsModifyForm" property="readOnlyCrsOfferingId" value="<%= co.toString() %>" >
								<A href="#null" onClick="document.forms[0].elements['hdnOp'].value='delete';document.forms[0].elements['deletedCourseOfferingId'].value='<%= co.toString() %>';document.forms[0].submit();"><IMG border="0" src="images/Delete16.gif" title="Remove course from instructional offering & mark it as not offered."></A>
							</logic:notEqual>
							 &nbsp;
						</TD>
					</TR>
					</logic:iterate>
					
					<% if ( ((java.util.List)cos).size()>1 ) { %>
					<TR>
						<TD align="left" class='rowTotal'><I> Total </I></TD>
						<TD align="center" class='rowTotal'><I> &nbsp; </I></TD>
						<TD class='rowTotal' align='right'><DIV id='resvTotal'><%= resvTotal %>&nbsp; &nbsp;</DIV></TD>
						<TD align="right" class='rowTotal'>&nbsp;<!-- I> Requested </I --></TD>
						<TD class='rowTotal' align='right'><%= projTotal>=0 ? projTotal : "" %>&nbsp; </TD>
						<TD class='rowTotal' align='right'><%= lastTermTotal>=0 ? lastTermTotal : "" %>&nbsp; </TD>
						<TD>&nbsp;</TD>
					</TR>
					<% } %>

					
				</TABLE>
			</TD>
		</TR>

<!-- Buttons -->
		<TR>
			<TD colspan="2" valign="middle">
				<DIV class="WelcomeRowHeadBlank">&nbsp;</DIV>
			</TD>
		</TR>

		<TR>
			<TD colspan="2" align="right">
				<html:submit property="op"
					styleClass="btn" accesskey="U" titleKey="title.updateCrossLists" onclick="displayLoading();">
					<bean:message key="button.updateCrossLists" />
				</html:submit>

				<bean:define id="instrOfferingId">
					<bean:write name="crossListsModifyForm" property="instrOfferingId" />
				</bean:define>

				<html:button property="op"
					styleClass="btn" accesskey="B" titleKey="title.backToInstrOffrDetail"
					onclick="document.location.href='instructionalOfferingDetail.do?op=view&io=${instrOfferingId}';">
					<bean:message key="button.backToInstrOffrDetail" />
				</html:button>

			</TD>
		</TR>

	</TABLE>
</html:form>

<SCRIPT language="javascript">
	<!--
	origTotal = <%=resvTotal%>;
	// -->
</SCRIPT>
