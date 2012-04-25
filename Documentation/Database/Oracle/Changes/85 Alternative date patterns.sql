/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC
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

create table date_pattern_pref (
	uniqueid number(20,0) constraint nn_date_patt_pref_uniqueid not null,
	owner_id number(20,0) constraint nn_date_patt_pref_owner not null,
	pref_level_id number(20,0) constraint nn_date_patt_pref_pref_level not null,
	date_pattern_id number(20,0) constraint nn_date_patt_pref_date_pat not null
	);
alter table date_pattern_pref add constraint pk_date_pattern_pref primary key (uniqueid);

create table date_pattern_parent (
	date_pattern_id number(20,0) constraint nn_datepattparent_datepatt_id not null,
	parent_id number(20,0) constraint nn_date_patt_parent_parent_id not null
	);

alter table date_pattern_parent add constraint pk_date_pattern_parent primary key (date_pattern_id,parent_id);

alter table date_pattern_pref add constraint fk_datepatt_pref_pref_level foreign key (pref_level_id)
	references preference_level (uniqueid) on delete cascade;

alter table date_pattern_pref add constraint fk_datepatt_pref_date_pat foreign key (date_pattern_id)
	references date_pattern (uniqueid) on delete cascade;

alter table date_pattern_parent add constraint fk_date_patt_parent_parent foreign key (parent_id)
	references date_pattern (uniqueid) on delete cascade;

alter table date_pattern_parent add constraint fk_date_patt_parent_date_patt foreign key (date_pattern_id)
	references date_pattern (uniqueid) on delete cascade;

/*
 * Update database version
 */

update application_config set value='85' where name='tmtbl.db.version';

commit;
