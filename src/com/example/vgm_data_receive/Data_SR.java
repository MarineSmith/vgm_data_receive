package com.example.vgm_data_receive;

import java.io.UnsupportedEncodingException;

import android.content.Context;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;

public class Data_SR {
	
	private UsbManager mUsbManager;
	private UsbDevice mUsbDevice;
	private Context context;
	private TextView tv1;
	private UsbInterface mUsbInterface;
	private UsbEndpoint[] mUsbEndpoint;
	private UsbDeviceConnection mUsbDeviceConnection;
	int ret;
	boolean init = true;
	boolean latch = false;
	
	public Data_SR(UsbManager mUsbManager,UsbDevice mUsbDevice,Context context,TextView tv1){
		this.mUsbManager = mUsbManager;
		this.mUsbDevice = mUsbDevice;
		this.context = context;
		this.tv1=tv1;
	}
	
	public boolean initialize(){
		mUsbInterface = mUsbDevice.getInterface(0);if(mUsbInterface==null)return false;
		mUsbEndpoint = new UsbEndpoint[mUsbInterface.getEndpointCount()];
		mUsbEndpoint[0] = mUsbInterface.getEndpoint(0);if((mUsbEndpoint[0].getType()!=UsbConstants.USB_ENDPOINT_XFER_BULK)||(mUsbEndpoint[0].getDirection()!=UsbConstants.USB_DIR_IN))return false;
		mUsbEndpoint[1] = mUsbInterface.getEndpoint(1);if((mUsbEndpoint[1].getType()!=UsbConstants.USB_ENDPOINT_XFER_BULK)||(mUsbEndpoint[1].getDirection()!=UsbConstants.USB_DIR_OUT))return false;
		mUsbDeviceConnection = mUsbManager.openDevice(mUsbDevice);
		if(mUsbDeviceConnection==null)return false;
		if(!mUsbDeviceConnection.claimInterface(mUsbInterface, true))return false;
		new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				ret = mUsbDeviceConnection.controlTransfer(0x40, 0, 0, 1, null, 0, 0);if(ret<0)init=false;
				ret = mUsbDeviceConnection.controlTransfer(0x40, 0, 1, 1, null, 0, 0);if(ret<0)init=false;
				ret = mUsbDeviceConnection.controlTransfer(0x40, 0, 2, 1, null, 0,0);if(ret<0)init=false;
				ret = mUsbDeviceConnection.controlTransfer(0x40, 0x02, 0x0000, 1, null, 0, 0);if(ret<0)init=false;
				ret = mUsbDeviceConnection.controlTransfer(0x40, 0x03, 0x001a, 1, null, 0, 0);if(ret<0)init=false;
				ret = mUsbDeviceConnection.controlTransfer(0x40, 0x04, 0x0008, 1, null, 0, 0);if(ret<0)init=false;
			}}).start();
		return init;
	}
	
	public void data_read(){
		new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				
			}}).start();
	}
	
	public void read_data(){
		new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				while(latch){
					try{
						Message msg = new Message();
						byte[] buffer = new byte[34];
						int ret = mUsbDeviceConnection.bulkTransfer(mUsbEndpoint[1], new byte[]{0x03,0x03,0x03,0x03,0x03,0x03}, 6, 0);
						if(ret<0){
							msg.what=3;
							handler.sendMessage(msg);
						}
						
						Thread.sleep(500);
						
						ret = mUsbDeviceConnection.bulkTransfer(mUsbEndpoint[0], buffer, 34, 0);
						if(ret>0){
							msg.what=1;
							msg.obj=buffer;
							handler.sendMessage(msg);
						}
					}catch(Exception e){
						e.printStackTrace();
					}
					
				}
			}}).start();
	}
	
	
	public Handler handler = new Handler(){
		public void handleMessage(Message msg){
			switch(msg.what){
			case 3:
				Toast.makeText(context, "oops something wrong", Toast.LENGTH_SHORT).show();
			case 1:
				byte[] buffer = (byte[])msg.obj;
				StringBuilder SB = new StringBuilder();
					for(int i = 0;i<buffer.length;i++){
						SB.append("|"+buffer[i]);
					}
					//tv1.setText(SB);
				float div;
				float tol_ = ((int)(buffer[31] & 0xff))+((int)(buffer[30] & 0xff))*255+((int)(buffer[29] & 0xff))*65535;
				if(buffer[27]>=8){
					div = (int) Math.pow((int)10, ((int)buffer[27])-8);
				}else{
					div = (int) Math.pow((int)10, (int)buffer[27]);
				}
				tv1.setText(""+(float)(tol_/div));
						
			}
			super.handleMessage(msg);
		}
	};
}
