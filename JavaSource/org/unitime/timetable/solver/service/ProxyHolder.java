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
package org.unitime.timetable.solver.service;

import java.io.Serializable;

public class ProxyHolder<U extends Serializable, T> implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private U iId = null;
	private transient T iProxy = null;
	
	public ProxyHolder(U id, T proxy) {
		iId = id; iProxy = proxy;
	}
	
	public U getId() { return iId; }
	
	public T getProxy() { return iProxy; }
	
	public boolean isValid() { return iProxy != null; }
	
	public boolean isValid(U id) { return iProxy != null && getId().equals(id); }
	
	@Override
	public String toString() {
		return "ProxyHolder{id = " + getId() + ", valid = " + isValid() + (iProxy != null ? ", type = " + iProxy.getClass().getSimpleName() : "") + "}";
	}
}
