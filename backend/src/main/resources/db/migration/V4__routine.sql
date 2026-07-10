-- Class & exam routine (timetable) slots.

create table routine_slot (
    id            uuid primary key,
    created_at    timestamptz,
    updated_at    timestamptz,
    created_by    uuid,
    updated_by    uuid,
    version       bigint,
    institute_id  uuid        not null,
    kind          varchar(8)  not null,
    section_id    uuid,
    grade_id      uuid,
    subject_id    uuid,
    teacher_id    uuid,
    exam_id       uuid,
    day_of_week   varchar(12),
    slot_date     date,
    start_time    time        not null,
    end_time      time        not null,
    venue         varchar(128),
    label         varchar(128),
    constraint fk_routine_institute foreign key (institute_id) references institute (id),
    constraint fk_routine_section foreign key (section_id) references section (id),
    constraint fk_routine_grade foreign key (grade_id) references grade (id),
    constraint fk_routine_subject foreign key (subject_id) references subject (id),
    constraint fk_routine_teacher foreign key (teacher_id) references teacher (id),
    constraint fk_routine_exam foreign key (exam_id) references exam (id)
);

create index idx_routine_institute on routine_slot (institute_id);
create index idx_routine_kind_section on routine_slot (kind, section_id);
create index idx_routine_kind_exam on routine_slot (kind, exam_id);
