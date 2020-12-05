package com.danielkim.soundrecorder.fragments;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

import com.danielkim.soundrecorder.BuildConfig;
import com.danielkim.soundrecorder.MySharedPreferences;
import com.danielkim.soundrecorder.R;
import com.danielkim.soundrecorder.activities.SettingsActivity;

/**
 * Created by Daniel on 5/22/2017.
 */

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        CheckBoxPreference highQualityPref = (CheckBoxPreference) findPreference(getResources().getString(R.string.pref_high_quality_key));
        highQualityPref.setChecked(MySharedPreferences.getPrefHighQuality(getActivity()));
        highQualityPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                MySharedPreferences.setPrefHighQuality(getActivity(), (boolean) newValue);
                return true;
            }
        });

        Preference aboutPref = findPreference(getString(R.string.pref_about_key));
        aboutPref.setSummary(getString(R.string.pref_about_desc, BuildConfig.VERSION_NAME));
        aboutPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                LicensesFragment licensesFragment = new LicensesFragment();
                licensesFragment.show(((SettingsActivity)getActivity()).getSupportFragmentManager().beginTransaction(), "dialog_licenses");
                return true;
            }
        });

        EditTextPreference hostPref = (EditTextPreference) findPreference("pref_host");
        hostPref.setSummary(MySharedPreferences.getPrefHost(getActivity()));
        hostPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                MySharedPreferences.setPrefHost(getActivity(), (String) newValue);

                EditTextPreference hostPref = (EditTextPreference) findPreference("pref_host");
                hostPref.setSummary(MySharedPreferences.getPrefHost(getActivity()));
                return true;
            }
        });


        EditTextPreference devicePref = (EditTextPreference) findPreference("pref_device");
        devicePref.setSummary(MySharedPreferences.getPrefDevice(getActivity()));
        devicePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                MySharedPreferences.setPrefDevice(getActivity(), (String) newValue);

                EditTextPreference devicePref = (EditTextPreference) findPreference("pref_device");
                devicePref.setSummary(MySharedPreferences.getPrefDevice(getActivity()));
                return true;
            }
        });


        EditTextPreference sampleRatePref = (EditTextPreference) findPreference("pref_sample_rate");
        sampleRatePref.setSummary(MySharedPreferences.getPrefSampleRate(getActivity()));
        sampleRatePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                MySharedPreferences.setPrefSampleRate(getActivity(), (String) newValue);

                EditTextPreference  sampleRatePref = (EditTextPreference) findPreference("pref_sample_rate");
                sampleRatePref.setSummary(MySharedPreferences.getPrefSampleRate(getActivity()));
                return true;
            }
        });

        EditTextPreference audioSampleRatePref = (EditTextPreference) findPreference("pref_audio_sample_rate");
        audioSampleRatePref.setSummary(MySharedPreferences.getPrefAudioSampleRate(getActivity()));
        audioSampleRatePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                MySharedPreferences.setPrefAudioSampleRate(getActivity(), (String) newValue);
                EditTextPreference audioSampleRatePref = (EditTextPreference) findPreference("pref_audio_sample_rate");
                audioSampleRatePref.setSummary(MySharedPreferences.getPrefAudioSampleRate(getActivity()));
                return true;
            }
        });


    }
}
