package org.magic.gui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.DefaultRowSorter;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXTable;
import org.magic.api.beans.MagicCard;
import org.magic.api.beans.MagicCardNames;
import org.magic.api.beans.MagicCollection;
import org.magic.api.beans.MagicEdition;
import org.magic.api.beans.MagicRuling;
import org.magic.api.interfaces.MTGCardsExport;
import org.magic.api.interfaces.abstracts.AbstractCardExport.MODS;
import org.magic.game.gui.components.DisplayableCard;
import org.magic.game.gui.components.HandPanel;
import org.magic.gui.components.charts.CmcChartPanel;
import org.magic.gui.components.charts.HistoryPricesPanel;
import org.magic.gui.components.charts.ManaRepartitionPanel;
import org.magic.gui.components.charts.RarityRepartitionPanel;
import org.magic.gui.components.charts.TypeRepartitionPanel;
import org.magic.gui.models.MagicCardTableModel;
import org.magic.gui.renderer.EditionCellRenderer;
import org.magic.gui.renderer.MagicEditionListRenderer;
import org.magic.gui.renderer.ManaCellRenderer;
import org.magic.services.MTGConstants;
import org.magic.services.MTGControler;
import org.magic.services.MTGLogger;
import org.magic.services.ThreadManager;
import org.magic.sorters.CardsEditionSorter;
import org.utils.patterns.observer.Observable;
import org.utils.patterns.observer.Observer;

import net.coderazzi.filters.gui.AutoChoices;
import net.coderazzi.filters.gui.TableFilterHeader;

public class CardSearchPanel extends JPanel {

	private transient Logger logger = MTGLogger.getLogger(this.getClass());

	public static final int INDEX_PRICES = 2;
	public static final int INDEX_THUMB = 1;

	private MagicCard selectedCard;
	private MagicEdition selectedEdition;

	private MagicCardTableModel cardsModeltable;

	private JTabbedPane tabbedCardsView;
	private JTabbedPane tabbedCardsInfo;

	private static CardSearchPanel inst;

	private HandPanel thumbnailPanel;
	private ManaRepartitionPanel manaRepartitionPanel;
	private TypeRepartitionPanel typeRepartitionPanel;
	private RarityRepartitionPanel rarityRepartitionPanel;
	private CmcChartPanel cmcChart;
	private CardsPicPanel cardsPicPanel;
	private HistoryPricesPanel historyChartPanel;
	private MagicEditionDetailPanel magicEditionDetailPanel;
	private MagicCardDetailPanel detailCardPanel;
	private PricesTablePanel priceTablePanel;
	private JTextArea txtRulesArea;
	private JSONPanel panelJson;

	private JTextField txtMagicSearch;

	private JPopupMenu popupMenu = new JPopupMenu();

	private JComboBox<MagicCardNames> cboLanguages;
	private JComboBox<String> cboQuereableItems;
	private JComboBox<MagicCollection> cboCollections;

	private JXTable tableCards;

	private JButton btnExport;

		private JList<MagicEdition> listEdition;

	private JLabel lblLoading;

	public static CardSearchPanel getInstance() {
		if (inst == null)
			inst = new CardSearchPanel();

		return inst;
	}

	public void loading(boolean show, String text) {
		lblLoading.setText(text);
		lblLoading.setVisible(show);
	}

	public List<MagicCard> getMultiSelection() {
		int[] viewRow = tableCards.getSelectedRows();
		List<MagicCard> listCards = new ArrayList<>();
		for (int i : viewRow) {
			int modelRow = tableCards.convertRowIndexToModel(i);
			MagicCard mc = (MagicCard) tableCards.getModel().getValueAt(modelRow, 0);
			listCards.add(mc);
		}
		return listCards;
	}

	public MagicCard getSelected() {
		return selectedCard;
	}

