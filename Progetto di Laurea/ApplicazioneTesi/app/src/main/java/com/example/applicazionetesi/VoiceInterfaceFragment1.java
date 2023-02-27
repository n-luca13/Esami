package com.example.applicazionetesi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;



public class VoiceInterfaceFragment1 extends Fragment {
    private AppCompatImageButton microphone_button;
    private ImageView icon_voice_feedback;
    private TextView utterance_textView;
    private TextView mic_prompt_hint;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.voice_interface_fragment1, container, false);
        microphone_button = rootView.findViewById(R.id.microphone_button);
        icon_voice_feedback = rootView.findViewById(R.id.icon_voice_feedback);
        utterance_textView = rootView.findViewById(R.id.utterance_textView);
        mic_prompt_hint = rootView.findViewById(R.id.mic_prompt_hint);
        return rootView;
    }

    @Override
    public void onResume() {
        if (getActivity() instanceof MicCallbackInterface) {
            setOnMicListener((MicCallbackInterface) getActivity());
        }
        micListener.onMicCallback();
        super.onResume();
    }

    public void updateFragmentOne(boolean isListening){
        if (isListening){
            microphone_button.setBackgroundResource(R.drawable.circle_mic_on);
            microphone_button.setColorFilter(ContextCompat.getColor(requireActivity(), R.color.blue_1));
            icon_voice_feedback.setColorFilter(ContextCompat.getColor(requireActivity(), R.color.gray_6));
            utterance_textView.setText(". . .");
            mic_prompt_hint.setText(R.string.tocca_per_interrompere);
        } else {
            microphone_button.setBackgroundResource(R.drawable.circle_mic_off);
            microphone_button.setColorFilter(ContextCompat.getColor(requireActivity(), R.color.gray_7));
            icon_voice_feedback.setColorFilter(ContextCompat.getColor(requireActivity(), R.color.bckgrd_gray_3));
            utterance_textView.setText(null);
            mic_prompt_hint.setText(R.string.tocca_per_parlare);
        }
    }

    public interface MicCallbackInterface{
        void onMicCallback();
    }
    private MicCallbackInterface micListener;

    public void setOnMicListener(MicCallbackInterface micListener) {
        this.micListener = micListener;
    }
}