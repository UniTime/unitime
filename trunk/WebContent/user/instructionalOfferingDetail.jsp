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
<%@ page import="org.unitime.timetable.defaults.SessionAttribute"%>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.model.DistributionPref" %>
<%@ page import="org.unitime.timetable.webutil.JavascriptFunctions" %>
<%@ page import="org.unitime.timetable.webutil.WebInstrOfferingConfigTableBuilder"%>
<%@ page import="org.unitime.timetable.form.InstructionalOfferingDetailForm"%>
<%@ page import="org.unitime.timetable.solver.WebSolver"%>
<%@ page import="org.unitime.timetable.model.CourseOffering" %>
<%@ page import="org.unitime.timetable.model.Reservation" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<%@ taglib uri="/WEB-INF/tld/localization.tld" prefix="loc" %> 
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<tiles:importAttribute />
<tt:session-context/>
<% 
	String frmName = "instructionalOfferingDetailForm";
	InstructionalOfferingDetailForm frm = (InstructionalOfferingDetailForm) request.getAttribute(frmName);

	String crsNbr = (String)sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber);
%>
<loc:bundle name="CourseMessages">
<SCRIPT language="javascript">
	<!--
		<%= JavascriptFunctions.getJsConfirm(sessionContext) %>
		
		function confirmMakeOffered() {
			if (jsConfirm!=null && !jsConfirm)
				return true;

			if (!confirm('<%=MSG.confirmMakeOffered() %>')) {
				return false;
			}

			return true;
		}

		function confirmMakeNotOffered() {
			if (jsConfirm!=null && !jsConfirm)
				return true;
				
			if (!confirm('<%=MSG.confirmMakeNotOffered() %>')) {
				return false;
			}
			
			return true;
		}
		
		function confirmDelete() {
			if (jsConfirm!=null && !jsConfirm)
				return true;

			if (!confirm('<%=MSG.confirmDeleteIO() %>')) {
				return false;
			}

			return true;
		}

	// -->
