package org.magic.gui;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.magic.gui.components.ManaPanel;

public class ManaCellRenderer extends DefaultTableCellRenderer {

	ManaPanel pane=new ManaPanel();
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,int row, int column) {
		{
			
			
			pane.setManaCost(value.toString());
			pane.setBackground(super.getTableCellRendererComponent(table, value, isSelected,hasFocus, row, column).getBackground());
			
			return pane;
		}
	}
	
	
}
