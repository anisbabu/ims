-- Binary file store (profile photos), photo columns, and non-teaching employees.

create table stored_file (
    id           uuid primary key,
    created_at   timestamptz,
    updated_at   timestamptz,
    created_by   uuid,
    updated_by   uuid,
    version      bigint,
    deleted      boolean not null default false,
    institute_id uuid         not null,
    filename     varchar(255) not null,
    content_type varchar(128) not null,
    size_bytes   bigint       not null,
    data         bytea        not null,
    constraint fk_file_institute foreign key (institute_id) references institute (id)
);
create index idx_stored_file_institute on stored_file (institute_id);

alter table teacher  add column photo_url varchar(255);
alter table guardian add column photo_url varchar(255);

create table employee (
    id           uuid primary key,
    created_at   timestamptz,
    updated_at   timestamptz,
    created_by   uuid,
    updated_by   uuid,
    version      bigint,
    deleted      boolean not null default false,
    institute_id uuid         not null,
    full_name    varchar(255) not null,
    designation  varchar(64),
    dob          date,
    gender       varchar(16),
    phone        varchar(255),
    email        varchar(255),
    address      varchar(255),
    join_date    date,
    photo_url    varchar(255),
    status       varchar(16)  not null,
    constraint fk_employee_institute foreign key (institute_id) references institute (id)
);
create index idx_employee_institute on employee (institute_id);
