package org.magic.gui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;

import org.jsoup.Jsoup;
import org.magic.api.beans.MTGNotification;
import org.magic.api.beans.MTGStory;
import org.magic.gui.abstracts.AbstractBuzyIndicatorComponent;
import org.magic.gui.abstracts.MTGUIComponent;
import org.magic.gui.renderer.MTGStoryListRenderer;
import org.magic.services.MTGConstants;
import org.magic.services.MTGControler;
import org.magic.services.ThreadManager;
import org.magic.services.extra.StoryProvider;

public class StoriesGUI extends MTGUIComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private AbstractBuzyIndicatorComponent lblLoading;
	private transient StoryProvider provider;
	private JList<MTGStory> listResult;
	private DefaultListModel<MTGStory> resultListModel;
	private JEditorPane editorPane;

	@Override
	public ImageIcon getIcon() {
		return MTGConstants.ICON_STORY;
	}
	
	@Override
	public String getTitle() {
		return MTGControler.getInstance().getLangService().getCapitalize("HISTORY_MODULE");
	}
	
	
	public StoriesGUI() {
		provider = new StoryProvider(MTGControler.getInstance().getLocale());

		initGUI();
		initStories();
	}

	private void initGUI() {
		JScrollPane scrollList = new JScrollPane();
		JScrollPane scrollEditor = new JScrollPane();

		setLayout(new BorderLayout(0, 0));
		resultListModel = new DefaultListModel<>();

		listResult = new JList<>(resultListModel);
		listResult.setCellRenderer(new MTGStoryListRenderer());
		listResult.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listResult.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() == 1) {
					evt.consume();

					ThreadManager.getInstance().execute(() -> {
						lblLoading.start();
						try {
							editorPane.setText(Jsoup.connect(listResult.getSelectedValue().getUrl().toString()).get()
									.select("div#content-detail-page-of-an-article").html());

						} catch (Exception e) {
							MTGControler.getInstance().notify(new MTGNotification(MTGControler.getInstance().getLangService().getError(),e));
						}
						lblLoading.end();
					}, "Load story");
				} else {
					try {
						Desktop.getDesktop().browse(listResult.getSelectedValue().getUrl().toURI());
					} catch (Exception e) {
						MTGControler.getInstance().notify(new MTGNotification(MTGControler.getInstance().getLangService().getError(),e));
					}
				}
			}
		});

		scrollList.setViewportView(listResult);

		JPanel panel = new JPanel();
		add(panel, BorderLayout.NORTH);

		JButton btnLoadNext = new JButton("Load Next");
		btnLoadNext.addActionListener(ae -> initStories());
		panel.add(btnLoadNext);

		lblLoading = AbstractBuzyIndicatorComponent.createLabelComponent();
		panel.add(lblLoading);

		editorPane = new JEditorPane();
		editorPane.setEditable(false);
		editorPane.setContentType("text/html");

		HTMLEditorKit kit = new HTMLEditorKit();
		editorPane.setEditorKit(kit);
		Document doc = kit.createDefaultDocument();
		editorPane.setDocument(doc);
		scrollEditor.setViewportView(editorPane);

		JSplitPane splitPane = new JSplitPane();
		splitPane.setLeftComponent(scrollList);
		splitPane.setRightComponent(scrollEditor);
		add(splitPane, BorderLayout.CENTER);
	}

	public void initStories() {
		ThreadManager.getInstance().execute(() -> {
			lblLoading.start();

			try {
				for (MTGStory story : provider.next())
					resultListModel.addElement(story);
			} catch (IOException e) {
				logger.error(e);
			} finally {
				lblLoading.end();
			}
		}, "loading stories");
	}

}
