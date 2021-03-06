package comeon.core.extmetadata;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.bean.CsvToBean;

import comeon.model.Picture;

public final class CsvMetadataSource implements ExternalMetadataSource<Object> {
  private static final Logger LOGGER = LoggerFactory.getLogger(CsvMetadataSource.class);
  
  private HashMap<String, Object> metadata;
  
  private final Path metadataFile;

  private final String pictureExpression;
  
  private final String metadataExpression;
  
  private final char separator;

  private final char quote;

  private final char escape;

  private final int skipLines;

  private final boolean strictQuotes;

  private final boolean ignoreLeadingWhiteSpace;
  
  private final Charset charset;
  
  public CsvMetadataSource(final String pictureExpression, final String metadataExpression, final Path metadataFile, final char separator, final char quote, final char escape,
      final int skipLines, final boolean strictQuotes, final boolean ignoreLeadingWhiteSpace, final Charset charset) {
    this.pictureExpression = pictureExpression;
    this.metadataExpression = metadataExpression;
    this.metadataFile = metadataFile;
    this.separator = separator;
    this.quote = quote;
    this.escape = escape;
    this.skipLines = skipLines;
    this.strictQuotes = strictQuotes;
    this.ignoreLeadingWhiteSpace = ignoreLeadingWhiteSpace;
    this.charset = charset;
  }
  
  @Override
  public void loadMetadata() {
    final CGLibMappingStrategy strategy = new CGLibMappingStrategy();
    final CsvToBean<Object> csvToBean = new CsvToBean<>();
    try (final CSVReader reader = new CSVReader(Files.newBufferedReader(metadataFile, charset), separator, quote, escape, skipLines, strictQuotes, ignoreLeadingWhiteSpace)) {
      final List<Object> beans = csvToBean.parse(strategy, reader);
      this.metadata = new HashMap<>(beans.size());
      for (final Object bean : beans) {
        final String key = String.valueOf(PropertyUtils.getProperty(bean, metadataExpression));
        this.metadata.put(key, bean);
      }
    } catch (final IOException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      LOGGER.error("Could not read metadata file", e);
    }
  }
  
  @Override
  public Object getPictureMetadata(final Picture picture) {
    try {
      final String key = String.valueOf(PropertyUtils.getProperty(picture, pictureExpression));
      return metadata.get(key);
    } catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      LOGGER.warn("Can't get property {} from picture", pictureExpression, e);
      return null;
    }
  }
}
