package org.springframework.cloud.bootstrap.encrypt;
/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.ClassPathExclusions;
import org.springframework.cloud.FilteredClassPathRunner;
import org.springframework.context.ConfigurableApplicationContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

/**
 * @author Ryan Baxter
 */
@RunWith(FilteredClassPathRunner.class)
@ClassPathExclusions({"spring-security-rsa*.jar"})
public class RsaDisabledTests {

	private ConfigurableApplicationContext context;

	@Before
	public void setUp() {
		context = new SpringApplicationBuilder().web(false)
				.sources(EncryptionBootstrapConfiguration.class).web(false).properties(
						"encrypt.key:mykey",
						"encrypt.rsa.strong:true",
						"encrypt.rsa.salt:foobar").run();
	}

	@After
	public void tearDown() {
		if(context != null) {
			context.close();
		}
	}

	@Test
	public void testLoadBalancedRetryFactoryBean() throws Exception {
		Map<String, RsaProperties> properties =  context.getBeansOfType(RsaProperties.class);
		assertThat(properties.values(), hasSize(0));
	}
}
