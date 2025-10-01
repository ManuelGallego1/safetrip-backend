package com.safetrip.backend.infrastructure.persistence.adapter;

import com.safetrip.backend.domain.model.Person;
import com.safetrip.backend.domain.model.enums.DocumentType;
import com.safetrip.backend.domain.repository.PersonRepository;
import com.safetrip.backend.infrastructure.persistence.entity.PersonEntity;
import com.safetrip.backend.infrastructure.persistence.mapper.PersonMapper;
import com.safetrip.backend.infrastructure.persistence.repository.PersonJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PersonRepositoryImpl implements PersonRepository {

    private final PersonJpaRepository personJpaRepository;

    public PersonRepositoryImpl(PersonJpaRepository personJpaRepository) {
        this.personJpaRepository = personJpaRepository;
    }

    @Override
    public Person save(Person person) {
        PersonEntity entity = PersonMapper.toEntity(person);
        return PersonMapper.toDomain(personJpaRepository.save(entity));
    }

    @Override
    public Optional<Person> findById(Long id) {
        return personJpaRepository.findById(id)
                .map(PersonMapper::toDomain);
    }

    @Override
    public Optional<Person> findByDocument(DocumentType documentType, String documentNumber) {
        return personJpaRepository.findByDocumentTypeAndDocumentNumber(documentType, documentNumber)
                .map(PersonMapper::toDomain);
    }

    @Override
    public List<Person> findAll() {
        return personJpaRepository.findAll()
                .stream()
                .map(PersonMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        personJpaRepository.deleteById(id);
    }
}