package org.magic.gui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXSearchField;
import org.jdesktop.swingx.JXTable;
import org.magic.api.beans.MTGNotification;
import org.magic.api.beans.MTGNotification.MESSAGE_TYPE;
import org.magic.api.beans.MagicCard;
import org.magic.api.beans.MagicDeck;
import org.magic.api.beans.MagicFormat;
import org.magic.api.interfaces.MTGCardsExport;
import org.magic.api.interfaces.MTGCardsIndexer;
import org.magic.api.interfaces.MTGCardsProvider;
import org.magic.api.interfaces.abstracts.AbstractCardExport.MODS;
import org.magic.game.gui.components.HandPanel;
import org.magic.game.model.Player;
import org.magic.gui.abstracts.AbstractBuzyIndicatorComponent;
import org.magic.gui.components.charts.CmcChartPanel;
import org.magic.gui.components.charts.DrawProbabilityPanel;
import org.magic.gui.components.charts.ManaRepartitionPanel;
import org.magic.gui.components.charts.RarityRepartitionPanel;
import org.magic.gui.components.charts.TypeRepartitionPanel;
import org.magic.gui.components.dialog.JDeckChooserDialog;
import org.magic.gui.models.DeckCardsTableModel;
import org.magic.gui.renderer.IntegerCellEditor;
import org.magic.gui.renderer.MagicCardListRenderer;
import org.magic.gui.renderer.MagicEditionsComboBoxCellEditor;
import org.magic.gui.renderer.MagicEditionsComboBoxCellRenderer;
import org.magic.gui.renderer.ManaCellRenderer;
import org.magic.services.MTGConstants;
import org.magic.services.MTGControler;
import org.magic.services.MTGDeckManager;
import org.magic.services.MTGLogger;
import org.magic.services.ThreadManager;
import org.magic.tools.UITools;

public class ConstructPanel extends JPanel {

	
	private static final long serialVersionUID = 1L;
	private static final String UPDATED_DECK = "UPDATED_DECK";
	private static final String FINISHED = "FINISHED";

	private DeckDetailsPanel deckDetailsPanel;
	private CmcChartPanel cmcChartPanel;
	private ManaRepartitionPanel manaRepartitionPanel;
	private TypeRepartitionPanel typeRepartitionPanel;
	private RarityRepartitionPanel rarityRepartitionPanel;
	private MagicCardDetailPanel magicCardDetailPanel;
	private DrawProbabilityPanel drawProbabilityPanel;
	private DeckPricePanel deckPricePanel;
	private DeckCardsTableModel deckSidemodel;
	private DeckCardsTableModel deckmodel;
	private MagicDeck deck;
	private JButton btnExports;
	private transient MTGDeckManager deckManager;
	private DefaultListModel<MagicCard> resultListModel = new DefaultListModel<>();
	private JList<MagicCard> listResult;
	private DrawProbabilityPanel cardDrawProbaPanel;
	public static final int MAIN = 0;
	public static final int SIDE = 1;
	protected int selectedIndex = 0;
	private File exportedFile;
	private transient Logger logger = MTGLogger.getLogger(this.getClass());
	private File f;
	private Player p;
	private JLabel lblCards;
	private DeckStockComparatorPanel stockPanel;
	
	public ConstructPanel() {
		deck = new MagicDeck();
		deckManager = new MTGDeckManager();
		initGUI();
		setDeck(deck);
	}

	public void setDeck(MagicDeck deck) {
		this.deck = deck;
		deckDetailsPanel.setMagicDeck(deck);
		deckDetailsPanel.updatePicture();
		deckmodel.init(deck);
		stockPanel.setCurrentDeck(deck);
		p = new Player(deck);
	}

