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
<%@ page language="java" autoFlush="true" errorPage="../error.jsp" %>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.model.CourseOffering" %>
<%@ page import="org.unitime.timetable.defaults.SessionAttribute"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-localization" prefix="loc" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>

<tiles:importAttribute />
<tt:session-context/>
<%
	String crsNbr = (String)sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber);
%>

<loc:bundle name="CourseMessages">

<SCRIPT language="javascript">
	<!--
	var ioLimit = -1;
	var mismatchHtml = 
		" &nbsp;&nbsp; " +
		"<img src='images/cancel.png' alt='<%=MSG.altCrossListsLimitsDoNotMatch()%>' title='<%=MSG.titleCrossListsLimitsDoNotMatch() %>' border='0' align='top'> &nbsp;" +
		"<font color='#FF0000'><%= MSG.errorCrossListsLimitsDoNotMatch()%></font>";
	
	String.prototype.trim = function() {
		return this.replace(/^\s+|\s+$/g,"");
	};
	String.prototype.ltrim = function() {
		return this.replace(/^\s+/,"");
	};
	String.prototype.rtrim = function() {
		return this.replace(/\s+$/,"");
	};
		
	function updateResvTotal() {
		i=0;
		total = 0;
		blanksExist = false;
		allBlank = true;
		while ( (o = document.getElementById("reserved_" + i++)) !=null ) {
			val = o.value = o.value.trim();
			if (val=="") {
				val = 0;
				blanksExist = true;
			} else  if (val == parseInt(val) && parseInt(val)>=0) {
				total += parseInt(val);
				allBlank = false;
			} else {
				document.getElementById("resvTotal").innerHTML = "<font color='red'><b>?</b></font>";
				return;
			}
		}
		
		str = "<b>" + total + "</b>&nbsp;&nbsp;";
		if (ioLimit >= 0) {
			if (total < ioLimit) {
				str = "<font color='red'><b>" + total + "</b></font>&nbsp;&nbsp;";
			} else {
				str = "<font color='green'><b>" + total + "</b></font>&nbsp;&nbsp;";
			}
		}
		if (allBlank) str = "";
		
		if (document.getElementById('resvTotal'))
			document.getElementById('resvTotal').innerHTML = str;
		
		if (!allBlank && total<ioLimit) 
			document.getElementById("resvTotalDiff").innerHTML = mismatchHtml;
		else 
			document.getElementById("resvTotalDiff").innerHTML = "";
	}
	
	// -->
