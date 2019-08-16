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

import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Simple implementation class to define default strategies
 */
public class CasquatchPodamFactoryImpl extends PodamFactoryImpl {
    /**
     * Extends constructor for default settings
     */
    public CasquatchPodamFactoryImpl() {
        super();
        this.getStrategy().addOrReplaceTypeManufacturer(BigDecimal.class, new BigDecimalStrategy());
        this.getStrategy().addOrReplaceTypeManufacturer(BigInteger.class, new BigIntegerStrategy());
        this.getStrategy().addOrReplaceTypeManufacturer(ByteBuffer.class, new ByteBufferStrategy());
        this.getStrategy().addOrReplaceTypeManufacturer(InetAddress.class, new InetAddressStrategy());
        this.getStrategy().addOrReplaceTypeManufacturer(Instant.class, new InstantStrategy());
        this.getStrategy().addOrReplaceTypeManufacturer(LocalDate.class, new LocalDateStrategy());
        this.getStrategy().addOrReplaceTypeManufacturer(LocalTime.class, new LocalTimeStrategy());
        this.getStrategy().addOrReplaceTypeManufacturer(UUID.class, new UUIDStrategy());
    }
}
