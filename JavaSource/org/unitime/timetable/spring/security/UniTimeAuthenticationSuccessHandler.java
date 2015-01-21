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
package org.unitime.timetable.spring.security;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.unitime.timetable.util.LoginManager;

/**
 * @author Tomas Muller
 */
@Service("unitimeAuthenticationSuccessHandler")
public class UniTimeAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
	protected final Log logger = LogFactory.getLog(this.getClass());

    private RequestCache requestCache = new HttpSessionRequestCache();
    private boolean useReferer = false;
	
	public UniTimeAuthenticationSuccessHandler() {
		setAlwaysUseDefaultTargetUrl(false);
		setDefaultTargetUrl("/selectPrimaryRole.do");
		setTargetUrlParameter("target");
	}
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
		LoginManager.loginSuceeded(authentication.getName());
		request.getSession().removeAttribute("SUGGEST_PASSWORD_RESET");
		super.onAuthenticationSuccess(request, response, authentication);
	}
	
	@Override
	protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
		if (isAlwaysUseDefaultTargetUrl())
			return getDefaultTargetUrl();
		
		String targetUrl = null;

		if (getTargetUrlParameter() != null) {
			targetUrl = request.getParameter(getTargetUrlParameter());
		}
		
		SavedRequest savedRequest = requestCache.getRequest(request, response);
		if (savedRequest != null && !StringUtils.hasText(targetUrl)) {
			targetUrl = savedRequest.getRedirectUrl();
		}
		
		if (useReferer && !StringUtils.hasText(targetUrl)) {
			targetUrl = request.getHeader("Referer");
		}
		
		if (StringUtils.hasText(targetUrl)) {
			try {
				request.setAttribute("target", targetUrl);
				return getDefaultTargetUrl() + "?" + getTargetUrlParameter() + "=" + URLEncoder.encode(targetUrl, "UTF-8");
			} catch (Exception e) {}
		}
		
		return getDefaultTargetUrl();
	}
	
	public void setUseReferer(boolean useReferer) {
        this.useReferer = useReferer;
    }
}
