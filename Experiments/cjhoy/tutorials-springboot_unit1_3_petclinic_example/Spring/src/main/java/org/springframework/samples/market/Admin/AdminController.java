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
package org.springframework.samples.market.Admin;

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
class AdminController {

    @Autowired
    AdminRepository adminRepository;

    private final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @RequestMapping(method = RequestMethod.POST, path = "/admins/new")
    public String saveOwner(@RequestBody Admins admin) {
        adminRepository.save(admin);
        return "New Admin "+ admin.getFirstName() + " Saved";
    }
     // function just to create dummy data
    @RequestMapping(method = RequestMethod.GET, path = "/admins/create")
    public String createDummyData() {
        Admins o1 = new Admins("Cooper", "Hoy", "Magic Cards", "cooper.hoy3105@gmail.com");
        Admins o2 = new Admins("Marcus", "Yeung", "Pokemon Cards", "Myeung@gmail.com");
        Admins o3 = new Admins("Hrushi", "Bhatt", "Pokemon Cards", "Hbhatt10@iastate.edu");
        Admins o4 = new Admins("Kennedy", "Lind", "Funko Pops", "kklind10@gmail.com");
        adminRepository.save(o1);
        adminRepository.save(o2);
        adminRepository.save(o3);
        adminRepository.save(o4);
        return "Successfully created dummy data";
    }

    @RequestMapping(method = RequestMethod.GET, path = "/admins")
    public List<Admins> getAllAdmins() {
        logger.info("Entered into Controller Layer");
        List<Admins> results = adminRepository.findAll();
        logger.info("Number of Records Fetched:" + results.size());
        return results;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/admins/{adminId}")
    public Optional<Admins> findOwnerById(@PathVariable("adminId") int id) {
        logger.info("Entered into Controller Layer");
        Optional<Admins> results = adminRepository.findById(id);
        return results;
    }

}
