package com.qupai.lib_printer.hiti_u826;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.blankj.utilcode.util.ImageUtils;
import com.uni.usb.printer.PrintPara;
import com.uni.usb.printer.PrintPara.PaperSize;
import com.uni.usb.printer.PrinterJob;
import com.uni.usb.service.Action;
import com.uni.usb.service.ErrorCode;
import com.uni.usb.service.ServiceConnector;

import java.io.InputStream;

public class PrinterOperation {

	private ServiceConnector serviceConnector;
	private Context context;

	// printer job id
	private int mJobId;
	public short		MATTE, PRINTCOUNT, PAPERSIZE, PRINTMODE, PAPERTYPE;
	public int PaperType;
	public String m_strTablesRoot;
	public boolean bFinalPage;

	public PrinterOperation(Context context, ServiceConnector serviceConnector) {
		mJobId = 0;
		MATTE = 0;
		PRINTCOUNT = 1;
		PRINTMODE = 1;
		PAPERTYPE = 0;
		PAPERSIZE = 2;
		PAPERTYPE = 0; //0:Photo, 1:Transparency, 2:Metallic, 3:High definition
		bFinalPage = true;
		m_strTablesRoot= "";
		this.context = context;
		this.serviceConnector = serviceConnector;
	}

	public ErrorCode updateFirmware(String version, String path) {
		return serviceConnector.updateFirmware(version, path);
	}

	public ErrorCode updateFirmwareP525N(String version, String path, String bootpath, String kernelpath) {
		return serviceConnector.updateFirmwareP525N(version, path, bootpath, kernelpath);
	}
	// ----------------------------------------------------------------------------
	// com.qupai.lib_printer.renwoyin.bean.Printer operation call, it is a block call and should not be execute by main thread.
	//---------------------------------------------------------------------------------

	public PrinterJob getPrinterStatus() {

		PrinterJob job = printerService(Action.USB_CHECK_PRINTER_STATUS);
		return job;
	}

	public PrinterJob getPrintCount() {

		PrinterJob job = printerService(Action.USB_DEVICE_PRINT_COUNT);
		return job;
	}

	public PrinterJob getRibbonInfo() {

		PrinterJob job = printerService(Action.USB_DEVICE_RIBBON_INFO);
		return job;
	}

	public PrinterJob getFirmwareVersion() {

		PrinterJob job = printerService(Action.USB_DEVICE_FW_VERSION);
		return job;
	}

	public PrinterJob getModelName() {

		PrinterJob job = printerService(Action.USB_DEVICE_MODEL_NAME);
		return job;
	}

	public PrinterJob getSerialNumber() {

		PrinterJob job = printerService(Action.USB_DEVICE_SERIAL_NUM);
		return job;
	}

	public PrinterJob resetPrinter() {

		PrinterJob job = printerService(Action.USB_COMMAND_RESET_PRINTER);
		return job;
	}

	public PrinterJob resumeJob() {

		PrinterJob job = printerService(Action.USB_COMMAND_CLEAN_PAPER_PATH);//USB_COMMAND_RESUME_JOB);
		return job;
	}

	public PrinterJob ejectPaperJam() {

		PrinterJob job = printerService(Action.USB_EJECT_PAPER_JAM);
		return job;
	}

	public PrinterJob cleanPaperPath() {

		PrinterJob job = printerService(Action.USB_COMMAND_CLEAN_PAPER_PATH);//USB_CLEAN_PAPER_PATH);
		return job;
	}

	public PrinterJob PrintPhotosStart() {

		PrinterJob job = printerService(Action.USB_PRINT_PHOTOS_START);
		return job;
	}

	public PrinterJob print(String photoPath) {

		PrinterJob job = printerSetService(Action.USB_PRINT_PHOTOS, photoPath);
		return job;
	}
	public PrinterJob PrintPhotosEnd() {

		PrinterJob job = printerService(Action.USB_PRINT_PHOTOS_END);
		return job;
	}

	public PrinterJob getStorageID() {

		PrinterJob job = printerService(Action.USB_GET_STORAGE_ID);
		return job;
	}

	public PrinterJob getObjectNumber(long storageId, byte format, long objectId) {

		PrinterJob job = printerObjectService(Action.USB_GET_OBJECT_NUMBER, storageId, format, objectId);
		return job;
	}