	public void initPopupCollection() throws SQLException {
		JMenu menuItemAdd = new JMenu(MTGControler.getInstance().getLangService().getCapitalize("ADD"));

		for (MagicCollection mc : MTGControler.getInstance().getEnabledDAO().getCollections()) {

			JMenuItem adds = new JMenuItem(mc.getName());
			adds.addActionListener(addEvent -> {

				String collec = ((JMenuItem) addEvent.getSource()).getText();
				loading(true, MTGControler.getInstance().getLangService().getCapitalize("ADD_CARDS_TO") + " " + collec);

				for (int i = 0; i < tableCards.getSelectedRowCount(); i++) {

					int viewRow = tableCards.getSelectedRows()[i];
					int modelRow = tableCards.convertRowIndexToModel(viewRow);

					MagicCard mcCard = (MagicCard) tableCards.getModel().getValueAt(modelRow, 0);
					try {
						MTGControler.getInstance().getEnabledDAO().saveCard(mcCard,
								MTGControler.getInstance().getEnabledDAO().getCollection(collec));
					} catch (SQLException e1) {
						logger.error(e1);
						JOptionPane.showMessageDialog(null, e1, MTGControler.getInstance().getLangService().getError(),
								JOptionPane.ERROR_MESSAGE);
					}

				}
				loading(false, "");
			});
			menuItemAdd.add(adds);
		}

		popupMenu.add(menuItemAdd);
	}

