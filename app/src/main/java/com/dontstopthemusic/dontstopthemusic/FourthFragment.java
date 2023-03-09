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

import com.dontstopthemusic.dontstopthemusic.databinding.FragmentFourthBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FourthFragment extends Fragment {

    private FragmentFourthBinding binding;
    private HissStates currentState = HissStates.ZERO_Init;
    private int channel = 1;
    boolean[] TEST_hiss;
    String variableValue="soundboard";
    boolean completed = false;
    boolean premature = false;
    boolean asked = false;


    enum HissStates {
        ZERO_Init,
        ONE_HitPFL,
        TWO_UnplugReplug,
        THREE_TurnUpGain,
        FOUR_TurnDownFader,
        FIVE_UnhitPFL,
        SIX_Exit,
        ERROR
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFourthBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DialogInterface.OnClickListener dialogClickListener=new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == DialogInterface.BUTTON_POSITIVE) {
                    asked = true;
                }
                else if (i == DialogInterface.BUTTON_NEGATIVE) {
                    currentState = HissStates.FOUR_TurnDownFader;
                    premature = true;
                } // go to unhit PFL instruction if user does not want to continue
            }
        };

        binding.buttonFourth.setOnClickListener(new View.OnClickListener() {
            JSONObject localJSON = MainActivity.getUpdatedJson();
            AlertDialog.Builder builder=null;
            @Override
            public void onClick(View view) {
                TextView textView = (TextView) view.getRootView().findViewById(R.id.textview_fourth);
                Button buttonFourth = (Button) view.getRootView().findViewById(R.id.button_fourth);
                ImageView imageView = (ImageView) view.getRootView().findViewById(R.id.imageview_fourth);

                localJSON = MainActivity.getUpdatedJson(); // get updated values

                try {
                    JSONArray hissArray = localJSON.getJSONArray("hiss");
                    TEST_hiss = new boolean[]{hissArray.getBoolean(0), hissArray.getBoolean(1)};
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                switch (currentState) {
                    case ZERO_Init: {
                        currentState = HissStates.ONE_HitPFL;
                        variableValue = String.format("pfl_%s", channel);

                        textView.setText(String.format("Hit PFL on channel %s", channel));
                        imageView.setImageResource(getResources().getIdentifier(variableValue,
                                "drawable", "com.dontstopthemusic.dontstopthemusic"));
                        buttonFourth.setText("Next");
                        break;
                    }
                    case ONE_HitPFL: {
                        if (!TEST_hiss[1]) { // if no hiss detected
                            currentState = HissStates.TWO_UnplugReplug;
                            variableValue = "outputs";
                            textView.setText(String.format("Unplug and replug channel %s input", channel));

                            if (!asked) {
                                if (!(builder == null)) {
                                    break;
                                }

                                builder = new AlertDialog.Builder(view.getRootView().getContext());
                                builder.setMessage("No hiss detected. Would you like to continue debugging?")
                                        .setNegativeButton("No", dialogClickListener).setPositiveButton("Yes", dialogClickListener)
                                        .setCancelable(false).show();
                            }
                        }
                        else if (!TEST_hiss[0]) { // if no hiss detected in monitor
                            currentState = HissStates.FOUR_TurnDownFader;
                            variableValue = "soundboard";
                            textView.setText("This is not the correct channel. Proceed to next instruction.");
                        }
                        else { // hiss detected in monitor and stereo
                            currentState = HissStates.TWO_UnplugReplug;
                            variableValue = "outputs";
                            textView.setText(String.format("Unplug and replug channel %s input", channel));
                        }

                        imageView.setImageResource(getResources().getIdentifier(variableValue,
                                "drawable", "com.dontstopthemusic.dontstopthemusic"));
                        buttonFourth.setText("Next");
                        break;
                    }
                    case TWO_UnplugReplug: {
                        currentState = HissStates.THREE_TurnUpGain;
                        variableValue = String.format("gain_%s", channel);

                        if (!TEST_hiss[1]) {
                            if (!asked) {
                                if (!(builder==null)){
                                    break;
                                }
                                builder = new AlertDialog.Builder(view.getRootView().getContext());
                                builder.setMessage("No hiss detected. Would you like to continue debugging?")
                                        .setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener)
                                        .setCancelable(false).show();
                            }
                        }

                        textView.setText(String.format("Turn up the gain for channel %s", channel));
                        imageView.setImageResource(getResources().getIdentifier(variableValue,
                                "drawable", "com.dontstopthemusic.dontstopthemusic"));
                        buttonFourth.setText("Next");
                        break;
                    }
                    case THREE_TurnUpGain: {
                        currentState = HissStates.FOUR_TurnDownFader;
                        variableValue = String.format("fader_%s", channel);

                        if (!TEST_hiss[1]) {
                            if (!asked) {
                                if (!(builder == null)) {
                                    break;
                                }

                                builder = new AlertDialog.Builder(view.getRootView().getContext());
                                builder.setMessage("No hiss detected. Would you like to continue debugging?")
                                        .setNegativeButton("No", dialogClickListener).setPositiveButton("Yes", dialogClickListener)
                                        .setCancelable(false).show();
                            }
                        }

                        if (channel >= 7) {
                            completed = true; // all channels have been checked
                        }

                        textView.setText(String.format("Turn down the fader for channel %s", channel));
                        imageView.setImageResource(getResources().getIdentifier(variableValue,
                                "drawable", "com.dontstopthemusic.dontstopthemusic"));
                        buttonFourth.setText("Next");
                        break;
                    }

                    case FOUR_TurnDownFader: {
                        int oldChannel = channel;

                        if (completed || premature) {
                            currentState = HissStates.FIVE_UnhitPFL;
                        } // early exit or all channels checked
                        else if (channel == 5) {
                            channel += 2;
                            currentState = HissStates.ZERO_Init;
                        } // pfl on channels 5 and 6 has the same effect
                        else {
                            channel += 1;
                            currentState = HissStates.ZERO_Init;
                        } // move on to next channel

                        variableValue = String.format("pfl_%s", oldChannel);

                        textView.setText(String.format("Hit the PFL button for channel %s to deselect " +
                                "this channel.", oldChannel));
                        imageView.setImageResource(getResources().getIdentifier(variableValue,
                                "drawable", "com.dontstopthemusic.dontstopthemusic"));
                        buttonFourth.setText("Next");
                        break;
                    }
                    case FIVE_UnhitPFL: {
                        currentState = HissStates.SIX_Exit;
                        if (TEST_hiss[1]) { // still detect hiss
                                textView.setText("Unfortunately, there are no other options to solve " +
                                        "the issue via the soundboard. Please check the DI box.");
                        }
                        else {
                            if (completed) {
                                textView.setText("We have gone through all of the options and no more hiss " +
                                        "is detected. If you still hear any hiss, check the DI box.");
                            }
                            else { // user cancelled debug process
                                textView.setText("Hiss was no longer detected during the debug process for " +
                                        "one of the channels. If you still hear any hiss, try again or " +
                                        "check the DI box.");
                            }
                        }
                        variableValue = "soundboard";
                        imageView.setImageResource(getResources().getIdentifier(variableValue,
                                "drawable", "com.dontstopthemusic.dontstopthemusic"));
                        buttonFourth.setText("End debug process");
                        break;
                    }
                    case SIX_Exit: {
                        NavHostFragment.findNavController(FourthFragment.this)
                                .navigate(R.id.action_FourthFragment_to_ThirdFragment);
                        break; // return to homepage
                    }
                    default:
                        textView.setText("ERROR default");
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