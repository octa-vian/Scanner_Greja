package com.leonardus.irfan.bluetoothprinter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.leonardus.irfan.bluetoothprinter.Model.Item;
import com.leonardus.irfan.bluetoothprinter.Model.Transaksi;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NotaPrinter extends BluetoothPrinter {
    /*
        BLUETOOTH PRINTER Nota
        Library untuk menggunakan bluetooth printer. Langkah menggunakan :
        1. Buat objek NotaPrinter dengan menggunakan keyword new dengan parameter input context
            (ex : btPrint = new BluetoothPrinter(this))
        2. panggil method startService untuk menginisialisasi object bluetooth printer
            (ex : btnPrint.startService())
        3. panggil method showDevices untuk melakukan koneksi dengan device bluetooth printer
            (ex : btPrint.showDevices())
        4. panggil method print dengan parameter transaksi untuk mencetak nota di device
            (ex :
            Calendar date = Calendar.getInstance();
            List<Item> listTransaksi = new ArrayList<>();
            listTransaksi.add(new Item("Perdana Simpati 5k + 1 GB Internet", 1000, 5500));
            listTransaksi.add(new Item("Denom 25k", 200, 20500));
            listTransaksi.add(new Item("Denom 100k", 55000, 97000));

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date jatuh_tempo = dateFormat.parse("11/09/2018");
            transaksi = new Transaksi("Yunma Jaya Cell", "Andi Kusworo", "PD001", date.getTime(), jatuh_tempo,listTransaksi);

            btPrint.print(transaksi))
        5. panggil method stopService untuk mengakhiri koneksi, saran : gunakan di method onDestroy Activity
            (ex : btPrint.stopService())
    */

    private byte[] header;

    public NotaPrinter(Context context){
        super(context);
    }

    public NotaPrinter(Context context, Bitmap bmp){
        super(context);
        header = PrintFormatter.decodeBitmap(bmp);
    }

    // this will send text data to be printed by the bluetooth printer
    public void print(Transaksi transaksi){
        header = PrintFormatter.decodeBitmap(transaksi.getIcon());
        final int NAMA_MAX = 15;
        final int JUMLAH_MAX = 4;
        final int HARGA_TOTAL_MAX = 11;

        if(bluetoothDevice == null){
            Toast.makeText(context, "Sambungkan ke device printer terlebih dahulu!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double jum = 0;
            byte[] cc = new byte[]{0x1B,0x21,0x00};  // 0- normal size text
            byte[] bb = new byte[]{0x1B,0x21,0x08};  // 1- only bold text
            byte[] bb2 = new byte[]{0x1B,0x21,0x20}; // 2- bold with medium text
            byte[] bb3 = new byte[]{0x1B,0x21,0x10}; // 3- bold with large text
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

            //PROSES CETAK HEADER
            outputStream.write(PrintFormatter.DEFAULT_STYLE);
            outputStream.write(PrintFormatter.ALIGN_CENTER);

            outputStream.write("\n".getBytes());
            outputStream.write("      GBI ROCK BANJARMASIN      \n".getBytes());
            outputStream.write("--------------------------------\n".getBytes());
            //outputStream.write(String.format("ID       :  %s\n", transaksi.getId_note()).getBytes());
            outputStream.write(PrintFormatter.ALIGN_CENTER);

            outputStream.write(bb2);
            //outputStream.write(String.format("%s\n", transaksi.getNo_nota()).getBytes());
            if(header != null){
                outputStream.write(header);
                outputStream.write(String.format(transaksi.getTempat()).getBytes());
                //outputStream.write(PrintFormatter.NEW_LINE);
            }

            outputStream.write(PrintFormatter.NEW_LINE);
            outputStream.write(PrintFormatter.DEFAULT_STYLE);
            outputStream.write(PrintFormatter.NEW_LINE);

            outputStream.write(PrintFormatter.ALIGN_LEFT);
            outputStream.write(String.format("Nama        : %s\n", transaksi.getOutlet()).getBytes());
            outputStream.write(String.format("Nama Ibadah : %s\n", transaksi.getNama_Ibadah()).getBytes());
            outputStream.write(String.format("%s\n", transaksi.getTgl_hari()+", "+transaksi.getSales()).getBytes());
            //outputStream.write(String.format("Jam:  %s\n", transaksi.getSales()).getBytes());

            outputStream.write(PrintFormatter.NEW_LINE);
            //PROSES CETAK TRANSAKSI
            outputStream.write("---------*Terima Kasih*---------\n".getBytes());
            outputStream.write("----*Tuhan Yesus Memberkati*----\n".getBytes());
            outputStream.write("\n".getBytes());
            outputStream.write("\n".getBytes());
            outputStream.write("\n".getBytes());
           /* outputStream.write(PrintFormatter.ALIGN_LEFT);
            outputStream.write("Nama Barang    Jumlah      Total\n".getBytes());
            outputStream.write("--------------------------------\n".getBytes());*/


            List<Item> listItem = transaksi.getListItem();
            for(int i = 0; i < listItem.size(); i++){
                Item t =  listItem.get(i);
                String nama = t.getNama() + " @" + RupiahFormatter.getRupiah(t.getHarga());
                String jumlah = String.valueOf(t.getJumlah());
                String harga_total = RupiahFormatter.getRupiah(t.getHarga()*t.getJumlah());

                int n = 1;
                if(nama.length() > NAMA_MAX){
                    n = Math.max((int)Math.ceil((double)nama.length()/(double)NAMA_MAX), n);
                }
                if(jumlah.length() > JUMLAH_MAX){
                    n = Math.max((int)Math.ceil((double)jumlah.length()/(double)JUMLAH_MAX), n);
                }
                if(harga_total.length() > HARGA_TOTAL_MAX){
                    n = Math.max((int)Math.ceil((double)harga_total.length()/(double)HARGA_TOTAL_MAX), n);
                }

                String[] nama_array = leftAligned(nama, NAMA_MAX, n);
                String[] jumlah_array = rightAligned(jumlah, JUMLAH_MAX, n);
                String[] harga_total_array = rightAligned(harga_total, HARGA_TOTAL_MAX, n);

                for(int j = 0; j < n; j++){
                    outputStream.write(String.format(Locale.getDefault(), "%s %s %s\n",
                            nama_array[j], jumlah_array[j], harga_total_array[j]).getBytes());
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
            String ppn = "DPP : " + RupiahFormatter.getRupiah(Math.round(transaksi.getTunai() * 10/ 11)) +
                    "  PPN : " + RupiahFormatter.getRupiah(Math.round(transaksi.getTunai() / 11));
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
            outputStream.write("Paket internet 24 jam 2GB Rp 20.000\n".getBytes());
            outputStream.write(PrintFormatter.DEFAULT_STYLE);*/
            outputStream.write(PrintFormatter.NEW_LINE);
            outputStream.write(PrintFormatter.NEW_LINE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String[] leftAligned(String s, int max_length, int n){
        //Mencetak transaksi secara rata kiri
        String[] result = new String[n];
        int counter = 0;
        for(int i = 0; i < n; i++){
            StringBuilder builder = new StringBuilder();
            for(int j = 0; j < max_length; j++){
                if(counter < s.length()){
                    builder.append(s.charAt(counter));
                    counter++;
                }
                else{
                    builder.append(" ");
                }
            }
            result[i] = builder.toString();
            //System.out.println(result[i]);
        }
        return result;
    }

    public String[] rightAligned(String s, int max_length, int n){
        //Mencekak transaksi secara rata kanan
        String[] result = new String[n];
        int counter = 0;
        for(int i = 0; i < n; i++) {
            StringBuilder builder = new StringBuilder();
            if (counter >= s.length()) {
                for (int j = 0; j < max_length; j++) {
                    builder.append(" ");
                }
            } else if (s.length() - i * max_length < max_length) {
                int pad = max_length - (s.length() - i * max_length);
                for (int j = 0; j < max_length; j++) {
                    if (j < pad) {
                        builder.append(" ");
                    } else {
                        builder.append(s.charAt(counter));
                        counter++;
                    }
                }
            } else {
                for (int j = 0; j < max_length; j++) {
                    builder.append(s.charAt(counter));
                    counter++;
                }
            }
            result[i] = builder.toString();
            //System.out.println(result[i]);
        }
        return result;
    }

    public byte[] getHeader() {
        return header;
    }

    @Override
    public BluetoothDevice getBluetoothDevice() {
        return super.getBluetoothDevice();
    }

    @Override
    public OutputStream getOutputStream() {
        return super.getOutputStream();
    }

    @Override
    public Context getContext() {
        return super.getContext();
    }


}