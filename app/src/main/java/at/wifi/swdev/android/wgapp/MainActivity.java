package at.wifi.swdev.android.wgapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.List;

import at.wifi.swdev.android.wgapp.data.User;
import at.wifi.swdev.android.wgapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity
{
    public static final int RC_SIGN_IN = 12357;
    private ActivityMainBinding binding;
    private static String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        //Ist User schon angemeldet?
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null)
        {
            //Der Benutzer ist nicht angemeldet
            List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().build());

            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(), RC_SIGN_IN);
        } else
        {
            binding = ActivityMainBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
            showNavigationBar();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == RC_SIGN_IN)
        {
            binding = ActivityMainBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
//            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK)
            {
                if (data != null)
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
                                showNavigationBar();
                            } else
                            {
                                Toast.makeText(MainActivity.this, R.string.no_account, Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    });
                } else
                {
                    binding.container.setClickable(false);
                    Toast.makeText(MainActivity.this, R.string.no_account, Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else
            {
                //Anmeldung nicht erfolgreich
                Toast.makeText(MainActivity.this, R.string.no_account, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public void showNavigationBar()
    {
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_todo, R.id.nav_shoppinglist, R.id.nav_cal).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }
    /*
    TODO: 2. Alle erledigten löschen
    TODO: 3: maybe kalender swipe to delete? & Kalender Popup??
    */

}
