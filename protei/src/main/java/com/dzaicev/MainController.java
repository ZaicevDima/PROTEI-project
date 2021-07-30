package com.dzaicev;


import com.dzaicev.domain.Person;
import com.dzaicev.exceptions.NotFoundException;
import com.dzaicev.exceptions.NullArgumentException;
import com.dzaicev.repos.PersonRepo;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
public class MainController {
    Logger logger = Logger.getLogger(MainController.class.getName());
    private final Map<Integer, FutureTask<String>> taskMap = new HashMap<>();
    private final int TIME = 5 * 60 * 1000;

    @Autowired
    private PersonRepo personRepo;

    @PostMapping("/add")
    public ResponseEntity<String> add(@RequestParam String name, @RequestParam String email, @RequestParam String phone) {

        Map<String, String> map = new HashMap<>();
        if (name == null || email == null || phone == null || name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            return nullArgumentException(map);
        }
        Person person = new Person(name, email, phone);
        personRepo.save(person);

        map.put("id", String.valueOf(person.getId()));
        return ResponseEntity.ok(new JSONObject(map).toString());
    }

    @PostMapping("/search")
    public ResponseEntity<String> search(@RequestParam String id) {
        Map<String, String> map = new HashMap<>();

        if (id != null && !id.isEmpty()) {
            Iterable<Person> info = personRepo.findById(Integer.valueOf(id));
            if (!info.iterator().hasNext()) {
                return notFoundException(map);
            }
            Person person = info.iterator().next();
            setSearchJSONMap(map, person);

            return ResponseEntity.ok(new JSONObject(map).toString());
        } else {
            return nullArgumentException(map);
        }
    }

    @PostMapping("/status")
    public ResponseEntity<String> changeStatus(@RequestParam String id, @RequestParam String status) {
        Iterable<Person> info;
        Map<String, String> map = new HashMap<>();

        if ((id != null && !id.isEmpty()) && (status != null && !status.isEmpty())) {
            info = personRepo.findById(Integer.valueOf(id));
            if (!info.iterator().hasNext()) {
                return notFoundException(map);
            }
            setPerson(map, info, status);

            return ResponseEntity.ok(new JSONObject(map).toString());
        } else {
            return nullArgumentException(map);
        }
    }

    private void setPerson(Map<String, String> map, Iterable<Person> info, String newStatus) {
        Person person = info.iterator().next();
        setChangeStatusJSONMap(map, person, newStatus);

        person.setStatus(newStatus);
        personRepo.save(person);

        FutureTask<String> task = createTask(person);

        if (newStatus.equals("Online")) {
            if (taskMap.containsKey(person.getId())) {
                taskMap.get(person.getId()).cancel(true);
            }
            taskMap.put(person.getId(), task);
            CompletableFuture.runAsync(task);
        }
    }

    private ResponseEntity<String> nullArgumentException(Map<String, String> map) {
        try {
            throw new NullArgumentException("Argument must be not null");
        } catch (NullArgumentException e) {
            logger.log(Level.WARNING, e.getMessage());
            map.put("exception", e.getMessage());
            return ResponseEntity.badRequest().body(new JSONObject(map).toString());
        }
    }

    private ResponseEntity<String> notFoundException(Map<String, String> map) {
        try {
            throw new NotFoundException("Person with this ID was not found");
        } catch (NotFoundException e) {
            logger.log(Level.WARNING, e.getMessage());
            map.put("exception", e.getMessage());
            return ResponseEntity.badRequest().body(new JSONObject(map).toString());
        }
    }

    private void setSearchJSONMap(Map<String, String> map, Person person) {
        map.put("id", String.valueOf(person.getId()));
        map.put("email", person.getEmail());
        map.put("phone", person.getPhone());
        map.put("status", person.getStatus());
    }

    private void setChangeStatusJSONMap(Map<String, String> map, Person person, String newStatus) {
        String oldStatus = person.getStatus();

        map.put("id", String.valueOf(person.getId()));
        map.put("oldstatus", oldStatus);
        map.put("newstatus", newStatus);
    }

    private FutureTask<String> createTask(Person person) {
        return new FutureTask<>(() -> {
            Thread.sleep(TIME);
            person.setStatus("Away");
            personRepo.save(person);
            taskMap.remove(person.getId());
            return null;
        });
    }
}
