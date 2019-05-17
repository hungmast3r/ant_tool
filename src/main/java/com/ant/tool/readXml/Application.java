package com.ant.tool.readXml;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.management.Attribute;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingConstants;
import javax.swing.JComboBox;

public class Application extends JFrame {

    private JPanel contentPanel;
    private JTextField txtValueSearch;
    private JButton btnSave;
    private JButton btnReset;
    private JPanel panelSearch;
    private JComboBox<String> cbbKey;
    private JPanel panelAttribute;
    private File file;
    private Document document;
    private int lengthAttribute;
    private JTextField[] textFieldListValue;
    private JLabel[] labelListKey;
    private String strChildKey;
    private String strChildValue;
    private Element editElement;

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return name.substring(lastIndexOf);
    }

    private Document parseXML(String filePath) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(filePath);
        doc.getDocumentElement().normalize();
        return doc;
    }

    private Element getElementByKey(String key, String value, Document document) throws Exception {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr = xpath.compile("//*[@" + key + "='" + value + "']");
        NodeList nl = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
        return (Element) nl.item(0);
    }

    private void updateElementValue() {
        for (int i = 0; i < lengthAttribute; i++) {
            if (strChildKey.equals(editElement.getAttributes().item(i).getNodeName())) {
                continue;
            }
            editElement.setAttribute(labelListKey[i].getText(), textFieldListValue[i].getText());
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Application frame = new Application();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public Application() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 476, 392);
        contentPanel = new JPanel();
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPanel.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPanel);

        JLabel lblMessage = new JLabel("");
        lblMessage.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(lblMessage, BorderLayout.SOUTH);

        panelSearch = new JPanel();
        contentPanel.add(panelSearch, BorderLayout.NORTH);

        JButton btnSelect = new JButton("Select...");
        btnSelect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                JFileChooser fileDialog = new JFileChooser();
                int returnVal = fileDialog.showSaveDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    file = fileDialog.getSelectedFile();
                    if (file.exists() && getFileExtension(file).equals(".xml")) {
                        try {
                            document = parseXML(file.getPath());
                            document.getDocumentElement().normalize();
                            Element docEl = document.getDocumentElement();
                            Node childNode = docEl.getFirstChild();
                            while (childNode.getNextSibling() != null) {
                                childNode = childNode.getNextSibling();
                                if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                                    Element childElement = (Element) childNode;
                                    lengthAttribute = childElement.getAttributes().getLength();
                                    for (int i = 0; i < lengthAttribute; i++) {
                                        cbbKey.addItem(childElement.getAttributes().item(i).getNodeName());
                                    }
                                    break;
                                }
                            }
                            panelSearch.updateUI();

                            lblMessage.setText("validate file OK");
                        } catch (Exception e) {
                            e.printStackTrace();
                            lblMessage.setText("Error! Call dev now =))");
                        }
                    } else {
                        lblMessage.setText("File extension invalid");
                    }
                }
            }
        });
        panelSearch.add(btnSelect);

        cbbKey = new JComboBox<>();
        cbbKey.addItem("--Key--");
        cbbKey.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getItem().equals("--Key--")) txtValueSearch.setEnabled(false);
                else {
                    txtValueSearch.setEnabled(true);
                    lblMessage.setText("key: " + cbbKey.getSelectedItem().toString());
                }
            }
        });
        panelSearch.add(cbbKey);

        txtValueSearch = new JTextField();
        txtValueSearch.setToolTipText("value");
        txtValueSearch.setColumns(10);
        txtValueSearch.setEnabled(false);
        txtValueSearch.addKeyListener(
                new KeyListener() {
                    @Override
                    public void keyTyped(KeyEvent e) {

                    }

                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            btnSave.setEnabled(true);
                            btnSelect.setEnabled(false);
                            strChildKey = cbbKey.getSelectedItem().toString();
                            strChildValue = txtValueSearch.getText();
                            try {
                                editElement = getElementByKey(strChildKey, strChildValue, document);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            if (editElement != null) {
                                GridBagConstraints textFieldConstraints = new GridBagConstraints();
                                textFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
                                textFieldConstraints.weightx = 1;
                                textFieldConstraints.weighty = 1;
                                textFieldListValue = new JTextField[lengthAttribute];
                                labelListKey = new JLabel[lengthAttribute];
                                for (int i = 0; i < lengthAttribute; i++) {
                                    if (strChildKey.equals(editElement.getAttributes().item(i).getNodeName())) {
                                        continue;
                                    }
                                    for (int j = 0; j < 2; j++) {
                                        if (j % 2 == 0) {
                                            labelListKey[i] = new JLabel();
                                            labelListKey[i].setText(editElement.getAttributes().item(i).getNodeName());
                                            textFieldConstraints.gridx = j;
                                            textFieldConstraints.gridy = i;
                                            panelAttribute.add(labelListKey[i], textFieldConstraints);
                                        } else {
                                            textFieldListValue[i] = new JTextField();
                                            textFieldListValue[i].setText(editElement.getAttributes().item(i).getNodeValue());
                                            textFieldConstraints.gridx = j;
                                            textFieldConstraints.gridy = i;
                                            panelAttribute.add(textFieldListValue[i], textFieldConstraints);
                                        }
                                    }
                                }
                                lblMessage.setText("Select " + strChildKey + " : " + strChildValue);
                                panelAttribute.updateUI();
                            }
                        }
                    }

                    @Override
                    public void keyReleased(KeyEvent e) {

                    }
                }
        );
        panelSearch.add(txtValueSearch);

        btnSave = new JButton("Save");
        btnSave.setEnabled(false);
        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateElementValue();
                document.getDocumentElement().normalize();
                TransformerFactory transformerFactory = TransformerFactory.newInstance();

                try {
                    Transformer transformer = transformerFactory.newTransformer();
                    DOMSource source = new DOMSource(document);
                    StreamResult result = new StreamResult(new File(file.getPath()));
                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                    transformer.transform(source, result);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    lblMessage.setText("Error! Call dev now =))");
                }
                lblMessage.setText("XML file updated successfully");
            }
        });
        panelSearch.add(btnSave);

        btnReset = new JButton("Reset");
        btnReset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnSave.setEnabled(false);
                btnSelect.setEnabled(true);
                file = null;
                cbbKey.removeAllItems();
                cbbKey.addItem("--Key--");
                panelSearch.updateUI();
            }
        });
        panelSearch.add(btnReset);

        panelAttribute = new JPanel(new GridBagLayout());
        contentPanel.add(panelAttribute, BorderLayout.CENTER);

    }

}