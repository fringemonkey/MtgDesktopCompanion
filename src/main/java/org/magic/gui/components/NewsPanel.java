package org.magic.gui.components;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;
import org.magic.api.beans.MagicNews;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;

import java.net.MalformedURLException;
import java.net.URL;

public class NewsPanel extends JPanel {

	private MagicNews magicNews = new MagicNews();
	private JTextField categorieJTextField;
	private JTextField nameJTextField;
	private JTextField urlJTextField;

	public NewsPanel(org.magic.api.beans.MagicNews newMagicNews) {
		this();
		setMagicNews(newMagicNews);
	}

	public NewsPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 1.0E-4 };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0E-4 };
		setLayout(gridBagLayout);

		JLabel categorieLabel = new JLabel("Categorie:");
		GridBagConstraints labelGbc0 = new GridBagConstraints();
		labelGbc0.insets = new Insets(5, 5, 5, 5);
		labelGbc0.gridx = 0;
		labelGbc0.gridy = 0;
		add(categorieLabel, labelGbc0);

		categorieJTextField = new JTextField();
		GridBagConstraints componentGbc0 = new GridBagConstraints();
		componentGbc0.insets = new Insets(5, 0, 5, 0);
		componentGbc0.fill = GridBagConstraints.HORIZONTAL;
		componentGbc0.gridx = 1;
		componentGbc0.gridy = 0;
		add(categorieJTextField, componentGbc0);

		JLabel nameLabel = new JLabel("Name:");
		GridBagConstraints labelGbc2 = new GridBagConstraints();
		labelGbc2.insets = new Insets(5, 5, 5, 5);
		labelGbc2.gridx = 0;
		labelGbc2.gridy = 1;
		add(nameLabel, labelGbc2);

		nameJTextField = new JTextField();
		GridBagConstraints componentGbc2 = new GridBagConstraints();
		componentGbc2.insets = new Insets(5, 0, 5, 0);
		componentGbc2.fill = GridBagConstraints.HORIZONTAL;
		componentGbc2.gridx = 1;
		componentGbc2.gridy = 1;
		add(nameJTextField, componentGbc2);

		JLabel urlLabel = new JLabel("Url:");
		GridBagConstraints labelGbc3 = new GridBagConstraints();
		labelGbc3.insets = new Insets(5, 5, 0, 5);
		labelGbc3.gridx = 0;
		labelGbc3.gridy = 2;
		add(urlLabel, labelGbc3);

		urlJTextField = new JTextField();
		GridBagConstraints componentGbc3 = new GridBagConstraints();
		componentGbc3.insets = new Insets(5, 0, 0, 0);
		componentGbc3.fill = GridBagConstraints.HORIZONTAL;
		componentGbc3.gridx = 1;
		componentGbc3.gridy = 2;
		add(urlJTextField, componentGbc3);

	}

	public MagicNews getMagicNews() {
		
		magicNews.setCategorie(categorieJTextField.getText());
		magicNews.setName(nameJTextField.getText());
		try {
			magicNews.setUrl(new URL(urlJTextField.getText()));
		} catch (MalformedURLException e) {
			magicNews.setUrl(null);
		}
		
		return magicNews;
	}

	
	
	public void setMagicNews(MagicNews newMagicNews) {
		this.magicNews=newMagicNews;
		nameJTextField.setText(magicNews.getName());
		urlJTextField.setText(String.valueOf(magicNews.getUrl()));
		categorieJTextField.setText(magicNews.getCategorie());
	}

}
