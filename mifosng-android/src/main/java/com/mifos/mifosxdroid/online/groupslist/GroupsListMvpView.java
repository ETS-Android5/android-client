package com.mifos.mifosxdroid.online.groupslist;

import com.mifos.mifosxdroid.base.MvpView;
import com.mifos.objects.client.Page;
import com.mifos.objects.group.Group;

import retrofit.client.Response;

/**
 * Created by Rajan Maurya on 7/6/16.
 */
public interface GroupsListMvpView extends MvpView {

    void showGroups(Page<Group> groupPage);

    void showMoreGroups(Page<Group> groupPage);

    void showFetchingError(String s, Response response);
}
