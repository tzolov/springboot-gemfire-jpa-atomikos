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

import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = GtxJpaGfxApplication.class)
public class GtxJpaGfxApplicationTests {

  static {
    System.setProperty("gemfire.name", "server1");
    System.setProperty("gemfire.server.port", "40406");
    System.setProperty("gemfire.jmx-manager", "false");
    System.setProperty("gemfire.jmx-manager-start", "true");
  }

  @Test
  public void contextLoads() {

  }

}
