/* Copyright 2018 Telstra Open Source
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.openkilda.floodlight.utils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.openkilda.floodlight.utils.CorrelationContext.CorrelationContextClosable;

import java.util.UUID;

/**
 * An aspect for @NewCorrelationContext which decorates each call with a new correlation context.
 */
@Aspect
public class CorrelationContextInitializer {

    @Around("execution(@NewCorrelationContext * *(..))")
    public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        try (CorrelationContextClosable closable = CorrelationContext.create(UUID.randomUUID().toString())) {
            return joinPoint.proceed();
        }
    }
}