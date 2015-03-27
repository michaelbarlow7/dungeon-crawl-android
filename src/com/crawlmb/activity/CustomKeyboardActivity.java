package com.crawlmb.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.crawlmb.Preferences;
import com.crawlmb.R;
import com.crawlmb.keyboard.CrawlKeyboardWrapper;
import com.crawlmb.keyboard.CrawlKeyboardWrapper.SpecialKey;
import com.crawlmb.keyboard.KeyboardLayoutSpinnerAdapter;
import com.crawlmb.keylistener.KeyListener;

/**
 * Created by michael on 25/03/15.
 */
public class CustomKeyboardActivity extends Activity implements KeyListener, AdapterView.OnItemSelectedListener {

    private Spinner layoutSpinner;
    private Button deleteLayout;
    private CrawlKeyboardWrapper virtualKeyboard;
    private Button newLayout;
    private KeyboardLayoutSpinnerAdapter adapter;
    private int changingKey = -1;
    private int changingKeyIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        RelativeLayout parentLayout = (RelativeLayout) layoutInflater.inflate(R.layout.custom_keyboard, null);
        setContentView(parentLayout);

        // Add keyboard
        virtualKeyboard = new CrawlKeyboardWrapper(this, this);
        parentLayout.addView(virtualKeyboard.virtualKeyboardView);

        View buttons = layoutInflater.inflate(R.layout.custom_keyboard_options, null);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.ABOVE, R.id.keyboard);
        parentLayout.addView(buttons, params);

        layoutSpinner = (Spinner) buttons.findViewById(R.id.layoutSpinner);
        adapter = new KeyboardLayoutSpinnerAdapter();
        layoutSpinner.setAdapter(adapter);

        layoutSpinner.setOnItemSelectedListener(this);

        deleteLayout = (Button) findViewById(R.id.deleteLayout);

        newLayout = (Button) findViewById(R.id.newLayout);
    }

    @Override
    protected void onResume() {
        super.onResume();

        setViews();
    }

    private void setViews() {
        adapter.notifyDataSetChanged();

        int currentKeyboardLayout = Preferences.getCurrentKeyboardLayout();
        int keyboardCount = Preferences.getLayoutCount();

        virtualKeyboard.virtualKeyboardView.invalidateAllKeys();

        layoutSpinner.setSelection(currentKeyboardLayout);

        deleteLayout.setEnabled(currentKeyboardLayout > 0);

        newLayout.setEnabled(keyboardCount == 0);
    }


    @Override
    public void addKey(int key, int keyIndex) {
        if (keyIndex < 0){
            return;
        }
        changingKey = key;
        changingKeyIndex = keyIndex;
        Dialog characterBindingDialog = createCharacterBindingDialog();
        characterBindingDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        characterBindingDialog.show();
    }

    @Override
    public void addDirectionKey(int key) {
        // I don't think I need this right now
    }

    // We don't really need this right now, but might be useful if we want multiple layouts.
    // My ideal solution would involve storing string sets in preferences, but that's only possible
    // on API 11 and above, so maybe once people stop using Gingerbread
    public void onNewLayoutClick(View v){
        Preferences.addNewKeyboardLayout();
        setViews();
    }

    public void onDeleteLayoutClick(View v){
        final int currentKeyboardLayout = Preferences.getCurrentKeyboardLayout();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_layout);
        builder.setMessage(R.string.delete_layout_message);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Preferences.deleteLayout(CustomKeyboardActivity.this, currentKeyboardLayout);
                setViews();
            }
        });
        builder.setNegativeButton(android.R.string.no, null);
        builder.show();
    }

    private Dialog createCharacterBindingDialog()
    {
        final Dialog characterBindingDialog = new Dialog(this);
        characterBindingDialog.setContentView(R.layout.character_binding_dialog);
        final char changingChar = (char) changingKey;
        characterBindingDialog.setTitle("Changing character " + changingChar + " to...");
        final EditText characterField = (EditText) characterBindingDialog.findViewById(R.id.character_field);
        characterField.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                //Not needed
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
                //Not needed
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                characterField.selectAll();
                characterField.clearFocus();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(characterField.getWindowToken(), 0);
            }
        });
        characterField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean isFocussed) {
                if (isFocussed){
                    characterField.selectAll();
                }
            }
        });
        Button positiveButton = (Button) characterBindingDialog.findViewById(R.id.positiveButton);
        positiveButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (characterField.getText().length() == 0)
                {
                    Toast.makeText(CustomKeyboardActivity.this, R.string.please_select_a_character, Toast.LENGTH_SHORT).show();
                    return;
                }

                Editable bindChar = characterField.getText();
                char code = bindChar.charAt(0);
                String label = String.valueOf(code);

                Preferences.addKeybindingToLayout(v.getContext(), virtualKeyboard.getCurrentKeyboardType(), changingKeyIndex , code, label);

                if (Preferences.getLayoutCount() == 0){
                    Preferences.setCustomLayoutCount(1);
                }
                Preferences.setCurrentKeyboardLayout(1);
                characterBindingDialog.dismiss();
                setViews();
                Toast.makeText(v.getContext(), "Set character " + changingChar + " to " + label, Toast.LENGTH_SHORT).show();

            }
        });
        Button revertButton = (Button) characterBindingDialog.findViewById(R.id.revertButton);
        revertButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Preferences.clearKeybindingInLayout(CustomKeyboardActivity.this, virtualKeyboard.getCurrentKeyboardType(), changingKeyIndex);
                characterBindingDialog.dismiss();
                Toast.makeText(view.getContext(), "Reverted character", Toast.LENGTH_LONG).show();
                setViews();
            }
        });
        Button negativeButton = (Button) characterBindingDialog.findViewById(R.id.negativeButton);
        negativeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                characterBindingDialog.dismiss();
            }
        });
        Button specialButton = (Button) characterBindingDialog.findViewById(R.id.specialButton);
        specialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                characterBindingDialog.dismiss();
                showGetSpecialCharacterDialog();
            }
        });
        //TODO: Have a "revert to default" button


        return characterBindingDialog;
    }

    private void showGetSpecialCharacterDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setAdapter(new ArrayAdapter<SpecialKey>(getBaseContext(), R.layout.simple_list_item_black, SpecialKey.values()), new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                int code = SpecialKey.values()[which].getCode();
                Preferences.addKeybindingToLayout(CustomKeyboardActivity.this, virtualKeyboard.getCurrentKeyboardType(), changingKeyIndex, code, null);
                setViews();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        char changingChar = (char) changingKey;
        builder.setTitle("Changing character " + changingChar + " to...");
        builder.show();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        Preferences.setCurrentKeyboardLayout(position);
        setViews();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // I don't think we need to do anything here
    }
}
