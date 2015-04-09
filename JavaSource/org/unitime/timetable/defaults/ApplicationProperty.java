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
package org.unitime.timetable.defaults;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.Driver;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.dialect.Dialect;
import org.hibernate.id.IdentifierGenerator;
import org.unitime.commons.Email;
import org.unitime.commons.hibernate.util.DatabaseUpdate;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.interfaces.ExternalClassEditAction;
import org.unitime.timetable.interfaces.ExternalClassNameHelperInterface;
import org.unitime.timetable.interfaces.ExternalCourseCrosslistAction;
import org.unitime.timetable.interfaces.ExternalCourseOfferingEditAction;
import org.unitime.timetable.interfaces.ExternalCourseOfferingRemoveAction;
import org.unitime.timetable.interfaces.ExternalCourseOfferingReservationEditAction;
import org.unitime.timetable.interfaces.ExternalInstrOfferingConfigAssignInstructorsAction;
import org.unitime.timetable.interfaces.ExternalInstrOffrConfigChangeAction;
import org.unitime.timetable.interfaces.ExternalInstructionalOfferingAddAction;
import org.unitime.timetable.interfaces.ExternalInstructionalOfferingDeleteAction;
import org.unitime.timetable.interfaces.ExternalInstructionalOfferingInCrosslistAddAction;
import org.unitime.timetable.interfaces.ExternalInstructionalOfferingNotOfferedAction;
import org.unitime.timetable.interfaces.ExternalInstructionalOfferingOfferedAction;
import org.unitime.timetable.interfaces.ExternalLinkLookup;
import org.unitime.timetable.interfaces.ExternalSchedulingSubpartEditAction;
import org.unitime.timetable.interfaces.ExternalSolutionCommitAction;
import org.unitime.timetable.interfaces.ExternalUidLookup;
import org.unitime.timetable.interfaces.ExternalUidTranslation;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.CourseDetailsProvider;
import org.unitime.timetable.onlinesectioning.custom.CourseUrlProvider;
import org.unitime.timetable.onlinesectioning.custom.ExternalTermProvider;
import org.unitime.timetable.onlinesectioning.custom.SectionUrlProvider;
import org.unitime.timetable.onlinesectioning.custom.SectionLimitProvider;
import org.unitime.timetable.onlinesectioning.custom.StudentEnrollmentProvider;
import org.unitime.timetable.spring.ldap.SpringLdapExternalUidLookup;
import org.unitime.timetable.spring.ldap.SpringLdapExternalUidTranslation;

/**
 * @author Tomas Muller
 */
@SuppressWarnings("deprecation")
public enum ApplicationProperty {
	@Type(Class.class)
	@Implements(Dialect.class)
	@Description("Database: dialect (e.g., org.hibernate.dialect.Oracle10gDialect)")
	@ReadOnly
	DatabaseDialect("dialect"),
	
	@Description("Database: connection url")
	@ReadOnly
	ConnectionUrl("connection.url"),
	
	@Description("Database: connection user")
	@ReadOnly
	ConnectionUser("connection.username"),
	
	@Secret
	@Description("Database: connection password")
	@ReadOnly
	ConnectionPassword("connection.password"),
	
	@Type(Class.class)
	@Implements(Driver.class)
	@Description("Database: connection driver class")
	@ReadOnly
	ConnectionDriver("connection.driver_class"),
	
	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Database: connection logging")
	ConnectionLogging("connection.logging"),

	@DefaultValue("timetable")
	@Description("Database: schema (e.g., timetable)")
	@ReadOnly
	DatabaseSchema("default_schema"),

	@Type(Class.class)
	@Implements(IdentifierGenerator.class)
	@Description("Database: unique id generator (e.g., org.hibernate.id.SequenceGenerator)")
	@ReadOnly
	DatabaseUniqueIdGenerator("tmtbl.uniqueid.generator"),

	@Type(Integer.class)
	@Description("Database: schema version (DO NOT EDIT!)")
	@ReadOnly
	DatabaseVersion("tmtbl.db.version"),

	@DefaultValue("dbupdate.xml")
	@Description("Database: update file")
	@ReadOnly
	DatabaseUpdateFile("tmtbl.db.update"),

	@DefaultValue("en")
	@Description("All Pages: default locale (e.g., en for english)")
	Locale("unitime.locale"),

	@Description("Configuration: data folder (defaults to Tomcat/data/unitime)")
	DataDir("unitime.data.dir"),

	@DefaultValue("custom.properties")
	@Description("Configuration: custom application properties file")
	@ReadOnly
	CustomProperties("tmtbl.custom.properties"),

	/**
	 * Use the following property to set the login page when user types '/UniTime'
	 */
	@DefaultValue("login.jsp")
	@Description("Login: page url")
	LoginPage("tmtbl.login_url"),
	
	@DefaultValue("forward")
	@Description("Login: if set to \"forward\" login.do will be forwarded to the login page url, if set to \"redirect\" the page will be redirected instead")
	LoginMethod("tmtbl.login_method"),

	/**
	 * Custom login page header, see http://help35.unitime.org/Customizations for more details. 
	 */
	@Description("Login: custom page header")
	LoginPageHeader("tmtbl.header.external"),

	/**
	 * Custom login page footer, see http://help35.unitime.org/Customizations for more details. 
	 */
	@Description("Login: custom page footer")
	LoginPageFooter("tmtbl.footer.external"),

	/**
	 * A welcome message can be printed on the first page (when the user logs in).
	 */
	@Description("Main Page: welcome message (e.g., Welcome to Woebegon College test suite.)")
	SystemMessage("tmtbl.system_message"),

	@Description("All Pages: a (warning) message can be included at the top of each page")
	GlobalWarningMessage("tmtbl.global.warn"),

	@Description("All Pages: a disclaimer message can be added at the bottom of each page")
	DisclaimerMessage("tmtbl.page.disclaimer"),

	@Description("All Pages: a custom style sheet can be provided too, the changes are applied on top of the existing styles")
	CustomStyleSheet("tmtbl.custom.css"),

	/**
	 * Default application menu style. Values:<ul>
	 * <li>Static On Top ... Horizontal menu bar on the top of the page (takes 100% width of the page, does not move with the page)
	 * <li>Dynamic On Top ... Horizontal menu bar on the top of the page (takes 100% width of the client window, moves with the page as it is scrolled)
	 * <li>Tree On Side ... Tree on the left side of the page content (moves with the page, resembles the UniTime 3.1 menu the most)
	 * <li>Stack On Side ... Tree on the left side of the page content, but the first level of the tree is a StackPanel (only one top level item can be opened at a time)
	 * </ul>
	 * If manager setting for this property is created, each user (timetable manager) can change his/her menu style.
	 * See http://help35.unitime.org/Customizations for more details.
	 */
	@DefaultValue("Dynamic On Top")
	@Values({"Dynamic On Top", "Static On Top", "Tree On Side", "Static Tree On Side", "Dynamic Tree On Side", "Stack On Side", "Static Stack On Side", "Dynamic Stack On Side"})
	@Description("All Pages: menu style")
	MenuStyle("unitime.menu.style"),

	/**
	 * Use the following property to configure the number of minutes a user will be locked
	 * out of the system if they exceed the maximum number of failed login attempts.
	 */
	@Type(Integer.class)
	@DefaultValue("15")
	@Description("Login: failed login lock out time in minutes")
	LoginFailedLockout("tmtbl.login.failed.lockout.minutes"),

	/**
	 * Use the following property to configure maximum number of failed login attempts
	 * before the user is locked out of the system for a period of time.
	 */
	@Type(Integer.class)
	@DefaultValue("7")
	@Description("Login: maximal number of failed login attempts")
	LoginMaxFailedAttempts("tmtbl.login.max.failed.attempts"),

	/**
	 * Use the following property to configure the number of milliseconds to delay responding
	 * to a user with an unable to log in error if they have exceeded their maximum number
	 * of failed login attempts by more than 3.
	 */
	@Type(Integer.class)
	@DefaultValue("15000")
	@Description("Login: login page response delay (in milliseconds), after the maximal number of failed logins is reached")
	LoginFailedAttemptDelay("tmtbl.login.failed.delay.milliseconds"),

