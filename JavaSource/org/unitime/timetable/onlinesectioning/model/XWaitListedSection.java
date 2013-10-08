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

import java.util.Date;

import org.unitime.timetable.model.ClassWaitList;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;

public class XWaitListedSection extends XSection {
	private static final long serialVersionUID = 1L;
	private Long iWaitListId = null;
	private Date iTimeStamp = null;
	private ClassWaitList.Type iType = null;
	
	public XWaitListedSection(ClassWaitList clw, OnlineSectioningHelper helper) {
		super(clw.getClazz(), helper);
		iWaitListId = clw.getUniqueId();
		iTimeStamp = clw.getTimestamp();
		iType = ClassWaitList.Type.values()[clw.getType()];
	}
	
	public Long getWaitListId() { return iWaitListId; }
	public ClassWaitList.Type getType() { return iType; }
	public Date getTimeStamp() { return iTimeStamp; }
}
