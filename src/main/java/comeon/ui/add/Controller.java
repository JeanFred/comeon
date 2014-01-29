package comeon.ui.add;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;

import au.com.bytecode.opencsv.CSVReader;


class Controller implements PropertyChangeListener {
  
  private final DefaultListModel<File> picturesListModel;
  
  private final DefaultComboBoxModel<String> metadataExpressionModel;
  
  private Model model;
  
  private FilesPanel view;

  public Controller() {
    this.picturesListModel = new DefaultListModel<>();
    this.metadataExpressionModel = new DefaultComboBoxModel<>();
  }
  
  public void registerModel(final Model model) {
    this.model = model;
    this.model.addPropertyChangeListener(this);
  }
  
  public void registerView(final FilesPanel view) {
    this.view = view;
  }
  
  DefaultListModel<File> getPicturesListModel() {
    return picturesListModel;
  }
  
  public DefaultComboBoxModel<String> getMetadataExpressionModel() {
    return metadataExpressionModel;
  }
  
  public void setUseMetadata(final Boolean useMetadata) {
    model.setUseMetadata(useMetadata);
  }
  
  public void setMetadataFile(final File metadataFile) {
    model.setMetadataFile(metadataFile);
  }

  public void setPicturesFiles(final File[] picturesFiles) {
    model.setPicturesFiles(picturesFiles);
  }
  
  public void setPictureExpression(final String pictureExpression) {
    model.setPictureExpression(pictureExpression);
  }
  
  public void setMetadataExpression(final String metadataExpression) {
    model.setMetadataExpression(metadataExpression);
  }
  
  @Override
  public void propertyChange(final PropertyChangeEvent evt) {
    if (Model.Properties.USE_METADATA.name().equals(evt.getPropertyName())) {
      if ((Boolean) evt.getNewValue()) {
        view.activateMetadataZone();
      } else {
        view.deactivateMetadataZone();
      }
    } else if (Model.Properties.PICTURES_FILES.name().equals(evt.getPropertyName())) {
      picturesListModel.removeAllElements();
      final File[] files = (File[]) evt.getNewValue();
      for (final File file : files) {
        picturesListModel.addElement(file);
      } 
    } else if (Model.Properties.METADATA_FILE.name().equals(evt.getPropertyName())) {
      final File location = (File) evt.getNewValue();
      try {
        final String[] columns = this.peekMetadataFileHeader(location);
        metadataExpressionModel.removeAllElements();
        for (final String column : columns) {
          metadataExpressionModel.addElement(column);
        }
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            view.updateMetadataFileLocation(location.getAbsolutePath());
          }
        });
      } catch (final IOException e) {
        
      }
    }
  }
  
  private String[] peekMetadataFileHeader(final File metadataFile) throws IOException {
    try (final CSVReader reader = new CSVReader(new FileReader(metadataFile))) {
      return reader.readNext();
    }
  }
}