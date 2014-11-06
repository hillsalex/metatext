package hills.sms.texter.sender;

import android.content.Context;

import com.hillsalex.metatext.database.models.SmsMessageModel;
import com.hillsalex.metatext.persons.Person;
import com.hillsalex.metatext.persons.Persons;
import com.hillsalex.metatext.senders.SmsSender;


public class Sender {

    static boolean mInitialized = false;

    public static Context context = null;

    public static boolean sendSMS(String address, String body, Context context) {
        Sender.context = context;
        sendSMS(address, body, context, 0);
        return true;
    }

    public static boolean sendSMS(String address, String body, Context context, long threadID) {
        SmsSender sender = new SmsSender(context);//SendReceiveServices.getSmsSender();
        Person person = new Person(0, address, null, null, "test");
        SmsMessageModel message = new SmsMessageModel(body, new Persons(person));
        sender.handleSendMessage(message);
        /*
        Sender.context = context;
        DisplayRecord.Body newBody = new DisplayRecord.Body("Get fucked",true);
        Recipient recp = new Recipient("alex", "4242424419", 2, Uri.parse(""), null,null);
        Recipients recipients = new Recipients(recp);
        try {
            Sender.deliverPlaintextMessage(new SmsMessageRecord(context, 15215,
            newBody, recipients,recp,0,0,0,1,2,threadID,0));
        } catch (UndeliverableMessageException e) {
            e.printStackTrace();
        }*/
        /*
        MmsConnection.Apn apn = null;
        try {
            apn = MmsConnection.getApn(context,"Test");
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            Settings settings = Utils.getDefaultSendSettings(context);
            settings.setProxy(apn.getProxy());
            settings.setMmsc(apn.getMmsc());
            settings.setPort(8080+"");//apn.getPort()+"");

            Transaction sendTransaction = new Transaction(context,settings);
            Message mMessage = new Message(body,address);
            //mMessage.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.send_arrow));
            mMessage.setType(Message.TYPE_SMSMMS);

            sendTransaction.sendNewMessage(mMessage,threadID);
        } catch (ApnUnavailableException e) {
            e.printStackTrace();
        }*/

        /*
        SmsManager manager = SmsManager.getDefault();
		manager.sendTextMessage(address, null, body, null, null);*/
        return true;
    }

    public static void initialize(Context context) {
        if (mInitialized) return;
        //ApnUtils.initDefaultApns(context,listener);
    }
/*
    private static class FinishedListener implements ApnUtils.OnApnFinishedListener {

        @Override
        public void onFinished() {

        }
    }*/

}