</SCRIPT>

	<bean:define name="instructionalOfferingDetailForm" property="instrOfferingName" id="instrOfferingName"/>
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD valign="middle" colspan='2'>
				<html:form action="/instructionalOfferingDetail" styleClass="FormWithNoPadding">
					<input type='hidden' name='confirm' value='y'/>
					<html:hidden property="instrOfferingId"/>	
					<html:hidden property="nextId"/>
					<html:hidden property="previousId"/>
					<html:hidden property="catalogLinkLabel"/>
					<html:hidden property="catalogLinkLocation"/>
					
				<tt:section-header>
					<tt:section-title>
							<A  title="<%=MSG.titleBackToIOList(MSG.accessBackToIOList()) %>" 
								accesskey="<%=MSG.accessBackToIOList() %>"
								class="l8" 
								href="instructionalOfferingShowSearch.do?doit=Search&subjectAreaId=<bean:write name="instructionalOfferingDetailForm" property="subjectAreaId" />&courseNbr=<%=crsNbr%>#A<bean:write name="instructionalOfferingDetailForm" property="instrOfferingId" />"
							><bean:write name="instructionalOfferingDetailForm" property="instrOfferingName" /></A> 
					</tt:section-title>						
					<bean:define id="instrOfferingId">
						<bean:write name="instructionalOfferingDetailForm" property="instrOfferingId" />				
					</bean:define>
					<bean:define id="subjectAreaId">
						<bean:write name="instructionalOfferingDetailForm" property="subjectAreaId" />				
					</bean:define>
				 
					<sec:authorize access="hasPermission(#instrOfferingId, 'InstructionalOffering', 'OfferingCanLock')">
						<html:submit property="op" styleClass="btn" 
								accesskey="<%=MSG.accessLockIO() %>" 
								title="<%=MSG.titleLockIO(MSG.accessLockIO()) %>"
								onclick="<%=MSG.jsSubmitLockIO((String)instrOfferingName)%>">
							<loc:message name="actionLockIO"/>
						</html:submit>
					</sec:authorize>
					 <sec:authorize access="hasPermission(#instrOfferingId, 'InstructionalOffering', 'OfferingCanUnlock')">
						<html:submit property="op" styleClass="btn" 
								accesskey="<%=MSG.accessUnlockIO() %>" 
								title="<%=MSG.titleUnlockIO(MSG.accessUnlockIO()) %>"
								onclick="<%=MSG.jsSubmitUnlockIO((String)instrOfferingName)%>">
							<loc:message name="actionUnlockIO"/>
						</html:submit>
					</sec:authorize>

					<sec:authorize access="hasPermission(#instrOfferingId, 'InstructionalOffering', 'InstrOfferingConfigAdd')">
							<html:submit property="op" 
									styleClass="btn" 
									accesskey="<%=MSG.accessAddConfiguration() %>" 
									title="<%=MSG.titleAddConfiguration(MSG.accessAddConfiguration()) %>">
								<loc:message name="actionAddConfiguration" />
							</html:submit>
					</sec:authorize>
					
					<sec:authorize access="hasPermission(#instrOfferingId, 'InstructionalOffering', 'InstructionalOfferingCrossLists')">
							<html:submit property="op" 
									styleClass="btn" 
									accesskey="<%=MSG.accessCrossLists() %>" 
									title="<%=MSG.titleCrossLists(MSG.accessCrossLists()) %>">
								<loc:message name="actionCrossLists" />
							</html:submit>
					</sec:authorize>

					<sec:authorize access="hasPermission(#instrOfferingId, 'InstructionalOffering', 'OfferingMakeOffered')">
							<html:submit property="op" 
									onclick="return confirmMakeOffered();"
									styleClass="btn" 
									accesskey="<%=MSG.accessMakeOffered() %>" 
									title="<%=MSG.titleMakeOffered(MSG.accessMakeOffered()) %>">
								<loc:message name="actionMakeOffered" />
							</html:submit>
					</sec:authorize>
					
					<sec:authorize access="hasPermission(#instrOfferingId, 'InstructionalOffering', 'OfferingDelete')">
							<html:submit property="op" 
									onclick="return confirmDelete();"
									styleClass="btn" 
									accesskey="<%=MSG.accessDeleteIO() %>" 
									title="<%=MSG.titleDeleteIO(MSG.accessDeleteIO()) %>">
								<loc:message name="actionDeleteIO" />
							</html:submit>
					</sec:authorize>
					
					<sec:authorize access="hasPermission(#instrOfferingId, 'InstructionalOffering', 'OfferingMakeNotOffered')">
							<html:submit property="op" 
									onclick="return confirmMakeNotOffered();"
									styleClass="btn" 
									accesskey="<%=MSG.accessMakeNotOffered() %>"
									title="<%=MSG.titleMakeNotOffered(MSG.accessMakeNotOffered()) %>">
								<loc:message name="actionMakeNotOffered" />
							</html:submit>
					</sec:authorize>
									
					<logic:notEmpty name="instructionalOfferingDetailForm" property="previousId">
						<html:submit property="op" 
								styleClass="btn" 
								accesskey="<%=MSG.accessPreviousIO() %>" 
								title="<%=MSG.titlePreviousIO(MSG.accessPreviousIO()) %>">
							<loc:message name="actionPreviousIO" />
						</html:submit> 
					</logic:notEmpty>
					<logic:notEmpty name="instructionalOfferingDetailForm" property="nextId">
						<html:submit property="op" 
								styleClass="btn" 
								accesskey="<%=MSG.accessNextIO() %>" 
								title="<%=MSG.titleNextIO(MSG.accessNextIO()) %>">
							<loc:message name="actionNextIO" />
						</html:submit> 
					</logic:notEmpty>

					<tt:back styleClass="btn" 
							name="<%=MSG.actionBackIODetail() %>" 
							title="<%=MSG.titleBackIODetail(MSG.accessBackIODetail()) %>" 
							accesskey="<%=MSG.accessBackIODetail() %>" 
							type="InstructionalOffering">
						<bean:write name="instructionalOfferingDetailForm" property="instrOfferingId"/>
					</tt:back>
				</tt:section-header>					
				
				</html:form>
			</TD>
		</TR>		

		<logic:messagesPresent>
		<TR>
			<TD colspan="2" align="left" class="errorCell">
					<B><U><loc:message name="errors"/></U></B><BR>
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
			<TD width="20%" valign="top"><loc:message name="propertyCourseOfferings"/></TD>
			<TD>
				<TABLE border="0" width="100%" cellspacing="0" cellpadding="2">
					<TR>
						<TD align="center" class="WebTableHeader">&nbsp;</TD>
						<logic:equal name="instructionalOfferingDetailForm" property="hasCourseTypes" value="true">
							<TD align="left" class="WebTableHeader"><loc:message name="columnCourseType"/></TD>
						</logic:equal>
						<TD align="left" class="WebTableHeader"><loc:message name="columnTitle"/></TD>
						<TD align="left" class="WebTableHeader"><loc:message name="columnReserved"/></TD>
						<TD align="left" class="WebTableHeader"><loc:message name="columnScheduleOfClassesNote"/></TD>
						<logic:equal name="instructionalOfferingDetailForm" property="hasDemandOfferings" value="true">
							<TD align="left" class="WebTableHeader"><loc:message name="columnDemandsFrom"/></TD>
						</logic:equal>
						<TD align="left" class="WebTableHeader"><loc:message name="columnConsent"/></TD>
						<tt:hasProperty name="unitime.custom.CourseUrlProvider">
						<TD align="left" class="WebTableHeader"><loc:message name="columnCourseCatalog"/></TD>
						</tt:hasProperty>
						<TD align="center" class="WebTableHeader">&nbsp;</TD>
					</TR>
				<logic:iterate id="co" name="instructionalOfferingDetailForm" property="courseOfferings" type="org.unitime.timetable.model.CourseOffering">
					<TR>
						<TD align="center" class="BottomBorderGray">
							&nbsp;
							<logic:equal name="co" property="isControl" value="true">
								<IMG src="images/tick.gif" alt="<%=MSG.altControllingCourse() %>" title="<%=MSG.titleControllingCourse() %>" border="0">
							</logic:equal>
							&nbsp;
						</TD>
						<logic:equal name="instructionalOfferingDetailForm" property="hasCourseTypes" value="true">
							<TD class="BottomBorderGray">
								<logic:notEmpty name="co" property="courseType">
									<span title='<%=co.getCourseType().getLabel()%>'><%=co.getCourseType().getReference()%></span>
								</logic:notEmpty>
							</TD>
						</logic:equal>
						<TD class="BottomBorderGray"><bean:write name="co" property="courseNameWithTitle"/></TD>
						<TD class="BottomBorderGray">
							<logic:notEmpty name="co" property="reservation">
								<bean:write name="co" property="reservation"/>
							</logic:notEmpty>
						</TD>
						<TD class="BottomBorderGray">&nbsp;<bean:write name="co" property="scheduleBookNote"/></TD>
						<logic:equal name="instructionalOfferingDetailForm" property="hasDemandOfferings" value="true">
							<TD class="BottomBorderGray">&nbsp;
							<%
								CourseOffering cod = ((CourseOffering)co).getDemandOffering();
								if (cod!=null) out.write(cod.getCourseName()); 
							 %>
							</TD>
						</logic:equal>
						<TD class="BottomBorderGray">
							<logic:empty name="co" property="consentType">
								<loc:message name="noConsentRequired"/>
							</logic:empty>
							<logic:notEmpty name="co" property="consentType">
								<bean:define name="co" property="consentType" id="consentType"/>
								<bean:write name="consentType" property="abbv"/>
							</logic:notEmpty>
						</TD>
						<tt:hasProperty name="unitime.custom.CourseUrlProvider">
							<TD class="BottomBorderGray">
								<span name='UniTimeGWT:CourseLink' style="display: none;"><bean:write name="co" property="uniqueId"/></span>
							</TD>
						</tt:hasProperty>
						<TD align="right" class="BottomBorderGray">
							<sec:authorize access="hasPermission(#co, 'EditCourseOffering') or hasPermission(#co, 'EditCourseOfferingNote') or hasPermission(#co, 'EditCourseOfferingCoordinators')">
								<html:form action="/courseOfferingEdit" styleClass="FormWithNoPadding">
									<html:hidden property="courseOfferingId" value="<%= ((CourseOffering)co).getUniqueId().toString() %>" />
									<html:submit property="op" 
											styleClass="btn" 
											title="<%=MSG.titleEditCourseOffering() %>">
										<loc:message name="actionEditCourseOffering" />
									</html:submit>
								</html:form>
							</sec:authorize>
						</TD>
					</TR>
				</logic:iterate>
				</TABLE>
			</TD>
		</TR>
		
		<TR>
			<TD><loc:message name="propertyEnrollment"/> </TD>
			<TD>
				<bean:write name="instructionalOfferingDetailForm" property="enrollment" /> 
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="propertyLastEnrollment"/> </TD>
			<TD>
				<logic:equal name="instructionalOfferingDetailForm" property="demand" value="0">
					-
				</logic:equal>
				<logic:notEqual name="instructionalOfferingDetailForm" property="demand" value="0">
					<bean:write name="instructionalOfferingDetailForm" property="demand" /> 
				</logic:notEqual>
			</TD>
		</TR>

		<logic:notEqual name="instructionalOfferingDetailForm" property="projectedDemand" value="0">
			<TR>
				<TD><loc:message name="propertyProjectedDemand"/> </TD>
				<TD>
					<bean:write name="instructionalOfferingDetailForm" property="projectedDemand" /> 
				</TD>
			</TR>
		</logic:notEqual>

		<TR>
			<TD><loc:message name="propertyOfferingLimit"/> </TD>
			<TD>
				<logic:equal name="instructionalOfferingDetailForm" property="unlimited" value="false">
					<bean:write name="instructionalOfferingDetailForm" property="limit" />
					<logic:present name="limitsDoNotMatch" scope="request"> 
						&nbsp;
						<img src='images/Error16.jpg' alt='<%=MSG.altLimitsDoNotMatch() %>' title='<%=MSG.titleLimitsDoNotMatch() %>' border='0' align='top'>
						<font color="#FF0000"><loc:message name="errorReservedSpacesForOfferingsTotal"><bean:write name="limitsDoNotMatch" scope="request"/></loc:message></font>
					</logic:present>
					<logic:present name="configsWithTooHighLimit" scope="request">
						<logic:notPresent name="limitsDoNotMatch" scope="request">
							&nbsp;
							<img src='images/Error16.jpg' alt='<%=MSG.altLimitsDoNotMatch() %>' title='<%=MSG.titleLimitsDoNotMatch() %>' border='0' align='top'>
						</logic:notPresent>
						<font color="#FF0000"><bean:write name="configsWithTooHighLimit" scope="request"/></font>
					</logic:present>
				</logic:equal>
				<logic:equal name="instructionalOfferingDetailForm" property="unlimited" value="true">
					<span title="<%=MSG.titleUnlimitedEnrollment() %>"><font size="+1">&infin;</font></span>
				</logic:equal>
			</TD>
		</TR>

		<logic:notEmpty name="instructionalOfferingDetailForm" property="creditText">
			<TR>
				<TD><loc:message name="propertyCredit"/></TD>
				<TD>
					<bean:write name="instructionalOfferingDetailForm" property="creditText" />
				</TD>
			</TR>
		</logic:notEmpty>
		
		<logic:equal name="instructionalOfferingDetailForm" property="byReservationOnly" value="true">
			<TR>
				<TD><loc:message name="propertyByReservationOnly"/></TD>
				<TD>
					<IMG src="images/tick.gif" alt="ENABLED" title="<%=MSG.descriptionByReservationOnly2() %>" border="0">
					<i><loc:message name="descriptionByReservationOnly2"/></i>
				</TD>
			</TR>
		</logic:equal>
		
		<logic:notEmpty name="instructionalOfferingDetailForm" property="coordinators">
			<TR>
				<TD valign="top"><loc:message name="propertyCoordinators"/></TD>
				<TD>
					<bean:write name="instructionalOfferingDetailForm" property="coordinators" filter="false"/>
				</TD>
			</TR>
		</logic:notEmpty>
		
		<logic:notEmpty name="instructionalOfferingDetailForm" property="wkEnroll">
			<TR>
				<TD valign="top"><loc:message name="propertyLastWeekEnrollment"/></TD>
				<TD>
					<loc:message name="textLastWeekEnrollment"><bean:write name="instructionalOfferingDetailForm" property="wkEnroll" /></loc:message>
				</TD>
			</TR>
		</logic:notEmpty>
		
		<logic:notEmpty name="instructionalOfferingDetailForm" property="wkChange">
			<TR>
				<TD valign="top"><loc:message name="propertyLastWeekChange"/></TD>
				<TD>
					<loc:message name="textLastWeekChange"><bean:write name="instructionalOfferingDetailForm" property="wkChange" /></loc:message>
				</TD>
			</TR>
		</logic:notEmpty>

		<logic:notEmpty name="instructionalOfferingDetailForm" property="wkDrop">
			<TR>
				<TD valign="top"><loc:message name="propertyLastWeekDrop"/></TD>
				<TD>
					<loc:message name="textLastWeekDrop"><bean:write name="instructionalOfferingDetailForm" property="wkDrop" /></loc:message>
				</TD>
			</TR>
		</logic:notEmpty>
		
		<logic:equal name="instructionalOfferingDetailForm" property="displayEnrollmentDeadlineNote" value="true">
			<TR>
				<TD valign="top">&nbsp;</TD>
				<TD>
					<i><loc:message name="descriptionEnrollmentDeadlines"><bean:write name="instructionalOfferingDetailForm" property="weekStartDayOfWeek" /></loc:message></i>
				</TD>
			</TR>
		</logic:equal>

		<logic:notEmpty name="instructionalOfferingDetailForm" property="catalogLinkLabel">
		<TR>
			<TD><loc:message name="propertyCourseCatalog"/> </TD>
			<TD>
				<A href="<bean:write name="instructionalOfferingDetailForm" property="catalogLinkLocation" />" 
						target="_blank"><bean:write name="instructionalOfferingDetailForm" property="catalogLinkLabel" /></A>
			</TD>
		</TR>
		</logic:notEmpty>
		
		<sec:authorize access="hasPermission(null, 'Session', 'CurriculumView')">
		<TR>
			<TD colspan="2">
				<div id='UniTimeGWT:CourseCurricula' style="display: none;"><bean:write name="instructionalOfferingDetailForm" property="instrOfferingId" /></div>
			</TD>
		</TR>
		</sec:authorize>
		
		<sec:authorize access="hasPermission(null, 'Department', 'Reservations')">
		<TR>
			<TD colspan="2">
				<a name="reservations"></a>
				<sec:authorize access="hasPermission(#instrOfferingId, 'InstructionalOffering', 'ReservationOffering') and hasPermission(null, null, 'ReservationAdd')">
					<div id='UniTimeGWT:OfferingReservations' style="display: none;"><bean:write name="instructionalOfferingDetailForm" property="instrOfferingId" /></div>
				</sec:authorize>
				<sec:authorize access="not hasPermission(#instrOfferingId, 'InstructionalOffering', 'ReservationOffering') or not hasPermission(null, null, 'ReservationAdd')">
					<div id='UniTimeGWT:OfferingReservationsRO' style="display: none;"><bean:write name="instructionalOfferingDetailForm" property="instrOfferingId" /></div>
				</sec:authorize>
			</TD>
		</TR>
		</sec:authorize>

		<TR>
			<TD colspan="2" >&nbsp;</TD>
		</TR>

