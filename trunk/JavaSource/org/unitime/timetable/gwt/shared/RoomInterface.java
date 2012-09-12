/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unitime.timetable.gwt.command.client.GwtRpcImplementedBy;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RoomInterface implements IsSerializable {

	public static class RoomSharingDisplayMode implements IsSerializable {
		private String iName;
		private int iFirstDay = 0, iLastDay = 4, iFirstSlot = 90, iLastSlot = 210, iStep = 6;
		
		public RoomSharingDisplayMode() {}
		
		public RoomSharingDisplayMode(String m) {
			String[] model = m.split("\\|");
			iName = model[0];
			iFirstDay = Integer.parseInt(model[1]);
			iLastDay = Integer.parseInt(model[2]);
			iFirstSlot = Integer.parseInt(model[3]);
			iLastSlot = Integer.parseInt(model[4]);
			iStep = Integer.parseInt(model[5]);
		}
		
		public int getFirstDay() { return iFirstDay; }
		public void setFirstDay(int firstDay) { iFirstDay = firstDay; }
		
		public int getLastDay() { return iLastDay; }
		public void setLastDay(int lastDay) { iLastDay = lastDay; }
		
		public int getFirstSlot() { return iFirstSlot; }
		public void setFirstSlot(int firstSlot) { iFirstSlot = firstSlot; }

		public int getLastSlot() { return iLastSlot; }
		public void setLastSlot(int lastSlot) { iLastSlot = lastSlot; }
		
		public int getStep() { return iStep; }
		public void setStep(int step) { iStep = step; }
		
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }

		public String toString() { 
			return getFirstDay() + "|" + getLastDay() + "|" + getFirstSlot() + "|" + getLastSlot() + "|" + getStep();
		}
	}
	
	public static class RoomSharingOption implements IsSerializable {
		private String iCode, iName;
		private String iColor;
		private Long iId;
		private boolean iEditable;
		
		public RoomSharingOption() {}
		public RoomSharingOption(Long id, String color, String code, String name, boolean editable) {
			iId = id; iColor = color; iCode = code; iName = name; iEditable = editable;
		}
		
		public String getColor() { return iColor; }
		public void setColor(String color) { iColor = color; }
		
		public String getCode() { return iCode; }
		public void setCode(String code) { iCode = code; }

		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		
		public void setEditable(boolean editable) { iEditable = editable; }
		public boolean isEditable() { return iEditable; }

		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof RoomSharingOption)) return false;
			return getId().equals(((RoomSharingOption)o).getId());
		}
		
		@Override
		public int hashCode() {
			return getId().hashCode();
		}
	}
	
	public static class RoomSharingModel implements IsSerializable, GwtRpcResponse {
		private Long iId;
		private String iName;
		private Long iDefaultOption;
		private List<RoomSharingDisplayMode> iModes;
		private List<RoomSharingOption> iOptions;
		private List<RoomSharingOption> iOtherOptions;
		private Map<Integer, Map<Integer, Long>> iModel;
		private Map<Integer, Map<Integer, Boolean>> iEditable;
		private boolean iDefaultEditable = true;
		
		public RoomSharingModel() {}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id;}
		
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		
		public void addMode(RoomSharingDisplayMode mode) {
			if (iModes == null) iModes = new ArrayList<RoomSharingDisplayMode>();
			iModes.add(mode);
		}
		
		public List<RoomSharingDisplayMode> getModes() {
			return iModes;
		}
		
		public void setDefaultOption(RoomSharingOption option) {
			iDefaultOption = (option == null ? null : option.getId());
		}

		public RoomSharingOption getDefaultOption() {
			return getOption(iDefaultOption);
		}
		
		public RoomSharingOption getOption(Long id) {
			if (id == null) id = iDefaultOption;
			if (iOptions == null || id == null) return null;
			for (RoomSharingOption option: iOptions)
				if (option.getId().equals(id)) return option;
			return (!id.equals(iDefaultOption) ? getOption(iDefaultOption) : null);
		}
		
		public void addOption(RoomSharingOption option) {
			if (iOptions == null) iOptions = new ArrayList<RoomSharingOption>();
			iOptions.add(option);
		}
		
		public void addOther(RoomSharingOption option) {
			if (iOtherOptions == null) iOtherOptions = new ArrayList<RoomSharingOption>();
			iOtherOptions.add(option);
		}
		
		public List<RoomSharingOption> getAdditionalOptions() {
			List<RoomSharingOption> other = new ArrayList<RoomSharingOption>();
			if (iOtherOptions == null || iOtherOptions.isEmpty()) return other;
			for (RoomSharingOption option: iOtherOptions)
				if (!iOptions.contains(option)) other.add(option);
			return other;
		}

		public List<RoomSharingOption> getOptions() { return iOptions; }
		
		public boolean isEditable() {
			if (iOptions == null) return false;
			for (RoomSharingOption option: iOptions)
				if (option.isEditable()) {
					if (iDefaultEditable) return true;
					if (iEditable == null || iEditable.isEmpty()) return false;
					for (Map<Integer, Boolean> slot2ed: iEditable.values())
						for (Boolean ed: slot2ed.values())
							if (ed) return true;
				}
			return false;
		}

		public RoomSharingOption getOption(int day, int slot) {
			if (iModel == null) return getOption(iDefaultOption);
			Map<Integer, Long> slot2id = iModel.get(day);
			return getOption(slot2id == null ? null : slot2id.get(slot)); 
		}
		
		public void setOption(int day, int slot, Long optionId) {
			if (iModel == null) iModel = new HashMap<Integer, Map<Integer,Long>>();
			Map<Integer, Long> slot2id = iModel.get(day);
			if (slot2id == null) {
				slot2id = new HashMap<Integer, Long>();
				iModel.put(day, slot2id);
			}
			if (optionId == null)
				slot2id.remove(slot);
			else
				slot2id.put(slot, optionId);
		}

		public void setOption(int day, int slot, RoomSharingOption option) {
			setOption(day, slot, option == null ? null : option.getId());
		}
		
		public boolean isEditable(int day, int slot) {
			if (iEditable == null) return iDefaultEditable;
			Map<Integer, Boolean> slot2ed = iEditable.get(day);
			if (slot2ed == null) return iDefaultEditable;
			Boolean ed = slot2ed.get(slot);
			return (ed == null ? iDefaultEditable : ed);
		}

		public void setEditable(int day, int slot, boolean editable) {
			if (iEditable == null) iEditable = new HashMap<Integer, Map<Integer,Boolean>>();
			Map<Integer, Boolean> slot2ed = iEditable.get(day);
			if (slot2ed == null) {
				slot2ed = new HashMap<Integer, Boolean>();
				iEditable.put(day, slot2ed);
			}
			slot2ed.put(slot, editable);
		}
		
		public void setDefaultEditable(boolean editable) { iDefaultEditable = editable; }
		
	}
	
	@GwtRpcImplementedBy("org.unitime.timetable.server.rooms.RoomSharingBackend")
	public static class RoomSharingRequest implements GwtRpcRequest<RoomSharingModel> {
		public static enum Operation implements IsSerializable {
			LOAD,
			SAVE
		}
		private Operation iOperation;
		private Long iLocationId;
		private RoomSharingModel iModel;
		
		public RoomSharingRequest() {}
		
		public Long getLocationId() { return iLocationId; }
		public void setLocationId(Long locationId) { iLocationId = locationId; }
		
		public RoomSharingModel getModel() { return iModel; }
		public void setModel(RoomSharingModel model) { iModel = model; }

		public Operation getOperation() { return iOperation; }
		public void setOperation(Operation operation) { iOperation = operation; }
		
		public String toString() { return getOperation().name() + "[" + getLocationId() + "]"; }
		
		public static RoomSharingRequest load(Long locationId) {
			RoomSharingRequest request = new RoomSharingRequest();
			request.setOperation(Operation.LOAD);
			request.setLocationId(locationId);
			return request;
		}
		
		public static RoomSharingRequest save(Long locationId, RoomSharingModel model) {
			RoomSharingRequest request = new RoomSharingRequest();
			request.setOperation(Operation.SAVE);
			request.setLocationId(locationId);
			request.setModel(model);
			return request;
		}
	}

}
