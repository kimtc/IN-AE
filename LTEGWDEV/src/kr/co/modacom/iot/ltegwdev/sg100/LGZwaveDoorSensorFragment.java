package kr.co.modacom.iot.ltegwdev.sg100;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import kr.co.modacom.iot.ltegwdev.R;
import kr.co.modacom.iot.ltegwdev.control.ZWaveController;
import kr.co.modacom.iot.ltegwdev.model.PropertyVO;
import kr.co.modacom.iot.ltegwdev.model.ZwaveDeviceVO;
import kr.co.modacom.iot.ltegwdev.model.type.DevCat;
import kr.co.modacom.iot.ltegwdev.model.type.DevLabel;
import kr.co.modacom.iot.ltegwdev.model.type.DevModel;
import kr.co.modacom.iot.ltegwdev.model.type.FunctionId;
import kr.co.modacom.iot.ltegwdev.model.type.OpenCloseStatus;
import kr.co.modacom.iot.ltegwdev.model.type.Target;
import kr.co.modacom.iot.ltegwdev.onem2m.M2MManager.OnM2MSendListener;

public class LGZwaveDoorSensorFragment extends Fragment {

	private static final String TAG = LGZwaveDoorSensorFragment.class.getSimpleName();

	private TextView tvInstanceId;
	private TextView tvStatus;

	private ZWaveController mZWaveCon;
	private ZwaveDeviceVO mItem;
	
	private Context mCtx;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.lg_fragment_zw_door_sensor, container, false);
		
		if (getActivity() instanceof LGZwaveActivity) {
			mCtx = ((LGZwaveActivity)getActivity()).getContext();
		}
		
		mZWaveCon = ZWaveController.getInstance(mCtx);

		tvInstanceId = (TextView) view.findViewById(R.id.tv_lg_zwave_door_instance_id);
		tvStatus = (TextView) view.findViewById(R.id.tv_lg_zwave_door_status);

		return view;
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		mItem = getItemFromList(true);
		updateZWaveItemStatus(mItem);
	}

	public ZwaveDeviceVO getItem() {
		return mItem;
	}

	public void setItem(ZwaveDeviceVO mItem) {
		this.mItem = mItem;
	}

	public ZwaveDeviceVO getItemFromList(boolean flag) {
		for (ZwaveDeviceVO item : mZWaveCon.getItems()) {
			if (!((LGZwaveActivity)mCtx).isInModelName(item)) {
				mItem = item;
				if (((LGZwaveActivity) mCtx).getUnpairingFlag() == 1) {
					((LGZwaveActivity) mCtx).showUnpairDialog();
				}
				return item;
			} else if (item.getDevCat() == DevCat.ZWAVE.getCode()
					&& item.getModelName().equals(DevModel.IOT_DOOR_WINDOW_SENSOR.getName())
					&& item.getPairingFlag() == flag) {
				return item;
			}
		}
		return null;
	}

	public ZwaveDeviceVO getItemFromList(String deviceId) {
		for (ZwaveDeviceVO item : mZWaveCon.getItems())
			if (item.getDeviceId().equals(deviceId))
				return item;
		return null;
	}

	public void requestDeviceInfo() {
		mItem = getItemFromList(true);
		if (mItem != null)
			mZWaveCon.requestDeviceInfo(mOnM2MSendListener, mItem.getDeviceId(), Target.ZWAVE);
	}

	public void updateModelInfo() {
		Activity activity = getActivity();
		if (activity instanceof LGZwaveActivity) {
			((LGZwaveActivity) activity).updateModelInfo(mItem.getModelInfo().substring(0, 4),
					mItem.getModelInfo().substring(4, 8), mItem.getModelInfo().substring(8, 12));
		}
	}

	public void clearModelInfo() {
		Activity activity = getActivity();
		if (activity instanceof LGZwaveActivity) {
			((LGZwaveActivity) activity).clearModelInfo();
		}
	}

	public void updateZWaveItemStatus(ZwaveDeviceVO item) {
		// TODO Auto-generated method stub
		if (item != null) {
			if (tvInstanceId != null && tvStatus != null) {
				int status = -1;

				if (item.getModelName().contains("Unknown")) {
					return;
				}
				
				for (PropertyVO property : item.getProperties()) {
					if (property.getLabel().equals(DevLabel.ACCESS_CONTROL.getName())) {
						tvInstanceId.setText(Integer.toString(property.getInstanceId()));
						status = (Integer) property.getValue();
						break;
					}
				}

				tvStatus.setText(status == -1 ? ""
						: (status == 0 ? OpenCloseStatus.CLOSE.getName() : OpenCloseStatus.OPEN.getName()));
			}
			
			updateModelInfo();
		} else {
			clearModelInfo();
			clearZWaveItemStatus();
		}
	}

	public void clearZWaveItemStatus() {
		// TODO Auto-generated method stub
		mItem = null;

		if (tvInstanceId != null)
			tvInstanceId.setText("");

		if (tvStatus != null)
			tvStatus.setText("");
	}

	public void showProgressDialog(String msg) {
		Activity activity = getActivity();
		if (activity instanceof LGZwaveActivity) {
			((LGZwaveActivity) activity).showProgressDialog(msg);
		}
	}

	public void hideProgressDialog() {
		Activity activity = getActivity();
		if (activity instanceof LGZwaveActivity) {
			((LGZwaveActivity) activity).hideProgressDialog();
		}
	}

	public void updateZWaveState(FunctionId funcId) {
		if (mZWaveCon == null) {
			return;
		}

		switch (funcId) {
		case DEVICE_PAIRING:
			hideProgressDialog();
			mItem = getItemFromList(true);
			updateZWaveItemStatus(mItem);
			break;
		case DEVICE_UNPAIRING:
			hideProgressDialog();
			mItem = getItemFromList(mItem.getDeviceId());
			updateZWaveItemStatus(null);
			break;
		case DEVICE_INFO:
			mItem = getItemFromList(true);
			updateZWaveItemStatus(mItem);
			break;
		case DEVICE_CONTROL:
		case UPDATED_DEVICE:
			mItem = getItemFromList(mItem.getDeviceId());
			updateZWaveItemStatus(mItem);
			break;
		default:
			break;
		}
	}

	protected OnM2MSendListener mOnM2MSendListener = new OnM2MSendListener() {

		@Override
		public void onPreSend() {
			showProgressDialog("메시지 전송 요청 중...");
		}

		@Override
		public void onPostSend() {
		}

		@Override
		public void onCancelled() {
			hideProgressDialog();
		}
	};
}
