package org.unitime.timetable.spring.security;

import java.io.Serializable;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("unitimePermissionEvaluatorNoException")
public class UniTimePermissionEvaluatorNoException extends UniTimePermissionEvaluator {
	
	@Override
	public boolean hasPermission(Authentication authentication, Object domainObject, Object permission) {
		try {
			return super.hasPermission(authentication, domainObject, permission);
		} catch (Exception e) {
			return false;
		}
	}
	
	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
		try {
			return super.hasPermission(authentication, targetId, targetType, permission);
		} catch (Exception e) {
			return false;
		}
	}

}
