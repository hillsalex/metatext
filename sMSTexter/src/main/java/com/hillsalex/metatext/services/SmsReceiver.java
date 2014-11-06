package com.hillsalex.metatext.services;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Telephony;
import android.support.v4.util.Pair;
import android.telephony.SmsMessage;

import com.hillsalex.metatext.messages.sms.IncomingTextMessage;

import java.io.IOException;
import java.util.List;

/**
 * Created by alex on 11/6/2014.
 */

public class SmsReceiver {

    private final Context context;

    public SmsReceiver(Context context) {
        this.context = context;
    }

    private IncomingTextMessage assembleMessageFragments(List<IncomingTextMessage> messages) {
        IncomingTextMessage message = new IncomingTextMessage(messages);
        /*
        if (WirePrefix.isEncryptedMessage(message.getMessageBody()) ||
                WirePrefix.isKeyExchange(message.getMessageBody())      ||
                WirePrefix.isPreKeyBundle(message.getMessageBody())     ||
                WirePrefix.isEndSession(message.getMessageBody()))
        {
            return multipartMessageHandler.processPotentialMultipartMessage(message);
        } else {*/
            return message;
        //}
    }
/*
    private Pair<Long, Long> storeSecureMessage(MasterSecret masterSecret, IncomingTextMessage message) {
        Pair<Long, Long> messageAndThreadId = DatabaseFactory.getEncryptingSmsDatabase(context)
                .insertMessageInbox(masterSecret, message);

        if (masterSecret != null) {
            DecryptingQueue.scheduleDecryption(context, masterSecret, messageAndThreadId.first,
                    messageAndThreadId.second,
                    message.getSender(), message.getSenderDeviceId(),
                    message.getMessageBody(), message.isSecureMessage(),
                    message.isKeyExchange(), message.isEndSession());
        }

        return messageAndThreadId;
    }
*/
    private Pair<Long, Long> storeStandardMessage(IncomingTextMessage message) {
        //SmsDatabase           plaintextDatabase  = DatabaseFactory.getSmsDatabase(context);
        ContentResolver contentResolver = context.getContentResolver();
        Uri smsUri = Telephony.Sms.CONTENT_URI;
        String body = message.getMessageBody();
        String address = message.getSender();
        ContentValues values = new ContentValues();
        values.put(Telephony.Sms.ADDRESS, address);
        values.put(Telephony.Sms.BODY, body);

        Uri uri = contentResolver.insert(smsUri, values);
        return Pair.create(0l,0l);
        //return plaintextDatabase.insertMessageInbox(message);
    }
/*
    private Pair<Long, Long> storePreKeyWhisperMessage(MasterSecret masterSecret,
                                                       IncomingPreKeyBundleMessage message)
    {
        Log.w("SmsReceiver", "Processing prekey message...");
        EncryptingSmsDatabase database = DatabaseFactory.getEncryptingSmsDatabase(context);

        if (masterSecret != null) {
            try {
                Recipient            recipient            = RecipientFactory.getRecipientsFromString(context, message.getSender(), false).getPrimaryRecipient();
                RecipientDevice      recipientDevice      = new RecipientDevice(recipient.getRecipientId(), message.getSenderDeviceId());
                SmsTransportDetails  transportDetails     = new SmsTransportDetails();
                TextSecureCipher     cipher               = new TextSecureCipher(context, masterSecret, recipientDevice, transportDetails);
                byte[]               rawMessage           = transportDetails.getDecodedMessage(message.getMessageBody().getBytes());
                PreKeyWhisperMessage preKeyWhisperMessage = new PreKeyWhisperMessage(rawMessage);
                byte[]               plaintext            = cipher.decrypt(preKeyWhisperMessage);

                IncomingEncryptedMessage bundledMessage     = new IncomingEncryptedMessage(message, new String(transportDetails.getEncodedMessage(preKeyWhisperMessage.getWhisperMessage().serialize())));
                Pair<Long, Long>         messageAndThreadId = database.insertMessageInbox(masterSecret, bundledMessage);

                database.updateMessageBody(masterSecret, messageAndThreadId.first, new String(plaintext));

                Intent intent = new Intent(KeyExchangeProcessor.SECURITY_UPDATE_EVENT);
                intent.putExtra("thread_id", messageAndThreadId.second);
                intent.setPackage(context.getPackageName());
                context.sendBroadcast(intent, KeyCachingService.KEY_PERMISSION);

                return messageAndThreadId;
            } catch (InvalidKeyException | RecipientFormattingException | InvalidMessageException | IOException | NoSessionException e) {
                Log.w("SmsReceiver", e);
                message.setCorrupted(true);
            } catch (InvalidVersionException e) {
                Log.w("SmsReceiver", e);
                message.setInvalidVersion(true);
            } catch (InvalidKeyIdException e) {
                Log.w("SmsReceiver", e);
                message.setStale(true);
            } catch (UntrustedIdentityException e) {
                Log.w("SmsReceiver", e);
            } catch (DuplicateMessageException e) {
                Log.w("SmsReceiver", e);
                message.setDuplicate(true);
            } catch (LegacyMessageException e) {
                Log.w("SmsReceiver", e);
                message.setLegacyVersion(true);
            }
        }

        return storeStandardMessage(masterSecret, message);
    }
*/
    /*
    private Pair<Long, Long> storeKeyExchangeMessage(MasterSecret masterSecret,
                                                     IncomingKeyExchangeMessage message)
    {
        if (masterSecret != null && TextSecurePreferences.isAutoRespondKeyExchangeEnabled(context)) {
            try {
                Recipient            recipient       = RecipientFactory.getRecipientsFromString(context, message.getSender(), false).getPrimaryRecipient();
                RecipientDevice      recipientDevice = new RecipientDevice(recipient.getRecipientId(), message.getSenderDeviceId());
                KeyExchangeMessage   exchangeMessage = new KeyExchangeMessage(Base64.decodeWithoutPadding(message.getMessageBody()));
                KeyExchangeProcessor processor       = new KeyExchangeProcessor(context, masterSecret, recipientDevice);
                long                 threadId        = DatabaseFactory.getThreadDatabase(context).getThreadIdFor(new Recipients(recipient));
                OutgoingKeyExchangeMessage response = processor.processKeyExchangeMessage(exchangeMessage, threadId);

                message.setProcessed(true);

                Pair<Long, Long> messageAndThreadId = storeStandardMessage(masterSecret, message);

                if (response != null) {
                    MessageSender.send(context, masterSecret, response, messageAndThreadId.second, true);
                }

                return messageAndThreadId;
            } catch (InvalidVersionException e) {
                Log.w("SmsReceiver", e);
                message.setInvalidVersion(true);
            } catch (InvalidMessageException | InvalidKeyException | IOException | RecipientFormattingException e) {
                Log.w("SmsReceiver", e);
                message.setCorrupted(true);
            } catch (LegacyMessageException e) {
                Log.w("SmsReceiver", e);
                message.setLegacyVersion(true);
            } catch (StaleKeyExchangeException e) {
                Log.w("SmsReceiver", e);
                message.setStale(true);
            } catch (UntrustedIdentityException e) {
                Log.w("SmsReceiver", e);
            }
        }

        return storeStandardMessage(masterSecret, message);
    }*/

    private Pair<Long, Long> storeMessage(IncomingTextMessage message) {
        /*if      (message.isSecureMessage()) return storeSecureMessage(masterSecret, message);
        else if (message.isPreKeyBundle())  return storePreKeyWhisperMessage(masterSecret, (IncomingPreKeyBundleMessage) message);
        else if (message.isKeyExchange())   return storeKeyExchangeMessage(masterSecret, (IncomingKeyExchangeMessage) message);
        else if (message.isEndSession())    return storeSecureMessage(masterSecret, message);
        else                                */return storeStandardMessage(message);
    }

    private void handleReceiveMessage(Intent intent) {
        if (intent.getExtras() == null) return;

        List<IncomingTextMessage> messagesList = intent.getExtras().getParcelableArrayList("text_messages");
        IncomingTextMessage       message      = assembleMessageFragments(messagesList);

        if (message != null) {
            Pair<Long, Long> messageAndThreadId = storeMessage(message);
            //MessageNotifier.updateNotification(context, masterSecret, messageAndThreadId.second);
        }
    }

    public void process(Intent intent) {
        if (SendReceiveServices.RECEIVE_SMS_ACTION.equals(intent.getAction())) {
            handleReceiveMessage(intent);
        }
    }
}

