<%--
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC
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
<%@ page import="org.unitime.timetable.model.DistributionPref" %>
<%@ page import="org.unitime.commons.web.Web" %>
<%@ page import="org.unitime.timetable.webutil.JavascriptFunctions" %>
<%@ page import="org.unitime.timetable.webutil.WebInstrOfferingConfigTableBuilder"%>
<%@ page import="org.unitime.timetable.form.InstructionalOfferingDetailForm"%>
<%@ page import="org.unitime.timetable.solver.WebSolver"%>
<%@ page import="org.unitime.timetable.model.CourseOffering" %>
<%@ page import="org.unitime.timetable.model.Reservation" %>
<%@ page import="org.unitime.timetable.model.Roles" %>
<%@ page import="org.unitime.commons.User" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<tiles:importAttribute />
<% 
	User user = Web.getUser(session);
	String frmName = "instructionalOfferingDetailForm";
	InstructionalOfferingDetailForm frm = (InstructionalOfferingDetailForm) request.getAttribute(frmName);

	String crsNbr = "";
	if (session.getAttribute(Constants.CRS_NBR_ATTR_NAME)!=null )
		crsNbr = session.getAttribute(Constants.CRS_NBR_ATTR_NAME).toString();
%>
<SCRIPT language="javascript">
	<!--
		<%= JavascriptFunctions.getJsConfirm(Web.getUser(session)) %>
		
		function confirmMakeOffered() {
			if (jsConfirm!=null && !jsConfirm)
				return true;

			if (!confirm('Do you really want to make this offering offered?')) {
				return false;
			}

			return true;
		}

		function confirmMakeNotOffered() {
			if (jsConfirm!=null && !jsConfirm)
				return true;
				
			if (!confirm('Do you really want to make this offering not offered?')) {
				return false;
			}
			
			return true;
		}
		
		function confirmDelete() {
			if (jsConfirm!=null && !jsConfirm)
				return true;

			if (!confirm('This option will delete all associated course offerings.\nDo you really want to delete this offering?')) {
				return false;
			}

			return true;
		}

	// -->
