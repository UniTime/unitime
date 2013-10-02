/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.onlinesectioning.model;

import java.util.BitSet;

import net.sf.cpsolver.studentsct.model.FreeTimeRequest;

import org.unitime.timetable.model.CourseDemand;

public class XFreeTimeRequest extends XRequest {
	private static final long serialVersionUID = 1L;
	private XTime iTime;
	
	public XFreeTimeRequest() {}
	
	public XFreeTimeRequest(CourseDemand demand, BitSet freeTimePattern) {
		super(demand);
		iTime = new XTime(demand.getFreeTime(), freeTimePattern);
	}
	
	public XFreeTimeRequest(FreeTimeRequest request) {
		super(request);
		iTime = new XTime(request.getTime());
	}
	
	public XTime getTime() { return iTime; }

	@Override
	public String toString() {
		return super.toString() + " Free " + getTime();
	}
}