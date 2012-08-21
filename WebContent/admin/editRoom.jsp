<%-- 
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008-2009, UniTime LLC
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
<%@ page import="org.unitime.timetable.form.EditRoomForm" %>
<%@ page import="org.apache.struts.util.LabelValueBean" %>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.model.Department" %>
<%@ page import="org.unitime.timetable.model.Building" %>
<%@ page import="net.sf.cpsolver.ifs.util.DistanceMetric"%>
<%@page import="org.unitime.timetable.ApplicationProperties"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<%
	// Get Form 
	String frmName = "editRoomForm";	
	EditRoomForm frm = (EditRoomForm) request.getAttribute(frmName);
%>	

<tiles:importAttribute />
<html:form action="/editRoom" focus="name">
	<html:hidden property="id"/>
	<html:hidden property="room"/>

	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<logic:empty name="<%=frmName%>" property="id">
						<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="S" titleKey="title.saveRoom">
							<bean:message key="button.save" />
						</html:submit>
					</logic:empty>
					<logic:notEmpty name="<%=frmName%>" property="id">
						<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="U" titleKey="title.updateRoom">
							<bean:message key="button.update" />
						</html:submit>
					</logic:notEmpty>
					&nbsp;
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="B" titleKey="title.returnToRoomList">
						<bean:message key="button.returnToRoomDetail" />
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
		
		<tt:propertyEquals name="unitime.coordinates.googlemap" value="true">
			</table>
			<table width="100%" border="0" cellspacing="0" cellpadding="0">
				<tr><td valign="top">
					<table width="100%" border="0" cellspacing="0" cellpadding="3">
		</tt:propertyEquals>
		
		<logic:empty name="<%=frmName%>" property="id">
			<TR>
				<TD>Building:</TD>
				<TD width='100%'>
					<html:select property="bldgId" onchange="javascript: buldingChanged(this.value);">
						<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
						<html:options collection="<%=Building.BLDG_LIST_ATTR_NAME%>" property="value" labelProperty="label"/>
					</html:select>
				</TD>
			</TR>

			<TR>
				<TD>Room Number:</TD>
				<TD width='100%'>
					<html:text property="name" maxlength="10" size="10" />
				</TD>
			</TR>

			<TR>
				<TD nowrap>Department:</TD>
				<TD>
					<html:select property="controlDept">
						<html:options collection="<%=Department.DEPT_ATTR_NAME%>" property="value" labelProperty="label"/>
					</html:select>
				</TD>
			</TR>
		</logic:empty>
		
		
		<logic:notEmpty name="<%=frmName%>" property="id">
			<TR>
				<TD>Name:</TD>
				<sec:authorize access="hasPermission(#editRoomForm.id, 'Location', 'RoomEditChangeRoomProperties')">
				<TD width='100%'>
					<bean:write name="<%=frmName%>" property="bldgName"/>
					<logic:empty name="<%=frmName%>" property="bldgName">
						<html:text property="name" maxlength="20" size="20" />
					</logic:empty>
					<logic:notEmpty name="<%=frmName%>" property="bldgName">
						<html:text property="name" maxlength="10" size="10" />
					</logic:notEmpty>
				</TD>
				</sec:authorize>
				<sec:authorize access="!hasPermission(#editRoomForm.id, 'Location', 'RoomEditChangeRoomProperties')">
					<TD width='100%'>
						<bean:write name="<%=frmName%>" property="bldgName"/>
						<bean:write name="<%=frmName%>" property="name"/>
						<html:hidden property="name"/>
					</TD>
				</sec:authorize>
			</TR>
		</logic:notEmpty>
			
		<logic:equal name="<%=frmName%>" property="room" value="true">
			<sec:authorize access="hasPermission(#editRoomForm.id, 'Location', 'RoomEditChangeExternalId')">
				<TR>
					<TD>External Id:</TD>
					<TD width='100%'>
						<html:text property="externalId" maxlength="40" size="40" />
					</TD>
				</TR>
			</sec:authorize>
			<sec:authorize access="!hasPermission(#editRoomForm.id, 'Location', 'RoomEditChangeExternalId')">
				<html:hidden property="externalId"/>
			</sec:authorize>
		</logic:equal>
		
		<sec:authorize access="hasPermission(#editRoomForm.id, 'Location', 'RoomEditChangeType')">
			<TR>
				<TD>Type:</TD>
				<TD width='100%'>
					<html:select property="type">
						<html:optionsCollection property="roomTypes" label="label" value="uniqueId"/>
					</html:select>
				</TD>
			</TR>
		</sec:authorize>
		<sec:authorize access="!hasPermission(#editRoomForm.id, 'Location', 'RoomEditChangeType')">
			<html:hidden property="type"/>
		</sec:authorize>

		<TR>
			<TD>Capacity:</TD>
			<TD>
				<sec:authorize access="hasPermission(#editRoomForm.id, 'Location', 'RoomEditChangeCapacity')">
					<html:text property="capacity" maxlength="15" size="10"/>
				</sec:authorize>
				<sec:authorize access="!hasPermission(#editRoomForm.id, 'Location', 'RoomEditChangeCapacity')">
					<bean:write name="<%=frmName%>" property="capacity"/>
					<html:hidden property="capacity"/>
				</sec:authorize>
			</TD>
		</TR>
		
		<logic:notEmpty name="<%=frmName%>" property="id">
			<logic:notEmpty name="<%=Department.DEPT_ATTR_NAME%>" scope="request">
				<TR>
					<TD nowrap>Controlling Department:</TD>
					<TD>
						<sec:authorize access="hasPermission(#editRoomForm.id, 'Location', 'RoomEditChangeControll')">
							<html:select property="controlDept">
								<html:option value="<%=Constants.BLANK_OPTION_VALUE%>">No controlling department</html:option>
								<html:options collection="<%=Department.DEPT_ATTR_NAME%>" property="value" labelProperty="label"/>
							</html:select>
						</sec:authorize>
						<sec:authorize access="!hasPermission(#editRoomForm.id, 'Location', 'RoomEditChangeControll')">
							<html:hidden property="controlDept"/>
							<logic:iterate scope="request" name="<%=Department.DEPT_ATTR_NAME%>" id="d">
								<logic:equal name="<%=frmName%>" property="controlDept" value="<%=((LabelValueBean)d).getValue()%>">
									<bean:write name="d" property="label"/>
								</logic:equal>
							</logic:iterate>
						</sec:authorize>
					</TD>
				</TR>
			</logic:notEmpty>
		</logic:notEmpty>
			
		<TR>
			<TD>Coordinates:</TD>
			<TD>
				<sec:authorize access="hasPermission(#editRoomForm.id, 'Location', 'RoomEditChangeRoomProperties')">
					<html:text property="coordX" maxlength="12" size="12" styleId="coordX" onchange="setMarker();"/>, <html:text property="coordY" maxlength="12" size="12" styleId="coordY" onchange="setMarker();"/>
				</sec:authorize>
				<sec:authorize access="!hasPermission(#editRoomForm.id, 'Location', 'RoomEditChangeRoomProperties')">
					<bean:write name="<%=frmName%>" property="coordX"/>, <bean:write name="<%=frmName%>" property="coordY"/>
					<html:hidden property="coordX" styleId="coordX"/>
					<html:hidden property="coordY" styleId="coordY"/>
				</sec:authorize>
				<% DistanceMetric.Ellipsoid ellipsoid = DistanceMetric.Ellipsoid.valueOf(ApplicationProperties.getProperty("unitime.distance.ellipsoid", DistanceMetric.Ellipsoid.LEGACY.name())); %>
				&nbsp;&nbsp;&nbsp;<i><%=ellipsoid.getEclipsoindName()%></i>
			</TD>
		</TR>

		<sec:authorize access="hasPermission(#editRoomForm.id, 'Location', 'RoomEditChangeRoomProperties')">
			<TR>
				<TD nowrap>Ignore Too Far Distances:</TD>
				<TD>
					<html:checkbox property="ignoreTooFar" />
				</TD>
			</TR>
			<TR>
				<TD nowrap>Ignore Room Checks:</TD>
				<TD>
					<html:checkbox property="ignoreRoomCheck" />
				</TD>
			</TR>
		</sec:authorize>
		<sec:authorize access="!hasPermission(#editRoomForm.id, 'Location', 'RoomEditChangeRoomProperties')">
			<html:hidden property="ignoreTooFar" />
			<html:hidden property="ignoreRoomCheck" />
		</sec:authorize>
		
		<sec:authorize access="hasPermission(#editRoomForm.id, 'Location', 'RoomEditChangeExaminationStatus')">
		<TR>
			<TD nowrap>Examination Room:</TD>
			<TD>
				Final: <html:checkbox property="examEnabled" onchange="document.getElementById('finPref').style.display=(this.checked?null:'none');"/>
				<tt:hasMidtermExams>
				, Midterm: <html:checkbox property="examEEnabled" onchange="document.getElementById('evenPref').style.display=(this.checked?null:'none');"/>
				</tt:hasMidtermExams>
			</TD>
		</TR>

		<TR>
			<TD nowrap>Exam Seating Capacity:</TD>
			<TD>
				<html:text property="examCapacity" maxlength="15" size="10"/>
			</TD>
		</TR>
		</sec:authorize>
		<sec:authorize access="!hasPermission(#editRoomForm.id, 'Location', 'RoomEditChangeExaminationStatus')">
			<html:hidden property="examEnabled"/>
			<html:hidden property="examEEnabled"/>
			<html:hidden property="examCapacity"/>
		</sec:authorize>
		
		<tt:propertyEquals name="unitime.coordinates.googlemap" value="true">
					</table>
				</td><td width="1%" nowrap="nowrap" style="padding-right: 3px;">
					<div id="map_canvas" style="width: 600px; height: 400px; border: 1px solid #9CB0CE;"></div>
				</td></tr>
			</table>
			<table width="100%" border="0" cellspacing="0" cellpadding="3">
		</tt:propertyEquals>

		
		<logic:notEmpty scope="request" name="PeriodPrefs">
			<logic:equal name="<%=frmName%>" property="examEnabled" value="true">
				<TR id='finPref' style='display:null;'>
			</logic:equal>
			<logic:notEqual name="<%=frmName%>" property="examEnabled" value="true">
				<TR id='finPref' style='display:none;'>
			</logic:notEqual>
				<TD nowrap valign="top">Final Examination<br>Periods Preferences:</TD>
				<TD>
					<bean:write scope="request" name="PeriodPrefs" filter="false"/>
				</TD>
			</TR>
		</logic:notEmpty>

		<logic:notEmpty scope="request" name="PeriodEPrefs">
			<logic:equal name="<%=frmName%>" property="examEEnabled" value="true">
				<TR id='evenPref' style='display:null;'>
			</logic:equal>
			<logic:notEqual name="<%=frmName%>" property="examEEnabled" value="true">
				<TR id='evenPref' style='display:none;'>
			</logic:notEqual>
				<TD nowrap valign="top">Midterm Examination<br>Periods Preferences:</TD>
				<TD>
					<bean:write scope="request" name="PeriodEPrefs" filter="false"/>
				</TD>
			</TR>
		</logic:notEmpty>
		
		<TR>
			<TD colspan='2'>
				<tt:section-title/>
			</TD>
		</TR>
			
		<TR>
			<TD colspan='2' align='right'>
					<logic:empty name="<%=frmName%>" property="id">
						<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="S" titleKey="title.saveRoom">
							<bean:message key="button.save" />
						</html:submit>
					</logic:empty>
					<logic:notEmpty name="<%=frmName%>" property="id">
						<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="U" titleKey="title.updateRoom">
							<bean:message key="button.update" />
						</html:submit>
					</logic:notEmpty>
					&nbsp;
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="B" titleKey="title.returnToRoomList">
						<bean:message key="button.returnToRoomDetail" />
					</html:submit>
			</TD>
		</TR>
	</TABLE>
	
