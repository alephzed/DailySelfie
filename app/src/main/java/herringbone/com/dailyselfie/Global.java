package herringbone.com.dailyselfie;

import android.app.Application;

public class Global extends Application {
    private String user;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