</SCRIPT>

	<TABLE width="93%" border="0" cellspacing="0" cellpadding="3">
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
							<A  title="Back to Instructional Offering List (Alt+I)" 
								accesskey="I"
								class="l7" 
								href="instructionalOfferingShowSearch.do?doit=Search&subjectAreaId=<bean:write name="instructionalOfferingDetailForm" property="subjectAreaId" />&courseNbr=<%=crsNbr%>#A<bean:write name="instructionalOfferingDetailForm" property="instrOfferingId" />"
							><bean:write name="instructionalOfferingDetailForm" property="instrOfferingName" /></A> 
					</tt:section-title>						
					<bean:define id="instrOfferingId">
						<bean:write name="instructionalOfferingDetailForm" property="instrOfferingId" />				
					</bean:define>
					<bean:define id="subjectAreaId">
						<bean:write name="instructionalOfferingDetailForm" property="subjectAreaId" />				
					</bean:define>
				 
					<!-- Display buttons only if editable by current user -->
					<logic:equal name="instructionalOfferingDetailForm" property="isEditable" value="true">
					
						<!-- Do not display buttons if offered -->
						<logic:equal name="instructionalOfferingDetailForm" property="notOffered" value="false">
					
							<html:submit property="op" 
								styleClass="btn" accesskey="C" titleKey="title.addConfig">
								<bean:message key="button.addConfig" />
							</html:submit>
						</logic:equal>
						
					</logic:equal>
	
					<!-- Display buttons only if managed by current user -->
					<logic:equal name="instructionalOfferingDetailForm" property="isManager" value="true">

						<logic:equal name="instructionalOfferingDetailForm" property="notOffered" value="false">
						<%--
							<bean:define id="cos" name="instructionalOfferingDetailForm" property="courseOfferings" />
							<% if ( ((java.util.List)cos).size()>1 ) { %>
							<html:submit property="op" 
								styleClass="btn" accesskey="R" titleKey="title.addReservation">
								<bean:message key="button.addReservation" />
							</html:submit>
							<% } --%>
							
							<html:submit property="op" 
								styleClass="btn" accesskey="L" titleKey="title.crossLists">
								<bean:message key="button.crossLists" />
							</html:submit>
						</logic:equal>
					</logic:equal>

					<logic:equal name="instructionalOfferingDetailForm" property="isFullyEditable" value="true">
						<!-- Display 'Make Offered' if offering is currently 'Not Offered' -->
						<logic:equal name="instructionalOfferingDetailForm" property="notOffered" value="true">
							<html:submit property="op" 
								onclick="return confirmMakeOffered();"
								styleClass="btn" accesskey="F" titleKey="title.makeOffered">
								<bean:message key="button.makeOffered" />
							</html:submit>
							
						<% if (user!=null
								&& user.getRole().equals(Roles.ADMIN_ROLE)) { %>
							<html:submit property="op" 
								onclick="return confirmDelete();"
								styleClass="btn" accesskey="D" titleKey="title.deleteIo">
								<bean:message key="button.deleteIo" />
							</html:submit>
						<% } %>
						
						</logic:equal>
	
						<!-- Display 'Make NOT Offered' if offering is currently 'Offered' -->
						<logic:notEqual name="instructionalOfferingDetailForm" property="notOffered" value="true">
							<html:submit property="op" 
								onclick="return confirmMakeNotOffered();"
								styleClass="btn" accesskey="F" titleKey="title.makeNotOffered">
								<bean:message key="button.makeNotOffered" />
							</html:submit>
						</logic:notEqual>
			
					</logic:equal>
				
					<logic:notEmpty name="instructionalOfferingDetailForm" property="previousId">
						<html:submit property="op" 
								styleClass="btn" accesskey="P" titleKey="title.previousInstructionalOffering">
							<bean:message key="button.previousInstructionalOffering" />
						</html:submit> 
					</logic:notEmpty>
					<logic:notEmpty name="instructionalOfferingDetailForm" property="nextId">
						<html:submit property="op" 
							styleClass="btn" accesskey="N" titleKey="title.nextInstructionalOffering">
							<bean:message key="button.nextInstructionalOffering" />
						</html:submit> 
					</logic:notEmpty>

					<tt:back styleClass="btn" name="Back" title="Return to %% (Alt+B)" accesskey="B" type="InstructionalOffering">
						<bean:write name="instructionalOfferingDetailForm" property="instrOfferingId"/>
					</tt:back>
				</tt:section-header>					
				
				</html:form>
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
			<TD width="20%" valign="top">Course Offerings: </TD>
			<TD>
				<TABLE border="0" width="100%" cellspacing="0" cellpadding="2">
					<TR>
						<TD align="center" class="WebTableHeader">&nbsp;</TD>
						<TD align="left" class="WebTableHeader">Title</TD>
						<TD align="left" class="WebTableHeader">Schedule of Classes Note</TD>
						<logic:equal name="instructionalOfferingDetailForm" property="hasDemandOfferings" value="true">
							<TD align="left" class="WebTableHeader">Demands From</TD>
						</logic:equal>
						<TD align="center">&nbsp;</TD>
					</TR>
				<logic:iterate id="co" name="instructionalOfferingDetailForm" property="courseOfferings" >
					<TR>
						<TD align="center">&nbsp;<logic:equal name="co" property="isControl" value="true"><IMG src="images/tick.gif" alt="Controlling Course" title="Controlling Course" border="0"></logic:equal>&nbsp;</TD>
						<TD class="BottomBorderGray"><bean:write name="co" property="courseNameWithTitle"/></TD>
						<TD class="BottomBorderGray">&nbsp;<bean:write name="co" property="scheduleBookNote"/></TD>
						<logic:equal name="instructionalOfferingDetailForm" property="hasDemandOfferings" value="true">
							<TD class="BottomBorderGray">&nbsp;
							<%
								CourseOffering cod = ((CourseOffering)co).getDemandOffering();
								if (cod!=null) out.write(cod.getCourseName()); 
							 %>
							</TD>
						</logic:equal>
						<TD align="right" class="BottomBorderGray">
							<!-- Display buttons if course offering is owned by current user -->
							<% 
								String courseOfferingId = ((CourseOffering)co).getUniqueId().toString();
								boolean isEditableBy = ((CourseOffering)co).isEditableBy(Web.getUser(session));
								boolean isLimitEditableBy = ((CourseOffering)co).isLimitedEditableBy(Web.getUser(session));
								if (isEditableBy || isLimitEditableBy) {
							%>
							
							<html:form action="/courseOfferingEdit" styleClass="FormWithNoPadding">
								<html:hidden property="courseOfferingId" value="<%= courseOfferingId %>" />

								<!-- TODO Reservations functionality to be removed later -->
								<html:submit property="op" 
									styleClass="btn" titleKey="title.addReservationCo">
									<bean:message key="button.addReservation" />
								</html:submit>
								<!-- End -->
								<% if (isEditableBy) { %>
								<html:submit property="op" 
									styleClass="btn" titleKey="title.editCourseOffering">
									<bean:message key="button.editCourseOffering" />
								</html:submit>
								<% } %>
							</html:form>
							<%
								}
							%>
						</TD>
					</TR>
				</logic:iterate>
				</TABLE>
			</TD>
		</TR>
		
		<TR>
			<TD>Enrollment: </TD>
			<TD>
				<bean:write name="instructionalOfferingDetailForm" property="enrollment" /> 
			</TD>
		</TR>

		<TR>
			<TD>Last Enrollment: </TD>
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
				<TD>Projected Demand: </TD>
				<TD>
					<bean:write name="instructionalOfferingDetailForm" property="projectedDemand" /> 
				</TD>
			</TR>
		</logic:notEqual>

		<TR>
			<TD>Offering Limit: </TD>
			<TD>
				<logic:equal name="instructionalOfferingDetailForm" property="unlimited" value="false">
					<bean:write name="instructionalOfferingDetailForm" property="limit" /> 
					<% if (request.getAttribute("limitsDoNotMatch")!=null) { %>
						&nbsp;
						<img src='images/Error16.jpg' alt='Limits do not match' title='Limits do not match' border='0' align='top'> &nbsp;
						<font color="#FF0000">Limit does not match sum of course limits - <%= request.getAttribute("limitsDoNotMatch").toString() %></font>
					<% } %>
				</logic:equal>
				<logic:equal name="instructionalOfferingDetailForm" property="unlimited" value="true">
					<img src='images/infinity.gif' alt='Unlimited Enrollment' title='Unlimited Enrollment' border='0' align='top'>
				</logic:equal>
			</TD>
		</TR>

		<TR>
			<TD>Consent: </TD>
			<TD>
				<bean:write name="instructionalOfferingDetailForm" property="consentType" /> 
			</TD>
		</TR>

		<TR>
			<TD>Designator Required: </TD>
			<TD>
				<logic:equal name="instructionalOfferingDetailForm" property="designatorRequired" value="true">
					<IMG src="images/tick.gif" alt="Designator Required" title="Designator Required" border="0">
				</logic:equal>
				<logic:equal name="instructionalOfferingDetailForm" property="designatorRequired" value="false">
					No
				</logic:equal>&nbsp;
			</TD>
		</TR>
		<TR>
			<TD>Credit:</TD>
			<TD>
				<bean:write name="instructionalOfferingDetailForm" property="creditText" />
			<TD>
		</TR>
		
		<logic:notEmpty name="instructionalOfferingDetailForm" property="catalogLinkLabel">
		<TR>
			<TD>Course Catalog: </TD>
			<TD>
				<A href="<bean:write name="instructionalOfferingDetailForm" property="catalogLinkLocation" />" target="_blank"><bean:write name="instructionalOfferingDetailForm" property="catalogLinkLabel" /></A>
			</TD>
		</TR>
		</logic:notEmpty>
		
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
									session,
				    		        WebSolver.getClassAssignmentProxy(session),
				    		        WebSolver.getExamSolver(session),
				    		        frm.getInstrOfferingId(), 
				    		        Web.getUser(session), out,
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
		