</html:form>

<tt:propertyEquals name="unitime.coordinates.googlemap" value="true">
<script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=false"></script>
<sec:authorize access="hasPermission(#editRoomForm.id, 'Location', 'RoomEditChangeRoomProperties')">
	<script type="text/javascript" language="javascript">
		function createGoogleSeachControl(map) {
			var controlDiv = document.createElement('DIV');
		    controlDiv.index = 1;
			controlDiv.style.marginBottom = '15px';
			var controlUI = document.createElement('DIV');
			controlUI.style.backgroundColor = 'transparent';
			controlUI.style.cursor = 'pointer';
			controlUI.style.textAlign = 'center';
			controlUI.title = "Seach";
			controlDiv.appendChild(controlUI);
			var controltxtbox = document.createElement('input');
			controltxtbox.setAttribute("id", "txt_googleseach");
			controltxtbox.setAttribute("type", "text");
			controltxtbox.setAttribute("value", "");
			controltxtbox.style.height = '22px';
			controltxtbox.style.width = '450px';
			controltxtbox.style.marginRight = '2px';
			controlUI.appendChild(controltxtbox);
			var controlbtn = document.createElement('input');
			controlbtn.setAttribute("id", "btn_googleseach");
			controlbtn.setAttribute("type", "button");
			controlbtn.setAttribute("value", "Geocode");
			controlUI.appendChild(controlbtn);
			google.maps.event.addDomListener(controlbtn, 'click', function() {
				geoceodeAddress(controltxtbox.value);
			});
			controltxtbox.onkeypress = function(e) {
				var key = e.keyCode || e.which;
				if (key == 13) {
					geoceodeAddress(controltxtbox.value);
					return false;
				}
				return true;
			};
			map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(controlDiv);
			return controltxtbox;
		}
		function canDragMarker() { return true; }
	</script>
