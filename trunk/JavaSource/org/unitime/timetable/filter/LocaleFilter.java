package org.unitime.timetable.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;

public class LocaleFilter implements Filter {
	private boolean iUseBrowserSettings;
	
	@Override
	public void init(FilterConfig fc) throws ServletException {
		iUseBrowserSettings = "true".equals(fc.getInitParameter("use-browser-settings"));
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
		if (req instanceof HttpServletRequest) {
			HttpServletRequest request = (HttpServletRequest)req;
			String locale = null;
			
			// Try HTTP header, Accept-Language field
			if (iUseBrowserSettings)
				locale = request.getHeader("Accept-Language");
			
			// Try locale parameter (use http session to store)
			if (req.getParameter("locale") != null) {
				locale = req.getParameter("locale");
				request.getSession().setAttribute("unitime.locale", locale);
			} else if (request.getSession().getAttribute("unitime.locale") != null) {
				locale = (String)request.getSession().getAttribute("unitime.locale");
			}
			
			// Fall back to unitime.locale
			if (locale == null)
				locale = ApplicationProperties.getProperty("unitime.locale", "en");
			
			Localization.setLocale(locale);
		}
		chain.doFilter(req, resp);
	}

	@Override
	public void destroy() {
	}

}