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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.amf.util.AmfResources;
import org.apache.jmeter.protocol.amf.util.AmfXmlConverter;
import org.apache.jmeter.protocol.http.control.CacheManager;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler2;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/**
 * A sampler for the AMF protocol.
 *
 */
public class AmfRequest extends HTTPSampler2 implements Interruptible {

	private static final long serialVersionUID = 1L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    public static final String RESPONSE_CODE_200 = "200"; // $NON-NLS-1$

    // Properties
    public static final String AMFXML = "AmfSampler.amfxml"; // $NON-NLS-1$
    public static final String RAWAMF = "AmfSampler.rawamf"; // $NON-NLS-1$
    public static final String OBJECT_ENCODING_VERSION = "AmfSampler.objectEncoding"; // $NON-NLS-1$
    public static final String PROPERTY_OVERRIDES = "AmfSampler.property_overrides"; // $NON-NLS-1$
    public static final String RESPONSE_VAR = "AmfSampler.resVar"; // $NON-NLS-1$

    public void setAmfXml(String amfXml) {
        setProperty(AMFXML, amfXml);
    }

    public String getAmfXml() {
        return getPropertyAsString(AMFXML);
    }
    
    public void setPropertyOverrides(Arguments vars) {
        setProperty(new TestElementProperty(PROPERTY_OVERRIDES, vars));
    }
    
    public JMeterProperty getPropertyOverridesAsProperty() {
        return getProperty(PROPERTY_OVERRIDES);
    }
    
    private Arguments getPropertyOverrides() {
    	Arguments args = (Arguments) getProperty(PROPERTY_OVERRIDES).getObjectValue();
        if (args == null) {
            args = new Arguments();
            setPropertyOverrides(args);
        }
        return args;
    }
    
    public String getResponseVar() {
    	return getPropertyAsString(RESPONSE_VAR);
    }
    
    public void setResponseVar(String resVar) {
    	setProperty(RESPONSE_VAR, resVar);
    }

    /**
     * Performs a test sample.
     *
     * The <code>sample()</code> method retrieves the reference to the Java
     * client and calls its <code>runTest()</code> method.
     *
     * @see JavaSamplerClient#runTest(JavaSamplerContext)
     *
     * @param entry
     *            the Entry for this sample
     * @return test SampleResult
     */
    public SampleResult sample(Entry entry) {

        //amfRequest.open();

        SampleResult result = null;
        try {
	        // Issue Http request
	        result = super.sample();
	        
	        if (result.getResponseCode().equals(RESPONSE_CODE_200)) {
	        	
	        	// decode and process AMF message response
	            //amfRequest.processResponse(result);
	            
	        }
        } finally {
        	//amfRequest.close();
        }

        return result;
    }