</sec:authorize>
<sec:authorize access="!hasPermission(#editRoomForm.id, 'Location', 'RoomEditChangeRoomProperties')">
	<script type="text/javascript" language="javascript">
		function createGoogleSeachControl(map) { return null; }
		function canDragMarker() { return false; }
	</script>
</sec:authorize>
<script type="text/javascript" language="javascript">
	var latlng = new google.maps.LatLng(50, -58);
	var myOptions = {
		zoom: 2,
		center: latlng,
		mapTypeId: google.maps.MapTypeId.ROADMAP
		
	};
	var geocoder = new google.maps.Geocoder();
	var map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);
	var marker = new google.maps.Marker({
		position: latlng, 
		map: map,
		draggable: true,
		visible: false
	});
	
    var searchBox = createGoogleSeachControl(map);
	
	function geoceodeAddress(address) {
		var address = document.getElementById("txt_googleseach").value;
		geocoder.geocode({ 'address': address }, function(results, status) {
			if (status == google.maps.GeocoderStatus.OK) {
				if (results[0]) {
					marker.setPosition(results[0].geometry.location);
					marker.setTitle(results[0].formatted_address);
					marker.setVisible(true);
					if (map.getZoom() <= 10) map.setZoom(16);
					map.panTo(results[0].geometry.location);
				} else {
					marker.setVisible(false);
				}
			} else {
				marker.setVisible(false);
			}
		});
	}
	
	function geoceodeMarker() {
		geocoder.geocode({'location': marker.getPosition()}, function(results, status) {
			if (status == google.maps.GeocoderStatus.OK) {
				if (results[0]) {
					marker.setTitle(results[0].formatted_address);
					if (searchBox != null)
						searchBox.value = results[0].formatted_address;
				} else {
					marker.setTitle(null);
					if (searchBox != null) searchBox.value = "";
				}
			} else {
				marker.setTitle(null);
				if (searchBox != null) searchBox.value = "";
			}
		});
	}
	
	var t = null;	
	
	google.maps.event.addListener(marker, 'position_changed', function() {
		document.getElementById("coordX").value = '' + marker.getPosition().lat().toFixed(6);
		document.getElementById("coordY").value = '' + marker.getPosition().lng().toFixed(6);
		if (t != null) clearTimeout(t);
		t = setTimeout("geoceodeMarker()", 500);
	});
	if (canDragMarker()) {
		google.maps.event.addListener(map, 'rightclick', function(event) {
			marker.setPosition(event.latLng);
			marker.setVisible(true);
		});
	} else {
		marker.setDraggable(false);
	}
	function setMarker() {
		var x = document.getElementById("coordX").value;
		var y = document.getElementById("coordY").value;
		if (x && y) {
			var pos = new google.maps.LatLng(x, y);
			marker.setPosition(pos);
			marker.setVisible(true);
			if (map.getZoom() <= 10) map.setZoom(16);
			map.panTo(pos);
		} else {
			marker.setVisible(false);
		}
	}
	setMarker();
