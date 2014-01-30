package com.orestis.tudelftlogger.sensors;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;

/**
 * Simple yet elegant.
 * 
 * @author Orestis
 *
 */
public class LightSensor extends Activity{
	  private SensorManager mSensorManager;
	  private Sensor mLight;

	  @Override
	  public final void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    // Get an instance of the sensor service, and use that to get an instance of
	    // a particular sensor.
	    mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	    mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

	  }
	  
	  final float getLight(SensorEvent event) {
		  return event.values[0];
	  }
	}