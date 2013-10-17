/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.command.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Tomas Muller
 */
public class GwtRpcException extends RuntimeException implements IsSerializable {
	private static final long serialVersionUID = 1L;
	private Throwable iCause = null;

	public GwtRpcException() {
		super();
	}
	
	public GwtRpcException(String message) {
		super(message);
	}
	
	public GwtRpcException(String message, Throwable cause) {
		super(message, cause);
		if (cause instanceof IsSerializable)
			iCause = cause;
	}
	
	public boolean hasCause() {
		return iCause != null || super.getCause() != null;
	}
	
	@Override
	public Throwable getCause() {
		return (iCause != null ? iCause : super.getCause());
	}
}
