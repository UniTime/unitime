<!DOCTYPE html>
<!-- 
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
 -->
<html>
	<head>
		<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>
		<title>Password Change</title>
	</head>
	<body style="font-family: sans-serif, verdana, arial;">
		<table style="border: 1px solid #9CB0CE; padding: 5px; margin-top: 10px; min-width: 800px;" align="center">
			<!-- header table -->
			<tr><td><table width="100%">
				<tr>
					<td rowspan="2"><img src="http://www.unitime.org/include/unitime.png" border="0" height="80px"/></td>
					<td colspan="2" style="font-size: x-large; font-weight: bold; color: #333333; text-align: right; padding: 20px 30px 10px 10px;">Password Change</td>
				</tr>
				</table></td></tr>
			<tr><td>
				Hi ${username},<br>
				<br>
				There was recently a request to change the password on your account.<br>
				<br>
				If you requested this password change, please set a new password by following the link below:<br>
				<br>
				<a href='${url}'>${url}</a><br>
				<br>
				If you do not want to change your password, ignore this message. The link expires in 48 hours.<br>
				<br>
				Thanks,<br>
				${sender}
			</td></tr>
		</table>
		<!-- footer -->
		<table style="width: 800px; margin-top: -3px;" align="center">
			<tr>
				<td width="33%" align="left" style="font-size: 9pt; vertical-align: top; font-style: italic; color: #9CB0CE; white-space: nowrap;">${version}</td>
				<td width="34%" align="center" style="font-size: 9pt; vertical-align: top; font-style: italic; color: #9CB0CE; white-space: nowrap;">${msg.pageCopyright()}</td>
				<td width="33%" align="right" style="font-size: 9pt; vertical-align: top; font-style: italic; color: #9CB0CE; white-space: nowrap;">${ts?string(const.timeStampFormat())}</td>
			</tr>
		</table>
	</body>
</html>