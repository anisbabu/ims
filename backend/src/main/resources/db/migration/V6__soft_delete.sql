-- Soft delete: every table gets a `deleted` flag. Hibernate @SoftDelete turns
-- removals into UPDATE deleted=true and filters deleted=false on all reads,
-- so records are archived for the life of the system, never physically removed.

alter table institute        add column deleted boolean not null default false;
alter table app_user         add column deleted boolean not null default false;
alter table academic_year    add column deleted boolean not null default false;
alter table grade            add column deleted boolean not null default false;
alter table section          add column deleted boolean not null default false;
alter table student          add column deleted boolean not null default false;
alter table teacher          add column deleted boolean not null default false;
alter table guardian         add column deleted boolean not null default false;
alter table student_guardian add column deleted boolean not null default false;
alter table admission        add column deleted boolean not null default false;
alter table subject          add column deleted boolean not null default false;
alter table exam_type        add column deleted boolean not null default false;
alter table exam             add column deleted boolean not null default false;
alter table mark             add column deleted boolean not null default false;
alter table certificate      add column deleted boolean not null default false;
alter table fee              add column deleted boolean not null default false;
alter table payment          add column deleted boolean not null default false;
alter table attendance       add column deleted boolean not null default false;
alter table routine_slot     add column deleted boolean not null default false;
alter table financial_year   add column deleted boolean not null default false;
alter table account          add column deleted boolean not null default false;
alter table journal_entry    add column deleted boolean not null default false;
alter table journal_line     add column deleted boolean not null default false;