	/**
	 * @param storageId
	 * @param format		2: jpeg, 3: album,
	 * @param objectId
	 * @return
	 */
	public PrinterJob getObjectHandleId(long storageId, byte format, long objectId) {

		PrinterJob job = printerObjectService(Action.USB_GET_OBJECT_HANDLE_ID, storageId, format, objectId);
		return job;
	}

	public PrinterJob getObjectInfo(long storageId, long objectId) {

		PrinterJob job = printerObjectService(Action.USB_GET_OBJECT_INFO, storageId, (byte)0x00, objectId);
		return job;
	}

	/**
	 *
	 * @param storageId
	 * @param objectId
	 * @param type  0x01: original photo, 0x02: thumbnail
	 * @return
	 */
	public PrinterJob getObjectData(long storageId, long objectId, byte type) {

		PrinterJob job = printerObjectService(Action.USB_GET_OBJECT_DATA, storageId, type, objectId);
		return job;
	}

	public PrinterJob getJobInQueueNumber() {

		PrinterJob job = printerService(Action.USB_DEVICE_JOB_IN_QUEUE);
		return job;
	}

	//==========================================================================================

	/**
	 * Get bitmap from res\drawable
	 */
	private Bitmap getBitmap(String name) {

		int id = context.getResources().getIdentifier(name, "drawable", context.getPackageName());

		InputStream is = context.getResources().openRawResource(id);
		return BitmapFactory.decodeStream(is, null , null);
	}

	/**
	 * Set com.qupai.lib_printer.renwoyin.bean.Printer operation parameter.
	 *
	 * Please be noticed that below parameter can be customize but can't not be null for specific printer operation.
	 */
	private Object getPrinterPara(Action action, Object data) {

		Object attr =  null;
		switch(action) {

			case USB_PRINT_PHOTOS:

				/**
				 * Paper size / photo pixels match table
				 *
				 * +-----------------------------+------------+
				 * |  Paper size                 |  pixels    |
				 * +-----------------------------+------------+
				 * | PAPER_SIZE_6X4_PHOTO        | 1844x1240  |
				 * +-----------------------------+------------+
				 * | PAPER_SIZE_6X8_PHOTO        | 1844x2434  |
				 * +-----------------------------+------------+
				 * | PAPER_SIZE_6X9_PHOTO        | 1844x2740  |
				 * +-----------------------------+------------+
				 * | PAPER_SIZE_6X8_2UP    		 | 1844x1240  |
				 * * +-----------------------------+------------+
				 * | PAPER_SIZE_6X8_6x4_2SPLIT   | 1844x1240  |
				 * * +-----------------------------+------------+
				 * | PAPER_SIZE_6X8_6x4_3SPLIT   | 1844x1240  |
				 * +-----------------------------+------------+
				 * | PAPER_SIZE_5X7_PHOTO        | 1548x2140  |
				 * +-----------------------------+------------+
				 * | PAPER_SIZE_6X4_SPLIT_2UP    | 1240x1844  |
				 * * +-----------------------------+------------+
				 * | PAPER_SIZE_6X4_SPLIT_3UP    | 1240x1844  |
				 * +-----------------------------+------------+
				 * | PAPER_SIZE_5X7_SPLIT_2UP    | 1548x2152  |
				 * +-----------------------------+------------+
				 */

				//-------------------------------------------------------
				// photo printer test
				//-------------------------------------------------------

				Log.i("getPrinterPara", "getPrinterPara bitmap: " + data.toString());
				Bitmap bitmap = ImageUtils.getBitmap(data.toString()); //getBitmap("pic1844x1240");

				if(bitmap == null) {

					mHandler.post(new Runnable(){

						@Override
						public void run() {
							// TODO Auto-generated method stub
							Toast.makeText(context, "not found bitmap", Toast.LENGTH_SHORT).show(); // test
						}

					});
				}else {
					Log.i("size:", "getPrinterPara: "+PAPERSIZE);
					switch(PAPERSIZE) {
						case 2://6x4
							attr = PrintPara.getPrintPhotoPara(bitmap, PRINTCOUNT, MATTE, PRINTMODE, PaperSize.PAPER_SIZE_6X4_PHOTO, m_strTablesRoot, PAPERTYPE, bFinalPage);
							break;

						case 3://5x7
							attr = PrintPara.getPrintPhotoPara(bitmap, PRINTCOUNT, MATTE, PRINTMODE, PaperSize.PAPER_SIZE_5X7_PHOTO, m_strTablesRoot, PAPERTYPE, bFinalPage);
							break;

						case 4://6x8
							attr = PrintPara.getPrintPhotoPara(bitmap, PRINTCOUNT, MATTE, PRINTMODE, PaperSize.PAPER_SIZE_6X8_PHOTO, m_strTablesRoot, PAPERTYPE, bFinalPage);
							break;

						case 5://6x4 2 splits
							attr = PrintPara.getPrintPhotoPara(bitmap, PRINTCOUNT, MATTE, PRINTMODE, PaperSize.PAPER_SIZE_6X4_SPLIT_2UP, m_strTablesRoot, PAPERTYPE, bFinalPage);
							break;

						case 6://6x4 3 splits
							attr = PrintPara.getPrintPhotoPara(bitmap, PRINTCOUNT, MATTE, PRINTMODE, PaperSize.PAPER_SIZE_6X4_SPLIT_3UP, m_strTablesRoot, PAPERTYPE, bFinalPage);
							break;

						case 7: //6x8 2up/6x4
							attr = PrintPara.getPrintPhotoPara(bitmap, PRINTCOUNT, MATTE, PRINTMODE, PaperSize.PAPER_SIZE_6X8_2UP, m_strTablesRoot, PAPERTYPE, bFinalPage);
							break;

						case 8: //6x8 for 6x4 2splits
							attr = PrintPara.getPrintPhotoPara(bitmap, PRINTCOUNT, MATTE, PRINTMODE, PaperSize.PAPER_SIZE_6X8_6x4_2SPLIT, m_strTablesRoot, PAPERTYPE, bFinalPage);
							break;

						case 9: //6x8 for 6x4 3splits
							attr = PrintPara.getPrintPhotoPara(bitmap, PRINTCOUNT, MATTE, PRINTMODE, PaperSize.PAPER_SIZE_6X8_6x4_3SPLIT, m_strTablesRoot, PAPERTYPE, bFinalPage);
							break;

						default:
							attr = PrintPara.getPrintPhotoPara(bitmap, PRINTCOUNT, MATTE, PRINTMODE, PaperSize.PAPER_SIZE_6X4_PHOTO, m_strTablesRoot, PAPERTYPE, bFinalPage);
							break;
					}
				}

				break;

			case USB_SET_AUTO_POWER_OFF:

				if(data != null && data instanceof Short) attr = PrintPara.getSetCommandPara((short)data);
				break;

			default:
		}

		return attr;
	}

