package hills.sms.texter.util;

import java.io.IOException;

/**
 * Created by alex on 11/5/2014.
 */

public class GroupUtil {

    private static final String ENCODED_GROUP_PREFIX = "__textsecure_group__!";

    public static String getEncodedId(byte[] groupId) {
        return ENCODED_GROUP_PREFIX + Hex.toStringCondensed(groupId);
    }

    public static byte[] getDecodedId(String groupId) throws IOException {
        if (!isEncodedGroup(groupId)) {
            throw new IOException("Invalid encoding");
        }

        return Hex.fromStringCondensed(groupId.split("!", 2)[1]);
    }

    public static boolean isEncodedGroup(String groupId) {
        return groupId.startsWith(ENCODED_GROUP_PREFIX);
    }

    public static String getDescription(String encodedGroup) {
        if (encodedGroup == null) {
            return "Group updated.";
        }

        try {
            String description = "";
            /*GroupContext context     = GroupContext.parseFrom(Base64.decode(encodedGroup));
            List<String> members     = context.getMembersList();
            String       title       = context.getName();

            if (!members.isEmpty()) {
                description += Util.join(members, ", ") + " joined the group.";
            }

            if (title != null && !title.trim().isEmpty()) {
                description += " Title is now '" + title + "'.";
            }
            */
            return description;
        } catch (Exception e) {
            return "";
        }
        /* catch (InvalidProtocolBufferException e) {
            Log.w("GroupUtil", e);
            return "Group updated.";
        } catch (IOException e) {
            Log.w("GroupUtil", e);
            return "Group updated.";
        }*/

    }
}
