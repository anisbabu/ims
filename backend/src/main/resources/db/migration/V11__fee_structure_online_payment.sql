-- Fee structures (per year+grade fee heads for bulk generation) and online payments.

create table fee_structure (
    id               uuid primary key,
    created_at       timestamptz,
    updated_at       timestamptz,
    created_by       uuid,
    updated_by       uuid,
    version          bigint,
    deleted          boolean        not null default false,
    institute_id     uuid           not null,
    academic_year_id uuid           not null,
    grade_id         uuid           not null,
    title            varchar(128)   not null,
    amount           numeric(12, 2) not null,
    due_date         date,
    constraint fk_fee_structure_institute foreign key (institute_id) references institute (id),
    constraint fk_fee_structure_year foreign key (academic_year_id) references academic_year (id),
    constraint fk_fee_structure_grade foreign key (grade_id) references grade (id)
);
create index idx_fee_structure_institute on fee_structure (institute_id);
create unique index uq_fee_structure_head on fee_structure (institute_id, academic_year_id, grade_id, title)
    where deleted = false;

create table online_payment (
    id           uuid primary key,
    created_at   timestamptz,
    updated_at   timestamptz,
    created_by   uuid,
    updated_by   uuid,
    version      bigint,
    deleted      boolean        not null default false,
    institute_id uuid           not null,
    fee_id       uuid           not null,
    student_id   uuid           not null,
    user_id      uuid           not null,
    amount       numeric(12, 2) not null,
    provider     varchar(32)    not null,
    status       varchar(16)    not null,
    reference    varchar(128),
    paid_at      timestamptz,
    constraint fk_online_payment_institute foreign key (institute_id) references institute (id),
    constraint fk_online_payment_fee foreign key (fee_id) references fee (id),
    constraint fk_online_payment_user foreign key (user_id) references app_user (id)
);
create index idx_online_payment_institute on online_payment (institute_id);
create index idx_online_payment_user on online_payment (user_id);
