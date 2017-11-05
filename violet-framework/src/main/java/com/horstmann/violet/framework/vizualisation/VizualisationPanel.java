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

import java.util.Collection;
import com.horstmann.violet.product.diagram.abstracts.node.INode;
import com.horstmann.violet.product.diagram.classes.node.*;
import com.horstmann.violet.product.diagram.classes.*;

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

        for (Object node : c) {
            System.out.println(node.getClass());
            if (node instanceof ClassNode) {
                ClassNode c_node = (ClassNode) node;
                System.out.println(c_node.getAttributes());
                System.out.println(c_node.getMethods());
            }
        }

        JPanel panel = new JPanel();
        JLabel label = new JLabel(c.toString());
        panel.add(label);

        add(panel, BorderLayout.CENTER);

    }


    private IGraph graph;
    private Rectangle2D bounds;
    private double scaleGraph = 1;


}