	private void initGUI() {
		setLayout(new BorderLayout(0, 0));

		
		
		JPanel panneauHaut = new JPanel();
		JButton btnUpdate;
		HandPanel thumbnail;
		JPanel panelBottom;
		JXSearchField txtSearch;
		JComboBox<String> cboAttributs;
		JTabbedPane tabbedPane;
		ButtonGroup groupsFilterResult;
		AbstractBuzyIndicatorComponent buzyLabel = AbstractBuzyIndicatorComponent.createProgressComponent();
		deckmodel = new DeckCardsTableModel(DeckCardsTableModel.TYPE.DECK);
		deckSidemodel = new DeckCardsTableModel(DeckCardsTableModel.TYPE.SIDE);
		deckDetailsPanel = new DeckDetailsPanel();
		panelBottom = new JPanel();
		thumbnail = new HandPanel();
		FlowLayout flowLayout = (FlowLayout) panneauHaut.getLayout();
		cboAttributs = UITools.createCombobox(MTGControler.getInstance().getEnabled(MTGCardsProvider.class).getQueryableAttributs());	
		txtSearch = new JXSearchField(MTGControler.getInstance().getLangService().getCapitalize("SEARCH_MODULE"));
		lblCards = new JLabel();
		JButton btnNewDeck = new JButton(MTGConstants.ICON_NEW);
		JButton btnOpen = new JButton(MTGConstants.ICON_OPEN);
		btnUpdate = new JButton();
		JButton btnSave = new JButton(MTGConstants.ICON_SAVE);
		JButton btnImport = new JButton(MTGConstants.ICON_IMPORT);
		btnExports = new JButton();
		stockPanel = new DeckStockComparatorPanel();
		
		
		
		
		thumbnail.setThumbnailSize(new Dimension(223, 311));
		thumbnail.enableDragging(false);
		thumbnail.setMaxCardsRow(4);
		flowLayout.setAlignment(FlowLayout.LEFT);
		txtSearch.setSearchMode(MTGConstants.SEARCH_MODE);
		txtSearch.setBackground(Color.WHITE);

		
		
		add(panneauHaut, BorderLayout.NORTH);
		panneauHaut.add(cboAttributs);


		panneauHaut.add(txtSearch);
		txtSearch.setColumns(25);

		panneauHaut.add(lblCards);

		btnNewDeck.setToolTipText(MTGControler.getInstance().getLangService().getCapitalize("CREATE_NEW_DECK"));

		panneauHaut.add(btnNewDeck);

		btnNewDeck.addActionListener(newDeckEvent -> {

			MagicDeck newDeck = new MagicDeck();
			setDeck(newDeck);
			deckmodel.init(newDeck);
			deckSidemodel.init(newDeck);
		});

		btnOpen.setToolTipText(MTGControler.getInstance().getLangService().getCapitalize("OPEN_DECK"));
		panneauHaut.add(btnOpen);

		btnOpen.addActionListener(openEvent -> {
			try {
				JDeckChooserDialog choose = new JDeckChooserDialog();
				choose.setVisible(true);
				deck = choose.getSelectedDeck();
				if (deck != null) {
					deckDetailsPanel.setMagicDeck(deck);
					deckmodel.init(deck);
					deckSidemodel.init(deck);
					setDeck(deck);
					updatePanels();
				}
			} catch (Exception ex) {
				logger.error(ex);
				MTGControler.getInstance()
						.notify(new MTGNotification(MTGControler.getInstance().getLangService().getError(), ex));
			}

		});

		btnUpdate.setToolTipText(MTGControler.getInstance().getLangService().getCapitalize("UPDATE_DECK"));
		btnUpdate.addActionListener(updateEvent -> ThreadManager.getInstance().execute(() -> {

			Map<MagicCard, Integer> updateM = new HashMap<>();
			Map<MagicCard, Integer> updateS = new HashMap<>();

			btnUpdate.setEnabled(false);
			buzyLabel.start(deck.getMap().size() + deck.getMapSideBoard().size());
			for (MagicCard mc : deck.getMap().keySet()) {
				try {
					updateM.put(MTGControler.getInstance().getEnabled(MTGCardsProvider.class).searchCardByName(mc.getName(), mc.getCurrentSet(), true).get(0),deck.getMap().get(mc));
					buzyLabel.progress();
				} catch (Exception e) {
					logger.error(e);
					btnUpdate.setEnabled(true);
					buzyLabel.end();
				}
			}
			for (MagicCard mc : deck.getMapSideBoard().keySet()) {
				try {
					updateS.put(MTGControler.getInstance().getEnabled(MTGCardsProvider.class).searchCardByName(mc.getName(), mc.getCurrentSet(), true).get(0),deck.getMapSideBoard().get(mc));
					buzyLabel.progress();
				} catch (Exception e) {
					btnUpdate.setEnabled(true);
					buzyLabel.end();
				}
			}

			deck.getMap().clear();
			deck.setMapDeck(updateM);

			deck.getMapSideBoard().clear();
			deck.setMapSideBoard(updateS);

			updatePanels();

			btnUpdate.setEnabled(true);
			buzyLabel.end();
			MTGControler.getInstance()
					.notify(new MTGNotification(MTGControler.getInstance().getLangService().getCapitalize(FINISHED),
							MTGControler.getInstance().getLangService().getCapitalize(UPDATED_DECK),
							MESSAGE_TYPE.INFO));
		}, "Update Deck"));
		btnUpdate.setIcon(MTGConstants.ICON_REFRESH);

		panneauHaut.add(btnUpdate);

		btnSave.setToolTipText(MTGControler.getInstance().getLangService().getCapitalize("SAVE_DECK"));
		panneauHaut.add(btnSave);

		btnSave.addActionListener(e -> {
			try {
				logger.debug("saving " + deck);
				String name = JOptionPane.showInputDialog(MTGControler.getInstance().getLangService().getCapitalize("DECK_NAME") + " ?", deck.getName());
				
				if(name!=null && !name.isEmpty())
				{
					deck.setName(name);
					deckManager.saveDeck(deck);
				}
			} catch (Exception ex) {
				logger.error("error saving", ex);
				MTGControler.getInstance()
						.notify(new MTGNotification(MTGControler.getInstance().getLangService().getError(), ex));
			}

		});

			btnImport.setToolTipText(MTGControler.getInstance().getLangService().getCapitalize("DECK_IMPORT_AS"));

		btnImport.addActionListener(ae -> {
			JPopupMenu menu = new JPopupMenu();
			for (final MTGCardsExport exp : MTGControler.getInstance().listEnabled(MTGCardsExport.class)) {
				if (exp.getMods() == MODS.BOTH || exp.getMods() == MODS.IMPORT) {

					JMenuItem it = new JMenuItem();
					it.setIcon(exp.getIcon());
					it.setText(exp.getName());
					it.addActionListener(itEvent -> {
						JFileChooser jf = new JFileChooser(".");
						jf.setFileFilter(new FileFilter() {
							@Override
							public String getDescription() {
								return exp.getName();
							}

							@Override
							public boolean accept(File f) {
								return (f.isDirectory() || f.getName().endsWith(exp.getFileExtension()));
							}
						});

						int res = -1;
						f = new File("");

						if (!exp.needDialogGUI()) {
							res = jf.showOpenDialog(null);
							f = jf.getSelectedFile();
						} else {
							res = JFileChooser.APPROVE_OPTION;

						}

						if (res == JFileChooser.APPROVE_OPTION)
							ThreadManager.getInstance().execute(() -> {
								try {
									buzyLabel.start();
									buzyLabel.setText(MTGControler.getInstance().getLangService().get("LOADING_FILE",f.getName(), exp));
									exp.addObserver(buzyLabel);
									
									deck = exp.importDeck(f);

									MTGControler.getInstance()
											.notify(new MTGNotification(
													MTGControler.getInstance().getLangService().getCapitalize(FINISHED),
													exp.getName() + " "
															+ MTGControler.getInstance().getLangService().get(FINISHED),
													MESSAGE_TYPE.INFO));
									setDeck(deck);
									buzyLabel.end();
									deckmodel.init(deck);
									deckSidemodel.init(deck);
									setDeck(deck);
									updatePanels();

								} catch (Exception e) {
									logger.error("error import", e);
									buzyLabel.end();
									MTGControler.getInstance().notify(new MTGNotification(
											MTGControler.getInstance().getLangService().getError(), e));
								}
								finally {
									exp.removeObserver(buzyLabel);
								}

							}, "import " + exp);

					});

					menu.add(it);

				}
			}

			Component b = (Component) ae.getSource();
			Point point = b.getLocationOnScreen();
			menu.show(b, 0, 0);
			menu.setLocation(point.x, point.y + b.getHeight());

		});

		panneauHaut.add(btnImport);

		btnExports.setEnabled(false);
		btnExports.setToolTipText(MTGControler.getInstance().getLangService().getCapitalize("EXPORT_AS"));
		btnExports.setIcon(MTGConstants.ICON_EXPORT);

		btnExports.addActionListener(exportsAction -> {
			JPopupMenu menu = new JPopupMenu();

			for (final MTGCardsExport exp : MTGControler.getInstance().listEnabled(MTGCardsExport.class)) {
				if (exp.getMods() == MODS.BOTH || exp.getMods() == MODS.EXPORT) {
					JMenuItem it = new JMenuItem();
					it.setIcon(exp.getIcon());
					it.setText(exp.getName());
					it.addActionListener(pluginExportEvent -> {
						JFileChooser jf = new JFileChooser(".");
						jf.setSelectedFile(new File(deck.getName() + exp.getFileExtension()));
						jf.showSaveDialog(null);
						exportedFile = jf.getSelectedFile();
						ThreadManager.getInstance().execute(() -> {
							try {
								buzyLabel.start(deck.getAsList().size());
								buzyLabel.setText(MTGControler.getInstance().getLangService().get("EXPORT_TO", deck, exp));
								exp.addObserver(buzyLabel);
								exp.export(deck, exportedFile);
								MTGControler.getInstance()
										.notify(new MTGNotification(
												exp.getName() + " "
														+ MTGControler.getInstance().getLangService()
																.getCapitalize(FINISHED),
												MTGControler.getInstance().getLangService().combine("EXPORT", FINISHED),
												MESSAGE_TYPE.INFO));
								buzyLabel.end();
							} catch (Exception e) {
								logger.error("error", e);
								buzyLabel.end();
								MTGControler.getInstance().notify(
										new MTGNotification(MTGControler.getInstance().getLangService().getError(), e));
							}
							finally
							{
								exp.removeObserver(buzyLabel);
							}
						}, "Export " + deck + " to " + exp.getName());
					});
					menu.add(it);
				}
			}

			Component b = (Component) exportsAction.getSource();
			Point point = b.getLocationOnScreen();
			menu.show(b, 0, 0);
			menu.setLocation(point.x, point.y + b.getHeight());

		});
		panneauHaut.add(btnExports);

		panneauHaut.add(buzyLabel);

		JPanel panneauBas = new JPanel();
		add(panneauBas, BorderLayout.SOUTH);

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);

