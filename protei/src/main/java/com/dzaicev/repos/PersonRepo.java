package com.dzaicev.repos;

import com.dzaicev.domain.Person;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PersonRepo extends CrudRepository<Person, Long> {
    List<Person> findById(Integer id);
}
