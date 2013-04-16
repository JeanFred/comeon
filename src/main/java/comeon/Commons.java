package comeon;

import in.yuvi.http.fluent.ProgressListener;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.mediawiki.api.MWApi;

import com.google.common.io.Files;
import comeon.model.Picture;
import comeon.model.User;

final class Commons {
  // TODO this URL should not be hard-coded
  private static final String URL = "http://commons.wikimedia.org/w/api.php";
  
  private final User user;
  
  private final MWApi api;
  
  Commons(final User user) {
    this.user = user;
    final DefaultHttpClient client = new DefaultHttpClient();
    // TODO Filter version from POM
    client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, "ComeOn!/1.0-SNAPSHOT (http://github.com/edouardhue/comeon; EdouardHue) using org.mediawiki:api:1.3");
    // XXX MWApi requires AbstractHttpClient instead of HttpClient
    this.api = new MWApi(URL, (AbstractHttpClient) client);
  }
  
  void login() throws NotLoggedInException, FailedLoginException {
    try {
      final String result = this.api.login(user.getLogin(), user.getPassword());
      if (!this.api.isLoggedIn) {
        throw new NotLoggedInException(result);
      }
    } catch (final IOException e) {
      throw new FailedLoginException(e);
    }
  }
  
  void upload(final Picture picture, final ProgressListener listener) throws NotLoggedInException, FailedLoginException, FailedUploadException, IOException {
    if (this.api.isLoggedIn) {
      final InputStream stream = Files.newInputStreamSupplier(picture.getFile()).getInput();
      try {
        this.api.upload(picture.getFile().getName(), stream, picture.getFile().length(), picture.getRenderedTemplate(), "Uploaded with ComeOn!", true, listener);
      } catch (final IOException e) {
        throw new FailedUploadException(e);
      }
    } else {
      this.login();
      this.upload(picture, listener);
    }
  }
  
  void logout() throws FailedLogoutException {
    if (this.api.isLoggedIn) {
      try {
        this.api.logout();
      } catch (final IOException e) {
        throw new FailedLogoutException(e);
      }
    }
  }
}
