/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
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
*/
package org.unitime.timetable.form;

/**
 * @author Tomas Muller
 */
public interface ClassListFormInterface extends InstructionalOfferingListFormInterface{
	public String[] getSubjectAreaIds();
	public String getCourseNbr();
	
	public String getSortBy();
	public String getFilterManager();
	public String getFilterAssignedRoom();
	public String getFilterInstructor();
	public String getFilterIType();
	public int getFilterDayCode();
	public int getFilterStartSlot();
	public int getFilterLength();
	public boolean getSortByKeepSubparts();
	public boolean getShowCrossListedClasses();

	public boolean isReturnAllControlClassesForSubjects();
	public void setSortBy(String sortBy);
	public void setFilterAssignedRoom(String filterAssignedRoom);
	public void setFilterManager(String filterManager);
	public void setFilterIType(String filterIType);
	public void setFilterDayCode(int filterDayCode);
	public void setFilterStartSlot(int filterStartSlot);
	public void setFilterLength(int filterLength);
	public void setSortByKeepSubparts(boolean sortByKeepSubparts);
	public void setShowCrossListedClasses(boolean showCrossListedClasses);
}
