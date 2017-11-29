/*
 Violet - A program for editing UML diagrams.

 Copyright (C) 2007 Cay S. Horstmann (http://horstmann.com)
 Alexandre de Pellegrin (http://alexdp.free.fr);

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.horstmann.violet.framework.statistics;

import org.knowm.xchart.*;
import java.io.File;
import java.nio.file.Paths;
import com.google.gson.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.*;
import java.io.*;

import java.util.Collection;
import com.horstmann.violet.product.diagram.abstracts.node.INode;
import com.horstmann.violet.product.diagram.abstracts.edge.IEdge;

import com.horstmann.violet.product.diagram.classes.*;
import com.horstmann.violet.product.diagram.classes.node.*;
import com.horstmann.violet.product.diagram.classes.edge.*;

import com.horstmann.violet.product.diagram.sequence.*;
import com.horstmann.violet.product.diagram.sequence.node.*;
import com.horstmann.violet.product.diagram.sequence.edge.*;

import com.horstmann.violet.product.diagram.property.text.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;  
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.*;
import javax.swing.*;

import java.util.*;

import com.horstmann.violet.framework.injection.resources.ResourceBundleInjector;
import com.horstmann.violet.framework.injection.resources.annotation.ResourceBundleBean;
import com.horstmann.violet.framework.swingextension.RolloverButtonUI;
import com.horstmann.violet.framework.theme.ITheme;
import com.horstmann.violet.framework.theme.ThemeManager;

/**
 * This class implements a dialog for previewing and printing a graph.
 */
public class StatisticsPanel extends JPanel
{
    /**
     * Constructs a print dialog.
     * 
     * @param gr the graph to be printed
     */
    public StatisticsPanel(String panelType, File[] statsFiles)
    {
        ResourceBundleInjector.getInjector().inject(this);
        if (panelType.equals("class")) 
            classPanelUI(statsFiles);
        else if (panelType.equals("sequence"))
            seqPanelUI(statsFiles);
        else
            throw new java.lang.Error("Need a Class or Sequence diagram");
    }

