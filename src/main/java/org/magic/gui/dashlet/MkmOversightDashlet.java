package org.magic.gui.dashlet;

import java.awt.BorderLayout;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jdesktop.swingx.JXTable;
import org.magic.api.exports.impl.MKMFileWantListExport;
import org.magic.api.interfaces.abstracts.AbstractJDashlet;
import org.magic.gui.models.MkmInsightTableModel;

public class MkmOversightDashlet extends AbstractJDashlet {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MkmOversightDashlet() {
		super();
	}
	
	@Override
	public Icon getIcon() {
		return new ImageIcon(MKMFileWantListExport.class.getResource("/icons/plugins/magiccardmarket.png"));
	}
	
	
	@Override
	public String getName() {
		return "Mkm Oversight";
	}
	
	@Override
	public STATUT getStatut() {
		return STATUT.DEV;
	}
	
	@Override
	public void initGUI() 
	{
		getContentPane().setLayout(new BorderLayout(0, 0));
		MkmInsightTableModel model=new MkmInsightTableModel();
		JPanel panneauHaut = new JPanel();
		getContentPane().add(panneauHaut, BorderLayout.NORTH);
		
		JComboBox<String> comboBox = new JComboBox<>();
		panneauHaut.add(comboBox);
		
		
		JXTable table = new JXTable(model);
		getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
	}

	@Override
	public void init() {
		//TODO make the dev
	}

}
