package hills.sms.texter.mms;

/**
 * Created by alex on 11/5/2014.
 */
public class ApnUnavailableException extends Exception {

    public ApnUnavailableException() {
    }

    public ApnUnavailableException(String detailMessage) {
        super(detailMessage);
    }

    public ApnUnavailableException(Throwable throwable) {
        super(throwable);
    }

    public ApnUnavailableException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
