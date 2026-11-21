package org.unitime.timetable.spring.security;

import java.util.Arrays;
import java.util.HashSet;

import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

@Service("unitimeCsrfMatcher")
public class UniTimeCsrfRequestMatcher implements RequestMatcher {
	
	private final HashSet<String> allowedMethods = new HashSet<>(Arrays.asList("GET", "HEAD", "TRACE", "OPTIONS"));

	@Override
	public boolean matches(HttpServletRequest request) {
		if (request.getRequestURI().endsWith(".action") && !allowedMethods.contains(request.getMethod()))
			return true;
		if (request.getRequestURI().endsWith("/login") && !allowedMethods.contains(request.getMethod()))
			return true;
		return false;
	}
}
