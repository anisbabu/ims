-- Double-entry accounting: financial years, chart of accounts, journals.

create table financial_year (
    id            uuid primary key,
    created_at    timestamptz,
    updated_at    timestamptz,
    created_by    uuid,
    updated_by    uuid,
    version       bigint,
    institute_id  uuid        not null,
    name          varchar(64) not null,
    start_date    date        not null,
    end_date      date        not null,
    is_current    boolean     not null,
    closed        boolean     not null,
    constraint fk_fy_institute foreign key (institute_id) references institute (id),
    constraint uq_fy_name unique (institute_id, name)
);

create table account (
    id            uuid primary key,
    created_at    timestamptz,
    updated_at    timestamptz,
    created_by    uuid,
    updated_by    uuid,
    version       bigint,
    institute_id  uuid         not null,
    code          varchar(32)  not null,
    name          varchar(128) not null,
    type          varchar(16)  not null,
    parent_id     uuid,
    system_key    varchar(32),
    active        boolean      not null,
    constraint fk_account_institute foreign key (institute_id) references institute (id),
    constraint fk_account_parent foreign key (parent_id) references account (id),
    constraint uq_account_code unique (institute_id, code)
);

create table journal_entry (
    id                uuid primary key,
    created_at        timestamptz,
    updated_at        timestamptz,
    created_by        uuid,
    updated_by        uuid,
    version           bigint,
    institute_id      uuid        not null,
    financial_year_id uuid        not null,
    entry_date        date        not null,
    reference         varchar(64),
    narration         varchar(255),
    source            varchar(8)  not null,
    source_type       varchar(32),
    source_id         uuid,
    posted            boolean     not null,
    constraint fk_je_institute foreign key (institute_id) references institute (id),
    constraint fk_je_fy foreign key (financial_year_id) references financial_year (id)
);

create table journal_line (
    id                uuid primary key,
    created_at        timestamptz,
    updated_at        timestamptz,
    created_by        uuid,
    updated_by        uuid,
    version           bigint,
    institute_id      uuid           not null,
    journal_entry_id  uuid           not null,
    account_id        uuid           not null,
    debit             numeric(14, 2) not null,
    credit            numeric(14, 2) not null,
    memo              varchar(255),
    constraint fk_jl_institute foreign key (institute_id) references institute (id),
    constraint fk_jl_entry foreign key (journal_entry_id) references journal_entry (id),
    constraint fk_jl_account foreign key (account_id) references account (id)
);

create index idx_fy_institute on financial_year (institute_id);
create index idx_account_institute on account (institute_id);
create index idx_account_system_key on account (institute_id, system_key);
create index idx_je_institute on journal_entry (institute_id);
create index idx_je_fy on journal_entry (financial_year_id);
create index idx_jl_institute on journal_line (institute_id);
create index idx_jl_entry on journal_line (journal_entry_id);
create index idx_jl_account on journal_line (account_id);