	private void initGUI() {
		logger.info("init search GUI");

		JPanel panelResultsCards;
		JPanel panelFilters;
		JPanel panelmana;
		JPanel editionDetailPanel;
		JPanel panneauHaut;
		JPanel panneauCard;
		JPanel panneauStat;
		JTextField txtFilter;
		JComboBox<MagicEdition> cboEdition;
		TableFilterHeader filterHeader;
		JButton btnClear;
		JButton btnSearch;
		JButton btnFilter;
		
		
		DefaultRowSorter<DefaultTableModel, Integer> sorterCards;
		sorterCards = new TableRowSorter<>(cardsModeltable);
		sorterCards.setComparator(7, (String num1, String num2) -> {
			try {
				num1 = num1.replaceAll("a", "").replaceAll("b", "").trim();
				num2 = num2.replaceAll("a", "").replaceAll("b", "").trim();
				if (Integer.parseInt(num1) > Integer.parseInt(num2))
					return 1;
				else
					return -1;
			} catch (NumberFormatException e) {
				return -1;
			}
		});

		List<MagicEdition> li = new ArrayList<>();
		try {
			li = MTGControler.getInstance().getEnabledCardsProviders().loadEditions();
			Collections.sort(li);
		} catch (Exception e2) {
			logger.error("error no edition loaded", e2);
		}

		//////// INIT COMPONENTS
		JScrollPane scrollEditions = new JScrollPane();
		JScrollPane scrollThumbnails = new JScrollPane();
		JScrollPane scrollPaneRules = new JScrollPane();
		JScrollPane scrollCards = new JScrollPane();
		JSplitPane panneauCentral = new JSplitPane();
		panneauStat = new JPanel();
		panneauHaut = new JPanel();
		panneauCard = new JPanel();
		editionDetailPanel = new JPanel();
		panelResultsCards = new JPanel();
		cmcChart = new CmcChartPanel();
		manaRepartitionPanel = new ManaRepartitionPanel();
		typeRepartitionPanel = new TypeRepartitionPanel();
		historyChartPanel = new HistoryPricesPanel();
		cardsPicPanel = new CardsPicPanel();
		priceTablePanel = new PricesTablePanel();
		rarityRepartitionPanel = new RarityRepartitionPanel();
		detailCardPanel = new MagicCardDetailPanel(new MagicCard());
		panelmana = new JPanel();
		panelFilters = new JPanel();
		ManaPanel pan = new ManaPanel();
		panelJson = new JSONPanel();

		tabbedCardsView = new JTabbedPane(JTabbedPane.TOP);
		tabbedCardsInfo = new JTabbedPane(JTabbedPane.TOP);
		thumbnailPanel = new HandPanel();
		thumbnailPanel.setBackground(MTGConstants.THUMBNAIL_BACKGROUND_COLOR);

		btnSearch = new JButton(MTGConstants.ICON_SEARCH);
		btnExport = new JButton(MTGConstants.ICON_EXPORT);
		btnFilter = new JButton(MTGConstants.ICON_FILTER);
		btnClear = new JButton(MTGConstants.ICON_CLEAR);

		cboQuereableItems = new JComboBox<>(new DefaultComboBoxModel<String>(
				MTGControler.getInstance().getEnabledCardsProviders().getQueryableAttributs()));
		try {
			cboCollections = new JComboBox<>(new DefaultComboBoxModel<MagicCollection>(
					MTGControler.getInstance().getEnabledDAO().getCollections().toArray(
							new MagicCollection[MTGControler.getInstance().getEnabledDAO().getCollections().size()])));
		} catch (SQLException e2) {
			logger.error("could not load collections combobox", e2);
		}
		cboLanguages = new JComboBox<>();

		tableCards = new JXTable();

		lblLoading = new JLabel(MTGConstants.ICON_LOADING);
		JLabel lblFilter = new JLabel();

		listEdition = new JList<>();

		txtMagicSearch = new JTextField();
		txtRulesArea = new JTextArea();
		txtFilter = new JTextField();

		filterHeader = new TableFilterHeader(tableCards, AutoChoices.ENABLED);

		cboEdition = new JComboBox<>(new DefaultComboBoxModel<MagicEdition>(li.toArray(new MagicEdition[li.size()])));

		//////// MODELS
		listEdition.setModel(new DefaultListModel<MagicEdition>());
		tableCards.setModel(cardsModeltable);

		//////// RENDERER
		tableCards.getColumnModel().getColumn(2).setCellRenderer(new ManaCellRenderer());
		tableCards.getColumnModel().getColumn(6).setCellRenderer(new EditionCellRenderer());
		cboEdition.setRenderer(new MagicEditionListRenderer());
		listEdition.setCellRenderer(new MagicEditionListRenderer());

		///////// CONFIGURE COMPONENTS
		txtRulesArea.setLineWrap(true);
		txtRulesArea.setWrapStyleWord(true);
		txtRulesArea.setEditable(false);
		btnFilter.setToolTipText(MTGControler.getInstance().getLangService().getCapitalize("FILTER"));
		btnExport.setToolTipText(MTGControler.getInstance().getLangService().getCapitalize("EXPORT_RESULTS"));
		btnExport.setEnabled(false);
		filterHeader.setSelectionBackground(Color.LIGHT_GRAY);
		cboQuereableItems.addItem("collections");
		listEdition.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		thumbnailPanel.enableDragging(false);
		panneauCentral.setOrientation(JSplitPane.VERTICAL_SPLIT);
		panneauCentral.setRightComponent(tabbedCardsInfo);
		panneauCentral.setLeftComponent(tabbedCardsView);
		tableCards.setRowHeight(MTGConstants.TABLE_ROW_HEIGHT);
		tableCards.setRowSorter(sorterCards);

		/////// LAYOUT
		setLayout(new BorderLayout());
		panneauStat.setLayout(new GridLayout(2, 2, 0, 0));
		panneauCard.setLayout(new BorderLayout());
		editionDetailPanel.setLayout(new BorderLayout());
		panelResultsCards.setLayout(new BorderLayout(0, 0));
		panelmana.setLayout(new GridLayout(1, 0, 2, 2));

		FlowLayout flpanelFilters = (FlowLayout) panelFilters.getLayout();
		flpanelFilters.setAlignment(FlowLayout.LEFT);

		FlowLayout flowLayout = (FlowLayout) panneauHaut.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);

