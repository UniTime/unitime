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
package org.unitime.timetable.security.qualifiers;

import java.io.Serializable;

import org.unitime.timetable.security.Qualifiable;
import org.unitime.timetable.security.UserQualifier;

/**
 * @author Tomas Muller
 */
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
