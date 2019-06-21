package upx.uplexa.androidminer;

public class PreferenceHelper {

    final public static String KEY_DEMO_NAME = "YOUR_UPX_ADDRESS_HERE"; // Rename this, lol.
    final public static String MINPAY_STRING = "MINPAY"; // Rename this, lol.
    final public static String WORKER_STRING = "WORKER"; // Rename this, lol.
    public static void setName(String value) {
        MainActivity.preferences.edit().putString(KEY_DEMO_NAME, value ).commit();
    }
    public static String getName() {
        return MainActivity.preferences.getString(KEY_DEMO_NAME,"");
    }

    public static void setThreshold(float value) {
        //value = String.valueOf(value);
        MainActivity.preferences.edit().putString(MINPAY_STRING, String.valueOf(value)).commit();
    }
    public static String getThreshold() {
        if(!MainActivity.preferences.getString(MINPAY_STRING,"").isEmpty()) {
            return MainActivity.preferences.getString(MINPAY_STRING, "");
        }
        else{
            return "75";
        }
    }

    public static void setWorkerID(String value) {
        //value = String.valueOf(value);
        MiningService.workerId = value;
        MainActivity.preferences.edit().putString(WORKER_STRING, String.valueOf(value)).commit();
    }
    public static String getWorkerID() {
        return MainActivity.preferences.getString(WORKER_STRING,"");
    }

}