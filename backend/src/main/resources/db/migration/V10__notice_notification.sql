-- Notice board and in-app notifications.

create table notice (
    id           uuid primary key,
    created_at   timestamptz,
    updated_at   timestamptz,
    created_by   uuid,
    updated_by   uuid,
    version      bigint,
    deleted      boolean      not null default false,
    institute_id uuid         not null,
    title        varchar(255) not null,
    body         text,
    audience     varchar(16)  not null,
    grade_id     uuid,
    section_id   uuid,
    publish_date date         not null,
    expires_on   date,
    pinned       boolean      not null default false,
    constraint fk_notice_institute foreign key (institute_id) references institute (id),
    constraint fk_notice_grade foreign key (grade_id) references grade (id),
    constraint fk_notice_section foreign key (section_id) references section (id)
);
create index idx_notice_institute on notice (institute_id);

create table notification (
    id           uuid primary key,
    created_at   timestamptz,
    updated_at   timestamptz,
    created_by   uuid,
    updated_by   uuid,
    version      bigint,
    deleted      boolean      not null default false,
    institute_id uuid         not null,
    user_id      uuid         not null,
    type         varchar(24)  not null,
    title        varchar(255) not null,
    body         text,
    link         varchar(255),
    read_at      timestamptz,
    constraint fk_notification_institute foreign key (institute_id) references institute (id),
    constraint fk_notification_user foreign key (user_id) references app_user (id)
);
create index idx_notification_institute on notification (institute_id);
create index idx_notification_user_unread on notification (user_id) where read_at is null and deleted = false;