		add(tabbedPane, BorderLayout.CENTER);

		JSplitPane panneauDeck = new JSplitPane();
		panneauDeck.setDividerLocation(0.5);
		panneauDeck.setResizeWeight(0.5);

		panneauDeck.setOrientation(JSplitPane.VERTICAL_SPLIT);
		tabbedPane.addTab(MTGControler.getInstance().getLangService().getCapitalize("DECK"), MTGConstants.ICON_TAB_DECK,
				panneauDeck, null);
		

		magicCardDetailPanel = new MagicCardDetailPanel();
		magicCardDetailPanel.enableThumbnail(true);
		panelBottom.setLayout(new BorderLayout(0, 0));
		panelBottom.add(magicCardDetailPanel);
		panneauDeck.setRightComponent(panelBottom);

		cardDrawProbaPanel = new DrawProbabilityPanel();
		panelBottom.add(cardDrawProbaPanel, BorderLayout.EAST);

		final JTabbedPane tabbedDeckSide = new JTabbedPane(JTabbedPane.RIGHT);

		panneauDeck.setLeftComponent(tabbedDeckSide);
		
		

		JXTable tableDeck = new JXTable();
		JXTable tableSide = new JXTable();
		tabbedDeckSide.addTab("Main", MTGConstants.ICON_TAB_DECK, new JScrollPane(tableDeck), null);
		tabbedDeckSide.addTab("SideBoard", MTGConstants.ICON_TAB_DECK, new JScrollPane(tableSide), null);
		
