package com.ieti.proyectoieti.repositories;

import com.ieti.proyectoieti.models.Event;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends MongoRepository<Event, String> {
  List<Event> findByCategory(String category);

  List<Event> findByDateAfter(LocalDate date);

  List<Event> findByLocationContainingIgnoreCase(String location);
}
