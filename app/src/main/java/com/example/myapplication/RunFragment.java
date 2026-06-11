package com.example.myapplication;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class RunFragment extends Fragment implements SensorEventListener {

    // ── Views ─────────────────────────────────────────────
    private TextView tvTimer, tvDistance, tvPace, tvSteps,
            tvCalories, tvBPM, tvStatus, tvStreak;
    private Button   btnStartStop, btnFinish;

    // ── State ─────────────────────────────────────────────
    private boolean isRunning  = false;
    private boolean hasStarted = false;

    // ── Timer ─────────────────────────────────────────────
    private long startTimeMs = 0;
    private long elapsedMs   = 0;
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private final Runnable timerRunnable = new Runnable() {
        @Override public void run() {
            if (!isRunning) return;
            elapsedMs = SystemClock.elapsedRealtime() - startTimeMs;
            updateAllStats();
            timerHandler.postDelayed(this, 1000);
        }
    };

    // ── GPS ───────────────────────────────────────────────
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback            locationCallback;
    private Location                    lastLocation;
    private double                      totalDistanceKm = 0;

    // ── Step sensor ───────────────────────────────────────
    private SensorManager sensorManager;
    private Sensor        stepSensor;
    private Sensor        heartRateSensor;
    private int           stepCount   = 0;
    private int           stepOffset  = -1;

    // ── Heart rate ────────────────────────────────────────
    // Built-in sensor OR Bluetooth HR strap
    private int currentBPM = 0;
    private boolean usingBuiltInHR = false;

    // ── Bluetooth HR strap ────────────────────────────────
    // Standard Bluetooth GATT UUIDs for Heart Rate Service
    private static final UUID HR_SERVICE_UUID =
            UUID.fromString("0000180D-0000-1000-8000-00805F9B34FB");
    private static final UUID HR_CHAR_UUID =
            UUID.fromString("00002A37-0000-1000-8000-00805F9B34FB");
    private static final UUID CCCD_UUID =
            UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");

    private BluetoothLeScanner bleScanner;
    private BluetoothGatt      bluetoothGatt;
    private boolean            bleScanning = false;
    private boolean            bleConnected = false;

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocation = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                if (fineLocation != null && fineLocation) {
                    startRun();
                } else {
                    Toast.makeText(getContext(),
                            getString(R.string.permissions_required),
                            Toast.LENGTH_LONG).show();
                }
            });

    // ── User profile (for calorie calc) ──────────────────
    private double userWeightKg = 70.0;

    // ── Streak ────────────────────────────────────────────
    private int currentStreak = 0;

    // ── Firebase ─────────────────────────────────────────
    private FirebaseServices fbs;

    private static final String TAG = "RunFragment";

    public RunFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_run, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fbs = FirebaseServices.getInstance();
        bindViews(view);
        setupSensors();
        setupLocation();
        setupBluetooth();
        setupButtons();
        loadUserProfile();
        loadStreakFromFirestore();
    }

    // ── Binding ───────────────────────────────────────────

    private void bindViews(View view) {
        tvTimer      = view.findViewById(R.id.tvRunTimer);
        tvDistance   = view.findViewById(R.id.tvRunDistance);
        tvPace       = view.findViewById(R.id.tvRunPace);
        tvSteps      = view.findViewById(R.id.tvRunSteps);
        tvCalories   = view.findViewById(R.id.tvRunCalories);
        tvBPM        = view.findViewById(R.id.tvRunBPM);
        tvStatus     = view.findViewById(R.id.tvRunStatus);
        tvStreak     = view.findViewById(R.id.tvRunStreak);
        btnStartStop = view.findViewById(R.id.btnRunStartStop);
        btnFinish    = view.findViewById(R.id.btnRunFinish);
    }

    // ── Load user weight for accurate calories ────────────

    private void loadUserProfile() {
        fbs.getUserData(task -> {
            if (!isAdded() || getContext() == null) return;
            if (task.isSuccessful() && task.getResult() != null
                    && task.getResult().exists()) {
                User user = task.getResult().toObject(User.class);
                if (user != null && user.getWeightKg() > 0) {
                    userWeightKg = user.getWeightKg();
                }
            }
        });
    }

    // ── Load real streak from Firestore ───────────────────

    private void loadStreakFromFirestore() {
        fbs.getUserRuns(task -> {
            if (!isAdded() || getContext() == null) return;
            if (!task.isSuccessful() || task.getResult() == null) return;

            List<String> dates = new ArrayList<>();
            for (QueryDocumentSnapshot doc : task.getResult()) {
                RunItem run = doc.toObject(RunItem.class);
                if (run.getDate() != null && !run.getDate().isEmpty())
                    dates.add(run.getDate());
            }
            // Add today tentatively so streak shows correctly before this run saves
            dates.add(Utils.getTodayDate());

            currentStreak = Utils.calculateStreak(dates);
            if (tvStreak != null)
                requireActivity().runOnUiThread(() ->
                        tvStreak.setText(String.format(Locale.getDefault(), "%d day streak", currentStreak)));
        });
    }

    // ── Built-in sensors ─────────────────────────────────

    private void setupSensors() {
        sensorManager   = (SensorManager) requireContext()
                .getSystemService(Context.SENSOR_SERVICE);
        stepSensor      = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

        if (heartRateSensor != null) {
            usingBuiltInHR = true;
            Log.d(TAG, "Built-in HR sensor found");
        }
        if (stepSensor == null) {
            tvSteps.setText("N/A");
        }
    }

    private void registerSensors() {
        if (stepSensor != null)
            sensorManager.registerListener(this, stepSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        if (heartRateSensor != null)
            sensorManager.registerListener(this, heartRateSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void unregisterSensors() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!isAdded()) return;

        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int total = (int) event.values[0];
            if (stepOffset == -1) stepOffset = total;
            stepCount = total - stepOffset;
            requireActivity().runOnUiThread(() ->
                    tvSteps.setText(String.valueOf(stepCount)));
        }

        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE && usingBuiltInHR) {
            int bpm = (int) event.values[0];
            if (bpm > 0) {
                currentBPM = bpm;
                requireActivity().runOnUiThread(() ->
                        tvBPM.setText(String.format(Locale.getDefault(), "%d bpm", bpm)));
            }
        }
    }

    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    // ── Bluetooth BLE HR strap ────────────────────────────

    private void setupBluetooth() {
        BluetoothManager bm = (BluetoothManager) requireContext()
                .getSystemService(Context.BLUETOOTH_SERVICE);
        if (bm != null) {
            BluetoothAdapter bluetoothAdapter = bm.getAdapter();
            if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                bleScanner = bluetoothAdapter.getBluetoothLeScanner();
            }
        }
    }

    /**
     * Scans for BLE devices advertising the Heart Rate Service UUID.
     * Connects automatically to the first one found.
     * This covers Polar, Garmin, Wahoo, and most standard HR straps.
     */
    private void startBLEScan() {
        if (bleScanner == null || bleConnected || bleScanning) return;
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "BLE scan permission not granted");
            return;
        }

        ScanFilter hrFilter = new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(HR_SERVICE_UUID))
                .build();
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        bleScanning = true;
        bleScanner.startScan(
                Collections.singletonList(hrFilter), settings, bleScanCallback);

        // Stop scan after 10 seconds to save battery
        timerHandler.postDelayed(this::stopBLEScan, 10_000);
        Log.d(TAG, "BLE scan started for HR strap");
    }

    private void stopBLEScan() {
        if (!bleScanning || bleScanner == null) return;
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED) return;
        bleScanner.stopScan(bleScanCallback);
        bleScanning = false;
    }

    private final ScanCallback bleScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (bleConnected) return;
            BluetoothDevice device = result.getDevice();
            Log.d(TAG, "HR strap found: " + device.getAddress());
            stopBLEScan();
            connectToHRStrap(device);
        }
        @Override
        public void onScanFailed(int errorCode) {
            bleScanning = false;
            Log.e(TAG, "BLE scan failed: " + errorCode);
        }
    };

    private void connectToHRStrap(BluetoothDevice device) {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) return;

        bluetoothGatt = device.connectGatt(
                requireContext(), false, gattCallback, BluetoothDevice.TRANSPORT_LE);
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt,
                                            int status, int newState) {
            if (ActivityCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) return;

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                bleConnected = true;
                Log.d(TAG, "Connected to HR strap, discovering services…");
                gatt.discoverServices();
                if (isAdded())
                    requireActivity().runOnUiThread(() -> {
                        if (tvBPM != null) tvBPM.setText(R.string.strap_connected);
                        Toast.makeText(getContext(),
                                getString(R.string.hr_strap_connected), Toast.LENGTH_SHORT).show();
                    });
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                bleConnected = false;
                Log.d(TAG, "HR strap disconnected");
                if (isAdded())
                    requireActivity().runOnUiThread(() -> {
                        if (tvBPM != null && !usingBuiltInHR)
                            tvBPM.setText(R.string.no_bpm);
                    });
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (ActivityCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) return;

            BluetoothGattService hrService = gatt.getService(HR_SERVICE_UUID);
            if (hrService == null) { Log.e(TAG, "HR service not found"); return; }

            BluetoothGattCharacteristic hrChar =
                    hrService.getCharacteristic(HR_CHAR_UUID);
            if (hrChar == null) { Log.e(TAG, "HR characteristic not found"); return; }

            // Enable notifications
            gatt.setCharacteristicNotification(hrChar, true);
            BluetoothGattDescriptor descriptor = hrChar.getDescriptor(CCCD_UUID);
            if (descriptor != null) {
                gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            }
            Log.d(TAG, "HR notifications enabled");
        }

        @Override
        public void onCharacteristicChanged(@NonNull BluetoothGatt gatt,
                                            @NonNull BluetoothGattCharacteristic characteristic,
                                            @NonNull byte[] value) {
            if (!characteristic.getUuid().equals(HR_CHAR_UUID)) return;

            // Parse standard Heart Rate Measurement format (IEEE spec)
            // Byte 0: Flags, Byte 1: HR Value (UINT8) or HR Value (UINT16)
            int flag = value[0] & 0x01;
            int bpm;
            if (flag == 0) {
                bpm = value[1] & 0xFF;
            } else {
                bpm = ((value[2] & 0xFF) << 8) | (value[1] & 0xFF);
            }

            if (bpm > 0 && !usingBuiltInHR) {
                currentBPM = bpm;
                if (isAdded())
                    requireActivity().runOnUiThread(() -> {
                        if (tvBPM != null)
                            tvBPM.setText(String.format(Locale.getDefault(), "%d bpm", bpm));
                    });
            }
        }
    };

    private void disconnectBLE() {
        if (bluetoothGatt == null) return;
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) return;
        bluetoothGatt.disconnect();
        bluetoothGatt.close();
        bluetoothGatt = null;
        bleConnected  = false;
    }

    // ── GPS ───────────────────────────────────────────────

    private void setupLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(
                requireActivity());
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                if (!isRunning) return;
                Location loc = result.getLastLocation();
                if (loc == null) return;
                if (lastLocation != null) {
                    float[] dist = new float[1];
                    Location.distanceBetween(
                            lastLocation.getLatitude(),  lastLocation.getLongitude(),
                            loc.getLatitude(),           loc.getLongitude(), dist);
                    if (dist[0] > 2f && dist[0] < 50f)
                        totalDistanceKm += dist[0] / 1000.0;
                }
                lastLocation = loc;
                if (isAdded())
                    requireActivity().runOnUiThread(() ->
                            tvDistance.setText(
                                    String.format(Locale.getDefault(), "%.2f", totalDistanceKm)));
            }
        };
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACTIVITY_RECOGNITION,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
            });
            return;
        }
        LocationRequest req = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 3000)
                .setMinUpdateIntervalMillis(2000).build();
        fusedLocationClient.requestLocationUpdates(
                req, locationCallback, Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    // ── Buttons ───────────────────────────────────────────

    private void setupButtons() {
        btnStartStop.setOnClickListener(v -> {
            if (!isRunning) startRun();
            else            pauseRun();
        });
        btnFinish.setOnClickListener(v -> {
            if (!hasStarted) {
                Toast.makeText(getContext(),
                        getString(R.string.start_run_first), Toast.LENGTH_SHORT).show();
                return;
            }
            finishRun();
        });
    }

    private void startRun() {
        isRunning   = true;
        hasStarted  = true;
        startTimeMs = SystemClock.elapsedRealtime() - elapsedMs;
        timerHandler.post(timerRunnable);
        startLocationUpdates();
        registerSensors();
        // Try BLE HR strap if no built-in HR
        if (!usingBuiltInHR) startBLEScan();
        btnStartStop.setText(R.string.pause);
        tvStatus.setText(R.string.running);
    }

    private void pauseRun() {
        isRunning = false;
        timerHandler.removeCallbacks(timerRunnable);
        stopLocationUpdates();
        unregisterSensors();
        btnStartStop.setText(R.string.resume);
        tvStatus.setText(R.string.paused);
    }

    private void finishRun() {
        isRunning = false;
        timerHandler.removeCallbacks(timerRunnable);
        stopLocationUpdates();
        unregisterSensors();
        stopBLEScan();
        disconnectBLE();

        // ── Use MET-based calories if we have distance, else step-based ──
        int calories = totalDistanceKm > 0.1
                ? Utils.calculateCaloriesMET(totalDistanceKm,
                elapsedMs / 1000, userWeightKg)
                : Utils.calculateCalories(stepCount, userWeightKg);

        String distStr  = String.format(Locale.getDefault(), "%.2f", totalDistanceKm);
        String timeStr  = formatTime(elapsedMs);
        String calStr   = String.valueOf(calories);
        String paceStr  = calcPaceString();
        String bpmStr   = currentBPM > 0 ? String.valueOf(currentBPM) : "0";
        String dateStr  = Utils.getTodayDate();
        String streakStr = String.valueOf(currentStreak);

        RunItem run = new RunItem(distStr, timeStr, calStr, streakStr,
                paceStr, bpmStr, dateStr, null);

        fbs.addRun(run, task -> {
            if (!isAdded() || getContext() == null) return;
            if (task.isSuccessful()) {
                Toast.makeText(getContext(),
                        getString(R.string.run_saved), Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameLayout,
                                RunDetailsFragment.newInstance(run))
                        .addToBackStack(null)
                        .commit();
            } else {
                Toast.makeText(getContext(),
                        getString(R.string.failed_save_run), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Stats update ──────────────────────────────────────

    private void updateAllStats() {
        if (tvTimer    != null) tvTimer.setText(formatTime(elapsedMs));

        int calories = totalDistanceKm > 0.1
                ? Utils.calculateCaloriesMET(totalDistanceKm,
                elapsedMs / 1000, userWeightKg)
                : Utils.calculateCalories(stepCount, userWeightKg);
        if (tvCalories != null) tvCalories.setText(String.valueOf(calories));
        if (tvPace     != null) tvPace.setText(calcPaceString());
    }

    private String calcPaceString() {
        if (totalDistanceKm < 0.01 || elapsedMs < 1000) return "--:--";
        long paceMs  = (long)(elapsedMs / totalDistanceKm);
        long paceMin = paceMs / 60000;
        long paceSec = (paceMs % 60000) / 1000;
        return String.format(Locale.getDefault(), "%d:%02d /km", paceMin, paceSec);
    }

    private String formatTime(long ms) {
        long s = ms / 1000;
        long h = s / 3600;
        long m = (s % 3600) / 60;
        long sec = s % 60;
        if (h > 0) return String.format(Locale.getDefault(), "%d:%02d:%02d", h, m, sec);
        return String.format(Locale.getDefault(), "%02d:%02d", m, sec);
    }

    // ── Lifecycle ─────────────────────────────────────────

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        timerHandler.removeCallbacks(timerRunnable);
        if (isRunning) { stopLocationUpdates(); unregisterSensors(); }
        stopBLEScan();
        disconnectBLE();
    }
}