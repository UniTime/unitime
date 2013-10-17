/*
 * UniTime 3.3 - 3.5 (University Timetabling Application)
 * Copyright (C) 2011 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.localization.messages;

/**
 * @author Tomas Muller
 */
public interface ConstantsMessages extends Messages {
	
	@DefaultMessage("Monday")
	String monday();

	@DefaultMessage("Tuesday")
	String tuesday();

	@DefaultMessage("Wednesday")
	String wednesday();

	@DefaultMessage("Thursday")
	String thursday();

	@DefaultMessage("Friday")
	String friday();

	@DefaultMessage("Saturday")
	String saturday();

	@DefaultMessage("Sunday")
	String sunday();

	@DefaultMessage("Mon")
	String mon();

	@DefaultMessage("Tue")
	String tue();

	@DefaultMessage("Wed")
	String wed();

	@DefaultMessage("Thu")
	String thu();

	@DefaultMessage("Fri")
	String fri();

	@DefaultMessage("Sat")
	String sat();

	@DefaultMessage("Sun")
	String sun();
	
	@DefaultMessage("Select...")
	String select();

	@DefaultMessage("All")
	String all();

}