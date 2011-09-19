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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
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
	
    private JFrame xmlEditor;
    
    private StringBuffer amfXml;
    
    private JLabel xmlSize;

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
        
        amfXml = new StringBuffer();

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

        amfXml.setLength(0);
        amfXml.append(element.getPropertyAsString(AmfRequest.AMFXML));
        objectEncodingCombo.setSelectedItem(element.getPropertyAsString(AmfRequest.OBJECT_ENCODING_VERSION));
        
        updateXmlBytes();
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
        element.setProperty(AmfRequest.AMFXML, amfXml.toString(), "");
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
        amfXml.setLength(0);
    }

    
    protected final JPanel getAmfRequestPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "AMF Request")); // $NON-NLS-1$

        List<String> values = new ArrayList<String>();
        values.add("AMF"+String.valueOf(MessageIOConstants.AMF3));

        objectEncodingCombo = new JComboBox(values.toArray());
        objectEncodingCombo.setEditable(false);
        
        panel.add(objectEncodingCombo);
        
        JButton editXml = new JButton("Edit XML");
        editXml.addActionListener(new ActionListener() {
			@SuppressWarnings("serial")
			public void actionPerformed(ActionEvent e) {
        		openXmlEditor();
        	}
        });
        panel.add(editXml);
        
        xmlSize = new JLabel();
        panel.add(xmlSize);
        
        return panel;
    }
    
    @SuppressWarnings("serial")
	private void openXmlEditor() {
    	xmlEditor = new AmfXmlEditorGui(amfXml) {
			public void onSave() {
				updateXmlBytes();
			}
		};
		xmlEditor.pack();
		xmlEditor.setVisible(true);
    }
    
    private void updateXmlBytes() {
    	xmlSize.setText("("+amfXml.length()+" chars)");
    }
}
