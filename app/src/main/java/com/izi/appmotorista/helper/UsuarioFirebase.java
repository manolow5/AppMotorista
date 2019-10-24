package com.izi.appmotorista.helper;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;


public class UsuarioFirebase {

    public static String getIdUsuario(){

        FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        return autenticacao.getCurrentUser().getUid();

    }

    public static FirebaseUser getUsuarioAtual(){
        FirebaseAuth usuario = ConfiguracaoFirebase.getFirebaseAutenticacao();
        return usuario.getCurrentUser();
    }

    public static boolean atualizarTipoUsuario(String tipo){

        try {

            FirebaseUser user = getUsuarioAtual();
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setDisplayName(tipo)
                    .build();
            user.updateProfile(profile);
            return true;

        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }




  /*  public static void redirecionaUsuarioLogado(final Activity activity){

        FirebaseUser user = getUsuarioAtual();
        if(user != null ){
            Log.d("resultado", "onDataChange: " + getIdentificadorUsuario());
            DatabaseReference usuariosRef = ConfiguracaoFirebase.getFirebaseDatabase()
                    .child("cliente")
                    .child( getIdentificadorUsuario() );
            usuariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        Log.d("resultado", "onDataChange: " + dataSnapshot.toString());
                        Usuario usuario = dataSnapshot.getValue(Usuario.class);
                        //String tipoUsuario = empresa.getTipo();
                        Intent i = new Intent(activity, MainActivity.class);
                        activity.startActivity(i);
                        activity.finish();
                    }else {
                        Intent i = new Intent(activity, InicioActivity.class);
                        activity.startActivity(i);
                        activity.finish();

                    }



                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }else {
            Intent inicio = new Intent(activity, InicioActivity.class);
            activity.startActivity(inicio);
        }

    }
   */
    public static String getIdentificadorUsuario(){
        return getUsuarioAtual().getUid();
    }

}
