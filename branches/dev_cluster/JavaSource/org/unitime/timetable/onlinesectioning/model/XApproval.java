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

import java.io.Serializable;
import java.util.Date;

public class XApproval implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String iExternalId = null;
	private Date iTimeStamp = null;
	private String iName = null;
	
	public XApproval(String externalId, Date timeStamp, String name) {
		iExternalId = externalId;
		iTimeStamp = timeStamp;
		iName = name;
	}
	
	public XApproval(String[] approval) {
		iTimeStamp = new Date(Long.valueOf(approval[0]));
		iExternalId = approval[1];
		iName = approval[2];
	}
	
	public String getExternalId() { return iExternalId; }
	
	public Date getTimeStamp() { return iTimeStamp; }
	
	public String getName() { return iName; }

}
