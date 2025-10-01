package com.safetrip.backend.domain.repository;

import com.safetrip.backend.domain.model.Person;
import com.safetrip.backend.domain.model.enums.DocumentType;

import java.util.List;
import java.util.Optional;

public interface PersonRepository {

    Person save(Person person);

    Optional<Person> findById(Long id);

    Optional<Person> findByDocument(DocumentType documentType, String documentNumber);

    List<Person> findAll();

    void deleteById(Long id);
}