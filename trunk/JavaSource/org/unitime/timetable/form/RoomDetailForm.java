/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
 * as indicated by the @authors tag.
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
*/
package org.unitime.timetable.form;

import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/** 
 * MyEclipse Struts
 * Creation date: 05-12-2006
 * 
 * XDoclet definition:
 * @struts.form name="roomDetailForm"
 */
public class RoomDetailForm extends ActionForm {

	// --------------------------------------------------------- Instance Variables
	private String id;
	private String doit;
	private String sharingTable;
	private String name;
	private Integer capacity;
	private Integer coordinateX;
	private Integer coordinateY;
	private String type;
	private String patterns;
	private Collection groups;
	private Collection globalFeatures;	
	private Collection departmentFeatures;	
	private List roomPrefs;	
	private List depts;
	private boolean deleteFlag;
	private boolean owner;
	private boolean ignoreTooFar = false;
	private boolean ignoreRoomCheck = false;
	private String control = null;
	private boolean nonUniv;
	private boolean editable = false;

	// --------------------------------------------------------- Methods

	/**
	 * 
	 */
	private static final long serialVersionUID = -542603705961314236L;

	/** 
	 * Method validate
	 * @param mapping
	 * @param request
	 * @return ActionErrors
	 */
	public ActionErrors validate(
		ActionMapping mapping,
		HttpServletRequest request) {

		return null;
	}

	/** 
	 * Method reset
	 * @param mapping
	 * @param request
	 */
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		ignoreTooFar = false;
		ignoreRoomCheck = false;
		editable = false;
		control = null;
	}

	public Integer getCapacity() {
		return capacity;
	}

	public void setCapacity(Integer capacity) {
		this.capacity = capacity;
	}

	public Integer getCoordinateX() {
		return coordinateX;
	}

	public void setCoordinateX(Integer coordinateX) {
		this.coordinateX = coordinateX;
	}

	public Integer getCoordinateY() {
		return coordinateY;
	}

	public void setCoordinateY(Integer coordinateY) {
		this.coordinateY = coordinateY;
	}

	public Collection getGlobalFeatures() {
		return globalFeatures;
	}

	public void setGlobalFeatures(Collection globalFeatures) {
		this.globalFeatures = globalFeatures;
	}

	public Collection getDepartmentFeatures() {
		return departmentFeatures;
	}

	public void setDepartmentFeatures(Collection departmentFeatures) {
		this.departmentFeatures = departmentFeatures;
	}

	public Collection getGroups() {
		return groups;
	}

	public void setGroups(Collection groups) {
		this.groups = groups;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPatterns() {
		return patterns;
	}

	public void setPatterns(String patterns) {
		this.patterns = patterns;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isDeleteFlag() {
		return deleteFlag;
	}

	public void setDeleteFlag(boolean deleteFlag) {
		this.deleteFlag = deleteFlag;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSharingTable() {
		return sharingTable;
	}

	public void setSharingTable(String sharingTable) {
		this.sharingTable = sharingTable;
	}

	public String getDoit() {
		return doit;
	}

	public void setDoit(String doit) {
		this.doit = doit;
	}

	public boolean isOwner() {
		return owner;
	}

	public void setOwner(boolean owner) {
		this.owner = owner;
	}

	public void setIgnoreTooFar(boolean ignoreTooFar) {
		this.ignoreTooFar = ignoreTooFar;
	}
	
	public boolean getIgnoreTooFar() { return ignoreTooFar; }

	public boolean isIgnoreRoomCheck() {
		return ignoreRoomCheck;
	}

	public void setIgnoreRoomCheck(boolean ignoreRoomCheck) {
		this.ignoreRoomCheck = ignoreRoomCheck;
	}

	public String getControl() {
		return control;
	}

	public void setControl(String control) {
		this.control = control;
	}

	public List getRoomPrefs() {
		return roomPrefs;
	}

	public void setRoomPrefs(List roomPrefs) {
		this.roomPrefs = roomPrefs;
	}

	public List getDepts() {
		return depts;
	}

	public void setDepts(List depts) {
		this.depts = depts;
	}

	public boolean isNonUniv() {
		return nonUniv;
	}

	public void setNonUniv(boolean nonUniv) {
		this.nonUniv = nonUniv;
	}
	
	public boolean isEditable() { return editable; }
	public void setEditable(boolean editable) { this.editable = editable; }
}

