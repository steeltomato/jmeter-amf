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

package org.apache.jmeter.protocol.amf.visualizers;

import org.apache.jmeter.protocol.amf.util.AmfXmlConverter;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.visualizers.ResultRenderer;
import org.apache.jmeter.visualizers.SamplerResultTab;

public class RenderAsAMF extends SamplerResultTab implements ResultRenderer {
	
	private static final int MAX_RESPONSE_SIZE = 500; /* Max size (in KB) of response for rendering */ 

    /** {@inheritDoc} */
    public void renderResult(SampleResult sampleResult) {
    	results.setContentType("text/plain");
    	
    	if (!sampleResult.getContentType().equals("application/x-amf")) {
    		results.setText("Invalid content type for AMF response: "+sampleResult.getContentType());
    	}
    	else if (sampleResult.getResponseData().length == 0) {
    		results.setText("Empty response");
    	}
    	else if (sampleResult.getResponseData().length > MAX_RESPONSE_SIZE * 1024) {
    		results.setText("Response size too large for display, limit is "+MAX_RESPONSE_SIZE+"k");
    	}
    	else {
    		String xml = AmfXmlConverter.convertAmfMessageToXml(sampleResult.getResponseData());
    		results.setText(xml);
    	}
    	
    	results.setCaretPosition(0);
    	resultsScrollPane.setViewportView(results);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "AMF"; // $NON-NLS-1$
    }

}