	/**
	 * Access Level: all | {dept code}(:{dept code})*
	 */
	@Description("Login: application access level (deprecated)")
	@Deprecated
	AccessLevel("tmtbl.access_level"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Application Properties: reload application properties on the fly if modified when deployed to the app server")
	ApplicationPropertiesDynamicReload("tmtbl.properties.dynamic_reload"),

	@Type(Integer.class)
	@DefaultValue("15000")
	@Description("Application Properties: reload interval (in milliseconds) for checking if property files have changed")
	ApplicationPropertiesDynamicReloadInterval("tmtbl.properties.dynamic_reload_interval"),

	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("Login: allow for password reset (when authentication fails)")
	PasswordReset("unitime.password.reset"),

	@DefaultValue("high")
	@Values({"high", "medium", "low"})
	@Description("Main Page: obtrusiveness of the registration popup")
	RegistrationPopupObtrusiveness("unitime.registration.obtrusiveness"),

	@DefaultValue("ThisIs8Secret")
	@Description("Configuration: encoder secret, please change the value in the custom properties!")
	@Secret
	UrlEncoderSecret("unitime.encode.secret"),

	@Description("JAAS authentication modules (deprecated)")
	@Deprecated
	AuthenticationModules("tmtbl.authenticate.modules"),

	/**
	 * LDAP Authentication. See http://help35.unitime.org/LDAP for more details.
	 */
	@Description("LDAP Authentication: ldap url")
	AuthenticationLdapUrl("unitime.authentication.ldap.url"),

	@DefaultValue("uid={0},ou=authenticate")
	@Description("LDAP Authentication: authentication query")
	AuthenticationLdapAuthenticate("unitime.authentication.ldap.user-dn-pattern"),

	@DefaultValue("ou=authorize")
	@Description("LDAP Authentication: authorize query (use to retrieve external user id)")
	AuthenticationLdapAuthorize("unitime.authentication.ldap.group-search-base"),

	@DefaultValue("uid\\={1}")
	@Description("LDAP Authentication: authorization query search filter")
	AuthenitcationLdapFilter("unitime.authentication.ldap.group-search-filter"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("LDAP Authentication: convert user external id to upper case")
	AuthorizationLdapUpCase("unitime.authentication.ldap.group-convert-to-uppercase"),

	/**
	 * Translation between LDAP uid and UniTime's external user id.
	 */
	@DefaultValue("uid={0},ou=identify")
	@Description("LDAP Authentication: user identification query")
	AuthenticationLdapIdentify("unitime.authentication.ldap.identify"),

	@DefaultValue("uid")
	@Description("LDAP Authentication: user external id attribute name")
	AuthenticationLdapIdAttribute("unitime.authentication.ldap.group-role-attribute"),

	@DefaultValue("uid={0},ou=identify")
	@Description("LDAP Authentication: translation of user login to user external id")
	@Replaces({"unitime.authentication.ldap.identify"})
	AuthenticationLdapLogin2UserId("unitime.authentication.ldap.uid2ext"),

	@DefaultValue("%={0},ou=identify")
	@Description("LDAP Authentication: translation of user external id to user login (% is replaced with id attribute)")
	AuthenticationLdapUserId2Login("unitime.authentication.ldap.ext2uid"),

	@DefaultValue("menu.xml")
	@Description("All Pages: menu configuration file")
	MenuFile("unitime.menu"),

	@DefaultValue("menu-custom.xml")
	@Description("All Pages: custom menu configuration file")
	CustomMenuFile("unitime.menu.custom"),

	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("All Pages: enable page help")
	PageHelpEnabled("tmtbl.wiki.help"),

	@DefaultValue("https://sites.google.com/a/unitime.org/help35/")
	@Description("All Pages: page help url")
	PageHelpUrl("tmtbl.wiki.url"),

	@Description("Page %: warning message (yellow stripe on the top of the page)")
	@Parameter("page name")
	PageWarning("tmtbl.page.warn.%"),

	@Description("Page %: info message (blue stripe on the top of the page)")
	@Parameter("page name")
	PageInfo("tmtbl.page.info.%"),

	@Description("Configuration: UniTime URL (e.g., used in email notifications where URL cannot be deducted from the http request)")
	UniTimeUrl("unitime.url"),

	@Type(Integer.class)
	@Description("Configuration: maximum upload file size in bytes")
	MaxUploadSize("unitime.upload.max_size_in_bytes"),

	/**
	 * If the following property is defined and set to true, last used academic session is
	 * automatically selected for an authenticated user (if it is still available for the user).
	 */
	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Login: on login, automatically select the last used academic session")
	KeepLastUsedAcademicSession("tmtbl.keeplastused.session"),

	/**
	 * The following property sets the ellipsoid that is used to compute distances.
	 * Values:<ul>
	 * <li>LEGACY ... Euclidean metric (1 unit equals to 10 meters)
	 * <li>WGS84 ... WGS-84 (GPS, a = 6378137 m, b = 6356752.3142 m)
	 * <li>GRS80 ... GRS-80 (a = 6378137 m, b = 6356752.3141 m)
	 * <li>Airy1830 ... Airy (1830) (a = 6377563.396 m, b = 6356256.909 m)
	 * <li>Intl1924 ... Int'l 1924 (a = 6378388 m, b = 6356911.946 m)
	 * <li>Clarke1880 ... Clarke (1880) (a = 6378249.145 m, b = 6356514.86955 m)
	 * <li>GRS67 ... GRS-67 (a = 6378160 m, b = 6356774.719 m)
	 * </ul>
	 */
	@DefaultValue("WGS84")
	@Values({"LEGACY", "WGS84", "GRS80", "Airy1830", "Intl1924", "Clarke1880", "GRS67"})
	@Description("Configuration: distance matrix ellipsoid")
	DistanceEllipsoid("unitime.distance.ellipsoid"),

	@Type(Double.class)
	@DefaultValue("67.0")
	@Description("Room Filter: travel speed (meters in minute) that is used for nearby locations")
	EventDistanceSpeed("tmtbl.events.distanceSpeed"),

	@Type(Double.class)
	@DefaultValue("670.0")
	@Description("Room Filter: distance limit (in meters) for nearby locations")
	EventNearByDistance("tmtbl.events.nearByDistance"),

	@Type(Integer.class)
	@DefaultValue("0")
	@Description("Rooms: default break time for a room of type % (% is the room type reference)")
	@Parameter("room type")
	RoomDefaultBreakTime("unitime.events.breakTime.%"),

	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("Solver: enable 'local' option in the solver host selection")
	SolverLocalEnabled("tmtbl.solver.local.enabled"),

	/**
	 * Minimum amount of available memory (in megabytes) needed to be able to start additional solver instance.
	 * If there is not enough memory on any of the registered solver servers, the following exception is thrown:
	 * 		"Not enough resources to create a solver instance, please try again later."
	 */
	@Type(Integer.class)
	@DefaultValue("200")
	@Description("Solver: minimal amount of free memory (in MB) for the solver to load")
	SolverMemoryLimit("tmtbl.solver.mem_limit"),

	@Values({"fatal", "error", "warn", "info", "debug", "trace"})
	@Description("Solver: log level for %")
	@Parameter("operation")
	SolverLogLevel("unitime.solver.log.level.%"),

	@DefaultValue("#,##0.00")
	@Description("Rooms: room area decimal format")
	RoomAreaUnitsFormat("unitime.room.area.units.format"),

	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("Class Assignment: when re-assigned change past meetings as well")
	ClassAssignmentChangePastMeetings("tmtbl.classAssign.changePastMeetings"),

	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("Class Assignment: ignore past meetings in conflict checking")
	ClassAssignmentIgnorePastMeetings("tmtbl.classAssign.ignorePastMeetings"),

	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("Class Assignment: show student conflicts")
	ClassAssignmentShowStudentConflicts("tmtbl.classAssign.showStudentConflicts"),

	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("Class Assignment: allow unassignments of conflicting classes")
	ClassAssignmentAllowUnassignments("tmtbl.classAssign.allowUnassignment"),

	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("Class Setup: display external ids")
	ClassSetupShowExternalIds("tmtbl.class_setup.show_display_external_ids"),
	
	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Class Setup: edit external ids")
	ClassSetupEditExternalIds("tmtbl.class_setup.edit_external_ids"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Class Setup: display instructor flags")
	ClassSetupDisplayInstructorFlags("tmtbl.class_setup.show_display_instructor_flags"),

	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("Class Setup: show enabled for student scheduling toggle")
	ClassSetupEnabledForStudentScheduling("tmtbl.class_setup.show_enabled_for_student_scheduling"),

	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("Course Offerings: course number must be unique (within a subject area)")
	CourseOfferingNumberMustBeUnique("tmtbl.courseNumber.unique"),

	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("Course Offerings: automatically upper case course offering numbers")
	CourseOfferingNumberUpperCase("tmtbl.courseNumber.upperCase"),

	@DefaultValue("^[0-9][0-9][0-9]([A-Za-z]){0,1}$")
	@Description("Course Offerings: required matching pattern (e.g., 3 numbers followed by an optional letter)")
	CourseOfferingNumberPattern("tmtbl.courseNumber.pattern"),

	@DefaultValue("Course Number must have 3 numbers followed by an optional letter (e.g. 214, 342X)")
	@Description("Course Offerings: warning message when course number does not match")
	CourseOfferingNumberPatternInfo("tmtbl.courseNumber.patternInfo"),

	@Description("Non Univeristy Locations: required matching pattern for the location name")
	NonUniversityLocationPattern("tmtbl.nonUniversityLocation.pattern"),

	@Description("Non Univeristy Locations: warning message when the location name does not match")
	NonUniversityLocationPatternInfo("tmtbl.nonUniversityLocation.patternInfo"),

	/**
	 * Trim leading zeros from student and staff external ids when loading from XML.
	 * This is useful if the data loads have ids with leading zeros and external lookup systems ignore the leading zeros.
	 * In this case the trim leading zeros should be set to true, by default it is false.
	 */
	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Data Exchange: trim leading zeros from extednal user id")
	DataExchangeTrimLeadingZerosFromExternalIds("tmtbl.data.exchange.trim.externalId"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Data Exchange: export default date pattern in course offering export")
	DataExchangeIncludeDefaultDatePattern("tmtbl.export.defaultDatePattern"),

	@Description("Data Exchange: default import timetabling manager (e.g., if executed from command line)")
	DataExchangeXmlManager("unitime.xml.manager"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Student Enrollment Import: update examination student conflicts for final exams")
	DataExchangeUpdateStudentConflictsFinal("tmtbl.data.import.studentEnrl.finalExam.updateConflicts"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Student Enrollment Import: update examination student conflicts for midterm exams")
	DataExchangeUpdateStudentConflictsMidterm("tmtbl.data.import.studentEnrl.midtermExam.updateConflicts"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Time Patterns: set to true if used time patterns are to be editable during the initial data load (a session status that allows for roll forward)")
	TimePatternEditableDuringInitialDataLoad("tmtbl.time_pattern.initial_data_load.editable"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Buildings Update Data: also update existing rooms")
	BuildingsExternalUpdateExistingRooms("unitime.external.room.update.existing"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Buildings Update Data: when updating existing rooms, also reset global room features")
	BuildingsExternalUpdateExistingRoomFeatures("unitime.external.room.update.existing.features"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Buildings Update Data: when updating existing rooms, also reset room departments")
	BuildingsExternalUpdateExistingRoomDepartments("unitime.external.room.update.existing.departments"),

	@Description("Buildings Update Data: if set, only consider rooms of the given classifications (comma separated list)")
	BuildingsExternalUpdateClassification("unitime.external.room.update.classifications"),

	@Parameter("index")
	@Description("Room Sharing Mode: defines %-th room sharing grid (starting with 1, format is name|first slot|last slot|first day|last day|increment, e.g., Workdays \u00D7 Daytime|0|4|90|222|6 means Monday - Friday, starting at 7:30 am, ending at 6:30 pm, in half-hour increments)")
	RoomSharingMode("unitime.room.sharingMode%"),

	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("Online Student Scheduling: check enrollment deadlines")
	OnlineSchedulingCheckDeadlines("unitime.enrollment.deadline"),

	@DefaultValue("last-first-middle")
	@Description("Online Student Scheduling: student name format")
	OnlineSchedulingStudentNameFormat("unitime.enrollment.student.name"),

	@DefaultValue("initial-last")
	@Description("Online Student Scheduling: instructor name format")
	OnlineSchedulingInstructorNameFormat("unitime.enrollment.instructor.name"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Online Student Scheduling: save course requests during the scheduling assistent mode")
	OnlineSchedulingSaveRequests("unitime.enrollment.requests.save"),

	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("Online Student Scheduling: enable student email confirmations")
	OnlineSchedulingEmailConfirmation("unitime.enrollment.email"),

	@Type(Boolean.class)
	@Description("Online Student Scheduling: allow student to select over-expected sections (even if there is a choise avoiding them)")
	OnlineSchedulingAllowOverExpected("unitime.sectioning.allowOverExpected"),

	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("Online Student Scheduling Log: enable logging (online sectioning log)")
	OnlineSchedulingLoggingEnabled("unitime.sectioning.log"),

	@Type(Integer.class)
	@DefaultValue("5000")
	@Description("Online Student Scheduling Log: limit on the number of records held in memory (before persisted)")
	OnlineSchedulingLogLimit("unitime.sectioning.log.limit"),

	@Description("Online Student Scheduling Log: besides of the database, also log the actions in the given file (if set)")
	OnlineSchedulingLogFile("unitime.sectioning.log.file"),

	@Description("Online Student Scheduling: override for the solver parameter %")
	@Parameter("solver parameter")
	OnlineSchedulingParameter("unitime.sectioning.config.%"),

	/**
	 * Room availability. By default, use the included event management system.
	 * See http://help35.unitime.org/Custom_Room_Availability for more details.
	 */
	@Type(Class.class)
	@Implements(RoomAvailabilityInterface.class)
	@DefaultValue("org.unitime.timetable.util.DefaultRoomAvailabilityService")
	@Description("Room Availability: implementation class (implementing RoomAvailabilityInterface)")
	RoomAvailabilityImplementation("tmtbl.room.availability.class"),

	@Type(Integer.class)
	@DefaultValue("0")
	@Description("Room Availability: extra time in minutes required to be empty before a class starts")
	RoomAvailabilityClassBreakTimeStart("tmtbl.room.availability.class.breakTime.start"),

	@Type(Integer.class)
	@DefaultValue("0")
	@Description("Room Availability: extra time in minutes required to be empty before a class stops")
	RoomAvailabilityClassBreakTimeStop("tmtbl.room.availability.class.breakTime.stop"),

	@DefaultValue("Midterm Examination Event")
	@Description("Room Availability: midterm examination type")
	RoomAvailabilityMidtermExamType("tmtbl.room.availability.eventType.midtermExam"),

	@DefaultValue("Final Examination Event")
	@Description("Room Availability: final examination type")
	RoomAvailabilityFinalExamType("tmtbl.room.availability.eventType.finalExam"),

	@DefaultValue("Class Event")
	@Description("Room Availability: class type")
	RoomAvailabilityClassType("tmtbl.room.availability.eventType.class"),

	@DefaultValue("{ call room_avail_interface.request(?) }")
	@Description("Blob Room Availability Service: request sql")
	BlobRoomAvailabilityRequestSQL("tmtbl.room.availability.request"),

	@DefaultValue("{? = call room_avail_interface.response()}")
	@Description("Blob Room Availability Service: response sql")
	BlobRoomAvailabilityResponseSQL("tmtbl.room.availability.response"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Room Availability: enable instructor unavailabilty; instructor unavailabilty includes special and course-related events that are approved and where the instructor is the main contact or he/she is present in the additional contacts of the event")
	RoomAvailabilityIncludeInstructors("unitime.events.instructorUnavailability"),

	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("Solver: wait for the room availabity to synchronize")
	RoomAvailabilitySolverWaitForSync("tmtbl.room.availability.solver.waitForSync"),

	/**
	 * If you are using UniTime in conjunction with an external system that identifies classes differently than UniTime
	 * you can create a java class that implements the ExternalClassNameHelperInterface interface to create custom
	 * naming for the class that is displayed in places such as the personal exam schedule and personal class
	 * schedule for students.
	 */
	@Type(Class.class)
	@Implements(ExternalClassNameHelperInterface.class)
	@DefaultValue("org.unitime.timetable.util.DefaultExternalClassNameHelper")
	@Description("Classes: class naming helper class (implementing ExternalClassNameHelperInterface)")
	ClassNamingHelper("tmtbl.class.naming.helper"),

	@Description("Classes: text of a blue message on the top of the page")
	@Deprecated
	ClassesMessage("tmtbl.classes.message"),

	@Description("Examinations: text of a blue message on the top of the page")
	@Deprecated
	ExamsMessage("tmtbl.exams.message"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Instructional Offering Detail: make not offered stays on the detail page")
	MakeNotOfferedStaysOnDetail("unitime.offering.makeNotOfferedStaysOnDetail"),

	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("Instructional Offering Config: check limits")
	ConfigEditCheckLimits("unitime.instrOfferingConfig.checkConfigLimit"),

	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("Scheduling Subpart Credit: editation allowed")
	SubpartCreditEditable("tmtbl.subpart.credit.editable"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Instructors: enable external id lookup (lookup provider must be defined)")
	InstructorExternalIdLookup("tmtbl.instructor.external_id.lookup.enabled"),

	/**
	 * Use {@link SpringLdapExternalUidLookup} when LDAP authentication is enabled.
	 * See http://help35.unitime.org/LDAP for more details.
	 */
	@Type(Class.class)
	@Implements(ExternalUidLookup.class)
	@Description("Instructors: external id lookup provider (class ExternalUidLookup)")
	InstructorExternalIdLookupClass("tmtbl.instructor.external_id.lookup.class"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Timetabling Managers: enable external id lookup (lookup provider must be defined)")
	ManagerExternalIdLookup("tmtbl.manager.external_id.lookup.enabled"),

	/**
	 * Use {@link SpringLdapExternalUidLookup} when LDAP authentication is enabled.
	 * See http://help35.unitime.org/LDAP for more details.
	 */
	@Type(Class.class)
	@Implements(ExternalUidLookup.class)
	@Description("Timetabling Managers: external id lookup provider (class ExternalUidLookup)")
	ManagerExternalIdLookupClass("tmtbl.manager.external_id.lookup.class"),

	@Type(Class.class)
	@Implements(ExternalClassEditAction.class)
	@Description("ExternalClassEditAction interface called when a class was edited")
	ExternalActionClassEdit("tmtbl.external.class.edit_action.class"),

	@Type(Class.class)
	@Implements(ExternalInstrOfferingConfigAssignInstructorsAction.class)
	@Description("ExternalInstrOfferingConfigAssignInstructorsAction interface called when instructor assignments are changed")
	ExternalActionInstrOfferingConfigAssignInstructors("tmtbl.external.instr_offr_config.assign_instructors_action.class"),

	@Type(Class.class)
	@Implements(ExternalCourseOfferingEditAction.class)
	@Description("ExternalCourseOfferingEditAction interface called when course offering was edited")
	ExternalActionCourseOfferingEdit("tmtbl.external.course_offering.edit_action.class"),

	@Type(Class.class)
	@Implements(ExternalInstructionalOfferingAddAction.class)
	@Description("ExternalInstructionalOfferingAddAction interface called when an instructional offering was created")
	ExternalActionInstructionalOfferingAdd("tmtbl.external.instr_offr.add_action.class"),

	@Type(Class.class)
	@Implements(ExternalCourseOfferingRemoveAction.class)
	@Description("ExternalCourseOfferingRemoveAction interface called when a course offering is about to be removed")
	ExternalActionCourseOfferingRemove("tmtbl.external.course_offering.remove_action.class"),

	@Type(Class.class)
	@Implements(ExternalInstructionalOfferingInCrosslistAddAction.class)
	@Description("ExternalInstructionalOfferingInCrosslistAddAction interface called when a course offering was removed from a cross-list")
	ExternalActionInstructionalOfferingInCrosslistAdd("tmtbl.external.instr_offr_in_crosslist.add_action.class"),

	@Type(Class.class)
	@Implements(ExternalCourseCrosslistAction.class)
	@Description("ExternalCourseCrosslistAction interface called when a cross-list was edited")
	ExternalActionCourseCrosslist("tmtbl.external.instr_offr.crosslist_action.class"),

	@Type(Class.class)
	@Implements(ExternalInstrOffrConfigChangeAction.class)
	@Description("ExternalInstrOffrConfigChangeAction interface called when an instructional offering configuration was edited")
	ExternalActionInstrOffrConfigChange("tmtbl.external.instr_offr_config.change_action.class"),

	@Type(Class.class)
	@Implements(ExternalInstructionalOfferingDeleteAction.class)
	@Description("ExternalInstructionalOfferingDeleteAction interface called when an instructional offering is about to be deleted.")
	ExternalActionInstructionalOfferingDelete("tmtbl.external.instr_offr.delete_action.class"),

	@Type(Class.class)
	@Implements(ExternalInstructionalOfferingNotOfferedAction.class)
	@Description("ExternalInstructionalOfferingNotOfferedAction interface called when an instructional offering was made not offered")
	ExternalActionInstructionalOfferingNotOffered("tmtbl.external.instr_offr.not_offered_action.class"),

	@Type(Class.class)
	@Implements(ExternalInstructionalOfferingOfferedAction.class)
	@Description("ExternalInstructionalOfferingOfferedAction interface called when an instructional offering was made offered")
	ExternalActionInstructionalOfferingOffered("tmtbl.external.instr_offr.offered_action.class"),

	@Type(Class.class)
	@Implements(ExternalSolutionCommitAction.class)
	@Description("ExternalSolutionCommitAction interface called when a solution was committed or uncommitted")
	ExternalActionSolutionCommit("tmtbl.external.solution.commit_action.class"),

	@Type(Class.class)
	@Implements(ExternalSchedulingSubpartEditAction.class)
	@Description("ExternalSchedulingSubpartEditAction interface called when a scheduling subpart was edited")
	ExternalActionSchedulingSubpartEdit("tmtbl.external.sched_subpart.edit_action.class"),
	
	@Type(Class.class)
	@Implements(ExternalCourseOfferingReservationEditAction.class)
	@Description("ExternalCourseOfferingReservationEditAction interface called when a reservation was edited")
	ExternalActionCourseOfferingReservationEdit("tmtbl.external.reservation.edit_action.class"),

	/**
	 * Use {@link SpringLdapExternalUidTranslation} when LDAP authentication is enabled.
	 * See http://help35.unitime.org/LDAP for more details.
	 */
	@Type(Class.class)
	@Implements(ExternalUidTranslation.class)
	@Description("ExternalUidTranslation interface for translating user external ids from different sources")
	ExternalUserIdTranslation("tmtbl.externalUid.translation"),

	@Type(Class.class)
	@Implements(DatabaseUpdate.class)
	@Description("Database: additional database update class to be executed at startup")
	DatabaseUpdateAddonClass("tmtbl.db.addon.update.class"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Examination Reports: use class suffixes instead of the section numbers")
	ExaminationReportsClassSufix("tmtbl.exam.report.suffix"),

	@DefaultValue("")
	@Description("Examination Reports: text to be displayed when an exam has no room assinged (e.g., INSTR OFFC)")
	ExaminationsNoRoomText("tmtbl.exam.report.noroom"),

	@Description("Examination Reports: room code (e.g., LAMB F101:A,ELLT 116:E,STEW 183:L,STEW 130:F)")
	ExaminationRoomCode("tmtbl.exam.report.roomcode"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Examination Reports: display class external id instead of instructional type")
	ExaminationReportsExternalId("tmtbl.exam.report.external"),

	@Description("Examination Reports: column name if external ids are to be displayed")
	ExaminationReportsExternalIdName("tmtbl.exam.report.external.name"),

	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("Examination Reports: show instructional types")
	ExaminationReportsShowInstructionalType("tmtbl.exam.report.itype"),

	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("Examination Reports: include student back-to-back conflicts by default")
	ExaminationReportsStudentBackToBacks("tmtbl.exams.reports.student.btb"),

	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("Examination Reports: include instructor back-to-back conflicts by default")
	ExaminationReportsInstructorBackToBacks("tmtbl.exams.reports.instructor.btb"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Examination PDF Reports: can use in-memory solution")
	ExaminationPdfReportsCanUseSolution("tmtbl.exam.pdfReports.canUseSolution"),

	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("Examination PDF Reports: pre-load cross-listed examinations")
	ExaminationPdfReportsPreloadCrosslistedExams("tmtbl.exam.pdfReports.useSolution.preloadCrosslistedExams"),

	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("Examination PDF Reports: skip suffixed subparts of the same instructional type (e.g., Lec 1a if there is a Lec 1) in the verification report")
	ExaminationPdfReportsSkipSuffixSubpart("tmtbl.exam.report.verification.skipSuffixSubparts"),

	@Description("Examination PDF Reports: % examinations title in the report")
	@Parameter("examination type")
	ExaminationPdfReportTitle("tmtbl.exam.report.%"),

	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("Examination PDF Reports: include class schedule in student / instructor individual reports")
	ExaminationPdfReportsIncludeClassSchedule("tmtbl.exam.report.cschedule"),

	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("Examination PDF Reports: class meeting time, use default date pattern as full term indication")
	ExaminationPdfReportsFullTermCheckDatePattern("tmtbl.exam.report.fullterm.checkdp"),

	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("Examination PDF Reports: use class event to compute class meeting time")
	ExaminationPdfReportsUseEventsForMeetingTimes("tmtbl.exam.report.meeting_time.use_events"),

	/**
	 * Exams default start and stop offsets. This is to be used to reserve time for
	 * students entering the exam room before the exam begins and time for students
	 * to leave the exam after the exam has finished. The start offset must be a
	 * positive number that is not greater than the number of minutes in the exam
	 * period.
	 */
	@Type(Integer.class)
	@DefaultValue("0")
	@Description("Examination Period: default start offset (% is the reference of the examination problem)")
	@Parameter("examination type")
	ExaminationPeriodDefaultStartOffset("tmtbl.exam.defaultStartOffset.%"),

	/**
	 * Exams default start and stop offsets. This is to be used to reserve time for
	 * students entering the exam room before the exam begins and time for students
	 * to leave the exam after the exam has finished. The start offset must be a
	 * positive number that is not greater than the number of minutes in the exam
	 * period.
	 */
	@Type(Integer.class)
	@DefaultValue("0")
	@Description("Examination Period: default stop offset (% is the reference of the examination problem)")
	@Parameter("examination type")
	ExaminationPeriodDefaultStopOffset("tmtbl.exam.defaultStopOffset.%"),

	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("Examinations: cache examination conflicts with solution")
	ExaminationCacheConflicts("tmtbl.exams.conflicts.cache"),

	/**
	 * Required minimal travel time between class event and an exam (in the number of 5-minute long time slots).
	 */
	@Type(Integer.class)
	@DefaultValue("6")
	@Description("Examinations: minimal travel time (in the number of 5 minute long slots) between an exam and any class for a student")
	ExaminationTravelTimeClass("tmtbl.exam.eventConflicts.travelTime.classEvent"),

	/**
	 * Required minimal travel time between course event and an exam (in the number of 5-minute long time slots).
	 */
	@Type(Integer.class)
	@DefaultValue("0")
	@Description("Examinations: minimal travel time (in the number of 5 minute long slots) between an exam and any course related event for a student")
	ExaminationTravelTimeCourse("tmtbl.exam.eventConflicts.travelTime.courseEvent"),

	@Type(Boolean.class)
	@Description("Examinations: use class / course limits to compute examination size instead of the actuall enrollment (defaults to false, except of final exams where it is true)")
	@Parameter("examination type")
	ExaminationSizeUseLimitInsteadOfEnrollment("tmtbl.exam.useLimit.%"),

	/**
	 * Examination Timetabling: Consider direct student and instructor conflicts with other events
	 * (that is class events, course events with required attendance) for the given examination problem.
	 */
	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("Examinations: consider event conflicts for % exams")
	@Parameter("examination type")
	ExaminationConsiderEventConflicts("tmtbl.exam.eventConflicts.%"),

	/**
	 * Examination Timetabling: Automatically create strongly preferred same room constraint between exams of the same owner(s)
	 * (while examination data are loaded into the solver).
	 */
	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Examinations: create strongly preferred same room constraints between exams of the same class / course")
	@Parameter("examination type")
	ExaminationCreateSameRoomConstraints("tmtbl.exam.sameRoom.%"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Examination Name: if there is an examiantion for a cross-listed instructional offering, name the examination after all the courses (not just after the controlling one)")
	ExaminationNameExpandCrossListedOfferingsToCourses("tmtbl.exam.name.expandCrosslistedOfferingToCourses"),

	/**
	 * See http://help35.unitime.org/Exam_Naming_Convention for more details.
	 */
	@Type(Integer.class)
	@DefaultValue("100")
	@Description("Examination Name: maximum length")
	ExamNameMaxLength("tmtbl.exam.name.maxLength"),

	@Description("Examination Name: type for % exams")
	@Parameter("examination type")
	ExamNameType("tmtbl.exam.name.type.%"),

	@DefaultValue("")
	@Description("Examination Name: suffix")
	ExamNameSuffix("tmtbl.exam.name.suffix"),

	@DefaultValue(";%_")
	@Description("Examination Name: differnt subjects separator (use %_ for space)")
	ExamNameSeparator("tmtbl.exam.name.diffSubject.separator"),

	@DefaultValue("%s %c")
	@Description("Examination Name: course exam (use %s for subject area abbreviation and %c for course number)")
	ExamNameCourse("tmtbl.exam.name.Course"),

	@DefaultValue("%s %c")
	@Description("Examination Name: offering exam (use %s for subject area abbreviation and %c for course number)")
	ExamNameOffering("tmtbl.exam.name.Offering"),

	@DefaultValue("%s %c [%x]")
	@Description("Examination Name: config exam (use %x for configuration name)")
	ExamNameConfig("tmtbl.exam.name.Config"),

	@DefaultValue("%s %c %i %n")
	@Description("Examination Name: class exam (use %i for instructional type and %n for section number)")
	ExamNameClass("tmtbl.exam.name.Class"),

	@DefaultValue("; %c")
	@Description("Examination Name: next course exam of the same subject")
	ExamNameSameSubjectCourse("tmtbl.exam.name.sameSubject.Course"),

	@DefaultValue("; %c")
	@Description("Examination Name: next offering exam of the same subject")
	ExamNameSameSubjectOffering("tmtbl.exam.name.sameSubject.Offering"),

	@DefaultValue("; %c [%x]")
	@Description("Examination Name: next config exam of the same subject")
	ExamNameSameSubjectConfig("tmtbl.exam.name.sameSubject.Config"),

	@DefaultValue("; %c %i %n")
	@Description("Examination Name: next class exam of the same subject")
	ExamNameSameSubjectClass("tmtbl.exam.name.sameSubject.Class"),

	@DefaultValue(", [%x]")
	@Description("Examination Name: next config exam of the same course")
	ExamNameSameCourseConfig("tmtbl.exam.name.sameCourse.Config"),

	@DefaultValue(", %i %n")
	@Description("Examination Name: next class exam of the same course")
	ExamNameSameCourseClass("tmtbl.exam.name.sameCourse.Class"),

	@DefaultValue(", %n")
	@Description("Examination Name: next class exam of the same subpart")
	ExamNameSameSubpartClass("tmtbl.exam.name.sameSubpart.Class"),

	/**
	 * Examination Timetabling: Automatically create following preferences (while an exam is being saved into the database).
	 * 
	 * For an exam that is attached to an evening class -> put period preference on a period that overlaps
	 * in time and day of week with the class (if not preference is set by the user).
	 */
	@DefaultValue("0")
	@Description("Examination Preferences: default prefence for an evening class exam of % examination type (R for required, -2 strongly preferred, -1 preferred, 0 rule disabled, 1 discouraged, 2 strongly discouraged, P prohibited)")
	@Parameter("examination type")
	ExamDefaultsEveningClassPreference("tmtbl.exam.defaultPrefs.%.eveningClasses.pref"),

	@Type(Integer.class)
	@DefaultValue("216")
	@Description("Examination Preferences: evening class is a class that takes place after this period (number of 5-minute long time slots from midnight, e.g., 216 is 6pm)")
	@Parameter("examination type")
	ExamDefaultsEveningClassStart("tmtbl.exam.defaultPrefs.%.eveningClasses.firstEveningPeriod"),

	/**
	 * Examination Timetabling: Automatically create following preferences (while an exam is being saved into the database).
	 * 
	 * For an exam that is attached to a class -> put room preference on a room (if it allows exams) of the class.
	 */
	@DefaultValue("0")
	@Description("Examination Preferences: default room prefence for a class exam of % examination type (R for required, -2 strongly preferred, -1 preferred, 0 rule disabled, 1 discouraged, 2 strongly discouraged, P prohibited)")
	@Parameter("examination type")
	ExamDefaultsOriginalRoomPreference("tmtbl.exam.defaultPrefs.%.originalRoom.pref"),

	/**
	 * Examination Timetabling: Automatically create following preferences (while an exam is being saved into the database).
	 * 
	 * For an exam that is attached to a class -> put building preference on a room of the class.
	 */
	@DefaultValue("0")
	@Description("Examination Preferences: default building prefence for a class exam of % examination type (R for required, -2 strongly preferred, -1 preferred, 0 rule disabled, 1 discouraged, 2 strongly discouraged, P prohibited)")
	@Parameter("examination type")
	ExamDefaultsOriginalBuildingPreference("tmtbl.exam.defaultPrefs.%.originalBuilding.pref"),

	/**
	 * Examination Timetabling: Automatically create following preferences (while an exam is being saved into the database).
	 * 
	 * If the building preference rule is enabled, set whether it is to be applied only when a class is in a room that allows exams.
	 */
	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Examination Preferences: put default building preference for a class that did not take place in an examination room (for a class exam of % examination type)")
	@Parameter("examination type")
	ExamDefaultsOriginalBuildingOnlyForExamRooms("tmtbl.exam.defaultPrefs.%.originalBuilding.onlyForExaminationRooms"),

	@Type(Class.class)
	@Implements(CourseDetailsProvider.class)
	@DefaultValue("org.unitime.timetable.onlinesectioning.custom.DefaultCourseDetailsProvider")
	@Description("Customization: course details provider (interface CourseDetailsProvider)")
	CustomizationCourseDetails("unitime.custom.CourseDetailsProvider"),

	@Type(Class.class)
	@Implements(CourseUrlProvider.class)
	@Description("Customization: course catalog link provider (interface CourseUrlProvider)")
	CustomizationCourseLink("unitime.custom.CourseUrlProvider"),
	
	@Type(Class.class)
	@Implements(StudentEnrollmentProvider.class)
	@Description("Customization: student enrollment provider (interface StudentEnrollmentProvider, used by Student Scheduling Assistant when the academic session is in the assistant mode)")
	CustomizationStudentEnrollments("unitime.custom.StudentEnrollmentProvider"),
	
	@Type(Class.class)
	@Implements(ExternalTermProvider.class)
	@Description("Customization: external term provider (interface ExternalTermProvider converting academic session info into an external term string etc.)")
	@Since(3.5)
	CustomizationExternalTerm("unitime.custom.ExternalTermProvider"),
	
	@Type(Class.class)
	@Implements(ExternalLinkLookup.class)
	@Description("Customization: course catalog link provider (interface ExternalLinkLookup, deprecated)")
	@Deprecated
	CourseCatalogLinkProvider("tmtbl.catalogLink.lookup.class"),
	
	@Type(Class.class)
	@Implements(SectionUrlProvider.class)
	@Deprecated
	@Description("Customization: section link provider (interface SectionUrlProvider, deprecated)")
	CustomizationSectionUrl("unitime.custom.SectionUrlProvider"),
	
	@Type(Class.class)
	@Implements(SectionLimitProvider.class)
	@Deprecated
	@Description("Customization: section limit provider (interface SectionLimitProvider, deprecated)")
	CustomizationSectionLimit("unitime.custom.SectionLimitProvider"),
	
	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Curriculum: convert academic area, classification and major codes and names to initial case")
	CurriculumConvertNamesToInitialCase("tmtbl.toInitialCase.curriculum"),

	@Type(Double.class)
	@DefaultValue("0.03")
	@Description("Makup Curriculum: minimal procentual projection across all the classifications (for a course to show up)")
	CurriculumLastLikeDemandsTotalShareLimit("tmtbl.curriculum.lldemands.totalShareLimit"),

	@Type(Double.class)
	@DefaultValue("0.0")
	@Description("Makup Curriculum: minimal procentual projection")
	CurriculumLastLikeDemandsShareLimit("tmtbl.curriculum.lldemands.shareLimit"),

	@Type(Integer.class)
	@DefaultValue("0")
	@Description("Makup Curriculum: minimal number of last-like students")
	CurriculumLastLikeDemandsEnrollmentLimit("tmtbl.curriculum.lldemands.enrlLimit"),

	@Type(Integer.class)
	@DefaultValue("-1")
	@Description("Events: indicate that a meeting is on an unusual time (too early); the value is the last time slot that is considered too early (e.g., 72 means 6 am)")
	EventTooEarlySlot("unitime.event.tooEarly"),

	@Type(Integer.class)
	@DefaultValue("5")
	@Description("Event: event expiration service thread update interval in minutes")
	EventExpirationServiceUpdateInterval("unitime.events.expiration.updateIntervalInMinutes"),

	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("Room Timetable: allow to see all the rooms (when set to false)")
	EventRoomTimetableAllRooms("unitime.event_timetable.event_rooms_only"),

	@Type(Class.class)
	@Implements(Email.class)
	@DefaultValue("org.unitime.commons.JavaMailWrapper")
	@Description("Email Configuration: provider class")
	EmailProvider("unitime.email.class"),

	@DefaultValue("127.0.0.1")
	@Description("Email Configuration: SMTP host")
	@Replaces({"tmtbl.smtp.host"})
	EmailSmtpHost("mail.smtp.host"),

	@DefaultValue("25")
	@Description("Email Configuration: SMTP port")
	EmailSmtpPort("mail.smtp.port"),

	@Description("Email Configuration: SMTP user (if authentication is needed)")
	@Replaces({"unitime.email.user", "tmtbl.mail.user"})
	EmailSmtpUser("mail.smtp.user"),

	@Description("Email Configuration: SMTP password (if authentication is needed)")
	@Replaces({"unitime.email.password", "tmtbl.mail.pwd"})
	@Secret
	EmailSmtpPassword("mail.smtp.password"),

	@Type(Boolean.class)
	@Description("Email Configuration: enable SSL if needed. Other mail.smtp.ssl properties may need to be set")
	EmailSmtpSSL("mail.smtp.ssl.enable"),

	@DefaultValue("noreply@unitime.org")
	@Description("Email Configuration: sender email address")
	EmailSenderAddress("unitime.email.sender"),

	@DefaultValue("UniTime Application")
	@Description("Email Configuration: sender name")
	EmailSenderName("unitime.email.sender.name"),

	@DefaultValue("support@unitime.org")
	@Description("Email Configuration: reply-to address")
	EmailReplyToAddress("unitime.email.replyto"),

	@DefaultValue("UniTime Support")
	@Description("Email Configuration: reply-to name")
	EmailReplyToName("unitime.email.replyto.name"),

	@DefaultValue("demo@unitime.org")
	@Description("Email Notifications: email address")
	EmailNotificationAddress("unitime.email.notif"),

	@DefaultValue("UniTime Demo")
	@Description("Email Notifications: email address name")
	EmailNotificationAddressName("unitime.email.notif.name"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Email Notifications: send solution commit / uncommit notifications")
	EmailNotificationSolutionCommits("unitime.email.notif.commit"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Email Notifications: send error reports")
	EmailNotificationErrorReports("unitime.email.notif.error"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Email Notifications: data exchange emails")
	EmailNotificationDataExchange("unitime.email.notif.data"),

	@Description("Contact Us: email address (if not used, unitime.email.notif is used instead)")
	@Replaces({"unitime.email.notif"})
	EmailInquiryAddress("unitime.email.inquiry"),

	@Description("Contact Us: email address name (if not used, unitime.email.notif.name is used instead)")
	@Replaces({"unitime.email.notif.name"})
	EmailInquiryAddressName("unitime.email.inquiry.name"),

	@DefaultValue("@unitime.org")
	@Description("Contact Us: for users without email, an email is constructed as login name + the given address suffix")
	EmailInquiryAddressSuffix("unitime.email.inquiry.suffix"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Contact Us: also send an autoreply message back to the user (if false, the user is CC-ed in the original message); this is handy when sending emails to users can fail (e.g., because of an invalid email address)")
	EmailInquiryAutoreply("unitime.email.inquiry.autoreply"),

	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("Email Confirmations: Enable confirmation emails for event management")
	@Replaces({"tmtbl.event.confirmationEmail"})
	EmailConfirmationEvents("unitime.email.confirm.event"),

	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("Event Email Confirmations: default value of the \"Send email confirmation\" toggle")
	EmailConfirmationEventsDefault("unitime.email.confirm.default"),

	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("Event Email Confirmations: include instructors")
	EmailConfirmationEventInstructors("unitime.email.event.instructor"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Event Email Confirmations: include coordinators")
	EmailConfirmationEventCoordinators("unitime.email.event.coordinator"),
	
	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Event Email Confirmations: include event managers")
	EmailConfirmationEventManagers("unitime.email.event.managers"),

	@Description("Event Email Confirmations: default email suffix (e.g., @unitime.org)")
	EmailDefaultAddressSuffix("unitime.email.event.suffix"),

	/**
	 * Enable UniTime to receive replies on event confirmation emails.
	 * If a reply message is received, a new note is added to the event's notes and an updated confirmation is sent to all contacts of the event.
	 * If the reply message contains an attachment, it is also added to the new event note and included in the new confirmation email.
	 */
	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Event Email Confirmations: Enable UniTime to receive replies on event confirmation emails. If a reply message is received, a new note is added to the event's notes and an updated confirmation is sent to all contacts of the event. If the reply message contains an attachment, it is also added to the new event note and included in the new confirmation email.")
	InboundEmailsEnabled("unitime.email.inbound.enabled"),

	@Secret
	@Description("Event Email Confirmations: IMAP connection for receiving replies (for Gmail and SSL use imaps://[username]:[password]@imap.gmail.com/INBOX, if username contains a whole email address, use %40 instead of @ in the username)")
	InboundEmailsAddress("unitime.email.inbound.uri"),

	@Description("Event Email Confirmations: reply-to address for event confirmation emails (e.g., [username]@gmail.com)")
	InboundEmailsReplyToAddress("unitime.email.inbound.address"),
	
	@DefaultValue("UniTime Events")
	@Description("Event Email Confirmations: reply-to address name for event confirmation emails")
	InboundEmailsReplyToAddressName("unitime.email.inbound.name"),

	@Description("IP address of the machine running UniTime")
	@Since(3.5)
	JGroupsBindAddress("jgroups.bind_addr"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Clustering: form hibernate cluster to replicate Hibernate L2 cache")
	@Since(3.5)
	HibernateClusterEnabled("unitime.hibernate.cluster"),
	
	@DefaultValue("hibernate-jgroups-tcp.xml")
	@Description("Clustering: hibernate L2 cache cluster configuration")
	@Since(3.5)
	HibernateClusterConfiguration("unitime.hibernate.jgroups.config"),
	
	@Description("Clustering: default port for the hibernate cluster")
	@Since(3.5)
	HibernateClusterPort("unitime.hibernate.port"),

	@Description("Clustering: initial hosts for the hibernate cluster (cluster discovery)")
	@Since(3.5)
	HibernateClusterInitialHosts("unitime.hibernate.initial_hosts"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Clustering: fork online student scheduling cluster from the solver server cluster")
	@Since(3.5)
	OnlineSchedulingClusterForkChannel("unitime.enrollment.jgroups.fork_channel"),

	@DefaultValue("sectioning-jgroups-tcp.xml")
	@Description("Clustering: online scheduling cluster configuration")
	@Since(3.5)
	OnlineSchedulingClusterConfiguration("unitime.enrollment.jgroups.config"),
	
	@Description("Clustering: default port for the online scheduling cluster")
	@Since(3.5)
	OnlineSchedulingClusterPort("unitime.enrollment.port"),
	
	@Description("Clustering: initial hosts for the online scheduling cluster (cluster discovery)")
	@Since(3.5)
	OnlineSchedulingClusterInitialHosts("unitime.enrollment.initial_hosts"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Clustering: form solver cluster to communitace between solver servers")
	@Since(3.5)
	SolverClusterEnabled("unitime.solver.cluster"),

	@DefaultValue("solver-jgroups-tcp.xml")
	@Description("Clustering: solver cluster configuration")
	@Since(3.5)
	SolverClusterConfiguration("unitime.solver.jgroups.config"),
	
	@Description("Clustering: default port for the solver cluster")
	@Since(3.5)
	SolverClusterPort("unitime.solver.port"),
	
	@Description("Clustering: initial hosts for the solver cluster (cluster discovery)")
	@Since(3.5)
	SolverClusterInitialHosts("unitime.solver.initial_hosts"),

	@Type(Integer.class)
	@DefaultValue("100")
	@Description("Test HQL: maximum number of returned lines")
	TestHQLMaxLines("tmtbl.test_hql.max_line"),

	@Type(Integer.class)
	@DefaultValue("5000")
	@Description("Query Log: limit on the number of queries held in memory (before persisted)")
	QueryLogLimit("unitime.query.log.limit"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Classes: if there are two or more scheduling subparts in a parent-child relation with the same instructional type (e.g., Lec - Lec a - Lec b stacked underneath), inherit preferences and the date pattern from the parent subpart whenever possible")
	@Since(3.5)
	PreferencesHierarchicalInheritance("unitime.preferences.hierarchicalInheritance"),
	
	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Highlight preferences that are set directly on classes")
	@Since(3.5)
	PreferencesHighlighClassPreferences("unitime.preferences.highlightClassPrefs"),

	@Type(Integer.class)
	@DefaultValue("0")
	@Description("Session Start/End Month: add a given number of days to the first / last day of a session")
	SessionNrExcessDays("unitime.session.nrExcessDays"),

	@Type(Integer.class)
	@DefaultValue("3")
	@Description("Date Pattern Start/End Month: add a given number of months to the first / last month of a session")
	DatePatternNrExessMonth("unitime.pattern.nrExcessMoths"),

	@Type(Integer.class)
	@DefaultValue("1000")
	@Description("People Lookup: limit on the number of returned records")
	PeopleLookupLimit("tmtbl.lookup.limit"),

	@Description("People Lookup LDAP: ldap url (if not configured using the ldapPeopleLookupTemplate bean)")
	PeopleLookupLdapUrl("tmtbl.lookup.ldap"),

	@DefaultValue("")
	@Description("People Lookup LDAP: ldap search base (if not configured using the ldapPeopleLookupTemplate bean)")
	PeopleLookupLdapBase("tmtbl.lookup.ldap.name"),

	@Description("People Lookup LDAP: ldap user (if not configured using the ldapPeopleLookupTemplate bean)")
	PeopleLookupLdapUser("tmtbl.lookup.ldap.user"),

	@Description("People Lookup LDAP: ldap password (if not configured using the ldapPeopleLookupTemplate bean)")
	@Secret
	PeopleLookupLdapPassword("tmtbl.lookup.ldap.password"),

	@Type(Integer.class)
	@DefaultValue("100")
	@Description("People Lookup LDAP: search limit")
	PeopleLookupLdapLimit("tmtbl.lookup.ldap.countLimit"),

	@DefaultValue("(|(|(sn=%*)(uid=%))(givenName=%*)(cn=* %* *)(mail=%*))")
	@Description("People Lookup LDAP: search query")
	PeopleLookupLdapQuery("tmtbl.lookup.ldap.query"),

	@DefaultValue("mail")
	@Description("People Lookup LDAP: email attribute")
	PeopleLookupLdapEmailAttribute("tmtbl.lookup.ldap.email"),

	@DefaultValue("phone,officePhone,homePhone,telephoneNumber")
	@Description("People Lookup LDAP: phone attribute")
	PeopleLookupLdapPhoneAttribute("tmtbl.lookup.ldap.phone"),

	@DefaultValue("department")
	@Description("People Lookup LDAP: department attribute")
	PeopleLookupLdapDepartmentAttribute("tmtbl.lookup.ldap.department"),

	@DefaultValue("position,title")
	@Description("People Lookup LDAP: position attribute")
	PeopleLookupLdapPositionAttribute("tmtbl.lookup.ldap.position"),
	
	@Description("People Lookup LDAP: academic title attribute")
	PeopleLookupLdapAcademicTitleAttribute("tmtbl.lookup.ldap.title"),

	@Description("Reservations: default reservation expiration date for all reservation types (given either in the number of days relative to the academic session begin date, or as a date in yyyy-mm-dd format)")
	ReservationExpirationDateGlobal("unitime.reservations.expiration_date"),

	@Description("Reservations: default reservation expiration date for reservation of type % (given either in the number of days relative to the academic session begin date, or as a date in yyyy-mm-dd format)")
	@Parameter("reservation type")
	ReservationExpirationDate("unitime.reservations.%.expiration_date"),

	@Type(Integer.class)
	@Description("Reservations: default expiration date for all reservation types; it is defined by a number of days after today (e.g., 7 days from now)")
	ReservationExpirationInDaysGlobal("unitime.reservations.expires_in_days"),

	@Type(Integer.class)
	@Description("Reservations: default expiration date for reservation of type %; it is defined by a number of days after today (e.g., 7 days from now)")
	@Parameter("reservation type")
	ReservationExpirationInDays("unitime.reservations.%.expires_in_days"),

	/**
	 * Minimap (to be displayed if set, e.g., on the Room Detail page)
	 * <ul>
	 * <li>Strings %x and %y are replaced by the room's coordinates
	 * <li>String %n is replaced by the room name
	 * <li>String %i is replaced by the room external id 
	 * </ul>
	 */
	@DefaultValue("https://maps.google.com/maps/api/staticmap?center=%x,%y&zoom=16&size=600x400&maptype=roadmap&sensor=false&markers=color:blue|%x,%y")
	@Description("Rooms: Campus map image")
	RoomMapStatic("unitime.minimap.url"),

	/**
	 * Minimap to be used in location's tooltip
	 * <ul>
	 * <li>Strings %x and %y are replaced by the room's coordinates
	 * <li>String %n is replaced by the room name
	 * <li>String %i is replaced by the room external id 
	 * </ul>
	 */
	@DefaultValue("https://maps.google.com/maps/api/staticmap?center=%x,%y&zoom=15&size=300x200&maptype=roadmap&sensor=false&markers=color:blue|%x,%y")
	@Description("Rooms: minimap to be used in location's tooltip (%x and %y are replaced by the room's coordinates)")
	RoomHintMinimapUrl("unitime.minimap.hint"),

	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("Rooms: use Google maps to enter room / building coordinates")
	RoomUseGoogleMap("unitime.coordinates.googlemap"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Rooms: include break time in the hint")
	RoomHintShowBreakTime("unitime.roomHint.showBreakTime"),

	@Type(Integer.class)
	@DefaultValue("1000")
	@Description("Last Changes: limit on the number of returned changes")
	LastChangesLimit("unitime.changelog.limit"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Solver: include names in the XML Export")
	SolverXMLExportNames("unitime.solution.export.names"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Solver: serialize uniuqe ids in the XML Export")
	SolverXMLExportConvertIds("unitime.solution.export.id-conv"),

	/**
	 * Solution passivation time.
	 * Number of minutes after which an inactive solution can be passivated to disk to save memory.
	 * Passivation is disabled if set to zero, default is 30 minutes.
	 */
	@Type(Integer.class)
	@DefaultValue("30")
	@Description("Solver: passivate solution after given number of minutes of inactivity (disable passivation by setting to zero)")
	SolverPasivationTime("unitime.solver.passivation.time"),

	@Type(Integer.class)
	@DefaultValue("300")
	@Description("Online Student Scheduling: sectioning queue load interval in seconds")
	OnlineSchedulingQueueLoadInterval("unitime.sectioning.queue.loadInterval"),

	@Type(Integer.class)
	@DefaultValue("30")
	@Description("Online Student Scheduling: sectioning queue update interval in seconds")
	OnlineSchedulingQueueUpdateInterval("unitime.sectioning.queue.updateInterval"),

	@Description("Online Student Scheduling: only academic sessions matching this year (regular expression) are loaded in")
	OnlineSchedulingAcademicYear("unitime.enrollment.year"),

	@Description("Online Student Scheduling: only academic sessions matching this term (regular expression) are loaded in")
	OnlineSchedulingAcademicTerm("unitime.enrollment.term"),

	@Description("Online Student Scheduling: only academic sessions matching this campus (initiative, regular expression) are loaded in")
	OnlineSchedulingAcademicCampus("unitime.enrollment.campus"),

	@Type(Class.class)
	@Implements(OnlineSectioningServer.class)
	@Description("Online Student Scheduling: server implementation")
	OnlineSchedulingServerClass("unitime.enrollment.server.class"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Online Student Scheduling: server support replications (it can be loaded on multiple machines of the cluster)")
	OnlineSchedulingServerReplicated("unitime.enrollment.server.replicated"),

	@Type(Integer.class)
	@DefaultValue("366")
	@Description("Change Log: automatically remove records after the given number of days")
	LogCleanupChangeLog("unitime.cleanup.changeLog"),

	@Type(Integer.class)
	@DefaultValue("92")
	@Description("Query Log: automatically remove records after the given number of days")
	LogCleanupQueryLog("unitime.cleanup.queryLog"),

	@Type(Integer.class)
	@DefaultValue("366")
	@Description("Online Scheduling Log: automatically remove records after the given number of days")
	LogCleanupOnlineSchedulingLog("unitime.cleanup.sectioningLog"),

	@Type(Integer.class)
	@DefaultValue("14")
	@Description("Message Log: automatically remove records after the given number of days")
	LogCleanupMessageLog("unitime.message.log.cleanup.days"),

	@Type(Integer.class)
	@DefaultValue("14")
	@Description("Online Scheduling Queue: automatically remove records after the given number of days")
	LogCleanupOnlineSchedulingQueue("unitime.cleanup.sectioningQueue"),

	@DefaultValue("WARN")
	@Values({"ERROR", "WARN", "INFO"})
	@Description("Message Log: minimum message level to be logged in the MessageLog table")
	MessageLogLevel("unitime.message.log.level"),

	@Type(Integer.class)
	@DefaultValue("5000")
	@Description("Message Log: limit on the number of records held in memory (before persisted)")
	MessageLogLimit("unitime.message.log.limit"),

	@Type(Integer.class)
	@DefaultValue("180")
	@Description("Message Log: message log cleanup interval in minutes")
	MessageLogCleanupInterval("unitime.message.log.cleanup.interval"),

	/**
	 * For various PDF exports, if you need other fonts than the ones bundled in the iText library (e.g., because of their poor unicode support),
	 * set the following unitime.pdf.font properties. The fonts will be embedded in the exported PDF.
	 * See application.properties for some tested examples.
	 */
	@Description("PDF Font: normal font")
	PdfFontNormal("unitime.pdf.font"),

	@Description("PDF Font: italic font")
	PdfFontItalic("unitime.pdf.font.italic"),

	@Description("PDF Font: bold font")
	PdfFontBold("unitime.pdf.font.bold"),

	@Description("PDF Font: bold italic font")
	PdfFontBoldItalic("unitime.pdf.font.bolditalic"),

	@Description("PDF Font: fixed font")
	PdfFontFixed("unitime.pdf.font.fixed"),

	@Type(Float.class)
	@DefaultValue("9")
	@Description("PDF Font: small size")
	PdfFontSizeSmall("unitime.pdf.fontsize.small"),

	@Type(Float.class)
	@DefaultValue("12")
	@Description("PDF Font: normal size")
	PdfFontSizeNormal("unitime.pdf.fontsize.normal"),

	@Type(Float.class)
	@DefaultValue("9")
	@Description("PDF Font: fixed font size")
	PdfFontSizeFixed("unitime.pdf.fontsize.fixed"),

	@Type(Float.class)
	@DefaultValue("16")
	@Description("PDF Font: big size")
	PdfFontSizeBig("unitime.pdf.fontsize.big"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("PDF Font: cache fonts in memory")
	PdfFontCache("unitime.pdf.fontcache"),

	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("Instructional Offerings: enable PDF Worksheet export")
	WorksheetPdfEnabled("tmtbl.pdf.worksheet"),

	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("PDF Worksheet: include committed assignments")
	WorksheetPdfUseCommittedAssignments("tmtbl.pdf.worksheet.useCommitedAssignments"),

	@DefaultValue("UniTime %")
	@Description("PDF Worksheet: author (% is replaced with version number)")
	WorksheetPdfAuthor("tmtbl.pdf.worksheet.author"),

	@DefaultValue("PDF WORKSHEET")
	@Description("PDF Worksheet: report title")
	WorksheetPdfTitle("tmtbl.pdf.worksheet.title"),

	@Type(Integer.class)
	@DefaultValue("6")
	@Description("Timetable Grid: number of slots (5 minute long) per period")
	TimetableGridSlotsPerPeriod("tmtbl.timeGrid.slotsPerPeriod"),

	@Type(Integer.class)
	@DefaultValue("90")
	@Description("Timetable Grid: start time (in the number of 5 minute long slots starting midnight, e.g., 90 is 7:30 am)")
	TimetableGridFirstDaySlot("tmtbl.timeGrid.firstDaySlot"),

	@Type(Integer.class)
	@DefaultValue("209")
	@Description("Timetable Grid: end time (in the number of 5 minute long slots starting midnight, e.g., 209 is 5:25 pm)")
	TimetableGridLastDaySlot("tmtbl.timeGrid.lastDaySlot"),

	@Type(Integer.class)
	@DefaultValue("275")
	@Description("Timetable Grid: evening end time (in the number of 5 minute long slots starting midnight, e.g., 275 is 10:55 pm)")
	TimetableGridLastEveningSlot("tmtbl.timeGrid.lastEveningSlot"),

	@Type(Integer.class)
	@DefaultValue("10")
	@Description("Timetable Grid: max cell width")
	TimetableGridMaxCellWidth("tmtbl.timeGrid.maxCellWidth"),

	@Type(Integer.class)
	@DefaultValue("200")
	@Description("Timetable Grid: max cell width (vertical layout)")
	TimetableGridMaxCellWidthVertical("tmtbl.timeGrid.maxCellWidthVertical"),

	@Description("Contact Us: address")
	ContactUsAddress("tmtbl.contact.address"),

	@Description("Contact Us: phone")
	ContactUsPhone("tmtbl.contact.phone"),

	@Description("Contact Us: office hours")
	ContactUsOfficeHours("tmtbl.contact.office_hours"),

	@Description("Contact Us: email")
	ContactUsEmail("tmtbl.contact.email"),

	@Description("Contact Us: mailto anchor (e.g., timetabling@universty.edu?subject=TIMETABLING: &lt;add subject here&gt)")
	ContactUsMailTo("tmtbl.contact.email_mailto"),

	@DefaultValue("^(tmtbl|unitime)\\..*$")
	@Description("Application Configuration: only properties matching the following regular expression are displayed")
	ApplicationConfigPattern("tmtbl.appConfig.pattern"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Events: allow edit past meetings (deprecated, use EventEditPast permission instead)")
	@Deprecated
	EventAllowEditPast("tmtbl.event.allowEditPast"),

	@Description("Exams / Classes Page: user login message (deprecated)")
	@Deprecated
	ExamsLoginMessage("tmtbl.exams.login.message"),

	@DefaultValue("http://help35.unitime.org/Frequently_Asked_Questions")
	@Description("Help: FAQ page")
	HelpFAQ("tmtbl.help.faq"),

	@DefaultValue("http://www.unitime.org/papers/event_documentation.pdf")
	@Description("Manuals: event manual")
	ManualEvents("tmtbl.help.manual.events"),

	@DefaultValue("http://www.unitime.org/papers/ttman_data.pdf")
	@Description("Manuals: course timetabling data entry manual")
	ManualCourseDataEntry("tmtbl.help.manual.input_data"),

	@DefaultValue("http://www.unitime.org/papers/ttman_solver.pdf")
	@Description("Manuals: course timetabling solver manual")
	ManualCourseSolver("tmtbl.help.manual.solver"),

	@DefaultValue("help/Release-Notes.xml")
	@Description("Help: release notes")
	HelpReleaseNotes("tmtbl.help.release_notes"),

	@DefaultValue("http://help35.unitime.org/Timetabling")
	@Description("Help: online help landing page")
	HelpMain("tmtbl.help.root"),

	@DefaultValue("http://help35.unitime.org/Tips_and_Tricks")
	@Description("Help: tips and tricks")
	HelpTricks("tmtbl.help.tricks"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Session Roll Forward: allow roll forward of class preferences")
	RollForwardAllowClassPreferences("unitime.rollforward.allowClassPrefs"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Main Page: when Internet Explorer is used, show \"the UniTime application may run very slow in Internet Explorer\" warning")
	@Deprecated
	ChromeFrameWarning("unitime.warn.chromeframe"),
	
	@Parameter("class")
	@Description("Logging: logging level for %")
	@ReadOnly
	LoggingLevel("log4j.logger.%"),
	
	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("Instructor Add/Edit: allow editation of external id")
	InstructorAllowEditExternalId("tmtbl.instructor.allowExternalIdEdit"),
	
	@Type(Class.class)
	@Implements(org.unitime.timetable.backup.SessionBackupInterface.class)
	@DefaultValue("org.unitime.timetable.backup.SessionBackup")
	@Description("Implementation of the session backup interface.")
	SessionBackupInterface("unitime.session_backup.class"),

	@Type(Class.class)
	@Implements(org.unitime.timetable.backup.SessionRestoreInterface.class)
	@DefaultValue("org.unitime.timetable.backup.SessionRestore")
	@Description("Implementation of the session restore interface.")
	SessionRestoreInterface("unitime.session_restore.class"),
	
	
	@Type(String.class)
	@DefaultValue("https://selfservice.mypurdue.purdue.edu/prod/bzwsrch.p_catalog_detail?term=:xterm&subject=:subject&cnbr=:courseNbr&enhanced=Y")
	@Description("DefaultCourseDetailsProvider: course url\n"
			+ "Use:\n"
			+ " - :campus, :term, :year for academic session identification or\n"
			+ " - :xcampus, :xterm when ExternalTermProvider is configured,\n"
			+ " - :subject for subject area abbreviation and :courseNbr for course number or\n"
			+ " - :xsubject, :xcourseNbr when ExternalTermProvider is configured.\n"
			+ "Example: https://www.university.edu/catalog?term=:term:year&amp;subject=:subject&cnbr=:courseNbr")
	CustomizationDefaultCourseUrl("unitime.custom.default.course_url"),
	
	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("DefaultCourseDetailsProvider: downloads course details (unitime.custom.default.course_url must be set)")
	CustomizationDefaultCourseDetailsDownload("unitime.custom.default.course_download"),
	
	@Type(String.class)
	@DefaultValue("(?idm)<body[^>]*>(.*)</body>")
	@Description("DefaultCourseDetailsProvider: if course details are downloaded (unitime.custom.default.course_download is true), "
			+"this property contains regular expression that is used to get the content of the downloaded page")
	CustomizationDefaultCourseDetailsContent("unitime.custom.default.course_regexp"),
	
	@Type(String.class)
	@Description("DefaultCourseDetailsProvider: if course details are downloaded (unitime.custom.default.course_download is true), "
			+"this property contains a list of regular expressions that are used to reformat the content. This property "
			+"can contain multiple lines with the following sequence:"
			+"\n  1st regural expression,\n  1st replacement,\n  2nd regular expression,\n  2nd replacement,\n  ...\n"
			+"Example:\n  (?i)<a href=\"[^>]*\">\n  <b>\n  (?i)</a>\n  </b>")
	CustomizationDefaultCourseDetailsModifiers("unitime.custom.default.course_modifiers"),
	
	
	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("Scheduling Subpart: default value of the Automatic Spread In Time toggle")
	SchedulingSubpartAutoSpreadInTimeDefault("unitime.defaults.subpartSpreadInTime"),
	
	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Scheduling Subpart: default value of the Student Overlaps toggle")
	SchedulingSubpartStudentOverlapsDefault("unitime.defaults.subpartStudentOverlaps"),
	
	@Type(Integer.class)
	@DefaultValue("100")
	@Description("Reservations: Individual reservation priority")
	ReservationPriorityIndividual("unitime.reservation.priority.individual"),

	@Type(Integer.class)
	@DefaultValue("200")
	@Description("Reservations: Group reservation priority")
	ReservationPriorityGroup("unitime.reservation.priority.group"),
	
	@Type(Integer.class)
	@DefaultValue("300")
	@Description("Reservations: Reservation override priority")
	ReservationPriorityOverride("unitime.reservation.priority.override"),
	
	@Type(Integer.class)
	@DefaultValue("400")
	@Description("Reservations: Course reservation priority")
	ReservationPriorityCourse("unitime.reservation.priority.course"),
	
	@Type(Integer.class)
	@DefaultValue("500")
	@Description("Reservations: Curriculum reservation priority")
	ReservationPriorityCurriculum("unitime.reservation.priority.curriculum"),
	
	@Type(Integer.class)
	@DefaultValue("600")
	@Description("Reservations: Curriculum reservation priority")
	ReservationPriorityDummy("unitime.reservation.priority.dummy"),
	
	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("Reservations: Individual reservation must be used")
	ReservationMustBeUsedIndividual("unitime.reservation.mustBeUsed.individual"),

	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("Reservations: Group reservation must be used")
	ReservationMustBeUsedGroup("unitime.reservation.mustBeUsed.group"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Reservations: Course reservation must be used")
	ReservationMustBeUsedCourse("unitime.reservation.mustBeUsed.course"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Reservations: Curriculum reservation must be used")
	ReservationMustBeUsedCurriculum("unitime.reservation.mustBeUsed.curriculum"),

	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("Reservations: Individual reservation can over limit")
	ReservationCanOverLimitIndividual("unitime.reservation.canOverLimit.individual"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Reservations: Group reservation can over limit")
	ReservationCanOverLimitGroup("unitime.reservation.canOverLimit.group"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Reservations: Course reservation can over limit")
	ReservationCanOverLimitCourse("unitime.reservation.canOverLimit.course"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Reservations: Curriculum reservation can over limit")
	ReservationCanOverLimitCurriculum("unitime.reservation.canOverLimit.curriculum"),

	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("Reservations: Individual reservation allow time conflicts")
	ReservationAllowOverlapIndividual("unitime.reservation.allowOverlap.individual"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Reservations: Group reservation allow time conflicts")
	ReservationAllowOverlapGroup("unitime.reservation.allowOverlap.group"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Reservations: Course reservation allow time conflicts")
	ReservationAllowOverlapCourse("unitime.reservation.allowOverlap.course"),

	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("Reservations: Curriculum reservation allow time conflicts")
	ReservationAllowOverlapCurriculum("unitime.reservation.allowOverlap.curriculum"),
	
	@Description("CAS Authentication: user external id attribute, if not set uid translation will take place instead")
	AuthenticationCasIdAttribute("unitime.authentication.cas.id-attribute"),
	
	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("CAS Authentication: always translate the id-attribute using the provided external user id translation class (see tmtbl.externalUid.translation property)")
	AuthenticationCasIdAlwaysTranslate("unitime.authentication.cas.id-translate"),
	
	@Type(Boolean.class)
	@DefaultValue("false")
	@Description("CAS Authentication: trim leading zeros from the user external id")
	AuthenticationCasIdTrimLeadingZerosFrom("unitime.authentication.cas.id-trim"),
	
	@Description("CAS Authentication: user full name attribute")
	AuthenticationCasNameAttribute("unitime.authentication.cas.name-attribute"),

	@Type(Boolean.class)
	@DefaultValue("true")
	@Description("Conflict-statistics: save to file")
	ConflictStatisticsSaveToFile("unitime.cbs.saveToFile"),
	;

	String iKey;
	
	ApplicationProperty(String key) { iKey = key; }
	
	public String key() { return iKey; }
	public String key(String reference) { return (reference == null ? iKey : iKey.replace("%", reference)); }
	
	public String defaultValue() {
		try {
			DefaultValue defaultValue = ApplicationProperty.class.getField(name()).getAnnotation(DefaultValue.class);
			return (defaultValue == null ? null : defaultValue.value());
		} catch (NoSuchFieldException e) {
			return null;
		} catch (SecurityException e) {
			return null;
		}
	}
	public String description() {
		try {
			Description description = ApplicationProperty.class.getField(name()).getAnnotation(Description.class);
			return (description == null ? null : description.value());
		} catch (NoSuchFieldException e) {
			return null;
		} catch (SecurityException e) {
			return null;
		}
	}
	
	public Class type() {
		try {
			Type type = ApplicationProperty.class.getField(name()).getAnnotation(Type.class);
			return (type == null ? String.class : type.value());
		} catch (NoSuchFieldException e) {
			return null;
		} catch (SecurityException e) {
			return null;
		}
	}
	
	public Class implementation() {
		try {
			Implements impl = ApplicationProperty.class.getField(name()).getAnnotation(Implements.class);
			return (impl == null ? null : impl.value());
		} catch (NoSuchFieldException e) {
			return null;
		} catch (SecurityException e) {
			return null;
		}
	}
	
	public String[] availableValues() {
		try {
			Values type = ApplicationProperty.class.getField(name()).getAnnotation(Values.class);
			return (type == null ? null : type.value());
		} catch (NoSuchFieldException e) {
			return null;
		} catch (SecurityException e) {
			return null;
		}
	}
	
	public Double since() {
		try {
			Since since = ApplicationProperty.class.getField(name()).getAnnotation(Since.class);
			return (since == null ? null : since.value());
		} catch (NoSuchFieldException e) {
			return null;
		} catch (SecurityException e) {
			return null;
		}
	}
	
	public String value() {
		return value(null, null);
	}
	
	public String value(String reference) {
		return value(reference, null);
	}
	
	public String value(String reference, String defaultValueOverride) {
		String value = ApplicationProperties.getProperty(key(reference));
		if (value != null) return value;
		
		String[] oldKeys = replaces();
		if (oldKeys != null)
			for (String key: oldKeys) {
				value = ApplicationProperties.getProperty(reference == null ? key : key.replace("%", reference));
				if (value != null) return value;
			}
		
		return defaultValueOverride != null ? defaultValueOverride : defaultValue();
	}
	
	public boolean isTrue() {
		return "true".equalsIgnoreCase(value());
	}
	
	public boolean isTrue(String reference) {
		return "true".equalsIgnoreCase(value(reference));
	}
	
	public boolean isTrue(String reference, boolean defaultValueOverride) {
		return "true".equalsIgnoreCase(value(reference, defaultValueOverride ? "true": "false"));
	}
	
	public boolean isFalse() {
		return !isTrue();
	}
	
	public Integer intValue() {
		try {
			return Integer.valueOf(value());
		} catch (Exception e) {
			return null;
		}
	}
	
	public Integer intValue(String reference) {
		try {
			return Integer.valueOf(value(reference));
		} catch (Exception e) {
			return null;
		}
	}
	
	public Float floatValue() {
		try {
			return Float.valueOf(value());
		} catch (Exception e) {
			return null;
		}
	}
	
	public Double doubleValue() {
		try {
			return Double.valueOf(value());
		} catch (Exception e) {
			return null;
		}
	}
	
	private String[] replaces() {
		try {
			Replaces replaces = ApplicationProperty.class.getField(name()).getAnnotation(Replaces.class);
			return (replaces == null ? null : replaces.value());
		} catch (NoSuchFieldException e) {
			return null;
		} catch (SecurityException e) {
			return null;
		}
	}
	
	public boolean matches(String key) {
		if (key.matches(key().replace(".", "\\.").replace("%", "(.+)"))) return true;
		
		String[] oldKeys = replaces();
		if (oldKeys != null)
			for (String old: oldKeys) {
				if (key.matches(old.replace(".", "\\.").replace("%", "(.+)"))) return true;
			}
			
		return false;
	}
	
	public String reference() {
		try {
			Parameter reference = ApplicationProperty.class.getField(name()).getAnnotation(Parameter.class);
			return (reference == null ? null : reference.value());
		} catch (NoSuchFieldException e) {
			return null;
		} catch (SecurityException e) {
			return null;
		}
	}
	
	public String reference(String key) {
		Matcher m = Pattern.compile(key().replace(".", "\\.").replace("%", "(.+)")).matcher(key);
		if (m.matches() && m.groupCount() > 0)
			return m.group(1);

		String[] oldKeys = replaces();
		if (oldKeys != null)
			for (String old: oldKeys) {
				m = Pattern.compile(old.replace(".", "\\.").replace("%", "(.+)")).matcher(key);
				if (m.matches() && m.groupCount() > 0)
					return m.group(1);
			}
		
		return null;
	}
	
	public static ApplicationProperty fromKey(String key) {
		for (ApplicationProperty property: values())
			if (property.key().equals(key)) return property;

		for (ApplicationProperty property: values())
			if (property.matches(key)) return property;
		
		return null;
	}
	
	public static String getDescription(String key) {
		ApplicationProperty property = fromKey(key);
		if (property != null) {
			String reference = property.reference(key);
			String description = property.description();
			if (reference != null)
				return description.replace("%", reference);
			return description;
		}
		return null;
	}
	
	public boolean isDeprecated() {
		try {
			return (ApplicationProperty.class.getField(name()).getAnnotation(Deprecated.class) != null);
		} catch (NoSuchFieldException e) {
			return false;
		} catch (SecurityException e) {
			return false;
		}
	}
	
	public boolean isSecret() {
		try {
			return (ApplicationProperty.class.getField(name()).getAnnotation(Secret.class) != null);
		} catch (NoSuchFieldException e) {
			return false;
		} catch (SecurityException e) {
			return false;
		}
	}
	
	public boolean isReadOnly() {
		try {
			return (ApplicationProperty.class.getField(name()).getAnnotation(ReadOnly.class) != null);
		} catch (NoSuchFieldException e) {
			return false;
		} catch (SecurityException e) {
			return false;
		}
	}
	
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(value={ElementType.FIELD})
	/**
	 * Application property is secret (should not show up on the Application Configuration page).
	 */
	public static @interface Secret {
	}
	
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(value={ElementType.FIELD})
	/**
	 * Application property cannot be changed on the Application Configuration page.
	 */
	public static @interface ReadOnly {
	}
	
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(value={ElementType.FIELD})
	/**
	 * UniTime version where the application property was introduced.
	 */
	static @interface Since {
		double value();
	}
	
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(value={ElementType.FIELD})
	/**
	 * Short description of the application property.
	 */
	static @interface Description {
		String value();
	}
	
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(value={ElementType.FIELD})
	/**
	 * Default value of the application property.
	 */
	static @interface DefaultValue {
		String value();
	}
	
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(value={ElementType.FIELD})
	/**
	 * Type of the application property.
	 */
	static @interface Type {
		Class value() default String.class;
	}
	
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(value={ElementType.FIELD})
	/**
	 * Possible values of the application property.
	 */
	static @interface Values {
		String[] value();
	}
	
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(value={ElementType.FIELD})
	/**
	 * Old key (or keys) of the application property.
	 */
	static @interface Replaces {
		String[] value();
	}
	
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(value={ElementType.FIELD})
	/**
	 * Description of the parameter (%) in the property's key.
	 */
	static @interface Parameter {
		String value();
	}
	
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(value={ElementType.FIELD})
	/**
	 * If the property is class, interface (or abstract class) that the given class must implement (extend).
	 */
	static @interface Implements {
		Class value();
	}
	
	@Override
	public String toString() {
		String description = description();
		String defaultValue = defaultValue();
		return (description == null ? "" : "# " + description + "\n") + key() + "=" + (defaultValue == null ? "" : defaultValue);
	}
	
	public static void main(String[] args) {
		for (ApplicationProperty p: ApplicationProperty.values()) {
			System.out.println();
			if (p.description() != null)
				System.out.println("# " + p.description());
			
			if (p.implementation() != null)
				System.out.println("# Implements: " + p.implementation().getName());
			else if (p.type() != null && !p.type().equals(String.class))
				System.out.println("# Type: " + p.type().getSimpleName().toLowerCase());
			
			if (p.reference() != null)
				System.out.println("# Parameter: " + p.reference());
			
			if (p.replaces() != null)
				for (String r: p.replaces())
					System.out.println("# Replaces: " + r);

			if (p.since() != null)
				System.out.println("# Since " + p.since());

			if (p.isDeprecated())
				System.out.println("# Deprecated");

			String value = p.value();
			if (p.defaultValue() != null && !p.isSecret() && !p.defaultValue().equals(value))
				System.out.println("# Default: " + p.defaultValue());
			
			if (value == null)
				System.out.println("#" + p.key() + "=" + (p.isSecret() ? "****" : ""));
			else
				System.out.println(p.key() + "=" + (p.isSecret() ? "****" : value));
		}
	}
}
