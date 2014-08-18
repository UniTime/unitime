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
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.model.TimetableManager" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %> 

<tiles:importAttribute />

<SCRIPT type="text/javascript" language="javascript">
	function doDel(id) {
		var delId = document.inquiryForm.deleteId;
		delId.value = id;
	}
</SCRIPT>				

<html:form action="/inquiry" focus="type">
	<INPUT type="hidden" name="deleteId" id="deleteId" value="">
	
	<logic:equal name="inquiryForm" property="op" value="Sent">
	
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan="2">
				Your inquiry was successfully submitted. Thank you.
			</TD>
		</TR>
		<TR>
			<TD valign="middle" colspan='2' class='WelcomeRowHead'>
				&nbsp;
			</TD>
		</TR>
		
		<TR>
			<TD valign="middle" colspan='2' align="right">
				<html:submit property="op" accesskey="S" value="Submit Another Inquiry" />
				<html:submit property="op" accesskey="B" value="Back" />
			</TD>
		</TR>
	</TABLE>
	</logic:equal>
	<logic:notEqual name="inquiryForm" property="op" value="Sent">
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan="2">
				<tt:section-header>
					<tt:section-title>Inquiry</tt:section-title>
					<html:submit property="op" accesskey="S" value="Submit" styleClass="btn" />
					<html:submit property="op" accesskey="C" value="Cancel" styleClass="btn" />
				</tt:section-header>
			</TD>
		</TR>

		<TR>
			<TD>Category:</TD>
			<TD>
				<html:select property="type" onchange="submit();">
					<html:optionsCollection name="inquiryForm" property="typeOptions" label="value" value="id" />
				</html:select>
				&nbsp;<html:errors property="type"/>
			</TD>
		</TR>
		
		<logic:equal name="inquiryForm" property="noRole" value="false">
		<TR>
			<TD>CC:</TD>
			<TD>
				<html:select property="puid">
					<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
					<html:options collection="<%= TimetableManager.MGR_LIST_ATTR_NAME %>" 
						property="emailAddress" labelProperty="name"/>
					</html:select>
					<html:submit property="op" accesskey="I" titleKey="title.insertAddress"  styleClass="btn" >
						<bean:message key="button.insertAddress" />
					</html:submit>
				&nbsp;<html:errors property="puid"/>
			</TD>
		</TR>

		<logic:notEmpty name="inquiryForm" property="carbonCopy">
		<TR>
			<TD>&nbsp;</TD>
			<TD>
				<logic:iterate id="cc" name="inquiryForm" property="carbonCopy" indexId="ctr">
					<INPUT type="hidden" name='<%= "carbonCopy[" + ctr + "]" %>' value="<%=cc%>" />
					<font class="font8Gray"><%=cc%></font>
					<html:image 
						src="images/Error16.jpg" border="0" align="absmiddle"						
						titleKey="title.deleteAddress"  
						styleClass="btn" style="border:0;background-color:#FFFFFF;"
						onclick="<%= \"javascript: doDel('\" + ctr + \"');\"%>" />&nbsp;
				</logic:iterate>
			</TD>
		</TR>
		</logic:notEmpty>
		</logic:equal>
		
		<TR>
			<TD>Subject:</TD>
			<TD>
				<html:text property="subject" size="120" maxlength="100"/>
				&nbsp;<html:errors property="subject"/>
			</TD>
		</TR>

		<TR>
			<TD valign="top">Message:</TD>
			<TD>
				<html:errors property="message"/>
				<html:textarea property="message" rows="20" cols="120"/>
			</TD>
		</TR>

		<TR>
			<TD valign="middle" colspan='2' align="right" style='border-top:1px solid black'>
				<html:submit property="op" accesskey="S" value="Submit" styleClass="btn" />
				<html:submit property="op" accesskey="C" value="Cancel" styleClass="btn" />
			</TD>
		</TR>
		
		<TR><TD colspan='2'>&nbsp;</TD></TR>
		
		<TR>
			<TD colspan="2">
				<tt:section-header>
					<tt:section-title>Contact Information</tt:section-title>
				</tt:section-header>
			</TD>
		</TR>

		<tt:hasProperty name="tmtbl.contact.address">
			<TR>
				<TD valign="top">Address:</TD>
				<TD><tt:property name="tmtbl.contact.address"/></TD>
			</TR>
		</tt:hasProperty>
		
		<tt:hasProperty name="tmtbl.contact.phone">
			<TR>
				<TD valign="top">Phone:</TD>
				<TD><tt:property name="tmtbl.contact.phone"/></TD>
			</TR>
		</tt:hasProperty>
		
		<tt:hasProperty name="tmtbl.contact.office_hours">
			<TR>
				<TD valign="top">Office Hours:</TD>
				<TD><tt:property name="tmtbl.contact.office_hours"/></TD>
			</TR>
		</tt:hasProperty>
		
		<tt:hasProperty name="tmtbl.contact.email">
			<TR>
				<TD valign="top">Email:</TD>
				<TD>
					<tt:hasProperty name="tmtbl.contact.email_mailto">
						<a href="mailto:%tmtbl.contact.email_mailto%"><tt:property name="tmtbl.contact.email"/></a>
					</tt:hasProperty>
					<tt:notHasProperty name="tmtbl.contact.email_mailto">
						<a href="mailto:%tmtbl.contact.email%"><tt:property name="tmtbl.contact.email"/></a>
					</tt:notHasProperty>
				</TD>
			</TR>
		</tt:hasProperty>
	</TABLE>
	</logic:notEqual>
</html:form>
