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
        Gson classFiles = new Gson();
        setLayout(new BorderLayout());
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

        System.out.println(classesPerProject);
        System.out.println(relationshipsPerProject);
        System.out.println(cboPerClass);
        System.out.println(methPerClass);
        System.out.println(attrPerClass);
    }

    public void seqPanelUI(File[] statsFiles)
    {
        System.out.println("To be implemented when sequence diagram format is agreed upon");
        setLayout(new BorderLayout());
        if (statsFiles != null) {
            for (File cf : statsFiles) {
                System.out.println(cf); 
            }   
        }
    }

    private Rectangle2D bounds;
    private double scaleGraph = 1;

}