		/////// DIMENSION
		thumbnailPanel.setThumbnailSize(new Dimension(179, 240));
		tabbedCardsInfo.setPreferredSize(new Dimension(0, 350));
		historyChartPanel.setPreferredSize(new Dimension(400, 10));
		cardsPicPanel.setPreferredSize(new Dimension(400, 10));
		tabbedCardsInfo.setMinimumSize(new Dimension(23, 200));
		scrollCards.setMinimumSize(new Dimension(0, 0));
		scrollThumbnails.getVerticalScrollBar().setUnitIncrement(10);
		txtFilter.setColumns(25);
		txtMagicSearch.setColumns(35);

		/////// VISIBILITY
		tableCards.setColumnControlVisible(true);
		filterHeader.setVisible(false);
		panelFilters.setVisible(false);
		lblLoading.setVisible(false);
		cboCollections.setVisible(false);
		tableCards.setShowVerticalLines(false);
		cboEdition.setVisible(false);

		////// ADD PANELS
		for (String s : new String[] { "W", "U", "B", "R", "G", "C", "1" }) {
			final JButton btnG = new JButton();
			btnG.setToolTipText(s);
			if (s.equals("1"))
				btnG.setToolTipText("[0-9]*");

			btnG.setIcon(new ImageIcon(pan.getManaSymbol(s).getScaledInstance(15, 15, Image.SCALE_SMOOTH)));
			btnG.setForeground(btnG.getBackground());
			btnG.addActionListener(e -> {
				txtFilter.setText("\\{" + btnG.getToolTipText() + "}");
				sorterCards.setRowFilter(RowFilter.regexFilter(txtFilter.getText()));
			});
			panelmana.add(btnG);

		}
		scrollEditions.setViewportView(listEdition);
		scrollCards.setViewportView(tableCards);
		scrollPaneRules.setViewportView(txtRulesArea);
		scrollThumbnails.setViewportView(thumbnailPanel);

		panneauHaut.add(cboQuereableItems);
		panneauHaut.add(cboCollections);
		panneauHaut.add(txtMagicSearch);
		panneauHaut.add(cboEdition);
		panneauHaut.add(btnSearch);
		panneauHaut.add(btnFilter);
		panneauHaut.add(btnExport);
		panneauHaut.add(lblLoading);

		panneauCard.add(cboLanguages, BorderLayout.NORTH);
		panneauCard.add(scrollEditions, BorderLayout.SOUTH);
		panneauCard.add(cardsPicPanel, BorderLayout.CENTER);

		panelResultsCards.add(panelFilters, BorderLayout.NORTH);
		panelResultsCards.add(scrollCards);
		magicEditionDetailPanel = new MagicEditionDetailPanel();

		editionDetailPanel.add(magicEditionDetailPanel, BorderLayout.CENTER);

		panelFilters.add(lblFilter);
		panelFilters.add(txtFilter);
		panelFilters.add(btnClear);
		panelFilters.add(panelmana);

		tabbedCardsInfo.addTab(MTGControler.getInstance().getLangService().getCapitalize("DETAILS"), MTGConstants.ICON_TAB_DETAILS,
				detailCardPanel, null);
		tabbedCardsInfo.addTab(MTGControler.getInstance().getLangService().getCapitalize("EDITION"), MTGConstants.ICON_BACK,
				editionDetailPanel, null);
		tabbedCardsInfo.addTab(MTGControler.getInstance().getLangService().getCapitalize("PRICES"), MTGConstants.ICON_TAB_PRICES,
				priceTablePanel, null);
		tabbedCardsInfo.addTab(MTGControler.getInstance().getLangService().getCapitalize("RULES"), MTGConstants.ICON_TAB_RULES,
				scrollPaneRules, null);
		tabbedCardsInfo.addTab(MTGControler.getInstance().getLangService().getCapitalize("PRICE_VARIATIONS"), MTGConstants.ICON_TAB_VARIATIONS,
				historyChartPanel, null);

