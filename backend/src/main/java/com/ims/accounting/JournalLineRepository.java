package com.ims.accounting;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JournalLineRepository extends JpaRepository<JournalLine, UUID> {

    List<JournalLine> findByJournalEntryId(UUID journalEntryId);

    /** Posted lines for a financial year, joined to their (posted) entry. */
    @Query("""
            select l from JournalLine l, JournalEntry e
            where l.journalEntryId = e.id and e.posted = true
              and e.financialYearId = :fyId
            """)
    List<JournalLine> findPostedByFinancialYear(@Param("fyId") UUID fyId);

    @Query("""
            select l from JournalLine l, JournalEntry e
            where l.journalEntryId = e.id and e.posted = true
              and e.financialYearId = :fyId and l.accountId = :accountId
            order by e.entryDate, e.createdAt
            """)
    List<JournalLine> findPostedByFinancialYearAndAccount(@Param("fyId") UUID fyId,
                                                          @Param("accountId") UUID accountId);
}
