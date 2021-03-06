package org.magic.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.SystemColor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.LineBorder;

import org.apache.log4j.Logger;
import org.magic.api.beans.MagicCard;
import org.magic.api.beans.MagicCollection;
import org.magic.api.beans.MagicEdition;
import org.magic.api.interfaces.MTGCardsProvider;
import org.magic.api.interfaces.MTGDao;
import org.magic.api.interfaces.MTGPlugin;
import org.magic.gui.components.MagicCardDetailPanel;
import org.magic.gui.renderer.MagicCollectionIconListRenderer;
import org.magic.gui.renderer.MagicEditionIconListRenderer;
import org.magic.gui.renderer.MagicEditionIconListRenderer.SIZE;
import org.magic.gui.renderer.PluginIconListRenderer;
import org.magic.services.MTGConstants;
import org.magic.services.MTGControler;
import org.magic.services.MTGLogger;
import org.magic.services.ThreadManager;

import net.coderazzi.filters.gui.AutoChoices;
import net.coderazzi.filters.gui.TableFilterHeader;

public class UITools {

	private UITools() {}
	
	protected static Logger logger = MTGLogger.getLogger(UITools.class);
	
	public static String[] stringLineSplit(String s)
	{
		return s.split("[\\r\\n]+");
	}
	

	
	
	public static <T extends MTGPlugin> JComboBox<T> createCombobox(Class<T> classe,boolean all)
	{
		DefaultComboBoxModel<T> model = new DefaultComboBoxModel<>();
		JComboBox<T> combo = new JComboBox<>(model);
		if(all)
			MTGControler.getInstance().getPlugins(classe).stream().forEach(model::addElement);
		else
			MTGControler.getInstance().listEnabled(classe).stream().forEach(model::addElement);
		combo.setRenderer(new PluginIconListRenderer());
		return combo;
	}
	



	public static JComboBox<MagicEdition> createComboboxEditions(List<MagicEdition> value,SIZE s) {
		DefaultComboBoxModel<MagicEdition> model = new DefaultComboBoxModel<>();
		JComboBox<MagicEdition> combo = new JComboBox<>(model);
		value.forEach(model::addElement);
		combo.setRenderer(new MagicEditionIconListRenderer(s));
		return combo;
	}
	
	
	public static JComboBox<MagicEdition> createComboboxEditions()
	{
		try {
			List<MagicEdition> list = MTGControler.getInstance().getEnabled(MTGCardsProvider.class).loadEditions();
			Collections.sort(list);
			return createComboboxEditions(list,SIZE.MEDIUM);
		} catch (IOException e) {
			logger.error(e);
			return new JComboBox<>();
		}

	}
	
	public static <T> JComboBox<T> createCombobox(T[] items)
	{
		return createCombobox(Arrays.asList(items));
	}
	
	
	public static <T> JComboBox<T> createCombobox(List<T> items)
	{
		DefaultComboBoxModel<T> model = new DefaultComboBoxModel<>();
		JComboBox<T> combo = new JComboBox<>(model);
			items.stream().forEach(model::addElement);
			
			combo.setRenderer((list,value, index,isSelected,cellHasFocus)->{
					JLabel l ;
					if(value==null)
					{
						l= new JLabel();
					}
					else
					{
						l=new JLabel(value.toString());
						if(value instanceof LookAndFeelInfo)
						{
							l=new JLabel(((LookAndFeelInfo)value).getName());
						}
						l.setIcon(MTGConstants.ICON_MANA_INCOLOR);
					}
					
					l.setOpaque(true);
					if (isSelected) {
						l.setBackground(list.getSelectionBackground());
						l.setForeground(list.getSelectionForeground());
					} else {
						l.setBackground(list.getBackground());
						l.setForeground(list.getForeground());
					}
					
					return l;
			});
			
			
		return combo;
	}
	
	
	
	public static JComboBox<MagicCollection> createComboboxCollection()
	{
		DefaultComboBoxModel<MagicCollection> model = new DefaultComboBoxModel<>();
		JComboBox<MagicCollection> combo = new JComboBox<>(model);
	
		try {
			MTGControler.getInstance().getEnabled(MTGDao.class).listCollections().stream().forEach(model::addElement);
			combo.setRenderer(new MagicCollectionIconListRenderer());
		return combo;
		} catch (Exception e) {
			logger.error(e);
			return combo;
		}

	}
	
	
	
	public static String formatDouble(Object f)
	{
		return new DecimalFormat("#0.##").format(f);
	}

	public static void initTableFilter(JTable table)
	{
		ThreadManager.getInstance().runInEdt(()->{
			TableFilterHeader filterHeader = new TableFilterHeader(table, AutoChoices.ENABLED);
			filterHeader.setSelectionBackground(Color.LIGHT_GRAY);	
		});
		
	}
	
	public static void initCardToolTipTable(final JTable table, final Integer cardPos, final Integer edPos) {
		final JPopupMenu popUp = new JPopupMenu();
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int row = table.rowAtPoint(e.getPoint());
				MagicCardDetailPanel pane = new MagicCardDetailPanel();
				pane.enableThumbnail(true);
				if (row > -1) {
					table.setRowSelectionInterval(row, row);
					String cardName = table.getValueAt(row, cardPos.intValue()).toString();

					if (cardName.indexOf('(') >= 0)
						cardName = cardName.substring(0, cardName.indexOf('(')).trim();

					MagicEdition ed = null;
					if (edPos != null) {
						String edID = table.getValueAt(row, edPos).toString();
						ed = new MagicEdition();
						ed.setId(edID);
					}

					try {
						MagicCard mc = MTGControler.getInstance().getEnabled(MTGCardsProvider.class).searchCardByName(cardName, ed, true).get(0);
						pane.setMagicCard(mc);
						
						popUp.setBorder(new LineBorder(Color.black));
						popUp.setVisible(false);
						popUp.removeAll();
						popUp.setLayout(new BorderLayout());
						popUp.add(pane, BorderLayout.CENTER);
						popUp.show(table, e.getX(), e.getY());
						popUp.setVisible(true);

					} catch (Exception ex) {
						logger.error("Error on " + cardName, ex);
					}
				}
			}
		});
	}
	

	public static <T> List<T> getTableSelection(JTable tableCards,int columnID) {
		int[] viewRow = tableCards.getSelectedRows();
		List<T> listCards = new ArrayList<>();
		for (int i : viewRow) {
			int modelRow = tableCards.convertRowIndexToModel(i);
			T mc = (T) tableCards.getModel().getValueAt(modelRow, columnID);
			listCards.add(mc);
		}
		return listCards;
	}



	public static void applyDefaultSelection(Component pane) {
			pane.setForeground(SystemColor.textHighlightText);
			pane.setBackground(SystemColor.inactiveCaption);
	}

}
