/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC
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

insert into distribution_type (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref) values 
	(ref_table_seq.nextval, 'MAX_HRS_DAY(6)', 'At Most 6 Hours A Day', 0, 39, '210R', 'Classes are to be placed in a way that there is no more than six hours in any day.', 'At Most 6 Hrs', 1, 0); 


insert into distribution_type (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref) values 
	(ref_table_seq.nextval, 'MAX_HRS_DAY(7)', 'At Most 7 Hours A Day', 0, 40, '210R', 'Classes are to be placed in a way that there is no more than seven hours in any day.', 'At Most 7 Hrs', 1, 0); 

insert into distribution_type (uniqueid, reference, label, sequencing_required, req_id, allowed_pref, description, abbreviation, instructor_pref, exam_pref) values 
	(ref_table_seq.nextval, 'MAX_HRS_DAY(8)', 'At Most 8 Hours A Day', 0, 41, '210R', 'Classes are to be placed in a way that there is no more than eight hours in any day.', 'At Most 8 Hrs', 1, 0); 

/*
 * Update database version
 */

update application_config set value='72' where name='tmtbl.db.version';

commit;
