package comeon;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import comeon.model.User;

public final class Users {
  
  private User user;
  
  /**
   * @throws UserNotSetException
   */
  public User getUser() {
    if (this.user == null) {
      this.user = loadUser();
    }
    return user;
  }
  
  public void setUser(final User user) throws BackingStoreException {
    this.user = user;
    final Preferences userPrefs = getUserPreferences();
    userPrefs.put(PreferencesKeys.LOGIN.name(), user.getLogin());
    userPrefs.put(PreferencesKeys.PASSWORD.name(), user.getPassword());
    userPrefs.put(PreferencesKeys.DISPLAY_NAME.name(), user.getDisplayName());
    userPrefs.flush();
  }

  /**
   * @throws UserNotSetException
   */
  private User loadUser() {
    final Preferences userPrefs = getUserPreferences();
    final String login = userPrefs.get(PreferencesKeys.LOGIN.name(), null);
    final String password = userPrefs.get(PreferencesKeys.PASSWORD.name(), null);
    final String displayName = userPrefs.get(PreferencesKeys.DISPLAY_NAME.name(), null);
    final User user;
    if (login == null || password == null || displayName == null) {
      // XXX Ugly hack
//      throw new UserNotSetException();
      user = new User("EdouardHue", "xxx", "Édouard Hue");
    } else {
      user = new User(login, password, displayName);
    }
    return user;
  }

  private Preferences getUserPreferences() {
    return Preferences.userNodeForPackage(Core.class).node("user");
  }
  
  private enum PreferencesKeys {
    LOGIN,
    PASSWORD,
    DISPLAY_NAME
  }
}