	Handler mHandler = new Handler();

	/**
	 * call printer service and print service error code if operation not success to execute
	 */
	private PrinterJob printerService(Action action) {

		PrinterJob job = null;

		if(action != null) {
			job = new PrinterJob(mJobId, action);
			/*if(action == Action.USB_PRINT_PHOTOS_START)
			{
				mJobId = 0;
				job = new PrinterJob(mJobId, action);
			}
			else
				job = new PrinterJob(-1, action);*/
			serviceConnector.m_strTablesRoot = m_strTablesRoot;
			serviceConnector.doService(job);
		}

		return  job;
	}

	/**
	 * call printer service and print service error code if operation not success to execute
	 */
	private PrinterJob printerSetService(Action action, Object data) {

		PrinterJob job = null;
		if(action != null) {
			job = new PrinterJob(mJobId++, action).setJobPara(getPrinterPara(action, data));
			/*if(action == Action.USB_PRINT_PHOTOS)
			{
				mJobId++;
				job = new PrinterJob(mJobId, action).setJobPara(getPrinterPara(action, data));
			}
			else
				job = new PrinterJob(-2, action).setJobPara(getPrinterPara(action, data));*/
			serviceConnector.doService(job);
		}
		return  job;
	}


	/**
	 * call printer service and print service error code if operation not success to execute
	 */
	private PrinterJob printerObjectService(Action action, long storageId, byte format, long handleId) {

		PrinterJob job = null;

		if(action != null) {
			job = new PrinterJob(mJobId++, action).setJobPara(getObjectPara(action, storageId, format, handleId));
			serviceConnector.doService(job);
		}

		return  job;
	}

	private Object getObjectPara(Action action, long storageId, byte format, long handleId) {

		Object attr = null;

		switch (action) {

			case USB_GET_OBJECT_NUMBER:
			case USB_GET_OBJECT_HANDLE_ID:
			case USB_GET_OBJECT_INFO:
			case USB_GET_OBJECT_DATA:
				attr = PrintPara.getGetObjectValue(storageId, format, handleId);
				break;
			default:
		}

		return attr;
	}
}