		if (MTGControler.getInstance().get("debug-json-panel").equalsIgnoreCase("true"))
			tabbedCardsInfo.addTab("Json", MTGConstants.ICON_TAB_JSON, panelJson, null);

		panneauStat.add(cmcChart);
		panneauStat.add(manaRepartitionPanel);
		panneauStat.add(typeRepartitionPanel);
		panneauStat.add(rarityRepartitionPanel);

		tabbedCardsView.addTab(MTGControler.getInstance().getLangService().getCapitalize("RESULTS"),  MTGConstants.ICON_TAB_RESULTS,panelResultsCards, null);
		tabbedCardsView.addTab(MTGControler.getInstance().getLangService().getCapitalize("THUMBNAIL"), MTGConstants.ICON_TAB_THUMBNAIL,scrollThumbnails, null);
		tabbedCardsView.addTab(MTGControler.getInstance().getLangService().getCapitalize("STATS"), MTGConstants.ICON_TAB_ANALYSE, panneauStat,null);

		add(panneauHaut, BorderLayout.NORTH);
		add(panneauCard, BorderLayout.EAST);
		add(panneauCentral, BorderLayout.CENTER);

		/////// Right click
		try {
			initPopupCollection();
		} catch (Exception e2) {
			logger.error(e2);
		}

		/////// Action listners

		cboEdition
				.addActionListener(ae -> txtMagicSearch.setText(((MagicEdition) cboEdition.getSelectedItem()).getId()));

		btnClear.addActionListener(ae -> {
			txtFilter.setText("");
			sorterCards.setRowFilter(null);
		});

		btnFilter.addActionListener(ae -> {
			if (panelFilters.isVisible()) {
				panelFilters.setVisible(false);
				filterHeader.setVisible(false);
			} else {
				panelFilters.setVisible(true);
				filterHeader.setVisible(true);
			}
		});

		cboQuereableItems.addActionListener(e -> {
			if (cboQuereableItems.getSelectedItem().toString().equalsIgnoreCase("set")) {
				txtMagicSearch.setVisible(false);
				cboEdition.setVisible(true);
				cboCollections.setVisible(false);
			} else if (cboQuereableItems.getSelectedItem().toString().equalsIgnoreCase("collections")) {
				txtMagicSearch.setVisible(false);
				cboEdition.setVisible(false);
				cboCollections.setVisible(true);
			} else {
				txtMagicSearch.setVisible(true);
				cboEdition.setVisible(false);
				cboCollections.setVisible(false);
			}
		});

		btnSearch.addActionListener(ae -> {

			Observer ob = new Observer() {
				@Override
				public void update(Observable o, Object arg) {
					cardsModeltable.addCard((MagicCard) arg);
				}
			};
			selectedEdition = null;
			if (txtMagicSearch.getText().equals("") && !cboCollections.isVisible())
				return;
			
			
			
			cardsModeltable.clear();
			new SwingWorker<Object, Object>() {
				protected Void doInBackground() {
					loading(true, MTGControler.getInstance().getLangService().getCapitalize("SEARCHING"));
					String searchName = txtMagicSearch.getText();
					try {
						List<MagicCard> cards;
						MTGControler.getInstance().getEnabledCardsProviders().addObserver(ob);

						if (cboCollections.isVisible())
							cards = MTGControler.getInstance().getEnabledDAO()
									.listCardsFromCollection((MagicCollection) cboCollections.getSelectedItem());
						else
							cards = MTGControler.getInstance().getEnabledCardsProviders().searchCardByCriteria(
									cboQuereableItems.getSelectedItem().toString(), searchName, null, false);

						Collections.sort(cards, new CardsEditionSorter());

						open(cards);
						return null;
					} catch (Exception e) {
						logger.error("Erreur search", e);
						return null;
					}

				}

				@Override
				protected void done() {
					super.done();
					loading(false, "");
					cardsModeltable.fireTableDataChanged();
					btnExport.setEnabled(tableCards.getRowCount() > 0);
					MTGControler.getInstance().getEnabledCardsProviders().removeObserver(ob);

				}
			}.execute();
		});

