/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2014, UniTime LLC, and individual contributors
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
