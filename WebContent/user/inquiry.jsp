<%--
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
--%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="tt" uri="http://www.unitime.org/tags-custom" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="loc" uri="http://www.unitime.org/tags-localization" %>
<loc:bundle name="CourseMessages"><s:set var="msg" value="#attr.MSG"/> 
<s:form action="inquiry" focusElement="type" enctype="multipart/form-data" method="POST" id="form">
<SCRIPT type="text/javascript">
	function doDel(id) {
		document.getElementById('deleteId').value = id;
		document.getElementById('form').submit();
	}
	function doDelFile(name) {
		document.getElementById('deleteFile').value = name;
		document.getElementById('form').submit();
	}
</SCRIPT>
	<s:hidden name="deleteId" id="deleteId" value=""/>
	<s:hidden name="deleteFile" id="deleteFile" value=""/>
	<s:hidden name="op2" value="Resubmit"/>
	
	<s:if test="form.op == 'Sent'">
	<table class="unitime-MainTable">
		<TR>
			<TD colspan="2">
				<loc:message name="messageInquirySubmitted"/>
			</TD>
		</TR>
		<TR>
			<TD valign="middle" colspan='2' class='WelcomeRowHead'>
				&nbsp;
			</TD>
		</TR>
		
		<TR>
			<TD valign="middle" colspan='2' align="right">
				<s:submit name='op' value='%{#msg.actionInquirySubmitAnother()}'
					accesskey='%{#msg.accessInquirySubmitAnother()}' title='%{#msg.titleInquirySubmitAnother(#msg.accessInquirySubmitAnother())}'/>
				<s:submit name='op' value='%{#msg.actionInquiryBack()}'
					accesskey='%{#msg.accessInquiryBack()}' title='%{#msg.titleInquiryBack(#msg.accessInquiryBack())}'/>
			</TD>
		</TR>
	</table>
	</s:if>
	<s:else>
	<table class="unitime-MainTable">
		<TR>
			<TD colspan="2">
				<tt:section-header>
					<tt:section-title><loc:message name="sectionInquiry"/></tt:section-title>
					<s:submit name='op' value='%{#msg.actionInquirySubmit()}'
						accesskey='%{#msg.accessInquirySubmit()}' title='%{#msg.titleInquirySubmit(#msg.accessInquirySubmit())}'/>
					<s:submit name='op' value='%{#msg.actionInquiryCancel()}'
						accesskey='%{#msg.accessInquiryCancel()}' title='%{#msg.titleInquiryCancel(#msg.accessInquiryCancel())}'/>
				</tt:section-header>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="propCategory"/></TD>
			<TD>
				<s:select name="form.type" onchange="submit();"
					list="form.typeOptions" listKey="id" listValue="value"/>
				&nbsp;<s:fielderror fieldName="form.type"/>
			</TD>
		</TR>
		
		<s:if test="form.noRole == false">
		<TR>
			<TD><loc:message name="propEmailCC"/></TD>
			<TD>
				<s:select name="form.puid"
					list="#request.managerList" listKey="externalUniqueId" listValue="getName(nameFormat)"
					headerKey="" headerValue="%{#msg.itemSelect()}"/>
				<s:submit name='op' value='%{#msg.actionAddRecipient()}'
					accesskey='%{#msg.accessAddRecipient()}' title='%{#msg.titleAddRecipient(#msg.accessAddRecipient())}'/>
				&nbsp;<s:fielderror fieldName="form.puid"/>
			</TD>
		</TR>

		<s:if test="form.carbonCopy != null && !form.carbonCopy.isEmpty()">
		<TR>
			<TD>&nbsp;</TD>
			<TD>
				<s:iterator value="form.carbonCopy" var="cc" status="stat"><s:set var="ctr" value="#stat.index"/>
					<s:hidden name="form.carbonCopy[%{#ctr}]"/>
					<s:hidden name="form.carbonCopyName[%{#ctr}]"/>
					<s:property value="form.carbonCopyName[#ctr]"/> &lt;<s:property value="#cc"/>&gt;
					<img src="images/cancel.png" border="0"
						title="${MSG.titleDeleteRecipient()}"
						class="btn" style="border:0;background-color:#FFFFFF;vertical-align:middle;"
						onclick="doDel('${ctr}');"/>
					<s:if test="#stat.last == false"><br></s:if>
				</s:iterator>
			</TD>
		</TR>
		</s:if>
		</s:if>
		
		<TR>
			<TD><loc:message name="propEmailSubject"/></TD>
			<TD>
				<s:textfield name="form.subject" size="120" maxlength="100"/>
				&nbsp;<s:fielderror fieldName="form.subject"/>
			</TD>
		</TR>

		<TR>
			<TD valign="top"><loc:message name="propEmailMessage"/></TD>
			<TD>
				<s:textarea name="form.message" rows="20" cols="120"/>
				&nbsp;<s:fielderror fieldName="form.message"/>
			</TD>
		</TR>

		<TR>
			<TD nowrap><loc:message name="propEmailAttachment"/></TD>
			<TD>
				<s:file name="form.file" size="100"/>
				<s:submit name='op' value='%{#msg.actionAttachFile()}'
					accesskey='%{#msg.accessAttachFile()}' title='%{#msg.titleAttachFile(#msg.accessAttachFile())}'/>
			</TD>
		</TR>
		<s:if test="attachedFiles != null && !attachedFiles.isEmpty()">
			<TR><TD>&nbsp;</TD><TD>
			<s:iterator value="attachedFiles" var="name" status="stat">
				<s:property value="#name"/>
				<loc:message name="attachmentFileSize"><s:property value="getAttachedFileSize(#name)"/></loc:message>
				<img src="images/cancel.png" border="0"
					title="${MSG.titleDeleteAttachedFile(name)}"
					class="btn" style="border:0;background-color:#FFFFFF;vertical-align:middle;"
					onclick="doDelFile('${name}');"/>
				<s:if test="#stat.last == false"><br></s:if>
			</s:iterator>
			</TD></TR>
		</s:if>

		<TR>
			<TD valign="middle" colspan='2' align="right" style='border-top:1px solid #9CB0CE; padding-top: 4px;'>
				<s:submit name='op' value='%{#msg.actionInquirySubmit()}'
					accesskey='%{#msg.accessInquirySubmit()}' title='%{#msg.titleInquirySubmit(#msg.accessInquirySubmit())}'/>
				<s:submit name='op' value='%{#msg.actionInquiryCancel()}'
					accesskey='%{#msg.accessInquiryCancel()}' title='%{#msg.titleInquiryCancel(#msg.accessInquiryCancel())}'/>
			</TD>
		</TR>
		
		<TR><TD colspan='2'>&nbsp;</TD></TR>
		
		<TR>
			<TD colspan="2">
				<tt:section-header>
					<tt:section-title><loc:message name="sectionContactInformation"/></tt:section-title>
				</tt:section-header>
			</TD>
		</TR>

		<tt:hasProperty name="tmtbl.contact.address">
			<TR>
				<TD valign="top"><loc:message name="propContactAddress"/></TD>
				<TD><tt:property name="tmtbl.contact.address"/></TD>
			</TR>
		</tt:hasProperty>
		
		<tt:hasProperty name="tmtbl.contact.phone">
			<TR>
				<TD valign="top"><loc:message name="propContactPhone"/></TD>
				<TD><tt:property name="tmtbl.contact.phone"/></TD>
			</TR>
		</tt:hasProperty>
		
		<tt:hasProperty name="tmtbl.contact.office_hours">
			<TR>
				<TD valign="top"><loc:message name="propContactOfficeHours"/></TD>
				<TD><tt:property name="tmtbl.contact.office_hours"/></TD>
			</TR>
		</tt:hasProperty>
		
		<tt:hasProperty name="tmtbl.contact.email">
			<TR>
				<TD valign="top"><loc:message name="propContactEmail"/></TD>
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
	</table>
	</s:else>
</s:form>
</loc:bundle>
