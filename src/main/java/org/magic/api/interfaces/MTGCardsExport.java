package org.magic.api.interfaces;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.Icon;

import org.magic.api.beans.MagicCard;
import org.magic.api.beans.MagicCardStock;
import org.magic.api.beans.MagicDeck;
import org.magic.api.interfaces.abstracts.AbstractCardExport.MODS;

public interface MTGCardsExport extends MTGPlugin {

	public String getFileExtension();

	public void export(MagicDeck deck, File dest) throws IOException;

	public MagicDeck importDeck(File f) throws IOException;

	public MagicDeck importDeck(String f, String name) throws IOException;
	
	public void export(List<MagicCard> cards, File f) throws IOException;

	public void exportStock(List<MagicCardStock> stock, File f) throws IOException;

	public List<MagicCardStock> importStock(File f) throws IOException;

	public Icon getIcon();

	public boolean needDialogGUI();

	public MODS getMods();

}