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

package org.apache.jmeter.protocol.amf.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.amf.sampler.AmfRequest;
import org.apache.jmeter.protocol.http.config.gui.UrlConfigGui;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import flex.messaging.io.MessageIOConstants;

// TODO: Take out the HTTP defaults - they are handled by any HTTP Request Defaults config

/**
 * JMeter configuration GUI component that provides configuration support
 * for the AmfSampler.
 * 
 */
public class AmfRequestDefaultsGui extends AbstractConfigGui implements ActionListener {

	private static final long serialVersionUID = 1L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    private JComboBox objectEncodingCombo;
    
    private ArgumentsPanel propertyOverrides;

    public AmfRequestDefaultsGui() {
        init();
    }
    
    /**
     * {@inheritDoc}
     */
    private void init() {
    	setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        
        add(makeTitlePanel(), BorderLayout.NORTH);
        
        VerticalPanel centerPanel = new VerticalPanel();
        
        centerPanel.add(getAmfRequestPanel());
        
        add(centerPanel, BorderLayout.CENTER);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getStaticLabel() {
        return "AMF Request Defaults"; // $NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
	@Override
	public String getLabelResource() {
		return "";
	}

    /**
     * {@inheritDoc}
     */
	@Override
    public void configure(TestElement element) {
		super.configure(element);
        
        // Configure AMF request specific properties
        objectEncodingCombo.setSelectedItem(element.getPropertyAsString(AmfRequest.OBJECT_ENCODING_VERSION));
        
        final JMeterProperty po = element.getProperty(AmfRequest.PROPERTY_OVERRIDES);
        if (po != null && po.getObjectValue() != null) {
            propertyOverrides.configure((Arguments) po.getObjectValue());
        }
    }

	/**
     * {@inheritDoc}
     */
	@Override
    public TestElement createTestElement() {
        ConfigTestElement element = new ConfigTestElement();
        element.setName(this.getName());
        element.setProperty(TestElement.GUI_CLASS, this.getClass().getName());
        element.setProperty(TestElement.TEST_CLASS, element.getClass().getName());
        modifyTestElement(element);
        return element;
    }

	/**
     * {@inheritDoc}
     */
    @Override
	public void modifyTestElement(TestElement element) {
		ConfigTestElement cfg = (ConfigTestElement) element;
		cfg.clear();
		super.configureTestElement(element);
        
		// Set AMF properties
		element.setProperty(AmfRequest.OBJECT_ENCODING_VERSION, String.valueOf(objectEncodingCombo.getSelectedItem()));
		
		element.setProperty(new TestElementProperty(AmfRequest.PROPERTY_OVERRIDES, (Arguments) propertyOverrides.createTestElement()));
    }
	
    public void clear() {
        propertyOverrides.clear();
    }

    protected final JPanel getAmfRequestPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "AMF Request Defaults")); // $NON-NLS-1$

        panel.add(getObjectEncodingPanel(), BorderLayout.NORTH);
        
        panel.add(getPropertyOverridesPanel(), BorderLayout.SOUTH);
        
        return panel;
    }

    protected final JPanel getObjectEncodingPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(getObjectEncodingVersionPanel(), BorderLayout.NORTH);
        return panel;
    }

    private JPanel getObjectEncodingVersionPanel() {
    	
        List<String> values = new ArrayList<String>();
        values.add("AMF"+String.valueOf(MessageIOConstants.AMF3));

        JLabel label = new JLabel("AMF Encoding"); // $NON-NLS-1$

        objectEncodingCombo = new JComboBox(values.toArray());
        objectEncodingCombo.setEditable(false);
        label.setLabelFor(objectEncodingCombo);

        HorizontalPanel panel = new HorizontalPanel();
        panel.add(label);
        panel.add(objectEncodingCombo);

        return panel;
    }
    
    private final JPanel getPropertyOverridesPanel() {
    	propertyOverrides = new ArgumentsPanel("Value Replacement (name: search string, value: replace string)");
    	
    	return propertyOverrides;
    }
    
	public void actionPerformed(ActionEvent evt) {
        //if (evt.getSource() == objectEncodingCombo) {
        //}
	}

}
