/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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

public interface InstructionalOfferingListFormInterface {
	public Boolean getDivSec();
	public Boolean getDemand();
	public Boolean getProjectedDemand();
	public Boolean getMinPerWk();
	public Boolean getLimit();
	public Boolean getRoomLimit();
	public Boolean getManager();
	public Boolean getDatePattern();
	public Boolean getTimePattern();
	public Boolean getPreferences();
	public Boolean getInstructor();
	public Boolean getTimetable();
	public Boolean getCredit();
	public Boolean getSubpartCredit();
	public Boolean getSchedulePrintNote();
	public Boolean getNote();
	public Boolean getConsent();
	public Boolean getTitle();
	public Boolean getExams();
	public String getSortBy();
}
