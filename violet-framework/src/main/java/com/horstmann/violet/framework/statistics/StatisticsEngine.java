package com.horstmann.violet.framework.statistics;

import javax.swing.JOptionPane;
import javax.swing.border.EmptyBorder;

import com.horstmann.violet.framework.dialog.DialogFactory;
import com.horstmann.violet.framework.injection.bean.ManiocFramework.BeanInjector;
import com.horstmann.violet.framework.injection.bean.ManiocFramework.InjectedBean;
import com.horstmann.violet.framework.injection.resources.ResourceBundleInjector;
import com.horstmann.violet.framework.injection.resources.annotation.ResourceBundleBean;
import com.horstmann.violet.product.diagram.abstracts.IGraph;

@ResourceBundleBean(resourceReference = StatisticsPanel.class)
public class StatisticsEngine
{

    public StatisticsEngine(IGraph graph)
    {
        BeanInjector.getInjector().inject(this);
    	ResourceBundleInjector.getInjector().inject(this);
        this.graph = graph;
    }

    public void start()
    {
        StatisticsPanel statsPanel = new StatisticsPanel(this.graph);
        JOptionPane optionPane = new JOptionPane();
        optionPane.setOptions(new String[]
        {
            this.statsCloseText
        });
        optionPane.setMessage(statsPanel);
        optionPane.setBorder(new EmptyBorder(0, 0, 10, 0));
        this.dialogFactory.showDialog(optionPane, this.statsTitle, true);
    }

    @ResourceBundleBean(key = "dialog.statistics.title.text", resourceReference = StatisticsPanel.class)
    private String statsTitle;
    
    @ResourceBundleBean(key = "dialog.statistics.close.text", resourceReference = StatisticsPanel.class)
    private String statsCloseText;
        
    @InjectedBean
    private DialogFactory dialogFactory;

    private IGraph graph;

}
