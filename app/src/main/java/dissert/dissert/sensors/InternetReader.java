package dissert.dissert.sensors;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class InternetReader {

    private static final int UNKNOWN = -2;
    private static final int NO_CONNECTION = -1;
    private static final int DISCONNECTING = 0;
    private static final int CONNECTING = 1;
    private static final int CELL_CONNECTION = 2;
    private static final int WIFI_CONNECTION = 3;

    private ConnectivityManager connectivityManager;
    private NetworkInfo networkInfo;

    public InternetReader(ConnectivityManager cm) {
        connectivityManager = cm;
    }

    /**
     * Return connection status. <br><br>
     * -2 = Unknown <br>
     * -1 = Disconnected <br>
     * 0 = Disconnecting; <br>
     * 1 = Connecting; <br>
     * 2 = Cell connection; <br>
     * 3 = Wifi connection; <br>
     * @return int result - connection status
     */
    public int getConnectionStatus() {
        networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo == null)
            return NO_CONNECTION;


        NetworkInfo.State state = networkInfo.getState();

        if(state.equals(NetworkInfo.State.DISCONNECTED))
            return NO_CONNECTION;
        else if(state.equals(NetworkInfo.State.DISCONNECTING))
            return DISCONNECTING;
        else if(state.equals(NetworkInfo.State.CONNECTING))
            return CONNECTING;
        else if(state.equals(NetworkInfo.State.CONNECTED)) {
            if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI)
                return WIFI_CONNECTION;
            return CELL_CONNECTION;
        }
        return UNKNOWN;
    }
}
