package org.apache.jmeter.protocol.amf.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class AmfXmlEditorGui extends JFrame {
	private static final long serialVersionUID = 1L;
	
	JFrame _this;
	
	StringBuffer xml;
	
	JTextArea textArea;
	JButton save;
	JButton cancel;

	/**
     * Create a new NamePanel with the default name.
     */
    public AmfXmlEditorGui(StringBuffer xml) {
    	_this = this;
    	
    	this.xml = xml;
    	
    	setLayout(new BorderLayout(5, 0));
        add(createAmfXmlEditor(), BorderLayout.CENTER);
        
        textArea.setText(xml.toString());
    }
    
    private JPanel createAmfXmlEditor() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        
        panel.setBorder(BorderFactory.createTitledBorder("AMF XML Editor")); //$NON-NLS-1$

        textArea = new JTextArea(40, 80);
        JScrollPane scrollableTextArea = new JScrollPane(textArea);
        panel.add(scrollableTextArea, BorderLayout.CENTER);
        

        panel.add(createButtons(), BorderLayout.PAGE_END);
        
        return panel;
    }
    
    private JPanel createButtons() {
    	JPanel btnRow = new JPanel();
        btnRow.setLayout(new BorderLayout());

        save = new JButton("Save");
        save.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		xml.setLength(0);
        		xml.append(textArea.getText());
        		onSave();
        		_this.setVisible(false);
        	}
        });
        
        cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		_this.setVisible(false);
        	}
        });
        
        
        btnRow.add(save, BorderLayout.LINE_END);
        btnRow.add(cancel, BorderLayout.LINE_START);
        
        return btnRow;
    }
    
    // Overridden by parent to provide save callback
    public void onSave() {
    	
    }
}