		tableCards.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {

				if (SwingUtilities.isRightMouseButton(evt)) {
					Point point = evt.getPoint();
					popupMenu.show(tableCards, (int) point.getX(), (int) point.getY());
				} else {
					try {
						selectedCard = (MagicCard) tableCards.getValueAt(tableCards.getSelectedRow(), 0);
						selectedEdition = selectedCard.getCurrentSet();
						updateCards();
					} catch (Exception e) {
						logger.error(e);
					}

				}
			}
		});

		listEdition.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent mev) {
				selectedEdition = listEdition.getSelectedValue();
				ThreadManager.getInstance().execute(() -> {
					loading(true, MTGControler.getInstance().getLangService().getCapitalize("LOADING_EDITIONS"));
					try {
						selectedCard = MTGControler.getInstance().getEnabledCardsProviders()
								.searchCardByCriteria("name", selectedCard.getName(), selectedEdition, false).get(0);
						detailCardPanel.setMagicCard(selectedCard);
						magicEditionDetailPanel.setMagicEdition(selectedEdition);

					} catch (Exception e) {
						logger.error(e);
					}
					cardsPicPanel.showPhoto(selectedCard);// backcard
					historyChartPanel.init(selectedCard, selectedEdition, selectedCard.getName());

					if (tabbedCardsInfo.getSelectedIndex() == INDEX_PRICES)
						priceTablePanel.init(selectedCard,selectedEdition);

					loading(false, "");
				}, "changeEdition");
			}
		});
	
		cboLanguages.addItemListener(e -> {

			MagicCardNames selLang = (MagicCardNames) cboLanguages.getSelectedItem();
			try {

				if (e.getStateChange() == ItemEvent.SELECTED && selLang != null) {
					MagicEdition ed = (MagicEdition) BeanUtils.cloneBean(selectedEdition);
					ed.setMultiverseid("" + selLang.getGathererId());

					logger.debug("change lang to " + selLang + " for " + ed);
					cardsPicPanel.showPhoto(selectedCard, ed);
				}

			} catch (Exception e1) {
				logger.error(e1);
			}
		});

		btnExport.addActionListener(ae -> {
			JPopupMenu menu = new JPopupMenu();

			for (final MTGCardsExport exp : MTGControler.getInstance().getEnabledDeckExports()) {
				if (exp.getMods() == MODS.BOTH || exp.getMods() == MODS.EXPORT) {
					JMenuItem it = new JMenuItem();
					it.setIcon(exp.getIcon());
					it.setText(exp.getName());
					it.addActionListener(exportEvent -> {
						JFileChooser jf = new JFileChooser(".");
						jf.setSelectedFile(new File("search" + exp.getFileExtension()));
						int result = jf.showSaveDialog(null);
						final File f = jf.getSelectedFile();

						if (result == JFileChooser.APPROVE_OPTION)
							ThreadManager.getInstance().execute(() -> {
								try {
									loading(true, "export " + exp);

									List<MagicCard> export = ((MagicCardTableModel) tableCards.getRowSorter().getModel())
											.getListCards();
									exp.export(export, f);
									loading(false, "");
									JOptionPane.showMessageDialog(null,
											MTGControler.getInstance().getLangService().combine("EXPORT", "FINISHED"),
											exp.getName() + " "
													+ MTGControler.getInstance().getLangService().get("FINISHED"),
											JOptionPane.INFORMATION_MESSAGE);
								} catch (Exception e) {
									logger.error(e);
									loading(false, "");
									JOptionPane.showMessageDialog(null, e,
											MTGControler.getInstance().getLangService().getError(),
											JOptionPane.ERROR_MESSAGE);
								}
							}, "export search " + exp);
					});

					menu.add(it);
				}
				
			}

			Component b = (Component) ae.getSource();
			Point p = b.getLocationOnScreen();
			menu.show(b, 0, 0);
			menu.setLocation(p.x, p.y + b.getHeight());
		});

		tabbedCardsInfo.addChangeListener(e -> {
			if (tabbedCardsInfo.getSelectedIndex() == INDEX_PRICES)
				priceTablePanel.init(selectedCard,selectedEdition);
		});

		txtFilter.addActionListener(ae -> {
			String text = txtFilter.getText();
			if (text.length() == 0) {
				sorterCards.setRowFilter(null);
			} else {
				sorterCards.setRowFilter(RowFilter.regexFilter(text));
			}
		});

		thumbnailPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				DisplayableCard lab = (DisplayableCard) thumbnailPanel.getComponentAt(new Point(e.getX(), e.getY()));
				selectedCard = lab.getMagicCard();
				selectedEdition = lab.getMagicCard().getCurrentSet();
				cardsPicPanel.showPhoto(selectedCard);
				updateCards();
			}

		});

		txtMagicSearch.addActionListener(e -> btnSearch.doClick());

	}

	public void thumbnail(List<MagicCard> cards) {
		tabbedCardsView.setSelectedIndex(INDEX_THUMB);
		thumbnailPanel.initThumbnails(cards, false, false);
	}

	public void setSelectedCard(MagicCard mc) {
		this.selectedCard = mc;
		updateCards();
	}

	public CardSearchPanel() {

		try {
			cardsModeltable = new MagicCardTableModel();
			initGUI();
		} catch (Exception e) {
			logger.error("Error init", e);
			JOptionPane.showMessageDialog(null, e, "ERROR", JOptionPane.ERROR_MESSAGE);
		}

		logger.debug("construction of GUI : done");
	}

	public HandPanel getThumbnailPanel() {
		return thumbnailPanel;
	}

	public void updateCards() {
		try {
			cboLanguages.removeAllItems();
			txtRulesArea.setText("");

			((DefaultListModel<MagicEdition>) listEdition.getModel()).removeAllElements();

			for (MagicCardNames mcn : selectedCard.getForeignNames())
				cboLanguages.addItem(mcn);

			for (MagicEdition me : selectedCard.getEditions())
				((DefaultListModel<MagicEdition>) listEdition.getModel()).addElement(me);

			detailCardPanel.setMagicCard(selectedCard, true);
			magicEditionDetailPanel.setMagicEdition(selectedCard.getCurrentSet());

			for (MagicRuling mr : selectedCard.getRulings()) {
				txtRulesArea.append(mr.toString());
				txtRulesArea.append("\n");
			}

			if (tabbedCardsInfo.getSelectedIndex() == INDEX_PRICES)
				priceTablePanel.init(selectedCard,selectedEdition);

			panelJson.show(selectedCard);

			ThreadManager.getInstance().execute(
					() -> historyChartPanel.init(selectedCard, selectedEdition, selectedCard.getName()),
					"load history for " + selectedEdition);

		} catch (Exception e1) {
			logger.error(e1);
		}

	}

	public void open(List<MagicCard> cards) {
		logger.debug("results " + cards.size() + " cards");

		if (!cards.isEmpty()) {
			cardsModeltable.init(cards);
			thumbnailPanel.initThumbnails(cards, false, false);
			cmcChart.init(cards);
			typeRepartitionPanel.init(cards);
			manaRepartitionPanel.init(cards);
			rarityRepartitionPanel.init(cards);
			tabbedCardsView.setTitleAt(0, MTGControler.getInstance().getLangService().getCapitalize("RESULTS") + " ("
					+ cardsModeltable.getRowCount() + ")");
			btnExport.setEnabled(true);
		}
	}

}
