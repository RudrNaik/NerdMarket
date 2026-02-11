/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @Modified By Tanmay Ghosh
 * @Modified By Vivek Bengre
 */
@RestController
class UserController {

    @Autowired
    UserRepository userRepository;

    private final Logger logger = LoggerFactory.getLogger(UserController.class);

    @RequestMapping(method = RequestMethod.POST, path = "/users/new")
    public String saveUser(@RequestBody Users user) {
        userRepository.save(user);
        return "New User "+ user.getFirstName() + " Saved";
    }
     // function just to create dummy data
    @RequestMapping(method = RequestMethod.GET, path = "/user/create")
    public String createDummyData() {
        Users o1 = new Users(1, "John", "Doe", "Johndoe@gmail.com", "0908765432");
        Users o2 = new Users(2, "Jane", "Doe", "Jdoe@icloud.com", "6458983212");
        Users o3 = new Users(3, "Some", "Pleb", "S_pleb@yahoo.com", "515-345-41213");
        Users o4 = new Users(4, "Chad", "Champion", "CChampion@outlook.com", "4204204200");
        userRepository.save(o1);
        userRepository.save(o2);
        userRepository.save(o3);
        userRepository.save(o4);
        return "Successfully created new N3RD M@RKET user!";
    }

    @RequestMapping(method = RequestMethod.GET, path = "/users")
    public List<Users> getAllUsers() {
        logger.info("List of N3RD M@RKET users");
        List<Users> results = userRepository.findAll();
        logger.info("Number of Records Fetched:" + results.size());
        return results;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/users/{userId}")
    public Optional<Users> findUserById(@PathVariable("userId") int id) {
        logger.info("N3RD M@RKET user with id:");
        Optional<Users> results = userRepository.findById(id);
        return results;
    }

}
