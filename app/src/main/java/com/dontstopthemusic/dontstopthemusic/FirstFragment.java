package com.dontstopthemusic.dontstopthemusic;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.dontstopthemusic.dontstopthemusic.databinding.FragmentFirstBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.dontstopthemusic.dontstopthemusic.MainActivity;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;


    enum FeedbackStates{
        ZERO_Init,
        ONE_PlugMonitorOut,
        TWO_HitPFL,
        THREE_CheckFreqRange,
        FOUR_TurnDownGain,
        FIVE_TurnDownChannelFader,
        SIX_TurnDownAux,
        SEVEN_UnhitPFL,
        EIGHT_TurnDownMF,
        ERROR
    }

    private FeedbackStates current_state=FeedbackStates.ZERO_Init;
    private int current_channel=0;

    //lots of placeholder variables for testing
    JSONArray TEST_monitorFeedback;
    JSONArray TEST_stereoFeedback;
    String variableValue="soundboard";



    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {


        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DialogInterface.OnClickListener dialogClickListener=new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch(i){
                    case DialogInterface.BUTTON_POSITIVE:{
                        break;
                    }
                    case DialogInterface.BUTTON_NEGATIVE:{
                        NavHostFragment.findNavController(FirstFragment.this)
                                .navigate(R.id.action_FirstFragment_to_ThirdFragment);
                        break;
                    }
                }
            }
        };
        binding.buttonfirst.setOnClickListener(new View.OnClickListener() {
            JSONObject localJson=MainActivity.getUpdatedJson();
            AlertDialog.Builder builder=null;

            @Override
            public void onClick(View view) {
                TextView tv1= (TextView) view.getRootView().findViewById(R.id.textview_first);
                Button buttonFirst= (Button) view.getRootView().findViewById(R.id.buttonfirst);
                ImageView iv1= (ImageView) view.getRootView().findViewById(R.id.imageview_first);

                //get update-st copy of json
                localJson=MainActivity.getUpdatedJson();
                try {
                    TEST_monitorFeedback=localJson.getJSONArray("feedback").getJSONArray(1);
                    TEST_stereoFeedback=localJson.getJSONArray("feedback").getJSONArray(0);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                switch (current_state){
                    case ZERO_Init: {
                        current_state=FeedbackStates.ONE_PlugMonitorOut;
                        tv1.setText("Plug into monitor out");//"Plug into monitor out"
                        buttonFirst.setText("Next");
                        iv1.setImageResource(R.drawable.monitor_out);
                        break;
                    }
                    case ONE_PlugMonitorOut:{
                        current_state=FeedbackStates.TWO_HitPFL;
                        if (current_channel==0){
                            current_channel=1;
                        }
                        tv1.setText("Hit PFL on channel "+current_channel);
                        variableValue="pfl_"+current_channel;
                        iv1.setImageResource(getResources().getIdentifier(variableValue,"drawable","com.dontstopthemusic.dontstopthemusic"));
                        break;
                    }
                    case TWO_HitPFL:{
                        if (!(TEST_monitorFeedback.length()==0)){
                            //feedback exists in this channel
                            current_state=FeedbackStates.THREE_CheckFreqRange;
                            //stuff happens to determine the problem area
                            int average=0; //brute ugly average
                            for (int i=0;i<TEST_monitorFeedback.length();i++){
                                try {
                                    average=average+TEST_monitorFeedback.getInt(i);
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            average=average/TEST_monitorFeedback.length();
                            //tv1.setText("average is "+average);
                            String problem_area="ERROR";
                            if (average>8000){
                                problem_area="HIGH";
                                variableValue="eq_high_"+current_channel;
                            }
                            else if (average>1500){
                                problem_area="MEDIUM";
                                variableValue="eq_mid_"+current_channel;
                            }
                            else{
                                problem_area="LOW";
                                variableValue="eq_low_"+current_channel;
                            }
                            iv1.setImageResource(getResources().getIdentifier(variableValue,"drawable","com.dontstopthemusic.dontstopthemusic"));
                            tv1.setText("We have found a potential problem channel. The feedback is centered around the "+problem_area+" range ("+average+"Hz). Try turning down the "+problem_area+" knob.");
                        }
                        else{
                            //this is not the problem channel
                            current_state=FeedbackStates.SEVEN_UnhitPFL;
                            tv1.setText("This does not seem to be the problem channel. Hit the PFL button for channel "+current_channel+" again to deselect the channel.");
                            variableValue="pfl_"+current_channel;
                            iv1.setImageResource(getResources().getIdentifier(variableValue,"drawable","com.dontstopthemusic.dontstopthemusic"));
                            current_channel+=1;
                        }
                        break;
                    }
                    case THREE_CheckFreqRange:{
                        if (!(TEST_monitorFeedback.length()==0)){
                            //that did not fix it
                            current_state=FeedbackStates.FOUR_TurnDownGain;
                            variableValue="gain_"+current_channel;
                            iv1.setImageResource(getResources().getIdentifier(variableValue,"drawable","com.dontstopthemusic.dontstopthemusic"));
                            tv1.setText("That did not work. Try turning down gain for channel "+ current_channel+" instead.");
                        }
                        else{
                           //that fixed it
                            current_state=FeedbackStates.SEVEN_UnhitPFL;
                            tv1.setText("That seemed to fix this channel. Hit the PFL button for channel "+current_channel+" again to deselect the channel.");
                            variableValue="pfl_"+current_channel;
                            iv1.setImageResource(getResources().getIdentifier(variableValue,"drawable","com.dontstopthemusic.dontstopthemusic"));
                            current_channel+=1;

                        }
                        break;
                    }
                    case FOUR_TurnDownGain:{
                        if (!(TEST_monitorFeedback.length()==0)){
                            //that did not fix it
                            current_state=FeedbackStates.FIVE_TurnDownChannelFader;
                            tv1.setText("That did not work. Try pushing down fader for channel "+ current_channel+" instead.");
                            variableValue="fader_"+current_channel;
                            iv1.setImageResource(getResources().getIdentifier(variableValue,"drawable","com.dontstopthemusic.dontstopthemusic"));
                        }
                        else{
                            //that fixed it
                            current_state=FeedbackStates.SEVEN_UnhitPFL;
                            tv1.setText("That seemed to fix this channel. Hit the PFL button for channel "+current_channel+" again to deselect the channel.");
                            variableValue="pfl_"+current_channel;
                            iv1.setImageResource(getResources().getIdentifier(variableValue,"drawable","com.dontstopthemusic.dontstopthemusic"));
                            current_channel+=1;
                        }
                        break;
                    }
                    case FIVE_TurnDownChannelFader:{
                        if (!(TEST_monitorFeedback.length()==0)){
                            //that did not fix it
                            current_state=FeedbackStates.FOUR_TurnDownGain;
                            variableValue="aux_"+current_channel;
                            iv1.setImageResource(getResources().getIdentifier(variableValue,"drawable","com.dontstopthemusic.dontstopthemusic"));
                            tv1.setText("That did not work. Try turning down aux for channel "+ current_channel+" instead.");
                        }
                        else{
                            //that fixed it
                            current_state=FeedbackStates.SEVEN_UnhitPFL;
                            tv1.setText("That seemed to fix this channel. Hit the PFL button for channel "+current_channel+" again to deselect the channel.");
                            variableValue="pfl_"+current_channel;
                            iv1.setImageResource(getResources().getIdentifier(variableValue,"drawable","com.dontstopthemusic.dontstopthemusic"));
                            current_channel+=1;
                        }
                    }
                    case SIX_TurnDownAux:{
                        if (!(TEST_monitorFeedback.length()==0)){
                            //that did not fix it
                            current_state=FeedbackStates.SEVEN_UnhitPFL;
                            tv1.setText("That did not work, but we've run out of things to try. Hit the PFL button for channel "+current_channel+" again to deselect this channel.");

                        }
                        else{
                            //that fixed it
                            current_state=FeedbackStates.SEVEN_UnhitPFL;
                            tv1.setText("That seemed to fix this channel. Hit the PFL button for channel "+current_channel+" again to deselect the channel.");
                        }
                        variableValue="pfl_"+current_channel;
                        iv1.setImageResource(getResources().getIdentifier(variableValue,"drawable","com.dontstopthemusic.dontstopthemusic"));
                        current_channel+=1;
                        break;
                    }
                    case SEVEN_UnhitPFL: {
                        if (current_channel <= 8) {
                            if (TEST_stereoFeedback.length() == 0) {
                                //early exit option
                                if (!(builder==null)){
                                    break;
                                }
                                builder = new AlertDialog.Builder(view.getRootView().getContext());
                                builder.setMessage("We have detected that there is no more feedback in stereo. Do you still want to continue debug feedback?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).setCancelable(false).show();
                            }
                            //test other channels
                            current_state = FeedbackStates.ONE_PlugMonitorOut;
                            tv1.setText("Press next to continue debugging.");
                            variableValue="soundboard";
                            iv1.setImageResource(getResources().getIdentifier(variableValue,"drawable","com.dontstopthemusic.dontstopthemusic"));
                            break;
                        } else {
                            //already tested all channels
                            if (TEST_stereoFeedback.length() == 0) {
                                //already tested all channels, no more feedback
                                current_state = FeedbackStates.EIGHT_TurnDownMF;
                                tv1.setText("We have went through all the options, and no more stereo feedback is detected. If you still hear feedback, you can try turning the Master Fader down at your own judgement.");
                            } else {
                                //already tested all channels, feedback still exist
                                //we can do no more
                                current_state = FeedbackStates.EIGHT_TurnDownMF;
                                tv1.setText("We have went through all the options, but there is still stereo feedback. Do you still hear feedback? If yes, try turning the Master Fader down at your own judgement.");
                            }
                            variableValue="master_fader";
                            iv1.setImageResource(getResources().getIdentifier(variableValue,"drawable","com.dontstopthemusic.dontstopthemusic"));
                            buttonFirst.setText("End debug process");
                            break;
                        }
                    }
                    case EIGHT_TurnDownMF:{
                        NavHostFragment.findNavController(FirstFragment.this)
                                .navigate(R.id.action_FirstFragment_to_ThirdFragment);
                        break;
                    }
                    default:
                        tv1.setText("ERROR default");
                        break;
                }
            }
        });


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}