package com.example.zfile.audiovis;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.zfile.audiovis.ColorPicker.ColorEnvelope;
import com.example.zfile.audiovis.ColorPicker.ColorListener;
import com.example.zfile.audiovis.ColorPicker.ColorPickerDialog;
import com.example.zfile.audiovis.ColorPicker.ColorPickerView;
import com.example.zfile.audiovis.ColorPicker.CustomFlag;
import com.example.zfile.audiovis.permissions.PermissionsActivity;
import com.example.zfile.audiovis.permissions.PermissionsChecker;
import com.example.zfile.audiovis.renderer.RendererFactory;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Visualizer.OnDataCaptureListener {

    private static final int CAPTURE_SIZE = 256;
    private static final int REQUEST_CODE = 0;
    static final String[] PERMISSIONS = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.MODIFY_AUDIO_SETTINGS};

    private Visualizer visualiser;
    private WaveformView waveformView;
    private PermissionsChecker checker;
    private int foreground;
    private int background;
    public static final long startTime = System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null) {
            foreground = savedInstanceState.getInt(getString(R.string.foreground));
            background = savedInstanceState.getInt(getString(R.string.background));
        } else {
            foreground = getResources().getColor(R.color.foregroundColor);
            background = getResources().getColor(R.color.backgroundColor);
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        waveformView = (WaveformView) findViewById(R.id.waveform_view);
        RendererFactory rendererFactory = new RendererFactory();
        waveformView.setRenderer(rendererFactory.createSimpleWaveformRenderer(
                foreground,
                background)
        );
        if (savedInstanceState != null) {
            waveformView.setAskTime(savedInstanceState.getDouble(getString(R.string.askTime)));
            waveformView.setStartTime(savedInstanceState.getDouble(getString(R.string.startTime)));
        }

        checker = new PermissionsChecker(this);
        if (checker.lacksPermissions(PERMISSIONS)) {
            Snackbar.make(toolbar, R.string.no_permissions, Snackbar.LENGTH_INDEFINITE).show();
        }


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showColorDialog();
//                if (builder.getParent()!=null)
//                    ((ViewGroup)builder.getParent()).removeView(builder);
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(getString(R.string.foreground), foreground);
        outState.putInt(getString(R.string.background), background);
        outState.putDouble(getString(R.string.askTime), waveformView.getAskTime());
        outState.putDouble(getString(R.string.startTime), waveformView.getStartTime());
    }

    private void showColorDialog() {
        ColorPickerDialog.Builder builder = new ColorPickerDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        ColorPickerView colorPickerView = builder.getColorPickerView();
        colorPickerView.setPreferenceName("MyColorPickerDialog");
        builder.setTitle("ColorPicker Dialog");
        builder.setPreferenceName("MyColorPickerDialog");
        builder.setFlagView(new CustomFlag(this, R.layout.layout_flag));
        builder.setPositiveButton(getString(R.string.confirm), new ColorListener() {
            @Override
            public void onColorSelected(ColorEnvelope colorEnvelope) {
                String colorHtml = colorEnvelope.getColorHtml();
                String htmlthingy = "#" + colorHtml;
                foreground = Color.parseColor(htmlthingy);
                TextView textView = findViewById(R.id.textView);
                textView.setText(htmlthingy);

                waveformView.setRenderColor(foreground);
//                LinearLayout linearLayout = findViewById(R.id.linearLayout);
//                linearLayout.setBackgroundColor(colorEnvelope.getColor());
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (checker.lacksPermissions(PERMISSIONS)) {
            startPermissionsActivity();
        } else {
            startVisualiser();
        }
    }

    private void startPermissionsActivity() {
        PermissionsActivity.startActivityForResult(this, REQUEST_CODE, PERMISSIONS);
    }

    private void startVisualiser() {
        visualiser = new Visualizer(0);
        visualiser.setDataCaptureListener(this, Visualizer.getMaxCaptureRate(), true, false);
        visualiser.setCaptureSize(CAPTURE_SIZE);
        visualiser.setEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == PermissionsActivity.PERMISSIONS_DENIED) {
            finish();
        }
    }

    @Override
    protected void onPause() {
        if (visualiser != null) {
            visualiser.setEnabled(false);
            visualiser.release();
            visualiser.setDataCaptureListener(null, 0, false, false);
        }
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            default:
            case R.id.nav_manage:
                break;
            case R.id.nav_share:
                break;
            case R.id.nav_view:
                break;
            case R.id.fab:
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onWaveFormDataCapture(Visualizer thisVisualiser, byte[] waveform, int samplingRate) {
        if (waveformView != null) {
            waveformView.setWaveform(waveform);
        }
    }

    @Override
    public void onFftDataCapture(Visualizer thisVisualiser, byte[] fft, int samplingRate) {
        // NO-OP
    }

    public void openReferences(MenuItem item) {
        Intent intent = new Intent(this, CreditsActivity.class);
        startActivity(intent);
    }
}
