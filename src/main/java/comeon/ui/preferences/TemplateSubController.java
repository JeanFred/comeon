package comeon.ui.preferences;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;

import javax.swing.JComboBox;
import javax.swing.SwingUtilities;

import comeon.model.TemplateKind;

final class TemplateSubController extends SubController<TemplateModel, TemplateSubPanel> {

  @Override
  protected void registerViewInterval(final TemplateSubPanel view) {
    view.getNameField().getDocument().addDocumentListener(new AbstractDocumentListener() {
      @Override
      protected void doUpdate(final String text) {
        getModel().setName(text);
      }
    });
    view.getDescriptionField().getDocument().addDocumentListener(new AbstractDocumentListener() {
      @Override
      protected void doUpdate(final String text) {
        getModel().setDescription(text);
      }
    });
    view.getCharsetField().addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        @SuppressWarnings("unchecked")
        final JComboBox<Charset> source = (JComboBox<Charset>) e.getSource();
        final Charset newCharset = (Charset) source.getSelectedItem();
        getModel().setCharset(newCharset);
      }
    });
    view.getKindField().addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        @SuppressWarnings("unchecked")
        final JComboBox<TemplateKind> source = (JComboBox<TemplateKind>) e.getSource();
        final TemplateKind kind = (TemplateKind) source.getSelectedItem();
        getModel().setKind(kind);
      }
    });
    view.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(final PropertyChangeEvent evt) {
        if (TemplateSubPanel.SELECTED_FILE_PROPERTY.equals(evt.getPropertyName())) {
          final File selectedFile = (File) evt.getNewValue();
          getModel().setFile(selectedFile.toPath());
        }
      }
    });
  }
  
  @Override
  protected void onModelChangedInternal(final TemplateModel oldModel, final TemplateModel newModel) {
    getView().getNameField().setText(newModel.getName());
    getView().getDescriptionField().setText(newModel.getDescription());
    getView().getFileField().setText(newModel.getFile().toString());
    getView().getCharsetField().setSelectedItem(newModel.getCharset());
    getView().getKindField().setSelectedItem(newModel.getKind());
  }

  @Override
  public void propertyChange(final PropertyChangeEvent evt) {
    if (TemplateModel.Properties.FILE.name().equals(evt.getPropertyName())) {
      final Path file = (Path) evt.getNewValue();
      getModel().setFile(file);
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          getView().getFileField().setText(file == null ? null : file.toString());
        }
      });
    }
  }
}
