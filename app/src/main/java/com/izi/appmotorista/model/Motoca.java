package com.izi.appmotorista.model;


import com.google.firebase.database.DatabaseReference;
import com.izi.appmotorista.helper.ConfiguracaoFirebase;

public class Motoca {

   private Double latitude;
   private Double longitude;


    public void salvar(){

        DatabaseReference firebaseMotoca = ConfiguracaoFirebase.getFirebase();
        DatabaseReference produtoMotocaa = firebaseMotoca
                .child("motoca");

        // setIdPedido(produto.getIdProduto());
        produtoMotocaa.setValue(this);
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
