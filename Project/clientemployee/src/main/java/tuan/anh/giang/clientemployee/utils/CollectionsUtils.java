package tuan.anh.giang.clientemployee.utils;

import com.backendless.BackendlessUser;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.Collection;


public class CollectionsUtils {

    public static String makeStringFromUsersFullNames(ArrayList<QBUser> allUsers) {
        StringifyArrayList<String> usersNames = new StringifyArrayList<>();

        for (QBUser usr : allUsers) {
            if (usr.getFullName() != null) {
                usersNames.add(usr.getFullName());
            } else if (usr.getId() != null) {
                usersNames.add(String.valueOf(usr.getId()));
            }
        }
        return usersNames.getItemsAsString().replace(",", ", ");
    }

    public static ArrayList<Integer> getIdsSelectedOpponents(Collection<QBUser> selectedUsers) {
        ArrayList<Integer> opponentsIds = new ArrayList<>();
        if (!selectedUsers.isEmpty()) {
            for (QBUser qbUser : selectedUsers) {
                opponentsIds.add(qbUser.getId());
            }
        }

        return opponentsIds;
    }

    public static ArrayList<Integer> getIdSelectedEmployee(BackendlessUser selectedUsers) {
        ArrayList<Integer> opponentsIds = new ArrayList<>();
        if (selectedUsers != null) {
            opponentsIds.add((Integer) selectedUsers.getProperty("id_qb"));
        }
        return opponentsIds;
    }
    public static ArrayList<Integer> getIdSelectedEmployee(QBUser selectedUsers) {
        ArrayList<Integer> opponentsIds = new ArrayList<>();
        if (selectedUsers != null) {
            opponentsIds.add(selectedUsers.getId());
        }
        return opponentsIds;
    }
}
