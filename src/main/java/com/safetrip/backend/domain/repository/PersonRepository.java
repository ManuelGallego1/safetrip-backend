package com.safetrip.backend.domain.repository;

import com.safetrip.backend.domain.model.Person;

import java.util.List;
import java.util.Optional;

public interface PersonRepository {

    Person save(Person person);

    Optional<Person> findById(Long id);

    Optional<Person> findByDocument(String documentType, String documentNumber);

    List<Person> findAll();

    void deleteById(Long id);
}