    /**
     * Lays out the UI of the dialog.
     */
    public void classPanelUI(File[] statsFiles)
    {
        ArrayList<Map<String,Object>> jsonFiles = new ArrayList<Map<String,Object>>();
        if (statsFiles != null) {
            for (File cf : statsFiles) {
                try {
                    JsonReader reader = new JsonReader(new FileReader(cf));
                    Gson gson = new Gson();
                    java.lang.reflect.Type mapType = 
                        new com.google.gson.reflect.TypeToken<Map<String, Object>>(){}.getType(); 
                    Map<String,Object> statistics = gson.fromJson(reader, mapType);
                    jsonFiles.add(statistics);
                } catch (Exception e) {
                    System.out.println(e);
                    throw new java.lang.Error("Can't read input file");
                }
            }   
        }

        // Statistics we will want to display 
        Map<Double, Integer> classesPerProject = new HashMap<Double,Integer>();
        Map<Double, Integer> relationshipsPerProject = new HashMap<Double,Integer>();
        Map<Double, Integer> attrPerClass = new HashMap<Double,Integer>();
        Map<Double, Integer> methPerClass = new HashMap<Double,Integer>();
        Map<Double, Integer> cboPerClass = new HashMap<Double,Integer>();

        for (Map<String,Object> jf: jsonFiles) {
            Iterator it = jf.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                switch ((String) pair.getKey()) {
                    case "numOfClasses":
                        double numClasses = (double) pair.getValue();
                        if (classesPerProject.containsKey(numClasses))
                            classesPerProject.put(numClasses, (classesPerProject.get(numClasses) + 1));
                        else 
                            classesPerProject.put(numClasses, 1);
                        break;
                    case "numOfRelationships":
                        double numRelationships = (double) pair.getValue();
                        if (relationshipsPerProject.containsKey(numRelationships))
                            relationshipsPerProject.put(numRelationships, (relationshipsPerProject.get(numRelationships) + 1));
                        else 
                            relationshipsPerProject.put(numRelationships, 1);   
                        break;
                    case "classes":
                        Map<String,Object> classes = (Map<String,Object>) pair.getValue();

                        for (Map.Entry<String, Object> p2 : classes.entrySet()) {
                            Map<String, Double> classInfo = (Map<String, Double>) p2.getValue();
                            for (Map.Entry<String, Double> p3 : classInfo.entrySet()) {
                                switch ((String) p3.getKey()) {
                                    case "attributes":
                                        double numAttr = (double) p3.getValue();
                                        if (attrPerClass.containsKey(numAttr))
                                            attrPerClass.put(numAttr, (attrPerClass.get(numAttr) + 1));
                                        else 
                                            attrPerClass.put(numAttr, 1);
                                        break;
                                    case "methods":
                                        double numMeth = (double) p3.getValue();
                                        if (methPerClass.containsKey(numMeth))
                                            methPerClass.put(numMeth, (methPerClass.get(numMeth) + 1));
                                        else 
                                            methPerClass.put(numMeth, 1);
                                        break;
                                    case "CBO":
                                        double numCBO = (double) p3.getValue();
                                        if (cboPerClass.containsKey(numCBO))
                                            cboPerClass.put(numCBO, (cboPerClass.get(numCBO) + 1));
                                        else 
                                            cboPerClass.put(numCBO, 1);
                                        break;
                                }
                            }
                        }
                        break;               
                }
                it.remove(); // avoids a ConcurrentModificationException
            }
        }

        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(800, 600));
        GridBagConstraints c = new GridBagConstraints();

        // Classes per diagram 
        PieChart classChart = new PieChartBuilder().width(300).height(300).title("Classes per Diagram").build();
            for (Map.Entry<Double, Integer> entry : classesPerProject.entrySet()) {
                classChart.addSeries(entry.getKey().toString(), entry.getValue());
        }
        JPanel classPanel = new XChartPanel<PieChart>(classChart);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        add(classPanel, c);

        // Relationships per diagram 
        PieChart relationChart = new PieChartBuilder().width(300).height(300).title("Relationships per Diagram").build();
            for (Map.Entry<Double, Integer> entry : relationshipsPerProject.entrySet()) {
                relationChart.addSeries(entry.getKey().toString(), entry.getValue());
        }
        JPanel relationPanel = new XChartPanel<PieChart>(relationChart);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = 1;
        c.weightx = 0.5;
        add(relationPanel, c);

        // Attributes per class 
        PieChart attrChart = new PieChartBuilder().width(200).height(200).title("Attributes per Class").build();
            for (Map.Entry<Double, Integer> entry : attrPerClass.entrySet()) {
                attrChart.addSeries(entry.getKey().toString(), entry.getValue());
        }
        JPanel attrPanel = new XChartPanel<PieChart>(attrChart);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 3;
        c.weightx = 0;
        add(attrPanel, c);

        // Methods per class 
        PieChart methChart = new PieChartBuilder().width(200).height(200).title("Methods per Class").build();
            for (Map.Entry<Double, Integer> entry : methPerClass.entrySet()) {
                methChart.addSeries(entry.getKey().toString(), entry.getValue());
        }
        JPanel methPanel = new XChartPanel<PieChart>(methChart);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = 3;
        c.weightx = 0.5;
        add(methPanel, c);

        // CBO per class 
        PieChart cboChart = new PieChartBuilder().width(200).height(200).title("CBO per Class").build();
            for (Map.Entry<Double, Integer> entry : cboPerClass.entrySet()) {
                cboChart.addSeries(entry.getKey().toString(), entry.getValue());
        }
        JPanel cboPanel = new XChartPanel<PieChart>(cboChart);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = 3;
        c.weightx = 0.5;
        add(cboPanel, c);
    }

    public void seqPanelUI(File[] statsFiles)
    {


        ArrayList<Map<String,Object>> jsonFiles = new ArrayList<Map<String,Object>>();
        if (statsFiles != null) {
            for (File cf : statsFiles) {
                try {
                    JsonReader reader = new JsonReader(new FileReader(cf));
                    Gson gson = new Gson();
                    java.lang.reflect.Type mapType = 
                        new com.google.gson.reflect.TypeToken<Map<String, Object>>(){}.getType(); 
                    Map<String,Object> statistics = gson.fromJson(reader, mapType);
                    jsonFiles.add(statistics);
                } catch (Exception e) {
                    System.out.println(e);
                    throw new java.lang.Error("Can't read input file");
                }
            }   
        }

        // Statistics we will want to display 
        Map<Double, Integer> objectsPerProject = new HashMap<Double,Integer>();
        Map<Double, Integer> activationsPerObject = new HashMap<Double,Integer>();
        Map<Double, Integer> messagesPerObject = new HashMap<Double,Integer>();

        for (Map<String,Object> jf: jsonFiles) {
            Iterator it = jf.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                switch ((String) pair.getKey()) {
                    case "numOfObjects":
                        double numObjects = (double) pair.getValue();
                        if (objectsPerProject.containsKey(numObjects))
                            objectsPerProject.put(numObjects, (objectsPerProject.get(numObjects) + 1));
                        else 
                            objectsPerProject.put(numObjects, 1);
                        break;
                    case "Objects":
                        Map<String,Object> objects = (Map<String,Object>) pair.getValue();

                        for (Map.Entry<String, Object> p2 : objects.entrySet()) {
                            Map<String, Double> objInfo = (Map<String, Double>) p2.getValue();
                            for (Map.Entry<String, Double> p3 : objInfo.entrySet()) {
                                switch ((String) p3.getKey()) {
                                    case "activations":
                                        double numAct = (double) p3.getValue();
                                        if (activationsPerObject.containsKey(numAct))
                                            activationsPerObject.put(numAct, (activationsPerObject.get(numAct) + 1));
                                        else 
                                            activationsPerObject.put(numAct, 1);
                                        break;
                                    case "messages":
                                        double numMess = (double) p3.getValue();
                                        if (messagesPerObject.containsKey(numMess))
                                            messagesPerObject.put(numMess, (messagesPerObject.get(numMess) + 1));
                                        else 
                                            messagesPerObject.put(numMess, 1);
                                        break;
                                }
                            }
                        }
                        break;               
                }
                it.remove(); // avoids a ConcurrentModificationException
            }
        }

        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(800, 600));
        GridBagConstraints c = new GridBagConstraints();

        // Objects per diagram 
        PieChart objChart = new PieChartBuilder().width(300).height(300).title("Objects per Diagram").build();
            for (Map.Entry<Double, Integer> entry : objectsPerProject.entrySet()) {
                objChart.addSeries(entry.getKey().toString(), entry.getValue());
        }
        JPanel objPanel = new XChartPanel<PieChart>(objChart);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        add(objPanel, c);


        // Activations per objects 
        PieChart activChart = new PieChartBuilder().width(300).height(300).title("Activations per Object").build();
            for (Map.Entry<Double, Integer> entry : activationsPerObject.entrySet()) {
                activChart.addSeries(entry.getKey().toString(), entry.getValue());
        }
        JPanel activPanel = new XChartPanel<PieChart>(activChart);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = 2;
        c.weightx = 0.5;
        add(activPanel, c);

        // Messages per objects 
        PieChart messChart = new PieChartBuilder().width(300).height(300).title("Messages per Object").build();
            for (Map.Entry<Double, Integer> entry : messagesPerObject.entrySet()) {
                messChart.addSeries(entry.getKey().toString(), entry.getValue());
        }
        JPanel messPanel = new XChartPanel<PieChart>(messChart);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = 2;
        c.weightx = 0.5;
        add(messPanel, c);
    }

    private Rectangle2D bounds;
    private double scaleGraph = 1;

}