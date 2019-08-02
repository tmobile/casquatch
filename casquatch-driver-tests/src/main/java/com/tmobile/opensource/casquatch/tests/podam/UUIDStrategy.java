/*
 * Copyright 2018 T-Mobile US, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tmobile.opensource.casquatch.tests.podam;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import uk.co.jemos.podam.api.AttributeMetadata;
import uk.co.jemos.podam.api.DataProviderStrategy;
import uk.co.jemos.podam.typeManufacturers.AbstractTypeManufacturer;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;

/**
 * Implements UUID generation. Uses a TimeUUID
 */
class UUIDStrategy extends AbstractTypeManufacturer<UUID> {

    /**
     * Required interface to implement UUID generation.
     * @param strategy passed strategy
     * @param attributeMetadata passed attribute metadata
     * @param genericTypesArgumentsMap passed map
     * @return generated UUID
     */
    @Override
    public UUID getType(DataProviderStrategy strategy,
                          AttributeMetadata attributeMetadata,
                          Map<String, Type> genericTypesArgumentsMap) {

        return Uuids.timeBased();
    }
}
