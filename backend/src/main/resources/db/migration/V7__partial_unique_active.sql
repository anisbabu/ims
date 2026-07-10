-- Make uniqueness apply only to non-archived rows, so a unique key (email, code,
-- reg no, …) can be reused after the holding record is soft-deleted.
-- Drop each full unique constraint and recreate it as a partial unique index
-- filtered on deleted = false.

-- institute.code  (auto-named constraint institute_code_key)
alter table institute drop constraint if exists institute_code_key;
create unique index uq_institute_code_active on institute (code) where deleted = false;

-- app_user.email  (auto-named constraint app_user_email_key)
alter table app_user drop constraint if exists app_user_email_key;
create unique index uq_user_email_active on app_user (lower(email)) where deleted = false;

alter table academic_year drop constraint if exists uq_year_institute_name;
create unique index uq_year_active on academic_year (institute_id, name) where deleted = false;

alter table grade drop constraint if exists uq_grade_institute_name;
create unique index uq_grade_active on grade (institute_id, name) where deleted = false;

alter table section drop constraint if exists uq_section;
create unique index uq_section_active on section (institute_id, grade_id, name) where deleted = false;

alter table student drop constraint if exists uq_student_reg;
create unique index uq_student_reg_active on student (institute_id, reg_no) where deleted = false;

alter table student_guardian drop constraint if exists uq_sg;
create unique index uq_sg_active on student_guardian (student_id, guardian_id) where deleted = false;

alter table admission drop constraint if exists uq_adm_no;
create unique index uq_adm_no_active on admission (institute_id, admission_no) where deleted = false;

alter table subject drop constraint if exists uq_subject_name;
create unique index uq_subject_active on subject (institute_id, name) where deleted = false;

alter table exam_type drop constraint if exists uq_examtype_name;
create unique index uq_examtype_active on exam_type (institute_id, name) where deleted = false;

alter table mark drop constraint if exists uq_mark;
create unique index uq_mark_active on mark (exam_id, student_id, subject_id) where deleted = false;

alter table certificate drop constraint if exists uq_cert_serial;
create unique index uq_cert_serial_active on certificate (institute_id, serial_no) where deleted = false;

alter table attendance drop constraint if exists uq_att;
create unique index uq_att_active on attendance (student_id, att_date) where deleted = false;

alter table financial_year drop constraint if exists uq_fy_name;
create unique index uq_fy_active on financial_year (institute_id, name) where deleted = false;

alter table account drop constraint if exists uq_account_code;
create unique index uq_account_code_active on account (institute_id, code) where deleted = false;
