package co.id.gmedia.octavian.scannerapkgreja;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.JsonReader;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import co.id.gmedia.octavian.scannerapkgreja.util.APIvolley;
import co.id.gmedia.octavian.scannerapkgreja.util.AppSharedPreferences;
import co.id.gmedia.octavian.scannerapkgreja.util.Server;

public class LoginActivity extends AppCompatActivity {

    EditText edt_username, edt_pass;
    Button btn_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edt_username = findViewById(R.id.edt_username);
        edt_pass = findViewById(R.id.edt_pass);
        btn_login = findViewById(R.id.btn_login);

        if(AppSharedPreferences.isLoggedIn(this)){
            startActivity(new Intent(this, ActivityScanner.class));
            finish();
        }

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                useLogin();
            }
        });

    }

    private void useLogin() {
        JSONObject object = new JSONObject();

        try {
            object.put("username", edt_username.getText().toString());
            object.put("password", edt_pass.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new APIvolley(LoginActivity.this, object, "POST", Server.URL_LOGIN, new APIvolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {
                    JSONObject ob = new JSONObject(result);

                    String message = ob.getJSONObject("metadata").getString("message");
                    String status = ob.getJSONObject("metadata").getString("status");

                    if (Integer.parseInt(status)==200){
                        String Id = ob.getJSONObject("response").getString("id_scanner");
                        String token = ob.getJSONObject("response").getString("token");
                        String exp = ob.getJSONObject("response").getString("expired_at");
                        String nama = ob.getJSONObject("response").getString("nama");
                        AppSharedPreferences.Login(LoginActivity.this, Id, token );
                        Intent intent = new Intent(LoginActivity.this, ActivityScanner.class);
                        startActivity(intent);
                        finish();

                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();

                    } else{
                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String result) {
                Toast.makeText(LoginActivity.this, "Kesalahan Jaringan!", Toast.LENGTH_LONG).show();
            }
        });

    }

}