package com.example.spontaneity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.spontaneity.databinding.LoginBinding;
import com.example.spontaneity.databinding.OnboardingBinding;
import com.example.spontaneity.databinding.SignupBinding;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

// when the user first enters the app
// will signup or login

public class OnboardingFragment extends Fragment {

    // get the bindings for all three key layouts
    private OnboardingBinding binding;
    private LoginBinding loginBinding;
    private SignupBinding signupBinding;

    // file manager to query the uesr's data
    private FileManager userFileManager;

    // is this the user's first time installing the app
    private boolean firstTime;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = OnboardingBinding.inflate(inflater, container, false);
        userFileManager = new FileManager(getContext(), getActivity(),"user.txt");

        // DEBUG: TO DELETE USER PROFILE
//        userFileManager.deleteFile();

        if (userFileManager.wasCreated()) {
            firstTime = false;
            binding.onboardingFields.setLayoutResource(R.layout.login);
            loginBinding = LoginBinding.bind(binding.onboardingFields.inflate());
        } else {
            firstTime = true;
            binding.onboardingFields.setLayoutResource(R.layout.signup);
            signupBinding = SignupBinding.bind(binding.onboardingFields.inflate());
        }
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Spontaneity");
        // hide all the menu buttons, which would break on this screen, because they act on remindersdisplayfrag
        // probably a more efficient way to do this with toolbars
        MenuHost menuHost = getActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                MenuItem deleteAllButton = menu.findItem(R.id.delete_all_button);
                MenuItem addDefaultReminders = menu.findItem(R.id.add_default_reminders);
                MenuItem settingsButton = menu.findItem(R.id.settings_button);
                if (deleteAllButton != null) {
                    deleteAllButton.setVisible(false);
                }
                if (addDefaultReminders != null) {
                    addDefaultReminders.setVisible(false);
                }
                if (settingsButton != null) {
                    settingsButton.setVisible(false);
                }
            }

            // mandatory implementation
            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                return false;
            }
        });

        if (firstTime) {
            signupBinding.signupButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // save the binding's fields to a new file
                    userFileManager.createFile(new String[] {
                            signupBinding.nameFieldSignup.getText().toString(),
                            signupBinding.passwordFieldSignup.getText().toString(),
                            signupBinding.realNameFieldSignup.getText().toString(),
                            signupBinding.frequencyFieldSignup.getText().toString()
                    });
                    // and navigate to the next screen
                    NavHostFragment.findNavController(OnboardingFragment.this)
                            .navigate(R.id.action_onboardingFragment_to_reminderDisplayFragment);
                }
            });
        } else {
            // login button is present
            loginBinding.loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String[] readFile = userFileManager.readFile();
                    // compare the saved data and the user's inputs
                    if (readFile[0].equals(loginBinding.nameFieldLogin.getText().toString()) &&
                            readFile[1].equals(loginBinding.passwordFieldLogin.getText().toString())) {
                        // both are the same
                        // all good, do nothing
                    } else {
                        Snackbar.make(
                                getContext(),
                                view,
                                "Incorrect. Please try again.",
                                BaseTransientBottomBar.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    NavHostFragment.findNavController(OnboardingFragment.this)
                            .navigate(R.id.action_onboardingFragment_to_reminderDisplayFragment);
                }
            });
        }
    }
}