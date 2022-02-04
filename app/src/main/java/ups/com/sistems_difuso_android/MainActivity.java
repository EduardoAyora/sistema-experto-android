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


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private android.widget.TextView txtSeekBar;
    private android.widget.TextView txtVelocidad;
    private android.widget.TextView DegreeTV;
    private android.widget.SeekBar seekBar;
    private FIS _FIS;

    private SensorManager SensorManage;
    private float DegreeStart = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtSeekBar = this.findViewById(R.id.txtSeekBar);
        txtVelocidad = this.findViewById(R.id.valorDefusificado);

        seekBar = this.findViewById(R.id.seekBar);

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
                txtSeekBar.setText("Los grados son: "+seekBar.getProgress());
                _FIS.setVariable("grados", seekBar.getProgress());
                //_FIS.setVariable("grados", seekBar.getProgress());
                _FIS.evaluate();
                double res = _FIS.getFunctionBlock(null).getVariable("direccion").getLatestDefuzzifiedValue();
                txtVelocidad.setText("La direccion es: " + res);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        DegreeTV = (TextView) findViewById(R.id.DegreeTV);
        // initialize your android device sensor capabilities
        SensorManage = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // to stop the listener and save battery
        SensorManage.unregisterListener(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        // code for system's orientation sensor registered listeners
        SensorManage.registerListener(this, SensorManage.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        // get angle around the z-axis rotated
        float degree = Math.round(event.values[0]);
        DegreeTV.setText("Heading: " + Float.toString(degree) + " degrees");
        // rotation animation - reverse turn degree degrees
        DegreeStart = -degree;
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }
}