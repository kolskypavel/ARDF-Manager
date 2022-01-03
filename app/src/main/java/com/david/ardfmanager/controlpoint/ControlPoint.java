package com.david.ardfmanager.controlpoint;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class ControlPoint implements Serializable {

        private String name;    //Name of the CP (e.g F1, F1, 4, 3)
        private int code;       //The SI code of the CP
        private int type;       //The type of the CP: 0-classic, 1-beacon, 2-spectator

        public ControlPoint(String name, int code, int type) {
                this.name = name;
                this.code = code;
                this.type = type;
        }

    //method for JSON saving
    public JSONObject ControlPointToJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("number", this.name);
        json.put("code", this.code);
        json.put("type", this.type);
        return json;
    }


    //getters and setters
        public String getName() {
            return name;
        }
        public void setName(String name) {
                this.name = name;
        }
        public int getCode() {
            return code;
        }
        public void setCode(int code) {
                this.code = code;
        }
        public int getType() {
        return type;
    }
        public void setType(int type) {
        this.type = type;
    }
}
