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
