/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.model.base;

import java.io.Serializable;

import org.unitime.timetable.model.ItypeDesc;

public abstract class BaseItypeDesc implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer iItype;
	private String iAbbv;
	private String iDesc;
	private String iSis_ref;
	private Integer iBasic;
	private Boolean iOrganized;

	private ItypeDesc iParent;

	public static String PROP_ITYPE = "itype";
	public static String PROP_ABBV = "abbv";
	public static String PROP_DESCRIPTION = "desc";
	public static String PROP_SIS_REF = "sis_ref";
	public static String PROP_BASIC = "basic";
	public static String PROP_ORGANIZED = "organized";

	public BaseItypeDesc() {
		initialize();
	}

	public BaseItypeDesc(Integer itype) {
		setItype(itype);
		initialize();
	}

	protected void initialize() {}

	public Integer getItype() { return iItype; }
	public void setItype(Integer itype) { iItype = itype; }

	public String getAbbv() { return iAbbv; }
	public void setAbbv(String abbv) { iAbbv = abbv; }

	public String getDesc() { return iDesc; }
	public void setDesc(String desc) { iDesc = desc; }

	public String getSis_ref() { return iSis_ref; }
	public void setSis_ref(String sis_ref) { iSis_ref = sis_ref; }

	public Integer getBasic() { return iBasic; }
	public void setBasic(Integer basic) { iBasic = basic; }

	public Boolean isOrganized() { return iOrganized; }
	public Boolean getOrganized() { return iOrganized; }
	public void setOrganized(Boolean organized) { iOrganized = organized; }

	public ItypeDesc getParent() { return iParent; }
	public void setParent(ItypeDesc parent) { iParent = parent; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof ItypeDesc)) return false;
		if (getItype() == null || ((ItypeDesc)o).getItype() == null) return false;
		return getItype().equals(((ItypeDesc)o).getItype());
	}

	public int hashCode() {
		if (getItype() == null) return super.hashCode();
		return getItype().hashCode();
	}

	public String toString() {
		return "ItypeDesc["+getItype()+"]";
	}

	public String toDebugString() {
		return "ItypeDesc[" +
			"\n	Abbv: " + getAbbv() +
			"\n	Basic: " + getBasic() +
			"\n	Desc: " + getDesc() +
			"\n	Itype: " + getItype() +
			"\n	Organized: " + getOrganized() +
			"\n	Parent: " + getParent() +
			"\n	Sis_ref: " + getSis_ref() +
			"]";
	}
}
