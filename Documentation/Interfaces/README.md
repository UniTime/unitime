# XML Interfaces

Following XML interfaces reflect the UniTime data model. Data may be imported once (for each term) and/or periodically updated by a nightly batch process. It is also possible to maintain most of these data directly within UniTime, see Administration > Academic Sessions menu. Usually, records that have external ids (attribute externalId) are expected to be maintained in an external system, records without external ids are fully editable in UniTime.

Use [Data Exchange](https://help.unitime.org/data-exchange) page (menu Administration > Academic Sessions > Data Exchange) to import or export an XML. Alternatively, the [Data Exchange API](https://help.unitime.org/manuals/api#9data-exchange) can be used to import or export an XML.

## XML formats for import of academic session related data

* Academic session setup [[XML](Examples/sessionSetup.xml)] [[DTD](AcademicSessionSetup.dtd)]
    * Academic session, timetabling managers, departments, subject areas, solver groups, date patterns, time patterns, examination periods,
    academic areas, academic classifications, majors, minors, student groups, and accommodations.
    
* Other resources
    * External buildings and rooms [[XML](Examples/buildingRoomImport.xml)] [[DTD](BuildingRoom.dtd)]
    * Rooms [[XML](Examples/rooms.xml)] [[DTD](Rooms.dtd)] *-- Replaces the previous/external buildings and rooms import. Added in UniTime 4.9.168.*
    * Travel times [[XML](Examples/travelTimes.xml)] [[DTD](TravelTimes.dtd)]
    * Room Sharing [[XML](Examples/roomSharing.xml)] [[DTD](RoomSharing.dtd)]
    * Staff [[XML](Examples/staffImport.xml)] [[DTD](Staff.dtd)]
    * Instructors [[XML](Examples/instructors.xml)] [[DTD](Instructors.dtd)] *-- Added in UniTime 4.9.168.*
        * Including instructor preferences [[XML](Examples/instructorsWithPrefs.xml)]
    * Course catalog [[XML](Examples/courseCatalogImport.xml)] [[DTD](CourseCatalog.dtd)]
    * Event Load [[XML](Examples/eventLoad.xml)] [[DTD](EventLoad.dtd)] *-- One time load for Special Events and Course Related Events.*

#### Academic session setup
The XML import can be used to import/update the academic session, timetabling managers, departments, subject areas, solver groups, date patterns, time patterns, examination periods, academic areas, academic classifications, majors, minors, student groups, and/or student accommodations.
* Only sections that exist in the XML are imported.
* Set `incremental="true"` on the section for incremental update (inserting/updating only items listed in the file).
* Academic areas, classification, and majors are needed for curriculum timetabling and for curriculum reservations. Student groups are useful for group reservations.

#### Buildings and rooms
Please note that the [Buildings](https://help.unitime.org/buildings) and [Rooms](https://help.unitime.org/rooms) are not updated directly with the external buildings and rooms XML import, but the [Update Data](https://help.unitime.org/buildings#update-data) needs to be used to update the buildings and rooms.

*New in **UniTime 4.9.168** or later:* Use the new Rooms [[XML](Examples/rooms.xml)] [[DTD](Rooms.dtd)] to import buildings and rooms directly. This import optionally includes:

* Buildings
* Room pictures
* Room sharing
* Event-related properties (event status, note, break time, email, availability)
* Examination-related properties (examination capacity, examination types, period preferences)

It also includes all room groups and features (both global and departmental; creating the corresponding room group or feature if it does not already exist), and room partitions. Use `incremental="true"` on the root element to enable incremental mode. In this mode, only the buildings and/or rooms present in the file are created or updated.

#### Staff
Please note that this import does **NOT** update the [Instructors](https://help.unitime.org/instructors) directly. The imported records will show in the [People Lookup](https://help.unitime.org/people-lookup) dialog and can be pull into Instructors using the [Manage Instructor List](https://help.unitime.org/manage-instructor-list) page.

#### Instructors

*New in **UniTime 4.9.168** or later:* Use the new Instructors [[XML](Examples/instructors.xml)] [[DTD](Instructors.dtd)] to import instructors directly.

The import optionally includes instructor preferences, which can also be imported separately using the Preferences XML listed below. Use `incremental="true"` on the root element to enable incremental mode. In this mode, only the instructors present in the file are created or updated.

#### Course catalog
Please note that course catalog information does not get displayed directly in UniTime. The data can be used to create new courses during the [Roll Forward](https://help.unitime.org/roll-forward-session), Add New Course Offerings section.

### Deprecated XML exports and imports
The following APIs are still working, but are marked as **deprecated** (to be removed in future UniTime releases). If possible, please use the **Academic session setup** XML import instead.
* Student properties
    * Academic areas [[XML](Examples/academicAreaImport.xml)] [[DTD](AcademicArea.dtd)]
    * Academic classifications [[XML](Examples/academicClassificationImport.xml)] [[DTD](AcademicClassification.dtd)]
    * Majors [[XML](Examples/posMajorImport.xml)] [[DTD](PosMajor.dtd)]
    * Minors [[XML](Examples/posMinorImport.xml)] [[DTD](PosMinor.dtd)]
    * Student Groups [[XML](Examples/studentGroupImport.xml)] [[DTD](StudentGroup.dtd)]
    * Student Accommodations [[XML](Examples/studentAccomodationImport.xml)] [[DTD](StudentAccomodation.dtd)] *(disabilities etc.)*
* Other resources
    * Departments [[XML](Examples/departmentImport.xml)] [[DTD](Department.dtd)]
    * Subject areas [[XML](Examples/subjectAreaImport.xml)] [[DTD](SubjectArea.dtd)]

## XML formats for student data import and export

* Students [[XML](Examples/studentInfoImport.xml)] [[DTD](Student.dtd)]
    * Student course requests [[XML](Examples/woebegonStudents.xml)] [[DTD](StudentSectioning.dtd)] *(pre-registration)*
    * Student class enrollments [[XML](Examples/studentEnrollmentImport.xml)] [[DTD](StudentEnrollment.dtd)]
* Curricula [[XML](Examples/curricula.xml)] [[DTD](Curricula_3_2.dtd)]
* Last-like term student course demands [[XML](Examples/studentCrsDemandImport.xml)] [[DTD](StudentCourse.dtd)] *(import only)*

[Course timetabling](https://help.unitime.org/course-timetabling) can be either based on curriculum information, last-like term student course demands, student course requests, or the actual student class enrollments. [Examination timetabling](https://help.unitime.org/examination-timetabling) requires student class enrollments. If both [course timetabling](https://help.unitime.org/course-timetabling) and [student scheduling](https://help.unitime.org/student-scheduling) is done in UniTime, at the very least, students need to be loaded from some external system.

#### Students & Student course requests
Use `incremental="true"` on the root element to enable incremental mode. In this mode, only the students present in the file are created or updated.

#### Curricula
Use `incremental="true"` on the root element to enable incremental mode. In this mode, only the curricula present in the file are created or updated.

## XML formats for course data and timetable import / export

* Course offerings [[XML](Examples/courseOfferingExport.xml)] [[DTD](CourseOfferingExport.dtd)] *(including the course structure and time/room assignments)*
    * Including final/evening exams [[XML](Examples/courseOfferingWithExamsExport.xml)] *(including exam definitions and period/room assignments)*
    * Import example [[XML](Examples/courseOfferingWithExamsImport.xml)] *(including basis of the course structure, class and exam assignments)*
* Timetable [[XML](Examples/courseTimetableExport.xml)] [[DTD](CourseTimetable.dtd)] *(no course structure, only time/room assignments)*
    * Import example [[XML](Examples/courseTimetableImport.xml)]
* Reservations [[XML](Examples/reservations.xml)] [[DTD](Reservations.dtd)]
* Preferences [[XML](Examples/preferences.xml)] [[DTD](Preferences.dtd)]

#### Course offerings
Use `incremental="true"` on the root element to enable incremental mode. In this mode, only the offerings present in the file are created or updated.

#### Preferences
The preferences import will only update departments, instructors, subparts, and/or classes that are present in the file, resetting their preferences to what is listed in the file (including the option to clear their preferences when no preferences are listed).

#### Reservations
Use `incremental="true"` on the root element to enable incremental mode. In this mode, only the offerings which do have reservations present in the file are updated (have their reservations reset to what is present in the XML file).

## XML formats for (online) data interchange

* Room availability [[Request XML](Examples/roomAvailabilityRequest.xml)] [[Response XML](Examples/roomAvailabilityResponse.xml)] [[DTD](RoomAvailability.dtd)]
    *(see [Custom Room Availability](custom-room-availability))*

## Other XML formats:
* Permissions [[XML](Examples/permissions.xml)] [[DTD](Permissions.dtd)]
* Custom Reports [[Examples](../Reports)] [[XML](Examples/hqlReport.xml)] [[DTD](Reports.dtd)] *(see [HQL Reports](https://help.unitime.org/hql-reports) page)*
* Scripts [[Examples](../Scripts)] [[XML](Examples/exampleScripts.xml)] [[DTD](Script.dtd)] *(see [Scripts](https://help.unitime.org/scripts) page)*
* Point in time data [[DTD](PointInTimeData.dtd)]
* Scheduled tasks [[XML](Examples/tasks.xml)] [[DTD](Tasks.dtd)] *(added in UniTime 4.9.168, for the [Task Scheduler](https://help.unitime.org/task-scheduler))*
* Menu [[XML](../../JavaSource/menu-custom.xml)] [[DTD](menu.dtd)]

#### Permissions
Use `incremental="true"` on the root element to enable incremental mode. In this mode, only listed roles will be created/updated.

#### Menu
Use this for custom menus.
* Unlike all the other XMLs, this file **cannot be imported** using the [Data Exchange](https://help.unitime.org/data-exchange) page!
* The custom menu XML file needs to be placed somewhere UniTime can read it and the `unitime.menu.custom` property needs to point to the file.

## Other APIs

Besides the above XML interfaces, there are additional RESTful interfaces. See [UniTime APIs](https://help.unitime.org/manuals/api) for more details. Also, the [Scripts](https://help.unitime.org/scripts) can be used to create additional custom imports and exports.