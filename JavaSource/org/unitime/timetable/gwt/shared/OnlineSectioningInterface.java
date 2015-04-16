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
		private String iCheckboxMessage = null;
		private Long iSessionId = null, iStudentId = null;
		
		public static enum EligibilityFlag implements IsSerializable {
			IS_ADMIN, IS_ADVISOR, IS_GUEST,
			CAN_USE_ASSISTANT,
			CAN_ENROLL,
			PIN_REQUIRED,
			CAN_WAITLIST,
			RECHECK_AFTER_ENROLLMENT,
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
		
		public void setCheckboxMessage(String message) { iCheckboxMessage = message; }
		public boolean hasCheckboxMessage() { return iCheckboxMessage != null && !iCheckboxMessage.isEmpty(); }
		public String getCheckboxMessage() { return iCheckboxMessage; }

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
		private boolean iRequestUpdate = false;
		
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
		
		public void setRequestUpdate(boolean requestUpdate) { iRequestUpdate = requestUpdate; }
		public boolean isRequestUpdate() { return iRequestUpdate; }
		
		public boolean isCanSelectStudent() {
			return iEmail || iMassCancel || iChangeStatus || iRequestUpdate;
		}
	}

}
