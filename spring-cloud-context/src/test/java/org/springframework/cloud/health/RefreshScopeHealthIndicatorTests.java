/*
 * Copyright 2013-2015 the original author or authors.
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

package org.springframework.cloud.health;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.actuate.health.Status;
import org.springframework.cloud.context.properties.ConfigurationPropertiesRebinder;
import org.springframework.cloud.context.scope.refresh.RefreshScope;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * @author Dave Syer
 */
public class RefreshScopeHealthIndicatorTests {

	private ConfigurationPropertiesRebinder rebinder = Mockito
			.mock(ConfigurationPropertiesRebinder.class);
	private RefreshScope scope = Mockito.mock(RefreshScope.class);
	private RefreshScopeHealthIndicator indicator = new RefreshScopeHealthIndicator(
			this.scope, this.rebinder);

	@Before
	public void init() {
		when(this.rebinder.getErrors())
				.thenReturn(Collections.<String, Exception> emptyMap());
		when(this.scope.getErrors())
				.thenReturn(Collections.<String, Exception> emptyMap());
	}

	@Test
	public void sunnyDay() {
		assertEquals(Status.UP, this.indicator.health().getStatus());
	}

	@Test
	public void binderError() {
		when(this.rebinder.getErrors()).thenReturn(Collections
				.<String, Exception> singletonMap("foo", new RuntimeException("FOO")));
		assertEquals(Status.DOWN, this.indicator.health().getStatus());
	}

	@Test
	public void scopeError() {
		when(this.scope.getErrors()).thenReturn(Collections
				.<String, Exception> singletonMap("foo", new RuntimeException("FOO")));
		assertEquals(Status.DOWN, this.indicator.health().getStatus());
	}

	@Test
	public void bothError() {
		when(this.rebinder.getErrors()).thenReturn(Collections
				.<String, Exception> singletonMap("foo", new RuntimeException("FOO")));
		when(this.scope.getErrors()).thenReturn(Collections
				.<String, Exception> singletonMap("bar", new RuntimeException("BAR")));
		assertEquals(Status.DOWN, this.indicator.health().getStatus());
	}

}
