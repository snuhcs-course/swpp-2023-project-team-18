package snu.swpp.moment.data.repository;

import snu.swpp.moment.data.callback.NudgeGetCallback;
import snu.swpp.moment.data.callback.NudgeMarkCallback;
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

    public void markNudge(String access_token, NudgeMarkCallback callback) {
        remoteDataSource.markNudge(access_token, new NudgeMarkCallback() {
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
