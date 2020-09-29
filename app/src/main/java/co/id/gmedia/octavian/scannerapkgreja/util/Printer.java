package co.id.gmedia.octavian.scannerapkgreja.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.Toast;

import com.leonardus.irfan.bluetoothprinter.Model.Item;
import com.leonardus.irfan.bluetoothprinter.Model.Transaksi;
import com.leonardus.irfan.bluetoothprinter.NotaPrinter;
import com.leonardus.irfan.bluetoothprinter.PrintFormatter;
import com.leonardus.irfan.bluetoothprinter.RupiahFormatter;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import co.id.gmedia.octavian.scannerapkgreja.model.ModelScann;

public class Printer extends NotaPrinter {
    public Printer(Context context, Bitmap bmp) {
        super(context, bmp);
    }

    public void printNota(Transaksi transaksi){
        final int NAMA_MAX = 21;
        final int JUMLAH_MAX = 4;
        final int HARGA_MAX = 10;
        final int HARGA_TOTAL_MAX = 11;

        if(bluetoothDevice == null){
            Toast.makeText(getContext(), "Sambungkan ke device printer terlebih dahulu!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double jum = 0;
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

            //PROSES CETAK HEADER
            //outputStream.write(PrintFormatter.DEFAULT_STYLE);
            outputStream.write(PrintFormatter.ALIGN_LEFT);

            if(getHeader() != null){
                outputStream.write(getHeader());
                //outputStream.write(PrintFormatter.NEW_LINE);
            }

            /*getOutputStream().write(PrintFormatter.getSmall());
            getOutputStream().write("CV. Prima Agro Lancar\n".getBytes());
            getOutputStream().write("Pestisida, Pupuk, Benih\n".getBytes());
            getOutputStream().write("Jl. Gatot Subroto Blok A2 No.17 Komplek Industri Candi\nRT. 007 RW. 009\n".getBytes());
            getOutputStream().write("Ngaliyan Semarang - Jawa Tengah Telp/Fax (024) 7626900\n".getBytes());
            getOutputStream().write("NPWP - 02.774.909.2-503.000\n".getBytes());*/
            getOutputStream().write(PrintFormatter.NEW_LINE);

            outputStream.write(PrintFormatter.ALIGN_LEFT);
            outputStream.write(PrintFormatter.DEFAULT_STYLE);
            outputStream.write(String.format("Outlet      :  %s\n", transaksi.getOutlet()).getBytes());
            outputStream.write(String.format("Sales       :  %s\n", transaksi.getSales()).getBytes());
            outputStream.write(String.format("No. PS      :  %s\n", transaksi.getNo_nota()).getBytes());
            if(transaksi.getJatuh_tempo() != null){
                String stringJatuhTempo = dateFormat.format(transaksi.getJatuh_tempo());
                outputStream.write(String.format("Jatuh Tempo :  %s\n", stringJatuhTempo).getBytes());
            }

            outputStream.write(PrintFormatter.NEW_LINE);

            //PROSES CETAK TRANSAKSI
            outputStream.write("------------------------------------------------\n".getBytes());
            outputStream.write(PrintFormatter.ALIGN_LEFT);
            outputStream.write("Nama Barang          Jumlah     Harga      Total\n".getBytes());
            outputStream.write("------------------------------------------------\n".getBytes());
            outputStream.write(PrintFormatter.ALIGN_LEFT);

            List<Item> listItem = transaksi.getListItem();
            for(int i = 0; i < listItem.size(); i++){
                Item t =  listItem.get(i);
                String nama = t.getNama();
                String jumlah = String.valueOf(t.getJumlah());
                String harga = RupiahFormatter.getRupiah(t.getHarga());
                String harga_total = RupiahFormatter.getRupiah(t.getHarga()*t.getJumlah());

                int n = 1;
                if(nama.length() > NAMA_MAX){
                    n = Math.max((int)Math.ceil((double)nama.length()/(double)NAMA_MAX), n);
                }
                if(jumlah.length() > JUMLAH_MAX){
                    n = Math.max((int)Math.ceil((double)jumlah.length()/(double)JUMLAH_MAX), n);
                }
                if(harga.length() > HARGA_MAX){
                    n = Math.max((int)Math.ceil((double)harga.length()/(double)HARGA_MAX), n);
                }
                if(harga_total.length() > HARGA_TOTAL_MAX){
                    n = Math.max((int)Math.ceil((double)harga_total.length()/(double)HARGA_TOTAL_MAX), n);
                }

                String[] nama_array = leftAligned(nama, NAMA_MAX, n);
                String[] jumlah_array = rightAligned(jumlah, JUMLAH_MAX, n);
                String[] harga_array = rightAligned(harga, HARGA_MAX, n);
                String[] harga_total_array = rightAligned(harga_total, HARGA_TOTAL_MAX, n);

                for(int j = 0; j < n; j++){
                    outputStream.write(String.format(Locale.getDefault(), "%s %s %s%s\n",
                            nama_array[j], jumlah_array[j], harga_array[j], harga_total_array[j]).getBytes());
                }

                jum += t.getHarga()*t.getJumlah();
            }

            //transaksi.setTunai(jum); //tunai selalu sama dengan jumlah
            String jum_string, tunai_string, diskon_string;
            //String kembali_string;
            jum_string = RupiahFormatter.getRupiah(jum);

            outputStream.write(PrintFormatter.ALIGN_RIGHT);
            outputStream.write("----------".getBytes());
            outputStream.write("\nSUBTOTAL :  ".getBytes());
            outputStream.write(jum_string.getBytes());

            if(transaksi.getDiskon() != 0){
                diskon_string = RupiahFormatter.getRupiah(transaksi.getDiskon());
                StringBuilder builder = new StringBuilder(diskon_string);
                for(int i = 0; i < jum_string.length() - diskon_string.length(); i++){
                    builder.insert(0, " ");
                }
                diskon_string = builder.toString();
                outputStream.write("\nDISKON   :  ".getBytes());
                outputStream.write(diskon_string.getBytes());
            }

            tunai_string = RupiahFormatter.getRupiah(transaksi.getTunai());
            StringBuilder builder = new StringBuilder(tunai_string);
            for(int i = 0; i < jum_string.length() - tunai_string.length(); i++){
                builder.insert(0, " ");
            }
            tunai_string = builder.toString();
            //kembali_string = RupiahFormatter.getRupiah(transaksi.getTunai() - jum);

            outputStream.write("\nTUNAI    :  ".getBytes());
            outputStream.write(tunai_string.getBytes());

            /*outputStream.write("\nKEMBALI : ".getBytes());
            for(int i = 0; i < character_size - kembali_string.length(); i++){
                outputStream.write(" ".getBytes());
            }
            outputStream.write(kembali_string.getBytes());*/

            outputStream.write(PrintFormatter.NEW_LINE);
            outputStream.write(PrintFormatter.NEW_LINE);

            outputStream.write(PrintFormatter.ALIGN_LEFT);
            /*String ppn = "DPP : " + RupiahFormatter.getRupiah(Math.round(transaksi.getTunai() * 10/ 11)) +
                    "  PPN : " + RupiahFormatter.getRupiah(Math.round(transaksi.getTunai() / 11));*/

            String ppn = "Harga sudah termasuk PPN 10% kecuali Benih dibebaskan dari PPN";
            outputStream.write(ppn.getBytes());

            outputStream.write(PrintFormatter.NEW_LINE);
            outputStream.write(PrintFormatter.NEW_LINE);

            //PROSES CETAK FOOTER
            outputStream.write(PrintFormatter.ALIGN_CENTER);
            outputStream.write("Terima Kasih\n".getBytes());

            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
            String currentDateandTime = sdf.format(transaksi.getTgl_transaksi());

            outputStream.write(String.format("%s\n", currentDateandTime).getBytes());
            outputStream.write("==============================\n".getBytes());
            /*outputStream.write(PrintFormatter.getSmall());
            outputStream.write("Promo Telkomsel Flash\n".getBytes());
            outputStream.write("Paket internet 24 jam 2GB Rp 20.000\n".getBytes());*/
            outputStream.write(PrintFormatter.DEFAULT_STYLE);
            outputStream.write(PrintFormatter.NEW_LINE);
            outputStream.write(PrintFormatter.NEW_LINE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printerSc(ModelScann modelScann){
        final int NAMA_MAX = 17;
        final int TGL_MAX = 16;
        final int HARGA_TOTAL_MAX = 13;

        if(getBluetoothDevice() == null){
            Toast.makeText(getContext(), "Sambungkan ke device printer terlebih dahulu!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double jum = 0;
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

            //PROSES CETAK HEADER
            getOutputStream().write(PrintFormatter.DEFAULT_STYLE);
            getOutputStream().write(PrintFormatter.ALIGN_LEFT);

            if(getHeader() != null){
                getOutputStream().write(getHeader());
                //getOutputStream().write(PrintFormatter.NEW_LINE);
            }

            /*getOutputStream().write(PrintFormatter.getSmall());
            getOutputStream().write("CV. Prima Agro Lancar\n".getBytes());
            getOutputStream().write("Pestisida, Pupuk, Benih\n".getBytes());
            getOutputStream().write("Jl. Gatot Subroto Blok A2 No.17 Komplek Industri Candi RT. 007 RW. 009\n".getBytes());
            getOutputStream().write("Ngaliyan Semarang - Jawa Tengah Telp/Fax (024) 7626900\n".getBytes());
            getOutputStream().write("NPWP - 02.774.909.2-503.000\n".getBytes());
            getOutputStream().write(PrintFormatter.NEW_LINE);*/

            getOutputStream().write(PrintFormatter.NEW_LINE);
            outputStream.write(PrintFormatter.ALIGN_LEFT);
            outputStream.write(PrintFormatter.DEFAULT_STYLE);
            getOutputStream().write(String.format("ID       :  %s\n", modelScann.getItem4()).getBytes());
            getOutputStream().write(String.format("Nama      :  %s\n", modelScann.getItem1().trim()).getBytes());
            getOutputStream().write(String.format("Jam        :  %s\n", modelScann.getItem2()).getBytes());
            getOutputStream().write(String.format("Kursi       :  %s\n", modelScann.getItem3()).getBytes());
           /* getOutputStream().write(String.format("No. Nota    :  %s\n", modelScann.getNo_nota()).getBytes());
            getOutputStream().write(String.format("Cara Bayar  :  %s\n", modelScann.getCrBayar()).getBytes());*/

            getOutputStream().write(PrintFormatter.NEW_LINE);

            //PROSES CETAK TRANSAKSI
            getOutputStream().write("------------------------------------------------\n".getBytes());
            getOutputStream().write(PrintFormatter.ALIGN_LEFT);
            getOutputStream().write("Nomor nota       Tanggal Nota           Terbayar\n".getBytes());
            getOutputStream().write("------------------------------------------------\n".getBytes());
            getOutputStream().write(PrintFormatter.ALIGN_LEFT);


            //transaksi.setTunai(jum); //tunai selalu sama dengan jumlah
            String jum_string, tunai_string;
            //String kembali_string;
            jum_string = RupiahFormatter.getRupiah(jum);

            getOutputStream().write(PrintFormatter.ALIGN_RIGHT);
            getOutputStream().write("----------".getBytes());
            getOutputStream().write("\nSUBTOTAL :  ".getBytes());
            getOutputStream().write(jum_string.getBytes());

           // tunai_string = RupiahFormatter.getRupiah(pelunasan.getTunai());

            //kembali_string = RupiahFormatter.getRupiah(transaksi.getTunai() - jum);
            /*outputStream.write("\nKEMBALI : ".getBytes());
            for(int i = 0; i < character_size - kembali_string.length(); i++){
                outputStream.write(" ".getBytes());
            }
            outputStream.write(kembali_string.getBytes());*/

            getOutputStream().write(PrintFormatter.NEW_LINE);

            getOutputStream().write("------------------------------------------------\n".getBytes());
            getOutputStream().write("     Pelanggan                       Penagih    \n".getBytes());

            getOutputStream().write(PrintFormatter.NEW_LINE);
            getOutputStream().write(PrintFormatter.NEW_LINE);
            getOutputStream().write(PrintFormatter.NEW_LINE);
            getOutputStream().write(PrintFormatter.NEW_LINE);
            getOutputStream().write(PrintFormatter.NEW_LINE);

            /*getOutputStream().write(PrintFormatter.ALIGN_LEFT);
            String ppn = "DPP : " + RupiahFormatter.getRupiah(Math.round(pelunasan.getTunai() * 10/ 11)) +
                    "  PPN : " + RupiahFormatter.getRupiah(Math.round(pelunasan.getTunai() / 11));
            getOutputStream().write(ppn.getBytes());

            getOutputStream().write(PrintFormatter.NEW_LINE);
            getOutputStream().write(PrintFormatter.NEW_LINE);*/

            //PROSES CETAK FOOTER
            getOutputStream().write(PrintFormatter.ALIGN_CENTER);
            getOutputStream().write("Terima Kasih\n".getBytes());

            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
          //  String currentDateandTime = sdf.format(pelunasan.getTgl_transaksi());

            //getOutputStream().write(String.format("%s\n", currentDateandTime).getBytes());
            getOutputStream().write("==============================\n".getBytes());
            /*outputStream.write(PrintFormatter.getSmall());
            outputStream.write("Promo Telkomsel Flash\n".getBytes());
            outputStream.write("Paket internet 24 jam 2GB Rp 20.000\n".getBytes());
            outputStream.write(PrintFormatter.DEFAULT_STYLE);*/
            getOutputStream().write(PrintFormatter.NEW_LINE);
            getOutputStream().write(PrintFormatter.NEW_LINE);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
