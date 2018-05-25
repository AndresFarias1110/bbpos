package com.example.andresfarias.bbpos;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.bbpos.bbdevice.BBDeviceController;
import com.bbpos.bbdevice.BBDeviceController.CurrencyCharacter;
import com.bbpos.bbdevice.CAPK;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "BBDeviceController";
    protected static final String[] DEVICE_NAMES = new String[] { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
    public static List<BluetoothDevice> foundDevices;
    public MainActivity currentActivity = this;
    public BBDeviceController bbDeviceController;
    public ArrayAdapter<String> mArrayDevices;
    private static BBDeviceController.CheckCardMode checkCardMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Spinner spinnerDevices = (Spinner) findViewById(R.id.devices);

        bbDeviceController = BBDeviceController.getInstance(this,new ListenerBBPos());
        BBDeviceController.setDebugLogEnabled(true);
        bbDeviceController.setDetectAudioDevicePlugged(true);

        bbDeviceController.initSession("2BC1EF345F564C7C");


        bbDeviceController.startBTScan(DEVICE_NAMES, 120);

        if (checkBluetoothPermission()){

            mArrayDevices = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

            spinnerDevices.setAdapter(mArrayDevices);

            spinnerDevices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Log.d(TAG, "onItemClick: " + foundDevices.get(i).getName());
                    connectBT(foundDevices.get(i));
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

        }else{
            Toast.makeText(this, "No tienes permisos de bluetooth", Toast.LENGTH_SHORT).show();
        }

    }

    public void connectBT(BluetoothDevice device){
        bbDeviceController.connectBT(device);
    }

    public void startEmv(BBDeviceController.CheckCardMode checkCardMode, String orderID, String randomNumber) {

        Hashtable<String, Object> data = new Hashtable<String, Object>();

        data.put("emvOption", BBDeviceController.EmvOption.START);
        data.put("checkCardMode", checkCardMode);
        data.put("orderID", orderID);
        data.put("randomNumber", randomNumber);

        Log.d(TAG, "startEmv: checkCardMode:" + checkCardMode);

        String terminalTime = new SimpleDateFormat("yyMMddHHmmss").format(Calendar.getInstance().getTime());
        data.put("terminalTime", terminalTime);

        bbDeviceController.startEmv(data);
    }

    public void promptForAmount(String amount, String cashbackAmount){

        BBDeviceController.TransactionType transactionType = BBDeviceController.TransactionType.PAYMENT;

        String currencyCode = "484";

        bbDeviceController.setAmount(amount, cashbackAmount, currencyCode, transactionType, new CurrencyCharacter[]{CurrencyCharacter.M, CurrencyCharacter.X, CurrencyCharacter.N});
    }


    protected boolean checkBluetoothPermission() {
        if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED)) {
            return true;
        } else {
            return false;
        }
    }


    public class ListenerBBPos implements BBDeviceController.BBDeviceControllerListener{
        private String TAG = "ListenerBBPos";


        @Override
        public void onWaitingForCard(BBDeviceController.CheckCardMode checkCardMode) {
            switch (checkCardMode) {
                case INSERT:
                    Log.d(TAG, "onWaitingForCard: please_insert_card" );
                    break;
                case SWIPE:
                    Log.d(TAG, "onWaitingForCard: please_swipe_card");
                    break;
                case SWIPE_OR_INSERT:
                    Log.d(TAG, "onWaitingForCard: please_swipe_insert_card");
                    break;
                case TAP:
                    Log.d(TAG, "onWaitingForCard: please_tap_card");
                    break;
                case SWIPE_OR_INSERT_OR_TAP:
                    Log.d(TAG, "onWaitingForCard: please_swipe_insert_tap_card");
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onWaitingReprintOrPrintNext() {

        }

        @Override
        public void onBTReturnScanResults(List<BluetoothDevice> list) {
            //Log.d(TAG, "onBTReturnScanResults: "+ list.toString());

            foundDevices = list;

            if (mArrayDevices != null && foundDevices != null){
                mArrayDevices.clear();
                for (int i = 0; i < foundDevices.size(); i++) {
                    Log.d(TAG, "onBTReturnScanResults: " + foundDevices.get(i).getName());
                    mArrayDevices.add(foundDevices.get(i).getName());
                }
                mArrayDevices.notifyDataSetChanged();
            }

        }

        @Override
        public void onBTScanTimeout() {
            Log.d(TAG, "onBTScanTimeout: Bluetooth finished scan");
        }

        @Override
        public void onBTScanStopped() {
            Log.d(TAG, "onBTScanStopped: stop scan devices");
        }

        @Override
        public void onBTConnected(BluetoothDevice bluetoothDevice) {
            Log.d(TAG, "onBTConnected: " + bluetoothDevice.getName());

            /*bbDeviceController.getDeviceInfo();
            bbDeiceController.getEmvCardNumber();
            //Programing transaction
            */
            startEmv(BBDeviceController.CheckCardMode.SWIPE_OR_INSERT, "0123456789ABCDEF0123456789ABCDEF", "012345");
        }

        @Override
        public void onBTDisconnected() {
            Log.d(TAG, "onBTDisconnected: ");
        }

        @Override
        public void onUsbConnected() {

        }

        @Override
        public void onUsbDisconnected() {

        }

        @Override
        public void onSerialConnected() {

        }

        @Override
        public void onSerialDisconnected() {

        }

        @Override
        public void onReturnCheckCardResult(BBDeviceController.CheckCardResult checkCardResult, Hashtable<String, String> hashtable) {
            Log.d(TAG, "onReturnCheckCardResult: " + hashtable.toString());
        }

        @Override
        public void onReturnCancelCheckCardResult(boolean b) {

        }

        @Override
        public void onReturnDeviceInfo(Hashtable<String, String> hashtable) {
            Log.d(TAG, "onReturnDeviceInfo: " + hashtable.toString());
        }

        @Override
        public void onReturnTransactionResult(BBDeviceController.TransactionResult transactionResult) {
            Log.d(TAG, "onReturnTransactionResult: " + transactionResult.toString());

            if (transactionResult == BBDeviceController.TransactionResult.APPROVED){
                Toast.makeText(MainActivity.this, "APROVADO", Toast.LENGTH_LONG).show();
            }else if(transactionResult == BBDeviceController.TransactionResult.DECLINED){
                Toast.makeText(MainActivity.this, "RECHAZADO", Toast.LENGTH_LONG).show();
            }else if(transactionResult == BBDeviceController.TransactionResult.CANCELED){
                Toast.makeText(MainActivity.this, "CANCELADO", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onReturnBatchData(String tlv) {
            Log.d(TAG, "onReturnBatchData: " + tlv);

            String content = "BATCH DATA" + "\n";

            Hashtable<String, String> decodeData = BBDeviceController.decodeTlv(tlv);

            Object[] keys = decodeData.keySet().toArray();

            Arrays.sort(keys);

            for (Object key : keys) {
                String value = decodeData.get(key);
                content += key + ": " + value + "\n";
            }

            Log.d(TAG, "onReturnBatchData: " + content);
        }

        @Override
        public void onReturnReversalData(String s) {

        }

        @Override
        public void onReturnAmountConfirmResult(boolean b) {

        }

        @Override
        public void onReturnPinEntryResult(BBDeviceController.PinEntryResult pinEntryResult, Hashtable<String, String> hashtable) {

        }

        @Override
        public void onReturnPrintResult(BBDeviceController.PrintResult printResult) {
            Log.d(TAG, "onReturnPrintResult: " + printResult.toString());
        }

        @Override
        public void onReturnAccountSelectionResult(BBDeviceController.AccountSelectionResult accountSelectionResult, int i) {

        }

        @Override
        public void onReturnAmount(Hashtable<String, String> hashtable) {
            Log.d(TAG, "onReturnAmount: " + hashtable.toString());
        }

        @Override
        public void onReturnUpdateAIDResult(Hashtable<String, BBDeviceController.TerminalSettingStatus> hashtable) {

        }

        @Override
        public void onReturnUpdateGprsSettingsResult(boolean b, Hashtable<String, BBDeviceController.TerminalSettingStatus> hashtable) {

        }

        @Override
        public void onReturnUpdateTerminalSettingResult(BBDeviceController.TerminalSettingStatus terminalSettingStatus) {

        }

        @Override
        public void onReturnUpdateWiFiSettingsResult(boolean b, Hashtable<String, BBDeviceController.TerminalSettingStatus> hashtable) {

        }

        @Override
        public void onReturnReadAIDResult(Hashtable<String, Object> hashtable) {

        }

        @Override
        public void onReturnReadGprsSettingsResult(boolean b, Hashtable<String, Object> hashtable) {

        }

        @Override
        public void onReturnReadTerminalSettingResult(BBDeviceController.TerminalSettingStatus terminalSettingStatus, String s) {

        }

        @Override
        public void onReturnReadWiFiSettingsResult(boolean b, Hashtable<String, Object> hashtable) {
            Log.d(TAG, "onReturnReadWiFiSettingsResult: " + hashtable.toString());
        }

        @Override
        public void onReturnEnableAccountSelectionResult(boolean b) {

        }

        @Override
        public void onReturnEnableInputAmountResult(boolean b) {

        }

        @Override
        public void onReturnCAPKList(List<CAPK> list) {

        }

        @Override
        public void onReturnCAPKDetail(CAPK capk) {
            Log.d(TAG, "onReturnCAPKDetail: " + capk.toString());
        }

        @Override
        public void onReturnCAPKLocation(String s) {
            Log.d(TAG, "onReturnCAPKLocation: " + s);
        }

        @Override
        public void onReturnUpdateCAPKResult(boolean b) {

        }

        @Override
        public void onReturnEmvReportList(Hashtable<String, String> hashtable) {
            Log.d(TAG, "onReturnEmvReportList: " + hashtable.toString());
        }

        @Override
        public void onReturnEmvReport(String s) {
            Log.d(TAG, "onReturnEmvReport: " + s);
        }

        @Override
        public void onReturnDisableAccountSelectionResult(boolean b) {

        }

        @Override
        public void onReturnDisableInputAmountResult(boolean b) {
            Log.d(TAG, "onReturnDisableInputAmountResult: " + b);
        }

        @Override
        public void onReturnPhoneNumber(BBDeviceController.PhoneEntryResult phoneEntryResult, String s) {
            Log.d(TAG, "onReturnPhoneNumber: " + s);
        }

        @Override
        public void onReturnEmvCardDataResult(boolean b, String s) {
            Log.d(TAG, "onReturnEmvCardDataResult: " + s);
        }

        @Override
        public void onReturnEmvCardNumber(boolean b, String s) {
            Log.d(TAG, "onReturnEmvCardNumber: " + s);
        }

        @Override
        public void onReturnEncryptPinResult(boolean b, Hashtable<String, String> hashtable) {

        }

        @Override
        public void onReturnEncryptDataResult(boolean b, Hashtable<String, String> hashtable) {
            Log.d(TAG, "onReturnEncryptDataResult: " + hashtable.toString());
        }

        @Override
        public void onReturnInjectSessionKeyResult(boolean b, Hashtable<String, String> hashtable) {

        }

        @Override
        public void onReturnPowerOnIccResult(boolean b, String s, String s1, int i) {

        }

        @Override
        public void onReturnPowerOffIccResult(boolean b) {

        }

        @Override
        public void onReturnApduResult(boolean b, Hashtable<String, Object> hashtable) {

        }

        @Override
        public void onRequestSelectApplication(ArrayList<String> arrayList) {
            Log.d(TAG, "onRequestSelectApplication: " + arrayList.toString());
        }

        @Override
        public void onRequestSetAmount() {
            Log.d(TAG, "onRequestSetAmount: Requested set amount");
            promptForAmount("2", "0");
        }

        @Override
        public void onRequestPinEntry(BBDeviceController.PinEntrySource pinEntrySource) {

        }

        @Override
        public void onRequestOnlineProcess(String s) {
            Log.d(TAG, "onRequestOnlineProcess: " + s);
        }

        @Override
        public void onRequestTerminalTime() {
            Log.d(TAG, "onRequestTerminalTime: ");
        }

        @Override
        public void onRequestDisplayText(BBDeviceController.DisplayText displayText) {
            Log.d(TAG, "onRequestDisplayText: " + displayText);

        }

        @Override
        public void onRequestDisplayAsterisk(int i) {

        }

        @Override
        public void onRequestDisplayLEDIndicator(BBDeviceController.ContactlessStatus contactlessStatus) {

        }

        @Override
        public void onRequestProduceAudioTone(BBDeviceController.ContactlessStatusTone contactlessStatusTone) {

        }

        @Override
        public void onRequestClearDisplay() {

        }

        @Override
        public void onRequestFinalConfirm() {
            Log.d(TAG, "onRequestFinalConfirm: true");
            bbDeviceController.sendFinalConfirmResult(true);
            //bbDeviceController.getEmvCardData();
        }

        @Override
        public void onRequestPrintData(int i, boolean b) {

        }

        @Override
        public void onPrintDataCancelled() {

        }

        @Override
        public void onPrintDataEnd() {

        }

        @Override
        public void onBatteryLow(BBDeviceController.BatteryStatus batteryStatus) {

        }

        @Override
        public void onAudioDevicePlugged() {

        }

        @Override
        public void onAudioDeviceUnplugged() {

        }

        @Override
        public void onError(BBDeviceController.Error error, String s) {
            
            Log.d(TAG, "onError: " + error);
            
            if (error == BBDeviceController.Error.INPUT_INVALID_FORMAT){
                Log.d(TAG, "onError: INPUT_INVALID_FORMAT");
            } else if(error == BBDeviceController.Error.INPUT_INVALID){
                Log.d(TAG, "onError: INPUT_INVALID");
            }else if(error == BBDeviceController.Error.CASHBACK_NOT_SUPPORTED){
                Log.d(TAG, "onError: CASHBACK_NOT_SUPPORTED");
            }else if (error == BBDeviceController.Error.DEVICE_BUSY){
                Log.d(TAG, "onError: BUSY");
                Toast.makeText(MainActivity.this, "DISPOSITIVO OCUPADO", Toast.LENGTH_SHORT).show();
            }
            
        }

        @Override
        public void onSessionInitialized() {
            Log.d(TAG, "onSessionInitialized: ");
        }

        @Override
        public void onSessionError(BBDeviceController.SessionError sessionError, String s) {

        }

        @Override
        public void onAudioAutoConfigProgressUpdate(double v) {

        }

        @Override
        public void onAudioAutoConfigCompleted(boolean b, String s) {

        }

        @Override
        public void onAudioAutoConfigError(BBDeviceController.AudioAutoConfigError audioAutoConfigError) {

        }

        @Override
        public void onNoAudioDeviceDetected() {

        }

        @Override
        public void onDeviceHere(boolean b) {
            Log.d(TAG, "onDeviceHere: " + b);
        }

        @Override
        public void onPowerDown() {

        }

        @Override
        public void onPowerButtonPressed() {

        }

        @Override
        public void onDeviceReset() {

        }

        @Override
        public void onEnterStandbyMode() {

        }

        @Override
        public void onReturnNfcDataExchangeResult(boolean b, Hashtable<String, String> hashtable) {

        }

        @Override
        public void onReturnNfcDetectCardResult(BBDeviceController.NfcDetectCardResult nfcDetectCardResult, Hashtable<String, Object> hashtable) {

        }

        @Override
        public void onReturnControlLEDResult(boolean b, String s) {

        }

        @Override
        public void onReturnVasResult(BBDeviceController.VASResult vasResult, Hashtable<String, Object> hashtable) {

        }

        @Override
        public void onRequestStartEmv() {
            Log.d(TAG, "onRequestStartEmv: ");
        }

        @Override
        public void onBarcodeReaderConnected() {

        }

        @Override
        public void onBarcodeReaderDisconnected() {

        }

        @Override
        public void onReturnBarcode(String s) {

        }
    }

}
