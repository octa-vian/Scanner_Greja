package co.id.gmedia.octavian.scannerapkgreja;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;
import com.journeyapps.barcodescanner.camera.CenterCropStrategy;
import com.leonardus.irfan.bluetoothprinter.BluetoothPrinter;
import com.leonardus.irfan.bluetoothprinter.Model.Transaksi;
import com.leonardus.irfan.bluetoothprinter.NotaPrinter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import co.id.gmedia.octavian.scannerapkgreja.model.ModelList;
import co.id.gmedia.octavian.scannerapkgreja.model.ModelScann;
import co.id.gmedia.octavian.scannerapkgreja.util.APIvolley;
import co.id.gmedia.octavian.scannerapkgreja.util.AppSharedPreferences;
import co.id.gmedia.octavian.scannerapkgreja.util.Printer;
import co.id.gmedia.octavian.scannerapkgreja.util.Server;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class ActivityScanner extends AppCompatActivity {

    private AudioManager audioManager;
    private static Context activity;
    private int max_volume = 100;
    private int current_volume = 0;
    private Printer printerManager;
    private NotaPrinter Nprinter;
    private final int PERMISSION_CODE = 900;
    private DecoratedBarcodeView barcodeView;
    private MediaPlayer mp_success, mp_fail;
    private boolean dialog_show = false;
    private String Id ="";
    private Activity cotext;
    private ImageView btn_exit;
    private String id_qr ="", nama = "", tanggal = "", jam = "", kursi ="", tempat="", denah="", namaIbdh="";
    private ModelList item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        cotext = this;
        activity = this;
        Nprinter = new NotaPrinter(cotext);
        btn_exit = findViewById(R.id.exit);
       //Nprinter.startService();

        if(getIntent().hasExtra(Server.EXTRA_ID)){
            Gson gson = new Gson();
            item = gson.fromJson(getIntent().getStringExtra(Server.EXTRA_ID), ModelList.class);
        }

        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppSharedPreferences.Logout(ActivityScanner.this);
                Intent intent = new Intent(ActivityScanner.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        barcodeView = findViewById(R.id.barcode_scanner);
        Collection<BarcodeFormat> formats = Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39);
        barcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats));
        barcodeView.initializeFromIntent(getIntent());
        barcodeView.setStatusText("Posisikan barcode didalam area pindai");
        BarcodeCallback barcodeCallback= new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if(result.getText() == null) {
                    return;
                }
                //playBeep(true);
                Qrcode(result.getText());
                Id = result.getText();
                barcodeView.pause();
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {

            }
        };
        barcodeView.decodeContinuous(barcodeCallback);

        if(!checkPermission()){
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.CAMERA}, PERMISSION_CODE);
        }

        //AUTO LOUD
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        current_volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        max_volume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        increaseVolume();
        Toast.makeText(this, "Volume telah dinaikkan secara otomatis", Toast.LENGTH_SHORT).show();

        mp_success = MediaPlayer.create(this, R.raw.beep);
        mp_fail = MediaPlayer.create(this, R.raw.fail);

    }

    private void increaseVolume(){
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, max_volume, AudioManager.FLAG_SHOW_UI);
    }

    private void decreaseVolume(){
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, current_volume, AudioManager.FLAG_SHOW_UI);
    }

    private void playBeep(boolean success){
        if(success){
            if(mp_success != null){
                mp_success.start();
            }
        }
        else{
            if(mp_fail != null){
                mp_fail.start();
            }
        }
    }

    private Bitmap createBitmapFromView(String kursi) {
        TextView tv = new TextView(activity);
        tv.setText(kursi);
        tv.setTextColor(Color.BLACK);
        tv.setGravity(Gravity.CENTER);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize(45);
        tv.setBackgroundColor(Color.WHITE);
        int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

        /*Bitmap b = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length)
        profileImage.setImageBitmap(Bitmap.createScaledBitmap(b, 120, 120, false));*/

        tv.measure(spec, spec);
        tv.layout(0, 0, tv.getMeasuredWidth(), tv.getMeasuredHeight());
        Bitmap b = Bitmap.createBitmap(tv.getMeasuredWidth(), tv.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        //c.translate((-tv.getScrollX()), (-tv.getScrollY()));
        int h = Integer.parseInt(String.valueOf(tv.getMeasuredHeight()));
        int w = Integer.parseInt(String.valueOf(tv.getMeasuredWidth()));
        double hs = (double) h/w;
        double sh = (double) hs*w;
        tv.draw(c);
        tv.getTextScaleX();
        return Bitmap.createScaledBitmap(b, (int) sh, h, false);

    }



    private void Qrcode( String Qr) {

        JSONObject object = new JSONObject();
        try {

            object.put("nobukti",Qr);
            object.put("id_jadwal",item.getItem1());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        new APIvolley(cotext, object, "POST", Server.URL_SCAN, new APIvolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {

                    JSONObject ob = new JSONObject(result);
                    String message = ob.getJSONObject("metadata").getString("message");
                    String status = ob.getJSONObject("metadata").getString("status");

                    if (Integer.parseInt(status) == 200){

                        id_qr = ob.getJSONObject("response").getString("nobukti");
                        nama = ob.getJSONObject("response").getString("nama");
                        tanggal = ob.getJSONObject("response").getString("tanggal");
                        jam = ob.getJSONObject("response").getString("jam");
                        kursi = ob.getJSONObject("response").getString("kursi");
                        tempat = ob.getJSONObject("response").getString("tempat");
                        denah = ob.getJSONObject("response").getString("denah");
                        namaIbdh = ob.getJSONObject("response").getString("nama_ibadah");

                        playBeep(true);
                        Display display = getWindowManager().getDefaultDisplay();
                        Point size = new Point();
                        display.getRealSize(size);
                        int device_TotalWidth = size.x;
                        final Dialog dialog = new Dialog(ActivityScanner.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.setContentView(R.layout.popup_dialog_sukses);

                        if(dialog.getWindow() != null){
                            dialog.getWindow().setLayout(device_TotalWidth * 100 / 100 ,
                                    WRAP_CONTENT);
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                            lp.copyFrom(dialog.getWindow().getAttributes());
                            lp.gravity = Gravity.BOTTOM;
                            lp.windowAnimations = R.style.DialogAnimation;
                            dialog.getWindow().setAttributes(lp);
                        }
                        dialog.setCancelable(false);

                        TextView txt_Id, txt_nama, txt_tgl, txt_jam, txt_kursi, txt_tempat, txt_namaIbdh;
                        Button btn_ok;
                        ImageView img_back;


                        txt_Id = dialog.findViewById(R.id.txt_ID);
                        btn_ok = dialog.findViewById(R.id.btn_ok);
                        txt_nama = dialog.findViewById(R.id.txt_nama);
                        txt_tgl = dialog.findViewById(R.id.txt_tanggal);
                        txt_jam = dialog.findViewById(R.id.txt_jam);
                        txt_kursi = dialog.findViewById(R.id.txt_kursi);
                        txt_tempat = dialog.findViewById(R.id.txt_tempat);
                        txt_namaIbdh = dialog.findViewById(R.id.nama_ibadah);
                        img_back = dialog.findViewById(R.id.img_back);

                        txt_Id.setText(id_qr);
                        txt_nama.setText(nama);
                        txt_jam.setText(jam);
                        txt_tgl.setText(tanggal);
                        txt_kursi.setText(kursi);
                        txt_tempat.setText(denah);
                        txt_namaIbdh.setText(namaIbdh);

                        img_back.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                barcodeView.resume();
                                dialog.dismiss();
                                dialog_show = false;
                            }
                        });

                        btn_ok.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Nprinter.startService();

               /* Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.ic_logo);
                Nprinter = new Printer(ActivityScanner.this, Bitmap.createScaledBitmap(
                        logo, 550, 180, false));*/

                                Nprinter.setListener(new BluetoothPrinter.BluetoothListener() {
                                    @Override
                                    public void onBluetoothConnected() {
                                        Transaksi modelScann = new Transaksi(nama, jam, kursi, id_qr, tanggal, denah, createBitmapFromView(kursi), namaIbdh);
                                        Nprinter.print(modelScann);


                                    }

                                    @Override
                                    public void onBluetoothFailed(String message) {
                                        Toast.makeText(ActivityScanner.this, message, Toast.LENGTH_SHORT).show();
                                    }
                                });
                                barcodeView.resume();
                                dialog.dismiss();
                                dialog_show = false;

                            }
                        });
                        dialog.show();
                        dialog_show = true;
                    } else{

                        playBeep(false);
                        Display display = getWindowManager().getDefaultDisplay();
                        Point size = new Point();
                        display.getRealSize(size);
                        int device_TotalWidth = size.x;
                        final Dialog dialog = new Dialog(ActivityScanner.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.setContentView(R.layout.popup_dialog_gagal);

                        if(dialog.getWindow() != null){
                            dialog.getWindow().setLayout(device_TotalWidth * 100 / 100 ,
                                    WRAP_CONTENT);
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                            lp.copyFrom(dialog.getWindow().getAttributes());
                            lp.gravity = Gravity.BOTTOM;
                            lp.windowAnimations = R.style.DialogAnimation;
                            dialog.getWindow().setAttributes(lp);
                        }
                        dialog.setCancelable(false);

                        TextView tx_message, txt_nama, txt_tgl, txt_jam, txt_kursi;
                        Button btn_ok;
                        ImageView img_back;


                        tx_message = dialog.findViewById(R.id.txt_message);
                        btn_ok = dialog.findViewById(R.id.btn_ok);
                        img_back = dialog.findViewById(R.id.img_back);

                        tx_message.setText(message);

                        img_back.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                barcodeView.resume();
                                dialog.dismiss();
                                dialog_show = false;
                            }
                        });

                        btn_ok.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                barcodeView.resume();
                                dialog.dismiss();
                                dialog_show = false;

                            }
                        });
                        dialog.show();
                        dialog_show = true;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(String result) {
                Toast.makeText(ActivityScanner.this, "Kesalahan Jaringan", Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean checkPermission(){
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[]
            permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSION_CODE){
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                barcodeView.resume();
            }
            return;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onResume() {
        if(!dialog_show){
            barcodeView.resume();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if(Nprinter != null){
            Nprinter.stopService();
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        barcodeView.pause();
        decreaseVolume();
        super.onPause();

    }
}