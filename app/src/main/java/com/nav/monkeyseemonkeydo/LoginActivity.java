package com.nav.monkeyseemonkeydo;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    /** SUMMARY
     * Variable declarations */
    EditText IEmail;
    EditText IPassword;
    TextView PWForgot;
    Button Login;
    Button GoRegister;
    ProgressBar progressBar;
    FirebaseAuth mAuth;
    TextView Verify;
    LinearLayout MainL;

/** SUMMARY
 *  onCreate method, from here variables will be set to there element id if needed and othre methods will be called to preform specific functions*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        IEmail = findViewById(R.id.edtIEmail);
        IPassword = findViewById(R.id.edtIPassword);
        Login = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.pbbar1);
        PWForgot = findViewById(R.id.btnResetPW);
        Verify = findViewById(R.id.txtVerify);
        GoRegister = findViewById(R.id.btnGoToRegister);
        MainL = findViewById(R.id.lvparent);



        GoRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
        mAuth = FirebaseAuth.getInstance();
        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkDataEntered();
            }
        });

        /** SUMMARY
         * used if the users forgets their password
         * the user enters their email
         * they receive a link to follow*/
        PWForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText resetMail = new EditText(view.getContext());
                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(view.getContext());
                passwordResetDialog.setTitle("Reset Password?");
                passwordResetDialog.setMessage("Enter Your Email To Receive Reset Link");
                passwordResetDialog.setView(resetMail);

                passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String mail = resetMail.getText().toString();
                        mAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(LoginActivity.this,"Reset Link Has Been Sent To Your Email", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(LoginActivity.this,"Error! Reset Link Not Sent To Your Email" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                });

                passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                passwordResetDialog.create().show();
            }
        });



    }

    /** SUMMARY
     * returns true or false, determines if their is a blank input */
    boolean isEmpty(EditText text) {
        CharSequence str = text.getText().toString();
        return TextUtils.isEmpty(str);
    }

    /** SUMMARY
     * returns true or false, determines if their is a blank input */
    boolean isEmail(EditText text) {
        CharSequence email = text.getText().toString();
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    /** SUMMARY
     * Validates the user inputs */
    void checkDataEntered()
    {
        if (isEmail(IEmail) == false) {
            IEmail.setError("Enter valid email!");
        }else

        if (isEmpty(IPassword)) {
            Toast t = Toast.makeText(this, "You must enter a password to login!", Toast.LENGTH_SHORT);
            t.show();
        }else
            LogUserIn();

    }

    /** SUMMARY
     * if the user account is verified and the account details are correct
     * the user will be logged in*/
    void LogUserIn()
    {
        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(IEmail.getText().toString(), IPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {


                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    if (!user.isEmailVerified())
                    {

                        Verify.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                    else
                    {
                        Toast.makeText(LoginActivity.this, "Logged in successfully.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    }

                } else {

                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(LoginActivity.this, "Error!" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                }
            }
        });




    }


}