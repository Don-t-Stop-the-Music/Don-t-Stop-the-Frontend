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
    private int channel = 0;
    boolean[] TEST_hiss;
    String variableValue="soundboard";


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
                if (i == DialogInterface.BUTTON_POSITIVE) {}
                else if (i == DialogInterface.BUTTON_NEGATIVE) {
                    NavHostFragment.findNavController(FourthFragment.this)
                            .navigate(R.id.action_FourthFragment_to_ThirdFragment);
                }
            }
        };

        binding.buttonFourth.setOnClickListener(new View.OnClickListener() {
            JSONObject localJSON = MainActivity.getUpdatedJson();
            @Override
            public void onClick(View view) {
                TextView textView = (TextView) view.getRootView().findViewById(R.id.textview_fourth);
                Button buttonFourth = (Button) view.getRootView().findViewById(R.id.button_fourth);
                ImageView imageView = (ImageView) view.getRootView().findViewById(R.id.imageview_fourth);

                localJSON = MainActivity.getUpdatedJson();

                try {
                    JSONArray hissArray = localJSON.getJSONArray("hiss");
                    TEST_hiss = new boolean[]{hissArray.getBoolean(0), hissArray.getBoolean(1)};
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                switch (currentState) {
                    case ZERO_Init: {
                        if (TEST_hiss[0]) {
                            channel = 0;
                        } else if (TEST_hiss[1]) {
                            channel = 1;
                        }

                        currentState = HissStates.ONE_HitPFL;
                        textView.setText(String.format("Hit PFL on channel: %s", channel));
                        variableValue = String.format("pfl_%s", channel);
                        imageView.setImageResource(getResources().getIdentifier(variableValue,
                                "drawable", "com.dontstopthemusic.dontstopthemusic"));
                        break;
                    }
                    case ONE_HitPFL: {
                        currentState = HissStates.TWO_UnplugReplug;
                        textView.setText(String.format("Unplug and replug channel %s input", channel));
                        variableValue = String.format("pfl_%s", channel); //TODO: replace
                        imageView.setImageResource(getResources().getIdentifier(variableValue,
                                "drawable", "com.dontstopthemusic.dontstopthemusic"));
                        break;
                    }
                    case TWO_UnplugReplug: {
                        currentState = HissStates.THREE_TurnUpGain;
                        textView.setText(String.format("Turn up the gain for channel %s", channel));
                        variableValue = String.format("gain_%s", channel);
                        imageView.setImageResource(getResources().getIdentifier(variableValue,
                                "drawable", "com.dontstopthemusic.dontstopthemusic"));
                        break;
                    }
                    case THREE_TurnUpGain: {
                        currentState = HissStates.FOUR_TurnDownFader;
                        textView.setText(String.format("Turn down the fader for channel %s", channel));
                        variableValue = String.format("fader_%s", channel);
                        imageView.setImageResource(getResources().getIdentifier(variableValue,
                                "drawable", "com.dontstopthemusic.dontstopthemusic"));
                        break;
                    }
                    case FOUR_TurnDownFader: {
                        currentState = HissStates.FIVE_UnhitPFL;
                        textView.setText(String.format("Hit the PFL button for channel %s to deselect" +
                                "this channel."));
                        variableValue = String.format("pfl_%s", channel);
                        imageView.setImageResource(getResources().getIdentifier(variableValue,
                                "drawable", "com.dontstopthemusic.dontstopthemusic"));
                        break;
                    }
                    case FIVE_UnhitPFL: {
                        currentState = HissStates.SIX_Exit;
                        int oldChannel = channel;
                        if (TEST_hiss[0] || TEST_hiss[1]) {
                            channel = TEST_hiss[0] ? 0 : 1;
                            if (oldChannel == channel) {
                                textView.setText("Unfortunately, there are no other options to solve" +
                                        "the issue via the soundboard. Please check the DI box.");
                            }
                        }
                        else {
                            textView.setText("We have gone through all of the options and no more hiss" +
                                    "is detected. If you still hear any hiss, check the DI box.");
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
                        break;
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