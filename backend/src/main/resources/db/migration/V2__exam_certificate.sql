-- Exam / evaluation, marks, and certificates.

create table subject (
    id            uuid primary key,
    created_at    timestamptz,
    updated_at    timestamptz,
    created_by    uuid,
    updated_by    uuid,
    version       bigint,
    institute_id  uuid         not null,
    name          varchar(128) not null,
    code          varchar(32),
    constraint fk_subject_institute foreign key (institute_id) references institute (id),
    constraint uq_subject_name unique (institute_id, name)
);

create table exam_type (
    id             uuid primary key,
    created_at     timestamptz,
    updated_at     timestamptz,
    created_by     uuid,
    updated_by     uuid,
    version        bigint,
    institute_id   uuid        not null,
    name           varchar(64) not null,
    weight_percent integer,
    constraint fk_examtype_institute foreign key (institute_id) references institute (id),
    constraint uq_examtype_name unique (institute_id, name)
);

create table exam (
    id               uuid primary key,
    created_at       timestamptz,
    updated_at       timestamptz,
    created_by       uuid,
    updated_by       uuid,
    version          bigint,
    institute_id     uuid         not null,
    name             varchar(128) not null,
    exam_type_id     uuid         not null,
    academic_year_id uuid         not null,
    grade_id         uuid,
    start_date       date,
    end_date         date,
    status           varchar(16)  not null,
    constraint fk_exam_institute foreign key (institute_id) references institute (id),
    constraint fk_exam_type foreign key (exam_type_id) references exam_type (id),
    constraint fk_exam_year foreign key (academic_year_id) references academic_year (id),
    constraint fk_exam_grade foreign key (grade_id) references grade (id)
);

create table mark (
    id             uuid primary key,
    created_at     timestamptz,
    updated_at     timestamptz,
    created_by     uuid,
    updated_by     uuid,
    version        bigint,
    institute_id   uuid          not null,
    exam_id        uuid          not null,
    student_id     uuid          not null,
    subject_id     uuid          not null,
    max_marks      numeric(6, 2) not null,
    obtained_marks numeric(6, 2) not null,
    remarks        varchar(255),
    constraint fk_mark_institute foreign key (institute_id) references institute (id),
    constraint fk_mark_exam foreign key (exam_id) references exam (id),
    constraint fk_mark_student foreign key (student_id) references student (id),
    constraint fk_mark_subject foreign key (subject_id) references subject (id),
    constraint uq_mark unique (exam_id, student_id, subject_id)
);

create table certificate (
    id            uuid primary key,
    created_at    timestamptz,
    updated_at    timestamptz,
    created_by    uuid,
    updated_by    uuid,
    version       bigint,
    institute_id  uuid         not null,
    student_id    uuid         not null,
    type          varchar(24)  not null,
    serial_no     varchar(64),
    title         varchar(255) not null,
    issue_date    date,
    content       text,
    constraint fk_cert_institute foreign key (institute_id) references institute (id),
    constraint fk_cert_student foreign key (student_id) references student (id),
    constraint uq_cert_serial unique (institute_id, serial_no)
);

create index idx_subject_institute on subject (institute_id);
create index idx_examtype_institute on exam_type (institute_id);
create index idx_exam_institute on exam (institute_id);
create index idx_mark_institute on mark (institute_id);
create index idx_mark_exam on mark (exam_id);
create index idx_certificate_institute on certificate (institute_id);
create index idx_certificate_student on certificate (student_id);
