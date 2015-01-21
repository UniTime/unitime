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
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%> 
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<html:form action="/dataImport" focus="file" enctype="multipart/form-data">

	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">

	<logic:messagesPresent>
		<TR>
			<TD colspan='2'>
				<tt:section-title><font color='red'>Errors</font></tt:section-title>
			</TD>
		</TR>
		<TR>
			<TD colspan="2" align="left" class="errorCell">
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
		<TR><TD>&nbsp;</TD></TR>
	</logic:messagesPresent>
	<logic:notEmpty name="table" scope="request">
		<TR><TD colspan="2">
			<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
				<bean:write name="table" scope="request" filter="false"/>
			</TABLE>
		</TD></TR>
		<TR><TD colspan='2'>&nbsp;</TD></TR>
	</logic:notEmpty>
	<logic:notEmpty name="log" scope="request">
		<TR>
			<TD colspan='2'>
				<tt:section-header>
					<tt:section-title>
						Log of <bean:write name="logname" scope="request" filter="false"/>
					</tt:section-title>
					<bean:define id="logid" name="logid" scope="request"/>
					<input type="hidden" name="log" value="<%=logid%>">
					<html:submit onclick="displayLoading();" accesskey="R" property="op" value="Refresh" title="Refresh Log (Alt+R)"/>
				</tt:section-header>
			</TD>
		</TR>
		<TR>
  			<TD colspan='2'>
  				<blockquote>
	  				<bean:write name="log" scope="request" filter="false"/>
  				</blockquote>
  			</TD>
		</TR>
	</logic:notEmpty>	

		<TR>
			<TD colspan="2">
				<tt:section-header>
					<tt:section-title>Data Import</tt:section-title>
					<html:submit property="op" onclick="displayLoading()">Import</html:submit>
				</tt:section-header>
			</TD>
		</TR>
		
		<TR>
			<TD nowrap>File:</TD>
			<TD>
				<html:file name="dataImportForm" property="file" size="100" maxlength="255"/>
			</TD>
		</TR>
		
		<TR>
			<TD colspan="2">
				&nbsp;
			</TD>
		</TR>
		
		<TR>
			<TD colspan="2">
				<tt:section-header>
					<tt:section-title>Data Export</tt:section-title>
					<html:submit property="op" onclick="displayLoading()">Export</html:submit>
				</tt:section-header>
			</TD>
		</TR>
	
		<TR>
			<TD nowrap>Type:</TD>
			<TD>
				<html:select property="export">
					<html:option value="">Select...</html:option>
					<html:optionsCollection name="dataImportForm" property="exportTypes" value="value" label="label"/>
				</html:select>
			</TD>
		</TR>

		<TR>
			<TD colspan="2">
				&nbsp;
			</TD>
		</TR>

		<TR>
			<TD colspan="2">
				<tt:section-title>Options</tt:section-title>
			</TD>
		</TR>
		
		<TR>
			<TD nowrap>Email (Log, Export XML):</TD>
			<TD>
				<html:checkbox property="email" onclick="document.getElementById('eml').style.display=(this.checked?'inline':'none');" styleId="emlChk"/>
				<html:text property="address" size="70" styleId="eml" style="display:none;"/>
				<script type="text/javascript">document.getElementById('eml').style.display=(document.getElementById('emlChk').checked?'inline':'none');</script>
			</TD>
		</TR>
		
		<TR>
			<TD colspan="2">
				<tt:section-title/>
			</TD>
		</TR>

		<TR>
			<TD align="right" colspan='2'>
				<html:submit property="op" onclick="displayLoading()">Import</html:submit>
				<html:submit property="op" onclick="displayLoading()">Export</html:submit>
			</TD>
		</TR>
		
	</TABLE>
</html:form>