</script>
</tt:propertyEquals>
<tt:propertyNotEquals name="unitime.coordinates.googlemap" value="true">
	<script type="text/javascript" language="javascript">
		function setMarker() {}
	</script>
</tt:propertyNotEquals>

<script type="text/javascript" language="javascript">
	// Validator
	var frmvalidator  = new Validator("editRoomForm");
	frmvalidator.addValidation("capacity","numeric");	
</script>

<SCRIPT type="text/javascript" language="javascript">

	function buldingChanged(id) {
		var xObj = document.getElementsByName('coordX')[0];
		var yObj = document.getElementsByName('coordY')[0];
		
		if (id=='') {
			xObj.value='';
			yObj.value='';
			return;
		}
		
		// Request initialization
		if (window.XMLHttpRequest) req = new XMLHttpRequest();
		else if (window.ActiveXObject) req = new ActiveXObject( "Microsoft.XMLHTTP" );

		// Response
		req.onreadystatechange = function() {
			if (req.readyState == 4) {
				if (req.status == 200) {
					// Response
					var xmlDoc = req.responseXML;
					if (xmlDoc && xmlDoc.documentElement && xmlDoc.documentElement.childNodes && xmlDoc.documentElement.childNodes.length > 0) {
						var count = xmlDoc.documentElement.childNodes.length;
						for(i=0; i<count; i++) {
							var optId = xmlDoc.documentElement.childNodes[i].getAttribute("id");
							var optVal = xmlDoc.documentElement.childNodes[i].getAttribute("value");
							if (optId=='x') xObj.value = optVal;
							if (optId=='y') yObj.value = optVal;
						}
						setMarker();
					}
				}
			}
		};
	
		// Request
		var vars = "id="+id;
		req.open( "POST", "buildingCoordsAjax.do", true );
		req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		req.setRequestHeader("Content-Length", vars.length);
		//setTimeout("try { req.send('" + vars + "') } catch(e) {}", 1000);
		req.send(vars);
	}
</SCRIPT>
