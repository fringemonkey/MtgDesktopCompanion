package org.magic.gui.components.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.magic.api.beans.Booster;
import org.magic.api.beans.MagicEdition;
import org.magic.gui.renderer.MagicEditionListRenderer;
import org.magic.services.MTGControler;
import javax.swing.JButton;
import javax.swing.BoxLayout;
import java.awt.FlowLayout;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class BoosterQtyPanel extends JPanel {
	private JComboBox<MagicEdition> cboEditions;
	private JSpinner spinner;
	
	public BoosterQtyPanel() {
		initGUI();
	}

	public void setValue(MagicEdition ed,Integer qty)
	{
		spinner.setValue(qty);
		cboEditions.setSelectedItem(ed);
	}
	
	
	public BoosterQtyPanel(MagicEdition ed,Integer qty) {
		initGUI();
		spinner.setValue(qty);
		cboEditions.setSelectedItem(ed);
	}
	
	private void initGUI() {
		setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		List<MagicEdition> li=new ArrayList<>();
		try {
			li = MTGControler.getInstance().getEnabledProviders().loadEditions();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		cboEditions = new JComboBox<>(new DefaultComboBoxModel<MagicEdition>(li.toArray(new MagicEdition[li.size()])));
		cboEditions.setRenderer(new MagicEditionListRenderer());
		add(cboEditions);
		
		spinner = new JSpinner();
		spinner.setModel(new SpinnerNumberModel(new Integer(6), new Integer(0), null, new Integer(1)));
		add(spinner);
		
	}



	public MagicEdition getEdition()
	{
		return (MagicEdition)cboEditions.getSelectedItem();
	}
	
	public int getQty()
	{
		return (Integer)spinner.getValue();
	}
	
	
}