		initTables(tableDeck,MAIN,deckmodel);
		initTables(tableSide,SIDE,deckSidemodel);

		

		JPanel panelInfoDeck = new JPanel();
		tabbedPane.addTab(MTGControler.getInstance().getLangService().getCapitalize("INFORMATIONS"),
				MTGConstants.ICON_TAB_DETAILS, panelInfoDeck, null);
		panelInfoDeck.setLayout(new BorderLayout(0, 0));

		panelInfoDeck.add(deckDetailsPanel, BorderLayout.NORTH);
		deckDetailsPanel.setMagicDeck(deck);

		cmcChartPanel = new CmcChartPanel();
		manaRepartitionPanel = new ManaRepartitionPanel();
		typeRepartitionPanel = new TypeRepartitionPanel();
		rarityRepartitionPanel = new RarityRepartitionPanel();
		drawProbabilityPanel = new DrawProbabilityPanel();

		JPanel randomHandPanel = new JPanel();
		JPanel statPanel = new JPanel();

		randomHandPanel.setLayout(new BorderLayout(0, 0));
		randomHandPanel.add(thumbnail, BorderLayout.CENTER);

		statPanel.setLayout(new GridLayout(3, 2, 0, 0));
		statPanel.add(manaRepartitionPanel);
		statPanel.add(typeRepartitionPanel);
		statPanel.add(rarityRepartitionPanel);
		statPanel.add(cmcChartPanel);
		statPanel.add(drawProbabilityPanel);
		
