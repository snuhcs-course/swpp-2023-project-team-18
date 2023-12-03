package snu.swpp.moment.data.repository;

import snu.swpp.moment.data.callback.NudgeDeleteCallback;
import snu.swpp.moment.data.callback.NudgeGetCallback;
import snu.swpp.moment.data.source.NudgeRemoteDataSource;

public class NudgeRepository extends BaseRepository<NudgeRemoteDataSource> {

    public NudgeRepository(NudgeRemoteDataSource remoteDataSource) {
        super(remoteDataSource);
    }

    public void getNudge(String access_token, long start, long end, NudgeGetCallback callback) {
        remoteDataSource.getNudge(access_token, start, end, new NudgeGetCallback() {
            @Override
            public void onSuccess(String nudge) {
                callback.onSuccess(nudge);
            }

            @Override
            public void onFailure(Exception error) {
                callback.onFailure(error);
            }
        });
    }

    public void deleteNudge(String access_token, NudgeDeleteCallback callback) {
        remoteDataSource.deleteNudge(access_token, new NudgeDeleteCallback() {
            @Override
            public void onSuccess() {
                callback.onSuccess();
            }

            @Override
            public void onFailure(Exception error) {
                callback.onFailure(error);
            }
        });
    }
}
