package co.edu.javeriana.wowguau_paseador.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.Map;

import co.edu.javeriana.wowguau_paseador.R;
import co.edu.javeriana.wowguau_paseador.model.Paseo;
import co.edu.javeriana.wowguau_paseador.model.Perro;
import co.edu.javeriana.wowguau_paseador.utils.FirebaseUtils;
import co.edu.javeriana.wowguau_paseador.utils.Utils;

public class InfoPaseoActivity extends AppCompatActivity {
    ImageView iv_dog;
    TextView tv_nombre;
    TextView tv_raza;
    TextView tv_tamano;
    TextView tv_edad;
    TextView tv_genero;
    TextView tv_observaciones;
    TextView tv_duracion;
    TextView tv_costo;
    TextView tv_distancia;
    Button btn_aceptar;
    Button btn_rechazar;

    private FirebaseAuth mAuth;
    FirebaseUser mFireUser = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    Perro perro;
    String uidPerro;
    String uidPaseo;
    Paseo paseo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_paseo);
        mAuth = FirebaseAuth.getInstance();

        perro = (Perro) getIntent().getSerializableExtra("perro");
        uidPerro = getIntent().getStringExtra("uid");

        iv_dog = findViewById(R.id.iv_dog);
        tv_nombre = findViewById(R.id.tv_nombre);
        tv_raza = findViewById(R.id.tv_raza);
        tv_tamano = findViewById(R.id.tv_tamano);
        tv_edad = findViewById(R.id.tv_edad);
        tv_genero = findViewById(R.id.tv_genero);
        tv_observaciones = findViewById(R.id.tv_observaciones);
        tv_duracion = findViewById(R.id.tv_duracion);
        tv_costo = findViewById(R.id.tv_costo);
        tv_distancia = findViewById(R.id.tv_distancia);
        btn_aceptar = findViewById(R.id.btn_aceptar);
        btn_rechazar = findViewById(R.id.btn_rechazar);

        db.collection("Paseos")
                .whereEqualTo("uidPaseador","").whereEqualTo("estado",true).whereEqualTo("uidPerro", uidPerro)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            uidPaseo = document.getId();
                            paseo = new Paseo((String) document.getData().get("uidPerro"),
                                    "", (String) document.getData().get("uidDueno"),
                                    (long) document.getData().get("duracion"),
                                    (long) document.getData().get("costo"),
                                    (String)((Map<String, Object>) document.getData().get("direccion")).get("direccion"),
                                    (Double)((Map<String, Object>) document.getData().get("direccion")).get("latitud"),
                                    (Double) ((Map<String, Object>) document.getData().get("direccion")).get("longitud"),
                                    (Boolean) document.getData().get("estado"),
                                    (String) document.getData().get("nomPerro"),
                                    (String) document.getData().get("uriPerro"));



                            updateUI();
                        }
                    } else {
                        Log.d("INFOPASEO", "Error getting documents: ", task.getException());
                    }
                }
            });
        btn_aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final DocumentReference paseadorRef = db.collection("Paseadores").document(mAuth.getCurrentUser().getUid());
                final DocumentReference duenoRef = db.collection("Clientes").document(paseo.getUidDueno());

                db.runTransaction(new Transaction.Function<Void>() {
                    @Override
                    public Void apply(Transaction transaction) throws FirebaseFirestoreException {


                        DocumentSnapshot snapshotPaseador = transaction.get(paseadorRef);
                        DocumentSnapshot snapshotDueno = transaction.get(duenoRef);

                        // Note: this could be done without a transaction
                        //       by updating the population using FieldValue.increment()
                        long saldoPaseador = snapshotPaseador.getLong("saldo");
                        long saldoDueno = snapshotDueno.getLong("saldo");

                        long costo = paseo.getCosto();

                        if(saldoDueno>=costo){
                            saldoDueno-=costo;
                            saldoPaseador+=costo;
                        } else {
                            Toast.makeText(getApplicationContext(), "No se puede aceptar el paseo por fondos", Toast.LENGTH_LONG ).show();
                            throw new FirebaseFirestoreException("Fallo", FirebaseFirestoreException.Code.CANCELLED );
                        }

                        //double newPopulation = snapshot.getDouble("population") + 1;
                        transaction.update(paseadorRef, "saldo", saldoPaseador);
                        transaction.update(duenoRef, "saldo", saldoDueno);
                        // Success
                        return null;
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        FirebaseUtils.checkPaseo(uidPaseo, InfoPaseoActivity.this, perro);
                        Log.d("", "Transaction success!");
                        Toast.makeText(getApplicationContext(), "Transacción exitosa", Toast.LENGTH_LONG ).show();
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("", "Transaction failure.", e);
                                Toast.makeText(getApplicationContext(), "Insuficiente Saldo", Toast.LENGTH_LONG ).show();
                            }
                        });


            }
        });
        btn_rechazar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                //TODO deberia quitar el paseo de mis paseos
            }
        });
    }

    private void updateUI() {
        if(paseo.getMyImage() == null){
            paseo.setMyImage(FirebaseUtils.descargarFotoImageView( paseo.getUriPerrito(),iv_dog));
        } else {
            iv_dog.setImageURI(Uri.fromFile(paseo.getMyImage()));
        }
        tv_nombre.setText(perro.getNombre());
        tv_raza.setText(perro.getRaza());
        tv_tamano.setText(perro.getTamano());
        tv_edad.setText(Utils.getAge(perro.getFechaNacimiento())+" Meses");
        tv_genero.setText(perro.getSexo());
        tv_observaciones.setText(perro.getObservaciones());
        tv_distancia.setText(String.valueOf(paseo.getDist()) + " km");
        tv_duracion.setText(String.valueOf(paseo.getDuracionMinutos()) + " min");
        tv_costo.setText(String.valueOf(paseo.getCosto())+" petCoins");
    }
}
