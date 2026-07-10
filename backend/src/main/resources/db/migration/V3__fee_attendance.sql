-- Fees (charges + payments) and daily attendance.

create table fee (
    id               uuid primary key,
    created_at       timestamptz,
    updated_at       timestamptz,
    created_by       uuid,
    updated_by       uuid,
    version          bigint,
    institute_id     uuid           not null,
    student_id       uuid           not null,
    academic_year_id uuid,
    title            varchar(128)   not null,
    amount           numeric(12, 2) not null,
    paid_amount      numeric(12, 2) not null,
    due_date         date,
    status           varchar(16)    not null,
    constraint fk_fee_institute foreign key (institute_id) references institute (id),
    constraint fk_fee_student foreign key (student_id) references student (id),
    constraint fk_fee_year foreign key (academic_year_id) references academic_year (id)
);

create table payment (
    id            uuid primary key,
    created_at    timestamptz,
    updated_at    timestamptz,
    created_by    uuid,
    updated_by    uuid,
    version       bigint,
    institute_id  uuid           not null,
    fee_id        uuid           not null,
    student_id    uuid           not null,
    amount        numeric(12, 2) not null,
    method        varchar(16)    not null,
    reference     varchar(128),
    paid_on       date,
    constraint fk_payment_institute foreign key (institute_id) references institute (id),
    constraint fk_payment_fee foreign key (fee_id) references fee (id),
    constraint fk_payment_student foreign key (student_id) references student (id)
);

create table attendance (
    id            uuid primary key,
    created_at    timestamptz,
    updated_at    timestamptz,
    created_by    uuid,
    updated_by    uuid,
    version       bigint,
    institute_id  uuid        not null,
    student_id    uuid        not null,
    section_id    uuid,
    att_date      date        not null,
    status        varchar(16) not null,
    remarks       varchar(255),
    constraint fk_att_institute foreign key (institute_id) references institute (id),
    constraint fk_att_student foreign key (student_id) references student (id),
    constraint fk_att_section foreign key (section_id) references section (id),
    constraint uq_att unique (student_id, att_date)
);

create index idx_fee_institute on fee (institute_id);
create index idx_fee_student on fee (student_id);
create index idx_payment_institute on payment (institute_id);
create index idx_payment_fee on payment (fee_id);
create index idx_attendance_institute on attendance (institute_id);
create index idx_attendance_date on attendance (att_date);
create index idx_attendance_student on attendance (student_id);
