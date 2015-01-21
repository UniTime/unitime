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

/**
 * @author Tomas Muller
 */
public class SimpleQualifier extends AbstractQualifier {
	private static final long serialVersionUID = 1L;

	
	public SimpleQualifier(Qualifiable qualifiable) {
		super(qualifiable.getQualifierType(), qualifiable.getQualifierId(), qualifiable.getQualifierReference(), qualifiable.getQualifierLabel());
	}
	
	
	public SimpleQualifier(String type, Serializable id, String reference, String label) {
		super(type, id, reference, label);
	}


	public SimpleQualifier(String type, Serializable idOrReference) {
		super(type, (idOrReference instanceof String ? null : idOrReference), (idOrReference instanceof String ? (String)idOrReference : null), null);
	}
}
