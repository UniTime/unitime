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
package org.unitime.timetable.onlinesectioning;

import org.cpsolver.ifs.util.JProf;

/**
 * @author Tomas Muller
 */
public class CacheElement<T> {
	private T iElement;
	private long iCreated;
	
	public CacheElement(T element) {
		iElement = element;
		iCreated = JProf.currentTimeMillis();
	}
	
	public T element() { return iElement; }
	
	public long created() { return iCreated; }
	
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o instanceof CacheElement && ((CacheElement<T>)o).element().equals(element())) return true;
		return o.equals(element());
	}
	
	public int hashCode() {
		return element().hashCode();
	}

}
