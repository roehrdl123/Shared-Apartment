package at.wifi.swdev.android.wgapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.List;

import at.wifi.swdev.android.wgapp.data.User;
import at.wifi.swdev.android.wgapp.databinding.ActivityMainBinding;
import at.wifi.swdev.android.wgapp.shoppinglist.ShoppingListActivity;
import at.wifi.swdev.android.wgapp.todolist.TodoListMainActivity;

public class MainActivity extends AppCompatActivity
{
    public static final int RC_SIGN_IN = 12357;
    private List<AuthUI.IdpConfig> providers;
    private ActivityMainBinding binding;
    private static String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseApp.initializeApp(this);
        //Ist User schon angemeldet?
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null)
        {
            //Der Benutzer ist nicht angemeldet
            providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build(),
                    new AuthUI.IdpConfig.PhoneBuilder().build(),
                    new AuthUI.IdpConfig.GoogleBuilder().build());

            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(), RC_SIGN_IN);
        }
    }

    public void onShoppingList(View view)
    {
        Intent intent = new Intent(this, ShoppingListActivity.class);
        startActivity(intent);
    }

    public void onOpenTodo(View view)
    {
        Intent intent = new Intent(this, TodoListMainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == RC_SIGN_IN)
        {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK)
            {
                //User auslesen
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                //In Datenbank speichern
                DatabaseReference users = FirebaseDatabase.getInstance().getReference("users");
                users.child(user.getUid()).setValue(new User(user.getUid(), user.getDisplayName())).addOnCompleteListener(this, new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            Log.d(TAG, "onComplete: USER created");
                        }
                        else
                        {
                            Log.e(TAG, "onComplete: USER failed");
                        }
                    }
                });
            }
            else
            {
                //Anmeldung nicht erfolgreich
                binding.button.setClickable(false);
                binding.button2.setClickable(false);
                Snackbar.make(binding.getRoot(), "Fehler bei der Anmeldung", BaseTransientBottomBar.LENGTH_LONG).show();
                Log.d(TAG, "Failed to sign in: " + response.getError());
            }
        }
    }
}