    @Override
    protected HTTPSampleResult sample(URL url, String method, boolean areFollowingRedirect, int frameDepth) {

        String urlStr = url.toString();

        log.debug("Sampling " + urlStr);

        PostMethod httpMethod = new PostMethod(urlStr);

        String contentType = AmfResources.getResString("amf_content_type");
        
        String amfXml = getAmfXml();
        
        // Replace properties with override values, if they exist
        amfXml = overrideProperties(amfXml);
        
        if (log.isDebugEnabled())
        	log.debug("AMF Sample XML: \n"+amfXml);
        
        // Create an AMF request from the XML and add it as the POST request body
        byte[] amfMessage = AmfXmlConverter.convertXmlToAmfMessage(amfXml);
        
        if (amfMessage != null) {
	        ByteArrayRequestEntity requestEntity = new ByteArrayRequestEntity(amfMessage, contentType); 
	        httpMethod.setRequestEntity(requestEntity);
        }

        HTTPSampleResult res = new HTTPSampleResult();
        res.setMonitor(isMonitor());

        res.setSampleLabel(urlStr); // May be replaced later
        res.setHTTPMethod(method);
        res.setURL(url);

        res.sampleStart(); // Count the retries as well in the time
        HttpClient client = null;
        InputStream instream = null;
        
        try {
            // Set any default request headers
            setDefaultRequestHeaders(httpMethod);
            // Setup connection
            client = setupConnection(url, httpMethod, res);
            //savedClient = client;

            // Execute POST
            int statusCode = client.executeMethod(httpMethod);

            // Needs to be done after execute to pick up all the headers
            res.setRequestHeaders(getConnectionHeaders(httpMethod));

            // Request sent. Now get the response:
            instream = httpMethod.getResponseBodyAsStream();

            if (instream != null) {// will be null for HEAD

                Header responseHeader = httpMethod.getResponseHeader(HEADER_CONTENT_ENCODING);
                if (responseHeader!= null && ENCODING_GZIP.equals(responseHeader.getValue())) {
                    instream = new GZIPInputStream(instream);
                }
                res.setResponseData(readResponse(res, instream, (int) httpMethod.getResponseContentLength()));
            }

            res.sampleEnd();
            // Done with the sampling proper.

            // Now collect the results into the HTTPSampleResult:

            res.setSampleLabel(httpMethod.getURI().toString());
            // Pick up Actual path (after redirects)

            res.setResponseCode(Integer.toString(statusCode));
            res.setSuccessful(isSuccessCode(statusCode));

            res.setResponseMessage(httpMethod.getStatusText());

            String ct = null;
            org.apache.commons.httpclient.Header h
                = httpMethod.getResponseHeader(HEADER_CONTENT_TYPE);
            if (h != null)// Can be missing, e.g. on redirect
            {
                ct = h.getValue();
                res.setContentType(ct);// e.g. text/html; charset=ISO-8859-1
                res.setEncodingAndType(ct);
            }

            res.setResponseHeaders(getResponseHeaders(httpMethod));

            // Store any cookies received in the cookie manager:
            saveConnectionCookies(httpMethod, res.getURL(), getCookieManager());

            // Save cache information
            final CacheManager cacheManager = getCacheManager();
            if (cacheManager != null){
                cacheManager.saveDetails(httpMethod, res);
            }

            log.debug("Sample Complete");
            httpMethod.releaseConnection();
            return res;
        } catch (IllegalArgumentException e)// e.g. some kinds of invalid URL
        {
            res.sampleEnd();
            HTTPSampleResult err = errorResult(e, res);
            err.setSampleLabel("Error: " + url.toString());
            return err;
        } catch (IOException e) {
            res.sampleEnd();
            HTTPSampleResult err = errorResult(e, res);
            err.setSampleLabel("Error: " + url.toString());
            return err;
        } finally {
            //savedClient = null;
            JOrphanUtils.closeQuietly(instream);
            if (httpMethod != null) {
                httpMethod.releaseConnection();
            }
            
            // Post Process the XML into 
            //   TODO: Make sure this doesn't change response times
            String resVar = getResponseVar();
            if (resVar != null && !resVar.isEmpty() && res.getBytes() > 0) {
            	log.debug("Decoding response and saving in ${"+resVar+"}");
            	
            	// Decode response
            	String amfResXml = AmfXmlConverter.convertAmfMessageToXml(res.getResponseData());
            	
            	JMeterVariables variables = JMeterContextService.getContext().getVariables();
            	variables.put(resVar, amfResXml);
            }
        }
    }

    protected void setDefaultRequestHeaders(HttpMethod httpMethod) {
    	httpMethod.setRequestHeader("Cache-Control", "no-cache");
    	httpMethod.setRequestHeader("Accept", "*/*");
    	httpMethod.setRequestHeader("Accept-Encoding", "gzip, deflate");
    }
    
    public void testEnded() {
    	super.testEnded();
    }
    
    /**
     * Replace properties (tags) in the xml with the provided overrides using simple regex
     * 
     * @param xml
     * @return
     */
    private String overrideProperties(String xml) {
    	String newXml = new String(xml);
    	
    	Map<String, String> args = getPropertyOverrides().getArgumentsAsMap();
    	
    	for(Map.Entry<String, String> arg : args.entrySet()) {
    		String findStr = arg.getKey();
    		String replaceStr = arg.getValue();
    		
    		//log.debug("Replacing \""+findStr+"\" with \""+replaceStr+"\"");
    		
    		newXml = newXml.replace(findStr, replaceStr);
    	}
    	
    	return newXml;
    }

    /**
     * Generate a String identifier of this instance for debugging purposes.
     *
     * @return a String identifier for this sampler instance
     */
    private String whoAmI() {
        StringBuilder sb = new StringBuilder();
        sb.append(Thread.currentThread().getName());
        sb.append("@");
        sb.append(Integer.toHexString(hashCode()));
        sb.append("-");
        sb.append(getName());
        return sb.toString();
    }
}
