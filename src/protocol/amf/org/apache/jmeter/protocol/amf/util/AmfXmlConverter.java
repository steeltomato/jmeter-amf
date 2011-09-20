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

package org.apache.jmeter.protocol.amf.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.mapper.Mapper;

import flex.messaging.io.MessageDeserializer;
import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.ASObject;
import flex.messaging.io.amf.ActionContext;
import flex.messaging.io.amf.ActionMessage;
import flex.messaging.io.amf.Amf3Input;
import flex.messaging.io.amf.Amf3Output;
import flex.messaging.io.amf.AmfMessageDeserializer;
import flex.messaging.io.amf.AmfMessageSerializer;
import flex.messaging.io.amf.MessageBody;
import flex.messaging.io.amf.MessageHeader;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.RemotingMessage;

public class AmfXmlConverter {
	private static final Logger log = LoggingManager.getLoggerForClass();
	
	private static XStream xstream;
	
    /**
     * Converts XML to an object then serializes it
     */
    public static byte[] convertXmlToAmf(String xml) {
    	XStream xs = getXStream();
    	Amf3Output amf3out = new Amf3Output(SerializationContext.getSerializationContext());
    	
    	try {
    		Object msg = xs.fromXML(xml);
    		
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			amf3out.setOutputStream(baos);
			amf3out.writeObject(msg);
			
			return baos.toByteArray();
		}
		catch (Exception ex) {
			// TODO: Error message for user
			log.error("Failed to generate AMF stream", ex);
		}
    	
    	return new byte[0];
    }
    
    /**
     * Converts AMF encoded object to XML String
     */
    public static String convertAmfToXml(byte[] amf) {
    	XStream xs = getXStream();
    	SerializationContext serializationContext = new SerializationContext();
        serializationContext.createASObjectForMissingType = true;
        serializationContext.instantiateTypes = true;
    	
    	Amf3Input amf3in = new Amf3Input(serializationContext);
    	ByteArrayInputStream bais = new ByteArrayInputStream(amf);
    	amf3in.setInputStream(bais);
    	
    	try {
    		Object obj = amf3in.readObject();
			String xml = xs.toXML(obj);
			return xml;
    	}
    	catch (Exception ex) {
    		log.error("Failed to process AMF binary data", ex);
    	}
    	finally {
    		try {
    			amf3in.close();
    		}
    		catch (IOException e) {
    			
    		}
    	}
    	
    	return "";
    }
    
    
    /**
     * Converts XML to a complete AMF message
     */
    public static byte[] convertXmlToAmfMessage(String xml) {
    	XStream xs = getXStream();
    	ActionContext actionContext = new ActionContext();
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	
    	ActionMessage message = (ActionMessage) xs.fromXML(xml);
    	
    	actionContext.setRequestMessage(message);
        
        AmfMessageSerializer amfMessageSerializer = new AmfMessageSerializer();
        amfMessageSerializer.initialize(SerializationContext.getSerializationContext(), baos, null);
    	
        try {
			amfMessageSerializer.writeMessage(message);
		} catch (IOException ex) {
            log.error("An exception was encountered while serializing AMF. ", ex);
		}
        
    	return baos.toByteArray();
    }
    
    
    /**
     * Converts complete AMF message to XML representation
     */
    public static String convertAmfMessageToXml(byte[] amf) {
    	XStream xs = getXStream();
    	ActionContext actionContext = new ActionContext();
    	SerializationContext serializationContext = new SerializationContext();
    	
    	// TODO: Maybe let users change these options if they want?
    	serializationContext.createASObjectForMissingType = true;
    	//serializationContext.instantiateTypes = false;
    	
    	ByteArrayInputStream bin = new ByteArrayInputStream(amf);
        
        ActionMessage message = new ActionMessage();
        actionContext.setRequestMessage(message);
        
        MessageDeserializer deserializer = new AmfMessageDeserializer();
        deserializer.initialize(serializationContext, bin, null);
        
        try {
        	deserializer.readMessage(message, actionContext);
        } catch (Exception ex) {
        	log.error("An exception was encountered while deserializing response. ", ex);
        }
        
        return xs.toXML(message);
    }
    
	public static XStream getXStream() {
		if (xstream == null) {
			xstream = new XStream();
			
			xstream.alias("ActionMessage", ActionMessage.class);
			xstream.alias("MessageHeader", MessageHeader.class);
			xstream.alias("MessageBody", MessageBody.class);
			xstream.alias("RemotingMessage", RemotingMessage.class);
			xstream.alias("CommandMessage", CommandMessage.class);
			xstream.alias("ASObject", ASObject.class);
			
			// Better ASObject Converter
			Mapper mapper = xstream.getMapper();
			xstream.registerConverter(new ASObjectConverter(mapper));
		}
		
		return xstream;
	}
}
