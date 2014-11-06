package hills.sms.texter.util;

/**
 * Created by alex on 11/5/2014.
 */
public interface FutureTaskListener<V> {
    public void onSuccess(V result);
    public void onFailure(Throwable error);
}
