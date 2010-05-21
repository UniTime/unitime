package org.unitime.timetable.model.base;

import java.io.Serializable;


/**
 * This is an object that contains data related to the  table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table=""
 */

public abstract class BaseRoomTypeOption  implements Serializable {

	public static String REF = "RoomTypeOption";
	public static String PROP_STATUS = "status";
	public static String PROP_MESSAGE = "message";


	// constructors
	public BaseRoomTypeOption () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseRoomTypeOption (
		org.unitime.timetable.model.RoomType roomType,
		org.unitime.timetable.model.Session session) {

		this.setRoomType(roomType);
		this.setSession(session);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseRoomTypeOption (
		org.unitime.timetable.model.RoomType roomType,
		org.unitime.timetable.model.Session session,
		java.lang.Integer status,
		java.lang.String message) {

		this.setRoomType(roomType);
		this.setSession(session);
		this.setStatus(status);
		this.setMessage(message);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key

	private org.unitime.timetable.model.RoomType roomType;

	private org.unitime.timetable.model.Session session;

	// fields
	private java.lang.Integer status;
	private java.lang.String message;



	/**
     * @hibernate.property
     *  column=ROOM_TYPE
	 * not-null=true
	 */
	public org.unitime.timetable.model.RoomType getRoomType () {
		return this.roomType;
	}

	/**
	 * Set the value related to the column: ROOM_TYPE
	 * @param roomType the ROOM_TYPE value
	 */
	public void setRoomType (org.unitime.timetable.model.RoomType roomType) {
		this.roomType = roomType;
		this.hashCode = Integer.MIN_VALUE;
	}

	/**
     * @hibernate.property
     *  column=SESSION_ID
	 * not-null=true
	 */
	public org.unitime.timetable.model.Session getSession () {
		return this.session;
	}

	/**
	 * Set the value related to the column: SESSION_ID
	 * @param session the SESSION_ID value
	 */
	public void setSession (org.unitime.timetable.model.Session session) {
		this.session = session;
		this.hashCode = Integer.MIN_VALUE;
	}




	/**
	 * Return the value associated with the column: STATUS
	 */
	public java.lang.Integer getStatus () {
		return status;
	}

	/**
	 * Set the value related to the column: STATUS
	 * @param status the STATUS value
	 */
	public void setStatus (java.lang.Integer status) {
		this.status = status;
	}



	/**
	 * Return the value associated with the column: MESSAGE
	 */
	public java.lang.String getMessage () {
		return message;
	}

	/**
	 * Set the value related to the column: MESSAGE
	 * @param message the MESSAGE value
	 */
	public void setMessage (java.lang.String message) {
		this.message = message;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.RoomTypeOption)) return false;
		else {
			org.unitime.timetable.model.RoomTypeOption roomTypeOption = (org.unitime.timetable.model.RoomTypeOption) obj;
			if (null != this.getRoomType() && null != roomTypeOption.getRoomType()) {
				if (!this.getRoomType().equals(roomTypeOption.getRoomType())) {
					return false;
				}
			}
			else {
				return false;
			}
			if (null != this.getSession() && null != roomTypeOption.getSession()) {
				if (!this.getSession().equals(roomTypeOption.getSession())) {
					return false;
				}
			}
			else {
				return false;
			}
			return true;
		}
	}

	public int hashCode () {
		if (Integer.MIN_VALUE == this.hashCode) {
			StringBuffer sb = new StringBuffer();
			if (null != this.getRoomType()) {
				sb.append(this.getRoomType().hashCode());
				sb.append(":");
			}
			else {
				return super.hashCode();
			}
			if (null != this.getSession()) {
				sb.append(this.getSession().hashCode());
				sb.append(":");
			}
			else {
				return super.hashCode();
			}
			this.hashCode = sb.toString().hashCode();
		}
		return this.hashCode;
	}


	public String toString () {
		return super.toString();
	}


}