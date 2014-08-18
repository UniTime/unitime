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
<%@ page import="org.unitime.timetable.model.Department" %>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.form.NonUnivLocationForm" %>
<%@page import="org.cpsolver.ifs.util.DistanceMetric"%>
<%@page import="org.unitime.timetable.ApplicationProperties"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<%@ taglib uri="http://www.unitime.org/tags-localization" prefix="loc" %>

<%		
	// Get Form 
	String frmName = "nonUnivLocationForm";	
	NonUnivLocationForm frm = (NonUnivLocationForm) request.getAttribute(frmName);
%>	

<tiles:importAttribute />
<html:form action="/addNonUnivLocation" focus="name">
	<loc:bundle name="CourseMessages">
	
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header title="Add Non University Location">
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="A" 
							title="Add Non University Location (Alt+A)">
						<bean:message key="button.addNew" />
					</html:submit>
					&nbsp;
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="B" 
						title="Back to Room List (Alt+B)">
						<bean:message key="button.returnToRoomList"/>
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

			<TR>
				<TD>Name:</TD>
				<TD width='100%'>
					<html:text property="name" maxlength="20" size="20" />
				</TD>
			</TR>
			
			<TR>
				<TD>External Id:</TD>
				<TD width='100%'>
					<html:text property="externalId" maxlength="40" size="40" />
				</TD>
			</TR>

			<TR>
				<TD>Type:</TD>
				<TD width='100%'>
					<html:select property="type">
						<html:optionsCollection property="roomTypes" label="label" value="uniqueId"/>
					</html:select>
				</TD>
			</TR>

			<TR>
				<TD>Capacity:</TD>
				<TD>
					<html:text property="capacity" maxlength="15" size="10"/>
				</TD>
			</TR>

			<TR>
				<TD>Coordinates:</TD>
				<TD>
					<html:text property="coordX" maxlength="12" size="12" styleId="coordX" onchange="setMarker();"/>, <html:text property="coordY" maxlength="12" size="12" styleId="coordY" onchange="setMarker();"/>
					<% DistanceMetric.Ellipsoid ellipsoid = DistanceMetric.Ellipsoid.valueOf(ApplicationProperties.getProperty("unitime.distance.ellipsoid", DistanceMetric.Ellipsoid.LEGACY.name())); %>
					&nbsp;&nbsp;&nbsp;<i><%=ellipsoid.getEclipsoindName()%></i>
				</TD>
			</TR>
			
			<TR>
				<TD><loc:message name="propertyRoomArea"/></TD>
				<TD>
					<html:text property="area" maxlength="12" size="12"/> <loc:message name="roomAreaUnitsLong"/>
				</TD>
			</TR>

			
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

			<TR>
				<TD>Department:</TD>
				<TD>
					<html:select property="deptCode">
						<logic:empty name="<%=frmName%>" property="deptCode">
							<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
						</logic:empty>
						<logic:equal name="<%=frmName%>" property="deptCode" value="<%=Constants.ALL_OPTION_VALUE%>">
							<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
						</logic:equal>
						<html:options collection="<%=Department.DEPT_ATTR_NAME%>" property="value" labelProperty="label"/>
					</html:select>
				</TD>
			</TR>
			
		<tt:propertyEquals name="unitime.coordinates.googlemap" value="true">
					</table>
				</td><td width="1%" nowrap="nowrap" style="padding-right: 3px;">
					<div id="map_canvas" style="width: 600px; height: 400px; border: 1px solid #9CB0CE;"></div>
				</td></tr>
			</table>
			<table width="100%" border="0" cellspacing="0" cellpadding="3">
		</tt:propertyEquals>

		<TR>
			<TD colspan='2'>
				<tt:section-title/>
			</TD>
		</TR>
		
		<TR>
			<TD colspan='2' align='right'>
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="A" 
							title="Add Non University Location (Alt+A)">
						<bean:message key="button.addNew" />
					</html:submit>
					&nbsp;
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="B" 
						title="Back to Room List (Alt+B)">
						<bean:message key="button.returnToRoomList"/>
					</html:submit>
			</TD>
		</TR>
	</TABLE>
	</loc:bundle>
</html:form>

<tt:propertyEquals name="unitime.coordinates.googlemap" value="true">
<script type="text/javascript" src="https://maps.google.com/maps/api/js?sensor=false"></script>
<script type="text/javascript" language="javascript">
	var latlng = new google.maps.LatLng(50, -58);
	var myOptions = {
		zoom: 2,
		center: latlng,
		mapTypeId: google.maps.MapTypeId.ROADMAP
	};
	var geocoder = new google.maps.Geocoder();
	var map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);
	var marker = marker = new google.maps.Marker({
		position: latlng, 
		map: map,
		draggable: true,
		visible: false
	});

    var searchBox = createGoogleSeachControl(map);
	
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
	google.maps.event.addListener(map, 'rightclick', function(event) {
		marker.setPosition(event.latLng);
		marker.setVisible(true);
	});
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