package comeon.core;

import java.io.File;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.google.inject.Inject;

import comeon.core.extmetadata.ExternalMetadataSource;
import comeon.model.Template;
import comeon.model.processors.PreProcessor;

public final class RealPicturesBatchFactory implements PicturesBatchFactory {

  private final ExecutorService pool;
  
  private final Set<PreProcessor> preProcessors;
  
  @Inject
  public RealPicturesBatchFactory(final ExecutorService pool, final Set<PreProcessor> preProcessors) {
    this.pool = pool;
    this.preProcessors = preProcessors;
  }
  
  @Override
  public PicturesBatch makePicturesBatch(final File[] files, final Template defautTemplate, final ExternalMetadataSource<?> externalMetadataSource) {
    return new PicturesBatch(files, defautTemplate, pool, preProcessors, externalMetadataSource);
  }

}
