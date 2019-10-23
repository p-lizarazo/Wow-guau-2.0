package co.edu.javeriana.wow_guau.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import co.edu.javeriana.wow_guau.R;
import co.edu.javeriana.wow_guau.model.Perro;
import co.edu.javeriana.wow_guau.utils.FirebaseUtils;

public class ActividadPerfilPerro extends AppCompatActivity
{
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    ImageView imgViewFotoPerfilPerro;
    TextView textViewNombre;
    TextView textViewRaza;
    TextView textViewFechaNacimiento;
    TextView textViewTamano;
    TextView textViewSexo;
    TextView textViewObs;
    Button btnActualizar;
    Button btnMonitorear;
    String uidPerro;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actividad_perfil_perro);

        //Inflates
        imgViewFotoPerfilPerro = findViewById(R.id.imgViewPerfilPerro);
        textViewNombre = findViewById(R.id.txtViewNombre);
        textViewRaza = findViewById(R.id.txtViewRaza);
        textViewFechaNacimiento = findViewById(R.id.txtViewFechaNacimiento);
        textViewTamano = findViewById(R.id.txtViewTamano);
        textViewSexo = findViewById(R.id.txtViewSexo);
        textViewObs = findViewById(R.id.txtViewObs);
        btnActualizar = findViewById(R.id.btnActualizar);
        btnMonitorear = findViewById(R.id.btnMonitorear);

        uidPerro = getIntent().getStringExtra("idPerro");

        db = FirebaseFirestore.getInstance();


        fillData();

        btnActualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Toast.makeText(getApplicationContext(),"Funcionalidad a Implementar",Toast.LENGTH_LONG).show();
            }
        });
    }

    public void fillData()
    {
        db.collection("Mascotas")
                .whereEqualTo("perroID",uidPerro)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task)
                    {
                        if(task.isSuccessful())
                        {
                            for(QueryDocumentSnapshot document : task.getResult())
                            {
                                FirebaseUtils.descargarFotoImageView(document
                                        .getString("direccionFoto"),imgViewFotoPerfilPerro);

                                textViewNombre.setText("Nombre: " + document.getString("nombre"));
                                textViewRaza.setText("Raza: " + document.getString("raza"));

                                Date date = document.getDate("fechaNacimiento");
                                DateFormat dateFormat = new SimpleDateFormat("dd-mm-yyyy");
                                String strDate = dateFormat.format(date);

                                textViewFechaNacimiento.setText("Fecha de Nacimiento: " + strDate);

                                textViewTamano.setText("Tamano: "+ document.getString("tamano"));
                                textViewSexo.setText("Sexo: "+document.getString("sexo"));
                                textViewObs.setText("Observaciones: "+ document.getString("observaciones"));

                                if(!document.getBoolean("estado"))
                                {
                                    btnMonitorear.setText("No Activo");
                                    btnMonitorear.setTextColor(Color.GRAY);
                                    btnMonitorear.setClickable(false);
                                }

                            }
                        }
                        else
                        {
                            Log.d("Errror Perron", "Error al buscar perritos ", task.getException());
                        }
                    }
                });
    }
}