package com.ims.accounting;

/**
 * Fundamental account classification. normalDebit tells which side increases the
 * account (assets & expenses are debit-normal; the rest are credit-normal).
 */
public enum AccountType {
    ASSET(true),
    LIABILITY(false),
    EQUITY(false),
    INCOME(false),
    EXPENSE(true);

    private final boolean normalDebit;

    AccountType(boolean normalDebit) {
        this.normalDebit = normalDebit;
    }

    public boolean isNormalDebit() {
        return normalDebit;
    }
}
