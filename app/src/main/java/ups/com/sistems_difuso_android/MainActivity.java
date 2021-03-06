package ups.com.sistems_difuso_android;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import net.sourceforge.jFuzzyLogic.FIS;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class MainActivity extends AppCompatActivity {
    private android.widget.TextView txtGrados;
    private android.widget.TextView txtVelocidad;
    private android.widget.TextView txtDireccionPedida;
    private android.widget.TextView txtPuntuacion;
    private android.widget.TextView txtGameOver;
    private android.widget.TextView txtTimer;
    private FIS _FIS;

    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private Sensor sensorMagneticField;
    private String direccionPedida;
    private int puntos;
    private boolean isGameOver;
    private int direccionApuntadaGrados;
    private String direccionApuntadaTexto;

    private float[] floatGravity = new float[3];
    private float[] floatGeoMagnetic = new float[3];

    private float[] floatOrientation = new float[3];
    private float[] floatRotationMatrix = new float[9];

    private int seconds;
    private long timeLeftInMilliseconds = 61000;
    private CountDownTimer conCountDownTimer;
    RequestQueue MyRequestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MyRequestQueue = Volley.newRequestQueue(this);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        txtGrados = this.findViewById(R.id.txtGrados);
        txtVelocidad = this.findViewById(R.id.valorDefusificado);
        txtDireccionPedida = this.findViewById(R.id.txtDireccionPedida);
        txtPuntuacion = this.findViewById(R.id.txtPuntos);
        txtTimer = this.findViewById(R.id.txtTimer);
        txtGameOver = this.findViewById(R.id.txtGameOver);

        direccionPedida = getRandomDirection();
        txtDireccionPedida.setText("Apunta al " + direccionPedida);
        puntos = 0;
        txtPuntuacion.setText("Puntuaci??n: " + puntos);
        isGameOver = false;
        txtGameOver.setText("");

        startTimer();

        try {
            java.io.InputStream flujo = getAssets().open("ControladorDifuso.fcl");

            _FIS = FIS.load(flujo, true);

        } catch(Exception e){
            Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        SensorEventListener sensorEventListenerAccelrometer = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                floatGravity = event.values;

                SensorManager.getRotationMatrix(floatRotationMatrix, null, floatGravity, floatGeoMagnetic);
                SensorManager.getOrientation(floatRotationMatrix, floatOrientation);

                float grados = (float)(-floatOrientation[0]*180/3.14159);
                int gradosEnteros = (int)grados + 180;
                direccionApuntadaGrados = gradosEnteros;
                txtGrados.setText(gradosEnteros + "??");

                _FIS.setVariable("grados", gradosEnteros);
                _FIS.evaluate();
                int res = (int)_FIS.getFunctionBlock(null).getVariable("direccion").getLatestDefuzzifiedValue();
                txtVelocidad.setText("La direccion es: " + res);

                String direccionApuntada = "";
                if (res == 124) {
                    direccionApuntada = "Sur";
                }
                if (res == 67) {
                    direccionApuntada = "Sureste";
                }
                if (res == 112) {
                    direccionApuntada = "Este";
                }
                if (res == 157) {
                    direccionApuntada = "Noreste";
                }
                if (res == 202) {
                    direccionApuntada = "Norte";
                }
                if (res == 247) {
                    direccionApuntada = "Noroeste";
                }
                if (res == 292) {
                    direccionApuntada = "Oeste";
                }
                if (res == 233) {
                    direccionApuntada = "Suroeste";
                }
                direccionApuntadaTexto = direccionApuntada;

                if (direccionApuntada == direccionPedida) {
                    puntos += 1;
                    System.out.println("Puntos: " + puntos);
                    direccionPedida = getRandomDirection();
                    txtDireccionPedida.setText("Apunta al " + direccionPedida);
                    txtPuntuacion.setText("Puntuaci??n: " + puntos);
                }

                if (isGameOver == true) {
                    txtGrados.setText("");
                    txtVelocidad.setText("");
                    txtPuntuacion.setText("");
                    txtTimer.setText("");
                    txtDireccionPedida.setText("");
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        sensorManager.registerListener(sensorEventListenerAccelrometer, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        SensorEventListener sensorEventListenerMagneticField = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                floatGeoMagnetic = event.values;
                SensorManager.getRotationMatrix(floatRotationMatrix, null, floatGravity, floatGeoMagnetic);
                SensorManager.getOrientation(floatRotationMatrix, floatOrientation);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        sensorManager.registerListener(sensorEventListenerMagneticField, sensorMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public String getRandomDirection() {
        String[] direcciones = {"Sur", "Sureste", "Este", "Noreste", "Norte", "Noroeste", "Oeste", "Suroeste"};
        int indiceRandom = new Random().nextInt(direcciones.length);
        return direcciones[indiceRandom];
    }

    public void startTimer(){
        conCountDownTimer = new CountDownTimer(timeLeftInMilliseconds, 1000) {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onTick(long l) {
                timeLeftInMilliseconds = l;
                updateTimer();


                String url = "http://192.168.1.4:3000/direccion";
                StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //This code is executed if the server responds, whether or not the response contains data.
                        //The String 'response' contains the server's response.
                    }
                }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }) {
                    protected Map<String, String> getParams() {
                        Map<String, String> MyData = new HashMap<String, String>();
                        MyData.put("grados", Integer.toString(direccionApuntadaGrados));
                        MyData.put("direccion", direccionApuntadaTexto);
                        return MyData;
                    }
                };
                MyRequestQueue.add(MyStringRequest);
            }


            @Override
            public void onFinish() {
                System.out.println("El timer termino!!");
                boolean isOver = isGameOver;
                if(isOver == false) {
                    isGameOver = true;
                    txtGameOver.setText("Tu puntuaci??n fu??: " + puntos);
                    timeLeftInMilliseconds = 16000;
                    startTimer();
                }
                else {
                    isGameOver = false;
                    txtGameOver.setText("");
                    timeLeftInMilliseconds = 61000;
                    direccionPedida = getRandomDirection();
                    txtDireccionPedida.setText("Apunta al " + direccionPedida);
                    puntos = 0;
                    txtPuntuacion.setText("Puntuaci??n: " + puntos);
                    startTimer();
                }
            }

        }.start();
        //timerRunning=true;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void updateTimer(){
        seconds = (int) timeLeftInMilliseconds % 60000 / 1000;
        String timeLeftText;
        timeLeftText = "";
        timeLeftText += seconds;
        if(isGameOver == false) {
            txtTimer.setText(timeLeftText);
        }
        //timerRunning=false;
    }

}