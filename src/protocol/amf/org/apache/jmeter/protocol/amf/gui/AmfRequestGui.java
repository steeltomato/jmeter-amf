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
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.amf.sampler.AmfRequest;
import org.apache.jmeter.protocol.amf.sampler.AmfRequestFactory;
import org.apache.jmeter.protocol.http.config.gui.UrlConfigGui;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledTextArea;

import flex.messaging.io.MessageIOConstants;

/**
 * The JMeter GUI component which manage the AmfSampler.
 *
 */
public class AmfRequestGui extends AbstractSamplerGui {

	private static final long serialVersionUID = 1L;

	private UrlConfigGui urlConfigGui;
	
	private JComboBox objectEncodingCombo;
	
    private JLabeledTextArea amfXml;

    public AmfRequestGui() {
        init();
    }
    
    /**
     * {@inheritDoc}
     */
    public String getStaticLabel() {
        return "AMF Request"; // $NON-NLS-1$
    }

	@Override
	public String getLabelResource() {
		return "";
	}

    private void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);

        VerticalPanel centerPanel = new VerticalPanel();
        
        centerPanel.add(getAmfRequestPanel());
        
        // TODO: Some sort of accordian to shrink the URL config area
        urlConfigGui = new UrlConfigGui(false, false);
        centerPanel.add(urlConfigGui);
        
        add(centerPanel, BorderLayout.CENTER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(TestElement element) {
        super.configure(element);
        urlConfigGui.configure(element);

        objectEncodingCombo.setSelectedItem(element.getPropertyAsString(AmfRequest.OBJECT_ENCODING_VERSION));
        amfXml.setText(element.getPropertyAsString(AmfRequest.AMFXML));
    }

    /**
     * {@inheritDoc}
     */
    public TestElement createTestElement() {
        AmfRequest sampler = AmfRequestFactory.newInstance();// create default sampler
        modifyTestElement(sampler);
        return sampler;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     * <p>
     * {@inheritDoc}
     */
    public void modifyTestElement(TestElement element) {
        element.clear();
        urlConfigGui.modifyTestElement(element);
        super.configureTestElement(element);
        
        element.setProperty(AmfRequest.OBJECT_ENCODING_VERSION, String.valueOf(objectEncodingCombo.getSelectedItem()));
        element.setProperty(AmfRequest.AMFXML, amfXml.getText(), "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearGui() {
        super.clearGui();
        urlConfigGui.clear();
        amfXml.setText("");
    }

    
    protected final JPanel getAmfRequestPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "AMF Request")); // $NON-NLS-1$

        panel.add(getObjectEncodingPanel(), BorderLayout.NORTH);
        panel.add(getAmfXmlPanel(), BorderLayout.CENTER);
        
        return panel;
    }
    
    protected JPanel getAmfXmlPanel() {
        amfXml = new JLabeledTextArea("XML Representation"); // $NON-NLS-1$
        return amfXml;
    }
    
    protected final JPanel getObjectEncodingPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(getObjectEncodingVersionPanel(), BorderLayout.NORTH);
        return panel;
    }
    
    /**
     * Create a panel with GUI components allowing the user to select an
     * AMF Object Encoding Version.
     *
     * @return a panel containing the relevant components
     */
    protected JPanel getObjectEncodingVersionPanel() {
    	
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
}
