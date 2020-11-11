package co.id.gmedia.octavian.scannerapkgreja.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.leonardus.irfan.bluetoothprinter.BluetoothPrinter;
import com.leonardus.irfan.bluetoothprinter.NotaPrinter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import co.id.gmedia.octavian.scannerapkgreja.ActivityListJadwal;
import co.id.gmedia.octavian.scannerapkgreja.AdapterListJadwal;
import co.id.gmedia.octavian.scannerapkgreja.LoginActivity;
import co.id.gmedia.octavian.scannerapkgreja.R;
import co.id.gmedia.octavian.scannerapkgreja.model.ModelList;
import co.id.gmedia.octavian.scannerapkgreja.util.APIvolley;
import co.id.gmedia.octavian.scannerapkgreja.util.AppSharedPreferences;
import co.id.gmedia.octavian.scannerapkgreja.util.LoadMoreScrollListener;
import co.id.gmedia.octavian.scannerapkgreja.util.Server;


public class FragmentJadwal extends Fragment {
    private View view;
    private Activity activity;
    private List<ModelList> listItem = new ArrayList<>();
    public BluetoothPrinter bluetoothDevice;
    private AdapterListJadwal adapterListJadwal;
    private ImageView btn_logout, img_profile, img_wtm, img_search;
    private NotaPrinter Nprinter;
    private ProgressBar loading;
    private Button btn_ibadah, btn_event;
    private static EditText old_pass, new_pass, re_pass;
    private LoadMoreScrollListener loadMoreScrollListener;
    private EditText edt_search;

    public FragmentJadwal() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        activity = getActivity();
        if (view == null){
          view = inflater.inflate(R.layout.fragment_jadwal, container, false);

            //View
            btn_logout = view.findViewById(R.id.logout);
            img_profile = view.findViewById(R.id.profile);
            img_wtm = view.findViewById(R.id.wtf);
            loading = view.findViewById(R.id.loading);
            Nprinter = new NotaPrinter(activity);
            Nprinter.StartPermission();
            img_search = view.findViewById(R.id.img_search);
            img_search.setVisibility(View.GONE);
            edt_search = view.findViewById(R.id.edt_search);
            edt_search.setVisibility(View.GONE);

            RecyclerView rcView = view.findViewById(R.id.rv_list_jadwal);
            rcView.setItemAnimator(new DefaultItemAnimator());
            rcView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false));
            adapterListJadwal = new AdapterListJadwal(activity, listItem);
            rcView.setAdapter(adapterListJadwal);

            loadMoreScrollListener = new LoadMoreScrollListener() {
                @Override
                public void onLoadMore() {
                    getData(false);
                }
            };
            rcView.addOnScrollListener(loadMoreScrollListener);

            img_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Dialog dialog = new Dialog(activity);
                    dialog.setContentView(R.layout.pop_up_profile);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                    TextView tv_nama;
                    Button btn_logout, btn_ch_pass;

                    btn_logout = dialog.findViewById(R.id.btn_logout);
                    btn_ch_pass = dialog.findViewById(R.id.btn_ChangePass);
                    tv_nama = dialog.findViewById(R.id.txt_nama);
                    tv_nama.setText(AppSharedPreferences.getNama(activity));

                    btn_ch_pass.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            final Dialog dialog = new Dialog(activity);
                            dialog.setContentView(R.layout.popup_ganti_pass);
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            ImageView img_exit;
                            img_exit = dialog.findViewById(R.id.exit);
                            old_pass = dialog.findViewById(R.id.txt_pass_lama);
                            new_pass = dialog.findViewById(R.id.txt_pass);
                            re_pass = dialog.findViewById(R.id.txt_ulang_pass);

                            img_exit.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.dismiss();
                                }
                            });

                            dialog.findViewById(R.id.btn_simpan).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    initChangePass();
                                }
                            });
                            dialog.show();
                        }
                    });

                    btn_logout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AppSharedPreferences.Logout(activity);
                            Intent intent = new Intent(activity, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            activity.finish();
                        }
                    });

                    dialog.show();
                }
            });

            getData(true);

        }
         return view;
    }

    private void getData(final boolean init) {
        loading.setVisibility(View.VISIBLE);
        if (init){
            loadMoreScrollListener.initLoad();
        }
        JSONObject object = new JSONObject();
        try {
            object.put("start",loadMoreScrollListener.getLoaded());
            object.put("limit",10);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new APIvolley(activity, object, "POST", Server.URL_LIST_JADWAL, new APIvolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                loading.setVisibility(View.GONE);

                try {
                    if (init){
                        listItem.clear();
                    }
                    JSONObject ob = new JSONObject(result);
                    String message = ob.getJSONObject("metadata").getString("message");
                    String status = ob.getJSONObject("metadata").getString("status");

                    if (Integer.parseInt(status)==200){
                        img_wtm.setVisibility(View.GONE);
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
                        loadMoreScrollListener.finishLoad(jsonArray.length());
                        adapterListJadwal.notifyDataSetChanged();
                    } else {
                    }

                } catch (JSONException e) {
                    loadMoreScrollListener.finishLoad(0);
                    e.printStackTrace();
                }
                adapterListJadwal.notifyDataSetChanged();
            }

            @Override
            public void onError(String result) {
                loadMoreScrollListener.finishLoad(0);
                adapterListJadwal.notifyDataSetChanged();
                img_wtm.setVisibility(View.VISIBLE);
                listItem.clear();
                Toast.makeText(activity, "Kesalahan Jaringan", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void initChangePass() {

        JSONObject object = new JSONObject();
        try {
            object.put("oldpassword", old_pass.getText().toString());
            object.put("newpassword", new_pass.getText().toString());
            object.put("retype_newpassword", re_pass.getText().toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        new APIvolley(activity, object, "POST", Server.URL_CHANGE_PASS, new APIvolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject response = new JSONObject(result);
                    String message = response.getJSONObject("metadata").getString("message");
                    String status = response.getJSONObject("metadata").getString("status");

                    if(status.equals("200")){
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(activity, ActivityListJadwal.class);
                        startActivity(intent);
                        activity.finish();
                    }
                    else {
                        Toast.makeText(activity,message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String result) {
                Toast.makeText(activity,"Kesalahan Jaringan", Toast.LENGTH_SHORT).show();
            }
        });

    }
}