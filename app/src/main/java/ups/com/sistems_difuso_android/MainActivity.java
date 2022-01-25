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
import android.widget.Toast;

import net.sourceforge.jFuzzyLogic.FIS;


public class MainActivity extends AppCompatActivity {
    private android.widget.TextView txtSeekBar;
    private android.widget.TextView txtHumedad;
    private android.widget.TextView txtVelocidad;
    private android.widget.SeekBar seekBar;
    private android.widget.SeekBar seekBar2;
    private android.widget.Button btnCargarFCL;
    private FIS _FIS;

    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorEventListener sensorEventListener;
    int whip = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        if (sensor == null) {
            Toast.makeText(this, "El dispositivo no tiene aceletometro", Toast.LENGTH_SHORT).show();
            finish();
        }

        sensorEventListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent sensorEvent) {
                float x = sensorEvent.values[0];
                if(x<-5 && whip==0){
                    System.out.println("valor giro" + x);
                    whip++;
                    getWindow().getDecorView().setBackgroundColor(Color.BLUE);
                }else if(x>5 && whip==1){
                    whip++;
                    getWindow().getDecorView().setBackgroundColor(Color.RED);
                }
                if(whip==2){
                    whip=0;
                    System.out.println("sonido");
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
        start();

        txtSeekBar = this.findViewById(R.id.txtSeekBar);
        txtHumedad = this.findViewById(R.id.txtHumedad);
        txtVelocidad = this.findViewById(R.id.valorDefusificado);

        seekBar = this.findViewById(R.id.seekBar);
        seekBar2 = this.findViewById(R.id.seekBar2);
        btnCargarFCL = this.findViewById(R.id.btnCargar);

        try {
            java.io.InputStream flujo = getAssets().open("ControladorDifuso.fcl");

            _FIS = FIS.load(flujo, true);

        }catch(Exception e){
            Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                txtSeekBar.setText("La temperatura es: "+seekBar.getProgress());
                _FIS.setVariable("temperatura", seekBar.getProgress());
                _FIS.evaluate();
                double res = _FIS.getFunctionBlock(null).getVariable("velocidad").getLatestDefuzzifiedValue();
                txtVelocidad.setText("La velocidad es: "+res);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                txtHumedad.setText("La humedad es: "+seekBar2.getProgress());
                _FIS.setVariable("humedad", seekBar2.getProgress());
                _FIS.evaluate();
                double res = _FIS.getFunctionBlock(null).getVariable("velocidad").getLatestDefuzzifiedValue();
                txtVelocidad.setText("La velocidad es: "+res);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        btnCargarFCL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    //java.io.InputStream flujo = getAssets().open("ControladorDifuso.fcl");

                    //_FIS = FIS.load(flujo,true);
                    _FIS.setVariable("temperatura", seekBar.getProgress());
                    _FIS.evaluate();

                    double res = _FIS.getFunctionBlock(null).getVariable("velocidad").getLatestDefuzzifiedValue();

                    txtVelocidad.setText(""+res);

                    Toast.makeText(MainActivity.this, "Archivo FCL Cargado con éxito!!!", Toast.LENGTH_LONG).show();
                }catch(Exception e){
                    Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });


    }

    private void start(){
        sensorManager.registerListener(sensorEventListener,sensor,sensorManager.SENSOR_DELAY_NORMAL);
    }

    private void stop(){
        sensorManager.unregisterListener(sensorEventListener);
    }

    @Override
    protected void onPause() {
        stop();
        super.onPause();
    }

    @Override
    protected void onResume() {
        start();
        super.onResume();
    }

    //@Override
    //protected void onResume() {
    //    super.onResume();
    //    sensorManager.registerListener(gyroscopeEventListener, gyroscopeSensor, SensorManager.SENSOR_DELAY_FASTEST);
    //}

    //@Override
    //protected void onPause() {
    //    super.onPause();
    //    sensorManager.unregisterListener(gyroscopeEventListener);
    //}
}