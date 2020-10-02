package co.id.gmedia.octavian.scannerapkgreja;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import co.id.gmedia.octavian.scannerapkgreja.model.ModelList;
import co.id.gmedia.octavian.scannerapkgreja.util.APIvolley;
import co.id.gmedia.octavian.scannerapkgreja.util.AppSharedPreferences;
import co.id.gmedia.octavian.scannerapkgreja.util.Server;

public class ActivityListJadwal extends AppCompatActivity {

    private List<ModelList> listItem = new ArrayList<>();
    private AdapterListJadwal adapterListJadwal;
    private ImageView btn_logout, img_profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_jadwal);

        btn_logout = findViewById(R.id.logout);
        img_profile = findViewById(R.id.profile);

        RecyclerView rcView = findViewById(R.id.rv_list_jadwal);
        rcView.setItemAnimator(new DefaultItemAnimator());
        rcView.setLayoutManager(new LinearLayoutManager(ActivityListJadwal.this, LinearLayoutManager.VERTICAL, false));
        adapterListJadwal = new AdapterListJadwal(ActivityListJadwal.this, listItem);
        rcView.setAdapter(adapterListJadwal);

        img_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(ActivityListJadwal.this);
                dialog.setContentView(R.layout.pop_up_profile);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                TextView tv_nama;
                Button btn_logout;

                btn_logout = dialog.findViewById(R.id.btn_logout);
                tv_nama = dialog.findViewById(R.id.txt_nama);
                tv_nama.setText(AppSharedPreferences.getNama(ActivityListJadwal.this));

                btn_logout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AppSharedPreferences.Logout(ActivityListJadwal.this);
                        Intent intent = new Intent(ActivityListJadwal.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                });

                dialog.show();
            }
        });

        getData();
    }

    private void getData() {
        JSONObject object = new JSONObject();
        try {
            object.put("start",0);
            object.put("limit",10);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new APIvolley(ActivityListJadwal.this, object, "POST", Server.URL_LIST_JADWAL, new APIvolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                listItem.clear();
                try {
                    JSONObject ob = new JSONObject(result);
                    String message = ob.getJSONObject("metadata").getString("message");
                    String status = ob.getJSONObject("metadata").getString("status");

                    if (Integer.parseInt(status)==200){
                        JSONArray jsonArray = ob.getJSONArray("response");
                        for (int i=0; i < jsonArray.length(); i++){
                            JSONObject object1 = jsonArray.getJSONObject(i);
                            listItem.add(new ModelList(
                                    object1.getString("id")
                                    ,object1.getString("tanggal")
                                    ,object1.getString("jam")
                                    ,object1.getString("tempat")
                                    ,object1.getString("status")
                                    ,object1.getString("status_warna")
                                    ,object1.getString("nama_ibadah")
                            ));
                        }
                    } else {
                        Toast.makeText(ActivityListJadwal.this, message, Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                adapterListJadwal.notifyDataSetChanged();
            }

            @Override
            public void onError(String result) {
                Toast.makeText(ActivityListJadwal.this, "Kesalahan Jaringan", Toast.LENGTH_SHORT).show();
            }
        });

    }
}