<!-- Reservations -->
		<% if (request.getAttribute(Reservation.RESV_REQUEST_ATTR)!=null) { %>
			<TR>
				<TD colspan="2" >&nbsp;</TD>
			</TR>

			<TR>
				<TD colspan="2">
					<TABLE width="100%" cellspacing="0" cellpadding="0" border="0" style="margin:0;">
						<%=request.getAttribute(Reservation.RESV_REQUEST_ATTR)%>
					</TABLE>
				</TD>
			</TR>
		<% } %>
		
		<TR>
			<TD colspan="2">
				<tt:exams type='InstructionalOffering' add='true'>
					<bean:write name="<%=frmName%>" property="instrOfferingId"/>
				</tt:exams>
			</TD>
		</TR>
		
		<tt:last-change type='InstructionalOffering'>
			<bean:write name="<%=frmName%>" property="instrOfferingId"/>
		</tt:last-change>		


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
					
				<!-- Display buttons only if editable by current user -->
				<logic:equal name="instructionalOfferingDetailForm" property="isEditable" value="true">
				
					<!-- Do not display buttons if offered -->
					<logic:equal name="instructionalOfferingDetailForm" property="notOffered" value="false">
				
						<html:submit property="op" 
							styleClass="btn" accesskey="C" titleKey="title.addConfig">
							<bean:message key="button.addConfig" />
						</html:submit>
					</logic:equal>
					
				</logic:equal>

				<!-- Display buttons only if managed by current user -->
				<logic:equal name="instructionalOfferingDetailForm" property="isManager" value="true">

					<logic:equal name="instructionalOfferingDetailForm" property="notOffered" value="false">
					<%--
						<bean:define id="cos" name="instructionalOfferingDetailForm" property="courseOfferings" />
						<% if ( ((java.util.List)cos).size()>1 ) { %>
						<html:submit property="op" 
							styleClass="btn" accesskey="R" titleKey="title.addReservation">
							<bean:message key="button.addReservation" />
						</html:submit>
						<% } --%>

						<html:submit property="op" 
							styleClass="btn" accesskey="L" titleKey="title.crossLists">
							<bean:message key="button.crossLists" />
						</html:submit>
					</logic:equal>

				</logic:equal>

				<logic:equal name="instructionalOfferingDetailForm" property="isFullyEditable" value="true">
					<!-- Display 'Make Offered' if offering is currently 'Not Offered' -->
					<logic:equal name="instructionalOfferingDetailForm" property="notOffered" value="true">
						<html:submit property="op" 
							onclick="return confirmMakeOffered();"
							styleClass="btn" accesskey="F" titleKey="title.makeOffered">
							<bean:message key="button.makeOffered" />
						</html:submit>

						<% if (user!=null
								&& user.getRole().equals(Roles.ADMIN_ROLE)) { %>
						<html:submit property="op" 
							onclick="return confirmDelete();"
							styleClass="btn" accesskey="D" titleKey="title.deleteIo">
							<bean:message key="button.deleteIo" />
						</html:submit>
						<% } %>
						
					</logic:equal>
	
					<!-- Display 'Make NOT Offered' if offering is currently 'Offered' -->
					<logic:notEqual name="instructionalOfferingDetailForm" property="notOffered" value="true">
						<html:submit property="op" 
							onclick="return confirmMakeNotOffered();"
							styleClass="btn" accesskey="F" titleKey="title.makeNotOffered">
							<bean:message key="button.makeNotOffered" />
						</html:submit>
					</logic:notEqual>
		
				</logic:equal>

				<logic:notEmpty name="instructionalOfferingDetailForm" property="previousId">
					<html:submit property="op" 
							styleClass="btn" accesskey="P" titleKey="title.previousInstructionalOffering">
						<bean:message key="button.previousInstructionalOffering" />
					</html:submit> 
				</logic:notEmpty>
				<logic:notEmpty name="instructionalOfferingDetailForm" property="nextId">
					<html:submit property="op" 
						styleClass="btn" accesskey="N" titleKey="title.nextInstructionalOffering">
						<bean:message key="button.nextInstructionalOffering" />
					</html:submit> 
				</logic:notEmpty>

				<tt:back styleClass="btn" name="Back" title="Return to %% (Alt+B)" accesskey="B" type="InstructionalOffering">
					<bean:write name="instructionalOfferingDetailForm" property="instrOfferingId"/>
				</tt:back>
				
				</html:form>					
			</TD>
		</TR>

	</TABLE>
