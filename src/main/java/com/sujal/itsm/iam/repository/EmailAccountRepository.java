package com.sujal.itsm.iam.repository;

import com.sujal.itsm.iam.enums.AccountStatus;
import com.sujal.itsm.iam.model.EmailAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmailAccountRepository extends JpaRepository<EmailAccount, Long> {

    long countByStatus(AccountStatus status);

    long countByIsMfaEnabledFalseAndStatus(AccountStatus status);

    Iterable<EmailAccount> findByStatus(AccountStatus status);

    boolean existsByEmailAddress(String emailAddress);

    EmailAccount findByUserId(Long userId);

    // ✅ THIS IS THE CRUCIAL FIX: Forces Hibernate to load the User and License immediately
    @Query("SELECT e FROM EmailAccount e JOIN FETCH e.user LEFT JOIN FETCH e.license")
    List<EmailAccount> findAllWithUserAndLicense();
}