<!-- Configuration -->
		<TR>
			<TD colspan="2" valign="middle">
	<% //output configuration
	if (frm.getInstrOfferingId() != null){
		WebInstrOfferingConfigTableBuilder ioTableBuilder = new WebInstrOfferingConfigTableBuilder();
		ioTableBuilder.setDisplayDistributionPrefs(false);
		ioTableBuilder.setDisplayConfigOpButtons(true);
		ioTableBuilder.htmlConfigTablesForInstructionalOffering(
									sessionContext,
				    		        WebSolver.getClassAssignmentProxy(session),
				    		        WebSolver.getExamSolver(session),
				    		        frm.getInstrOfferingId(), 
				    		        out,
				    		        request.getParameter("backType"),
				    		        request.getParameter("backId"));
	}
	%>
			</TD>
		</TR>

		<TR>
			<TD valign="middle" colspan='3' align='left'>
				<tt:displayPrefLevelLegend/>
			</TD>
		</TR>
		
		<% if (request.getAttribute(DistributionPref.DIST_PREF_REQUEST_ATTR)!=null) { %>
			<TR>
				<TD colspan="2" >&nbsp;</TD>
			</TR>
	
			<TR>
				<TD colspan="2">
					<TABLE width="100%" cellspacing="0" cellpadding="0" border="0" style="margin:0;">
						<%=request.getAttribute(DistributionPref.DIST_PREF_REQUEST_ATTR)%>
					</TABLE>
				</TD>
			</TR>
		<% } %>
		

		<logic:equal name="instructionalOfferingDetailForm" property="notOffered" value="false">
		<TR>
			<TD colspan="2">
				<tt:exams type='InstructionalOffering' add='true'>
					<bean:write name="<%=frmName%>" property="instrOfferingId"/>
				</tt:exams>
			</TD>
		</TR>
		</logic:equal>
		
		<tt:last-change type='InstructionalOffering'>
			<bean:write name="<%=frmName%>" property="instrOfferingId"/>
		</tt:last-change>		

		<TR>
			<TD colspan="2">
				<div id='UniTimeGWT:OfferingEnrollments' style="display: none;"><bean:write name="instructionalOfferingDetailForm" property="instrOfferingId" /></div>
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
			
				<html:form action="/instructionalOfferingDetail" styleClass="FormWithNoPadding">
					<input type='hidden' name='confirm' value='y'/>
					<html:hidden property="instrOfferingId"/>	
					<html:hidden property="nextId"/>
					<html:hidden property="previousId"/>
					
					<sec:authorize access="hasPermission(#instrOfferingId, 'InstructionalOffering', 'OfferingCanLock')">
						<html:submit property="op" styleClass="btn" 
								accesskey="<%=MSG.accessLockIO() %>" 
								title="<%=MSG.titleLockIO(MSG.accessLockIO()) %>"
								onclick="<%=MSG.jsSubmitLockIO((String)instrOfferingName)%>">
							<loc:message name="actionLockIO"/>
						</html:submit>
					</sec:authorize>
					 <sec:authorize access="hasPermission(#instrOfferingId, 'InstructionalOffering', 'OfferingCanUnlock')">
						<html:submit property="op" styleClass="btn" 
								accesskey="<%=MSG.accessUnlockIO() %>" 
								title="<%=MSG.titleUnlockIO(MSG.accessUnlockIO()) %>"
								onclick="<%=MSG.jsSubmitUnlockIO((String)instrOfferingName)%>">
							<loc:message name="actionUnlockIO"/>
						</html:submit>
					</sec:authorize>
				
					<sec:authorize access="hasPermission(#instrOfferingId, 'InstructionalOffering', 'InstrOfferingConfigAdd')">
							<html:submit property="op" 
									styleClass="btn" 
									accesskey="<%=MSG.accessAddConfiguration() %>" 
									title="<%=MSG.titleAddConfiguration(MSG.accessAddConfiguration()) %>">
								<loc:message name="actionAddConfiguration" />
							</html:submit>
					</sec:authorize>
					
					<sec:authorize access="hasPermission(#instrOfferingId, 'InstructionalOffering', 'InstructionalOfferingCrossLists')">
							<html:submit property="op" 
									styleClass="btn" 
									accesskey="<%=MSG.accessCrossLists() %>" 
									title="<%=MSG.titleCrossLists(MSG.accessCrossLists()) %>">
								<loc:message name="actionCrossLists" />
							</html:submit>
					</sec:authorize>

					<sec:authorize access="hasPermission(#instrOfferingId, 'InstructionalOffering', 'OfferingMakeOffered')">
							<html:submit property="op" 
									onclick="return confirmMakeOffered();"
									styleClass="btn" 
									accesskey="<%=MSG.accessMakeOffered() %>" 
									title="<%=MSG.titleMakeOffered(MSG.accessMakeOffered()) %>">
								<loc:message name="actionMakeOffered" />
							</html:submit>
					</sec:authorize>
					
					<sec:authorize access="hasPermission(#instrOfferingId, 'InstructionalOffering', 'OfferingDelete')">
							<html:submit property="op" 
									onclick="return confirmDelete();"
									styleClass="btn" 
									accesskey="<%=MSG.accessDeleteIO() %>" 
									title="<%=MSG.titleDeleteIO(MSG.accessDeleteIO()) %>">
								<loc:message name="actionDeleteIO" />
							</html:submit>
					</sec:authorize>
					
					<sec:authorize access="hasPermission(#instrOfferingId, 'InstructionalOffering', 'OfferingMakeNotOffered')">
							<html:submit property="op" 
									onclick="return confirmMakeNotOffered();"
									styleClass="btn" 
									accesskey="<%=MSG.accessMakeNotOffered() %>"
									title="<%=MSG.titleMakeNotOffered(MSG.accessMakeNotOffered()) %>">
								<loc:message name="actionMakeNotOffered" />
							</html:submit>
					</sec:authorize>
					
					<logic:notEmpty name="instructionalOfferingDetailForm" property="previousId">
						<html:submit property="op" 
								styleClass="btn" 
								accesskey="<%=MSG.accessPreviousIO() %>" 
								title="<%=MSG.titlePreviousIO(MSG.accessPreviousIO()) %>">
							<loc:message name="actionPreviousIO" />
						</html:submit> 
					</logic:notEmpty>
					<logic:notEmpty name="instructionalOfferingDetailForm" property="nextId">
						<html:submit property="op" 
								styleClass="btn" 
								accesskey="<%=MSG.accessNextIO() %>" 
								title="<%=MSG.titleNextIO(MSG.accessNextIO()) %>">
							<loc:message name="actionNextIO" />
						</html:submit> 
					</logic:notEmpty>

					<tt:back styleClass="btn" 
							name="<%=MSG.actionBackIODetail() %>" 
							title="<%=MSG.titleBackIODetail(MSG.accessBackIODetail()) %>" 
							accesskey="<%=MSG.accessBackIODetail() %>" 
							type="InstructionalOffering">
						<bean:write name="instructionalOfferingDetailForm" property="instrOfferingId"/>
					</tt:back>				

				</html:form>					
			</TD>
		</TR>

	</TABLE>
</loc:bundle>
