/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.model.base;

import java.io.Serializable;

import org.unitime.timetable.model.ItypeDesc;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseItypeDesc implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer iItype;
	private String iAbbv;
	private String iDesc;
	private String iSis_ref;
	private Boolean iBasic;
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

	public Boolean isBasic() { return iBasic; }
	public Boolean getBasic() { return iBasic; }
	public void setBasic(Boolean basic) { iBasic = basic; }

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
