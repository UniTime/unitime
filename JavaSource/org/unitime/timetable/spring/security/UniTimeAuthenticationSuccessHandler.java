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
