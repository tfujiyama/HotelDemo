package com.example.fuji.hoteldemo;

import android.os.Bundle;
import android.os.Handler;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.ActionBarActivity;


public class MainActivity extends Activity {

    private static final String TAG = "HotelDemo";

    private static final long SCAN_PERIOD = 1000;               // BLE 機器検索のタイムアウト(ミリ秒) -> 1秒
    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private static final String SCANNING = "Scanning....";
    private static final String NOT_FOUND = "Not Found.";
    private static final String EXTRA_REPLY = "extra_reply";

    private static final String KEY01 = "b9407f30-f5f8-466e-aff9-25556b57fe6d";     // Key01用のuuid（英字は小文字で設定）
    private static final String KEY02 = "52414449-5553-4e45-5457-4f524b53434f";     // Key02用のuuid（英字は小文字で設定）
    private static final String KEY03 = "16a1566c-84a0-4e8b-82e3-653c271a247b";     // Key03用のuuid（英字は小文字で設定）

    private BluetoothAdapter mBluetoothAdapter;
    Handler mHandler = new Handler();
    private String g_uuid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // BLUETOOTHが利用可能かどうか、およびBLUETOOTHがONかどうかをチェック
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            // BLUETOOTHがOFFの場合、ONにするよう求めるダイアログを表示
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
        }

        Button btn = (Button)findViewById(R.id.button1);
        btn.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick(View v) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // タイムアウト
                        Log.d(TAG, "タイムアウト");
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);

                        // textView1 の内容を取得
                        TextView textView = (TextView) findViewById(R.id.textView1);

                        // iBeacon が見つからなかった場合は "Scanning...." なので
                        // "Not Found." に変更し、invalidate() を使い再描画
                        // 見つかった場合は iBeacon の情報が格納されているので
                        // invalidate() を使い再描画し、通知を送信
                        if (textView.getText() == SCANNING) {
                            textView.setText(NOT_FOUND);
                            textView.invalidate();
                        } else {
                            textView.invalidate();
                            sendNotification(g_uuid);
                        }

                        // スマートウォッチからのReplyを取得
                        String stringFromReply = getIntent().getStringExtra(EXTRA_REPLY);
                        if (stringFromReply != null) {
                            TextView textFromReply = (TextView) findViewById(R.id.reply);
                            textFromReply.setText(stringFromReply);
                            textFromReply.invalidate();
                        }

                    }
                }, SCAN_PERIOD);

                // スキャン開始
                mBluetoothAdapter.startLeScan(mLeScanCallback);

                // ボタンが押された後に textView1 を "Scanning...." に変更
                TextView textView = (TextView) findViewById(R.id.textView1);
                textView.setText(SCANNING);
            }
        } );

    }

    private void sendNotification(String g_uuid) {

//        Log.d(TAG, "g_uuid: ["+ g_uuid + "]");

        String contextTitle;
        String contextText;

        // UUID により、顧客情報と通知の種類を変更する
        if (g_uuid.equals(KEY01)) {
            sendNotification_Key01();

        } else if (g_uuid.equals(KEY02)) {
            sendNotification_Key02();

        } else if (g_uuid.equals(KEY03)) {
            sendNotification_Key03();

        } else {
            Log.d(TAG, "不明なUUID - g_uuid: ["+ g_uuid + "]");
        }

    }

    private void sendNotification_Key01() {

        // 詳細情報用ページのメッセージを作成
        CharSequence message = TextUtils.concat(
                "他の宿泊者", "\n",
                "ﾔﾏﾀﾞ ﾊﾅｺ", "\n",
                "ﾔﾏﾀﾞ ｼﾞﾛｳ", "\n",
                "CI: 2016/07/14", "\n",
                "CO: 2016/07/16");

        // 詳細情報用ページの通知を作成
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle()
            .bigText(message);

        Notification infoPage = new NotificationCompat.Builder(this)
            .setContentTitle("1001: ﾔﾏﾀﾞ ﾀﾛｳ")
            .setContentText(message)
            .setStyle(bigTextStyle)
            .build();

        /*****************************************
        // 対応用のアクションを追加
        PendingIntent pendingIntent = getPendingIntent();
        NotificationCompat.Action action = new NotificationCompat.Action(
                R.drawable.ic_full_reply, "対応します", pendingIntent);

        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
            .addPage(infoPage)
            .addAction(action);
        *************************************************/

        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                .addPage(infoPage);


        // 通知の準備
        Notification notificationWithPage = new NotificationCompat.Builder(this)
            .setSmallIcon(R.mipmap.ic_launcher)                      // アイコン画像（必須）
            .setContentTitle("ゲスト外出")                           // タイトル（必須）
            .setContentText("山田様がｴﾚﾍﾞｰﾀ乗車(10階)")      // メッセージ内容（必須）
            .setDefaults(Notification.DEFAULT_VIBRATE)              // バイブレーション
                .extend(wearableExtender)
            .build();

        // 通知を送るためのノティフィケーションマネージャーの生成
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // 通知を送る
        notificationManager.notify(1, notificationWithPage);

    }

    private void sendNotification_Key02() {

        // 詳細情報用ページのメッセージを作成
        CharSequence message = TextUtils.concat(
                "他の宿泊者", "\n",
                "ﾀｶﾊｼ ﾐｴ", "\n",
                "CI: 2016/07/15", "\n",
                "CO: 2016/07/17");

        // 詳細情報用ページの通知を作成
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle()
                .bigText(message);

        Notification infoPage = new NotificationCompat.Builder(this)
                .setContentTitle("0707: ｽｽﾞｷ ﾚｲｺ")
                .setContentText(message)
                .setStyle(bigTextStyle)
                .build();

        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                .addPage(infoPage);

        // 通知の準備
        Notification notificationWithPage = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)                      // アイコン画像（必須）
                .setContentTitle("ゲスト戻り")                           // タイトル（必須）
                .setContentText("鈴木様がエントランス到着")             // メッセージ内容（必須）
                .setDefaults(Notification.DEFAULT_VIBRATE)              // バイブレーション
                .extend(wearableExtender)
                .build();

        // 通知を送るためのノティフィケーションマネージャーの生成
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // 通知を送る
        notificationManager.notify(2, notificationWithPage);

    }

    private void sendNotification_Key03() {

        // 通知の準備
        Notification notificationWithPage = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)                      // アイコン画像（必須）
                .setContentTitle("ゲスト帰宅")                           // タイトル（必須）
                .setContentText("KEY03様がレストランから帰宅")      // メッセージ内容（必須）
                .setDefaults(Notification.DEFAULT_VIBRATE)              // バイブレーション
                .build();

        // 通知を送るためのノティフィケーションマネージャーの生成
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // 通知を送る
        notificationManager.notify(3, notificationWithPage);

    }

    private PendingIntent getPendingIntent() {
        Intent mainActivityIntent = new Intent(this, MainActivity.class);

        mainActivityIntent.putExtra(EXTRA_REPLY, "スタッフ001が対応します");

        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(this, 0,
                    mainActivityIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    // BLUETOOTHをONにするよう求めるダイアログの結果を受けてからの処理
    @Override
    protected void onActivityResult(int requestCode, int ResultCode, Intent date){
        if(requestCode == REQUEST_ENABLE_BLUETOOTH){
            if(ResultCode == Activity.RESULT_OK){
                // BLUETOOTHがONにされた場合の処理
                Log.d(TAG, "Bluetooth enabled.");
            }else{
                Log.d(TAG, "Bluetooth not enabled.");
                finish();
            }
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,byte[] scanRecord) {
            Log.d(TAG, "receive!!!");

            getScanData(scanRecord);

            Log.d(TAG, "device name:" + device.getName());
            Log.d(TAG, "device address:" + device.getAddress());
        }

    };

    private void getScanData( byte[] scanRecord ){
        if(scanRecord.length > 30)
        {
            if((scanRecord[5] == (byte)0x4c) && (scanRecord[6] == (byte)0x00) &&
                    (scanRecord[7] == (byte)0x02) && (scanRecord[8] == (byte)0x15))
            {
                String uuid = Integer.toHexString(scanRecord[9] & 0xff)
                        + Integer.toHexString(scanRecord[10] & 0xff)
                        + Integer.toHexString(scanRecord[11] & 0xff)
                        + Integer.toHexString(scanRecord[12] & 0xff)
                        + "-"
                        + Integer.toHexString(scanRecord[13] & 0xff)
                        + Integer.toHexString(scanRecord[14] & 0xff)
                        + "-"
                        + Integer.toHexString(scanRecord[15] & 0xff)
                        + Integer.toHexString(scanRecord[16] & 0xff)
                        + "-"
                        + Integer.toHexString(scanRecord[17] & 0xff)
                        + Integer.toHexString(scanRecord[18] & 0xff)
                        + "-"
                        + Integer.toHexString(scanRecord[19] & 0xff)
                        + Integer.toHexString(scanRecord[20] & 0xff)
                        + Integer.toHexString(scanRecord[21] & 0xff)
                        + Integer.toHexString(scanRecord[22] & 0xff)
                        + Integer.toHexString(scanRecord[23] & 0xff)
                        + Integer.toHexString(scanRecord[24] & 0xff);

                String major = Integer.toHexString(scanRecord[25] & 0xff) + Integer.toHexString(scanRecord[26] & 0xff);
                String minor = Integer.toHexString(scanRecord[27] & 0xff) + Integer.toHexString(scanRecord[28] & 0xff);

                Log.d(TAG, "UUID:"+uuid );
                Log.d(TAG, "major:" + major);
                Log.d(TAG, "minor:" + minor);

                // アプリ側に受け渡す変数に格納
                g_uuid = uuid;

                // Intensity(rssi) を取得
                String rssi = Integer.toHexString(scanRecord[29] & 0xff);

                // iBeacon の情報を textView1 に格納
                TextView textView = (TextView) findViewById(R.id.textView1);
                textView.setText("uuid: "+uuid + "\n" + "major: " + major + "\n" + "minor: " + minor + "\n" + "intensity: -" + rssi);

                // コールバック関数の中は画面の再描画がされないので、
                // mHandler.postDelayed() において textView.invalidate() を呼び出す

            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
