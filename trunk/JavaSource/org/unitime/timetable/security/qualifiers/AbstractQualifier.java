/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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
package org.unitime.timetable.security.qualifiers;

import java.io.Serializable;

import org.unitime.timetable.security.Qualifiable;
import org.unitime.timetable.security.UserQualifier;

public abstract class AbstractQualifier implements UserQualifier {
	private static final long serialVersionUID = 1L;
	private Serializable iId;
	private String iType, iReference, iLabel;
	
	public AbstractQualifier(String type, Serializable id, String reference, String label) {
		iType = type; iId = id; iReference = reference; iLabel = label;
	}
	
	@Override
	public String getQualifierType() { return iType; }

	@Override
	public Serializable getQualifierId() { return iId; }

	@Override
	public String getQualifierReference() { return iReference; }

	@Override
	public String getQualifierLabel() { return iLabel == null ? iReference : iLabel; }
	
	@Override
	public String toString() { return getQualifierType() + ":" + (getQualifierReference() == null ? getQualifierId().toString() : getQualifierReference()); }
	
	@Override
	public int hashCode() { return getQualifierId() == null ? getQualifierReference().hashCode() : getQualifierId().hashCode(); }
	
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o instanceof Qualifiable) {
			Qualifiable q = (Qualifiable)o;
			return notNullAndEqual(getQualifierType(), q.getQualifierType()) &&
					(notNullAndEqual(getQualifierId(), q.getQualifierId()) || notNullAndEqual(getQualifierReference(), q.getQualifierReference()));
		}
		
		return false;
	}
	
	private boolean notNullAndEqual(Object o1, Object o2) {
		return o1 == null ? false : o2 == null ? false : o1.equals(o2);
	}
}
