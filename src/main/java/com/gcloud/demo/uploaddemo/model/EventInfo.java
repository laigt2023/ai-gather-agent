package com.gcloud.demo.uploaddemo.model;

public class EventInfo {
    /*
    *{
	"event_id": "2ac6754d-bb0b-42e9-a73b-46328aa7aa2f",
	"event_state": 0,
	"device_name": "evm220",
	"device_id": "318b4b04-d8ee-599b-b1b0-a0c6dd13008c",
	"task_name": "塔吊履职管理v9（推流）",
	"task_id": "f9417580-9e1d-4eda-a16e-f3cc0cb6b15c",
	"app_name": "塔吊履职管理v9",
	"app_id": "100002910",
	"src_name": "output_000.mp4",
	"src_id": "7b97cec3-5680-4108-885c-392ba6ebc327",
	"created": 1681896750,
	"details": [{
		"frame_id": 429,
		"model_id": "471911845052653568",
		"model_name": "04-14/TD_safety_officer",
		"model_thres": 0.4000000059604645,
		"model_type": 1,
		"regions": [
			[
				[236, 97],
				[276, 97],
				[275, 144],
				[237, 144]
			],
			[
				[1034, 569],
				[1037, 669],
				[1135, 666],
				[1130, 566]
			]
		],
		"targets": [{
			"box": {
				"left_top_x": 243.09017944335938,
				"left_top_y": 101.63827514648438,
				"right_bottom_x": 264.0901794433594,
				"right_bottom_y": 131.63827514648438
			},
			"color": [255, 0, 0, 1],
			"distance": 0,
			"id": 0,
			"label": "TD_Number",
			"prev_id": 7,
			"prob": 0.682,
			"roi_id": 0
		}, {
			"box": {
				"left_top_x": 1070.9210205078125,
				"left_top_y": 566.2862548828125,
				"right_bottom_x": 1104.6710205078125,
				"right_bottom_y": 641.2862548828125
			},
			"color": [255, 255, 0, 1],
			"distance": 0,
			"id": 0,
			"label": "TD_DG_working",
			"prev_id": 0,
			"prob": 0.7683,
			"roi_id": 0
		}]
	}]
}
    * */
    private String event_id;
    private int event_state;
    private String device_name;
    private String device_id;

    private String task_name;
    private String task_id;
    private String app_name;
    private String app_id;
    private String src_name;
    private String src_id;
    private long created;
    private String details;

    public String getEvent_id() {
        return event_id;
    }

    public void setEvent_id(String event_id) {
        this.event_id = event_id;
    }

    public int getEvent_state() {
        return event_state;
    }

    public void setEvent_state(int event_state) {
        this.event_state = event_state;
    }

    public String getDevice_name() {
        return device_name;
    }

    public void setDevice_name(String device_name) {
        this.device_name = device_name;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getTask_name() {
        return task_name;
    }

    public void setTask_name(String task_name) {
        this.task_name = task_name;
    }

    public String getTask_id() {
        return task_id;
    }

    public void setTask_id(String task_id) {
        this.task_id = task_id;
    }

    public String getApp_name() {
        return app_name;
    }

    public void setApp_name(String app_name) {
        this.app_name = app_name;
    }

    public String getApp_id() {
        return app_id;
    }

    public void setApp_id(String app_id) {
        this.app_id = app_id;
    }

    public String getSrc_name() {
        return src_name;
    }

    public void setSrc_name(String src_name) {
        this.src_name = src_name;
    }

    public String getSrc_id() {
        return src_id;
    }

    public void setCreated(int created) {
        this.created = created;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }
}
