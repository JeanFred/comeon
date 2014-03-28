package comeon.core;

import in.yuvi.http.fluent.ProgressListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import comeon.core.extmetadata.ExternalMetadataSource;
import comeon.mediawiki.FailedLoginException;
import comeon.mediawiki.FailedLogoutException;
import comeon.mediawiki.FailedUploadException;
import comeon.mediawiki.MediaWiki;
import comeon.mediawiki.MediaWikiImpl;
import comeon.mediawiki.NotLoggedInException;
import comeon.model.Picture;
import comeon.model.Template;
import comeon.model.Wiki;
import comeon.model.Picture.State;
import comeon.ui.actions.PictureRemovedEvent;
import comeon.ui.actions.PicturesAddedEvent;
import comeon.wikis.ActiveWikiChangeEvent;
import comeon.wikis.Wikis;

@Singleton
public final class CoreImpl implements Core {
  private static final Logger LOGGER = LoggerFactory.getLogger(CoreImpl.class);

  private final List<Picture> pictures;

  private final ExecutorService pool;
  
  private final Wikis wikis;

  private final EventBus bus;
  
  private final PicturesBatchFactory picturesBatchFactory;

  private MediaWiki activeMediaWiki;
  

  @Inject
  private CoreImpl(final Wikis wikis, final ExecutorService pool, final EventBus bus, final PicturesBatchFactory picturesBatchFactory) {
    this.pictures = new ArrayList<>();
    this.pool = pool;
    this.bus = bus;
    this.wikis = wikis;
    this.picturesBatchFactory = picturesBatchFactory;
    // TODO Use dependecy injection
    final Wiki activeWiki = wikis.getActiveWiki();
    if (activeWiki == null) {
      throw new IllegalStateException("There must be one active wiki.");
    } else {
      this.activeMediaWiki = new MediaWikiImpl(activeWiki);
    }
  }

  @Override
  public void addPictures(final File[] files, final Template defautTemplate, final ExternalMetadataSource<?> externalMetadataSource) {
    final PicturesBatch picturesReader = picturesBatchFactory.makePicturesBatch(files, defautTemplate, externalMetadataSource);
    final List<Picture> newPictures = picturesReader.readFiles(wikis.getActiveWiki().getUser()).getPictures();
    this.pictures.addAll(newPictures);
    bus.post(new PicturesAddedEvent());
  }

  @Override
  public void removePicture(final Picture picture) {
    pictures.remove(picture);
    bus.post(new PictureRemovedEvent());
  }
  
  @Override
  public List<Picture> getPictures() {
    return pictures;
  }
  
  private boolean shouldUpload(final Picture picture) {
    return !State.UploadedSuccessfully.equals(picture.getState());
  }

  @Override
  public int countPicturesToBeUploaded() {
    int picturesToBeUploaded = 0;
    for (final Picture picture : pictures) {
      if (shouldUpload(picture)) {
        picturesToBeUploaded++;
      }
    }
    return picturesToBeUploaded;
  }
  
  @Override
  public void uploadPictures(final UploadMonitor monitor) {
    final List<Picture> batch = new ArrayList<>(this.pictures);
    monitor.setBatchSize(batch.size());
    this.pool.submit(new Runnable() {
      @Override
      public void run() {
        try {
          monitor.uploadStarting();
          int index = 0;
          for (final Picture picture : batch) {
            if (shouldUpload(picture)) {
              try {
                final ProgressListener listener = monitor.itemStarting(index, picture.getFile().length(),
                    picture.getFileName());
                activeMediaWiki.upload(picture, listener);
                picture.setState(State.UploadedSuccessfully);
              } catch (final NotLoggedInException | FailedLoginException | FailedUploadException | IOException e) {
                // TODO i18n
                LOGGER.warn("Picture upload failed", e);
                picture.setState(State.FailedUpload);
              } finally {
                monitor.itemDone(index);
                index++;
              }
            }
          }
          activeMediaWiki.logout();
          monitor.uploadDone();
        } catch (final FailedLogoutException e) {
          // TODO i18n
          LOGGER.warn("Couldn't close Mediawiki session properly", e);
        }
      }
    });
  }
  
  @Subscribe
  public void handleActiveWikiChangeEvent(final ActiveWikiChangeEvent event) {
    if (this.activeMediaWiki.isLoggedIn()) {
      try {
        this.activeMediaWiki.logout();
      } catch (final FailedLogoutException e) {
        // TODO i18n
        LOGGER.warn("Failed implicit logout", e);
      }
    }
  }
}
