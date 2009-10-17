/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.apache.webbeans.atinject.tck.specific;

import javax.enterprise.inject.BeanTypes;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.New;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

import org.atinject.tck.auto.Drivers;
import org.atinject.tck.auto.DriversSeat;
import org.atinject.tck.auto.FuelTank;
import org.atinject.tck.auto.Seat;
import org.atinject.tck.auto.accessories.Cupholder;
import org.atinject.tck.auto.accessories.SpareTire;


public class SpecificProducer
{
    public SpecificProducer()
    {
        
    }
    
    @Produces @Drivers
    public Seat produceDrivers(@New Cupholder cupHolder)
    {
        return new DriversSeat(cupHolder);
    }
    
    
    @Produces @DriverBinding @BeanTypes(value={DriversSeat.class})
    public DriversSeat produceDriverSeat(@New Cupholder cupHolder)
    {
        return new DriversSeat(cupHolder);
    }
    
    
    @Produces @Named("spare") @SpareBinding
    public SpareTire produceSpare(@New FuelTank forSuper, @New FuelTank forSub)
    {
        return new SpareTire(forSuper, forSub);
    }
    
    @Produces @Default @BeanTypes(value={SpareTire.class})
    public SpareTire produceSpareTire(@New FuelTank forSuper, @New FuelTank forSub)
    {
        return new SpareTire(forSuper, forSub);
    }
    
}
