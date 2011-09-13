/*
* Copyright 2011 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.apache.jmeter.protocol.amf.sampler;

public class AmfRequestFactory {

    public static final String AMF_SAMPLER = "AmfSampler"; //$NON-NLS-1$

    public static final String DEFAULT_CLASSNAME = AMF_SAMPLER; //$NON-NLS-1$

    private AmfRequestFactory() {
        // Not intended to be instantiated
    }

    /**
     * Create a new instance of the default sampler
     *
     * @return instance of default sampler
     */
    public static AmfRequest newInstance() {
        return newInstance(DEFAULT_CLASSNAME);
    }

    /**
     * Create a new instance of the requested sampler type
     *
     * @param alias
     * @return sampler
     * @throws UnsupportedOperationException if alias is not recognised
     */
    public static AmfRequest newInstance(String alias) {
        if (alias.length() == 0) {
            alias = DEFAULT_CLASSNAME;
        }
        if (alias.equals(AMF_SAMPLER)) {
            return new AmfRequest();
        }
        throw new UnsupportedOperationException("Cannot create class: " + alias);
    }

}
