package co.id.gmedia.octavian.scannerapkgreja;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.List;

import co.id.gmedia.octavian.scannerapkgreja.model.ModelList;
import co.id.gmedia.octavian.scannerapkgreja.util.Server;

/*import com.google.gson.Gson;

import java.util.List;

import co.id.gmedia.octavian.kartikaapps.activity.pembayaran.ActivityDetailPembayaranPiutang;
import co.id.gmedia.octavian.kartikaapps.model.ModelProduk;
import co.id.gmedia.octavian.kartikaapps.util.Constant;*/


public class AdapterListJadwal extends RecyclerView.Adapter<AdapterListJadwal.TemplateViewHolder> {

    private Activity activity;
    private List<ModelList> listItem;

    public AdapterListJadwal(Activity activity, List<ModelList> listItem){
        this.activity = activity;
        this.listItem = listItem ;
    }

    @NonNull
    @Override
    public TemplateViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new TemplateViewHolder(LayoutInflater.from(activity).
                inflate(R.layout.adapter_list_jadwal, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TemplateViewHolder templateViewHolder, int i) {
        final ModelList item = listItem.get(i);
        final int final_position = i;

        templateViewHolder.txt_nama.setText(item.getItem7());
        templateViewHolder.txt_tanggal.setText(item.getItem2());
        templateViewHolder.txt_jam.setText(item.getItem3());

      /*  final ModelProduk itemSelected = listItem.get(i);
        if(itemSelected.getItem5().toLowerCase().trim().equals("Terbayar")  || itemSelected.getItem5().toLowerCase().trim().equals("terbayar")){
            templateViewHolder.txt_status.setTextColor(activity.getResources().getColor(R.color.grey_dark));
        }
        else{
            templateViewHolder.txt_status.setTextColor(activity.getResources().getColor(R.color.color_red_drak));
        }*/

        /*final ModelProduk itemSelected = listItem.get(i);
        if(itemSelected.getItem5().toUpperCase().trim().equals("available")){
            templateViewHolder.txt_status.setTextColor(activity.getResources().getColor(R.color.color_green_dialog));
        }
        else if(itemSelected.getItem5().toUpperCase().trim().equals("preorder")){
            templateViewHolder.txt_status.setTextColor(activity.getResources().getColor(R.color.colorPrimary));
        }*/

         final Gson gson = new Gson();
            templateViewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(activity, ActivityScanner.class);
                i.putExtra(Server.EXTRA_ID, gson.toJson(item));
                activity.startActivity(i);

            }
        });



    }

    @Override
    public int getItemCount() {
        return listItem.size();
    }

    public static class TemplateViewHolder extends RecyclerView.ViewHolder{


        private ImageView iv_cardview;
        private TextView txt_nama, txt_jam, txt_tanggal;
        private CardView cardView;

        public TemplateViewHolder(@NonNull View itemView) {
            super(itemView);

            txt_nama = (TextView) itemView.findViewById(R.id.txt_nama);
            txt_jam = (TextView) itemView.findViewById(R.id.txt_jam);
            txt_tanggal = (TextView) itemView.findViewById(R.id.txt_tanggal);
            cardView = (CardView) itemView.findViewById(R.id.cr_view);


        }
    }
}
