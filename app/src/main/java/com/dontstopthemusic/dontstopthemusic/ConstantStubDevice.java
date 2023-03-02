package com.dontstopthemusic.dontstopthemusic;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A pretend device for testing purposes.
 */
public class ConstantStubDevice extends StubDevice
{

	/**
	 * @return A new stub JSONObject
	 */
	@Override
	public JSONObject generateJSON () throws JSONException
	{
		/* Create the stub data */
		JSONObject stubData = new JSONObject ();

		/* Set the min and max frequency */
		final int minFrequency = 0, maxFrequency = 22050;
		stubData.put ( "minFrequency", minFrequency );
		stubData.put ( "maxFrequency", maxFrequency );

		/* Create the frequency data */
		JSONArray freqData = new JSONArray ();
		stubData.put ( "frequency", freqData );
		JSONArray freqData1 = new JSONArray ();
		JSONArray freqData2 = new JSONArray ();
		freqData.put ( freqData1 );
		freqData.put ( freqData2 );
		for ( int i = 0; i < 160; ++i )
		{
			freqData1.put ( Math.pow ( Math.sin ( i / Math.PI ), 2 ) );
			freqData2.put ( Math.pow ( Math.cos ( i / ( 2 * Math.PI ) ), 2 ) );
		}

		/* Set hiss */
		JSONArray hissData = new JSONArray ();
		stubData.put ( "hiss", hissData );
		hissData.put ( false );
		hissData.put ( false );

		/* Set feedback */
		JSONArray feedbackData = new JSONArray ();
		feedbackData.put ( new JSONArray () );
		feedbackData.put ( new JSONArray () );
		stubData.put ( "feedback", feedbackData );

		/* Return the object */
		return stubData;
	}

}
