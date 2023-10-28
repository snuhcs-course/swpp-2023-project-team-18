package snu.swpp.moment.data.repository;

import java.util.ArrayList;
import snu.swpp.moment.data.callback.MomentGetCallBack;
import snu.swpp.moment.data.callback.MomentWriteCallBack;
import snu.swpp.moment.data.model.MomentPairModel;
import snu.swpp.moment.data.source.MomentRemoteDataSource;

public class MomentRepository {

    private final MomentRemoteDataSource remoteDataSource;

    public MomentRepository(MomentRemoteDataSource remoteDataSource) {
        this.remoteDataSource = remoteDataSource;
    }

    public void getMoment(String access_token, long start, long end, MomentGetCallBack callback) {
        remoteDataSource.getMoment(access_token, start, end, new MomentGetCallBack() {
            @Override
            public void onSuccess(ArrayList<MomentPairModel> momentPair) {
                //System.out.println("#DEBUG: REPO " + momentPair.get(0).getMoment());
                callback.onSuccess(momentPair);
            }

            @Override
            public void onFailure(int error) {
                callback.onFailure(error);
            }
        });
    }

    public void writeMoment(String access_token, String moment, MomentWriteCallBack callback) {
        remoteDataSource.writeMoment(access_token, moment, new MomentWriteCallBack() {
            @Override
            public void onSuccess(MomentPairModel momentPair) {
                callback.onSuccess(momentPair);
            }

            @Override
            public void onFailure(int error) {
                callback.onFailure(error);
            }
        });
    }

}