package com.leonardus.irfan.bluetoothprinter.Model;

import java.util.Date;
import java.util.List;

public class Transaksi {
    private String outlet;
    private String sales;
    private String no_nota;
    private String Id_note;
    private String tgl_hari;
    private String tempat;
    private Date tgl_transaksi;
    private Date jatuh_tempo;
    private double tunai;
    private double diskon;
    private List<Item> listItem;

    private double dibayar;

    public Transaksi(String outlet, String sales, String no_nota, Date tgl_transaksi, Date jatuh_tempo, List<Item> listItem){
        this.outlet = outlet;
        this.sales = sales;
        this.no_nota = no_nota;
        this.tgl_transaksi = tgl_transaksi;
        this.jatuh_tempo = jatuh_tempo;
        this.listItem = listItem;
    }

    public Transaksi(String outlet, String sales, String no_nota, String id_note, String tgl, String tempat){
        this.outlet = outlet;
        this.sales = sales;
        this.no_nota = no_nota;
        this.Id_note = id_note;
        this.tgl_hari = tgl;
        this.tempat = tempat;
    }

    public Transaksi(String outlet, String sales, String no_nota, Date tgl_transaksi, List<Item> listItem){
        this.outlet = outlet;
        this.sales = sales;
        this.no_nota = no_nota;
        this.tgl_transaksi = tgl_transaksi;
        this.listItem = listItem;
    }

    public void setTunai(double tunai){
        this.tunai = tunai;
    }
    public void setDiskon(double diskon){
        this.diskon = diskon;
    }

    public double getTunai() {
        return tunai;
    }

    public double getDiskon() {
        return diskon;
    }

    public String getNo_nota() {
        return no_nota;
    }

    public Date getTgl_transaksi() {
        return tgl_transaksi;
    }

    public Date getJatuh_tempo() {
        return jatuh_tempo;
    }

    public List<Item> getListItem() {
        return listItem;
    }

    public String getOutlet() {
        return outlet;
    }

    public String getSales() {
        return sales;
    }
    public String getId_note() {
        return Id_note;
    }
    public String getTgl_hari() {
        return tgl_hari;
    }
    public String getTempat() {
        return tempat;
    }
}
