package org.magic.gui.models;

import org.magic.api.beans.MagicDeck;
import org.magic.gui.abstracts.GenericTableModel;
import org.magic.services.MTGDeckManager;

public class DeckSelectionTableModel extends GenericTableModel<MagicDeck> {

	private static final long serialVersionUID = 1L;
	
	public DeckSelectionTableModel() {
		columns=new String[] {"DECK","CARD_COLOR","Standard","Modern","Legacy","Vintage","CARDS"};
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return MagicDeck.class;
		case 1:
			return String.class;
		case 2:
			return Boolean.class;
		case 3:
			return Boolean.class;
		case 4:
			return Boolean.class;
		case 5:
			return Boolean.class;
		case 6:
			return Integer.class;
		default:
			return super.getColumnClass(columnIndex);
		}
	}

	@Override
	public Object getValueAt(int row, int column) {
		switch (column) {
		case 0:
			return items.get(row);
		case 1:
			return items.get(row).getColors();
		case 2:
			return MTGDeckManager.isLegal(items.get(row), columns[column]);
		case 3:
			return MTGDeckManager.isLegal(items.get(row), columns[column]);
		case 4:
			return MTGDeckManager.isLegal(items.get(row), columns[column]);
		case 5:
			return MTGDeckManager.isLegal(items.get(row), columns[column]);
		case 6:
			return items.get(row).getAsList().size();

		default:
			return "";
		}
	}

}
