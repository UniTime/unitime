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
package org.unitime.timetable.gwt.shared;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Tomas Muller
 */
public class OnlineSectioningInterface implements IsSerializable, Serializable {
	private static final long serialVersionUID = 1L;
	
	public static class EligibilityCheck implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private int iFlags = 0;
		private String iMessage = null;
		private Long iSessionId = null, iStudentId = null;
		
		public static enum EligibilityFlag implements IsSerializable {
			IS_ADMIN, IS_ADVISOR,
			CAN_USE_ASSISTANT,
			CAN_ENROLL,
			PIN_REQUIRED,
			CAN_WAITLIST,
			
			;
			
			public int flag() { return 1 << ordinal(); }
			
		}
		
		public EligibilityCheck() {}
		
		public EligibilityCheck(String message, EligibilityFlag... flags) {
			iMessage = message;
			for (EligibilityFlag flag: flags)
				setFlag(flag, true);
		}
		
		public EligibilityCheck(EligibilityFlag... flags) {
			for (EligibilityFlag flag: flags)
				setFlag(flag, true);
		}
		
		public void setFlag(EligibilityFlag flag, boolean set) {
			if (set && !hasFlag(flag))
				iFlags += flag.flag();
			if (!set && hasFlag(flag))
				iFlags -= flag.flag();
		}
		
		public boolean hasFlag(EligibilityFlag flag) {
			return (iFlags & flag.flag()) != 0;
		}
		
		public void setMessage(String message) { iMessage = message; }
		public boolean hasMessage() { return iMessage != null && !iMessage.isEmpty(); }
		public String getMessage() { return iMessage; }
		
		public void setSessionId(Long sessionId) { iSessionId = sessionId; }
		public Long getSessionId() { return iSessionId; }
		public void setStudentId(Long studentId) { iStudentId = studentId; }
		public Long getStudentId() { return iStudentId; }
	}
	
	public static class SectioningProperties implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private Long iSessionId = null;
		private boolean iAdmin = false, iAdvisor = false;
		private boolean iEmail = false, iMassCancel = false, iChangeStatus = false;
		
		public SectioningProperties() {
		}
		
		public void setSessionId(Long sessionId) { iSessionId = sessionId; }
		public Long getSessionId() { return iSessionId; }
		
		public boolean isAdmin() { return iAdmin; }
		public boolean isAdvisor() { return iAdvisor; }
		public boolean isAdminOrAdvisor() { return iAdmin || iAdvisor; }
		
		public void setAdmin(boolean admin) { iAdmin = admin; }
		public void setAdvisor(boolean advisor) { iAdvisor = advisor; }
		
		public void setEmail(boolean email) { iEmail = email; }
		public boolean isEmail() { return iEmail; }
		
		public void setMassCancel(boolean massCancel) { iMassCancel = massCancel; }
		public boolean isMassCancel() { return iMassCancel; }
		
		public void setChangeStatus(boolean changeStatus) { iChangeStatus = changeStatus; }
		public boolean isChangeStatus() { return iChangeStatus; }
	}

}
