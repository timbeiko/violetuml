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

package com.horstmann.violet.framework.vizualisation;

import org.knowm.xchart.*;

import java.util.Collection;
import com.horstmann.violet.product.diagram.abstracts.node.INode;
import com.horstmann.violet.product.diagram.classes.node.*;
import com.horstmann.violet.product.diagram.classes.*;
import com.horstmann.violet.product.diagram.property.text.MultiLineText;

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
import com.horstmann.violet.product.diagram.abstracts.IGraph;

/**
 * This class implements a dialog for previewing and printing a graph.
 */
public class VizualisationPanel extends JPanel
{
    /**
     * Constructs a print dialog.
     * 
     * @param gr the graph to be printed
     */
    public VizualisationPanel(IGraph gr)
    {
        ResourceBundleInjector.getInjector().inject(this);
        this.graph = gr;
        layoutUI(gr);
    }

    /**
     * Lays out the UI of the dialog.
     */
    public void layoutUI(IGraph gr)
    {
        setLayout(new BorderLayout());
        Collection<INode> c = gr.getAllNodes();

        HashMap<Integer, Integer> attrMap = new HashMap<Integer,Integer>();
        HashMap<Integer, Integer> methMap = new HashMap<Integer,Integer>();

        for (Object node : c) {
            if (node instanceof ClassNode) {
                ClassNode cNode = (ClassNode) node;
                MultiLineText cNodeAttr = (MultiLineText) cNode.getAttributes();
                MultiLineText cNodeMeth = (MultiLineText) cNode.getMethods();
                int attrCount = cNodeAttr.getNumRows();
                int methCount = cNodeMeth.getNumRows();

                if (attrMap.containsKey(attrCount))
                    attrMap.put(attrCount, (attrMap.get(attrCount) + 1));
                else 
                    attrMap.put(attrCount, 1);

                if (methMap.containsKey(methCount))
                    methMap.put(methCount, (attrMap.get(methCount) + 1));
                else 
                    methMap.put(methCount, 1);
            }
        }

        PieChart attrChart = new PieChartBuilder().width(300).height(300).title("Attributes").build();
        for (Map.Entry<Integer, Integer> entry : attrMap.entrySet()) {
            attrChart.addSeries(entry.getKey().toString(), entry.getValue());
        }
        JPanel attrPanel = new XChartPanel<PieChart>(attrChart);
        attrPanel.setLayout(new BorderLayout());
        add(attrPanel, BorderLayout.WEST);

        PieChart methChart = new PieChartBuilder().width(300).height(300).title("Methods").build();
        for (Map.Entry<Integer, Integer> entry : methMap.entrySet()) {
            methChart.addSeries(entry.getKey().toString(), entry.getValue());
        }
        JPanel methPanel = new XChartPanel<PieChart>(methChart);
        methPanel.setLayout(new BorderLayout());
        add(methPanel, BorderLayout.EAST);

    }


    private IGraph graph;
    private Rectangle2D bounds;
    private double scaleGraph = 1;


}