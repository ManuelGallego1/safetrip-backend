package com.safetrip.backend.domain.repository;

import com.safetrip.backend.domain.model.PolicyPerson;

import java.util.List;
import java.util.Optional;

public interface PolicyPersonRepository {

    PolicyPerson save(PolicyPerson policyPerson);

    Optional<PolicyPerson> findById(Long policyPersonId);

    List<PolicyPerson> findByPolicyId(Long policyId);

    List<PolicyPerson> findByPersonId(Long personId);

    List<PolicyPerson> findAll();

    void delete(Long policyPersonId);
}