package network;

public class ResponseParser {
    int type;
    private String time, device, data;

//	public void parser(String response) throws JSONException {
//		System.out.println("response " + response);
//
//		JSONObject jsonObject;
//		JSONArray jsonArrayData;
//		try {
//			jsonObject = new JSONObject(response);
//		} catch (JSONException e) {
//			return;
//		}
//
//		String str = null;
//		int type = -1;
//		// ----------get id device------------
//
//		try {
//			str = jsonObject.getString("device");
//		} catch (JSONException e) {
//			str = null;
//		}
//		if (str != null) {
//			setDevice(str);
//		} else {
//			setDevice(null);
//		}
//
//		// ----------get time event------------
//
//		try {
//			str = jsonObject.getString("time");
//		} catch (JSONException e) {
//			str = null;
//		}
//		if (str != null) {
//			setTime(str);
//		} else {
//			setTime(null);
//		}
//		// ----------type data------------
//		try {
//			type = Integer.parseInt(jsonObject.getString("type"));
//		} catch (JSONException e) {
//			type = -1;
//		}
//		if (type != -1) {
//			setType(type);
//		} else {
//			setType(-1);
//
//		}
//		// ----------data------------
//		jsonArrayData = jsonObject.getJSONArray("data");
//		System.out.println("jsonArrayData" + jsonArrayData);
//
//		switch (type) {
//		case 1:
//			str = jsonArrayData.get(1).toString();
//			break;
//		case 2:
////			Location loc = new Location (jsonArrayData.get(1).toString());
//
//			break;
//		case 3:
//
//			break;
//		default:
//			break;
//		}
//
//	}

    private void setTime(String time) {
        // TODO Auto-generated method stub
        this.time = time;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getDevice() {
        return this.device;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return this.type;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getData() {
        return this.data;
    }

}
