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
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.gemstone.gemfire.cache.DataPolicy.PARTITION;

import com.atomikos.icatch.jta.UserTransactionManager;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.distributed.ServerLauncher;

import javax.annotation.PostConstruct;
import javax.naming.NamingException;

import io.pivotal.poc.gemfire.gtx.jndi.SimpleNamingContextBuilder;

/**
 * This application demonstrates how to use Gemfire and JPA with a Global Transaction Manager: Atomikos.
 * <br/>
 * 1. Add the following starters to your POM:
 * spring-boot-starter-data-gemfire, spring-boot-starter-data-jpa, spring-boot-starter-jta-atomikos
 * <br/>
 * 2. Create an In-Memory JNDI provider instance BEFORE the application context is initialized.
 * - Gemfire requires JNDI to lookup the global transaction manager.
 * <br/>
 * 3. After the Application context initialization bind the Atomikos UserTransaction manager
 * to the JNDI name: java:comp/UserTransaction
 */
@SpringBootApplication
@EnableTransactionManagement
public class GtxJpaGfxApplication implements CommandLineRunner {

  public static final String KEY_ONE = "key";
  private static final Logger LOG = LoggerFactory.getLogger(GtxJpaGfxApplication.class);
  // In-Memory JNDI service used by Gemfire to lookup global transactions.
  private static SimpleNamingContextBuilder inMemoryJndiBuilder = null;

  // Note: the SimpleNamingContextBuilder MUST be created before the Spring Application Context!!!
  static {
    try {
      inMemoryJndiBuilder = SimpleNamingContextBuilder.emptyActivatedContextBuilder();
    } catch (NamingException e) {
      LOG.error("Failed to create in-memory JNDI provider", e);
    }
  }

  @Autowired
  private TransactionalService txService;
  @Autowired
  private UserTransactionManager atomikosTxManager;

  public static void main(String[] args) throws NamingException {
    SpringApplication.run(GtxJpaGfxApplication.class, args).close();
  }

  @PostConstruct
  public void registerAtomikos() {
    // Gemfire uses JNDI:java:comp/UserTransaction to lookup global transactions.
    inMemoryJndiBuilder.bind("java:comp/UserTransaction", atomikosTxManager);
  }

  @Override
  public void run(String... args) throws Exception {

    ServerLauncher serverLauncher = new ServerLauncher.Builder()
        .set("jmx-manager", "true")
        .set("jmx-manager-start", "true")
        .build();

    ServerLauncher.ServerState start = serverLauncher.start();

    Cache cache = new CacheFactory().create();

    // Create Loader Region
    Region<String, Integer> region = cache.<String, Integer>createRegionFactory()
        .setDataPolicy(PARTITION)
        .create("testRegion");

    // put new entity in the region
    region.put(KEY_ONE, 999);

    LOG.info("Cache Server Started");

    // Within XA transaction read the entity update an external DB
    txService.syncDBFromRegion(region);

    // Make sure the external DB is updated
    LOG.info(">>> Found JPA Entry:" + txService.findEntry(TransactionalService.TEST_PERSON_NAME));

    // Within XA transaction read from External DB and create new entity in Gemfire
    txService.syncRegionFromDB(region);

    // Make sure the new entitiy is created
    LOG.info(">>> New Region Entry:" + region.get(TransactionalService.TEST_PERSON_NAME));

    Thread.sleep(5000);

    serverLauncher.stop();
  }
}
