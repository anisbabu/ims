-- Library, hostel and transport modules. All tables carry the soft-delete flag
-- and use partial unique indexes (deleted = false) like the rest of the schema.

-- ===== Library =====
create table book (
    id               uuid primary key,
    created_at       timestamptz,
    updated_at       timestamptz,
    created_by       uuid,
    updated_by       uuid,
    version          bigint,
    deleted          boolean not null default false,
    institute_id     uuid         not null,
    title            varchar(255) not null,
    author           varchar(255),
    isbn             varchar(32),
    category         varchar(64),
    shelf            varchar(32),
    total_copies     integer      not null,
    available_copies integer      not null,
    constraint fk_book_institute foreign key (institute_id) references institute (id)
);

create table book_issue (
    id           uuid primary key,
    created_at   timestamptz,
    updated_at   timestamptz,
    created_by   uuid,
    updated_by   uuid,
    version      bigint,
    deleted      boolean not null default false,
    institute_id uuid          not null,
    book_id      uuid          not null,
    student_id   uuid          not null,
    issue_date   date          not null,
    due_date     date,
    return_date  date,
    status       varchar(16)   not null,
    fine         numeric(10, 2) not null,
    constraint fk_issue_institute foreign key (institute_id) references institute (id),
    constraint fk_issue_book foreign key (book_id) references book (id),
    constraint fk_issue_student foreign key (student_id) references student (id)
);

-- ===== Hostel =====
create table hostel (
    id           uuid primary key,
    created_at   timestamptz,
    updated_at   timestamptz,
    created_by   uuid,
    updated_by   uuid,
    version      bigint,
    deleted      boolean not null default false,
    institute_id uuid         not null,
    name         varchar(128) not null,
    type         varchar(8)   not null,
    address      varchar(255),
    warden_id    uuid,
    constraint fk_hostel_institute foreign key (institute_id) references institute (id)
);

create table hostel_room (
    id           uuid primary key,
    created_at   timestamptz,
    updated_at   timestamptz,
    created_by   uuid,
    updated_by   uuid,
    version      bigint,
    deleted      boolean not null default false,
    institute_id uuid        not null,
    hostel_id    uuid        not null,
    room_no      varchar(32) not null,
    capacity     integer     not null,
    occupied     integer     not null,
    constraint fk_room_institute foreign key (institute_id) references institute (id),
    constraint fk_room_hostel foreign key (hostel_id) references hostel (id)
);

create table hostel_allocation (
    id             uuid primary key,
    created_at     timestamptz,
    updated_at     timestamptz,
    created_by     uuid,
    updated_by     uuid,
    version        bigint,
    deleted        boolean not null default false,
    institute_id   uuid        not null,
    student_id     uuid        not null,
    hostel_id      uuid        not null,
    room_id        uuid        not null,
    allocated_date date        not null,
    vacated_date   date,
    status         varchar(16) not null,
    constraint fk_alloc_institute foreign key (institute_id) references institute (id),
    constraint fk_alloc_student foreign key (student_id) references student (id),
    constraint fk_alloc_hostel foreign key (hostel_id) references hostel (id),
    constraint fk_alloc_room foreign key (room_id) references hostel_room (id)
);

-- ===== Transport =====
create table vehicle (
    id           uuid primary key,
    created_at   timestamptz,
    updated_at   timestamptz,
    created_by   uuid,
    updated_by   uuid,
    version      bigint,
    deleted      boolean not null default false,
    institute_id uuid        not null,
    reg_no       varchar(32) not null,
    model        varchar(255),
    capacity     integer     not null,
    driver_name  varchar(255),
    driver_phone varchar(32),
    constraint fk_vehicle_institute foreign key (institute_id) references institute (id)
);

create table transport_route (
    id           uuid primary key,
    created_at   timestamptz,
    updated_at   timestamptz,
    created_by   uuid,
    updated_by   uuid,
    version      bigint,
    deleted      boolean not null default false,
    institute_id uuid         not null,
    name         varchar(128) not null,
    stops        text,
    fare         numeric(10, 2),
    vehicle_id   uuid,
    constraint fk_route_institute foreign key (institute_id) references institute (id),
    constraint fk_route_vehicle foreign key (vehicle_id) references vehicle (id)
);

create table transport_assignment (
    id            uuid primary key,
    created_at    timestamptz,
    updated_at    timestamptz,
    created_by    uuid,
    updated_by    uuid,
    version       bigint,
    deleted       boolean not null default false,
    institute_id  uuid        not null,
    student_id    uuid        not null,
    route_id      uuid        not null,
    stop_name     varchar(255),
    assigned_date date        not null,
    end_date      date,
    status        varchar(16) not null,
    constraint fk_tassign_institute foreign key (institute_id) references institute (id),
    constraint fk_tassign_student foreign key (student_id) references student (id),
    constraint fk_tassign_route foreign key (route_id) references transport_route (id)
);

-- Partial unique indexes (active rows only)
create unique index uq_book_isbn_active on book (institute_id, isbn) where deleted = false;
create unique index uq_hostel_name_active on hostel (institute_id, name) where deleted = false;
create unique index uq_room_no_active on hostel_room (hostel_id, room_no) where deleted = false;
create unique index uq_vehicle_reg_active on vehicle (institute_id, reg_no) where deleted = false;
create unique index uq_route_name_active on transport_route (institute_id, name) where deleted = false;

-- Tenant scoping indexes
create index idx_book_institute on book (institute_id);
create index idx_book_issue_institute on book_issue (institute_id);
create index idx_hostel_institute on hostel (institute_id);
create index idx_room_institute on hostel_room (institute_id);
create index idx_alloc_institute on hostel_allocation (institute_id);
create index idx_vehicle_institute on vehicle (institute_id);
create index idx_route_institute on transport_route (institute_id);
create index idx_tassign_institute on transport_assignment (institute_id);
