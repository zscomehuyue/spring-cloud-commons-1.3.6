/*
 * Copyright 2017 the original author or authors.
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

package org.springframework.cloud.client.loadbalancer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.Random;


import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

/**
 * @author Ryan Baxter
 */
public abstract class AbstractLoadBalancerAutoConfigurationTests {

	@Test
	public void restTemplateGetsLoadBalancerInterceptor() {
		ConfigurableApplicationContext context = init(OneRestTemplate.class);
		final Map<String, RestTemplate> restTemplates = context
				.getBeansOfType(RestTemplate.class);

		assertThat(restTemplates, is(notNullValue()));
		assertThat(restTemplates.values(), hasSize(1));
		RestTemplate restTemplate = restTemplates.values().iterator().next();
		assertThat(restTemplate, is(notNullValue()));

		assertLoadBalanced(restTemplate);
	}

	protected abstract void assertLoadBalanced(RestTemplate restTemplate);

	@Test
	public void multipleRestTemplates() {
		ConfigurableApplicationContext context = init(TwoRestTemplates.class);
		final Map<String, RestTemplate> restTemplates = context
				.getBeansOfType(RestTemplate.class);

		assertThat(restTemplates, is(notNullValue()));
		Collection<RestTemplate> templates = restTemplates.values();
		assertThat(templates, hasSize(2));

		TwoRestTemplates.Two two = context.getBean(TwoRestTemplates.Two.class);

		assertThat(two.loadBalanced, is(notNullValue()));
		assertLoadBalanced(two.loadBalanced);

		assertThat(two.nonLoadBalanced, is(notNullValue()));
		assertThat(two.nonLoadBalanced.getInterceptors(), is(empty()));
	}

	protected ConfigurableApplicationContext init(Class<?> config) {
		return new SpringApplicationBuilder().web(false)
				.properties("spring.aop.proxyTargetClass=true")
				.sources(config, LoadBalancerAutoConfiguration.class).run();
	}

	@Configuration
	protected static class OneRestTemplate {

		@LoadBalanced
		@Bean
		RestTemplate loadBalancedRestTemplate() {
			return new RestTemplate();
		}

		@Bean
		LoadBalancerClient loadBalancerClient() {
			return new NoopLoadBalancerClient();
		}

		@Bean
		LoadBalancedRetryPolicyFactory loadBalancedRetryPolicyFactory() { return new LoadBalancedRetryPolicyFactory.NeverRetryFactory();}

	}

	@Configuration
	protected static class TwoRestTemplates {

		@Primary
		@Bean
		RestTemplate restTemplate() {
			return new RestTemplate();
		}

		@LoadBalanced
		@Bean
		RestTemplate loadBalancedRestTemplate() {
			return new RestTemplate();
		}

		@Bean
		LoadBalancerClient loadBalancerClient() {
			return new NoopLoadBalancerClient();
		}

		@Bean
		LoadBalancedRetryPolicyFactory loadBalancedRetryPolicyFactory() { return new LoadBalancedRetryPolicyFactory.NeverRetryFactory();}

		@Configuration
		protected static class Two {
			@Autowired
			RestTemplate nonLoadBalanced;

			@Autowired
			@LoadBalanced
			RestTemplate loadBalanced;
		}

	}

	private static class NoopLoadBalancerClient implements LoadBalancerClient {
		private final Random random = new Random();

		@Override
		public ServiceInstance choose(String serviceId) {
			return new DefaultServiceInstance(serviceId, serviceId,
					this.random.nextInt(40000), false);
		}

		@Override
		public <T> T execute(String serviceId, LoadBalancerRequest<T> request) {
			try {
				return request.apply(choose(serviceId));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public <T> T execute(String serviceId, ServiceInstance serviceInstance, LoadBalancerRequest<T> request) throws IOException {
			try {
				return request.apply(choose(serviceId));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public URI reconstructURI(ServiceInstance instance, URI original) {
			return DefaultServiceInstance.getUri(instance);
		}
	}
}
