package ups.com.sistems_difuso_android;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import net.sourceforge.jFuzzyLogic.FIS;

import java.util.Random;


public class MainActivity extends AppCompatActivity {
    private android.widget.TextView txtSeekBar;
    private android.widget.TextView txtVelocidad;
    private FIS _FIS;

    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private Sensor sensorMagneticField;
    private String direccionPedida;
    private int puntos;

    private float[] floatGravity = new float[3];
    private float[] floatGeoMagnetic = new float[3];

    private float[] floatOrientation = new float[3];
    private float[] floatRotationMatrix = new float[9];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        txtSeekBar = this.findViewById(R.id.txtSeekBar);
        txtVelocidad = this.findViewById(R.id.valorDefusificado);
        direccionPedida = getRandomDirection();
        System.out.println("Direccion pedida: " + direccionPedida);
        puntos = 0;

        try {
            java.io.InputStream flujo = getAssets().open("ControladorDifuso.fcl");

            _FIS = FIS.load(flujo, true);

        }catch(Exception e){
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
                txtSeekBar.setText(gradosEnteros + "°");

                _FIS.setVariable("grados", gradosEnteros);
                _FIS.evaluate();
                int res = (int)_FIS.getFunctionBlock(null).getVariable("direccion").getLatestDefuzzifiedValue();
                txtVelocidad.setText("La direccion es: " + res);

                String direccionApuntada = "";
                if (res == 114) {
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
                if (res == 243) {
                    direccionApuntada = "Suroeste";
                }

                if (direccionApuntada == direccionPedida) {
                    puntos += 1;
                    System.out.println("Puntos: " + puntos);
                    direccionPedida = getRandomDirection();
                    System.out.println("Direccion pedida: " + direccionPedida);
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

}