/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.syncope.fit.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.ws.rs.core.Response;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.io.IOUtils;
import org.apache.syncope.client.lib.SyncopeClientFactoryBean;
import org.apache.syncope.common.lib.to.WorkflowDefinitionTO;
import org.apache.syncope.common.lib.types.AnyTypeKind;
import org.apache.syncope.fit.AbstractITCase;
import org.apache.syncope.fit.ActivitiDetector;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

public class WorkflowITCase extends AbstractITCase {

    private static String defaultUserKey = null;

    @BeforeClass
    public static void findDefault() {
        Assume.assumeFalse(clientFactory.getContentType() == SyncopeClientFactoryBean.ContentType.YAML);
        Assume.assumeTrue(ActivitiDetector.isActivitiEnabledForUsers(syncopeService));

        WorkflowDefinitionTO found = IterableUtils.find(
                workflowService.list(AnyTypeKind.USER.name()), new Predicate<WorkflowDefinitionTO>() {

            @Override
            public boolean evaluate(final WorkflowDefinitionTO object) {
                return object.isMain();
            }
        });
        if (found != null) {
            defaultUserKey = found.getKey();
        }
        assertNotNull(defaultUserKey);
    }

    @Test
    public void exportUserDefinition() throws IOException {
        Assume.assumeFalse(clientFactory.getContentType() == SyncopeClientFactoryBean.ContentType.YAML);
        Assume.assumeTrue(ActivitiDetector.isActivitiEnabledForUsers(syncopeService));

        Response response = workflowService.get(AnyTypeKind.USER.name(), defaultUserKey);
        assertTrue(response.getMediaType().toString().
                startsWith(clientFactory.getContentType().getMediaType().toString()));
        assertTrue(response.getEntity() instanceof InputStream);
        String definition = IOUtils.toString((InputStream) response.getEntity(), StandardCharsets.UTF_8);
        assertNotNull(definition);
        assertFalse(definition.isEmpty());
    }

    @Test
    public void updateUserDefinition() throws IOException {
        Assume.assumeFalse(clientFactory.getContentType() == SyncopeClientFactoryBean.ContentType.YAML);
        Assume.assumeTrue(ActivitiDetector.isActivitiEnabledForUsers(syncopeService));

        Response response = workflowService.get(AnyTypeKind.USER.name(), defaultUserKey);
        String definition = IOUtils.toString((InputStream) response.getEntity(), StandardCharsets.UTF_8);

        workflowService.set(AnyTypeKind.USER.name(), defaultUserKey, definition);
    }
}