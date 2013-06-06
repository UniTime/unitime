<!DOCTYPE html>
<!-- 
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC
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
					<td rowspan="2"><img src="http://www.unitime.org/include/unitime.png" border="0" height="100px"/></td>
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