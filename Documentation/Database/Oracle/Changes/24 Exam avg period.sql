/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime.org
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/

/**
 * Add average period to exam
 */
 
alter table exam add avg_period number(10);

/**
 * Add organized flag to itype_desc
 */
 
alter table itype_desc add organized number(1);

update itype_desc set organized = 0;
update itype_desc set organized = 1 where abbv like 'Lec%';
update itype_desc set organized = 1 where abbv like 'Rec%';
update itype_desc set organized = 1 where abbv like 'Prsn%';
update itype_desc set organized = 1 where abbv like 'Lab%';
update itype_desc set organized = 1 where abbv like 'LabP%';
update itype_desc set organized = 1 where abbv like 'Stdo%';
 
alter table itype_desc modify organized add constraint nn_itype_desc_organized check (organized is not null);
 
 /**
  * Add email to departmental instructor
  */

alter table departmental_instructor add email varchar2(200);
  
/*
 * Update database version
 */

update application_config set value='24' where name='tmtbl.db.version';

commit;  