</SCRIPT>
<%
	int resvTotal = 0;
	int projTotal = 0;
	int lastTermTotal = 0;
	boolean resvExists = false;
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

	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan="2" valign="middle">
				<DIV class="WelcomeRowHead">
					<A  title="<%=MSG.titleBackToIOList(MSG.accessBackToIOList()) %>"
						accesskey="I"
						class="l8"
						href="instructionalOfferingShowSearch.do?doit=Search&subjectAreaId=<bean:write name="crossListsModifyForm" property="subjectAreaId" />&courseNbr=<%=crsNbr%>#A<bean:write name="crossListsModifyForm" property="instrOfferingId" />"
					><bean:write name="crossListsModifyForm" property="instrOfferingName" /></A>
				</DIV>
			</TD>
		</TR>

		<logic:messagesPresent>
		<TR>
			<TD colspan="2" align="left" class="errorCell">
					<B><U><loc:message name="errorsIOCrossLists"/></U></B><BR>
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
			<TD><loc:message name="propertyIOLimit"/></TD>
			<TD align="left">
				<logic:equal name="crossListsModifyForm" property="unlimited" value="true">
					<span title="Unlimited Enrollment"><font size="+1">&infin;</font></span>
					<bean:define id="instrOffrLimit" value="-1" />
				</logic:equal>
				<logic:notEqual name="crossListsModifyForm" property="unlimited" value="true">
					<TABLE border="0" cellspacing="0" cellpadding="0" align="left">
					<TR><TD align="left">
						<bean:write name="crossListsModifyForm" property="ioLimit"/>
						<logic:notEmpty name="crossListsModifyForm" property="ioLimit">
							<bean:define id="instrOffrLimit">
								<bean:write name="crossListsModifyForm" property="ioLimit"/>
							</bean:define>
						</logic:notEmpty>
						<logic:empty name="crossListsModifyForm" property="ioLimit">
							<bean:define id="instrOffrLimit" value="-1" />
						</logic:empty>
					</TD>
					<TD align="left">
						<DIV id='resvTotalDiff'>
						</DIV>
					</TD></TR>
					</TABLE>					
				</logic:notEqual>
			</TD>
		</TR>

		<TR>
			<TD valign="top" rowspan="2"><loc:message name="propertyCourseOfferings"/> </TD>
			<TD>
				<table border="0" cellpadding="0" cellspacing="0"><tr><td>
				<html:select
					name="crossListsModifyForm"									
					property="addCourseOfferingId">
					<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
					<html:options collection="<%=CourseOffering.CRS_OFFERING_LIST_ATTR_NAME%>" property="uniqueId" labelProperty="courseNameWithTitle" />
				</html:select>
				</td><td style="padding-left: 5px;">
				<html:submit property="op" styleClass="btn" 
					accesskey="<%=MSG.accessAddCourseToCrossList() %>"
					title="<%=MSG.titleAddCourseToCrossList(MSG.accessAddCourseToCrossList()) %>">
					<loc:message name="actionAddCourseToCrossList" />
				</html:submit>
				</td></tr><tr><td class="unitime-Hint" colspan="2">
				<loc:message name="hintCrossLists"/>
				</td></tr></table>
			</TD>
		</TR>
		<TR>
			<TD align="left">
				<bean:define id="cos" name="crossListsModifyForm" property="courseOfferingIds" />
				<TABLE align="left" border="0" cellspacing="0" cellpadding="3">
					<TR>
						<TD align="left" class="WebTableHeader"><loc:message name="columnCrossListsOffering"/> </TD>
						<TD align="center" class="WebTableHeader"> <loc:message name="columnCrossListsControlling"/> </TD>
						<TD align="center" class="WebTableHeader"> <loc:message name="columnCrossListsReserved"/> </TD>
						<TD align="right" class="WebTableHeader"><!-- I> Requested </I --></TD>
						<TD align="right" class="WebTableHeader"> <loc:message name="columnCrossListsProjected"/></TD>
						<TD align="right" class="WebTableHeader"> <loc:message name="columnCrossListsLastTerm"/></TD>
						<TD>&nbsp;</TD>
					</TR>

					<logic:iterate name="crossListsModifyForm" property="courseOfferingIds" id="co" indexId="ctr">
					<TR>
						<TD class="BottomBorderGray">
							<html:hidden property='<%= "courseOfferingIds[" + ctr + "]" %>'/>
							<html:hidden property='<%= "courseOfferingNames[" + ctr + "]" %>'/>
							<html:hidden property='<%= "ownedCourse[" + ctr + "]" %>'/>
							<bean:write name="crossListsModifyForm" property='<%= "courseOfferingNames[" + ctr + "]" %>'/> &nbsp;
						</TD>
						<TD align="center" class="BottomBorderGray">
							&nbsp;
							<logic:equal name="crossListsModifyForm" property='<%= "ownedCourse[" + ctr + "]" %>' value="true" >
								<html:radio name="crossListsModifyForm" property="ctrlCrsOfferingId" value="<%= co.toString() %>" />
							</logic:equal>
							&nbsp;
						</TD>
						<TD align="center" class="BottomBorderGray">
							&nbsp;
							<html:hidden property='<%= "resvId[" + ctr + "]" %>'/>
							<% if ( ((java.util.List)cos).size() == 1 ) { %>
								<bean:write name="crossListsModifyForm" property='<%= "limits[" + ctr + "]" %>' />
								<html:hidden property='<%= "limits[" + ctr + "]" %>' />
							<% } else { %>
								<logic:equal name="crossListsModifyForm" property="ownedInstrOffr" value="true" >
									<html:text name="crossListsModifyForm" styleId='<%= "reserved_" + ctr %>' onchange="updateResvTotal();" property='<%= "limits[" + ctr + "]" %>' size="4" maxlength="4" style="text-align:right;"/>
								</logic:equal>
								<logic:notEqual name="crossListsModifyForm" property="ownedInstrOffr" value="true" >
									<bean:write name="crossListsModifyForm" property='<%= "limits[" + ctr + "]" %>' />
									<html:hidden property='<%= "limits[" + ctr + "]" %>' />
								</logic:notEqual>
							<% } %>
							<bean:define id="resvSpace" name="crossListsModifyForm" property='<%= "limits[" + ctr + "]" %>'/>							
							<% if (resvSpace!=null && resvSpace.toString().length()>0 && Constants.isInteger(resvSpace.toString())) { 
								resvExists = true;
								resvTotal += Integer.parseInt((String) resvSpace); }%>
							&nbsp;
						</TD>
						<TD align="right" class="BottomBorderGray">
							&nbsp;
							<html:hidden property='<%= "requested[" + ctr + "]" %>'/>
							<bean:write name="crossListsModifyForm" property='<%= "requested[" + ctr + "]" %>' />
							&nbsp;
						</TD>
						<TD align="right" class="BottomBorderGray">
							&nbsp;
							<html:hidden property='<%= "projected[" + ctr + "]" %>'/>
							<bean:write name="crossListsModifyForm" property='<%= "projected[" + ctr + "]" %>' />
							<bean:define id="projSpace" name="crossListsModifyForm" property='<%= "projected[" + ctr + "]" %>'/>							
							<% if ( projSpace!=null && projSpace.toString().length()>0 && Constants.isInteger(projSpace.toString())) 
								projTotal += Integer.parseInt((String) projSpace); 
							   else 
							   	out.print("-"); %>&nbsp;
						</TD>
						<TD align="right" class="BottomBorderGray">
							&nbsp;
							<html:hidden property='<%= "lastTerm[" + ctr + "]" %>'/>
							<bean:write name="crossListsModifyForm" property='<%= "lastTerm[" + ctr + "]" %>' />
							<bean:define id="lastTermSpace" name="crossListsModifyForm" property='<%= "lastTerm[" + ctr + "]" %>'/>							
							<% if (lastTermSpace!=null && lastTermSpace.toString().length()>0 && Constants.isInteger(lastTermSpace.toString())) 
								lastTermTotal += Integer.parseInt((String) lastTermSpace); 
							   else 
							   	out.print("-"); %>&nbsp;
						</TD>
						<TD align="center" nowrap class="BottomBorderGray">
							&nbsp;
							<logic:notEqual name="crossListsModifyForm" property="readOnlyCrsOfferingId" value="<%= co.toString() %>" >
								<IMG border="0" src="images/action_delete.png" title="<%=MSG.titleRemoveCourseFromCrossList() %>"
									onMouseOver="this.style.cursor='hand';this.style.cursor='pointer';"
									onClick="document.forms[0].elements['hdnOp'].value='delete';document.forms[0].elements['deletedCourseOfferingId'].value='<%= co.toString() %>';document.forms[0].submit();">
							</logic:notEqual>
							 &nbsp;
						</TD>
					</TR>
					</logic:iterate>
					
					<% if ( ((java.util.List)cos).size()>1 ) { %>
					<TR>
						<TD align="left" class='rowTotal'><I> <loc:message name="rowCrossListsTotal"/> </I></TD>
						<TD align="center" class='rowTotal'><I> &nbsp; </I></TD>
						<TD class='rowTotal' align='right'><DIV id='resvTotal'><%= resvExists ? resvTotal : "" %>&nbsp; &nbsp;</DIV></TD>
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
				<html:submit property="op" styleClass="btn" 
					accesskey="<%=MSG.accessUpdateCrossLists() %>" 
					title="<%=MSG.titleUpdateCrossLists(MSG.accessUpdateCrossLists()) %>" 
					onclick="displayLoading();">
					<loc:message name="actionUpdateCrossLists" />
				</html:submit>

				<bean:define id="instrOfferingId">
					<bean:write name="crossListsModifyForm" property="instrOfferingId" />
				</bean:define>

				<html:button property="op" styleClass="btn" 
					accesskey="<%=MSG.accessBackToIODetail() %>" 
					title="<%=MSG.titleBackToIODetail(MSG.accessBackToIODetail()) %>"
					onclick="document.location.href='instructionalOfferingDetail.do?op=view&io=${instrOfferingId}';">
					<loc:message name="actionBackToIODetail" />
				</html:button>

			</TD>
		</TR>

	</TABLE>
</html:form>

<SCRIPT language="javascript">
	<!--
	ioLimit = <%= pageContext.getAttribute("instrOffrLimit")!=null ? Integer.valueOf((String) pageContext.getAttribute("instrOffrLimit")) : -1 %> 
	updateResvTotal();
	// -->
</SCRIPT>

</loc:bundle>