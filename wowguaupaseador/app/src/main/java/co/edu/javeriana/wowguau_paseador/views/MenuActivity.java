package co.edu.javeriana.wowguau_paseador.views;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import co.edu.javeriana.wowguau_paseador.R;
import co.edu.javeriana.wowguau_paseador.model.Direccion;
import co.edu.javeriana.wowguau_paseador.model.Paseador;
import co.edu.javeriana.wowguau_paseador.utils.FirebaseUtils;
import co.edu.javeriana.wowguau_paseador.utils.Utils;

public class MenuActivity extends AppCompatActivity {
    private ConstraintLayout cl_logout;
    private TextView tv_h_nombre;
    private ImageView iv_perfil;
    private TextView tv_estado;
    private TextView tv_saldo;

    private AppBarConfiguration mAppBarConfiguration;
    private FirebaseAuth mAuth;
    private ListenerRegistration registration;
    private FirebaseFirestore db;
    private DocumentReference docRef;
    private Paseador paseador;
    private FragmentRefreshListener fragmentRefreshListener;

    private String TAG="MENU";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerview = navigationView.getHeaderView(0);
        tv_h_nombre = headerview.findViewById(R.id.tv_h_nombre);
        iv_perfil = headerview.findViewById(R.id.iv_perfil);
        tv_estado = headerview.findViewById(R.id.estado);
        tv_saldo = headerview.findViewById(R.id.saldo);

        cl_logout = findViewById(R.id.cl_logout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_inicio, R.id.nav_actualizar, R.id.nav_historial) //acá se agregan las otras opciones del menu
                .setDrawerLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        headerview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), PerfilActivity.class);
                i.putExtra("user", paseador);
                startActivity(i);
            }
        });
        cl_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(MenuActivity.this, LoginActivity.class);
                intent.setFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }
    @Override
    public void onStart() {
        super.onStart();
        if(getIntent().hasExtra("uid")) {
            docRef = db.collection("Paseadores").document(getIntent().getStringExtra("uid"));
            registration = docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        Log.d(TAG, "Current data: " + snapshot.getData());
                        Direccion dir = new Direccion(String.valueOf(((HashMap)snapshot.get("direccion")).get("direccion")), Double.parseDouble(((HashMap)snapshot.get("direccion")).get("latitud").toString()), Double.parseDouble(((HashMap)snapshot.get("direccion")).get("longitud").toString()));
                        paseador = new Paseador(String.valueOf(snapshot.get("correo")), String.valueOf(snapshot.get("nombre")), Long.parseLong(snapshot.get("cedula").toString()), snapshot.getTimestamp("fechaNacimiento").toDate(), Long.parseLong(snapshot.get("telefono").toString()), String.valueOf(snapshot.get("genero")), dir, String.valueOf(snapshot.get("descripcion")), Integer.parseInt(snapshot.get("experiencia").toString()));
                        paseador.setDireccionFoto(snapshot.get("direccionFoto").toString());
                        updateUI();
                    } else {
                        Log.d(TAG, "Current data: null");
                    }
                }
            });
        }
        else
            return;
    }
    @Override
    protected void onDestroy() {
        if(registration!=null)
            registration.remove();
        super.onDestroy();
    }

    private void updateUI(){

        db.collection("Paseadores").document(mAuth.getCurrentUser().getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                Map<String,Object> vals = documentSnapshot.getData();
                paseador.setNombre( (String) vals.get("nombre"));
                paseador.setSaldo( (long) vals.get("saldo"));
                paseador.setEstado((boolean) vals.get("estado"));

                tv_h_nombre.setText(paseador.getNombre());
                tv_estado.setText(" "+(paseador.isEstado()? "Disponible": "No disponible"));
                tv_saldo.setText(paseador.getSaldo()+" petCoins");
                fragmentRefreshListener.onRefresh();
            }
        });

        //FirebaseUtils.descargarFotoImageView( paseador.getDireccionFoto(), iv_perfil);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public Paseador getPaseador() {
        return paseador;
    }

    public void setPaseador(Paseador paseador) {
        this.paseador = paseador;
        updateUI();
    }
    public FragmentRefreshListener getFragmentRefreshListener() {
        return fragmentRefreshListener;
    }

    public void setFragmentRefreshListener(FragmentRefreshListener fragmentRefreshListener) {
        this.fragmentRefreshListener = fragmentRefreshListener;
    }

    public interface FragmentRefreshListener{
        void onRefresh();
    }
}