		tabbedPane.addTab(MTGControler.getInstance().getLangService().getCapitalize("STATS"),
				MTGConstants.ICON_TAB_ANALYSE, statPanel, null);

		deckPricePanel = new DeckPricePanel();
		statPanel.add(deckPricePanel);
		tabbedPane.addTab(MTGControler.getInstance().getLangService().getCapitalize("SAMPLE_HAND"),MTGConstants.ICON_TAB_THUMBNAIL, randomHandPanel, null);
		tabbedPane.addTab(MTGControler.getInstance().getLangService().getCapitalize("STOCK_MODULE"),MTGConstants.ICON_TAB_STOCK, stockPanel, null);
		

		JPanel panel = new JPanel();
		randomHandPanel.add(panel, BorderLayout.NORTH);

		JButton btnDrawAHand = new JButton(MTGControler.getInstance().getLangService().getCapitalize("DRAW_HAND"));
		btnDrawAHand.addActionListener(ae -> {
			thumbnail.removeAll();
			p.mixHandAndLibrary();
			p.shuffleLibrary();
			p.drawCard(7);
			thumbnail.initThumbnails(p.getHand().getCards(), false, false);

		});
		panel.add(btnDrawAHand);

		JPanel panneauGauche = new JPanel();
		add(panneauGauche, BorderLayout.WEST);
		panneauGauche.setLayout(new BorderLayout(0, 0));

	

		listResult = new JList<>(new DefaultListModel<MagicCard>());
		listResult.setCellRenderer(new MagicCardListRenderer());
		listResult.setMinimumSize(new Dimension(100, 0));
		listResult.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		panneauGauche.add(new JScrollPane(listResult));
		
		
		JPanel panneauResultFilter = new JPanel();
		panneauGauche.add(panneauResultFilter, BorderLayout.NORTH);

		groupsFilterResult = new ButtonGroup() {
			private static final long serialVersionUID = 1L;

			@Override
			public void setSelected(ButtonModel model, boolean selected) {
				if (selected) {
					super.setSelected(model, selected);
				} else {
					clearSelection();
				}
			}
		};

		JToggleButton tglbtnStd = new JToggleButton("STD");
		tglbtnStd.setActionCommand("Standard");
		panneauResultFilter.add(tglbtnStd);

		JToggleButton tglbtnMdn = new JToggleButton("MDN");
		tglbtnMdn.setActionCommand("Modern");
		panneauResultFilter.add(tglbtnMdn);

		JToggleButton tglbtnLeg = new JToggleButton("LEG");
		tglbtnLeg.setActionCommand("Legacy");
		panneauResultFilter.add(tglbtnLeg);

		JToggleButton tglbtnVin = new JToggleButton("VIN");
		tglbtnVin.setActionCommand("Vintage");
		panneauResultFilter.add(tglbtnVin);

		groupsFilterResult.add(tglbtnStd);
		groupsFilterResult.add(tglbtnMdn);
		groupsFilterResult.add(tglbtnLeg);
		groupsFilterResult.add(tglbtnVin);

