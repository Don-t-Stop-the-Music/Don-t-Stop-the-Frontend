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

import com.dontstopthemusic.dontstopthemusic.databinding.FragmentSecondBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;

    enum SilenceStates{
        ZERO_init,
        ONE_MF,
        ONE_POINT_FIVE,
        TWO_slider,
        THREE_ST,
        FOUR_whichchannel,
        FIVE_plugPFL,
        SIX_gain,
        SEVEN_EQ,
        EIGHT_mic,
        NINE_pluggedin,
        TEN_dontknow,
        ELEVEN_giveup,
        TWELVE_end
    }
    private SilenceStates current_state= SilenceStates.ZERO_init;
    private int current_channel=-1;
    JSONArray TEST_stereoFreq;
    Double TEST_maxValueFreq;
    String variableValue="soundboard";

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DialogInterface.OnClickListener dialogClickListener=new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch(i){
                    case DialogInterface.BUTTON_POSITIVE:{
                        current_state=SilenceStates.FIVE_plugPFL;
                        current_channel=0; //"that"
                        break;
                    }
                    case DialogInterface.BUTTON_NEGATIVE:{
                        current_state=SilenceStates.TEN_dontknow;
                        current_channel=1;
                        break;
                    }
                }
            }
        };
        binding.buttonsecond.setOnClickListener(new View.OnClickListener() {

            JSONObject localJson=MainActivity.getUpdatedJson();
            AlertDialog.Builder builder=null;

            int checkSilence(){
                try {
                    TEST_stereoFreq=localJson.getJSONArray("frequency").getJSONArray(0);
                    TEST_maxValueFreq=-1.;
                    for (int i=0;i<TEST_stereoFreq.length();i++){
                        if ((TEST_stereoFreq.getDouble(i))>TEST_maxValueFreq){
                            TEST_maxValueFreq=TEST_stereoFreq.getDouble(i);
                        }
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                if ((TEST_maxValueFreq<1)&&(TEST_maxValueFreq>=0)){
                    return 1;
                }
                else if (TEST_maxValueFreq>=1){
                    return 0;
                }
                else{
                    return -1;
                }
            }
            @Override
            public void onClick(View view) {
                TextView tv2= (TextView) view.getRootView().findViewById(R.id.textview_second);
                Button buttonSecond= (Button) view.getRootView().findViewById(R.id.buttonsecond);
                ImageView iv2= (ImageView) view.getRootView().findViewById(R.id.imageview_second);

                //get update-st copy of json
                localJson=MainActivity.getUpdatedJson();
                int no_sound=checkSilence();
                String done="(Note: we are detecting sound from the desk.) ";

                switch (current_state){
                    case ZERO_init:{
                        buttonSecond.setText("Next");
                        if (no_sound==1){
                            current_state=SilenceStates.ONE_MF;
                            tv2.setText("We cannot detect any sound (max value is "+TEST_maxValueFreq+"). Is the Master Fader up? (Note: you can end this debug process whenever you can hear sound again.)");
                            variableValue="master_fader";
                        }
                        else if (no_sound==(-1)){
                            throw new RuntimeException("No sound test not working ERROR");
                        }
                        else{
                            current_state=SilenceStates.ONE_POINT_FIVE;
                            tv2.setText("Check that the fader is up on on all channels. (Note: you can end this debug process whenever you can hear sound again.)");
                            variableValue="fader_all";
                        }
                        iv2.setImageResource(getResources().getIdentifier(variableValue,"drawable","com.dontstopthemusic.dontstopthemusic"));
                        break;
                    }
                    case ONE_MF:{
                        current_state=SilenceStates.ONE_POINT_FIVE;
                        if (no_sound==0){
                            tv2.setText(done+"Check that the fader is up on all channels.");
                        }
                        else{
                            tv2.setText("Check that the fader is up on all channels.");
                        }
                        variableValue="fader_all";
                        iv2.setImageResource(getResources().getIdentifier(variableValue,"drawable","com.dontstopthemusic.dontstopthemusic"));
                        break;
                    }
                    case ONE_POINT_FIVE:{
                        current_state=SilenceStates.TWO_slider;
                        if (no_sound==0){
                            tv2.setText(done+"Check that the on buttons are on on all channels.");
                        }
                        else{
                            tv2.setText("Check that the on buttons are on on all channels.");
                        }
                        variableValue="on";
                        iv2.setImageResource(getResources().getIdentifier(variableValue,"drawable","com.dontstopthemusic.dontstopthemusic"));
                        break;
                    }
                    case TWO_slider:{
                        current_state=SilenceStates.THREE_ST;
                        if (no_sound==0){
                            tv2.setText(done+"Check that the ST button is on.");
                        }
                        else{
                            tv2.setText("Check that the ST button is on.");
                        }
                        variableValue="st_all";
                        iv2.setImageResource(getResources().getIdentifier(variableValue,"drawable","com.dontstopthemusic.dontstopthemusic"));
                        break;
                    }
                    case THREE_ST:{
                        if (!(builder==null)){

                        }
                        else{
                            builder = new AlertDialog.Builder(view.getRootView().getContext());
                            builder.setMessage("Do you know which channel might be silent?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).setCancelable(false).show();
                            tv2.setText("Press next to continue");
                            variableValue="soundboard";
                            iv2.setImageResource(getResources().getIdentifier(variableValue,"drawable","com.dontstopthemusic.dontstopthemusic"));
                            //current_state=SilenceStates.FOUR_whichchannel;
                        }
                        break;

                    }
                    case FOUR_whichchannel:{
                        tv2.setText("ERRORRRR 4");
                        break;
                    }
                    case FIVE_plugPFL:{
                        current_state=SilenceStates.SIX_gain;
                        String text="channel "+current_channel;
                        variableValue="monitor_out";
                        if (current_channel==0){
                            text="the problem channel";
                        }
                        else if (current_channel==1){
                            variableValue="pfl_monitor_1";
                        }
                        String prep="Plug into monitor out.";
                        if (current_channel>1){
                            prep="Unhit PFL for channel "+(current_channel-1)+".";
                            if (current_channel==7){
                                prep="Unhit PFL for channel "+5+".";
                            }
                            variableValue="pfl_"+current_channel;
                        }
                        if (no_sound==0){
                            prep=done+prep;
                        }
                        tv2.setText(prep+"Hit PFL for "+text+".");
                        iv2.setImageResource(getResources().getIdentifier(variableValue,"drawable","com.dontstopthemusic.dontstopthemusic"));
                        break;
                    }
                    case SIX_gain:{
                        current_state=SilenceStates.SEVEN_EQ;
                        String text="channel "+current_channel;
                        variableValue="gain_"+current_channel;
                        if (current_channel==0){
                            text="the problem channel";
                            variableValue="soundboard";
                        }
                        if (no_sound==0){
                            tv2.setText(done+"Try turning up the gain knob for "+text+".");
                        }
                        else{
                            tv2.setText("Try turning up the gain knob for "+text+". ");
                        }
                        iv2.setImageResource(getResources().getIdentifier(variableValue,"drawable","com.dontstopthemusic.dontstopthemusic"));
                        break;
                    }
                    case SEVEN_EQ:{
                        current_state=SilenceStates.EIGHT_mic;
                        if (current_channel>1){
                            current_state=SilenceStates.NINE_pluggedin;
                        }
                        String text="channel "+current_channel;
                        variableValue="eq_"+current_channel;
                        if (current_channel==0){
                            text="the problem channel";
                            variableValue="soundboard";
                        }
                        tv2.setText("Try turning all three EQ knobs to the middle position for "+text+".  If it didn't fix it, turn it back down to its previous settings.");
                        iv2.setImageResource(getResources().getIdentifier(variableValue,"drawable","com.dontstopthemusic.dontstopthemusic"));
                        break;
                    }
                    case EIGHT_mic:{
                        current_state=SilenceStates.NINE_pluggedin;
                        if ((current_channel==0)||(current_channel==1)){
                            tv2.setText("Are you using a condenser mic? If yes, check that phantom power is on. (If you are not sure don't turn it on!)");
                        }
                        variableValue="phantom";
                        iv2.setImageResource(getResources().getIdentifier(variableValue,"drawable","com.dontstopthemusic.dontstopthemusic"));
                        break;
                    }
                    case NINE_pluggedin:{
                        String text="channel "+current_channel;
                        if (current_channel==0){
                            text="the problem channel";
                        }
                        current_state=SilenceStates.ELEVEN_giveup;
                        if (no_sound==0){
                            tv2.setText(done+"Check if things plugged in correctly for "+text+"?");
                        }
                        else{
                            tv2.setText("Check if things plugged in correctly for "+text+"?");
                        }
                        variableValue="outputs";
                        iv2.setImageResource(getResources().getIdentifier(variableValue,"drawable","com.dontstopthemusic.dontstopthemusic"));
                        break;
                    }
                    case TEN_dontknow:{
                        current_state=SilenceStates.FIVE_plugPFL;
                        tv2.setText("The following actions might mess up your current settings. Press next if you want to proceed and check through all the channels.");
                        variableValue="soundboard";
                        iv2.setImageResource(getResources().getIdentifier(variableValue,"drawable","com.dontstopthemusic.dontstopthemusic"));
                        break;
                    }
                    case ELEVEN_giveup:{
                        if (current_channel==0||current_channel==7){
                            buttonSecond.setText("End debug silence");
                            tv2.setText("We don't think it is an issue of the sound board. Try checking other connections instead.");
                            current_state=SilenceStates.TWELVE_end;
                        }
                        else{
                            tv2.setText("Moving on to next channel...");
                            current_channel++;
                            if (current_channel==6){
                                current_channel++;
                            }
                            current_state=SilenceStates.FIVE_plugPFL;
                        }
                        variableValue="soundboard";
                        iv2.setImageResource(getResources().getIdentifier(variableValue,"drawable","com.dontstopthemusic.dontstopthemusic"));
                        break;
                    }
                    case TWELVE_end:{
                        NavHostFragment.findNavController(SecondFragment.this)
                                .navigate(R.id.action_SecondFragment_to_ThirdFragment);
                        break;
                    }
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