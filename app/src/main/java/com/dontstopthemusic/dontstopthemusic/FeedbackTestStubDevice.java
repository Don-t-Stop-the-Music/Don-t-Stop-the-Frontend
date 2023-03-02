package com.dontstopthemusic.dontstopthemusic;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

/**
 * A pretend device for testing purposes.
 */
public class FeedbackTestStubDevice extends StubDevice
{
	/* Hiss values */
	private final boolean[] hiss = { false, false };

	/* Feedback frequency values */
	private final int[][] feedback = { {}, {} };


	/**
	 * @param channel Either 0 or 1.
	 * @param newHiss Truth value for hiss.
	 */
	public void setHiss ( int channel, boolean newHiss )
	{
		hiss [ channel ] = newHiss;
	}

	/**
	 * @param channel Either 0 or 1.
	 * @param newFeedback A new array of feedback frequencies.
	 */
	public void setFeedback ( int channel, int[] newFeedback )
	{
		feedback [ channel ] = newFeedback;
	}

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
			freqData1.put ( 0 );
			freqData1.put ( 0 );
		}

		/* Set hiss */
		JSONArray hissData = new JSONArray ();
		stubData.put ( "hiss", hissData );
		hissData.put ( hiss [ 0 ] );
		hissData.put ( hiss [ 1 ] );

		/* Set feedback */
		JSONArray feedbackData = new JSONArray ();
		JSONArray feedbackChannel1Data = new JSONArray ();
		JSONArray feedbackChannel2Data = new JSONArray ();
		for ( int f : feedback [ 0 ] ) feedbackChannel1Data.put ( f );
		for ( int f : feedback [ 1 ] ) feedbackChannel2Data.put ( f );
		feedbackData.put ( feedbackChannel1Data );
		feedbackData.put ( feedbackChannel2Data );
		stubData.put ( "feedback", feedbackData );

		/* Return the object */
		return stubData;
	}

}