		listResult.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent ev) {

				if (ev.getClickCount() == 1 && !ev.isConsumed()) {
					ev.consume();
					MagicCard mc = listResult.getSelectedValue();
					magicCardDetailPanel.setMagicCard(mc);

				}

				if (ev.getClickCount() == 2 && !ev.isConsumed()) {
					ev.consume();

					MagicCard mc = listResult.getSelectedValue();

					if (getSelectedMap().get(mc) != null) {
						getSelectedMap().put(mc, deck.getMap().get(mc) + 1);
					} else {
						getSelectedMap().put(mc, 1);
					}
					deckmodel.init(deck);
					deckSidemodel.init(deck);
				}
			}
		});

		tabbedDeckSide.addChangeListener(e -> selectedIndex = tabbedDeckSide.getSelectedIndex());

		txtSearch.addActionListener(aeSearch -> {

			if (txtSearch.getText().equals(""))
				return;

			resultListModel.removeAllElements();

			ThreadManager.getInstance().execute(() -> {
				try {
					String searchName = txtSearch.getText();
					List<MagicCard> cards = MTGControler.getInstance().getEnabled(MTGCardsProvider.class)
							.searchCardByCriteria(cboAttributs.getSelectedItem().toString(), searchName, null, false);
					MagicFormat form = new MagicFormat();

					for (MagicCard m : cards) {
						if (groupsFilterResult.getSelection() != null) {
							form.setFormat(groupsFilterResult.getSelection().getActionCommand());
							if (m.getLegalities().contains(form))
								resultListModel.addElement(m);
						} else {
							resultListModel.addElement(m);
						}
					}
					lblCards.setText(
							resultListModel.size() + " " + MTGControler.getInstance().getLangService().get("RESULTS"));
					listResult.setModel(resultListModel);
					listResult.updateUI();

				} catch (Exception e) {
					MTGControler.getInstance()
							.notify(new MTGNotification(MTGControler.getInstance().getLangService().getError(), e));
				}

			}, "search deck");

		});
	}

	private void initTables(JXTable table, int f, DeckCardsTableModel model) {
		table.setModel(model);
		table.setRowSorter(new TableRowSorter<DefaultTableModel>(model));
		table.setRowHeight(MTGConstants.TABLE_ROW_HEIGHT);
		table.getColumnModel().getColumn(2).setCellRenderer(new ManaCellRenderer());
		table.getColumnModel().getColumn(3).setCellRenderer(new MagicEditionsComboBoxCellRenderer());
		table.getColumnModel().getColumn(3).setCellEditor(new MagicEditionsComboBoxCellEditor());
		table.getColumnModel().getColumn(4).setCellEditor(new IntegerCellEditor());
		
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent ev) {
				
				MagicCard mc = (MagicCard) UITools.getTableSelection(table, 0).get(0);
				magicCardDetailPanel.setMagicCard(mc);
				
				if(f==MAIN)
					cardDrawProbaPanel.init(deck, mc);
				
				if(SwingUtilities.isRightMouseButton(ev))
				{
					JPopupMenu menu = new JPopupMenu();
					JMenuItem item = new JMenuItem(MTGControler.getInstance().getLangService().getCapitalize("MORE_LIKE_THIS"));
					menu.add(item);
					item.addActionListener(ae->{
						
						resultListModel.removeAllElements();
						listResult.updateUI();
						
						
						try {
							for(MagicCard card : MTGControler.getInstance().getEnabled(MTGCardsIndexer.class).similarity(mc).keySet())
								resultListModel.addElement(card);
						
							lblCards.setText(resultListModel.size() + " " + MTGControler.getInstance().getLangService().get("RESULTS"));
							listResult.setModel(resultListModel);
							listResult.updateUI();
							
						} catch (IOException e) {
							logger.error(e);
						}
					});
					Point point = ev.getPoint();
					menu.show(table, (int) point.getX(), (int) point.getY());
					
				}
				
				
			}
		});
		
		table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				MagicCard mc = (MagicCard) table.getValueAt(table.getSelectedRow(), 0);
				if (e.getKeyCode() == 0) {
					if(f==MAIN)
						deck.getMap().remove(mc);
					else
						deck.getMapSideBoard().remove(mc);
					
					deckmodel.init(deck);
				}
			}
		});
		
		table.getModel().addTableModelListener(e -> updatePanels());

		table.getDefaultEditor(String.class).addCellEditorListener(new CellEditorListener() {

			@Override
			public void editingStopped(ChangeEvent e) {
				updatePanels();

			}

			@Override
			public void editingCanceled(ChangeEvent e) {
				updatePanels();

			}
		});
	}

	public Map<MagicCard, Integer> getSelectedMap() {
		if (selectedIndex > 0)
			return deck.getMapSideBoard();
		else
			return deck.getMap();

	}

	protected void updatePanels() {

		deckDetailsPanel.setMagicDeck(deck);
		cmcChartPanel.init(deck);
		typeRepartitionPanel.init(deck.getAsList());
		manaRepartitionPanel.init(deck);
		rarityRepartitionPanel.init(deck);
		deckPricePanel.initDeck(deck);
		drawProbabilityPanel.init(deck);
		btnExports.setEnabled(!deck.getAsList().isEmpty());

	}
}
