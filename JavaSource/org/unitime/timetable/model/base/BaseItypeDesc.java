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

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

import java.io.Serializable;

import org.unitime.timetable.model.ItypeDesc;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseItypeDesc implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer iItype;
	private String iAbbv;
	private String iDesc;
	private String iSis_ref;
	private Boolean iBasic;
	private Boolean iOrganized;

	private ItypeDesc iParent;

	public BaseItypeDesc() {
	}

	public BaseItypeDesc(Integer itype) {
		setItype(itype);
	}


	@Id
	@Column(name="itype")
	public Integer getItype() { return iItype; }
	public void setItype(Integer itype) { iItype = itype; }

	@Column(name = "abbv", nullable = true, length = 7)
	public String getAbbv() { return iAbbv; }
	public void setAbbv(String abbv) { iAbbv = abbv; }

	@Column(name = "description", nullable = true, length = 50)
	public String getDesc() { return iDesc; }
	public void setDesc(String desc) { iDesc = desc; }

	@Column(name = "sis_ref", nullable = true, length = 20)
	public String getSis_ref() { return iSis_ref; }
	public void setSis_ref(String sis_ref) { iSis_ref = sis_ref; }

	@Column(name = "basic", nullable = true)
	public Boolean isBasic() { return iBasic; }
	@Transient
	public Boolean getBasic() { return iBasic; }
	public void setBasic(Boolean basic) { iBasic = basic; }

	@Column(name = "organized", nullable = false)
	public Boolean isOrganized() { return iOrganized; }
	@Transient
	public Boolean getOrganized() { return iOrganized; }
	public void setOrganized(Boolean organized) { iOrganized = organized; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "parent", nullable = true)
	public ItypeDesc getParent() { return iParent; }
	public void setParent(ItypeDesc parent) { iParent = parent; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ItypeDesc)) return false;
		if (getItype() == null || ((ItypeDesc)o).getItype() == null) return false;
		return getItype().equals(((ItypeDesc)o).getItype());
	}

	@Override
	public int hashCode() {
		if (getItype() == null) return super.hashCode();
		return getItype().hashCode();
	}

	@Override
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
