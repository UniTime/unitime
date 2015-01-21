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
package org.unitime.timetable.authenticate.jaas;

import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.spi.LoginModule;

/**
 * @author Tomas Muller
 */
public abstract class AuthenticateModule implements LoginModule {
	private Subject subject = null;
	private CallbackHandler callbackHandler = null;
	private Map sharedState;
	private Map options;

	private boolean authSucceeded;
	private boolean commitSucceeded;

	private String user;

	public abstract boolean doAuthenticate(HashMap userProps) throws Exception;

	public void initialize(Subject subject, CallbackHandler callbackHandler,
			Map sharedState, Map options) {
		setSubject(subject);
		setCallbackHandler(callbackHandler);
		setSharedState(sharedState);
		setOptions(options);

		setUser(null);
		setAuthSucceeded(false);
		setCommitSucceeded(false);
	}

	protected void reset() {
		setUser(null);
		setAuthSucceeded(false);
		setCommitSucceeded(false);
	}

	public boolean isAuthSucceeded() {
		return authSucceeded;
	}

	public void setAuthSucceeded(boolean authSucceeded) {
		this.authSucceeded = authSucceeded;
	}

	public CallbackHandler getCallbackHandler() {
		return callbackHandler;
	}

	public void setCallbackHandler(CallbackHandler callbackHandler) {
		this.callbackHandler = callbackHandler;
	}

	public boolean isCommitSucceeded() {
		return commitSucceeded;
	}

	public void setCommitSucceeded(boolean commitSucceeded) {
		this.commitSucceeded = commitSucceeded;
	}

	public Map getOptions() {
		return options;
	}

	public void setOptions(Map options) {
		this.options = options;
	}

	public Map getSharedState() {
		return sharedState;
	}

	public void setSharedState(Map sharedState) {
		this.sharedState = sharedState;
	}

	public Subject getSubject() {
		return subject;
	}

	public void setSubject(Subject subject) {
		this.subject = subject;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}
}