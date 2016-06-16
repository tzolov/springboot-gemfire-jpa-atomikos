/*
 * Copyright 2012-2014 the original author or authors.
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
package io.pivotal.poc.gemfire.gtx;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gemstone.gemfire.cache.Region;

@Service
public class TransactionalService {

  public static final String TEST_PERSON_NAME = "pippo";

  private static final Logger LOG = LoggerFactory.getLogger(TransactionalService.class);

  @Autowired
  private PersonRepository personRepository;

  @Transactional(timeout = 600)
  public void syncDBFromRegion(Region<String, Integer> region) throws Exception {

    int value = region.get(GtxJpaGfxApplication.KEY_ONE);

    LOG.info(">>> Region Value:" + value);

    personRepository.save(new Person(TEST_PERSON_NAME, value));
  }

  @Transactional(timeout = 600, readOnly = true)
  public Person findEntry(String name) throws Exception {
    return personRepository.findOne(name);
  }

  @Transactional
  public void syncRegionFromDB(Region<String, Integer> region) {
    Person person = personRepository.findOne(TEST_PERSON_NAME);
    region.put(person.getName(), person.getAge());
  }
}
