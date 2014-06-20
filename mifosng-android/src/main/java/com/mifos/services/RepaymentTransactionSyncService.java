package com.mifos.services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.mifos.objects.db.Loan;
import com.mifos.objects.db.RepaymentTransaction;
import com.mifos.services.data.BulkRepaymentTransactions;
import com.mifos.services.data.CollectionSheetPayload;
import com.mifos.services.data.SaveResponse;
import com.mifos.utils.Network;
import com.orm.query.Select;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RepaymentTransactionSyncService extends Service {

    private static final String TAG = RepaymentTransactionSyncService.class.getSimpleName();
    private final long FIFTEEN_MINUTES = 30 * 1000L;

    private Timer timer;

    private TimerTask updateTask = new TimerTask() {
        @Override
        public void run() {

            List<RepaymentTransaction> transactions = Select.from(RepaymentTransaction.class).list();
            Log.i(TAG, "Fetch transactions from Database:" + transactions);

            if (transactions.size() > 0) {
                List<BulkRepaymentTransactions> repaymentTransactions = new ArrayList<BulkRepaymentTransactions>();

                for (RepaymentTransaction transaction : transactions) {
                    Loan loan = transaction.getLoan();
                    if(loan.getAccountStatusId() == 300) {
                        repaymentTransactions.add(new BulkRepaymentTransactions(loan.getLoanId(), transaction.getTransactionAmount()));
                    }
                }

                BulkRepaymentTransactions[] repaymentTransactionArray = new BulkRepaymentTransactions[repaymentTransactions.size()];
                repaymentTransactions.toArray(repaymentTransactionArray);


                CollectionSheetPayload payload = new CollectionSheetPayload();
                payload.bulkRepaymentTransactions = repaymentTransactionArray;

                SaveCollectionSheetTask task = new SaveCollectionSheetTask();
                task.execute(payload);
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        timer = new Timer("TransactionCollectorTimer");
        timer.schedule(updateTask, 100L, FIFTEEN_MINUTES);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        timer.cancel();
        timer = null;
    }

    private class SaveCollectionSheetTask extends AsyncTask<CollectionSheetPayload, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(CollectionSheetPayload... collectionSheetPayloads) {

            if (Network.isOnline(getApplicationContext())) {

                SaveResponse response = API.centerService.saveCollectionSheet(collectionSheetPayloads[0]);

                if (response != null) {

                    Log.i(TAG, "saveCollectionSheet - Response:" + response.toString());
                    RepaymentTransaction.deleteAll(RepaymentTransaction.class);

                }
            }

            return null;
        }

    }
}