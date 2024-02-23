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
package org.apache.syncope.core.persistence.neo4j.outer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.List;
import org.apache.syncope.core.persistence.api.dao.DelegationDAO;
import org.apache.syncope.core.persistence.api.dao.UserDAO;
import org.apache.syncope.core.persistence.api.entity.Delegation;
import org.apache.syncope.core.persistence.neo4j.AbstractTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class DelegationTest extends AbstractTest {

    @Autowired
    private DelegationDAO delegationDAO;

    @Autowired
    private UserDAO userDAO;

    @Test
    public void findValidFor() {
        Delegation delegation = entityFactory.newEntity(Delegation.class);
        delegation.setDelegating(userDAO.findByUsername("bellini").orElseThrow());
        delegation.setDelegated(userDAO.findByUsername("rossini").orElseThrow());
        delegation.setStart(OffsetDateTime.now());
        delegation = delegationDAO.save(delegation);

        List<String> delegating = delegationDAO.findValidDelegating(
                userDAO.findKey("rossini").orElseThrow(), OffsetDateTime.now());
        assertEquals(List.of("bellini"), delegating);

        String valid = delegationDAO.findValidFor(
                userDAO.findKey("bellini").orElseThrow(),
                userDAO.findKey("rossini").orElseThrow(),
                OffsetDateTime.now()).orElseThrow();
        assertEquals(delegation.getKey(), valid);

        assertTrue(delegationDAO.findByDelegating(userDAO.findByUsername("bellini").orElseThrow()).
                contains(delegation));
        assertTrue(delegationDAO.findByDelegated(userDAO.findByUsername("rossini").orElseThrow()).
                contains(delegation));
    }
}
