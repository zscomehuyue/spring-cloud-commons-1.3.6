/*
 * Copyright 2006-2017 the original author or authors.
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
package org.springframework.cloud.context.properties;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.cloud.autoconfigure.ConfigurationPropertiesRebinderAutoConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.cloud.context.properties.ConfigurationPropertiesRebinderListIntegrationTests.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestConfiguration.class,
		properties = "messages=one,two")
public class ConfigurationPropertiesRebinderListIntegrationTests {

	@Autowired
	private TestProperties properties;

	@Autowired
	private ConfigurationPropertiesRebinder rebinder;

	@Autowired
	private ConfigurableEnvironment environment;

	@Test
	@DirtiesContext
	public void testAppendProperties() throws Exception {
		assertEquals("[one, two]", this.properties.getMessages().toString());
		EnvironmentTestUtils.addEnvironment(this.environment, "messages[0]:foo");
		this.rebinder.rebind();
		assertEquals("[foo, two]", this.properties.getMessages().toString());
	}

	@Test
	@DirtiesContext
	@Ignore("Can't rebind to list and re-initialize it (need refresh scope for this to work)")
	public void testReplaceProperties() throws Exception {
		assertEquals("[one, two]", this.properties.getMessages().toString());
		Map<String, Object> map = findTestProperties();
		map.clear();
		EnvironmentTestUtils.addEnvironment(this.environment, "messages[0]:foo");
		this.rebinder.rebind();
		assertEquals("[foo]", this.properties.getMessages().toString());
	}

	private Map<String, Object> findTestProperties() {
		for (PropertySource<?> source : this.environment.getPropertySources()) {
			if (source.getName().toLowerCase().contains("test")) {
				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>) source.getSource();
				return map;
			}
		}
		throw new IllegalStateException("Could not find test property source");
	}

	@Test
	@DirtiesContext
	public void testReplacePropertiesWithCommaSeparated() throws Exception {
		assertEquals("[one, two]", this.properties.getMessages().toString());
		Map<String, Object> map = findTestProperties();
		map.clear();
		EnvironmentTestUtils.addEnvironment(this.environment, "messages:foo");
		this.rebinder.rebind();
		assertEquals("[foo]", this.properties.getMessages().toString());
	}

	@Configuration
	@EnableConfigurationProperties
	@Import({ RefreshConfiguration.RebinderConfiguration.class,
			PropertyPlaceholderAutoConfiguration.class })
	protected static class TestConfiguration {

		@Bean
		protected TestProperties properties() {
			return new TestProperties();
		}

	}

	// Hack out a protected inner class for testing
	protected static class RefreshConfiguration extends RefreshAutoConfiguration {
		@Configuration
		protected static class RebinderConfiguration
				extends ConfigurationPropertiesRebinderAutoConfiguration {

		}
	}

	@ConfigurationProperties
	protected static class TestProperties {
		private List<String> messages;
		private int count;

		public List<String> getMessages() {
			return this.messages;
		}

		public void setMessages(List<String> messages) {
			this.messages = messages;
		}

		public int getCount() {
			return this.count;
		}

		@PostConstruct
		public void init() {
			this.count++;
		}
	}

}
