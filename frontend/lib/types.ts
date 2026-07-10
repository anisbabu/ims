export type Role =
  | "SUPER_ADMIN"
  | "INSTITUTE_ADMIN"
  | "TEACHER"
  | "STUDENT"
  | "GUARDIAN";

export interface Me {
  id: string;
  email: string;
  fullName: string;
  role: Role;
  instituteId: string | null;
  profileId: string | null;
}

export interface AppUser {
  id: string;
  email: string;
  fullName: string;
  role: Role;
  status: "ACTIVE" | "DISABLED";
  instituteId: string | null;
  profileId: string | null;
  createdAt: string;
}

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface Student {
  id: string;
  fullName: string;
  regNo?: string;
  rollNo?: string;
  gender?: string;
  phone?: string;
  email?: string;
  status: string;
}

export interface Teacher {
  id: string;
  fullName: string;
  designation: string;
  phone?: string;
  email?: string;
  status: string;
}

export interface Guardian {
  id: string;
  fullName: string;
  phone?: string;
  email?: string;
  occupation?: string;
}

export interface AcademicYear {
  id: string;
  name: string;
  startDate?: string;
  endDate?: string;
  current: boolean;
}

export interface Grade {
  id: string;
  name: string;
  orderNo: number;
}

export interface Section {
  id: string;
  gradeId: string;
  name: string;
  classTeacherId?: string;
  capacity: number;
}

export interface Admission {
  id: string;
  studentId: string;
  academicYearId: string;
  gradeId: string;
  sectionId?: string;
  admissionNo?: string;
  admissionDate?: string;
  status: string;
}

export const DESIGNATIONS = [
  "HEADMASTER",
  "PRINCIPAL",
  "VICE_PRINCIPAL",
  "CLASS_TEACHER",
  "PLAY",
  "CULTURAL",
  "MUSIC",
  "PT",
  "SUBJECT",
  "OTHER",
];

export const RELATIONS = ["FATHER", "MOTHER", "GUARDIAN", "OTHER"];
export const ADMISSION_STATUSES = ["APPLIED", "ENROLLED", "REJECTED", "WITHDRAWN"];

export interface Subject {
  id: string;
  name: string;
  code?: string;
}

export interface ExamType {
  id: string;
  name: string;
  weightPercent?: number;
}

export interface Exam {
  id: string;
  name: string;
  examTypeId: string;
  academicYearId: string;
  gradeId?: string;
  startDate?: string;
  endDate?: string;
  status: string;
}

export interface MarksheetLine {
  subjectId: string;
  subjectName: string;
  maxMarks: number;
  obtainedMarks: number;
  percent: number;
  letter: string;
  pass: boolean;
}

export interface Marksheet {
  examId: string;
  examName: string;
  studentId: string;
  studentName: string;
  lines: MarksheetLine[];
  totalMax: number;
  totalObtained: number;
  percent: number;
  gpa: number;
  letter: string;
  pass: boolean;
  position?: number;
}

export interface Certificate {
  id: string;
  studentId: string;
  type: string;
  serialNo?: string;
  title: string;
  issueDate?: string;
  content?: string;
}

export interface Fee {
  id: string;
  studentId: string;
  academicYearId?: string;
  title: string;
  amount: number;
  paidAmount: number;
  dueAmount: number;
  dueDate?: string;
  status: string;
}

export interface FeeSummary {
  studentId: string;
  totalBilled: number;
  totalPaid: number;
  totalDue: number;
  fees: Fee[];
}

export interface AttendanceRecord {
  id: string;
  studentId: string;
  sectionId?: string;
  date: string;
  status: string;
  remarks?: string;
}

export interface RoutineSlot {
  id: string;
  kind: "CLASS" | "EXAM";
  sectionId?: string;
  gradeId?: string;
  subjectId?: string;
  teacherId?: string;
  examId?: string;
  dayOfWeek?: string;
  slotDate?: string;
  startTime: string;
  endTime: string;
  venue?: string;
  label?: string;
}

export const WEEKDAYS = [
  "MONDAY",
  "TUESDAY",
  "WEDNESDAY",
  "THURSDAY",
  "FRIDAY",
  "SATURDAY",
  "SUNDAY",
];

export const PAYMENT_METHODS = ["CASH", "CARD", "BANK", "ONLINE", "OTHER"];
export const ATTENDANCE_STATUSES = ["PRESENT", "ABSENT", "LATE", "EXCUSED", "HOLIDAY"];

// ---- Accounting ----
export type AccountTypeT = "ASSET" | "LIABILITY" | "EQUITY" | "INCOME" | "EXPENSE";

export interface LedgerAccount {
  id: string;
  code: string;
  name: string;
  type: AccountTypeT;
  parentId?: string;
  systemKey?: string;
  active: boolean;
}

export interface FinancialYear {
  id: string;
  name: string;
  startDate: string;
  endDate: string;
  current: boolean;
  closed: boolean;
}

export interface JournalLineResp {
  id: string;
  accountId: string;
  accountCode: string;
  accountName: string;
  debit: number;
  credit: number;
  memo?: string;
}

export interface Journal {
  id: string;
  financialYearId: string;
  entryDate: string;
  reference?: string;
  narration?: string;
  source: "MANUAL" | "AUTO";
  sourceType?: string;
  sourceId?: string;
  posted: boolean;
  totalDebit: number;
  totalCredit: number;
  lines: JournalLineResp[];
}

export interface TrialBalanceRow {
  accountId: string;
  code: string;
  name: string;
  type: AccountTypeT;
  debit: number;
  credit: number;
}
export interface TrialBalance {
  financialYearId: string;
  rows: TrialBalanceRow[];
  totalDebit: number;
  totalCredit: number;
  balanced: boolean;
}

export interface ReportLine {
  accountId: string;
  code: string;
  name: string;
  amount: number;
}
export interface ProfitAndLoss {
  financialYearId: string;
  income: ReportLine[];
  totalIncome: number;
  expense: ReportLine[];
  totalExpense: number;
  netProfit: number;
}
export interface BalanceSheet {
  financialYearId: string;
  asOf: string;
  assets: ReportLine[];
  totalAssets: number;
  liabilities: ReportLine[];
  totalLiabilities: number;
  equity: ReportLine[];
  totalEquity: number;
  netProfit: number;
  totalLiabilitiesAndEquity: number;
  balanced: boolean;
}
export interface LedgerRow {
  date: string;
  journalEntryId: string;
  reference?: string;
  narration?: string;
  debit: number;
  credit: number;
  balance: number;
}
export interface AccountLedger {
  accountId: string;
  accountCode: string;
  accountName: string;
  type: AccountTypeT;
  openingBalance: number;
  rows: LedgerRow[];
  closingBalance: number;
}

export const ACCOUNT_TYPES = ["ASSET", "LIABILITY", "EQUITY", "INCOME", "EXPENSE"];

export const EXAM_STATUSES = ["SCHEDULED", "ONGOING", "COMPLETED", "PUBLISHED"];
export const CERTIFICATE_TYPES = [
  "MARKSHEET",
  "TRANSFER",
  "CHARACTER",
  "COMPLETION",
  "PARTICIPATION",
  "OTHER",
];
